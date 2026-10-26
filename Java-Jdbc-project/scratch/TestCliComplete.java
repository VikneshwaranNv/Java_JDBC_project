import java.io.*;
import java.sql.*;
import java.util.Scanner;

/**
 * Comprehensive Automated Test Suite for KEC ACADEMIX Interactive CLI.
 * Verifies all 10 required menu operations, RBAC, input validation, and database safety.
 */
public class TestCliComplete {

    private static int passed = 0;
    private static int total = 0;

    private static void check(String desc, boolean cond, String detail) {
        total++;
        if (cond) {
            passed++;
            System.out.printf("  [PASS] Test %2d: %s%n", total, desc);
        } else {
            System.err.printf("  [FAIL] Test %2d: %s%n", total, desc);
            if (detail != null) System.err.println("         Detail: " + detail);
        }
    }

    public static void main(String[] args) {
        System.out.println("==================================================================");
        System.out.println("    KEC ACADEMIX — INTERACTIVE CLI COMPREHENSIVE TEST SUITE");
        System.out.println("==================================================================\n");

        // 1. Test Admin Login & Main Menu Rendering
        runAdminLoginAndMenuTest();

        // 2. Test Student Registration (Listing, Search, Duplicate Check)
        runStudentRegistrationTest();

        // 3. Test Course Catalog (Listing, Protection Check)
        runCourseCatalogTest();

        // 4. Test Course Enrollment & Attendance View
        runEnrollmentAndAttendanceTest();

        // 5. Test Student Semester Marksheet Generation & Evaluation Parity
        runSemesterMarksheetTest();

        // 6. Test Attendance Defaulter Report & Condonation Workflow
        runAttendanceDefaulterTest();

        // 7. Test Class Performance Summary & Institutional Analytics
        runClassPerformanceTest();

        // 8. Test Automated Demonstration Proof of Execution
        runAutomatedDemonstrationTest();

        // 9. Test Web Dashboard Architectural Explanation
        runWebDashboardStatusTest();

        // 10. Test Role-Based Access Control (Student Account Restrictions)
        runStudentRbacRestrictionTest();

        // 11. Test Graceful Exit and Navigation
        runExitTest();

        // 12. Database Integrity Verification
        runDatabaseIntegrityCheck();

        System.out.println("\n==================================================================");
        System.out.printf("Final CLI Test Summary: %d / %d Tests Passed (%.1f%%)%n",
                passed, total, ((double) passed / total) * 100.0);
        System.out.println("==================================================================");

        if (passed < total) {
            System.exit(1);
        }
    }

    public static String executeCliWithInput(String input) {
        PrintStream origOut = System.out;
        PrintStream origErr = System.err;
        InputStream origIn = System.in;

        ByteArrayOutputStream outCapture = new ByteArrayOutputStream();
        PrintStream ps = new PrintStream(outCapture);
        System.setOut(ps);
        System.setErr(ps);

        try {
            System.setIn(new ByteArrayInputStream(input.getBytes()));
            Main.main(new String[]{});
        } catch (Exception e) {
            e.printStackTrace(ps);
        } finally {
            System.setOut(origOut);
            System.setErr(origErr);
            System.setIn(origIn);
        }
        return outCapture.toString();
    }

    private static void runAdminLoginAndMenuTest() {
        // Login as test_admin_sys, then choose 0 to exit
        String input = "test_admin_sys\nAdminSecret2026!\n0\n";
        String out = executeCliWithInput(input);

        boolean hasWelcome = out.contains("Login Successful") && out.contains("test_admin_sys");
        boolean hasRole = out.contains("Role: ADMINISTRATOR");
        boolean hasMenu = out.contains("1. Student Registration") && out.contains("9. Web Dashboard & Swing Application Status") && out.contains("0. Exit Console");

        check("Admin Login & Main Menu Navigation", hasWelcome && hasRole && hasMenu,
                "Welcome: " + hasWelcome + ", Role: " + hasRole + ", Menu: " + hasMenu);
    }

    private static void runStudentRegistrationTest() {
        // Login as admin -> 1 (Student Reg) -> 1 (List All) -> 2 (Search 24CSE001) -> 0 (Back) -> 0 (Exit)
        String input = "test_admin_sys\nAdminSecret2026!\n1\n1\n2\n24CSE001\n0\n0\n";
        String out = executeCliWithInput(input);

        boolean hasList = out.contains("REGISTERED STUDENTS") && out.contains("24CSE001") && out.contains("Arun Kumar");
        boolean hasSearch = out.contains("STUDENT DETAILS") && out.contains("Department   : CSE");

        check("Student Directory Listing & Roll Number Search", hasList && hasSearch,
                "List: " + hasList + ", Search: " + hasSearch);
    }

