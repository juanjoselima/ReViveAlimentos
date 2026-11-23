package interfaz;

import modelo.*;
import negocio.*;
import java.util.ArrayList;

/**
 * Pantalla de consola del módulo M2 (RF-05 a RF-08).
 */
public class MenuEvaluacion {
    private SistemaReVive sistema;
    private GestorEvaluacion ge;

    public MenuEvaluacion(SistemaReVive sistema) {
        this.sistema = sistema;
        this.ge = sistema.getGestorEvaluacion();
    }

    public void mostrar() {
        int opcion;
        do {
            sistema.actualizarEstadosAutomaticos();
            Consola.titulo("M2 - EVALUACIÓN Y PRIORIDAD");
            System.out.println("1. Registrar responsable operativo");
            System.out.println("2. Actualizar responsable");
            System.out.println("3. Listar responsables");
            System.out.println("4. Evaluar elegibilidad de una oferta");
            System.out.println("5. Ver ofertas ordenadas por prioridad");
            System.out.println("6. Verificar si una oferta es asignable");
            System.out.println("0. Volver");
            opcion = Consola.leerEntero("Opción: ", 0, 6);
            try {
                switch (opcion) {
                    case 1: registrar(); break;
                    case 2: actualizar(); break;
                    case 3: Consola.listar("Responsables operativos", sistema.getResponsables()); break;
                    case 4: evaluar(); break;
                    case 5: verPrioridad(); break;
                    case 6: verificarAsignable(); break;
                }
            } catch (ReglaNegocioException e) {
                Consola.error(e.getMessage());
            }
        } while (opcion != 0);
    }

    private String elegirFuncion() {
        System.out.println("Función: 1. Evaluador  2. Retiro  3. Entrega");
        int f = Consola.leerEntero("Opción: ", 1, 3);
        if (f == 1) return Responsable.EVALUADOR;
        if (f == 2) return Responsable.RETIRO;
        return Responsable.ENTREGA;
    }

    private void registrar() throws ReglaNegocioException {
        String cedula = Consola.leerTexto("Cédula (10 dígitos): ");
        String nombre = Consola.leerTexto("Nombre completo: ");
        String funcion = elegirFuncion();
        Responsable r = ge.registrarResponsable(cedula, nombre, funcion);
        Consola.exito("Responsable registrado: " + r);
    }

    private void actualizar() throws ReglaNegocioException {
        Responsable r = Consola.elegir("Responsable", sistema.getResponsables());
        if (r == null) return;
        String nombre = Consola.leerTexto("Nombre [" + r.getNombre() + "]: ");
        if (nombre.isEmpty()) nombre = r.getNombre();
        String funcion = elegirFuncion();
        boolean disponible = Consola.leerSiNo("¿Está disponible?");
        ge.actualizarResponsable(r, nombre, funcion, disponible);
        Consola.exito("Responsable actualizado: " + r);
    }

    private void evaluar() throws ReglaNegocioException {
        ArrayList<Oferta> pendientes = new ArrayList<>();
        for (Oferta o : sistema.getOfertas()) {
            if (o.getEstado().equals(Oferta.REGISTRADA)) pendientes.add(o);
        }
        Oferta o = Consola.elegir("Ofertas pendientes de evaluación", pendientes);
        if (o == null) return;
        Responsable ev = Consola.elegir("Evaluador", ge.listarPorFuncion(Responsable.EVALUADOR));
        if (ev == null) return;
        System.out.println("Lista de verificación:");
        boolean empaque = Consola.leerSiNo("  ¿Empaque íntegro?");
        boolean etiqueta = Consola.leerSiNo("  ¿Etiqueta con fecha de caducidad?");
        boolean temperatura = true;
        if (o.getCategoria().isPerecible()) {
            temperatura = Consola.leerSiNo("  ¿Temperatura adecuada? (producto refrigerado)");
        }
        Evaluacion resultado = ge.evaluarOferta(o, ev, empaque, etiqueta, temperatura);
        Consola.exito(resultado + " -> Oferta en estado " + o.getEstado());
    }

    private void verPrioridad() {
        System.out.println("\n== Ofertas por prioridad (Crítica < " + sistema.getParametros().getUmbralCritica()
                + " h, Alta < " + sistema.getParametros().getUmbralAlta() + " h, Media < "
                + sistema.getParametros().getUmbralMedia() + " h; perecibles suben un nivel) ==");
        ArrayList<Oferta> lista = ge.listarPorPrioridad();
        if (lista.isEmpty()) System.out.println("  (no hay ofertas pendientes)");
        for (Oferta o : lista) {
            System.out.println("  [" + ge.nombrePrioridad(o) + "] faltan " + Formato.num(ge.calcularHorasRestantes(o))
                    + " h | " + o);
        }
    }

    private void verificarAsignable() {
        Oferta o = Consola.elegir("Oferta", sistema.getGestorOfertas().listarOfertas());
        if (o == null) return;
        String motivo = ge.motivoNoAsignable(o);
        if (motivo == null) Consola.exito("La oferta " + o.getId() + " es asignable.");
        else Consola.error("La oferta " + o.getId() + " NO es asignable: " + motivo + ".");
    }
}
