package negocio;

import modelo.Oferta;
import modelo.RegistroTrazabilidad;
import java.util.ArrayList;

/**
 * M5 - RF-20: guarda automáticamente cada cambio de estado de las ofertas.
 * Solo permite agregar y consultar; no hay métodos para editar ni eliminar.
 */
public class BitacoraTrazabilidad {
    private ArrayList<RegistroTrazabilidad> registros = new ArrayList<>();

    public void registrar(Oferta oferta, String estadoAnterior, String estadoNuevo, String detalle, String responsable) {
        registros.add(new RegistroTrazabilidad(Reloj.ahora(), oferta.getId(), estadoAnterior, estadoNuevo,
                detalle, responsable));
    }

    /** Registros de una oferta en orden cronológico (se guardan en el orden en que ocurren). */
    public ArrayList<RegistroTrazabilidad> consultar(int idOferta) {
        ArrayList<RegistroTrazabilidad> resultado = new ArrayList<>();
        for (RegistroTrazabilidad r : registros) {
            if (r.getIdOferta() == idOferta) resultado.add(r);
        }
        return resultado;
    }

    public int totalRegistros() { return registros.size(); }
}
