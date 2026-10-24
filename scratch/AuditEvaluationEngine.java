import java.util.ArrayList;
import java.util.List;

public class AuditEvaluationEngine {

    public static class AuditResult {
        public final String testName;
        public final String expected;
        public final String actual;
        public final boolean passed;

        public AuditResult(String testName, String expected, String actual, boolean passed) {
            this.testName = testName;
            this.expected = expected;
            this.actual = actual;
            this.passed = passed;
        }
    }

    public static void main(String[] args) {
        System.out.println("=== Phase 9: Evaluation Engine Boundary & Precision Audit ===");
        List<AuditResult> results = new ArrayList<>();

        EvaluationEngine.AttendanceEvaluation attFull = EvaluationEngine.evaluateAttendance(100, 100, false);

        // 1. Grade Boundary: 49.99 vs 50.00 (Theory: internal 50% => 20/40, ESE 29.99 vs 30.00)
        // 49.99: CIA 100-scale = 50.0 => scaled CIA = 20.00. ESE = 29.99 => total = 49.99 => RA
        EvaluationEngine.CourseEvaluationResult g4999 = EvaluationEngine.evaluateCourse(
                EvaluationEngine.CourseType.THEORY, 3, 50.0, 29.99, false, attFull
        );
        results.add(new AuditResult(
                "Grade Boundary 49.99 (Below Pass Minimum 50.00)",
                "Grade: RA, Passed: false",
                "Grade: " + g4999.grade + ", Passed: " + g4999.isPassed,
                "RA".equals(g4999.grade) && !g4999.isPassed
        ));

        // 50.00: CIA = 20.00, ESE = 30.00 => total = 50.00 => B (GP 6)
        EvaluationEngine.CourseEvaluationResult g5000 = EvaluationEngine.evaluateCourse(
                EvaluationEngine.CourseType.THEORY, 3, 50.0, 30.00, false, attFull
        );
        results.add(new AuditResult(
                "Grade Boundary 50.00 (Minimum Pass Aggregate)",
                "Grade: B, GP: 6, Passed: true",
                "Grade: " + g5000.grade + ", GP: " + g5000.gradePoint + ", Passed: " + g5000.isPassed,
                "B".equals(g5000.grade) && g5000.gradePoint == 6 && g5000.isPassed
        ));

        // 2. Grade Boundary: 60.00 vs 61.00 (B vs B+)
        EvaluationEngine.CourseEvaluationResult g6000 = EvaluationEngine.evaluateCourse(
                EvaluationEngine.CourseType.THEORY, 3, 75.0, 30.00, false, attFull // 30 + 30 = 60.00 => B
        );
        EvaluationEngine.CourseEvaluationResult g6100 = EvaluationEngine.evaluateCourse(
                EvaluationEngine.CourseType.THEORY, 3, 75.0, 31.00, false, attFull // 30 + 31 = 61.00 => B+
        );
        results.add(new AuditResult(
                "Grade Boundary 60.00 (Upper Boundary of Grade B)",
                "Grade: B, GP: 6",
                "Grade: " + g6000.grade + ", GP: " + g6000.gradePoint,
                "B".equals(g6000.grade) && g6000.gradePoint == 6
        ));
        results.add(new AuditResult(
                "Grade Boundary 61.00 (Lower Boundary of Grade B+)",
                "Grade: B+, GP: 7",
                "Grade: " + g6100.grade + ", GP: " + g6100.gradePoint,
                "B+".equals(g6100.grade) && g6100.gradePoint == 7
        ));

        // 3. Grade Boundary: 70.00 vs 71.00 (B+ vs A)
        EvaluationEngine.CourseEvaluationResult g7000 = EvaluationEngine.evaluateCourse(
                EvaluationEngine.CourseType.THEORY, 3, 75.0, 40.00, false, attFull // 30 + 40 = 70.00 => B+
        );
        EvaluationEngine.CourseEvaluationResult g7100 = EvaluationEngine.evaluateCourse(
                EvaluationEngine.CourseType.THEORY, 3, 75.0, 41.00, false, attFull // 30 + 41 = 71.00 => A
        );
        results.add(new AuditResult(
                "Grade Boundary 70.00 (Upper Boundary of Grade B+)",
                "Grade: B+, GP: 7",
                "Grade: " + g7000.grade + ", GP: " + g7000.gradePoint,
                "B+".equals(g7000.grade) && g7000.gradePoint == 7
        ));
        results.add(new AuditResult(
                "Grade Boundary 71.00 (Lower Boundary of Grade A)",
                "Grade: A, GP: 8",
                "Grade: " + g7100.grade + ", GP: " + g7100.gradePoint,
                "A".equals(g7100.grade) && g7100.gradePoint == 8
        ));

        // 4. Grade Boundary: 80.00 vs 81.00 (A vs A+)
        EvaluationEngine.CourseEvaluationResult g8000 = EvaluationEngine.evaluateCourse(
                EvaluationEngine.CourseType.THEORY, 3, 100.0, 40.00, false, attFull // 40 + 40 = 80.00 => A
        );
        EvaluationEngine.CourseEvaluationResult g8100 = EvaluationEngine.evaluateCourse(
                EvaluationEngine.CourseType.THEORY, 3, 100.0, 41.00, false, attFull // 40 + 41 = 81.00 => A+
        );
        results.add(new AuditResult(
                "Grade Boundary 80.00 (Upper Boundary of Grade A)",
                "Grade: A, GP: 8",
                "Grade: " + g8000.grade + ", GP: " + g8000.gradePoint,
                "A".equals(g8000.grade) && g8000.gradePoint == 8
        ));
        results.add(new AuditResult(
                "Grade Boundary 81.00 (Lower Boundary of Grade A+)",
                "Grade: A+, GP: 9",
                "Grade: " + g8100.grade + ", GP: " + g8100.gradePoint,
                "A+".equals(g8100.grade) && g8100.gradePoint == 9
        ));

        // 5. Grade Boundary: 90.00 vs 91.00 (A+ vs O)
        EvaluationEngine.CourseEvaluationResult g9000 = EvaluationEngine.evaluateCourse(
                EvaluationEngine.CourseType.THEORY, 3, 100.0, 50.00, false, attFull // 40 + 50 = 90.00 => A+
        );
        EvaluationEngine.CourseEvaluationResult g9100 = EvaluationEngine.evaluateCourse(
                EvaluationEngine.CourseType.THEORY, 3, 100.0, 51.00, false, attFull // 40 + 51 = 91.00 => O
        );
        results.add(new AuditResult(
                "Grade Boundary 90.00 (Upper Boundary of Grade A+)",
                "Grade: A+, GP: 9",
                "Grade: " + g9000.grade + ", GP: " + g9000.gradePoint,
                "A+".equals(g9000.grade) && g9000.gradePoint == 9
        ));
        results.add(new AuditResult(
                "Grade Boundary 91.00 (Lower Boundary of Outstanding Grade O)",
                "Grade: O, GP: 10",
                "Grade: " + g9100.grade + ", GP: " + g9100.gradePoint,
                "O".equals(g9100.grade) && g9100.gradePoint == 10
        ));

        // 6. ESE Minimum Thresholds
        // Theory: min 27.0 / 60
        EvaluationEngine.CourseEvaluationResult thFail = EvaluationEngine.evaluateCourse(
                EvaluationEngine.CourseType.THEORY, 4, 100.0, 26.99, false, attFull // Total = 66.99, but ESE < 27
        );
        EvaluationEngine.CourseEvaluationResult thPass = EvaluationEngine.evaluateCourse(
                EvaluationEngine.CourseType.THEORY, 4, 100.0, 27.00, false, attFull // Total = 67.00, ESE >= 27
        );
        results.add(new AuditResult(
                "Theory ESE Threshold 26.99 / 60 (Below Min 27)",
                "Passed: false, Grade: RA",
                "Passed: " + thFail.isPassed + ", Grade: " + thFail.grade,
                !thFail.isPassed && "RA".equals(thFail.grade)
        ));
        results.add(new AuditResult(
                "Theory ESE Threshold 27.00 / 60 (Exact Min Threshold)",
                "Passed: true, Grade: B+",
                "Passed: " + thPass.isPassed + ", Grade: " + thPass.grade,
                thPass.isPassed && "B+".equals(thPass.grade)
        ));

        // Practical: min 20.0 / 40
        EvaluationEngine.CourseEvaluationResult prFail = EvaluationEngine.evaluateCourse(
                EvaluationEngine.CourseType.PRACTICAL, 2, 100.0, 19.99, false, attFull
        );
        EvaluationEngine.CourseEvaluationResult prPass = EvaluationEngine.evaluateCourse(
                EvaluationEngine.CourseType.PRACTICAL, 2, 100.0, 20.00, false, attFull
        );
        results.add(new AuditResult(
                "Practical ESE Threshold 19.99 / 40 (Below Min 20)",
                "Passed: false, Grade: RA",
                "Passed: " + prFail.isPassed + ", Grade: " + prFail.grade,
                !prFail.isPassed && "RA".equals(prFail.grade)
        ));
        results.add(new AuditResult(
                "Practical ESE Threshold 20.00 / 40 (Exact Min Threshold)",
                "Passed: true, Grade: A",
                "Passed: " + prPass.isPassed + ", Grade: " + prPass.grade,
                prPass.isPassed && "A".equals(prPass.grade)
        ));

        // Embedded: min 22.5 / 50
        EvaluationEngine.CourseEvaluationResult embFail = EvaluationEngine.evaluateCourse(
                EvaluationEngine.CourseType.EMBEDDED, 3, 100.0, 22.49, false, attFull
        );
        EvaluationEngine.CourseEvaluationResult embPass = EvaluationEngine.evaluateCourse(
                EvaluationEngine.CourseType.EMBEDDED, 3, 100.0, 22.50, false, attFull
        );
        results.add(new AuditResult(
                "Embedded ESE Threshold 22.49 / 50 (Below Min 22.5)",
                "Passed: false, Grade: RA",
                "Passed: " + embFail.isPassed + ", Grade: " + embFail.grade,
                !embFail.isPassed && "RA".equals(embFail.grade)
        ));
        results.add(new AuditResult(
                "Embedded ESE Threshold 22.50 / 50 (Exact Min Threshold)",
                "Passed: true, Grade: A",
                "Passed: " + embPass.isPassed + ", Grade: " + embPass.grade,
                embPass.isPassed && "A".equals(embPass.grade)
        ));

        // 7. Attendance Boundaries
        EvaluationEngine.AttendanceEvaluation att6499 = EvaluationEngine.evaluateAttendance(10000, 6499, false);
        EvaluationEngine.AttendanceEvaluation att6500 = EvaluationEngine.evaluateAttendance(10000, 6500, false);
        EvaluationEngine.AttendanceEvaluation att7499 = EvaluationEngine.evaluateAttendance(10000, 7499, false);
        EvaluationEngine.AttendanceEvaluation att7500 = EvaluationEngine.evaluateAttendance(10000, 7500, false);

        results.add(new AuditResult(
                "Attendance 64.99% (Strictly below 65% Detention)",
                "Status: DETAINED_SA, Eligible: false",
                "Status: " + att6499.status + ", Eligible: " + att6499.status.isEligibleForEse,
                att6499.status == EvaluationEngine.AttendanceStatus.DETAINED_SA && !att6499.status.isEligibleForEse
        ));
        results.add(new AuditResult(
                "Attendance 65.00% (Unapproved Condonation Band)",
                "Status: CONDONATION_REQUIRED, Eligible: false",
                "Status: " + att6500.status + ", Eligible: " + att6500.status.isEligibleForEse,
                att6500.status == EvaluationEngine.AttendanceStatus.CONDONATION_REQUIRED && !att6500.status.isEligibleForEse
        ));
        results.add(new AuditResult(
                "Attendance 74.99% (Upper Boundary of Condonation Band)",
                "Status: CONDONATION_REQUIRED, Eligible: false",
                "Status: " + att7499.status + ", Eligible: " + att7499.status.isEligibleForEse,
                att7499.status == EvaluationEngine.AttendanceStatus.CONDONATION_REQUIRED && !att7499.status.isEligibleForEse
        ));
        results.add(new AuditResult(
                "Attendance 75.00% (Full Eligibility Threshold)",
                "Status: ELIGIBLE, Eligible: true",
                "Status: " + att7500.status + ", Eligible: " + att7500.status.isEligibleForEse,
                att7500.status == EvaluationEngine.AttendanceStatus.ELIGIBLE && att7500.status.isEligibleForEse
        ));

        // 8. Precedence Rules: SA > AB > Incomplete > RA > Pass
        EvaluationEngine.CourseEvaluationResult saOverride = EvaluationEngine.evaluateCourse(
                EvaluationEngine.CourseType.THEORY, 3, 100.0, 60.0, true, att6499 // SA overrides 100 marks and AB
        );
        results.add(new AuditResult(
                "Precedence: SA Overrides 100% Marks & ESE Status",
                "Grade: SA, GP: 0, Result: SA / SHORTAGE OF ATTENDANCE",
                "Grade: " + saOverride.grade + ", GP: " + saOverride.gradePoint + ", Result: " + saOverride.resultStatus,
                "SA".equals(saOverride.grade) && saOverride.gradePoint == 0
        ));

        EvaluationEngine.CourseEvaluationResult abOverride = EvaluationEngine.evaluateCourse(
                EvaluationEngine.CourseType.THEORY, 3, 100.0, null, true, attFull // AB overrides null ESE
        );
        results.add(new AuditResult(
                "Precedence: AB Overrides Unentered Marks",
                "Grade: AB, GP: 0",
                "Grade: " + abOverride.grade + ", GP: " + abOverride.gradePoint,
                "AB".equals(abOverride.grade) && abOverride.gradePoint == 0
        ));

        // Print Report
        int passedCount = 0;
        System.out.printf("%-55s %-35s %-35s %-8s%n", "Boundary Test Case", "Expected", "Actual", "Status");
        System.out.println("--------------------------------------------------------------------------------------------------------------------------------------");
        for (AuditResult r : results) {
            if (r.passed) passedCount++;
            System.out.printf("%-55s %-35s %-35s %-8s%n",
                    r.testName, r.expected, r.actual, (r.passed ? "PASS" : "FAIL"));
        }
        System.out.println("--------------------------------------------------------------------------------------------------------------------------------------");
        System.out.printf("Audit Summary: %d / %d Tests Passed (%.1f%%)%n",
                passedCount, results.size(), (passedCount * 100.0 / results.size()));

        if (passedCount == results.size()) {
            System.out.println(">>> EVALUATION ENGINE BOUNDARY AUDIT: 100% ACCURATE & VERIFIED <<<");
        } else {
            System.exit(1);
        }
    }
}

