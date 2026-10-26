import java.util.List;

/**
 * Centralized Academic Evaluation Engine for KEC ACADEMIX (R2024 Regulations).
 * Implements authoritative weightages, ESE minimums, attendance eligibility,
 * grade mapping, grade points, SGPA, and equivalent percentage calculation.
 */
public class EvaluationEngine {

    public enum CourseType {
        THEORY("Theory", 0.40, 0.60, 40.0, 60.0, 27.0),
        PRACTICAL("Practical", 0.60, 0.40, 60.0, 40.0, 20.0),
        EMBEDDED("Embedded", 0.50, 0.50, 50.0, 50.0, 22.5);

        public final String displayName;
        public final double internalRatio;
        public final double eseRatio;
        public final double maxInternal;
        public final double maxEse;
        public final double minEseThreshold;

        CourseType(String displayName, double internalRatio, double eseRatio,
                   double maxInternal, double maxEse, double minEseThreshold) {
            this.displayName = displayName;
            this.internalRatio = internalRatio;
            this.eseRatio = eseRatio;
            this.maxInternal = maxInternal;
            this.maxEse = maxEse;
            this.minEseThreshold = minEseThreshold;
        }

        public static CourseType fromString(String str) {
            if (str == null) return THEORY;
            String clean = str.trim().toUpperCase();
            if (clean.contains("PRAC") || clean.contains("LAB")) return PRACTICAL;
            if (clean.contains("EMB") || clean.contains("INTEG")) return EMBEDDED;
            return THEORY;
        }
    }

    public enum AttendanceStatus {
        ELIGIBLE("Eligible", true),
        ELIGIBLE_CONDONED("Eligible (Condoned)", true),
        CONDONATION_REQUIRED("Condonation Required (Ineligible)", false),
        DETAINED_SA("Shortage of Attendance (SA)", false);

        public final String description;
        public final boolean isEligibleForEse;

        AttendanceStatus(String description, boolean isEligibleForEse) {
            this.description = description;
            this.isEligibleForEse = isEligibleForEse;
        }
    }

    /**
     * Evaluates attendance percentage and ESE eligibility according to R2024:
     * - >= 75%: Eligible
     * - 65% to < 75%: Conditionally eligible ONLY IF condonation approved; otherwise ineligible
     * - < 65%: SA (Shortage of Attendance), strictly barred from ESE
     */
    public static AttendanceEvaluation evaluateAttendance(int classesHeld, int classesAttended, boolean condonationApproved) {
        if (classesHeld < 0) classesHeld = 0;
        if (classesAttended < 0) classesAttended = 0;
        if (classesAttended > classesHeld && classesHeld > 0) classesAttended = classesHeld;

        double percentage = (classesHeld == 0) ? 100.0 : ((double) classesAttended / (double) classesHeld) * 100.0;
        AttendanceStatus status;

        if (percentage >= 75.0) {
            status = AttendanceStatus.ELIGIBLE;
        } else if (percentage >= 65.0) {
            status = condonationApproved ? AttendanceStatus.ELIGIBLE_CONDONED : AttendanceStatus.CONDONATION_REQUIRED;
        } else {
            status = AttendanceStatus.DETAINED_SA;
        }

        return new AttendanceEvaluation(classesHeld, classesAttended, percentage, condonationApproved, status);
    }

    public static class AttendanceEvaluation {
        public final int classesHeld;
        public final int classesAttended;
        public final double percentage;
        public final boolean condonationApproved;
        public final AttendanceStatus status;

        public AttendanceEvaluation(int classesHeld, int classesAttended, double percentage,
                                    boolean condonationApproved, AttendanceStatus status) {
            this.classesHeld = classesHeld;
            this.classesAttended = classesAttended;
            this.percentage = percentage;
            this.condonationApproved = condonationApproved;
            this.status = status;
        }

        public boolean isBarredFromEse() {
            return !status.isEligibleForEse;
        }
    }

    /**
     * Comprehensive Evaluation Result for a course enrollment.
     */
    public static class CourseEvaluationResult {
        public final CourseType courseType;
        public final int credits;
        public final double rawInternalTotal;
        public final double maxInternalScale;
        public final double scaledInternal;
        public final Double rawEseScore; // null if not entered / pending
        public final double maxEseScale;
        public final double scaledEse;
        public final double totalMarks;
        public final boolean isAbsentEse;
        public final boolean isAttendanceShortage;
        public final boolean isPassed;
        public final String grade;
        public final int gradePoint;
        public final String resultStatus;
        public final AttendanceEvaluation attendance;

        public CourseEvaluationResult(CourseType courseType, int credits,
                                    double rawInternalTotal, double maxInternalScale, double scaledInternal,
                                    Double rawEseScore, double maxEseScale, double scaledEse,
                                    double totalMarks, boolean isAbsentEse, boolean isAttendanceShortage,
                                    boolean isPassed, String grade, int gradePoint,
                                    String resultStatus, AttendanceEvaluation attendance) {
            this.courseType = courseType;
            this.credits = credits;
            this.rawInternalTotal = rawInternalTotal;
            this.maxInternalScale = maxInternalScale;
            this.scaledInternal = scaledInternal;
            this.rawEseScore = rawEseScore;
            this.maxEseScale = maxEseScale;
            this.scaledEse = scaledEse;
            this.totalMarks = totalMarks;
            this.isAbsentEse = isAbsentEse;
            this.isAttendanceShortage = isAttendanceShortage;
            this.isPassed = isPassed;
            this.grade = grade;
            this.gradePoint = gradePoint;
            this.resultStatus = resultStatus;
            this.attendance = attendance;
        }
    }

