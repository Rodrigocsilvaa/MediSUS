package br.com.medisus.api.api;

import br.com.medisus.api.ai.AiUnavailableException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.multipart.support.MissingServletRequestPartException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> validation(MethodArgumentNotValidException ex, HttpServletRequest request) {
        Map<String, String> fields = new LinkedHashMap<>();
        for (FieldError error : ex.getBindingResult().getFieldErrors()) {
            fields.putIfAbsent(error.getField(), error.getDefaultMessage());
        }
        return response(HttpStatus.BAD_REQUEST, "Validation failed", request, fields);
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiError> notFound(ResourceNotFoundException ex, HttpServletRequest request) {
        return response(HttpStatus.NOT_FOUND, ex.getMessage(), request, Map.of());
    }

    @ExceptionHandler(ConflictException.class)
    public ResponseEntity<ApiError> conflict(ConflictException ex, HttpServletRequest request) {
        return response(HttpStatus.CONFLICT, ex.getMessage(), request, Map.of());
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiError> forbidden(AccessDeniedException ex, HttpServletRequest request) {
        return response(HttpStatus.FORBIDDEN, "You do not have permission to access this resource",
                request, Map.of());
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ApiError> unauthorized(AuthenticationException ex, HttpServletRequest request) {
        return response(HttpStatus.UNAUTHORIZED, "Invalid email or password", request, Map.of());
    }

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<ApiError> status(ResponseStatusException ex, HttpServletRequest request) {
        HttpStatus status = HttpStatus.valueOf(ex.getStatusCode().value());
        return response(status, ex.getReason() == null ? status.getReasonPhrase() : ex.getReason(),
                request, Map.of());
    }

    @ExceptionHandler({HttpMessageNotReadableException.class, MissingServletRequestParameterException.class,
            MissingServletRequestPartException.class, MethodArgumentTypeMismatchException.class})
    public ResponseEntity<ApiError> malformedRequest(Exception ex, HttpServletRequest request) {
        return response(HttpStatus.BAD_REQUEST, "The request is invalid or incomplete", request, Map.of());
    }

    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    public ResponseEntity<ApiError> unsupportedMedia(HttpMediaTypeNotSupportedException ex,
                                                      HttpServletRequest request) {
        return response(HttpStatus.UNSUPPORTED_MEDIA_TYPE, "The request media type is not supported",
                request, Map.of());
    }

    @ExceptionHandler(AiUnavailableException.class)
    public ResponseEntity<ApiError> aiUnavailable(AiUnavailableException ex, HttpServletRequest request) {
        return response(HttpStatus.SERVICE_UNAVAILABLE, ex.getMessage(), request, Map.of());
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiError> dataConflict(DataIntegrityViolationException ex, HttpServletRequest request) {
        return response(HttpStatus.CONFLICT, "The request conflicts with an existing resource", request, Map.of());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> unexpected(Exception ex, HttpServletRequest request) {
        return response(HttpStatus.INTERNAL_SERVER_ERROR, "An unexpected error occurred", request, Map.of());
    }

    private ResponseEntity<ApiError> response(HttpStatus status, String message, HttpServletRequest request,
                                               Map<String, String> fields) {
        return ResponseEntity.status(status).body(new ApiError(Instant.now(), status.value(),
                status.getReasonPhrase(), message, request.getRequestURI(), fields));
    }
}
