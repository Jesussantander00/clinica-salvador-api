package co.clinicasalvador.citas.dto;

import co.clinicasalvador.citas.domain.EstadoCita;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

/** Contratos JSON de citas. */
public final class CitaDtos {

    private CitaDtos() {
    }

    public record CitaRequest(
            @NotNull(message = "es obligatorio") Long pacienteId,
            @NotNull(message = "es obligatorio") Long medicoId,
            @NotNull(message = "es obligatoria (formato 2026-12-01T09:00:00)") LocalDateTime fechaHora,
            @NotBlank(message = "es obligatorio") @Size(max = 255, message = "máximo 255 caracteres") String motivo) {
    }

    public record PacienteResumen(Long id, String nombreCompleto, String documento) {
    }

    public record MedicoResumen(Long id, String nombreCompleto, String especialidad) {
    }

    public record CitaResponse(
            Long id,
            PacienteResumen paciente,
            MedicoResumen medico,
            LocalDateTime fechaHora,
            String motivo,
            EstadoCita estado,
            LocalDateTime creadaEn) {
    }
}
