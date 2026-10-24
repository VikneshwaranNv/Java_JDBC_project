import java.util.ArrayList;
import java.util.List;

public class TestEvaluationEngine {
    public static void main(String[] args) {
        System.out.println("=== Testing EvaluationEngine R2024 Rules ===");

        // 1. Attendance Tests
        var attEligible = EvaluationEngine.evaluateAttendance(50, 40, false); // 80%
        assert attEligible.status == EvaluationEngine.AttendanceStatus.ELIGIBLE;
        assert !attEligible.isBarredFromEse();
        System.out.println("✓ Attendance 80% eligible");

        var attCondonationReq = EvaluationEngine.evaluateAttendance(50, 35, false); // 70%
        assert attCondonationReq.status == EvaluationEngine.AttendanceStatus.CONDONATION_REQUIRED;
        assert attCondonationReq.isBarredFromEse();
        System.out.println("✓ Attendance 70% unapproved is condonation required & barred");

        var attCondoned = EvaluationEngine.evaluateAttendance(50, 35, true); // 70% condoned
        assert attCondoned.status == EvaluationEngine.AttendanceStatus.ELIGIBLE_CONDONED;
        assert !attCondoned.isBarredFromEse();
        System.out.println("✓ Attendance 70% approved condonation is eligible");

        var attDetained = EvaluationEngine.evaluateAttendance(50, 30, true); // 60% (<65)
        assert attDetained.status == EvaluationEngine.AttendanceStatus.DETAINED_SA;
        assert attDetained.isBarredFromEse();
        System.out.println("✓ Attendance 60% is detained SA even if condonation flag set");

        // 2. Theory Course (40/60, ESE min 27, total 50)
        // Student A: Internal 90/100 (36.0), ESE 55/60 (55.0), Total = 91.0 -> Grade O, GP 10
        var evalTheoryO = EvaluationEngine.evaluateCourse(EvaluationEngine.CourseType.THEORY, 4, 90.0, 55.0, false, attEligible);
        assert evalTheoryO.grade.equals("O");
        assert evalTheoryO.gradePoint == 10;
        assert evalTheoryO.isPassed;
        System.out.println("✓ Theory 91.0 marks gives Grade O (GP 10)");

        // Student B: Internal 90/100 (36.0), ESE 26/60 (26.0), Total = 62.0 (>=50 but ESE < 27) -> Grade RA, GP 0
        var evalTheoryFailEse = EvaluationEngine.evaluateCourse(EvaluationEngine.CourseType.THEORY, 4, 90.0, 26.0, false, attEligible);
        assert evalTheoryFailEse.grade.equals("RA");
        assert evalTheoryFailEse.gradePoint == 0;
        assert !evalTheoryFailEse.isPassed;
        System.out.println("✓ Theory ESE 26/60 fails with RA even though Total is 62.0/100");

        // 3. Practical Course (60/40, ESE min 20, total 50)
        // Internal 80/100 (48.0), ESE 20/40 (20.0), Total = 68.0 -> Grade B+, GP 7
        var evalPrac = EvaluationEngine.evaluateCourse(EvaluationEngine.CourseType.PRACTICAL, 2, 80.0, 20.0, false, attEligible);
        assert evalPrac.grade.equals("B+");
        assert evalPrac.gradePoint == 7;
        assert evalPrac.isPassed;
        System.out.println("✓ Practical 60/40 passes with ESE 20/40");

        // Practical ESE 19/40 (<20) -> Fail
        var evalPracFail = EvaluationEngine.evaluateCourse(EvaluationEngine.CourseType.PRACTICAL, 2, 90.0, 19.0, false, attEligible);
        assert evalPracFail.grade.equals("RA");
        assert !evalPracFail.isPassed;
        System.out.println("✓ Practical fails when ESE < 20/40");

        // 4. Embedded Course (50/50, ESE min 22.5, total 50)
        var evalEmb = EvaluationEngine.evaluateCourse(EvaluationEngine.CourseType.EMBEDDED, 3, 70.0, 23.0, false, attEligible);
        // Internal 35.0 + ESE 23.0 = 58.0 -> Grade B (50-60), GP 6
        assert evalEmb.grade.equals("B");
        assert evalEmb.gradePoint == 6;
        assert evalEmb.isPassed;
        System.out.println("✓ Embedded 50/50 passes with ESE 23.0/50 (min 22.5)");

        // Embedded ESE 22.0 (<22.5) -> Fail
        var evalEmbFail = EvaluationEngine.evaluateCourse(EvaluationEngine.CourseType.EMBEDDED, 3, 80.0, 22.0, false, attEligible);
        assert evalEmbFail.grade.equals("RA");
        assert !evalEmbFail.isPassed;
        System.out.println("✓ Embedded fails when ESE < 22.5/50");

        // 5. Precedence: Shortage of Attendance (SA)
        var evalSA = EvaluationEngine.evaluateCourse(EvaluationEngine.CourseType.THEORY, 4, 90.0, 55.0, false, attDetained);
        assert evalSA.grade.equals("SA");
        assert evalSA.gradePoint == 0;
        assert evalSA.isAttendanceShortage;
        System.out.println("✓ Precedence: SA overrides high marks and awards Grade SA");

        // 6. Precedence: Absent (AB)
        var evalAB = EvaluationEngine.evaluateCourse(EvaluationEngine.CourseType.THEORY, 4, 90.0, 0.0, true, attEligible);
        assert evalAB.grade.equals("AB");
        assert evalAB.gradePoint == 0;
        assert evalAB.isAbsentEse;
        System.out.println("✓ Precedence: AB awards Grade AB");

        // 7. SGPA & Percentage Calculation
        // Course 1 (4 credits, GP 10) -> 40 points
        // Course 2 (2 credits, GP 7)  -> 14 points
        // Course 3 (3 credits, GP 6)  -> 18 points
        // Total credits = 9, Total points = 72
        // SGPA = 72 / 9 = 8.00
        // Percentage = 80.0%
        List<EvaluationEngine.CourseEvaluationResult> semester = new ArrayList<>();
        semester.add(evalTheoryO);
        semester.add(evalPrac);
        semester.add(evalEmb);

        double sgpa = EvaluationEngine.calculateSGPA(semester);
        assert Math.abs(sgpa - 8.00) < 0.01;
        double pct = EvaluationEngine.calculateEquivalentPercentage(sgpa);
        assert Math.abs(pct - 80.0) < 0.01;
        System.out.println("✓ SGPA calculation = " + sgpa + ", Percentage = " + pct + "%");

        System.out.println("=== All EvaluationEngine Tests PASSED! ===");
    }
}

