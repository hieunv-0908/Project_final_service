package re.project_final_service.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import re.project_final_service.model.dto.request.auth.UserDto;
import re.project_final_service.model.entity.User;
import re.project_final_service.model.entity.myEnum.Role;
import re.project_final_service.repo.UserRepo;
import re.project_final_service.service.UserService;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserRepo userRepo;
    private final PasswordEncoder passwordEncoder;

    @Override
    public User register(UserDto userDto) {
        if (userRepo.existsByEmail(userDto.getEmail())) {
            throw new RuntimeException("Email đã được đăng ký");
        }

        if (userDto.getRole() == null) {
            throw new RuntimeException("Role is required");
        } else if (userDto.getRole() == Role.ADMIN) {
            throw new RuntimeException("Không được đăng ký với role ADMIN");
        }

        String hashed = passwordEncoder.encode(userDto.getPasswordHash());

        User user = User.builder()
                .username(userDto.getEmail())
                .email(userDto.getEmail())
                .passwordHash(hashed)
                .role(userDto.getRole())
                .isActive(true)
                .build();
        return userRepo.save(user);
    }

    @Override
    public User login(User user) {
        return null;
    }

    @Override
    public User logout(User user) {
        return null;
    }

    @Override
    public User changePassword(User user, String newPassword) {
        return null;
    }
}
