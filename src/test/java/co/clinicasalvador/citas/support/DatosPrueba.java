package co.clinicasalvador.citas.support;

import co.clinicasalvador.citas.domain.Cita;
import co.clinicasalvador.citas.domain.Especialidad;
import co.clinicasalvador.citas.domain.Medico;
import co.clinicasalvador.citas.domain.Paciente;
import co.clinicasalvador.citas.domain.TipoDocumento;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;

/** Objetos de prueba reutilizables (patrón Object Mother). */
public final class DatosPrueba {

    public static final ZoneId BOGOTA = ZoneId.of("America/Bogota");

    /** "Ahora" fijo para todas las pruebas: martes 1 de diciembre de 2026, 10:00. */
    public static final LocalDateTime AHORA = LocalDateTime.of(2026, 12, 1, 10, 0);

    /** Miércoles siguiente al "ahora" de las pruebas. */
    public static final LocalDate MIERCOLES = LocalDate.of(2026, 12, 2);

    /** Sábado de la misma semana. */
    public static final LocalDate SABADO = LocalDate.of(2026, 12, 5);

    private DatosPrueba() {
    }

    public static Clock relojFijo() {
        return Clock.fixed(AHORA.atZone(BOGOTA).toInstant(), BOGOTA);
    }

    public static Especialidad especialidad(long id, String nombre) {
        return conId(new Especialidad(nombre), id);
    }

    /** Médico general que atiende de 08:00 a 12:00. */
    public static Medico medico(long id) {
        Medico medico = new Medico("Andrés", "Martínez Pérez", "RM-10234",
                especialidad(1L, "Medicina general"), LocalTime.of(8, 0), LocalTime.of(12, 0));
        return conId(medico, id);
    }

    public static Paciente paciente(long id) {
        Paciente paciente = new Paciente(TipoDocumento.CC, "1047383921", "Valentina", "Ruiz Mendoza",
                "3004567812", "valentina.ruiz@correo.com", LocalDate.of(1998, 4, 17));
        return conId(paciente, id);
    }

    public static Cita cita(long id, LocalDateTime fechaHora) {
        Cita cita = new Cita(paciente(1L), medico(1L), fechaHora, "Control general", AHORA.minusDays(1));
        return conId(cita, id);
    }

    public static <T> T conId(T entidad, long id) {
        ReflectionTestUtils.setField(entidad, "id", id);
        return entidad;
    }
}
