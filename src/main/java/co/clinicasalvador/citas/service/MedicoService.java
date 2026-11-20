package co.clinicasalvador.citas.service;

import co.clinicasalvador.citas.dto.MedicoDtos.DisponibilidadResponse;
import co.clinicasalvador.citas.dto.MedicoDtos.MedicoRequest;
import co.clinicasalvador.citas.dto.MedicoDtos.MedicoResponse;

import java.time.LocalDate;
import java.util.List;

public interface MedicoService {

    List<MedicoResponse> listar(Long especialidadId);

    MedicoResponse buscar(Long id);

    MedicoResponse crear(MedicoRequest solicitud);

    MedicoResponse actualizar(Long id, MedicoRequest solicitud);

    MedicoResponse cambiarEstado(Long id, boolean activo);

    /** Horarios en los que el médico puede recibir una cita nueva en la fecha dada. */
    DisponibilidadResponse disponibilidad(Long id, LocalDate fecha);
}
