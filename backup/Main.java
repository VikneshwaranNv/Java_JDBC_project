
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Scanner;

public class Main {

    private static final String URL =
        "jdbc:mysql://127.0.0.1:3306/kec_academic";

    private static final String USERNAME = "root";

    // Enter the same MySQL password used by your working program.
    private static final String PASSWORD = "Vickyy@07";

    private static Connection getConnection() throws Exception {
        return DriverManager.getConnection(URL, USERNAME, PASSWORD);
    }

    public static void main(String[] args) {

        Scanner sc = new Scanner(System.in);
        boolean running = true;

        while (running) {
            System.out.println("\n========================================");
            System.out.println("   KEC ACADEMIC EVALUATION SYSTEM");
            System.out.println("========================================");
            System.out.println("1. Search Student");
            System.out.println("2. View All Students");
            System.out.println("3. View All Courses");
            System.out.println("4. Generate Academic Marks Report");
            System.out.println("5. Exit");
            System.out.println("========================================");
            System.out.print("Enter your choice: ");

            String choice = sc.nextLine().trim();

            switch (choice) {
                case "1":
                    searchStudent(sc);
                    break;

                case "2":
                    viewAllStudents();
                    break;

                case "3":
                    viewAllCourses();
                    break;

                case "4":
                    generateMarksReport(sc);
                    break;

                case "5":
                    running = false;
                    System.out.println("Thank you for using KEC Academic Evaluation System!");
                    break;

                default:
                    System.out.println("Invalid choice. Enter a number from 1 to 5.");
            }
        }

        sc.close();
    }

