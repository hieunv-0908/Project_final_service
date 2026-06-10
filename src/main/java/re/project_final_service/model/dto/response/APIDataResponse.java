package re.project_final_service.model.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.http.HttpStatusCode;
import re.project_final_service.model.entity.User;

@AllArgsConstructor
@Data
public class APIDataResponse<T> {
    private T data;
    private String message;
    private boolean success;
    private HttpStatusCode statusCode;
}
