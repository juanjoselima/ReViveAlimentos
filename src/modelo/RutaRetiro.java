package modelo;

import java.time.LocalDate;
import java.util.ArrayList;

/**
 * Recorrido de un día que hace un vehículo con un responsable, pasando por varios retiros
 * (RF-22 a RF-24, módulo adicional M6). Las paradas se mantienen ordenadas por hora.
 */
public class RutaRetiro {
    public static final String PLANIFICADA = "Planificada";
    public static final String EN_CURSO = "En curso";
    public static final String FINALIZADA = "Finalizada";

    private static int contador = 0;

    private int id;
    private LocalDate fecha;
    private Vehiculo vehiculo;
    private Responsable responsable;
    private ArrayList<Retiro> paradas;
    private String estado;

    public RutaRetiro(LocalDate fecha, Vehiculo vehiculo, Responsable responsable) {
        contador++;
        this.id = contador;
        this.fecha = fecha;
        this.vehiculo = vehiculo;
        this.responsable = responsable;
        this.paradas = new ArrayList<>();
        this.estado = PLANIFICADA;
    }

    public int getId() { return id; }
    public LocalDate getFecha() { return fecha; }
    public Vehiculo getVehiculo() { return vehiculo; }
    public Responsable getResponsable() { return responsable; }
    public ArrayList<Retiro> getParadas() { return new ArrayList<>(paradas); }
    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }

    /** Inserta el retiro en la posición que le toca según su hora programada. */
    public void agregarParada(Retiro retiro) {
        int pos = 0;
        while (pos < paradas.size() && !paradas.get(pos).getHoraProgramada().isAfter(retiro.getHoraProgramada())) {
            pos++;
        }
        paradas.add(pos, retiro);
    }

    public void quitarParada(Retiro retiro) { paradas.remove(retiro); }

    public boolean contiene(Retiro retiro) { return paradas.contains(retiro); }

    /** Kilos que se planea recoger (cantidad asignada de cada parada). */
    public double kgPlanificados() {
        double total = 0;
        for (Retiro r : paradas) total += r.getAsignacion().getCantidadKg();
        return total;
    }

    /** Kilos realmente recogidos en las paradas completadas. */
    public double kgRecogidos() {
        double total = 0;
        for (Retiro r : paradas) {
            if (r.getEstado().equals(Retiro.COMPLETADO)) {
                total += r.getAsignacion().getOferta().getUnidad().aKg(r.getCantidadRecogida());
            }
        }
        return total;
    }

    /** % de ocupación = kg planificados ÷ capacidad del vehículo × 100. */
    public double porcentajeOcupacion() {
        if (vehiculo.getCapacidadKg() <= 0) return 0;
        return kgPlanificados() / vehiculo.getCapacidadKg() * 100;
    }

    public boolean llevaPerecibles() {
        for (Retiro r : paradas) {
            if (r.getAsignacion().getOferta().getCategoria().isPerecible()) return true;
        }
        return false;
    }

    @Override
    public String toString() {
        return "Ruta " + id + " | " + fecha + " | Vehículo " + vehiculo.getPlaca() + " | " + responsable.getNombre()
                + " | Paradas: " + paradas.size() + " | Ocupación: " + Formato.num(porcentajeOcupacion()) + " % | Estado: " + estado;
    }
}
