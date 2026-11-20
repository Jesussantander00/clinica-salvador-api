package co.clinicasalvador.citas.service.regla;

import co.clinicasalvador.citas.domain.Medico;
import co.clinicasalvador.citas.domain.Paciente;

import java.time.LocalDateTime;

/** Datos que cada regla necesita para decidir si una cita se puede agendar. */
public record SolicitudCita(Paciente paciente, Medico medico, LocalDateTime fechaHora) {
}
