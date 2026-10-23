package gov.aicte.apaar.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "upload_jobs")
public class UploadJob {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 32)
    private String institutionId;

    private String fileName;
    private Integer totalRows = 0;
    private Integer processedCount = 0;
    private Integer successCount = 0;
    private Integer duplicateCount = 0;
    private Integer errorCount = 0;

    @Column(length = 20)
    private String status = "PROCESSING"; // PROCESSING, COMPLETED, FAILED

    @Column(columnDefinition = "TEXT")
    private String errorSummary;

    private LocalDateTime createdAt;
    private String createdBy;

    public UploadJob() {}

    public UploadJob(String institutionId, String fileName, Integer totalRows, String createdBy) {
        this.institutionId = institutionId;
        this.fileName = fileName;
        this.totalRows = totalRows;
        this.createdBy = createdBy;
        this.createdAt = LocalDateTime.now();
        this.status = "PROCESSING";
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getInstitutionId() { return institutionId; }
    public void setInstitutionId(String institutionId) { this.institutionId = institutionId; }

    public String getFileName() { return fileName; }
    public void setFileName(String fileName) { this.fileName = fileName; }

    public Integer getTotalRows() { return totalRows; }
    public void setTotalRows(Integer totalRows) { this.totalRows = totalRows; }

    public Integer getProcessedCount() { return processedCount; }
    public void setProcessedCount(Integer processedCount) { this.processedCount = processedCount; }

    public Integer getSuccessCount() { return successCount; }
    public void setSuccessCount(Integer successCount) { this.successCount = successCount; }

    public Integer getDuplicateCount() { return duplicateCount; }
    public void setDuplicateCount(Integer duplicateCount) { this.duplicateCount = duplicateCount; }

    public Integer getErrorCount() { return errorCount; }
    public void setErrorCount(Integer errorCount) { this.errorCount = errorCount; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getErrorSummary() { return errorSummary; }
    public void setErrorSummary(String errorSummary) { this.errorSummary = errorSummary; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public String getCreatedBy() { return createdBy; }
    public void setCreatedBy(String createdBy) { this.createdBy = createdBy; }
}
