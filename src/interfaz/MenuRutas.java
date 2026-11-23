package interfaz;

import modelo.*;
import negocio.*;
import java.time.LocalDate;
import java.util.ArrayList;

/**
 * Pantalla de consola del módulo adicional M6 (RF-21 a RF-24).
 */
public class MenuRutas {
    private SistemaReVive sistema;
    private GestorRutas gr;

    public MenuRutas(SistemaReVive sistema) {
        this.sistema = sistema;
        this.gr = sistema.getGestorRutas();
    }

    public void mostrar() {
        int opcion;
        do {
            sistema.actualizarEstadosAutomaticos();
            Consola.titulo("M6 - PLANIFICACIÓN DE RUTAS DE RETIRO (módulo adicional)");
            System.out.println(" 1. Registrar vehículo         2. Actualizar vehículo");
            System.out.println(" 3. Activar/desactivar vehículo  4. Listar vehículos");
            System.out.println(" 5. Crear ruta de retiro       6. Agregar retiro a una ruta");
            System.out.println(" 7. Quitar retiro de una ruta  8. Iniciar ruta");
            System.out.println(" 9. Finalizar ruta            10. Ver detalle y ocupación de una ruta");
            System.out.println("11. Listar rutas");
            System.out.println(" 0. Volver");
            opcion = Consola.leerEntero("Opción: ", 0, 11);
            try {
                switch (opcion) {
                    case 1: registrarVehiculo(); break;
                    case 2: actualizarVehiculo(); break;
                    case 3: cambiarEstadoVehiculo(); break;
                    case 4: Consola.listar("Vehículos", gr.listarVehiculos()); break;
                    case 5: crearRuta(); break;
                    case 6: agregarRetiro(); break;
                    case 7: quitarRetiro(); break;
                    case 8: iniciarRuta(); break;
                    case 9: finalizarRuta(); break;
                    case 10: verDetalle(); break;
                    case 11: Consola.listar("Rutas", gr.listarRutas()); break;
                }
            } catch (ReglaNegocioException e) {
                Consola.error(e.getMessage());
            }
        } while (opcion != 0);
    }

    private void registrarVehiculo() throws ReglaNegocioException {
        String placa = Consola.leerTexto("Placa (ABC-1234): ");
        String descripcion = Consola.leerTexto("Descripción (camioneta, furgón...): ");
        double capacidad = Consola.leerDecimal("Capacidad de carga (kg): ");
        boolean refrigerado = Consola.leerSiNo("¿Tiene refrigeración?");
        Vehiculo v = gr.registrarVehiculo(placa, descripcion, capacidad, refrigerado);
        Consola.exito("Vehículo registrado: " + v);
    }

    private void actualizarVehiculo() throws ReglaNegocioException {
        Vehiculo v = Consola.elegir("Vehículo", gr.listarVehiculos());
        if (v == null) return;
        String descripcion = Consola.leerTexto("Descripción [" + v.getDescripcion() + "]: ");
        if (descripcion.isEmpty()) descripcion = v.getDescripcion();
        double capacidad = Consola.leerDecimal("Capacidad de carga (kg): ");
        boolean refrigerado = Consola.leerSiNo("¿Tiene refrigeración?");
        gr.actualizarVehiculo(v, descripcion, capacidad, refrigerado);
        Consola.exito("Vehículo actualizado: " + v);
    }

    private void cambiarEstadoVehiculo() throws ReglaNegocioException {
        Vehiculo v = Consola.elegir("Vehículo", gr.listarVehiculos());
        if (v == null) return;
        gr.cambiarEstadoVehiculo(v, !v.isActivo());
        Consola.exito("Vehículo ahora está " + (v.isActivo() ? "activo." : "inactivo."));
    }

    private void crearRuta() throws ReglaNegocioException {
        LocalDate fecha = Consola.leerFecha("Fecha de la ruta");
        Vehiculo v = Consola.elegir("Vehículo", gr.listarVehiculos());
        if (v == null) return;
        Responsable r = Consola.elegir("Responsable de retiro", sistema.getGestorEvaluacion().listarPorFuncion(Responsable.RETIRO));
        if (r == null) return;
        RutaRetiro ruta = gr.crearRuta(fecha, v, r);
        Consola.exito("Ruta creada: " + ruta);
    }

    private ArrayList<RutaRetiro> rutasEnEstado(String estado) {
        ArrayList<RutaRetiro> lista = new ArrayList<>();
        for (RutaRetiro r : gr.listarRutas()) {
            if (r.getEstado().equals(estado)) lista.add(r);
        }
        return lista;
    }

    private void agregarRetiro() throws ReglaNegocioException {
        RutaRetiro ruta = Consola.elegir("Rutas planificadas", rutasEnEstado(RutaRetiro.PLANIFICADA));
        if (ruta == null) return;
        Retiro retiro = Consola.elegir("Retiros programados del mismo día y responsable", gr.retirosCandidatos(ruta));
        if (retiro == null) return;
        gr.agregarRetiro(ruta, retiro);
        Consola.exito("Retiro agregado. Ocupación de la ruta: " + Formato.num(ruta.porcentajeOcupacion()) + " %");
    }

    private void quitarRetiro() throws ReglaNegocioException {
        RutaRetiro ruta = Consola.elegir("Rutas planificadas", rutasEnEstado(RutaRetiro.PLANIFICADA));
        if (ruta == null) return;
        Retiro retiro = Consola.elegir("Paradas de la ruta", ruta.getParadas());
        if (retiro == null) return;
        gr.quitarRetiro(ruta, retiro);
        Consola.exito("Retiro quitado de la ruta.");
    }

    private void iniciarRuta() throws ReglaNegocioException {
        RutaRetiro ruta = Consola.elegir("Rutas planificadas", rutasEnEstado(RutaRetiro.PLANIFICADA));
        if (ruta == null) return;
        gr.iniciarRuta(ruta);
        Consola.exito("Ruta " + ruta.getId() + " en curso.");
    }

    private void finalizarRuta() throws ReglaNegocioException {
        RutaRetiro ruta = Consola.elegir("Rutas en curso", rutasEnEstado(RutaRetiro.EN_CURSO));
        if (ruta == null) return;
        System.out.println(gr.finalizarRuta(ruta));
        Consola.exito("Ruta " + ruta.getId() + " finalizada.");
    }

    private void verDetalle() {
        RutaRetiro ruta = Consola.elegir("Ruta", gr.listarRutas());
        if (ruta == null) return;
        System.out.println(gr.resumenRuta(ruta));
    }
}
