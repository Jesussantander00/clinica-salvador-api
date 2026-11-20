package co.clinicasalvador.citas.exception;

/**
 * La operación choca con datos que ya existen (documento repetido, horario
 * ocupado por otra cita…). Se responde con HTTP 409.
 */
public class ConflictoException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public ConflictoException(String mensaje) {
        super(mensaje);
    }
}
