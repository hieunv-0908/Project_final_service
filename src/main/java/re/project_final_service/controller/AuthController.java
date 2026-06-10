package re.project_final_service.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import re.project_final_service.model.dto.request.auth.UserDto;
import re.project_final_service.model.dto.request.auth.UserDtoLogin;
import re.project_final_service.model.dto.response.APIDataResponse;
import re.project_final_service.model.entity.TokenBlacklist;
import re.project_final_service.model.entity.User;
import re.project_final_service.repo.TokenBlackListRepo;
import re.project_final_service.repo.UserRepo;
import re.project_final_service.security.JwtTokenProvider;
import re.project_final_service.service.impl.UserServiceImpl;

import java.time.LocalDateTime;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {
    private final UserServiceImpl userService;
    private final UserDetailsService userDetailsService;
    private final JwtTokenProvider jwtTokenProvider;
    private final TokenBlackListRepo tokenBlackListRepo;
    private final UserRepo userRepo;

    @PostMapping("/register")
    public ResponseEntity<APIDataResponse<User>> register(@Valid @RequestBody UserDto userDto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(
                new APIDataResponse<>(
                        userService.register(userDto),
                        "Đăng ký thành công",
                        true,
                        HttpStatus.CREATED
                )
        );
    }

    @PostMapping("/login")
    public ResponseEntity<Object> login(@Valid @RequestBody UserDtoLogin login) {

        var userDetails = userDetailsService.loadUserByUsername(login.getEmail());
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

        if (userDetails == null || !encoder.matches(login.getPassword(), userDetails.getPassword())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(java.util.Map.of("error", "Invalid credentials"));
        }
        Authentication authentication = new UsernamePasswordAuthenticationToken(userDetails.getUsername(), null, userDetails.getAuthorities());
        String accessToken = jwtTokenProvider.generateAccessToken(authentication);
//        String refreshToken = jwtTokenProvider.generateRefreshToken(authentication);
        return ResponseEntity.ok().body(Map.of("accessToken", accessToken));
    }

    @PostMapping("/logout")
    public ResponseEntity<Object> logout(HttpServletRequest request) {
        String header = request.getHeader("Authorization");
        if (header != null && header.startsWith("Bearer ")) {
            String token = header.substring(7);
            if (jwtTokenProvider.validateToken(token)) {
                String username = jwtTokenProvider.getUsernameFromJwt(token);
                User u = userRepo.findAll().stream().filter(x -> username.equals(x.getEmail())).findFirst().orElse(null);
                TokenBlacklist tb = TokenBlacklist.builder()
                        .tokenString(token)
                        .revokedAt(LocalDateTime.now())
                        .user(u)
                        .build();
                tokenBlackListRepo.save(tb);
            }
        }
        return ResponseEntity.ok(java.util.Map.of("message", "logged out"));
    }
}
