package co.clinicasalvador.citas.service.impl;

import co.clinicasalvador.citas.domain.Cita;
import co.clinicasalvador.citas.domain.EstadoCita;
import co.clinicasalvador.citas.dto.CitaDtos.CitaRequest;
import co.clinicasalvador.citas.dto.CitaDtos.CitaResponse;
import co.clinicasalvador.citas.exception.ConflictoException;
import co.clinicasalvador.citas.exception.RecursoNoEncontradoException;
import co.clinicasalvador.citas.exception.ReglaNegocioException;
import co.clinicasalvador.citas.repository.CitaRepository;
import co.clinicasalvador.citas.repository.MedicoRepository;
import co.clinicasalvador.citas.repository.PacienteRepository;
import co.clinicasalvador.citas.service.regla.ReglaAgendamiento;
import co.clinicasalvador.citas.service.regla.SolicitudCita;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static co.clinicasalvador.citas.support.DatosPrueba.AHORA;
import static co.clinicasalvador.citas.support.DatosPrueba.MIERCOLES;
import static co.clinicasalvador.citas.support.DatosPrueba.cita;
import static co.clinicasalvador.citas.support.DatosPrueba.conId;
import static co.clinicasalvador.citas.support.DatosPrueba.medico;
import static co.clinicasalvador.citas.support.DatosPrueba.paciente;
import static co.clinicasalvador.citas.support.DatosPrueba.relojFijo;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("CitaServiceImpl")
class CitaServiceImplTest {

    @Mock
    private CitaRepository citas;
    @Mock
    private PacienteRepository pacientes;
    @Mock
    private MedicoRepository medicos;
    @Mock
    private ReglaAgendamiento primeraRegla;
    @Mock
    private ReglaAgendamiento segundaRegla;

    private CitaServiceImpl servicio;

    private final LocalDateTime fecha = MIERCOLES.atTime(9, 0);
    private final CitaRequest solicitud = new CitaRequest(1L, 1L, fecha, "  Dolor de cabeza  ");

    @BeforeEach
    void configurar() {
        servicio = new CitaServiceImpl(citas, pacientes, medicos, List.of(primeraRegla, segundaRegla), relojFijo());
    }

    @Test
    @DisplayName("agenda una cita aplicando todas las reglas en orden")
    void agendaAplicandoReglas() {
        when(pacientes.findById(1L)).thenReturn(Optional.of(paciente(1L)));
        when(medicos.findById(1L)).thenReturn(Optional.of(medico(1L)));
        when(citas.saveAndFlush(any(Cita.class))).thenAnswer(inv -> conId(inv.getArgument(0), 10L));

        CitaResponse respuesta = servicio.agendar(solicitud);

        InOrder orden = inOrder(primeraRegla, segundaRegla);
        orden.verify(primeraRegla).validar(any(SolicitudCita.class));
        orden.verify(segundaRegla).validar(any(SolicitudCita.class));

        ArgumentCaptor<Cita> guardada = ArgumentCaptor.forClass(Cita.class);
        verify(citas).saveAndFlush(guardada.capture());
        assertThat(guardada.getValue().getMotivo()).isEqualTo("Dolor de cabeza");
        assertThat(guardada.getValue().getCreadaEn()).isEqualTo(AHORA);

        assertThat(respuesta.id()).isEqualTo(10L);
        assertThat(respuesta.estado()).isEqualTo(EstadoCita.PROGRAMADA);
        assertThat(respuesta.medico().nombreCompleto()).isEqualTo("Andrés Martínez Pérez");
        assertThat(respuesta.paciente().documento()).isEqualTo("1047383921");
    }

    @Test
    @DisplayName("no guarda la cita si una regla la rechaza")
    void noGuardaSiUnaReglaFalla() {
        when(pacientes.findById(1L)).thenReturn(Optional.of(paciente(1L)));
        when(medicos.findById(1L)).thenReturn(Optional.of(medico(1L)));
        doThrow(new ReglaNegocioException("fuera de horario")).when(primeraRegla).validar(any());

        assertThatThrownBy(() -> servicio.agendar(solicitud)).isInstanceOf(ReglaNegocioException.class);

        verify(segundaRegla, never()).validar(any());
        verify(citas, never()).saveAndFlush(any());
    }

    @Test
    @DisplayName("responde 404 si el paciente no existe")
    void pacienteInexistente() {
        when(pacientes.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> servicio.agendar(solicitud))
                .isInstanceOf(RecursoNoEncontradoException.class)
                .hasMessageContaining("Paciente");
    }

    @Test
    @DisplayName("responde 404 si el médico no existe")
    void medicoInexistente() {
        when(pacientes.findById(1L)).thenReturn(Optional.of(paciente(1L)));
        when(medicos.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> servicio.agendar(solicitud))
                .isInstanceOf(RecursoNoEncontradoException.class)
                .hasMessageContaining("Médico");
    }

    @Test
    @DisplayName("convierte la violación del índice único en un conflicto (reserva simultánea)")
    void reservaSimultanea() {
        when(pacientes.findById(1L)).thenReturn(Optional.of(paciente(1L)));
        when(medicos.findById(1L)).thenReturn(Optional.of(medico(1L)));
        when(citas.saveAndFlush(any())).thenThrow(new DataIntegrityViolationException("ux_cita_medico_horario"));

        assertThatThrownBy(() -> servicio.agendar(solicitud)).isInstanceOf(ConflictoException.class);
    }

    @Test
    @DisplayName("cancela una cita futura usando la hora del reloj")
    void cancelaCita() {
        when(citas.findById(5L)).thenReturn(Optional.of(cita(5L, fecha)));

        CitaResponse respuesta = servicio.cancelar(5L);

        assertThat(respuesta.estado()).isEqualTo(EstadoCita.CANCELADA);
    }

    @Test
    @DisplayName("no cancela una cita que ya pasó")
    void noCancelaPasada() {
        when(citas.findById(5L)).thenReturn(Optional.of(cita(5L, AHORA.minusHours(2))));

        assertThatThrownBy(() -> servicio.cancelar(5L)).isInstanceOf(ReglaNegocioException.class);
    }

    @Test
    @DisplayName("marca atendida una cita cuya hora llegó")
    void marcaAtendida() {
        when(citas.findById(6L)).thenReturn(Optional.of(cita(6L, AHORA.minusMinutes(10))));

        assertThat(servicio.marcarAtendida(6L).estado()).isEqualTo(EstadoCita.ATENDIDA);
    }

    @Test
    @DisplayName("responde 404 al consultar una cita inexistente")
    void citaInexistente() {
        when(citas.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> servicio.buscarPorId(99L))
                .isInstanceOf(RecursoNoEncontradoException.class)
                .hasMessage("Cita con id 99 no existe.");
    }
}
