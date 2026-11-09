package com.frescuratotal.erp.sales.model;

import com.frescuratotal.erp.inventory.model.Product;
import com.frescuratotal.erp.inventory.model.ProductDao;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;

import javax.imageio.ImageIO;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.GraphicsEnvironment;
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
import java.util.Arrays;
import java.util.HexFormat;
import java.util.List;
import java.util.Locale;

/**
 * Builds the simulated DIAN data of an invoice (CUFE and QR link)
 * and draws the invoice as a PNG image inside the "invoices" folder.
 */
public class InvoiceGenerator {
    private static final String OUTPUT_FOLDER = "invoices";
    private static final String DIAN_QR_URL = "https://catalogo-vpfe.dian.gov.co/document/searchqr?documentkey=";
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final DateTimeFormatter SHORT_DATE = DateTimeFormatter.ofPattern("MMM dd, yyyy", Locale.ENGLISH);
    private static final NumberFormat MONEY = NumberFormat.getCurrencyInstance(Locale.of("es", "CO"));

    // Page size (A4 proportion) and layout
    private static final int WIDTH = 1240;
    private static final int MIN_HEIGHT = 1754;
    private static final int MARGIN = 70;
    private static final int ROW_HEIGHT = 58;
    private static final int QR_SIZE = 230;

    // Colors of the Frescura Total brand
    private static final Color PRIMARY = new Color(84, 40, 125);
    private static final Color LIGHT = new Color(244, 239, 250);
    private static final Color GOLD = new Color(196, 154, 66);
    private static final Color ORANGE = new Color(194, 94, 32);
    private static final Color TEXT = new Color(33, 37, 41);
    private static final Color GRAY = new Color(108, 117, 125);
    private static final Color LINE = new Color(221, 214, 230);

    // Times New Roman when the computer has it; a similar serif font if not
    private static final String FONT = pickFont("Times New Roman", "Liberation Serif", "Serif");

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

    /**
     * Text inside the QR, with the same fields the DIAN uses on real invoices.
     * Scanning it shows the invoice data, and the last line is the validation link.
     */
    public String buildQrContent(Invoice invoice, CompanyInfo company) {
        return "NumFac: " + invoice.getInvoiceNumber() + "\n"
                + "FecFac: " + invoice.getIssueDate().toLocalDate() + "\n"
                + "HorFac: " + invoice.getIssueDate().toLocalTime().withNano(0) + "\n"
                + "NitFac: " + company.getNit() + "\n"
                + "DocAdq: " + invoice.getSale().getCustomer().getDocument() + "\n"
                + "ValFac: " + invoice.getSubtotal().subtract(invoice.getTotalDiscount()) + "\n"
                + "ValIva: " + invoice.getTaxAmount() + "\n"
                + "ValOtroIm: 0.00\n"
                + "ValTolFac: " + invoice.getTotalAmount() + "\n"
                + "CUFE: " + invoice.getCufe() + "\n"
                + "QRCode: " + invoice.getQrLink();
    }

