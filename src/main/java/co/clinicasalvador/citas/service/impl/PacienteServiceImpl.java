package co.clinicasalvador.citas.service.impl;

import co.clinicasalvador.citas.domain.Paciente;
import co.clinicasalvador.citas.dto.PacienteDtos.PacienteRequest;
import co.clinicasalvador.citas.dto.PacienteDtos.PacienteResponse;
import co.clinicasalvador.citas.exception.ConflictoException;
import co.clinicasalvador.citas.exception.RecursoNoEncontradoException;
import co.clinicasalvador.citas.mapper.DtoMapper;
import co.clinicasalvador.citas.repository.CitaRepository;
import co.clinicasalvador.citas.repository.PacienteRepository;
import co.clinicasalvador.citas.service.PacienteService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class PacienteServiceImpl implements PacienteService {

    private final PacienteRepository pacientes;
    private final CitaRepository citas;

    public PacienteServiceImpl(PacienteRepository pacientes, CitaRepository citas) {
        this.pacientes = pacientes;
        this.citas = citas;
    }

    @Override
    public List<PacienteResponse> listar() {
        return pacientes.findAllByOrderByApellidosAsc().stream().map(DtoMapper::aRespuesta).toList();
    }

    @Override
    public PacienteResponse buscar(Long id) {
        return DtoMapper.aRespuesta(obtener(id));
    }

    @Override
    public PacienteResponse buscarPorDocumento(String documento) {
        return pacientes.findByDocumento(documento)
                .map(DtoMapper::aRespuesta)
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "No existe un paciente con documento " + documento + "."));
    }

    @Override
    @Transactional
    public PacienteResponse crear(PacienteRequest solicitud) {
        if (pacientes.existsByDocumento(solicitud.documento())) {
            throw new ConflictoException("Ya existe un paciente con documento " + solicitud.documento() + ".");
        }
        Paciente paciente = new Paciente(solicitud.tipoDocumento(), solicitud.documento(), solicitud.nombres(),
                solicitud.apellidos(), solicitud.telefono(), solicitud.correo(), solicitud.fechaNacimiento());
        return DtoMapper.aRespuesta(pacientes.save(paciente));
    }

    @Override
    @Transactional
    public PacienteResponse actualizar(Long id, PacienteRequest solicitud) {
        Paciente paciente = obtener(id);
        paciente.actualizarDatos(solicitud.nombres(), solicitud.apellidos(), solicitud.telefono(),
                solicitud.correo(), solicitud.fechaNacimiento());
        return DtoMapper.aRespuesta(paciente);
    }

    @Override
    @Transactional
    public void eliminar(Long id) {
        Paciente paciente = obtener(id);
        if (citas.existsByPacienteId(id)) {
            throw new ConflictoException("El paciente tiene citas registradas y no se puede eliminar.");
        }
        pacientes.delete(paciente);
    }

    private Paciente obtener(Long id) {
        return pacientes.findById(id).orElseThrow(() -> new RecursoNoEncontradoException("Paciente", id));
    }
}
