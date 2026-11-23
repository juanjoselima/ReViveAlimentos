package modelo;

/**
 * Vehículo de la red que hace los retiros (RF-21, módulo adicional M6).
 */
public class Vehiculo {
    private String placa;
    private String descripcion;
    private double capacidadKg;
    private boolean refrigerado;
    private boolean activo;

    public Vehiculo(String placa, String descripcion, double capacidadKg, boolean refrigerado) {
        this.placa = placa;
        this.descripcion = descripcion;
        this.capacidadKg = capacidadKg;
        this.refrigerado = refrigerado;
        this.activo = true;
    }

    public String getPlaca() { return placa; }
    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }
    public double getCapacidadKg() { return capacidadKg; }
    public void setCapacidadKg(double capacidadKg) { this.capacidadKg = capacidadKg; }
    public boolean isRefrigerado() { return refrigerado; }
    public void setRefrigerado(boolean refrigerado) { this.refrigerado = refrigerado; }
    public boolean isActivo() { return activo; }
    public void setActivo(boolean activo) { this.activo = activo; }

    @Override
    public String toString() {
        return "Vehículo " + placa + " | " + descripcion + " | Capacidad: " + capacidadKg + " kg | "
                + (refrigerado ? "Refrigerado" : "Sin refrigeración") + " | " + (activo ? "Activo" : "Inactivo");
    }
}
