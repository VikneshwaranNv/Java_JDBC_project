import java.sql.*;

public class ListUsers {
    public static void main(String[] args) throws Exception {
        try (Connection c = DriverManager.getConnection("jdbc:mysql://127.0.0.1:3306/kec_academic", "root", "Vickyy@07");
             ResultSet rs = c.createStatement().executeQuery("SELECT user_id, username, role, student_id, is_active FROM app_users")) {
            while (rs.next()) {
                System.out.println(rs.getInt(1) + " | " + rs.getString(2) + " | " + rs.getString(3) + " | student_id: " + rs.getObject(4) + " | active: " + rs.getBoolean(5));
            }
        }
    }
}

