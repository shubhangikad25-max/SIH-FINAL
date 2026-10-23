package gov.aicte.apaar.config;

import gov.aicte.apaar.model.*;
import gov.aicte.apaar.repository.*;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Component
public class DataInitializer implements CommandLineRunner {

    private final StudentRepository studentRepository;
    private final CollegeRepository collegeRepository;
    private final VerificationLogRepository logRepository;
    private final UserRepository userRepository;
    private final DuplicateConflictRepository duplicateConflictRepository;
    private final VerificationTokenRepository verificationTokenRepository;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(StudentRepository studentRepository,
                           CollegeRepository collegeRepository,
                           VerificationLogRepository logRepository,
                           UserRepository userRepository,
                           DuplicateConflictRepository duplicateConflictRepository,
                           VerificationTokenRepository verificationTokenRepository,
                           PasswordEncoder passwordEncoder) {
        this.studentRepository = studentRepository;
        this.collegeRepository = collegeRepository;
        this.logRepository = logRepository;
        this.userRepository = userRepository;
        this.duplicateConflictRepository = duplicateConflictRepository;
        this.verificationTokenRepository = verificationTokenRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        // 1. Seed Real User Accounts
        if (userRepository.count() == 0) {
            List<User> users = List.of(
                    new User("college@aicte.ac.in", passwordEncoder.encode("123456"),
                            "Dr. Ramesh Verma (Registrar, DIET)", "INSTITUTION_ADMIN", "COL001", "APPROVED"),
                    new User("aicte.admin@gov.in", passwordEncoder.encode("admin123"),
                            "AICTE Regulation Bureau Director", "AICTE_ADMIN", null, "APPROVED"),
                    new User("pending.college@aicte.ac.in", passwordEncoder.encode("123456"),
                            "Prof. Alok Tripathi (UP Council of Tech)", "INSTITUTION_ADMIN", "COL005", "PENDING"),
                    new User("student@aicte.ac.in", passwordEncoder.encode("123456"),
                            "Aarav Sharma", "STUDENT", null, "APPROVED"),
                    new User("verifier@aicte.ac.in", passwordEncoder.encode("123456"),
                            "National Credential Verifier", "VERIFIER", null, "APPROVED")
            );
            userRepository.saveAll(users);
        }

        // 2. Seed Colleges
        if (collegeRepository.count() == 0) {
            List<College> colleges = List.of(
                    new College("COL001", "Delhi Institute of Engineering & Technology", "AICTE-DL-0012",
                            "Delhi", "New Delhi", 1200, 1240, 980, 200, 60, 45, 24, 8),
                    new College("COL002", "Maharashtra National Institute of Technology", "AICTE-MH-0845",
                            "Maharashtra", "Pune", 420, 418, 380, 28, 10, 21, 12, 6),
                    new College("COL003", "Bangalore Technical & Science Academy", "AICTE-KA-0491",
                            "Karnataka", "Bengaluru", 300, 295, 260, 30, 5, 15, 10, 8),
                    new College("COL004", "Tamil Nadu Engineering & Management Campus", "AICTE-TN-0219",
                            "Tamil Nadu", "Chennai", 480, 472, 440, 24, 8, 24, 15, 10),
                    new College("COL005", "Uttar Pradesh Council of Technology", "AICTE-UP-0672",
                            "Uttar Pradesh", "Lucknow", 500, 515, 410, 85, 20, 25, 6, 2)
            );
            collegeRepository.saveAll(colleges);
        }

        // 3. Seed 50 authentic 12-digit Indian students if empty
        if (studentRepository.count() == 0) {
            List<Student> students = new ArrayList<>();

            students.add(new Student("123456789012", "Aarav Sharma", "2004-05-14", "Male", "+91 98765 43210",
                    "4829", "COL001", "Delhi Institute of Engineering & Technology", "B.Tech Computer Science & Engineering",
                    3, 96, 94.6, 92.4, "verified", "General", "2022-CSE-001", "2022-08-16", true, "Delhi", null, null, null));

            students.add(new Student("100000000002", "Priya Lakshmi Sundaram", "2003-11-22", "Female", "+91 98450 11223",
                    "9102", "COL004", "Tamil Nadu Engineering & Management Campus", "B.Tech Artificial Intelligence & Data Science",
                    4, 128, 96.2, 95.8, "verified", "General", "2021-AIDS-018", "2021-09-01", true, "Tamil Nadu", null, null, null));

            students.add(new Student("100000000003", "Rohan Deshmukh", "2005-02-18", "Male", "+91 97654 32189",
                    "6234", "COL002", "Maharashtra National Institute of Technology", "B.Tech Electronics & Communication",
                    2, 64, 88.5, 86.2, "pending", "Supernumerary (TFW)", "2023-ECE-044", "2023-08-20", false, "Maharashtra", null, null, null));

            students.add(new Student("100000000004", "Aditi Vikram Rao", "2004-09-30", "Female", "+91 94481 23456",
                    "3319", "COL003", "Bangalore Technical & Science Academy", "B.Tech Information Technology",
                    3, 92, 91.0, 89.4, "verified", "General", "2022-IT-012", "2022-08-19", true, "Karnataka", null, null, null));

            students.add(new Student("100000000005", "Mohammad Farhan Akhtar", "2004-07-12", "Male", "+91 98112 34567",
                    "5087", "COL001", "Delhi Institute of Engineering & Technology", "B.Tech Mechanical Engineering",
                    3, 88, 84.0, 82.5, "duplicate", "General", "2022-ME-089", "2022-08-14", true, "Delhi",
                    "Uttar Pradesh Council of Technology", "COL005", "Simultaneous regular enrollment active at UP Council of Technology under UID UP-2022-892"));

            students.add(new Student("100000000006", "Sneha Nair", "2005-04-05", "Female", "+91 94951 88776",
                    "7124", "COL004", "Tamil Nadu Engineering & Management Campus", "B.Tech Computer Science & Engineering",
                    2, 58, 93.8, 92.0, "verified", "Supernumerary (TFW)", "2023-CSE-091", "2023-08-10", true, "Tamil Nadu", null, null, null));

            students.add(new Student("100000000007", "Vikramaditya Roy", "2003-12-08", "Male", "+91 98301 44556",
                    "1983", "COL005", "Uttar Pradesh Council of Technology", "Master of Business Administration (MBA)",
                    2, 52, 89.2, 87.5, "pending", "General", "2023-MBA-015", "2023-07-28", false, "Uttar Pradesh", null, null, null));

            students.add(new Student("100000000008", "Kavya Murthy", "2004-01-25", "Female", "+91 98459 33221",
                    "4472", "COL003", "Bangalore Technical & Science Academy", "B.Tech Robotics & Automation",
                    3, 94, 95.0, 93.8, "verified", "General", "2022-ROB-008", "2022-08-22", true, "Karnataka", null, null, null));

            students.add(new Student("100000000009", "Tariq Ahmad Mir", "2005-06-19", "Male", "+91 94190 22334",
                    "8521", "COL001", "Delhi Institute of Engineering & Technology", "B.Tech Civil Engineering",
                    2, 62, 82.4, 80.6, "pending", "General", "2023-CE-033", "2023-08-11", false, "Delhi", null, null, null));

            students.add(new Student("100000000010", "Meera Krishnan", "2004-10-15", "Female", "+91 94470 66554",
                    "6639", "COL002", "Maharashtra National Institute of Technology", "B.Tech AI & Data Science",
                    3, 98, 97.4, 96.1, "verified", "General", "2022-AIDS-004", "2022-08-15", true, "Maharashtra", null, null, null));

            students.add(new Student("100000000011", "Arjun Reddy", "2004-03-29", "Male", "+91 98490 77889",
                    "2201", "COL004", "Tamil Nadu Engineering & Management Campus", "B.Tech Electrical & Electronics",
                    3, 90, 90.2, 88.6, "verified", "General", "2022-EEE-027", "2022-08-18", true, "Tamil Nadu", null, null, null));

            students.add(new Student("100000000012", "Ananya Banerjee", "2005-08-14", "Female", "+91 98310 99887",
                    "3984", "COL001", "Delhi Institute of Engineering & Technology", "B.Tech Computer Science & Engineering",
                    1, 30, 96.0, 94.2, "duplicate", "General", "2024-CSE-112", "2024-08-01", true, "Delhi",
                    "Bangalore Technical & Science Academy", "COL003", "Dual enrolment recorded across Delhi and Karnataka colleges in AY 2024-25"));

            // Add remaining 38 students with 12-digit numeric IDs
            String[] firstNames = {"Rajesh", "Pooja", "Amit", "Divya", "Suresh", "Kavita", "Manish", "Sunita", "Deepak", "Anjali",
                    "Gaurav", "Swati", "Naveen", "Preeti", "Sanjay", "Ritu", "Alok", "Shweta", "Sachin", "Archana",
                    "Ashok", "Kiran", "Vijay", "Anita", "Sunil", "Rekha", "Manoj", "Usha", "Pradeep", "Geeta",
                    "Ramesh", "Seema", "Dinesh", "Mamta", "Naresh", "Sarita", "Mukesh", "Shashi"};

            for (int i = 0; i < firstNames.length; i++) {
                String id = String.format("1000000000%02d", (i + 13));
                String name = firstNames[i] + " " + (i % 2 == 0 ? "Kumar" : "Singh");
                String gender = (i % 2 == 0) ? "Male" : "Female";
                String col = "COL00" + ((i % 5) + 1);
                String colName = (i % 5 == 0) ? "Delhi Institute of Engineering & Technology" :
                        (i % 5 == 1) ? "Maharashtra National Institute of Technology" :
                        (i % 5 == 2) ? "Bangalore Technical & Science Academy" :
                        (i % 5 == 3) ? "Tamil Nadu Engineering & Management Campus" :
                        "Uttar Pradesh Council of Technology";
                String course = (i % 3 == 0) ? "B.Tech Computer Science & Engineering" :
                        (i % 3 == 1) ? "B.Tech Electronics & Communication" :
                        "B.Tech Information Technology";
                String cat = (i % 8 == 0) ? "Supernumerary (TFW)" : (i % 12 == 0) ? "NRI" : "General";
                String st = (i % 4 == 0) ? "pending" : (i % 9 == 0) ? "duplicate" : "verified";
                String dupCol = (st.equals("duplicate")) ? "Bangalore Technical & Science Academy" : null;
                String dupId = (st.equals("duplicate")) ? "COL003" : null;
                String reason = (st.equals("duplicate")) ? "Simultaneous active enrolment flagged in Karnataka" : null;

                students.add(new Student(id, name, "2004-06-15", gender, "+91 98000 " + String.format("%05d", (10000 + i)),
                        String.format("%04d", (2000 + i)), col, colName, course,
                        (i % 4) + 1, 30 * ((i % 4) + 1), 85.0 + (i % 10), 84.0 + (i % 10),
                        st, cat, "2022-REG-" + (100 + i), "2022-08-15", st.equals("verified"),
                        "Delhi", dupCol, dupId, reason));
            }

            // Assign unique QR verification tokens and persist tokens
            List<VerificationToken> tokens = new ArrayList<>();
            for (Student s : students) {
                String tokenStr = UUID.randomUUID().toString().replace("-", "");
                s.setQrVerificationToken(tokenStr);
                tokens.add(new VerificationToken(tokenStr, s.getApaarId(), LocalDateTime.now().plusYears(1)));
            }

            studentRepository.saveAll(students);
            verificationTokenRepository.saveAll(tokens);
        }

        // 4. Seed persistent duplicate conflicts
        if (duplicateConflictRepository.count() == 0) {
            duplicateConflictRepository.save(new DuplicateConflict(
                    "100000000005", "Mohammad Farhan Akhtar",
                    "COL001", "Delhi Institute of Engineering & Technology",
                    "COL005", "Uttar Pradesh Council of Technology",
                    "SIMULTANEOUS_ENROLMENT",
                    "Simultaneous regular enrollment active at UP Council of Technology under UID UP-2022-892"
            ));

            duplicateConflictRepository.save(new DuplicateConflict(
                    "100000000012", "Ananya Banerjee",
                    "COL001", "Delhi Institute of Engineering & Technology",
                    "COL003", "Bangalore Technical & Science Academy",
                    "SIMULTANEOUS_ENROLMENT",
                    "Dual enrolment recorded across Delhi and Karnataka colleges in AY 2024-25"
            ));
        }

        // 5. Seed initial verification logs
        if (logRepository.count() == 0) {
            logRepository.save(new VerificationLog(LocalDateTime.now().minusMinutes(25), "123456789012", "Aarav Sharma",
                    "VERIFICATION_QUERY_SUCCESS", "student_portal@aicte.gov.in", "STUDENT", "103.24.188.1", "SUCCESS", "Official verification complete"));
            logRepository.save(new VerificationLog(LocalDateTime.now().minusMinutes(12), "100000000005", "Mohammad Farhan Akhtar",
                    "DUPLICATE_CONFLICT_FLAGGED", "audit_engine@aicte.gov.in", "SYSTEM", "127.0.0.1", "CONFLICT", "Published to Kafka topic aicte.apaar.duplicate-events"));
        }
    }
}
