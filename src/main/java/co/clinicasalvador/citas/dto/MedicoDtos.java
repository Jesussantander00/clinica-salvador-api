package co.clinicasalvador.citas.dto;

import co.clinicasalvador.citas.dto.EspecialidadDtos.EspecialidadResponse;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

/** Contratos JSON de médicos y de su disponibilidad. */
public final class MedicoDtos {

    private MedicoDtos() {
    }

    public record MedicoRequest(
            @NotBlank(message = "es obligatorio") @Size(max = 80) String nombres,
            @NotBlank(message = "es obligatorio") @Size(max = 80) String apellidos,
            @NotBlank(message = "es obligatorio") @Size(max = 30) String registroProfesional,
            @NotNull(message = "es obligatorio") Long especialidadId,
            @NotNull(message = "es obligatorio") LocalTime horaInicio,
            @NotNull(message = "es obligatorio") LocalTime horaFin) {
    }

    public record MedicoResponse(
            Long id,
            String nombres,
            String apellidos,
            String registroProfesional,
            EspecialidadResponse especialidad,
            LocalTime horaInicio,
            LocalTime horaFin,
            boolean activo) {
    }

    /** Horarios libres de un médico en una fecha. */
    public record DisponibilidadResponse(Long medicoId, String medico, LocalDate fecha, List<LocalTime> horariosLibres) {
    }
}
