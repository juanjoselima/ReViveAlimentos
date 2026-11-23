package negocio;

import modelo.*;
import java.time.Duration;
import java.time.LocalDate;
import java.util.ArrayList;

/**
 * M5 - Indicadores de aprovechamiento e informe de incidentes (RF-18 y RF-19). Responsable: Anahí Barahona.
 */
public class GestorIndicadores {
    public static final double META_APROVECHAMIENTO = 70;
    public static final int INCIDENTES_PARA_OBSERVACION = 3;

    private SistemaReVive sistema;

    public GestorIndicadores(SistemaReVive sistema) {
        this.sistema = sistema;
    }

    // ---------------- RF-18 ----------------

    /** Ofertas registradas dentro del período y de la zona (zona null = todas). */
    private ArrayList<Oferta> ofertasDelPeriodo(LocalDate desde, LocalDate hasta, Zona zona) {
        ArrayList<Oferta> lista = new ArrayList<>();
        for (Oferta o : sistema.getOfertas()) {
            LocalDate dia = o.getFechaRegistro().toLocalDate();
            boolean enPeriodo = !dia.isBefore(desde) && !dia.isAfter(hasta);
            boolean enZona = (zona == null) || o.getDonante().getZona() == zona;
            if (enPeriodo && enZona) lista.add(o);
        }
        return lista;
    }

    private double kgEntregadosDe(Oferta o) {
        double total = 0;
        for (Entrega e : sistema.getEntregas()) {
            if (e.getRetiro().getAsignacion().getOferta() == o) total += e.getCantidadEntregadaKg();
        }
        return total;
    }

    /** % aprovechamiento = kg entregados ÷ kg ofrecidos aptos × 100. */
    public double calcularAprovechamiento(LocalDate desde, LocalDate hasta, Zona zona) {
        double aptos = 0, entregados = 0;
        for (Oferta o : ofertasDelPeriodo(desde, hasta, zona)) {
            if (o.isApta()) {
                aptos += o.getCantidadKg();
                entregados += kgEntregadosDe(o);
            }
        }
        return (aptos == 0) ? 0 : entregados / aptos * 100;
    }

    /** Tasa de vencimiento = ofertas vencidas ÷ ofertas registradas × 100. */
    public double calcularTasaVencimiento(LocalDate desde, LocalDate hasta, Zona zona) {
        ArrayList<Oferta> lista = ofertasDelPeriodo(desde, hasta, zona);
        if (lista.isEmpty()) return 0;
        int vencidas = 0;
        for (Oferta o : lista) {
            if (o.getEstado().equals(Oferta.VENCIDA)) vencidas++;
        }
        return vencidas * 100.0 / lista.size();
    }

    /** Horas promedio desde que se registró la oferta hasta cada entrega. */
    public double calcularTiempoPromedioEntrega(LocalDate desde, LocalDate hasta, Zona zona) {
        ArrayList<Oferta> lista = ofertasDelPeriodo(desde, hasta, zona);
        double sumaHoras = 0;
        int cuenta = 0;
        for (Entrega e : sistema.getEntregas()) {
            Oferta o = e.getRetiro().getAsignacion().getOferta();
            if (lista.contains(o) && !e.getEstado().equals(Entrega.RECHAZADA_RECEPTOR)) {
                sumaHoras += Duration.between(o.getFechaRegistro(), e.getFecha()).toMinutes() / 60.0;
                cuenta++;
            }
        }
        return (cuenta == 0) ? 0 : sumaHoras / cuenta;
    }

