package co.clinicasalvador.citas.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Traduce las excepciones a respuestas HTTP con un cuerpo {@link ApiError}.
 *
 * <p>Los controladores y servicios no conocen códigos HTTP: solo lanzan
 * excepciones del dominio, y esta clase decide cómo se presentan
 * (responsabilidad única).</p>
 */
@RestControllerAdvice
public class ManejadorGlobalErrores {

    @ExceptionHandler(RecursoNoEncontradoException.class)
    public ResponseEntity<ApiError> noEncontrado(RecursoNoEncontradoException ex, HttpServletRequest peticion) {
        return responder(HttpStatus.NOT_FOUND, ex.getMessage(), peticion, List.of());
    }

    @ExceptionHandler(ConflictoException.class)
    public ResponseEntity<ApiError> conflicto(ConflictoException ex, HttpServletRequest peticion) {
        return responder(HttpStatus.CONFLICT, ex.getMessage(), peticion, List.of());
    }

    @ExceptionHandler(ReglaNegocioException.class)
    public ResponseEntity<ApiError> reglaNegocio(ReglaNegocioException ex, HttpServletRequest peticion) {
        return responder(HttpStatus.UNPROCESSABLE_ENTITY, ex.getMessage(), peticion, List.of());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> validacion(MethodArgumentNotValidException ex, HttpServletRequest peticion) {
        List<String> detalles = ex.getBindingResult().getFieldErrors().stream()
                .map(campo -> campo.getField() + ": " + campo.getDefaultMessage())
                .sorted()
                .toList();
        return responder(HttpStatus.BAD_REQUEST, "La petición tiene datos inválidos.", peticion, detalles);
    }

    @ExceptionHandler({HttpMessageNotReadableException.class, MethodArgumentTypeMismatchException.class})
    public ResponseEntity<ApiError> formatoInvalido(Exception ex, HttpServletRequest peticion) {
        return responder(HttpStatus.BAD_REQUEST,
                "El cuerpo o los parámetros de la petición no tienen el formato esperado.", peticion, List.of());
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiError> integridad(DataIntegrityViolationException ex, HttpServletRequest peticion) {
        return responder(HttpStatus.CONFLICT,
                "La operación viola una restricción de la base de datos (dato duplicado o en uso).",
                peticion, List.of());
    }

    private static ResponseEntity<ApiError> responder(HttpStatus estado, String mensaje,
                                                      HttpServletRequest peticion, List<String> detalles) {
        ApiError cuerpo = new ApiError(LocalDateTime.now(), estado.value(), estado.getReasonPhrase(),
                mensaje, peticion.getRequestURI(), detalles);
        return ResponseEntity.status(estado).body(cuerpo);
    }
}
