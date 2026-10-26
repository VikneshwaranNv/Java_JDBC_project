public class DebugCli2 {
    public static void main(String[] args) {
        String in5 = "test_admin_sys\nAdminSecret2026!\n5\n24CSE001\n4\nN\n0\n";
        String out5 = TestCliComplete.executeCliWithInput(in5);
        System.out.println("OUTPUT OF 5:\n" + out5);
    }
}

