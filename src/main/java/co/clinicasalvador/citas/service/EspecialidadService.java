package co.clinicasalvador.citas.service;

import co.clinicasalvador.citas.dto.EspecialidadDtos.EspecialidadRequest;
import co.clinicasalvador.citas.dto.EspecialidadDtos.EspecialidadResponse;

import java.util.List;

public interface EspecialidadService {

    List<EspecialidadResponse> listar();

    EspecialidadResponse crear(EspecialidadRequest solicitud);
}
