package com.frescuratotal.erp.sales.model;

import com.frescuratotal.erp.inventory.model.Product;
import com.frescuratotal.erp.inventory.model.ProductDao;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;

import javax.imageio.ImageIO;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.NumberFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HexFormat;
import java.util.Locale;

/**
 * Builds the simulated DIAN data of an invoice (CUFE and QR link)
 * and draws the invoice as a PNG image inside the "invoices" folder.
 */
public class InvoiceGenerator {
    private static final String OUTPUT_FOLDER = "invoices";
    private static final String DIAN_QR_URL = "https://catalogo-vpfe.dian.gov.co/document/searchqr?documentkey=";
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final NumberFormat MONEY = NumberFormat.getCurrencyInstance(Locale.of("es", "CO"));

    private static final int WIDTH = 1000;
    private static final int MARGIN = 50;
    private static final int ROW_HEIGHT = 28;
    private static final int QR_SIZE = 170;

    private final ProductDao productDao;

    public InvoiceGenerator(ProductDao productDao) {
        this.productDao = productDao;
    }

    /**
     * Simulated CUFE: a SHA-384 hash (like the real DIAN one) of the invoice key data.
     * It is unique because the invoice number and date never repeat.
     */
    public String generateCufe(String invoiceNumber, LocalDateTime issueDate, Sale sale, String companyNit) {
        String data = invoiceNumber
                + issueDate.format(DATE_FORMAT)
                + sale.getSubtotal()
                + sale.getTotalTax()
                + sale.getTotalAmount()
                + companyNit
                + sale.getCustomer().getDocument();
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-384");
            byte[] hash = digest.digest(data.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-384 is not available", e);
        }
    }

    public String buildQrLink(String cufe) {
        return DIAN_QR_URL + cufe;
    }

