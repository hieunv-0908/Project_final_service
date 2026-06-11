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
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import re.project_final_service.model.dto.request.RefreshRequest;
import re.project_final_service.model.dto.request.auth.UserDto;
import re.project_final_service.model.dto.request.auth.UserDtoLogin;
import re.project_final_service.model.dto.request.auth.ChangePasswordRequest;
import re.project_final_service.model.dto.request.auth.ForgotPasswordRequest;
import re.project_final_service.model.dto.request.auth.ResetPasswordRequest;
import re.project_final_service.model.dto.response.APIDataResponse;
import re.project_final_service.model.entity.RefreshToken;
import re.project_final_service.model.entity.TokenBlacklist;
import re.project_final_service.model.entity.User;
import re.project_final_service.repo.TokenBlackListRepo;
import re.project_final_service.repo.UserRepo;
import re.project_final_service.security.JwtTokenProvider;
import re.project_final_service.service.impl.RefreshTokenService;
import re.project_final_service.service.impl.UserServiceImpl;
import re.project_final_service.service.PasswordResetService;
import re.project_final_service.model.entity.PasswordResetToken;
import org.springframework.security.core.context.SecurityContextHolder;

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
    private final PasswordResetService passwordResetService;

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
        return ResponseEntity.ok().body(new APIDataResponse<>(Map.of("accessToken", accessToken, "refreshToken", refreshToken.getToken()), "Đăng nhập thành công", true, HttpStatus.OK));
    }

    @PostMapping("/logout")
    public ResponseEntity<Object> logout(HttpServletRequest request) {
        String header = request.getHeader("Authorization");
        if (header != null && header.startsWith("Bearer ")) {
            String token = header.substring(7);
            System.out.println("TOKEN = " + token);

            System.out.println(
                    "VALID = " +
                            jwtTokenProvider.validateToken(token)
            );
            if (jwtTokenProvider.validateToken(token)) {
                String username = jwtTokenProvider.getUsernameFromJwt(token);
                User u = userRepo.findByEmail(username)
                        .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng"));
                TokenBlacklist tb = TokenBlacklist.builder()
                        .tokenString(token)
                        .revokedAt(LocalDateTime.now())
                        .user(u)
                        .build();

                // THÊM: Revoke tất cả refresh token của user
                // (thay vì chỉ delete)
                refreshTokenService.deleteByUser(u);

                tokenBlackListRepo.save(tb);
            }
        }else{
            throw new RuntimeException("Không tìm thấy token");
        }
        return ResponseEntity.ok(Map.of("message", "logged out"));
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

        // THÊM KIỂM TRA: Nếu token đã bị revoke
        if (oldToken.isRevoked()) {
            throw new RuntimeException("Refresh token đã bị vô hiệu hóa");
        }

        if (oldToken.getExpiryDate().isBefore(Instant.now())) {
            throw new RuntimeException("Refresh token hết hạn");
        }

        User user = oldToken.getUser();

        // SỬA: Thay vì chỉ delete, thêm revoke trước
        oldToken.setRevoked(true);
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

        // SỬA: Trả về chỉ token string (nhất quán với response)
        return ResponseEntity.ok(
                Map.of(
                        "accessToken", accessToken,
                        "refreshToken", newToken.getToken()
                )
        );
    }

    @PostMapping("/change-password")
    public ResponseEntity<Object> changePassword(@RequestBody ChangePasswordRequest req) {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getPrincipal())) {
            throw new RuntimeException("Không có quyền");
        }

        String username = auth.getName();
        User user = userRepo.findByEmail(username).orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng"));

        if (!passwordEncoder.matches(req.getCurrentPassword(), user.getPasswordHash())) {
            throw new RuntimeException("Mật khẩu hiện tại không đúng");
        }

        userService.changePassword(user, req.getNewPassword());

        return ResponseEntity.ok(Map.of("message", "Đổi mật khẩu thành công"));
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<Object> forgotPassword(@RequestBody @Valid ForgotPasswordRequest req) {
        var opt = userRepo.findByEmail(req.getEmail());
        if (opt.isPresent()) {
            PasswordResetToken token = passwordResetService.createTokenForUser(opt.get());
            // In production, send token via email. For now, return token in response for convenience / testing.
            System.out.println("Password reset token for " + req.getEmail() + " -> " + token.getToken());
            return ResponseEntity.ok(Map.of("message", "Nếu email tồn tại, một token reset đã được tạo", "token", token.getToken()));
        }

        // Do not reveal whether email exists
        return ResponseEntity.ok(Map.of("message", "Nếu email tồn tại, một token reset đã được tạo"));
    }

    @PostMapping("/reset-password")
    public ResponseEntity<Object> resetPassword(@RequestBody @Valid ResetPasswordRequest req) {
        PasswordResetToken prt = passwordResetService.findByToken(req.getToken()).orElseThrow(() -> new RuntimeException("Token không hợp lệ"));

        if (prt.isRevoked() || prt.getExpiryDate().isBefore(java.time.Instant.now())) {
            throw new RuntimeException("Token đã hết hạn hoặc đã bị thu hồi");
        }

        User user = prt.getUser();
        userService.changePassword(user, req.getNewPassword());

        // revoke tokens for the user
        passwordResetService.revokeAllForUser(user);

        return ResponseEntity.ok(Map.of("message", "Đặt lại mật khẩu thành công"));
    }
}
