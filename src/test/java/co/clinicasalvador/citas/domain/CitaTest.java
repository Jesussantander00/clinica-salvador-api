package co.clinicasalvador.citas.domain;

import co.clinicasalvador.citas.exception.ReglaNegocioException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static co.clinicasalvador.citas.support.DatosPrueba.AHORA;
import static co.clinicasalvador.citas.support.DatosPrueba.cita;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("Entidad Cita: ciclo de vida (RN-06 y RN-07)")
class CitaTest {

    private final LocalDateTime manana = AHORA.plusDays(1);

    @Test
    @DisplayName("una cita nueva queda PROGRAMADA")
    void citaNuevaQuedaProgramada() {
        assertThat(cita(1L, manana).getEstado()).isEqualTo(EstadoCita.PROGRAMADA);
    }

    @Test
    @DisplayName("se cancela una cita programada antes de su hora")
    void cancelaCitaProgramadaFutura() {
        Cita cita = cita(1L, manana);

        cita.cancelar(AHORA);

        assertThat(cita.getEstado()).isEqualTo(EstadoCita.CANCELADA);
    }

    @Test
    @DisplayName("no se cancela una cita cuya hora ya pasó")
    void noCancelaCitaPasada() {
        Cita cita = cita(1L, AHORA.minusHours(1));

        assertThatThrownBy(() -> cita.cancelar(AHORA))
                .isInstanceOf(ReglaNegocioException.class)
                .hasMessageContaining("ya pasó");
        assertThat(cita.getEstado()).isEqualTo(EstadoCita.PROGRAMADA);
    }

    @Test
    @DisplayName("no se cancela dos veces la misma cita")
    void noCancelaDosVeces() {
        Cita cita = cita(1L, manana);
        cita.cancelar(AHORA);

        assertThatThrownBy(() -> cita.cancelar(AHORA))
                .isInstanceOf(ReglaNegocioException.class)
                .hasMessageContaining("CANCELADA");
    }

    @Test
    @DisplayName("se marca atendida cuando llegó su hora")
    void marcaAtendidaCuandoLlegaLaHora() {
        Cita cita = cita(1L, AHORA);

        cita.marcarAtendida(AHORA.plusMinutes(5));

        assertThat(cita.getEstado()).isEqualTo(EstadoCita.ATENDIDA);
    }

    @Test
    @DisplayName("no se atiende una cita antes de su hora")
    void noAtiendeAntesDeTiempo() {
        Cita cita = cita(1L, manana);

        assertThatThrownBy(() -> cita.marcarAtendida(AHORA))
                .isInstanceOf(ReglaNegocioException.class);
    }

    @Test
    @DisplayName("una cita cancelada no se puede atender")
    void canceladaNoSeAtiende() {
        Cita cita = cita(1L, manana);
        cita.cancelar(AHORA);

        assertThatThrownBy(() -> cita.marcarAtendida(manana.plusMinutes(1)))
                .isInstanceOf(ReglaNegocioException.class)
                .hasMessageContaining("CANCELADA");
    }
}
