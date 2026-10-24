import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.sql.*;

public class MarksManagement extends JFrame {

    private static final String DEFAULT_DB_URL =
            "jdbc:mysql://127.0.0.1:3306/kec_academic";
    private static final String DEFAULT_DB_USER = "root";
    private static final String DEFAULT_DB_PASSWORD = "Vickyy@07";

    // Design System Color Palette (KEC Branding)
    public static final Color COLOR_NAVY = KECTheme.DEEP_NAVY;
    public static final Color COLOR_MUTED_BLUE = KECTheme.KEC_CYAN;
    public static final Color COLOR_WHITE = KECTheme.WHITE;
    public static final Color COLOR_BG = KECTheme.BG_LIGHT;
    public static final Color COLOR_BORDER = KECTheme.BORDER_GREY;
    public static final Color COLOR_TEXT_DARK = KECTheme.TEXT_DARK;
    public static final Color COLOR_TEXT_MUTED = KECTheme.TEXT_MUTED;

    // Status colors
    public static final Color COLOR_GREEN = new Color(16, 124, 65);       // #107C41
    public static final Color COLOR_GREEN_BG = new Color(227, 245, 233);
    public static final Color COLOR_AMBER = new Color(178, 89, 0);        // #B25900
    public static final Color COLOR_AMBER_BG = new Color(254, 243, 214);
    public static final Color COLOR_RED = new Color(197, 34, 31);         // #C5221F
    public static final Color COLOR_RED_BG = new Color(252, 232, 230);

    // Typography
    public static final Font FONT_TITLE = new Font("Segoe UI", Font.BOLD, 22);
    public static final Font FONT_SUBTITLE = new Font("Segoe UI", Font.PLAIN, 13);
    public static final Font FONT_SECTION = new Font("Segoe UI", Font.BOLD, 15);
    public static final Font FONT_BOLD = new Font("Segoe UI", Font.BOLD, 14);
    public static final Font FONT_REGULAR = new Font("Segoe UI", Font.PLAIN, 14);
    public static final Font FONT_SMALL = new Font("Segoe UI", Font.PLAIN, 12);

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

    private final JComboBox<EnrollmentItem> enrollmentBox = new JComboBox<>();

    // Assessment inputs
    private final JTextField cia1Field = new JTextField("0", 8);
    private final JTextField cia2Field = new JTextField("0", 8);
    private final JTextField cia3Field = new JTextField("0", 8);
    private final JTextField eseField = new JTextField("0", 8);
    private final JCheckBox absentEseBox = new JCheckBox("Student Absent in ESE (AB)");

    // Attendance inputs
    private final JTextField classesHeldField = new JTextField("45", 6);
    private final JTextField classesAttendedField = new JTextField("40", 6);
    private final JCheckBox condonationBox = new JCheckBox("Medical Condonation Approved");

    // Dynamic labels & badges
    private final JLabel courseTypeBadge = new JLabel("  THEORY COURSE (40% CIA / 60% ESE)  ");
    private final JLabel attendanceBadge = new JLabel("  88.9% — ELIGIBLE  ");
    private final JLabel eseFieldLabel = new JLabel("End Semester Exam - ESE (out of 60):");
    private final JLabel cia1Label = new JLabel("CIA Test 1 (out of 100):");
    private final JLabel cia2Label = new JLabel("CIA Test 2 (out of 100):");
    private final JLabel cia3Label = new JLabel("Assignment / Quiz / Lab (out of 100):");

    // Summary cards
    private final JLabel internalTitleLabel = new JLabel("INTERNAL MARKS");
    private final JLabel internalValueLabel = new JLabel("0.00 / 40.00");
    private final JLabel internalSubLabel = new JLabel("Scaled to 40%");

    private final JLabel eseTitleLabel = new JLabel("END SEMESTER");
    private final JLabel eseValueLabel = new JLabel("0.00 / 60.00");
    private final JLabel eseSubLabel = new JLabel("Min passing: 27/60");

    private final JLabel totalValueLabel = new JLabel("0.00 / 100");
    private final JLabel gradeBadgeLabel = new JLabel("PENDING");

    private final JLabel statusLabel = new JLabel("Ready");
    private boolean loadingEnrollments = false;
    private final UserSession currentSession;
    private JButton saveButton;

    public MarksManagement() {
        this(null);
    }