    private static void searchStudent(Scanner sc) {

        System.out.print("Enter student roll number: ");
        String rollNo = sc.nextLine().trim();

        String query =
            "SELECT student_id, roll_no, student_name, department, semester " +
            "FROM students WHERE roll_no = ?";

        try (
            Connection con = getConnection();
            PreparedStatement ps = con.prepareStatement(query)
        ) {
            ps.setString(1, rollNo);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    System.out.println("\nSTUDENT DETAILS");
                    System.out.println("-------------------------------");
                    System.out.println("Student ID: " + rs.getInt("student_id"));
                    System.out.println("Roll Number: " + rs.getString("roll_no"));
                    System.out.println("Name: " + rs.getString("student_name"));
                    System.out.println("Department: " + rs.getString("department"));
                    System.out.println("Semester: " + rs.getInt("semester"));
                } else {
                    System.out.println("Student not found.");
                }
            }

        } catch (Exception e) {
            System.out.println("Error searching student: " + e.getMessage());
        }
    }

    private static void viewAllStudents() {

        String query =
            "SELECT student_id, roll_no, student_name, department, semester " +
            "FROM students ORDER BY roll_no";

        try (
            Connection con = getConnection();
            Statement stmt = con.createStatement();
            ResultSet rs = stmt.executeQuery(query)
        ) {
            System.out.println("\nALL STUDENTS");
            System.out.println("---------------------------------------------------------------");
            System.out.printf("%-5s %-15s %-22s %-12s %-8s%n",
                "ID", "Roll Number", "Name", "Department", "Semester");
            System.out.println("---------------------------------------------------------------");

            boolean found = false;

            while (rs.next()) {
                found = true;
                System.out.printf("%-5d %-15s %-22s %-12s %-8d%n",
                    rs.getInt("student_id"),
                    rs.getString("roll_no"),
                    rs.getString("student_name"),
                    rs.getString("department"),
                    rs.getInt("semester"));
            }

            if (!found) {
                System.out.println("No students found.");
            }

        } catch (Exception e) {
            System.out.println("Error retrieving students: " + e.getMessage());
        }
    }

    private static void viewAllCourses() {

        String query =
            "SELECT course_id, course_code, course_name, credits " +
            "FROM courses ORDER BY course_code";

        try (
            Connection con = getConnection();
            Statement stmt = con.createStatement();
            ResultSet rs = stmt.executeQuery(query)
        ) {
            System.out.println("\nALL COURSES");
            System.out.println("---------------------------------------------------------------");
            System.out.printf("%-5s %-12s %-40s %-8s%n",
                "ID", "Code", "Course Name", "Credits");
            System.out.println("---------------------------------------------------------------");

            boolean found = false;

            while (rs.next()) {
                found = true;
                System.out.printf("%-5d %-12s %-40s %-8d%n",
                    rs.getInt("course_id"),
                    rs.getString("course_code"),
                    rs.getString("course_name"),
                    rs.getInt("credits"));
            }

            if (!found) {
                System.out.println("No courses found.");
            }

        } catch (Exception e) {
            System.out.println("Error retrieving courses: " + e.getMessage());
        }
    }

    private static void generateMarksReport(Scanner sc) {

        System.out.print("Enter student roll number: ");
        String rollNo = sc.nextLine().trim();

        String query =
            "SELECT s.student_name, s.roll_no, c.course_code, " +
            "c.course_name, am.assessment_name, am.max_marks, " +
            "am.marks_obtained " +
            "FROM students s " +
            "JOIN enrollments e ON s.student_id = e.student_id " +
            "JOIN courses c ON e.course_id = c.course_id " +
            "JOIN assessment_marks am ON e.enrollment_id = am.enrollment_id " +
            "WHERE s.roll_no = ? " +
            "ORDER BY c.course_code, am.assessment_name";

        try (
            Connection con = getConnection();
            PreparedStatement ps = con.prepareStatement(query)
        ) {
            ps.setString(1, rollNo);

            try (ResultSet rs = ps.executeQuery()) {

                boolean found = false;
                double totalObtained = 0;
                double totalMaximum = 0;

                System.out.println("\nACADEMIC MARKS REPORT");
                System.out.println("========================================");

                while (rs.next()) {

                    if (!found) {
                        System.out.println("Student: " +
                            rs.getString("student_name"));
                        System.out.println("Roll Number: " +
                            rs.getString("roll_no"));
                        System.out.println("----------------------------------------");
                        found = true;
                    }

                    double obtained = rs.getDouble("marks_obtained");
                    double maximum = rs.getDouble("max_marks");

                    System.out.println("\nCourse: " +
                        rs.getString("course_code") + " - " +
                        rs.getString("course_name"));
                    System.out.println("Assessment: " +
                        rs.getString("assessment_name"));
                    System.out.printf("Marks: %.2f / %.2f%n",
                        obtained, maximum);

                    totalObtained += obtained;
                    totalMaximum += maximum;
                }

                if (!found) {
                    System.out.println(
                        "No assessment records found for this student.");
                } else if (totalMaximum > 0) {

                    double percentage =
                        (totalObtained / totalMaximum) * 100.0;

                    String grade;
                    String result;

                    // Demonstration thresholds, not official KEC regulations.
                    if (percentage >= 90) {
                        grade = "O";
                        result = "PASS";
                    } else if (percentage >= 80) {
                        grade = "A+";
                        result = "PASS";
                    } else if (percentage >= 70) {
                        grade = "A";
                        result = "PASS";
                    } else if (percentage >= 60) {
                        grade = "B+";
                        result = "PASS";
                    } else if (percentage >= 50) {
                        grade = "B";
                        result = "PASS";
                    } else if (percentage >= 40) {
                        grade = "C";
                        result = "PASS";
                    } else {
                        grade = "F";
                        result = "FAIL";
                    }

                    System.out.println("\n========================================");
                    System.out.println("FINAL SUMMARY");
                    System.out.printf("Total Marks: %.2f / %.2f%n",
                        totalObtained, totalMaximum);
                    System.out.printf("Percentage: %.2f%%%n", percentage);
                    System.out.println("Grade: " + grade);
                    System.out.println("Result: " + result);
                    System.out.println("========================================");

                } else {
                    System.out.println("No valid maximum marks available.");
                }
            }

        } catch (Exception e) {
            System.out.println("Error generating report: " + e.getMessage());
        }
    }
}
