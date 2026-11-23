package modelo;

/**
 * Persona del equipo operativo: evalúa, retira o entrega alimentos (RF-05).
 */
public class Responsable {
    public static final String EVALUADOR = "Evaluador";
    public static final String RETIRO = "Retiro";
    public static final String ENTREGA = "Entrega";

    private String cedula;
    private String nombre;
    private String funcion;
    private boolean disponible;

    public Responsable(String cedula, String nombre, String funcion) {
        this.cedula = cedula;
        this.nombre = nombre;
        this.funcion = funcion;
        this.disponible = true;
    }

    public String getCedula() { return cedula; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public String getFuncion() { return funcion; }
    public void setFuncion(String funcion) { this.funcion = funcion; }
    public boolean isDisponible() { return disponible; }
    public void setDisponible(boolean disponible) { this.disponible = disponible; }

    @Override
    public String toString() {
        return nombre + " | Cédula " + cedula + " | Función: " + funcion + " | " + (disponible ? "Disponible" : "No disponible");
    }
}
