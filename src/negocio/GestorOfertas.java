package negocio;

import modelo.*;
import java.time.LocalDateTime;
import java.util.ArrayList;

/**
 * M1 - Ofertas, disponibilidad y vencimiento (RF-03 y RF-04). Responsable: Juan José Lima.
 */
public class GestorOfertas {
    public static final int MINUTOS_MINIMOS_RETIRO = 30;
    private static final double MARGEN = 0.0001;   // para comparar decimales

    private SistemaReVive sistema;

    public GestorOfertas(SistemaReVive sistema) {
        this.sistema = sistema;
    }

    // ---------------- RF-03 ----------------

    public Oferta registrarOferta(Donante donante, Categoria categoria, double cantidad, Unidad unidad,
                                  LocalDateTime caducidad, LocalDateTime horaLimite) throws ReglaNegocioException {
        if (donante == null) throw new ReglaNegocioException("Debe seleccionar un donante.");
        if (!donante.isActivo()) throw new ReglaNegocioException("El donante está inactivo y no puede registrar ofertas.");
        LocalDateTime ahora = Reloj.ahora();
        validarDatosOferta(categoria, cantidad, unidad, caducidad, horaLimite, ahora);

        Oferta oferta = new Oferta(donante, categoria, cantidad, unidad, caducidad, horaLimite, ahora);
        sistema.getOfertas().add(oferta);
        sistema.getBitacora().registrar(oferta, "-", Oferta.REGISTRADA,
                "Oferta registrada: " + cantidad + " " + unidad.getNombre() + " de " + categoria.getNombre(), "Coordinador");
        return oferta;
    }

    /** Reglas comunes al registrar y al editar una oferta. */
    private void validarDatosOferta(Categoria categoria, double cantidad, Unidad unidad, LocalDateTime caducidad,
                                    LocalDateTime horaLimite, LocalDateTime fechaRegistro) throws ReglaNegocioException {
        if (categoria == null) throw new ReglaNegocioException("Debe seleccionar una categoría.");
        if (unidad == null) throw new ReglaNegocioException("Debe seleccionar una unidad.");
        if (cantidad <= 0) throw new ReglaNegocioException("La cantidad debe ser mayor a cero.");
        if (caducidad == null || horaLimite == null) throw new ReglaNegocioException("Las fechas son obligatorias.");
        if (horaLimite.isBefore(fechaRegistro.plusMinutes(MINUTOS_MINIMOS_RETIRO))) {
            throw new ReglaNegocioException("La hora límite de retiro debe ser al menos " + MINUTOS_MINIMOS_RETIRO
                    + " minutos posterior al registro (" + Formato.fechaHora(fechaRegistro) + ").");
        }
        if (horaLimite.isAfter(caducidad)) {
            throw new ReglaNegocioException("La hora límite de retiro no puede superar la fecha de caducidad.");
        }
    }

    /** Solo se edita en estado Registrada. */
    public void editarOferta(Oferta oferta, Categoria categoria, double cantidad, Unidad unidad,
                             LocalDateTime caducidad, LocalDateTime horaLimite) throws ReglaNegocioException {
        if (oferta == null) throw new ReglaNegocioException("La oferta no existe.");
        if (!oferta.getEstado().equals(Oferta.REGISTRADA)) {
            throw new ReglaNegocioException("Solo se puede editar una oferta en estado Registrada (estado actual: "
                    + oferta.getEstado() + ").");
        }
        validarDatosOferta(categoria, cantidad, unidad, caducidad, horaLimite, oferta.getFechaRegistro());
        if (horaLimite.isBefore(Reloj.ahora())) throw new ReglaNegocioException("La hora límite no puede estar en el pasado.");
        oferta.setCategoria(categoria);
        oferta.setCantidad(cantidad);
        oferta.setUnidad(unidad);
        oferta.setFechaCaducidad(caducidad);
        oferta.setHoraLimiteRetiro(horaLimite);
        sistema.getBitacora().registrar(oferta, oferta.getEstado(), oferta.getEstado(), "Datos de la oferta editados", "Coordinador");
    }

