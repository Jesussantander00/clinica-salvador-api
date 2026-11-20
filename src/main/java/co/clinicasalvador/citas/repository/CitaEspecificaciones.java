package co.clinicasalvador.citas.repository;

import co.clinicasalvador.citas.domain.Cita;
import co.clinicasalvador.citas.domain.EstadoCita;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;

/**
 * Filtros opcionales para la búsqueda de citas.
 *
 * <p>Cada método devuelve {@code null} cuando el filtro no se usa, y Spring Data
 * ignora las especificaciones nulas al combinarlas.</p>
 */
public final class CitaEspecificaciones {

    private CitaEspecificaciones() {
    }

    public static Specification<Cita> delMedico(Long medicoId) {
        return medicoId == null ? null
                : (raiz, consulta, cb) -> cb.equal(raiz.get("medico").get("id"), medicoId);
    }

    public static Specification<Cita> delPaciente(Long pacienteId) {
        return pacienteId == null ? null
                : (raiz, consulta, cb) -> cb.equal(raiz.get("paciente").get("id"), pacienteId);
    }

    public static Specification<Cita> enFecha(LocalDate fecha) {
        return fecha == null ? null
                : (raiz, consulta, cb) -> cb.between(raiz.get("fechaHora"),
                fecha.atStartOfDay(), fecha.plusDays(1).atStartOfDay().minusNanos(1000));
    }

    public static Specification<Cita> conEstado(EstadoCita estado) {
        return estado == null ? null
                : (raiz, consulta, cb) -> cb.equal(raiz.get("estado"), estado);
    }
}
