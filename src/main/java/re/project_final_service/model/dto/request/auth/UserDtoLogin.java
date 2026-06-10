package re.project_final_service.model.dto.request.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class UserDtoLogin {
	@Email
	@NotNull
	@NotBlank
	private String email;

	@NotNull
	@NotBlank
	private String password;
}
