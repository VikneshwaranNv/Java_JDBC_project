public class TestLogin {
    public static void main(String[] args) throws Exception {
        UserSession sStud = AuthService.authenticate("test_stud_arun", "ValidStudentPass123!");
        System.out.println("LOGGED IN STUD: " + sStud);
    }
}

