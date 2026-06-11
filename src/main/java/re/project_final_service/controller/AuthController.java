package re.project_final_service.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import re.project_final_service.model.dto.request.RefreshRequest;
import re.project_final_service.model.dto.request.auth.UserDto;
import re.project_final_service.model.dto.request.auth.UserDtoLogin;
import re.project_final_service.model.dto.response.APIDataResponse;
import re.project_final_service.model.entity.RefreshToken;
import re.project_final_service.model.entity.TokenBlacklist;
import re.project_final_service.model.entity.User;
import re.project_final_service.repo.TokenBlackListRepo;
import re.project_final_service.repo.UserRepo;
import re.project_final_service.security.JwtTokenProvider;
import re.project_final_service.service.impl.RefreshTokenService;
import re.project_final_service.service.impl.UserServiceImpl;

import java.time.Instant;
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
    private final PasswordEncoder passwordEncoder;
    private final RefreshTokenService refreshTokenService;

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
    public ResponseEntity<APIDataResponse<Object>> login(@Valid @RequestBody UserDtoLogin login) {

        UserDetails userDetails = userDetailsService.loadUserByUsername(login.getEmail());

        if (!passwordEncoder.matches(login.getPassword(), userDetails.getPassword())) {
            throw new UsernameNotFoundException("Thông tin đăng nhập không đúng");
        }


        Authentication authentication = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
        String accessToken = jwtTokenProvider.generateAccessToken(authentication);
        User user = userRepo.findByEmail(authentication.getName()).orElse(null);
        RefreshToken refreshToken = refreshTokenService.createRefreshToken(user);
        return ResponseEntity.ok().body(new APIDataResponse<>(Map.of("accessToken", accessToken, "refreshToken", refreshToken), "Đăng nhập thành công", true, HttpStatus.OK));
    }

    @PostMapping("/logout")
    public ResponseEntity<Object> logout(HttpServletRequest request) {
        String header = request.getHeader("Authorization");
        if (header != null && header.startsWith("Bearer ")) {
            String token = header.substring(7);
            if (jwtTokenProvider.validateToken(token)) {
                String username = jwtTokenProvider.getUsernameFromJwt(token);
                User u = userRepo.findByEmail(username)
                        .orElseThrow(() -> new RuntimeException("Khônh tìm thấy người dùng"));
                TokenBlacklist tb = TokenBlacklist.builder()
                        .tokenString(token)
                        .revokedAt(LocalDateTime.now())
                        .user(u)
                        .build();
                refreshTokenService.deleteByUser(u);
                tokenBlackListRepo.save(tb);
            }
        }
        return ResponseEntity.ok(java.util.Map.of("message", "logged out"));
    }

    @Transactional
    @PostMapping("/refresh")
    public ResponseEntity<Object> refreshToken(
            @RequestBody RefreshRequest request) {

        RefreshToken oldToken =
                refreshTokenService
                        .findByToken(request.getRefreshToken())
                        .orElseThrow(
                                () -> new RuntimeException("Refresh token không hợp lệ")
                        );

        if (oldToken.getExpiryDate().isBefore(Instant.now())) {
            throw new RuntimeException("Refresh token hết hạn");
        }

        User user = oldToken.getUser();

        refreshTokenService.delete(oldToken);

        RefreshToken newToken =
                refreshTokenService.createRefreshToken(user);

        UserDetails userDetails =
                userDetailsService.loadUserByUsername(
                        user.getEmail()
                );
        Authentication authentication =
                new UsernamePasswordAuthenticationToken(
                        userDetails,
                        null,
                        userDetails.getAuthorities()
                );

        String accessToken =
                jwtTokenProvider.generateAccessToken(authentication);

        return ResponseEntity.ok(
                Map.of(
                        "accessToken", accessToken,
                        "refreshToken", newToken.getToken()
                )
        );
    }
}
