
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class MarksManagement extends JFrame {

    private static final String URL =
            "jdbc:mysql://127.0.0.1:3306/kec_academic";
    private static final String USER = "root";
    private static final String PASSWORD = "Vickyy@07";

    private final JComboBox<EnrollmentItem> enrollmentBox =
            new JComboBox<>();

    private final JTextField cia1Field = new JTextField("0", 8);
    private final JTextField cia2Field = new JTextField("0", 8);
    private final JTextField cia3Field = new JTextField("0", 8);
    private final JTextField eseField = new JTextField("0", 8);

    private final JLabel internalLabel = new JLabel("Internal: 0 / 40");
    private final JLabel totalLabel = new JLabel("Total: 0 / 100");
    private final JLabel gradeLabel = new JLabel("Grade: -");
    private final JLabel resultLabel = new JLabel("Result: -");
    private final JLabel statusLabel = new JLabel("Ready");

    private final Color navy = new Color(23, 43, 77);
    private final Color blue = new Color(53, 92, 138);


    private boolean loadingEnrollments = false;

   public MarksManagement() {
    setTitle("KEC ACADEMIX | Marks Management");
    setSize(760, 600);
    setLocationRelativeTo(null);
    setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

    buildUI();
    loadEnrollments();

    setVisible(true);
}

   
private void buildUI() {
    setSize(900, 680);
    setMinimumSize(new Dimension(820, 620));

    Color bg = new Color(246, 248, 251);
    Color border = new Color(227, 232, 239);
    Color textColor = new Color(35, 48, 68);

    JPanel root = new JPanel(new BorderLayout(0, 22));
    root.setBorder(new EmptyBorder(24, 28, 22, 28));
    root.setBackground(bg);
    setContentPane(root);

    // Header
    JPanel header = new JPanel(new BorderLayout(10, 8));
    header.setOpaque(false);

    JPanel headingPanel = new JPanel();
    headingPanel.setLayout(new BoxLayout(headingPanel, BoxLayout.Y_AXIS));
    headingPanel.setOpaque(false);

    JLabel title = new JLabel("Marks Management");
    title.setFont(new Font("SansSerif", Font.BOLD, 27));
    title.setForeground(navy);
    title.setAlignmentX(Component.LEFT_ALIGNMENT);

    JLabel subtitle = new JLabel(
            "Student academic evaluation and grade calculation");
    subtitle.setFont(new Font("SansSerif", Font.PLAIN, 14));
    subtitle.setForeground(new Color(100, 112, 130));
    subtitle.setAlignmentX(Component.LEFT_ALIGNMENT);

    headingPanel.add(title);
    headingPanel.add(Box.createVerticalStrut(7));
    headingPanel.add(subtitle);

    JLabel badge = new JLabel("  KEC ACADEMIX  ");
    badge.setOpaque(true);
    badge.setBackground(navy);
    badge.setForeground(Color.WHITE);
    badge.setFont(new Font("SansSerif", Font.BOLD, 12));
    badge.setBorder(new EmptyBorder(10, 12, 10, 12));

    header.add(headingPanel, BorderLayout.CENTER);
    header.add(badge, BorderLayout.EAST);
    root.add(header, BorderLayout.NORTH);

    // Main card
    JPanel card = new JPanel(new GridBagLayout());
    card.setBackground(Color.WHITE);
    card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(border),
            new EmptyBorder(24, 26, 24, 26)));

    GridBagConstraints g = new GridBagConstraints();
    g.insets = new Insets(10, 8, 10, 8);
    g.fill = GridBagConstraints.HORIZONTAL;
    g.anchor = GridBagConstraints.WEST;

    JLabel section = new JLabel("ASSESSMENT DETAILS");
    section.setFont(new Font("SansSerif", Font.BOLD, 13));
    section.setForeground(blue);

    g.gridx = 0;
    g.gridy = 0;
    g.gridwidth = 2;
    card.add(section, g);

    g.gridwidth = 1;

    addRow(card, g, 1, "Student / Course", enrollmentBox);
    addRow(card, g, 2, "CIA Test 1 (out of 100)", cia1Field);
    addRow(card, g, 3, "CIA Test 2 (out of 100)", cia2Field);
    addRow(card, g, 4, "CIA Test 3 (out of 100)", cia3Field);
    addRow(card, g, 5, "ESE (out of 60)", eseField);

    // Make input controls easier to see
    enrollmentBox.setFont(new Font("SansSerif", Font.PLAIN, 14));
    enrollmentBox.setBackground(Color.WHITE);
    enrollmentBox.setPreferredSize(new Dimension(340, 38));

    for (JTextField field : new JTextField[]{
            cia1Field, cia2Field, cia3Field, eseField}) {
        field.setFont(new Font("SansSerif", Font.PLAIN, 15));
        field.setForeground(textColor);
        field.setBackground(Color.WHITE);
        field.setPreferredSize(new Dimension(340, 38));
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(195, 207, 222)),
                new EmptyBorder(5, 10, 5, 10)));
    }

    // Results panel
    JPanel summary = new JPanel(new GridLayout(2, 2, 12, 12));
    summary.setBackground(bg);
    summary.setBorder(new EmptyBorder(16, 16, 16, 16));

    for (JLabel label : new JLabel[]{
            internalLabel, totalLabel, gradeLabel, resultLabel}) {
        label.setFont(new Font("SansSerif", Font.BOLD, 15));
        label.setForeground(navy);
        summary.add(label);
    }

    g.gridx = 0;
    g.gridy = 6;
    g.gridwidth = 2;
    g.insets = new Insets(18, 8, 5, 8);
    card.add(summary, g);

    root.add(card, BorderLayout.CENTER);

    // Action buttons
    JButton refreshButton = new JButton("Refresh Students");
    JButton calculateButton = new JButton("Calculate Marks");
    JButton saveButton = new JButton("Save Marks");

    styleButton(refreshButton, new Color(232, 238, 246),
            navy);
    styleButton(calculateButton, blue, Color.WHITE);
    styleButton(saveButton, navy, Color.WHITE);

    refreshButton.addActionListener(e -> loadEnrollments());
    calculateButton.addActionListener(e -> calculate());
    saveButton.addActionListener(e -> saveMarks());
    enrollmentBox.addActionListener(e -> {
        if (!loadingEnrollments) {
            loadMarksForSelectedEnrollment();
        }
    });

    JPanel buttons = new JPanel(
            new FlowLayout(FlowLayout.RIGHT, 12, 8));
    buttons.setOpaque(false);
    buttons.add(refreshButton);
    buttons.add(calculateButton);
    buttons.add(saveButton);

    JPanel footer = new JPanel(new BorderLayout(10, 10));
    footer.setOpaque(false);

    statusLabel.setFont(new Font("SansSerif", Font.PLAIN, 13));
    statusLabel.setForeground(new Color(80, 95, 115));

    footer.add(statusLabel, BorderLayout.WEST);
    footer.add(buttons, BorderLayout.EAST);
    root.add(footer, BorderLayout.SOUTH);
}


    private void addRow(JPanel panel, GridBagConstraints g,
                        int row, String label, JComponent component) {
        g.gridy = row;
        g.gridwidth = 1;
        g.gridx = 0;
        g.weightx = 0.45;

        JLabel text = new JLabel(label);
        text.setForeground(navy);
        text.setFont(new Font("SansSerif", Font.PLAIN, 14));
        panel.add(text, g);

        g.gridx = 1;
        g.weightx = 0.55;
        panel.add(component, g);
    }

   