    private static void runCourseCatalogTest() {
        // Login as admin -> 2 (Courses) -> 1 (List All) -> 4 (Delete CS2401) -> 0 (Back) -> 0 (Exit)
        // Deleting CS2401 should trigger enrollment protection!
        String input = "test_admin_sys\nAdminSecret2026!\n2\n1\n4\nCS2401\n0\n0\n";
        String out = executeCliWithInput(input);

        boolean hasCourses = out.contains("CURRICULUM COURSES") && out.contains("CS2401") && out.contains("CS2402");
        boolean hasProtection = out.contains("Cannot delete course 'CS2401") && out.contains("active student enrollment(s) linked");

        check("Course Catalog & Active Enrollment Protection Guard", hasCourses && hasProtection,
                "Courses: " + hasCourses + ", Protection: " + hasProtection);
    }

    private static void runEnrollmentAndAttendanceTest() {
        // Login as admin -> 3 (Enrollments) -> 1 (View Enrollments) -> 0 (Back) -> 0 (Exit)
        String input = "test_admin_sys\nAdminSecret2026!\n3\n1\n0\n0\n";
        String out = executeCliWithInput(input);

        boolean hasHeader = out.contains("ACTIVE COURSE ENROLLMENTS & ATTENDANCE");
        boolean hasRecords = out.contains("24CSE001") && out.contains("CS2401") && out.contains("Eligible");

        check("Course Enrollment & Attendance Record Viewing", hasHeader && hasRecords,
                "Header: " + hasHeader + ", Records: " + hasRecords);
    }

    private static void runSemesterMarksheetTest() {
        // Login as admin -> 5 (Marksheet) -> roll "24CSE001", sem "4", PDF "N" -> 0 (Exit)
        String input = "test_admin_sys\nAdminSecret2026!\n5\n24CSE001\n4\nN\n0\n";
        String out = executeCliWithInput(input);

        boolean hasHeader = out.contains("STUDENT ACADEMIC MARKS REPORT (R2024)");
        boolean hasStudent = out.contains("Arun Kumar") && out.contains("24CSE001");
        boolean hasMarks = out.contains("CS2401") && out.contains("CS2402");
        boolean hasRegCredits = out.contains("Total Credits Registered : 7");
        boolean hasIncompleteHandling = out.contains("Semester SGPA            : INCOMPLETE (Pending ESE evaluation)") &&
                                        out.contains("Academic Status          : INCOMPLETE / PENDING ASSESSMENT");

        check("Student Semester Marksheet R2024 Evaluation & Missing Marks Handling",
                hasHeader && hasStudent && hasMarks && hasRegCredits && hasIncompleteHandling,
                "Header: " + hasHeader + ", RegCredits: " + hasRegCredits + ", IncompleteHandling: " + hasIncompleteHandling);
    }

    private static void runAttendanceDefaulterTest() {
        // Login as admin -> 6 (Defaulters) -> 1 (All) -> 2 (Condonation) -> 3 (Detained) -> 0 (Back) -> 0 (Exit)
        String input = "test_admin_sys\nAdminSecret2026!\n6\n1\n2\n3\n0\n0\n";
        String out = executeCliWithInput(input);

        boolean hasAudit = out.contains("ATTENDANCE DEFAULTER & ELIGIBILITY AUDIT");
        boolean hasSummary = out.contains("Total Records:") && out.contains("Fully Eligible:");

        check("Attendance Defaulter & Eligibility Audit Reporting", hasAudit && hasSummary,
                "Audit: " + hasAudit + ", Summary: " + hasSummary);
    }

    private static void runClassPerformanceTest() {
        // Login as admin -> 7 (Performance) -> Dept "CSE", Sem "4", Year "2026-2027" -> 0 (Exit)
        String input = "test_admin_sys\nAdminSecret2026!\n7\nCSE\n4\n2026-2027\n0\n";
        String out = executeCliWithInput(input);

        boolean hasHeader = out.contains("INSTITUTIONAL ACADEMIC PERFORMANCE SUMMARY");
        boolean hasCohort = out.contains("Total Distinct Enrolled Students : 2");
        boolean hasIncompleteCohort = out.contains("Incomplete Evaluations (Pending) : 2");

        check("Institutional Class Performance Analytics & Distinct Student Counting",
                hasHeader && hasCohort && hasIncompleteCohort,
                "Header: " + hasHeader + ", Cohort: " + hasCohort + ", IncompleteCohort: " + hasIncompleteCohort);
    }

