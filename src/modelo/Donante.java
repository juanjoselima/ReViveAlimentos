package modelo;

/**
 * Establecimiento que entrega sus excedentes (panadería, cafetería, restaurante). RF-01.
 */
public class Donante extends Organizacion {
    private static int contador = 0;

    private int id;
    private String ruc;
    private String tipo;

    public Donante(String nombre, String ruc, String tipo, String contacto, Zona zona) {
        super(nombre, contacto, zona);
        contador++;
        this.id = contador;
        this.ruc = ruc;
        this.tipo = tipo;
    }

    public int getId() { return id; }
    public String getRuc() { return ruc; }
    public String getTipo() { return tipo; }
    public void setTipo(String tipo) { this.tipo = tipo; }

    @Override
    public String getTipoOrganizacion() { return "Donante"; }

    @Override
    public String toString() {
        return "Donante " + id + ": " + getNombre() + " | RUC " + ruc + " | " + tipo
                + " | Contacto: " + getContacto() + " | Zona: " + getZona().getNombre()
                + " | " + (isActivo() ? "Activo" : "Inactivo");
    }
}
