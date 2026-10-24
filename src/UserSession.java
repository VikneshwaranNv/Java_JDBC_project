/**
 * Represents an authenticated immutable user session in KEC ACADEMIX.
 * Enforces role-based permissions and session validity checks.
 */
public class UserSession {

    public static final String ROLE_ADMIN = "ADMINISTRATOR";
    public static final String ROLE_STAFF = "STAFF";
    public static final String ROLE_STUDENT = "STUDENT";

    private final int userId;
    private final String username;
    private final String role;
    private final Integer studentId;
    private final String studentRoll;
    private final String studentName;
    private final long loginTimestamp;
    private volatile boolean active;

    public UserSession(int userId, String username, String role, Integer studentId, String studentRoll, String studentName) {
        if (username == null || username.trim().isEmpty()) {
            throw new IllegalArgumentException("Username cannot be null or empty.");
        }
        if (role == null || (!role.equals(ROLE_ADMIN) && !role.equals(ROLE_STAFF) && !role.equals(ROLE_STUDENT))) {
            throw new IllegalArgumentException("Invalid role for session: " + role);
        }
        if (ROLE_STUDENT.equals(role) && studentId == null) {
            throw new IllegalArgumentException("Student session must have an associated student ID.");
        }

        this.userId = userId;
        this.username = username.trim();
        this.role = role.trim();
        this.studentId = studentId;
        this.studentRoll = studentRoll != null ? studentRoll.trim() : null;
        this.studentName = studentName != null ? studentName.trim() : null;
        this.loginTimestamp = System.currentTimeMillis();
        this.active = true;
    }

    public int getUserId() {
        checkActive();
        return userId;
    }

    public String getUsername() {
        checkActive();
        return username;
    }

    public String getRole() {
        checkActive();
        return role;
    }

    public Integer getStudentId() {
        checkActive();
        return studentId;
    }

    public String getStudentRoll() {
        checkActive();
        return studentRoll;
    }

    public String getStudentName() {
        checkActive();
        return studentName;
    }

    public long getLoginTimestamp() {
        return loginTimestamp;
    }

    public boolean isActive() {
        return active;
    }

    public synchronized void invalidate() {
        this.active = false;
    }

    public void checkActive() {
        if (!active) {
            throw new SecurityException("User session has been invalidated or expired.");
        }
    }

    public boolean isAdministrator() {
        checkActive();
        return ROLE_ADMIN.equalsIgnoreCase(role);
    }

    public boolean isStaff() {
        checkActive();
        return ROLE_STAFF.equalsIgnoreCase(role);
    }

    public boolean isStudent() {
        checkActive();
        return ROLE_STUDENT.equalsIgnoreCase(role);
    }

    public boolean canManageMarks() {
        checkActive();
        return isAdministrator() || isStaff();
    }

    public boolean canReviewSAP() {
        checkActive();
        return isAdministrator() || isStaff();
    }

    public boolean canManageCourses() {
        checkActive();
        return isAdministrator() || isStaff();
    }

    public boolean canManageEnrollments() {
        checkActive();
        return isAdministrator() || isStaff();
    }

    public boolean canManageUsers() {
        checkActive();
        return isAdministrator();
    }

    @Override
    public String toString() {
        return String.format("UserSession[user=%s, role=%s, studentId=%s, active=%b]",
                username, role, studentId, active);
    }
}

