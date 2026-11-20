package co.clinicasalvador.citas.domain;

import co.clinicasalvador.citas.exception.ReglaNegocioException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.time.LocalTime;

import static co.clinicasalvador.citas.support.DatosPrueba.MIERCOLES;
import static co.clinicasalvador.citas.support.DatosPrueba.SABADO;
import static co.clinicasalvador.citas.support.DatosPrueba.medico;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("Médico y Agenda: horario de atención")
class MedicoYAgendaTest {

    @ParameterizedTest(name = "a las {0} atiende = {1}")
    @CsvSource({"08:00, true", "11:30, true", "07:30, false", "11:45, false", "12:00, false"})
    @DisplayName("la cita debe empezar y terminar dentro del horario 08:00-12:00")
    void atiendeSoloDentroDeSuHorario(LocalTime hora, boolean esperado) {
        assertThat(medico(1L).atiendeA(hora)).isEqualTo(esperado);
    }

    @Test
    @DisplayName("no acepta un horario cuya hora de fin no es posterior al inicio")
    void rechazaHorarioInvertido() {
        Medico medico = medico(1L);

        assertThatThrownBy(() -> medico.cambiarHorario(LocalTime.of(12, 0), LocalTime.of(8, 0)))
                .isInstanceOf(ReglaNegocioException.class);
    }

    @Test
    @DisplayName("la clínica atiende de lunes a viernes")
    void diasDeAtencion() {
        assertThat(Agenda.esDiaDeAtencion(MIERCOLES)).isTrue();
        assertThat(Agenda.esDiaDeAtencion(SABADO)).isFalse();
        assertThat(Agenda.esDiaDeAtencion(SABADO.plusDays(1))).isFalse();
    }

    @ParameterizedTest(name = "{0} es inicio de bloque = {1}")
    @CsvSource({"08:00, true", "08:30, true", "08:15, false", "09:00:30, false"})
    @DisplayName("las citas empiezan en bloques exactos de 30 minutos")
    void bloquesDeTreintaMinutos(LocalTime hora, boolean esperado) {
        assertThat(Agenda.esInicioDeBloque(hora)).isEqualTo(esperado);
    }
}
