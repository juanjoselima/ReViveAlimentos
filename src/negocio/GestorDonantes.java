package negocio;

import modelo.*;
import java.util.ArrayList;

/**
 * M1 - Donantes y zonas (RF-01 y RF-02). Responsable: Juan José Lima.
 */
public class GestorDonantes {
    private SistemaReVive sistema;

    public GestorDonantes(SistemaReVive sistema) {
        this.sistema = sistema;
    }

    // ---------------- RF-02: zonas y adyacencias ----------------

    public Zona registrarZona(String nombre) throws ReglaNegocioException {
        nombre = Validador.obligatorio(nombre, "Nombre de la zona");
        if (buscarZonaPorNombre(nombre) != null) {
            throw new ReglaNegocioException("Ya existe una zona llamada '" + nombre + "'.");
        }
        Zona zona = new Zona(nombre);
        sistema.getZonas().add(zona);
        return zona;
    }

    /** Marca A y B como vecinas en ambos sentidos. */
    public void marcarAdyacentes(Zona a, Zona b) throws ReglaNegocioException {
        if (a == null || b == null) throw new ReglaNegocioException("Debe seleccionar dos zonas existentes.");
        if (a == b) throw new ReglaNegocioException("Una zona no puede ser adyacente de sí misma.");
        if (a.esAdyacente(b)) throw new ReglaNegocioException("Las zonas ya están registradas como adyacentes.");
        a.agregarAdyacente(b);
        b.agregarAdyacente(a);   // simetría automática
    }

    public void quitarAdyacencia(Zona a, Zona b) throws ReglaNegocioException {
        if (a == null || b == null || !a.esAdyacente(b)) {
            throw new ReglaNegocioException("Esas zonas no son adyacentes.");
        }
        a.quitarAdyacente(b);
        b.quitarAdyacente(a);
    }

    public void eliminarZona(Zona zona) throws ReglaNegocioException {
        for (Donante d : sistema.getDonantes()) {
            if (d.getZona() == zona) throw new ReglaNegocioException("No se puede eliminar: la zona tiene donantes asociados.");
        }
        for (Receptor r : sistema.getReceptores()) {
            if (r.getZona() == zona) throw new ReglaNegocioException("No se puede eliminar: la zona tiene receptores asociados.");
        }
        for (Zona vecina : zona.getAdyacentes()) {
            vecina.quitarAdyacente(zona);
        }
        sistema.getZonas().remove(zona);
    }

    public Zona buscarZona(int id) {
        for (Zona z : sistema.getZonas()) {
            if (z.getId() == id) return z;
        }
        return null;
    }

    public Zona buscarZonaPorNombre(String nombre) {
        for (Zona z : sistema.getZonas()) {
            if (z.getNombre().equalsIgnoreCase(nombre.trim())) return z;
        }
        return null;
    }

    public ArrayList<Zona> listarZonas() { return new ArrayList<>(sistema.getZonas()); }

    // ---------------- RF-01: donantes ----------------

    public Donante registrarDonante(String nombre, String ruc, String tipo, String contacto, Zona zona)
            throws ReglaNegocioException {
        nombre = Validador.obligatorio(nombre, "Nombre");
        ruc = Validador.obligatorio(ruc, "RUC");
        tipo = Validador.obligatorio(tipo, "Tipo de establecimiento");
        contacto = Validador.obligatorio(contacto, "Contacto");
        if (!Validador.rucValido(ruc)) throw new ReglaNegocioException("El RUC debe tener exactamente 13 dígitos numéricos.");
        if (buscarDonantePorRuc(ruc) != null) throw new ReglaNegocioException("Ya existe un donante con el RUC " + ruc + ".");
        if (zona == null || !sistema.getZonas().contains(zona)) throw new ReglaNegocioException("La zona indicada no existe.");

        Donante donante = new Donante(nombre, ruc, tipo, contacto, zona);
        sistema.getDonantes().add(donante);
        return donante;
    }

    public void actualizarDonante(Donante donante, String nombre, String tipo, String contacto, Zona zona)
            throws ReglaNegocioException {
        if (donante == null) throw new ReglaNegocioException("El donante no existe.");
        nombre = Validador.obligatorio(nombre, "Nombre");
        tipo = Validador.obligatorio(tipo, "Tipo de establecimiento");
        contacto = Validador.obligatorio(contacto, "Contacto");
        if (zona == null || !sistema.getZonas().contains(zona)) throw new ReglaNegocioException("La zona indicada no existe.");
        donante.setNombre(nombre);
        donante.setTipo(tipo);
        donante.setContacto(contacto);
        donante.setZona(zona);
    }

    /** No se puede desactivar un donante que tenga ofertas activas. */
    public void desactivarDonante(Donante donante) throws ReglaNegocioException {
        if (donante == null) throw new ReglaNegocioException("El donante no existe.");
        if (!donante.isActivo()) throw new ReglaNegocioException("El donante ya está inactivo.");
        for (Oferta o : sistema.getOfertas()) {
            if (o.getDonante() == donante && sistema.getGestorOfertas().estaActiva(o)) {
                throw new ReglaNegocioException("No se puede desactivar: el donante tiene la oferta " + o.getId()
                        + " en estado " + o.getEstado() + ".");
            }
        }
        donante.setActivo(false);
    }

    public void reactivarDonante(Donante donante) throws ReglaNegocioException {
        if (donante == null) throw new ReglaNegocioException("El donante no existe.");
        donante.setActivo(true);
    }

    public Donante buscarDonante(int id) {
        for (Donante d : sistema.getDonantes()) {
            if (d.getId() == id) return d;
        }
        return null;
    }

    public Donante buscarDonantePorRuc(String ruc) {
        for (Donante d : sistema.getDonantes()) {
            if (d.getRuc().equals(ruc)) return d;
        }
        return null;
    }

    public ArrayList<Donante> listarDonantes() { return new ArrayList<>(sistema.getDonantes()); }
}
