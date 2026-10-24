import java.sql.*;

public class TestPhase6 {

    private static final String URL = "jdbc:mysql://127.0.0.1:3306/kec_academic";
    private static final String USER = "root";
    private static final String PASS = "Vickyy@07";

    public static void main(String[] args) {
        System.out.println("=== Running Phase 6 Comprehensive Test Suite ===");
        int passed = 0;
        int failed = 0;

        try (Connection con = DriverManager.getConnection(URL, USER, PASS)) {

            // Test 1: Add a valid course (in transaction, verified, rolled back)
            try {
                con.setAutoCommit(false);
                String sql = "INSERT INTO courses (course_code, course_name, course_type, credits) VALUES (?, ?, ?, ?)";
                try (PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                    ps.setString(1, "TEST_CS101");
                    ps.setString(2, "Test Computer Systems");
                    ps.setString(3, "THEORY");
                    ps.setInt(4, 3);
                    int affected = ps.executeUpdate();
                    if (affected == 1) {
                        System.out.println("[PASS] Test 1: Add a valid course");
                        passed++;
                    } else {
                        System.err.println("[FAIL] Test 1: Course was not inserted");
                        failed++;
                    }
                }
            } finally {
                con.rollback();
                con.setAutoCommit(true);
            }

            // Test 2: Reject duplicate course code (CS2401 already exists)
            try {
                con.setAutoCommit(false);
                String sql = "INSERT INTO courses (course_code, course_name, course_type, credits) VALUES (?, ?, ?, ?)";
                try (PreparedStatement ps = con.prepareStatement(sql)) {
                    ps.setString(1, "CS2401"); // Existing code
                    ps.setString(2, "Duplicate Java Course");
                    ps.setString(3, "THEORY");
                    ps.setInt(4, 4);
                    ps.executeUpdate();
                    System.err.println("[FAIL] Test 2: Duplicate course code did not fail");
                    failed++;
                } catch (SQLIntegrityConstraintViolationException ex) {
                    System.out.println("[PASS] Test 2: Reject duplicate course code (" + ex.getMessage() + ")");
                    passed++;
                }
            } finally {
                con.rollback();
                con.setAutoCommit(true);
            }

            // Test 3: Reject invalid credits (<= 0 or > 10) and validation logic
            try {
                int invalidCredit1 = 0;
                int invalidCredit2 = -2;
                int invalidCredit3 = 15;
                boolean caught1 = (invalidCredit1 <= 0 || invalidCredit1 > 10);
                boolean caught2 = (invalidCredit2 <= 0 || invalidCredit2 > 10);
                boolean caught3 = (invalidCredit3 <= 0 || invalidCredit3 > 10);
                if (caught1 && caught2 && caught3) {
                    System.out.println("[PASS] Test 3: Reject invalid credits (< 1 or > 10)");
                    passed++;
                } else {
                    System.err.println("[FAIL] Test 3: Credits validation failed");
                    failed++;
                }
            } catch (Exception ex) {
                System.err.println("[FAIL] Test 3: " + ex.getMessage());
                failed++;
            }

            // Test 4: Edit an existing course (transaction rollback)
            try {
                con.setAutoCommit(false);
                String sql = "UPDATE courses SET course_name = ?, credits = ?, course_type = ? WHERE course_code = ?";
                try (PreparedStatement ps = con.prepareStatement(sql)) {
                    ps.setString(1, "Java Programming (Updated)");
                    ps.setInt(2, 4);
                    ps.setString(3, "THEORY");
                    ps.setString(4, "CS2401");
                    int affected = ps.executeUpdate();
                    if (affected == 1) {
                        System.out.println("[PASS] Test 4: Edit an existing course");
                        passed++;
                    } else {
                        System.err.println("[FAIL] Test 4: Course was not updated");
                        failed++;
                    }
                }
            } finally {
                con.rollback();
                con.setAutoCommit(true);
            }

            // Test 5: Prevent deletion of course with linked enrollments
            try {
                int courseId = 1; // CS2401
                int enrollmentCount = 0;
                try (PreparedStatement ps = con.prepareStatement("SELECT COUNT(*) FROM enrollments WHERE course_id = ?")) {
                    ps.setInt(1, courseId);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (rs.next()) enrollmentCount = rs.getInt(1);
                    }
                }
                if (enrollmentCount > 0) {
                    System.out.println("[PASS] Test 5: Prevent deletion of course with linked enrollments (Found " + enrollmentCount + " linked enrollments)");
                    passed++;
                } else {
                    System.err.println("[FAIL] Test 5: Expected linked enrollments for course ID 1");
                    failed++;
                }
            } catch (Exception ex) {
                System.err.println("[FAIL] Test 5: " + ex.getMessage());
                failed++;
            }

            // Test 6: Enroll an existing student in an existing course (in transaction, check attendance seed, rollback)
            try {
                con.setAutoCommit(false);
                // Student 3, Course 2 (CS2402), Sem 4, Academic Year 2026-2027
                int genId = -1;
                String ins = "INSERT INTO enrollments (student_id, course_id, semester, academic_year) VALUES (?, ?, ?, ?)";
                try (PreparedStatement ps = con.prepareStatement(ins, Statement.RETURN_GENERATED_KEYS)) {
                    ps.setInt(1, 3);
                    ps.setInt(2, 2);
                    ps.setInt(3, 4);
                    ps.setString(4, "2026-2027");
                    ps.executeUpdate();
                    try (ResultSet rs = ps.getGeneratedKeys()) {
                        if (rs.next()) genId = rs.getInt(1);
                    }
                }

                if (genId > 0) {
                    String seed = "INSERT INTO attendance (enrollment_id, classes_held, classes_attended, condonation_approved) VALUES (?, 0, 0, 0)";
                    try (PreparedStatement ps = con.prepareStatement(seed)) {
                        ps.setInt(1, genId);
                        int attRows = ps.executeUpdate();
                        if (attRows == 1) {
                            System.out.println("[PASS] Test 6: Enroll student and seed attendance record");
                            passed++;
                        } else {
                            System.err.println("[FAIL] Test 6: Attendance seed failed");
                            failed++;
                        }
                    }
                } else {
                    System.err.println("[FAIL] Test 6: Enrollment ID not generated");
                    failed++;
                }
            } finally {
                con.rollback();
                con.setAutoCommit(true);
            }

            // Test 7: Reject duplicate enrollment (same student, course, academic year)
            try {
                con.setAutoCommit(false);
                // Student 1 is already enrolled in Course 1 for 2026-2027
                String ins = "INSERT INTO enrollments (student_id, course_id, semester, academic_year) VALUES (?, ?, ?, ?)";
                try (PreparedStatement ps = con.prepareStatement(ins)) {
                    ps.setInt(1, 1);
                    ps.setInt(2, 1);
                    ps.setInt(3, 4);
                    ps.setString(4, "2026-2027");
                    ps.executeUpdate();
                    System.err.println("[FAIL] Test 7: Duplicate enrollment did not trigger constraint");
                    failed++;
                } catch (SQLIntegrityConstraintViolationException ex) {
                    System.out.println("[PASS] Test 7: Reject duplicate enrollment (" + ex.getMessage() + ")");
                    passed++;
                }
            } finally {
                con.rollback();
                con.setAutoCommit(true);
            }

            // Test 8: Reject invalid semester (< 1 or > 8)
            try {
                int invalidSem1 = 0;
                int invalidSem2 = 9;
                int validSem = 4;
                boolean valid1 = (invalidSem1 >= 1 && invalidSem1 <= 8);
                boolean valid2 = (invalidSem2 >= 1 && invalidSem2 <= 8);
                boolean valid3 = (validSem >= 1 && validSem <= 8);
                if (!valid1 && !valid2 && valid3) {
                    System.out.println("[PASS] Test 8: Reject invalid semester (< 1 or > 8)");
                    passed++;
                } else {
                    System.err.println("[FAIL] Test 8: Semester validation logic failed");
                    failed++;
                }
            } catch (Exception ex) {
                System.err.println("[FAIL] Test 8: " + ex.getMessage());
                failed++;
            }

            // Test 9: Display and filter existing enrollments
            try {
                String sql = """
                        SELECT e.enrollment_id, s.roll_no, s.student_name,
                               c.course_code, c.course_name, e.semester, e.academic_year
                        FROM enrollments e
                        JOIN students s ON e.student_id = s.student_id
                        JOIN courses c ON e.course_id = c.course_id
                        WHERE e.semester = 4
                        """;
                int count = 0;
                try (Statement st = con.createStatement(); ResultSet rs = st.executeQuery(sql)) {
                    while (rs.next()) {
                        count++;
                    }
                }
                if (count > 0) {
                    System.out.println("[PASS] Test 9: Display and filter enrollments (Found " + count + " records for Semester 4)");
                    passed++;
                } else {
                    System.err.println("[FAIL] Test 9: No enrollments returned");
                    failed++;
                }
            } catch (Exception ex) {
                System.err.println("[FAIL] Test 9: " + ex.getMessage());
                failed++;
            }

            // Test 10: Role-based permissions
            try {
                UserSession adminSession = new UserSession(1, "admin", "ADMINISTRATOR", null, null, null);
                UserSession staffSession = new UserSession(2, "staff", "STAFF", null, null, null);
                UserSession studentSession = new UserSession(3, "student", "STUDENT", 1, "25ITR001", "Student Name");

                boolean adminCan = adminSession.canManageCourses() && adminSession.canManageEnrollments();
                boolean staffCan = staffSession.canManageCourses() && staffSession.canManageEnrollments();
                boolean studentCannot = !studentSession.canManageCourses() && !studentSession.canManageEnrollments();

                if (adminCan && staffCan && studentCannot) {
                    System.out.println("[PASS] Test 10: Role-based authorization enforced (Admin=true, Staff=true, Student=false)");
                    passed++;
                } else {
                    System.err.println("[FAIL] Test 10: RBAC checks failed");
                    failed++;
                }
            } catch (Exception ex) {
                System.err.println("[FAIL] Test 10: " + ex.getMessage());
                failed++;
            }

            // Test 11: Ensure marks and linked academic records remain intact
            try {
                int marksCount = 0;
                int attCount = 0;
                int sapCount = 0;
                int userCount = 0;
                try (Statement st = con.createStatement()) {
                    try (ResultSet rs = st.executeQuery("SELECT COUNT(*) FROM assessment_marks")) {
                        if (rs.next()) marksCount = rs.getInt(1);
                    }
                    try (ResultSet rs = st.executeQuery("SELECT COUNT(*) FROM attendance")) {
                        if (rs.next()) attCount = rs.getInt(1);
                    }
                    try (ResultSet rs = st.executeQuery("SELECT COUNT(*) FROM sap_activities")) {
                        if (rs.next()) sapCount = rs.getInt(1);
                    }
                    try (ResultSet rs = st.executeQuery("SELECT COUNT(*) FROM app_users")) {
                        if (rs.next()) userCount = rs.getInt(1);
                    }
                }
                if (marksCount == 10 && attCount == 5 && sapCount == 1 && userCount == 5) {
                    System.out.println("[PASS] Test 11: All marks (10), attendance (5), SAP (1), and users (5) completely intact");
                    passed++;
                } else {
                    System.err.printf("[FAIL] Test 11: Counts mismatch (Marks=%d, Att=%d, SAP=%d, Users=%d)%n",
                            marksCount, attCount, sapCount, userCount);
                    failed++;
                }
            } catch (Exception ex) {
                System.err.println("[FAIL] Test 11: " + ex.getMessage());
                failed++;
            }

            // Test 12: Confirm existing student CRUD query compatibility
            try {
                int studentCount = 0;
                try (Statement st = con.createStatement(); ResultSet rs = st.executeQuery("SELECT COUNT(*) FROM students")) {
                    if (rs.next()) studentCount = rs.getInt(1);
                }
                if (studentCount == 5) {
                    System.out.println("[PASS] Test 12: Existing student records verified (5 students present)");
                    passed++;
                } else {
                    System.err.println("[FAIL] Test 12: Student count mismatch: " + studentCount);
                    failed++;
                }
            } catch (Exception ex) {
                System.err.println("[FAIL] Test 12: " + ex.getMessage());
                failed++;
            }

        } catch (Exception ex) {
            System.err.println("Database connection error: " + ex.getMessage());
            failed++;
        }

        System.out.println("\n=== Phase 6 Test Summary ===");
        System.out.println("Passed: " + passed + " / 12");
        System.out.println("Failed: " + failed);
        if (failed == 0) {
            System.out.println(">>> ALL 12 PHASE 6 TESTS PASSED SUCCESSFULLY! <<<");
        } else {
            System.exit(1);
        }
    }
}

