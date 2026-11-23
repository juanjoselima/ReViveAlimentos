package negocio;

import modelo.*;
import java.util.ArrayList;

/**
 * Punto central de la capa de negocio. Guarda en memoria todas las listas del sistema
 * y crea un gestor para cada módulo. La interfaz solo habla con esta clase y sus gestores.
 */
public class SistemaReVive {
    // Datos en memoria
    private ArrayList<Zona> zonas = new ArrayList<>();
    private ArrayList<Donante> donantes = new ArrayList<>();
    private ArrayList<Oferta> ofertas = new ArrayList<>();
    private ArrayList<Responsable> responsables = new ArrayList<>();
    private ArrayList<Evaluacion> evaluaciones = new ArrayList<>();
    private ArrayList<Receptor> receptores = new ArrayList<>();
    private ArrayList<Asignacion> asignaciones = new ArrayList<>();
    private ArrayList<Retiro> retiros = new ArrayList<>();
    private ArrayList<Entrega> entregas = new ArrayList<>();
    private ArrayList<Incidente> incidentes = new ArrayList<>();
    private ArrayList<Categoria> categorias = new ArrayList<>();
    private ArrayList<Unidad> unidades = new ArrayList<>();
    private ArrayList<CambioParametro> historialParametros = new ArrayList<>();
    private Parametros parametros = new Parametros();

    // Un gestor por módulo
    private GestorDonantes gestorDonantes;          // M1
    private GestorOfertas gestorOfertas;            // M1
    private GestorEvaluacion gestorEvaluacion;      // M2
    private GestorAsignaciones gestorAsignaciones;  // M3
    private GestorLogistica gestorLogistica;        // M4
    private GestorParametros gestorParametros;      // M5
    private GestorIndicadores gestorIndicadores;    // M5
    private BitacoraTrazabilidad bitacora;          // M5

    public SistemaReVive() {
        bitacora = new BitacoraTrazabilidad();
        gestorDonantes = new GestorDonantes(this);
        gestorOfertas = new GestorOfertas(this);
        gestorEvaluacion = new GestorEvaluacion(this);
        gestorAsignaciones = new GestorAsignaciones(this);
        gestorLogistica = new GestorLogistica(this);
        gestorParametros = new GestorParametros(this);
        gestorIndicadores = new GestorIndicadores(this);
        cargarCatalogosBase();
    }

    /** Categorías y unidades mínimas para empezar a trabajar. */
    private void cargarCatalogosBase() {
        categorias.add(new Categoria("Panadería", false));
        categorias.add(new Categoria("Frutas y verduras", false));
        categorias.add(new Categoria("Lácteos", true));
        categorias.add(new Categoria("Comidas preparadas", true));
        categorias.add(new Categoria("Abarrotes", false));
        unidades.add(new Unidad("kg", 1.0));
        unidades.add(new Unidad("litro", 1.0));
        unidades.add(new Unidad("bandeja", 1.5));
        unidades.add(new Unidad("porción", 0.4));
    }

    /**
     * Reglas automáticas por tiempo: expira propuestas (RF-12) y vence ofertas (RF-04).
     * La interfaz la llama antes de mostrar cada menú.
     */
    public void actualizarEstadosAutomaticos() {
        gestorAsignaciones.expirarPropuestas();
        gestorOfertas.revisarVencimientos();
    }

    public ArrayList<Zona> getZonas() { return zonas; }
    public ArrayList<Donante> getDonantes() { return donantes; }
    public ArrayList<Oferta> getOfertas() { return ofertas; }
    public ArrayList<Responsable> getResponsables() { return responsables; }
    public ArrayList<Evaluacion> getEvaluaciones() { return evaluaciones; }
    public ArrayList<Receptor> getReceptores() { return receptores; }
    public ArrayList<Asignacion> getAsignaciones() { return asignaciones; }
    public ArrayList<Retiro> getRetiros() { return retiros; }
    public ArrayList<Entrega> getEntregas() { return entregas; }
    public ArrayList<Incidente> getIncidentes() { return incidentes; }
    public ArrayList<Categoria> getCategorias() { return categorias; }
    public ArrayList<Unidad> getUnidades() { return unidades; }
    public ArrayList<CambioParametro> getHistorialParametros() { return historialParametros; }
    public Parametros getParametros() { return parametros; }

    public GestorDonantes getGestorDonantes() { return gestorDonantes; }
    public GestorOfertas getGestorOfertas() { return gestorOfertas; }
    public GestorEvaluacion getGestorEvaluacion() { return gestorEvaluacion; }
    public GestorAsignaciones getGestorAsignaciones() { return gestorAsignaciones; }
    public GestorLogistica getGestorLogistica() { return gestorLogistica; }
    public GestorParametros getGestorParametros() { return gestorParametros; }
    public GestorIndicadores getGestorIndicadores() { return gestorIndicadores; }
    public BitacoraTrazabilidad getBitacora() { return bitacora; }
}
