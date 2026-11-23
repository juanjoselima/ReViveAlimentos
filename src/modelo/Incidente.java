package modelo;

import java.time.Duration;
import java.time.LocalDateTime;

/**
 * Problema ocurrido durante el retiro o la entrega (RF-15).
 */
public class Incidente {
    public static final String RETRASO = "Retraso";
    public static final String MAL_ESTADO = "Producto en mal estado";
    public static final String RECEPTOR_AUSENTE = "Receptor ausente";
    public static final String DIFERENCIA = "Diferencia de cantidad";
    public static final String[] TIPOS = {RETRASO, MAL_ESTADO, RECEPTOR_AUSENTE, DIFERENCIA};

    public static final String ABIERTO = "Abierto";
    public static final String CERRADO = "Cerrado";

    private static int contador = 0;

    private int id;
    private String tipo;
    private Asignacion asignacion;
    private String descripcion;
    private String estado;
    private String resolucion;
    private LocalDateTime fechaApertura;
    private LocalDateTime fechaCierre;

    public Incidente(String tipo, Asignacion asignacion, String descripcion, LocalDateTime fechaApertura) {
        contador++;
        this.id = contador;
        this.tipo = tipo;
        this.asignacion = asignacion;
        this.descripcion = descripcion;
        this.fechaApertura = fechaApertura;
        this.estado = ABIERTO;
        this.resolucion = "";
    }

    public int getId() { return id; }
    public String getTipo() { return tipo; }
    public Asignacion getAsignacion() { return asignacion; }
    public Receptor getReceptor() { return asignacion.getReceptor(); }
    public String getDescripcion() { return descripcion; }
    public String getEstado() { return estado; }
    public String getResolucion() { return resolucion; }
    public LocalDateTime getFechaApertura() { return fechaApertura; }
    public LocalDateTime getFechaCierre() { return fechaCierre; }
    public boolean estaAbierto() { return estado.equals(ABIERTO); }

    /** Cierra el incidente guardando la resolución escrita. */
    public void cerrar(String resolucion, LocalDateTime fecha) {
        this.resolucion = resolucion;
        this.fechaCierre = fecha;
        this.estado = CERRADO;
    }

    /** Horas que tomó resolverlo (solo si está cerrado). */
    public double horasResolucion() {
        if (fechaCierre == null) return 0;
        return Duration.between(fechaApertura, fechaCierre).toMinutes() / 60.0;
    }

    @Override
    public String toString() {
        return "Incidente " + id + ": " + tipo + " | Asignación " + asignacion.getId() + " | Receptor: "
                + getReceptor().getNombre() + " | " + Formato.fechaHora(fechaApertura) + " | " + estado
                + " | " + descripcion + (resolucion.isEmpty() ? "" : " | Resolución: " + resolucion);
    }
}