    /** Draws the invoice and saves it as invoices/<invoice number>.png */
    public Path generateImage(Invoice invoice, CompanyInfo company) {
        Sale sale = invoice.getSale();
        // Everything but the rows: sections above the table, totals, DIAN box and footer
        int height = Math.max(MIN_HEIGHT, 1270 + sale.getSaleDetails().size() * ROW_HEIGHT);

        BufferedImage image = new BufferedImage(WIDTH, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = image.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g.setColor(Color.WHITE);
        g.fillRect(0, 0, WIDTH, height);

        drawHeader(g, invoice, company);
        drawInfoBoxes(g, invoice, 290);
        int y = drawDetails(g, sale, 520);
        y = drawTotals(g, invoice, y + 40);
        drawDianData(g, invoice, company, y + 50);
        drawFooter(g, company, height);

        g.dispose();
        return save(image, invoice.getInvoiceNumber());
    }

    // ---------- Sections of the invoice ----------

    private void drawHeader(Graphics2D g, Invoice invoice, CompanyInfo company) {
        // Purple band with the company data
        g.setColor(PRIMARY);
        g.fillRect(0, 0, WIDTH, 220);
        g.setColor(GOLD);
        g.fillRect(0, 220, WIDTH, 8);

        g.setColor(Color.WHITE);
        g.setFont(font(Font.BOLD, 46));
        g.drawString(company.getName(), MARGIN, 95);
        g.setFont(font(Font.ITALIC, 22));
        g.drawString("Fresh food from the field to your table", MARGIN, 130);
        g.setFont(font(Font.PLAIN, 20));
        g.drawString("NIT " + company.getNit() + "   |   " + company.getAddress() + ", " + company.getCity(), MARGIN, 175);

        // White card with the invoice number
        int cardX = WIDTH - MARGIN - 360;
        g.setColor(Color.WHITE);
        g.fillRoundRect(cardX, 45, 360, 135, 18, 18);
        g.setColor(PRIMARY);
        g.setFont(font(Font.BOLD, 20));
        drawCentered(g, "ELECTRONIC SALES INVOICE", cardX, 360, 85);
        g.setColor(TEXT);
        g.setFont(font(Font.BOLD, 30));
        drawCentered(g, invoice.getInvoiceNumber(), cardX, 360, 135);
        g.setColor(GRAY);
        g.setFont(font(Font.PLAIN, 17));
        drawCentered(g, "Issued " + invoice.getIssueDate().format(DATE_FORMAT), cardX, 360, 165);
    }

    private void drawInfoBoxes(Graphics2D g, Invoice invoice, int y) {
        Sale sale = invoice.getSale();
        Customer customer = sale.getCustomer();
        int boxWidth = (WIDTH - 2 * MARGIN - 30) / 2;

        drawBox(g, "BILLED TO", MARGIN, y, boxWidth, 190, List.of(
                customer.getName(),
                "Document: " + customer.getDocument(),
                "Customer type: " + (customer.getType() == CustomerType.WHOLESALE ? "Wholesale" : "Retail"),
                text(customer.getAddress()) + "   " + text(customer.getPhone())));

        drawBox(g, "SALE DETAILS", MARGIN + boxWidth + 30, y, boxWidth, 190, List.of(
                "Sale No.: " + sale.getSaleNumber(),
                "Date: " + invoice.getIssueDate().format(SHORT_DATE),
                "Seller: " + text(sale.getRegisteredBy()),
                "Payment: Cash / Transfer"));
    }

    private int drawDetails(Graphics2D g, Sale sale, int y) {
        int tableWidth = WIDTH - 2 * MARGIN;
        // Column positions: product on the left, numbers aligned to the right edge of each column
        int qtyRight = MARGIN + 600;
        int priceRight = MARGIN + 770;
        int discRight = MARGIN + 880;
        int totalRight = MARGIN + tableWidth - 20;

        // Header row
        g.setColor(PRIMARY);
        g.fillRoundRect(MARGIN, y, tableWidth, 50, 12, 12);
        g.setColor(Color.WHITE);
        g.setFont(font(Font.BOLD, 20));
        g.drawString("Product", MARGIN + 20, y + 33);
        drawRight(g, "Qty", qtyRight, y + 33);
        drawRight(g, "Unit price", priceRight, y + 33);
        drawRight(g, "Disc.", discRight, y + 33);
        drawRight(g, "Line total", totalRight, y + 33);
        y += 50;

        // One row per sale detail, with zebra colors
        boolean shaded = false;
        for (SaleDetail detail : sale.getSaleDetails()) {
            if (shaded) {
                g.setColor(LIGHT);
                g.fillRect(MARGIN, y, tableWidth, ROW_HEIGHT);
            }
            shaded = !shaded;

            g.setColor(TEXT);
            g.setFont(font(Font.BOLD, 20));
            g.drawString(fit(g, productName(detail.getLot().getProductId()), 520), MARGIN + 20, y + 26);

            g.setColor(GRAY);
            g.setFont(font(Font.PLAIN, 16));
            String lotInfo = "Lot " + detail.getLot().getLotNumber();
            if (detail.getLot().getExpirationDate() != null) {
                lotInfo += "  ·  expires " + detail.getLot().getExpirationDate();
            }
            g.drawString(lotInfo, MARGIN + 20, y + 48);

            g.setColor(TEXT);
            g.setFont(font(Font.PLAIN, 20));
            drawRight(g, detail.getQuantitySold().stripTrailingZeros().toPlainString(), qtyRight, y + 36);
            drawRight(g, money(detail.getUnitPrice()), priceRight, y + 36);

            boolean hasDiscount = detail.getDiscountApplied().compareTo(BigDecimal.ZERO) > 0;
            g.setColor(hasDiscount ? ORANGE : GRAY);
            g.setFont(font(hasDiscount ? Font.BOLD : Font.PLAIN, 20));
            drawRight(g, percent(detail.getDiscountApplied()), discRight, y + 36);

            g.setColor(TEXT);
            g.setFont(font(Font.BOLD, 20));
            drawRight(g, money(detail.getLineTotal()), totalRight, y + 36);
            y += ROW_HEIGHT;
        }

        g.setColor(LINE);
        g.setStroke(new BasicStroke(2));
        g.drawLine(MARGIN, y, MARGIN + tableWidth, y);
        return y;
    }

    private int drawTotals(Graphics2D g, Invoice invoice, int y) {
        int boxWidth = 430;
        int boxX = WIDTH - MARGIN - boxWidth;
        int labelX = boxX + 25;
        int valueRight = boxX + boxWidth - 25;

        // Thank-you note on the left
        g.setColor(PRIMARY);
        g.setFont(font(Font.ITALIC, 26));
        g.drawString("Thank you for your purchase!", MARGIN, y + 40);
        g.setColor(GRAY);
        g.setFont(font(Font.PLAIN, 17));
        g.drawString("Products marked in orange had a near-expiry discount.", MARGIN, y + 72);
        g.drawString("VAT is calculated after discounts.", MARGIN, y + 96);
        if (invoice.getSale() != null && invoice.getSale().getDeliveryFee() != null
                && invoice.getSale().getDeliveryFee().signum() > 0) {
            g.drawString("Total includes " + invoice.getSale().getDeliveryMode().getLabel() + " delivery ("
                    + invoice.getSale().getDeliveryMode().getDescription() + "): "
                    + money(invoice.getSale().getDeliveryFee()), MARGIN, y + 120);
        }

        g.setColor(LIGHT);
        g.fillRoundRect(boxX, y, boxWidth, 210, 16, 16);

        g.setFont(font(Font.PLAIN, 21));
        g.setColor(TEXT);
        g.drawString("Subtotal", labelX, y + 42);
        drawRight(g, money(invoice.getSubtotal()), valueRight, y + 42);

        g.setColor(ORANGE);
        g.drawString("Discount", labelX, y + 82);
        drawRight(g, "- " + money(invoice.getTotalDiscount()), valueRight, y + 82);

        g.setColor(TEXT);
        g.drawString("VAT (19%)", labelX, y + 122);
        drawRight(g, money(invoice.getTaxAmount()), valueRight, y + 122);

        // Total in a purple bar
        g.setColor(PRIMARY);
        g.fillRoundRect(boxX, y + 145, boxWidth, 65, 16, 16);
        g.setColor(Color.WHITE);
        g.setFont(font(Font.BOLD, 26));
        g.drawString("TOTAL", labelX, y + 188);
        drawRight(g, money(invoice.getTotalAmount()), valueRight, y + 188);

        return y + 210;
    }

    private void drawDianData(Graphics2D g, Invoice invoice, CompanyInfo company, int y) {
        int boxWidth = WIDTH - 2 * MARGIN;
        g.setColor(LINE);
        g.setStroke(new BasicStroke(2));
        g.drawRoundRect(MARGIN, y, boxWidth, QR_SIZE + 40, 16, 16);

        g.drawImage(createQr(buildQrContent(invoice, company)), MARGIN + 20, y + 20, null);

        int textX = MARGIN + QR_SIZE + 50;
        g.setColor(PRIMARY);
        g.setFont(font(Font.BOLD, 22));
        g.drawString("CUFE (Unique Electronic Invoice Code)", textX, y + 55);

        // The CUFE has 96 characters: split it in two lines so it fits
        g.setColor(TEXT);
        g.setFont(font(Font.PLAIN, 19));
        String cufe = invoice.getCufe();
        g.drawString(cufe.substring(0, 48), textX, y + 92);
        g.drawString(cufe.substring(48), textX, y + 120);

        g.setColor(GRAY);
        g.setFont(font(Font.ITALIC, 17));
        g.drawString("Scan the QR code to see the invoice data and the DIAN validation link (simulated).", textX, y + 165);
        g.drawString("Simulated electronic invoice for an academic project.", textX, y + 190);
    }

    private void drawFooter(Graphics2D g, CompanyInfo company, int height) {
        g.setColor(GOLD);
        g.fillRect(0, height - 78, WIDTH, 6);
        g.setColor(PRIMARY);
        g.fillRect(0, height - 72, WIDTH, 72);
        g.setColor(Color.WHITE);
        g.setFont(font(Font.PLAIN, 18));
        drawCentered(g, text(company.getPhone()) + "   |   " + text(company.getEmail())
                + "   |   " + text(company.getCity()) + ", Colombia", 0, WIDTH, height - 30);
    }

    // ---------- Small drawing helpers ----------

    private void drawBox(Graphics2D g, String title, int x, int y, int width, int height, List<String> lines) {
        g.setColor(LIGHT);
        g.fillRoundRect(x, y, width, height, 16, 16);
        g.setColor(PRIMARY);
        g.fillRoundRect(x, y, 8, height, 8, 8);

        g.setFont(font(Font.BOLD, 18));
        g.drawString(title, x + 30, y + 38);

        int lineY = y + 75;
        for (int i = 0; i < lines.size(); i++) {
            g.setColor(i == 0 ? TEXT : GRAY);
            g.setFont(font(i == 0 ? Font.BOLD : Font.PLAIN, i == 0 ? 23 : 19));
            g.drawString(fit(g, lines.get(i), width - 50), x + 30, lineY);
            lineY += i == 0 ? 34 : 28;
        }
    }

    /** Cuts the text with "..." so it does not run over the next column. */
    private String fit(Graphics2D g, String text, int maxWidth) {
        if (g.getFontMetrics().stringWidth(text) <= maxWidth) {
            return text;
        }
        String cut = text;
        while (!cut.isEmpty() && g.getFontMetrics().stringWidth(cut + "...") > maxWidth) {
            cut = cut.substring(0, cut.length() - 1);
        }
        return cut + "...";
    }

    private void drawRight(Graphics2D g, String text, int rightX, int y) {
        g.drawString(text, rightX - g.getFontMetrics().stringWidth(text), y);
    }

    private void drawCentered(Graphics2D g, String text, int x, int width, int y) {
        g.drawString(text, x + (width - g.getFontMetrics().stringWidth(text)) / 2, y);
    }

    private BufferedImage createQr(String content) {
        try {
            BitMatrix matrix = new QRCodeWriter().encode(content, BarcodeFormat.QR_CODE, QR_SIZE, QR_SIZE);
            BufferedImage qr = new BufferedImage(QR_SIZE, QR_SIZE, BufferedImage.TYPE_INT_RGB);
            for (int x = 0; x < QR_SIZE; x++) {
                for (int y = 0; y < QR_SIZE; y++) {
                    qr.setRGB(x, y, matrix.get(x, y) ? TEXT.getRGB() : Color.WHITE.getRGB());
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

    private Font font(int style, int size) {
        return new Font(FONT, style, size);
    }

    private static String pickFont(String... options) {
        List<String> installed = Arrays.asList(
                GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames());
        for (String option : options) {
            if (installed.contains(option)) {
                return option;
            }
        }
        return Font.SERIF;
    }

    private String text(String value) {
        return value != null ? value : "";
    }

    private String money(BigDecimal value) {
        return MONEY.format(value);
    }

    private String percent(BigDecimal rate) {
        return rate.multiply(new BigDecimal("100")).stripTrailingZeros().toPlainString() + "%";
    }
}
