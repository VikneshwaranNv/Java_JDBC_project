import java.sql.*;

public class CheckDepts {
    public static void main(String[] args) throws Exception {
        try (Connection con = DriverManager.getConnection("jdbc:mysql://127.0.0.1:3306/kec_academic", "root", "Vickyy@07");
             Statement st = con.createStatement();
             ResultSet rs = st.executeQuery("SELECT student_id, roll_no, student_name, department FROM students")) {
            while (rs.next()) {
                System.out.printf("ID: %d | Roll: '%s' | Name: '%s' | Dept: '%s'%n",
                        rs.getInt("student_id"), rs.getString("roll_no"), rs.getString("student_name"), rs.getString("department"));
            }
        }
    }
}

