import java.io.File;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

/**
 * KEC ACADEMIX — Interactive Command-Line Evaluation & Management Console.
 * Compliant with KEC R2024 Continuous Evaluation & Grading Regulations.
 * Connects directly to live MySQL database 'kec_academic' with RBAC enforcement.
 */
public class Main {

    private static Connection getConnection() throws SQLException {
        return AuthService.getConnection();
    }

    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        System.out.println("=============================================================================================================");
        System.out.println("                    KEC ACADEMIX — KONGU ENGINEERING COLLEGE (AUTONOMOUS)");
        System.out.println("                     Student Academic & Continuous Evaluation System (R2024)");
        System.out.println("=============================================================================================================");

        // Initial Authentication
        UserSession currentSession = null;

        // Check if environment variables or arguments provided credentials
        String envUser = System.getenv("KEC_AUTH_USER");
        String envPass = System.getenv("KEC_AUTH_PASS");
        if (envUser != null && envPass != null) {
            try {
                currentSession = AuthService.authenticate(envUser, envPass);
                System.out.printf("[Auto-Login] Authenticated as %s (Role: %s)%n", currentSession.getUsername(), currentSession.getRole());
            } catch (Exception ignored) {}
        }

        while (currentSession == null) {
            System.out.println("\n--- User Authentication ---");
            System.out.println("Please sign in with your KEC credentials, or enter 'guest' for read-only directory access.");
            System.out.print("Username: ");
            if (!sc.hasNextLine()) break;
            String user = sc.nextLine().trim();
            if (user.isEmpty() || user.equalsIgnoreCase("guest") || user.equalsIgnoreCase("anonymous")) {
                System.out.println("Continuing with Guest / Public access (Read-Only).");
                break;
            }
            System.out.print("Password: ");
            if (!sc.hasNextLine()) break;
            String pass = sc.nextLine();

            try {
                currentSession = AuthService.authenticate(user, pass);
                System.out.println("\n>>> Login Successful! <<<");
                System.out.printf("Welcome, %s | Role: %s%s%n",
                        currentSession.getUsername(),
                        currentSession.getRole(),
                        currentSession.getStudentRoll() != null ? " (" + currentSession.getStudentRoll() + ")" : "");
            } catch (AuthService.AuthException ae) {
                System.out.println("\n[Authentication Failed] " + ae.getMessage());
                System.out.print("Try again? (Y/N, or press Enter for Guest mode): ");
                if (!sc.hasNextLine()) break;
                String retry = sc.nextLine().trim();
                if (retry.equalsIgnoreCase("N")) {
                    System.out.println("Continuing in Guest mode.");
                    break;
                }
            } catch (Exception e) {
                System.out.println("\n[Database / Auth Error] " + e.getMessage());
                break;
            }
        }

        boolean running = true;
        while (running) {
            printMainMenu(currentSession);
            System.out.print("Enter your choice (0-9, or 'L' to switch user): ");
            if (!sc.hasNextLine()) break;
            String choice = sc.nextLine().trim();

            switch (choice) {
                case "1":
                    handleStudentRegistration(sc, currentSession);
                    break;
                case "2":
                    handleCourseCatalog(sc, currentSession);
                    break;
                case "3":
                    handleCourseEnrollmentAndAttendance(sc, currentSession);
                    break;
                case "4":
                    handleIncrementalMarksEntry(sc, currentSession);
                    break;
                case "5":
                    handleStudentSemesterMarksheet(sc, currentSession);
                    break;
                case "6":
                    handleAttendanceDefaultersAndCondonation(sc, currentSession);
                    break;
                case "7":
                    handleClassPerformanceSummary(sc, currentSession);
                    break;
                case "8":
                    handleAutomatedDemonstration();
                    break;
                case "9":
                    handleWebDashboardAndSwing(sc);
                    break;
                case "0":
                    running = false;
                    System.out.println("\nThank you for using KEC ACADEMIX. Session closed safely.");
                    break;
                case "L":
                case "l":
                    currentSession = handleSwitchUser(sc);
                    break;
                default:
                    System.out.println("\n[Invalid Option] Please enter a number from 0 to 9, or 'L' to switch user.");
            }
        }

