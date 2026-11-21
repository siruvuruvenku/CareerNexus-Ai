package com.careernexus.auth;

import com.careernexus.profile.UserProfile;
import com.careernexus.security.JwtService;
import com.careernexus.user.AppUser;
import com.careernexus.user.AppUserRepository;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Base64;

@Service
public class AuthService {
    private final AppUserRepository users;
    private final PasswordResetTokenRepository resetTokens;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final ObjectProvider<JavaMailSender> mailSender;
    private final String frontendUrl;
    private final String mailFrom;
    private final String smtpHost;
    private final long expirationMs;

    public AuthService(AppUserRepository users, PasswordResetTokenRepository resetTokens, PasswordEncoder passwordEncoder,
                       AuthenticationManager authenticationManager, JwtService jwtService, ObjectProvider<JavaMailSender> mailSender,
                       @Value("${app.frontend-url}") String frontendUrl, @Value("${MAIL_FROM:no-reply@careernexus.local}") String mailFrom,
                       @Value("${SMTP_HOST:}") String smtpHost,
                       @Value("${app.jwt.expiration-ms}") long expirationMs) {
        this.users = users; this.resetTokens = resetTokens; this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager; this.jwtService = jwtService; this.mailSender = mailSender;
        this.frontendUrl = frontendUrl; this.mailFrom = mailFrom; this.smtpHost = smtpHost; this.expirationMs = expirationMs;
    }

    @Transactional
    public AuthDtos.AuthResponse register(AuthDtos.RegisterRequest request) {
        if (users.existsByEmailIgnoreCase(request.email())) throw new ResponseStatusException(HttpStatus.CONFLICT, "An account with this email already exists");
        AppUser user = new AppUser(); user.setEmail(request.email().trim().toLowerCase()); user.setPasswordHash(passwordEncoder.encode(request.password()));
        UserProfile profile = new UserProfile(); profile.setUser(user); profile.setFullName(request.fullName().trim()); profile.setYearsExperience(0);
        user.setProfile(profile); user = users.save(user);
        return response(user);
    }

    @Transactional(readOnly = true)
    public AuthDtos.AuthResponse login(AuthDtos.LoginRequest request) {
        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(request.email().trim().toLowerCase(), request.password()));
        AppUser user = users.findByEmailIgnoreCase(request.email()).orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Account not found"));
        return response(user);
    }

    @Transactional
    public void requestPasswordReset(String email) {
        var user = users.findByEmailIgnoreCase(email);
        JavaMailSender sender = mailSender.getIfAvailable();
        if (user.isEmpty() || sender == null || smtpHost.isBlank()) return;
        String raw = randomToken();
        PasswordResetToken token = new PasswordResetToken(); token.setUser(user.get()); token.setTokenHash(hash(raw));
        token.setExpiresAt(Instant.now().plus(30, ChronoUnit.MINUTES)); resetTokens.save(token);
        try {
            MimeMessage message = sender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, StandardCharsets.UTF_8.name());
            helper.setFrom(mailFrom); helper.setTo(user.get().getEmail()); helper.setSubject("Reset your CareerNexus password");
            helper.setText("Use this secure link within 30 minutes to choose a new password: " + frontendUrl + "/reset-password?token=" + raw, false);
            sender.send(message);
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "Password reset email could not be sent");
        }
    }

    @Transactional
    public void resetPassword(String rawToken, String newPassword) {
        var reset = resetTokens.findByTokenHashAndUsedAtIsNull(hash(rawToken)).orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Reset link is invalid or expired"));
        if (reset.getExpiresAt().isBefore(Instant.now())) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Reset link is invalid or expired");
        reset.getUser().setPasswordHash(passwordEncoder.encode(newPassword)); reset.setUsedAt(Instant.now());
    }

    private AuthDtos.AuthResponse response(AppUser user) {
        String fullName = user.getProfile() == null ? "" : user.getProfile().getFullName();
        return new AuthDtos.AuthResponse(jwtService.issue(user), "Bearer", expirationMs / 1000,
                new AuthDtos.AuthUser(user.getId(), user.getEmail(), fullName));
    }
    private static String randomToken() { byte[] bytes = new byte[32]; new SecureRandom().nextBytes(bytes); return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes); }
    private static String hash(String token) {
        try { return java.util.HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(token.getBytes(StandardCharsets.UTF_8))); }
        catch (Exception e) { throw new IllegalStateException("SHA-256 is unavailable", e); }
    }
}

