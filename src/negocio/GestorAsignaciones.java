package negocio;

import modelo.*;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;

/**
 * M3 - Receptores, compatibilidad y asignaciones (RF-09 a RF-12). Responsable: Tomás Muñoz.
 */
public class GestorAsignaciones {
    private static final double MARGEN = 0.0001;

    private SistemaReVive sistema;

    public GestorAsignaciones(SistemaReVive sistema) {
        this.sistema = sistema;
    }

    // ---------------- RF-09 ----------------

    public Receptor registrarReceptor(String nombre, String contacto, Zona zona, double capacidadKg,
                                      ArrayList<FranjaHoraria> franjas, ArrayList<Categoria> categorias)
            throws ReglaNegocioException {
        nombre = Validador.obligatorio(nombre, "Nombre");
        contacto = Validador.obligatorio(contacto, "Contacto");
        validarDatosReceptor(zona, capacidadKg, franjas, categorias);
        Receptor r = new Receptor(nombre, contacto, zona, capacidadKg);
        r.setFranjas(franjas);
        r.setCategoriasAceptadas(categorias);
        sistema.getReceptores().add(r);
        return r;
    }

    public void actualizarReceptor(Receptor r, String nombre, String contacto, Zona zona, double capacidadKg,
                                   ArrayList<FranjaHoraria> franjas, ArrayList<Categoria> categorias)
            throws ReglaNegocioException {
        if (r == null) throw new ReglaNegocioException("El receptor no existe.");
        nombre = Validador.obligatorio(nombre, "Nombre");
        contacto = Validador.obligatorio(contacto, "Contacto");
        validarDatosReceptor(zona, capacidadKg, franjas, categorias);
        r.setNombre(nombre);
        r.setContacto(contacto);
        r.setZona(zona);
        r.setCapacidadDiariaKg(capacidadKg);
        r.setFranjas(franjas);
        r.setCategoriasAceptadas(categorias);
    }

    private void validarDatosReceptor(Zona zona, double capacidadKg, ArrayList<FranjaHoraria> franjas,
                                      ArrayList<Categoria> categorias) throws ReglaNegocioException {
        if (zona == null || !sistema.getZonas().contains(zona)) throw new ReglaNegocioException("La zona indicada no existe.");
        if (capacidadKg <= 0) throw new ReglaNegocioException("La capacidad diaria debe ser mayor a cero.");
        if (franjas == null || franjas.isEmpty()) throw new ReglaNegocioException("Debe registrar al menos una franja de recepción.");
        if (categorias == null || categorias.isEmpty()) throw new ReglaNegocioException("Debe indicar al menos una categoría aceptada.");
        for (int i = 0; i < franjas.size(); i++) {
            FranjaHoraria f = franjas.get(i);
            if (!f.getInicio().isBefore(f.getFin())) {
                throw new ReglaNegocioException("En la franja " + f + " la hora de inicio debe ser menor que la de fin.");
            }
            for (int j = i + 1; j < franjas.size(); j++) {
                if (f.seSuperponeCon(franjas.get(j))) {
                    throw new ReglaNegocioException("Las franjas " + f + " y " + franjas.get(j) + " se superponen.");
                }
            }
        }
    }

    public void cambiarEstadoReceptor(Receptor r, boolean activo) throws ReglaNegocioException {
        if (r == null) throw new ReglaNegocioException("El receptor no existe.");
        if (!activo) {
            for (Asignacion a : sistema.getAsignaciones()) {
                if (a.getReceptor() == r && a.estaActiva()) {
                    throw new ReglaNegocioException("No se puede desactivar: tiene la asignación " + a.getId() + " activa.");
                }
            }
        }
        r.setActivo(activo);
    }

    public Receptor buscarReceptor(int id) {
        for (Receptor r : sistema.getReceptores()) {
            if (r.getId() == id) return r;
        }
        return null;
    }

    public ArrayList<Receptor> listarReceptores() { return new ArrayList<>(sistema.getReceptores()); }

    // ---------------- RF-10 ----------------

    /** Capacidad diaria - kg ya asignados al receptor para ese día. */
    public double capacidadRestanteKg(Receptor r, LocalDate dia) {
        double usado = 0;
        for (Asignacion a : sistema.getAsignaciones()) {
            if (a.getReceptor() == r && (a.estaActiva() || a.estaCerrada())
                    && a.getOferta().getHoraLimiteRetiro().toLocalDate().equals(dia)) {
                usado += a.getCantidadKg();
            }
        }
        double restante = r.getCapacidadDiariaKg() - usado;
        return restante < 0 ? 0 : restante;
    }

    /** Primera franja del receptor que empieza antes de la hora límite (o null). */
    public FranjaHoraria franjaUtil(Receptor r, Oferta oferta) {
        LocalTime limite = oferta.getHoraLimiteRetiro().toLocalTime();
        FranjaHoraria mejor = null;
        for (FranjaHoraria f : r.getFranjas()) {
            if (f.getInicio().isBefore(limite)) {
                if (mejor == null || f.getInicio().isBefore(mejor.getInicio())) mejor = f;
            }
        }
        return mejor;
    }

