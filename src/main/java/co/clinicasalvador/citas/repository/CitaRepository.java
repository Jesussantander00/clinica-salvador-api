package co.clinicasalvador.citas.repository;

import co.clinicasalvador.citas.domain.Cita;
import co.clinicasalvador.citas.domain.EstadoCita;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.time.LocalDateTime;
import java.util.List;

public interface CitaRepository extends JpaRepository<Cita, Long>, JpaSpecificationExecutor<Cita> {

    boolean existsByMedicoIdAndFechaHoraAndEstadoNot(Long medicoId, LocalDateTime fechaHora, EstadoCita estado);

    boolean existsByPacienteIdAndFechaHoraAndEstadoNot(Long pacienteId, LocalDateTime fechaHora, EstadoCita estado);

    boolean existsByPacienteId(Long pacienteId);

    List<Cita> findByMedicoIdAndFechaHoraBetweenAndEstadoNot(Long medicoId, LocalDateTime desde,
                                                             LocalDateTime hasta, EstadoCita estado);
}
