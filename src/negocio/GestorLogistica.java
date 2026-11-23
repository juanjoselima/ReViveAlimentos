package negocio;

import modelo.*;
import java.time.LocalDateTime;
import java.util.ArrayList;

/**
 * M4 - Retiros, entregas, incidentes y cierre (RF-13 a RF-16). Responsable: Juliana Barreiro.
 */
public class GestorLogistica {
    private static final double MARGEN = 0.0001;

    private SistemaReVive sistema;

    public GestorLogistica(SistemaReVive sistema) {
        this.sistema = sistema;
    }

    // ---------------- RF-13 ----------------

    public Retiro programarRetiro(Asignacion a, Responsable responsable, LocalDateTime hora) throws ReglaNegocioException {
        if (a == null) throw new ReglaNegocioException("La asignación no existe.");
        if (!a.getEstado().equals(Asignacion.CONFIRMADA)) {
            throw new ReglaNegocioException("Solo se programa el retiro de asignaciones Confirmadas (estado actual: " + a.getEstado() + ").");
        }
        if (buscarRetiroDeAsignacion(a) != null) throw new ReglaNegocioException("La asignación ya tiene un retiro programado.");
        if (responsable == null || !responsable.getFuncion().equals(Responsable.RETIRO)) {
            throw new ReglaNegocioException("El responsable debe tener la función de Retiro.");
        }
        if (!responsable.isDisponible()) throw new ReglaNegocioException("El responsable " + responsable.getNombre() + " no está disponible.");
        if (hora == null) throw new ReglaNegocioException("La hora del retiro es obligatoria.");
        if (hora.isBefore(Reloj.ahora())) throw new ReglaNegocioException("La hora del retiro no puede estar en el pasado.");
        if (hora.isAfter(a.getOferta().getHoraLimiteRetiro())) {
            throw new ReglaNegocioException("La hora del retiro no puede ser posterior a la hora límite ("
                    + Formato.fechaHora(a.getOferta().getHoraLimiteRetiro()) + ").");
        }
        for (Retiro otro : sistema.getRetiros()) {
            if (otro.getResponsable() == responsable && otro.getEstado().equals(Retiro.PROGRAMADO) && otro.seCruzaCon(hora)) {
                throw new ReglaNegocioException(responsable.getNombre() + " ya tiene el retiro " + otro.getId()
                        + " a las " + Formato.fechaHora(otro.getHoraProgramada()) + " (horarios superpuestos).");
            }
        }
        Retiro r = new Retiro(a, responsable, hora);
        sistema.getRetiros().add(r);
        Oferta o = a.getOferta();
        sistema.getBitacora().registrar(o, o.getEstado(), o.getEstado(),
                "Retiro " + r.getId() + " programado para " + Formato.fechaHora(hora), responsable.getNombre());
        return r;
    }

    public void registrarRecogida(Retiro r, double cantidadRecogida) throws ReglaNegocioException {
        if (r == null) throw new ReglaNegocioException("El retiro no existe.");
        if (!r.getEstado().equals(Retiro.PROGRAMADO)) throw new ReglaNegocioException("El retiro ya fue completado.");
        if (cantidadRecogida < 0) throw new ReglaNegocioException("La cantidad recogida no puede ser negativa.");
        if (cantidadRecogida > r.getAsignacion().getCantidad() + MARGEN) {
            throw new ReglaNegocioException("La cantidad recogida no puede superar la asignada ("
                    + Formato.num(r.getAsignacion().getCantidad()) + ").");
        }
        r.completar(cantidadRecogida, Reloj.ahora());
        Oferta o = r.getAsignacion().getOferta();
        sistema.getBitacora().registrar(o, o.getEstado(), o.getEstado(),
                "Retiro " + r.getId() + " completado: " + Formato.num(cantidadRecogida) + " recogidos", r.getResponsable().getNombre());
    }

    public Retiro buscarRetiro(int id) {
        for (Retiro r : sistema.getRetiros()) {
            if (r.getId() == id) return r;
        }
        return null;
    }

    public Retiro buscarRetiroDeAsignacion(Asignacion a) {
        for (Retiro r : sistema.getRetiros()) {
            if (r.getAsignacion() == a) return r;
        }
        return null;
    }

    // ---------------- RF-14 ----------------

