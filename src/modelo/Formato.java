package modelo;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Utilidad pequeña para mostrar fechas y números de forma uniforme.
 */
public class Formato {
    public static final DateTimeFormatter FECHA_HORA = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    public static String fechaHora(LocalDateTime fecha) {
        if (fecha == null) return "-";
        return fecha.format(FECHA_HORA);
    }

    /** Redondea a 2 decimales para mostrar. */
    public static String num(double valor) {
        return String.format("%.2f", valor);
    }
}
