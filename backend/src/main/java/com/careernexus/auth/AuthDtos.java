package com.careernexus.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public final class AuthDtos {
    private AuthDtos() {}
    public record RegisterRequest(@Email @NotBlank String email, @NotBlank @Size(min = 10, max = 72) String password,
                                  @NotBlank @Size(max = 160) String fullName) {}
    public record LoginRequest(@Email @NotBlank String email, @NotBlank String password) {}
    public record ForgotPasswordRequest(@Email @NotBlank String email) {}
    public record ResetPasswordRequest(@NotBlank String token, @NotBlank @Size(min = 10, max = 72) String newPassword) {}
    public record AuthUser(Long id, String email, String fullName) {}
    public record AuthResponse(String accessToken, String tokenType, long expiresInSeconds, AuthUser user) {}
}

