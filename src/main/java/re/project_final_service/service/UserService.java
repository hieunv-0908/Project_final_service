package re.project_final_service.service;

import org.springframework.stereotype.Service;
import re.project_final_service.model.dto.request.auth.UserDto;
import re.project_final_service.model.entity.User;

public interface UserService {
    public User register(UserDto userDto);
    public User login(User user);
    public User logout(User user);
    public User changePassword(User user, String newPassword);
}
