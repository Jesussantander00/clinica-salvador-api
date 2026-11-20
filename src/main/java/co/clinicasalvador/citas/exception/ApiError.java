package co.clinicasalvador.citas.exception;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Cuerpo JSON uniforme para todos los errores de la API.
 *
 * @param estado   código HTTP
 * @param error    nombre corto del error (p. ej. "Conflicto")
 * @param mensaje  explicación para quien consume la API
 * @param ruta     endpoint que produjo el error
 * @param detalles errores de validación por campo, si los hay
 */
public record ApiError(
        LocalDateTime fecha,
        int estado,
        String error,
        String mensaje,
        String ruta,
        List<String> detalles) {
}