    public String reporteIndicadores(LocalDate desde, LocalDate hasta, Zona zona) throws ReglaNegocioException {
        if (desde == null || hasta == null) throw new ReglaNegocioException("Debe indicar el período.");
        if (hasta.isBefore(desde)) throw new ReglaNegocioException("La fecha final no puede ser anterior a la inicial.");
        int registradas = ofertasDelPeriodo(desde, hasta, zona).size();
        double aprovechamiento = calcularAprovechamiento(desde, hasta, zona);
        String texto = "INDICADORES DE APROVECHAMIENTO\n"
                + "Período: " + desde + " a " + hasta + " | Zona: " + (zona == null ? "Todas" : zona.getNombre()) + "\n"
                + "Ofertas registradas: " + registradas + "\n"
                + "% de aprovechamiento: " + Formato.num(aprovechamiento) + " %\n"
                + "Tasa de vencimiento: " + Formato.num(calcularTasaVencimiento(desde, hasta, zona)) + " %\n"
                + "Tiempo promedio registro -> entrega: " + Formato.num(calcularTiempoPromedioEntrega(desde, hasta, zona)) + " horas";
        if (registradas > 0 && aprovechamiento < META_APROVECHAMIENTO) {
            texto += "\n*** ALERTA: el aprovechamiento está por debajo del " + (int) META_APROVECHAMIENTO + " % ***";
        }
        return texto;
    }

    // ---------------- RF-19 ----------------

    /** Marca o desmarca "En observación" según los incidentes de los últimos 30 días. */
    public void actualizarObservacion(Receptor r) {
        int recientes = sistema.getGestorLogistica().contarIncidentesRecientes(r, 30);
        r.setEnObservacion(recientes >= INCIDENTES_PARA_OBSERVACION);
    }

    public String informeIncidentes() {
        ArrayList<Incidente> lista = sistema.getIncidentes();
        if (lista.isEmpty()) return "No hay incidentes registrados.";
        for (Receptor r : sistema.getReceptores()) actualizarObservacion(r);

        // Se arman listas de claves distintas para cada agrupación
        ArrayList<String> tipos = new ArrayList<>();
        ArrayList<String> zonas = new ArrayList<>();
        ArrayList<String> receptores = new ArrayList<>();
        for (Incidente i : lista) {
            if (!tipos.contains(i.getTipo())) tipos.add(i.getTipo());
            String z = i.getReceptor().getZona().getNombre();
            if (!zonas.contains(z)) zonas.add(z);
            if (!receptores.contains(i.getReceptor().getNombre())) receptores.add(i.getReceptor().getNombre());
        }
        String texto = "INFORME DE INCIDENTES (total: " + lista.size() + ")\n";
        texto += "\nPor tipo:\n" + agrupar(tipos, 1);
        texto += "\nPor zona:\n" + agrupar(zonas, 2);
        texto += "\nPor receptor:\n" + agrupar(receptores, 3);
        texto += "\nReceptores en observación (" + INCIDENTES_PARA_OBSERVACION + " o más incidentes en 30 días):\n";
        boolean alguno = false;
        for (Receptor r : sistema.getReceptores()) {
            if (r.isEnObservacion()) {
                texto += "  - " + r.getNombre() + " (pierde los puntos de historial en el puntaje)\n";
                alguno = true;
            }
        }
        if (!alguno) texto += "  Ninguno\n";
        return texto;
    }

    /** criterio: 1 = tipo, 2 = zona, 3 = receptor. */
    private String agrupar(ArrayList<String> claves, int criterio) {
        ArrayList<Incidente> lista = sistema.getIncidentes();
        String texto = "";
        for (String clave : claves) {
            int frecuencia = 0, cerrados = 0;
            double horas = 0;
            for (Incidente i : lista) {
                String valor;
                if (criterio == 1) valor = i.getTipo();
                else if (criterio == 2) valor = i.getReceptor().getZona().getNombre();
                else valor = i.getReceptor().getNombre();
                if (valor.equals(clave)) {
                    frecuencia++;
                    if (!i.estaAbierto()) {
                        cerrados++;
                        horas += i.horasResolucion();
                    }
                }
            }
            double porcentaje = frecuencia * 100.0 / lista.size();
            String promedio = (cerrados == 0) ? "sin cerrar" : Formato.num(horas / cerrados) + " h";
            texto += "  - " + clave + ": " + frecuencia + " (" + Formato.num(porcentaje) + " %) | Resolución promedio: " + promedio + "\n";
        }
        return texto;
    }
}
