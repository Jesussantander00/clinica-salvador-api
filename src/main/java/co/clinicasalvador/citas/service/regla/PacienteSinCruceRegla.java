package co.clinicasalvador.citas.service.regla;

import co.clinicasalvador.citas.domain.EstadoCita;
import co.clinicasalvador.citas.exception.ConflictoException;
import co.clinicasalvador.citas.repository.CitaRepository;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/** RN-02: un paciente no puede tener dos citas activas a la misma hora. */
@Component
@Order(5)
public class PacienteSinCruceRegla implements ReglaAgendamiento {

    private final CitaRepository citas;

    public PacienteSinCruceRegla(CitaRepository citas) {
        this.citas = citas;
    }

    @Override
    public void validar(SolicitudCita solicitud) {
        boolean ocupado = citas.existsByPacienteIdAndFechaHoraAndEstadoNot(
                solicitud.paciente().getId(), solicitud.fechaHora(), EstadoCita.CANCELADA);
        if (ocupado) {
            throw new ConflictoException("El paciente ya tiene una cita el " + solicitud.fechaHora() + ".");
        }
    }
}
