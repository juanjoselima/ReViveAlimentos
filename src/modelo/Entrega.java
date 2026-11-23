package modelo;

import java.time.LocalDateTime;

/**
 * Llegada del alimento a la organización receptora (RF-14).
 */
public class Entrega {
    public static final String ENTREGADA = "Entregada";
    public static final String ENTREGADA_PARCIAL = "Entregada parcial";
    public static final String RECHAZADA_RECEPTOR = "Rechazada por el receptor";

    private static int contador = 0;

    private int id;
    private Retiro retiro;
    private Responsable responsable;
    private double cantidadEntregada;
    private String estado;
    private String motivo;
    private LocalDateTime fecha;

    public Entrega(Retiro retiro, Responsable responsable, double cantidadEntregada, String estado,
                   String motivo, LocalDateTime fecha) {
        contador++;
        this.id = contador;
        this.retiro = retiro;
        this.responsable = responsable;
        this.cantidadEntregada = cantidadEntregada;
        this.estado = estado;
        this.motivo = motivo;
        this.fecha = fecha;
    }

    public int getId() { return id; }
    public Retiro getRetiro() { return retiro; }
    public Responsable getResponsable() { return responsable; }
    public double getCantidadEntregada() { return cantidadEntregada; }
    public double getCantidadEntregadaKg() {
        return retiro.getAsignacion().getOferta().getUnidad().aKg(cantidadEntregada);
    }
    public String getEstado() { return estado; }
    public String getMotivo() { return motivo; }
    public LocalDateTime getFecha() { return fecha; }

    @Override
    public String toString() {
        return "Entrega " + id + ": Asignación " + retiro.getAsignacion().getId() + " | Entregado: "
                + Formato.num(cantidadEntregada) + " | Estado: " + estado
                + (motivo.isEmpty() ? "" : " | Motivo: " + motivo);
    }
}
