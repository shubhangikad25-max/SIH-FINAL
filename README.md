# APAAR Verify: AICTE Student Data Management Platform
**Problem Statement ID:** 26263  
**Title:** Development of an APAAR ID-Based Student Data Management and Verification Platform for AICTE-Approved Institutions  
**Organization:** All India Council for Technical Education (AICTE), Ministry of Education, Government of India  
**Department:** Regulation Bureau  
**Theme:** Smart Automation (SIH26263)  
**Architecture:** Enterprise e-Governance Stack (Java 25/21 + Spring Boot 3.3.5 + Spring Security 6 + Angular 21 + Angular Material + PostgreSQL 16 / Dynamic Multi-Mode Datasource + Apache POI 5.2.5 + Apache Kafka)

---

## 🌐 Live Deployments & Endpoints

| Component | Cloud Platform | Live Deployment URL | Health Check / Status |
| :--- | :--- | :--- | :---: |
| **Frontend Portal** | Render Static Cloud | **[https://aict-apaar-verify.onrender.com](https://aict-apaar-verify.onrender.com)** | 🟢 `Active (200 OK)` |
| **Backend REST API** | Render Docker Cloud | **[https://apaar-backend.onrender.com](https://apaar-backend.onrender.com)** | 🟢 `Active (200 OK)` |
| **API Health Endpoint** | Spring Boot 3 | `https://apaar-backend.onrender.com/api/public/health` | 🟢 `{"status":"UP"}` |

---

## 🏛️ System Overview

**APAAR Verify** is the national academic data verification and regulatory intake compliance platform mandated under the **National Education Policy (NEP 2020)** and **DigiLocker / Academic Bank of Credits (ABC)** guidelines. It establishes the 12-digit **APAAR ID (Automated Permanent Academic Account Registry)** as the verifiable academic identity across all AICTE-approved technical institutions.

### Core Capabilities
1. **Authentic Authentication & Authorization:**
   - Spring Security 6 with stateless JWT Bearer tokens and BCrypt password encryption.
   - Genuine **Continue with Google** OAuth 2.0 / OpenID Connect (OIDC) flow validating token authenticity on the backend.
   - Brute-force rate limiting (5 consecutive failed attempts lock the account for 15 minutes).
   - Time-limited, single-use cryptographic password reset tokens (30-minute validity).
   - **Role-Based Access Control (RBAC):** `ROLE_STUDENT`, `ROLE_INSTITUTION_ADMIN`, `ROLE_AICTE_ADMIN`, `ROLE_VERIFIER`.
   - **Insecure Direct Object Reference (IDOR) Protection:** Institution Admins can strictly view and manage their authorized institution's records.
   - **Pending Approval Workflow:** New institutional administrator registrations remain in `PENDING` state until formally approved by an AICTE Super Admin. Public registration is prohibited from acquiring `AICTE_ADMIN`.

2. **Persistent PostgreSQL Database & Multi-Mode Datasource:**
   - Dynamic connection pool parser for Render's `DATABASE_URL` with HikariCP.
   - Automatic zero-setup embedded H2 fallback for instant local development and test execution without external dependencies.
   - Relational entities: Users, Roles, Colleges, Students, Duplicate Conflicts, DPDP Consent Records, Opaque QR Verification Tokens, Upload Jobs, Verification Audit Logs, and Password Reset Tokens.

3. **Realistic 12-Digit APAAR Verification:**
   - Strict validation matching the official 12-digit numeric identifier specification (`^\d{12}$`).
   - Verifiable outcomes: `INVALID_IDENTIFIER_FORMAT`, `CONSENT_REQUIRED`, `RECORD_NOT_FOUND`, `VERIFICATION_PENDING`, `DUPLICATE_CONFLICT`, and `DEMO_VERIFICATION`.
   - DPDP Act 2023 statutory consent capture before data disclosure.
   - Cryptographic QR seal backed by opaque random verification tokens without leaking raw student PII.

4. **Intake Cap & Supernumerary Quota Interception:**
   - Reconciles actual enrolled students against sanctioned intake limits.
   - Audits 5% Tuition Fee Waiver (TFW) supernumerary quota allocations.
   - Intercepts over-intake violations in real time with dynamic variance calculations.

5. **Cross-Institutional Duplicate Detection & Conflict Resolution:**
   - Flags simultaneous admissions across state boundaries.
   - Publishes duplicate seat anomalies asynchronously to Apache Kafka topic `aicte.apaar.duplicate-events`.
   - Provides institutional dispute review and persistent resolution trails.

6. **Real CSV Batch Ingestion:**
   - Multipart file upload with row-by-row validation, duplicate checks, and intake cap compliance.

7. **Official Government-Grade Reporting:**
   - Server-side spreadsheet generation using **Apache POI 5.2.5** (`.xlsx`).

8. **AICTE Sahayak (सहायक) Virtual Assistant:**
   - Connected directly to backend API (`POST /api/public/sahayak-query`) with role enforcement and privacy safeguards.

---

## 🔐 Built-in Seed Accounts for SIH Evaluation

The database seeds authentic records for instant testing of all roles:

| Role | Email Address | Password | Permissions & Scope |
| :--- | :--- | :--- | :--- |
| **AICTE Super Admin** | `aicte.admin@gov.in` | `admin123` | National regulator: approve colleges, national intake stats, Excel export |
| **Institution Admin (Approved)** | `college@aicte.ac.in` | `123456` | Delhi Institute of Engineering & Technology (Inst ID: `COL001`) |
| **Institution Admin (Pending)** | `pending.college@aicte.ac.in` | `123456` | Awaiting approval from AICTE Admin (Login correctly denied until approved) |
| **Verified Student** | `student@aicte.ac.in` | `123456` | Student verification profile and cryptographic academic pass |
| **Credential Verifier** | `verifier@aicte.ac.in` | `123456` | Verifies opaque QR tokens for employer / university checks |

---

## 🚀 Running the Project Locally

### Prerequisites
- **Java:** JDK 21 or higher (`java -version`)
- **Node.js:** v18.x, v20.x, or v22+ (`node -v`)

---

### Step 1: Start the Spring Boot Backend (Port 8085)
```bash
cd backend
# On Windows:
$env:JAVA_HOME="<path-to-jdk-21-or-25>" # if not already in system PATH
.\mvnw.cmd spring-boot:run

# Or run the packaged fat JAR:
java -jar target/apaar-verify-backend-1.0.0.jar
```
*The Spring Boot server initializes the database with seeded institutions and students, configures Spring Security with JWT filters, starts on port `8085`, and prepares the Apache POI Excel report engine.*

---

### Step 2: Start the Angular Frontend (Port 4200)
```bash
cd frontend
npm start
```
*The Angular development server will compile and serve the portal at **`http://localhost:4200`**.*

---

## 🧪 Testing Workflows

### 1. Student APAAR Verification (`/student`)
1. Navigate to **`http://localhost:4200/student`**.
2. Select any authentic 12-digit preset:
   - **`123456789012` (Verified):** Aarav Sharma (B.Tech CSE, 96 ABC Credits, DIET Delhi).
   - **`100000000003` (Pending):** Rohan Deshmukh (Mechanical, pending intake audit).
   - **`100000000005` (Duplicate Conflict):** Mohammad Farhan Akhtar (flagged for simultaneous active seat in Delhi and UP).
3. Observe the DPDP Act 2023 statutory consent requirement, verification latency simulation, and the verifiable academic pass with cryptographic QR seal.

### 2. College Admin Portal (`/college`)
1. Navigate to **`http://localhost:4200/college`**.
2. Sign in with:
   - Email: `college@aicte.ac.in`
   - Password: `College@2026!`
3. Verify the **Intake Regulatory Anomaly Alert**: Enrolment (`1,240`) exceeds sanctioned intake (`1,200`) by **+40 students**.
4. Test **Continue with Google**: Displays genuine OAuth flow or server-configured message.
5. In the **Student Registry**, filter by status, test real-time status updates, resolve duplicate conflicts with notes, and upload CSV student rosters.

### 3. AICTE Super Admin Portal (`/aicte`)
1. Navigate to **`http://localhost:4200/aicte`**.
2. Sign in with:
   - Email: `aicte.admin@aicte-india.org`
   - Password: `Admin@2026!`
3. Review national intake variance compliance tables.
4. Review **Pending Institution Administrator Approvals**: Approve or reject pending college registrations.
5. Click **Export Apache POI Excel (.xlsx)**: Downloads an authentic `.xlsx` compliance report generated by Spring Boot.

---

## 🛠️ Automated Testing & Build Validation

### Run Backend Unit and Integration Tests:
```bash
cd backend
.\mvnw.cmd test
```
*Executes all 15 test suites covering JWT authentication, rate limiting, IDOR prevention, 12-digit APAAR verification, DPDP consent, and QR token lookup.*

### Run Frontend Tests:
```bash
cd frontend
npm test -- --run
```
*Executes all 8 Vitest unit tests verifying state management, auth interceptors, and components.*

### Build Frontend for Production:
```bash
cd frontend
npm run build
```

---

## 📁 Repository Structure

```text
├── backend/                  # Java 25/21 + Spring Boot 3.3.5 Backend
│   ├── src/main/java/        
│   │   └── gov/aicte/apaar/
│   │       ├── config/       # SecurityConfig, DataSourceConfig, DataInitializer
│   │       ├── controller/   # Auth, Verification, College, AICTE, Public Controllers
│   │       ├── model/        # User, Student, College, DuplicateConflict, ConsentRecord
│   │       ├── repository/   # Spring Data JPA Repositories
│   │       ├── security/     # JwtTokenProvider, JwtAuthenticationFilter, RateLimiterService
│   │       └── service/      # AuthService, ApaarVerificationService, CollegeService, POI Export
│   ├── src/test/java/        # Comprehensive Authentication, Security, and Verification Tests
│   ├── pom.xml               # Spring Boot 3, JJWT, PostgreSQL, Apache POI, Kafka, H2
│   └── mvnw, mvnw.cmd        # Maven Wrapper
├── frontend/                 # Angular 21 + Angular Material Frontend
│   ├── src/app/
│   │   ├── pages/            # Landing, Student, College, AICTE Components
│   │   ├── services/         # ApaarService (JWT Auth, dynamic APIs, session storage)
│   │   └── app.ts            # Root component with Header, Footer, and AICTE Sahayak
│   └── styles.css            # Official AICTE Ministry Design System
├── docker-compose.yml        # Multi-container orchestration (PostgreSQL 16 + Backend)
└── README.md                 # Complete project documentation & testing manual
```
