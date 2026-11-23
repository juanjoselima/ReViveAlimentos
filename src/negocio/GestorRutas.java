package negocio;

import modelo.*;
import java.time.LocalDate;
import java.util.ArrayList;

/**
 * M6 (módulo adicional) - Planificación de rutas de retiro (RF-21 a RF-24).
 * Agrupa los retiros de un día en rutas por vehículo, controlando capacidad y refrigeración.
 */
public class GestorRutas {
    public static final double OCUPACION_MINIMA = 50;   // % por debajo del cual la ruta es ineficiente
    private static final double MARGEN = 0.0001;

    private SistemaReVive sistema;

    public GestorRutas(SistemaReVive sistema) {
        this.sistema = sistema;
    }

    // ---------------- RF-21: vehículos ----------------

    /** Placa ecuatoriana: 3 letras, guion y 3 o 4 dígitos (por ejemplo PBA-1234). */
    public static boolean placaValida(String placa) {
        return placa != null && placa.matches("[A-Z]{3}-[0-9]{3,4}");
    }

    public Vehiculo registrarVehiculo(String placa, String descripcion, double capacidadKg, boolean refrigerado)
            throws ReglaNegocioException {
        placa = Validador.obligatorio(placa, "Placa").toUpperCase();
        descripcion = Validador.obligatorio(descripcion, "Descripción");
        if (!placaValida(placa)) throw new ReglaNegocioException("La placa debe tener el formato ABC-1234 (3 letras, guion y 3 o 4 dígitos).");
        if (buscarVehiculo(placa) != null) throw new ReglaNegocioException("Ya existe un vehículo con la placa " + placa + ".");
        if (capacidadKg <= 0) throw new ReglaNegocioException("La capacidad del vehículo debe ser mayor a cero.");
        Vehiculo v = new Vehiculo(placa, descripcion, capacidadKg, refrigerado);
        sistema.getVehiculos().add(v);
        return v;
    }

    public void actualizarVehiculo(Vehiculo v, String descripcion, double capacidadKg, boolean refrigerado)
            throws ReglaNegocioException {
        if (v == null) throw new ReglaNegocioException("El vehículo no existe.");
        descripcion = Validador.obligatorio(descripcion, "Descripción");
        if (capacidadKg <= 0) throw new ReglaNegocioException("La capacidad del vehículo debe ser mayor a cero.");
        for (RutaRetiro r : sistema.getRutas()) {
            if (r.getVehiculo() == v && !r.getEstado().equals(RutaRetiro.FINALIZADA)) {
                if (capacidadKg + MARGEN < r.kgPlanificados()) {
                    throw new ReglaNegocioException("La ruta " + r.getId() + " ya tiene " + Formato.num(r.kgPlanificados())
                            + " kg planificados; la capacidad no puede ser menor.");
                }
                if (!refrigerado && r.llevaPerecibles()) {
                    throw new ReglaNegocioException("La ruta " + r.getId() + " lleva perecibles; el vehículo debe seguir refrigerado.");
                }
            }
        }
        v.setDescripcion(descripcion);
        v.setCapacidadKg(capacidadKg);
        v.setRefrigerado(refrigerado);
    }

    /** No se desactiva un vehículo que tenga rutas sin finalizar. */
    public void cambiarEstadoVehiculo(Vehiculo v, boolean activo) throws ReglaNegocioException {
        if (v == null) throw new ReglaNegocioException("El vehículo no existe.");
        if (!activo) {
            for (RutaRetiro r : sistema.getRutas()) {
                if (r.getVehiculo() == v && !r.getEstado().equals(RutaRetiro.FINALIZADA)) {
                    throw new ReglaNegocioException("No se puede desactivar: tiene la ruta " + r.getId() + " en estado " + r.getEstado() + ".");
                }
            }
        }
        v.setActivo(activo);
    }

    public Vehiculo buscarVehiculo(String placa) {
        for (Vehiculo v : sistema.getVehiculos()) {
            if (v.getPlaca().equalsIgnoreCase(placa.trim())) return v;
        }
        return null;
    }

    public ArrayList<Vehiculo> listarVehiculos() { return new ArrayList<>(sistema.getVehiculos()); }

    // ---------------- RF-22: crear ruta ----------------

