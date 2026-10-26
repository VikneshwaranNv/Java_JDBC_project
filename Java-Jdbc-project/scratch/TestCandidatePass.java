import org.mindrot.jbcrypt.BCrypt;

public class TestCandidatePass {
    public static void main(String[] args) {
        String adminHash = "$2a$12$HMeYgnRzvn0BW.0.8hoMIe43ZsLid7ThqvcU6GNAYYuNdiCpr3NEu";
        String[] candidates = {
            "Admin@123", "Admin@1234", "admin123", "Admin123", "admin@123",
            "password", "Password123", "Password@123", "kecadmin", "KecAdmin123",
            "admin", "test_admin_sys", "Vickyy@07", "vickyy@07", "Kec@1234",
            "Kec@2024", "Admin2024", "Admin@2024", "Welcome123", "Welcome@123",
            "Admin1234", "Admin#123", "adminpassword", "Secret123", "Secret@123"
        };
        for (String c : candidates) {
            if (BCrypt.checkpw(c, adminHash)) {
                System.out.println("FOUND ADMIN PASSWORD: " + c);
                return;
            }
        }
        System.out.println("None of the candidates matched.");
    }
}

