package gov.aicte.apaar.controller;

import gov.aicte.apaar.model.College;
import gov.aicte.apaar.repository.CollegeRepository;
import gov.aicte.apaar.repository.StudentRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/public")
@CrossOrigin(origins = "*")
public class PublicController {

    private final CollegeRepository collegeRepository;
    private final StudentRepository studentRepository;

    public PublicController(CollegeRepository collegeRepository, StudentRepository studentRepository) {
        this.collegeRepository = collegeRepository;
        this.studentRepository = studentRepository;
    }

    @GetMapping("/health")
    public ResponseEntity<?> health() {
        return ResponseEntity.ok(Map.of(
                "status", "UP",
                "service", "AICTE APAAR Verification Platform",
                "version", "1.0.0",
                "stack", "Java 21 + Spring Boot 3 + Angular 21 + PostgreSQL"
        ));
    }

    @PostMapping("/sahayak/query")
    public ResponseEntity<?> sahayakQuery(@RequestBody Map<String, String> body, Authentication auth) {
        String query = body.getOrDefault("query", "").trim().toLowerCase();
        String reply;

        if (query.contains("apaar") || query.contains("what is")) {
            reply = "Under National Education Policy (NEP 2020), the 12-digit APAAR ID (Automated Permanent Academic Account Registry) serves as the sovereign academic identifier for all students across school and higher technical education, directly synchronizing with DigiLocker and the Academic Bank of Credits (ABC).";
        } else if (query.contains("duplicate") || query.contains("conflict") || query.contains("caught")) {
            reply = "APAAR Verify detects multi-college duplicate admissions by reconciling 12-digit identifiers across state registries in real-time, publishing flagged conflicts to the AICTE Kafka anomaly stream (aicte.apaar.duplicate-events) for regulatory resolution.";
        } else if (query.contains("intake") || query.contains("quota") || query.contains("cap") || query.contains("supernumerary")) {
            reply = "In accordance with AICTE Approval Process Handbook (APH 2026-27), technical institutions may not exceed sanctioned intake except for statutory quotas: up to 5% Tuition Fee Waiver (TFW) supernumerary seats and up to 15% NRI/Foreign National seats.";
        } else if (query.contains("college") || query.contains("enrolment") || query.contains("stats")) {
            if (auth != null && auth.isAuthenticated()) {
                long totalStudents = studentRepository.count();
                long verifiedStudents = studentRepository.countByStatus("verified");
                reply = String.format("Current authenticated database records show %d registered students across approved institutions, with %d verified credentials in the persistent repository.",
                        totalStudents, verifiedStudents);
            } else {
                reply = "Institutional admission metrics and student registry access are protected under role-based authorization. Please sign in with your institutional administrator credentials to review live compliance data.";
            }
        } else if (query.contains("student") || query.contains("marks") || query.contains("profile")) {
            reply = "In compliance with the Digital Personal Data Protection (DPDP) Act 2023, individual student profiles and credit ledgers cannot be disclosed via virtual helpdesk queries. Please use the Student Portal with valid 12-digit APAAR ID and statutory consent.";
        } else {
            reply = "AICTE Sahayak is your automated guide for APAAR ID integration, institutional intake compliance, supernumerary quotas, and cross-state duplicate reconciliation under SIH PS 26263.";
        }

        return ResponseEntity.ok(Map.of(
                "success", true,
                "reply", reply
        ));
    }
}
