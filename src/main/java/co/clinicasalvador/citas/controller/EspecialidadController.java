package co.clinicasalvador.citas.controller;

import co.clinicasalvador.citas.dto.EspecialidadDtos.EspecialidadRequest;
import co.clinicasalvador.citas.dto.EspecialidadDtos.EspecialidadResponse;
import co.clinicasalvador.citas.service.EspecialidadService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.util.List;

@RestController
@RequestMapping("/api/especialidades")
@Tag(name = "Especialidades", description = "Catálogo de especialidades médicas")
public class EspecialidadController {

    private final EspecialidadService servicio;

    public EspecialidadController(EspecialidadService servicio) {
        this.servicio = servicio;
    }

    @GetMapping
    @Operation(summary = "Lista las especialidades")
    public List<EspecialidadResponse> listar() {
        return servicio.listar();
    }

    @PostMapping
    @Operation(summary = "Registra una especialidad")
    public ResponseEntity<EspecialidadResponse> crear(@Valid @RequestBody EspecialidadRequest solicitud) {
        EspecialidadResponse creada = servicio.crear(solicitud);
        return ResponseEntity.created(ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}").buildAndExpand(creada.id()).toUri()).body(creada);
    }
}
