package co.clinicasalvador.citas.service.impl;

import co.clinicasalvador.citas.domain.Paciente;
import co.clinicasalvador.citas.domain.TipoDocumento;
import co.clinicasalvador.citas.dto.PacienteDtos.PacienteRequest;
import co.clinicasalvador.citas.dto.PacienteDtos.PacienteResponse;
import co.clinicasalvador.citas.exception.ConflictoException;
import co.clinicasalvador.citas.exception.RecursoNoEncontradoException;
import co.clinicasalvador.citas.repository.CitaRepository;
import co.clinicasalvador.citas.repository.PacienteRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;

import static co.clinicasalvador.citas.support.DatosPrueba.conId;
import static co.clinicasalvador.citas.support.DatosPrueba.paciente;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("PacienteServiceImpl (RN-05)")
class PacienteServiceImplTest {

    @Mock
    private PacienteRepository pacientes;
    @Mock
    private CitaRepository citas;
    @InjectMocks
    private PacienteServiceImpl servicio;

    private final PacienteRequest solicitud = new PacienteRequest(TipoDocumento.CC, "1002003004", "Camilo",
            "Torres Ortiz", "3012223344", "camilo@correo.com", LocalDate.of(2000, 1, 15));

    @Test
    @DisplayName("registra un paciente nuevo")
    void registraPaciente() {
        when(pacientes.existsByDocumento("1002003004")).thenReturn(false);
        when(pacientes.save(any(Paciente.class))).thenAnswer(inv -> conId(inv.getArgument(0), 4L));

        PacienteResponse respuesta = servicio.crear(solicitud);

        assertThat(respuesta.id()).isEqualTo(4L);
        assertThat(respuesta.documento()).isEqualTo("1002003004");
    }

    @Test
    @DisplayName("no registra dos pacientes con el mismo documento")
    void documentoDuplicado() {
        when(pacientes.existsByDocumento("1002003004")).thenReturn(true);

        assertThatThrownBy(() -> servicio.crear(solicitud))
                .isInstanceOf(ConflictoException.class)
                .hasMessageContaining("1002003004");
        verify(pacientes, never()).save(any());
    }

    @Test
    @DisplayName("actualiza los datos de contacto sin cambiar el documento")
    void actualizaDatos() {
        when(pacientes.findById(1L)).thenReturn(Optional.of(paciente(1L)));

        PacienteResponse respuesta = servicio.actualizar(1L, solicitud);

        assertThat(respuesta.telefono()).isEqualTo("3012223344");
        assertThat(respuesta.documento()).isEqualTo("1047383921");
    }

    @Test
    @DisplayName("no elimina un paciente con citas registradas")
    void noEliminaConCitas() {
        when(pacientes.findById(1L)).thenReturn(Optional.of(paciente(1L)));
        when(citas.existsByPacienteId(1L)).thenReturn(true);

        assertThatThrownBy(() -> servicio.eliminar(1L)).isInstanceOf(ConflictoException.class);
        verify(pacientes, never()).delete(any());
    }

    @Test
    @DisplayName("elimina un paciente sin citas")
    void eliminaSinCitas() {
        Paciente paciente = paciente(1L);
        when(pacientes.findById(1L)).thenReturn(Optional.of(paciente));
        when(citas.existsByPacienteId(1L)).thenReturn(false);

        servicio.eliminar(1L);

        verify(pacientes).delete(paciente);
    }

    @Test
    @DisplayName("responde 404 al buscar un documento inexistente")
    void documentoInexistente() {
        when(pacientes.findByDocumento("000")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> servicio.buscarPorDocumento("000"))
                .isInstanceOf(RecursoNoEncontradoException.class);
    }
}
