package co.clinicasalvador.citas.service.regla;

import co.clinicasalvador.citas.exception.ReglaNegocioException;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.LocalDateTime;

/** RN-03: las citas solo se agendan en fechas y horas futuras. */
@Component
@Order(2)
public class FechaFuturaRegla implements ReglaAgendamiento {

    private final Clock reloj;

    public FechaFuturaRegla(Clock reloj) {
        this.reloj = reloj;
    }

    @Override
    public void validar(SolicitudCita solicitud) {
        if (!solicitud.fechaHora().isAfter(LocalDateTime.now(reloj))) {
            throw new ReglaNegocioException("La cita debe agendarse en una fecha y hora futuras.");
        }
    }
}
