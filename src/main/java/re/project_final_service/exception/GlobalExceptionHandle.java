package re.project_final_service.exception;

import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import re.project_final_service.model.dto.response.APIDataResponse;
import re.project_final_service.model.dto.response.ErrorResponse;

@RestControllerAdvice
public class GlobalExceptionHandle {

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<APIDataResponse<ErrorResponse>> handleRuntimeException(RuntimeException e) {
        ErrorResponse errorResponse = new ErrorResponse(
                e.getMessage(),
                java.time.LocalDateTime.now(),
                HttpStatusCode.valueOf(500)
        );
        return ResponseEntity.status(500).body(new APIDataResponse<>(errorResponse, e.getMessage(), false, HttpStatusCode.valueOf(500)));
    }
}
