package gov.aicte.apaar.controller;

import gov.aicte.apaar.model.College;
import gov.aicte.apaar.model.DuplicateConflict;
import gov.aicte.apaar.model.User;
import gov.aicte.apaar.model.VerificationLog;
import gov.aicte.apaar.repository.*;
import gov.aicte.apaar.service.ExcelReportService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/aicte")
@CrossOrigin(origins = "*")
public class AicteController {

    private final CollegeRepository collegeRepository;
    private final StudentRepository studentRepository;
    private final VerificationLogRepository logRepository;
    private final UserRepository userRepository;
    private final DuplicateConflictRepository duplicateConflictRepository;
    private final ExcelReportService excelReportService;

    public AicteController(CollegeRepository collegeRepository,
                           StudentRepository studentRepository,
                           VerificationLogRepository logRepository,
                           UserRepository userRepository,
                           DuplicateConflictRepository duplicateConflictRepository,
                           ExcelReportService excelReportService) {
        this.collegeRepository = collegeRepository;
        this.studentRepository = studentRepository;
        this.logRepository = logRepository;
        this.userRepository = userRepository;
        this.duplicateConflictRepository = duplicateConflictRepository;
        this.excelReportService = excelReportService;
    }

    @GetMapping("/colleges")
    public ResponseEntity<?> getAllColleges() {
        List<College> list = collegeRepository.findAll();
        return ResponseEntity.ok(Map.of(
                "success", true,
                "totalInstitutions", list.size(),
                "activeInstitutions", list.size(),
                "data", list,
                "benchmarkLabel", "[SYNTHETIC DEMO BENCHMARK DATA - AICTE APH 2026-27]"
        ));
    }

    @GetMapping("/stats")
    public ResponseEntity<?> getNationalStats() {
        long totalColleges = collegeRepository.count();
        long totalStudents = studentRepository.count();
        long verifiedStudents = studentRepository.countByStatus("verified");
        long duplicateStudents = studentRepository.countByStatus("duplicate");
        long activeConflicts = duplicateConflictRepository.countByStatus("FLAGGED");

        return ResponseEntity.ok(Map.of(
                "success", true,
                "totalInstitutions", totalColleges,
                "totalEnrolled", totalStudents,
                "verifiedCount", verifiedStudents,
                "duplicateCount", duplicateStudents + activeConflicts,
                "isSyntheticDemoBenchmark", true,
                "demoLabel", "DEMO DATA"
        ));
    }

    @GetMapping("/logs")
    public ResponseEntity<?> getAuditLogs() {
        return ResponseEntity.ok(Map.of(
                "success", true,
                "data", logRepository.findTop50ByOrderByTimestampDesc()
        ));
    }

    @GetMapping("/pending-institutions")
    public ResponseEntity<?> getPendingInstitutions() {
        List<User> pending = userRepository.findByApprovalStatus("PENDING");
        return ResponseEntity.ok(Map.of(
                "success", true,
                "total", pending.size(),
                "data", pending.stream().map(u -> Map.of(
                        "id", u.getId(),
                        "email", u.getEmail(),
                        "fullName", u.getFullName(),
                        "role", u.getRole(),
                        "institutionId", u.getInstitutionId() != null ? u.getInstitutionId() : "",
                        "approvalStatus", u.getApprovalStatus(),
                        "createdAt", u.getCreatedAt() != null ? u.getCreatedAt().toString() : ""
                )).toList()
        ));
    }

    @PostMapping("/institutions/{userId}/approve")
    public ResponseEntity<?> approveInstitution(
            @PathVariable Long userId,
            Authentication auth
    ) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User account not found"));

        user.setApprovalStatus("APPROVED");
        userRepository.save(user);

        logRepository.save(new VerificationLog(LocalDateTime.now(), null, user.getFullName(),
                "INSTITUTION_APPROVED", auth != null ? auth.getName() : "aicte_admin",
                "AICTE_ADMIN", "127.0.0.1", "SUCCESS", "Approved institutionId: " + user.getInstitutionId()));

        return ResponseEntity.ok(Map.of("success", true, "message", "Institution Administrator approved successfully."));
    }

    @PostMapping("/institutions/{userId}/reject")
    public ResponseEntity<?> rejectInstitution(
            @PathVariable Long userId,
            Authentication auth
    ) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User account not found"));

        user.setApprovalStatus("REJECTED");
        userRepository.save(user);

        logRepository.save(new VerificationLog(LocalDateTime.now(), null, user.getFullName(),
                "INSTITUTION_REJECTED", auth != null ? auth.getName() : "aicte_admin",
                "AICTE_ADMIN", "127.0.0.1", "REJECTED", "Rejected institutionId: " + user.getInstitutionId()));

        return ResponseEntity.ok(Map.of("success", true, "message", "Institution Administrator registration rejected."));
    }

    @GetMapping("/duplicate-conflicts")
    public ResponseEntity<?> getAllDuplicateConflicts() {
        List<DuplicateConflict> list = duplicateConflictRepository.findAllByOrderByDetectedAtDesc();
        return ResponseEntity.ok(Map.of("success", true, "total", list.size(), "data", list));
    }

    /**
     * GET /api/aicte/export-excel
     * Uses Apache POI to generate authentic government-standard .xlsx document for download
     */
    @GetMapping("/export-excel")
    public ResponseEntity<byte[]> exportExcelReport() {
        try {
            byte[] excelBytes = excelReportService.generateNationalComplianceReport();

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=AICTE_National_Compliance_Report_2026.xlsx")
                    .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                    .body(excelBytes);
        } catch (IOException e) {
            return ResponseEntity.internalServerError().build();
        }
    }
}