    /** Solo se cancela si no tiene asignaciones confirmadas. Libera las propuestas. */
    public void cancelarOferta(Oferta oferta) throws ReglaNegocioException {
        if (oferta == null) throw new ReglaNegocioException("La oferta no existe.");
        if (!estaActiva(oferta)) throw new ReglaNegocioException("La oferta ya está en estado final: " + oferta.getEstado() + ".");
        if (oferta.tieneAsignacionesConfirmadas()) {
            throw new ReglaNegocioException("No se puede cancelar: la oferta tiene asignaciones confirmadas.");
        }
        for (Asignacion a : oferta.getAsignaciones()) {
            if (a.getEstado().equals(Asignacion.PROPUESTA)) a.setEstado(Asignacion.CANCELADA);
        }
        cambiarEstado(oferta, Oferta.CANCELADA, "Oferta cancelada por el coordinador", "Coordinador");
    }

    /** Una oferta está activa mientras no llegue a un estado final. */
    public boolean estaActiva(Oferta o) {
        String e = o.getEstado();
        return e.equals(Oferta.REGISTRADA) || e.equals(Oferta.EVALUADA)
                || e.equals(Oferta.ASIGNADA_PARCIAL) || e.equals(Oferta.ASIGNADA_TOTAL);
    }

    /** Todo cambio de estado pasa por aquí para que quede en la bitácora (RF-20). */
    public void cambiarEstado(Oferta oferta, String nuevoEstado, String detalle, String responsable) {
        String anterior = oferta.getEstado();
        oferta.setEstado(nuevoEstado);
        sistema.getBitacora().registrar(oferta, anterior, nuevoEstado, detalle, responsable);
    }

    // ---------------- RF-04 ----------------

    public double calcularDisponible(Oferta oferta) {
        return oferta.calcularDisponible();
    }

    /**
     * Si pasó la hora límite: las propuestas pendientes se liberan y, si no hubo
     * asignaciones confirmadas, la oferta pasa a Vencida.
     */
    public void revisarVencimientos() {
        LocalDateTime ahora = Reloj.ahora();
        for (Oferta o : sistema.getOfertas()) {
            if (!estaActiva(o) || ahora.isBefore(o.getHoraLimiteRetiro())) continue;

            for (Asignacion a : o.getAsignaciones()) {
                if (a.getEstado().equals(Asignacion.PROPUESTA)) {
                    a.setEstado(Asignacion.EXPIRADA);
                    sistema.getBitacora().registrar(o, o.getEstado(), o.getEstado(),
                            "Propuesta " + a.getId() + " liberada por vencimiento de la hora límite", "Sistema");
                }
            }
            if (!o.tieneAsignacionesConfirmadas()) {
                cambiarEstado(o, Oferta.VENCIDA, "Hora límite de retiro alcanzada sin asignaciones confirmadas", "Sistema");
            } else {
                actualizarEstadoPorAsignaciones(o, "Sistema");
            }
        }
    }

    /**
     * Recalcula el estado de la oferta según sus asignaciones:
     * Asignada total / parcial, vuelve a Evaluada si se cancelan todas, o Cerrada al terminar.
     */
    public void actualizarEstadoPorAsignaciones(Oferta oferta, String responsable) {
        if (!estaActiva(oferta)) return;
        double confirmada = oferta.calcularConfirmada();
        boolean hayActivas = false;
        boolean hayCerradas = false;
        for (Asignacion a : oferta.getAsignaciones()) {
            if (a.estaActiva()) hayActivas = true;
            if (a.estaCerrada()) hayCerradas = true;
        }
        boolean sinSaldoUtil = confirmada >= oferta.getCantidad() - MARGEN
                || !Reloj.ahora().isBefore(oferta.getHoraLimiteRetiro());

        String nuevo;
        if (hayCerradas && !hayActivas && sinSaldoUtil) {
            nuevo = Oferta.CERRADA;
        } else if (confirmada >= oferta.getCantidad() - MARGEN) {
            nuevo = Oferta.ASIGNADA_TOTAL;
        } else if (confirmada > 0) {
            nuevo = Oferta.ASIGNADA_PARCIAL;
        } else {
            nuevo = Oferta.EVALUADA;
        }
        if (!nuevo.equals(oferta.getEstado())) {
            cambiarEstado(oferta, nuevo, "Estado actualizado según sus asignaciones", responsable);
        }
    }

    public Oferta buscarOferta(int id) {
        for (Oferta o : sistema.getOfertas()) {
            if (o.getId() == id) return o;
        }
        return null;
    }

    public ArrayList<Oferta> listarOfertas() { return new ArrayList<>(sistema.getOfertas()); }
}
