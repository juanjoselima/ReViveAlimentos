package interfaz;

import modelo.*;
import negocio.*;
import java.time.LocalTime;
import java.util.ArrayList;

/**
 * Pantalla de consola del módulo M3 (RF-09 a RF-12).
 */
public class MenuAsignaciones {
    private SistemaReVive sistema;
    private GestorAsignaciones ga;

    public MenuAsignaciones(SistemaReVive sistema) {
        this.sistema = sistema;
        this.ga = sistema.getGestorAsignaciones();
    }

    public void mostrar() {
        int opcion;
        do {
            sistema.actualizarEstadosAutomaticos();
            Consola.titulo("M3 - RECEPTORES Y ASIGNACIONES");
            System.out.println(" 1. Registrar receptor        2. Actualizar receptor");
            System.out.println(" 3. Activar/desactivar receptor  4. Listar receptores");
            System.out.println(" 5. Ver receptores compatibles y puntaje de una oferta");
            System.out.println(" 6. Proponer asignación       7. Confirmar asignación");
            System.out.println(" 8. Rechazar asignación       9. Cancelar asignación");
            System.out.println("10. Listar asignaciones");
            System.out.println(" 0. Volver");
            opcion = Consola.leerEntero("Opción: ", 0, 10);
            try {
                switch (opcion) {
                    case 1: registrar(); break;
                    case 2: actualizar(); break;
                    case 3: cambiarEstado(); break;
                    case 4: listarReceptores(); break;
                    case 5: verCompatibles(); break;
                    case 6: proponer(); break;
                    case 7: confirmar(); break;
                    case 8: rechazar(); break;
                    case 9: cancelar(); break;
                    case 10: Consola.listar("Asignaciones", ga.listarAsignaciones()); break;
                }
            } catch (ReglaNegocioException e) {
                Consola.error(e.getMessage());
            }
        } while (opcion != 0);
    }

    private ArrayList<FranjaHoraria> leerFranjas() {
        ArrayList<FranjaHoraria> franjas = new ArrayList<>();
        do {
            LocalTime inicio = Consola.leerHora("  Inicio de la franja");
            LocalTime fin = Consola.leerHora("  Fin de la franja");
            franjas.add(new FranjaHoraria(inicio, fin));
        } while (Consola.leerSiNo("¿Agregar otra franja?"));
        return franjas;
    }

    private ArrayList<Categoria> leerCategorias() {
        ArrayList<Categoria> elegidas = new ArrayList<>();
        ArrayList<Categoria> disponibles = sistema.getGestorParametros().listarCategorias();
        do {
            Categoria c = Consola.elegir("Categoría que acepta", disponibles);
            if (c != null && !elegidas.contains(c)) elegidas.add(c);
        } while (Consola.leerSiNo("¿Agregar otra categoría?"));
        return elegidas;
    }

    private void registrar() throws ReglaNegocioException {
        String nombre = Consola.leerTexto("Nombre de la organización: ");
        String contacto = Consola.leerTexto("Contacto: ");
        Zona zona = Consola.elegir("Zona", sistema.getGestorDonantes().listarZonas());
        double capacidad = Consola.leerDecimal("Capacidad máxima diaria (kg): ");
        System.out.println("Franjas de recepción:");
        ArrayList<FranjaHoraria> franjas = leerFranjas();
        ArrayList<Categoria> categorias = leerCategorias();
        Receptor r = ga.registrarReceptor(nombre, contacto, zona, capacidad, franjas, categorias);
        Consola.exito("Receptor registrado: " + r);
    }

    private void actualizar() throws ReglaNegocioException {
        Receptor r = Consola.elegir("Receptor", ga.listarReceptores());
        if (r == null) return;
        String nombre = Consola.leerTexto("Nombre [" + r.getNombre() + "]: ");
        String contacto = Consola.leerTexto("Contacto [" + r.getContacto() + "]: ");
        if (nombre.isEmpty()) nombre = r.getNombre();
        if (contacto.isEmpty()) contacto = r.getContacto();
        Zona zona = Consola.elegir("Zona", sistema.getGestorDonantes().listarZonas());
        if (zona == null) zona = r.getZona();
        double capacidad = Consola.leerDecimal("Capacidad máxima diaria (kg): ");
        System.out.println("Franjas de recepción (reemplazan a las anteriores):");
        ArrayList<FranjaHoraria> franjas = leerFranjas();
        ArrayList<Categoria> categorias = leerCategorias();
        ga.actualizarReceptor(r, nombre, contacto, zona, capacidad, franjas, categorias);
        Consola.exito("Receptor actualizado: " + r);
    }

