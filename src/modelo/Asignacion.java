package modelo;

import java.time.LocalDateTime;

/**
 * Destino que se da a una parte (o a toda) una oferta: qué receptor recibe y cuánto (RF-12).
 */
public class Asignacion {
    public static final String PROPUESTA = "Propuesta";
    public static final String CONFIRMADA = "Confirmada";
    public static final String RECHAZADA = "Rechazada";
    public static final String CANCELADA = "Cancelada";
    public static final String EXPIRADA = "Expirada";
    public static final String CERRADA_CONFORME = "Cerrada conforme";
    public static final String CERRADA_DIFERENCIA = "Cerrada con diferencia";

    private static int contador = 0;

    private int id;
    private Oferta oferta;
    private Receptor receptor;
    private double cantidad;
    private double puntaje;
    private String estado;
    private LocalDateTime fechaPropuesta;
    private LocalDateTime fechaConfirmacion;

    public Asignacion(Oferta oferta, Receptor receptor, double cantidad, double puntaje, LocalDateTime fechaPropuesta) {
        contador++;
        this.id = contador;
        this.oferta = oferta;
        this.receptor = receptor;
        this.cantidad = cantidad;
        this.puntaje = puntaje;
        this.fechaPropuesta = fechaPropuesta;
        this.estado = PROPUESTA;
    }

    public int getId() { return id; }
    public Oferta getOferta() { return oferta; }
    public Receptor getReceptor() { return receptor; }
    public double getCantidad() { return cantidad; }
    public double getCantidadKg() { return oferta.getUnidad().aKg(cantidad); }
    public double getPuntaje() { return puntaje; }
    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }
    public LocalDateTime getFechaPropuesta() { return fechaPropuesta; }
    public LocalDateTime getFechaConfirmacion() { return fechaConfirmacion; }
    public void setFechaConfirmacion(LocalDateTime fechaConfirmacion) { this.fechaConfirmacion = fechaConfirmacion; }

    /** Activa = todavía compromete cantidad de la oferta. */
    public boolean estaActiva() {
        return estado.equals(PROPUESTA) || estado.equals(CONFIRMADA);
    }

    public boolean estaCerrada() {
        return estado.equals(CERRADA_CONFORME) || estado.equals(CERRADA_DIFERENCIA);
    }

    @Override
    public String toString() {
        return "Asignación " + id + ": Oferta " + oferta.getId() + " (" + oferta.getCategoria().getNombre() + ") -> "
                + receptor.getNombre() + " | " + Formato.num(cantidad) + " " + oferta.getUnidad().getNombre()
                + " | Puntaje: " + Formato.num(puntaje) + " | Estado: " + estado;
    }
}
