package modelo;

import java.time.LocalTime;

/**
 * Horario en el que un receptor puede recibir alimentos (RF-09).
 */
public class FranjaHoraria {
    private LocalTime inicio;
    private LocalTime fin;

    public FranjaHoraria(LocalTime inicio, LocalTime fin) {
        this.inicio = inicio;
        this.fin = fin;
    }

    public LocalTime getInicio() { return inicio; }
    public LocalTime getFin() { return fin; }

    /** Dos franjas se superponen si una empieza antes de que termine la otra. */
    public boolean seSuperponeCon(FranjaHoraria otra) {
        return this.inicio.isBefore(otra.fin) && otra.inicio.isBefore(this.fin);
    }

    @Override
    public String toString() { return inicio + "-" + fin; }
}
