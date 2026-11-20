package co.clinicasalvador.citas.service.regla;

import co.clinicasalvador.citas.exception.ReglaNegocioException;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/** Solo se agenda con médicos activos. */
@Component
@Order(1)
public class MedicoActivoRegla implements ReglaAgendamiento {

    @Override
    public void validar(SolicitudCita solicitud) {
        if (!solicitud.medico().isActivo()) {
            throw new ReglaNegocioException("El médico " + solicitud.medico().getNombreCompleto()
                    + " no está activo y no recibe citas.");
        }
    }
}
