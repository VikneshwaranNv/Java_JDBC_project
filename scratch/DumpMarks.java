import java.sql.*;

public class DumpMarks {
    public static void main(String[] args) throws Exception {
        try (Connection con = DriverManager.getConnection("jdbc:mysql://127.0.0.1:3306/kec_academic", "root", "Vickyy@07")) {
            String sql = """
                    SELECT am.mark_id, e.enrollment_id, s.roll_no, c.course_code, c.course_type,
                           am.assessment_name, am.max_marks, am.marks_obtained, am.is_absent
                    FROM assessment_marks am
                    JOIN enrollments e ON am.enrollment_id = e.enrollment_id
                    JOIN students s ON e.student_id = s.student_id
                    JOIN courses c ON e.course_id = c.course_id
                    ORDER BY e.enrollment_id, am.mark_id
                    """;
            try (Statement st = con.createStatement(); ResultSet rs = st.executeQuery(sql)) {
                System.out.printf("%-8s %-12s %-10s %-12s %-25s %-10s %-10s %-8s%n",
                        "EnrollID", "Roll", "Course", "Type", "Assessment", "Max", "Obtained", "Absent");
                while (rs.next()) {
                    System.out.printf("%-8d %-12s %-10s %-12s %-25s %-10.2f %-10.2f %-8b%n",
                            rs.getInt("enrollment_id"),
                            rs.getString("roll_no"),
                            rs.getString("course_code"),
                            rs.getString("course_type"),
                            rs.getString("assessment_name"),
                            rs.getDouble("max_marks"),
                            rs.getDouble("marks_obtained"),
                            rs.getBoolean("is_absent"));
                }
            }
        }
    }
}

