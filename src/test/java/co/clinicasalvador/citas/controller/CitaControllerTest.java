package co.clinicasalvador.citas.controller;

import co.clinicasalvador.citas.domain.EstadoCita;
import co.clinicasalvador.citas.dto.CitaDtos.CitaRequest;
import co.clinicasalvador.citas.dto.CitaDtos.CitaResponse;
import co.clinicasalvador.citas.dto.CitaDtos.MedicoResumen;
import co.clinicasalvador.citas.dto.CitaDtos.PacienteResumen;
import co.clinicasalvador.citas.exception.ConflictoException;
import co.clinicasalvador.citas.exception.RecursoNoEncontradoException;
import co.clinicasalvador.citas.exception.ReglaNegocioException;
import co.clinicasalvador.citas.service.CitaService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static co.clinicasalvador.citas.support.DatosPrueba.AHORA;
import static co.clinicasalvador.citas.support.DatosPrueba.MIERCOLES;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CitaController.class)
@DisplayName("CitaController: contrato HTTP/JSON")
class CitaControllerTest {

    @Autowired
    private MockMvc mvc;

    @MockBean
    private CitaService servicio;

    private final LocalDateTime fecha = MIERCOLES.atTime(9, 0);

    private CitaResponse respuesta(EstadoCita estado) {
        return new CitaResponse(10L,
                new PacienteResumen(1L, "Valentina Ruiz Mendoza", "1047383921"),
                new MedicoResumen(1L, "Andrés Martínez Pérez", "Medicina general"),
                fecha, "Control general", estado, AHORA);
    }

    private static final String CUERPO_VALIDO = """
            {"pacienteId": 1, "medicoId": 1, "fechaHora": "2026-12-02T09:00:00", "motivo": "Control general"}
            """;

    @Test
    @DisplayName("POST /api/citas responde 201 con Location y el JSON de la cita")
    void agendaCita() throws Exception {
        when(servicio.agendar(any(CitaRequest.class))).thenReturn(respuesta(EstadoCita.PROGRAMADA));

        mvc.perform(post("/api/citas").contentType(MediaType.APPLICATION_JSON).content(CUERPO_VALIDO))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "http://localhost/api/citas/10"))
                .andExpect(jsonPath("$.id").value(10))
                .andExpect(jsonPath("$.estado").value("PROGRAMADA"))
                .andExpect(jsonPath("$.fechaHora").value("2026-12-02T09:00:00"))
                .andExpect(jsonPath("$.medico.especialidad").value("Medicina general"));
    }

    @Test
    @DisplayName("POST /api/citas con datos incompletos responde 400 con el detalle de cada campo")
    void validaCuerpo() throws Exception {
        mvc.perform(post("/api/citas").contentType(MediaType.APPLICATION_JSON).content("{\"motivo\": \"\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.estado").value(400))
                .andExpect(jsonPath("$.detalles.length()").value(4));
        verifyNoInteractions(servicio);
    }

    @Test
    @DisplayName("POST /api/citas con JSON mal formado responde 400")
    void jsonMalFormado() throws Exception {
        mvc.perform(post("/api/citas").contentType(MediaType.APPLICATION_JSON).content("{pacienteId:"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("una regla de negocio incumplida responde 422")
    void reglaDeNegocio() throws Exception {
        when(servicio.agendar(any())).thenThrow(new ReglaNegocioException("La clínica solo agenda citas de lunes a viernes."));

        mvc.perform(post("/api/citas").contentType(MediaType.APPLICATION_JSON).content(CUERPO_VALIDO))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.mensaje").value("La clínica solo agenda citas de lunes a viernes."))
                .andExpect(jsonPath("$.ruta").value("/api/citas"));
    }

    @Test
    @DisplayName("un horario ocupado responde 409")
    void horarioOcupado() throws Exception {
        when(servicio.agendar(any())).thenThrow(new ConflictoException("El médico ya tiene una cita."));

        mvc.perform(post("/api/citas").contentType(MediaType.APPLICATION_JSON).content(CUERPO_VALIDO))
                .andExpect(status().isConflict());
    }

    @Test
    @DisplayName("GET /api/citas/{id} inexistente responde 404")
    void citaInexistente() throws Exception {
        when(servicio.buscarPorId(99L)).thenThrow(new RecursoNoEncontradoException("Cita", 99L));

        mvc.perform(get("/api/citas/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.mensaje").value("Cita con id 99 no existe."));
    }

    @Test
    @DisplayName("GET /api/citas pasa los filtros al servicio")
    void buscaConFiltros() throws Exception {
        when(servicio.buscar(1L, null, MIERCOLES, EstadoCita.PROGRAMADA))
                .thenReturn(List.of(respuesta(EstadoCita.PROGRAMADA)));

        mvc.perform(get("/api/citas").param("medicoId", "1").param("fecha", "2026-12-02").param("estado", "PROGRAMADA"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
        verify(servicio).buscar(eq(1L), eq(null), eq(MIERCOLES), eq(EstadoCita.PROGRAMADA));
    }

    @Test
    @DisplayName("un estado inválido en el filtro responde 400")
    void estadoInvalido() throws Exception {
        mvc.perform(get("/api/citas").param("estado", "PERDIDA"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("PATCH /api/citas/{id}/cancelar devuelve la cita cancelada")
    void cancela() throws Exception {
        when(servicio.cancelar(10L)).thenReturn(respuesta(EstadoCita.CANCELADA));

        mvc.perform(patch("/api/citas/10/cancelar"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("CANCELADA"));
    }
}
