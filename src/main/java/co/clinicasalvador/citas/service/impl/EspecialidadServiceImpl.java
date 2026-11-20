package co.clinicasalvador.citas.service.impl;

import co.clinicasalvador.citas.domain.Especialidad;
import co.clinicasalvador.citas.dto.EspecialidadDtos.EspecialidadRequest;
import co.clinicasalvador.citas.dto.EspecialidadDtos.EspecialidadResponse;
import co.clinicasalvador.citas.exception.ConflictoException;
import co.clinicasalvador.citas.mapper.DtoMapper;
import co.clinicasalvador.citas.repository.EspecialidadRepository;
import co.clinicasalvador.citas.service.EspecialidadService;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class EspecialidadServiceImpl implements EspecialidadService {

    private final EspecialidadRepository especialidades;

    public EspecialidadServiceImpl(EspecialidadRepository especialidades) {
        this.especialidades = especialidades;
    }

    @Override
    public List<EspecialidadResponse> listar() {
        return especialidades.findAll(Sort.by("nombre")).stream()
                .map(DtoMapper::aRespuesta)
                .toList();
    }

    @Override
    @Transactional
    public EspecialidadResponse crear(EspecialidadRequest solicitud) {
        String nombre = solicitud.nombre().trim();
        if (especialidades.existsByNombreIgnoreCase(nombre)) {
            throw new ConflictoException("Ya existe la especialidad " + nombre + ".");
        }
        return DtoMapper.aRespuesta(especialidades.save(new Especialidad(nombre)));
    }
}
