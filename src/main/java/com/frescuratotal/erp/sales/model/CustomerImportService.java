package com.frescuratotal.erp.sales.model;

import com.frescuratotal.erp.common.DatabaseConnection;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * EJERCICIO 4: importar clientes desde un archivo de texto.
 *
 * Regla de oro: TODO O NADA.
 *   1. Primero se validan TODAS las lineas (sin guardar nada).
 *   2. Si una sola esta mal, se cancela y no se guarda ningun cliente.
 *   3. Si todas estan bien, se guardan todas dentro de una transaccion:
 *      si falla una insercion, el rollback deshace las anteriores.
 */
public class CustomerImportService {

    // Textos fijos en constantes: si cambian, se cambian en un solo lugar.
    private static final String FILE_NAME = "clientes_importar.txt";
    private static final String HEADER = "nombre;documento;correo;telefono;tipo_cliente";
    private static final String INVALID_MESSAGE = "Se encontraron registros inválidos. Importación cancelada.";

    // Expresion regular sencilla para el correo: algo@algo.algo (sin espacios).
    private static final String EMAIL_REGEX = "^[\\w.+-]+@[\\w-]+(\\.[\\w-]+)+$";

    private final CustomerDao customerDao = new CustomerDaoImpl();

    /**
     * Lee, valida e importa los clientes.
     * Devuelve cuantos clientes se registraron.
     * Si algo falla lanza IllegalArgumentException con el mensaje para el usuario;
     * el controlador lo atrapa y lo muestra, asi la aplicacion no se cierra.
     */
    public int importCustomers() {
        // Path sin carpeta = el archivo se busca en la raiz del proyecto, junto al README.md
        Path file = Path.of(FILE_NAME);

        // ---------- 1. ¿Existe el archivo? ----------
        if (!Files.exists(file)) {
            throw new IllegalArgumentException("No se encontró el archivo " + FILE_NAME + ".");
        }

        // ---------- 2. Leer todas las lineas ----------
        // readAllLines devuelve una lista donde cada elemento es una linea del archivo.
        List<String> lines;
        try {
            lines = Files.readAllLines(file);
        } catch (IOException e) {
            throw new RuntimeException("Error leyendo " + FILE_NAME, e);
        }

        // ---------- 3. ¿La cabecera es exactamente la esperada? ----------
        // lines.get(0) es la primera linea. Los textos se comparan con equals, nunca con ==.
        if (lines.isEmpty() || !lines.get(0).trim().equals(HEADER)) {
            throw new IllegalArgumentException(INVALID_MESSAGE + "\n  Detalle: la cabecera no es la esperada.");
        }

        // ---------- 4. Validar cada linea y convertirla en Customer ----------
        List<Customer> customers = new ArrayList<>();
        // Un Set NO permite repetidos: add() devuelve false si el documento ya estaba.
        // Asi detectamos documentos repetidos DENTRO del mismo archivo.
        Set<String> documentsInFile = new HashSet<>();

        // Empezamos en 1 porque la posicion 0 es la cabecera.
        for (int i = 1; i < lines.size(); i++) {
            String line = lines.get(i);
            int lineNumber = i + 1; // numero de linea "humano" para el mensaje de error

            // Lineas completamente vacias: se ignoran y se sigue con la siguiente.
            if (line.isBlank()) {
                continue;
            }

            // split(";", -1) separa por punto y coma. El -1 hace que NO se pierdan
            // los campos vacios del final: "a;b;c;d;" da 5 campos (el ultimo vacio) y no 4.
            String[] fields = line.split(";", -1);

            // Exactamente 5 campos
            if (fields.length != 5) {
                throw invalid(lineNumber, "debe tener 5 campos y tiene " + fields.length);
            }

            // trim() quita espacios sobrantes de cada campo
            String name = fields[0].trim();
            String document = fields[1].trim();
            String email = fields[2].trim();
            String phone = fields[3].trim();
            String typeText = fields[4].trim();

            // Ningun campo vacio
            if (name.isEmpty() || document.isEmpty() || email.isEmpty() || phone.isEmpty() || typeText.isEmpty()) {
                throw invalid(lineNumber, "tiene campos vacíos");
            }

            // Correo con estructura valida
            if (!email.matches(EMAIL_REGEX)) {
                throw invalid(lineNumber, "el correo '" + email + "' no es válido");
            }

            // Tipo de cliente: solo MAYORISTA o MINORISTA
            CustomerType type = parseType(typeText);
            if (type == null) {
                throw invalid(lineNumber, "tipo_cliente debe ser MAYORISTA o MINORISTA");
            }

            // Documento repetido dentro del archivo
            if (!documentsInFile.add(document)) {
                throw invalid(lineNumber, "el documento " + document + " está repetido en el archivo");
            }

            // Documento que ya existe en la base de datos
            if (customerDao.findByDocument(document).isPresent()) {
                throw invalid(lineNumber, "el documento " + document + " ya existe en la base de datos");
            }

            // El archivo no trae direccion: en la tabla clientes la columna direccion
            // permite NULL, por eso se envia null.
            customers.add(new Customer(name, document, email, phone, null, type));
        }

        // ---------- 5. Guardar TODOS en una sola transaccion ----------
        // Si llegamos aqui, todas las lineas son validas.
        // inTransaction recibe una lambda con el bloque a guardar: si cualquier
        // save falla, hace rollback y no queda ningun cliente guardado a medias.
        DatabaseConnection.getInstance().inTransaction(() -> {
            for (Customer customer : customers) {
                if (!customerDao.save(customer)) {
                    throw new RuntimeException("No se pudo guardar el cliente " + customer.getDocument());
                }
            }
        });

        return customers.size();
    }

    /** Convierte el texto del archivo en el enum del proyecto. Devuelve null si no es valido. */
    private CustomerType parseType(String text) {
        return switch (text.toUpperCase()) {
            case "MAYORISTA" -> CustomerType.WHOLESALE;
            case "MINORISTA" -> CustomerType.RETAIL;
            default -> null;
        };
    }

    /** Arma el error de "registros invalidos" con el detalle de la linea que fallo. */
    private IllegalArgumentException invalid(int lineNumber, String reason) {
        return new IllegalArgumentException(INVALID_MESSAGE + "\n  Detalle: línea " + lineNumber + " - " + reason + ".");
    }
}
