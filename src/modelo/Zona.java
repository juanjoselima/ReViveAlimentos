package modelo;

import java.util.ArrayList;

/**
 * Sector de Quito donde se ubican donantes y receptores (RF-02).
 * Conoce sus zonas vecinas (adyacentes).
 */
public class Zona {
    private static int contador = 0;

    private int id;
    private String nombre;
    private ArrayList<Zona> adyacentes;

    public Zona(String nombre) {
        contador++;
        this.id = contador;
        this.nombre = nombre;
        this.adyacentes = new ArrayList<>();
    }

    public int getId() { return id; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    /** Devuelve una copia para que nadie modifique la lista desde afuera. */
    public ArrayList<Zona> getAdyacentes() { return new ArrayList<>(adyacentes); }

    public boolean esAdyacente(Zona otra) { return adyacentes.contains(otra); }

    public void agregarAdyacente(Zona otra) {
        if (otra != null && otra != this && !adyacentes.contains(otra)) {
            adyacentes.add(otra);
        }
    }

    public void quitarAdyacente(Zona otra) { adyacentes.remove(otra); }

    @Override
    public String toString() {
        String vecinas = "";
        for (int i = 0; i < adyacentes.size(); i++) {
            vecinas += adyacentes.get(i).getNombre();
            if (i < adyacentes.size() - 1) vecinas += ", ";
        }
        if (vecinas.isEmpty()) vecinas = "ninguna";
        return "Zona " + id + ": " + nombre + " | Adyacentes: " + vecinas;
    }
}