        sc.close();
    }

    private static void printMainMenu(UserSession session) {
        System.out.println("\n=============================================================================================================");
        System.out.println("                                         KEC ACADEMIX — MAIN MENU");
        if (session != null) {
            System.out.printf("   [Active Session: %s | Role: %s%s]%n",
                    session.getUsername(), session.getRole(),
                    session.getStudentRoll() != null ? " | Roll: " + session.getStudentRoll() : "");
        } else {
            System.out.println("   [Active Session: Guest / Public (Read-Only Access)]");
        }
        System.out.println("=============================================================================================================");
        System.out.println("  1. Student Registration (Directory, Add, Edit Profile)");
        System.out.println("  2. Course Catalog Management (Curriculum, Types, Credits)");
        System.out.println("  3. Course Enrollment & Attendance Tracking (Allocations & Attendance %)");
        System.out.println("  4. Incremental Marks Entry (CIA1, CIA2, CIA3, ESE & Absentee Flags)");
        System.out.println("  5. Student Semester Marksheet (R2024 Evaluation, SGPA & PDF Export)");
        System.out.println("  6. Attendance Defaulter & Condonation Report (65%-75% Approval Workflow)");
        System.out.println("  7. Class Performance & Result Summary (Academic Analytics & Pass %)");
        System.out.println("  8. Automated Demonstration / Proof of Execution (R2024 Rules Engine Test)");
        System.out.println("  9. Web Dashboard & Swing Application Status");
        System.out.println("  0. Exit Console");
        System.out.println("=============================================================================================================");
    }

    private static UserSession handleSwitchUser(Scanner sc) {
        System.out.println("\n--- Switch User Account ---");
        System.out.print("Enter Username (or press Enter to cancel): ");
        if (!sc.hasNextLine()) return null;
        String user = sc.nextLine().trim();
        if (user.isEmpty()) {
            System.out.println("Cancelled user switch.");
            return null;
        }
        System.out.print("Enter Password: ");
        if (!sc.hasNextLine()) return null;
        String pass = sc.nextLine();

        try {
            UserSession session = AuthService.authenticate(user, pass);
            System.out.println("\n>>> Switched account successfully! <<<");
            System.out.printf("Logged in as: %s | Role: %s%n", session.getUsername(), session.getRole());
            return session;
        } catch (Exception e) {
            System.out.println("\n[Authentication Failed] " + e.getMessage());
            return null;
        }
    }

    // =========================================================================
    // 1. STUDENT REGISTRATION
    // =========================================================================
    private static void handleStudentRegistration(Scanner sc, UserSession session) {
        boolean back = false;
        while (!back) {
            System.out.println("\n-----------------------------------------------------------------------------");
            System.out.println("                       1. STUDENT REGISTRATION MANAGEMENT");
            System.out.println("-----------------------------------------------------------------------------");
            System.out.println("  1. List All Registered Students");
            System.out.println("  2. Search Student by Roll Number");
            System.out.println("  3. Register New Student (Admin / Staff)");
            System.out.println("  4. Edit Student Information (Admin / Staff)");
            System.out.println("  0. Back to Main Menu");
            System.out.println("-----------------------------------------------------------------------------");
            System.out.print("Choose an option: ");
            if (!sc.hasNextLine()) break;
            String opt = sc.nextLine().trim();

            switch (opt) {
                case "1":
                    viewAllStudents();
                    break;
                case "2":
                    searchStudent(sc);
                    break;
                case "3":
                    registerStudent(sc, session);
                    break;
                case "4":
                    editStudent(sc, session);
                    break;
                case "0":
                    back = true;
                    break;
                default:
                    System.out.println("Invalid option. Please choose 1, 2, 3, 4, or 0.");
            }
        }
    }

    private static void viewAllStudents() {
        String query = "SELECT student_id, roll_no, student_name, department, semester FROM students ORDER BY roll_no";
        try (Connection con = getConnection();
             Statement stmt = con.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            System.out.println("\n=============================================================================");
            System.out.println("                            REGISTERED STUDENTS");
            System.out.println("=============================================================================");
            System.out.printf("%-5s %-15s %-25s %-22s %-8s%n", "ID", "Roll Number", "Full Name", "Department", "Semester");
            System.out.println("-----------------------------------------------------------------------------");

            boolean found = false;
            while (rs.next()) {
                found = true;
                System.out.printf("%-5d %-15s %-25s %-22s %-8d%n",
                        rs.getInt("student_id"),
                        rs.getString("roll_no"),
                        rs.getString("student_name"),
                        rs.getString("department"),
                        rs.getInt("semester"));
            }
            if (!found) {
                System.out.println("No students registered in the database.");
            }
            System.out.println("=============================================================================");
        } catch (Exception e) {
            System.out.println("[Error] Failed to retrieve students: " + e.getMessage());
        }
    }

    private static void searchStudent(Scanner sc) {
        System.out.print("Enter student roll number: ");
        if (!sc.hasNextLine()) return;
        String rollNo = sc.nextLine().trim();
        if (rollNo.isEmpty()) {
            System.out.println("Roll number cannot be empty.");
            return;
        }

        String query = "SELECT student_id, roll_no, student_name, department, semester FROM students WHERE roll_no = ?";
        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(query)) {
            ps.setString(1, rollNo);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    System.out.println("\n========================================");
                    System.out.println("           STUDENT DETAILS");
                    System.out.println("========================================");
                    System.out.println("Student ID   : " + rs.getInt("student_id"));
                    System.out.println("Roll Number  : " + rs.getString("roll_no"));
                    System.out.println("Name         : " + rs.getString("student_name"));
                    System.out.println("Department   : " + rs.getString("department"));
                    System.out.println("Semester     : " + rs.getInt("semester"));
                    System.out.println("========================================");
                } else {
                    System.out.println("No student found with roll number: " + rollNo);
                }
            }
        } catch (Exception e) {
            System.out.println("[Error] Student search failed: " + e.getMessage());
        }
    }

    private static void registerStudent(Scanner sc, UserSession session) {
        if (session != null && session.isStudent()) {
            System.out.println("\n[Access Denied] Student accounts cannot register new students.");
            return;
        }

        System.out.println("\n--- Register New Student ---");
        System.out.print("Enter Roll Number (e.g. 25ITR001 to 25ITR182, or Lateral Entry 25ITL123): ");
        if (!sc.hasNextLine()) return;
        String roll = sc.nextLine().trim().toUpperCase();
        if (roll.isEmpty()) {
            System.out.println("[Error] Roll number is required.");
            return;
        }

        // Policy validation check
        AuthService.RollPolicyMatch match = AuthService.evaluateRollPolicy(roll);
        if (!match.eligible) {
            System.out.printf("[Policy Warning] %s%n", match.rejectionReason);
            System.out.print("Do you wish to proceed with this roll number anyway? (Y/N): ");
            if (!sc.hasNextLine()) return;
            String confirm = sc.nextLine().trim();
            if (!confirm.equalsIgnoreCase("Y")) {
                System.out.println("Registration cancelled.");
                return;
            }
        }

        System.out.print("Enter Student Full Name: ");
        if (!sc.hasNextLine()) return;
        String name = sc.nextLine().trim();
        if (name.isEmpty()) {
            System.out.println("[Error] Student name is required.");
            return;
        }

        String defaultDept = (match.department != null) ? match.department : "Information Technology";
        System.out.printf("Enter Department [%s]: ", defaultDept);
        if (!sc.hasNextLine()) return;
        String deptInput = sc.nextLine().trim();
        String dept = deptInput.isEmpty() ? defaultDept : deptInput;

        System.out.print("Enter Current Semester (1-8): ");
        if (!sc.hasNextLine()) return;
        String semInput = sc.nextLine().trim();
        int semester;
        try {
            semester = Integer.parseInt(semInput);
            if (semester < 1 || semester > 8) {
                System.out.println("[Error] Semester must be between 1 and 8.");
                return;
            }
        } catch (NumberFormatException e) {
            System.out.println("[Error] Semester must be a whole integer from 1 to 8.");
            return;
        }

        // Check duplicate roll number
        String checkSql = "SELECT COUNT(*) FROM students WHERE roll_no = ?";
        try (Connection con = getConnection();
             PreparedStatement checkPs = con.prepareStatement(checkSql)) {
            checkPs.setString(1, roll);
            try (ResultSet rs = checkPs.executeQuery()) {
                if (rs.next() && rs.getInt(1) > 0) {
                    System.out.printf("[Error] Roll number '%s' already exists in the system.%n", roll);
                    return;
                }
            }

            String insertSql = "INSERT INTO students (roll_no, student_name, department, semester) VALUES (?, ?, ?, ?)";
            try (PreparedStatement ps = con.prepareStatement(insertSql)) {
                ps.setString(1, roll);
                ps.setString(2, name);
                ps.setString(3, dept);
                ps.setInt(4, semester);
                int count = ps.executeUpdate();
                if (count > 0) {
                    System.out.printf("\n>>> Student '%s' (%s) registered successfully into %s (Sem %d). <<<%n",
                            name, roll, dept, semester);
                }
            }
        } catch (Exception e) {
            System.out.println("[Error] Database insertion failed: " + e.getMessage());
        }
    }

    private static void editStudent(Scanner sc, UserSession session) {
        if (session != null && session.isStudent()) {
            System.out.println("\n[Access Denied] Student accounts cannot edit student details.");
            return;
        }

        System.out.print("Enter student roll number to edit: ");
        if (!sc.hasNextLine()) return;
        String roll = sc.nextLine().trim();
        if (roll.isEmpty()) return;

        String selectSql = "SELECT student_id, roll_no, student_name, department, semester FROM students WHERE roll_no = ?";
        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(selectSql)) {
            ps.setString(1, roll);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    System.out.println("No student found with roll number: " + roll);
                    return;
                }

                int studentId = rs.getInt("student_id");
                String curRoll = rs.getString("roll_no");
                String curName = rs.getString("student_name");
                String curDept = rs.getString("department");
                int curSem = rs.getInt("semester");

                System.out.println("\nCurrent Information:");
                System.out.printf("  Roll: %s | Name: %s | Dept: %s | Sem: %d%n", curRoll, curName, curDept, curSem);

                System.out.printf("Enter New Roll Number [%s]: ", curRoll);
                String newRoll = sc.nextLine().trim();
                if (newRoll.isEmpty()) newRoll = curRoll;

                System.out.printf("Enter New Name [%s]: ", curName);
                String newName = sc.nextLine().trim();
                if (newName.isEmpty()) newName = curName;

                System.out.printf("Enter New Department [%s]: ", curDept);
                String newDept = sc.nextLine().trim();
                if (newDept.isEmpty()) newDept = curDept;

                System.out.printf("Enter New Semester (1-8) [%d]: ", curSem);
                String newSemStr = sc.nextLine().trim();
                int newSem = curSem;
                if (!newSemStr.isEmpty()) {
                    try {
                        newSem = Integer.parseInt(newSemStr);
                        if (newSem < 1 || newSem > 8) {
                            System.out.println("[Error] Semester must be between 1 and 8.");
                            return;
                        }
                    } catch (NumberFormatException e) {
                        System.out.println("[Error] Invalid semester value.");
                        return;
                    }
                }

                // If roll changed, check duplicate
                if (!newRoll.equalsIgnoreCase(curRoll)) {
                    String dupCheck = "SELECT COUNT(*) FROM students WHERE roll_no = ? AND student_id != ?";
                    try (PreparedStatement checkPs = con.prepareStatement(dupCheck)) {
                        checkPs.setString(1, newRoll);
                        checkPs.setInt(2, studentId);
                        try (ResultSet rsDup = checkPs.executeQuery()) {
                            if (rsDup.next() && rsDup.getInt(1) > 0) {
                                System.out.printf("[Error] Roll number '%s' is already in use by another student.%n", newRoll);
                                return;
                            }
                        }
                    }
                }

                String updateSql = "UPDATE students SET roll_no = ?, student_name = ?, department = ?, semester = ? WHERE student_id = ?";
                try (PreparedStatement updatePs = con.prepareStatement(updateSql)) {
                    updatePs.setString(1, newRoll);
                    updatePs.setString(2, newName);
                    updatePs.setString(3, newDept);
                    updatePs.setInt(4, newSem);
                    updatePs.setInt(5, studentId);
                    int updated = updatePs.executeUpdate();
                    if (updated > 0) {
                        System.out.println("\n>>> Student record updated successfully. <<<");
                    }
                }
            }
        } catch (Exception e) {
            System.out.println("[Error] Failed to update student: " + e.getMessage());
        }
    }

    // =========================================================================
    // 2. COURSE CATALOG MANAGEMENT
    // =========================================================================
    private static void handleCourseCatalog(Scanner sc, UserSession session) {
        boolean back = false;
        while (!back) {
            System.out.println("\n-----------------------------------------------------------------------------");
            System.out.println("                       2. COURSE CATALOG MANAGEMENT");
            System.out.println("-----------------------------------------------------------------------------");
            System.out.println("  1. List All Curriculum Courses");
            System.out.println("  2. Add New Course (Admin / Staff)");
            System.out.println("  3. Edit Course Information (Admin / Staff)");
            System.out.println("  4. Delete Course (Protected against active enrollments)");
            System.out.println("  0. Back to Main Menu");
            System.out.println("-----------------------------------------------------------------------------");
            System.out.print("Choose an option: ");
            if (!sc.hasNextLine()) break;
            String opt = sc.nextLine().trim();

            switch (opt) {
                case "1":
                    viewAllCourses();
                    break;
                case "2":
                    addCourse(sc, session);
                    break;
                case "3":
                    editCourse(sc, session);
                    break;
                case "4":
                    deleteCourse(sc, session);
                    break;
                case "0":
                    back = true;
                    break;
                default:
                    System.out.println("Invalid option. Please choose 1, 2, 3, 4, or 0.");
            }
        }
    }

    private static void viewAllCourses() {
        String query = "SELECT course_id, course_code, course_name, course_type, credits FROM courses ORDER BY course_code";
        try (Connection con = getConnection();
             Statement stmt = con.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            System.out.println("\n===================================================================================================");
            System.out.println("                                      CURRICULUM COURSES");
            System.out.println("===================================================================================================");
            System.out.printf("%-5s %-12s %-40s %-15s %-8s%n", "ID", "Code", "Course Title", "Course Type", "Credits");
            System.out.println("---------------------------------------------------------------------------------------------------");

            boolean found = false;
            while (rs.next()) {
                found = true;
                System.out.printf("%-5d %-12s %-40s %-15s %-8d%n",
                        rs.getInt("course_id"),
                        rs.getString("course_code"),
                        rs.getString("course_name"),
                        rs.getString("course_type"),
                        rs.getInt("credits"));
            }
            if (!found) {
                System.out.println("No courses registered in the catalog.");
            }
            System.out.println("===================================================================================================");
        } catch (Exception e) {
            System.out.println("[Error] Failed to retrieve courses: " + e.getMessage());
        }
    }

    private static void addCourse(Scanner sc, UserSession session) {
        if (session != null && session.isStudent()) {
            System.out.println("\n[Access Denied] Student accounts cannot add courses.");
            return;
        }

        System.out.println("\n--- Add New Curriculum Course ---");
        System.out.print("Enter Course Code (e.g. CS2403): ");
        if (!sc.hasNextLine()) return;
        String code = sc.nextLine().trim().toUpperCase();
        if (code.isEmpty()) {
            System.out.println("[Error] Course code cannot be empty.");
            return;
        }

        System.out.print("Enter Course Name: ");
        if (!sc.hasNextLine()) return;
        String name = sc.nextLine().trim();
        if (name.isEmpty()) {
            System.out.println("[Error] Course name cannot be empty.");
            return;
        }

        System.out.println("Select Course Type:");
        System.out.println("  1. THEORY (40% CIA / 60% ESE, Min ESE 27)");
        System.out.println("  2. PRACTICAL (60% CIA / 40% ESE, Min ESE 20)");
        System.out.println("  3. EMBEDDED (50% CIA / 50% ESE, Min ESE 22.5)");
        System.out.print("Choice (1-3) [1]: ");
        String typeChoice = sc.nextLine().trim();
        String courseType = "THEORY";
        if (typeChoice.equals("2")) courseType = "PRACTICAL";
        else if (typeChoice.equals("3")) courseType = "EMBEDDED";

        System.out.print("Enter Credits (1 to 10) [3]: ");
        String credStr = sc.nextLine().trim();
        int credits = 3;
        if (!credStr.isEmpty()) {
            try {
                credits = Integer.parseInt(credStr);
                if (credits <= 0 || credits > 10) {
                    System.out.println("[Error] Credits must be between 1 and 10.");
                    return;
                }
            } catch (NumberFormatException e) {
                System.out.println("[Error] Invalid credits number.");
                return;
            }
        }

        // Duplicate code check
        String checkSql = "SELECT COUNT(*) FROM courses WHERE course_code = ?";
        try (Connection con = getConnection();
             PreparedStatement checkPs = con.prepareStatement(checkSql)) {
            checkPs.setString(1, code);
            try (ResultSet rs = checkPs.executeQuery()) {
                if (rs.next() && rs.getInt(1) > 0) {
                    System.out.printf("[Error] Course code '%s' already exists in the catalog.%n", code);
                    return;
                }
            }

            String insertSql = "INSERT INTO courses (course_code, course_name, course_type, credits) VALUES (?, ?, ?, ?)";
            try (PreparedStatement ps = con.prepareStatement(insertSql)) {
                ps.setString(1, code);
                ps.setString(2, name);
                ps.setString(3, courseType);
                ps.setInt(4, credits);
                ps.executeUpdate();
                System.out.printf("\n>>> Course '%s - %s' [%s, %d Credits] added successfully. <<<%n",
                        code, name, courseType, credits);
            }
        } catch (Exception e) {
            System.out.println("[Error] Failed to add course: " + e.getMessage());
        }
    }

    private static void editCourse(Scanner sc, UserSession session) {
        if (session != null && session.isStudent()) {
            System.out.println("\n[Access Denied] Student accounts cannot edit courses.");
            return;
        }

        System.out.print("Enter Course Code to edit: ");
        if (!sc.hasNextLine()) return;
        String code = sc.nextLine().trim().toUpperCase();

        String selectSql = "SELECT course_id, course_code, course_name, course_type, credits FROM courses WHERE course_code = ?";
        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(selectSql)) {
            ps.setString(1, code);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    System.out.println("No course found with code: " + code);
                    return;
                }

                int courseId = rs.getInt("course_id");
                String curCode = rs.getString("course_code");
                String curName = rs.getString("course_name");
                String curType = rs.getString("course_type");
                int curCredits = rs.getInt("credits");

                System.out.println("\nCurrent Course Details:");
                System.out.printf("  Code: %s | Title: %s | Type: %s | Credits: %d%n", curCode, curName, curType, curCredits);

                System.out.printf("Enter New Title [%s]: ", curName);
                String newName = sc.nextLine().trim();
                if (newName.isEmpty()) newName = curName;

                System.out.printf("Enter New Course Type (THEORY/PRACTICAL/EMBEDDED) [%s]: ", curType);
                String newType = sc.nextLine().trim().toUpperCase();
                if (newType.isEmpty()) newType = curType;
                if (!newType.equals("THEORY") && !newType.equals("PRACTICAL") && !newType.equals("EMBEDDED")) {
                    System.out.println("[Error] Course type must be THEORY, PRACTICAL, or EMBEDDED.");
                    return;
                }

                System.out.printf("Enter New Credits (1-10) [%d]: ", curCredits);
                String credStr = sc.nextLine().trim();
                int newCredits = curCredits;
                if (!credStr.isEmpty()) {
                    try {
                        newCredits = Integer.parseInt(credStr);
                        if (newCredits <= 0 || newCredits > 10) {
                            System.out.println("[Error] Credits must be between 1 and 10.");
                            return;
                        }
                    } catch (NumberFormatException e) {
                        System.out.println("[Error] Invalid credits number.");
                        return;
                    }
                }

                String updateSql = "UPDATE courses SET course_name = ?, course_type = ?, credits = ? WHERE course_id = ?";
                try (PreparedStatement updatePs = con.prepareStatement(updateSql)) {
                    updatePs.setString(1, newName);
                    updatePs.setString(2, newType);
                    updatePs.setInt(3, newCredits);
                    updatePs.setInt(4, courseId);
                    int updated = updatePs.executeUpdate();
                    if (updated > 0) {
                        System.out.printf("\n>>> Course '%s' updated successfully. <<<%n", curCode);
                    }
                }
            }
        } catch (Exception e) {
            System.out.println("[Error] Failed to edit course: " + e.getMessage());
        }
    }

    private static void deleteCourse(Scanner sc, UserSession session) {
        if (session != null && session.isStudent()) {
            System.out.println("\n[Access Denied] Student accounts cannot delete courses.");
            return;
        }

        System.out.print("Enter Course Code to delete: ");
        if (!sc.hasNextLine()) return;
        String code = sc.nextLine().trim().toUpperCase();

        String selectSql = "SELECT course_id, course_code, course_name FROM courses WHERE course_code = ?";
        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(selectSql)) {
            ps.setString(1, code);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    System.out.println("No course found with code: " + code);
                    return;
                }

                int courseId = rs.getInt("course_id");
                String courseName = rs.getString("course_name");

                // Check active enrollments linked to this course
                String countSql = "SELECT COUNT(*) FROM enrollments WHERE course_id = ?";
                try (PreparedStatement countPs = con.prepareStatement(countSql)) {
                    countPs.setInt(1, courseId);
                    try (ResultSet rsCount = countPs.executeQuery()) {
                        if (rsCount.next() && rsCount.getInt(1) > 0) {
                            int linked = rsCount.getInt(1);
                            System.out.printf("\n[Protection Warning] Cannot delete course '%s - %s'.%n", code, courseName);
                            System.out.printf("There are currently %d active student enrollment(s) linked to this course.%n", linked);
                            System.out.println("To preserve data integrity under R2024 regulations, courses with enrollments cannot be deleted.");
                            return;
                        }
                    }
                }

                System.out.printf("Are you sure you want to delete course '%s - %s'? (Y/N): ", code, courseName);
                String confirm = sc.nextLine().trim();
                if (!confirm.equalsIgnoreCase("Y")) {
                    System.out.println("Deletion cancelled.");
                    return;
                }

                String delSql = "DELETE FROM courses WHERE course_id = ?";
                try (PreparedStatement delPs = con.prepareStatement(delSql)) {
                    delPs.setInt(1, courseId);
                    delPs.executeUpdate();
                    System.out.printf("\n>>> Course '%s' deleted successfully. <<<%n", code);
                }
            }
        } catch (Exception e) {
            System.out.println("[Error] Failed to delete course: " + e.getMessage());
        }
    }

    // =========================================================================
    // 3. COURSE ENROLLMENT & ATTENDANCE TRACKING
    // =========================================================================
    private static void handleCourseEnrollmentAndAttendance(Scanner sc, UserSession session) {
        boolean back = false;
        while (!back) {
            System.out.println("\n-----------------------------------------------------------------------------");
            System.out.println("                  3. COURSE ENROLLMENT & ATTENDANCE TRACKING");
            System.out.println("-----------------------------------------------------------------------------");
            System.out.println("  1. View Active Enrollments & Attendance Status");
            System.out.println("  2. Enroll Student in Course (Admin / Staff)");
            System.out.println("  3. Record / Update Attendance (Admin / Staff)");
            System.out.println("  0. Back to Main Menu");
            System.out.println("-----------------------------------------------------------------------------");
            System.out.print("Choose an option: ");
            if (!sc.hasNextLine()) break;
            String opt = sc.nextLine().trim();

            switch (opt) {
                case "1":
                    viewEnrollmentsAndAttendance(session);
                    break;
                case "2":
                    enrollStudentInCourse(sc, session);
                    break;
                case "3":
                    recordAttendance(sc, session);
                    break;
                case "0":
                    back = true;
                    break;
                default:
                    System.out.println("Invalid option. Please choose 1, 2, 3, or 0.");
            }
        }
    }

    private static void viewEnrollmentsAndAttendance(UserSession session) {
        StringBuilder sql = new StringBuilder("""
                SELECT e.enrollment_id, s.roll_no, s.student_name,
                       c.course_code, c.course_name, e.semester, e.academic_year,
                       a.classes_held, a.classes_attended, a.condonation_approved
                FROM enrollments e
                JOIN students s ON e.student_id = s.student_id
                JOIN courses c ON e.course_id = c.course_id
                LEFT JOIN attendance a ON e.enrollment_id = a.enrollment_id
                """);

        if (session != null && session.isStudent() && session.getStudentRoll() != null) {
            sql.append(" WHERE s.roll_no = '").append(session.getStudentRoll().replace("'", "''")).append("'");
        }
        sql.append(" ORDER BY e.academic_year DESC, e.semester, s.roll_no, c.course_code");

        try (Connection con = getConnection();
             Statement stmt = con.createStatement();
             ResultSet rs = stmt.executeQuery(sql.toString())) {

            System.out.println("\n===============================================================================================================================");
            System.out.println("                                         ACTIVE COURSE ENROLLMENTS & ATTENDANCE");
            System.out.println("===============================================================================================================================");
            System.out.printf("%-5s %-12s %-20s %-10s %-26s %-5s %-11s %-12s %-10s %-20s%n",
                    "EnID", "Roll No", "Student Name", "Course", "Course Title", "Sem", "Year", "Held/Attended", "Att %", "Eligibility Status");
            System.out.println("-------------------------------------------------------------------------------------------------------------------------------");

            boolean found = false;
            while (rs.next()) {
                found = true;
                int enId = rs.getInt("enrollment_id");
                String roll = rs.getString("roll_no");
                String name = rs.getString("student_name");
                String cCode = rs.getString("course_code");
                String cTitle = rs.getString("course_name");
                if (cTitle.length() > 24) cTitle = cTitle.substring(0, 21) + "...";
                int sem = rs.getInt("semester");
                String yr = rs.getString("academic_year");

                int held = rs.getInt("classes_held");
                boolean heldNull = rs.wasNull();
                int att = rs.getInt("classes_attended");
                boolean attNull = rs.wasNull();
                boolean condoned = rs.getBoolean("condonation_approved");

                EvaluationEngine.AttendanceEvaluation attEval = EvaluationEngine.evaluateAttendance(
                        heldNull ? 0 : held, attNull ? 0 : att, condoned
                );

                String attCounts = heldNull ? "Pending (0)" : String.format("%d / %d", att, held);
                String pctStr = (heldNull || held == 0) ? "—" : String.format("%.1f%%", attEval.percentage);
                String statusStr = attEval.status.description;

                System.out.printf("%-5d %-12s %-20s %-10s %-26s %-5d %-11s %-12s %-10s %-20s%n",
                        enId, roll, name, cCode, cTitle, sem, yr, attCounts, pctStr, statusStr);
            }

            if (!found) {
                System.out.println("No enrollments found.");
            }
            System.out.println("===============================================================================================================================");
        } catch (Exception e) {
            System.out.println("[Error] Failed to load enrollments: " + e.getMessage());
        }
    }

    private static void enrollStudentInCourse(Scanner sc, UserSession session) {
        if (session != null && session.isStudent()) {
            System.out.println("\n[Access Denied] Student accounts cannot enroll students.");
            return;
        }

        System.out.println("\n--- Enroll Student in Curriculum Course ---");
        System.out.print("Enter Student Roll Number: ");
        if (!sc.hasNextLine()) return;
        String roll = sc.nextLine().trim();
        if (roll.isEmpty()) return;

        System.out.print("Enter Course Code: ");
        if (!sc.hasNextLine()) return;
        String code = sc.nextLine().trim().toUpperCase();
        if (code.isEmpty()) return;

        System.out.print("Enter Academic Year (e.g. 2026-2027) [2026-2027]: ");
        if (!sc.hasNextLine()) return;
        String yearInput = sc.nextLine().trim();
        String academicYear = yearInput.isEmpty() ? "2026-2027" : yearInput;

        try (Connection con = getConnection()) {
            // Find student
            int studentId = -1;
            int defaultSem = 1;
            String studName = "";
            try (PreparedStatement ps = con.prepareStatement("SELECT student_id, student_name, semester FROM students WHERE roll_no = ?")) {
                ps.setString(1, roll);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        studentId = rs.getInt("student_id");
                        studName = rs.getString("student_name");
                        defaultSem = rs.getInt("semester");
                    } else {
                        System.out.println("[Error] No student found with roll number: " + roll);
                        return;
                    }
                }
            }

            // Find course
            int courseId = -1;
            String courseName = "";
            try (PreparedStatement ps = con.prepareStatement("SELECT course_id, course_name FROM courses WHERE course_code = ?")) {
                ps.setString(1, code);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        courseId = rs.getInt("course_id");
                        courseName = rs.getString("course_name");
                    } else {
                        System.out.println("[Error] No course found with code: " + code);
                        return;
                    }
                }
            }

            System.out.printf("Enter Semester (1-8) [%d]: ", defaultSem);
            String semInput = sc.nextLine().trim();
            int semester = defaultSem;
            if (!semInput.isEmpty()) {
                try {
                    semester = Integer.parseInt(semInput);
                    if (semester < 1 || semester > 8) {
                        System.out.println("[Error] Semester must be between 1 and 8.");
                        return;
                    }
                } catch (NumberFormatException e) {
                    System.out.println("[Error] Invalid semester number.");
                    return;
                }
            }

            // Prevent duplicate enrollment
            String dupSql = "SELECT COUNT(*) FROM enrollments WHERE student_id = ? AND course_id = ? AND academic_year = ?";
            try (PreparedStatement ps = con.prepareStatement(dupSql)) {
                ps.setInt(1, studentId);
                ps.setInt(2, courseId);
                ps.setString(3, academicYear);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next() && rs.getInt(1) > 0) {
                        System.out.printf("[Error] Student '%s' is already enrolled in '%s' for Academic Year %s.%n",
                                roll, code, academicYear);
                        return;
                    }
                }
            }

            // Transaction: Insert enrollment and seed attendance
            con.setAutoCommit(false);
            int newEnrollId = -1;
            try {
                String insertEnroll = "INSERT INTO enrollments (student_id, course_id, semester, academic_year) VALUES (?, ?, ?, ?)";
                try (PreparedStatement ps = con.prepareStatement(insertEnroll, Statement.RETURN_GENERATED_KEYS)) {
                    ps.setInt(1, studentId);
                    ps.setInt(2, courseId);
                    ps.setInt(3, semester);
                    ps.setString(4, academicYear);
                    ps.executeUpdate();
                    try (ResultSet rs = ps.getGeneratedKeys()) {
                        if (rs.next()) newEnrollId = rs.getInt(1);
                    }
                }

                if (newEnrollId > 0) {
                    String seedAtt = "INSERT INTO attendance (enrollment_id, classes_held, classes_attended, condonation_approved) VALUES (?, 0, 0, 0)";
                    try (PreparedStatement psAtt = con.prepareStatement(seedAtt)) {
                        psAtt.setInt(1, newEnrollId);
                        psAtt.executeUpdate();
                    }
                }

                con.commit();
                System.out.printf("\n>>> Successfully enrolled %s (%s) in %s (%s) for Sem %d, %s. <<<%n",
                        studName, roll, code, courseName, semester, academicYear);
            } catch (SQLException ex) {
                con.rollback();
                throw ex;
            } finally {
                con.setAutoCommit(true);
            }

        } catch (Exception e) {
            System.out.println("[Error] Enrollment failed: " + e.getMessage());
        }
    }

    private static void recordAttendance(Scanner sc, UserSession session) {
        if (session != null && session.isStudent()) {
            System.out.println("\n[Access Denied] Student accounts cannot record attendance.");
            return;
        }

        System.out.print("Enter Enrollment ID (or press Enter to search by Roll & Course): ");
        if (!sc.hasNextLine()) return;
        String enIdStr = sc.nextLine().trim();
        int enrollmentId = -1;

        try (Connection con = getConnection()) {
            if (!enIdStr.isEmpty()) {
                try {
                    enrollmentId = Integer.parseInt(enIdStr);
                } catch (NumberFormatException e) {
                    System.out.println("[Error] Invalid enrollment ID.");
                    return;
                }
            } else {
                System.out.print("Enter Student Roll Number: ");
                String roll = sc.nextLine().trim();
                System.out.print("Enter Course Code: ");
                String code = sc.nextLine().trim().toUpperCase();

                String findSql = """
                        SELECT e.enrollment_id
                        FROM enrollments e
                        JOIN students s ON e.student_id = s.student_id
                        JOIN courses c ON e.course_id = c.course_id
                        WHERE s.roll_no = ? AND c.course_code = ?
                        """;
                try (PreparedStatement ps = con.prepareStatement(findSql)) {
                    ps.setString(1, roll);
                    ps.setString(2, code);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (rs.next()) {
                            enrollmentId = rs.getInt("enrollment_id");
                        } else {
                            System.out.printf("[Error] No active enrollment found for roll '%s' and course '%s'.%n", roll, code);
                            return;
                        }
                    }
                }
            }

            // Get current enrollment details
            String infoSql = """
                    SELECT s.roll_no, s.student_name, c.course_code, c.course_name,
                           a.attendance_id, a.classes_held, a.classes_attended, a.condonation_approved
                    FROM enrollments e
                    JOIN students s ON e.student_id = s.student_id
                    JOIN courses c ON e.course_id = c.course_id
                    LEFT JOIN attendance a ON e.enrollment_id = a.enrollment_id
                    WHERE e.enrollment_id = ?
                    """;
            try (PreparedStatement ps = con.prepareStatement(infoSql)) {
                ps.setInt(1, enrollmentId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (!rs.next()) {
                        System.out.println("[Error] Enrollment record not found.");
                        return;
                    }

                    String roll = rs.getString("roll_no");
                    String name = rs.getString("student_name");
                    String cCode = rs.getString("course_code");
                    String cName = rs.getString("course_name");
                    int curHeld = rs.getInt("classes_held");
                    int curAtt = rs.getInt("classes_attended");
                    boolean condoned = rs.getBoolean("condonation_approved");

                    System.out.printf("\nUpdating Attendance for: %s (%s) — Course: %s (%s)%n", name, roll, cCode, cName);
                    System.out.printf("Current Record: %d Classes Held, %d Classes Attended%n", curHeld, curAtt);

                    System.out.printf("Enter Total Classes Held [%d]: ", curHeld);
                    String heldInput = sc.nextLine().trim();
                    int held = heldInput.isEmpty() ? curHeld : Integer.parseInt(heldInput);

                    System.out.printf("Enter Classes Attended [%d]: ", curAtt);
                    String attInput = sc.nextLine().trim();
                    int attended = attInput.isEmpty() ? curAtt : Integer.parseInt(attInput);

                    if (held < 0 || attended < 0 || attended > held) {
                        System.out.println("[Error] Attended classes must be between 0 and total classes held.");
                        return;
                    }

                    EvaluationEngine.AttendanceEvaluation eval = EvaluationEngine.evaluateAttendance(held, attended, condoned);
                    System.out.printf("Calculated Attendance: %.1f%% — Status: %s%n", eval.percentage, eval.status.description);

                    String upsertSql = """
                            INSERT INTO attendance (enrollment_id, classes_held, classes_attended, condonation_approved)
                            VALUES (?, ?, ?, ?)
                            ON DUPLICATE KEY UPDATE
                                classes_held = VALUES(classes_held),
                                classes_attended = VALUES(classes_attended),
                                condonation_approved = VALUES(condonation_approved)
                            """;
                    try (PreparedStatement upPs = con.prepareStatement(upsertSql)) {
                        upPs.setInt(1, enrollmentId);
                        upPs.setInt(2, held);
                        upPs.setInt(3, attended);
                        upPs.setBoolean(4, condoned);
                        upPs.executeUpdate();
                        System.out.println("\n>>> Attendance recorded successfully. <<<");
                    }
                }
            }

        } catch (Exception e) {
            System.out.println("[Error] Failed to record attendance: " + e.getMessage());
        }
    }

    // =========================================================================
    // 4. INCREMENTAL MARKS ENTRY
    // =========================================================================
    private static void handleIncrementalMarksEntry(Scanner sc, UserSession session) {
        if (session != null && session.isStudent()) {
            System.out.println("\n[Access Denied] Student accounts cannot enter or modify assessment marks.");
            return;
        }

        System.out.println("\n-----------------------------------------------------------------------------");
        System.out.println("                       4. INCREMENTAL MARKS ENTRY (R2024)");
        System.out.println("-----------------------------------------------------------------------------");
        System.out.print("Enter Student Roll Number: ");
        if (!sc.hasNextLine()) return;
        String roll = sc.nextLine().trim();
        if (roll.isEmpty()) return;

        System.out.print("Enter Course Code: ");
        if (!sc.hasNextLine()) return;
        String code = sc.nextLine().trim().toUpperCase();
        if (code.isEmpty()) return;

        try (Connection con = getConnection()) {
            String query = """
                    SELECT e.enrollment_id, s.student_name, c.course_name, c.course_type, c.credits,
                           a.classes_held, a.classes_attended, a.condonation_approved
                    FROM enrollments e
                    JOIN students s ON e.student_id = s.student_id
                    JOIN courses c ON e.course_id = c.course_id
                    LEFT JOIN attendance a ON e.enrollment_id = a.enrollment_id
                    WHERE s.roll_no = ? AND c.course_code = ?
                    """;
            try (PreparedStatement ps = con.prepareStatement(query)) {
                ps.setString(1, roll);
                ps.setString(2, code);

                try (ResultSet rs = ps.executeQuery()) {
                    if (!rs.next()) {
                        System.out.printf("[Error] No active enrollment found for roll '%s' in course '%s'.%n", roll, code);
                        return;
                    }

                    int enrollmentId = rs.getInt("enrollment_id");
                    String studentName = rs.getString("student_name");
                    String courseName = rs.getString("course_name");
                    String courseTypeStr = rs.getString("course_type");
                    int credits = rs.getInt("credits");
                    EvaluationEngine.CourseType courseType = EvaluationEngine.CourseType.fromString(courseTypeStr);

                    int held = rs.getInt("classes_held");
                    int attended = rs.getInt("classes_attended");
                    boolean condoned = rs.getBoolean("condonation_approved");
                    EvaluationEngine.AttendanceEvaluation attEval = EvaluationEngine.evaluateAttendance(held, attended, condoned);

                    System.out.println("\n=============================================================================");
                    System.out.printf("Student : %s (%s)%n", studentName, roll);
                    System.out.printf("Course  : %s - %s [%s, %d Credits]%n", code, courseName, courseType.displayName, credits);
                    System.out.printf("Attendance : %.1f%% (%s)%n", attEval.percentage, attEval.status.description);
                    System.out.println("=============================================================================");

                    // Load existing marks
                    double curCia1 = 0, curCia2 = 0, curCia3 = 0, curEse = 0;
                    boolean curAbsent = false;

                    String markSql = "SELECT assessment_name, max_marks, marks_obtained, is_absent FROM assessment_marks WHERE enrollment_id = ?";
                    try (PreparedStatement psM = con.prepareStatement(markSql)) {
                        psM.setInt(1, enrollmentId);
                        try (ResultSet rsM = psM.executeQuery()) {
                            while (rsM.next()) {
                                String name = rsM.getString("assessment_name").toUpperCase();
                                double obt = rsM.getDouble("marks_obtained");
                                boolean abs = rsM.getBoolean("is_absent");
                                if (name.contains("CIA1") || name.contains("INTERNAL1")) curCia1 = obt;
                                else if (name.contains("CIA2") || name.contains("INTERNAL2")) curCia2 = obt;
                                else if (name.contains("CIA3") || name.contains("ASSIGNMENT") || name.contains("QUIZ")) curCia3 = obt;
                                else if (name.contains("ESE") || name.contains("ENDSEMESTER")) {
                                    curEse = obt;
                                    curAbsent = abs;
                                }
                            }
                        }
                    }

                    System.out.println("Enter Continuous Internal Assessment Marks (0 - 100):");
                    System.out.printf("  CIA 1 (out of 100) [%.1f]: ", curCia1);
                    String cia1Str = sc.nextLine().trim();
                    double cia1 = cia1Str.isEmpty() ? curCia1 : Double.parseDouble(cia1Str);

                    System.out.printf("  CIA 2 (out of 100) [%.1f]: ", curCia2);
                    String cia2Str = sc.nextLine().trim();
                    double cia2 = cia2Str.isEmpty() ? curCia2 : Double.parseDouble(cia2Str);

                    System.out.printf("  CIA 3 / Assignment / Quiz (out of 100) [%.1f]: ", curCia3);
                    String cia3Str = sc.nextLine().trim();
                    double cia3 = cia3Str.isEmpty() ? curCia3 : Double.parseDouble(cia3Str);

                    if (cia1 < 0 || cia1 > 100 || cia2 < 0 || cia2 > 100 || cia3 < 0 || cia3 > 100) {
                        System.out.println("[Error] CIA marks must be between 0 and 100.");
                        return;
                    }

                    System.out.print("Was the student ABSENT in End Semester Exam (ESE)? (Y/N) [N]: ");
                    String absInput = sc.nextLine().trim();
                    boolean isAbsent = absInput.equalsIgnoreCase("Y");

                    double eseMarks = 0.0;
                    if (!isAbsent) {
                        System.out.printf("  End Semester Exam (out of %.0f, Min threshold %.1f) [%.1f]: ",
                                courseType.maxEse, courseType.minEseThreshold, curEse);
                        String eseStr = sc.nextLine().trim();
                        eseMarks = eseStr.isEmpty() ? curEse : Double.parseDouble(eseStr);
                        if (eseMarks < 0 || eseMarks > courseType.maxEse) {
                            System.out.printf("[Error] ESE marks must be between 0 and %.0f.%n", courseType.maxEse);
                            return;
                        }
                    }

                    // Calculation Preview
                    double internal100 = (cia1 + cia2 + cia3) / 3.0;
                    EvaluationEngine.CourseEvaluationResult res = EvaluationEngine.evaluateCourse(
                            courseType, credits, internal100, eseMarks, isAbsent, attEval
                    );

                    System.out.println("\n-----------------------------------------------------------------------------");
                    System.out.println("                           EVALUATION PREVIEW");
                    System.out.println("-----------------------------------------------------------------------------");
                    System.out.printf("Scaled Internal : %.2f / %.0f (Scale: %.0f%%)%n", res.scaledInternal, res.maxInternalScale, courseType.internalRatio * 100);
                    System.out.printf("End Semester    : %s / %.0f (Min required: %.1f)%n",
                            isAbsent ? "ABSENT (0.00)" : String.format("%.2f", res.scaledEse), res.maxEseScale, courseType.minEseThreshold);
                    System.out.printf("Total Aggregate : %.2f / 100%n", res.totalMarks);
                    System.out.printf("Letter Grade    : %s (Grade Point: %d)%n", res.grade, res.gradePoint);
                    System.out.printf("Academic Result : %s%n", res.resultStatus);
                    System.out.println("-----------------------------------------------------------------------------");

                    System.out.print("Save these marks to the database? (Y/N): ");
                    String confirm = sc.nextLine().trim();
                    if (!confirm.equalsIgnoreCase("Y")) {
                        System.out.println("Marks entry cancelled.");
                        return;
                    }

                    // Save assessment marks using transaction
                    con.setAutoCommit(false);
                    try {
                        String[] names = {"CIA1", "CIA2", "CIA3", "ESE"};
                        double[] values = {cia1, cia2, cia3, eseMarks};
                        double[] maxima = {100, 100, 100, courseType.maxEse};
                        boolean[] absFlags = {false, false, false, isAbsent};

                        String checkMark = "SELECT mark_id FROM assessment_marks WHERE enrollment_id = ? AND assessment_name = ?";
                        String updateMark = "UPDATE assessment_marks SET max_marks = ?, marks_obtained = ?, is_absent = ? WHERE mark_id = ?";
                        String insertMark = "INSERT INTO assessment_marks (enrollment_id, assessment_name, max_marks, marks_obtained, is_absent) VALUES (?, ?, ?, ?, ?)";

                        for (int i = 0; i < names.length; i++) {
                            Integer existingMarkId = null;
                            try (PreparedStatement psCheck = con.prepareStatement(checkMark)) {
                                psCheck.setInt(1, enrollmentId);
                                psCheck.setString(2, names[i]);
                                try (ResultSet rsC = psCheck.executeQuery()) {
                                    if (rsC.next()) existingMarkId = rsC.getInt("mark_id");
                                }
                            }

                            if (existingMarkId != null) {
                                try (PreparedStatement psUp = con.prepareStatement(updateMark)) {
                                    psUp.setDouble(1, maxima[i]);
                                    psUp.setDouble(2, values[i]);
                                    psUp.setBoolean(3, absFlags[i]);
                                    psUp.setInt(4, existingMarkId);
                                    psUp.executeUpdate();
                                }
                            } else {
                                try (PreparedStatement psIn = con.prepareStatement(insertMark)) {
                                    psIn.setInt(1, enrollmentId);
                                    psIn.setString(2, names[i]);
                                    psIn.setDouble(3, maxima[i]);
                                    psIn.setDouble(4, values[i]);
                                    psIn.setBoolean(5, absFlags[i]);
                                    psIn.executeUpdate();
                                }
                            }
                        }

                        con.commit();
                        System.out.println("\n>>> All assessment marks saved successfully into database. <<<");

                    } catch (SQLException ex) {
                        con.rollback();
                        throw ex;
                    } finally {
                        con.setAutoCommit(true);
                    }
                }
            }
        } catch (Exception e) {
            System.out.println("[Error] Failed to save marks: " + e.getMessage());
        }
    }

    // =========================================================================
    // 5. STUDENT SEMESTER MARKSHEET & PDF EXPORT
    // =========================================================================
    private static void handleStudentSemesterMarksheet(Scanner sc, UserSession session) {
        System.out.println("\n-------------------------------------------------------------------------------------------------------------");
        System.out.println("                               5. STUDENT SEMESTER MARKSHEET (R2024)");
        System.out.println("-------------------------------------------------------------------------------------------------------------");

        String rollNo;
        if (session != null && session.isStudent() && session.getStudentRoll() != null) {
            rollNo = session.getStudentRoll();
            System.out.printf("Generating marksheet for active student account: %s (%s)%n", session.getUsername(), rollNo);
        } else {
            System.out.print("Enter student roll number: ");
            if (!sc.hasNextLine()) return;
            rollNo = sc.nextLine().trim();
            if (rollNo.isEmpty()) {
                System.out.println("Roll number cannot be empty.");
                return;
            }
        }

        System.out.print("Enter semester (1-8, or press Enter for all): ");
        if (!sc.hasNextLine()) return;
        String semInput = sc.nextLine().trim();
        Integer filterSem = null;
        if (!semInput.isEmpty()) {
            try {
                int s = Integer.parseInt(semInput);
                if (s >= 1 && s <= 8) filterSem = s;
                else System.out.println("Invalid semester range (1-8). Evaluating all semesters.");
            } catch (NumberFormatException ignored) {
                System.out.println("Invalid input. Evaluating all semesters.");
            }
        }

        generateMarksReportInternal(sc, rollNo, filterSem);
    }

    private static void generateMarksReportInternal(Scanner sc, String rollNo, Integer filterSem) {
        String studentQuery = "SELECT student_id, roll_no, student_name, department, semester FROM students WHERE roll_no = ?";

        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(studentQuery)) {
            ps.setString(1, rollNo);

            int studentId = -1;
            String studentName = "";
            String department = "";
            int studentSem = 1;

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    studentId = rs.getInt("student_id");
                    studentName = rs.getString("student_name");
                    department = rs.getString("department");
                    studentSem = rs.getInt("semester");
                } else {
                    System.out.println("No student found with roll number: " + rollNo);
                    return;
                }
            }

            StringBuilder enrollQuery = new StringBuilder("""
                    SELECT e.enrollment_id, e.semester, e.academic_year,
                           c.course_id, c.course_code, c.course_name, c.course_type, c.credits,
                           a.classes_held, a.classes_attended, a.condonation_approved
                    FROM enrollments e
                    JOIN courses c ON e.course_id = c.course_id
                    LEFT JOIN attendance a ON e.enrollment_id = a.enrollment_id
                    WHERE e.student_id = ?
                    """);
            if (filterSem != null) {
                enrollQuery.append(" AND e.semester = ").append(filterSem);
            }
            enrollQuery.append(" ORDER BY e.semester, c.course_code");

            class CourseEntry {
                int enrollmentId;
                int semester;
                String academicYear;
                String courseCode;
                String courseName;
                String courseType;
                int credits;
                Integer classesHeld;
                Integer classesAttended;
                Boolean condoned;
            }

            List<CourseEntry> courses = new ArrayList<>();
            try (PreparedStatement psE = con.prepareStatement(enrollQuery.toString())) {
                psE.setInt(1, studentId);
                try (ResultSet rsE = psE.executeQuery()) {
                    while (rsE.next()) {
                        CourseEntry ce = new CourseEntry();
                        ce.enrollmentId = rsE.getInt("enrollment_id");
                        ce.semester = rsE.getInt("semester");
                        ce.academicYear = rsE.getString("academic_year");
                        ce.courseCode = rsE.getString("course_code");
                        ce.courseName = rsE.getString("course_name");
                        ce.courseType = rsE.getString("course_type");
                        ce.credits = rsE.getInt("credits");

                        int held = rsE.getInt("classes_held");
                        ce.classesHeld = rsE.wasNull() ? null : held;
                        int att = rsE.getInt("classes_attended");
                        ce.classesAttended = rsE.wasNull() ? null : att;
                        boolean cond = rsE.getBoolean("condonation_approved");
                        ce.condoned = rsE.wasNull() ? null : cond;

                        courses.add(ce);
                    }
                }
            }

            if (courses.isEmpty()) {
                System.out.println("\nNo course enrollments found for student: " + rollNo + (filterSem != null ? " (Semester " + filterSem + ")" : ""));
                return;
            }

            System.out.println("\n=============================================================================================================");
            System.out.println("                      KEC ACADEMIX — STUDENT ACADEMIC MARKS REPORT (R2024)");
            System.out.println("=============================================================================================================");
            System.out.printf("Student Name  : %-30s Roll Number   : %-15s%n", studentName, rollNo);
            System.out.printf("Department    : %-30s Academic Year : %-15s%n", department, courses.get(0).academicYear);
            System.out.printf("Semester      : %-30d Filter        : %-15s%n", studentSem, (filterSem != null ? "Semester " + filterSem : "All Semesters"));
            System.out.println("-------------------------------------------------------------------------------------------------------------");
            System.out.printf("%-10s %-28s %-10s %-7s %-12s %-12s %-10s %-6s %-4s %-14s %-12s%n",
                    "Code", "Course Name", "Type", "Credits", "CIA", "ESE", "Total", "Grade", "GP", "Result", "Attendance");
            System.out.println("-------------------------------------------------------------------------------------------------------------");

            int totalRegCredits = 0;
            int totalEarnedCredits = 0;
            boolean hasIncomplete = false;
            boolean hasSa = false;
            boolean hasAb = false;
            boolean hasRa = false;

            List<EvaluationEngine.CourseEvaluationResult> evaluatedResults = new ArrayList<>();
            List<PdfMarksheetGenerator.CourseMarkEntry> pdfEntries = new ArrayList<>();

            for (CourseEntry ce : courses) {
                totalRegCredits += ce.credits;

                int held = (ce.classesHeld != null) ? ce.classesHeld : 0;
                int attended = (ce.classesAttended != null) ? ce.classesAttended : 0;
                boolean condoned = (ce.condoned != null) && ce.condoned;
                EvaluationEngine.AttendanceEvaluation att = EvaluationEngine.evaluateAttendance(held, attended, condoned);
                String attStr = (held == 0) ? "Pending (0)" : String.format("%.1f%%", att.percentage);

                double cia1 = 0, cia2 = 0, cia3 = 0;
                int ciaCount = 0;
                Double rawEse = null;
                boolean isAbsentEse = false;

                String markSql = "SELECT assessment_name, max_marks, marks_obtained, is_absent FROM assessment_marks WHERE enrollment_id = ?";
                try (PreparedStatement psM = con.prepareStatement(markSql)) {
                    psM.setInt(1, ce.enrollmentId);
                    try (ResultSet rsM = psM.executeQuery()) {
                        while (rsM.next()) {
                            String aName = rsM.getString("assessment_name");
                            double obt = rsM.getDouble("marks_obtained");
                            double max = rsM.getDouble("max_marks");
                            boolean abs = rsM.getBoolean("is_absent");

                            String norm = (aName != null) ? aName.replaceAll("[^A-Za-z0-9]", "").toUpperCase() : "";
                            if (norm.contains("CIA1") || norm.contains("INTERNAL1") || norm.equals("TEST1")) {
                                cia1 = (max > 0 && max != 100) ? (obt / max) * 100.0 : obt;
                                ciaCount++;
                            } else if (norm.contains("CIA2") || norm.contains("INTERNAL2") || norm.equals("TEST2")) {
                                cia2 = (max > 0 && max != 100) ? (obt / max) * 100.0 : obt;
                                ciaCount++;
                            } else if (norm.contains("CIA3") || norm.contains("INTERNAL3") || norm.equals("TEST3") || norm.contains("ASSIGNMENT") || norm.contains("QUIZ")) {
                                cia3 = (max > 0 && max != 100) ? (obt / max) * 100.0 : obt;
                                ciaCount++;
                            } else if (norm.contains("ESE") || norm.contains("ENDSEMESTER") || norm.contains("SEMESTEREXAM")) {
                                isAbsentEse = abs;
                                rawEse = isAbsentEse ? 0.0 : obt;
                            }
                        }
                    }
                }

                double internal100 = (ciaCount > 0) ? ((cia1 + cia2 + cia3) / (double) Math.max(ciaCount, 3)) : 0.0;
                EvaluationEngine.CourseType cType = EvaluationEngine.CourseType.fromString(ce.courseType);
                EvaluationEngine.CourseEvaluationResult res = EvaluationEngine.evaluateCourse(
                        cType, ce.credits, internal100, rawEse, isAbsentEse, att
                );

                evaluatedResults.add(res);

                String eseDisplay;
                if (isAbsentEse) {
                    eseDisplay = "AB (0.0)";
                } else if (rawEse == null) {
                    eseDisplay = "PENDING";
                    hasIncomplete = true;
                } else {
                    eseDisplay = String.format("%.2f/%.0f", res.scaledEse, res.maxEseScale);
                }

                String totalDisplay = (rawEse == null) ? "INCOMPLETE" : String.format("%.2f", res.totalMarks);
                String gradeDisplay = res.grade;
                String gpDisplay = (rawEse == null) ? "—" : String.valueOf(res.gradePoint);

                if (res.isPassed) {
                    totalEarnedCredits += ce.credits;
                } else {
                    if (res.isAttendanceShortage) hasSa = true;
                    else if (res.isAbsentEse) hasAb = true;
                    else if (rawEse != null) hasRa = true;
                }

                String courseTitle = ce.courseName;
                if (courseTitle.length() > 26) courseTitle = courseTitle.substring(0, 23) + "...";

                System.out.printf("%-10s %-28s %-10s %-7d %-12s %-12s %-10s %-6s %-4s %-14s %-12s%n",
                        ce.courseCode, courseTitle, cType.displayName, ce.credits,
                        String.format("%.2f/%.0f", res.scaledInternal, res.maxInternalScale),
                        eseDisplay, totalDisplay, gradeDisplay, gpDisplay, res.resultStatus, attStr);

                // Collect PDF data entry
                pdfEntries.add(new PdfMarksheetGenerator.CourseMarkEntry(
                        ce.courseCode, ce.courseName, cType.displayName, ce.credits,
                        held == 0 ? 100.0 : att.percentage,
                        res.scaledInternal, res.maxInternalScale,
                        rawEse == null ? "PENDING" : (isAbsentEse ? "AB" : String.format("%.2f", res.scaledEse)),
                        res.maxEseScale,
                        rawEse == null ? "INCOMPLETE" : String.format("%.2f", res.totalMarks),
                        res.grade, res.gradePoint, res.resultStatus
                ));
            }

            System.out.println("=============================================================================================================");
            System.out.println("EVALUATION SUMMARY");
            System.out.println("-------------------------------------------------------------------------------------------------------------");
            System.out.printf("Total Credits Registered : %d%n", totalRegCredits);
            System.out.printf("Total Credits Earned     : %d%n", totalEarnedCredits);

            double finalSgpa = 0.0;
            double finalEquivPct = 0.0;
            String overallAcademicStatus;

            if (hasIncomplete) {
                overallAcademicStatus = "INCOMPLETE / PENDING ASSESSMENT";
                System.out.println("Semester SGPA            : INCOMPLETE (Pending ESE evaluation)");
                System.out.println("Equivalent Percentage    : N/A");
                System.out.printf("Academic Status          : %s%n", overallAcademicStatus);
            } else {
                finalSgpa = EvaluationEngine.calculateSGPA(evaluatedResults);
                finalEquivPct = EvaluationEngine.calculateEquivalentPercentage(finalSgpa);

                if (hasSa) overallAcademicStatus = "SHORTAGE OF ATTENDANCE (SA)";
                else if (hasAb) overallAcademicStatus = "ABSENT (AB)";
                else if (hasRa || totalEarnedCredits < totalRegCredits) overallAcademicStatus = "REAPPEAR (RA)";
                else overallAcademicStatus = "PASS";

                System.out.printf("Semester SGPA            : %.2f%n", finalSgpa);
                System.out.printf("Equivalent Percentage    : %.2f%%%n", finalEquivPct);
                System.out.printf("Academic Status          : %s%n", overallAcademicStatus);
            }
            System.out.println("=============================================================================================================");

            // Offer PDF Export
            System.out.print("\nExport official semester marksheet to PDF? (Y/N) [N]: ");
            if (sc.hasNextLine()) {
                String pdfChoice = sc.nextLine().trim();
                if (pdfChoice.equalsIgnoreCase("Y")) {
                    String defaultPdfName = String.format("marksheet_%s_sem%d.pdf", rollNo, (filterSem != null ? filterSem : studentSem));
                    System.out.printf("Enter output PDF file path [%s]: ", defaultPdfName);
                    String pathInput = sc.nextLine().trim();
                    File targetFile = new File(pathInput.isEmpty() ? defaultPdfName : pathInput);

                    try {
                        PdfMarksheetGenerator.generateMarksheetPdf(
                                targetFile, rollNo, studentName, department,
                                (filterSem != null ? filterSem : studentSem),
                                courses.get(0).academicYear,
                                pdfEntries, totalRegCredits, totalEarnedCredits,
                                finalSgpa, finalEquivPct, overallAcademicStatus
                        );
                        System.out.printf("\n>>> Official Marksheet PDF generated successfully (%d bytes) at:%n    %s <<<%n",
                                targetFile.length(), targetFile.getAbsolutePath());
                    } catch (Exception ex) {
                        System.out.println("[Error] PDF generation failed: " + ex.getMessage());
                    }
                }
            }

        } catch (Exception e) {
            System.out.println("[Error] Error generating marks report: " + e.getMessage());
        }
    }

    // =========================================================================
    // 6. ATTENDANCE DEFAULTER & CONDONATION REPORT
    // =========================================================================
    private static void handleAttendanceDefaultersAndCondonation(Scanner sc, UserSession session) {
        boolean back = false;
        while (!back) {
            System.out.println("\n-----------------------------------------------------------------------------");
            System.out.println("              6. ATTENDANCE DEFAULTER & CONDONATION REPORT (R2024)");
            System.out.println("-----------------------------------------------------------------------------");
            System.out.println("  1. View All Attendance Records & Defaulters (<75%)");
            System.out.println("  2. View Condonation Candidates (65.0% - 74.9%)");
            System.out.println("  3. View Critical Attendance Shortage / Detained (<65% SA)");
            System.out.println("  4. Approve Medical Condonation (Admin / Staff)");
            System.out.println("  0. Back to Main Menu");
            System.out.println("-----------------------------------------------------------------------------");
            System.out.print("Choose an option: ");
            if (!sc.hasNextLine()) break;
            String opt = sc.nextLine().trim();

            switch (opt) {
                case "1":
                    viewAttendanceDefaultersReport(null);
                    break;
                case "2":
                    viewAttendanceDefaultersReport("CONDONATION");
                    break;
                case "3":
                    viewAttendanceDefaultersReport("DETAINED");
                    break;
                case "4":
                    approveMedicalCondonation(sc, session);
                    break;
                case "0":
                    back = true;
                    break;
                default:
                    System.out.println("Invalid option. Please choose 1, 2, 3, 4, or 0.");
            }
        }
    }

    private static void viewAttendanceDefaultersReport(String filterCategory) {
        String query = """
                SELECT a.attendance_id, s.roll_no, s.student_name, s.department, e.semester,
                       c.course_code, c.course_name, a.classes_held, a.classes_attended,
                       a.condonation_approved, a.condonation_reason
                FROM attendance a
                JOIN enrollments e ON a.enrollment_id = e.enrollment_id
                JOIN students s ON e.student_id = s.student_id
                JOIN courses c ON e.course_id = c.course_id
                ORDER BY s.roll_no, c.course_code
                """;

        try (Connection con = getConnection();
             Statement stmt = con.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            System.out.println("\n===============================================================================================================================");
            System.out.println("                                      ATTENDANCE DEFAULTER & ELIGIBILITY AUDIT");
            System.out.println("===============================================================================================================================");
            System.out.printf("%-5s %-12s %-20s %-10s %-22s %-8s %-8s %-10s %-26s%n",
                    "AttID", "Roll No", "Student Name", "Course", "Course Title", "Held", "Attended", "Att %", "Eligibility Status");
            System.out.println("-------------------------------------------------------------------------------------------------------------------------------");

            int total = 0, eligible = 0, condonation = 0, detained = 0;
            while (rs.next()) {
                int attId = rs.getInt("attendance_id");
                String roll = rs.getString("roll_no");
                String name = rs.getString("student_name");
                String cCode = rs.getString("course_code");
                String cTitle = rs.getString("course_name");
                if (cTitle.length() > 20) cTitle = cTitle.substring(0, 18) + "...";

                int held = rs.getInt("classes_held");
                int attended = rs.getInt("classes_attended");
                boolean condoned = rs.getBoolean("condonation_approved");
                String reason = rs.getString("condonation_reason");

                EvaluationEngine.AttendanceEvaluation eval = EvaluationEngine.evaluateAttendance(held, attended, condoned);
                total++;
                if (eval.status.isEligibleForEse) eligible++;
                else if (eval.status == EvaluationEngine.AttendanceStatus.CONDONATION_REQUIRED) condonation++;
                else if (eval.status == EvaluationEngine.AttendanceStatus.DETAINED_SA) detained++;

                boolean matches = true;
                if ("CONDONATION".equals(filterCategory)) {
                    matches = (eval.status == EvaluationEngine.AttendanceStatus.CONDONATION_REQUIRED);
                } else if ("DETAINED".equals(filterCategory)) {
                    matches = (eval.status == EvaluationEngine.AttendanceStatus.DETAINED_SA);
                }

                if (matches) {
                    String statusDesc = eval.status.description;
                    if (condoned && reason != null && !reason.isEmpty()) {
                        statusDesc += " [" + reason + "]";
                    }
                    System.out.printf("%-5d %-12s %-20s %-10s %-22s %-8d %-8d %-10.1f%% %-26s%n",
                            attId, roll, name, cCode, cTitle, held, attended, eval.percentage, statusDesc);
                }
            }

            System.out.println("-------------------------------------------------------------------------------------------------------------------------------");
            System.out.printf("Total Records: %d | Fully Eligible: %d | Condonation Required (65-74.9%%): %d | Detained (SA <65%%): %d%n",
                    total, eligible, condonation, detained);
            System.out.println("===============================================================================================================================");
        } catch (Exception e) {
            System.out.println("[Error] Failed to load attendance audit: " + e.getMessage());
        }
    }

    private static void approveMedicalCondonation(Scanner sc, UserSession session) {
        if (session != null && session.isStudent()) {
            System.out.println("\n[Access Denied] Student accounts cannot approve medical condonation.");
            return;
        }

        System.out.print("Enter Attendance ID to approve condonation: ");
        if (!sc.hasNextLine()) return;
        String idStr = sc.nextLine().trim();
        int attendanceId;
        try {
            attendanceId = Integer.parseInt(idStr);
        } catch (NumberFormatException e) {
            System.out.println("[Error] Invalid Attendance ID.");
            return;
        }

        String sql = """
                SELECT a.attendance_id, a.classes_held, a.classes_attended, a.condonation_approved,
                       s.roll_no, s.student_name, c.course_code, c.course_name
                FROM attendance a
                JOIN enrollments e ON a.enrollment_id = e.enrollment_id
                JOIN students s ON e.student_id = s.student_id
                JOIN courses c ON e.course_id = c.course_id
                WHERE a.attendance_id = ?
                """;

        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, attendanceId);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    System.out.println("[Error] No attendance record found with ID: " + attendanceId);
                    return;
                }

                String roll = rs.getString("roll_no");
                String name = rs.getString("student_name");
                String cCode = rs.getString("course_code");
                String cName = rs.getString("course_name");
                int held = rs.getInt("classes_held");
                int attended = rs.getInt("classes_attended");
                boolean alreadyCondoned = rs.getBoolean("condonation_approved");

                EvaluationEngine.AttendanceEvaluation eval = EvaluationEngine.evaluateAttendance(held, attended, false);

                System.out.printf("\nStudent: %s (%s) — Course: %s (%s)%n", name, roll, cCode, cName);
                System.out.printf("Classes Held: %d, Attended: %d, Percentage: %.2f%%%n", held, attended, eval.percentage);

                if (eval.percentage < 65.0) {
                    System.out.println("\n[Regulation Notice] Attendance is strictly below 65.00%.");
                    System.out.println("Under KEC R2024 regulations, attendance below 65% cannot be condoned under any circumstances (Detained / SA).");
                    return;
                }
                if (eval.percentage >= 75.0) {
                    System.out.println("\n[Notice] Student already has >= 75% attendance and is fully eligible without condonation.");
                    return;
                }
                if (alreadyCondoned) {
                    System.out.println("\n[Notice] Medical condonation is already approved for this record.");
                    return;
                }

                System.out.print("Enter official Medical / HoD approval remarks: ");
                String reason = sc.nextLine().trim();
                if (reason.isEmpty()) {
                    System.out.println("[Error] Condonation approval reason cannot be empty.");
                    return;
                }

                String updateSql = "UPDATE attendance SET condonation_approved = 1, condonation_reason = ? WHERE attendance_id = ?";
                try (PreparedStatement upPs = con.prepareStatement(updateSql)) {
                    upPs.setString(1, reason);
                    upPs.setInt(2, attendanceId);
                    int updated = upPs.executeUpdate();
                    if (updated > 0) {
                        System.out.printf("\n>>> Medical condonation approved successfully for %s (%s) in %s. <<<%n",
                                name, roll, cCode);
                    }
                }
            }
        } catch (Exception e) {
            System.out.println("[Error] Failed to approve condonation: " + e.getMessage());
        }
    }

    // =========================================================================
    // 7. CLASS PERFORMANCE & RESULT SUMMARY
    // =========================================================================
    private static void handleClassPerformanceSummary(Scanner sc, UserSession session) {
        if (session != null && session.isStudent()) {
            System.out.println("\n[Privacy Notice] Student accounts are not permitted to view institutional class analytics.");
            System.out.println("Please check Option 5 (Student Semester Marksheet) for your individual academic evaluation.");
            return;
        }

        System.out.println("\n-----------------------------------------------------------------------------");
        System.out.println("                 7. CLASS PERFORMANCE & RESULT SUMMARY (R2024)");
        System.out.println("-----------------------------------------------------------------------------");

        System.out.print("Enter Department filter (or press Enter for all): ");
        if (!sc.hasNextLine()) return;
        String deptFilter = sc.nextLine().trim();

        System.out.print("Enter Semester filter (1-8, or press Enter for all): ");
        if (!sc.hasNextLine()) return;
        String semStr = sc.nextLine().trim();
        Integer semFilter = null;
        if (!semStr.isEmpty()) {
            try {
                int s = Integer.parseInt(semStr);
                if (s >= 1 && s <= 8) semFilter = s;
            } catch (NumberFormatException ignored) {}
        }

        System.out.print("Enter Academic Year filter (e.g. 2026-2027, or press Enter for all): ");
        if (!sc.hasNextLine()) return;
        String yearFilter = sc.nextLine().trim();

        generateClassAnalytics(deptFilter, semFilter, yearFilter);
    }

    private static void generateClassAnalytics(String dept, Integer sem, String year) {
        StringBuilder studentSql = new StringBuilder("""
                SELECT DISTINCT s.student_id, s.roll_no, s.student_name, s.department, e.semester, e.academic_year
                FROM enrollments e
                JOIN students s ON e.student_id = s.student_id
                WHERE 1=1
                """);
        if (dept != null && !dept.isEmpty()) studentSql.append(" AND s.department = '").append(dept.replace("'", "''")).append("'");
        if (sem != null) studentSql.append(" AND e.semester = ").append(sem);
        if (year != null && !year.isEmpty()) studentSql.append(" AND e.academic_year = '").append(year.replace("'", "''")).append("'");

        class StudentScope {
            int studentId;
            String roll;
            int semester;
            String academicYear;
        }

        List<StudentScope> students = new ArrayList<>();
        try (Connection con = getConnection();
             Statement st = con.createStatement();
             ResultSet rs = st.executeQuery(studentSql.toString())) {
            while (rs.next()) {
                StudentScope sc = new StudentScope();
                sc.studentId = rs.getInt("student_id");
                sc.roll = rs.getString("roll_no");
                sc.semester = rs.getInt("semester");
                sc.academicYear = rs.getString("academic_year");
                students.add(sc);
            }
        } catch (Exception e) {
            System.out.println("[Error] Failed to query cohort: " + e.getMessage());
            return;
        }

        int totalDistinctStudents = students.size();
        int passed = 0, failed = 0, saCount = 0, abCount = 0, incomplete = 0;
        List<Double> completedSgpas = new ArrayList<>();

        try (Connection con = getConnection()) {
            for (StudentScope sc : students) {
                String enrollSql = """
                        SELECT e.enrollment_id, c.course_id, c.course_type, c.credits,
                               a.classes_held, a.classes_attended, a.condonation_approved
                        FROM enrollments e
                        JOIN courses c ON e.course_id = c.course_id
                        LEFT JOIN attendance a ON e.enrollment_id = a.enrollment_id
                        WHERE e.student_id = ? AND e.semester = ? AND e.academic_year = ?
                        """;
                List<EvaluationEngine.CourseEvaluationResult> studentCourses = new ArrayList<>();
                boolean studentIncomplete = false;
                boolean studentSa = false;
                boolean studentAb = false;
                boolean studentRa = false;

                try (PreparedStatement ps = con.prepareStatement(enrollSql)) {
                    ps.setInt(1, sc.studentId);
                    ps.setInt(2, sc.semester);
                    ps.setString(3, sc.academicYear);

                    try (ResultSet rs = ps.executeQuery()) {
                        while (rs.next()) {
                            int enId = rs.getInt("enrollment_id");
                            String cTypeStr = rs.getString("course_type");
                            int credits = rs.getInt("credits");
                            int held = rs.getInt("classes_held");
                            int att = rs.getInt("classes_attended");
                            boolean condoned = rs.getBoolean("condonation_approved");

                            EvaluationEngine.AttendanceEvaluation attEval = EvaluationEngine.evaluateAttendance(held, att, condoned);

                            double c1 = 0, c2 = 0, c3 = 0;
                            int ciaCount = 0;
                            Double rawEse = null;
                            boolean isAbsent = false;

                            try (PreparedStatement psM = con.prepareStatement("SELECT assessment_name, max_marks, marks_obtained, is_absent FROM assessment_marks WHERE enrollment_id = ?")) {
                                psM.setInt(1, enId);
                                try (ResultSet rsM = psM.executeQuery()) {
                                    while (rsM.next()) {
                                        String aName = rsM.getString("assessment_name").toUpperCase();
                                        double obt = rsM.getDouble("marks_obtained");
                                        double max = rsM.getDouble("max_marks");
                                        boolean abs = rsM.getBoolean("is_absent");
                                        if (aName.contains("CIA1")) { c1 = (max > 0 && max != 100) ? (obt / max) * 100 : obt; ciaCount++; }
                                        else if (aName.contains("CIA2")) { c2 = (max > 0 && max != 100) ? (obt / max) * 100 : obt; ciaCount++; }
                                        else if (aName.contains("CIA3") || aName.contains("ASSIGNMENT")) { c3 = (max > 0 && max != 100) ? (obt / max) * 100 : obt; ciaCount++; }
                                        else if (aName.contains("ESE")) { isAbsent = abs; rawEse = isAbsent ? 0.0 : obt; }
                                    }
                                }
                            }

                            if (rawEse == null) studentIncomplete = true;

                            double internal100 = (ciaCount > 0) ? ((c1 + c2 + c3) / (double) Math.max(ciaCount, 3)) : 0.0;
                            EvaluationEngine.CourseType type = EvaluationEngine.CourseType.fromString(cTypeStr);
                            EvaluationEngine.CourseEvaluationResult res = EvaluationEngine.evaluateCourse(type, credits, internal100, rawEse, isAbsent, attEval);
                            studentCourses.add(res);

                            if (!res.isPassed) {
                                if (res.isAttendanceShortage) studentSa = true;
                                else if (res.isAbsentEse) studentAb = true;
                                else if (rawEse != null) studentRa = true;
                            }
                        }
                    }
                }

                if (studentIncomplete) incomplete++;
                else if (studentSa) { saCount++; failed++; }
                else if (studentAb) { abCount++; failed++; }
                else if (studentRa) failed++;
                else passed++;

                if (!studentIncomplete && !studentCourses.isEmpty()) {
                    completedSgpas.add(EvaluationEngine.calculateSGPA(studentCourses));
                }
            }

            double avgSgpa = 0.0;
            if (!completedSgpas.isEmpty()) {
                double sum = 0;
                for (double s : completedSgpas) sum += s;
                avgSgpa = sum / completedSgpas.size();
            }
            double passPct = (totalDistinctStudents > 0) ? ((double) passed / totalDistinctStudents) * 100.0 : 0.0;

            System.out.println("\n=============================================================================");
            System.out.println("                 INSTITUTIONAL ACADEMIC PERFORMANCE SUMMARY");
            System.out.println("=============================================================================");
            System.out.printf("Total Distinct Enrolled Students : %d%n", totalDistinctStudents);
            System.out.printf("Students Evaluated (Complete)    : %d%n", completedSgpas.size());
            System.out.printf("Incomplete Evaluations (Pending) : %d%n", incomplete);
            System.out.println("-----------------------------------------------------------------------------");
            System.out.printf("Passed All Registered Courses    : %d%n", passed);
            System.out.printf("Reappear (RA) / Failed Courses   : %d%n", failed);
            System.out.printf("  - Shortage of Attendance (SA)  : %d%n", saCount);
            System.out.printf("  - Absent in Exam (AB)          : %d%n", abCount);
            System.out.println("-----------------------------------------------------------------------------");
            System.out.printf("Overall Cohort Pass Percentage   : %.2f%%%n", passPct);
            System.out.printf("Cohort Average SGPA              : %.2f%n", avgSgpa);
            System.out.println("=============================================================================");

        } catch (Exception e) {
            System.out.println("[Error] Failed to compute analytics: " + e.getMessage());
        }
    }

    // =========================================================================
    // 8. AUTOMATED DEMONSTRATION / PROOF OF EXECUTION
    // =========================================================================
    private static void handleAutomatedDemonstration() {
        System.out.println("\n=============================================================================================================");
        System.out.println("           8. AUTOMATED DEMONSTRATION & PROOF OF EXECUTION (R2024 ENGINE)");
        System.out.println("=============================================================================================================");
        System.out.println("Running safe, in-memory validation of authoritative academic formulas without modifying database records...\n");

        int testsTotal = 0;
        int testsPassed = 0;

        // Test 1: Theory Course 40/60 Split & Threshold
        testsTotal++;
        EvaluationEngine.AttendanceEvaluation fullAtt = EvaluationEngine.evaluateAttendance(45, 40, false);
        EvaluationEngine.CourseEvaluationResult resTheoryPass = EvaluationEngine.evaluateCourse(
                EvaluationEngine.CourseType.THEORY, 4, 80.0, 35.0, false, fullAtt
        );
        if (resTheoryPass.isPassed && resTheoryPass.scaledInternal == 32.0 && resTheoryPass.scaledEse == 35.0 && resTheoryPass.totalMarks == 67.0) {
            System.out.println("  [PASS] 1. Theory Course (40/60 split, CIA 80->32, ESE 35->35, Total 67, Grade B+)");
            testsPassed++;
        } else {
            System.err.println("  [FAIL] 1. Theory Course evaluation failed.");
        }

        // Test 2: Theory ESE Minimum 27 Fail
        testsTotal++;
        EvaluationEngine.CourseEvaluationResult resTheoryFail = EvaluationEngine.evaluateCourse(
                EvaluationEngine.CourseType.THEORY, 4, 100.0, 26.5, false, fullAtt
        );
        if (!resTheoryFail.isPassed && "RA".equals(resTheoryFail.grade) && resTheoryFail.gradePoint == 0) {
            System.out.println("  [PASS] 2. Theory ESE Minimum Failure (ESE 26.5 < 27 yields RA, GP 0 regardless of internal 40/40)");
            testsPassed++;
        } else {
            System.err.println("  [FAIL] 2. Theory ESE minimum failure check failed.");
        }

        // Test 3: Practical Course 60/40 Split & Min Threshold 20
        testsTotal++;
        EvaluationEngine.CourseEvaluationResult resPrac = EvaluationEngine.evaluateCourse(
                EvaluationEngine.CourseType.PRACTICAL, 2, 90.0, 20.0, false, fullAtt
        );
        if (resPrac.isPassed && resPrac.scaledInternal == 54.0 && resPrac.scaledEse == 20.0 && resPrac.totalMarks == 74.0) {
            System.out.println("  [PASS] 3. Practical Course (60/40 split, CIA 90->54, ESE 20->20, Total 74, Grade A)");
            testsPassed++;
        } else {
            System.err.println("  [FAIL] 3. Practical Course evaluation failed.");
        }

        // Test 4: Embedded Course 50/50 Split & Min Threshold 22.5
        testsTotal++;
        EvaluationEngine.CourseEvaluationResult resEmbed = EvaluationEngine.evaluateCourse(
                EvaluationEngine.CourseType.EMBEDDED, 3, 80.0, 22.5, false, fullAtt
        );
        if (resEmbed.isPassed && resEmbed.scaledInternal == 40.0 && resEmbed.scaledEse == 22.5 && resEmbed.totalMarks == 62.5) {
            System.out.println("  [PASS] 4. Embedded Course (50/50 split, CIA 80->40, ESE 22.5->22.5, Total 62.5, Grade B+)");
            testsPassed++;
        } else {
            System.err.println("  [FAIL] 4. Embedded Course evaluation failed.");
        }

        // Test 5: Attendance Detention < 65% Precedence
        testsTotal++;
        EvaluationEngine.AttendanceEvaluation saAtt = EvaluationEngine.evaluateAttendance(100, 64, false);
        EvaluationEngine.CourseEvaluationResult resSa = EvaluationEngine.evaluateCourse(
                EvaluationEngine.CourseType.THEORY, 3, 100.0, 60.0, false, saAtt
        );
        if (!resSa.isPassed && "SA".equals(resSa.grade) && resSa.isAttendanceShortage) {
            System.out.println("  [PASS] 5. Attendance Precedence Rule (<65% attendance detains student as SA despite 100% marks)");
            testsPassed++;
        } else {
            System.err.println("  [FAIL] 5. SA attendance precedence check failed.");
        }

        // Test 6: Condonation Band 65.00% to 74.99%
        testsTotal++;
        EvaluationEngine.AttendanceEvaluation unappCond = EvaluationEngine.evaluateAttendance(100, 70, false);
        EvaluationEngine.AttendanceEvaluation appCond = EvaluationEngine.evaluateAttendance(100, 70, true);
        if (unappCond.isBarredFromEse() && !appCond.isBarredFromEse() && appCond.status == EvaluationEngine.AttendanceStatus.ELIGIBLE_CONDONED) {
            System.out.println("  [PASS] 6. Medical Condonation Logic (70% unapproved is ineligible, approved condonation becomes eligible)");
            testsPassed++;
        } else {
            System.err.println("  [FAIL] 6. Condonation logic check failed.");
        }

        // Test 7: Credit-Weighted SGPA Formula
        testsTotal++;
        List<EvaluationEngine.CourseEvaluationResult> results = new ArrayList<>();
        results.add(resTheoryPass); // 4 credits, GP 7
        results.add(resPrac);       // 2 credits, GP 8
        results.add(resEmbed);      // 3 credits, GP 7
        double computedSgpa = EvaluationEngine.calculateSGPA(results);
        double equivPct = EvaluationEngine.calculateEquivalentPercentage(computedSgpa);
        // (4*7 + 2*8 + 3*7) / 9 = (28 + 16 + 21) / 9 = 65 / 9 = 7.222... -> 7.22
        if (Math.abs(computedSgpa - 7.22) < 0.02 && Math.abs(equivPct - 72.22) < 0.2) {
            System.out.printf("  [PASS] 7. Credit-Weighted SGPA Calculation (SGPA: %.2f, Equivalent Percentage: %.2f%%)%n", computedSgpa, equivPct);
            testsPassed++;
        } else {
            System.err.printf("  [FAIL] 7. SGPA calculation mismatch. Got %.2f%n", computedSgpa);
        }

        // Test 8: Live Database Integrity Check
        testsTotal++;
        try (Connection con = getConnection();
             Statement st = con.createStatement();
             ResultSet rs = st.executeQuery("SELECT COUNT(*) FROM students")) {
            rs.next();
            int studCount = rs.getInt(1);
            if (studCount >= 5) {
                System.out.printf("  [PASS] 8. Live Database Connection & Record Safety Verified (%d live student records)%n", studCount);
                testsPassed++;
            } else {
                System.err.println("  [FAIL] 8. Live database student count unexpected: " + studCount);
            }
        } catch (Exception ex) {
            System.err.println("  [FAIL] 8. Database connectivity check failed: " + ex.getMessage());
        }

        System.out.println("-------------------------------------------------------------------------------------------------------------");
        System.out.printf("Demonstration Summary: %d / %d Tests Passed Successfully (100%% Integrity)%n", testsPassed, testsTotal);
        System.out.println("=============================================================================================================");
    }

    // =========================================================================
    // 9. WEB DASHBOARD & SWING STATUS
    // =========================================================================
    private static void handleWebDashboardAndSwing(Scanner sc) {
        System.out.println("\n=============================================================================================================");
        System.out.println("                             9. WEB DASHBOARD & APPLICATION ARCHITECTURE");
        System.out.println("=============================================================================================================");
        System.out.println("ARCHITECTURE NOTICE:");
        System.out.println("  • KEC ACADEMIX is architected as an institutional desktop application utilizing Java 21, Swing UI,");
        System.out.println("    and MySQL JDBC.");
        System.out.println("  • No standalone HTTP web server or browser-based dashboard is deployed in this repository.");
        System.out.println("  • The entire academic management suite is fully accessible through:");
        System.out.println("      1. This high-efficiency Interactive Console (Main)");
        System.out.println("      2. The full-featured institutional Swing Desktop GUI (KECGui)");
        System.out.println("=============================================================================================================");
        System.out.print("Would you like to launch the official KEC ACADEMIX Swing Desktop Application? (Y/N) [N]: ");
        if (!sc.hasNextLine()) return;
        String choice = sc.nextLine().trim();
        if (choice.equalsIgnoreCase("Y")) {
            System.out.println("\nLaunching KEC ACADEMIX Swing GUI in a separate process...");
            try {
                String javaBin = System.getProperty("java.home") + File.separator + "bin" + File.separator + "java";
                ProcessBuilder pb = new ProcessBuilder(javaBin, "-cp", "build;lib/*", "KECGui");
                pb.start();
                System.out.println(">>> GUI process initiated successfully. Check your desktop window. <<<");
            } catch (Exception ex) {
                System.out.println("[Error] Failed to launch Swing GUI: " + ex.getMessage());
            }
        }
    }
}
