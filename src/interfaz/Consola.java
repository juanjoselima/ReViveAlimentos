package interfaz;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Scanner;

/**
 * Lectura segura de datos por teclado. Cada método repite la pregunta hasta que el dato
 * tenga el formato correcto, así el programa nunca se cae por un error de escritura.
 */
public class Consola {
    private static Scanner teclado = new Scanner(System.in);
    private static final DateTimeFormatter FORMATO_FECHA_HORA = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private static final DateTimeFormatter FORMATO_FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    public static String leerTexto(String mensaje) {
        System.out.print(mensaje);
        if (!teclado.hasNextLine()) {         // fin de la entrada: se cierra el programa con orden
            System.out.println("\nEntrada finalizada. Hasta pronto.");
            System.exit(0);
        }
        return teclado.nextLine().trim();
    }

    public static int leerEntero(String mensaje, int min, int max) {
        while (true) {
            String texto = leerTexto(mensaje);
            try {
                int valor = Integer.parseInt(texto);
                if (valor >= min && valor <= max) return valor;
                error("Ingrese un número entre " + min + " y " + max + ".");
            } catch (NumberFormatException e) {
                error("'" + texto + "' no es un número entero válido.");
            }
        }
    }

    public static double leerDecimal(String mensaje) {
        while (true) {
            String texto = leerTexto(mensaje).replace(",", ".");
            try {
                return Double.parseDouble(texto);
            } catch (NumberFormatException e) {
                error("'" + texto + "' no es un número válido. Ejemplo: 12.5");
            }
        }
    }

    public static LocalDateTime leerFechaHora(String mensaje) {
        while (true) {
            String texto = leerTexto(mensaje + " (dd/MM/aaaa HH:mm): ");
            try {
                return LocalDateTime.parse(texto, FORMATO_FECHA_HORA);
            } catch (DateTimeParseException e) {
                error("Formato incorrecto. Ejemplo: 25/11/2026 18:30");
            }
        }
    }

    public static LocalDate leerFecha(String mensaje) {
        while (true) {
            String texto = leerTexto(mensaje + " (dd/MM/aaaa): ");
            try {
                return LocalDate.parse(texto, FORMATO_FECHA);
            } catch (DateTimeParseException e) {
                error("Formato incorrecto. Ejemplo: 25/11/2026");
            }
        }
    }

    public static LocalTime leerHora(String mensaje) {
        while (true) {
            String texto = leerTexto(mensaje + " (HH:mm): ");
            try {
                return LocalTime.parse(texto);
            } catch (DateTimeParseException e) {
                error("Formato incorrecto. Ejemplo: 08:00");
            }
        }
    }

    public static boolean leerSiNo(String mensaje) {
        while (true) {
            String texto = leerTexto(mensaje + " (S/N): ").toUpperCase();
            if (texto.equals("S")) return true;
            if (texto.equals("N")) return false;
            error("Responda S o N.");
        }
    }

    /** Muestra una lista numerada y devuelve el elemento elegido (o null si elige 0). */
    public static <T> T elegir(String titulo, ArrayList<T> lista) {
        if (lista.isEmpty()) {
            error("No hay elementos para elegir en: " + titulo);
            return null;
        }
        System.out.println("-- " + titulo + " --");
        for (int i = 0; i < lista.size(); i++) {
            System.out.println("  " + (i + 1) + ". " + lista.get(i));
        }
        int opcion = leerEntero("Seleccione (0 = cancelar): ", 0, lista.size());
        if (opcion == 0) return null;
        return lista.get(opcion - 1);
    }

    public static <T> void listar(String titulo, ArrayList<T> lista) {
        System.out.println("\n== " + titulo + " ==");
        if (lista.isEmpty()) System.out.println("  (sin registros)");
        for (T elemento : lista) System.out.println("  " + elemento);
    }

    public static void titulo(String texto) {
        System.out.println("\n==================================================");
        System.out.println("  " + texto);
        System.out.println("==================================================");
    }

    public static void exito(String mensaje) { System.out.println("[OK] " + mensaje); }

    public static void error(String mensaje) { System.out.println("[ERROR] " + mensaje); }
}
