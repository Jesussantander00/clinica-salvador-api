package co.clinicasalvador.citas.service.impl;

import co.clinicasalvador.citas.domain.Agenda;
import co.clinicasalvador.citas.domain.Cita;
import co.clinicasalvador.citas.domain.Especialidad;
import co.clinicasalvador.citas.domain.EstadoCita;
import co.clinicasalvador.citas.domain.Medico;
import co.clinicasalvador.citas.dto.MedicoDtos.DisponibilidadResponse;
import co.clinicasalvador.citas.dto.MedicoDtos.MedicoRequest;
import co.clinicasalvador.citas.dto.MedicoDtos.MedicoResponse;
import co.clinicasalvador.citas.exception.ConflictoException;
import co.clinicasalvador.citas.exception.RecursoNoEncontradoException;
import co.clinicasalvador.citas.mapper.DtoMapper;
import co.clinicasalvador.citas.repository.CitaRepository;
import co.clinicasalvador.citas.repository.EspecialidadRepository;
import co.clinicasalvador.citas.repository.MedicoRepository;
import co.clinicasalvador.citas.service.MedicoService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class MedicoServiceImpl implements MedicoService {

    private final MedicoRepository medicos;
    private final EspecialidadRepository especialidades;
    private final CitaRepository citas;
    private final Clock reloj;

    public MedicoServiceImpl(MedicoRepository medicos, EspecialidadRepository especialidades,
                             CitaRepository citas, Clock reloj) {
        this.medicos = medicos;
        this.especialidades = especialidades;
        this.citas = citas;
        this.reloj = reloj;
    }

    @Override
    public List<MedicoResponse> listar(Long especialidadId) {
        List<Medico> resultado = especialidadId == null
                ? medicos.findAllByOrderByApellidosAsc()
                : medicos.findByEspecialidadIdOrderByApellidosAsc(especialidadId);
        return resultado.stream().map(DtoMapper::aRespuesta).toList();
    }

    @Override
    public MedicoResponse buscar(Long id) {
        return DtoMapper.aRespuesta(obtener(id));
    }

    @Override
    @Transactional
    public MedicoResponse crear(MedicoRequest solicitud) {
        if (medicos.existsByRegistroProfesionalIgnoreCase(solicitud.registroProfesional())) {
            throw new ConflictoException("Ya existe un médico con el registro " + solicitud.registroProfesional() + ".");
        }
        Medico medico = new Medico(solicitud.nombres(), solicitud.apellidos(), solicitud.registroProfesional(),
                obtenerEspecialidad(solicitud.especialidadId()), solicitud.horaInicio(), solicitud.horaFin());
        return DtoMapper.aRespuesta(medicos.save(medico));
    }

    @Override
    @Transactional
    public MedicoResponse actualizar(Long id, MedicoRequest solicitud) {
        Medico medico = obtener(id);
        boolean cambiaRegistro = !medico.getRegistroProfesional().equalsIgnoreCase(solicitud.registroProfesional());
        if (cambiaRegistro && medicos.existsByRegistroProfesionalIgnoreCase(solicitud.registroProfesional())) {
            throw new ConflictoException("Ya existe un médico con el registro " + solicitud.registroProfesional() + ".");
        }
        medico.setNombres(solicitud.nombres());
        medico.setApellidos(solicitud.apellidos());
        medico.setRegistroProfesional(solicitud.registroProfesional());
        medico.setEspecialidad(obtenerEspecialidad(solicitud.especialidadId()));
        medico.cambiarHorario(solicitud.horaInicio(), solicitud.horaFin());
        return DtoMapper.aRespuesta(medico);
    }

    @Override
    @Transactional
    public MedicoResponse cambiarEstado(Long id, boolean activo) {
        Medico medico = obtener(id);
        medico.setActivo(activo);
        return DtoMapper.aRespuesta(medico);
    }

    @Override
    public DisponibilidadResponse disponibilidad(Long id, LocalDate fecha) {
        Medico medico = obtener(id);
        List<LocalTime> libres = medico.isActivo() && Agenda.esDiaDeAtencion(fecha)
                ? horariosLibres(medico, fecha)
                : List.of();
        return new DisponibilidadResponse(medico.getId(), medico.getNombreCompleto(), fecha, libres);
    }

    private List<LocalTime> horariosLibres(Medico medico, LocalDate fecha) {
        Set<LocalTime> ocupados = citas.findByMedicoIdAndFechaHoraBetweenAndEstadoNot(
                        medico.getId(), fecha.atStartOfDay(), fecha.atTime(LocalTime.MAX), EstadoCita.CANCELADA)
                .stream()
                .map(Cita::getFechaHora)
                .map(LocalDateTime::toLocalTime)
                .collect(Collectors.toSet());
        LocalDateTime ahora = LocalDateTime.now(reloj);

        List<LocalTime> libres = new ArrayList<>();
        for (LocalTime hora = medico.getHoraInicio(); medico.atiendeA(hora); hora = hora.plus(Agenda.DURACION_CITA)) {
            boolean esFutura = fecha.atTime(hora).isAfter(ahora);
            if (esFutura && !ocupados.contains(hora)) {
                libres.add(hora);
            }
        }
        return libres;
    }

    private Medico obtener(Long id) {
        return medicos.findById(id).orElseThrow(() -> new RecursoNoEncontradoException("Médico", id));
    }

    private Especialidad obtenerEspecialidad(Long id) {
        return especialidades.findById(id).orElseThrow(() -> new RecursoNoEncontradoException("Especialidad", id));
    }
}
