package modelo;

/**
 * Clase padre (abstracta) de Donante y Receptor.
 * Agrupa los datos que comparten ambas organizaciones (herencia).
 */
public abstract class Organizacion {
    private String nombre;
    private String contacto;
    private Zona zona;
    private boolean activo;

    public Organizacion(String nombre, String contacto, Zona zona) {
        this.nombre = nombre;
        this.contacto = contacto;
        this.zona = zona;
        this.activo = true;
    }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public String getContacto() { return contacto; }
    public void setContacto(String contacto) { this.contacto = contacto; }
    public Zona getZona() { return zona; }
    public void setZona(Zona zona) { this.zona = zona; }
    public boolean isActivo() { return activo; }
    public void setActivo(boolean activo) { this.activo = activo; }

    /** Cada hija indica qué tipo de organización es. */
    public abstract String getTipoOrganizacion();
}