    private void cambiarEstado() throws ReglaNegocioException {
        Receptor r = Consola.elegir("Receptor", ga.listarReceptores());
        if (r == null) return;
        ga.cambiarEstadoReceptor(r, !r.isActivo());
        Consola.exito("Receptor ahora está " + (r.isActivo() ? "activo." : "inactivo."));
    }

    private void listarReceptores() {
        System.out.println("\n== Receptores ==");
        for (Receptor r : ga.listarReceptores()) {
            System.out.println("  " + r);
            System.out.println("      Capacidad restante hoy: " + Formato.num(ga.capacidadRestanteKg(r, Reloj.ahora().toLocalDate())) + " kg");
        }
    }

    /** Muestra la recomendación ordenada; la usa también "proponer". */
    private ArrayList<Receptor> mostrarRecomendacion(Oferta o) throws ReglaNegocioException {
        ArrayList<Receptor> lista = ga.recomendarReceptores(o);
        System.out.println("\n== Receptores compatibles para la oferta " + o.getId() + " (disponible: "
                + Formato.num(o.calcularDisponible()) + " " + o.getUnidad().getNombre() + ") ==");
        if (lista.isEmpty()) System.out.println("  Ningún receptor cumple los filtros (categoría, horario, capacidad, activo).");
        int pos = 1;
        for (Receptor r : lista) {
            System.out.println("  " + pos + ". " + r.getNombre() + " | Zona: " + r.getZona().getNombre()
                    + " | Puntaje: " + Formato.num(ga.calcularPuntaje(o, r)) + "/100"
                    + " | Capacidad restante: " + Formato.num(ga.capacidadRestanteKg(r, o.getHoraLimiteRetiro().toLocalDate())) + " kg"
                    + " | Kg recibidos en la semana: " + Formato.num(ga.kgRecibidosSemana(r)));
            pos++;
        }
        return lista;
    }

    private Oferta elegirOfertaAsignable() {
        ArrayList<Oferta> asignables = new ArrayList<>();
        for (Oferta o : sistema.getOfertas()) {
            if (sistema.getGestorEvaluacion().esAsignable(o)) asignables.add(o);
        }
        return Consola.elegir("Ofertas asignables", asignables);
    }

    private void verCompatibles() throws ReglaNegocioException {
        Oferta o = elegirOfertaAsignable();
        if (o == null) return;
        mostrarRecomendacion(o);
    }

    private void proponer() throws ReglaNegocioException {
        Oferta o = elegirOfertaAsignable();
        if (o == null) return;
        ArrayList<Receptor> lista = mostrarRecomendacion(o);
        if (lista.isEmpty()) return;
        int pos = Consola.leerEntero("Elija el receptor (0 = cancelar): ", 0, lista.size());
        if (pos == 0) return;
        double cantidad = Consola.leerDecimal("Cantidad a asignar (" + o.getUnidad().getNombre() + "): ");
        Asignacion a = ga.proponerAsignacion(o, lista.get(pos - 1), cantidad);
        Consola.exito("Propuesta creada: " + a + ". Debe confirmarse en "
                + sistema.getParametros().getMinutosExpiracion() + " minutos.");
    }

    private ArrayList<Asignacion> filtrarPorEstado(String... estados) {
        ArrayList<Asignacion> lista = new ArrayList<>();
        for (Asignacion a : ga.listarAsignaciones()) {
            for (String e : estados) {
                if (a.getEstado().equals(e)) lista.add(a);
            }
        }
        return lista;
    }

    private void confirmar() throws ReglaNegocioException {
        Asignacion a = Consola.elegir("Asignaciones propuestas", filtrarPorEstado(Asignacion.PROPUESTA));
        if (a == null) return;
        ga.confirmarAsignacion(a);
        Consola.exito("Asignación confirmada. Oferta " + a.getOferta().getId() + " en estado " + a.getOferta().getEstado());
    }

    private void rechazar() throws ReglaNegocioException {
        Asignacion a = Consola.elegir("Asignaciones propuestas", filtrarPorEstado(Asignacion.PROPUESTA));
        if (a == null) return;
        ga.rechazarAsignacion(a);
        Consola.exito("Asignación rechazada; la cantidad vuelve a estar disponible.");
    }

    private void cancelar() throws ReglaNegocioException {
        Asignacion a = Consola.elegir("Asignaciones activas", filtrarPorEstado(Asignacion.PROPUESTA, Asignacion.CONFIRMADA));
        if (a == null) return;
        ga.cancelarAsignacion(a);
        Consola.exito("Asignación cancelada.");
    }
}
