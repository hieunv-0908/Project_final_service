package re.project_final_service.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import re.project_final_service.model.dto.request.RefreshRequest;
import re.project_final_service.model.dto.request.auth.UserDto;
import re.project_final_service.model.dto.request.auth.UserDtoLogin;
import re.project_final_service.model.dto.request.auth.ChangePasswordRequest;
import re.project_final_service.model.dto.request.auth.ForgotPasswordRequest;
import re.project_final_service.model.dto.request.auth.ResetPasswordRequest;
import re.project_final_service.model.dto.response.APIDataResponse;
import re.project_final_service.model.entity.RefreshToken;
import re.project_final_service.model.entity.User;
import re.project_final_service.repo.UserRepo;
import re.project_final_service.security.JwtTokenProvider;
import re.project_final_service.service.impl.RedisBlacklistService;
import re.project_final_service.service.impl.RefreshTokenService;
import re.project_final_service.service.impl.UserServiceImpl;
import re.project_final_service.service.PasswordResetService;
import re.project_final_service.model.entity.PasswordResetToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.Instant;
import java.util.Date;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Transactional
public class AuthController {
    private final UserServiceImpl userService;
    private final UserDetailsService userDetailsService;
    private final JwtTokenProvider jwtTokenProvider;
    private final RedisBlacklistService redisBlacklistService;
    private final UserRepo userRepo;
    private final PasswordEncoder passwordEncoder;
    private final RefreshTokenService refreshTokenService;
    private final PasswordResetService passwordResetService;
    private final AuthenticationManager authenticationManager;

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
        Authentication authentication = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(login.getEmail(), login.getPassword()));

        String accessToken = jwtTokenProvider.generateAccessToken(authentication);
        User user = userRepo.findByEmail(authentication.getName()).orElseThrow(() -> new UsernameNotFoundException("Không tìm thấy người dùng"));
        RefreshToken refreshToken = refreshTokenService.createRefreshToken(user);
        return ResponseEntity.ok().body(new APIDataResponse<>(Map.of("accessToken", accessToken, "refreshToken", refreshToken.getToken()), "Đăng nhập thành công", true, HttpStatus.OK));
    }

    @PostMapping("/logout")
    public ResponseEntity<Object> logout(HttpServletRequest request) {
        String header = request.getHeader("Authorization");
        if (header != null && header.startsWith("Bearer ")) {
            String token = header.substring(7);

            if (jwtTokenProvider.validateToken(token)) {
                Date expiration =
                        jwtTokenProvider.getExpirationDate(token);

                long remainingTime =
                        expiration.getTime()
                                - System.currentTimeMillis();
                redisBlacklistService.blacklistToken(
                        token,
                        remainingTime
                );

                String username =
                        jwtTokenProvider.getUsernameFromJwt(token);
                User user =
                        userRepo.findByEmail(username)
                                .orElseThrow(
                                        () -> new RuntimeException(
                                                "Không tìm thấy user"
                                        )
                                );
                refreshTokenService.deleteByUser(user);
            }
        }else{
            throw new RuntimeException("Không tìm thấy token");
        }
        return ResponseEntity.ok(Map.of("message", "Đăng xuất thành công"));
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

    @PutMapping("/change-password")
    public ResponseEntity<Object> changePassword(@Valid @RequestBody ChangePasswordRequest req) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getPrincipal())) {
            throw new RuntimeException("Không có quyền");
        }

        String email = auth.getName();
        User user = userRepo.findByEmail(email).orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng"));

        Authentication authentication = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(user.getEmail(), req.getCurrentPassword()));

        userService.changePassword(user, req.getNewPassword());

        return ResponseEntity.ok(Map.of("message", "Đổi mật khẩu thành công"));
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<Object> forgotPassword(@Valid @RequestBody ForgotPasswordRequest req) {
        Optional<User> opt = userRepo.findByEmail(req.getEmail());
        if (opt.isPresent()) {
            PasswordResetToken token = passwordResetService.createTokenForUser(opt.get());
            System.out.println("Password reset token for " + req.getEmail() + " -> " + token.getToken());
            return ResponseEntity.ok(Map.of("message", "Email hợp lệ, dùng token để cài lại mật khẩu", "token", token.getToken()));
        }

        return ResponseEntity.ok(Map.of("message", "Email không hợp lệ"));
    }

    @PutMapping("/reset-password")
    public ResponseEntity<Object> resetPassword(@Valid @RequestBody ResetPasswordRequest req) {
        PasswordResetToken prt = passwordResetService.findByToken(req.getToken()).orElseThrow(() -> new RuntimeException("Token không hợp lệ"));

        if (prt.isRevoked() || prt.getExpiryDate().isBefore(Instant.now())) {
            throw new RuntimeException("Token đã hết hạn hoặc đã bị thu hồi");
        }

        User user = prt.getUser();
        userService.changePassword(user, req.getNewPassword());

        passwordResetService.revokeAllForUser(user);

        return ResponseEntity.ok(Map.of("message", "Đặt lại mật khẩu thành công"));
    }
}