    /**
     * Evaluates a course using KEC R2024 academic rules with explicit precedence:
     * Precedence: SA > AB > Incomplete > RA > Pass Grades (O, A+, A, B+, B)
     */
    public static CourseEvaluationResult evaluateCourse(
            CourseType type,
            int credits,
            double internal100, // Internal score out of 100
            Double rawEse,      // Raw ESE score out of maxEseScale (or null if unentered)
            boolean isAbsentEse,
            AttendanceEvaluation attendance
    ) {
        if (type == null) type = CourseType.THEORY;
        if (credits < 0) credits = 0;
        if (internal100 < 0) internal100 = 0;
        if (internal100 > 100) internal100 = 100;

        double maxInternal = type.maxInternal;
        double maxEse = type.maxEse;
        double scaledInternal = (internal100 / 100.0) * maxInternal;

        // 1. Check Attendance Shortage (SA)
        boolean isSa = (attendance != null && attendance.isBarredFromEse());
        if (isSa) {
            return new CourseEvaluationResult(
                    type, credits, internal100, maxInternal, scaledInternal,
                    rawEse, maxEse, 0.0, scaledInternal, false, true,
                    false, "SA", 0, "SA / SHORTAGE OF ATTENDANCE", attendance
            );
        }

        // 2. Check Absent in ESE (AB)
        if (isAbsentEse) {
            return new CourseEvaluationResult(
                    type, credits, internal100, maxInternal, scaledInternal,
                    0.0, maxEse, 0.0, scaledInternal, true, false,
                    false, "AB", 0, "AB / ABSENT", attendance
            );
        }

        // 3. Check Incomplete (ESE unentered)
        if (rawEse == null) {
            return new CourseEvaluationResult(
                    type, credits, internal100, maxInternal, scaledInternal,
                    null, maxEse, 0.0, scaledInternal, false, false,
                    false, "—", 0, "INCOMPLETE / PENDING ESE", attendance
            );
        }

        double eseScore = rawEse;
        if (eseScore < 0) eseScore = 0;
        if (eseScore > maxEse) eseScore = maxEse;

        double totalMarks = roundToTwoDecimals(scaledInternal + eseScore);
        double minEse = type.minEseThreshold;

        // 4. Check ESE Minimum Threshold and Overall Pass Aggregate (50)
        boolean esePassed = eseScore >= minEse;
        boolean aggregatePassed = totalMarks >= 50.0;
        boolean passed = esePassed && aggregatePassed;

        String grade;
        int gradePoint;
        String resultStatus;

        if (!passed) {
            grade = "RA";
            gradePoint = 0;
            if (!esePassed && !aggregatePassed) {
                resultStatus = String.format("RA / FAIL (ESE < %.1f and Total < 50.0)", minEse);
            } else if (!esePassed) {
                resultStatus = String.format("RA / FAIL (ESE < %.1f min threshold)", minEse);
            } else {
                resultStatus = "RA / FAIL (Total < 50.0 min aggregate)";
            }
        } else {
            // R2024 Passing Grade boundaries:
            // O: 91–100 (GP 10), A+: 81–90 (GP 9), A: 71–80 (GP 8), B+: 61–70 (GP 7), B: 50–60 (GP 6)
            if (totalMarks >= 91.0) {
                grade = "O";
                gradePoint = 10;
            } else if (totalMarks >= 81.0) {
                grade = "A+";
                gradePoint = 9;
            } else if (totalMarks >= 71.0) {
                grade = "A";
                gradePoint = 8;
            } else if (totalMarks >= 61.0) {
                grade = "B+";
                gradePoint = 7;
            } else {
                grade = "B";
                gradePoint = 6;
            }
            resultStatus = "PASS";
        }

        return new CourseEvaluationResult(
                type, credits, internal100, maxInternal, scaledInternal,
                eseScore, maxEse, eseScore, totalMarks, false, false,
                passed, grade, gradePoint, resultStatus, attendance
        );
    }

    /**
     * Calculates SGPA according to R2024:
     * SGPA = Σ(credits * grade_point) / Σ(enrolled_credits)
     */
    public static double calculateSGPA(List<CourseEvaluationResult> courses) {
        if (courses == null || courses.isEmpty()) return 0.0;

        int totalCredits = 0;
        int weightedPoints = 0;

        for (CourseEvaluationResult c : courses) {
            totalCredits += c.credits;
            weightedPoints += (c.credits * c.gradePoint);
        }

        if (totalCredits <= 0) return 0.0;
        return roundToTwoDecimals((double) weightedPoints / (double) totalCredits);
    }

    /**
     * Equivalent percentage = SGPA * 10
     */
    public static double calculateEquivalentPercentage(double sgpa) {
        return roundToTwoDecimals(sgpa * 10.0);
    }

    public static double roundToTwoDecimals(double val) {
        return Math.round(val * 100.0) / 100.0;
    }
}

