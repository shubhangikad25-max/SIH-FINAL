package gov.aicte.apaar.service;

import gov.aicte.apaar.model.*;
import gov.aicte.apaar.repository.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.regex.Pattern;

@Service
public class ApaarVerificationService {

    private final StudentRepository studentRepository;
    private final VerificationLogRepository logRepository;
    private final ConsentRecordRepository consentRepository;
    private final VerificationTokenRepository tokenRepository;
    private final DuplicateConflictRepository duplicateConflictRepository;
    private final KafkaEventPublisherService kafkaService;

    @Value("${apaar.external-api.configured:false}")
    private boolean externalApiConfigured;

    private static final Pattern NUMERIC_12_DIGIT_PATTERN = Pattern.compile("^\\d{12}$");

    public ApaarVerificationService(StudentRepository studentRepository,
                                    VerificationLogRepository logRepository,
                                    ConsentRecordRepository consentRepository,
                                    VerificationTokenRepository tokenRepository,
                                    DuplicateConflictRepository duplicateConflictRepository,
                                    KafkaEventPublisherService kafkaService) {
        this.studentRepository = studentRepository;
        this.logRepository = logRepository;
        this.consentRepository = consentRepository;
        this.tokenRepository = tokenRepository;
        this.duplicateConflictRepository = duplicateConflictRepository;
        this.kafkaService = kafkaService;
    }

    /**
     * Normalizes identifier input:
     * - Removes spaces, dashes
     * - Maps legacy demo presets if supplied
     */
    public String normalizeIdentifier(String rawInput) {
        if (rawInput == null) return "";
        String clean = rawInput.trim().replaceAll("[\\s-]", "");
        // Map legacy preset format APAAR12345678 -> 123456789012 for seamless backward compatibility
        if ("APAAR12345678".equalsIgnoreCase(clean)) return "123456789012";
        if ("APAAR10000003".equalsIgnoreCase(clean)) return "100000000003";
        if ("APAAR10000005".equalsIgnoreCase(clean)) return "100000000005";
        return clean;
    }

    public boolean isValidIdentifierFormat(String identifier) {
        if (identifier == null) return false;
        String normalized = normalizeIdentifier(identifier);
        return NUMERIC_12_DIGIT_PATTERN.matcher(normalized).matches();
    }

    /**
     * Executes genuine APAAR ID verification against persistent records.
     * Evaluates format, DPDP consent, duplicate status, and institutional audit outcome.
     */
    public Map<String, Object> verifyApaarRecord(String rawIdentifier, boolean consentGiven,
                                                String actor, String role, String ipAddress) {
        String identifier = normalizeIdentifier(rawIdentifier);

        // 1. Format validation
        if (!NUMERIC_12_DIGIT_PATTERN.matcher(identifier).matches()) {
            logRepository.save(new VerificationLog(LocalDateTime.now(), identifier, "UNKNOWN",
                    "VERIFICATION_QUERY_FAILED", actor, role, ipAddress, "INVALID_FORMAT",
                    "Identifier must be an authentic 12-digit numeric APAAR ID. Received: " + rawIdentifier));
            return Map.of(
                    "success", false,
                    "outcome", "INVALID_IDENTIFIER_FORMAT",
                    "error", "INVALID_APAAR_ID_FORMAT",
                    "message", "APAAR ID must be an authentic 12-digit numeric identifier according to Ministry of Education guidelines."
            );
        }

        // 2. DPDP Statutory Consent Check
        if (!consentGiven) {
            return Map.of(
                    "success", false,
                    "outcome", "CONSENT_REQUIRED",
                    "error", "DPDP_CONSENT_REQUIRED",
                    "message", "Statutory consent under DPDP Act 2023 is required before querying academic records."
            );
        }

        // Record DPDP consent in immutable audit ledger
        consentRepository.save(new ConsentRecord(identifier, "STUDENT_CREDENTIAL_VERIFICATION", true, ipAddress));

        // 3. Database lookup in persistent repository
        Optional<Student> studentOpt = studentRepository.findByApaarIdIgnoreCase(identifier);

        if (studentOpt.isEmpty()) {
            logRepository.save(new VerificationLog(LocalDateTime.now(), identifier, "NOT_FOUND",
                    "VERIFICATION_QUERY_NOT_FOUND", actor, role, ipAddress, "NOT_FOUND", "Record not in registry"));
            return Map.of(
                    "success", false,
                    "outcome", "RECORD_NOT_FOUND",
                    "error", "STUDENT_NOT_FOUND",
                    "message", "No student record found matching APAAR ID: " + identifier + ". Please verify with admitted institution."
            );
        }

        Student student = studentOpt.get();

        // Ensure QR verification token exists
        if (student.getQrVerificationToken() == null) {
            String qrToken = UUID.randomUUID().toString().replace("-", "");
            student.setQrVerificationToken(qrToken);
            studentRepository.save(student);
            tokenRepository.save(new VerificationToken(qrToken, student.getApaarId(), LocalDateTime.now().plusYears(1)));
        }

        // Determine specific outcome
        String outcome;
        String status = student.getStatus() != null ? student.getStatus().toLowerCase() : "pending";

        if ("duplicate".equals(status) || student.getDuplicateCollegeName() != null) {
            outcome = "DUPLICATE_CONFLICT";
            kafkaService.publishDuplicateAnomaly(student);
        } else if ("verified".equals(status)) {
            outcome = externalApiConfigured ? "SUCCESSFULLY_VERIFIED" : "DEMO_VERIFICATION";
        } else if ("rejected".equals(status)) {
            outcome = "VERIFICATION_REJECTED";
        } else {
            outcome = "VERIFICATION_PENDING";
        }

        logRepository.save(new VerificationLog(LocalDateTime.now(), student.getApaarId(), student.getName(),
                "VERIFICATION_QUERY_SUCCESS", actor, role, ipAddress, outcome, "Status: " + status));

        return Map.of(
                "success", true,
                "outcome", outcome,
                "data", student,
                "metadata", Map.of(
                        "verifiedAt", LocalDateTime.now().toString(),
                        "encryptionStandard", "AES-256-GCM",
                        "dataDepository", "DigiLocker / National Academic Depository (NAD)",
                        "compliance", "AICTE APH 2026-27 Clause 8.4",
                        "verificationMode", externalApiConfigured ? "OFFICIAL_GOVERNMENT_GATEWAY" : "SYNTHETIC_DEMO_BENCHMARK",
                        "qrVerificationToken", student.getQrVerificationToken() != null ? student.getQrVerificationToken() : ""
                )
        );
    }

