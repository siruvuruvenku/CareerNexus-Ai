package com.careernexus.auth;

import com.careernexus.common.ApiResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final AuthService auth;
    public AuthController(AuthService auth) { this.auth = auth; }
    @PostMapping("/register") @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<AuthDtos.AuthResponse> register(@Valid @RequestBody AuthDtos.RegisterRequest request) { return ApiResponse.ok(auth.register(request)); }
    @PostMapping("/login")
    public ApiResponse<AuthDtos.AuthResponse> login(@Valid @RequestBody AuthDtos.LoginRequest request) { return ApiResponse.ok(auth.login(request)); }
    @PostMapping("/forgot-password") @ResponseStatus(HttpStatus.ACCEPTED)
    public ApiResponse<Void> forgot(@Valid @RequestBody AuthDtos.ForgotPasswordRequest request) {
        auth.requestPasswordReset(request.email()); return ApiResponse.ok(null, "If the account exists and email delivery is configured, reset instructions will be sent.");
    }
    @PostMapping("/reset-password")
    public ApiResponse<Void> reset(@Valid @RequestBody AuthDtos.ResetPasswordRequest request) {
        auth.resetPassword(request.token(), request.newPassword()); return ApiResponse.ok(null, "Password updated. You can now sign in.");
    }
}

