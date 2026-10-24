import java.io.PrintWriter;
import java.sql.*;

public class BackupDb {
    public static void main(String[] args) throws Exception {
        String url = System.getenv("KEC_DB_URL") != null ? System.getenv("KEC_DB_URL") : "jdbc:mysql://127.0.0.1:3306/kec_academic";
        String user = System.getenv("KEC_DB_USER") != null ? System.getenv("KEC_DB_USER") : "root";
        String pass = System.getenv("KEC_DB_PASSWORD") != null ? System.getenv("KEC_DB_PASSWORD") : "Vickyy@07";

        String outputFile = "backup/kec_academic_pre_r2024_backup.sql";

        try (Connection con = DriverManager.getConnection(url, user, pass);
             PrintWriter out = new PrintWriter(outputFile, "UTF-8")) {

            out.println("-- ====================================================================");
            out.println("-- KEC ACADEMIX Pre-R2024 Database Backup");
            out.println("-- Timestamp: " + new java.util.Date());
            out.println("-- Database: kec_academic");
            out.println("-- ====================================================================");
            out.println("SET FOREIGN_KEY_CHECKS = 0;");
            out.println();

            DatabaseMetaData meta = con.getMetaData();
            String[] tables = {"app_users", "students", "courses", "enrollments", "assessment_marks", "sap_activities", "sap_activity_reviews", "sap_bonus_rules"};

            for (String table : tables) {
                out.println("--");
                out.println("-- Data for table `" + table + "`");
                out.println("--");
                try (Statement st = con.createStatement();
                     ResultSet rs = st.executeQuery("SELECT * FROM " + table)) {
                    ResultSetMetaData rsmd = rs.getMetaData();
                    int colCount = rsmd.getColumnCount();

                    while (rs.next()) {
                        StringBuilder sb = new StringBuilder();
                        sb.append("INSERT INTO `").append(table).append("` (");
                        for (int i = 1; i <= colCount; i++) {
                            sb.append("`").append(rsmd.getColumnName(i)).append("`");
                            if (i < colCount) sb.append(", ");
                        }
                        sb.append(") VALUES (");
                        for (int i = 1; i <= colCount; i++) {
                            Object val = rs.getObject(i);
                            if (val == null) {
                                sb.append("NULL");
                            } else if (val instanceof Number || val instanceof Boolean) {
                                if (val instanceof Boolean) {
                                    sb.append(((Boolean) val) ? 1 : 0);
                                } else {
                                    sb.append(val);
                                }
                            } else {
                                String s = val.toString().replace("'", "\\'");
                                sb.append("'").append(s).append("'");
                            }
                            if (i < colCount) sb.append(", ");
                        }
                        sb.append(");");
                        out.println(sb.toString());
                    }
                }
                out.println();
            }

            out.println("SET FOREIGN_KEY_CHECKS = 1;");
            out.println("-- Backup complete.");
            System.out.println("Database safely backed up to: " + outputFile);
        }
    }
}

