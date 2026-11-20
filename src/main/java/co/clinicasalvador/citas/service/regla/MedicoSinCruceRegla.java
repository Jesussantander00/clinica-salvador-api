package co.clinicasalvador.citas.service.regla;

import co.clinicasalvador.citas.domain.EstadoCita;
import co.clinicasalvador.citas.exception.ConflictoException;
import co.clinicasalvador.citas.repository.CitaRepository;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/** RN-01: un médico no puede tener dos citas activas a la misma hora. */
@Component
@Order(4)
public class MedicoSinCruceRegla implements ReglaAgendamiento {

    private final CitaRepository citas;

    public MedicoSinCruceRegla(CitaRepository citas) {
        this.citas = citas;
    }

    @Override
    public void validar(SolicitudCita solicitud) {
        boolean ocupado = citas.existsByMedicoIdAndFechaHoraAndEstadoNot(
                solicitud.medico().getId(), solicitud.fechaHora(), EstadoCita.CANCELADA);
        if (ocupado) {
            throw new ConflictoException("El médico ya tiene una cita el " + solicitud.fechaHora() + ".");
        }
    }
}
