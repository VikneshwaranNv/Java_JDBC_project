import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.border.MatteBorder;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * KEC ACADEMIX — Academic Management & Continuous Evaluation System.
 * Main desktop application shell designed under the Kongu Engineering College (KEC) branding.
 */
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

    // Palette alias referencing official KECTheme
    public static final Color COLOR_NAVY = KECTheme.DEEP_NAVY;
    public static final Color COLOR_NAVY_HOVER = KECTheme.DEEP_NAVY_HOVER;
    public static final Color COLOR_MUTED_BLUE = KECTheme.KEC_CYAN;
    public static final Color COLOR_WHITE = KECTheme.WHITE;
    public static final Color COLOR_BG = KECTheme.BG_LIGHT;
    public static final Color COLOR_BORDER = KECTheme.BORDER_GREY;
    public static final Color COLOR_TEXT_DARK = KECTheme.TEXT_DARK;
    public static final Color COLOR_TEXT_MUTED = KECTheme.TEXT_MUTED;
    public static final Color COLOR_GREEN = KECTheme.KEC_GREEN;
    public static final Color COLOR_AMBER = KECTheme.WARNING_AMBER;
    public static final Color COLOR_RED = KECTheme.ERROR_RED;

    // Typography
    public static final Font FONT_TITLE = KECTheme.FONT_TITLE;
    public static final Font FONT_SUBTITLE = new Font("Segoe UI", Font.PLAIN, 13);
    public static final Font FONT_SECTION = KECTheme.FONT_SECTION;
    public static final Font FONT_BOLD = KECTheme.FONT_BOLD;
    public static final Font FONT_REGULAR = KECTheme.FONT_REGULAR;
    public static final Font FONT_SMALL = KECTheme.FONT_SMALL;
    public static final Font FONT_MONO = KECTheme.FONT_MONO;

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
    private JTextArea reportSummary = new JTextArea();

    // Tab 1: Marksheet fields
    private JComboBox<String> marksheetDeptCombo;
    private JComboBox<String> marksheetSemCombo;
    private JComboBox<String> marksheetYearCombo;
    private JLabel marksheetHeaderLabel;
    private JLabel marksheetCreditsRegisteredLabel;
    private JLabel marksheetCreditsEarnedLabel;
    private JLabel marksheetSgpaLabel;
    private JLabel marksheetPercentageLabel;
    private JLabel marksheetStatusBadge;

    // Tab 2: Attendance Defaulters fields
    private JComboBox<String> defaultersDeptCombo;
    private JComboBox<String> defaultersSemCombo;
    private JComboBox<String> defaultersYearCombo;
    private JComboBox<String> defaultersStatusCombo;
    private JTable defaultersTable;
    private JLabel defaultersSummaryLabel;

    // Tab 3: Department Analytics fields
    private JComboBox<String> analyticsDeptCombo;
    private JComboBox<String> analyticsSemCombo;
    private JComboBox<String> analyticsYearCombo;
    private JTable analyticsCourseTable;
    private JLabel analyticsEnrolledLabel;
    private JLabel analyticsPassedLabel;
    private JLabel analyticsFailedLabel;
    private JLabel analyticsSaAbLabel;
    private JLabel analyticsIncompleteLabel;
    private JLabel analyticsPassRateLabel;
    private JLabel analyticsAvgSgpaLabel;

    private JLabel statusLabel;
    private final UserSession currentSession;

    private final List<JButton> navButtons = new ArrayList<>();
    private String activePage = "Dashboard";

    public KECGui() {
        this(null);
    }

    public KECGui(UserSession session) {
        this.currentSession = session;
        setTitle("KEC ACADEMIX | Student Evaluation & Academic Performance System");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1380, 860);
        setMinimumSize(new Dimension(1100, 720));
        setLocationRelativeTo(null);

        getContentPane().setLayout(new BorderLayout());
        getContentPane().setBackground(COLOR_BG);

        buildSidebar();
        buildMainArea();

        refreshDashboard();
        loadStudents("");
        loadCourses();

        if (currentSession != null && currentSession.isStudent()) {
            if (reportRoll != null) {
                reportRoll.setText(currentSession.getStudentRoll());
                reportRoll.setEditable(false);
                reportRoll.setToolTipText("Locked to your registered student roll number.");
            }
        }

        setVisible(true);
    }

    private Connection connect() throws SQLException {
        return DriverManager.getConnection(getDbUrl(), getDbUser(), getDbPassword());
    }

    private void buildSidebar() {
        JPanel sidebar = new JPanel();
        sidebar.setPreferredSize(new Dimension(270, 0));
        sidebar.setBackground(KECTheme.DEEP_NAVY);
        sidebar.setBorder(new EmptyBorder(20, 16, 20, 16));
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));

        // Brand Emblem & Logo
        JPanel brand = new JPanel();
        brand.setOpaque(false);
        brand.setLayout(new BoxLayout(brand, BoxLayout.Y_AXIS));
        brand.setAlignmentX(Component.LEFT_ALIGNMENT);

        ImageIcon logoIcon = KECTheme.getKECLogo(210, 56);
        if (logoIcon != null) {
            JLabel logoLabel = new JLabel(logoIcon);
            logoLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
            brand.add(logoLabel);
            brand.add(Box.createVerticalStrut(10));
        }

        JLabel emblem = new JLabel("KEC ACADEMIX");
        emblem.setFont(new Font("Segoe UI", Font.BOLD, 20));
        emblem.setForeground(KECTheme.WHITE);
        emblem.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel brandSub = new JLabel("Autonomous • Anna University");
        brandSub.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        brandSub.setForeground(KECTheme.KEC_GREEN);
        brandSub.setAlignmentX(Component.LEFT_ALIGNMENT);

        brand.add(emblem);
        brand.add(Box.createVerticalStrut(2));
        brand.add(brandSub);

        sidebar.add(brand);
        sidebar.add(Box.createVerticalStrut(18));

        // User Profile & Role Badge Card
        JPanel userCard = new JPanel();
        userCard.setLayout(new BoxLayout(userCard, BoxLayout.Y_AXIS));
        userCard.setOpaque(true);
        userCard.setBackground(new Color(16, 38, 66));
        userCard.setBorder(new CompoundBorder(
                new LineBorder(new Color(42, 75, 117), 1, true),
                new EmptyBorder(10, 12, 10, 12)
        ));
        userCard.setAlignmentX(Component.LEFT_ALIGNMENT);
        userCard.setMaximumSize(new Dimension(245, 80));

        String username = (currentSession != null) ? currentSession.getUsername() : "Guest User";
        String role = (currentSession != null) ? currentSession.getRole() : "ANONYMOUS";

        JLabel userTitle = new JLabel(username);
        userTitle.setFont(FONT_BOLD);
        userTitle.setForeground(KECTheme.WHITE);

        JLabel roleBadge = new JLabel("  " + role + "  ");
        roleBadge.setFont(new Font("Segoe UI", Font.BOLD, 10));
        roleBadge.setOpaque(true);
        if ("ADMINISTRATOR".equals(role)) {
            roleBadge.setBackground(KECTheme.KEC_CYAN);
            roleBadge.setForeground(KECTheme.WHITE);
        } else if ("STAFF".equals(role)) {
            roleBadge.setBackground(KECTheme.KEC_GREEN);
            roleBadge.setForeground(KECTheme.WHITE);
        } else if ("STUDENT".equals(role)) {
            roleBadge.setBackground(new Color(53, 92, 138));
            roleBadge.setForeground(KECTheme.WHITE);
        } else {
            roleBadge.setBackground(COLOR_NAVY_HOVER);
            roleBadge.setForeground(KECTheme.WHITE);
        }
        roleBadge.setBorder(new EmptyBorder(2, 6, 2, 6));

        userCard.add(userTitle);
        userCard.add(Box.createVerticalStrut(4));
        userCard.add(roleBadge);

        if (currentSession != null && currentSession.isStudent() && currentSession.getStudentRoll() != null) {
            JLabel rollLabel = new JLabel("Roll: " + currentSession.getStudentRoll());
            rollLabel.setFont(FONT_SMALL);
            rollLabel.setForeground(new Color(190, 207, 235));
            userCard.add(Box.createVerticalStrut(2));
            userCard.add(rollLabel);
        }

        sidebar.add(userCard);
        sidebar.add(Box.createVerticalStrut(18));

        JLabel navLabel = new JLabel("MAIN NAVIGATION");
        navLabel.setFont(new Font("Segoe UI", Font.BOLD, 11));
        navLabel.setForeground(new Color(144, 166, 203));
        navLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        sidebar.add(navLabel);
        sidebar.add(Box.createVerticalStrut(10));

        addNav(sidebar, "Dashboard", "Dashboard");
        addNav(sidebar, "Student Directory", "Students");
        addNav(sidebar, "Course Directory", "Courses");
        addNav(sidebar, "Academic Reports", "Reports");
        addNav(sidebar, "Marks Management", "Marks");
        addNav(sidebar, "SAP Management", "SAP");
        if (currentSession != null && currentSession.isAdministrator()) {
            addNav(sidebar, "User Accounts", "Users");
        }

        sidebar.add(Box.createVerticalGlue());

        JButton logoutBtn = KECTheme.createDangerButton("Sign Out");
        logoutBtn.setAlignmentX(Component.LEFT_ALIGNMENT);
        logoutBtn.setMaximumSize(new Dimension(245, 40));
        logoutBtn.addActionListener(e -> logout());
        sidebar.add(logoutBtn);
        sidebar.add(Box.createVerticalStrut(12));

        JSeparator separator = new JSeparator();
        separator.setForeground(new Color(42, 75, 117));
        separator.setBackground(new Color(42, 75, 117));
        separator.setMaximumSize(new Dimension(240, 1));
        sidebar.add(separator);
        sidebar.add(Box.createVerticalStrut(10));

        JLabel footer = new JLabel("KONGU ENGINEERING COLLEGE");
        footer.setFont(new Font("Segoe UI", Font.BOLD, 10));
        footer.setForeground(new Color(174, 193, 222));
        footer.setAlignmentX(Component.LEFT_ALIGNMENT);
        sidebar.add(footer);

        JLabel version = new JLabel("Academic Management System  |  v2.0");
        version.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        version.setForeground(new Color(135, 158, 196));
        version.setAlignmentX(Component.LEFT_ALIGNMENT);
        sidebar.add(Box.createVerticalStrut(2));
        sidebar.add(version);

        getContentPane().add(sidebar, BorderLayout.WEST);
    }

    private void addNav(JPanel sidebar, String label, String page) {
        JButton button = new JButton(label);
        button.setFont(new Font("Segoe UI", Font.BOLD, 13));
        button.setHorizontalAlignment(SwingConstants.LEFT);
        button.setMaximumSize(new Dimension(245, 42));
        button.setPreferredSize(new Dimension(245, 42));
        button.setFocusPainted(false);
        button.setOpaque(true);
        button.setBorderPainted(false);
        button.setAlignmentX(Component.LEFT_ALIGNMENT);
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        button.putClientProperty("PAGE", page);

        applyNavButtonStyle(button, page.equals(activePage));
        navButtons.add(button);

        button.addActionListener(e -> switchPage(page));

        button.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent e) {
                if (!page.equals(activePage)) {
                    button.setBackground(new Color(28, 62, 105));
                }
            }

            @Override
            public void mouseExited(java.awt.event.MouseEvent e) {
                if (!page.equals(activePage)) {
                    button.setBackground(KECTheme.DEEP_NAVY);
                }
            }
        });

        button.addFocusListener(new java.awt.event.FocusAdapter() {
            @Override
            public void focusGained(java.awt.event.FocusEvent e) {
                if (!page.equals(activePage)) {
                    button.setBackground(new Color(28, 62, 105));
                }
            }

            @Override
            public void focusLost(java.awt.event.FocusEvent e) {
                if (!page.equals(activePage)) {
                    button.setBackground(KECTheme.DEEP_NAVY);
                }
            }
        });

        sidebar.add(button);
        sidebar.add(Box.createVerticalStrut(4));
    }

    public void switchPage(String page) {
        if ("Marks".equals(page)) {
            try {
                new MarksManagement(currentSession);
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
                new SAPManagement(currentSession);
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

        if ("Users".equals(page)) {
            try {
                new UserManagementDialog(this, currentSession).setVisible(true);
            } catch (Exception ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(
                        this,
                        "Unable to open User Management:\n" + ex.getMessage(),
                        "Error",
                        JOptionPane.ERROR_MESSAGE
                );
            }
            return;
        }

        activePage = page;
        for (JButton b : navButtons) {
            applyNavButtonStyle(b, page.equals(b.getClientProperty("PAGE")));
        }

        layout.show(pages, page);

        if ("Dashboard".equals(page)) {
            refreshDashboard();
        } else if ("Students".equals(page)) {
            loadStudents(studentSearch.getText().trim());
        } else if ("Courses".equals(page)) {
            loadCourses();
        }
    }

    private void applyNavButtonStyle(JButton button, boolean active) {
        if (active) {
            button.setBackground(new Color(32, 72, 122));
            button.setForeground(KECTheme.WHITE);
            button.setBorder(new CompoundBorder(
                    new MatteBorder(0, 4, 0, 0, KECTheme.KEC_GREEN),
                    new EmptyBorder(0, 14, 0, 10)
            ));
        } else {
            button.setBackground(KECTheme.DEEP_NAVY);
            button.setForeground(new Color(215, 228, 245));
            button.setBorder(new EmptyBorder(0, 18, 0, 10));
        }
    }

    private void logout() {
        int confirm = JOptionPane.showConfirmDialog(
                this,
                "Are you sure you want to sign out of KEC ACADEMIX?",
                "Confirm Sign Out",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE
        );
        if (confirm == JOptionPane.YES_OPTION) {
            if (currentSession != null) {
                currentSession.invalidate();
            }
            dispose();
            showLogin();
        }
    }

    private void buildMainArea() {
        JPanel main = new JPanel(new BorderLayout());
        main.setBackground(COLOR_BG);

        // Header Banner with KEC branding and status
        JPanel userBadgePanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        userBadgePanel.setOpaque(false);

        String role = (currentSession != null) ? currentSession.getRole() : "GUEST";
        JLabel badge = new JLabel("  " + role + " PORTAL  ");
        badge.setOpaque(true);
        badge.setBackground(KECTheme.KEC_CYAN_LIGHT);
        badge.setForeground(KECTheme.KEC_CYAN.darker());
        badge.setFont(new Font("Segoe UI", Font.BOLD, 12));
        badge.setBorder(new EmptyBorder(8, 14, 8, 14));
        userBadgePanel.add(badge);

        JPanel header = KECTheme.createHeaderBanner(
                "Student Evaluation & Continuous Academic Performance Portal",
                "Kongu Engineering College (Autonomous) • Perundurai, Erode",
                userBadgePanel
        );

        pages.setBackground(COLOR_BG);
        pages.add(buildDashboard(), "Dashboard");
        pages.add(buildStudentsPage(), "Students");
        pages.add(buildCoursesPage(), "Courses");
        pages.add(buildReportsPage(), "Reports");

        statusLabel = new JLabel("  Ready  |  Database: kec_academic  |  Kongu Engineering College");
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
        JPanel panel = new JPanel(new BorderLayout(0, 16));
        panel.setBackground(COLOR_BG);
        panel.setBorder(new EmptyBorder(20, 24, 18, 24));

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
        stats.add(statCard("REGISTERED STUDENTS", studentCount, KECTheme.KEC_CYAN, "Student Directory", () -> switchPage("Students")));
        stats.add(statCard("ACTIVE COURSES", courseCount, KECTheme.KEC_GREEN, "Course Curriculum", () -> switchPage("Courses")));
        stats.add(statCard("ASSESSMENT RECORDS", assessmentCount, KECTheme.DEEP_NAVY, "Marks Database", () -> switchPage("Marks")));
        stats.add(statCard("PENDING SAP REVIEWS", pendingSapCount, KECTheme.WARNING_AMBER, "Awaiting Staff Review", () -> switchPage("SAP")));

        JPanel overview = new JPanel(new BorderLayout(0, 16));
        overview.setBackground(COLOR_WHITE);
        overview.setBorder(new CompoundBorder(
                new LineBorder(COLOR_BORDER, 1, true),
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

    private JPanel statCard(String title, JLabel value, Color accent, String description, Runnable onClick) {
        JPanel card = KECTheme.createInteractiveCard(new EmptyBorder(16, 18, 16, 18), onClick);

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
        marker.setForeground(KECTheme.KEC_CYAN);
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
        KECTheme.styleTextField(studentSearch, 38);

        JButton search = KECTheme.createPrimaryButton("Search Students");
        JButton all = KECTheme.createSecondaryButton("Show All");
        JButton addStudent = KECTheme.createAccentButton("+ Add Student");
        JButton editStudent = KECTheme.createPrimaryButton("Edit Selected");
        JButton deleteStudent = KECTheme.createDangerButton("Delete Selected");

        // Two-row structured toolbar that NEVER clips or overflows
        JPanel tools = new JPanel();
        tools.setLayout(new BoxLayout(tools, BoxLayout.Y_AXIS));
        tools.setBackground(COLOR_WHITE);
        tools.setBorder(new CompoundBorder(
                new LineBorder(COLOR_BORDER, 1, true),
                new EmptyBorder(10, 14, 10, 14)
        ));

        // Row 1: Search controls
        JPanel searchRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        searchRow.setOpaque(false);
        JLabel searchLbl = new JLabel("Search by Roll or Name:");
        searchLbl.setFont(FONT_BOLD);
        searchLbl.setForeground(COLOR_TEXT_DARK);
        searchRow.add(searchLbl);
        searchRow.add(studentSearch);
        searchRow.add(search);
        searchRow.add(all);

        // Row 2: Management action buttons
        JPanel actionRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        actionRow.setOpaque(false);
        JLabel actionLbl = new JLabel("Actions: ");
        actionLbl.setFont(FONT_BOLD);
        actionLbl.setForeground(COLOR_TEXT_MUTED);
        actionRow.add(actionLbl);
        actionRow.add(addStudent);
        actionRow.add(editStudent);
        actionRow.add(deleteStudent);

        tools.add(searchRow);
        tools.add(Box.createVerticalStrut(10));
        tools.add(actionRow);

        studentTable = table(new String[]{
                "Student ID", "Roll Number", "Student Name", "Department", "Semester"
        });
        int[] studentWidths = {90, 140, 260, 180, 100};
        for (int i = 0; i < studentWidths.length && i < studentTable.getColumnCount(); i++) {
            studentTable.getColumnModel().getColumn(i).setPreferredWidth(studentWidths[i]);
        }

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

        KECTheme.styleTextField(rollField, 36);
        KECTheme.styleTextField(nameField, 36);
        KECTheme.styleTextField(departmentField, 36);
        KECTheme.styleTextField(semesterField, 36);

        JPanel form = new JPanel(new GridLayout(0, 2, 10, 10));
        form.setBorder(new EmptyBorder(14, 14, 14, 14));
        form.add(new JLabel("Roll Number (e.g. 25ITR001 / 25ITL123):"));
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
                "Registered curriculum courses, department offerings, course types, and credit allocations"
        );

        JButton addCourseBtn = KECTheme.createPrimaryButton("+ Add Course");
        JButton editCourseBtn = KECTheme.createSecondaryButton("Edit Course");
        JButton deleteCourseBtn = KECTheme.createDangerButton("Delete Course");
        JButton enrollmentsBtn = KECTheme.createAccentButton("Course Enrollments");
        JButton refreshBtn = KECTheme.createSecondaryButton("Refresh Courses");

        JPanel tools = toolbar();
        boolean canManage = currentSession == null || !currentSession.isStudent();
        if (canManage) {
            tools.add(addCourseBtn);
            tools.add(editCourseBtn);
            tools.add(deleteCourseBtn);
            tools.add(enrollmentsBtn);
        } else {
            enrollmentsBtn.setText("My Course Enrollments");
            tools.add(enrollmentsBtn);
        }
        tools.add(refreshBtn);

        courseTable = table(new String[]{
                "Course ID", "Course Code", "Course Name", "Course Type", "Credits"
        });
        int[] courseWidths = {80, 130, 320, 120, 80};
        for (int i = 0; i < courseWidths.length && i < courseTable.getColumnCount(); i++) {
            courseTable.getColumnModel().getColumn(i).setPreferredWidth(courseWidths[i]);
        }

        addCourseBtn.addActionListener(e -> showCourseDialog(null));
        editCourseBtn.addActionListener(e -> editSelectedCourse());
        deleteCourseBtn.addActionListener(e -> deleteSelectedCourse());
        enrollmentsBtn.addActionListener(e -> showEnrollmentsDialog());
        refreshBtn.addActionListener(e -> loadCourses());

        panel.add(tablePanel(tools, courseTable), BorderLayout.CENTER);
        return panel;
    }

    private JPanel buildReportsPage() {
        JPanel panel = page(
                "Academic Reports Center",
                "Official semester marksheets, attendance defaulter tracking, and department academic performance analytics"
        );

        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.setFont(FONT_BOLD);
        tabbedPane.setBackground(COLOR_WHITE);
        tabbedPane.setForeground(COLOR_NAVY);

        tabbedPane.addTab("Semester Marksheet", buildMarksheetTab());
        tabbedPane.addTab("Attendance Defaulters", buildAttendanceDefaultersTab());
        tabbedPane.addTab("Department Analytics", buildDepartmentAnalyticsTab());

        panel.add(tabbedPane, BorderLayout.CENTER);
        return panel;
    }

    private JPanel buildMarksheetTab() {
        JPanel tab = new JPanel(new BorderLayout(0, 10));
        tab.setOpaque(false);
        tab.setBorder(new EmptyBorder(10, 10, 10, 10));

        JPanel filterBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 8));
        filterBar.setBackground(COLOR_WHITE);
        filterBar.setBorder(new CompoundBorder(
                new LineBorder(COLOR_BORDER, 1, true),
                new EmptyBorder(4, 10, 4, 10)
        ));

        reportRoll = new JTextField(12);
        KECTheme.styleTextField(reportRoll, 34);
        if (currentSession != null && currentSession.isStudent()) {
            reportRoll.setText(currentSession.getStudentRoll());
            reportRoll.setEditable(false);
            reportRoll.setToolTipText("Locked to your student roll number");
        }

        marksheetDeptCombo = new JComboBox<>();
        marksheetSemCombo = new JComboBox<>(new String[]{
                "All Semesters", "Semester 1", "Semester 2", "Semester 3", "Semester 4",
                "Semester 5", "Semester 6", "Semester 7", "Semester 8"
        });
        marksheetYearCombo = new JComboBox<>();

        KECTheme.styleComboBox(marksheetDeptCombo, 34);
        KECTheme.styleComboBox(marksheetSemCombo, 34);
        KECTheme.styleComboBox(marksheetYearCombo, 34);

        populateFilterCombos(marksheetDeptCombo, marksheetYearCombo);

        JButton generateBtn = KECTheme.createPrimaryButton("Generate Marksheet");
        JButton exportPdfBtn = KECTheme.createAccentButton("Export Official PDF");

        JLabel rollLbl = new JLabel("Roll Number:");
        rollLbl.setFont(FONT_BOLD);
        rollLbl.setForeground(COLOR_TEXT_DARK);

        filterBar.add(rollLbl);
        filterBar.add(reportRoll);
        filterBar.add(new JLabel("  Dept:"));
        filterBar.add(marksheetDeptCombo);
        filterBar.add(new JLabel("  Semester:"));
        filterBar.add(marksheetSemCombo);
        filterBar.add(new JLabel("  Academic Year:"));
        filterBar.add(marksheetYearCombo);
        filterBar.add(generateBtn);
        filterBar.add(exportPdfBtn);

        marksheetHeaderLabel = new JLabel("Please enter a student roll number and click 'Generate Marksheet'.");
        marksheetHeaderLabel.setFont(FONT_BOLD);
        marksheetHeaderLabel.setForeground(COLOR_NAVY);
        JPanel headerCard = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 8));
        headerCard.setBackground(new Color(230, 243, 248));
        headerCard.setBorder(new LineBorder(COLOR_BORDER, 1, true));
        headerCard.add(marksheetHeaderLabel);

        reportTable = table(new String[]{
                "Roll Number", "Student Name", "Course Code", "Course Name", "Credits",
                "Internal Marks", "ESE Marks", "Total", "Grade", "Grade Point", "Result", "Attendance"
        });
        int[] widths = {85, 130, 85, 200, 55, 95, 85, 65, 55, 75, 110, 85};
        for (int i = 0; i < widths.length && i < reportTable.getColumnCount(); i++) {
            reportTable.getColumnModel().getColumn(i).setPreferredWidth(widths[i]);
        }

        JPanel tableCard = new JPanel(new BorderLayout());
        tableCard.setBackground(COLOR_WHITE);
        tableCard.setBorder(new LineBorder(COLOR_BORDER, 1, true));
        tableCard.add(new JScrollPane(reportTable), BorderLayout.CENTER);

        JPanel summaryBar = new JPanel(new GridLayout(1, 5, 10, 0));
        summaryBar.setOpaque(false);
        summaryBar.setPreferredSize(new Dimension(100, 72));

        marksheetCreditsRegisteredLabel = new JLabel("0", SwingConstants.CENTER);
        marksheetCreditsEarnedLabel = new JLabel("0", SwingConstants.CENTER);
        marksheetSgpaLabel = new JLabel("--", SwingConstants.CENTER);
        marksheetPercentageLabel = new JLabel("--", SwingConstants.CENTER);
        marksheetStatusBadge = new JLabel("NO RECORD", SwingConstants.CENTER);

        summaryBar.add(createMiniCard("CREDITS REGISTERED", marksheetCreditsRegisteredLabel, COLOR_NAVY));
        summaryBar.add(createMiniCard("CREDITS EARNED", marksheetCreditsEarnedLabel, COLOR_GREEN));
        summaryBar.add(createMiniCard("SEMESTER SGPA", marksheetSgpaLabel, COLOR_MUTED_BLUE));
        summaryBar.add(createMiniCard("EQUIVALENT %", marksheetPercentageLabel, COLOR_NAVY));
        summaryBar.add(createMiniCard("ACADEMIC STATUS", marksheetStatusBadge, COLOR_NAVY));

        JPanel centerContainer = new JPanel(new BorderLayout(0, 8));
        centerContainer.setOpaque(false);
        centerContainer.add(headerCard, BorderLayout.NORTH);
        centerContainer.add(tableCard, BorderLayout.CENTER);
        centerContainer.add(summaryBar, BorderLayout.SOUTH);

        tab.add(filterBar, BorderLayout.NORTH);
        tab.add(centerContainer, BorderLayout.CENTER);

        generateBtn.addActionListener(e -> generateMarksheet());
        reportRoll.addActionListener(e -> generateMarksheet());
        exportPdfBtn.addActionListener(e -> exportMarksheetPdf());

        return tab;
    }

    private JPanel buildAttendanceDefaultersTab() {
        JPanel tab = new JPanel(new BorderLayout(0, 10));
        tab.setOpaque(false);
        tab.setBorder(new EmptyBorder(10, 10, 10, 10));

        JPanel filterBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 8));
        filterBar.setBackground(COLOR_WHITE);
        filterBar.setBorder(new CompoundBorder(
                new LineBorder(COLOR_BORDER, 1, true),
                new EmptyBorder(4, 10, 4, 10)
        ));

        defaultersDeptCombo = new JComboBox<>();
        defaultersSemCombo = new JComboBox<>(new String[]{
                "All Semesters", "Semester 1", "Semester 2", "Semester 3", "Semester 4",
                "Semester 5", "Semester 6", "Semester 7", "Semester 8"
        });
        defaultersYearCombo = new JComboBox<>();
        defaultersStatusCombo = new JComboBox<>(new String[]{
                "All Students",
                "All Defaulters (< 75%)",
                "Critical Shortage (< 65% SA)",
                "Condonation Required (65%–74.9%)",
                "Eligible (>= 75%)"
        });

        KECTheme.styleComboBox(defaultersDeptCombo, 34);
        KECTheme.styleComboBox(defaultersSemCombo, 34);
        KECTheme.styleComboBox(defaultersYearCombo, 34);
        KECTheme.styleComboBox(defaultersStatusCombo, 34);

        populateFilterCombos(defaultersDeptCombo, defaultersYearCombo);

        JButton refreshBtn = KECTheme.createSecondaryButton("Refresh Report");
        JButton approveCondonationBtn = KECTheme.createSuccessButton("Approve Medical Condonation");
        if (currentSession != null && currentSession.isStudent()) {
            approveCondonationBtn.setVisible(false);
        }

        filterBar.add(new JLabel("Dept:"));
        filterBar.add(defaultersDeptCombo);
        filterBar.add(new JLabel("Semester:"));
        filterBar.add(defaultersSemCombo);
        filterBar.add(new JLabel("Year:"));
        filterBar.add(defaultersYearCombo);
        filterBar.add(new JLabel("Category:"));
        filterBar.add(defaultersStatusCombo);
        filterBar.add(refreshBtn);
        filterBar.add(approveCondonationBtn);

        defaultersTable = table(new String[]{
                "Attendance ID", "Roll Number", "Student Name", "Department", "Semester",
                "Course Code", "Course Name", "Classes Held", "Classes Attended", "Attendance %", "Eligibility Status"
        });
        int[] widths = {80, 95, 140, 95, 65, 85, 190, 80, 80, 90, 160};
        for (int i = 0; i < widths.length && i < defaultersTable.getColumnCount(); i++) {
            defaultersTable.getColumnModel().getColumn(i).setPreferredWidth(widths[i]);
        }

        JPanel tableCard = new JPanel(new BorderLayout());
        tableCard.setBackground(COLOR_WHITE);
        tableCard.setBorder(new LineBorder(COLOR_BORDER, 1, true));
        tableCard.add(new JScrollPane(defaultersTable), BorderLayout.CENTER);

        defaultersSummaryLabel = new JLabel("  Total Records: 0  |  Eligible: 0  |  Condonation Required: 0  |  Critical SA (<65%): 0");
        defaultersSummaryLabel.setFont(FONT_BOLD);
        defaultersSummaryLabel.setForeground(COLOR_NAVY);
        JPanel bottomCard = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 8));
        bottomCard.setBackground(COLOR_WHITE);
        bottomCard.setBorder(new LineBorder(COLOR_BORDER, 1, true));
        bottomCard.add(defaultersSummaryLabel);

        tab.add(filterBar, BorderLayout.NORTH);
        tab.add(tableCard, BorderLayout.CENTER);
        tab.add(bottomCard, BorderLayout.SOUTH);

        refreshBtn.addActionListener(e -> loadAttendanceDefaulters());
        defaultersDeptCombo.addActionListener(e -> loadAttendanceDefaulters());
        defaultersSemCombo.addActionListener(e -> loadAttendanceDefaulters());
        defaultersYearCombo.addActionListener(e -> loadAttendanceDefaulters());
        defaultersStatusCombo.addActionListener(e -> loadAttendanceDefaulters());
        approveCondonationBtn.addActionListener(e -> approveSelectedCondonation());

        loadAttendanceDefaulters();
        return tab;
    }

    private JPanel buildDepartmentAnalyticsTab() {
        JPanel tab = new JPanel(new BorderLayout(0, 10));
        tab.setOpaque(false);
        tab.setBorder(new EmptyBorder(10, 10, 10, 10));

        JPanel filterBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 8));
        filterBar.setBackground(COLOR_WHITE);
        filterBar.setBorder(new CompoundBorder(
                new LineBorder(COLOR_BORDER, 1, true),
                new EmptyBorder(4, 10, 4, 10)
        ));

        analyticsDeptCombo = new JComboBox<>();
        analyticsSemCombo = new JComboBox<>(new String[]{
                "All Semesters", "Semester 1", "Semester 2", "Semester 3", "Semester 4",
                "Semester 5", "Semester 6", "Semester 7", "Semester 8"
        });
        analyticsYearCombo = new JComboBox<>();

        KECTheme.styleComboBox(analyticsDeptCombo, 34);
        KECTheme.styleComboBox(analyticsSemCombo, 34);
        KECTheme.styleComboBox(analyticsYearCombo, 34);

        populateFilterCombos(analyticsDeptCombo, analyticsYearCombo);

        JButton generateBtn = KECTheme.createPrimaryButton("Generate Analytics");

        filterBar.add(new JLabel("Department:"));
        filterBar.add(analyticsDeptCombo);
        filterBar.add(new JLabel("Semester:"));
        filterBar.add(analyticsSemCombo);
        filterBar.add(new JLabel("Academic Year:"));
        filterBar.add(analyticsYearCombo);
        filterBar.add(generateBtn);

        JPanel metricsPanel = new JPanel(new GridLayout(1, 7, 8, 0));
        metricsPanel.setOpaque(false);
        metricsPanel.setPreferredSize(new Dimension(100, 72));

        analyticsEnrolledLabel = new JLabel("0", SwingConstants.CENTER);
        analyticsPassedLabel = new JLabel("0", SwingConstants.CENTER);
        analyticsFailedLabel = new JLabel("0", SwingConstants.CENTER);
        analyticsSaAbLabel = new JLabel("0", SwingConstants.CENTER);
        analyticsIncompleteLabel = new JLabel("0", SwingConstants.CENTER);
        analyticsPassRateLabel = new JLabel("0.0%", SwingConstants.CENTER);
        analyticsAvgSgpaLabel = new JLabel("0.00", SwingConstants.CENTER);

        metricsPanel.add(createMiniCard("TOTAL ENROLLED", analyticsEnrolledLabel, COLOR_NAVY));
        metricsPanel.add(createMiniCard("PASSED STUDENTS", analyticsPassedLabel, COLOR_GREEN));
        metricsPanel.add(createMiniCard("FAILED / RA", analyticsFailedLabel, COLOR_RED));
        metricsPanel.add(createMiniCard("SA / AB BARRED", analyticsSaAbLabel, COLOR_AMBER));
        metricsPanel.add(createMiniCard("INCOMPLETE", analyticsIncompleteLabel, COLOR_TEXT_MUTED));
        metricsPanel.add(createMiniCard("PASS RATE", analyticsPassRateLabel, COLOR_GREEN));
        metricsPanel.add(createMiniCard("CLASS AVG SGPA", analyticsAvgSgpaLabel, COLOR_MUTED_BLUE));

        analyticsCourseTable = table(new String[]{
                "Course Code", "Course Name", "Course Type", "Credits",
                "Enrolled", "Passed", "Failed (RA)", "SA / AB", "Incomplete", "Pass Rate (%)"
        });
        int[] widths = {90, 240, 95, 65, 80, 75, 80, 80, 85, 95};
        for (int i = 0; i < widths.length && i < analyticsCourseTable.getColumnCount(); i++) {
            analyticsCourseTable.getColumnModel().getColumn(i).setPreferredWidth(widths[i]);
        }

        JPanel tableCard = new JPanel(new BorderLayout());
        tableCard.setBackground(COLOR_WHITE);
        tableCard.setBorder(new LineBorder(COLOR_BORDER, 1, true));
        tableCard.add(new JScrollPane(analyticsCourseTable), BorderLayout.CENTER);

        JLabel footnote = new JLabel("  * Note: Incomplete results are excluded from the Pass Rate and Class Average SGPA calculations to prevent skewing official performance metrics.");
        footnote.setFont(FONT_SMALL);
        footnote.setForeground(COLOR_TEXT_MUTED);

        JPanel centerPanel = new JPanel(new BorderLayout(0, 8));
        centerPanel.setOpaque(false);
        centerPanel.add(metricsPanel, BorderLayout.NORTH);
        centerPanel.add(tableCard, BorderLayout.CENTER);
        centerPanel.add(footnote, BorderLayout.SOUTH);

        tab.add(filterBar, BorderLayout.NORTH);
        tab.add(centerPanel, BorderLayout.CENTER);

        generateBtn.addActionListener(e -> generateDepartmentAnalytics());
        analyticsDeptCombo.addActionListener(e -> generateDepartmentAnalytics());
        analyticsSemCombo.addActionListener(e -> generateDepartmentAnalytics());
        analyticsYearCombo.addActionListener(e -> generateDepartmentAnalytics());

        generateDepartmentAnalytics();
        return tab;
    }

    private JPanel createMiniCard(String title, JLabel valueLabel, Color accent) {
        JPanel card = new JPanel(new BorderLayout(0, 2));
        card.setBackground(COLOR_WHITE);
        card.setBorder(new CompoundBorder(
                new LineBorder(COLOR_BORDER, 1, true),
                new EmptyBorder(8, 6, 8, 6)
        ));

        JLabel titleLbl = new JLabel(title, SwingConstants.CENTER);
        titleLbl.setFont(new Font("Segoe UI", Font.BOLD, 10));
        titleLbl.setForeground(COLOR_TEXT_MUTED);

        valueLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        valueLabel.setForeground(accent);

        card.add(titleLbl, BorderLayout.NORTH);
        card.add(valueLabel, BorderLayout.CENTER);
        return card;
    }

    private void populateFilterCombos(JComboBox<String> deptCombo, JComboBox<String> yearCombo) {
        deptCombo.removeAllItems();
        deptCombo.addItem("All Departments");
        yearCombo.removeAllItems();
        yearCombo.addItem("All Years");

        try (Connection con = connect();
             Statement st = con.createStatement()) {
            try (ResultSet rs = st.executeQuery("SELECT DISTINCT department FROM students WHERE department IS NOT NULL AND department != '' ORDER BY department")) {
                while (rs.next()) {
                    deptCombo.addItem(rs.getString(1));
                }
            }
            try (ResultSet rs = st.executeQuery("SELECT DISTINCT academic_year FROM enrollments WHERE academic_year IS NOT NULL ORDER BY academic_year DESC")) {
                while (rs.next()) {
                    yearCombo.addItem(rs.getString(1));
                }
            }
        } catch (SQLException ex) {
            databaseError(ex);
        }
    }

    private JPanel toolbar() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 10));
        panel.setBackground(COLOR_WHITE);
        panel.setBorder(new CompoundBorder(
                new LineBorder(COLOR_BORDER, 1, true),
                new EmptyBorder(4, 10, 4, 10)
        ));
        return panel;
    }

    private JPanel tablePanel(JPanel toolbar, JTable table) {
        JPanel panel = new JPanel(new BorderLayout(0, 12));
        panel.setOpaque(false);

        JPanel holder = new JPanel(new BorderLayout());
        holder.setBackground(COLOR_WHITE);
        holder.setBorder(new LineBorder(COLOR_BORDER, 1, true));
        holder.add(new JScrollPane(table), BorderLayout.CENTER);

        panel.add(toolbar, BorderLayout.NORTH);
        panel.add(holder, BorderLayout.CENTER);
        return panel;
    }

    private JTable table(String[] columns) {
        DefaultTableModel model = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int col) {
                return false;
            }
        };

        JTable table = new JTable(model);
        KECTheme.styleTable(table);

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

        String sql = "SELECT course_id, course_code, course_name, course_type, credits FROM courses ORDER BY course_code";

        try (Connection con = connect();
             Statement st = con.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                model.addRow(new Object[]{
                        rs.getInt("course_id"),
                        rs.getString("course_code"),
                        rs.getString("course_name"),
                        rs.getString("course_type"),
                        rs.getInt("credits")
                });
            }

            setStatus("Course records loaded: " + model.getRowCount());

        } catch (SQLException e) {
            databaseError(e);
        }
    }

    private void showCourseDialog(Object[] existing) {
        if (currentSession != null && currentSession.isStudent()) {
            JOptionPane.showMessageDialog(this,
                    "Unauthorized: Students are not permitted to add or modify course records.",
                    "Access Denied", JOptionPane.ERROR_MESSAGE);
            return;
        }

        JTextField codeField = new JTextField(existing == null ? "" : String.valueOf(existing[1]), 18);
        JTextField nameField = new JTextField(existing == null ? "" : String.valueOf(existing[2]), 22);

        String[] types = {"THEORY", "PRACTICAL", "EMBEDDED"};
        JComboBox<String> typeCombo = new JComboBox<>(types);
        if (existing != null && existing[3] != null) {
            String existingType = String.valueOf(existing[3]).trim().toUpperCase();
            typeCombo.setSelectedItem(existingType);
        } else {
            typeCombo.setSelectedItem("THEORY");
        }

        JTextField creditsField = new JTextField(existing == null ? "3" : String.valueOf(existing[4]), 18);

        KECTheme.styleTextField(codeField, 36);
        KECTheme.styleTextField(nameField, 36);
        KECTheme.styleComboBox(typeCombo, 36);
        KECTheme.styleTextField(creditsField, 36);

        JPanel form = new JPanel(new GridLayout(0, 2, 10, 12));
        form.setBorder(new EmptyBorder(14, 14, 14, 14));
        form.add(new JLabel("Course Code (e.g. 22ITT41):"));
        form.add(codeField);
        form.add(new JLabel("Course Name:"));
        form.add(nameField);
        form.add(new JLabel("Course Type (R2024):"));
        form.add(typeCombo);
        form.add(new JLabel("Credits (1-10):"));
        form.add(creditsField);

        int choice = JOptionPane.showConfirmDialog(
                this, form,
                existing == null ? "Add New Course" : "Edit Course",
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE
        );
        if (choice != JOptionPane.OK_OPTION) return;

        String code = codeField.getText().trim();
        String name = nameField.getText().trim();
        String courseType = (String) typeCombo.getSelectedItem();
        String creditsText = creditsField.getText().trim();

        if (code.isEmpty() || name.isEmpty() || courseType == null || creditsText.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please fill in all course fields.",
                    "Missing Information", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int credits;
        try {
            credits = Integer.parseInt(creditsText);
            if (credits <= 0 || credits > 10) {
                throw new NumberFormatException();
            }
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Credits must be a positive integer between 1 and 10.",
                    "Invalid Credits", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Duplicate course code check
        String checkSql = (existing == null)
                ? "SELECT COUNT(*) FROM courses WHERE course_code = ?"
                : "SELECT COUNT(*) FROM courses WHERE course_code = ? AND course_id != ?";
        try (Connection con = connect();
             PreparedStatement ps = con.prepareStatement(checkSql)) {
            ps.setString(1, code);
            if (existing != null) {
                ps.setInt(2, ((Number) existing[0]).intValue());
            }
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next() && rs.getInt(1) > 0) {
                    JOptionPane.showMessageDialog(this,
                            "Course code '" + code + "' is already registered in the curriculum.\nPlease use a unique course code.",
                            "Duplicate Course Code", JOptionPane.WARNING_MESSAGE);
                    return;
                }
            }
        } catch (SQLException ex) {
            databaseError(ex);
            return;
        }

        String sql = (existing == null)
                ? "INSERT INTO courses (course_code, course_name, course_type, credits) VALUES (?, ?, ?, ?)"
                : "UPDATE courses SET course_code = ?, course_name = ?, course_type = ?, credits = ? WHERE course_id = ?";

        try (Connection con = connect();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, code);
            ps.setString(2, name);
            ps.setString(3, courseType);
            ps.setInt(4, credits);
            if (existing != null) {
                ps.setInt(5, ((Number) existing[0]).intValue());
            }

            int changed = ps.executeUpdate();
            if (changed > 0) {
                loadCourses();
                refreshDashboard();
                setStatus(existing == null ? "Course added: " + code : "Course updated: " + code);
                JOptionPane.showMessageDialog(this,
                        existing == null ? "Course '" + code + "' added successfully." : "Course '" + code + "' updated successfully.",
                        "Success", JOptionPane.INFORMATION_MESSAGE);
            }
        } catch (SQLException ex) {
            databaseError(ex);
        }
    }

    private Object[] getSelectedCourse() {
        if (courseTable == null) return null;
        int viewRow = courseTable.getSelectedRow();
        if (viewRow < 0) {
            JOptionPane.showMessageDialog(this, "Please select a course from the table first.",
                    "No Course Selected", JOptionPane.WARNING_MESSAGE);
            return null;
        }

        int row = courseTable.convertRowIndexToModel(viewRow);
        DefaultTableModel model = (DefaultTableModel) courseTable.getModel();
        return new Object[]{
                model.getValueAt(row, 0),
                model.getValueAt(row, 1),
                model.getValueAt(row, 2),
                model.getValueAt(row, 3),
                model.getValueAt(row, 4)
        };
    }

    private void editSelectedCourse() {
        if (currentSession != null && currentSession.isStudent()) {
            JOptionPane.showMessageDialog(this, "Access Denied: Students are not permitted to edit courses.",
                    "Unauthorized", JOptionPane.ERROR_MESSAGE);
            return;
        }
        Object[] selected = getSelectedCourse();
        if (selected != null) {
            showCourseDialog(selected);
        }
    }

    private void deleteSelectedCourse() {
        if (currentSession != null && currentSession.isStudent()) {
            JOptionPane.showMessageDialog(this, "Access Denied: Students are not permitted to delete courses.",
                    "Unauthorized", JOptionPane.ERROR_MESSAGE);
            return;
        }
        Object[] selected = getSelectedCourse();
        if (selected == null) return;

        int courseId = ((Number) selected[0]).intValue();
        String code = String.valueOf(selected[1]);
        String name = String.valueOf(selected[2]);

        // Guard: check if course has active student enrollments
        int enrollmentCount = 0;
        try (Connection con = connect();
             PreparedStatement ps = con.prepareStatement("SELECT COUNT(*) FROM enrollments WHERE course_id = ?")) {
            ps.setInt(1, courseId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) enrollmentCount = rs.getInt(1);
            }
        } catch (SQLException ex) {
            databaseError(ex);
            return;
        }

        if (enrollmentCount > 0) {
            JOptionPane.showMessageDialog(this,
                    "Cannot delete course '" + code + " - " + name + "'.\n\n"
                            + "There are currently " + enrollmentCount + " student enrollment(s) linked to this course.\n"
                            + "To preserve academic records and data integrity, courses with active enrollments cannot be deleted.",
                    "Course Has Linked Enrollments", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(
                this,
                "Are you sure you want to delete course " + code + " (" + name + ")?\n\nThis action cannot be undone.",
                "Confirm Course Deletion",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE
        );
        if (confirm != JOptionPane.YES_OPTION) return;

        String sql = "DELETE FROM courses WHERE course_id = ?";
        try (Connection con = connect();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, courseId);
            ps.executeUpdate();
            loadCourses();
            refreshDashboard();
            setStatus("Course deleted: " + code);
            JOptionPane.showMessageDialog(this, "Course '" + code + "' deleted successfully.",
                    "Success", JOptionPane.INFORMATION_MESSAGE);
        } catch (SQLIntegrityConstraintViolationException ex) {
            JOptionPane.showMessageDialog(this,
                    "This course cannot be deleted because dependent academic records exist in the database.",
                    "Constraint Violation", JOptionPane.WARNING_MESSAGE);
        } catch (SQLException ex) {
            databaseError(ex);
        }
    }

    private void showEnrollmentsDialog() {
        JDialog dialog = new JDialog(this, "KEC ACADEMIX — Course Enrollment Management", true);
        dialog.setSize(1020, 680);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new BorderLayout(0, 10));

        boolean canManage = currentSession == null || !currentSession.isStudent();
        String studentRollFilter = (currentSession != null && currentSession.isStudent()) ? currentSession.getStudentRoll() : null;

        // Top Banner
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(COLOR_NAVY);
        headerPanel.setBorder(new EmptyBorder(16, 20, 16, 20));

        JLabel titleLbl = new JLabel("Course Enrollment Management");
        titleLbl.setFont(FONT_SECTION);
        titleLbl.setForeground(COLOR_WHITE);

        JLabel subLbl = new JLabel(canManage
                ? "Register students into curriculum courses, manage semesters, and view active enrollments"
                : "My registered curriculum courses and semester allocations");
        subLbl.setFont(FONT_SMALL);
        subLbl.setForeground(new Color(220, 228, 233));

        headerPanel.add(titleLbl, BorderLayout.NORTH);
        headerPanel.add(subLbl, BorderLayout.SOUTH);

        // Center: Enrollment Table and Filter Toolbar
        String[] columns = {"Enrollment ID", "Roll Number", "Student Name", "Course Code", "Course Name", "Semester", "Academic Year"};
        DefaultTableModel enrollModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int col) {
                return false;
            }
        };
        JTable enrollTable = new JTable(enrollModel);
        KECTheme.styleTable(enrollTable);
        int[] enrollWidths = {90, 110, 200, 110, 260, 80, 110};
        for (int i = 0; i < enrollWidths.length && i < enrollTable.getColumnCount(); i++) {
            enrollTable.getColumnModel().getColumn(i).setPreferredWidth(enrollWidths[i]);
        }

        // Method to reload enrollments
        Runnable loadEnrollments = () -> {
            enrollModel.setRowCount(0);
            StringBuilder sql = new StringBuilder("""
                    SELECT e.enrollment_id, s.roll_no, s.student_name,
                           c.course_code, c.course_name, e.semester, e.academic_year
                    FROM enrollments e
                    JOIN students s ON e.student_id = s.student_id
                    JOIN courses c ON e.course_id = c.course_id
                    """);
            if (studentRollFilter != null) {
                sql.append(" WHERE s.roll_no = ?");
            }
            sql.append(" ORDER BY s.roll_no, e.semester, c.course_code");

            try (Connection con = connect();
                 PreparedStatement ps = con.prepareStatement(sql.toString())) {
                if (studentRollFilter != null) {
                    ps.setString(1, studentRollFilter);
                }
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        enrollModel.addRow(new Object[]{
                                rs.getInt("enrollment_id"),
                                rs.getString("roll_no"),
                                rs.getString("student_name"),
                                rs.getString("course_code"),
                                rs.getString("course_name"),
                                rs.getInt("semester"),
                                rs.getString("academic_year")
                        });
                    }
                }
            } catch (SQLException ex) {
                databaseError(ex);
            }
        };

        // Filter Bar
        JPanel filterBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 8));
        filterBar.setBackground(COLOR_WHITE);
        filterBar.setBorder(new CompoundBorder(
                new LineBorder(COLOR_BORDER, 1, true),
                new EmptyBorder(4, 10, 4, 10)
        ));

        JTextField searchField = new JTextField(15);
        KECTheme.styleTextField(searchField, 34);
        JLabel searchLbl = new JLabel("Search:");
        searchLbl.setFont(FONT_BOLD);
        searchLbl.setForeground(COLOR_TEXT_DARK);

        JComboBox<String> semFilter = new JComboBox<>(new String[]{
                "All Semesters", "Semester 1", "Semester 2", "Semester 3", "Semester 4",
                "Semester 5", "Semester 6", "Semester 7", "Semester 8"
        });
        KECTheme.styleComboBox(semFilter, 34);

        JButton filterBtn = KECTheme.createSecondaryButton("Filter");
        JButton resetBtn = KECTheme.createSecondaryButton("Reset");

        filterBar.add(searchLbl);
        filterBar.add(searchField);
        filterBar.add(new JLabel("  Filter Semester:"));
        filterBar.add(semFilter);
        filterBar.add(filterBtn);
        filterBar.add(resetBtn);

        // Filter action
        Runnable applyFilter = () -> {
            String query = searchField.getText().trim().toLowerCase();
            int selectedSem = semFilter.getSelectedIndex(); // 0 is All, 1 is Sem 1...
            enrollModel.setRowCount(0);

            StringBuilder sql = new StringBuilder("""
                    SELECT e.enrollment_id, s.roll_no, s.student_name,
                           c.course_code, c.course_name, e.semester, e.academic_year
                    FROM enrollments e
                    JOIN students s ON e.student_id = s.student_id
                    JOIN courses c ON e.course_id = c.course_id
                    WHERE 1=1
                    """);
            if (studentRollFilter != null) {
                sql.append(" AND s.roll_no = '").append(studentRollFilter.replace("'", "''")).append("'");
            }
            if (selectedSem > 0) {
                sql.append(" AND e.semester = ").append(selectedSem);
            }
            sql.append(" ORDER BY s.roll_no, e.semester, c.course_code");

            try (Connection con = connect();
                 Statement st = con.createStatement();
                 ResultSet rs = st.executeQuery(sql.toString())) {
                while (rs.next()) {
                    String roll = rs.getString("roll_no");
                    String name = rs.getString("student_name");
                    String code = rs.getString("course_code");
                    String cname = rs.getString("course_name");

                    if (!query.isEmpty()) {
                        String full = (roll + " " + name + " " + code + " " + cname).toLowerCase();
                        if (!full.contains(query)) continue;
                    }

                    enrollModel.addRow(new Object[]{
                            rs.getInt("enrollment_id"),
                            roll,
                            name,
                            code,
                            cname,
                            rs.getInt("semester"),
                            rs.getString("academic_year")
                    });
                }
            } catch (SQLException ex) {
                databaseError(ex);
            }
        };

        filterBtn.addActionListener(e -> applyFilter.run());
        searchField.addActionListener(e -> applyFilter.run());
        semFilter.addActionListener(e -> applyFilter.run());
        resetBtn.addActionListener(e -> {
            searchField.setText("");
            semFilter.setSelectedIndex(0);
            loadEnrollments.run();
        });

        // Center Panel with Table
        JPanel centerPanel = new JPanel(new BorderLayout(0, 10));
        centerPanel.setOpaque(false);
        centerPanel.setBorder(new EmptyBorder(10, 20, 10, 20));
        centerPanel.add(filterBar, BorderLayout.NORTH);

        JPanel tableCard = new JPanel(new BorderLayout());
        tableCard.setBackground(COLOR_WHITE);
        tableCard.setBorder(new LineBorder(COLOR_BORDER, 1, true));
        tableCard.add(new JScrollPane(enrollTable), BorderLayout.CENTER);
        centerPanel.add(tableCard, BorderLayout.CENTER);

        // Bottom Form / Actions Panel
        JPanel bottomContainer = new JPanel(new BorderLayout(0, 10));
        bottomContainer.setOpaque(false);
        bottomContainer.setBorder(new EmptyBorder(0, 20, 16, 20));

        if (canManage) {
            class StudentComboItem {
                final int id;
                final String roll;
                final String name;
                final int currentSemester;
                StudentComboItem(int id, String roll, String name, int currentSemester) {
                    this.id = id; this.roll = roll; this.name = name; this.currentSemester = currentSemester;
                }
                @Override
                public String toString() {
                    return roll + " - " + name + " (Sem " + currentSemester + ")";
                }
            }

            class CourseComboItem {
                final int id;
                final String code;
                final String name;
                final String type;
                final int credits;
                CourseComboItem(int id, String code, String name, String type, int credits) {
                    this.id = id; this.code = code; this.name = name; this.type = type; this.credits = credits;
                }
                @Override
                public String toString() {
                    return code + " - " + name + " [" + type + ", " + credits + "C]";
                }
            }

            JComboBox<StudentComboItem> studentCombo = new JComboBox<>();
            JComboBox<CourseComboItem> courseCombo = new JComboBox<>();
            JComboBox<Integer> semesterCombo = new JComboBox<>(new Integer[]{1, 2, 3, 4, 5, 6, 7, 8});
            JTextField yearField = new JTextField("2024-2025", 10);

            KECTheme.styleComboBox(studentCombo, 34);
            KECTheme.styleComboBox(courseCombo, 34);
            KECTheme.styleComboBox(semesterCombo, 34);
            KECTheme.styleTextField(yearField, 34);

            try (Connection con = connect();
                 Statement st = con.createStatement()) {
                try (ResultSet rs = st.executeQuery("SELECT student_id, roll_no, student_name, semester FROM students ORDER BY roll_no")) {
                    while (rs.next()) {
                        studentCombo.addItem(new StudentComboItem(
                                rs.getInt("student_id"),
                                rs.getString("roll_no"),
                                rs.getString("student_name"),
                                rs.getInt("semester")
                        ));
                    }
                }
                try (ResultSet rs = st.executeQuery("SELECT course_id, course_code, course_name, course_type, credits FROM courses ORDER BY course_code")) {
                    while (rs.next()) {
                        courseCombo.addItem(new CourseComboItem(
                                rs.getInt("course_id"),
                                rs.getString("course_code"),
                                rs.getString("course_name"),
                                rs.getString("course_type"),
                                rs.getInt("credits")
                        ));
                    }
                }
            } catch (SQLException ex) {
                databaseError(ex);
            }

            studentCombo.addActionListener(e -> {
                StudentComboItem item = (StudentComboItem) studentCombo.getSelectedItem();
                if (item != null && item.currentSemester >= 1 && item.currentSemester <= 8) {
                    semesterCombo.setSelectedItem(item.currentSemester);
                }
            });
            if (studentCombo.getItemCount() > 0) {
                StudentComboItem first = studentCombo.getItemAt(0);
                if (first != null && first.currentSemester >= 1 && first.currentSemester <= 8) {
                    semesterCombo.setSelectedItem(first.currentSemester);
                }
            }

            JPanel formCard = new JPanel(new GridBagLayout());
            formCard.setBackground(COLOR_WHITE);
            formCard.setBorder(new CompoundBorder(
                    new LineBorder(COLOR_BORDER, 1, true),
                    new EmptyBorder(12, 16, 12, 16)
            ));

            GridBagConstraints gbc = new GridBagConstraints();
            gbc.insets = new Insets(4, 6, 4, 6);
            gbc.fill = GridBagConstraints.HORIZONTAL;

            gbc.gridx = 0; gbc.gridy = 0;
            formCard.add(new JLabel("Student:"), gbc);
            gbc.gridx = 1; gbc.gridy = 0; gbc.weightx = 1.0;
            formCard.add(studentCombo, gbc);

            gbc.gridx = 2; gbc.gridy = 0; gbc.weightx = 0;
            formCard.add(new JLabel("Course:"), gbc);
            gbc.gridx = 3; gbc.gridy = 0; gbc.weightx = 1.0;
            formCard.add(courseCombo, gbc);

            gbc.gridx = 0; gbc.gridy = 1; gbc.weightx = 0;
            formCard.add(new JLabel("Semester:"), gbc);
            gbc.gridx = 1; gbc.gridy = 1; gbc.weightx = 0.5;
            formCard.add(semesterCombo, gbc);

            gbc.gridx = 2; gbc.gridy = 1; gbc.weightx = 0;
            formCard.add(new JLabel("Academic Year:"), gbc);
            gbc.gridx = 3; gbc.gridy = 1; gbc.weightx = 0.5;
            formCard.add(yearField, gbc);

            JButton enrollBtn = KECTheme.createPrimaryButton("+ Enroll Student");
            gbc.gridx = 4; gbc.gridy = 0; gbc.gridheight = 2; gbc.weightx = 0; gbc.fill = GridBagConstraints.BOTH;
            formCard.add(enrollBtn, gbc);

            enrollBtn.addActionListener(e -> {
                StudentComboItem student = (StudentComboItem) studentCombo.getSelectedItem();
                CourseComboItem course = (CourseComboItem) courseCombo.getSelectedItem();
                Integer sem = (Integer) semesterCombo.getSelectedItem();
                String yr = yearField.getText().trim();

                if (student == null || course == null || sem == null || yr.isEmpty()) {
                    JOptionPane.showMessageDialog(dialog, "Please select student, course, semester, and academic year.",
                            "Missing Information", JOptionPane.WARNING_MESSAGE);
                    return;
                }

                String dupCheck = "SELECT COUNT(*) FROM enrollments WHERE student_id = ? AND course_id = ? AND academic_year = ?";
                try (Connection con = connect();
                     PreparedStatement ps = con.prepareStatement(dupCheck)) {
                    ps.setInt(1, student.id);
                    ps.setInt(2, course.id);
                    ps.setString(3, yr);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (rs.next() && rs.getInt(1) > 0) {
                            JOptionPane.showMessageDialog(dialog,
                                    "Student '" + student.roll + "' is already enrolled in course '" + course.code
                                            + "' for Academic Year " + yr + ".\nDuplicate enrollment is not allowed.",
                                    "Duplicate Enrollment", JOptionPane.WARNING_MESSAGE);
                            return;
                        }
                    }
                } catch (SQLException ex) {
                    databaseError(ex);
                    return;
                }

                try (Connection con = connect()) {
                    con.setAutoCommit(false);
                    int generatedEnrollmentId = -1;

                    String insertSql = "INSERT INTO enrollments (student_id, course_id, semester, academic_year) VALUES (?, ?, ?, ?)";
                    try (PreparedStatement ps = con.prepareStatement(insertSql, Statement.RETURN_GENERATED_KEYS)) {
                        ps.setInt(1, student.id);
                        ps.setInt(2, course.id);
                        ps.setInt(3, sem);
                        ps.setString(4, yr);
                        ps.executeUpdate();
                        try (ResultSet rs = ps.getGeneratedKeys()) {
                            if (rs.next()) {
                                generatedEnrollmentId = rs.getInt(1);
                            }
                        }
                    }

                    if (generatedEnrollmentId > 0) {
                        String seedAtt = "INSERT INTO attendance (enrollment_id, classes_held, classes_attended, condonation_approved, condonation_reason) VALUES (?, 0, 0, 0, NULL)";
                        try (PreparedStatement psAtt = con.prepareStatement(seedAtt)) {
                            psAtt.setInt(1, generatedEnrollmentId);
                            psAtt.executeUpdate();
                        }
                    }

                    con.commit();
                    loadEnrollments.run();
                    refreshDashboard();
                    JOptionPane.showMessageDialog(dialog,
                            "Successfully enrolled student '" + student.roll + "' in course '" + course.code + "'.",
                            "Enrollment Successful", JOptionPane.INFORMATION_MESSAGE);

                } catch (SQLException ex) {
                    databaseError(ex);
                }
            });

            JPanel actionTools = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 6));
            actionTools.setOpaque(false);

            JButton dropBtn = KECTheme.createDangerButton("Drop Selected Enrollment");
            JButton closeBtn = KECTheme.createSecondaryButton("Close");

            actionTools.add(dropBtn);
            actionTools.add(closeBtn);

            dropBtn.addActionListener(e -> {
                int viewRow = enrollTable.getSelectedRow();
                if (viewRow < 0) {
                    JOptionPane.showMessageDialog(dialog, "Please select an enrollment record from the table to drop.",
                            "No Enrollment Selected", JOptionPane.WARNING_MESSAGE);
                    return;
                }
                int row = enrollTable.convertRowIndexToModel(viewRow);
                int enrollmentId = ((Number) enrollModel.getValueAt(row, 0)).intValue();
                String roll = String.valueOf(enrollModel.getValueAt(row, 1));
                String code = String.valueOf(enrollModel.getValueAt(row, 3));

                try (Connection con = connect()) {
                    int markCount = 0;
                    try (PreparedStatement ps = con.prepareStatement("SELECT COUNT(*) FROM assessment_marks WHERE enrollment_id = ?")) {
                        ps.setInt(1, enrollmentId);
                        try (ResultSet rs = ps.executeQuery()) {
                            if (rs.next()) markCount = rs.getInt(1);
                        }
                    }

                    int classesHeld = 0;
                    try (PreparedStatement ps = con.prepareStatement("SELECT classes_held FROM attendance WHERE enrollment_id = ?")) {
                        ps.setInt(1, enrollmentId);
                        try (ResultSet rs = ps.executeQuery()) {
                            if (rs.next()) classesHeld = rs.getInt(1);
                        }
                    }

                    if (markCount > 0 || classesHeld > 0) {
                        JOptionPane.showMessageDialog(dialog,
                                "Cannot drop enrollment for " + roll + " in " + code + ".\n\n"
                                        + "This enrollment has active academic records (" + markCount + " assessment marks, " + classesHeld + " classes held).\n"
                                        + "To preserve academic evaluation integrity, enrollments with active records cannot be dropped.",
                                "Active Records Exist", JOptionPane.WARNING_MESSAGE);
                        return;
                    }

                    int confirm = JOptionPane.showConfirmDialog(dialog,
                            "Are you sure you want to drop enrollment #" + enrollmentId + " (" + roll + " in " + code + ")?\nThis action cannot be undone.",
                            "Confirm Drop Enrollment", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
                    if (confirm != JOptionPane.YES_OPTION) return;

                    con.setAutoCommit(false);
                    try (PreparedStatement ps = con.prepareStatement("DELETE FROM attendance WHERE enrollment_id = ?")) {
                        ps.setInt(1, enrollmentId);
                        ps.executeUpdate();
                    }
                    try (PreparedStatement ps = con.prepareStatement("DELETE FROM enrollments WHERE enrollment_id = ?")) {
                        ps.setInt(1, enrollmentId);
                        ps.executeUpdate();
                    }
                    con.commit();
                    loadEnrollments.run();
                    refreshDashboard();
                    JOptionPane.showMessageDialog(dialog, "Enrollment dropped successfully.", "Success", JOptionPane.INFORMATION_MESSAGE);

                } catch (SQLException ex) {
                    databaseError(ex);
                }
            });

            closeBtn.addActionListener(e -> dialog.dispose());

            bottomContainer.add(formCard, BorderLayout.CENTER);
            bottomContainer.add(actionTools, BorderLayout.SOUTH);

        } else {
            JPanel actionTools = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 6));
            actionTools.setOpaque(false);
            JButton closeBtn = KECTheme.createSecondaryButton("Close");
            closeBtn.addActionListener(e -> dialog.dispose());
            actionTools.add(closeBtn);
            bottomContainer.add(actionTools, BorderLayout.CENTER);
        }

        dialog.add(headerPanel, BorderLayout.NORTH);
        dialog.add(centerPanel, BorderLayout.CENTER);
        dialog.add(bottomContainer, BorderLayout.SOUTH);

        loadEnrollments.run();
        dialog.setVisible(true);
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

    private static class CurrentMarksheetData {
        String roll;
        String name;
        String dept;
        int semester = 1;
        String academicYear = "";
        List<EvaluationEngine.CourseEvaluationResult> evaluatedResults = new ArrayList<>();
        List<PdfMarksheetGenerator.CourseMarkEntry> markEntries = new ArrayList<>();
        int totalCreditsRegistered = 0;
        int totalCreditsEarned = 0;
        double sgpa = 0.0;
        double equivalentPercentage = 0.0;
        String overallStatus = "PENDING";
        boolean isIncomplete = false;
    }
    private CurrentMarksheetData currentMarksheetData = null;

    private void generateReport() {
        generateMarksheet();
    }

    private void resetMarksheetSummary() {
        marksheetCreditsRegisteredLabel.setText("0");
        marksheetCreditsEarnedLabel.setText("0");
        marksheetSgpaLabel.setText("--");
        marksheetPercentageLabel.setText("--");
        marksheetStatusBadge.setText("NO RECORD");
        marksheetStatusBadge.setForeground(COLOR_TEXT_MUTED);
        currentMarksheetData = null;
    }

    private void generateMarksheet() {
        String roll = reportRoll.getText().trim();

        if (currentSession != null && currentSession.isStudent()) {
            if (!roll.equalsIgnoreCase(currentSession.getStudentRoll())) {
                JOptionPane.showMessageDialog(this,
                        "Unauthorized: Students are only permitted to view their own academic report.",
                        "Access Denied", JOptionPane.ERROR_MESSAGE);
                reportRoll.setText(currentSession.getStudentRoll());
                roll = currentSession.getStudentRoll();
            }
        }

        if (roll == null || roll.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter a student roll number.",
                    "Required Field", JOptionPane.WARNING_MESSAGE);
            return;
        }

        DefaultTableModel model = (DefaultTableModel) reportTable.getModel();
        model.setRowCount(0);

        currentMarksheetData = new CurrentMarksheetData();
        currentMarksheetData.roll = roll;

        // Query student info
        String studentSql = "SELECT student_id, roll_no, student_name, department, semester FROM students WHERE roll_no = ?";
        int studentId = -1;
        String studentName = "";
        String department = "";

        try (Connection con = connect();
             PreparedStatement ps = con.prepareStatement(studentSql)) {
            ps.setString(1, roll);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    studentId = rs.getInt("student_id");
                    studentName = rs.getString("student_name");
                    department = rs.getString("department");
                } else {
                    JOptionPane.showMessageDialog(this,
                            "No student record found for roll number: " + roll,
                            "Student Not Found", JOptionPane.WARNING_MESSAGE);
                    marksheetHeaderLabel.setText("No student found with roll number: " + roll);
                    resetMarksheetSummary();
                    return;
                }
            }
        } catch (SQLException ex) {
            databaseError(ex);
            return;
        }

        currentMarksheetData.name = studentName;
        currentMarksheetData.dept = department;

        int selectedSemIndex = marksheetSemCombo.getSelectedIndex();
        String selectedYear = (String) marksheetYearCombo.getSelectedItem();
        String selectedDept = (String) marksheetDeptCombo.getSelectedItem();

        if (selectedDept != null && !selectedDept.equals("All Departments") && !department.equalsIgnoreCase(selectedDept)) {
            JOptionPane.showMessageDialog(this,
                    "Student " + roll + " belongs to '" + department + "', not '" + selectedDept + "'.",
                    "Department Information", JOptionPane.INFORMATION_MESSAGE);
            marksheetDeptCombo.setSelectedItem(department);
        }

        StringBuilder enrollSql = new StringBuilder("""
                SELECT e.enrollment_id, e.semester, e.academic_year,
                       c.course_id, c.course_code, c.course_name, c.course_type, c.credits,
                       a.classes_held, a.classes_attended, a.condonation_approved
                FROM enrollments e
                JOIN courses c ON e.course_id = c.course_id
                LEFT JOIN attendance a ON e.enrollment_id = a.enrollment_id
                WHERE e.student_id = ?
                """);
        if (selectedSemIndex > 0) {
            enrollSql.append(" AND e.semester = ").append(selectedSemIndex);
        }
        if (selectedYear != null && !selectedYear.equals("All Years")) {
            enrollSql.append(" AND e.academic_year = '").append(selectedYear.replace("'", "''")).append("'");
        }
        enrollSql.append(" ORDER BY e.semester, c.course_code");

        class EnrollmentRecord {
            int enrollmentId;
            int semester;
            String academicYear;
            String courseCode;
            String courseName;
            String courseType;
            int credits;
            Integer classesHeld;
            Integer classesAttended;
            Boolean condonationApproved;
        }

        List<EnrollmentRecord> enrollList = new ArrayList<>();
        try (Connection con = connect();
             PreparedStatement ps = con.prepareStatement(enrollSql.toString())) {
            ps.setInt(1, studentId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    EnrollmentRecord rec = new EnrollmentRecord();
                    rec.enrollmentId = rs.getInt("enrollment_id");
                    rec.semester = rs.getInt("semester");
                    rec.academicYear = rs.getString("academic_year");
                    rec.courseCode = rs.getString("course_code");
                    rec.courseName = rs.getString("course_name");
                    rec.courseType = rs.getString("course_type");
                    rec.credits = rs.getInt("credits");

                    int held = rs.getInt("classes_held");
                    rec.classesHeld = rs.wasNull() ? null : held;
                    int att = rs.getInt("classes_attended");
                    rec.classesAttended = rs.wasNull() ? null : att;
                    boolean cond = rs.getBoolean("condonation_approved");
                    rec.condonationApproved = rs.wasNull() ? null : cond;

                    enrollList.add(rec);
                }
            }
        } catch (SQLException ex) {
            databaseError(ex);
            return;
        }

        if (enrollList.isEmpty()) {
            marksheetHeaderLabel.setText("Student: " + studentName + " (" + roll + ") | Dept: " + department + " | No course enrollments found for the selected filter.");
            resetMarksheetSummary();
            return;
        }

        int targetSem = (selectedSemIndex > 0) ? selectedSemIndex : enrollList.get(0).semester;
        String targetYear = (selectedYear != null && !selectedYear.equals("All Years")) ? selectedYear : enrollList.get(0).academicYear;
        currentMarksheetData.semester = targetSem;
        currentMarksheetData.academicYear = targetYear;

        marksheetHeaderLabel.setText(String.format("Student: %s  |  Roll: %s  |  Dept: %s  |  Semester: %d  |  Academic Year: %s",
                studentName, roll, department, targetSem, targetYear));

        int totalRegCredits = 0;
        int totalEarnedCredits = 0;
        boolean semesterIncomplete = false;
        boolean hasSa = false;
        boolean hasAb = false;
        boolean hasRa = false;

        try (Connection con = connect()) {
            for (EnrollmentRecord rec : enrollList) {
                totalRegCredits += rec.credits;

                // 1. Evaluate Attendance
                int held = (rec.classesHeld != null) ? rec.classesHeld : 0;
                int attended = (rec.classesAttended != null) ? rec.classesAttended : 0;
                boolean condoned = (rec.condonationApproved != null) && rec.condonationApproved;
                EvaluationEngine.AttendanceEvaluation attEval = EvaluationEngine.evaluateAttendance(held, attended, condoned);
                String attStr = (held == 0) ? "N/A (0 Held)" : String.format("%.1f%%", attEval.percentage);

                // 2. Read Marks
                double cia1 = 0, cia2 = 0, cia3 = 0;
                int ciaCount = 0;
                Double rawEse = null;
                boolean isAbsentEse = false;

                String markSql = "SELECT assessment_name, max_marks, marks_obtained, is_absent FROM assessment_marks WHERE enrollment_id = ?";
                try (PreparedStatement psMark = con.prepareStatement(markSql)) {
                    psMark.setInt(1, rec.enrollmentId);
                    try (ResultSet rsMark = psMark.executeQuery()) {
                        while (rsMark.next()) {
                            String aName = rsMark.getString("assessment_name");
                            double obt = rsMark.getDouble("marks_obtained");
                            double max = rsMark.getDouble("max_marks");
                            boolean abs = rsMark.getBoolean("is_absent");

                            String norm = (aName != null) ? aName.replaceAll("[^A-Za-z0-9]", "").toUpperCase() : "";
                            if (norm.contains("CIA1") || norm.contains("INTERNAL1") || norm.equals("TEST1")) {
                                cia1 = (max > 0 && max != 100) ? (obt / max) * 100.0 : obt;
                                ciaCount++;
                            } else if (norm.contains("CIA2") || norm.contains("INTERNAL2") || norm.equals("TEST2")) {
                                cia2 = (max > 0 && max != 100) ? (obt / max) * 100.0 : obt;
                                ciaCount++;
                            } else if (norm.contains("CIA3") || norm.contains("INTERNAL3") || norm.equals("TEST3") || norm.contains("ASSIGNMENT") || norm.contains("QUIZ")) {
                                cia3 = (max > 0 && max != 100) ? (obt / max) * 100.0 : obt;
                                ciaCount++;
                            } else if (norm.contains("ESE") || norm.contains("ENDSEMESTER") || norm.contains("SEMESTEREXAM")) {
                                isAbsentEse = abs;
                                rawEse = isAbsentEse ? 0.0 : obt;
                            }
                        }
                    }
                }

                double internal100 = 0.0;
                if (ciaCount > 0) {
                    internal100 = (cia1 + cia2 + cia3) / (double) Math.max(ciaCount, 3);
                }

                // 3. Evaluate via Centralized Evaluation Engine
                EvaluationEngine.CourseType cType = EvaluationEngine.CourseType.fromString(rec.courseType);
                EvaluationEngine.CourseEvaluationResult res = EvaluationEngine.evaluateCourse(
                        cType, rec.credits, internal100, rawEse, isAbsentEse, attEval
                );

                currentMarksheetData.evaluatedResults.add(res);

                String eseDisplay;
                if (isAbsentEse) {
                    eseDisplay = "AB (0.00)";
                } else if (rawEse == null) {
                    eseDisplay = "PENDING";
                    semesterIncomplete = true;
                } else {
                    eseDisplay = String.format("%.2f / %.0f", res.scaledEse, res.maxEseScale);
                }

                String totalDisplay = (rawEse == null) ? "INCOMPLETE" : String.format("%.2f", res.totalMarks);
                String gradeDisplay = res.grade;
                String gpDisplay = (rawEse == null) ? "—" : String.valueOf(res.gradePoint);

                if (res.isPassed) {
                    totalEarnedCredits += rec.credits;
                } else {
                    if (res.isAttendanceShortage) hasSa = true;
                    else if (res.isAbsentEse) hasAb = true;
                    else if (rawEse != null) hasRa = true;
                }

                model.addRow(new Object[]{
                        roll,
                        studentName,
                        rec.courseCode,
                        rec.courseName,
                        rec.credits,
                        String.format("%.2f / %.0f", res.scaledInternal, res.maxInternalScale),
                        eseDisplay,
                        totalDisplay,
                        gradeDisplay,
                        gpDisplay,
                        res.resultStatus,
                        attStr
                });

                currentMarksheetData.markEntries.add(new PdfMarksheetGenerator.CourseMarkEntry(
                        rec.courseCode,
                        rec.courseName,
                        cType.displayName,
                        rec.credits,
                        (held == 0 ? 100.0 : attEval.percentage),
                        res.scaledInternal,
                        res.maxInternalScale,
                        (rawEse == null ? "PENDING" : (isAbsentEse ? "AB" : String.format("%.2f", res.scaledEse))),
                        res.maxEseScale,
                        (rawEse == null ? "INCOMPLETE" : String.format("%.2f", res.totalMarks)),
                        res.grade,
                        res.gradePoint,
                        res.resultStatus
                ));
            }

        } catch (SQLException ex) {
            databaseError(ex);
            return;
        }

        currentMarksheetData.totalCreditsRegistered = totalRegCredits;
        currentMarksheetData.totalCreditsEarned = totalEarnedCredits;
        currentMarksheetData.isIncomplete = semesterIncomplete;

        marksheetCreditsRegisteredLabel.setText(String.valueOf(totalRegCredits));
        marksheetCreditsEarnedLabel.setText(String.valueOf(totalEarnedCredits));

        if (semesterIncomplete) {
            currentMarksheetData.sgpa = 0.0;
            currentMarksheetData.equivalentPercentage = 0.0;
            currentMarksheetData.overallStatus = "INCOMPLETE";

            marksheetSgpaLabel.setText("INCOMPLETE");
            marksheetPercentageLabel.setText("N/A");
            marksheetStatusBadge.setText("  INCOMPLETE (PENDING ESE)  ");
            marksheetStatusBadge.setForeground(COLOR_AMBER);
            setStatus("Semester evaluation incomplete: pending ESE marks for " + roll);

        } else {
            double sgpa = EvaluationEngine.calculateSGPA(currentMarksheetData.evaluatedResults);
            double equivPct = EvaluationEngine.calculateEquivalentPercentage(sgpa);

            currentMarksheetData.sgpa = sgpa;
            currentMarksheetData.equivalentPercentage = equivPct;

            String statusStr;
            Color statusColor;
            if (hasSa) {
                statusStr = "SHORTAGE OF ATTENDANCE (SA)";
                statusColor = COLOR_RED;
            } else if (hasAb) {
                statusStr = "ABSENT (AB)";
                statusColor = COLOR_RED;
            } else if (hasRa || totalEarnedCredits < totalRegCredits) {
                statusStr = "REAPPEAR (RA)";
                statusColor = COLOR_RED;
            } else {
                statusStr = "PASS";
                statusColor = COLOR_GREEN;
            }

            currentMarksheetData.overallStatus = statusStr;

            marksheetSgpaLabel.setText(String.format("%.2f", sgpa));
            marksheetPercentageLabel.setText(String.format("%.2f%%", equivPct));
            marksheetStatusBadge.setText("  " + statusStr + "  ");
            marksheetStatusBadge.setForeground(statusColor);
            setStatus(String.format("Official marksheet generated for %s (SGPA: %.2f)", roll, sgpa));
        }
    }

    private void exportMarksheetPdf() {
        if (currentMarksheetData == null || currentMarksheetData.markEntries.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Please generate a student semester marksheet first before exporting.",
                    "No Marksheet Loaded", JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (currentMarksheetData.isIncomplete) {
            int confirmIncomplete = JOptionPane.showConfirmDialog(this,
                    "Warning: This student's semester has incomplete course assessments (pending ESE).\n"
                            + "The exported marksheet will indicate incomplete evaluation.\n\n"
                            + "Do you still want to proceed with PDF export?",
                    "Incomplete Assessment Warning",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.WARNING_MESSAGE);
            if (confirmIncomplete != JOptionPane.YES_OPTION) return;
        }

        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Export Official KEC Marksheet PDF");
        String defaultFileName = String.format("Marksheet_%s_Sem%d.pdf",
                currentMarksheetData.roll.replace(" ", "_"),
                currentMarksheetData.semester);
        fileChooser.setSelectedFile(new File(defaultFileName));
        fileChooser.setFileFilter(new FileNameExtensionFilter("PDF Documents (*.pdf)", "pdf"));

        int userSelection = fileChooser.showSaveDialog(this);
        if (userSelection != JFileChooser.APPROVE_OPTION) {
            return;
        }

        File fileToSave = fileChooser.getSelectedFile();
        if (!fileToSave.getName().toLowerCase().endsWith(".pdf")) {
            fileToSave = new File(fileToSave.getParentFile(), fileToSave.getName() + ".pdf");
        }

        if (fileToSave.exists()) {
            int confirm = JOptionPane.showConfirmDialog(this,
                    "The file '" + fileToSave.getName() + "' already exists.\nDo you want to overwrite it?",
                    "Confirm Overwrite",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.WARNING_MESSAGE);
            if (confirm != JOptionPane.YES_OPTION) {
                return;
            }
        }

        try {
            PdfMarksheetGenerator.generateMarksheetPdf(
                    fileToSave,
                    currentMarksheetData.roll,
                    currentMarksheetData.name,
                    currentMarksheetData.dept,
                    currentMarksheetData.semester,
                    currentMarksheetData.academicYear,
                    currentMarksheetData.markEntries,
                    currentMarksheetData.totalCreditsRegistered,
                    currentMarksheetData.totalCreditsEarned,
                    currentMarksheetData.sgpa,
                    currentMarksheetData.equivalentPercentage,
                    currentMarksheetData.overallStatus
            );

            setStatus("PDF marksheet generated: " + fileToSave.getAbsolutePath());
            int openOption = JOptionPane.showConfirmDialog(this,
                    "Official marksheet PDF successfully exported to:\n" + fileToSave.getAbsolutePath()
                            + "\n\nWould you like to open it now?",
                    "Export Successful",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.INFORMATION_MESSAGE);
            if (openOption == JOptionPane.YES_OPTION && Desktop.isDesktopSupported()) {
                try {
                    Desktop.getDesktop().open(fileToSave);
                } catch (Exception ex) {
                    // Handled gracefully
                }
            }

        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this,
                    "Failed to export PDF marksheet: " + ex.getMessage(),
                    "Export Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void loadAttendanceDefaulters() {
        if (defaultersTable == null) return;
        DefaultTableModel model = (DefaultTableModel) defaultersTable.getModel();
        model.setRowCount(0);

        String dept = (String) defaultersDeptCombo.getSelectedItem();
        int semIndex = defaultersSemCombo.getSelectedIndex();
        String year = (String) defaultersYearCombo.getSelectedItem();
        String statusFilter = (String) defaultersStatusCombo.getSelectedItem();

        StringBuilder sql = new StringBuilder("""
                SELECT a.attendance_id, e.enrollment_id, s.roll_no, s.student_name, s.department,
                       e.semester, e.academic_year, c.course_code, c.course_name,
                       a.classes_held, a.classes_attended, a.condonation_approved, a.condonation_reason
                FROM enrollments e
                JOIN students s ON e.student_id = s.student_id
                JOIN courses c ON e.course_id = c.course_id
                LEFT JOIN attendance a ON e.enrollment_id = a.enrollment_id
                WHERE 1=1
                """);

        if (currentSession != null && currentSession.isStudent()) {
            sql.append(" AND s.roll_no = '").append(currentSession.getStudentRoll().replace("'", "''")).append("'");
        } else if (dept != null && !dept.equals("All Departments")) {
            sql.append(" AND s.department = '").append(dept.replace("'", "''")).append("'");
        }
        if (semIndex > 0) {
            sql.append(" AND e.semester = ").append(semIndex);
        }
        if (year != null && !year.equals("All Years")) {
            sql.append(" AND e.academic_year = '").append(year.replace("'", "''")).append("'");
        }
        sql.append(" ORDER BY s.roll_no, e.semester, c.course_code");

        int totalCount = 0;
        int eligibleCount = 0;
        int condonationCount = 0;
        int saCount = 0;

        try (Connection con = connect();
             Statement st = con.createStatement();
             ResultSet rs = st.executeQuery(sql.toString())) {

            while (rs.next()) {
                int attId = rs.getInt("attendance_id");
                String roll = rs.getString("roll_no");
                String name = rs.getString("student_name");
                String sDept = rs.getString("department");
                int sem = rs.getInt("semester");
                String cCode = rs.getString("course_code");
                String cName = rs.getString("course_name");
                int held = rs.getInt("classes_held");
                boolean heldNull = rs.wasNull();
                int att = rs.getInt("classes_attended");
                boolean attNull = rs.wasNull();
                boolean condoned = rs.getBoolean("condonation_approved");

                EvaluationEngine.AttendanceEvaluation eval = EvaluationEngine.evaluateAttendance(
                        heldNull ? 0 : held, attNull ? 0 : att, condoned
                );

                String attPctDisplay;
                String eligDisplay;

                if (heldNull || held == 0) {
                    attPctDisplay = "Pending (0 Held)";
                    eligDisplay = "Pending / Not Started";
                } else {
                    attPctDisplay = String.format("%.2f%%", eval.percentage);
                    if (eval.status == EvaluationEngine.AttendanceStatus.ELIGIBLE) {
                        eligDisplay = "ELIGIBLE (>=75%)";
                        eligibleCount++;
                    } else if (eval.status == EvaluationEngine.AttendanceStatus.ELIGIBLE_CONDONED) {
                        eligDisplay = "ELIGIBLE (CONDONED)";
                        eligibleCount++;
                    } else if (eval.status == EvaluationEngine.AttendanceStatus.CONDONATION_REQUIRED) {
                        eligDisplay = "CONDONATION REQUIRED (INELIGIBLE)";
                        condonationCount++;
                    } else {
                        eligDisplay = "DETAINED / SA (<65%)";
                        saCount++;
                    }
                }
                totalCount++;

                if (statusFilter != null && !statusFilter.equals("All Students")) {
                    if (statusFilter.contains("All Defaulters") && (eval.percentage >= 75.0 || held == 0)) {
                        continue;
                    }
                    if (statusFilter.contains("Critical Shortage") && (eval.status != EvaluationEngine.AttendanceStatus.DETAINED_SA || held == 0)) {
                        continue;
                    }
                    if (statusFilter.contains("Condonation Required") && (eval.status != EvaluationEngine.AttendanceStatus.CONDONATION_REQUIRED || held == 0)) {
                        continue;
                    }
                    if (statusFilter.contains("Eligible") && (eval.status != EvaluationEngine.AttendanceStatus.ELIGIBLE && eval.status != EvaluationEngine.AttendanceStatus.ELIGIBLE_CONDONED)) {
                        continue;
                    }
                }

                model.addRow(new Object[]{
                        attId, roll, name, sDept, sem, cCode, cName,
                        (heldNull ? 0 : held), (attNull ? 0 : att), attPctDisplay, eligDisplay
                });
            }

            defaultersSummaryLabel.setText(String.format("  Total Records: %d  |  Eligible: %d  |  Condonation Required: %d  |  Critical SA (<65%%): %d",
                    totalCount, eligibleCount, condonationCount, saCount));

        } catch (SQLException ex) {
            databaseError(ex);
        }
    }

    private void approveSelectedCondonation() {
        if (currentSession != null && currentSession.isStudent()) {
            JOptionPane.showMessageDialog(this, "Access Denied: Students cannot approve attendance condonations.",
                    "Unauthorized", JOptionPane.ERROR_MESSAGE);
            return;
        }

        int viewRow = defaultersTable.getSelectedRow();
        if (viewRow < 0) {
            JOptionPane.showMessageDialog(this, "Please select a student attendance record from the table first.",
                    "No Record Selected", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int row = defaultersTable.convertRowIndexToModel(viewRow);
        DefaultTableModel model = (DefaultTableModel) defaultersTable.getModel();
        int attendanceId = ((Number) model.getValueAt(row, 0)).intValue();
        String roll = String.valueOf(model.getValueAt(row, 1));
        String code = String.valueOf(model.getValueAt(row, 5));
        String eligStatus = String.valueOf(model.getValueAt(row, 10));

        if (eligStatus.contains("DETAINED") || eligStatus.contains("<65%")) {
            JOptionPane.showMessageDialog(this,
                    "Regulation Notice: Student " + roll + " has attendance strictly below 65%.\n"
                            + "Under KEC R2024 regulations, attendance below 65% cannot be condoned under any circumstances (Detained / SA).",
                    "Condonation Disallowed", JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (eligStatus.contains("ELIGIBLE (>=75%)")) {
            JOptionPane.showMessageDialog(this,
                    "Student " + roll + " in course " + code + " already has >=75% attendance and is fully eligible without condonation.",
                    "Already Eligible", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        if (eligStatus.contains("ELIGIBLE (CONDONED)")) {
            JOptionPane.showMessageDialog(this,
                    "Student " + roll + " in course " + code + " already has approved condonation.",
                    "Already Condoned", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        String reason = JOptionPane.showInputDialog(this,
                "Approve medical condonation for " + roll + " in " + code + "?\n\nEnter official medical approval remarks / HoD reference:",
                "Approve Medical Condonation",
                JOptionPane.QUESTION_MESSAGE);
        if (reason == null || reason.trim().isEmpty()) {
            return;
        }

        String sql = "UPDATE attendance SET condonation_approved = 1, condonation_reason = ? WHERE attendance_id = ?";
        try (Connection con = connect();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, reason.trim());
            ps.setInt(2, attendanceId);
            int updated = ps.executeUpdate();
            if (updated > 0) {
                loadAttendanceDefaulters();
                JOptionPane.showMessageDialog(this,
                        "Medical condonation approved successfully for " + roll + " in " + code + ".",
                        "Condonation Approved", JOptionPane.INFORMATION_MESSAGE);
            }
        } catch (SQLException ex) {
            databaseError(ex);
        }
    }

    private void generateDepartmentAnalytics() {
        if (analyticsCourseTable == null) return;
        DefaultTableModel model = (DefaultTableModel) analyticsCourseTable.getModel();
        model.setRowCount(0);

        String dept = (String) analyticsDeptCombo.getSelectedItem();
        int semIndex = analyticsSemCombo.getSelectedIndex();
        String year = (String) analyticsYearCombo.getSelectedItem();

        StringBuilder studentSql = new StringBuilder("""
                SELECT DISTINCT s.student_id, s.roll_no, s.student_name, s.department, e.semester, e.academic_year
                FROM enrollments e
                JOIN students s ON e.student_id = s.student_id
                WHERE 1=1
                """);
        if (dept != null && !dept.equals("All Departments")) {
            studentSql.append(" AND s.department = '").append(dept.replace("'", "''")).append("'");
        }
        if (semIndex > 0) {
            studentSql.append(" AND e.semester = ").append(semIndex);
        }
        if (year != null && !year.equals("All Years")) {
            studentSql.append(" AND e.academic_year = '").append(year.replace("'", "''")).append("'");
        }

        class StudentScope {
            int studentId;
            String roll;
            int semester;
            String academicYear;
        }

        List<StudentScope> students = new ArrayList<>();
        try (Connection con = connect();
             Statement st = con.createStatement();
             ResultSet rs = st.executeQuery(studentSql.toString())) {
            while (rs.next()) {
                StudentScope sc = new StudentScope();
                sc.studentId = rs.getInt("student_id");
                sc.roll = rs.getString("roll_no");
                sc.semester = rs.getInt("semester");
                sc.academicYear = rs.getString("academic_year");
                students.add(sc);
            }
        } catch (SQLException ex) {
            databaseError(ex);
            return;
        }

        int totalStudents = students.size();
        int passedStudents = 0;
        int failedStudents = 0;
        int saAbStudents = 0;
        int incompleteStudents = 0;
        List<Double> completedSgpas = new ArrayList<>();

        try (Connection con = connect()) {
            for (StudentScope sc : students) {
                String enrollSql = """
                        SELECT e.enrollment_id, c.course_id, c.course_type, c.credits,
                               a.classes_held, a.classes_attended, a.condonation_approved
                        FROM enrollments e
                        JOIN courses c ON e.course_id = c.course_id
                        LEFT JOIN attendance a ON e.enrollment_id = a.enrollment_id
                        WHERE e.student_id = ? AND e.semester = ? AND e.academic_year = ?
                        """;
                List<EvaluationEngine.CourseEvaluationResult> studentCourses = new ArrayList<>();
                boolean hasPendingEse = false;
                boolean hasSa = false;
                boolean hasAb = false;
                boolean hasRa = false;

                try (PreparedStatement ps = con.prepareStatement(enrollSql)) {
                    ps.setInt(1, sc.studentId);
                    ps.setInt(2, sc.semester);
                    ps.setString(3, sc.academicYear);

                    try (ResultSet rs = ps.executeQuery()) {
                        while (rs.next()) {
                            int enId = rs.getInt("enrollment_id");
                            String cTypeStr = rs.getString("course_type");
                            int credits = rs.getInt("credits");
                            int held = rs.getInt("classes_held");
                            boolean heldNull = rs.wasNull();
                            int att = rs.getInt("classes_attended");
                            boolean attNull = rs.wasNull();
                            boolean condoned = rs.getBoolean("condonation_approved");

                            EvaluationEngine.AttendanceEvaluation attEval = EvaluationEngine.evaluateAttendance(
                                    heldNull ? 0 : held, attNull ? 0 : att, condoned
                            );

                            double c1 = 0, c2 = 0, c3 = 0;
                            int ciaCount = 0;
                            Double rawEse = null;
                            boolean isAbsentEse = false;

                            try (PreparedStatement psM = con.prepareStatement("SELECT assessment_name, max_marks, marks_obtained, is_absent FROM assessment_marks WHERE enrollment_id = ?")) {
                                psM.setInt(1, enId);
                                try (ResultSet rsM = psM.executeQuery()) {
                                    while (rsM.next()) {
                                        String aName = rsM.getString("assessment_name");
                                        double obt = rsM.getDouble("marks_obtained");
                                        double max = rsM.getDouble("max_marks");
                                        boolean abs = rsM.getBoolean("is_absent");

                                        String norm = (aName != null) ? aName.replaceAll("[^A-Za-z0-9]", "").toUpperCase() : "";
                                        if (norm.contains("CIA1") || norm.contains("INTERNAL1") || norm.equals("TEST1")) {
                                            c1 = (max > 0 && max != 100) ? (obt / max) * 100.0 : obt;
                                            ciaCount++;
                                        } else if (norm.contains("CIA2") || norm.contains("INTERNAL2") || norm.equals("TEST2")) {
                                            c2 = (max > 0 && max != 100) ? (obt / max) * 100.0 : obt;
                                            ciaCount++;
                                        } else if (norm.contains("CIA3") || norm.contains("INTERNAL3") || norm.equals("TEST3") || norm.contains("ASSIGNMENT") || norm.contains("QUIZ")) {
                                            c3 = (max > 0 && max != 100) ? (obt / max) * 100.0 : obt;
                                            ciaCount++;
                                        } else if (norm.contains("ESE") || norm.contains("ENDSEMESTER") || norm.contains("SEMESTEREXAM")) {
                                            isAbsentEse = abs;
                                            rawEse = isAbsentEse ? 0.0 : obt;
                                        }
                                    }
                                }
                            }

                            double internal100 = (ciaCount > 0) ? ((c1 + c2 + c3) / Math.max(ciaCount, 3)) : 0.0;
                            EvaluationEngine.CourseType cType = EvaluationEngine.CourseType.fromString(cTypeStr);
                            EvaluationEngine.CourseEvaluationResult res = EvaluationEngine.evaluateCourse(
                                    cType, credits, internal100, rawEse, isAbsentEse, attEval
                            );

                            studentCourses.add(res);

                            if (rawEse == null) hasPendingEse = true;
                            if (res.isAttendanceShortage) hasSa = true;
                            if (res.isAbsentEse) hasAb = true;
                            if (!res.isPassed && rawEse != null && !res.isAttendanceShortage && !res.isAbsentEse) hasRa = true;
                        }
                    }
                }

                if (studentCourses.isEmpty()) {
                    continue;
                }

                if (hasPendingEse) {
                    incompleteStudents++;
                } else {
                    if (hasSa || hasAb) {
                        saAbStudents++;
                    } else if (hasRa) {
                        failedStudents++;
                    } else {
                        passedStudents++;
                    }
                    double sgpa = EvaluationEngine.calculateSGPA(studentCourses);
                    completedSgpas.add(sgpa);
                }
            }

            StringBuilder courseSql = new StringBuilder("""
                    SELECT c.course_id, c.course_code, c.course_name, c.course_type, c.credits,
                           COUNT(e.enrollment_id) as enrolled_count
                    FROM enrollments e
                    JOIN courses c ON e.course_id = c.course_id
                    JOIN students s ON e.student_id = s.student_id
                    WHERE 1=1
                    """);
            if (dept != null && !dept.equals("All Departments")) {
                courseSql.append(" AND s.department = '").append(dept.replace("'", "''")).append("'");
            }
            if (semIndex > 0) {
                courseSql.append(" AND e.semester = ").append(semIndex);
            }
            if (year != null && !year.equals("All Years")) {
                courseSql.append(" AND e.academic_year = '").append(year.replace("'", "''")).append("'");
            }
            courseSql.append(" GROUP BY c.course_id, c.course_code, c.course_name, c.course_type, c.credits ORDER BY c.course_code");

            try (Statement st = con.createStatement();
                 ResultSet rs = st.executeQuery(courseSql.toString())) {
                while (rs.next()) {
                    int cId = rs.getInt("course_id");
                    String code = rs.getString("course_code");
                    String name = rs.getString("course_name");
                    String type = rs.getString("course_type");
                    int credits = rs.getInt("credits");
                    int enrolled = rs.getInt("enrolled_count");

                    int cPassed = 0;
                    int cFailed = 0;
                    int cSaAb = 0;
                    int cIncomplete = 0;

                    StringBuilder cEnSql = new StringBuilder("""
                            SELECT e.enrollment_id, a.classes_held, a.classes_attended, a.condonation_approved
                            FROM enrollments e
                            JOIN students s ON e.student_id = s.student_id
                            LEFT JOIN attendance a ON e.enrollment_id = a.enrollment_id
                            WHERE e.course_id = ?
                            """);
                    if (dept != null && !dept.equals("All Departments")) {
                        cEnSql.append(" AND s.department = '").append(dept.replace("'", "''")).append("'");
                    }
                    if (semIndex > 0) {
                        cEnSql.append(" AND e.semester = ").append(semIndex);
                    }
                    if (year != null && !year.equals("All Years")) {
                        cEnSql.append(" AND e.academic_year = '").append(year.replace("'", "''")).append("'");
                    }

                    try (PreparedStatement psC = con.prepareStatement(cEnSql.toString())) {
                        psC.setInt(1, cId);
                        try (ResultSet rsC = psC.executeQuery()) {
                            while (rsC.next()) {
                                int enId = rsC.getInt("enrollment_id");
                                int held = rsC.getInt("classes_held");
                                boolean heldNull = rsC.wasNull();
                                int att = rsC.getInt("classes_attended");
                                boolean attNull = rsC.wasNull();
                                boolean condoned = rsC.getBoolean("condonation_approved");

                                EvaluationEngine.AttendanceEvaluation attEval = EvaluationEngine.evaluateAttendance(
                                        heldNull ? 0 : held, attNull ? 0 : att, condoned
                                );

                                double c1 = 0, c2 = 0, c3 = 0;
                                int ciaCount = 0;
                                Double rawEse = null;
                                boolean isAbsentEse = false;

                                try (PreparedStatement psM = con.prepareStatement("SELECT assessment_name, max_marks, marks_obtained, is_absent FROM assessment_marks WHERE enrollment_id = ?")) {
                                    psM.setInt(1, enId);
                                    try (ResultSet rsM = psM.executeQuery()) {
                                        while (rsM.next()) {
                                            String aName = rsM.getString("assessment_name");
                                            double obt = rsM.getDouble("marks_obtained");
                                            double max = rsM.getDouble("max_marks");
                                            boolean abs = rsM.getBoolean("is_absent");

                                            String norm = (aName != null) ? aName.replaceAll("[^A-Za-z0-9]", "").toUpperCase() : "";
                                            if (norm.contains("CIA1") || norm.contains("INTERNAL1") || norm.equals("TEST1")) {
                                                c1 = (max > 0 && max != 100) ? (obt / max) * 100.0 : obt;
                                                ciaCount++;
                                            } else if (norm.contains("CIA2") || norm.contains("INTERNAL2") || norm.equals("TEST2")) {
                                                c2 = (max > 0 && max != 100) ? (obt / max) * 100.0 : obt;
                                                ciaCount++;
                                            } else if (norm.contains("CIA3") || norm.contains("INTERNAL3") || norm.equals("TEST3") || norm.contains("ASSIGNMENT") || norm.contains("QUIZ")) {
                                                c3 = (max > 0 && max != 100) ? (obt / max) * 100.0 : obt;
                                                ciaCount++;
                                            } else if (norm.contains("ESE") || norm.contains("ENDSEMESTER") || norm.contains("SEMESTEREXAM")) {
                                                isAbsentEse = abs;
                                                rawEse = isAbsentEse ? 0.0 : obt;
                                            }
                                        }
                                    }
                                }

                                double internal100 = (ciaCount > 0) ? ((c1 + c2 + c3) / Math.max(ciaCount, 3)) : 0.0;
                                EvaluationEngine.CourseType cType = EvaluationEngine.CourseType.fromString(type);
                                EvaluationEngine.CourseEvaluationResult res = EvaluationEngine.evaluateCourse(
                                        cType, credits, internal100, rawEse, isAbsentEse, attEval
                                );

                                if (rawEse == null) {
                                    cIncomplete++;
                                } else if (res.isPassed) {
                                    cPassed++;
                                } else if (res.isAttendanceShortage || res.isAbsentEse) {
                                    cSaAb++;
                                } else {
                                    cFailed++;
                                }
                            }
                        }
                    }

                    int evaluatedCount = cPassed + cFailed + cSaAb;
                    double passRate = (evaluatedCount > 0) ? ((double) cPassed * 100.0 / (double) evaluatedCount) : 0.0;

                    model.addRow(new Object[]{
                            code, name, type, credits, enrolled, cPassed, cFailed, cSaAb, cIncomplete,
                            String.format("%.1f%%", passRate)
                    });
                }
            }

        } catch (SQLException ex) {
            databaseError(ex);
            return;
        }

        analyticsEnrolledLabel.setText(String.valueOf(totalStudents));
        analyticsPassedLabel.setText(String.valueOf(passedStudents));
        analyticsFailedLabel.setText(String.valueOf(failedStudents));
        analyticsSaAbLabel.setText(String.valueOf(saAbStudents));
        analyticsIncompleteLabel.setText(String.valueOf(incompleteStudents));

        int completedStudents = passedStudents + failedStudents + saAbStudents;
        double overallPassRate = (completedStudents > 0) ? ((double) passedStudents * 100.0 / (double) completedStudents) : 0.0;
        analyticsPassRateLabel.setText(String.format("%.1f%%", overallPassRate));

        if (!completedSgpas.isEmpty()) {
            double sumSgpa = 0.0;
            for (Double s : completedSgpas) sumSgpa += s;
            double avgSgpa = sumSgpa / completedSgpas.size();
            analyticsAvgSgpaLabel.setText(String.format("%.2f (N=%d)", avgSgpa, completedSgpas.size()));
        } else {
            analyticsAvgSgpaLabel.setText("-- (N=0)");
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

    public static void showLogin() {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception ignored) {
            }
            AuthDialog authDialog = new AuthDialog(null);
            authDialog.setVisible(true);
            UserSession session = authDialog.getAuthenticatedSession();
            if (session != null) {
                new KECGui(session);
            } else {
                System.exit(0);
            }
        });
    }

    public static void main(String[] args) {
        showLogin();
    }
}
