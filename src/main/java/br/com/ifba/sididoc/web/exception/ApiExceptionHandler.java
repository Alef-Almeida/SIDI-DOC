package br.com.ifba.sididoc.web.exception;

import br.com.ifba.sididoc.exception.*;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.time.Instant;

@Slf4j
@RestControllerAdvice
public class ApiExceptionHandler {

    @ExceptionHandler(DatabaseException.class)
    public ResponseEntity<ErrorMessage> handleDatabaseException(DatabaseException ex, HttpServletRequest request) {
        log.error("Api Error - Erro no banco de dados: ", ex);
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .contentType(MediaType.APPLICATION_JSON)
                .body(new ErrorMessage(request, HttpStatus.BAD_REQUEST, Instant.now(),ex.getMessage()));
    }

    @ExceptionHandler(InvalidDocumentTypeException.class)
    public ResponseEntity<ErrorMessage> handleInvalidDocumentTypeException(InvalidDocumentTypeException ex, HttpServletRequest request) {
        log.error("Api Error - Tipo de arquivo inválido: ", ex);
        return ResponseEntity
                .status(HttpStatus.UNSUPPORTED_MEDIA_TYPE)
                .contentType(MediaType.APPLICATION_JSON)
                .body(new ErrorMessage(request, HttpStatus.UNSUPPORTED_MEDIA_TYPE, Instant.now(), ex.getMessage()));
    }

    @ExceptionHandler(CloudStorageException.class)
    public ResponseEntity<ErrorMessage> handleCloudStorageException(CloudStorageException ex, HttpServletRequest request) {
        log.error("Api Error - Erro de Storage: ", ex);
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .contentType(MediaType.APPLICATION_JSON)
                .body(new ErrorMessage(request, HttpStatus.INTERNAL_SERVER_ERROR, Instant.now(), "Erro ao processar armazenamento do arquivo. Tente novamente mais tarde."));
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<ErrorMessage> handleMaxUploadSizeExceededException(MaxUploadSizeExceededException ex, HttpServletRequest request) {
        log.error("Api Error - Arquivo muito grande: ", ex);
        return ResponseEntity
                .status(HttpStatus.PAYLOAD_TOO_LARGE)
                .contentType(MediaType.APPLICATION_JSON)
                .body(new ErrorMessage(request, HttpStatus.PAYLOAD_TOO_LARGE, Instant.now(), "O arquivo excede o tamanho máximo permitido (50MB)."));
    }

    @ExceptionHandler(InvalidDocumentTitleException.class)
    public ResponseEntity<ErrorMessage> handleInvalidDocumentTitleException(InvalidDocumentTitleException ex, HttpServletRequest request) {
        log.error("Api Error - Título do documento inválido: ", ex);
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .contentType(MediaType.APPLICATION_JSON)
                .body(new ErrorMessage(request, HttpStatus.BAD_REQUEST, Instant.now(), ex.getMessage()));
    }

    @ExceptionHandler(SectorAccessDeniedException.class)
    public ResponseEntity<ErrorMessage> handleSectorAccessDeniedException(SectorAccessDeniedException ex, HttpServletRequest request) {
        log.warn("Api Error - Acesso negado ao setor: {}", ex.getMessage());
        return ResponseEntity
                .status(HttpStatus.FORBIDDEN)
                .contentType(MediaType.APPLICATION_JSON)
                .body(new ErrorMessage(request, HttpStatus.FORBIDDEN, Instant.now(), ex.getMessage()));
    }

    @ExceptionHandler(ResourceAlreadyExistsException.class)
    public ResponseEntity<ErrorMessage> handleResourceAlreadyExistsException(ResourceAlreadyExistsException ex, HttpServletRequest request) {
        log.warn("Api Error - Conflito de recurso: {}", ex.getMessage());
        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .contentType(MediaType.APPLICATION_JSON)
                .body(new ErrorMessage(request, HttpStatus.CONFLICT, Instant.now(), ex.getMessage()));
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorMessage> accessDeniedException(RuntimeException ex, HttpServletRequest request) {
        log.error("Api Error - ", ex);
        return ResponseEntity
                .status(HttpStatus.FORBIDDEN)
                .contentType(MediaType.APPLICATION_JSON)
                .body(new ErrorMessage(request, HttpStatus.FORBIDDEN, ex.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorMessage> methodArgumentNotValidException(MethodArgumentNotValidException ex,
                                                                        HttpServletRequest request,
                                                                        BindingResult result) {
        log.error("Api Error - ", ex);
        return ResponseEntity
                .status(HttpStatus.UNPROCESSABLE_ENTITY)
                .contentType(MediaType.APPLICATION_JSON)
                .body(new ErrorMessage(request, HttpStatus.UNPROCESSABLE_ENTITY, "Campo(s) invalido(s)", result));
    }
}
