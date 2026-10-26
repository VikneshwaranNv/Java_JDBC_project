
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;
import java.awt.*;
import java.sql.*;

public class KECGui extends JFrame {

    private static final String URL =
        "jdbc:mysql://127.0.0.1:3306/kec_academic";
    private static final String USER = "root";
    private static final String PASSWORD = "Vickyy@07";

    // Professional college portal color palette
    private static final Color NAVY = new Color(17, 35, 68);
    private static final Color NAVY_LIGHT = new Color(28, 52, 91);
    private static final Color BLUE = new Color(35, 99, 190);
    private static final Color BG = new Color(244, 247, 252);
    private static final Color WHITE = Color.WHITE;
    private static final Color DARK = new Color(35, 48, 69);
    private static final Color MUTED = new Color(102, 117, 139);
    private static final Color BORDER = new Color(222, 229, 239);
    private static final Color GREEN = new Color(20, 142, 112);
    private static final Color PURPLE = new Color(126, 87, 194);

    private final CardLayout layout = new CardLayout();
    private final JPanel pages = new JPanel(layout);

    private JLabel studentCount = new JLabel("0");
    private JLabel courseCount = new JLabel("0");
    private JLabel assessmentCount = new JLabel("0");

    private JTable studentTable;
    private JTable courseTable;
    private JTable reportTable;

    private JTextField studentSearch;
    private JTextField reportRoll;
    private JTextArea reportSummary;

    private JLabel statusLabel;

    public KECGui() {
        setTitle("KEC | Academic Evaluation System");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1280, 800);
        setMinimumSize(new Dimension(1000, 650));
        setLocationRelativeTo(null);

        getContentPane().setLayout(new BorderLayout());
        getContentPane().setBackground(BG);

        buildSidebar();
        buildMainArea();

        refreshDashboard();
        loadStudents("");
        loadCourses();

