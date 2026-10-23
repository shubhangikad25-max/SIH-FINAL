package gov.aicte.apaar.service;

import gov.aicte.apaar.dto.*;
import gov.aicte.apaar.model.PasswordResetToken;
import gov.aicte.apaar.model.User;
import gov.aicte.apaar.model.VerificationLog;
import gov.aicte.apaar.repository.CollegeRepository;
import gov.aicte.apaar.repository.PasswordResetTokenRepository;
import gov.aicte.apaar.repository.UserRepository;
import gov.aicte.apaar.repository.VerificationLogRepository;
import gov.aicte.apaar.security.JwtTokenProvider;
import gov.aicte.apaar.security.RateLimiterService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.regex.Pattern;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final CollegeRepository collegeRepository;
    private final PasswordResetTokenRepository resetTokenRepository;
    private final VerificationLogRepository logRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider tokenProvider;
    private final RateLimiterService rateLimiterService;
    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${google.client.id:}")
    private String googleClientId;

    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9+_.-]+@(.+)$");
    private static final Pattern PASSWORD_PATTERN = Pattern.compile("^(?=.*[A-Za-z])(?=.*\\d).{8,}$");

    public AuthService(UserRepository userRepository,
                       CollegeRepository collegeRepository,
                       PasswordResetTokenRepository resetTokenRepository,
                       VerificationLogRepository logRepository,
                       PasswordEncoder passwordEncoder,
                       JwtTokenProvider tokenProvider,
                       RateLimiterService rateLimiterService) {
        this.userRepository = userRepository;
        this.collegeRepository = collegeRepository;
        this.resetTokenRepository = resetTokenRepository;
        this.logRepository = logRepository;
        this.passwordEncoder = passwordEncoder;
        this.tokenProvider = tokenProvider;
        this.rateLimiterService = rateLimiterService;
    }

    public AuthResponse login(LoginRequest req, String ipAddress) {
        if (req.getEmail() == null || req.getPassword() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Email and password are required.");
        }

        String email = req.getEmail().trim().toLowerCase();
        String rateLimitKey = ipAddress + ":" + email;

        if (rateLimiterService.isBlocked(rateLimitKey)) {
            throw new ResponseStatusException(HttpStatus.TOO_MANY_REQUESTS,
                    "Too many failed login attempts. Please wait 15 minutes before trying again.");
        }

        Optional<User> userOpt = userRepository.findByEmailIgnoreCase(email);

        if (userOpt.isEmpty() || !passwordEncoder.matches(req.getPassword(), userOpt.get().getPasswordHash())) {
            rateLimiterService.loginFailed(rateLimitKey);
            logRepository.save(new VerificationLog(LocalDateTime.now(), null, email,
                    "LOGIN_FAILED", email, "ANONYMOUS", ipAddress, "FAILED", "Invalid credentials"));
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid email or password.");
        }

        User user = userOpt.get();

        if ("PENDING".equalsIgnoreCase(user.getApprovalStatus())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "Your Institution Administrator account is pending approval by AICTE Regulatory Bureau. You will receive access once your college registration is verified.");
        }

        if ("REJECTED".equalsIgnoreCase(user.getApprovalStatus()) || !user.isEnabled()) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "Your account has been deactivated or rejected by AICTE administration.");
        }

        rateLimiterService.loginSucceeded(rateLimitKey);

        String token = tokenProvider.createToken(user.getId(), user.getEmail(), user.getRole(),
                user.getFullName(), user.getInstitutionId(), user.getApprovalStatus());

        logRepository.save(new VerificationLog(LocalDateTime.now(), null, user.getFullName(),
                "LOGIN_SUCCESS", user.getEmail(), user.getRole(), ipAddress, "SUCCESS", "Role: " + user.getRole()));

        return new AuthResponse(true, token, user.getId(), user.getEmail(), user.getFullName(),
                user.getRole(), user.getInstitutionId(), user.getApprovalStatus(), "Login successful");
    }

    @Transactional
    public AuthResponse register(RegisterRequest req, String ipAddress) {
        if (req.getEmail() == null || req.getPassword() == null || req.getFullName() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Name, email, and password are required.");
        }

        String email = req.getEmail().trim().toLowerCase();

        if (!EMAIL_PATTERN.matcher(email).matches()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid email address format.");
        }

        if (!PASSWORD_PATTERN.matcher(req.getPassword()).matches()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Password must be at least 8 characters long and contain both letters and digits.");
        }

        if (req.getConfirmPassword() != null && !req.getPassword().equals(req.getConfirmPassword())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Password confirmation does not match.");
        }

        if (userRepository.existsByEmailIgnoreCase(email)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "An account with this email address already exists. Please sign in or use password reset.");
        }

        String requestedRole = req.getRole() != null ? req.getRole().trim().toUpperCase() : "STUDENT";

        // Security requirement: Public signup must NEVER grant AICTE_ADMIN
        if ("AICTE_ADMIN".equals(requestedRole) || "AICTE_SUPER_ADMIN".equals(requestedRole) || requestedRole.contains("AICTE")) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "AICTE Administrator accounts cannot be created via public registration. Contact AICTE Headquarters.");
        }

        String role = "STUDENT";
        String approvalStatus = "APPROVED";
        String institutionId = null;

        if ("INSTITUTION_ADMIN".equals(requestedRole) || "COLLEGE_ADMIN".equals(requestedRole)) {
            role = "INSTITUTION_ADMIN";
            approvalStatus = "PENDING"; // Must remain pending until AICTE administrator approves

            institutionId = req.getInstitutionId();
            if (institutionId == null || institutionId.trim().isEmpty()) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "Institution selection is required for Institutional Administrator registration.");
            }
            if (!collegeRepository.existsById(institutionId.trim())) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "Specified institution code not found in AICTE approved database.");
            }
        } else if ("VERIFIER".equals(requestedRole)) {
            role = "VERIFIER";
            approvalStatus = "APPROVED";
        }

        String passwordHash = passwordEncoder.encode(req.getPassword());
        User user = new User(email, passwordHash, req.getFullName().trim(), role, institutionId, approvalStatus);
        user = userRepository.save(user);

        logRepository.save(new VerificationLog(LocalDateTime.now(), null, user.getFullName(),
                "USER_REGISTERED", user.getEmail(), user.getRole(), ipAddress, "SUCCESS",
                "Role: " + role + ", Status: " + approvalStatus));

        if ("PENDING".equals(approvalStatus)) {
            return new AuthResponse(true, null, user.getId(), user.getEmail(), user.getFullName(),
                    user.getRole(), user.getInstitutionId(), user.getApprovalStatus(),
                    "Registration submitted! Your Institution Administrator account is pending AICTE regulatory approval.");
        }

        String token = tokenProvider.createToken(user.getId(), user.getEmail(), user.getRole(),
                user.getFullName(), user.getInstitutionId(), user.getApprovalStatus());

        return new AuthResponse(true, token, user.getId(), user.getEmail(), user.getFullName(),
                user.getRole(), user.getInstitutionId(), user.getApprovalStatus(),
                "Account created successfully");
    }

    public AuthResponse googleLogin(GoogleLoginRequest req, String ipAddress) {
        if (googleClientId == null || googleClientId.trim().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE,
                    "Google OAuth 2.0 is not configured on this server. Please configure GOOGLE_CLIENT_ID environment variable or sign in with email/password.");
        }

        if (req.getIdToken() == null || req.getIdToken().trim().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Google ID token is required.");
        }

        try {
            // Verify ID Token securely against Google's tokeninfo API
            String verifyUrl = "https://oauth2.googleapis.com/tokeninfo?id_token=" + req.getIdToken();
            @SuppressWarnings("unchecked")
            Map<String, Object> tokenInfo = restTemplate.getForObject(verifyUrl, Map.class);

            if (tokenInfo == null || !googleClientId.equals(tokenInfo.get("aud"))) {
                throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid Google token audience.");
            }

            String email = (String) tokenInfo.get("email");
            String sub = (String) tokenInfo.get("sub");
            String name = (String) tokenInfo.get("name");
            Object emailVerified = tokenInfo.get("email_verified");

            boolean isVerified = "true".equals(String.valueOf(emailVerified));
            if (!isVerified || email == null) {
                throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Unverified Google email address.");
            }

            Optional<User> userOpt = userRepository.findByGoogleSub(sub)
                    .or(() -> userRepository.findByEmailIgnoreCase(email));

            User user;
            if (userOpt.isPresent()) {
                user = userOpt.get();
                if (user.getGoogleSub() == null) {
                    user.setGoogleSub(sub);
                    userRepository.save(user);
                }

                if ("PENDING".equalsIgnoreCase(user.getApprovalStatus())) {
                    throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                            "Your Institution Administrator account is pending AICTE approval.");
                }
            } else {
                // First-time Google user defaults to STUDENT (never grants admin role)
                user = new User(email, passwordEncoder.encode(UUID.randomUUID().toString()),
                        name != null ? name : "Google User", "STUDENT", null, "APPROVED");
                user.setGoogleSub(sub);
                user = userRepository.save(user);
            }

            String token = tokenProvider.createToken(user.getId(), user.getEmail(), user.getRole(),
                    user.getFullName(), user.getInstitutionId(), user.getApprovalStatus());

            logRepository.save(new VerificationLog(LocalDateTime.now(), null, user.getFullName(),
                    "GOOGLE_LOGIN_SUCCESS", user.getEmail(), user.getRole(), ipAddress, "SUCCESS", "sub: " + sub));

            return new AuthResponse(true, token, user.getId(), user.getEmail(), user.getFullName(),
                    user.getRole(), user.getInstitutionId(), user.getApprovalStatus(), "Google authentication successful");

        } catch (ResponseStatusException e) {
            throw e;
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Google OAuth authentication failed: " + e.getMessage());
        }
    }

    @Transactional
    public Map<String, Object> forgotPassword(ForgotPasswordRequest req, String ipAddress) {
        if (req.getEmail() == null || req.getEmail().trim().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Email is required.");
        }

        String email = req.getEmail().trim().toLowerCase();
        Optional<User> userOpt = userRepository.findByEmailIgnoreCase(email);

        String resetToken = null;
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            resetTokenRepository.deleteByUser(user);

            resetToken = UUID.randomUUID().toString();
            PasswordResetToken tokenEntity = new PasswordResetToken(resetToken, user, LocalDateTime.now().plusMinutes(30));
            resetTokenRepository.save(tokenEntity);

            logRepository.save(new VerificationLog(LocalDateTime.now(), null, user.getFullName(),
                    "PASSWORD_RESET_TOKEN_GENERATED", email, user.getRole(), ipAddress, "SUCCESS",
                    "Token valid for 30 minutes"));
        }

        return Map.of(
                "success", true,
                "message", "If an account exists with this email, a time-limited password reset token has been dispatched.",
                "demoResetToken", resetToken != null ? resetToken : "" // Provided for demo/testing environments
        );
    }

    @Transactional
    public Map<String, Object> resetPassword(ResetPasswordRequest req, String ipAddress) {
        if (req.getToken() == null || req.getNewPassword() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Token and new password are required.");
        }

        if (!PASSWORD_PATTERN.matcher(req.getNewPassword()).matches()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Password must be at least 8 characters long and contain both letters and digits.");
        }

        if (req.getConfirmPassword() != null && !req.getNewPassword().equals(req.getConfirmPassword())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Password confirmation does not match.");
        }

        PasswordResetToken token = resetTokenRepository.findByTokenAndUsedFalse(req.getToken().trim())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid or expired password reset token."));

        if (token.isExpired()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Password reset token has expired.");
        }

        User user = token.getUser();
        user.setPasswordHash(passwordEncoder.encode(req.getNewPassword()));
        userRepository.save(user);

        token.setUsed(true);
        resetTokenRepository.save(token);

        logRepository.save(new VerificationLog(LocalDateTime.now(), null, user.getFullName(),
                "PASSWORD_RESET_SUCCESS", user.getEmail(), user.getRole(), ipAddress, "SUCCESS", "Password updated"));

        return Map.of("success", true, "message", "Password has been successfully updated. You may now sign in.");
    }

    public User getCurrentUser(String email) {
        return userRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
    }
}
