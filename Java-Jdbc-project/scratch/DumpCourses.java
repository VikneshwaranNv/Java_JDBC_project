import java.sql.*;

public class DumpCourses {
    public static void main(String[] args) throws Exception {
        try (Connection con = DriverManager.getConnection("jdbc:mysql://127.0.0.1:3306/kec_academic", "root", "Vickyy@07")) {
            System.out.println("--- COURSES ---");
            try (Statement st = con.createStatement(); ResultSet rs = st.executeQuery("SELECT * FROM courses")) {
                while (rs.next()) {
                    System.out.printf("ID: %d | Code: '%s' | Name: '%s' | Type: %s | Credits: %d%n",
                            rs.getInt("course_id"), rs.getString("course_code"), rs.getString("course_name"),
                            rs.getString("course_type"), rs.getInt("credits"));
                }
            }

            System.out.println("\n--- ENROLLMENTS ---");
            try (Statement st = con.createStatement(); ResultSet rs = st.executeQuery("SELECT * FROM enrollments")) {
                while (rs.next()) {
                    System.out.printf("Enrollment ID: %d | Student ID: %d | Course ID: %d | Sem: %d | AY: '%s'%n",
                            rs.getInt("enrollment_id"), rs.getInt("student_id"), rs.getInt("course_id"),
                            rs.getInt("semester"), rs.getString("academic_year"));
                }
            }
        }
    }
}