    public Entrega registrarEntrega(Retiro r, Responsable responsable, double cantidad, boolean rechazadaPorReceptor,
                                    String motivo) throws ReglaNegocioException {
        if (r == null) throw new ReglaNegocioException("El retiro no existe.");
        if (!r.getEstado().equals(Retiro.COMPLETADO)) throw new ReglaNegocioException("Solo se registra la entrega de un retiro Completado.");
        if (buscarEntregaDeRetiro(r) != null) throw new ReglaNegocioException("Este retiro ya tiene una entrega registrada.");
        if (responsable == null || !responsable.getFuncion().equals(Responsable.ENTREGA)) {
            throw new ReglaNegocioException("El responsable debe tener la función de Entrega.");
        }
        String estado;
        if (rechazadaPorReceptor) {
            motivo = Validador.obligatorio(motivo, "Motivo del rechazo");
            cantidad = 0;
            estado = Entrega.RECHAZADA_RECEPTOR;
        } else {
            if (cantidad <= 0) throw new ReglaNegocioException("La cantidad entregada debe ser mayor a cero (o registre un rechazo).");
            if (cantidad > r.getCantidadRecogida() + MARGEN) {
                throw new ReglaNegocioException("La cantidad entregada no puede superar la recogida ("
                        + Formato.num(r.getCantidadRecogida()) + ").");
            }
            estado = (Math.abs(cantidad - r.getCantidadRecogida()) < MARGEN) ? Entrega.ENTREGADA : Entrega.ENTREGADA_PARCIAL;
            motivo = "";
        }
        Entrega e = new Entrega(r, responsable, cantidad, estado, motivo, Reloj.ahora());
        sistema.getEntregas().add(e);
        Oferta o = r.getAsignacion().getOferta();
        sistema.getBitacora().registrar(o, o.getEstado(), o.getEstado(),
                "Entrega " + e.getId() + " registrada: " + estado + " (" + Formato.num(cantidad) + ")", responsable.getNombre());
        return e;
    }

    public Entrega buscarEntregaDeRetiro(Retiro r) {
        for (Entrega e : sistema.getEntregas()) {
            if (e.getRetiro() == r) return e;
        }
        return null;
    }

    // ---------------- RF-15 ----------------

    public Incidente registrarIncidente(String tipo, Asignacion a, String descripcion) throws ReglaNegocioException {
        boolean tipoValido = false;
        for (String t : Incidente.TIPOS) {
            if (t.equals(tipo)) tipoValido = true;
        }
        if (!tipoValido) throw new ReglaNegocioException("Tipo de incidente no válido.");
        if (a == null) throw new ReglaNegocioException("La asignación no existe.");
        if (!a.getEstado().equals(Asignacion.CONFIRMADA)) {
            throw new ReglaNegocioException("Solo se registran incidentes sobre asignaciones Confirmadas (en curso).");
        }
        descripcion = Validador.obligatorio(descripcion, "Descripción");
        Incidente inc = new Incidente(tipo, a, descripcion, Reloj.ahora());
        sistema.getIncidentes().add(inc);
        Oferta o = a.getOferta();
        sistema.getBitacora().registrar(o, o.getEstado(), o.getEstado(),
                "Incidente " + inc.getId() + " abierto: " + tipo, "Coordinador");
        sistema.getGestorIndicadores().actualizarObservacion(a.getReceptor());   // RF-19
        return inc;
    }

    /** Para cerrar un incidente es obligatorio escribir la resolución. */
    public void cerrarIncidente(Incidente inc, String resolucion) throws ReglaNegocioException {
        if (inc == null) throw new ReglaNegocioException("El incidente no existe.");
        if (!inc.estaAbierto()) throw new ReglaNegocioException("El incidente ya está cerrado.");
        resolucion = Validador.obligatorio(resolucion, "Resolución");
        inc.cerrar(resolucion, Reloj.ahora());
        Oferta o = inc.getAsignacion().getOferta();
        sistema.getBitacora().registrar(o, o.getEstado(), o.getEstado(), "Incidente " + inc.getId() + " cerrado", "Coordinador");
    }

    public boolean tieneIncidenteAbierto(Asignacion a) {
        for (Incidente i : sistema.getIncidentes()) {
            if (i.getAsignacion() == a && i.estaAbierto()) return true;
        }
        return false;
    }

