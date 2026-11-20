package co.clinicasalvador.citas.controller;

import co.clinicasalvador.citas.domain.TipoDocumento;
import co.clinicasalvador.citas.dto.PacienteDtos.PacienteResponse;
import co.clinicasalvador.citas.exception.ConflictoException;
import co.clinicasalvador.citas.service.PacienteService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.List;

import static org.hamcrest.Matchers.hasItem;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(PacienteController.class)
@DisplayName("PacienteController: contrato HTTP/JSON")
class PacienteControllerTest {

    @Autowired
    private MockMvc mvc;

    @MockBean
    private PacienteService servicio;

    private final PacienteResponse valentina = new PacienteResponse(1L, TipoDocumento.CC, "1047383921",
            "Valentina", "Ruiz Mendoza", "3004567812", "valentina.ruiz@correo.com", LocalDate.of(1998, 4, 17));

    @Test
    @DisplayName("GET /api/pacientes devuelve la lista en JSON")
    void lista() throws Exception {
        when(servicio.listar()).thenReturn(List.of(valentina));

        mvc.perform(get("/api/pacientes"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].documento").value("1047383921"))
                .andExpect(jsonPath("$[0].fechaNacimiento").value("1998-04-17"));
    }

    @Test
    @DisplayName("POST /api/pacientes valida correo y fecha de nacimiento")
    void validaCampos() throws Exception {
        String cuerpo = """
                {"tipoDocumento": "CC", "documento": "1002003004", "nombres": "Camilo",
                 "apellidos": "Torres", "telefono": "3012223344", "correo": "no-es-correo",
                 "fechaNacimiento": "2999-01-01"}
                """;

        mvc.perform(post("/api/pacientes").contentType(MediaType.APPLICATION_JSON).content(cuerpo))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detalles", hasItem("correo: no es un correo válido")))
                .andExpect(jsonPath("$.detalles", hasItem("fechaNacimiento: debe ser una fecha pasada")));
    }

    @Test
    @DisplayName("POST /api/pacientes con documento repetido responde 409")
    void documentoRepetido() throws Exception {
        when(servicio.crear(any())).thenThrow(new ConflictoException("Ya existe un paciente con documento 1047383921."));
        String cuerpo = """
                {"tipoDocumento": "CC", "documento": "1047383921", "nombres": "Valentina",
                 "apellidos": "Ruiz", "telefono": "3004567812", "correo": "v@correo.com",
                 "fechaNacimiento": "1998-04-17"}
                """;

        mvc.perform(post("/api/pacientes").contentType(MediaType.APPLICATION_JSON).content(cuerpo))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value("Conflict"));
    }

    @Test
    @DisplayName("DELETE /api/pacientes/{id} responde 204")
    void elimina() throws Exception {
        mvc.perform(delete("/api/pacientes/3")).andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("DELETE de un paciente con citas responde 409")
    void noEliminaConCitas() throws Exception {
        doThrow(new ConflictoException("El paciente tiene citas registradas y no se puede eliminar."))
                .when(servicio).eliminar(1L);

        mvc.perform(delete("/api/pacientes/1")).andExpect(status().isConflict());
    }
}
