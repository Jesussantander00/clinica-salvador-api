package co.clinicasalvador.citas.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** Contratos JSON de especialidades. */
public final class EspecialidadDtos {

    private EspecialidadDtos() {
    }

    public record EspecialidadRequest(
            @NotBlank(message = "es obligatorio") @Size(max = 80, message = "máximo 80 caracteres") String nombre) {
    }

    public record EspecialidadResponse(Long id, String nombre) {
    }
}
