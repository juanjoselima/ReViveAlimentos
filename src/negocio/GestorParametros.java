package negocio;

import modelo.*;
import java.util.ArrayList;

/**
 * M5 - Parámetros y catálogos (RF-17). Responsable: Anahí Barahona.
 * Cada cambio guarda el valor anterior y la fecha.
 */
public class GestorParametros {
    private SistemaReVive sistema;

    public GestorParametros(SistemaReVive sistema) {
        this.sistema = sistema;
    }

    private void guardarCambio(String parametro, String anterior, String nuevo) {
        sistema.getHistorialParametros().add(new CambioParametro(parametro, anterior, nuevo, Reloj.ahora()));
    }

    public Categoria agregarCategoria(String nombre, boolean perecible) throws ReglaNegocioException {
        nombre = Validador.obligatorio(nombre, "Nombre de la categoría");
        if (buscarCategoria(nombre) != null) throw new ReglaNegocioException("La categoría '" + nombre + "' ya existe.");
        Categoria c = new Categoria(nombre, perecible);
        sistema.getCategorias().add(c);
        guardarCambio("Categoría", "(no existía)", c.toString());
        return c;
    }

    public Unidad agregarUnidad(String nombre, double factorKg) throws ReglaNegocioException {
        nombre = Validador.obligatorio(nombre, "Nombre de la unidad");
        if (factorKg <= 0) throw new ReglaNegocioException("La equivalencia en kg debe ser mayor a cero.");
        if (buscarUnidad(nombre) != null) throw new ReglaNegocioException("La unidad '" + nombre + "' ya existe.");
        Unidad u = new Unidad(nombre, factorKg);
        sistema.getUnidades().add(u);
        guardarCambio("Unidad", "(no existía)", u.toString());
        return u;
    }

    /** Los umbrales deben ser positivos y crecientes: Crítica < Alta < Media. */
    public void cambiarUmbrales(double critica, double alta, double media) throws ReglaNegocioException {
        if (critica <= 0 || !(critica < alta && alta < media)) {
            throw new ReglaNegocioException("Los umbrales deben ser mayores a cero y crecientes (Crítica < Alta < Media).");
        }
        Parametros p = sistema.getParametros();
        String anterior = p.textoUmbrales();
        p.setUmbrales(critica, alta, media);
        guardarCambio("Umbrales de prioridad", anterior, p.textoUmbrales());
    }

    /** Los pesos del puntaje deben sumar exactamente 100. */
    public void cambiarPesos(int zona, int capacidad, int horario, int historial) throws ReglaNegocioException {
        if (zona < 0 || capacidad < 0 || horario < 0 || historial < 0) {
            throw new ReglaNegocioException("Los pesos no pueden ser negativos.");
        }
        int suma = zona + capacidad + horario + historial;
        if (suma != 100) throw new ReglaNegocioException("Los pesos deben sumar exactamente 100 (suman " + suma + ").");
        Parametros p = sistema.getParametros();
        String anterior = p.textoPesos();
        p.setPesos(zona, capacidad, horario, historial);
        guardarCambio("Pesos del puntaje", anterior, p.textoPesos());
    }

    public void cambiarTolerancia(double porcentaje) throws ReglaNegocioException {
        if (porcentaje < 0 || porcentaje > 100) throw new ReglaNegocioException("La tolerancia debe estar entre 0 y 100 %.");
        Parametros p = sistema.getParametros();
        String anterior = p.getToleranciaPorcentaje() + " %";
        p.setToleranciaPorcentaje(porcentaje);
        guardarCambio("Tolerancia", anterior, porcentaje + " %");
    }

    public void cambiarExpiracion(int minutos) throws ReglaNegocioException {
        if (minutos <= 0 || minutos > 1440) throw new ReglaNegocioException("La expiración debe estar entre 1 y 1440 minutos.");
        Parametros p = sistema.getParametros();
        String anterior = p.getMinutosExpiracion() + " min";
        p.setMinutosExpiracion(minutos);
        guardarCambio("Expiración de propuestas", anterior, minutos + " min");
    }

    public Categoria buscarCategoria(String nombre) {
        for (Categoria c : sistema.getCategorias()) {
            if (c.getNombre().equalsIgnoreCase(nombre.trim())) return c;
        }
        return null;
    }

    public Unidad buscarUnidad(String nombre) {
        for (Unidad u : sistema.getUnidades()) {
            if (u.getNombre().equalsIgnoreCase(nombre.trim())) return u;
        }
        return null;
    }

    public ArrayList<Categoria> listarCategorias() { return new ArrayList<>(sistema.getCategorias()); }
    public ArrayList<Unidad> listarUnidades() { return new ArrayList<>(sistema.getUnidades()); }
    public ArrayList<CambioParametro> listarHistorial() { return new ArrayList<>(sistema.getHistorialParametros()); }
}