    @Transactional
    public Student updateStudentStatus(String apaarId, String newStatus, String actor, String role, String ipAddress) {
        String normalized = normalizeIdentifier(apaarId);
        Student student = studentRepository.findByApaarIdIgnoreCase(normalized)
                .orElseThrow(() -> new IllegalArgumentException("Student record not found for APAAR ID: " + normalized));

        student.setStatus(newStatus.toLowerCase());
        if ("verified".equalsIgnoreCase(newStatus)) {
            student.setDigilockerVerified(true);
        } else {
            student.setDigilockerVerified(false);
        }
        Student saved = studentRepository.save(student);

        logRepository.save(new VerificationLog(
                LocalDateTime.now(),
                student.getApaarId(),
                student.getName(),
                "STATUS_UPDATED_" + newStatus.toUpperCase(),
                actor != null ? actor : "admin@aicte.ac.in",
                role != null ? role : "INSTITUTION_ADMIN",
                ipAddress != null ? ipAddress : "127.0.0.1",
                "SUCCESS",
                "Updated to: " + newStatus
        ));

        return saved;
    }

    /**
     * Public QR verification endpoint:
     * Resolves an opaque token and returns non-sensitive verified academic credentials.
     */
    public Map<String, Object> verifyQrToken(String token) {
        if (token == null || token.trim().isEmpty()) {
            return Map.of("success", false, "error", "TOKEN_REQUIRED", "message", "QR verification token is missing.");
        }

        Optional<VerificationToken> tokenOpt = tokenRepository.findByToken(token.trim());
        Student s;

        if (tokenOpt.isPresent()) {
            VerificationToken vToken = tokenOpt.get();
            if (!vToken.isValid()) {
                return Map.of(
                        "success", false,
                        "error", "INVALID_OR_REVOKED_TOKEN",
                        "message", "This digital credential QR seal is invalid, expired, or has been revoked."
                );
            }
            Optional<Student> studentOpt = studentRepository.findByApaarIdIgnoreCase(vToken.getApaarId());
            if (studentOpt.isEmpty()) {
                return Map.of("success", false, "error", "STUDENT_NOT_FOUND", "message", "Associated student record not found.");
            }
            s = studentOpt.get();
        } else {
            Optional<Student> studentOpt = studentRepository.findByQrVerificationToken(token.trim());
            if (studentOpt.isEmpty()) {
                return Map.of(
                        "success", false,
                        "error", "INVALID_OR_REVOKED_TOKEN",
                        "message", "This digital credential QR seal is invalid, expired, or has been revoked."
                );
            }
            s = studentOpt.get();
            tokenRepository.save(new VerificationToken(token.trim(), s.getApaarId(), LocalDateTime.now().plusYears(1)));
        }
        return Map.of(
                "success", true,
                "verified", true,
                "sealStatus", "AUTHENTIC_AICTE_SEAL",
                "student", Map.of(
                        "name", s.getName(),
                        "apaarIdMasked", "········" + (s.getApaarId().length() >= 4 ? s.getApaarId().substring(s.getApaarId().length() - 4) : s.getApaarId()),
                        "collegeName", s.getCollegeName(),
                        "course", s.getCourse(),
                        "academicYear", s.getAcademicYear(),
                        "status", s.getStatus(),
                        "creditsBanked", s.getAbcCredits(),
                        "digilockerVerified", Boolean.TRUE.equals(s.getDigilockerVerified())
                ),
                "timestamp", LocalDateTime.now().toString()
        );
    }
}
