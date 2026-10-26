import org.mindrot.jbcrypt.BCrypt;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Core authentication and authorization service for KEC ACADEMIX.
 * Manages database authentication, password hashing with BCrypt,
 * role validation, and secure account provisioning.
 */
public class AuthService {

    private static final String DEFAULT_DB_URL = "jdbc:mysql://127.0.0.1:3306/kec_academic";
    private static final String DEFAULT_DB_USER = "root";
    private static final String DEFAULT_DB_PASSWORD = "Vickyy@07";

    public static String getDbUrl() {
        String url = System.getenv("KEC_DB_URL");
        return (url != null && !url.trim().isEmpty()) ? url.trim() : DEFAULT_DB_URL;
    }

    public static String getDbUser() {
        String user = System.getenv("KEC_DB_USER");
        return (user != null && !user.trim().isEmpty()) ? user.trim() : DEFAULT_DB_USER;
    }

    public static String getDbPassword() {
        String pass = System.getenv("KEC_DB_PASSWORD");
        return (pass != null) ? pass : DEFAULT_DB_PASSWORD;
    }

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(getDbUrl(), getDbUser(), getDbPassword());
    }

    public static class AuthException extends Exception {
        public AuthException(String message) {
            super(message);
        }
    }

    public static class ValidationException extends Exception {
        public ValidationException(String message) {
            super(message);
        }
    }

    public static class UserAccountSummary {
        public final int userId;
        public final String username;
        public final String role;
        public final Integer studentId;
        public final String studentRoll;
        public final String studentName;
        public final boolean active;
        public final Timestamp createdAt;

        public UserAccountSummary(int userId, String username, String role, Integer studentId,
                                  String studentRoll, String studentName, boolean active, Timestamp createdAt) {
            this.userId = userId;
            this.username = username;
            this.role = role;
            this.studentId = studentId;
            this.studentRoll = studentRoll;
            this.studentName = studentName;
            this.active = active;
            this.createdAt = createdAt;
        }
    }

    /**
     * Authenticates credentials against app_users using BCrypt.
     */
    public static UserSession authenticate(String username, String password) throws AuthException, SQLException {
        if (username == null || username.trim().isEmpty() || password == null || password.isEmpty()) {
            throw new AuthException("Invalid username or password.");
        }

        String sql = """
                SELECT u.user_id, u.username, u.password_hash, u.role, u.student_id, u.is_active,
                       s.roll_no, s.student_name
                FROM app_users u
                LEFT JOIN students s ON u.student_id = s.student_id
                WHERE u.username = ?
                """;

        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, username.trim());

            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    // Constant-time dummy verification or generic failure message
                    throw new AuthException("Invalid username or password.");
                }

                int userId = rs.getInt("user_id");
                String storedUsername = rs.getString("username");
                String hash = rs.getString("password_hash");
                String role = rs.getString("role");
                int rawStudentId = rs.getInt("student_id");
                Integer studentId = rs.wasNull() ? null : rawStudentId;
                boolean isActive = rs.getBoolean("is_active");
                String rollNo = rs.getString("roll_no");
                String studentName = rs.getString("student_name");

                boolean passwordMatches = false;
                try {
                    passwordMatches = BCrypt.checkpw(password, hash);
                } catch (Exception e) {
                    passwordMatches = false;
                }

                if (!passwordMatches) {
                    throw new AuthException("Invalid username or password.");
                }

                if (!isActive) {
                    throw new AuthException("This account has been disabled. Please contact the administrator.");
                }

                return new UserSession(userId, storedUsername, role, studentId, rollNo, studentName);
            }
        }
    }

    /**
     * Validates password strength:
     * Minimum 8 characters, at least one letter, at least one digit.
     */
    public static void validatePasswordStrength(String password) throws ValidationException {
        if (password == null || password.length() < 8) {
            throw new ValidationException("Password must be at least 8 characters in length.");
        }
        boolean hasLetter = false;
        boolean hasDigit = false;
        for (char c : password.toCharArray()) {
            if (Character.isLetter(c)) hasLetter = true;
            if (Character.isDigit(c)) hasDigit = true;
        }
        if (!hasLetter || !hasDigit) {
            throw new ValidationException("Password must contain both letters and digits.");
        }
    }

    /**
     * Validates that a username is between 3-40 characters, allows spaces, letters, digits, dots,
     * hyphens, and underscores, and contains at least one alphanumeric character.
     */
    public static void validateUsername(String username) throws ValidationException {
        if (username == null) {
            throw new ValidationException("Username is required.");
        }
        String trimmed = username.trim();
        if (trimmed.length() < 3 || trimmed.length() > 40) {
            throw new ValidationException("Username must be between 3 and 40 characters in length.");
        }
        if (!trimmed.matches("^[a-zA-Z0-9_. -]{3,40}$")) {
            throw new ValidationException("Username can only contain letters, numbers, spaces, dots, hyphens, and underscores.");
        }
        if (!trimmed.matches(".*[a-zA-Z0-9].*")) {
            throw new ValidationException("Username must contain at least one letter or digit.");
        }
    }

    /**
     * Data carrier for live academic roll number verification.
     */
    /**
     * Evaluation result for KEC roll number policy.
     */
    public static class RollPolicyMatch {
        public final boolean eligible;
        public final String normalizedRoll;
        public final String department;
        public final String category; // "Regular" or "Lateral Entry"
        public final int defaultSemester;
        public final String rejectionReason;

        public RollPolicyMatch(boolean eligible, String normalizedRoll, String department, String category, int defaultSemester, String rejectionReason) {
            this.eligible = eligible;
            this.normalizedRoll = normalizedRoll;
            this.department = department;
            this.category = category;
            this.defaultSemester = defaultSemester;
            this.rejectionReason = rejectionReason;
        }

        public static RollPolicyMatch eligible(String normalizedRoll, String dept, String cat, int sem) {
            return new RollPolicyMatch(true, normalizedRoll, dept, cat, sem, null);
        }

        public static RollPolicyMatch ineligible(String reason) {
            return new RollPolicyMatch(false, null, null, null, 0, reason);
        }
    }

    /**
     * Evaluates whether a roll number conforms to KEC IT Department student format:
     * - Regular Students: 25ITR001 through 25ITR182
     * - Lateral Entry Students: 25ITL... (e.g. 25ITL123, any number from 001 upwards)
     */
    public static RollPolicyMatch evaluateRollPolicy(String rawRoll) {
        if (rawRoll == null || rawRoll.trim().isEmpty()) {
            return RollPolicyMatch.ineligible("Student roll number is required.");
        }
        String roll = rawRoll.trim().toUpperCase();

        // 1. Regular IT batch: 25ITR001 to 25ITR182
        Matcher mRegular = Pattern.compile("^25ITR(\\d+)$").matcher(roll);
        if (mRegular.matches()) {
            try {
                int num = Integer.parseInt(mRegular.group(1));
                if (num >= 1 && num <= 182) {
                    String formatted = String.format("25ITR%03d", num);
                    return RollPolicyMatch.eligible(formatted, "Information Technology", "Regular Batch", 4);
                } else {
                    return RollPolicyMatch.ineligible("Regular 25ITR roll numbers must be between 25ITR001 and 25ITR182 (entered: " + roll + ").");
                }
            } catch (NumberFormatException e) {
                return RollPolicyMatch.ineligible("Invalid numeric sequence in roll number.");
            }
        }

        // 2. Lateral Entry IT batch: 25ITL... (last number any from 001 onwards, e.g. 25ITL123)
        Matcher mLateral = Pattern.compile("^25ITL(\\d+)$").matcher(roll);
        if (mLateral.matches()) {
            try {
                int num = Integer.parseInt(mLateral.group(1));
                if (num >= 1) {
                    String digits = mLateral.group(1);
                    String formatted = digits.length() < 3 ? String.format("25ITL%03d", num) : ("25ITL" + digits);
                    return RollPolicyMatch.eligible(formatted, "Information Technology", "Lateral Entry", 4);
                } else {
                    return RollPolicyMatch.ineligible("Lateral Entry roll numbers must be 001 or greater (e.g. 25ITL123).");
                }
            } catch (NumberFormatException e) {
                return RollPolicyMatch.ineligible("Invalid numeric sequence in roll number.");
            }
        }

        return RollPolicyMatch.ineligible("Roll number '" + roll + "' is not eligible. Allowed formats: Regular 25ITR001–25ITR182, or Lateral Entry 25ITL (e.g. 25ITL123).");
    }

    /**
     * Data carrier for live academic roll number verification.
     */
    public static class StudentVerificationResult {
        public final boolean found;
        public final int studentId;
        public final String rollNo;
        public final String studentName;
        public final String department;
        public final int semester;
        public final boolean alreadyRegistered;
        public final String existingUsername;
        public final boolean isNewEnrollment;
        public final String category; // "Regular Batch", "Lateral Entry", or null
        public final String errorMessage;

        public StudentVerificationResult(boolean found, int studentId, String rollNo, String studentName,
                                        String department, int semester, boolean alreadyRegistered,
                                        String existingUsername, boolean isNewEnrollment, String category,
                                        String errorMessage) {
            this.found = found;
            this.studentId = studentId;
            this.rollNo = rollNo;
            this.studentName = studentName;
            this.department = department;
            this.semester = semester;
            this.alreadyRegistered = alreadyRegistered;
            this.existingUsername = existingUsername;
            this.isNewEnrollment = isNewEnrollment;
            this.category = category;
            this.errorMessage = errorMessage;
        }

        public static StudentVerificationResult notFound(String rollNo, String errorMessage) {
            return new StudentVerificationResult(false, -1, rollNo, null, null, 0, false, null, false, null, errorMessage);
        }
    }

    /**
     * Verifies a student roll number against KEC academic records or enrollment policy.
     */
    public static StudentVerificationResult verifyStudentRoll(String rollNo) throws SQLException {
        if (rollNo == null || rollNo.trim().isEmpty()) {
            return null;
        }
        String rawRoll = rollNo.trim().toUpperCase();

        try (Connection conn = getConnection()) {
            // 1. Check if student already exists in students database table
            String sql = "SELECT student_id, roll_no, student_name, department, semester FROM students WHERE UPPER(TRIM(roll_no)) = ?";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, rawRoll);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        int studentId = rs.getInt("student_id");
                        String actualRoll = rs.getString("roll_no");
                        String name = rs.getString("student_name");
                        String dept = rs.getString("department");
                        int sem = rs.getInt("semester");

                        // Check if an account is already linked
                        String checkSql = "SELECT user_id, username FROM app_users WHERE student_id = ?";
                        try (PreparedStatement ps2 = conn.prepareStatement(checkSql)) {
                            ps2.setInt(1, studentId);
                            try (ResultSet rs2 = ps2.executeQuery()) {
                                if (rs2.next()) {
                                    return new StudentVerificationResult(true, studentId, actualRoll, name, dept, sem, true, rs2.getString("username"), false, null, null);
                                }
                            }
                        }

                        return new StudentVerificationResult(true, studentId, actualRoll, name, dept, sem, false, null, false, null, null);
                    }
                }
            }

            // 2. Evaluate against KEC policy (25ITR001-25ITR182 or 25ITL...)
            RollPolicyMatch policy = evaluateRollPolicy(rawRoll);
            if (policy.eligible) {
                // Also check normalized roll in students
                if (!policy.normalizedRoll.equals(rawRoll)) {
                    try (PreparedStatement ps = conn.prepareStatement(sql)) {
                        ps.setString(1, policy.normalizedRoll);
                        try (ResultSet rs = ps.executeQuery()) {
                            if (rs.next()) {
                                int studentId = rs.getInt("student_id");
                                String actualRoll = rs.getString("roll_no");
                                String name = rs.getString("student_name");
                                String dept = rs.getString("department");
                                int sem = rs.getInt("semester");

                                String checkSql = "SELECT user_id, username FROM app_users WHERE student_id = ?";
                                try (PreparedStatement ps2 = conn.prepareStatement(checkSql)) {
                                    ps2.setInt(1, studentId);
                                    try (ResultSet rs2 = ps2.executeQuery()) {
                                        if (rs2.next()) {
                                            return new StudentVerificationResult(true, studentId, actualRoll, name, dept, sem, true, rs2.getString("username"), false, policy.category, null);
                                        }
                                    }
                                }

                                return new StudentVerificationResult(true, studentId, actualRoll, name, dept, sem, false, null, false, policy.category, null);
                            }
                        }
                    }
                }

                // Eligible new student enrollment
                return new StudentVerificationResult(true, -1, policy.normalizedRoll, null, policy.department, policy.defaultSemester, false, null, true, policy.category, null);
            }

            return StudentVerificationResult.notFound(rawRoll, policy.rejectionReason);
        }
    }

    /**
     * Public Student Signup:
     * Strictly creates STUDENT accounts.
     * Validates roll number against students table or KEC policy (25ITR001-25ITR182, 25ITL...).
     * Auto-enrolls student in `students` table if not present, and links account in `app_users`.
     */
    public static void signupStudent(String username, String password, String confirmPassword, String rollNo)
            throws ValidationException, SQLException {

        validateUsername(username);

        if (rollNo == null || rollNo.trim().isEmpty()) {
            throw new ValidationException("Student roll number is required.");
        }

        if (password == null || confirmPassword == null) {
            throw new ValidationException("Password and confirmation are required.");
        }

        if (!password.equals(confirmPassword)) {
            throw new ValidationException("Passwords do not match.");
        }

        validatePasswordStrength(password);

        String trimmedUser = username.trim();
        String rawRoll = rollNo.trim().toUpperCase();

        try (Connection conn = getConnection()) {
            // Check if username already exists
            String checkUserSql = "SELECT user_id FROM app_users WHERE username = ?";
            try (PreparedStatement ps = conn.prepareStatement(checkUserSql)) {
                ps.setString(1, trimmedUser);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        throw new ValidationException("The username '" + trimmedUser + "' is already registered. Please choose another.");
                    }
                }
            }

            // Look up student in students table
            int studentId = -1;
            String effectiveRoll = rawRoll;
            String checkStudentSql = "SELECT student_id, roll_no, student_name FROM students WHERE UPPER(TRIM(roll_no)) = ?";
            try (PreparedStatement ps = conn.prepareStatement(checkStudentSql)) {
                ps.setString(1, rawRoll);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        studentId = rs.getInt("student_id");
                        effectiveRoll = rs.getString("roll_no");
                    }
                }
            }

            // If not found in students table, evaluate policy and auto-enroll
            if (studentId == -1) {
                RollPolicyMatch policy = evaluateRollPolicy(rawRoll);
                if (!policy.eligible) {
                    throw new ValidationException(policy.rejectionReason);
                }
                effectiveRoll = policy.normalizedRoll;

                // Check again with normalized roll in students
                try (PreparedStatement ps = conn.prepareStatement(checkStudentSql)) {
                    ps.setString(1, effectiveRoll);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (rs.next()) {
                            studentId = rs.getInt("student_id");
                        }
                    }
                }

                // If still not in students table, insert new student record with user's name
                if (studentId == -1) {
                    String insertStudentSql = """
                            INSERT INTO students (roll_no, student_name, department, semester)
                            VALUES (?, ?, ?, ?)
                            """;
                    try (PreparedStatement ps = conn.prepareStatement(insertStudentSql, Statement.RETURN_GENERATED_KEYS)) {
                        ps.setString(1, effectiveRoll);
                        ps.setString(2, trimmedUser);
                        ps.setString(3, policy.department);
                        ps.setInt(4, policy.defaultSemester);
                        ps.executeUpdate();
                        try (ResultSet gk = ps.getGeneratedKeys()) {
                            if (gk.next()) {
                                studentId = gk.getInt(1);
                            } else {
                                throw new SQLException("Failed to auto-enroll student record for roll: " + effectiveRoll);
                            }
                        }
                    }
                }
            }

            // Check if an account already exists for this student
            String checkMappingSql = "SELECT user_id FROM app_users WHERE student_id = ?";
            try (PreparedStatement ps = conn.prepareStatement(checkMappingSql)) {
                ps.setInt(1, studentId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        throw new ValidationException("An account is already linked to student roll number: " + effectiveRoll);
                    }
                }
            }
            // Hash password securely with BCrypt
            String salt = BCrypt.gensalt(12);
            String hash = BCrypt.hashpw(password, salt);

            // Insert new STUDENT record
            String insertSql = """
                    INSERT INTO app_users (username, password_hash, role, student_id, is_active)
                    VALUES (?, ?, 'STUDENT', ?, 1)
                    """;
            try (PreparedStatement ps = conn.prepareStatement(insertSql)) {
                ps.setString(1, trimmedUser);
                ps.setString(2, hash);
                ps.setInt(3, studentId);
                ps.executeUpdate();
            }
        }
    }

    /**
     * Checks if at least one active administrator exists in the system.
     */
    public static boolean hasAdministrator() throws SQLException {
        String sql = "SELECT COUNT(*) FROM app_users WHERE role = 'ADMINISTRATOR' AND is_active = 1";
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
            return false;
        }
    }

    /**
     * Secure First-Run Setup:
     * Allows initializing the primary administrator account only if no administrator exists yet.
     */
    public static void setupInitialAdministrator(String username, String password, String confirmPassword)
            throws ValidationException, SecurityException, SQLException {

        if (hasAdministrator()) {
            throw new SecurityException("An administrator account already exists. New staff/admin accounts must be provisioned by the administrator.");
        }

        validateUsername(username);

        if (password == null || confirmPassword == null) {
            throw new ValidationException("Password and confirmation are required.");
        }

        if (!password.equals(confirmPassword)) {
            throw new ValidationException("Passwords do not match.");
        }

        validatePasswordStrength(password);

        String trimmedUser = username.trim();

        try (Connection conn = getConnection()) {
            // Check username uniqueness
            String checkUserSql = "SELECT user_id FROM app_users WHERE username = ?";
            try (PreparedStatement ps = conn.prepareStatement(checkUserSql)) {
                ps.setString(1, trimmedUser);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        throw new ValidationException("Username '" + trimmedUser + "' is already taken.");
                    }
                }
            }

            String salt = BCrypt.gensalt(12);
            String hash = BCrypt.hashpw(password, salt);

            String insertSql = """
                    INSERT INTO app_users (username, password_hash, role, student_id, is_active)
                    VALUES (?, ?, 'ADMINISTRATOR', NULL, 1)
                    """;
            try (PreparedStatement ps = conn.prepareStatement(insertSql)) {
                ps.setString(1, trimmedUser);
                ps.setString(2, hash);
                ps.executeUpdate();
            }
        }
    }

    /**
     * Protected provisioning of STAFF or ADMINISTRATOR accounts.
     * Enforces caller session permission check.
     */
    public static void createStaffOrAdmin(UserSession session, String username, String password, String role)
            throws ValidationException, SecurityException, SQLException {

        if (session == null || !session.isActive() || !session.isAdministrator()) {
            throw new SecurityException("Unauthorized: Only an active Administrator can create staff or administrator accounts.");
        }

        if (!"STAFF".equals(role) && !"ADMINISTRATOR".equals(role)) {
            throw new ValidationException("Role must be either STAFF or ADMINISTRATOR.");
        }

        validateUsername(username);

        validatePasswordStrength(password);

        String trimmedUser = username.trim();

        try (Connection conn = getConnection()) {
            String checkSql = "SELECT user_id FROM app_users WHERE username = ?";
            try (PreparedStatement ps = conn.prepareStatement(checkSql)) {
                ps.setString(1, trimmedUser);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        throw new ValidationException("Username '" + trimmedUser + "' already exists.");
                    }
                }
            }

            String salt = BCrypt.gensalt(12);
            String hash = BCrypt.hashpw(password, salt);

            String insertSql = """
                    INSERT INTO app_users (username, password_hash, role, student_id, is_active)
                    VALUES (?, ?, ?, NULL, 1)
                    """;
            try (PreparedStatement ps = conn.prepareStatement(insertSql)) {
                ps.setString(1, trimmedUser);
                ps.setString(2, hash);
                ps.setString(3, role);
                ps.executeUpdate();
            }
        }
    }

    /**
     * Lists all registered user accounts for administrative management.
     */
    public static List<UserAccountSummary> listUsers(UserSession session)
            throws SecurityException, SQLException {

        if (session == null || !session.isActive() || !session.isAdministrator()) {
            throw new SecurityException("Unauthorized: Only an active Administrator can view the user accounts list.");
        }

        List<UserAccountSummary> list = new ArrayList<>();
        String sql = """
                SELECT u.user_id, u.username, u.role, u.student_id, u.is_active, u.created_at,
                       s.roll_no, s.student_name
                FROM app_users u
                LEFT JOIN students s ON u.student_id = s.student_id
                ORDER BY u.user_id ASC
                """;

        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                int rawStudentId = rs.getInt("student_id");
                Integer studentId = rs.wasNull() ? null : rawStudentId;
                list.add(new UserAccountSummary(
                        rs.getInt("user_id"),
                        rs.getString("username"),
                        rs.getString("role"),
                        studentId,
                        rs.getString("roll_no"),
                        rs.getString("student_name"),
                        rs.getBoolean("is_active"),
                        rs.getTimestamp("created_at")
                ));
            }
        }
        return list;
    }

    /**
     * Toggle account activation state (Admin only).
     */
    public static void setUserActive(UserSession session, int targetUserId, boolean active)
            throws SecurityException, SQLException {

        if (session == null || !session.isActive() || !session.isAdministrator()) {
            throw new SecurityException("Unauthorized: Only an active Administrator can modify account status.");
        }

        if (session.getUserId() == targetUserId && !active) {
            throw new SecurityException("Administrators cannot deactivate their own currently active account.");
        }

        String sql = "UPDATE app_users SET is_active = ? WHERE user_id = ?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, active ? 1 : 0);
            ps.setInt(2, targetUserId);
            ps.executeUpdate();
        }
    }
}

