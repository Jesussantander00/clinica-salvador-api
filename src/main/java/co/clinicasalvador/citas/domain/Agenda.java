package co.clinicasalvador.citas.domain;

import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Set;

/**
 * Reglas fijas de la agenda de la clínica.
 *
 * <ul>
 *   <li>RN-03: las citas duran 30 minutos y empiezan en bloques exactos (8:00, 8:30…).</li>
 *   <li>RN-04: la clínica atiende de lunes a viernes.</li>
 * </ul>
 */
public final class Agenda {

    public static final Duration DURACION_CITA = Duration.ofMinutes(30);

    private static final Set<DayOfWeek> DIAS_DE_ATENCION = Set.of(
            DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY,
            DayOfWeek.THURSDAY, DayOfWeek.FRIDAY);

    private Agenda() {
        // Clase de constantes y utilidades: no se instancia.
    }

    public static boolean esDiaDeAtencion(LocalDate fecha) {
        return DIAS_DE_ATENCION.contains(fecha.getDayOfWeek());
    }

    /** {@code true} si la hora cae exactamente en el inicio de un bloque (minutos 00 o 30, sin segundos). */
    public static boolean esInicioDeBloque(LocalTime hora) {
        long minutosDelDia = hora.toSecondOfDay() / 60;
        return hora.getSecond() == 0 && hora.getNano() == 0
                && minutosDelDia % DURACION_CITA.toMinutes() == 0;
    }
}
