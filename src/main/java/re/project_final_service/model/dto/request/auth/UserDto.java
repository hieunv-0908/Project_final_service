package re.project_final_service.model.dto.request.auth;

import jakarta.persistence.Column;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import re.project_final_service.model.entity.myEnum.Role;

@AllArgsConstructor
@Data
public class UserDto {
    @Email
    @NotNull
    @NotBlank
    @NotEmpty
    @Column(unique = true)
    private String email;
    @NotNull
    @NotBlank
    @NotEmpty
    private String passwordHash;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;
}
