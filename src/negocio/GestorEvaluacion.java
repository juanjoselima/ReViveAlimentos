package negocio;

import modelo.*;
import java.time.Duration;
import java.util.ArrayList;

/**
 * M2 - Responsables, evaluación y prioridad (RF-05 a RF-08). Responsable: Doménica Arellano.
 */
public class GestorEvaluacion {
    public static final String[] NIVELES = {"", "Baja", "Media", "Alta", "Crítica"};

    private SistemaReVive sistema;

    public GestorEvaluacion(SistemaReVive sistema) {
        this.sistema = sistema;
    }

    // ---------------- RF-05 ----------------

    public Responsable registrarResponsable(String cedula, String nombre, String funcion) throws ReglaNegocioException {
        cedula = Validador.obligatorio(cedula, "Cédula");
        nombre = Validador.obligatorio(nombre, "Nombre");
        validarFuncion(funcion);
        if (!Validador.cedulaValida(cedula)) {
            throw new ReglaNegocioException("La cédula " + cedula + " no es válida (10 dígitos y dígito verificador).");
        }
        if (buscarResponsable(cedula) != null) throw new ReglaNegocioException("Ya existe un responsable con la cédula " + cedula + ".");
        Responsable r = new Responsable(cedula, nombre, funcion);
        sistema.getResponsables().add(r);
        return r;
    }

    public void actualizarResponsable(Responsable r, String nombre, String funcion, boolean disponible)
            throws ReglaNegocioException {
        if (r == null) throw new ReglaNegocioException("El responsable no existe.");
        nombre = Validador.obligatorio(nombre, "Nombre");
        validarFuncion(funcion);
        r.setNombre(nombre);
        r.setFuncion(funcion);
        r.setDisponible(disponible);
    }

    private void validarFuncion(String funcion) throws ReglaNegocioException {
        if (funcion == null || !(funcion.equals(Responsable.EVALUADOR) || funcion.equals(Responsable.RETIRO)
                || funcion.equals(Responsable.ENTREGA))) {
            throw new ReglaNegocioException("La función debe ser Evaluador, Retiro o Entrega.");
        }
    }

    public Responsable buscarResponsable(String cedula) {
        for (Responsable r : sistema.getResponsables()) {
            if (r.getCedula().equals(cedula)) return r;
        }
        return null;
    }

    public ArrayList<Responsable> listarPorFuncion(String funcion) {
        ArrayList<Responsable> lista = new ArrayList<>();
        for (Responsable r : sistema.getResponsables()) {
            if (r.getFuncion().equals(funcion)) lista.add(r);
        }
        return lista;
    }

    // ---------------- RF-06 ----------------

    public Evaluacion evaluarOferta(Oferta oferta, Responsable evaluador, boolean empaqueIntegro,
                                    boolean etiquetaConFecha, boolean temperaturaAdecuada) throws ReglaNegocioException {
        if (oferta == null) throw new ReglaNegocioException("La oferta no existe.");
        if (!oferta.getEstado().equals(Oferta.REGISTRADA)) {
            throw new ReglaNegocioException("Solo se evalúan ofertas en estado Registrada (estado actual: " + oferta.getEstado() + ").");
        }
        if (evaluador == null || !evaluador.getFuncion().equals(Responsable.EVALUADOR)) {
            throw new ReglaNegocioException("Solo un responsable con función de Evaluador puede registrar la evaluación.");
        }
        if (!evaluador.isDisponible()) throw new ReglaNegocioException("El evaluador " + evaluador.getNombre() + " no está disponible.");

        Evaluacion ev = new Evaluacion(oferta, evaluador, empaqueIntegro, etiquetaConFecha, temperaturaAdecuada, Reloj.ahora());
        sistema.getEvaluaciones().add(ev);
        GestorOfertas go = sistema.getGestorOfertas();
        if (ev.isAprobada()) {
            oferta.setApta(true);
            go.cambiarEstado(oferta, Oferta.EVALUADA, "Evaluación aprobada", evaluador.getNombre());
        } else {
            oferta.setMotivoRechazo(ev.getMotivo());
            go.cambiarEstado(oferta, Oferta.RECHAZADA, "Evaluación no aprobada: " + ev.getMotivo(), evaluador.getNombre());
        }
        return ev;
    }

    // ---------------- RF-07 ----------------

    /** Horas que faltan para la hora límite de retiro (negativo si ya pasó). */
    public double calcularHorasRestantes(Oferta oferta) {
        return Duration.between(Reloj.ahora(), oferta.getHoraLimiteRetiro()).toMinutes() / 60.0;
    }

    /** Nivel 4 = Crítica, 3 = Alta, 2 = Media, 1 = Baja. Los perecibles suben un nivel. */
    public int calcularNivelPrioridad(Oferta oferta) {
        Parametros p = sistema.getParametros();
        double horas = calcularHorasRestantes(oferta);
        int nivel;
        if (horas < p.getUmbralCritica()) nivel = 4;
        else if (horas < p.getUmbralAlta()) nivel = 3;
        else if (horas < p.getUmbralMedia()) nivel = 2;
        else nivel = 1;
        if (oferta.getCategoria().isPerecible() && nivel < 4) nivel++;
        return nivel;
    }

    public String nombrePrioridad(Oferta oferta) { return NIVELES[calcularNivelPrioridad(oferta)]; }

    /** Ofertas pendientes ordenadas por prioridad y, en empate, por menor tiempo restante. */
    public ArrayList<Oferta> listarPorPrioridad() {
        ArrayList<Oferta> lista = new ArrayList<>();
        for (Oferta o : sistema.getOfertas()) {
            String e = o.getEstado();
            if (e.equals(Oferta.REGISTRADA) || e.equals(Oferta.EVALUADA) || e.equals(Oferta.ASIGNADA_PARCIAL)) {
                lista.add(o);
            }
        }
        // Ordenamiento por inserción
        for (int i = 1; i < lista.size(); i++) {
            Oferta actual = lista.get(i);
            int j = i - 1;
            while (j >= 0 && vaAntes(actual, lista.get(j))) {
                lista.set(j + 1, lista.get(j));
                j--;
            }
            lista.set(j + 1, actual);
        }
        return lista;
    }

    private boolean vaAntes(Oferta a, Oferta b) {
        int na = calcularNivelPrioridad(a);
        int nb = calcularNivelPrioridad(b);
        if (na != nb) return na > nb;
        return calcularHorasRestantes(a) < calcularHorasRestantes(b);
    }

    // ---------------- RF-08 ----------------

    /** Devuelve null si la oferta es asignable; si no, el motivo del bloqueo. */
    public String motivoNoAsignable(Oferta oferta) {
        if (oferta == null) return "La oferta no existe";
        String e = oferta.getEstado();
        if (e.equals(Oferta.REGISTRADA)) return "Oferta sin evaluar";
        if (e.equals(Oferta.RECHAZADA)) return "Oferta rechazada en la evaluación";
        if (e.equals(Oferta.VENCIDA) || !Reloj.ahora().isBefore(oferta.getHoraLimiteRetiro())) return "Tiempo de retiro agotado";
        if (!(e.equals(Oferta.EVALUADA) || e.equals(Oferta.ASIGNADA_PARCIAL))) return "Oferta en estado " + e;
        if (oferta.calcularDisponible() <= 0) return "Oferta sin cantidad disponible";
        return null;
    }

    public boolean esAsignable(Oferta oferta) { return motivoNoAsignable(oferta) == null; }
}
