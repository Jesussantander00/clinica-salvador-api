package co.clinicasalvador.citas.service;

import co.clinicasalvador.citas.domain.EstadoCita;
import co.clinicasalvador.citas.dto.CitaDtos.CitaRequest;
import co.clinicasalvador.citas.dto.CitaDtos.CitaResponse;

import java.time.LocalDate;
import java.util.List;

public interface CitaService {

    List<CitaResponse> buscar(Long medicoId, Long pacienteId, LocalDate fecha, EstadoCita estado);

    CitaResponse buscarPorId(Long id);

    /** Agenda una cita validando todas las reglas de negocio. */
    CitaResponse agendar(CitaRequest solicitud);

    CitaResponse cancelar(Long id);

    CitaResponse marcarAtendida(Long id);
}
