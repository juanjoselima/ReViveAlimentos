package negocio;

import modelo.*;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;

/**
 * Carga datos de demostración para probar el sistema sin escribir todo a mano.
 * Usa los mismos gestores, así que también pasan por todas las validaciones.
 */
public class DatosIniciales {

    public static void cargar(SistemaReVive s) throws ReglaNegocioException {
        GestorDonantes gd = s.getGestorDonantes();
        GestorParametros gp = s.getGestorParametros();

        // Zonas de Quito y adyacencias (RF-02)
        Zona mariscal = gd.registrarZona("La Mariscal");
        Zona floresta = gd.registrarZona("La Floresta");
        Zona centro = gd.registrarZona("Centro Histórico");
        Zona chimbacalle = gd.registrarZona("Chimbacalle");
        Zona cotocollao = gd.registrarZona("Cotocollao");
        Zona carcelen = gd.registrarZona("Carcelén");
        gd.marcarAdyacentes(mariscal, floresta);
        gd.marcarAdyacentes(mariscal, centro);
        gd.marcarAdyacentes(centro, chimbacalle);
        gd.marcarAdyacentes(cotocollao, carcelen);

        // Donantes (RF-01)
        Donante trigal = gd.registrarDonante("Panadería El Trigal", "1791234567001", "Panadería", "0991112233", mariscal);
        Donante aroma = gd.registrarDonante("Cafetería Dulce Aroma", "1792345678001", "Cafetería", "0992223344", floresta);
        Donante sabor = gd.registrarDonante("Restaurante Sabor Quiteño", "1793456789001", "Restaurante", "0993334455", centro);
        Donante cosecha = gd.registrarDonante("Frutería La Cosecha", "1794567890001", "Frutería", "0994445566", cotocollao);

        // Responsables operativos con cédulas válidas (RF-05)
        GestorEvaluacion ge = s.getGestorEvaluacion();
        Responsable eva = ge.registrarResponsable("1712345675", "María Torres", Responsable.EVALUADOR);
        ge.registrarResponsable("1723456784", "Luis Andrade", Responsable.EVALUADOR);
        ge.registrarResponsable("1709876542", "Carlos Pérez", Responsable.RETIRO);
        ge.registrarResponsable("1711223345", "Sofía Vega", Responsable.RETIRO);
        ge.registrarResponsable("1755667787", "Daniel Ruiz", Responsable.ENTREGA);

        // Receptores (RF-09)
        GestorAsignaciones ga = s.getGestorAsignaciones();
        ArrayList<Categoria> todas = gp.listarCategorias();
        ga.registrarReceptor("Comedor Comunitario San Juan", "0981110000", centro, 60,
                franjas("07:00", "12:00", "13:00", "21:00"), categorias(gp, "Panadería", "Comidas preparadas", "Frutas y verduras"));
        ga.registrarReceptor("Fundación Manos Solidarias", "0982220000", mariscal, 40,
                franjas("08:00", "20:00"), categorias(gp, "Panadería", "Lácteos", "Abarrotes"));
        ga.registrarReceptor("Casa de Acogida Hogar Seguro", "0983330000", floresta, 25,
                franjas("00:00", "23:59"), todas);
        ga.registrarReceptor("Comedor del Adulto Mayor Carcelén", "0984440000", carcelen, 30,
                franjas("09:00", "17:00"), categorias(gp, "Frutas y verduras", "Panadería"));

        // Ofertas con horas relativas al momento de carga (RF-03)
        GestorOfertas go = s.getGestorOfertas();
        LocalDateTime ahora = Reloj.ahora();
        Oferta pan = go.registrarOferta(trigal, gp.buscarCategoria("Panadería"), 20, gp.buscarUnidad("kg"),
                ahora.plusHours(24), ahora.plusHours(3));
        go.registrarOferta(aroma, gp.buscarCategoria("Lácteos"), 15, gp.buscarUnidad("litro"),
                ahora.plusHours(30), ahora.plusHours(5));
        Oferta comida = go.registrarOferta(sabor, gp.buscarCategoria("Comidas preparadas"), 30, gp.buscarUnidad("porción"),
                ahora.plusHours(6), ahora.plusMinutes(90));
        go.registrarOferta(cosecha, gp.buscarCategoria("Frutas y verduras"), 40, gp.buscarUnidad("kg"),
                ahora.plusHours(48), ahora.plusHours(10));

        // Dos ofertas ya evaluadas para poder asignar de inmediato (RF-06)
        ge.evaluarOferta(pan, eva, true, true, true);
        ge.evaluarOferta(comida, eva, true, true, true);

        // Vehículos de la red (RF-21, módulo adicional M6)
        GestorRutas gr = s.getGestorRutas();
        gr.registrarVehiculo("PBA-1234", "Camioneta furgón refrigerada", 300, true);
        gr.registrarVehiculo("PCD-567", "Camioneta de carga", 150, false);
    }

    private static ArrayList<FranjaHoraria> franjas(String... horas) {
        ArrayList<FranjaHoraria> lista = new ArrayList<>();
        for (int i = 0; i + 1 < horas.length; i += 2) {
            lista.add(new FranjaHoraria(LocalTime.parse(horas[i]), LocalTime.parse(horas[i + 1])));
        }
        return lista;
    }

    private static ArrayList<Categoria> categorias(GestorParametros gp, String... nombres) {
        ArrayList<Categoria> lista = new ArrayList<>();
        for (String n : nombres) lista.add(gp.buscarCategoria(n));
        return lista;
    }
}
