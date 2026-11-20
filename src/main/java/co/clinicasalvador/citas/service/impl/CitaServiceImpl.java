package co.clinicasalvador.citas.service.impl;

import co.clinicasalvador.citas.domain.Cita;
import co.clinicasalvador.citas.domain.EstadoCita;
import co.clinicasalvador.citas.domain.Medico;
import co.clinicasalvador.citas.domain.Paciente;
import co.clinicasalvador.citas.dto.CitaDtos.CitaRequest;
import co.clinicasalvador.citas.dto.CitaDtos.CitaResponse;
import co.clinicasalvador.citas.exception.ConflictoException;
import co.clinicasalvador.citas.exception.RecursoNoEncontradoException;
import co.clinicasalvador.citas.mapper.DtoMapper;
import co.clinicasalvador.citas.repository.CitaRepository;
import co.clinicasalvador.citas.repository.MedicoRepository;
import co.clinicasalvador.citas.repository.PacienteRepository;
import co.clinicasalvador.citas.service.CitaService;
import co.clinicasalvador.citas.service.regla.ReglaAgendamiento;
import co.clinicasalvador.citas.service.regla.SolicitudCita;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static co.clinicasalvador.citas.repository.CitaEspecificaciones.conEstado;
import static co.clinicasalvador.citas.repository.CitaEspecificaciones.delMedico;
import static co.clinicasalvador.citas.repository.CitaEspecificaciones.delPaciente;
import static co.clinicasalvador.citas.repository.CitaEspecificaciones.enFecha;

/**
 * Casos de uso de las citas.
 *
 * <p>El servicio no contiene las reglas de agendamiento: recibe la lista de
 * {@link ReglaAgendamiento} por el constructor y las aplica en orden
 * (inversión de dependencias + abierto/cerrado).</p>
 */
@Service
@Transactional(readOnly = true)
public class CitaServiceImpl implements CitaService {

    private final CitaRepository citas;
    private final PacienteRepository pacientes;
    private final MedicoRepository medicos;
    private final List<ReglaAgendamiento> reglas;
    private final Clock reloj;

    public CitaServiceImpl(CitaRepository citas, PacienteRepository pacientes, MedicoRepository medicos,
                           List<ReglaAgendamiento> reglas, Clock reloj) {
        this.citas = citas;
        this.pacientes = pacientes;
        this.medicos = medicos;
        this.reglas = List.copyOf(reglas);
        this.reloj = reloj;
    }

    @Override
    public List<CitaResponse> buscar(Long medicoId, Long pacienteId, LocalDate fecha, EstadoCita estado) {
        Specification<Cita> filtro = Specification.where(delMedico(medicoId))
                .and(delPaciente(pacienteId))
                .and(enFecha(fecha))
                .and(conEstado(estado));
        return citas.findAll(filtro, Sort.by("fechaHora")).stream().map(DtoMapper::aRespuesta).toList();
    }

    @Override
    public CitaResponse buscarPorId(Long id) {
        return DtoMapper.aRespuesta(obtener(id));
    }

    @Override
    @Transactional
    public CitaResponse agendar(CitaRequest solicitud) {
        Paciente paciente = pacientes.findById(solicitud.pacienteId())
                .orElseThrow(() -> new RecursoNoEncontradoException("Paciente", solicitud.pacienteId()));
        Medico medico = medicos.findById(solicitud.medicoId())
                .orElseThrow(() -> new RecursoNoEncontradoException("Médico", solicitud.medicoId()));

        SolicitudCita solicitudCita = new SolicitudCita(paciente, medico, solicitud.fechaHora());
        reglas.forEach(regla -> regla.validar(solicitudCita));

        Cita cita = new Cita(paciente, medico, solicitud.fechaHora(), solicitud.motivo().trim(), ahora());
        try {
            return DtoMapper.aRespuesta(citas.saveAndFlush(cita));
        } catch (DataIntegrityViolationException carreraDeReservas) {
            // Otro usuario tomó el mismo horario entre la validación y el guardado:
            // el índice único de la base de datos lo impide y lo informamos como conflicto.
            throw new ConflictoException("El horario " + solicitud.fechaHora() + " acaba de ser reservado.");
        }
    }

    @Override
    @Transactional
    public CitaResponse cancelar(Long id) {
        Cita cita = obtener(id);
        cita.cancelar(ahora());
        return DtoMapper.aRespuesta(cita);
    }

    @Override
    @Transactional
    public CitaResponse marcarAtendida(Long id) {
        Cita cita = obtener(id);
        cita.marcarAtendida(ahora());
        return DtoMapper.aRespuesta(cita);
    }

    private Cita obtener(Long id) {
        return citas.findById(id).orElseThrow(() -> new RecursoNoEncontradoException("Cita", id));
    }

    private LocalDateTime ahora() {
        return LocalDateTime.now(reloj);
    }
}