    public RutaRetiro crearRuta(LocalDate fecha, Vehiculo v, Responsable responsable) throws ReglaNegocioException {
        if (fecha == null) throw new ReglaNegocioException("La fecha de la ruta es obligatoria.");
        if (fecha.isBefore(Reloj.ahora().toLocalDate())) throw new ReglaNegocioException("No se puede crear una ruta en una fecha pasada.");
        if (v == null || !v.isActivo()) throw new ReglaNegocioException("Debe elegir un vehículo activo.");
        if (responsable == null || !responsable.getFuncion().equals(Responsable.RETIRO)) {
            throw new ReglaNegocioException("El responsable de la ruta debe tener la función de Retiro.");
        }
        if (!responsable.isDisponible()) throw new ReglaNegocioException("El responsable " + responsable.getNombre() + " no está disponible.");
        for (RutaRetiro r : sistema.getRutas()) {
            if (r.getFecha().equals(fecha) && !r.getEstado().equals(RutaRetiro.FINALIZADA)) {
                if (r.getVehiculo() == v) throw new ReglaNegocioException("El vehículo " + v.getPlaca() + " ya tiene la ruta " + r.getId() + " ese día.");
                if (r.getResponsable() == responsable) throw new ReglaNegocioException(responsable.getNombre() + " ya tiene la ruta " + r.getId() + " ese día.");
            }
        }
        RutaRetiro ruta = new RutaRetiro(fecha, v, responsable);
        sistema.getRutas().add(ruta);
        return ruta;
    }

    // ---------------- RF-23: agregar retiros a la ruta ----------------

    public RutaRetiro rutaDeRetiro(Retiro retiro) {
        for (RutaRetiro r : sistema.getRutas()) {
            if (r.contiene(retiro)) return r;
        }
        return null;
    }

    /** Retiros programados que se podrían sumar a la ruta (mismo día y mismo responsable, sin otra ruta). */
    public ArrayList<Retiro> retirosCandidatos(RutaRetiro ruta) {
        ArrayList<Retiro> lista = new ArrayList<>();
        for (Retiro r : sistema.getRetiros()) {
            if (r.getEstado().equals(Retiro.PROGRAMADO) && r.getHoraProgramada().toLocalDate().equals(ruta.getFecha())
                    && r.getResponsable() == ruta.getResponsable() && rutaDeRetiro(r) == null) {
                lista.add(r);
            }
        }
        return lista;
    }

    public void agregarRetiro(RutaRetiro ruta, Retiro retiro) throws ReglaNegocioException {
        if (ruta == null || retiro == null) throw new ReglaNegocioException("Debe elegir la ruta y el retiro.");
        if (!ruta.getEstado().equals(RutaRetiro.PLANIFICADA)) throw new ReglaNegocioException("Solo se agregan paradas a rutas Planificadas.");
        if (!retiro.getEstado().equals(Retiro.PROGRAMADO)) throw new ReglaNegocioException("Solo se agregan retiros en estado Programado.");
        if (!retiro.getHoraProgramada().toLocalDate().equals(ruta.getFecha())) {
            throw new ReglaNegocioException("El retiro es de otro día (" + Formato.fechaHora(retiro.getHoraProgramada()) + ").");
        }
        if (retiro.getResponsable() != ruta.getResponsable()) {
            throw new ReglaNegocioException("El retiro está a cargo de " + retiro.getResponsable().getNombre()
                    + " y la ruta de " + ruta.getResponsable().getNombre() + ".");
        }
        RutaRetiro otra = rutaDeRetiro(retiro);
        if (otra != null) throw new ReglaNegocioException("El retiro ya está en la ruta " + otra.getId() + ".");
        double kgNuevo = retiro.getAsignacion().getCantidadKg();
        double capacidad = ruta.getVehiculo().getCapacidadKg();
        if (ruta.kgPlanificados() + kgNuevo > capacidad + MARGEN) {
            throw new ReglaNegocioException("Se supera la capacidad del vehículo: " + Formato.num(ruta.kgPlanificados()) + " + "
                    + Formato.num(kgNuevo) + " kg > " + Formato.num(capacidad) + " kg.");
        }
        if (retiro.getAsignacion().getOferta().getCategoria().isPerecible() && !ruta.getVehiculo().isRefrigerado()) {
            throw new ReglaNegocioException("El retiro lleva " + retiro.getAsignacion().getOferta().getCategoria().getNombre()
                    + " (perecible) y el vehículo " + ruta.getVehiculo().getPlaca() + " no tiene refrigeración.");
        }
        ruta.agregarParada(retiro);
        Oferta o = retiro.getAsignacion().getOferta();
        sistema.getBitacora().registrar(o, o.getEstado(), o.getEstado(),
                "Retiro " + retiro.getId() + " incluido en la ruta " + ruta.getId() + " (vehículo " + ruta.getVehiculo().getPlaca() + ")",
                ruta.getResponsable().getNombre());
    }

