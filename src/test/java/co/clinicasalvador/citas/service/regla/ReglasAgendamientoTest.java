package co.clinicasalvador.citas.service.regla;

import co.clinicasalvador.citas.domain.EstadoCita;
import co.clinicasalvador.citas.domain.Medico;
import co.clinicasalvador.citas.exception.ConflictoException;
import co.clinicasalvador.citas.exception.ReglaNegocioException;
import co.clinicasalvador.citas.repository.CitaRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.time.LocalTime;

import static co.clinicasalvador.citas.support.DatosPrueba.AHORA;
import static co.clinicasalvador.citas.support.DatosPrueba.MIERCOLES;
import static co.clinicasalvador.citas.support.DatosPrueba.SABADO;
import static co.clinicasalvador.citas.support.DatosPrueba.medico;
import static co.clinicasalvador.citas.support.DatosPrueba.paciente;
import static co.clinicasalvador.citas.support.DatosPrueba.relojFijo;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@DisplayName("Reglas de agendamiento")
class ReglasAgendamientoTest {

    private static SolicitudCita solicitud(LocalDateTime fechaHora) {
        return new SolicitudCita(paciente(1L), medico(1L), fechaHora);
    }

    @Nested
    @DisplayName("MedicoActivoRegla")
    class MedicoActivo {

        private final MedicoActivoRegla regla = new MedicoActivoRegla();

        @Test
        @DisplayName("rechaza un médico inactivo")
        void rechazaInactivo() {
            Medico inactivo = medico(1L);
            inactivo.setActivo(false);

            assertThatThrownBy(() -> regla.validar(new SolicitudCita(paciente(1L), inactivo, AHORA.plusDays(1))))
                    .isInstanceOf(ReglaNegocioException.class);
        }

        @Test
        @DisplayName("acepta un médico activo")
        void aceptaActivo() {
            assertThatCode(() -> regla.validar(solicitud(AHORA.plusDays(1)))).doesNotThrowAnyException();
        }
    }

    @Nested
    @DisplayName("FechaFuturaRegla (RN-03)")
    class FechaFutura {

        private final FechaFuturaRegla regla = new FechaFuturaRegla(relojFijo());

        @Test
        @DisplayName("rechaza una fecha pasada")
        void rechazaPasada() {
            assertThatThrownBy(() -> regla.validar(solicitud(AHORA.minusMinutes(30))))
                    .isInstanceOf(ReglaNegocioException.class);
        }

        @Test
        @DisplayName("rechaza la hora actual exacta")
        void rechazaAhora() {
            assertThatThrownBy(() -> regla.validar(solicitud(AHORA)))
                    .isInstanceOf(ReglaNegocioException.class);
        }

        @Test
        @DisplayName("acepta una fecha futura")
        void aceptaFutura() {
            assertThatCode(() -> regla.validar(solicitud(AHORA.plusMinutes(30)))).doesNotThrowAnyException();
        }
    }

    @Nested
    @DisplayName("HorarioAtencionRegla (RN-03 y RN-04)")
    class HorarioAtencion {

        private final HorarioAtencionRegla regla = new HorarioAtencionRegla();

        @Test
        @DisplayName("rechaza un sábado")
        void rechazaFinDeSemana() {
            assertThatThrownBy(() -> regla.validar(solicitud(SABADO.atTime(9, 0))))
                    .isInstanceOf(ReglaNegocioException.class)
                    .hasMessageContaining("lunes a viernes");
        }

        @Test
        @DisplayName("rechaza una hora que no es inicio de bloque")
        void rechazaHoraFueraDeBloque() {
            assertThatThrownBy(() -> regla.validar(solicitud(MIERCOLES.atTime(9, 10))))
                    .isInstanceOf(ReglaNegocioException.class)
                    .hasMessageContaining("bloques");
        }

        @Test
        @DisplayName("rechaza una hora fuera del horario del médico")
        void rechazaFueraDelHorario() {
            assertThatThrownBy(() -> regla.validar(solicitud(MIERCOLES.atTime(14, 0))))
                    .isInstanceOf(ReglaNegocioException.class)
                    .hasMessageContaining("fuera de su horario");
        }

        @Test
        @DisplayName("acepta el último bloque del horario")
        void aceptaUltimoBloque() {
            assertThatCode(() -> regla.validar(solicitud(MIERCOLES.atTime(LocalTime.of(11, 30)))))
                    .doesNotThrowAnyException();
        }
    }

    @Nested
    @ExtendWith(MockitoExtension.class)
    @DisplayName("MedicoSinCruceRegla (RN-01) y PacienteSinCruceRegla (RN-02)")
    class SinCruces {

        @Mock
        private CitaRepository citas;

        @InjectMocks
        private MedicoSinCruceRegla medicoSinCruce;

        @InjectMocks
        private PacienteSinCruceRegla pacienteSinCruce;

        private final LocalDateTime fecha = MIERCOLES.atTime(9, 0);

        @Test
        @DisplayName("rechaza si el médico ya tiene una cita activa a esa hora")
        void medicoOcupado() {
            when(citas.existsByMedicoIdAndFechaHoraAndEstadoNot(1L, fecha, EstadoCita.CANCELADA)).thenReturn(true);

            assertThatThrownBy(() -> medicoSinCruce.validar(solicitud(fecha)))
                    .isInstanceOf(ConflictoException.class);
        }

        @Test
        @DisplayName("acepta si el médico está libre")
        void medicoLibre() {
            when(citas.existsByMedicoIdAndFechaHoraAndEstadoNot(1L, fecha, EstadoCita.CANCELADA)).thenReturn(false);

            assertThatCode(() -> medicoSinCruce.validar(solicitud(fecha))).doesNotThrowAnyException();
        }

        @Test
        @DisplayName("rechaza si el paciente ya tiene otra cita a esa hora")
        void pacienteOcupado() {
            when(citas.existsByPacienteIdAndFechaHoraAndEstadoNot(1L, fecha, EstadoCita.CANCELADA)).thenReturn(true);

            assertThatThrownBy(() -> pacienteSinCruce.validar(solicitud(fecha)))
                    .isInstanceOf(ConflictoException.class);
        }
    }
}
