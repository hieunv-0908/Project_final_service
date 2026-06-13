package re.project_final_service.model.dto.request.user;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import re.project_final_service.model.entity.myEnum.Role;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AdminUpdateUserDto {

    private String email;

    private Role role;

    private Boolean isActive;
}