    public void quitarRetiro(RutaRetiro ruta, Retiro retiro) throws ReglaNegocioException {
        if (ruta == null || retiro == null || !ruta.contiene(retiro)) throw new ReglaNegocioException("El retiro no está en esa ruta.");
        if (!ruta.getEstado().equals(RutaRetiro.PLANIFICADA)) throw new ReglaNegocioException("Solo se quitan paradas de rutas Planificadas.");
        ruta.quitarParada(retiro);
    }

    // ---------------- RF-24: iniciar, finalizar y medir ----------------

    public void iniciarRuta(RutaRetiro ruta) throws ReglaNegocioException {
        if (ruta == null) throw new ReglaNegocioException("La ruta no existe.");
        if (!ruta.getEstado().equals(RutaRetiro.PLANIFICADA)) throw new ReglaNegocioException("Solo se inician rutas Planificadas.");
        if (ruta.getParadas().isEmpty()) throw new ReglaNegocioException("La ruta no tiene paradas.");
        ruta.setEstado(RutaRetiro.EN_CURSO);
    }

    /** Solo se finaliza cuando todos sus retiros están completados. */
    public String finalizarRuta(RutaRetiro ruta) throws ReglaNegocioException {
        if (ruta == null) throw new ReglaNegocioException("La ruta no existe.");
        if (!ruta.getEstado().equals(RutaRetiro.EN_CURSO)) throw new ReglaNegocioException("Solo se finalizan rutas En curso.");
        String pendientes = "";
        for (Retiro r : ruta.getParadas()) {
            if (!r.getEstado().equals(Retiro.COMPLETADO)) pendientes += " " + r.getId();
        }
        if (!pendientes.isEmpty()) {
            throw new ReglaNegocioException("No se puede finalizar: faltan registrar las recogidas de los retiros" + pendientes + ".");
        }
        ruta.setEstado(RutaRetiro.FINALIZADA);
        return resumenRuta(ruta);
    }

    public double porcentajeCumplimiento(RutaRetiro ruta) {
        double plan = ruta.kgPlanificados();
        return (plan <= 0) ? 0 : ruta.kgRecogidos() / plan * 100;
    }

    public String resumenRuta(RutaRetiro ruta) {
        String texto = ruta.toString() + "\n";
        int n = 1;
        for (Retiro r : ruta.getParadas()) {
            Oferta o = r.getAsignacion().getOferta();
            texto += "  " + n + ". " + Formato.fechaHora(r.getHoraProgramada()) + " | " + o.getDonante().getNombre()
                    + " (" + o.getDonante().getZona().getNombre() + ") | " + Formato.num(r.getAsignacion().getCantidadKg()) + " kg de "
                    + o.getCategoria().getNombre() + " | Retiro " + r.getEstado() + "\n";
            n++;
        }
        texto += "Kg planificados: " + Formato.num(ruta.kgPlanificados()) + " de " + Formato.num(ruta.getVehiculo().getCapacidadKg())
                + " | Ocupación: " + Formato.num(ruta.porcentajeOcupacion()) + " %"
                + " | Kg recogidos: " + Formato.num(ruta.kgRecogidos())
                + " | Cumplimiento: " + Formato.num(porcentajeCumplimiento(ruta)) + " %";
        if (!ruta.getParadas().isEmpty() && ruta.porcentajeOcupacion() < OCUPACION_MINIMA) {
            texto += "\n*** ALERTA: ocupación menor al " + (int) OCUPACION_MINIMA + " %. Conviene sumar retiros o usar un vehículo más pequeño ***";
        }
        return texto;
    }

    public RutaRetiro buscarRuta(int id) {
        for (RutaRetiro r : sistema.getRutas()) {
            if (r.getId() == id) return r;
        }
        return null;
    }

    public ArrayList<RutaRetiro> listarRutas() { return new ArrayList<>(sistema.getRutas()); }
}
