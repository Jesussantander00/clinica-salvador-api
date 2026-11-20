package co.clinicasalvador.citas.repository;

import co.clinicasalvador.citas.domain.Paciente;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PacienteRepository extends JpaRepository<Paciente, Long> {

    Optional<Paciente> findByDocumento(String documento);

    boolean existsByDocumento(String documento);

    List<Paciente> findAllByOrderByApellidosAsc();
}
