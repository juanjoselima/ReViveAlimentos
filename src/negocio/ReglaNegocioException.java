package negocio;

/**
 * Excepción propia del sistema. Se lanza cuando un dato no es válido o cuando
 * una operación incumple una regla de negocio. La interfaz la captura y muestra el mensaje.
 */
public class ReglaNegocioException extends Exception {
    public ReglaNegocioException(String mensaje) {
        super(mensaje);
    }
}
