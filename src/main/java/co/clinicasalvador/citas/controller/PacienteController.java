package co.clinicasalvador.citas.controller;

import co.clinicasalvador.citas.dto.PacienteDtos.PacienteRequest;
import co.clinicasalvador.citas.dto.PacienteDtos.PacienteResponse;
import co.clinicasalvador.citas.service.PacienteService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.util.List;

@RestController
@RequestMapping("/api/pacientes")
@Tag(name = "Pacientes", description = "Registro de pacientes")
public class PacienteController {

    private final PacienteService servicio;

    public PacienteController(PacienteService servicio) {
        this.servicio = servicio;
    }

    @GetMapping
    @Operation(summary = "Lista los pacientes")
    public List<PacienteResponse> listar() {
        return servicio.listar();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Consulta un paciente por id")
    public PacienteResponse buscar(@PathVariable Long id) {
        return servicio.buscar(id);
    }

    @GetMapping("/buscar")
    @Operation(summary = "Consulta un paciente por número de documento")
    public PacienteResponse buscarPorDocumento(@RequestParam String documento) {
        return servicio.buscarPorDocumento(documento);
    }

    @PostMapping
    @Operation(summary = "Registra un paciente")
    public ResponseEntity<PacienteResponse> crear(@Valid @RequestBody PacienteRequest solicitud) {
        PacienteResponse creado = servicio.crear(solicitud);
        return ResponseEntity.created(ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}").buildAndExpand(creado.id()).toUri()).body(creado);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualiza los datos de contacto de un paciente")
    public PacienteResponse actualizar(@PathVariable Long id, @Valid @RequestBody PacienteRequest solicitud) {
        return servicio.actualizar(id, solicitud);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Elimina un paciente sin citas registradas")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        servicio.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