    public int contarIncidentesRecientes(Receptor r, int dias) {
        LocalDateTime desde = Reloj.ahora().minusDays(dias);
        int total = 0;
        for (Incidente i : sistema.getIncidentes()) {
            if (i.getReceptor() == r && i.getFechaApertura().isAfter(desde)) total++;
        }
        return total;
    }

    public Incidente buscarIncidente(int id) {
        for (Incidente i : sistema.getIncidentes()) {
            if (i.getId() == id) return i;
        }
        return null;
    }

    // ---------------- RF-16 ----------------

    public double mermaRetiro(Asignacion a) {
        Retiro r = buscarRetiroDeAsignacion(a);
        return (r == null) ? 0 : a.getCantidad() - r.getCantidadRecogida();
    }

    public double mermaEntrega(Asignacion a) {
        Retiro r = buscarRetiroDeAsignacion(a);
        if (r == null) return 0;
        Entrega e = buscarEntregaDeRetiro(r);
        return (e == null) ? 0 : r.getCantidadRecogida() - e.getCantidadEntregada();
    }

    /** Porcentaje de una cantidad respecto a la asignada. */
    public double porcentaje(double valor, Asignacion a) {
        if (a.getCantidad() <= 0) return 0;
        return valor / a.getCantidad() * 100;
    }

    /** Texto con el cálculo de mermas de una asignación. */
    public String resumenMermas(Asignacion a) {
        double mr = mermaRetiro(a);
        double me = mermaEntrega(a);
        return "Asignada: " + Formato.num(a.getCantidad())
                + " | Merma de retiro: " + Formato.num(mr) + " (" + Formato.num(porcentaje(mr, a)) + " %)"
                + " | Merma de entrega: " + Formato.num(me) + " (" + Formato.num(porcentaje(me, a)) + " %)"
                + " | Diferencia total: " + Formato.num(mr + me) + " (" + Formato.num(porcentaje(mr + me, a)) + " %)";
    }

    /**
     * Cierra la asignación. Si la diferencia supera la tolerancia abre un incidente automático;
     * mientras haya incidentes abiertos no se puede cerrar.
     */
    public String cerrarAsignacion(Asignacion a) throws ReglaNegocioException {
        if (a == null) throw new ReglaNegocioException("La asignación no existe.");
        if (!a.getEstado().equals(Asignacion.CONFIRMADA)) throw new ReglaNegocioException("Solo se cierran asignaciones Confirmadas.");
        Retiro r = buscarRetiroDeAsignacion(a);
        if (r == null || buscarEntregaDeRetiro(r) == null) {
            throw new ReglaNegocioException("Primero debe registrar el retiro y la entrega de esta asignación.");
        }
        double pctTotal = porcentaje(mermaRetiro(a) + mermaEntrega(a), a);
        double tolerancia = sistema.getParametros().getToleranciaPorcentaje();
        boolean conDiferencia = pctTotal > tolerancia + MARGEN;

        if (conDiferencia && !existeIncidenteDiferencia(a)) {
            Incidente inc = registrarIncidente(Incidente.DIFERENCIA, a,
                    "Generado automáticamente: diferencia de " + Formato.num(pctTotal) + " % (tolerancia " + tolerancia + " %)");
            throw new ReglaNegocioException("La diferencia (" + Formato.num(pctTotal) + " %) supera la tolerancia. Se abrió el incidente "
                    + inc.getId() + ". Ciérrelo con su resolución y vuelva a cerrar la asignación.");
        }
        if (tieneIncidenteAbierto(a)) {
            throw new ReglaNegocioException("No se puede cerrar: la asignación tiene incidentes abiertos.");
        }
        a.setEstado(conDiferencia ? Asignacion.CERRADA_DIFERENCIA : Asignacion.CERRADA_CONFORME);
        Oferta o = a.getOferta();
        sistema.getBitacora().registrar(o, o.getEstado(), o.getEstado(),
                "Asignación " + a.getId() + " " + a.getEstado().toLowerCase(), "Coordinador");
        sistema.getGestorOfertas().actualizarEstadoPorAsignaciones(o, "Coordinador");
        return "Asignación " + a.getId() + ": " + a.getEstado() + ". " + resumenMermas(a);
    }

    private boolean existeIncidenteDiferencia(Asignacion a) {
        for (Incidente i : sistema.getIncidentes()) {
            if (i.getAsignacion() == a && i.getTipo().equals(Incidente.DIFERENCIA)) return true;
        }
        return false;
    }
}
