package co.clinicasalvador.citas.exception;

/** El recurso pedido (paciente, médico, cita…) no existe. Se responde con HTTP 404. */
public class RecursoNoEncontradoException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public RecursoNoEncontradoException(String recurso, Object id) {
        super(recurso + " con id " + id + " no existe.");
    }

    public RecursoNoEncontradoException(String mensaje) {
        super(mensaje);
    }
}
