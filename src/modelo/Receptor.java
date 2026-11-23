package modelo;

import java.util.ArrayList;

/**
 * Organización que recibe los alimentos (fundación, comedor comunitario). RF-09.
 */
public class Receptor extends Organizacion {
    private static int contador = 0;

    private int id;
    private double capacidadDiariaKg;
    private ArrayList<FranjaHoraria> franjas;
    private ArrayList<Categoria> categoriasAceptadas;
    private boolean enObservacion;

    public Receptor(String nombre, String contacto, Zona zona, double capacidadDiariaKg) {
        super(nombre, contacto, zona);
        contador++;
        this.id = contador;
        this.capacidadDiariaKg = capacidadDiariaKg;
        this.franjas = new ArrayList<>();
        this.categoriasAceptadas = new ArrayList<>();
        this.enObservacion = false;
    }

    public int getId() { return id; }
    public double getCapacidadDiariaKg() { return capacidadDiariaKg; }
    public void setCapacidadDiariaKg(double capacidadDiariaKg) { this.capacidadDiariaKg = capacidadDiariaKg; }
    public ArrayList<FranjaHoraria> getFranjas() { return new ArrayList<>(franjas); }
    public void setFranjas(ArrayList<FranjaHoraria> franjas) { this.franjas = new ArrayList<>(franjas); }
    public ArrayList<Categoria> getCategoriasAceptadas() { return new ArrayList<>(categoriasAceptadas); }
    public void setCategoriasAceptadas(ArrayList<Categoria> categorias) { this.categoriasAceptadas = new ArrayList<>(categorias); }
    public boolean isEnObservacion() { return enObservacion; }
    public void setEnObservacion(boolean enObservacion) { this.enObservacion = enObservacion; }

    public boolean aceptaCategoria(Categoria categoria) { return categoriasAceptadas.contains(categoria); }

    @Override
    public String getTipoOrganizacion() { return "Receptor"; }

    @Override
    public String toString() {
        String cats = "";
        for (int i = 0; i < categoriasAceptadas.size(); i++) {
            cats += categoriasAceptadas.get(i).getNombre();
            if (i < categoriasAceptadas.size() - 1) cats += ", ";
        }
        return "Receptor " + id + ": " + getNombre() + " | Zona: " + getZona().getNombre()
                + " | Capacidad: " + capacidadDiariaKg + " kg/día | Franjas: " + franjas
                + " | Acepta: " + cats + " | " + (isActivo() ? "Activo" : "Inactivo")
                + (enObservacion ? " | EN OBSERVACIÓN" : "");
    }
}
