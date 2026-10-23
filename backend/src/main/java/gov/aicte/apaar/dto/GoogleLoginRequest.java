package gov.aicte.apaar.dto;

public class GoogleLoginRequest {
    private String idToken;
    private String code;

    public GoogleLoginRequest() {}

    public String getIdToken() { return idToken; }
    public void setIdToken(String idToken) { this.idToken = idToken; }

    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
}
