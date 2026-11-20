package co.clinicasalvador.citas.repository;

import co.clinicasalvador.citas.domain.Especialidad;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EspecialidadRepository extends JpaRepository<Especialidad, Long> {

    boolean existsByNombreIgnoreCase(String nombre);
}
