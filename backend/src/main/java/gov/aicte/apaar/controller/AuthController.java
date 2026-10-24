package gov.aicte.apaar.controller;

import gov.aicte.apaar.dto.*;
import gov.aicte.apaar.model.User;
import gov.aicte.apaar.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody LoginRequest req, HttpServletRequest request) {
        String ip = extractClientIp(request);
        AuthResponse response = authService.login(req, ip);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@RequestBody RegisterRequest req, HttpServletRequest request) {
        String ip = extractClientIp(request);
        AuthResponse response = authService.register(req, ip);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/google/config")
    public ResponseEntity<Map<String, Object>> getGoogleAuthConfig() {
        String clientId = authService.getEffectiveGoogleClientId();
        boolean configured = clientId != null && !clientId.trim().isEmpty();
        return ResponseEntity.ok(Map.of(
                "configured", configured,
                "clientId", configured ? clientId : ""
        ));
    }

    @PostMapping("/google")
    public ResponseEntity<AuthResponse> googleLogin(@RequestBody GoogleLoginRequest req, HttpServletRequest request) {
        String ip = extractClientIp(request);
        AuthResponse response = authService.googleLogin(req, ip);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<Map<String, Object>> forgotPassword(@RequestBody ForgotPasswordRequest req, HttpServletRequest request) {
        String ip = extractClientIp(request);
        Map<String, Object> response = authService.forgotPassword(req, ip);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/reset-password")
    public ResponseEntity<Map<String, Object>> resetPassword(@RequestBody ResetPasswordRequest req, HttpServletRequest request) {
        String ip = extractClientIp(request);
        Map<String, Object> response = authService.resetPassword(req, ip);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/me")
    public ResponseEntity<?> getProfile(Authentication auth) {
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getPrincipal())) {
            return ResponseEntity.status(401).body(Map.of("success", false, "error", "Not authenticated"));
        }
        User user = authService.getCurrentUser(auth.getName());
        return ResponseEntity.ok(Map.of(
                "success", true,
                "user", Map.of(
                        "id", user.getId(),
                        "email", user.getEmail(),
                        "fullName", user.getFullName(),
                        "role", user.getRole(),
                        "institutionId", user.getInstitutionId() != null ? user.getInstitutionId() : "",
                        "approvalStatus", user.getApprovalStatus()
                )
        ));
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout() {
        SecurityContextHolder.clearContext();
        return ResponseEntity.ok(Map.of("success", true, "message", "Logged out successfully"));
    }

    private String extractClientIp(HttpServletRequest request) {
        String xf = request.getHeader("X-Forwarded-For");
        if (xf != null && !xf.isEmpty()) {
            return xf.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
