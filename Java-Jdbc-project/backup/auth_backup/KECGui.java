import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.border.MatteBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.sql.*;
import java.util.LinkedHashMap;
import java.util.Map;

public class KECGui extends JFrame {

    private static final String DEFAULT_DB_URL =
            "jdbc:mysql://127.0.0.1:3306/kec_academic";
    private static final String DEFAULT_DB_USER = "root";
    private static final String DEFAULT_DB_PASSWORD = "Vickyy@07";

    private static String getDbUrl() {
        String url = System.getenv("KEC_DB_URL");
        return (url != null && !url.trim().isEmpty()) ? url.trim() : DEFAULT_DB_URL;
    }

    private static String getDbUser() {
        String user = System.getenv("KEC_DB_USER");
        return (user != null && !user.trim().isEmpty()) ? user.trim() : DEFAULT_DB_USER;
    }

    private static String getDbPassword() {
        String pass = System.getenv("KEC_DB_PASSWORD");
        return (pass != null) ? pass : DEFAULT_DB_PASSWORD;
    }

    // Professional academic dashboard color palette (User specified)
    public static final Color COLOR_NAVY = new Color(23, 43, 77);         // #172B4D
    public static final Color COLOR_NAVY_HOVER = new Color(34, 62, 108);  // #223E6C
    public static final Color COLOR_MUTED_BLUE = new Color(53, 92, 138);   // #355C8A
    public static final Color COLOR_WHITE = Color.WHITE;                  // #FFFFFF
    public static final Color COLOR_BG = new Color(246, 248, 251);         // #F6F8FB
    public static final Color COLOR_BORDER = new Color(227, 232, 239);     // #E3E8EF
    public static final Color COLOR_TEXT_DARK = new Color(23, 43, 77);
    public static final Color COLOR_TEXT_MUTED = new Color(100, 115, 135);

    public static final Color COLOR_GREEN = new Color(16, 124, 65);       // #107C41
    public static final Color COLOR_AMBER = new Color(178, 89, 0);        // #B25900
    public static final Color COLOR_RED = new Color(197, 34, 31);         // #C5221F
    public static final Color COLOR_PURPLE = new Color(109, 40, 217);

    // Typography
    public static final Font FONT_TITLE = new Font("Segoe UI", Font.BOLD, 22);
    public static final Font FONT_SUBTITLE = new Font("Segoe UI", Font.PLAIN, 13);
    public static final Font FONT_SECTION = new Font("Segoe UI", Font.BOLD, 15);
    public static final Font FONT_BOLD = new Font("Segoe UI", Font.BOLD, 14);
    public static final Font FONT_REGULAR = new Font("Segoe UI", Font.PLAIN, 14);
    public static final Font FONT_SMALL = new Font("Segoe UI", Font.PLAIN, 12);
    public static final Font FONT_MONO = new Font("Consolas", Font.PLAIN, 13);

    private final CardLayout layout = new CardLayout();
    private final JPanel pages = new JPanel(layout);

    private final JLabel studentCount = new JLabel("0");
    private final JLabel courseCount = new JLabel("0");
    private final JLabel assessmentCount = new JLabel("0");
    private final JLabel pendingSapCount = new JLabel("0");

    private JTable studentTable;
    private JTable courseTable;
    private JTable reportTable;

    private JTextField studentSearch;
    private JTextField reportRoll;
    private JTextArea reportSummary;

    private JLabel statusLabel;

    public KECGui() {
        setTitle("KEC ACADEMIX | Student Evaluation and Academic Performance Management System");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1360, 840);
        setMinimumSize(new Dimension(1080, 700));
        setLocationRelativeTo(null);

        getContentPane().setLayout(new BorderLayout());
        getContentPane().setBackground(COLOR_BG);

        buildSidebar();
        buildMainArea();

        refreshDashboard();
        loadStudents("");
        loadCourses();

