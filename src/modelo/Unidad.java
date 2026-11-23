package modelo;

/**
 * Unidad de medida de una oferta y su equivalencia en kilogramos. RF-17.
 */
public class Unidad {
    private String nombre;
    private double factorKg;

    public Unidad(String nombre, double factorKg) {
        this.nombre = nombre;
        this.factorKg = factorKg;
    }

    public String getNombre() { return nombre; }
    public double getFactorKg() { return factorKg; }

    /** Convierte una cantidad expresada en esta unidad a kilogramos. */
    public double aKg(double cantidad) { return cantidad * factorKg; }

    @Override
    public String toString() { return nombre + " (1 " + nombre + " = " + factorKg + " kg)"; }
}
