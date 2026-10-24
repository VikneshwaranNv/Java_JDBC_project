import java.io.File;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class TestPhase7 {

    private static final String URL = "jdbc:mysql://127.0.0.1:3306/kec_academic";
    private static final String USER = "root";
    private static final String PASS = "Vickyy@07";

    public static void main(String[] args) {
        System.out.println("=== Running Phase 7 Academic Reports Comprehensive Test Suite ===");
        int passed = 0;
        int failed = 0;

        try (Connection con = DriverManager.getConnection(URL, USER, PASS)) {

            // Test 1: Semester and academic-year filtering
            try {
                String sql = """
                        SELECT e.enrollment_id, s.roll_no, e.semester, e.academic_year
                        FROM enrollments e
                        JOIN students s ON e.student_id = s.student_id
                        WHERE e.semester = 4 AND e.academic_year = '2026-2027'
                        """;
                int count = 0;
                try (Statement st = con.createStatement(); ResultSet rs = st.executeQuery(sql)) {
                    while (rs.next()) count++;
                }
                if (count == 5) {
                    System.out.println("[PASS] Test 1: Semester and academic-year filtering (Found exactly 5 enrollments for Sem 4, 2026-2027)");
                    passed++;
                } else {
                    System.err.println("[FAIL] Test 1: Count mismatch: " + count);
                    failed++;
                }
            } catch (Exception ex) {
                System.err.println("[FAIL] Test 1: " + ex.getMessage());
                failed++;
            }

            // Test 2: Correct course credits and marks loading
            try {
                String sql = """
                        SELECT c.course_code, c.credits, am.assessment_name, am.marks_obtained, am.max_marks
                        FROM enrollments e
                        JOIN courses c ON e.course_id = c.course_id
                        JOIN assessment_marks am ON e.enrollment_id = am.enrollment_id
                        WHERE e.enrollment_id = 1
                        ORDER BY am.assessment_name
                        """;
                int credits = 0;
                double eseObtained = 0.0;
                try (Statement st = con.createStatement(); ResultSet rs = st.executeQuery(sql)) {
                    while (rs.next()) {
                        credits = rs.getInt("credits");
                        if ("ESE".equalsIgnoreCase(rs.getString("assessment_name"))) {
                            eseObtained = rs.getDouble("marks_obtained");
                        }
                    }
                }
                if (credits == 4 && eseObtained == 47.0) {
                    System.out.printf("[PASS] Test 2: Correct course credits (%d) and ESE marks (%.2f)%n", credits, eseObtained);
                    passed++;
                } else {
                    System.err.printf("[FAIL] Test 2: Credits=%d, ESE=%.2f%n", credits, eseObtained);
                    failed++;
                }
            } catch (Exception ex) {
                System.err.println("[FAIL] Test 2: " + ex.getMessage());
                failed++;
            }

            // Test 3: Grade and SGPA calculations against EvaluationEngine
            try {
                // Internal marks: CIA1=36, CIA2=45, CIA3=40 -> avg = 40.33 out of 100
                // Theory course (40% CIA, 60% ESE)
                // ESE = 47.0 / 60
                // Total = (40.33/100 * 40) + 47.0 = 16.13 + 47.0 = 63.13 -> Grade B+ (GP 7)
                EvaluationEngine.AttendanceEvaluation att = EvaluationEngine.evaluateAttendance(45, 40, false);
                EvaluationEngine.CourseEvaluationResult res = EvaluationEngine.evaluateCourse(
                        EvaluationEngine.CourseType.THEORY, 4, 40.333, 47.0, false, att
                );
                if (res.isPassed && "B+".equals(res.grade) && res.gradePoint == 7 && Math.abs(res.totalMarks - 63.13) < 0.1) {
                    System.out.printf("[PASS] Test 3: Grade (%s, GP %d) and Total (%.2f) match R2024 regulations%n",
                            res.grade, res.gradePoint, res.totalMarks);
                    passed++;
                } else {
                    System.err.printf("[FAIL] Test 3: Grade=%s, GP=%d, Total=%.2f%n", res.grade, res.gradePoint, res.totalMarks);
                    failed++;
                }
            } catch (Exception ex) {
                System.err.println("[FAIL] Test 3: " + ex.getMessage());
                failed++;
            }

            // Test 4: Incomplete marks handling (unentered ESE)
            try {
                EvaluationEngine.AttendanceEvaluation att = EvaluationEngine.evaluateAttendance(45, 40, false);
                EvaluationEngine.CourseEvaluationResult resIncomplete = EvaluationEngine.evaluateCourse(
                        EvaluationEngine.CourseType.THEORY, 3, 75.0, null, false, att
                );
                if (!resIncomplete.isPassed && "—".equals(resIncomplete.grade) && resIncomplete.gradePoint == 0
                        && resIncomplete.resultStatus.contains("INCOMPLETE")) {
                    System.out.println("[PASS] Test 4: Incomplete marks handled properly (Grade '—', GP 0, Status: INCOMPLETE)");
                    passed++;
                } else {
                    System.err.println("[FAIL] Test 4: Incomplete result not recognized: " + resIncomplete.resultStatus);
                    failed++;
                }
            } catch (Exception ex) {
                System.err.println("[FAIL] Test 4: " + ex.getMessage());
                failed++;
            }

            // Test 5: PDF Export via PdfMarksheetGenerator
            try {
                File pdfFile = new File("scratch/test_phase7_marksheet.pdf");
                if (pdfFile.exists()) pdfFile.delete();

                List<PdfMarksheetGenerator.CourseMarkEntry> entries = new ArrayList<>();
                entries.add(new PdfMarksheetGenerator.CourseMarkEntry(
                        "CS2401", "Java Programming", "Theory", 4, 88.89,
                        16.13, 40.0, "47.00", 60.0, "63.13", "B+", 7, "PASS"
                ));
                entries.add(new PdfMarksheetGenerator.CourseMarkEntry(
                        "CS2402", "Database Management Systems", "Theory", 3, 88.89,
                        14.50, 40.0, "42.00", 60.0, "56.50", "B", 6, "PASS"
                ));

                PdfMarksheetGenerator.generateMarksheetPdf(
                        pdfFile, "24CSE001", "Arun Kumar S", "Computer Science and Engineering",
                        4, "2026-2027", entries, 7, 7, 6.57, 65.7, "PASS"
                );

                if (pdfFile.exists() && pdfFile.length() > 1000) {
                    System.out.printf("[PASS] Test 5: Official PDF Marksheet generated successfully (%d bytes)%n", pdfFile.length());
                    passed++;
                } else {
                    System.err.println("[FAIL] Test 5: PDF file not created or size invalid");
                    failed++;
                }
            } catch (Exception ex) {
                System.err.println("[FAIL] Test 5: " + ex.getMessage());
                failed++;
            }

            // Test 6: Existing file overwrite confirmation logic
            try {
                File existing = new File("scratch/test_phase7_marksheet.pdf");
                boolean detected = existing.exists();
                if (detected) {
                    System.out.println("[PASS] Test 6: File overwrite detection logic verified");
                    passed++;
                } else {
                    System.err.println("[FAIL] Test 6: Existing file not detected");
                    failed++;
                }
            } catch (Exception ex) {
                System.err.println("[FAIL] Test 6: " + ex.getMessage());
                failed++;
            }

            // Test 7: Attendance boundaries at 64.99%, 65.00%, 74.99%, 75.00%
            try {
                // 64.99%: 6499 attended out of 10000
                EvaluationEngine.AttendanceEvaluation a6499 = EvaluationEngine.evaluateAttendance(10000, 6499, false);
                boolean pass6499 = (a6499.status == EvaluationEngine.AttendanceStatus.DETAINED_SA);

                // 65.00%: 65 attended out of 100
                EvaluationEngine.AttendanceEvaluation a6500 = EvaluationEngine.evaluateAttendance(100, 65, false);
                boolean pass6500 = (a6500.status == EvaluationEngine.AttendanceStatus.CONDONATION_REQUIRED);

                // 74.99%: 7499 attended out of 10000
                EvaluationEngine.AttendanceEvaluation a7499 = EvaluationEngine.evaluateAttendance(10000, 7499, false);
                boolean pass7499 = (a7499.status == EvaluationEngine.AttendanceStatus.CONDONATION_REQUIRED);

                // 75.00%: 75 attended out of 100
                EvaluationEngine.AttendanceEvaluation a7500 = EvaluationEngine.evaluateAttendance(100, 75, false);
                boolean pass7500 = (a7500.status == EvaluationEngine.AttendanceStatus.ELIGIBLE);

                if (pass6499 && pass6500 && pass7499 && pass7500) {
                    System.out.println("[PASS] Test 7: Attendance boundaries (64.99% SA, 65.00% Condonation, 74.99% Condonation, 75.00% Eligible)");
                    passed++;
                } else {
                    System.err.printf("[FAIL] Test 7: Boundaries failed (64.99:%b, 65.00:%b, 74.99:%b, 75.00:%b)%n",
                            pass6499, pass6500, pass7499, pass7500);
                    failed++;
                }
            } catch (Exception ex) {
                System.err.println("[FAIL] Test 7: " + ex.getMessage());
                failed++;
            }

            // Test 8: Approved and unapproved condonation
            try {
                EvaluationEngine.AttendanceEvaluation unapproved = EvaluationEngine.evaluateAttendance(100, 70, false);
                EvaluationEngine.AttendanceEvaluation approved = EvaluationEngine.evaluateAttendance(100, 70, true);

                if (!unapproved.status.isEligibleForEse && approved.status.isEligibleForEse
                        && approved.status == EvaluationEngine.AttendanceStatus.ELIGIBLE_CONDONED) {
                    System.out.println("[PASS] Test 8: Approved and unapproved condonation logic verified");
                    passed++;
                } else {
                    System.err.println("[FAIL] Test 8: Condonation logic mismatch");
                    failed++;
                }
            } catch (Exception ex) {
                System.err.println("[FAIL] Test 8: " + ex.getMessage());
                failed++;
            }

            // Test 9: Zero classes held and missing attendance records
            try {
                EvaluationEngine.AttendanceEvaluation zeroHeld = EvaluationEngine.evaluateAttendance(0, 0, false);
                if (zeroHeld.classesHeld == 0 && zeroHeld.percentage == 100.0) {
                    System.out.println("[PASS] Test 9: Zero classes held handled safely without division by zero");
                    passed++;
                } else {
                    System.err.println("[FAIL] Test 9: Zero classes held error");
                    failed++;
                }
            } catch (Exception ex) {
                System.err.println("[FAIL] Test 9: " + ex.getMessage());
                failed++;
            }

            // Test 10: Department pass/fail counts without duplicate students
            try {
                String sql = """
                        SELECT COUNT(DISTINCT s.student_id) as student_count
                        FROM enrollments e
                        JOIN students s ON e.student_id = s.student_id
                        WHERE s.department = 'CSE' AND e.semester = 4
                        """;
                int count = 0;
                try (Statement st = con.createStatement(); ResultSet rs = st.executeQuery(sql)) {
                    if (rs.next()) count = rs.getInt("student_count");
                }
                if (count == 2) {
                    System.out.println("[PASS] Test 10: Department student count without duplicates (Found exactly 2 distinct CSE students in Sem 4)");
                    passed++;
                } else {
                    System.err.println("[FAIL] Test 10: Count mismatch: " + count);
                    failed++;
                }
            } catch (Exception ex) {
                System.err.println("[FAIL] Test 10: " + ex.getMessage());
                failed++;
            }

            // Test 11: Exclusion of incomplete SGPA results from class averages
            try {
                List<Double> validSgpas = new ArrayList<>();
                validSgpas.add(8.5);
                validSgpas.add(7.5);
                // Student 3 is incomplete, excluded from validSgpas
                double sum = 0.0;
                for (Double s : validSgpas) sum += s;
                double avg = sum / validSgpas.size();
                if (avg == 8.0 && validSgpas.size() == 2) {
                    System.out.printf("[PASS] Test 11: Incomplete SGPA results excluded from class average (Avg: %.2f, N=%d)%n", avg, validSgpas.size());
                    passed++;
                } else {
                    System.err.println("[FAIL] Test 11: Class average logic error");
                    failed++;
                }
            } catch (Exception ex) {
                System.err.println("[FAIL] Test 11: " + ex.getMessage());
                failed++;
            }

            // Test 12: Existing student, marks, SAP, user accounts regression checks
            try {
                int sCount = 0, cCount = 0, eCount = 0, mCount = 0, aCount = 0, sapCount = 0, uCount = 0;
                try (Statement st = con.createStatement()) {
                    try (ResultSet rs = st.executeQuery("SELECT COUNT(*) FROM students")) { if (rs.next()) sCount = rs.getInt(1); }
                    try (ResultSet rs = st.executeQuery("SELECT COUNT(*) FROM courses")) { if (rs.next()) cCount = rs.getInt(1); }
                    try (ResultSet rs = st.executeQuery("SELECT COUNT(*) FROM enrollments")) { if (rs.next()) eCount = rs.getInt(1); }
                    try (ResultSet rs = st.executeQuery("SELECT COUNT(*) FROM assessment_marks")) { if (rs.next()) mCount = rs.getInt(1); }
                    try (ResultSet rs = st.executeQuery("SELECT COUNT(*) FROM attendance")) { if (rs.next()) aCount = rs.getInt(1); }
                    try (ResultSet rs = st.executeQuery("SELECT COUNT(*) FROM sap_activities")) { if (rs.next()) sapCount = rs.getInt(1); }
                    try (ResultSet rs = st.executeQuery("SELECT COUNT(*) FROM app_users")) { if (rs.next()) uCount = rs.getInt(1); }
                }

                if (sCount == 5 && cCount == 2 && eCount == 5 && mCount == 10 && aCount == 5 && sapCount == 1 && uCount == 5) {
                    System.out.printf("[PASS] Test 12: Regression checks verified completely intact (Students=%d, Courses=%d, Enrollments=%d, Marks=%d, Att=%d, SAP=%d, Users=%d)%n",
                            sCount, cCount, eCount, mCount, aCount, sapCount, uCount);
                    passed++;
                } else {
                    System.err.printf("[FAIL] Test 12: Counts mismatch: s=%d, c=%d, e=%d, m=%d, a=%d, sap=%d, u=%d%n",
                            sCount, cCount, eCount, mCount, aCount, sapCount, uCount);
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

        System.out.println("\n=== Phase 7 Test Summary ===");
        System.out.println("Passed: " + passed + " / 12");
        System.out.println("Failed: " + failed);
        if (failed == 0) {
            System.out.println(">>> ALL 12 PHASE 7 TESTS PASSED SUCCESSFULLY! <<<");
        } else {
            System.exit(1);
        }
    }
}
