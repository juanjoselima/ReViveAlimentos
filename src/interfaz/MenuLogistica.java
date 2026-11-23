package interfaz;

import modelo.*;
import negocio.*;
import java.time.LocalDateTime;
import java.util.ArrayList;

/**
 * Pantalla de consola del módulo M4 (RF-13 a RF-16).
 */
public class MenuLogistica {
    private SistemaReVive sistema;
    private GestorLogistica gl;

    public MenuLogistica(SistemaReVive sistema) {
        this.sistema = sistema;
        this.gl = sistema.getGestorLogistica();
    }

    public void mostrar() {
        int opcion;
        do {
            sistema.actualizarEstadosAutomaticos();
            Consola.titulo("M4 - RETIROS, ENTREGAS E INCIDENTES");
            System.out.println("1. Programar retiro           2. Registrar recogida");
            System.out.println("3. Registrar entrega          4. Registrar incidente");
            System.out.println("5. Cerrar incidente           6. Calcular diferencias y cerrar asignación");
            System.out.println("7. Listar retiros y entregas  8. Listar incidentes");
            System.out.println("0. Volver");
            opcion = Consola.leerEntero("Opción: ", 0, 8);
            try {
                switch (opcion) {
                    case 1: programarRetiro(); break;
                    case 2: registrarRecogida(); break;
                    case 3: registrarEntrega(); break;
                    case 4: registrarIncidente(); break;
                    case 5: cerrarIncidente(); break;
                    case 6: cerrarAsignacion(); break;
                    case 7: Consola.listar("Retiros", sistema.getRetiros()); Consola.listar("Entregas", sistema.getEntregas()); break;
                    case 8: Consola.listar("Incidentes", sistema.getIncidentes()); break;
                }
            } catch (ReglaNegocioException e) {
                Consola.error(e.getMessage());
            }
        } while (opcion != 0);
    }

    private ArrayList<Asignacion> confirmadas() {
        ArrayList<Asignacion> lista = new ArrayList<>();
        for (Asignacion a : sistema.getAsignaciones()) {
            if (a.getEstado().equals(Asignacion.CONFIRMADA)) lista.add(a);
        }
        return lista;
    }

    private void programarRetiro() throws ReglaNegocioException {
        Asignacion a = Consola.elegir("Asignaciones confirmadas", confirmadas());
        if (a == null) return;
        Responsable r = Consola.elegir("Responsable de retiro", sistema.getGestorEvaluacion().listarPorFuncion(Responsable.RETIRO));
        if (r == null) return;
        System.out.println("Hora límite de la oferta: " + Formato.fechaHora(a.getOferta().getHoraLimiteRetiro()));
        LocalDateTime hora = Consola.leerFechaHora("Hora del retiro");
        Retiro ret = gl.programarRetiro(a, r, hora);
        Consola.exito("Retiro programado: " + ret);
    }

    private void registrarRecogida() throws ReglaNegocioException {
        ArrayList<Retiro> programados = new ArrayList<>();
        for (Retiro r : sistema.getRetiros()) {
            if (r.getEstado().equals(Retiro.PROGRAMADO)) programados.add(r);
        }
        Retiro r = Consola.elegir("Retiros programados", programados);
        if (r == null) return;
        System.out.println("Cantidad asignada: " + Formato.num(r.getAsignacion().getCantidad()));
        double cantidad = Consola.leerDecimal("Cantidad recogida: ");
        gl.registrarRecogida(r, cantidad);
        Consola.exito("Retiro completado: " + r);
    }

    private void registrarEntrega() throws ReglaNegocioException {
        ArrayList<Retiro> completados = new ArrayList<>();
        for (Retiro r : sistema.getRetiros()) {
            if (r.getEstado().equals(Retiro.COMPLETADO) && gl.buscarEntregaDeRetiro(r) == null) completados.add(r);
        }
        Retiro r = Consola.elegir("Retiros completados sin entrega", completados);
        if (r == null) return;
        Responsable resp = Consola.elegir("Responsable de entrega", sistema.getGestorEvaluacion().listarPorFuncion(Responsable.ENTREGA));
        if (resp == null) return;
        boolean rechazo = Consola.leerSiNo("¿El receptor rechazó el producto?");
        double cantidad = 0;
        String motivo = "";
        if (rechazo) {
            motivo = Consola.leerTexto("Motivo del rechazo: ");
        } else {
            System.out.println("Cantidad recogida: " + Formato.num(r.getCantidadRecogida()));
            cantidad = Consola.leerDecimal("Cantidad entregada: ");
        }
        Entrega e = gl.registrarEntrega(r, resp, cantidad, rechazo, motivo);
        Consola.exito("Entrega registrada: " + e);
    }

    private void registrarIncidente() throws ReglaNegocioException {
        Asignacion a = Consola.elegir("Asignación relacionada", confirmadas());
        if (a == null) return;
        System.out.println("Tipo de incidente:");
        for (int i = 0; i < Incidente.TIPOS.length; i++) System.out.println("  " + (i + 1) + ". " + Incidente.TIPOS[i]);
        int t = Consola.leerEntero("Opción: ", 1, Incidente.TIPOS.length);
        String descripcion = Consola.leerTexto("Descripción: ");
        Incidente inc = gl.registrarIncidente(Incidente.TIPOS[t - 1], a, descripcion);
        Consola.exito("Incidente registrado: " + inc);
    }

    private void cerrarIncidente() throws ReglaNegocioException {
        ArrayList<Incidente> abiertos = new ArrayList<>();
        for (Incidente i : sistema.getIncidentes()) {
            if (i.estaAbierto()) abiertos.add(i);
        }
        Incidente inc = Consola.elegir("Incidentes abiertos", abiertos);
        if (inc == null) return;
        String resolucion = Consola.leerTexto("Resolución (obligatoria): ");
        gl.cerrarIncidente(inc, resolucion);
        Consola.exito("Incidente cerrado.");
    }

    private void cerrarAsignacion() throws ReglaNegocioException {
        Asignacion a = Consola.elegir("Asignaciones confirmadas", confirmadas());
        if (a == null) return;
        System.out.println(gl.resumenMermas(a));
        String resultado = gl.cerrarAsignacion(a);
        Consola.exito(resultado);
    }
}