private void styleButton(JButton button, Color background,
                         Color foreground) {
    button.setFont(new Font("SansSerif", Font.BOLD, 13));
    button.setBackground(background);
    button.setForeground(foreground);
    button.setOpaque(true);
    button.setContentAreaFilled(true);
    button.setBorderPainted(false);
    button.setFocusPainted(false);
    button.setBorder(new EmptyBorder(12, 18, 12, 18));
    button.setPreferredSize(new Dimension(155, 44));
    button.setCursor(Cursor.getPredefinedCursor(
            Cursor.HAND_CURSOR));
}


    private Connection connect() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }

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

            statusLabel.setText(
                    "Loaded " + enrollmentBox.getItemCount()
                            + " enrollments");

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
        internalLabel.setText("Internal: 0.00 / 40");
        totalLabel.setText("Total: 0.00 / 100");
        gradeLabel.setText("Grade: -");
        resultLabel.setText("Result: -");
    }

    private double readMark(JTextField field, double maximum,
                            String name) {
        double value;

        try {
            value = Double.parseDouble(field.getText().trim());
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException(
                    name + " must be a valid number.");
        }

        if (!Double.isFinite(value) || value < 0 || value > maximum) {
            throw new IllegalArgumentException(
                    name + " must be between 0 and " + maximum + ".");
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

            internalLabel.setText(String.format(
                    "Internal: %.2f / 40", m[4]));
            totalLabel.setText(String.format(
                    "Total: %.2f / 100", m[5]));
            gradeLabel.setText("Grade: " + grade);
            resultLabel.setText(
                    "Result: " + (pass ? "PASS" : "FAIL"));

            statusLabel.setText("Calculation complete");
        } catch (IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(),
                    "Invalid Marks", JOptionPane.WARNING_MESSAGE);
        }
    }

    private void saveMarks() {
        EnrollmentItem item =
                (EnrollmentItem) enrollmentBox.getSelectedItem();

        if (item == null) {
            JOptionPane.showMessageDialog(this,
                    "Please select an enrolled student and course.");
            return;
        }

        final double[] m;
        try {
            m = readMarks();
        } catch (IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(),
                    "Invalid Marks", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String[] names = {"CIA1", "CIA2", "CIA3", "ESE"};
        double[] values = {m[0], m[1], m[2], m[3]};
        double[] maxima = {100, 100, 100, 60};

        String findSql = """
                SELECT mark_id FROM assessment_marks
                WHERE enrollment_id = ? AND assessment_name = ?
                """;

        String updateSql = """
                UPDATE assessment_marks
                SET max_marks = ?, marks_obtained = ?
                WHERE mark_id = ?
                """;

        String insertSql = """
                INSERT INTO assessment_marks
                    (enrollment_id, assessment_name,
                     max_marks, marks_obtained)
                VALUES (?, ?, ?, ?)
                """;

        try (Connection con = connect()) {
            con.setAutoCommit(false);

            try {
                for (int i = 0; i < names.length; i++) {
                    Integer markId = null;

                    try (PreparedStatement find =
                                 con.prepareStatement(findSql)) {
                        find.setInt(1, item.enrollmentId);
                        find.setString(2, names[i]);

                        try (ResultSet rs = find.executeQuery()) {
                            if (rs.next()) {
                                markId = rs.getInt("mark_id");
                            }
                        }
                    }

                    if (markId != null) {
                        try (PreparedStatement update =
                                     con.prepareStatement(updateSql)) {
                            update.setDouble(1, maxima[i]);
                            update.setDouble(2, values[i]);
                            update.setInt(3, markId);
                            update.executeUpdate();
                        }
                    } else {
                        try (PreparedStatement insert =
                                     con.prepareStatement(insertSql)) {
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
                statusLabel.setText("Marks saved successfully");

                JOptionPane.showMessageDialog(this,
                        "Marks saved for " + item.rollNo
                                + " - " + item.studentName
                                + "\nCourse: " + item.courseCode,
                        "Saved", JOptionPane.INFORMATION_MESSAGE);

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
        ex.printStackTrace();

        JOptionPane.showMessageDialog(this,
                "Could not access MySQL.\n"
                        + "Check the server, JDBC driver, username "
                        + "and password.\n\n"
                        + "Details: " + ex.getMessage(),
                "Database Error", JOptionPane.ERROR_MESSAGE);
    }

    private static class EnrollmentItem {
        final int enrollmentId;
        final String rollNo;
        final String studentName;
        final String courseCode;
        final String courseName;
        final int credits;

        EnrollmentItem(int enrollmentId, String rollNo,
                       String studentName, String courseCode,
                       String courseName, int credits) {
            this.enrollmentId = enrollmentId;
            this.rollNo = rollNo;
            this.studentName = studentName;
            this.courseCode = courseCode;
            this.courseName = courseName;
            this.credits = credits;
        }

        @Override
        public String toString() {
            return rollNo + " | " + studentName + " | "
                    + courseCode + " - " + courseName;
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() ->
                new MarksManagement().setVisible(true));
    }
}
