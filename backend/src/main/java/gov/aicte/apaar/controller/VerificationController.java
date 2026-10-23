package gov.aicte.apaar.controller;

import gov.aicte.apaar.model.Student;
import gov.aicte.apaar.service.ApaarVerificationService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/verify")
@CrossOrigin(origins = "*")
public class VerificationController {

    private final ApaarVerificationService verificationService;

    public VerificationController(ApaarVerificationService verificationService) {
        this.verificationService = verificationService;
    }

    /**
     * GET /api/verify/{apaarId}
     * Real-time APAAR verification with 12-digit format check, DPDP statutory consent, and distinct outcomes.
     */
    @GetMapping("/{apaarId}")
    public ResponseEntity<?> verifyApaar(
            @PathVariable String apaarId,
            @RequestParam(defaultValue = "true") boolean consent,
            Authentication auth,
            HttpServletRequest request
    ) {
        String actor = (auth != null && auth.isAuthenticated()) ? auth.getName() : "public_student_portal";
        String role = (auth != null && auth.isAuthenticated() && !auth.getAuthorities().isEmpty())
                ? auth.getAuthorities().iterator().next().getAuthority() : "STUDENT";
        String ip = extractClientIp(request);

        Map<String, Object> result = verificationService.verifyApaarRecord(apaarId, consent, actor, role, ip);

        boolean success = Boolean.TRUE.equals(result.get("success"));
        if (!success) {
            String outcome = (String) result.get("outcome");
            if ("INVALID_IDENTIFIER_FORMAT".equals(outcome) || "CONSENT_REQUIRED".equals(outcome)) {
                return ResponseEntity.badRequest().body(result);
            }
            if ("RECORD_NOT_FOUND".equals(outcome)) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(result);
            }
        }

        return ResponseEntity.ok(result);
    }

    /**
     * GET /api/verify/qr/{token}
     * Opaque QR verification token lookup. Never embeds sensitive raw student data directly in QR codes.
     */
    @GetMapping("/qr/{token}")
    public ResponseEntity<?> verifyQr(@PathVariable String token) {
        Map<String, Object> result = verificationService.verifyQrToken(token);
        boolean success = Boolean.TRUE.equals(result.get("success"));
        if (!success) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(result);
        }
        return ResponseEntity.ok(result);
    }

    /**
     * POST /api/verify/{apaarId}/status
     * Allows College Admin or AICTE Admin to approve/reject student status
     */
    @PostMapping("/{apaarId}/status")
    public ResponseEntity<?> updateStatus(
            @PathVariable String apaarId,
            @RequestBody Map<String, String> body,
            Authentication auth,
            HttpServletRequest request
    ) {
        String status = body.get("status");
        String actor = (auth != null && auth.isAuthenticated()) ? auth.getName() : body.get("actorEmail");
        String role = (auth != null && auth.isAuthenticated() && !auth.getAuthorities().isEmpty())
                ? auth.getAuthorities().iterator().next().getAuthority() : body.get("role");
        String ip = extractClientIp(request);

        if (status == null || (!status.equalsIgnoreCase("verified") && !status.equalsIgnoreCase("rejected"))) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "error", "Status must be 'verified' or 'rejected'"));
        }

        try {
            Student updated = verificationService.updateStudentStatus(apaarId, status, actor, role, ip);
            return ResponseEntity.ok(Map.of("success", true, "data", updated));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("success", false, "error", e.getMessage()));
        }
    }

    private String extractClientIp(HttpServletRequest request) {
        String xf = request.getHeader("X-Forwarded-For");
        if (xf != null && !xf.isEmpty()) {
            return xf.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
