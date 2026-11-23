package modelo;

import java.time.LocalDateTime;

/**
 * Línea de la bitácora de una oferta (RF-20).
 * Sus atributos son final y no tiene set: una vez creado no se puede modificar.
 */
public class RegistroTrazabilidad {
    private final LocalDateTime fecha;
    private final int idOferta;
    private final String estadoAnterior;
    private final String estadoNuevo;
    private final String detalle;
    private final String responsable;

    public RegistroTrazabilidad(LocalDateTime fecha, int idOferta, String estadoAnterior, String estadoNuevo,
                                String detalle, String responsable) {
        this.fecha = fecha;
        this.idOferta = idOferta;
        this.estadoAnterior = estadoAnterior;
        this.estadoNuevo = estadoNuevo;
        this.detalle = detalle;
        this.responsable = responsable;
    }

    public LocalDateTime getFecha() { return fecha; }
    public int getIdOferta() { return idOferta; }
    public String getEstadoAnterior() { return estadoAnterior; }
    public String getEstadoNuevo() { return estadoNuevo; }
    public String getDetalle() { return detalle; }
    public String getResponsable() { return responsable; }

    @Override
    public String toString() {
        String cambio = estadoAnterior.equals(estadoNuevo) ? estadoNuevo : estadoAnterior + " -> " + estadoNuevo;
        return Formato.fechaHora(fecha) + " | " + cambio + " | " + detalle + " | Responsable: " + responsable;
    }
}
