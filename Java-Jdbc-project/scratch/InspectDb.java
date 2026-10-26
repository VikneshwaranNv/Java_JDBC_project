import java.sql.*;

public class InspectDb {
    public static void main(String[] args) throws Exception {
        try (Connection con = DriverManager.getConnection("jdbc:mysql://127.0.0.1:3306/kec_academic", "root", "Vickyy@07")) {
            System.out.println("--- LIVE TABLES & ROW COUNTS ---");
            DatabaseMetaData meta = con.getMetaData();
            try (ResultSet rs = meta.getTables("kec_academic", null, "%", new String[]{"TABLE"})) {
                while (rs.next()) {
                    String table = rs.getString("TABLE_NAME");
                    try (Statement st = con.createStatement(); ResultSet countRs = st.executeQuery("SELECT COUNT(*) FROM " + table)) {
                        countRs.next();
                        System.out.printf("Table: %-22s Rows: %d%n", table, countRs.getInt(1));
                    }
                }
            }

            System.out.println("\n--- COURSES SCHEMA ---");
            try (ResultSet rs = meta.getColumns("kec_academic", null, "courses", "%")) {
                while (rs.next()) {
                    System.out.printf("  %-18s %-15s Nullable: %s%n", rs.getString("COLUMN_NAME"), rs.getString("TYPE_NAME"), rs.getString("IS_NULLABLE"));
                }
            }

            System.out.println("\n--- ATTENDANCE SCHEMA ---");
            try (ResultSet rs = meta.getColumns("kec_academic", null, "attendance", "%")) {
                while (rs.next()) {
                    System.out.printf("  %-25s %-15s Nullable: %s%n", rs.getString("COLUMN_NAME"), rs.getString("TYPE_NAME"), rs.getString("IS_NULLABLE"));
                }
            }

            System.out.println("\n--- ENROLLMENTS SCHEMA ---");
            try (ResultSet rs = meta.getColumns("kec_academic", null, "enrollments", "%")) {
                while (rs.next()) {
                    System.out.printf("  %-18s %-15s Nullable: %s%n", rs.getString("COLUMN_NAME"), rs.getString("TYPE_NAME"), rs.getString("IS_NULLABLE"));
                }
            }

            System.out.println("\n--- ASSESSMENT_MARKS SCHEMA ---");
            try (ResultSet rs = meta.getColumns("kec_academic", null, "assessment_marks", "%")) {
                while (rs.next()) {
                    System.out.printf("  %-18s %-15s Nullable: %s%n", rs.getString("COLUMN_NAME"), rs.getString("TYPE_NAME"), rs.getString("IS_NULLABLE"));
                }
            }
        }
    }
}

