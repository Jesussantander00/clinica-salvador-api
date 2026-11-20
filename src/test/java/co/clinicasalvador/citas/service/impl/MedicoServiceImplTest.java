package co.clinicasalvador.citas.service.impl;

import co.clinicasalvador.citas.domain.EstadoCita;
import co.clinicasalvador.citas.domain.Medico;
import co.clinicasalvador.citas.dto.MedicoDtos.DisponibilidadResponse;
import co.clinicasalvador.citas.dto.MedicoDtos.MedicoRequest;
import co.clinicasalvador.citas.exception.ConflictoException;
import co.clinicasalvador.citas.exception.RecursoNoEncontradoException;
import co.clinicasalvador.citas.repository.CitaRepository;
import co.clinicasalvador.citas.repository.EspecialidadRepository;
import co.clinicasalvador.citas.repository.MedicoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import static co.clinicasalvador.citas.support.DatosPrueba.AHORA;
import static co.clinicasalvador.citas.support.DatosPrueba.MIERCOLES;
import static co.clinicasalvador.citas.support.DatosPrueba.SABADO;
import static co.clinicasalvador.citas.support.DatosPrueba.cita;
import static co.clinicasalvador.citas.support.DatosPrueba.medico;
import static co.clinicasalvador.citas.support.DatosPrueba.relojFijo;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("MedicoServiceImpl")
class MedicoServiceImplTest {

    @Mock
    private MedicoRepository medicos;
    @Mock
    private EspecialidadRepository especialidades;
    @Mock
    private CitaRepository citas;

    private MedicoServiceImpl servicio;

    @BeforeEach
    void configurar() {
        servicio = new MedicoServiceImpl(medicos, especialidades, citas, relojFijo());
    }

    @Test
    @DisplayName("la disponibilidad excluye los horarios ya reservados")
    void disponibilidadSinHorariosOcupados() {
        when(medicos.findById(1L)).thenReturn(Optional.of(medico(1L)));
        when(citas.findByMedicoIdAndFechaHoraBetweenAndEstadoNot(eq(1L), any(), any(), eq(EstadoCita.CANCELADA)))
                .thenReturn(List.of(cita(1L, MIERCOLES.atTime(9, 0)), cita(2L, MIERCOLES.atTime(10, 30))));

        DisponibilidadResponse respuesta = servicio.disponibilidad(1L, MIERCOLES);

        // 08:00-12:00 son 8 bloques; dos están ocupados.
        assertThat(respuesta.horariosLibres())
                .hasSize(6)
                .doesNotContain(LocalTime.of(9, 0), LocalTime.of(10, 30))
                .startsWith(LocalTime.of(8, 0))
                .endsWith(LocalTime.of(11, 30));
    }

    @Test
    @DisplayName("hoy solo ofrece horarios posteriores a la hora actual")
    void disponibilidadDeHoySoloFutura() {
        when(medicos.findById(1L)).thenReturn(Optional.of(medico(1L)));
        when(citas.findByMedicoIdAndFechaHoraBetweenAndEstadoNot(eq(1L), any(), any(), any())).thenReturn(List.of());

        DisponibilidadResponse respuesta = servicio.disponibilidad(1L, AHORA.toLocalDate());

        // "Ahora" son las 10:00: quedan 10:30, 11:00 y 11:30.
        assertThat(respuesta.horariosLibres())
                .containsExactly(LocalTime.of(10, 30), LocalTime.of(11, 0), LocalTime.of(11, 30));
    }

    @Test
    @DisplayName("los fines de semana no hay disponibilidad")
    void finDeSemanaSinDisponibilidad() {
        when(medicos.findById(1L)).thenReturn(Optional.of(medico(1L)));

        assertThat(servicio.disponibilidad(1L, SABADO).horariosLibres()).isEmpty();
        verify(citas, never()).findByMedicoIdAndFechaHoraBetweenAndEstadoNot(any(), any(), any(), any());
    }

    @Test
    @DisplayName("un médico inactivo no tiene disponibilidad")
    void inactivoSinDisponibilidad() {
        Medico inactivo = medico(1L);
        inactivo.setActivo(false);
        when(medicos.findById(1L)).thenReturn(Optional.of(inactivo));

        assertThat(servicio.disponibilidad(1L, MIERCOLES).horariosLibres()).isEmpty();
    }

    @Test
    @DisplayName("no registra dos médicos con el mismo registro profesional")
    void registroDuplicado() {
        when(medicos.existsByRegistroProfesionalIgnoreCase("RM-10234")).thenReturn(true);
        MedicoRequest solicitud = new MedicoRequest("Ana", "Gil", "RM-10234", 1L,
                LocalTime.of(8, 0), LocalTime.of(12, 0));

        assertThatThrownBy(() -> servicio.crear(solicitud)).isInstanceOf(ConflictoException.class);
        verify(medicos, never()).save(any());
    }

    @Test
    @DisplayName("responde 404 si la especialidad no existe")
    void especialidadInexistente() {
        when(medicos.existsByRegistroProfesionalIgnoreCase("RM-99999")).thenReturn(false);
        when(especialidades.findById(7L)).thenReturn(Optional.empty());
        MedicoRequest solicitud = new MedicoRequest("Ana", "Gil", "RM-99999", 7L,
                LocalTime.of(8, 0), LocalTime.of(12, 0));

        assertThatThrownBy(() -> servicio.crear(solicitud)).isInstanceOf(RecursoNoEncontradoException.class);
    }
}
