package co.clinicasalvador.citas.service.regla;

import co.clinicasalvador.citas.domain.Agenda;
import co.clinicasalvador.citas.exception.ReglaNegocioException;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * RN-03 y RN-04: la cita debe caer en un día de atención (lunes a viernes),
 * empezar en un bloque exacto de 30 minutos y terminar dentro del horario del médico.
 */
@Component
@Order(3)
public class HorarioAtencionRegla implements ReglaAgendamiento {

    @Override
    public void validar(SolicitudCita solicitud) {
        LocalDateTime fechaHora = solicitud.fechaHora();
        LocalTime hora = fechaHora.toLocalTime();

        if (!Agenda.esDiaDeAtencion(fechaHora.toLocalDate())) {
            throw new ReglaNegocioException("La clínica solo agenda citas de lunes a viernes.");
        }
        if (!Agenda.esInicioDeBloque(hora)) {
            throw new ReglaNegocioException("Las citas empiezan en bloques de "
                    + Agenda.DURACION_CITA.toMinutes() + " minutos (por ejemplo 08:00 u 08:30).");
        }
        if (!solicitud.medico().atiendeA(hora)) {
            throw new ReglaNegocioException("El médico atiende de " + solicitud.medico().getHoraInicio()
                    + " a " + solicitud.medico().getHoraFin() + "; la cita queda fuera de su horario.");
        }
    }
}
