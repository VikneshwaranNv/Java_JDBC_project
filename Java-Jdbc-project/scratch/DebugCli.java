public class DebugCli {
    public static void main(String[] args) {
        // Test 4
        System.out.println("--- DEBUG TEST 4 ---");
        String in4 = "test_admin_sys\nAdminSecret2026!\n3\n1\n0\n0\n";
        String out4 = TestCliComplete.executeCliWithInput(in4);
        System.out.println(out4);

        // Test 5
        System.out.println("--- DEBUG TEST 5 ---");
        String in5 = "test_admin_sys\nAdminSecret2026!\n5\n24CSE001\n4\nN\n0\n";
        String out5 = TestCliComplete.executeCliWithInput(in5);
        System.out.println(out5);

        // Test 7
        System.out.println("--- DEBUG TEST 7 ---");
        String in7 = "test_admin_sys\nAdminSecret2026!\n7\nCSE\n4\n2026-2027\n0\n";
        String out7 = TestCliComplete.executeCliWithInput(in7);
        System.out.println(out7);
    }
}