        setVisible(true);
    }

    private Connection connect() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }

    private void buildSidebar() {
        JPanel sidebar = new JPanel();
        sidebar.setPreferredSize(new Dimension(245, 0));
        sidebar.setBackground(NAVY);
        sidebar.setBorder(new EmptyBorder(25, 16, 18, 16));
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));

        JPanel brand = new JPanel();
        brand.setOpaque(false);
        brand.setLayout(new BoxLayout(brand, BoxLayout.Y_AXIS));
        brand.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel emblem = new JLabel("KEC");
        emblem.setFont(new Font("SansSerif", Font.BOLD, 34));
        emblem.setForeground(WHITE);

        JLabel brandTitle = new JLabel("ACADEMIC PORTAL");
        brandTitle.setFont(new Font("SansSerif", Font.BOLD, 11));
        brandTitle.setForeground(new Color(190, 207, 235));

        JLabel brandSub = new JLabel("Student Evaluation System");
        brandSub.setFont(new Font("SansSerif", Font.PLAIN, 11));
        brandSub.setForeground(new Color(160, 181, 215));

        brand.add(emblem);
        brand.add(Box.createVerticalStrut(3));
        brand.add(brandTitle);
        brand.add(Box.createVerticalStrut(6));
        brand.add(brandSub);

        sidebar.add(brand);
        sidebar.add(Box.createVerticalStrut(34));

        JLabel navLabel = new JLabel("MAIN MENU");
        navLabel.setFont(new Font("SansSerif", Font.BOLD, 10));
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
        separator.setMaximumSize(new Dimension(220, 1));
        sidebar.add(separator);
        sidebar.add(Box.createVerticalStrut(14));

        JLabel footer = new JLabel("KEC ACADEMIC SERVICES");
        footer.setFont(new Font("SansSerif", Font.BOLD, 10));
        footer.setForeground(new Color(174, 193, 222));
        footer.setAlignmentX(Component.LEFT_ALIGNMENT);
        sidebar.add(footer);

        JLabel version = new JLabel("Academic Portal  |  v1.0");
        version.setFont(new Font("SansSerif", Font.PLAIN, 10));
        version.setForeground(new Color(135, 158, 196));
        version.setAlignmentX(Component.LEFT_ALIGNMENT);
        sidebar.add(Box.createVerticalStrut(5));
        sidebar.add(version);

        getContentPane().add(sidebar, BorderLayout.WEST);
    }

    private void addNav(JPanel sidebar, String label, String page) {
        JButton button = new JButton(label);
        button.setFont(new Font("SansSerif", Font.BOLD, 13));
        button.setForeground(new Color(232, 239, 250));
        button.setBackground(NAVY);
        button.setHorizontalAlignment(SwingConstants.LEFT);
        button.setMaximumSize(new Dimension(220, 46));
        button.setPreferredSize(new Dimension(220, 46));
        button.setBorder(new EmptyBorder(0, 14, 0, 8));
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
                button.setBackground(NAVY_LIGHT);
            }

            @Override
            public void mouseExited(java.awt.event.MouseEvent e) {
                button.setBackground(NAVY);
            }
        });

        sidebar.add(button);
        sidebar.add(Box.createVerticalStrut(7));
    }

    private void buildMainArea() {
        JPanel main = new JPanel(new BorderLayout());
        main.setBackground(BG);

        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(WHITE);
        header.setBorder(new CompoundBorder(
            new MatteBorder(0, 0, 1, 0, BORDER),
            new EmptyBorder(15, 28, 15, 28)
        ));

        JPanel heading = new JPanel();
        heading.setOpaque(false);
        heading.setLayout(new BoxLayout(heading, BoxLayout.Y_AXIS));

        JLabel college = new JLabel(
            "KEC  |  ACADEMIC EVALUATION SYSTEM"
        );
        college.setFont(new Font("SansSerif", Font.BOLD, 15));
        college.setForeground(NAVY);

        JLabel sub = new JLabel(
            "Student records, courses and academic performance"
        );
        sub.setFont(new Font("SansSerif", Font.PLAIN, 12));
        sub.setForeground(MUTED);

        heading.add(college);
        heading.add(Box.createVerticalStrut(5));
        heading.add(sub);

        JLabel badge = new JLabel("  ACADEMIC PORTAL  ");
        badge.setOpaque(true);
        badge.setBackground(new Color(229, 240, 255));
        badge.setForeground(BLUE);
        badge.setFont(new Font("SansSerif", Font.BOLD, 11));
        badge.setBorder(new EmptyBorder(8, 8, 8, 8));

        header.add(heading, BorderLayout.WEST);
        header.add(badge, BorderLayout.EAST);

        pages.setBackground(BG);
        pages.add(buildDashboard(), "Dashboard");
        pages.add(buildStudentsPage(), "Students");
        pages.add(buildCoursesPage(), "Courses");
        pages.add(buildReportsPage(), "Reports");

        statusLabel = new JLabel("  Ready  |  Database: kec_academic");
        statusLabel.setFont(new Font("SansSerif", Font.PLAIN, 11));
        statusLabel.setForeground(MUTED);
        statusLabel.setBorder(new EmptyBorder(9, 15, 9, 15));

        JPanel footer = new JPanel(new BorderLayout());
        footer.setBackground(WHITE);
        footer.setBorder(new MatteBorder(1, 0, 0, 0, BORDER));
        footer.add(statusLabel, BorderLayout.WEST);

        main.add(header, BorderLayout.NORTH);
        main.add(pages, BorderLayout.CENTER);
        main.add(footer, BorderLayout.SOUTH);

        getContentPane().add(main, BorderLayout.CENTER);
    }

    private JPanel page(String title, String description) {
        JPanel panel = new JPanel(new BorderLayout(0, 22));
        panel.setBackground(BG);
        panel.setBorder(new EmptyBorder(28, 30, 25, 30));

        JPanel heading = new JPanel();
        heading.setOpaque(false);
        heading.setLayout(new BoxLayout(heading, BoxLayout.Y_AXIS));

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 26));
        titleLabel.setForeground(DARK);

        JLabel descLabel = new JLabel(description);
        descLabel.setFont(new Font("SansSerif", Font.PLAIN, 13));
        descLabel.setForeground(MUTED);

        heading.add(titleLabel);
        heading.add(Box.createVerticalStrut(7));
        heading.add(descLabel);

        panel.add(heading, BorderLayout.NORTH);
        return panel;
    }

    private JPanel buildDashboard() {
        JPanel panel = page(
            "Dashboard",
            "Overview of student records and academic information"
        );

        JPanel body = new JPanel(new BorderLayout(0, 22));
        body.setOpaque(false);

        JPanel stats = new JPanel(new GridLayout(1, 3, 18, 0));
        stats.setOpaque(false);
        stats.add(statCard("REGISTERED STUDENTS",
            studentCount, BLUE, "Student directory"));
        stats.add(statCard("AVAILABLE COURSES",
            courseCount, GREEN, "Course directory"));
        stats.add(statCard("ASSESSMENT RECORDS",
            assessmentCount, PURPLE, "Academic marks"));

        JPanel overview = new JPanel(new BorderLayout(0, 18));
        overview.setBackground(WHITE);
        overview.setBorder(new CompoundBorder(
            new LineBorder(BORDER, 1),
            new EmptyBorder(24, 24, 24, 24)
        ));

        JLabel overviewTitle = new JLabel("Welcome to the Academic Portal");
        overviewTitle.setFont(new Font("SansSerif", Font.BOLD, 19));
        overviewTitle.setForeground(DARK);

        JPanel details = new JPanel();
        details.setOpaque(false);
        details.setLayout(new BoxLayout(details, BoxLayout.Y_AXIS));

        details.add(infoLine("Student Directory",
            "Search students by roll number or name."));
        details.add(Box.createVerticalStrut(17));
        details.add(infoLine("Course Directory",
            "Review course codes, names and credit values."));
        details.add(Box.createVerticalStrut(17));
        details.add(infoLine("Academic Reports",
            "Review assessment marks and combined percentages."));

        overview.add(overviewTitle, BorderLayout.NORTH);
        overview.add(details, BorderLayout.CENTER);

        body.add(stats, BorderLayout.NORTH);
        body.add(overview, BorderLayout.CENTER);

        panel.add(body, BorderLayout.CENTER);
        return panel;
    }

    private JPanel statCard(
        String title, JLabel value, Color accent, String description
    ) {
        JPanel card = new JPanel(new BorderLayout(0, 13));
        card.setBackground(WHITE);
        card.setBorder(new CompoundBorder(
            new LineBorder(BORDER, 1),
            new EmptyBorder(19, 20, 19, 20)
        ));

        JPanel top = new JPanel(new BorderLayout());
        top.setOpaque(false);

        JLabel name = new JLabel(title);
        name.setFont(new Font("SansSerif", Font.BOLD, 11));
        name.setForeground(MUTED);

        JLabel dot = new JLabel("●");
        dot.setForeground(accent);
        dot.setFont(new Font("SansSerif", Font.BOLD, 16));

        top.add(name, BorderLayout.WEST);
        top.add(dot, BorderLayout.EAST);

        value.setFont(new Font("SansSerif", Font.BOLD, 34));
        value.setForeground(DARK);

        JLabel desc = new JLabel(description);
        desc.setFont(new Font("SansSerif", Font.PLAIN, 11));
        desc.setForeground(MUTED);

        card.add(top, BorderLayout.NORTH);
        card.add(value, BorderLayout.CENTER);
        card.add(desc, BorderLayout.SOUTH);
        return card;
    }

    private JPanel infoLine(String title, String description) {
        JPanel panel = new JPanel(new BorderLayout(12, 0));
        panel.setOpaque(false);

        JLabel marker = new JLabel("●");
        marker.setForeground(BLUE);
        marker.setFont(new Font("SansSerif", Font.BOLD, 13));

        JPanel text = new JPanel();
        text.setOpaque(false);
        text.setLayout(new BoxLayout(text, BoxLayout.Y_AXIS));

        JLabel heading = new JLabel(title);
        heading.setFont(new Font("SansSerif", Font.BOLD, 14));
        heading.setForeground(DARK);

        JLabel desc = new JLabel(description);
        desc.setFont(new Font("SansSerif", Font.PLAIN, 12));
        desc.setForeground(MUTED);

        text.add(heading);
        text.add(Box.createVerticalStrut(5));
        text.add(desc);

        panel.add(marker, BorderLayout.WEST);
        panel.add(text, BorderLayout.CENTER);
        return panel;
    }

    private JPanel buildStudentsPage() {
        JPanel panel = page(
            "Student Directory",
            "Find and review student information"
        );

        studentSearch = new JTextField(20);
        styleField(studentSearch);

        JButton search = button("Search Students", BLUE);
        JButton all = button("Show All", NAVY_LIGHT);
        JButton addStudent = button("Add Student", GREEN);
        JButton editStudent = button("Edit Selected", PURPLE);
        JButton deleteStudent = button("Delete Selected", new Color(190, 50, 50));

        JPanel tools = toolbar();
        tools.add(new JLabel("Roll number or student name"));
        tools.add(studentSearch);
        tools.add(search);
        tools.add(all);
        tools.add(addStudent);
        tools.add(editStudent);
        tools.add(deleteStudent);

        studentTable = table(new String[]{
            "Student ID", "Roll Number", "Student Name",
            "Department", "Semester"
        });

        search.addActionListener(e ->
            loadStudents(studentSearch.getText().trim()));
        studentSearch.addActionListener(e ->
            loadStudents(studentSearch.getText().trim()));
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

        styleField(rollField);
        styleField(nameField);
        styleField(departmentField);
        styleField(semesterField);

        JPanel form = new JPanel(new GridLayout(0, 2, 10, 10));
        form.setBorder(new EmptyBorder(12, 12, 12, 12));
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
            JOptionPane.showMessageDialog(this, "Please select a student row first.",
                "No Student Selected", JOptionPane.WARNING_MESSAGE);
            return null;
        }

        int row = studentTable.convertRowIndexToModel(viewRow);
        DefaultTableModel model = (DefaultTableModel) studentTable.getModel();
        return new Object[] {
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
                + "Keep the academic history, or remove related records only if you are certain.",
                "Student Has Linked Records", JOptionPane.WARNING_MESSAGE);
        } catch (SQLException ex) {
            databaseError(ex);
        }
    }

    private JPanel buildCoursesPage() {
        JPanel panel = page(
            "Course Directory",
            "Registered courses and credit information"
        );

        JButton refresh = button("Refresh Courses", BLUE);
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
            "Assessment-level performance for an individual student"
        );

        reportRoll = new JTextField(15);
        styleField(reportRoll);

        JButton generate = button("Generate Report", BLUE);

        JPanel tools = toolbar();
        tools.add(new JLabel("Student roll number"));
        tools.add(reportRoll);
        tools.add(generate);

        reportTable = table(new String[]{
            "Course Code", "Course Name", "Assessment",
            "Marks Obtained", "Maximum Marks"
        });

        reportSummary = new JTextArea(5, 30);
        reportSummary.setEditable(false);
        reportSummary.setFont(new Font("Monospaced", Font.PLAIN, 13));
        reportSummary.setForeground(DARK);
        reportSummary.setBackground(WHITE);
        reportSummary.setBorder(new EmptyBorder(12, 12, 12, 12));
        reportSummary.setLineWrap(true);
        reportSummary.setWrapStyleWord(true);

        generate.addActionListener(e -> generateReport());
        reportRoll.addActionListener(e -> generateReport());

        JPanel reportBody = new JPanel(new BorderLayout(0, 15));
        reportBody.setOpaque(false);
        reportBody.add(new JScrollPane(reportTable), BorderLayout.CENTER);

        JScrollPane summaryScroll = new JScrollPane(reportSummary);
        summaryScroll.setPreferredSize(new Dimension(100, 125));
        reportBody.add(summaryScroll, BorderLayout.SOUTH);

        JPanel combined = new JPanel(new BorderLayout(0, 15));
        combined.setOpaque(false);
        combined.add(tools, BorderLayout.NORTH);
        combined.add(reportBody, BorderLayout.CENTER);

        panel.add(combined, BorderLayout.CENTER);
        return panel;
    }

    private JPanel toolbar() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 9, 10));
        panel.setBackground(WHITE);
        panel.setBorder(new CompoundBorder(
            new LineBorder(BORDER, 1),
            new EmptyBorder(3, 7, 3, 7)
        ));
        return panel;
    }

    private JPanel tablePanel(JPanel toolbar, JTable table) {
        JPanel panel = new JPanel(new BorderLayout(0, 15));
        panel.setOpaque(false);

        JPanel holder = new JPanel(new BorderLayout());
        holder.setBackground(WHITE);
        holder.setBorder(new LineBorder(BORDER, 1));
        holder.add(new JScrollPane(table), BorderLayout.CENTER);

        panel.add(toolbar, BorderLayout.NORTH);
        panel.add(holder, BorderLayout.CENTER);
        return panel;
    }

    private JButton button(String text, Color color) {
        JButton button = new JButton(text);
        button.setFont(new Font("SansSerif", Font.BOLD, 12));
        button.setForeground(WHITE);
        button.setBackground(color);
        button.setFocusPainted(false);
        button.setOpaque(true);
        button.setBorderPainted(false);
        button.setBorder(new EmptyBorder(10, 15, 10, 15));
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return button;
    }

    private void styleField(JTextField field) {
        field.setFont(new Font("SansSerif", Font.PLAIN, 13));
        field.setForeground(DARK);
        field.setBackground(WHITE);
        field.setCaretColor(DARK);
        field.setBorder(new CompoundBorder(
            new LineBorder(new Color(195, 207, 224), 1),
            new EmptyBorder(8, 9, 8, 9)
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
        table.setFont(new Font("SansSerif", Font.PLAIN, 13));
        table.setForeground(DARK);
        table.setBackground(WHITE);
        table.setSelectionForeground(DARK);
        table.setSelectionBackground(new Color(222, 235, 255));
        table.setRowHeight(32);
        table.setGridColor(new Color(233, 237, 244));
        table.setShowVerticalLines(false);
        table.setFillsViewportHeight(true);
        table.setAutoCreateRowSorter(true);

        JTableHeader header = table.getTableHeader();
        header.setFont(new Font("SansSerif", Font.BOLD, 12));
        header.setForeground(NAVY);
        header.setBackground(new Color(232, 239, 249));
        header.setPreferredSize(new Dimension(100, 38));
        header.setReorderingAllowed(false);

        return table;
    }

    private void loadStudents(String searchText) {
        if (studentTable == null) return;

        DefaultTableModel model =
            (DefaultTableModel) studentTable.getModel();
        model.setRowCount(0);

        String sql =
            "SELECT student_id, roll_no, student_name, department, semester " +
            "FROM students WHERE roll_no LIKE ? OR student_name LIKE ? " +
            "ORDER BY roll_no";

        try (
            Connection con = connect();
            PreparedStatement ps = con.prepareStatement(sql)
        ) {
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

        DefaultTableModel model =
            (DefaultTableModel) courseTable.getModel();
        model.setRowCount(0);

        String sql =
            "SELECT course_id, course_code, course_name, credits " +
            "FROM courses ORDER BY course_code";

        try (
            Connection con = connect();
            Statement st = con.createStatement();
            ResultSet rs = st.executeQuery(sql)
        ) {
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
            studentCount.setText(
                String.valueOf(count(con, "students")));
            courseCount.setText(
                String.valueOf(count(con, "courses")));
            assessmentCount.setText(
                String.valueOf(count(con, "assessment_marks")));

            setStatus("Connected to kec_academic");

        } catch (SQLException e) {
            studentCount.setText("--");
            courseCount.setText("--");
            assessmentCount.setText("--");
            databaseError(e);
        }
    }

    private int count(Connection con, String table)
        throws SQLException {
        String sql = "SELECT COUNT(*) FROM " + table;

        try (
            Statement st = con.createStatement();
            ResultSet rs = st.executeQuery(sql)
        ) {
            rs.next();
            return rs.getInt(1);
        }
    }

   
private void generateReport() {
    String roll = reportRoll.getText().trim();

    if (roll.isEmpty()) {
        JOptionPane.showMessageDialog(
            this,
            "Please enter a student roll number.",
            "Required Field",
            JOptionPane.WARNING_MESSAGE
        );
        return;
    }

    DefaultTableModel model =
        (DefaultTableModel) reportTable.getModel();

    model.setRowCount(0);
    reportSummary.setText("");

    String sql =
        "SELECT s.student_name, c.course_code, c.course_name, " +
        "am.assessment_name, am.max_marks, am.marks_obtained " +
        "FROM students s " +
        "JOIN enrollments e ON s.student_id = e.student_id " +
        "JOIN courses c ON e.course_id = c.course_id " +
        "JOIN assessment_marks am " +
        "ON e.enrollment_id = am.enrollment_id " +
        "WHERE s.roll_no = ? " +
        "ORDER BY c.course_code, am.assessment_name";

    String studentName = null;

    class CourseEvaluation {
        double cia1 = 0, cia2 = 0, cia3 = 0, ese = 0;
        int ciaCount = 0;
        boolean hasCiaOrEse = false;
        double totalObtained = 0;
        double totalMax = 0;
    }

    java.util.Map<String, CourseEvaluation> courseMarks =
        new java.util.LinkedHashMap<>();

    try (
        Connection con = connect();
        PreparedStatement ps = con.prepareStatement(sql)
    ) {
        ps.setString(1, roll);

        try (ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                studentName = rs.getString("student_name");

                String courseCode = rs.getString("course_code");
                String courseName = rs.getString("course_name");
                String assessment =
                    rs.getString("assessment_name");

                double marks = rs.getDouble("marks_obtained");
                double max = rs.getDouble("max_marks");

                model.addRow(new Object[]{
                    courseCode,
                    courseName,
                    assessment,
                    marks,
                    max
                });

                String key = courseCode + " - " + courseName;

                CourseEvaluation eval = courseMarks.computeIfAbsent(
                    key, k -> new CourseEvaluation()
                );
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
            reportSummary.setText(
                "No assessment records found for " + roll + "."
            );
            setStatus("No academic assessment records found");
            return;
        }

        StringBuilder summary = new StringBuilder();

        summary.append("Student: ")
            .append(studentName).append("\n");
        summary.append("Roll Number: ")
            .append(roll).append("\n\n");

        double overallTotal = 0;
        double overallMaximum = 0;
        int courseCount = 0;

        for (java.util.Map.Entry<String, CourseEvaluation> entry
                : courseMarks.entrySet()) {

            CourseEvaluation eval = entry.getValue();
            summary.append(entry.getKey()).append("\n");

            if (eval.hasCiaOrEse) {
                double internal = ((eval.cia1 + eval.cia2 + eval.cia3) / 3.0) * 0.40;
                double total = internal + eval.ese;

                summary.append(String.format("Internal: %.2f / 40%n", internal));
                summary.append(String.format("ESE: %.2f / 60%n", eval.ese));
                summary.append(String.format("Total: %.2f / 100%n", total));

                if (eval.ese >= 27 && total >= 50) {
                    summary.append("Result: PASS\n\n");
                } else {
                    summary.append("Result: RA / FAIL\n\n");
                }

                overallTotal += total;
                overallMaximum += 100;
            } else if (eval.totalMax > 0) {
                double pct = (eval.totalObtained / eval.totalMax) * 100.0;
                summary.append(String.format("Marks Obtained: %.2f / %.2f (%.2f%%)%n",
                    eval.totalObtained, eval.totalMax, pct));
                if (pct >= 50) {
                    summary.append("Result: PASS\n\n");
                } else {
                    summary.append("Result: RA / FAIL\n\n");
                }

                overallTotal += pct;
                overallMaximum += 100;
            }

            courseCount++;
        }

        if (courseCount > 0) {
            double percentage =
                overallTotal / overallMaximum * 100.0;

            summary.append("------------------------------\n");
            summary.append(String.format(
                "Courses with marks: %d%n", courseCount
            ));
            summary.append(String.format(
                "Average Total: %.2f / 100%n", percentage
            ));
            summary.append(
                "Note: Courses with missing marks may be incomplete."
            );
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

        JOptionPane.showMessageDialog(
            this,
            "Could not access the academic database.\n\n" + e.getMessage(),
            "Database Error",
            JOptionPane.ERROR_MESSAGE
        );
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(
                    UIManager.getSystemLookAndFeelClassName()
                );
            } catch (Exception ignored) {
                // Keep default Swing look and feel if unavailable.
            }

            new KECGui();
        });
    }
}