    private static void runAutomatedDemonstrationTest() {
        // Login as admin -> 8 (Demonstration) -> 0 (Exit)
        String input = "test_admin_sys\nAdminSecret2026!\n8\n0\n";
        String out = executeCliWithInput(input);

        boolean hasHeader = out.contains("AUTOMATED DEMONSTRATION & PROOF OF EXECUTION (R2024 ENGINE)");
        boolean hasRules = out.contains("Theory Course (40/60 split") &&
                           out.contains("Practical Course (60/40 split") &&
                           out.contains("Embedded Course (50/50 split") &&
                           out.contains("Attendance Precedence Rule") &&
                           out.contains("Credit-Weighted SGPA Calculation");
        boolean has100Pct = out.contains("8 / 8 Tests Passed Successfully (100% Integrity)");

        check("Automated Demonstration In-Memory Rules Engine Verification",
                hasHeader && hasRules && has100Pct,
                "Header: " + hasHeader + ", Rules: " + hasRules + ", 100%: " + has100Pct);
    }

    private static void runWebDashboardStatusTest() {
        // Login as admin -> 9 (Web Dashboard) -> N (don't launch GUI) -> 0 (Exit)
        String input = "test_admin_sys\nAdminSecret2026!\n9\nN\n0\n";
        String out = executeCliWithInput(input);

        boolean hasNotice = out.contains("ARCHITECTURE NOTICE") &&
                            out.contains("KEC ACADEMIX is architected as an institutional desktop application") &&
                            out.contains("No standalone HTTP web server or browser-based dashboard");

        check("Web Dashboard Architectural Status & Desktop GUI Option", hasNotice,
                "Notice: " + hasNotice);
    }

    private static void runStudentRbacRestrictionTest() {
        // Login as student test_stud_arun -> 1 (Student Reg) -> 3 (Register Student) -> 0 -> 4 (Marks Entry) -> 0
        String input = "test_stud_arun\nValidStudentPass123!\n1\n3\n0\n4\n7\n0\n";
        String out = executeCliWithInput(input);

        boolean regBlocked = out.contains("Student accounts cannot register new students");
        boolean marksBlocked = out.contains("Student accounts cannot enter or modify assessment marks");
        boolean analyticsBlocked = out.contains("Student accounts are not permitted to view institutional class analytics");

        check("Role-Based Access Control (Student Account Restriction Enforcement)",
                regBlocked && marksBlocked && analyticsBlocked,
                "RegBlocked: " + regBlocked + ", MarksBlocked: " + marksBlocked + ", AnalyticsBlocked: " + analyticsBlocked);
    }

    private static void runExitTest() {
        // Guest login -> 0 (Exit)
        String input = "guest\n0\n";
        String out = executeCliWithInput(input);

        boolean hasExit = out.contains("Thank you for using KEC ACADEMIX. Session closed safely.");
        check("Clean Console Session Exit", hasExit, "Exit: " + hasExit);
    }

    private static void runDatabaseIntegrityCheck() {
        boolean countsMatch = true;
        try (Connection con = AuthService.getConnection()) {
            int users = countRows(con, "app_users");
            int students = countRows(con, "students");
            int courses = countRows(con, "courses");
            int enrollments = countRows(con, "enrollments");
            int marks = countRows(con, "assessment_marks");
            int att = countRows(con, "attendance");

            countsMatch = (users == 5 && students == 5 && courses == 2 &&
                           enrollments == 5 && marks == 10 && att == 5);

            check("Live Database Baseline Records 100% Preserved (Zero Data Loss)", countsMatch,
                    String.format("Users=%d (5), Studs=%d (5), Courses=%d (2), Enrolls=%d (5), Marks=%d (10), Att=%d (5)",
                            users, students, courses, enrollments, marks, att));
        } catch (Exception ex) {
            check("Live Database Baseline Records 100% Preserved (Zero Data Loss)", false, ex.getMessage());
        }
    }

    private static int countRows(Connection con, String table) throws SQLException {
        try (Statement st = con.createStatement();
             ResultSet rs = st.executeQuery("SELECT COUNT(*) FROM " + table)) {
            rs.next();
            return rs.getInt(1);
        }
    }
}
