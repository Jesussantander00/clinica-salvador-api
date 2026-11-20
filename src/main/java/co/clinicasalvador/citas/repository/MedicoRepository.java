package co.clinicasalvador.citas.repository;

import co.clinicasalvador.citas.domain.Medico;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MedicoRepository extends JpaRepository<Medico, Long> {

    @EntityGraph(attributePaths = "especialidad")
    List<Medico> findAllByOrderByApellidosAsc();

    @EntityGraph(attributePaths = "especialidad")
    List<Medico> findByEspecialidadIdOrderByApellidosAsc(Long especialidadId);

    boolean existsByRegistroProfesionalIgnoreCase(String registroProfesional);
}
