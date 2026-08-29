package ele.embedded.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.NoHandlerFoundException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.time.LocalDateTime;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

  @ExceptionHandler(InconsistentDataException.class)
  public ResponseEntity<Map<String, Object>> handleInconsistentData(InconsistentDataException ex) {
    return buildResponse(HttpStatus.CONFLICT, ex.getMessage());
  }

  // Rutas inexistentes: 404, no 500. Si no, el catch-all convierte cualquier
  // path desconocido (p.ej. el health check del ALB) en un 500.
  @ExceptionHandler({NoResourceFoundException.class, NoHandlerFoundException.class})
  public ResponseEntity<Map<String, Object>> handleNoResource(Exception ex) {
    return buildResponse(HttpStatus.NOT_FOUND, "Resource not found");
  }

  @ExceptionHandler(ResourceNotFoundException.class)
  public ResponseEntity<Map<String, Object>> handleNotFound(ResourceNotFoundException ex) {
    return buildResponse(HttpStatus.NOT_FOUND, ex.getMessage());
  }

  // Catch-all: prevents raw JPA / Spring internals from leaking to the client
  @ExceptionHandler(Exception.class)
  public ResponseEntity<Map<String, Object>> handleGeneric(Exception ex) {
    return buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, "An unexpected error occurred. Please try again.");
  }

  private ResponseEntity<Map<String, Object>> buildResponse(HttpStatus status, String message) {
    Map<String, Object> body = Map.of(
      "timestamp", LocalDateTime.now().toString(),
      "status", status.value(),
      "error", status.getReasonPhrase(),
      "message", message
    );
    return ResponseEntity.status(status).body(body);
  }
}
