package negocio;

import java.time.LocalDateTime;

/**
 * Entrega la hora actual del sistema. Permite "adelantar" el tiempo para probar
 * vencimientos y expiraciones sin esperar horas reales.
 */
public class Reloj {
    private static int minutosAdelantados = 0;

    public static LocalDateTime ahora() {
        return LocalDateTime.now().withSecond(0).withNano(0).plusMinutes(minutosAdelantados);
    }

    public static void adelantar(int minutos) {
        if (minutos > 0) minutosAdelantados += minutos;
    }

    public static int getMinutosAdelantados() { return minutosAdelantados; }
}
