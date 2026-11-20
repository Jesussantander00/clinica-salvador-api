package co.clinicasalvador.citas.dto;

import co.clinicasalvador.citas.domain.TipoDocumento;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

/** Contratos JSON de pacientes. */
public final class PacienteDtos {

    private PacienteDtos() {
    }

    public record PacienteRequest(
            @NotNull(message = "es obligatorio (CC, TI, CE o PA)") TipoDocumento tipoDocumento,
            @NotBlank(message = "es obligatorio")
            @Pattern(regexp = "[0-9A-Za-z]{5,20}", message = "debe tener entre 5 y 20 letras o números")
            String documento,
            @NotBlank(message = "es obligatorio") @Size(max = 80) String nombres,
            @NotBlank(message = "es obligatorio") @Size(max = 80) String apellidos,
            @NotBlank(message = "es obligatorio")
            @Pattern(regexp = "[0-9+ ]{7,20}", message = "debe tener entre 7 y 20 dígitos")
            String telefono,
            @NotBlank(message = "es obligatorio") @Email(message = "no es un correo válido") @Size(max = 120)
            String correo,
            @NotNull(message = "es obligatoria") @Past(message = "debe ser una fecha pasada")
            LocalDate fechaNacimiento) {
    }

    public record PacienteResponse(
            Long id,
            TipoDocumento tipoDocumento,
            String documento,
            String nombres,
            String apellidos,
            String telefono,
            String correo,
            LocalDate fechaNacimiento) {
    }
}
