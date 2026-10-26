import java.sql.*;

public class CheckCoursesIndexes {
    public static void main(String[] args) throws Exception {
        try (Connection con = DriverManager.getConnection("jdbc:mysql://127.0.0.1:3306/kec_academic", "root", "Vickyy@07")) {
            DatabaseMetaData meta = con.getMetaData();
            try (ResultSet rs = meta.getIndexInfo("kec_academic", null, "courses", false, false)) {
                while (rs.next()) {
                    System.out.printf("Index: %-20s Col: %-15s NonUnique: %b%n",
                            rs.getString("INDEX_NAME"), rs.getString("COLUMN_NAME"), rs.getBoolean("NON_UNIQUE"));
                }
            }
        }
    }
}

