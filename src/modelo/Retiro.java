package modelo;

import java.time.LocalDateTime;

/**
 * Recogida del alimento en el local del donante (RF-13).
 */
public class Retiro {
    public static final String PROGRAMADO = "Programado";
    public static final String COMPLETADO = "Completado";
    public static final int DURACION_MINUTOS = 60;

    private static int contador = 0;

    private int id;
    private Asignacion asignacion;
    private Responsable responsable;
    private LocalDateTime horaProgramada;
    private double cantidadRecogida;
    private String estado;
    private LocalDateTime fechaRecogida;

    public Retiro(Asignacion asignacion, Responsable responsable, LocalDateTime horaProgramada) {
        contador++;
        this.id = contador;
        this.asignacion = asignacion;
        this.responsable = responsable;
        this.horaProgramada = horaProgramada;
        this.cantidadRecogida = 0;
        this.estado = PROGRAMADO;
    }

    public int getId() { return id; }
    public Asignacion getAsignacion() { return asignacion; }
    public Responsable getResponsable() { return responsable; }
    public LocalDateTime getHoraProgramada() { return horaProgramada; }
    public LocalDateTime getHoraFin() { return horaProgramada.plusMinutes(DURACION_MINUTOS); }
    public double getCantidadRecogida() { return cantidadRecogida; }
    public String getEstado() { return estado; }
    public LocalDateTime getFechaRecogida() { return fechaRecogida; }

    /** Registra lo recogido y marca el retiro como completado. */
    public void completar(double cantidadRecogida, LocalDateTime fecha) {
        this.cantidadRecogida = cantidadRecogida;
        this.fechaRecogida = fecha;
        this.estado = COMPLETADO;
    }

    /** Un retiro ocupa al responsable desde la hora programada hasta una hora después. */
    public boolean seCruzaCon(LocalDateTime otraHora) {
        LocalDateTime otraFin = otraHora.plusMinutes(DURACION_MINUTOS);
        return horaProgramada.isBefore(otraFin) && otraHora.isBefore(getHoraFin());
    }

    @Override
    public String toString() {
        return "Retiro " + id + ": Asignación " + asignacion.getId() + " | " + responsable.getNombre()
                + " | " + Formato.fechaHora(horaProgramada) + " | Estado: " + estado
                + (estado.equals(COMPLETADO) ? " | Recogido: " + Formato.num(cantidadRecogida) : "");
    }
}
