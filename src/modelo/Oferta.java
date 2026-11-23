package modelo;

import java.time.LocalDateTime;
import java.util.ArrayList;

/**
 * Excedente de alimento que un donante ofrece (RF-03).
 * Conoce sus asignaciones para calcular cuánto queda disponible (RF-04).
 */
public class Oferta {
    // Estados posibles de una oferta (constantes)
    public static final String REGISTRADA = "Registrada";
    public static final String EVALUADA = "Evaluada";
    public static final String RECHAZADA = "Rechazada";
    public static final String ASIGNADA_PARCIAL = "Asignada parcial";
    public static final String ASIGNADA_TOTAL = "Asignada total";
    public static final String CERRADA = "Cerrada";
    public static final String VENCIDA = "Vencida";
    public static final String CANCELADA = "Cancelada";

    private static int contador = 0;

    private int id;
    private Donante donante;
    private Categoria categoria;
    private double cantidad;
    private Unidad unidad;
    private LocalDateTime fechaCaducidad;
    private LocalDateTime horaLimiteRetiro;
    private LocalDateTime fechaRegistro;
    private String estado;
    private String motivoRechazo;
    private boolean apta;
    private ArrayList<Asignacion> asignaciones;

    public Oferta(Donante donante, Categoria categoria, double cantidad, Unidad unidad,
                  LocalDateTime fechaCaducidad, LocalDateTime horaLimiteRetiro, LocalDateTime fechaRegistro) {
        contador++;
        this.id = contador;
        this.donante = donante;
        this.categoria = categoria;
        this.cantidad = cantidad;
        this.unidad = unidad;
        this.fechaCaducidad = fechaCaducidad;
        this.horaLimiteRetiro = horaLimiteRetiro;
        this.fechaRegistro = fechaRegistro;
        this.estado = REGISTRADA;
        this.motivoRechazo = "";
        this.apta = false;
        this.asignaciones = new ArrayList<>();
    }

    public int getId() { return id; }
    public Donante getDonante() { return donante; }
    public Categoria getCategoria() { return categoria; }
    public void setCategoria(Categoria categoria) { this.categoria = categoria; }
    public double getCantidad() { return cantidad; }
    public void setCantidad(double cantidad) { this.cantidad = cantidad; }
    public Unidad getUnidad() { return unidad; }
    public void setUnidad(Unidad unidad) { this.unidad = unidad; }
    public LocalDateTime getFechaCaducidad() { return fechaCaducidad; }
    public void setFechaCaducidad(LocalDateTime fechaCaducidad) { this.fechaCaducidad = fechaCaducidad; }
    public LocalDateTime getHoraLimiteRetiro() { return horaLimiteRetiro; }
    public void setHoraLimiteRetiro(LocalDateTime horaLimiteRetiro) { this.horaLimiteRetiro = horaLimiteRetiro; }
    public LocalDateTime getFechaRegistro() { return fechaRegistro; }
    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }
    public String getMotivoRechazo() { return motivoRechazo; }
    public void setMotivoRechazo(String motivoRechazo) { this.motivoRechazo = motivoRechazo; }
    public boolean isApta() { return apta; }
    public void setApta(boolean apta) { this.apta = apta; }
    public ArrayList<Asignacion> getAsignaciones() { return new ArrayList<>(asignaciones); }

    public void agregarAsignacion(Asignacion asignacion) { asignaciones.add(asignacion); }

    /** Cantidad comprometida = suma de asignaciones propuestas y confirmadas (RF-04). */
    public double calcularComprometida() {
        double suma = 0;
        for (Asignacion a : asignaciones) {
            if (a.estaActiva()) suma += a.getCantidad();
        }
        return suma;
    }

    /** Cantidad ya confirmada (o cerrada) a receptores. */
    public double calcularConfirmada() {
        double suma = 0;
        for (Asignacion a : asignaciones) {
            if (a.getEstado().equals(Asignacion.CONFIRMADA) || a.estaCerrada()) suma += a.getCantidad();
        }
        return suma;
    }

    /** Disponible = ofrecida - comprometida; nunca negativo (RF-04). */
    public double calcularDisponible() {
        double disponible = cantidad - calcularComprometida();
        if (disponible < 0) disponible = 0;
        return disponible;
    }

    public double getCantidadKg() { return unidad.aKg(cantidad); }

    public boolean tieneAsignacionesConfirmadas() {
        for (Asignacion a : asignaciones) {
            if (a.getEstado().equals(Asignacion.CONFIRMADA) || a.estaCerrada()) return true;
        }
        return false;
    }

    @Override
    public String toString() {
        return "Oferta " + id + ": " + cantidad + " " + unidad.getNombre() + " de " + categoria.getNombre()
                + " | Donante: " + donante.getNombre() + " (" + donante.getZona().getNombre() + ")"
                + " | Límite retiro: " + Formato.fechaHora(horaLimiteRetiro)
                + " | Caduca: " + Formato.fechaHora(fechaCaducidad)
                + " | Disponible: " + Formato.num(calcularDisponible()) + " " + unidad.getNombre()
                + " | Estado: " + estado
                + (estado.equals(RECHAZADA) ? " (" + motivoRechazo + ")" : "");
    }
}
