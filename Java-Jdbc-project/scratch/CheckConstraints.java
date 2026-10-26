import java.sql.*;

public class CheckConstraints {
    public static void main(String[] args) throws Exception {
        try (Connection con = DriverManager.getConnection("jdbc:mysql://127.0.0.1:3306/kec_academic", "root", "Vickyy@07")) {
            DatabaseMetaData meta = con.getMetaData();
            System.out.println("--- INDEXES ON ENROLLMENTS ---");
            try (ResultSet rs = meta.getIndexInfo("kec_academic", null, "enrollments", false, false)) {
                while (rs.next()) {
                    System.out.printf("Index: %-25s Column: %-20s NonUnique: %b%n",
                            rs.getString("INDEX_NAME"), rs.getString("COLUMN_NAME"), rs.getBoolean("NON_UNIQUE"));
                }
            }

            System.out.println("--- FOREIGN KEYS ON ENROLLMENTS ---");
            try (ResultSet rs = meta.getImportedKeys("kec_academic", null, "enrollments")) {
                while (rs.next()) {
                    System.out.printf("FK: %-25s PKTable: %-15s PKCol: %-15s FKCol: %-15s%n",
                            rs.getString("FK_NAME"), rs.getString("PKTABLE_NAME"), rs.getString("PKCOLUMN_NAME"), rs.getString("FKCOLUMN_NAME"));
                }
            }

            System.out.println("--- FOREIGN KEYS REFERENCING ENROLLMENTS ---");
            try (ResultSet rs = meta.getExportedKeys("kec_academic", null, "enrollments")) {
                while (rs.next()) {
                    System.out.printf("Referenced by: Table %-18s FKCol: %-18s FKName: %s%n",
                            rs.getString("FKTABLE_NAME"), rs.getString("FKCOLUMN_NAME"), rs.getString("FK_NAME"));
                }
            }
        }
    }
}