    /** Draws the invoice and saves it as invoices/<invoice number>.png */
    public Path generateImage(Invoice invoice, CompanyInfo company) {
        Sale sale = invoice.getSale();
        int rows = sale.getSaleDetails().size();
        int height = 560 + rows * ROW_HEIGHT + QR_SIZE;

        BufferedImage image = new BufferedImage(WIDTH, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = image.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g.setColor(Color.WHITE);
        g.fillRect(0, 0, WIDTH, height);
        g.setColor(Color.BLACK);

        int y = drawHeader(g, invoice, company);
        y = drawCustomer(g, sale, y);
        y = drawDetails(g, sale, y);
        y = drawTotals(g, invoice, y);
        drawDianData(g, invoice, y);

        g.dispose();
        return save(image, invoice.getInvoiceNumber());
    }

    private int drawHeader(Graphics2D g, Invoice invoice, CompanyInfo company) {
        int y = MARGIN + 10;
        g.setFont(new Font("SansSerif", Font.BOLD, 26));
        g.drawString(company.getName(), MARGIN, y);

        g.setFont(new Font("SansSerif", Font.PLAIN, 14));
        g.drawString("NIT: " + company.getNit(), MARGIN, y += 26);
        g.drawString(company.getAddress() + " - " + company.getCity(), MARGIN, y += 20);
        g.drawString("Phone: " + company.getPhone() + "   Email: " + company.getEmail(), MARGIN, y += 20);

        int right = WIDTH - MARGIN - 330;
        g.setFont(new Font("SansSerif", Font.BOLD, 18));
        g.drawString("ELECTRONIC SALES INVOICE", right, MARGIN + 10);
        g.setFont(new Font("SansSerif", Font.PLAIN, 14));
        g.drawString("Invoice No.: " + invoice.getInvoiceNumber(), right, MARGIN + 36);
        g.drawString("Issue date: " + invoice.getIssueDate().format(DATE_FORMAT), right, MARGIN + 56);
        g.drawString("Sale No.: " + invoice.getSale().getSaleNumber(), right, MARGIN + 76);

        y += 20;
        g.drawLine(MARGIN, y, WIDTH - MARGIN, y);
        return y + 30;
    }

    private int drawCustomer(Graphics2D g, Sale sale, int y) {
        Customer customer = sale.getCustomer();
        g.setFont(new Font("SansSerif", Font.BOLD, 15));
        g.drawString("CUSTOMER", MARGIN, y);
        g.setFont(new Font("SansSerif", Font.PLAIN, 14));
        g.drawString("Name: " + customer.getName(), MARGIN, y += 22);
        g.drawString("Document: " + customer.getDocument()
                + "   Type: " + customer.getType(), MARGIN, y += 20);
        g.drawString("Address: " + customer.getAddress() + "   Phone: " + customer.getPhone(), MARGIN, y += 20);
        y += 20;
        g.drawLine(MARGIN, y, WIDTH - MARGIN, y);
        return y + 30;
    }

    private int drawDetails(Graphics2D g, Sale sale, int y) {
        int[] columns = {MARGIN, 300, 450, 530, 650, 730, 840};
        String[] titles = {"Product", "Lot", "Qty", "Unit price", "Disc.", "VAT", "Line total"};

        g.setFont(new Font("SansSerif", Font.BOLD, 13));
        for (int i = 0; i < titles.length; i++) {
            g.drawString(titles[i], columns[i], y);
        }
        y += 8;
        g.drawLine(MARGIN, y, WIDTH - MARGIN, y);

        g.setFont(new Font("SansSerif", Font.PLAIN, 13));
        for (SaleDetail detail : sale.getSaleDetails()) {
            y += ROW_HEIGHT - 6;
            g.drawString(productName(detail.getLot().getProductId()), columns[0], y);
            g.drawString(detail.getLot().getLotNumber(), columns[1], y);
            g.drawString(detail.getQuantitySold().toPlainString(), columns[2], y);
            g.drawString(money(detail.getUnitPrice()), columns[3], y);
            g.drawString(percent(detail.getDiscountApplied()), columns[4], y);
            g.drawString(money(detail.getTaxAmount()), columns[5], y);
            g.drawString(money(detail.getLineTotal()), columns[6], y);
            y += 6;
        }
        y += 14;
        g.drawLine(MARGIN, y, WIDTH - MARGIN, y);
        return y + 30;
    }

    private int drawTotals(Graphics2D g, Invoice invoice, int y) {
        int labelX = WIDTH - MARGIN - 330;
        int valueX = WIDTH - MARGIN - 150;

        g.setFont(new Font("SansSerif", Font.PLAIN, 15));
        g.drawString("Subtotal:", labelX, y);
        g.drawString(money(invoice.getSubtotal()), valueX, y);
        g.drawString("Discount:", labelX, y += 24);
        g.drawString("- " + money(invoice.getTotalDiscount()), valueX, y);
        g.drawString("VAT (19%):", labelX, y += 24);
        g.drawString(money(invoice.getTaxAmount()), valueX, y);

        g.setFont(new Font("SansSerif", Font.BOLD, 18));
        g.drawString("TOTAL:", labelX, y += 32);
        g.drawString(money(invoice.getTotalAmount()), valueX, y);
        return y + 40;
    }

    private void drawDianData(Graphics2D g, Invoice invoice, int y) {
        g.drawImage(createQr(invoice.getQrLink()), MARGIN, y, null);

        int textX = MARGIN + QR_SIZE + 20;
        g.setFont(new Font("SansSerif", Font.BOLD, 14));
        g.drawString("CUFE:", textX, y + 20);

        // The CUFE has 96 characters: split it in two lines so it fits
        g.setFont(new Font("Monospaced", Font.PLAIN, 13));
        String cufe = invoice.getCufe();
        g.drawString(cufe.substring(0, 48), textX, y + 42);
        g.drawString(cufe.substring(48), textX, y + 60);

        g.setFont(new Font("SansSerif", Font.ITALIC, 12));
        g.drawString("Scan the QR code to validate this invoice (simulated DIAN validation).", textX, y + 95);
        g.drawString("Simulated electronic invoice - academic project.", textX, y + 115);
    }

    private BufferedImage createQr(String content) {
        try {
            BitMatrix matrix = new QRCodeWriter().encode(content, BarcodeFormat.QR_CODE, QR_SIZE, QR_SIZE);
            BufferedImage qr = new BufferedImage(QR_SIZE, QR_SIZE, BufferedImage.TYPE_INT_RGB);
            for (int x = 0; x < QR_SIZE; x++) {
                for (int y = 0; y < QR_SIZE; y++) {
                    qr.setRGB(x, y, matrix.get(x, y) ? Color.BLACK.getRGB() : Color.WHITE.getRGB());
                }
            }
            return qr;
        } catch (WriterException e) {
            throw new IllegalStateException("Could not create the QR code", e);
        }
    }

    private Path save(BufferedImage image, String invoiceNumber) {
        try {
            Path folder = Paths.get(OUTPUT_FOLDER);
            Files.createDirectories(folder);
            Path file = folder.resolve(invoiceNumber + ".png");
            ImageIO.write(image, "png", file.toFile());
            return file.toAbsolutePath();
        } catch (IOException e) {
            throw new RuntimeException("Error saving invoice image ", e);
        }
    }

    private String productName(int productId) {
        Product product = productDao.findById(productId);
        return product != null ? product.getName() : "Product #" + productId;
    }

    private String money(BigDecimal value) {
        return MONEY.format(value);
    }

    private String percent(BigDecimal rate) {
        return rate.multiply(new BigDecimal("100")).stripTrailingZeros().toPlainString() + "%";
    }
}
