package com.leavemarker.controller;

import com.leavemarker.dto.ApiResponse;
import com.leavemarker.dto.auth.*;
import com.leavemarker.security.UserPrincipal;
import com.leavemarker.service.AuthService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/signup")
    public ResponseEntity<ApiResponse<JwtAuthResponse>> signup(
            @Valid @RequestBody SignupRequest request,
            HttpServletResponse response) {
        JwtAuthResponse authResponse = authService.signup(request);

        // Set httpOnly cookie
        setAuthCookie(response, authResponse.getAccessToken());

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("Company and user created successfully", authResponse));
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<JwtAuthResponse>> login(
            @Valid @RequestBody LoginRequest request,
            HttpServletResponse response) {
        JwtAuthResponse authResponse = authService.login(request);

        // Set httpOnly cookie
        setAuthCookie(response, authResponse.getAccessToken());

        return ResponseEntity.ok(ApiResponse.success("Login successful", authResponse));
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(HttpServletResponse response) {
        // Clear the cookie
        Cookie cookie = new Cookie("auth_token", null);
        cookie.setHttpOnly(true);
        cookie.setSecure(false); // Set to true in production with HTTPS
        cookie.setPath("/");
        cookie.setMaxAge(0); // Delete immediately
        response.addCookie(cookie);

        return ResponseEntity.ok(ApiResponse.success("Logged out successfully"));
    }

    @GetMapping("/verify-session")
    public ResponseEntity<ApiResponse<UserSessionResponse>> verifySession(
            @AuthenticationPrincipal UserPrincipal currentUser) {

        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("No active session"));
        }

        UserSessionResponse sessionData = UserSessionResponse.builder()
                .id(currentUser.getId())
                .email(currentUser.getEmail())
                .fullName(currentUser.getFullName())
                .role(currentUser.getRole().name())
                .companyId(currentUser.getCompanyId())
                .build();

        return ResponseEntity.ok(ApiResponse.success("Session valid", sessionData));
    }

    private void setAuthCookie(HttpServletResponse response, String token) {
        Cookie cookie = new Cookie("auth_token", token);
        cookie.setHttpOnly(true);
        cookie.setSecure(false); // Set to true in production with HTTPS
        cookie.setPath("/");
        cookie.setMaxAge(24 * 60 * 60); // 24 hours
        // SameSite is set via Spring Security configuration
        response.addCookie(cookie);
    }

    @PostMapping("/password-reset-request")
    public ResponseEntity<ApiResponse<Void>> requestPasswordReset(@Valid @RequestBody PasswordResetRequest request) {
        authService.requestPasswordReset(request);
        return ResponseEntity.ok(ApiResponse.success("Password reset email sent"));
    }

    @PostMapping("/password-reset-confirm")
    public ResponseEntity<ApiResponse<Void>> resetPassword(@Valid @RequestBody PasswordResetConfirmRequest request) {
        authService.resetPassword(request);
        return ResponseEntity.ok(ApiResponse.success("Password reset successful"));
    }
}