    public boolean tieneAsignacionActiva(Oferta oferta, Receptor r) {
        for (Asignacion a : oferta.getAsignaciones()) {
            if (a.getReceptor() == r && a.estaActiva()) return true;
        }
        return false;
    }

    public ArrayList<Receptor> filtrarCompatibles(Oferta oferta) throws ReglaNegocioException {
        String motivo = sistema.getGestorEvaluacion().motivoNoAsignable(oferta);
        if (motivo != null) throw new ReglaNegocioException("La oferta no es asignable: " + motivo + ".");
        LocalDate dia = oferta.getHoraLimiteRetiro().toLocalDate();
        ArrayList<Receptor> aptos = new ArrayList<>();
        for (Receptor r : sistema.getReceptores()) {
            if (!r.isActivo()) continue;
            if (!r.aceptaCategoria(oferta.getCategoria())) continue;
            if (franjaUtil(r, oferta) == null) continue;
            if (capacidadRestanteKg(r, dia) <= 0) continue;
            if (tieneAsignacionActiva(oferta, r)) continue;
            aptos.add(r);
        }
        return aptos;
    }

    // ---------------- RF-11 ----------------

    public double calcularPuntaje(Oferta oferta, Receptor r) {
        Parametros p = sistema.getParametros();
        double puntaje = 0;

        // Zona: misma zona = peso completo, adyacente = mitad, otra = 0
        Zona zonaDonante = oferta.getDonante().getZona();
        if (r.getZona() == zonaDonante) puntaje += p.getPesoZona();
        else if (r.getZona().esAdyacente(zonaDonante)) puntaje += p.getPesoZona() / 2.0;

        // Capacidad: completo si cubre todo lo disponible, proporcional si cubre una parte
        double disponibleKg = oferta.getUnidad().aKg(oferta.calcularDisponible());
        double restante = capacidadRestanteKg(r, oferta.getHoraLimiteRetiro().toLocalDate());
        if (disponibleKg > 0) {
            double cobertura = Math.min(restante, disponibleKg) / disponibleKg;
            puntaje += p.getPesoCapacidad() * cobertura;
        }

        // Horario: completo si la franja empieza al menos 1 h antes de la hora límite, mitad si es menos
        FranjaHoraria f = franjaUtil(r, oferta);
        if (f != null) {
            long minutos = Duration.between(f.getInicio(), oferta.getHoraLimiteRetiro().toLocalTime()).toMinutes();
            puntaje += (minutos >= 60) ? p.getPesoHorario() : p.getPesoHorario() / 2.0;
        }

        // Historial: sin incidentes en 30 días y no está en observación
        int incidentes = sistema.getGestorLogistica().contarIncidentesRecientes(r, 30);
        if (incidentes == 0 && !r.isEnObservacion()) puntaje += p.getPesoHistorial();

        return Math.round(puntaje * 100) / 100.0;
    }

    /** Kilos entregados al receptor en los últimos 7 días (para desempatar). */
    public double kgRecibidosSemana(Receptor r) {
        LocalDateTime desde = Reloj.ahora().minusDays(7);
        double total = 0;
        for (Entrega e : sistema.getEntregas()) {
            if (e.getRetiro().getAsignacion().getReceptor() == r && e.getFecha().isAfter(desde)) {
                total += e.getCantidadEntregadaKg();
            }
        }
        return total;
    }

    /** Receptores aptos ordenados por puntaje (mayor a menor); empate: menos kilos en la semana. */
    public ArrayList<Receptor> recomendarReceptores(Oferta oferta) throws ReglaNegocioException {
        ArrayList<Receptor> lista = filtrarCompatibles(oferta);
        for (int i = 1; i < lista.size(); i++) {
            Receptor actual = lista.get(i);
            int j = i - 1;
            while (j >= 0 && vaAntes(oferta, actual, lista.get(j))) {
                lista.set(j + 1, lista.get(j));
                j--;
            }
            lista.set(j + 1, actual);
        }
        return lista;
    }

    private boolean vaAntes(Oferta o, Receptor a, Receptor b) {
        double pa = calcularPuntaje(o, a);
        double pb = calcularPuntaje(o, b);
        if (Math.abs(pa - pb) > MARGEN) return pa > pb;
        return kgRecibidosSemana(a) < kgRecibidosSemana(b);
    }

    // ---------------- RF-12 ----------------

