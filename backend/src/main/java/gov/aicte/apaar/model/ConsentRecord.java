package gov.aicte.apaar.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "consent_records")
public class ConsentRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 32)
    private String apaarId;

    @Column(length = 255)
    private String purpose;

    private Boolean consentGiven;
    private LocalDateTime timestamp;
    private String ipAddress;

    public ConsentRecord() {}

    public ConsentRecord(String apaarId, String purpose, Boolean consentGiven, String ipAddress) {
        this.apaarId = apaarId;
        this.purpose = purpose;
        this.consentGiven = consentGiven;
        this.timestamp = LocalDateTime.now();
        this.ipAddress = ipAddress;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getApaarId() { return apaarId; }
    public void setApaarId(String apaarId) { this.apaarId = apaarId; }

    public String getPurpose() { return purpose; }
    public void setPurpose(String purpose) { this.purpose = purpose; }

    public Boolean getConsentGiven() { return consentGiven; }
    public void setConsentGiven(Boolean consentGiven) { this.consentGiven = consentGiven; }

    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }

    public String getIpAddress() { return ipAddress; }
    public void setIpAddress(String ipAddress) { this.ipAddress = ipAddress; }
}
