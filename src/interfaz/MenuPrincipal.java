package interfaz;

import negocio.DatosIniciales;
import negocio.ReglaNegocioException;
import negocio.Reloj;
import negocio.SistemaReVive;
import modelo.Formato;

/**
 * Clase con el método main. Muestra el menú principal y abre el menú de cada módulo.
 */
public class MenuPrincipal {

    public static void main(String[] args) {
        SistemaReVive sistema = new SistemaReVive();
        Consola.titulo("ReVive Alimentos - Coordinación de excedentes (modo consola)");

        if (Consola.leerSiNo("¿Desea cargar los datos de demostración?")) {
            try {
                DatosIniciales.cargar(sistema);
                Consola.exito("Datos de demostración cargados.");
            } catch (ReglaNegocioException e) {
                Consola.error("No se pudieron cargar los datos: " + e.getMessage());
            }
        }

        MenuDonantesOfertas m1 = new MenuDonantesOfertas(sistema);
        MenuEvaluacion m2 = new MenuEvaluacion(sistema);
        MenuAsignaciones m3 = new MenuAsignaciones(sistema);
        MenuLogistica m4 = new MenuLogistica(sistema);
        MenuParametros m5 = new MenuParametros(sistema);

        int opcion;
        do {
            sistema.actualizarEstadosAutomaticos();
            Consola.titulo("MENÚ PRINCIPAL  |  Hora del sistema: " + Formato.fechaHora(Reloj.ahora()));
            System.out.println("1. M1 - Donantes, zonas y ofertas");
            System.out.println("2. M2 - Evaluación y prioridad");
            System.out.println("3. M3 - Receptores y asignaciones");
            System.out.println("4. M4 - Retiros, entregas e incidentes");
            System.out.println("5. M5 - Parámetros, indicadores y trazabilidad");
            System.out.println("0. Salir");
            opcion = Consola.leerEntero("Opción: ", 0, 5);
            switch (opcion) {
                case 1: m1.mostrar(); break;
                case 2: m2.mostrar(); break;
                case 3: m3.mostrar(); break;
                case 4: m4.mostrar(); break;
                case 5: m5.mostrar(); break;
                default: System.out.println("Gracias por usar ReVive Alimentos.");
            }
        } while (opcion != 0);
    }
}
