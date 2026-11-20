package co.clinicasalvador.citas.service.regla;

/**
 * Una regla de negocio que debe cumplirse para agendar una cita.
 *
 * <p>Cada regla es una clase independiente que Spring descubre y entrega al
 * servicio de citas como una lista. Para agregar una regla nueva basta con
 * crear otra implementación: el servicio no se modifica (principio
 * abierto/cerrado), y cada regla tiene un solo motivo para cambiar
 * (responsabilidad única).</p>
 */
public interface ReglaAgendamiento {

    /**
     * Valida la solicitud.
     *
     * @throws co.clinicasalvador.citas.exception.ReglaNegocioException si la solicitud no es válida
     * @throws co.clinicasalvador.citas.exception.ConflictoException    si el horario ya está ocupado
     */
    void validar(SolicitudCita solicitud);
}
