package modelo;

/**
 * Tipo de alimento (panadería, lácteos, etc.). Los perecibles requieren refrigeración. RF-17.
 */
public class Categoria {
    private String nombre;
    private boolean perecible;

    public Categoria(String nombre, boolean perecible) {
        this.nombre = nombre;
        this.perecible = perecible;
    }

    public String getNombre() { return nombre; }
    public boolean isPerecible() { return perecible; }
    public void setPerecible(boolean perecible) { this.perecible = perecible; }

    @Override
    public String toString() { return nombre + (perecible ? " (perecible, requiere refrigeración)" : ""); }
}
