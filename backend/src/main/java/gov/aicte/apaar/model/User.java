package gov.aicte.apaar.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false, length = 100)
    private String email;

    @Column(nullable = false)
    private String passwordHash;

    @Column(nullable = false, length = 100)
    private String fullName;

    @Column(nullable = false, length = 30)
    private String role; // STUDENT, INSTITUTION_ADMIN, AICTE_ADMIN, VERIFIER

    @Column(length = 32)
    private String institutionId; // e.g. COL001 for INSTITUTION_ADMIN

    @Column(nullable = false, length = 20)
    private String approvalStatus; // APPROVED, PENDING, REJECTED

    @Column(length = 100)
    private String googleSub;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private boolean enabled = true;

    public User() {}

    public User(String email, String passwordHash, String fullName, String role,
                String institutionId, String approvalStatus) {
        this.email = email != null ? email.toLowerCase().trim() : null;
        this.passwordHash = passwordHash;
        this.fullName = fullName;
        this.role = role;
        this.institutionId = institutionId;
        this.approvalStatus = approvalStatus;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        this.enabled = true;
    }

    @PrePersist
    public void onPrePersist() {
        if (this.createdAt == null) this.createdAt = LocalDateTime.now();
        if (this.updatedAt == null) this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    public void onPreUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email != null ? email.toLowerCase().trim() : null; }

    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public String getInstitutionId() { return institutionId; }
    public void setInstitutionId(String institutionId) { this.institutionId = institutionId; }

    public String getApprovalStatus() { return approvalStatus; }
    public void setApprovalStatus(String approvalStatus) { this.approvalStatus = approvalStatus; }

    public String getGoogleSub() { return googleSub; }
    public void setGoogleSub(String googleSub) { this.googleSub = googleSub; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }
}
