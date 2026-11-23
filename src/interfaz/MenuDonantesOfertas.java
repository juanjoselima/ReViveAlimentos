package interfaz;

import modelo.*;
import negocio.*;
import java.time.LocalDateTime;

/**
 * Pantalla de consola del módulo M1 (RF-01 a RF-04).
 */
public class MenuDonantesOfertas {
    private SistemaReVive sistema;
    private GestorDonantes gd;
    private GestorOfertas go;

    public MenuDonantesOfertas(SistemaReVive sistema) {
        this.sistema = sistema;
        this.gd = sistema.getGestorDonantes();
        this.go = sistema.getGestorOfertas();
    }

    public void mostrar() {
        int opcion;
        do {
            sistema.actualizarEstadosAutomaticos();
            Consola.titulo("M1 - DONANTES, ZONAS Y OFERTAS");
            System.out.println(" 1. Registrar zona            2. Marcar zonas adyacentes");
            System.out.println(" 3. Quitar adyacencia         4. Eliminar zona");
            System.out.println(" 5. Listar zonas");
            System.out.println(" 6. Registrar donante         7. Actualizar donante");
            System.out.println(" 8. Desactivar/reactivar donante  9. Listar donantes");
            System.out.println("10. Registrar oferta         11. Editar oferta");
            System.out.println("12. Cancelar oferta          13. Listar ofertas y disponibilidad");
            System.out.println(" 0. Volver");
            opcion = Consola.leerEntero("Opción: ", 0, 13);
            try {
                switch (opcion) {
                    case 1: registrarZona(); break;
                    case 2: adyacencia(true); break;
                    case 3: adyacencia(false); break;
                    case 4: eliminarZona(); break;
                    case 5: Consola.listar("Zonas", gd.listarZonas()); break;
                    case 6: registrarDonante(); break;
                    case 7: actualizarDonante(); break;
                    case 8: cambiarEstadoDonante(); break;
                    case 9: Consola.listar("Donantes", gd.listarDonantes()); break;
                    case 10: registrarOferta(); break;
                    case 11: editarOferta(); break;
                    case 12: cancelarOferta(); break;
                    case 13: listarOfertas(); break;
                }
            } catch (ReglaNegocioException e) {
                Consola.error(e.getMessage());
            }
        } while (opcion != 0);
    }

    private void registrarZona() throws ReglaNegocioException {
        Zona z = gd.registrarZona(Consola.leerTexto("Nombre de la zona: "));
        Consola.exito("Zona registrada: " + z);
    }

    private void adyacencia(boolean marcar) throws ReglaNegocioException {
        Zona a = Consola.elegir("Zona A", gd.listarZonas());
        if (a == null) return;
        Zona b = Consola.elegir("Zona B", gd.listarZonas());
        if (b == null) return;
        if (marcar) {
            gd.marcarAdyacentes(a, b);
            Consola.exito(a.getNombre() + " y " + b.getNombre() + " ahora son adyacentes (en ambos sentidos).");
        } else {
            gd.quitarAdyacencia(a, b);
            Consola.exito("Adyacencia eliminada.");
        }
    }

    private void eliminarZona() throws ReglaNegocioException {
        Zona z = Consola.elegir("Zona a eliminar", gd.listarZonas());
        if (z == null) return;
        gd.eliminarZona(z);
        Consola.exito("Zona eliminada.");
    }

    private void registrarDonante() throws ReglaNegocioException {
        String nombre = Consola.leerTexto("Nombre del establecimiento: ");
        String ruc = Consola.leerTexto("RUC (13 dígitos): ");
        String tipo = Consola.leerTexto("Tipo (panadería, cafetería, restaurante...): ");
        String contacto = Consola.leerTexto("Contacto (teléfono o correo): ");
        Zona zona = Consola.elegir("Zona", gd.listarZonas());
        Donante d = gd.registrarDonante(nombre, ruc, tipo, contacto, zona);
        Consola.exito("Donante registrado: " + d);
    }

