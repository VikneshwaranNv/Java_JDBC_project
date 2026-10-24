import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class MarksManagement extends JFrame {

    private static final String DEFAULT_DB_URL =
            "jdbc:mysql://127.0.0.1:3306/kec_academic";
    private static final String DEFAULT_DB_USER = "root";
    private static final String DEFAULT_DB_PASSWORD = "Vickyy@07";

    // Design System Color Palette (User specified)
    public static final Color COLOR_NAVY = new Color(23, 43, 77);         // #172B4D
    public static final Color COLOR_MUTED_BLUE = new Color(53, 92, 138);   // #355C8A
    public static final Color COLOR_WHITE = Color.WHITE;                  // #FFFFFF
    public static final Color COLOR_BG = new Color(246, 248, 251);         // #F6F8FB
    public static final Color COLOR_BORDER = new Color(227, 232, 239);     // #E3E8EF
    public static final Color COLOR_TEXT_DARK = new Color(23, 43, 77);
    public static final Color COLOR_TEXT_MUTED = new Color(100, 115, 135);

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

    private final JTextField cia1Field = new JTextField("0", 10);
    private final JTextField cia2Field = new JTextField("0", 10);
    private final JTextField cia3Field = new JTextField("0", 10);
    private final JTextField eseField = new JTextField("0", 10);

    private final JLabel internalValueLabel = new JLabel("0.00 / 40");
    private final JLabel eseValueLabel = new JLabel("0.00 / 60");
    private final JLabel totalValueLabel = new JLabel("0.00 / 100");
    private final JLabel gradeBadgeLabel = new JLabel("PENDING");

    private final JLabel statusLabel = new JLabel("Ready");
    private boolean loadingEnrollments = false;

    public MarksManagement() {
        setTitle("KEC ACADEMIX | Student Marks & Continuous Internal Evaluation");
        setSize(980, 720);
        setMinimumSize(new Dimension(880, 640));
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        buildUI();
        loadEnrollments();

        setVisible(true);
    }

    private Connection connect() throws SQLException {
        return DriverManager.getConnection(getDbUrl(), getDbUser(), getDbPassword());
    }

    private void buildUI() {
        JPanel root = new JPanel(new BorderLayout(0, 16));
        root.setBackground(COLOR_BG);
        root.setBorder(new EmptyBorder(20, 24, 18, 24));
        setContentPane(root);

        // Header Banner
        JPanel header = new JPanel(new BorderLayout(10, 6));
        header.setOpaque(false);

        JPanel titleBlock = new JPanel();
        titleBlock.setLayout(new BoxLayout(titleBlock, BoxLayout.Y_AXIS));
        titleBlock.setOpaque(false);

        JLabel title = new JLabel("Student Marks & Academic Evaluation");
        title.setFont(FONT_TITLE);
        title.setForeground(COLOR_NAVY);
        title.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel subtitle = new JLabel("Continuous Internal Assessment (CIA: 40% weightage) & End Semester Examination (ESE: 60% weightage)");
        subtitle.setFont(FONT_SUBTITLE);
        subtitle.setForeground(COLOR_TEXT_MUTED);
        subtitle.setAlignmentX(Component.LEFT_ALIGNMENT);

        titleBlock.add(title);
        titleBlock.add(Box.createVerticalStrut(4));
        titleBlock.add(subtitle);

        JLabel badge = new JLabel("  ACADEMIC PORTAL  ");
        badge.setOpaque(true);
        badge.setBackground(COLOR_NAVY);
        badge.setForeground(COLOR_WHITE);
        badge.setFont(new Font("Segoe UI", Font.BOLD, 12));
        badge.setBorder(new EmptyBorder(8, 14, 8, 14));

        header.add(titleBlock, BorderLayout.CENTER);
        header.add(badge, BorderLayout.EAST);
        root.add(header, BorderLayout.NORTH);

        // Center Content: Assessment Card
        JPanel card = new JPanel(new GridBagLayout());
        card.setBackground(COLOR_WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(COLOR_BORDER, 1),
                new EmptyBorder(20, 24, 20, 24)
        ));

        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(8, 8, 8, 8);
        g.fill = GridBagConstraints.HORIZONTAL;
        g.anchor = GridBagConstraints.WEST;

        JLabel sectionTitle = new JLabel("ASSESSMENT DETAILS & MARKS ENTRY");
        sectionTitle.setFont(FONT_SECTION);
        sectionTitle.setForeground(COLOR_NAVY);
        g.gridx = 0;
        g.gridy = 0;
        g.gridwidth = 2;
        card.add(sectionTitle, g);

        JLabel sectionSub = new JLabel("Select an enrolled student and enter assessment scores");
        sectionSub.setFont(FONT_SMALL);
        sectionSub.setForeground(COLOR_TEXT_MUTED);
        g.gridy = 1;
        card.add(sectionSub, g);

        g.gridwidth = 1;

        styleControl(enrollmentBox, 38);
        enrollmentBox.addActionListener(e -> {
            if (!loadingEnrollments) {
                loadMarksForSelectedEnrollment();
            }
        });

        styleControl(cia1Field, 38);
        styleControl(cia2Field, 38);
        styleControl(cia3Field, 38);
        styleControl(eseField, 38);

        addRow(card, g, 2, "Student / Course Enrollment:", enrollmentBox);
        addRow(card, g, 3, "CIA Test 1 Score (out of 100):", cia1Field);
        addRow(card, g, 4, "CIA Test 2 Score (out of 100):", cia2Field);
        addRow(card, g, 5, "CIA Test 3 Score (out of 100):", cia3Field);
        addRow(card, g, 6, "End Semester Exam - ESE (out of 60):", eseField);

        // Summary Cards Grid (Internal, ESE, Total, Result & Grade)
        JPanel summaryGrid = new JPanel(new GridLayout(1, 4, 12, 0));
        summaryGrid.setOpaque(false);
        summaryGrid.setPreferredSize(new Dimension(0, 80));

        summaryGrid.add(createScoreCard("INTERNAL (40M)", internalValueLabel, "Avg of CIA tests (40%)", COLOR_MUTED_BLUE));
        summaryGrid.add(createScoreCard("END SEMESTER (60M)", eseValueLabel, "Min passing: 27/60", COLOR_MUTED_BLUE));
        summaryGrid.add(createScoreCard("TOTAL MARKS (100M)", totalValueLabel, "Min passing: 50/100", COLOR_NAVY));
        summaryGrid.add(createGradeCard("EVALUATION RESULT", gradeBadgeLabel));

        g.gridx = 0;
        g.gridy = 7;
        g.gridwidth = 2;
        g.insets = new Insets(16, 8, 4, 8);
        card.add(summaryGrid, g);

        root.add(card, BorderLayout.CENTER);

        // Bottom Action Toolbar & Status
        JButton refreshButton = new JButton("Refresh Enrollments");
        styleButton(refreshButton, COLOR_WHITE, COLOR_NAVY, 13);
        refreshButton.setBorder(BorderFactory.createLineBorder(COLOR_BORDER, 1));
        refreshButton.setPreferredSize(new Dimension(170, 42));
        refreshButton.addActionListener(e -> loadEnrollments());

        JButton calculateButton = new JButton("Calculate Marks");
        styleButton(calculateButton, COLOR_MUTED_BLUE, COLOR_WHITE, 14);
        calculateButton.setPreferredSize(new Dimension(150, 42));
        calculateButton.addActionListener(e -> calculate());

        JButton saveButton = new JButton("Save Marks to Database");
        styleButton(saveButton, COLOR_GREEN, COLOR_WHITE, 14);
        saveButton.setPreferredSize(new Dimension(200, 42));
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

    private JPanel createScoreCard(String title, JLabel valueLabel, String subtitle, Color color) {
        JPanel p = new JPanel(new BorderLayout(4, 3));
        p.setBackground(COLOR_BG);
        p.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(COLOR_BORDER, 1),
                new EmptyBorder(8, 12, 8, 12)
        ));

        JLabel t = new JLabel(title);
        t.setFont(new Font("Segoe UI", Font.BOLD, 11));
        t.setForeground(COLOR_TEXT_MUTED);

        valueLabel.setFont(new Font("Segoe UI", Font.BOLD, 17));
        valueLabel.setForeground(color);

        JLabel sub = new JLabel(subtitle);
        sub.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        sub.setForeground(COLOR_TEXT_MUTED);

        p.add(t, BorderLayout.NORTH);
        p.add(valueLabel, BorderLayout.CENTER);
        p.add(sub, BorderLayout.SOUTH);
        return p;
    }

    private JPanel createGradeCard(String title, JLabel badgeLabel) {
        JPanel p = new JPanel(new BorderLayout(4, 3));
        p.setBackground(COLOR_BG);
        p.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(COLOR_BORDER, 1),
                new EmptyBorder(8, 12, 8, 12)
        ));

        JLabel t = new JLabel(title);
        t.setFont(new Font("Segoe UI", Font.BOLD, 11));
        t.setForeground(COLOR_TEXT_MUTED);

        badgeLabel.setFont(new Font("Segoe UI", Font.BOLD, 15));
        badgeLabel.setHorizontalAlignment(SwingConstants.CENTER);
        badgeLabel.setOpaque(true);
        badgeLabel.setBackground(COLOR_WHITE);
        badgeLabel.setForeground(COLOR_TEXT_MUTED);
        badgeLabel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(COLOR_BORDER, 1),
                new EmptyBorder(4, 8, 4, 8)
        ));

        p.add(t, BorderLayout.NORTH);
        p.add(badgeLabel, BorderLayout.CENTER);
        return p;
    }

    private void addRow(JPanel panel, GridBagConstraints g, int row, String label, JComponent component) {
        g.gridy = row;
        g.gridwidth = 1;
        g.gridx = 0;
        g.weightx = 0.38;

        JLabel text = new JLabel(label);
        text.setForeground(COLOR_TEXT_DARK);
        text.setFont(FONT_BOLD);
        panel.add(text, g);

        g.gridx = 1;
        g.weightx = 0.62;
        panel.add(component, g);
    }

    private void styleControl(JComponent comp, int height) {
        comp.setFont(FONT_REGULAR);
        comp.setForeground(COLOR_TEXT_DARK);
        comp.setBackground(COLOR_WHITE);
        comp.setPreferredSize(new Dimension(380, height));
        if (comp instanceof JTextField) {
            ((JTextField) comp).setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(new Color(195, 207, 222), 1),
                    new EmptyBorder(4, 10, 4, 10)
            ));
        }
    }

    private void styleButton(JButton btn, Color bg, Color fg, int fontSize) {
        btn.setFont(new Font("Segoe UI", Font.BOLD, fontSize));
        btn.setBackground(bg);
        btn.setForeground(fg);
        btn.setOpaque(true);
        btn.setContentAreaFilled(true);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    }

    // ------------------ Data Loading & Calculations ------------------

    private void loadEnrollments() {
        loadingEnrollments = true;
        enrollmentBox.removeAllItems();

        String sql = """
                SELECT e.enrollment_id, s.roll_no, s.student_name,
                       c.course_code, c.course_name, c.credits
                FROM enrollments e
                JOIN students s ON s.student_id = e.student_id
                JOIN courses c ON c.course_id = e.course_id
                ORDER BY s.roll_no, c.course_code
                """;

        try (Connection con = connect();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                enrollmentBox.addItem(new EnrollmentItem(
                        rs.getInt("enrollment_id"),
                        rs.getString("roll_no"),
                        rs.getString("student_name"),
                        rs.getString("course_code"),
                        rs.getString("course_name"),
                        rs.getInt("credits")
                ));
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

        cia1Field.setText("0");
        cia2Field.setText("0");
        cia3Field.setText("0");
        eseField.setText("0");

        String sql = """
                SELECT assessment_name, marks_obtained, max_marks
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

                    double scaled = marks;
                    if (name.contains("CIA1") || name.contains("INTERNALASSESSMENT1") || name.equals("TEST1")) {
                        if (max > 0 && max != 100) scaled = (marks / max) * 100.0;
                        cia1Field.setText(formatMark(scaled));
                    } else if (name.contains("CIA2") || name.contains("INTERNALASSESSMENT2") || name.equals("TEST2")) {
                        if (max > 0 && max != 100) scaled = (marks / max) * 100.0;
                        cia2Field.setText(formatMark(scaled));
                    } else if (name.contains("CIA3") || name.contains("INTERNALASSESSMENT3") || name.equals("TEST3")) {
                        if (max > 0 && max != 100) scaled = (marks / max) * 100.0;
                        cia3Field.setText(formatMark(scaled));
                    } else if (name.contains("ESE") || name.contains("ENDSEMESTER")) {
                        if (max > 0 && max != 60) scaled = (marks / max) * 60.0;
                        eseField.setText(formatMark(scaled));
                    }
                }
                if (hasAny) {
                    calculate();
                } else {
                    resetSummary();
                }
            }
        } catch (SQLException ex) {
            showDatabaseError(ex);
        }
    }

    private String formatMark(double mark) {
        if (mark == (long) mark) {
            return String.valueOf((long) mark);
        }
        return String.format("%.2f", mark);
    }

    private void resetSummary() {
        internalValueLabel.setText("0.00 / 40");
        eseValueLabel.setText("0.00 / 60");
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

    private double[] readMarks() {
        double a = readMark(cia1Field, 100, "CIA Test 1");
        double b = readMark(cia2Field, 100, "CIA Test 2");
        double c = readMark(cia3Field, 100, "CIA Test 3");
        double ese = readMark(eseField, 60, "ESE");

        // Average CIA out of 100, scaled to an internal maximum of 40.
        double internal = ((a + b + c) / 3.0) * 0.40;
        double total = internal + ese;

        return new double[]{a, b, c, ese, internal, total};
    }

    private String gradeFor(double total, boolean pass) {
        if (!pass) return "RA";
        if (total >= 90) return "O";
        if (total >= 80) return "A+";
        if (total >= 70) return "A";
        if (total >= 60) return "B+";
        if (total >= 50) return "B";
        return "RA";
    }

    private void calculate() {
        try {
            double[] m = readMarks();
            boolean pass = m[3] >= 27 && m[5] >= 50;
            String grade = gradeFor(m[5], pass);

            internalValueLabel.setText(String.format("%.2f / 40", m[4]));
            eseValueLabel.setText(String.format("%.2f / 60", m[3]));
            totalValueLabel.setText(String.format("%.2f / 100", m[5]));

            if (pass) {
                gradeBadgeLabel.setText("PASS — Grade " + grade);
                gradeBadgeLabel.setBackground(COLOR_GREEN_BG);
                gradeBadgeLabel.setForeground(COLOR_GREEN);
            } else {
                gradeBadgeLabel.setText("FAIL — Grade RA");
                gradeBadgeLabel.setBackground(COLOR_RED_BG);
                gradeBadgeLabel.setForeground(COLOR_RED);
            }

            statusLabel.setText("Marks calculation complete");
        } catch (IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(),
                    "Invalid Marks Entry", JOptionPane.WARNING_MESSAGE);
        }
    }

    private void saveMarks() {
        EnrollmentItem item = (EnrollmentItem) enrollmentBox.getSelectedItem();
        if (item == null) {
            JOptionPane.showMessageDialog(this, "Please select an enrolled student and course.",
                    "Selection Required", JOptionPane.WARNING_MESSAGE);
            return;
        }

        final double[] m;
        try {
            m = readMarks();
        } catch (IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(),
                    "Invalid Marks Entry", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(
                this,
                String.format("Save assessment evaluation marks for:\n\nStudent: %s (%s)\nCourse: %s - %s\n\nCIA 1: %.1f\nCIA 2: %.1f\nCIA 3: %.1f\nESE: %.1f\nTotal: %.2f / 100\n\nProceed?",
                        item.studentName, item.rollNo, item.courseCode, item.courseName, m[0], m[1], m[2], m[3], m[5]),
                "Confirm Save Marks",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE
        );
        if (confirm != JOptionPane.YES_OPTION) return;

        String[] names = {"CIA1", "CIA2", "CIA3", "ESE"};
        double[] values = {m[0], m[1], m[2], m[3]};
        double[] maxima = {100, 100, 100, 60};

        String findSql = "SELECT mark_id FROM assessment_marks WHERE enrollment_id = ? AND assessment_name = ?";
        String updateSql = "UPDATE assessment_marks SET max_marks = ?, marks_obtained = ? WHERE mark_id = ?";
        String insertSql = "INSERT INTO assessment_marks (enrollment_id, assessment_name, max_marks, marks_obtained) VALUES (?, ?, ?, ?)";

        try (Connection con = connect()) {
            con.setAutoCommit(false);
            try {
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
                            update.setInt(3, markId);
                            update.executeUpdate();
                        }
                    } else {
                        try (PreparedStatement insert = con.prepareStatement(insertSql)) {
                            insert.setInt(1, item.enrollmentId);
                            insert.setString(2, names[i]);
                            insert.setDouble(3, maxima[i]);
                            insert.setDouble(4, values[i]);
                            insert.executeUpdate();
                        }
                    }
                }

                con.commit();
                calculate();
                statusLabel.setText("Marks saved successfully to MySQL database");

                JOptionPane.showMessageDialog(this,
                        String.format("Marks successfully saved for %s (%s)\nCourse: %s\nTotal Score: %.2f / 100",
                                item.studentName, item.rollNo, item.courseCode, m[5]),
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

    private static class EnrollmentItem {
        final int enrollmentId;
        final String rollNo;
        final String studentName;
        final String courseCode;
        final String courseName;
        final int credits;

        EnrollmentItem(int enrollmentId, String rollNo, String studentName,
                       String courseCode, String courseName, int credits) {
            this.enrollmentId = enrollmentId;
            this.rollNo = rollNo;
            this.studentName = studentName;
            this.courseCode = courseCode;
            this.courseName = courseName;
            this.credits = credits;
        }

        @Override
        public String toString() {
            return rollNo + " | " + studentName + " | " + courseCode + " - " + courseName + " (" + credits + " credits)";
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
