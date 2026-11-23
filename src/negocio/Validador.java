package negocio;

/**
 * Validaciones de datos de entrada que usan varios módulos.
 */
public class Validador {

    /** Lanza error si el texto está vacío. */
    public static String obligatorio(String texto, String campo) throws ReglaNegocioException {
        if (texto == null || texto.trim().isEmpty()) {
            throw new ReglaNegocioException("El campo '" + campo + "' es obligatorio.");
        }
        return texto.trim();
    }

    public static boolean soloDigitos(String texto) {
        if (texto == null || texto.isEmpty()) return false;
        for (int i = 0; i < texto.length(); i++) {
            if (!Character.isDigit(texto.charAt(i))) return false;
        }
        return true;
    }

    /** RF-01: el RUC debe tener exactamente 13 dígitos. */
    public static boolean rucValido(String ruc) {
        return ruc != null && ruc.length() == 13 && soloDigitos(ruc);
    }

    /**
     * RF-05: cédula ecuatoriana de 10 dígitos con dígito verificador (módulo 10).
     * Los dos primeros dígitos son la provincia (01-24 o 30) y el tercero es menor a 6.
     */
    public static boolean cedulaValida(String cedula) {
        if (cedula == null || cedula.length() != 10 || !soloDigitos(cedula)) return false;
        int provincia = Integer.parseInt(cedula.substring(0, 2));
        if ((provincia < 1 || provincia > 24) && provincia != 30) return false;
        int tercero = cedula.charAt(2) - '0';
        if (tercero >= 6) return false;

        int suma = 0;
        for (int i = 0; i < 9; i++) {
            int digito = cedula.charAt(i) - '0';
            if (i % 2 == 0) {            // posiciones impares (1, 3, 5...) se multiplican por 2
                digito = digito * 2;
                if (digito > 9) digito = digito - 9;
            }
            suma += digito;
        }
        int verificador = (10 - (suma % 10)) % 10;
        return verificador == (cedula.charAt(9) - '0');
    }
}