    public MarksManagement(UserSession session) {
        this.currentSession = session;
        setTitle("KEC ACADEMIX | Student Academic Evaluation (R2024)");
        setSize(1020, 800);
        setMinimumSize(new Dimension(920, 720));
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        buildUI();
        loadEnrollments();

        if (currentSession != null && currentSession.isStudent()) {
            cia1Field.setEditable(false);
            cia2Field.setEditable(false);
            cia3Field.setEditable(false);
            eseField.setEditable(false);
            classesHeldField.setEditable(false);
            classesAttendedField.setEditable(false);
            condonationBox.setEnabled(false);
            absentEseBox.setEnabled(false);
            if (saveButton != null) {
                saveButton.setEnabled(false);
                saveButton.setToolTipText("Students cannot modify assessment evaluation marks.");
            }
        }

        setVisible(true);
    }

    private Connection connect() throws SQLException {
        return DriverManager.getConnection(getDbUrl(), getDbUser(), getDbPassword());
    }

    private void buildUI() {
        JPanel root = new JPanel(new BorderLayout(0, 16));
        root.setBackground(COLOR_BG);
        root.setBorder(new EmptyBorder(18, 24, 18, 24));
        setContentPane(root);

        // Header Banner with Official KEC Logo
        JPanel header = new JPanel(new BorderLayout(14, 6));
        header.setOpaque(false);

        ImageIcon logoIcon = KECTheme.getKECLogo(150, 48);
        if (logoIcon != null) {
            header.add(new JLabel(logoIcon), BorderLayout.WEST);
        }

        JPanel titleBlock = new JPanel();
        titleBlock.setLayout(new BoxLayout(titleBlock, BoxLayout.Y_AXIS));
        titleBlock.setOpaque(false);

        JLabel title = new JLabel("Continuous Evaluation & Marks Management");
        title.setFont(FONT_TITLE);
        title.setForeground(COLOR_NAVY);
        title.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel subtitle = new JLabel("Regulations 2024 • Theory (40/60), Practical (60/40), Embedded (50/50) • Attendance & SGPA Engine");
        subtitle.setFont(FONT_SUBTITLE);
        subtitle.setForeground(COLOR_TEXT_MUTED);
        subtitle.setAlignmentX(Component.LEFT_ALIGNMENT);

        titleBlock.add(title);
        titleBlock.add(Box.createVerticalStrut(4));
        titleBlock.add(subtitle);

        String badgeText = "  EVALUATION PORTAL  ";
        if (currentSession != null) {
            badgeText = "  " + currentSession.getUsername() + " (" + currentSession.getRole() + ")  ";
        }
        JLabel badge = new JLabel(badgeText);
        badge.setOpaque(true);
        badge.setBackground(KECTheme.KEC_CYAN_LIGHT);
        badge.setForeground(KECTheme.DEEP_NAVY);
        badge.setFont(new Font("Segoe UI", Font.BOLD, 12));
        badge.setBorder(new EmptyBorder(8, 14, 8, 14));

        header.add(titleBlock, BorderLayout.CENTER);
        header.add(badge, BorderLayout.EAST);
        root.add(header, BorderLayout.NORTH);

        // Center Content: Scrollable Form Panel
        JPanel mainCard = new JPanel(new GridBagLayout());
        mainCard.setBackground(COLOR_WHITE);
        mainCard.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(COLOR_BORDER, 1, true),
                new EmptyBorder(18, 22, 18, 22)
        ));

        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(6, 6, 6, 6);
        g.fill = GridBagConstraints.HORIZONTAL;
        g.anchor = GridBagConstraints.WEST;

        // Section 1: Course Selection & Type Banner
        JLabel sectionTitle = new JLabel("1. STUDENT ENROLLMENT & COURSE INFORMATION");
        sectionTitle.setFont(FONT_SECTION);
        sectionTitle.setForeground(COLOR_NAVY);
        g.gridx = 0;
        g.gridy = 0;
        g.gridwidth = 2;
        mainCard.add(sectionTitle, g);

        styleControl(enrollmentBox, 38);
        enrollmentBox.addActionListener(e -> {
            if (!loadingEnrollments) {
                loadMarksForSelectedEnrollment();
            }
        });

        g.gridy = 1;
        g.gridwidth = 2;
        mainCard.add(enrollmentBox, g);

        // Badges Row
        JPanel badgesPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        badgesPanel.setOpaque(false);

        courseTypeBadge.setOpaque(true);
        courseTypeBadge.setFont(new Font("Segoe UI", Font.BOLD, 11));
        courseTypeBadge.setBackground(KECTheme.KEC_CYAN_LIGHT);
        courseTypeBadge.setForeground(COLOR_NAVY);
        courseTypeBadge.setBorder(new CompoundBorder(new LineBorder(KECTheme.KEC_CYAN, 1, true), new EmptyBorder(4, 10, 4, 10)));

        attendanceBadge.setOpaque(true);
        attendanceBadge.setFont(new Font("Segoe UI", Font.BOLD, 11));
        attendanceBadge.setBackground(COLOR_GREEN_BG);
        attendanceBadge.setForeground(COLOR_GREEN);
        attendanceBadge.setBorder(new CompoundBorder(new LineBorder(COLOR_GREEN, 1, true), new EmptyBorder(4, 10, 4, 10)));

        badgesPanel.add(courseTypeBadge);
        badgesPanel.add(attendanceBadge);

        g.gridy = 2;
        g.gridwidth = 2;
        mainCard.add(badgesPanel, g);

        // Section 2: Attendance Tracking & Condonation
        g.gridy = 3;
        g.gridwidth = 2;
        mainCard.add(Box.createVerticalStrut(6), g);

        g.gridy = 4;
        g.gridwidth = 2;
        JLabel attTitle = new JLabel("2. ATTENDANCE VERIFICATION & CONDONATION (R2024)");
        attTitle.setFont(FONT_SECTION);
        attTitle.setForeground(COLOR_NAVY);
        mainCard.add(attTitle, g);

        JPanel attPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 14, 0));
        attPanel.setOpaque(false);

        styleControl(classesHeldField, 36);
        styleControl(classesAttendedField, 36);

        JLabel heldLbl = new JLabel("Classes Held:");
        heldLbl.setFont(FONT_BOLD);
        heldLbl.setForeground(COLOR_TEXT_DARK);
        attPanel.add(heldLbl);
        attPanel.add(classesHeldField);

        JLabel attLbl = new JLabel("Classes Attended:");
        attLbl.setFont(FONT_BOLD);
        attLbl.setForeground(COLOR_TEXT_DARK);
        attPanel.add(attLbl);
        attPanel.add(classesAttendedField);

        condonationBox.setFont(FONT_BOLD);
        condonationBox.setForeground(COLOR_NAVY);
        condonationBox.setOpaque(false);
        attPanel.add(condonationBox);

        g.gridy = 5;
        g.gridwidth = 2;
        mainCard.add(attPanel, g);

        // Listener for live attendance changes
        classesHeldField.addActionListener(e -> calculate());
        classesAttendedField.addActionListener(e -> calculate());
        condonationBox.addActionListener(e -> calculate());

        // Section 3: Assessment Marks Entry
        g.gridy = 6;
        g.gridwidth = 2;
        mainCard.add(Box.createVerticalStrut(6), g);

        g.gridy = 7;
        g.gridwidth = 2;
        JLabel marksTitle = new JLabel("3. CONTINUOUS INTERNAL ASSESSMENT (CIA) & END SEMESTER EXAM (ESE)");
        marksTitle.setFont(FONT_SECTION);
        marksTitle.setForeground(COLOR_NAVY);
        mainCard.add(marksTitle, g);

        styleControl(cia1Field, 36);
        styleControl(cia2Field, 36);
        styleControl(cia3Field, 36);
        styleControl(eseField, 36);

        addRow(mainCard, g, 8, cia1Label, cia1Field);
        addRow(mainCard, g, 9, cia2Label, cia2Field);
        addRow(mainCard, g, 10, cia3Label, cia3Field);

        // ESE row with Absent checkbox
        JPanel eseContainer = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        eseContainer.setOpaque(false);
        eseContainer.add(eseField);

        absentEseBox.setFont(FONT_BOLD);
        absentEseBox.setForeground(COLOR_RED);
        absentEseBox.setOpaque(false);
        absentEseBox.addActionListener(e -> {
            eseField.setEnabled(!absentEseBox.isSelected());
            if (absentEseBox.isSelected()) {
                eseField.setText("0");
            }
            calculate();
        });
        eseContainer.add(absentEseBox);

        addRow(mainCard, g, 11, eseFieldLabel, eseContainer);

        // Section 4: Real-time Evaluation Summary Cards
        JPanel summaryGrid = new JPanel(new GridLayout(1, 4, 12, 0));
        summaryGrid.setOpaque(false);
        summaryGrid.setPreferredSize(new Dimension(0, 85));

        summaryGrid.add(createScoreCard(internalTitleLabel, internalValueLabel, internalSubLabel, KECTheme.KEC_CYAN));
        summaryGrid.add(createScoreCard(eseTitleLabel, eseValueLabel, eseSubLabel, KECTheme.KEC_CYAN));
        summaryGrid.add(createScoreCard(new JLabel("TOTAL MARKS (100M)"), totalValueLabel, new JLabel("Min passing: 50.0/100"), KECTheme.DEEP_NAVY));
        summaryGrid.add(createGradeCard("EVALUATION RESULT", gradeBadgeLabel));

        g.gridx = 0;
        g.gridy = 12;
        g.gridwidth = 2;
        g.insets = new Insets(14, 6, 4, 6);
        mainCard.add(summaryGrid, g);

        JScrollPane scrollPane = new JScrollPane(mainCard);
        scrollPane.setBorder(null);
        scrollPane.getViewport().setBackground(COLOR_BG);
        root.add(scrollPane, BorderLayout.CENTER);

        // Bottom Action Toolbar & Status
        JButton refreshButton = KECTheme.createSecondaryButton("Refresh Enrollments");
        refreshButton.setPreferredSize(new Dimension(170, 42));
        refreshButton.addActionListener(e -> loadEnrollments());

        JButton calculateButton = KECTheme.createPrimaryButton("Calculate Marks");
        calculateButton.setPreferredSize(new Dimension(150, 42));
        calculateButton.addActionListener(e -> calculate());

        saveButton = KECTheme.createSuccessButton("Save Marks & Attendance");
        saveButton.setPreferredSize(new Dimension(210, 42));
        saveButton.addActionListener(e -> saveMarks());

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        buttons.setOpaque(false);
        buttons.add(refreshButton);
        buttons.add(calculateButton);
        buttons.add(saveButton);

        statusLabel.setFont(FONT_REGULAR);
        statusLabel.setForeground(COLOR_TEXT_MUTED);

        JPanel footer = new JPanel(new BorderLayout(10, 8));
        footer.setOpaque(false);
        footer.add(statusLabel, BorderLayout.WEST);
        footer.add(buttons, BorderLayout.EAST);
        root.add(footer, BorderLayout.SOUTH);
    }

    private JPanel createScoreCard(JLabel titleLabel, JLabel valueLabel, JLabel subtitleLabel, Color color) {
        JPanel p = new JPanel(new BorderLayout(4, 3));
        p.setBackground(COLOR_WHITE);
        p.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(COLOR_BORDER, 1, true),
                new EmptyBorder(10, 14, 10, 14)
        ));

        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 11));
        titleLabel.setForeground(COLOR_TEXT_MUTED);

        valueLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        valueLabel.setForeground(color);

        subtitleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        subtitleLabel.setForeground(COLOR_TEXT_MUTED);

        p.add(titleLabel, BorderLayout.NORTH);
        p.add(valueLabel, BorderLayout.CENTER);
        p.add(subtitleLabel, BorderLayout.SOUTH);
        return p;
    }

    private JPanel createGradeCard(String title, JLabel badgeLabel) {
        JPanel p = new JPanel(new BorderLayout(4, 3));
        p.setBackground(COLOR_WHITE);
        p.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(COLOR_BORDER, 1, true),
                new EmptyBorder(10, 14, 10, 14)
        ));

        JLabel t = new JLabel(title);
        t.setFont(new Font("Segoe UI", Font.BOLD, 11));
        t.setForeground(COLOR_TEXT_MUTED);

        badgeLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        badgeLabel.setHorizontalAlignment(SwingConstants.CENTER);
        badgeLabel.setOpaque(true);
        badgeLabel.setBackground(KECTheme.BG_LIGHT);
        badgeLabel.setForeground(COLOR_TEXT_MUTED);
        badgeLabel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(COLOR_BORDER, 1, true),
                new EmptyBorder(4, 8, 4, 8)
        ));

        p.add(t, BorderLayout.NORTH);
        p.add(badgeLabel, BorderLayout.CENTER);
        return p;
    }

    private void addRow(JPanel panel, GridBagConstraints g, int row, JLabel label, JComponent component) {
        g.gridy = row;
        g.gridwidth = 1;
        g.gridx = 0;
        g.weightx = 0.38;

        label.setForeground(COLOR_TEXT_DARK);
        label.setFont(FONT_BOLD);
        panel.add(label, g);

        g.gridx = 1;
        g.weightx = 0.62;
        panel.add(component, g);
    }

    private void styleControl(JComponent comp, int height) {
        if (comp instanceof JTextField) {
            KECTheme.styleTextField((JTextField) comp, height);
        } else if (comp instanceof JComboBox) {
            KECTheme.styleComboBox((JComboBox<?>) comp, height);
        } else {
            comp.setFont(FONT_REGULAR);
            comp.setForeground(COLOR_TEXT_DARK);
            comp.setBackground(COLOR_WHITE);
            comp.setPreferredSize(new Dimension(380, height));
        }
    }

    // ------------------ Data Loading & Calculations ------------------

    private void loadEnrollments() {
        loadingEnrollments = true;
        enrollmentBox.removeAllItems();

        boolean isStudent = (currentSession != null && currentSession.isStudent());
        String sql = """
                SELECT e.enrollment_id, s.roll_no, s.student_name,
                       c.course_code, c.course_name, c.credits, c.course_type
                FROM enrollments e
                JOIN students s ON s.student_id = e.student_id
                JOIN courses c ON c.course_id = e.course_id
                """ + (isStudent ? " WHERE e.student_id = ? " : "") +
                """
                ORDER BY s.roll_no, c.course_code
                """;

        try (Connection con = connect();
             PreparedStatement ps = con.prepareStatement(sql)) {

            if (isStudent) {
                ps.setInt(1, currentSession.getStudentId());
            }

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String rawType = rs.getString("course_type");
                    EvaluationEngine.CourseType type = EvaluationEngine.CourseType.fromString(rawType);

                    enrollmentBox.addItem(new EnrollmentItem(
                            rs.getInt("enrollment_id"),
                            rs.getString("roll_no"),
                            rs.getString("student_name"),
                            rs.getString("course_code"),
                            rs.getString("course_name"),
                            rs.getInt("credits"),
                            type
                    ));
                }
            }

            statusLabel.setText("Loaded " + enrollmentBox.getItemCount() + " student enrollments from database");

        } catch (SQLException ex) {
            showDatabaseError(ex);
        } finally {
            loadingEnrollments = false;
        }

        if (enrollmentBox.getItemCount() > 0) {
            loadMarksForSelectedEnrollment();
        } else {
            resetSummary();
        }
    }

    private void loadMarksForSelectedEnrollment() {
        EnrollmentItem item = (EnrollmentItem) enrollmentBox.getSelectedItem();
        if (item == null) {
            resetSummary();
            return;
        }

        // Update labels for course type
        EvaluationEngine.CourseType type = item.courseType;
        updateCourseTypeUI(type);

        cia1Field.setText("0");
        cia2Field.setText("0");
        cia3Field.setText("0");
        eseField.setText("0");
        absentEseBox.setSelected(false);
        eseField.setEnabled(true);

        // Load Attendance
        String attSql = "SELECT classes_held, classes_attended, condonation_approved FROM attendance WHERE enrollment_id = ?";
        try (Connection con = connect();
             PreparedStatement ps = con.prepareStatement(attSql)) {
            ps.setInt(1, item.enrollmentId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    classesHeldField.setText(String.valueOf(rs.getInt("classes_held")));
                    classesAttendedField.setText(String.valueOf(rs.getInt("classes_attended")));
                    condonationBox.setSelected(rs.getBoolean("condonation_approved"));
                } else {
                    classesHeldField.setText("45");
                    classesAttendedField.setText("40");
                    condonationBox.setSelected(false);
                }
            }
        } catch (SQLException ex) {
            // Default attendance
            classesHeldField.setText("45");
            classesAttendedField.setText("40");
            condonationBox.setSelected(false);
        }

        // Load Assessment Marks
        String sql = """
                SELECT assessment_name, marks_obtained, max_marks, is_absent
                FROM assessment_marks
                WHERE enrollment_id = ?
                """;

        try (Connection con = connect();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, item.enrollmentId);

            try (ResultSet rs = ps.executeQuery()) {
                boolean hasAny = false;
                while (rs.next()) {
                    hasAny = true;
                    String rawName = rs.getString("assessment_name");
                    String name = rawName != null ? rawName.replaceAll("[^A-Za-z0-9]", "").toUpperCase() : "";
                    double marks = rs.getDouble("marks_obtained");
                    double max = rs.getDouble("max_marks");
                    boolean absent = rs.getBoolean("is_absent");

                    double scaled = marks;
                    if (name.contains("CIA1") || name.contains("INTERNALASSESSMENT1") || name.equals("TEST1") || name.contains("LABEXP")) {
                        if (max > 0 && max != 100) scaled = (marks / max) * 100.0;
                        cia1Field.setText(formatMark(scaled));
                    } else if (name.contains("CIA2") || name.contains("INTERNALASSESSMENT2") || name.equals("TEST2") || name.contains("MODELLAB")) {
                        if (max > 0 && max != 100) scaled = (marks / max) * 100.0;
                        cia2Field.setText(formatMark(scaled));
                    } else if (name.contains("CIA3") || name.contains("ASSIGNMENT") || name.contains("QUIZ") || name.contains("FORMATIVE")) {
                        if (max > 0 && max != 100) scaled = (marks / max) * 100.0;
                        cia3Field.setText(formatMark(scaled));
                    } else if (name.contains("ESE") || name.contains("ENDSEMESTER")) {
                        if (absent) {
                            absentEseBox.setSelected(true);
                            eseField.setEnabled(false);
                            eseField.setText("0");
                        } else {
                            absentEseBox.setSelected(false);
                            eseField.setEnabled(true);
                            eseField.setText(formatMark(marks));
                        }
                    }
                }

                calculate();

            }
        } catch (SQLException ex) {
            showDatabaseError(ex);
        }
    }

    private void updateCourseTypeUI(EvaluationEngine.CourseType type) {
        courseTypeBadge.setText("  " + type.displayName.toUpperCase() + " (" + (int)(type.internalRatio * 100) + "% CIA / " + (int)(type.eseRatio * 100) + "% ESE)  ");
        eseFieldLabel.setText(String.format("End Semester Exam - ESE (out of %.0f):", type.maxEse));
        internalTitleLabel.setText(String.format("INTERNAL (%.0fM)", type.maxInternal));
        eseTitleLabel.setText(String.format("END SEMESTER (%.0fM)", type.maxEse));
        internalSubLabel.setText(String.format("Scale: %.0f%%", type.internalRatio * 100));
        eseSubLabel.setText(String.format("Min passing: %.1f / %.0f", type.minEseThreshold, type.maxEse));

        if (type == EvaluationEngine.CourseType.PRACTICAL) {
            cia1Label.setText("Continuous Lab Experiments (out of 100):");
            cia2Label.setText("Model Lab Examination (out of 100):");
            cia3Label.setText("Viva / Lab Record / Observation (out of 100):");
        } else if (type == EvaluationEngine.CourseType.EMBEDDED) {
            cia1Label.setText("Theory Continuous Assessment (out of 100):");
            cia2Label.setText("Lab Continuous Assessment (out of 100):");
            cia3Label.setText("Integrated Mini-Project / Assignment (out of 100):");
        } else {
            cia1Label.setText("Continuous Assessment 1 - CIA 1 (out of 100):");
            cia2Label.setText("Continuous Assessment 2 - CIA 2 (out of 100):");
            cia3Label.setText("Formative Assessment / Assignment / Quiz (out of 100):");
        }
    }

    private String formatMark(double mark) {
        if (mark == (long) mark) {
            return String.valueOf((long) mark);
        }
        return String.format("%.2f", mark);
    }

    private void resetSummary() {
        internalValueLabel.setText("0.00 / 40.00");
        eseValueLabel.setText("0.00 / 60.00");
        totalValueLabel.setText("0.00 / 100");
        gradeBadgeLabel.setText("PENDING");
        gradeBadgeLabel.setBackground(COLOR_WHITE);
        gradeBadgeLabel.setForeground(COLOR_TEXT_MUTED);
    }

    private double readMark(JTextField field, double maximum, String name) {
        double value;
        try {
            value = Double.parseDouble(field.getText().trim());
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException(name + " must be a valid number.");
        }

        if (!Double.isFinite(value) || value < 0 || value > maximum) {
            throw new IllegalArgumentException(name + " must be between 0 and " + maximum + ".");
        }

        return value;
    }

    private EvaluationEngine.CourseEvaluationResult calculate() {
        EnrollmentItem item = (EnrollmentItem) enrollmentBox.getSelectedItem();
        if (item == null) {
            resetSummary();
            return null;
        }

        try {
            // Read Attendance
            int held = Integer.parseInt(classesHeldField.getText().trim());
            int attended = Integer.parseInt(classesAttendedField.getText().trim());
            boolean condoned = condonationBox.isSelected();
            EvaluationEngine.AttendanceEvaluation att = EvaluationEngine.evaluateAttendance(held, attended, condoned);

            // Update attendance badge
            attendanceBadge.setText(String.format("  %.1f%% — %s  ", att.percentage, att.status.description.toUpperCase()));
            if (att.status == EvaluationEngine.AttendanceStatus.ELIGIBLE || att.status == EvaluationEngine.AttendanceStatus.ELIGIBLE_CONDONED) {
                attendanceBadge.setBackground(COLOR_GREEN_BG);
                attendanceBadge.setForeground(COLOR_GREEN);
            } else if (att.status == EvaluationEngine.AttendanceStatus.CONDONATION_REQUIRED) {
                attendanceBadge.setBackground(COLOR_AMBER_BG);
                attendanceBadge.setForeground(COLOR_AMBER);
            } else {
                attendanceBadge.setBackground(COLOR_RED_BG);
                attendanceBadge.setForeground(COLOR_RED);
            }

            // Read Marks
            double c1 = readMark(cia1Field, 100, cia1Label.getText());
            double c2 = readMark(cia2Field, 100, cia2Label.getText());
            double c3 = readMark(cia3Field, 100, cia3Label.getText());
            double internal100 = (c1 + c2 + c3) / 3.0;

            boolean isAbsentEse = absentEseBox.isSelected();
            Double rawEse = isAbsentEse ? 0.0 : readMark(eseField, item.courseType.maxEse, "ESE Score");

            // Execute evaluation engine
            EvaluationEngine.CourseEvaluationResult res = EvaluationEngine.evaluateCourse(
                    item.courseType, item.credits, internal100, rawEse, isAbsentEse, att
            );

            internalValueLabel.setText(String.format("%.2f / %.2f", res.scaledInternal, res.maxInternalScale));
            eseValueLabel.setText(isAbsentEse ? "ABSENT (AB)" : String.format("%.2f / %.2f", res.scaledEse, res.maxEseScale));
            totalValueLabel.setText(String.format("%.2f / 100.00", res.totalMarks));

            if (res.isPassed) {
                gradeBadgeLabel.setText(String.format("PASS — Grade %s (GP %d)", res.grade, res.gradePoint));
                gradeBadgeLabel.setBackground(COLOR_GREEN_BG);
                gradeBadgeLabel.setForeground(COLOR_GREEN);
            } else if (res.isAttendanceShortage) {
                gradeBadgeLabel.setText("DETAINED — Grade SA (GP 0)");
                gradeBadgeLabel.setBackground(COLOR_RED_BG);
                gradeBadgeLabel.setForeground(COLOR_RED);
            } else if (res.isAbsentEse) {
                gradeBadgeLabel.setText("ABSENT — Grade AB (GP 0)");
                gradeBadgeLabel.setBackground(COLOR_RED_BG);
                gradeBadgeLabel.setForeground(COLOR_RED);
            } else {
                gradeBadgeLabel.setText(String.format("FAIL — Grade RA (GP 0)"));
                gradeBadgeLabel.setBackground(COLOR_RED_BG);
                gradeBadgeLabel.setForeground(COLOR_RED);
            }

            statusLabel.setText("Calculated via R2024 Evaluation Engine: " + res.resultStatus);
            return res;

        } catch (IllegalArgumentException ex) {
            statusLabel.setText("Input validation: " + ex.getMessage());
            return null;
        }
    }

    private void saveMarks() {
        if (currentSession != null && !currentSession.canManageMarks()) {
            JOptionPane.showMessageDialog(this,
                    "Unauthorized: Student accounts are not permitted to save or modify assessment marks.",
                    "Access Denied", JOptionPane.ERROR_MESSAGE);
            throw new SecurityException("Unauthorized marks modification attempt by user: " + currentSession.getUsername());
        }

        EnrollmentItem item = (EnrollmentItem) enrollmentBox.getSelectedItem();
        if (item == null) {
            JOptionPane.showMessageDialog(this, "Please select an enrolled student and course.",
                    "Selection Required", JOptionPane.WARNING_MESSAGE);
            return;
        }

        EvaluationEngine.CourseEvaluationResult res = calculate();
        if (res == null) {
            JOptionPane.showMessageDialog(this, "Please correct mark entries before saving.",
                    "Invalid Entry", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(
                this,
                String.format("Save R2024 Evaluation for:\n\nStudent: %s (%s)\nCourse: %s - %s (%s)\n\n"
                                + "Attendance: %.1f%% (%s)\nScaled CIA: %.2f / %.0f\nESE: %s / %.0f\nTotal: %.2f / 100\nGrade: %s (GP %d) — %s\n\nProceed to save to database?",
                        item.studentName, item.rollNo, item.courseCode, item.courseName, item.courseType.displayName,
                        res.attendance.percentage, res.attendance.status.description,
                        res.scaledInternal, res.maxInternalScale,
                        res.isAbsentEse ? "ABSENT" : String.format("%.2f", res.scaledEse),
                        res.maxEseScale, res.totalMarks, res.grade, res.gradePoint, res.resultStatus),
                "Confirm Save Assessment",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE
        );
        if (confirm != JOptionPane.YES_OPTION) return;

        double c1 = Double.parseDouble(cia1Field.getText().trim());
        double c2 = Double.parseDouble(cia2Field.getText().trim());
        double c3 = Double.parseDouble(cia3Field.getText().trim());
        double ese = res.isAbsentEse ? 0.0 : Double.parseDouble(eseField.getText().trim());
        boolean isAbsent = res.isAbsentEse;

        String[] names = {"CIA1", "CIA2", "CIA3", "ESE"};
        double[] values = {c1, c2, c3, ese};
        double[] maxima = {100, 100, 100, item.courseType.maxEse};
        boolean[] absentFlags = {false, false, false, isAbsent};

        try (Connection con = connect()) {
            con.setAutoCommit(false);
            try {
                // 1. Save or update Attendance
                int held = Integer.parseInt(classesHeldField.getText().trim());
                int attended = Integer.parseInt(classesAttendedField.getText().trim());
                boolean condoned = condonationBox.isSelected();

                String attUpsert = """
                        INSERT INTO attendance (enrollment_id, classes_held, classes_attended, condonation_approved)
                        VALUES (?, ?, ?, ?)
                        ON DUPLICATE KEY UPDATE
                            classes_held = VALUES(classes_held),
                            classes_attended = VALUES(classes_attended),
                            condonation_approved = VALUES(condonation_approved)
                        """;
                try (PreparedStatement psAtt = con.prepareStatement(attUpsert)) {
                    psAtt.setInt(1, item.enrollmentId);
                    psAtt.setInt(2, held);
                    psAtt.setInt(3, attended);
                    psAtt.setBoolean(4, condoned);
                    psAtt.executeUpdate();
                }

                // 2. Save or update Assessment Marks
                String findSql = "SELECT mark_id FROM assessment_marks WHERE enrollment_id = ? AND assessment_name = ?";
                String updateSql = "UPDATE assessment_marks SET max_marks = ?, marks_obtained = ?, is_absent = ? WHERE mark_id = ?";
                String insertSql = "INSERT INTO assessment_marks (enrollment_id, assessment_name, max_marks, marks_obtained, is_absent) VALUES (?, ?, ?, ?, ?)";

                for (int i = 0; i < names.length; i++) {
                    Integer markId = null;
                    try (PreparedStatement find = con.prepareStatement(findSql)) {
                        find.setInt(1, item.enrollmentId);
                        find.setString(2, names[i]);
                        try (ResultSet rs = find.executeQuery()) {
                            if (rs.next()) {
                                markId = rs.getInt("mark_id");
                            }
                        }
                    }

                    if (markId != null) {
                        try (PreparedStatement update = con.prepareStatement(updateSql)) {
                            update.setDouble(1, maxima[i]);
                            update.setDouble(2, values[i]);
                            update.setBoolean(3, absentFlags[i]);
                            update.setInt(4, markId);
                            update.executeUpdate();
                        }
                    } else {
                        try (PreparedStatement insert = con.prepareStatement(insertSql)) {
                            insert.setInt(1, item.enrollmentId);
                            insert.setString(2, names[i]);
                            insert.setDouble(3, maxima[i]);
                            insert.setDouble(4, values[i]);
                            insert.setBoolean(5, absentFlags[i]);
                            insert.executeUpdate();
                        }
                    }
                }

                con.commit();
                statusLabel.setText("Assessment evaluation & attendance saved successfully to MySQL database");

                JOptionPane.showMessageDialog(this,
                        String.format("Marks & Attendance saved for %s (%s)\nCourse: %s (%s)\nGrade: %s (GP %d) • %s",
                                item.studentName, item.rollNo, item.courseCode, item.courseType.displayName,
                                res.grade, res.gradePoint, res.resultStatus),
                        "Evaluation Saved", JOptionPane.INFORMATION_MESSAGE);

            } catch (SQLException ex) {
                con.rollback();
                throw ex;
            } finally {
                con.setAutoCommit(true);
            }

        } catch (SQLException ex) {
            showDatabaseError(ex);
        }
    }

    private void showDatabaseError(SQLException ex) {
        String msg = ex.getMessage();
        String pass = getDbPassword();
        if (pass != null && !pass.isEmpty()) {
            msg = msg.replace(pass, "******");
        }

        JOptionPane.showMessageDialog(this,
                "Could not access MySQL database 'kec_academic'.\n\nDetails:\n" + msg,
                "Database Error", JOptionPane.ERROR_MESSAGE);
    }

    public static class EnrollmentItem {
        public final int enrollmentId;
        public final String rollNo;
        public final String studentName;
        public final String courseCode;
        public final String courseName;
        public final int credits;
        public final EvaluationEngine.CourseType courseType;

        public EnrollmentItem(int enrollmentId, String rollNo, String studentName,
                              String courseCode, String courseName, int credits,
                              EvaluationEngine.CourseType courseType) {
            this.enrollmentId = enrollmentId;
            this.rollNo = rollNo;
            this.studentName = studentName;
            this.courseCode = courseCode;
            this.courseName = courseName;
            this.credits = credits;
            this.courseType = courseType;
        }

        @Override
        public String toString() {
            return rollNo + " | " + studentName + " | " + courseCode + " - " + courseName + " (" + credits + " cr, " + courseType.displayName + ")";
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception ignored) {
            }
            new MarksManagement().setVisible(true);
        });
    }
}
