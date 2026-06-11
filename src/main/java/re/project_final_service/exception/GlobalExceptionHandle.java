package re.project_final_service.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import re.project_final_service.model.dto.response.APIDataResponse;
import re.project_final_service.model.dto.response.ErrorResponse;

import java.time.LocalDateTime;

@RestControllerAdvice
public class GlobalExceptionHandle {

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ErrorResponse> handleRuntimeException(RuntimeException e) {
        ErrorResponse errorResponse = new ErrorResponse(
                e.getMessage(),
                LocalDateTime.now(),
                HttpStatusCode.valueOf(500)
        );
        return ResponseEntity.status(500).body(errorResponse);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handle(HttpMessageNotReadableException ex) {
        ErrorResponse errorResponse = new ErrorResponse(
                ex.getMessage(),
                LocalDateTime.now(),
                HttpStatusCode.valueOf(500)
        );
        return ResponseEntity.status(500).body(errorResponse);
    }

    @ExceptionHandler(UsernameNotFoundException.class)
    public ResponseEntity<APIDataResponse<Object>>
    handleUsernameNotFound(UsernameNotFoundException e) {

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(
                        new APIDataResponse<>(
                                null,
                                e.getMessage(),
                                false,
                                HttpStatus.UNAUTHORIZED
                        )
                );
    }
}
