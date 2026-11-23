package modelo;

/**
 * Valores configurables del sistema (RF-17): umbrales de prioridad, pesos del puntaje,
 * tolerancia de diferencias y minutos de expiración de propuestas.
 */
public class Parametros {
    private double umbralCritica;   // horas
    private double umbralAlta;
    private double umbralMedia;
    private int pesoZona;
    private int pesoCapacidad;
    private int pesoHorario;
    private int pesoHistorial;
    private double toleranciaPorcentaje;
    private int minutosExpiracion;

    /** Valores iniciales definidos en el primer avance. */
    public Parametros() {
        umbralCritica = 2;
        umbralAlta = 4;
        umbralMedia = 8;
        pesoZona = 40;
        pesoCapacidad = 30;
        pesoHorario = 20;
        pesoHistorial = 10;
        toleranciaPorcentaje = 5;
        minutosExpiracion = 30;
    }

    public double getUmbralCritica() { return umbralCritica; }
    public double getUmbralAlta() { return umbralAlta; }
    public double getUmbralMedia() { return umbralMedia; }
    public void setUmbrales(double critica, double alta, double media) {
        this.umbralCritica = critica;
        this.umbralAlta = alta;
        this.umbralMedia = media;
    }

    public int getPesoZona() { return pesoZona; }
    public int getPesoCapacidad() { return pesoCapacidad; }
    public int getPesoHorario() { return pesoHorario; }
    public int getPesoHistorial() { return pesoHistorial; }
    public void setPesos(int zona, int capacidad, int horario, int historial) {
        this.pesoZona = zona;
        this.pesoCapacidad = capacidad;
        this.pesoHorario = horario;
        this.pesoHistorial = historial;
    }

    public double getToleranciaPorcentaje() { return toleranciaPorcentaje; }
    public void setToleranciaPorcentaje(double toleranciaPorcentaje) { this.toleranciaPorcentaje = toleranciaPorcentaje; }
    public int getMinutosExpiracion() { return minutosExpiracion; }
    public void setMinutosExpiracion(int minutosExpiracion) { this.minutosExpiracion = minutosExpiracion; }

    public String textoUmbrales() { return umbralCritica + " / " + umbralAlta + " / " + umbralMedia + " h"; }
    public String textoPesos() { return pesoZona + " / " + pesoCapacidad + " / " + pesoHorario + " / " + pesoHistorial; }

    @Override
    public String toString() {
        return "Umbrales de prioridad (Crítica/Alta/Media): " + textoUmbrales()
                + "\nPesos del puntaje (Zona/Capacidad/Horario/Historial): " + textoPesos()
                + "\nTolerancia de diferencia: " + toleranciaPorcentaje + " %"
                + "\nExpiración de propuestas: " + minutosExpiracion + " minutos";
    }
}
