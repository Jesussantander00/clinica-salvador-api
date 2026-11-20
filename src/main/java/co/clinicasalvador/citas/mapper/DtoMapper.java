package co.clinicasalvador.citas.mapper;

import co.clinicasalvador.citas.domain.Cita;
import co.clinicasalvador.citas.domain.Especialidad;
import co.clinicasalvador.citas.domain.Medico;
import co.clinicasalvador.citas.domain.Paciente;
import co.clinicasalvador.citas.dto.CitaDtos.CitaResponse;
import co.clinicasalvador.citas.dto.CitaDtos.MedicoResumen;
import co.clinicasalvador.citas.dto.CitaDtos.PacienteResumen;
import co.clinicasalvador.citas.dto.EspecialidadDtos.EspecialidadResponse;
import co.clinicasalvador.citas.dto.MedicoDtos.MedicoResponse;
import co.clinicasalvador.citas.dto.PacienteDtos.PacienteResponse;

/**
 * Convierte entidades en DTO de respuesta.
 *
 * <p>Las entidades nunca salen de la capa de servicio: así el contrato JSON
 * puede cambiar sin tocar el modelo de base de datos, y viceversa.</p>
 */
public final class DtoMapper {

    private DtoMapper() {
    }

    public static EspecialidadResponse aRespuesta(Especialidad especialidad) {
        return new EspecialidadResponse(especialidad.getId(), especialidad.getNombre());
    }

    public static MedicoResponse aRespuesta(Medico medico) {
        return new MedicoResponse(medico.getId(), medico.getNombres(), medico.getApellidos(),
                medico.getRegistroProfesional(), aRespuesta(medico.getEspecialidad()),
                medico.getHoraInicio(), medico.getHoraFin(), medico.isActivo());
    }

    public static PacienteResponse aRespuesta(Paciente paciente) {
        return new PacienteResponse(paciente.getId(), paciente.getTipoDocumento(), paciente.getDocumento(),
                paciente.getNombres(), paciente.getApellidos(), paciente.getTelefono(),
                paciente.getCorreo(), paciente.getFechaNacimiento());
    }

    public static CitaResponse aRespuesta(Cita cita) {
        Paciente paciente = cita.getPaciente();
        Medico medico = cita.getMedico();
        return new CitaResponse(
                cita.getId(),
                new PacienteResumen(paciente.getId(), paciente.getNombreCompleto(), paciente.getDocumento()),
                new MedicoResumen(medico.getId(), medico.getNombreCompleto(), medico.getEspecialidad().getNombre()),
                cita.getFechaHora(),
                cita.getMotivo(),
                cita.getEstado(),
                cita.getCreadaEn());
    }
}
