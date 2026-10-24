import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class TestPhase8 {

    private static final String URL = "jdbc:mysql://127.0.0.1:3306/kec_academic";
    private static final String USER = "root";
    private static final String PASS = "Vickyy@07";

    public static void main(String[] args) {
        System.out.println("=== Running Phase 8 CLI Report Comprehensive Test Suite ===");
        int passed = 0;
        int failed = 0;

        // Test 1: Theory passing and failing ESE thresholds (min 27/60)
        try {
            EvaluationEngine.AttendanceEvaluation att = EvaluationEngine.evaluateAttendance(45, 40, false);
            // 27.0 ESE: Total = 16.0 + 27.0 = 43.0 < 50 => RA
            // Total >= 50 and ESE >= 27 => PASS
            EvaluationEngine.CourseEvaluationResult passTheory = EvaluationEngine.evaluateCourse(
                    EvaluationEngine.CourseType.THEORY, 4, 75.0, 35.0, false, att
            );
            EvaluationEngine.CourseEvaluationResult failTheoryEse = EvaluationEngine.evaluateCourse(
                    EvaluationEngine.CourseType.THEORY, 4, 100.0, 26.5, false, att
            );
            if (passTheory.isPassed && !failTheoryEse.isPassed && "RA".equals(failTheoryEse.grade)) {
                System.out.println("[PASS] Test 1: Theory passing (ESE 35 >= 27) and failing (ESE 26.5 < 27) thresholds verified");
                passed++;
            } else {
                System.err.println("[FAIL] Test 1: Theory threshold test failed");
                failed++;
            }
        } catch (Exception ex) {
            System.err.println("[FAIL] Test 1: " + ex.getMessage());
            failed++;
        }

        // Test 2: Practical (min 20/40) and Embedded (min 22.5/50) thresholds
        try {
            EvaluationEngine.AttendanceEvaluation att = EvaluationEngine.evaluateAttendance(45, 40, false);
            // Practical: maxInternal=60, maxEse=40, minEse=20.0
            EvaluationEngine.CourseEvaluationResult passPrac = EvaluationEngine.evaluateCourse(
                    EvaluationEngine.CourseType.PRACTICAL, 2, 80.0, 20.0, false, att
            );
            EvaluationEngine.CourseEvaluationResult failPrac = EvaluationEngine.evaluateCourse(
                    EvaluationEngine.CourseType.PRACTICAL, 2, 100.0, 19.5, false, att
            );

            // Embedded: maxInternal=50, maxEse=50, minEse=22.5
            EvaluationEngine.CourseEvaluationResult passEmb = EvaluationEngine.evaluateCourse(
                    EvaluationEngine.CourseType.EMBEDDED, 3, 70.0, 23.0, false, att
            );
            EvaluationEngine.CourseEvaluationResult failEmb = EvaluationEngine.evaluateCourse(
                    EvaluationEngine.CourseType.EMBEDDED, 3, 100.0, 22.0, false, att
            );

            if (passPrac.isPassed && !failPrac.isPassed && passEmb.isPassed && !failEmb.isPassed) {
                System.out.println("[PASS] Test 2: Practical (20.0 min) and Embedded (22.5 min) ESE thresholds verified");
                passed++;
            } else {
                System.err.println("[FAIL] Test 2: Practical/Embedded thresholds failed");
                failed++;
            }
        } catch (Exception ex) {
            System.err.println("[FAIL] Test 2: " + ex.getMessage());
            failed++;
        }

        // Test 3: AB, SA, RA, and Incomplete assessments
        try {
            EvaluationEngine.AttendanceEvaluation attEligible = EvaluationEngine.evaluateAttendance(45, 40, false);
            EvaluationEngine.AttendanceEvaluation attSa = EvaluationEngine.evaluateAttendance(45, 20, false);

            EvaluationEngine.CourseEvaluationResult resSa = EvaluationEngine.evaluateCourse(
                    EvaluationEngine.CourseType.THEORY, 3, 90.0, 50.0, false, attSa
            );
            EvaluationEngine.CourseEvaluationResult resAb = EvaluationEngine.evaluateCourse(
                    EvaluationEngine.CourseType.THEORY, 3, 90.0, 0.0, true, attEligible
            );
            EvaluationEngine.CourseEvaluationResult resRa = EvaluationEngine.evaluateCourse(
                    EvaluationEngine.CourseType.THEORY, 3, 40.0, 28.0, false, attEligible
            );
            EvaluationEngine.CourseEvaluationResult resInc = EvaluationEngine.evaluateCourse(
                    EvaluationEngine.CourseType.THEORY, 3, 75.0, null, false, attEligible
            );

            if ("SA".equals(resSa.grade) && "AB".equals(resAb.grade) && "RA".equals(resRa.grade) && "—".equals(resInc.grade)) {
                System.out.println("[PASS] Test 3: Distinct status mapping (SA, AB, RA, and Incomplete) verified");
                passed++;
            } else {
                System.err.printf("[FAIL] Test 3: SA=%s, AB=%s, RA=%s, Inc=%s%n",
                        resSa.grade, resAb.grade, resRa.grade, resInc.grade);
                failed++;
            }
        } catch (Exception ex) {
            System.err.println("[FAIL] Test 3: " + ex.getMessage());
            failed++;
        }

        // Test 4: Attendance at 64.99%, 65.00%, 74.99%, and 75.00%
        try {
            EvaluationEngine.AttendanceEvaluation a6499 = EvaluationEngine.evaluateAttendance(10000, 6499, false);
            EvaluationEngine.AttendanceEvaluation a6500 = EvaluationEngine.evaluateAttendance(100, 65, false);
            EvaluationEngine.AttendanceEvaluation a7499 = EvaluationEngine.evaluateAttendance(10000, 7499, false);
            EvaluationEngine.AttendanceEvaluation a7500 = EvaluationEngine.evaluateAttendance(100, 75, false);

            if (a6499.status == EvaluationEngine.AttendanceStatus.DETAINED_SA
                    && a6500.status == EvaluationEngine.AttendanceStatus.CONDONATION_REQUIRED
                    && a7499.status == EvaluationEngine.AttendanceStatus.CONDONATION_REQUIRED
                    && a7500.status == EvaluationEngine.AttendanceStatus.ELIGIBLE) {
                System.out.println("[PASS] Test 4: Exact attendance percentage boundary criteria verified");
                passed++;
            } else {
                System.err.println("[FAIL] Test 4: Attendance boundaries mismatch");
                failed++;
            }
        } catch (Exception ex) {
            System.err.println("[FAIL] Test 4: " + ex.getMessage());
            failed++;
        }

        // Test 5: Approved and unapproved condonation
        try {
            EvaluationEngine.AttendanceEvaluation unapproved = EvaluationEngine.evaluateAttendance(100, 70, false);
            EvaluationEngine.AttendanceEvaluation approved = EvaluationEngine.evaluateAttendance(100, 70, true);

            if (!unapproved.status.isEligibleForEse && approved.status.isEligibleForEse) {
                System.out.println("[PASS] Test 5: Approved and unapproved condonation eligibility verified");
                passed++;
            } else {
                System.err.println("[FAIL] Test 5: Condonation status mismatch");
                failed++;
            }
        } catch (Exception ex) {
            System.err.println("[FAIL] Test 5: " + ex.getMessage());
            failed++;
        }

        // Test 6: SGPA calculation weighted by credits
        try {
            EvaluationEngine.AttendanceEvaluation att = EvaluationEngine.evaluateAttendance(45, 40, false);
            // Course 1: 4 credits, Grade O (GP 10) => 40 points
            // Course 2: 3 credits, Grade B+ (GP 7) => 21 points
            // Total points = 61, Total credits = 7 => SGPA = 61 / 7 = 8.71
            EvaluationEngine.CourseEvaluationResult c1 = EvaluationEngine.evaluateCourse(
                    EvaluationEngine.CourseType.THEORY, 4, 95.0, 58.0, false, att
            );
            EvaluationEngine.CourseEvaluationResult c2 = EvaluationEngine.evaluateCourse(
                    EvaluationEngine.CourseType.THEORY, 3, 65.0, 40.0, false, att
            );
            List<EvaluationEngine.CourseEvaluationResult> list = List.of(c1, c2);
            double sgpa = EvaluationEngine.calculateSGPA(list);
            if (Math.abs(sgpa - 8.71) < 0.02) {
                System.out.printf("[PASS] Test 6: Credit-weighted SGPA calculation verified (SGPA = %.2f)%n", sgpa);
                passed++;
            } else {
                System.err.println("[FAIL] Test 6: SGPA calculation mismatch: " + sgpa);
                failed++;
            }
        } catch (Exception ex) {
            System.err.println("[FAIL] Test 6: " + ex.getMessage());
            failed++;
        }

        // Test 7: Percentage equivalent (SGPA * 10)
        try {
            double sgpa = 8.65;
            double equiv = EvaluationEngine.calculateEquivalentPercentage(sgpa);
            if (equiv == 86.5) {
                System.out.printf("[PASS] Test 7: Percentage equivalent verified (%.2f -> %.2f%%)%n", sgpa, equiv);
                passed++;
            } else {
                System.err.println("[FAIL] Test 7: Percentage equivalent error: " + equiv);
                failed++;
            }
        } catch (Exception ex) {
            System.err.println("[FAIL] Test 7: " + ex.getMessage());
            failed++;
        }

        // Test 8: Missing attendance or marks safely handled
        try {
            EvaluationEngine.AttendanceEvaluation zeroClasses = EvaluationEngine.evaluateAttendance(0, 0, false);
            if (zeroClasses.classesHeld == 0 && zeroClasses.percentage == 100.0) {
                System.out.println("[PASS] Test 8: Zero classes held and missing attendance records handled safely");
                passed++;
            } else {
                System.err.println("[FAIL] Test 8: Zero classes evaluation error");
                failed++;
            }
        } catch (Exception ex) {
            System.err.println("[FAIL] Test 8: " + ex.getMessage());
            failed++;
        }

        // Test 9: End-to-end CLI report execution with student 24CSE001
        try {
            PrintStream originalOut = System.out;
            ByteArrayOutputStream outCapture = new ByteArrayOutputStream();
            System.setOut(new PrintStream(outCapture));

            // Input: 4 (Generate marks report), roll "24CSE001", sem "4", 5 (Exit)
            String simulatedInput = "4\n24CSE001\n4\n5\n";
            ByteArrayInputStream inStream = new ByteArrayInputStream(simulatedInput.getBytes());
            System.setIn(inStream);

            Main.main(new String[]{});

            System.setOut(originalOut);
            String output = outCapture.toString();

            boolean hasHeader = output.contains("KEC ACADEMIX") && output.contains("STUDENT ACADEMIC MARKS REPORT (R2024)");
            boolean hasStudent = output.contains("Arun Kumar") && output.contains("24CSE001");
            boolean hasCourses = output.contains("CS2401") && output.contains("CS2402");
            boolean hasEvaluation = output.contains("EVALUATION SUMMARY") && output.contains("Total Credits Registered");

            if (hasHeader && hasStudent && hasCourses && hasEvaluation) {
                System.out.println("[PASS] Test 9: End-to-end CLI report generation executed successfully for student 24CSE001");
                passed++;
            } else {
                System.err.println("[FAIL] Test 9: CLI output missing expected sections. Output preview:\n"
                        + output.substring(0, Math.min(output.length(), 400)));
                failed++;
            }
        } catch (Exception ex) {
            System.err.println("[FAIL] Test 9: " + ex.getMessage());
            failed++;
        }

        // Test 10: Invalid input handling in CLI
        try {
            PrintStream originalOut = System.out;
            ByteArrayOutputStream outCapture = new ByteArrayOutputStream();
            System.setOut(new PrintStream(outCapture));

            // Input: 4, non-existent roll "INVALID999", "", 5
            String simulatedInput = "4\nINVALID999\n\n5\n";
            ByteArrayInputStream inStream = new ByteArrayInputStream(simulatedInput.getBytes());
            System.setIn(inStream);

            Main.main(new String[]{});

            System.setOut(originalOut);
            String output = outCapture.toString();

            if (output.contains("No student found with roll number: INVALID999")) {
                System.out.println("[PASS] Test 10: Invalid roll number input handled gracefully with user message");
                passed++;
            } else {
                System.err.println("[FAIL] Test 10: Invalid input message missing");
                failed++;
            }
        } catch (Exception ex) {
            System.err.println("[FAIL] Test 10: " + ex.getMessage());
            failed++;
        }

        // Test 11: Swing application class compatibility & reflection check
        try {
            Class<?> kecGuiClass = Class.forName("KECGui");
            Class<?> evalClass = Class.forName("EvaluationEngine");
            Class<?> pdfClass = Class.forName("PdfMarksheetGenerator");
            Class<?> sessionClass = Class.forName("UserSession");
            Class<?> authClass = Class.forName("AuthService");

            if (kecGuiClass != null && evalClass != null && pdfClass != null && sessionClass != null && authClass != null) {
                System.out.println("[PASS] Test 11: All GUI and core domain classes verified and loadable");
                passed++;
            } else {
                System.err.println("[FAIL] Test 11: Classes not found");
                failed++;
            }
        } catch (Exception ex) {
            System.err.println("[FAIL] Test 11: " + ex.getMessage());
            failed++;
        }

        // Test 12: Database integrity regression check
        try (Connection con = DriverManager.getConnection(URL, USER, PASS);
             Statement st = con.createStatement()) {
            int sCount = 0, cCount = 0, eCount = 0, mCount = 0, aCount = 0, sapCount = 0, uCount = 0;
            try (ResultSet rs = st.executeQuery("SELECT COUNT(*) FROM students")) { if (rs.next()) sCount = rs.getInt(1); }
            try (ResultSet rs = st.executeQuery("SELECT COUNT(*) FROM courses")) { if (rs.next()) cCount = rs.getInt(1); }
            try (ResultSet rs = st.executeQuery("SELECT COUNT(*) FROM enrollments")) { if (rs.next()) eCount = rs.getInt(1); }
            try (ResultSet rs = st.executeQuery("SELECT COUNT(*) FROM assessment_marks")) { if (rs.next()) mCount = rs.getInt(1); }
            try (ResultSet rs = st.executeQuery("SELECT COUNT(*) FROM attendance")) { if (rs.next()) aCount = rs.getInt(1); }
            try (ResultSet rs = st.executeQuery("SELECT COUNT(*) FROM sap_activities")) { if (rs.next()) sapCount = rs.getInt(1); }
            try (ResultSet rs = st.executeQuery("SELECT COUNT(*) FROM app_users")) { if (rs.next()) uCount = rs.getInt(1); }

            if (sCount == 5 && cCount == 2 && eCount == 5 && mCount == 10 && aCount == 5 && sapCount == 1 && uCount == 5) {
                System.out.printf("[PASS] Test 12: Database records 100%% intact (Students=%d, Courses=%d, Enrollments=%d, Marks=%d, Att=%d, SAP=%d, Users=%d)%n",
                        sCount, cCount, eCount, mCount, aCount, sapCount, uCount);
                passed++;
            } else {
                System.err.printf("[FAIL] Test 12: Database counts altered! s=%d, c=%d, e=%d, m=%d, a=%d, sap=%d, u=%d%n",
                        sCount, cCount, eCount, mCount, aCount, sapCount, uCount);
                failed++;
            }
        } catch (Exception ex) {
            System.err.println("[FAIL] Test 12: " + ex.getMessage());
            failed++;
        }

        System.out.println("\n=== Phase 8 Test Summary ===");
        System.out.println("Passed: " + passed + " / 12");
        System.out.println("Failed: " + failed);
        if (failed == 0) {
            System.out.println(">>> ALL 12 PHASE 8 TESTS PASSED SUCCESSFULLY! <<<");
        } else {
            System.exit(1);
        }
    }
}
