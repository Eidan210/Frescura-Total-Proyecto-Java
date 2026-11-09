package com.frescuratotal.erp.sales.model;

import com.frescuratotal.erp.common.DatabaseConnection;
import com.frescuratotal.erp.common.exception.ImportException;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * EJERCICIO 4 - Importación masiva de clientes desde clientes_importar.txt.
 *
 * Idea clave (requerimiento 5): "TODO O NADA".
 *   FASE 1: validar TODAS las filas y juntar los errores. Todavía no se guarda nada.
 *   FASE 2: solo si no hubo ningún error, guardar todo dentro de UNA transacción.
 *           Si una inserción falla -> rollback: no quedan registros a medias.
 */
public class CustomerImportService {

    // Ruta RELATIVA: se resuelve desde la carpeta donde se ejecuta el proyecto
    // (en IntelliJ, la raíz del proyecto, junto a README.md y pom.xml).
    private static final Path FILE = Paths.get("clientes_importar.txt");

    private static final String EXPECTED_HEADER = "nombre;documento;correo;telefono;tipo_cliente";

    // Mensajes EXACTOS del enunciado.
    public static final String MSG_NOT_FOUND = "Error: No se encontró el archivo clientes_importar.txt.";
    public static final String MSG_INVALID = "Error: Se encontraron registros inválidos. Importación cancelada.";

    // Expresión regular para el correo: algo@algo.dominio
    //   [\w.+-]+   -> una o más letras/números/punto/más/guion antes de la @
    //   @          -> la arroba
    //   [\w-]+     -> el dominio (gmail, omega...)
    //   (\.[\w-]+)+ -> uno o más ".com", ".co"...
    private static final Pattern EMAIL = Pattern.compile("^[\\w.+-]+@[\\w-]+(\\.[\\w-]+)+$");

    private final CustomerDao customerDao;

    public CustomerImportService() {
        this(new CustomerDaoImpl());
    }

    // Constructor con DAO: permite probar con un DAO falso.
    public CustomerImportService(CustomerDao customerDao) {
        this.customerDao = customerDao;
    }

    /** Devuelve cuántos clientes se registraron. Si algo falla lanza ImportException con el mensaje exacto. */
    public int importCustomers() {
        // Validación 1: el archivo existe
        if (!Files.exists(FILE)) {
            throw new ImportException(MSG_NOT_FOUND);
        }

        List<String> lines;
        try {
            lines = Files.readAllLines(FILE, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new ImportException("Error: No se pudo leer el archivo clientes_importar.txt.");
        }

        // FASE 1: validar todo (si algo está mal, validate lanza la excepción y no se guarda nada)
        List<Customer> customers = validate(lines);

        // FASE 2: guardar todo en UNA transacción.
        // inTransaction: setAutoCommit(false) -> ejecuta -> commit; si hay error -> rollback.
        // customerDao.save lanza RuntimeException si el INSERT falla, y eso dispara el rollback.
        try {
            DatabaseConnection.getInstance().inTransaction(() -> customers.forEach(customerDao::save));
        } catch (RuntimeException e) {
            throw new ImportException(MSG_INVALID, List.of("Error al insertar: " + e.getMessage()));
        }
        return customers.size();
    }

    /**
     * Revisa TODAS las líneas y devuelve los clientes listos para guardar.
     * No se detiene en el primer error: los junta todos para mostrarlos de una vez.
     * Es public para poder probarla sin base de datos ni archivo.
     */
    public List<Customer> validate(List<String> lines) {
        List<String> errors = new ArrayList<>();
        List<Customer> customers = new ArrayList<>();

        // Validación 2: cabecera exacta.
        // Se quita el BOM (﻿): un carácter invisible que algunos editores de Windows
        // ponen al inicio del archivo y haría fallar la comparación aunque "se vea igual".
        String header = lines.isEmpty() ? "" : lines.get(0).replace("﻿", "").strip();
        if (!header.equals(EXPECTED_HEADER)) {
            throw new ImportException(MSG_INVALID, List.of("Línea 1: la cabecera debe ser exactamente " + EXPECTED_HEADER));
        }

        // Set: no admite repetidos. add() devuelve false si el documento ya estaba -> repetido en el archivo.
        Set<String> documentsInFile = new HashSet<>();

        // for con índice (y no stream) porque necesitamos el número de línea para los mensajes.
        for (int i = 1; i < lines.size(); i++) {
            String line = lines.get(i);
            int lineNumber = i + 1;

            // Validación: ignorar líneas completamente vacías
            if (line.isBlank()) {
                continue;
            }

            // split con -1: CONSERVA los campos vacíos del final.
            // "a;b;c;d;".split(";")     -> 4 campos (pierde el último vacío)
            // "a;b;c;d;".split(";", -1) -> 5 campos (el último es "")
            String[] fields = line.split(";", -1);

            // Validación: exactamente 5 campos
            if (fields.length != 5) {
                errors.add("Línea " + lineNumber + ": tiene " + fields.length + " campos y deben ser 5.");
                continue; // si no tiene 5 campos no tiene sentido seguir revisando esta fila
            }

            String name = fields[0].strip();
            String document = fields[1].strip();
            String email = fields[2].strip();
            String phone = fields[3].strip();
            String type = fields[4].strip();

            // Validación: ningún campo vacío
            if (name.isEmpty() || document.isEmpty() || email.isEmpty() || phone.isEmpty() || type.isEmpty()) {
                errors.add("Línea " + lineNumber + ": todos los campos son obligatorios.");
                continue;
            }

            // Validación: correo con estructura válida
            if (!EMAIL.matcher(email).matches()) {
                errors.add("Línea " + lineNumber + ": correo inválido (" + email + ").");
            }

            // Validación: solo MAYORISTA o MINORISTA. Se traduce al enum del proyecto.
            CustomerType customerType = toCustomerType(type);
            if (customerType == null) {
                errors.add("Línea " + lineNumber + ": tipo_cliente debe ser MAYORISTA o MINORISTA (" + type + ").");
            }

            // Validación: documento repetido dentro del archivo o ya existente en la BD
            if (!documentsInFile.add(document)) {
                errors.add("Línea " + lineNumber + ": documento " + document + " repetido en el archivo.");
            } else if (customerDao.findByDocument(document).isPresent()) {
                errors.add("Línea " + lineNumber + ": documento " + document + " ya existe en la base de datos.");
            }

            // address = null: el archivo no trae dirección y en la BD la columna admite NULL.
            customers.add(new Customer(name, document, email, phone, null, customerType));
        }

        // Si hubo CUALQUIER error, se cancela todo (todavía no se ha guardado nada).
        if (!errors.isEmpty()) {
            throw new ImportException(MSG_INVALID, errors);
        }
        return customers;
    }

    // Exactamente como lo pide el enunciado (en mayúsculas). Devuelve null si no es válido.
    private CustomerType toCustomerType(String type) {
        return switch (type) {
            case "MAYORISTA" -> CustomerType.WHOLESALE;
            case "MINORISTA" -> CustomerType.RETAIL;
            default -> null;
        };
    }
}
