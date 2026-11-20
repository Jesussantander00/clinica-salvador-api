package co.clinicasalvador.citas.service;

import co.clinicasalvador.citas.dto.PacienteDtos.PacienteRequest;
import co.clinicasalvador.citas.dto.PacienteDtos.PacienteResponse;

import java.util.List;

public interface PacienteService {

    List<PacienteResponse> listar();

    PacienteResponse buscar(Long id);

    PacienteResponse buscarPorDocumento(String documento);

    PacienteResponse crear(PacienteRequest solicitud);

    PacienteResponse actualizar(Long id, PacienteRequest solicitud);

    void eliminar(Long id);
}
