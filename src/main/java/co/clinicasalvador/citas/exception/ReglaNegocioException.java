package co.clinicasalvador.citas.exception;

/**
 * Una operación incumple una regla de negocio (fecha pasada, fuera del horario
 * del médico, cancelar una cita ya atendida…). Se responde con HTTP 422.
 */
public class ReglaNegocioException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public ReglaNegocioException(String mensaje) {
        super(mensaje);
    }
}
