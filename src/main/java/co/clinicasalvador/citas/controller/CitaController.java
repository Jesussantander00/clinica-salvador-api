package co.clinicasalvador.citas.controller;

import co.clinicasalvador.citas.domain.EstadoCita;
import co.clinicasalvador.citas.dto.CitaDtos.CitaRequest;
import co.clinicasalvador.citas.dto.CitaDtos.CitaResponse;
import co.clinicasalvador.citas.service.CitaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/citas")
@Tag(name = "Citas", description = "Agendamiento, consulta, cancelación y atención de citas")
public class CitaController {

    private final CitaService servicio;

    public CitaController(CitaService servicio) {
        this.servicio = servicio;
    }

    @GetMapping
    @Operation(summary = "Busca citas con filtros opcionales")
    public List<CitaResponse> buscar(@RequestParam(required = false) Long medicoId,
                                     @RequestParam(required = false) Long pacienteId,
                                     @RequestParam(required = false)
                                     @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha,
                                     @RequestParam(required = false) EstadoCita estado) {
        return servicio.buscar(medicoId, pacienteId, fecha, estado);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Consulta una cita")
    public CitaResponse buscarPorId(@PathVariable Long id) {
        return servicio.buscarPorId(id);
    }

    @PostMapping
    @Operation(summary = "Agenda una cita (valida las reglas de negocio RN-01 a RN-04)")
    public ResponseEntity<CitaResponse> agendar(@Valid @RequestBody CitaRequest solicitud) {
        CitaResponse cita = servicio.agendar(solicitud);
        return ResponseEntity.created(ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}").buildAndExpand(cita.id()).toUri()).body(cita);
    }

    @PatchMapping("/{id}/cancelar")
    @Operation(summary = "Cancela una cita programada antes de su hora")
    public CitaResponse cancelar(@PathVariable Long id) {
        return servicio.cancelar(id);
    }

    @PatchMapping("/{id}/atender")
    @Operation(summary = "Marca una cita como atendida")
    public CitaResponse atender(@PathVariable Long id) {
        return servicio.marcarAtendida(id);
    }
}