    public Asignacion proponerAsignacion(Oferta oferta, Receptor r, double cantidad) throws ReglaNegocioException {
        if (r == null) throw new ReglaNegocioException("El receptor no existe.");
        ArrayList<Receptor> compatibles = filtrarCompatibles(oferta);   // valida también RF-08
        if (tieneAsignacionActiva(oferta, r)) {
            throw new ReglaNegocioException("El receptor ya tiene una asignación activa de esta oferta.");
        }
        if (!compatibles.contains(r)) throw new ReglaNegocioException("El receptor " + r.getNombre() + " no es compatible con esta oferta.");
        if (cantidad <= 0) throw new ReglaNegocioException("La cantidad a asignar debe ser mayor a cero.");
        if (cantidad > oferta.calcularDisponible() + MARGEN) {
            throw new ReglaNegocioException("La cantidad supera lo disponible (" + Formato.num(oferta.calcularDisponible())
                    + " " + oferta.getUnidad().getNombre() + ").");
        }
        double kg = oferta.getUnidad().aKg(cantidad);
        double restante = capacidadRestanteKg(r, oferta.getHoraLimiteRetiro().toLocalDate());
        if (kg > restante + MARGEN) {
            throw new ReglaNegocioException("La cantidad (" + Formato.num(kg) + " kg) supera la capacidad restante del receptor ("
                    + Formato.num(restante) + " kg).");
        }
        Asignacion a = new Asignacion(oferta, r, cantidad, calcularPuntaje(oferta, r), Reloj.ahora());
        sistema.getAsignaciones().add(a);
        oferta.agregarAsignacion(a);
        sistema.getBitacora().registrar(oferta, oferta.getEstado(), oferta.getEstado(),
                "Asignación " + a.getId() + " propuesta a " + r.getNombre() + " por " + Formato.num(cantidad) + " "
                        + oferta.getUnidad().getNombre(), "Coordinador");
        return a;
    }

    public void confirmarAsignacion(Asignacion a) throws ReglaNegocioException {
        if (a == null) throw new ReglaNegocioException("La asignación no existe.");
        expirarPropuestas();
        if (!a.getEstado().equals(Asignacion.PROPUESTA)) {
            throw new ReglaNegocioException("Solo se confirman asignaciones en estado Propuesta (estado actual: " + a.getEstado() + ").");
        }
        Oferta o = a.getOferta();
        if (!Reloj.ahora().isBefore(o.getHoraLimiteRetiro())) throw new ReglaNegocioException("Tiempo de retiro agotado.");
        // Se vuelve a verificar contra lo que realmente queda en este momento
        double libreParaConfirmar = o.getCantidad() - o.calcularConfirmada();
        if (a.getCantidad() > libreParaConfirmar + MARGEN) {
            throw new ReglaNegocioException("La cantidad ya no está disponible (quedan " + Formato.num(libreParaConfirmar) + ").");
        }
        a.setEstado(Asignacion.CONFIRMADA);
        a.setFechaConfirmacion(Reloj.ahora());
        sistema.getBitacora().registrar(o, o.getEstado(), o.getEstado(),
                "Asignación " + a.getId() + " confirmada con " + a.getReceptor().getNombre(), "Coordinador");
        sistema.getGestorOfertas().actualizarEstadoPorAsignaciones(o, "Coordinador");
    }

    public void rechazarAsignacion(Asignacion a) throws ReglaNegocioException {
        if (a == null) throw new ReglaNegocioException("La asignación no existe.");
        if (!a.getEstado().equals(Asignacion.PROPUESTA)) throw new ReglaNegocioException("Solo se rechazan asignaciones propuestas.");
        a.setEstado(Asignacion.RECHAZADA);
        Oferta o = a.getOferta();
        sistema.getBitacora().registrar(o, o.getEstado(), o.getEstado(),
                "Asignación " + a.getId() + " rechazada por " + a.getReceptor().getNombre(), "Coordinador");
    }

    public void cancelarAsignacion(Asignacion a) throws ReglaNegocioException {
        if (a == null) throw new ReglaNegocioException("La asignación no existe.");
        if (!a.estaActiva()) throw new ReglaNegocioException("La asignación no está activa (estado: " + a.getEstado() + ").");
        if (sistema.getGestorLogistica().buscarRetiroDeAsignacion(a) != null) {
            throw new ReglaNegocioException("No se puede cancelar: ya tiene un retiro programado.");
        }
        a.setEstado(Asignacion.CANCELADA);
        Oferta o = a.getOferta();
        sistema.getBitacora().registrar(o, o.getEstado(), o.getEstado(), "Asignación " + a.getId() + " cancelada", "Coordinador");
        sistema.getGestorOfertas().actualizarEstadoPorAsignaciones(o, "Coordinador");
    }

    /** Propuestas no confirmadas dentro del tiempo de expiración se liberan. */
    public void expirarPropuestas() {
        int minutos = sistema.getParametros().getMinutosExpiracion();
        LocalDateTime ahora = Reloj.ahora();
        for (Asignacion a : sistema.getAsignaciones()) {
            if (a.getEstado().equals(Asignacion.PROPUESTA) && !ahora.isBefore(a.getFechaPropuesta().plusMinutes(minutos))) {
                a.setEstado(Asignacion.EXPIRADA);
                Oferta o = a.getOferta();
                sistema.getBitacora().registrar(o, o.getEstado(), o.getEstado(),
                        "Propuesta " + a.getId() + " expirada (" + minutos + " min sin confirmar); cantidad liberada", "Sistema");
            }
        }
    }

    public Asignacion buscarAsignacion(int id) {
        for (Asignacion a : sistema.getAsignaciones()) {
            if (a.getId() == id) return a;
        }
        return null;
    }

    public ArrayList<Asignacion> listarAsignaciones() { return new ArrayList<>(sistema.getAsignaciones()); }
}
