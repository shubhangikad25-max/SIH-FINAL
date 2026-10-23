package gov.aicte.apaar;

import com.fasterxml.jackson.databind.ObjectMapper;
import gov.aicte.apaar.dto.GoogleLoginRequest;
import gov.aicte.apaar.dto.LoginRequest;
import gov.aicte.apaar.dto.RegisterRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class AuthenticationAndSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("1. Successful email/password login for approved College Admin")
    public void testSuccessfulLogin() throws Exception {
        LoginRequest req = new LoginRequest("college@aicte.ac.in", "123456");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.token").isString())
                .andExpect(jsonPath("$.role").value("INSTITUTION_ADMIN"))
                .andExpect(jsonPath("$.approvalStatus").value("APPROVED"));
    }

    @Test
    @DisplayName("2. Login failure with incorrect password")
    public void testIncorrectPasswordLogin() throws Exception {
        LoginRequest req = new LoginRequest("college@aicte.ac.in", "wrongpassword");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("3. Pending institution administrator access is denied")
    public void testPendingInstitutionAccessDenial() throws Exception {
        LoginRequest req = new LoginRequest("pending.college@aicte.ac.in", "123456");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message", containsString("pending approval")));
    }

    @Test
    @DisplayName("4. Public registration attempting to assign AICTE_ADMIN role is blocked")
    public void testPublicRegistrationBlockAicteAdmin() throws Exception {
        RegisterRequest req = new RegisterRequest();
        req.setEmail("hacker@malicious.in");
        req.setFullName("Malicious Attacker");
        req.setPassword("Password123!");
        req.setConfirmPassword("Password123!");
        req.setRole("AICTE_ADMIN");

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message", containsString("AICTE Administrator accounts cannot be created via public registration")));
    }

    @Test
    @DisplayName("5. Duplicate registration prevention")
    public void testDuplicateRegistration() throws Exception {
        RegisterRequest req = new RegisterRequest();
        req.setEmail("college@aicte.ac.in"); // Already seeded in database
        req.setFullName("Duplicate User");
        req.setPassword("Password123!");
        req.setConfirmPassword("Password123!");
        req.setRole("STUDENT");

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message", containsString("already exists")));
    }

    @Test
    @DisplayName("6. Unconfigured Google OAuth returns clear configuration error without mock fake login")
    public void testGoogleLoginUnconfigured() throws Exception {
        GoogleLoginRequest req = new GoogleLoginRequest();
        req.setIdToken("dummy_fake_token");

        mockMvc.perform(post("/api/auth/google")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isServiceUnavailable())
                .andExpect(jsonPath("$.message", containsString("Google OAuth 2.0 is not configured on this server")));
    }

    @Test
    @DisplayName("7. Protected AICTE regulatory endpoint denies access to unauthenticated requests")
    public void testAicteEndpointUnauthenticatedAccess() throws Exception {
        mockMvc.perform(get("/api/aicte/pending-institutions"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("8. College endpoint denies access without valid bearer token")
    public void testCollegeEndpointUnauthenticatedAccess() throws Exception {
        mockMvc.perform(get("/api/college/stats?collegeId=COL001"))
                .andExpect(status().isForbidden());
    }
}
