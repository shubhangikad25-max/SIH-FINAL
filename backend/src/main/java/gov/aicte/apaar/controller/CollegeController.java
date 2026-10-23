package gov.aicte.apaar.controller;

import gov.aicte.apaar.model.DuplicateConflict;
import gov.aicte.apaar.model.Student;
import gov.aicte.apaar.model.User;
import gov.aicte.apaar.repository.DuplicateConflictRepository;
import gov.aicte.apaar.repository.StudentRepository;
import gov.aicte.apaar.repository.UserRepository;
import gov.aicte.apaar.service.CollegeService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/college")
@CrossOrigin(origins = "*")
public class CollegeController {

    private final StudentRepository studentRepository;
    private final CollegeService collegeService;
    private final DuplicateConflictRepository duplicateConflictRepository;
    private final UserRepository userRepository;

    public CollegeController(StudentRepository studentRepository,
                             CollegeService collegeService,
                             DuplicateConflictRepository duplicateConflictRepository,
                             UserRepository userRepository) {
        this.studentRepository = studentRepository;
        this.collegeService = collegeService;
        this.duplicateConflictRepository = duplicateConflictRepository;
        this.userRepository = userRepository;
    }

    private String resolveAuthorizedCollegeId(Authentication auth, String requestedCollegeId) {
        if (auth != null && auth.isAuthenticated()) {
            Optional<User> userOpt = userRepository.findByEmailIgnoreCase(auth.getName());
            if (userOpt.isPresent()) {
                User user = userOpt.get();
                // If Institution Admin, strictly enforce institutionId from database account (prevent IDOR)
                if ("INSTITUTION_ADMIN".equalsIgnoreCase(user.getRole()) || "COLLEGE_ADMIN".equalsIgnoreCase(user.getRole())) {
                    if (user.getInstitutionId() != null && !user.getInstitutionId().isEmpty()) {
                        if (requestedCollegeId != null && !requestedCollegeId.equalsIgnoreCase(user.getInstitutionId())) {
                            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                                    "Access Denied: You are only authorized to manage institution: " + user.getInstitutionId());
                        }
                        return user.getInstitutionId();
                    }
                }
            }
        }
        return (requestedCollegeId != null && !requestedCollegeId.isEmpty()) ? requestedCollegeId : "COL001";
    }

    @GetMapping("/stats")
    public ResponseEntity<?> getCollegeStats(
            @RequestParam(required = false) String collegeId,
            Authentication auth
    ) {
        String effectiveCollegeId = resolveAuthorizedCollegeId(auth, collegeId);
        return ResponseEntity.ok(collegeService.getCollegeStats(effectiveCollegeId));
    }

    @GetMapping("/students")
    public ResponseEntity<?> getStudents(
            @RequestParam(required = false) String collegeId,
            @RequestParam(required = false) String q,
            @RequestParam(required = false) String course,
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String category,
            Authentication auth
    ) {
        String effectiveCollegeId = resolveAuthorizedCollegeId(auth, collegeId);

        List<Student> students = (q != null && !q.trim().isEmpty())
                ? studentRepository.searchStudents(q.trim())
                : studentRepository.findAll();

        // Enforce institution boundary
        students = students.stream()
                .filter(s -> effectiveCollegeId.equalsIgnoreCase(s.getCollegeId()))
                .collect(Collectors.toList());

        if (course != null && !"all".equalsIgnoreCase(course)) {
            students = students.stream().filter(s -> s.getCourse() != null && s.getCourse().toLowerCase().contains(course.toLowerCase())).collect(Collectors.toList());
        }

        if (year != null) {
            students = students.stream().filter(s -> year.equals(s.getAcademicYear())).collect(Collectors.toList());
        }

        if (status != null && !"all".equalsIgnoreCase(status)) {
            students = students.stream().filter(s -> s.getStatus() != null && status.equalsIgnoreCase(s.getStatus())).collect(Collectors.toList());
        }

        if (category != null && !"all".equalsIgnoreCase(category)) {
            students = students.stream().filter(s -> s.getCategory() != null && category.equalsIgnoreCase(s.getCategory())).collect(Collectors.toList());
        }

        return ResponseEntity.ok(Map.of(
                "success", true,
                "total", students.size(),
                "data", students
        ));
    }

    @PostMapping("/students")
    public ResponseEntity<?> addStudent(@RequestBody Student student, Authentication auth) {
        String effectiveCollegeId = resolveAuthorizedCollegeId(auth, student.getCollegeId());
        student.setCollegeId(effectiveCollegeId);

        if (student.getApaarId() == null || !student.getApaarId().matches("^\\d{12}$")) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "error", "INVALID_APAAR_FORMAT",
                    "message", "Student APAAR ID must be an authentic 12-digit numeric identifier."
            ));
        }

        if (student.getQrVerificationToken() == null) {
            student.setQrVerificationToken(UUID.randomUUID().toString().replace("-", ""));
        }

        Student saved = studentRepository.save(student);
        return ResponseEntity.ok(Map.of("success", true, "data", saved));
    }

    @PostMapping("/bulk-upload")
    public ResponseEntity<?> bulkUpload(
            @RequestBody List<Student> incoming,
            @RequestParam(required = false) String collegeId,
            Authentication auth
    ) {
        String effectiveCollegeId = resolveAuthorizedCollegeId(auth, collegeId);
        String actor = auth != null ? auth.getName() : "college_admin";
        Map<String, Object> result = collegeService.processStudentBatch(effectiveCollegeId, incoming, actor, "json_batch_upload");
        return ResponseEntity.ok(result);
    }

    @PostMapping("/upload-file")
    public ResponseEntity<?> uploadFile(
            @RequestParam("file") MultipartFile file,
            @RequestParam(required = false) String collegeId,
            Authentication auth
    ) {
        String effectiveCollegeId = resolveAuthorizedCollegeId(auth, collegeId);
        String actor = auth != null ? auth.getName() : "college_admin";

        try {
            Map<String, Object> result = collegeService.parseAndProcessCsv(effectiveCollegeId, file, actor);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "error", "FILE_PROCESSING_FAILED",
                    "message", "Failed to process upload file: " + e.getMessage()
            ));
        }
    }

    @GetMapping("/duplicate-conflicts")
    public ResponseEntity<?> getDuplicateConflicts(
            @RequestParam(required = false) String collegeId,
            Authentication auth
    ) {
        String effectiveCollegeId = resolveAuthorizedCollegeId(auth, collegeId);
        List<DuplicateConflict> list = duplicateConflictRepository.findByPrimaryCollegeIdOrderByDetectedAtDesc(effectiveCollegeId);
        return ResponseEntity.ok(Map.of("success", true, "total", list.size(), "data", list));
    }

    @PostMapping("/duplicate-conflicts/{id}/resolve")
    public ResponseEntity<?> resolveConflict(
            @PathVariable Long id,
            @RequestBody Map<String, String> body,
            Authentication auth
    ) {
        DuplicateConflict conflict = duplicateConflictRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Conflict record not found"));

        String notes = body.getOrDefault("notes", "Resolved after institutional cross-verification");
        conflict.setStatus("RESOLVED");
        conflict.setResolvedAt(LocalDateTime.now());
        conflict.setResolvedBy(auth != null ? auth.getName() : "admin");
        conflict.setResolutionNotes(notes);
        duplicateConflictRepository.save(conflict);

        return ResponseEntity.ok(Map.of("success", true, "message", "Conflict successfully resolved", "data", conflict));
    }
}
