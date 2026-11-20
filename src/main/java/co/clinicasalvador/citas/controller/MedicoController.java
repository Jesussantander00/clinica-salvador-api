package co.clinicasalvador.citas.controller;

import co.clinicasalvador.citas.dto.MedicoDtos.DisponibilidadResponse;
import co.clinicasalvador.citas.dto.MedicoDtos.MedicoRequest;
import co.clinicasalvador.citas.dto.MedicoDtos.MedicoResponse;
import co.clinicasalvador.citas.service.MedicoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/medicos")
@Tag(name = "Médicos", description = "Médicos, horarios y disponibilidad")
public class MedicoController {

    private final MedicoService servicio;

    public MedicoController(MedicoService servicio) {
        this.servicio = servicio;
    }

    @GetMapping
    @Operation(summary = "Lista los médicos, opcionalmente filtrados por especialidad")
    public List<MedicoResponse> listar(@RequestParam(required = false) Long especialidadId) {
        return servicio.listar(especialidadId);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Consulta un médico")
    public MedicoResponse buscar(@PathVariable Long id) {
        return servicio.buscar(id);
    }

    @PostMapping
    @Operation(summary = "Registra un médico")
    public ResponseEntity<MedicoResponse> crear(@Valid @RequestBody MedicoRequest solicitud) {
        MedicoResponse creado = servicio.crear(solicitud);
        return ResponseEntity.created(ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}").buildAndExpand(creado.id()).toUri()).body(creado);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualiza los datos y el horario de un médico")
    public MedicoResponse actualizar(@PathVariable Long id, @Valid @RequestBody MedicoRequest solicitud) {
        return servicio.actualizar(id, solicitud);
    }

    @PatchMapping("/{id}/activar")
    @Operation(summary = "Activa un médico para recibir citas")
    public MedicoResponse activar(@PathVariable Long id) {
        return servicio.cambiarEstado(id, true);
    }

    @PatchMapping("/{id}/desactivar")
    @Operation(summary = "Desactiva un médico (no recibe citas nuevas)")
    public MedicoResponse desactivar(@PathVariable Long id) {
        return servicio.cambiarEstado(id, false);
    }

    @GetMapping("/{id}/disponibilidad")
    @Operation(summary = "Horarios libres del médico en una fecha (bloques de 30 minutos)")
    public DisponibilidadResponse disponibilidad(@PathVariable Long id,
                                                 @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
                                                 LocalDate fecha) {
        return servicio.disponibilidad(id, fecha);
    }
}
