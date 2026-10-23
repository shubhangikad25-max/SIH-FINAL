package gov.aicte.apaar.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "duplicate_conflicts")
public class DuplicateConflict {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 32)
    private String apaarId;

    private String studentName;

    @Column(nullable = false, length = 32)
    private String primaryCollegeId;

    private String primaryCollegeName;

    @Column(length = 32)
    private String conflictingCollegeId;

    private String conflictingCollegeName;

    @Column(length = 50)
    private String conflictType; // SIMULTANEOUS_ENROLMENT, INTAKE_EXCEEDED, DUPLICATE_IDENTIFIER

    @Column(length = 500)
    private String flagReason;

    @Column(length = 20)
    private String status = "FLAGGED"; // FLAGGED, RESOLVED, DISMISSED

    private LocalDateTime detectedAt;
    private LocalDateTime resolvedAt;
    private String resolvedBy;
    private String resolutionNotes;

    public DuplicateConflict() {}

    public DuplicateConflict(String apaarId, String studentName, String primaryCollegeId,
                             String primaryCollegeName, String conflictingCollegeId,
                             String conflictingCollegeName, String conflictType, String flagReason) {
        this.apaarId = apaarId;
        this.studentName = studentName;
        this.primaryCollegeId = primaryCollegeId;
        this.primaryCollegeName = primaryCollegeName;
        this.conflictingCollegeId = conflictingCollegeId;
        this.conflictingCollegeName = conflictingCollegeName;
        this.conflictType = conflictType;
        this.flagReason = flagReason;
        this.status = "FLAGGED";
        this.detectedAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getApaarId() { return apaarId; }
    public void setApaarId(String apaarId) { this.apaarId = apaarId; }

    public String getStudentName() { return studentName; }
    public void setStudentName(String studentName) { this.studentName = studentName; }

    public String getPrimaryCollegeId() { return primaryCollegeId; }
    public void setPrimaryCollegeId(String primaryCollegeId) { this.primaryCollegeId = primaryCollegeId; }

    public String getPrimaryCollegeName() { return primaryCollegeName; }
    public void setPrimaryCollegeName(String primaryCollegeName) { this.primaryCollegeName = primaryCollegeName; }

    public String getConflictingCollegeId() { return conflictingCollegeId; }
    public void setConflictingCollegeId(String conflictingCollegeId) { this.conflictingCollegeId = conflictingCollegeId; }

    public String getConflictingCollegeName() { return conflictingCollegeName; }
    public void setConflictingCollegeName(String conflictingCollegeName) { this.conflictingCollegeName = conflictingCollegeName; }

    public String getConflictType() { return conflictType; }
    public void setConflictType(String conflictType) { this.conflictType = conflictType; }

    public String getFlagReason() { return flagReason; }
    public void setFlagReason(String flagReason) { this.flagReason = flagReason; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public LocalDateTime getDetectedAt() { return detectedAt; }
    public void setDetectedAt(LocalDateTime detectedAt) { this.detectedAt = detectedAt; }

    public LocalDateTime getResolvedAt() { return resolvedAt; }
    public void setResolvedAt(LocalDateTime resolvedAt) { this.resolvedAt = resolvedAt; }

    public String getResolvedBy() { return resolvedBy; }
    public void setResolvedBy(String resolvedBy) { this.resolvedBy = resolvedBy; }

    public String getResolutionNotes() { return resolutionNotes; }
    public void setResolutionNotes(String resolutionNotes) { this.resolutionNotes = resolutionNotes; }
}
