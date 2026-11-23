package modelo;

import java.time.LocalDateTime;

/**
 * Revisión de una oferta con la lista de verificación (RF-06).
 */
public class Evaluacion {
    private Oferta oferta;
    private Responsable evaluador;
    private boolean empaqueIntegro;
    private boolean etiquetaConFecha;
    private boolean temperaturaAdecuada;
    private boolean aprobada;
    private String motivo;
    private LocalDateTime fecha;

    public Evaluacion(Oferta oferta, Responsable evaluador, boolean empaqueIntegro, boolean etiquetaConFecha,
                      boolean temperaturaAdecuada, LocalDateTime fecha) {
        this.oferta = oferta;
        this.evaluador = evaluador;
        this.empaqueIntegro = empaqueIntegro;
        this.etiquetaConFecha = etiquetaConFecha;
        this.temperaturaAdecuada = temperaturaAdecuada;
        this.fecha = fecha;
        calcularResultado();
    }

    /** Aplica los criterios obligatorios y arma el motivo si alguno falla. */
    private void calcularResultado() {
        motivo = "";
        if (!empaqueIntegro) motivo += "Empaque dañado. ";
        if (!etiquetaConFecha) motivo += "Sin etiqueta con fecha de caducidad. ";
        if (oferta.getCategoria().isPerecible() && !temperaturaAdecuada) motivo += "Temperatura inadecuada. ";
        motivo = motivo.trim();
        aprobada = motivo.isEmpty();
    }

    public Oferta getOferta() { return oferta; }
    public Responsable getEvaluador() { return evaluador; }
    public boolean isEmpaqueIntegro() { return empaqueIntegro; }
    public boolean isEtiquetaConFecha() { return etiquetaConFecha; }
    public boolean isTemperaturaAdecuada() { return temperaturaAdecuada; }
    public boolean isAprobada() { return aprobada; }
    public String getMotivo() { return motivo; }
    public LocalDateTime getFecha() { return fecha; }

    @Override
    public String toString() {
        return "Evaluación de oferta " + oferta.getId() + " por " + evaluador.getNombre() + ": "
                + (aprobada ? "APTA" : "NO APTA - " + motivo);
    }
}