    private void actualizarDonante() throws ReglaNegocioException {
        Donante d = Consola.elegir("Donante a actualizar", gd.listarDonantes());
        if (d == null) return;
        String nombre = Consola.leerTexto("Nuevo nombre [" + d.getNombre() + "]: ");
        String tipo = Consola.leerTexto("Nuevo tipo [" + d.getTipo() + "]: ");
        String contacto = Consola.leerTexto("Nuevo contacto [" + d.getContacto() + "]: ");
        Zona zona = Consola.elegir("Nueva zona", gd.listarZonas());
        // Si deja vacío, se conserva el valor anterior
        if (nombre.isEmpty()) nombre = d.getNombre();
        if (tipo.isEmpty()) tipo = d.getTipo();
        if (contacto.isEmpty()) contacto = d.getContacto();
        if (zona == null) zona = d.getZona();
        gd.actualizarDonante(d, nombre, tipo, contacto, zona);
        Consola.exito("Donante actualizado: " + d);
    }

    private void cambiarEstadoDonante() throws ReglaNegocioException {
        Donante d = Consola.elegir("Donante", gd.listarDonantes());
        if (d == null) return;
        if (d.isActivo()) {
            gd.desactivarDonante(d);
            Consola.exito("Donante desactivado.");
        } else {
            gd.reactivarDonante(d);
            Consola.exito("Donante reactivado.");
        }
    }

    private void registrarOferta() throws ReglaNegocioException {
        Donante d = Consola.elegir("Donante", gd.listarDonantes());
        if (d == null) return;
        Categoria c = Consola.elegir("Categoría", sistema.getGestorParametros().listarCategorias());
        if (c == null) return;
        Unidad u = Consola.elegir("Unidad", sistema.getGestorParametros().listarUnidades());
        if (u == null) return;
        double cantidad = Consola.leerDecimal("Cantidad: ");
        LocalDateTime caducidad = Consola.leerFechaHora("Fecha de caducidad");
        LocalDateTime limite = Consola.leerFechaHora("Hora límite de retiro");
        Oferta o = go.registrarOferta(d, c, cantidad, u, caducidad, limite);
        Consola.exito("Oferta registrada: " + o);
    }

    private void editarOferta() throws ReglaNegocioException {
        Oferta o = Consola.elegir("Oferta a editar", go.listarOfertas());
        if (o == null) return;
        if (!o.getEstado().equals(Oferta.REGISTRADA)) {
            throw new ReglaNegocioException("Solo se puede editar una oferta en estado Registrada (estado actual: " + o.getEstado() + ").");
        }
        Categoria c = Consola.elegir("Categoría", sistema.getGestorParametros().listarCategorias());
        Unidad u = Consola.elegir("Unidad", sistema.getGestorParametros().listarUnidades());
        double cantidad = Consola.leerDecimal("Cantidad: ");
        LocalDateTime caducidad = Consola.leerFechaHora("Fecha de caducidad");
        LocalDateTime limite = Consola.leerFechaHora("Hora límite de retiro");
        go.editarOferta(o, c, cantidad, u, caducidad, limite);
        Consola.exito("Oferta actualizada: " + o);
    }

    private void cancelarOferta() throws ReglaNegocioException {
        Oferta o = Consola.elegir("Oferta a cancelar", go.listarOfertas());
        if (o == null) return;
        go.cancelarOferta(o);
        Consola.exito("Oferta " + o.getId() + " cancelada.");
    }

    private void listarOfertas() {
        System.out.println("\n== Ofertas (RF-04: Disponible = Ofrecida - Comprometida) ==");
        if (go.listarOfertas().isEmpty()) System.out.println("  (sin registros)");
        for (Oferta o : go.listarOfertas()) {
            System.out.println("  " + o);
            System.out.println("      Ofrecida: " + Formato.num(o.getCantidad()) + " | Comprometida: "
                    + Formato.num(o.calcularComprometida()) + " | Disponible: " + Formato.num(go.calcularDisponible(o))
                    + " " + o.getUnidad().getNombre());
        }
    }
}