        setVisible(true);
    }

    private Connection connect() throws SQLException {
        return DriverManager.getConnection(getDbUrl(), getDbUser(), getDbPassword());
    }

    private void buildSidebar() {
        JPanel sidebar = new JPanel();
        sidebar.setPreferredSize(new Dimension(265, 0));
        sidebar.setBackground(COLOR_NAVY);
        sidebar.setBorder(new EmptyBorder(24, 18, 20, 18));
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));

        // Brand Emblem & Title
        JPanel brand = new JPanel();
        brand.setOpaque(false);
        brand.setLayout(new BoxLayout(brand, BoxLayout.Y_AXIS));
        brand.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel emblem = new JLabel("KEC");
        emblem.setFont(new Font("Segoe UI", Font.BOLD, 36));
        emblem.setForeground(COLOR_WHITE);

        JLabel brandTitle = new JLabel("ACADEMIX PORTAL");
        brandTitle.setFont(new Font("Segoe UI", Font.BOLD, 11));
        brandTitle.setForeground(new Color(190, 207, 235));

        JLabel brandSub = new JLabel("Student Evaluation System");
        brandSub.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        brandSub.setForeground(new Color(160, 181, 215));

        brand.add(emblem);
        brand.add(Box.createVerticalStrut(2));
        brand.add(brandTitle);
        brand.add(Box.createVerticalStrut(4));
        brand.add(brandSub);

        sidebar.add(brand);
        sidebar.add(Box.createVerticalStrut(30));

        JLabel navLabel = new JLabel("MAIN NAVIGATION");
        navLabel.setFont(new Font("Segoe UI", Font.BOLD, 11));
        navLabel.setForeground(new Color(144, 166, 203));
        navLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        sidebar.add(navLabel);
        sidebar.add(Box.createVerticalStrut(12));

        addNav(sidebar, "Dashboard", "Dashboard");
        addNav(sidebar, "Student Directory", "Students");
        addNav(sidebar, "Course Directory", "Courses");
        addNav(sidebar, "Academic Reports", "Reports");
        addNav(sidebar, "Marks Management", "Marks");
        addNav(sidebar, "SAP Management", "SAP");

        sidebar.add(Box.createVerticalGlue());

        JSeparator separator = new JSeparator();
        separator.setForeground(new Color(57, 76, 111));
        separator.setBackground(new Color(57, 76, 111));
        separator.setMaximumSize(new Dimension(230, 1));
        sidebar.add(separator);
        sidebar.add(Box.createVerticalStrut(14));

        JLabel footer = new JLabel("KEC ACADEMIC SERVICES");
        footer.setFont(new Font("Segoe UI", Font.BOLD, 11));
        footer.setForeground(new Color(174, 193, 222));
        footer.setAlignmentX(Component.LEFT_ALIGNMENT);
        sidebar.add(footer);

        JLabel version = new JLabel("Academic Evaluation System  |  v2.0");
        version.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        version.setForeground(new Color(135, 158, 196));
        version.setAlignmentX(Component.LEFT_ALIGNMENT);
        sidebar.add(Box.createVerticalStrut(4));
        sidebar.add(version);

        getContentPane().add(sidebar, BorderLayout.WEST);
    }

    private void addNav(JPanel sidebar, String label, String page) {
        JButton button = new JButton(label);
        button.setFont(new Font("Segoe UI", Font.BOLD, 14));
        button.setForeground(new Color(235, 242, 252));
        button.setBackground(COLOR_NAVY);
        button.setHorizontalAlignment(SwingConstants.LEFT);
        button.setMaximumSize(new Dimension(235, 46));
        button.setPreferredSize(new Dimension(235, 46));
        button.setBorder(new EmptyBorder(0, 16, 0, 10));
        button.setFocusPainted(false);
        button.setOpaque(true);
        button.setBorderPainted(false);
        button.setAlignmentX(Component.LEFT_ALIGNMENT);
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        button.addActionListener(e -> {
            if ("Marks".equals(page)) {
                try {
                    new MarksManagement();
                } catch (Exception ex) {
                    ex.printStackTrace();
                    JOptionPane.showMessageDialog(
                            this,
                            "Unable to open Marks Management:\n" + ex.getMessage(),
                            "Error",
                            JOptionPane.ERROR_MESSAGE
                    );
                }
                return;
            }

            if ("SAP".equals(page)) {
                try {
                    Class.forName("SAPManagement").getDeclaredConstructor().newInstance();
                } catch (Exception ex) {
                    ex.printStackTrace();
                    JOptionPane.showMessageDialog(
                            this,
                            "Unable to open SAP Management:\n" + ex.getMessage(),
                            "Error",
                            JOptionPane.ERROR_MESSAGE
                    );
                }
                return;
            }

            layout.show(pages, page);

            if ("Dashboard".equals(page)) {
                refreshDashboard();
            } else if ("Students".equals(page)) {
                loadStudents(studentSearch.getText().trim());
            } else if ("Courses".equals(page)) {
                loadCourses();
            }
        });

        button.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent e) {
                button.setBackground(COLOR_NAVY_HOVER);
            }

            @Override
            public void mouseExited(java.awt.event.MouseEvent e) {
                button.setBackground(COLOR_NAVY);
            }
        });

        sidebar.add(button);
        sidebar.add(Box.createVerticalStrut(6));
    }

    private void buildMainArea() {
        JPanel main = new JPanel(new BorderLayout());
        main.setBackground(COLOR_BG);

        // Header Banner
        JPanel header = new JPanel(new BorderLayout(10, 6));
        header.setBackground(COLOR_WHITE);
        header.setBorder(new CompoundBorder(
                new MatteBorder(0, 0, 1, 0, COLOR_BORDER),
                new EmptyBorder(14, 28, 14, 28)
        ));

        JPanel heading = new JPanel();
        heading.setOpaque(false);
        heading.setLayout(new BoxLayout(heading, BoxLayout.Y_AXIS));

        JLabel college = new JLabel("KEC ACADEMIX  |  STUDENT EVALUATION SYSTEM");
        college.setFont(new Font("Segoe UI", Font.BOLD, 16));
        college.setForeground(COLOR_NAVY);

        JLabel sub = new JLabel("Curricular marks, student directory, courses, and co-curricular SAP reviews");
        sub.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        sub.setForeground(COLOR_TEXT_MUTED);

        heading.add(college);
        heading.add(Box.createVerticalStrut(3));
        heading.add(sub);

        JLabel badge = new JLabel("  ACADEMIC PORTAL  ");
        badge.setOpaque(true);
        badge.setBackground(new Color(235, 242, 252));
        badge.setForeground(COLOR_MUTED_BLUE);
        badge.setFont(new Font("Segoe UI", Font.BOLD, 12));
        badge.setBorder(new EmptyBorder(8, 12, 8, 12));

        header.add(heading, BorderLayout.WEST);
        header.add(badge, BorderLayout.EAST);

        pages.setBackground(COLOR_BG);
        pages.add(buildDashboard(), "Dashboard");
        pages.add(buildStudentsPage(), "Students");
        pages.add(buildCoursesPage(), "Courses");
        pages.add(buildReportsPage(), "Reports");

        statusLabel = new JLabel("  Ready  |  Database: kec_academic");
        statusLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        statusLabel.setForeground(COLOR_TEXT_MUTED);
        statusLabel.setBorder(new EmptyBorder(8, 16, 8, 16));

        JPanel footer = new JPanel(new BorderLayout());
        footer.setBackground(COLOR_WHITE);
        footer.setBorder(new MatteBorder(1, 0, 0, 0, COLOR_BORDER));
        footer.add(statusLabel, BorderLayout.WEST);

        main.add(header, BorderLayout.NORTH);
        main.add(pages, BorderLayout.CENTER);
        main.add(footer, BorderLayout.SOUTH);

        getContentPane().add(main, BorderLayout.CENTER);
    }

    private JPanel page(String title, String description) {
        JPanel panel = new JPanel(new BorderLayout(0, 18));
        panel.setBackground(COLOR_BG);
        panel.setBorder(new EmptyBorder(22, 26, 20, 26));

        JPanel heading = new JPanel();
        heading.setOpaque(false);
        heading.setLayout(new BoxLayout(heading, BoxLayout.Y_AXIS));

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(FONT_TITLE);
        titleLabel.setForeground(COLOR_NAVY);

        JLabel descLabel = new JLabel(description);
        descLabel.setFont(FONT_SUBTITLE);
        descLabel.setForeground(COLOR_TEXT_MUTED);

        heading.add(titleLabel);
        heading.add(Box.createVerticalStrut(4));
        heading.add(descLabel);

        panel.add(heading, BorderLayout.NORTH);
        return panel;
    }

    private JPanel buildDashboard() {
        JPanel panel = page(
                "Executive Academic Dashboard",
                "Real-time overview of student records, course offerings, marks evaluations, and SAP reviews"
        );

        JPanel body = new JPanel(new BorderLayout(0, 18));
        body.setOpaque(false);

        // 4 Key Stats: Students, Courses, Assessments, Pending SAP Reviews
        JPanel stats = new JPanel(new GridLayout(1, 4, 16, 0));
        stats.setOpaque(false);
        stats.add(statCard("REGISTERED STUDENTS", studentCount, COLOR_MUTED_BLUE, "Student Directory"));
        stats.add(statCard("ACTIVE COURSES", courseCount, COLOR_GREEN, "Course Curriculum"));
        stats.add(statCard("ASSESSMENT RECORDS", assessmentCount, COLOR_PURPLE, "Marks Database"));
        stats.add(statCard("PENDING SAP REVIEWS", pendingSapCount, COLOR_AMBER, "Awaiting Staff Review"));

        JPanel overview = new JPanel(new BorderLayout(0, 16));
        overview.setBackground(COLOR_WHITE);
        overview.setBorder(new CompoundBorder(
                new LineBorder(COLOR_BORDER, 1),
                new EmptyBorder(22, 24, 22, 24)
        ));

        JLabel overviewTitle = new JLabel("Academic Management Modules");
        overviewTitle.setFont(FONT_SECTION);
        overviewTitle.setForeground(COLOR_NAVY);

        JPanel details = new JPanel();
        details.setOpaque(false);
        details.setLayout(new BoxLayout(details, BoxLayout.Y_AXIS));

        details.add(infoLine("Student Directory", "Search, enroll, inspect, and update student demographic and semester records."));
        details.add(Box.createVerticalStrut(14));
        details.add(infoLine("Course Directory", "Review course codes, curriculum subjects, credit distributions, and requirements."));
        details.add(Box.createVerticalStrut(14));
        details.add(infoLine("Continuous Internal Evaluation (Marks)", "Enter CIA 1, CIA 2, CIA 3, and ESE scores with real-time pass/fail evaluation."));
        details.add(Box.createVerticalStrut(14));
        details.add(infoLine("Student Activity Points (SAP)", "Automated certificate text extraction, policy eligibility pre-check, and staff approval workflow."));

        overview.add(overviewTitle, BorderLayout.NORTH);
        overview.add(details, BorderLayout.CENTER);

        body.add(stats, BorderLayout.NORTH);
        body.add(overview, BorderLayout.CENTER);

        panel.add(body, BorderLayout.CENTER);
        return panel;
    }

    private JPanel statCard(String title, JLabel value, Color accent, String description) {
        JPanel card = new JPanel(new BorderLayout(0, 10));
        card.setBackground(COLOR_WHITE);
        card.setBorder(new CompoundBorder(
                new LineBorder(COLOR_BORDER, 1),
                new EmptyBorder(16, 18, 16, 18)
        ));

        JPanel top = new JPanel(new BorderLayout());
        top.setOpaque(false);

        JLabel name = new JLabel(title);
        name.setFont(new Font("Segoe UI", Font.BOLD, 11));
        name.setForeground(COLOR_TEXT_MUTED);

        JLabel dot = new JLabel("●");
        dot.setForeground(accent);
        dot.setFont(new Font("Segoe UI", Font.BOLD, 15));

        top.add(name, BorderLayout.WEST);
        top.add(dot, BorderLayout.EAST);

        value.setFont(new Font("Segoe UI", Font.BOLD, 32));
        value.setForeground(COLOR_NAVY);

        JLabel desc = new JLabel(description);
        desc.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        desc.setForeground(COLOR_TEXT_MUTED);

        card.add(top, BorderLayout.NORTH);
        card.add(value, BorderLayout.CENTER);
        card.add(desc, BorderLayout.SOUTH);
        return card;
    }

    private JPanel infoLine(String title, String description) {
        JPanel panel = new JPanel(new BorderLayout(12, 0));
        panel.setOpaque(false);

        JLabel marker = new JLabel("●");
        marker.setForeground(COLOR_MUTED_BLUE);
        marker.setFont(new Font("Segoe UI", Font.BOLD, 13));

        JPanel text = new JPanel();
        text.setOpaque(false);
        text.setLayout(new BoxLayout(text, BoxLayout.Y_AXIS));

        JLabel heading = new JLabel(title);
        heading.setFont(FONT_BOLD);
        heading.setForeground(COLOR_NAVY);

        JLabel desc = new JLabel(description);
        desc.setFont(FONT_REGULAR);
        desc.setForeground(COLOR_TEXT_MUTED);

        text.add(heading);
        text.add(Box.createVerticalStrut(3));
        text.add(desc);

        panel.add(marker, BorderLayout.WEST);
        panel.add(text, BorderLayout.CENTER);
        return panel;
    }

    private JPanel buildStudentsPage() {
        JPanel panel = page(
                "Student Directory",
                "Find, inspect, and manage student enrollment and department records"
        );

        studentSearch = new JTextField(22);
        styleField(studentSearch, 38);

        JButton search = button("Search Students", COLOR_MUTED_BLUE);
        JButton all = button("Show All", COLOR_NAVY);
        JButton addStudent = button("Add Student", COLOR_GREEN);
        JButton editStudent = button("Edit Selected", COLOR_PURPLE);
        JButton deleteStudent = button("Delete Selected", COLOR_RED);

        JPanel tools = toolbar();
        JLabel searchLbl = new JLabel("Search by Roll or Name:");
        searchLbl.setFont(FONT_BOLD);
        searchLbl.setForeground(COLOR_TEXT_DARK);
        tools.add(searchLbl);
        tools.add(studentSearch);
        tools.add(search);
        tools.add(all);
        tools.add(Box.createHorizontalStrut(15));
        tools.add(addStudent);
        tools.add(editStudent);
        tools.add(deleteStudent);

        studentTable = table(new String[]{
                "Student ID", "Roll Number", "Student Name", "Department", "Semester"
        });

        search.addActionListener(e -> loadStudents(studentSearch.getText().trim()));
        studentSearch.addActionListener(e -> loadStudents(studentSearch.getText().trim()));
        all.addActionListener(e -> {
            studentSearch.setText("");
            loadStudents("");
        });
        addStudent.addActionListener(e -> showStudentDialog(null));
        editStudent.addActionListener(e -> editSelectedStudent());
        deleteStudent.addActionListener(e -> deleteSelectedStudent());

        panel.add(tablePanel(tools, studentTable), BorderLayout.CENTER);
        return panel;
    }

    private void showStudentDialog(Object[] existing) {
        JTextField rollField = new JTextField(existing == null ? "" : String.valueOf(existing[1]), 18);
        JTextField nameField = new JTextField(existing == null ? "" : String.valueOf(existing[2]), 18);
        JTextField departmentField = new JTextField(existing == null ? "" : String.valueOf(existing[3]), 18);
        JTextField semesterField = new JTextField(existing == null ? "" : String.valueOf(existing[4]), 18);

        styleField(rollField, 36);
        styleField(nameField, 36);
        styleField(departmentField, 36);
        styleField(semesterField, 36);

        JPanel form = new JPanel(new GridLayout(0, 2, 10, 10));
        form.setBorder(new EmptyBorder(14, 14, 14, 14));
        form.add(new JLabel("Roll Number:"));
        form.add(rollField);
        form.add(new JLabel("Student Name:"));
        form.add(nameField);
        form.add(new JLabel("Department:"));
        form.add(departmentField);
        form.add(new JLabel("Semester (1-8):"));
        form.add(semesterField);

        int choice = JOptionPane.showConfirmDialog(
                this, form,
                existing == null ? "Add Student" : "Edit Student",
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE
        );
        if (choice != JOptionPane.OK_OPTION) return;

        String roll = rollField.getText().trim();
        String name = nameField.getText().trim();
        String department = departmentField.getText().trim();
        String semesterText = semesterField.getText().trim();

        if (roll.isEmpty() || name.isEmpty() || department.isEmpty() || semesterText.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please fill in all four fields.",
                    "Missing Information", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int semester;
        try {
            semester = Integer.parseInt(semesterText);
            if (semester < 1 || semester > 8) {
                throw new NumberFormatException();
            }
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Semester must be a whole number from 1 to 8.",
                    "Invalid Semester", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String sql = existing == null
                ? "INSERT INTO students (roll_no, student_name, department, semester) VALUES (?, ?, ?, ?)"
                : "UPDATE students SET roll_no = ?, student_name = ?, department = ?, semester = ? WHERE student_id = ?";

        try (Connection con = connect();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, roll);
            ps.setString(2, name);
            ps.setString(3, department);
            ps.setInt(4, semester);
            if (existing != null) ps.setInt(5, ((Number) existing[0]).intValue());

            int changed = ps.executeUpdate();
            if (changed > 0) {
                loadStudents(studentSearch.getText().trim());
                refreshDashboard();
                setStatus(existing == null ? "Student added: " + roll : "Student updated: " + roll);
                JOptionPane.showMessageDialog(this,
                        existing == null ? "Student added successfully." : "Student updated successfully.",
                        "Success", JOptionPane.INFORMATION_MESSAGE);
            }
        } catch (SQLIntegrityConstraintViolationException ex) {
            JOptionPane.showMessageDialog(this,
                    "That roll number already exists. Please enter a unique roll number.",
                    "Duplicate Roll Number", JOptionPane.WARNING_MESSAGE);
        } catch (SQLException ex) {
            databaseError(ex);
        }
    }

    private Object[] getSelectedStudent() {
        int viewRow = studentTable.getSelectedRow();
        if (viewRow < 0) {
            JOptionPane.showMessageDialog(this, "Please select a student row from the table first.",
                    "No Student Selected", JOptionPane.WARNING_MESSAGE);
            return null;
        }

        int row = studentTable.convertRowIndexToModel(viewRow);
        DefaultTableModel model = (DefaultTableModel) studentTable.getModel();
        return new Object[]{
                model.getValueAt(row, 0),
                model.getValueAt(row, 1),
                model.getValueAt(row, 2),
                model.getValueAt(row, 3),
                model.getValueAt(row, 4)
        };
    }

    private void editSelectedStudent() {
        Object[] selected = getSelectedStudent();
        if (selected != null) showStudentDialog(selected);
    }

    private void deleteSelectedStudent() {
        Object[] selected = getSelectedStudent();
        if (selected == null) return;

        String roll = String.valueOf(selected[1]);
        int confirm = JOptionPane.showConfirmDialog(
                this,
                "Delete student " + roll + "?\n\nThis cannot be undone. Students linked to enrollments may not be deletable.",
                "Confirm Student Deletion",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE
        );
        if (confirm != JOptionPane.YES_OPTION) return;

        String sql = "DELETE FROM students WHERE student_id = ?";
        try (Connection con = connect();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, ((Number) selected[0]).intValue());
            ps.executeUpdate();
            loadStudents(studentSearch.getText().trim());
            refreshDashboard();
            setStatus("Student deleted: " + roll);
            JOptionPane.showMessageDialog(this, "Student deleted successfully.",
                    "Success", JOptionPane.INFORMATION_MESSAGE);
        } catch (SQLIntegrityConstraintViolationException ex) {
            JOptionPane.showMessageDialog(this,
                    "This student has enrollment or assessment records, so the database prevented deletion.\n"
                            + "Preserve academic history, or remove related records only if you are certain.",
                    "Student Has Linked Records", JOptionPane.WARNING_MESSAGE);
        } catch (SQLException ex) {
            databaseError(ex);
        }
    }

    private JPanel buildCoursesPage() {
        JPanel panel = page(
                "Course Directory",
                "Registered curriculum courses, department offerings, and credit allocations"
        );

        JButton refresh = button("Refresh Courses", COLOR_MUTED_BLUE);
        JPanel tools = toolbar();
        tools.add(refresh);

        courseTable = table(new String[]{
                "Course ID", "Course Code", "Course Name", "Credits"
        });

        refresh.addActionListener(e -> loadCourses());

        panel.add(tablePanel(tools, courseTable), BorderLayout.CENTER);
        return panel;
    }

    private JPanel buildReportsPage() {
        JPanel panel = page(
                "Academic Reports",
                "Course-level marks and evaluation results for an individual student"
        );

        reportRoll = new JTextField(16);
        styleField(reportRoll, 38);

        JButton generate = button("Generate Report", COLOR_MUTED_BLUE);

        JPanel tools = toolbar();
        JLabel lbl = new JLabel("Student Roll Number:");
        lbl.setFont(FONT_BOLD);
        lbl.setForeground(COLOR_TEXT_DARK);
        tools.add(lbl);
        tools.add(reportRoll);
        tools.add(generate);

        reportTable = table(new String[]{
                "Course Code", "Course Name", "Assessment", "Marks Obtained", "Maximum Marks"
        });

        reportSummary = new JTextArea(6, 30);
        reportSummary.setEditable(false);
        reportSummary.setFont(FONT_MONO);
        reportSummary.setForeground(COLOR_TEXT_DARK);
        reportSummary.setBackground(COLOR_WHITE);
        reportSummary.setBorder(new EmptyBorder(12, 14, 12, 14));
        reportSummary.setLineWrap(true);
        reportSummary.setWrapStyleWord(true);

        generate.addActionListener(e -> generateReport());
        reportRoll.addActionListener(e -> generateReport());

        JPanel reportBody = new JPanel(new BorderLayout(0, 14));
        reportBody.setOpaque(false);
        reportBody.add(new JScrollPane(reportTable), BorderLayout.CENTER);

        JScrollPane summaryScroll = new JScrollPane(reportSummary);
        summaryScroll.setPreferredSize(new Dimension(100, 150));
        summaryScroll.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(COLOR_BORDER, 1),
                "Evaluation Summary", 0, 0, FONT_BOLD, COLOR_NAVY
        ));
        reportBody.add(summaryScroll, BorderLayout.SOUTH);

        JPanel combined = new JPanel(new BorderLayout(0, 14));
        combined.setOpaque(false);
        combined.add(tools, BorderLayout.NORTH);
        combined.add(reportBody, BorderLayout.CENTER);

        panel.add(combined, BorderLayout.CENTER);
        return panel;
    }

    private JPanel toolbar() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 8));
        panel.setBackground(COLOR_WHITE);
        panel.setBorder(new CompoundBorder(
                new LineBorder(COLOR_BORDER, 1),
                new EmptyBorder(4, 8, 4, 8)
        ));
        return panel;
    }

    private JPanel tablePanel(JPanel toolbar, JTable table) {
        JPanel panel = new JPanel(new BorderLayout(0, 12));
        panel.setOpaque(false);

        JPanel holder = new JPanel(new BorderLayout());
        holder.setBackground(COLOR_WHITE);
        holder.setBorder(new LineBorder(COLOR_BORDER, 1));
        holder.add(new JScrollPane(table), BorderLayout.CENTER);

        panel.add(toolbar, BorderLayout.NORTH);
        panel.add(holder, BorderLayout.CENTER);
        return panel;
    }

    private JButton button(String text, Color color) {
        JButton button = new JButton(text);
        button.setFont(new Font("Segoe UI", Font.BOLD, 13));
        button.setForeground(COLOR_WHITE);
        button.setBackground(color);
        button.setFocusPainted(false);
        button.setOpaque(true);
        button.setBorderPainted(false);
        button.setBorder(new EmptyBorder(10, 18, 10, 18));
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return button;
    }

    private void styleField(JTextField field, int height) {
        field.setFont(FONT_REGULAR);
        field.setForeground(COLOR_TEXT_DARK);
        field.setBackground(COLOR_WHITE);
        field.setCaretColor(COLOR_NAVY);
        field.setPreferredSize(new Dimension(field.getPreferredSize().width, height));
        field.setBorder(new CompoundBorder(
                new LineBorder(new Color(195, 207, 224), 1),
                new EmptyBorder(6, 10, 6, 10)
        ));
    }

    private JTable table(String[] columns) {
        DefaultTableModel model = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int col) {
                return false;
            }
        };

        JTable table = new JTable(model);
        table.setFont(FONT_REGULAR);
        table.setForeground(COLOR_TEXT_DARK);
        table.setBackground(COLOR_WHITE);
        table.setSelectionForeground(COLOR_TEXT_DARK);
        table.setSelectionBackground(new Color(222, 235, 255));
        table.setRowHeight(32);
        table.setGridColor(new Color(233, 237, 244));
        table.setShowVerticalLines(false);
        table.setFillsViewportHeight(true);
        table.setAutoCreateRowSorter(true);

        JTableHeader header = table.getTableHeader();
        header.setFont(new Font("Segoe UI", Font.BOLD, 13));
        header.setForeground(COLOR_WHITE);
        header.setBackground(COLOR_NAVY);
        header.setPreferredSize(new Dimension(100, 36));
        header.setReorderingAllowed(false);

        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(SwingConstants.CENTER);
        table.setDefaultRenderer(Integer.class, centerRenderer);

        return table;
    }

    private void loadStudents(String searchText) {
        if (studentTable == null) return;

        DefaultTableModel model = (DefaultTableModel) studentTable.getModel();
        model.setRowCount(0);

        String sql = "SELECT student_id, roll_no, student_name, department, semester FROM students WHERE roll_no LIKE ? OR student_name LIKE ? ORDER BY roll_no";

        try (Connection con = connect();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, "%" + searchText + "%");
            ps.setString(2, "%" + searchText + "%");

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    model.addRow(new Object[]{
                            rs.getInt("student_id"),
                            rs.getString("roll_no"),
                            rs.getString("student_name"),
                            rs.getString("department"),
                            rs.getInt("semester")
                    });
                }
            }

            setStatus("Student records loaded: " + model.getRowCount());

        } catch (SQLException e) {
            databaseError(e);
        }
    }

    private void loadCourses() {
        if (courseTable == null) return;

        DefaultTableModel model = (DefaultTableModel) courseTable.getModel();
        model.setRowCount(0);

        String sql = "SELECT course_id, course_code, course_name, credits FROM courses ORDER BY course_code";

        try (Connection con = connect();
             Statement st = con.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                model.addRow(new Object[]{
                        rs.getInt("course_id"),
                        rs.getString("course_code"),
                        rs.getString("course_name"),
                        rs.getInt("credits")
                });
            }

            setStatus("Course records loaded: " + model.getRowCount());

        } catch (SQLException e) {
            databaseError(e);
        }
    }

    private void refreshDashboard() {
        try (Connection con = connect()) {
            studentCount.setText(String.valueOf(count(con, "students")));
            courseCount.setText(String.valueOf(count(con, "courses")));
            assessmentCount.setText(String.valueOf(count(con, "assessment_marks")));

            // Count pending SAP activities
            try (Statement st = con.createStatement();
                 ResultSet rs = st.executeQuery("SELECT COUNT(*) FROM sap_activities WHERE status IN ('PENDING_REVIEW', 'PENDING_ANALYSIS')")) {
                if (rs.next()) {
                    pendingSapCount.setText(String.valueOf(rs.getInt(1)));
                }
            } catch (SQLException ignored) {
                pendingSapCount.setText("0");
            }

            setStatus("Connected to MySQL 'kec_academic' database");

        } catch (SQLException e) {
            studentCount.setText("--");
            courseCount.setText("--");
            assessmentCount.setText("--");
            pendingSapCount.setText("--");
            databaseError(e);
        }
    }

    private int count(Connection con, String table) throws SQLException {
        String sql = "SELECT COUNT(*) FROM " + table;
        try (Statement st = con.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            rs.next();
            return rs.getInt(1);
        }
    }

    private void generateReport() {
        String roll = reportRoll.getText().trim();

        if (roll.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter a student roll number.",
                    "Required Field", JOptionPane.WARNING_MESSAGE);
            return;
        }

        DefaultTableModel model = (DefaultTableModel) reportTable.getModel();
        model.setRowCount(0);
        reportSummary.setText("");

        String sql = """
                SELECT s.student_name, c.course_code, c.course_name,
                       am.assessment_name, am.max_marks, am.marks_obtained
                FROM students s
                JOIN enrollments e ON s.student_id = e.student_id
                JOIN courses c ON e.course_id = c.course_id
                JOIN assessment_marks am ON e.enrollment_id = am.enrollment_id
                WHERE s.roll_no = ?
                ORDER BY c.course_code, am.assessment_name
                """;

        String studentName = null;

        class CourseEvaluation {
            double cia1 = 0, cia2 = 0, cia3 = 0, ese = 0;
            int ciaCount = 0;
            boolean hasCiaOrEse = false;
            double totalObtained = 0;
            double totalMax = 0;
        }

        Map<String, CourseEvaluation> courseMarks = new LinkedHashMap<>();

        try (Connection con = connect();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, roll);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    studentName = rs.getString("student_name");
                    String courseCode = rs.getString("course_code");
                    String courseName = rs.getString("course_name");
                    String assessment = rs.getString("assessment_name");
                    double marks = rs.getDouble("marks_obtained");
                    double max = rs.getDouble("max_marks");

                    model.addRow(new Object[]{
                            courseCode, courseName, assessment, marks, max
                    });

                    String key = courseCode + " - " + courseName;
                    CourseEvaluation eval = courseMarks.computeIfAbsent(key, k -> new CourseEvaluation());
                    eval.totalObtained += marks;
                    eval.totalMax += max;

                    String norm = assessment != null ? assessment.replaceAll("[^A-Za-z0-9]", "").toUpperCase() : "";
                    if (norm.contains("CIA1") || norm.contains("INTERNALASSESSMENT1") || norm.contains("INTERNAL1") || norm.equals("TEST1")) {
                        eval.cia1 = (max > 0 && max != 100) ? (marks / max) * 100.0 : marks;
                        eval.ciaCount++;
                        eval.hasCiaOrEse = true;
                    } else if (norm.contains("CIA2") || norm.contains("INTERNALASSESSMENT2") || norm.contains("INTERNAL2") || norm.equals("TEST2")) {
                        eval.cia2 = (max > 0 && max != 100) ? (marks / max) * 100.0 : marks;
                        eval.ciaCount++;
                        eval.hasCiaOrEse = true;
                    } else if (norm.contains("CIA3") || norm.contains("INTERNALASSESSMENT3") || norm.contains("INTERNAL3") || norm.equals("TEST3")) {
                        eval.cia3 = (max > 0 && max != 100) ? (marks / max) * 100.0 : marks;
                        eval.ciaCount++;
                        eval.hasCiaOrEse = true;
                    } else if (norm.contains("ESE") || norm.contains("ENDSEMESTER") || norm.contains("SEMESTEREXAM")) {
                        eval.ese = (max > 0 && max != 60) ? (marks / max) * 60.0 : marks;
                        eval.hasCiaOrEse = true;
                    }
                }
            }

            if (studentName == null) {
                reportSummary.setText("No assessment records found for student roll number: " + roll + ".");
                setStatus("No academic assessment records found");
                return;
            }

            StringBuilder summary = new StringBuilder();
            summary.append("STUDENT ACADEMIC PERFORMANCE REPORT\n");
            summary.append("Student Name : ").append(studentName).append("\n");
            summary.append("Roll Number  : ").append(roll).append("\n\n");

            double overallTotal = 0;
            double overallMaximum = 0;
            int evaluatedCourses = 0;

            for (Map.Entry<String, CourseEvaluation> entry : courseMarks.entrySet()) {
                CourseEvaluation eval = entry.getValue();
                summary.append("============================================================\n");
                summary.append("Course: ").append(entry.getKey()).append("\n");

                if (eval.hasCiaOrEse) {
                    double internal = ((eval.cia1 + eval.cia2 + eval.cia3) / 3.0) * 0.40;
                    double total = internal + eval.ese;

                    summary.append(String.format("  CIA Internal : %.2f / 40.00%n", internal));
                    summary.append(String.format("  ESE Score    : %.2f / 60.00%n", eval.ese));
                    summary.append(String.format("  Total Score  : %.2f / 100.00%n", total));

                    if (eval.ese >= 27 && total >= 50) {
                        summary.append("  Result       : PASS\n\n");
                    } else {
                        summary.append("  Result       : RA / FAIL (Min 27 ESE and 50 Total required)\n\n");
                    }

                    overallTotal += total;
                    overallMaximum += 100;
                } else if (eval.totalMax > 0) {
                    double pct = (eval.totalObtained / eval.totalMax) * 100.0;
                    summary.append(String.format("  Marks Obtained: %.2f / %.2f (%.2f%%)%n",
                            eval.totalObtained, eval.totalMax, pct));
                    if (pct >= 50) {
                        summary.append("  Result        : PASS\n\n");
                    } else {
                        summary.append("  Result        : RA / FAIL\n\n");
                    }

                    overallTotal += pct;
                    overallMaximum += 100;
                }

                evaluatedCourses++;
            }

            if (evaluatedCourses > 0) {
                double percentage = overallTotal / overallMaximum * 100.0;
                summary.append("============================================================\n");
                summary.append(String.format("Total Courses Evaluated: %d%n", evaluatedCourses));
                summary.append(String.format("Overall Weighted Average: %.2f%%%n", percentage));
                summary.append("Status: Official continuous evaluation calculation.\n");
            } else {
                summary.append("No course assessment details available.");
            }

            reportSummary.setText(summary.toString());
            reportSummary.setCaretPosition(0);
            setStatus("Academic report generated for " + roll);

        } catch (SQLException e) {
            databaseError(e);
        }
    }

    private void setStatus(String message) {
        if (statusLabel != null) {
            statusLabel.setText("  " + message);
        }
    }

    private void databaseError(SQLException e) {
        setStatus("Database operation failed");
        String msg = e.getMessage();
        String pass = getDbPassword();
        if (pass != null && !pass.isEmpty()) {
            msg = msg.replace(pass, "******");
        }

        JOptionPane.showMessageDialog(
                this,
                "Could not access the academic database.\n\n" + msg,
                "Database Error",
                JOptionPane.ERROR_MESSAGE
        );
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception ignored) {
            }
            new KECGui();
        });
    }
}
