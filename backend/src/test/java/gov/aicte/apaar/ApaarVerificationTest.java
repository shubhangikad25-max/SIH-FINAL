package gov.aicte.apaar;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class ApaarVerificationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("1. Verification of authentic 12-digit verified record (Aarav Sharma - 123456789012)")
    public void testVerifiedRecordLookup() throws Exception {
        mockMvc.perform(get("/api/verify/123456789012?consent=true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.name").value("Aarav Sharma"))
                .andExpect(jsonPath("$.data.status").value("verified"))
                .andExpect(jsonPath("$.data.abcCredits").value(96))
                .andExpect(jsonPath("$.data.qrVerificationToken").isString());
    }

    @Test
    @DisplayName("2. Verification without DPDP consent returns CONSENT_REQUIRED")
    public void testVerificationWithoutConsent() throws Exception {
        mockMvc.perform(get("/api/verify/123456789012?consent=false"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.outcome").value("CONSENT_REQUIRED"))
                .andExpect(jsonPath("$.message", containsString("DPDP Act 2023")));
    }

    @Test
    @DisplayName("3. Invalid format identifier (non-12 digits) returns INVALID_IDENTIFIER_FORMAT")
    public void testInvalidIdentifierFormat() throws Exception {
        mockMvc.perform(get("/api/verify/INVALID999?consent=true"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.outcome").value("INVALID_IDENTIFIER_FORMAT"))
                .andExpect(jsonPath("$.message", containsString("12-digit numeric identifier")));
    }

    @Test
    @DisplayName("4. Valid 12-digit format but unknown record returns RECORD_NOT_FOUND (404)")
    public void testValidFormatUnknownRecord() throws Exception {
        mockMvc.perform(get("/api/verify/999999999999?consent=true"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.outcome").value("RECORD_NOT_FOUND"))
                .andExpect(jsonPath("$.message", containsString("No student record found")));
    }

    @Test
    @DisplayName("5. Pending verification record returns outcome VERIFICATION_PENDING")
    public void testPendingRecordLookup() throws Exception {
        mockMvc.perform(get("/api/verify/100000000003?consent=true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.outcome").value("VERIFICATION_PENDING"))
                .andExpect(jsonPath("$.data.status").value("pending"));
    }

    @Test
    @DisplayName("6. Duplicate enrolment record returns outcome DUPLICATE_CONFLICT")
    public void testDuplicateConflictLookup() throws Exception {
        mockMvc.perform(get("/api/verify/100000000005?consent=true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.outcome").value("DUPLICATE_CONFLICT"))
                .andExpect(jsonPath("$.data.duplicateCollegeName").isNotEmpty());
    }

    @Test
    @DisplayName("7. QR Verification token lookup works and masks sensitive identifiers")
    public void testQrVerification() throws Exception {
        // First retrieve token from verified student
        String responseBody = mockMvc.perform(get("/api/verify/123456789012?consent=true"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        String token = objectMapper.readTree(responseBody).path("data").path("qrVerificationToken").asText();

        // Query public QR token endpoint
        mockMvc.perform(get("/api/verify/qr/" + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.verified").value(true))
                .andExpect(jsonPath("$.student.name").value("Aarav Sharma"))
                .andExpect(jsonPath("$.student.apaarIdMasked", containsString("········")));
    }
}
