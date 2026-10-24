import java.io.BufferedReader;
import java.io.FileReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;

public class ApplyMigration002 {
    public static void main(String[] args) throws Exception {
        String url = System.getenv("KEC_DB_URL") != null ? System.getenv("KEC_DB_URL") : "jdbc:mysql://127.0.0.1:3306/kec_academic?allowMultiQueries=true";
        String user = System.getenv("KEC_DB_USER") != null ? System.getenv("KEC_DB_USER") : "root";
        String pass = System.getenv("KEC_DB_PASSWORD") != null ? System.getenv("KEC_DB_PASSWORD") : "Vickyy@07";

        StringBuilder sb = new StringBuilder();
        try (BufferedReader br = new BufferedReader(new FileReader("migrations/002_r2024_evaluation_system.sql"))) {
            String line;
            while ((line = br.readLine()) != null) {
                sb.append(line).append("\n");
            }
        }

        try (Connection con = DriverManager.getConnection(url, user, pass);
             Statement st = con.createStatement()) {
            st.execute(sb.toString());
            System.out.println("Migration 002 applied successfully.");
        }
    }
}

