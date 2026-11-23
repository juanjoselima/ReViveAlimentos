package interfaz;

import modelo.*;
import negocio.*;
import java.time.LocalDate;
import java.util.ArrayList;

/**
 * Pantalla de consola del módulo M5 (RF-17 a RF-20).
 */
public class MenuParametros {
    private SistemaReVive sistema;
    private GestorParametros gp;

    public MenuParametros(SistemaReVive sistema) {
        this.sistema = sistema;
        this.gp = sistema.getGestorParametros();
    }

    public void mostrar() {
        int opcion;
        do {
            sistema.actualizarEstadosAutomaticos();
            Consola.titulo("M5 - PARÁMETROS, INDICADORES Y TRAZABILIDAD");
            System.out.println(" 1. Ver parámetros y catálogos   2. Agregar categoría");
            System.out.println(" 3. Agregar unidad               4. Cambiar umbrales de prioridad");
            System.out.println(" 5. Cambiar pesos del puntaje    6. Cambiar tolerancia");
            System.out.println(" 7. Cambiar expiración de propuestas  8. Historial de cambios");
            System.out.println(" 9. Indicadores de aprovechamiento   10. Informe de incidentes");
            System.out.println("11. Trazabilidad de una oferta   12. Adelantar reloj (pruebas)");
            System.out.println(" 0. Volver");
            opcion = Consola.leerEntero("Opción: ", 0, 12);
            try {
                switch (opcion) {
                    case 1: verParametros(); break;
                    case 2: agregarCategoria(); break;
                    case 3: agregarUnidad(); break;
                    case 4: cambiarUmbrales(); break;
                    case 5: cambiarPesos(); break;
                    case 6: gp.cambiarTolerancia(Consola.leerDecimal("Nueva tolerancia (%): ")); Consola.exito("Tolerancia actualizada."); break;
                    case 7: gp.cambiarExpiracion(Consola.leerEntero("Minutos de expiración: ", -100000, 100000)); Consola.exito("Expiración actualizada."); break;
                    case 8: Consola.listar("Historial de cambios de parámetros", gp.listarHistorial()); break;
                    case 9: indicadores(); break;
                    case 10: System.out.println(sistema.getGestorIndicadores().informeIncidentes()); break;
                    case 11: trazabilidad(); break;
                    case 12: adelantarReloj(); break;
                }
            } catch (ReglaNegocioException e) {
                Consola.error(e.getMessage());
            }
        } while (opcion != 0);
    }

    private void verParametros() {
        System.out.println("\n" + sistema.getParametros());
        Consola.listar("Categorías", gp.listarCategorias());
        Consola.listar("Unidades", gp.listarUnidades());
    }

    private void agregarCategoria() throws ReglaNegocioException {
        String nombre = Consola.leerTexto("Nombre de la categoría: ");
        boolean perecible = Consola.leerSiNo("¿Es perecible (requiere refrigeración)?");
        Consola.exito("Categoría agregada: " + gp.agregarCategoria(nombre, perecible));
    }

    private void agregarUnidad() throws ReglaNegocioException {
        String nombre = Consola.leerTexto("Nombre de la unidad: ");
        double factor = Consola.leerDecimal("¿Cuántos kg equivale 1 " + nombre + "?: ");
        Consola.exito("Unidad agregada: " + gp.agregarUnidad(nombre, factor));
    }

    private void cambiarUmbrales() throws ReglaNegocioException {
        double c = Consola.leerDecimal("Umbral Crítica (horas): ");
        double a = Consola.leerDecimal("Umbral Alta (horas): ");
        double m = Consola.leerDecimal("Umbral Media (horas): ");
        gp.cambiarUmbrales(c, a, m);
        Consola.exito("Umbrales actualizados: " + sistema.getParametros().textoUmbrales());
    }

    private void cambiarPesos() throws ReglaNegocioException {
        int z = Consola.leerEntero("Peso zona: ", -1000, 1000);
        int c = Consola.leerEntero("Peso capacidad: ", -1000, 1000);
        int h = Consola.leerEntero("Peso horario: ", -1000, 1000);
        int hi = Consola.leerEntero("Peso historial: ", -1000, 1000);
        gp.cambiarPesos(z, c, h, hi);
        Consola.exito("Pesos actualizados: " + sistema.getParametros().textoPesos());
    }

    private void indicadores() throws ReglaNegocioException {
        LocalDate desde = Consola.leerFecha("Fecha inicial");
        LocalDate hasta = Consola.leerFecha("Fecha final");
        Zona zona = null;
        if (!Consola.leerSiNo("¿Todas las zonas?")) {
            zona = Consola.elegir("Zona", sistema.getGestorDonantes().listarZonas());
        }
        System.out.println(sistema.getGestorIndicadores().reporteIndicadores(desde, hasta, zona));
    }

    private void trazabilidad() {
        Oferta o = Consola.elegir("Oferta", sistema.getGestorOfertas().listarOfertas());
        if (o == null) return;
        ArrayList<RegistroTrazabilidad> registros = sistema.getBitacora().consultar(o.getId());
        Consola.listar("Trazabilidad de la oferta " + o.getId() + " (solo lectura)", registros);
    }

    private void adelantarReloj() {
        int minutos = Consola.leerEntero("Minutos a adelantar: ", 1, 100000);
        Reloj.adelantar(minutos);
        sistema.actualizarEstadosAutomaticos();
        Consola.exito("Hora del sistema: " + Formato.fechaHora(Reloj.ahora()));
    }
}
