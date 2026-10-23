package gov.aicte.apaar.dto;

public class AuthResponse {
    private boolean success;
    private String token;
    private Long id;
    private String email;
    private String fullName;
    private String role;
    private String institutionId;
    private String approvalStatus;
    private String message;

    public AuthResponse() {}

    public AuthResponse(boolean success, String token, Long id, String email,
                        String fullName, String role, String institutionId,
                        String approvalStatus, String message) {
        this.success = success;
        this.token = token;
        this.id = id;
        this.email = email;
        this.fullName = fullName;
        this.role = role;
        this.institutionId = institutionId;
        this.approvalStatus = approvalStatus;
        this.message = message;
    }

    public static AuthResponse error(String message) {
        AuthResponse res = new AuthResponse();
        res.setSuccess(false);
        res.setMessage(message);
        return res;
    }

    public boolean isSuccess() { return success; }
    public void setSuccess(boolean success) { this.success = success; }

    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public String getInstitutionId() { return institutionId; }
    public void setInstitutionId(String institutionId) { this.institutionId = institutionId; }

    public String getApprovalStatus() { return approvalStatus; }
    public void setApprovalStatus(String approvalStatus) { this.approvalStatus = approvalStatus; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
}
