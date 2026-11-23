package modelo;

import java.time.LocalDateTime;

/**
 * Historial de un cambio de parámetro: valor anterior, valor nuevo y fecha (RF-17).
 */
public class CambioParametro {
    private final String parametro;
    private final String valorAnterior;
    private final String valorNuevo;
    private final LocalDateTime fecha;

    public CambioParametro(String parametro, String valorAnterior, String valorNuevo, LocalDateTime fecha) {
        this.parametro = parametro;
        this.valorAnterior = valorAnterior;
        this.valorNuevo = valorNuevo;
        this.fecha = fecha;
    }

    public String getParametro() { return parametro; }
    public String getValorAnterior() { return valorAnterior; }
    public String getValorNuevo() { return valorNuevo; }
    public LocalDateTime getFecha() { return fecha; }

    @Override
    public String toString() {
        return Formato.fechaHora(fecha) + " | " + parametro + ": " + valorAnterior + " -> " + valorNuevo;
    }
}
