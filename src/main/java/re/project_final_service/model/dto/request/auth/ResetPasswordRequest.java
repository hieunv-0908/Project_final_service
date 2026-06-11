package re.project_final_service.model.dto.request.auth;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ResetPasswordRequest {
    @NotNull
    @NotBlank
    private String token;

    @NotNull
    @NotBlank
    private String newPassword;
}

