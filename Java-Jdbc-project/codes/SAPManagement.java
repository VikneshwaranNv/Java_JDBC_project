import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.io.File;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.security.MessageDigest;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;

public class SAPManagement extends JFrame {

    // Database configuration
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
    public static final Color COLOR_GRAY = new Color(94, 108, 132);
    public static final Color COLOR_GRAY_BG = new Color(235, 236, 240);

    // Typography (14-16px equivalent for body, larger bold for titles)
    public static final Font FONT_TITLE = new Font("Segoe UI", Font.BOLD, 22);
    public static final Font FONT_SUBTITLE = new Font("Segoe UI", Font.PLAIN, 13);
    public static final Font FONT_SECTION = new Font("Segoe UI", Font.BOLD, 15);
    public static final Font FONT_BOLD = new Font("Segoe UI", Font.BOLD, 14);
    public static final Font FONT_REGULAR = new Font("Segoe UI", Font.PLAIN, 14);
    public static final Font FONT_SMALL = new Font("Segoe UI", Font.PLAIN, 12);
    public static final Font FONT_MONO = new Font("Consolas", Font.PLAIN, 13);

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

    public static class StudentItem {
        final int studentId;
        final String rollNo;
        final String studentName;

        public StudentItem(int studentId, String rollNo, String studentName) {
            this.studentId = studentId;
            this.rollNo = rollNo;
            this.studentName = studentName;
        }

        @Override
        public String toString() {
            return rollNo + " - " + studentName;
        }
    }

    public static class CertificateAnalysisResult {
        public String extractedText = null;
        public String analysisStatus = "NOT_ANALYZED"; // NOT_ANALYZED, ANALYZED, ANALYSIS_FAILED
        public String eligibilityStatus = "UNCERTAIN"; // ELIGIBLE, INELIGIBLE, UNCERTAIN
        public String exclusionReason = null;
        public boolean duplicateFlag = false;
        public double confidence = 0.0;
        public String analysisNotes = "";
        public String detectedTitle = "";
        public String detectedOrganizer = "";
        public String detectedDate = "";
        public int recommendedPoints = 0;
    }

    // Top Summary Metric Labels
    private JLabel cardStudentValue;
    private JLabel cardStudentSub;
    private JLabel cardPointsValue;
    private JLabel cardPointsSub;
    private JLabel cardBonusValue;
    private JLabel cardBonusSub;
    private JLabel cardPendingValue;
    private JLabel cardPendingSub;
    private JLabel cardApprovedValue;
    private JLabel cardApprovedSub;

    // Section A: Submit Activity Form Fields
    private JComboBox<StudentItem> studentBox;
    private JTextField activityField;
    private JComboBox<String> typeBox;
    private JTextField dateField;
    private JTextField organizerField;
    private JTextField pointsField;
    private JTextArea descriptionArea;
    private JTextField certFileField;
    private JButton browseCertButton;
    private File chosenCertFile = null;

    // Section B: Activity Submissions Table
    private JTable activityTable;
    private DefaultTableModel tableModel;

    // Section C: Certificate Analysis & Eligibility Components
    private JLabel analysisStatusLabel;
    private JLabel eligibilityLabel;
    private JLabel confidenceLabel;
    private JLabel duplicateLabel;
    private JTextField detectedTitleField;
    private JTextField detectedOrganizerField;
    private JTextField detectedDateField;
    private JLabel exclusionReasonLabel;
    private JTextArea analysisNotesArea;
    private JTextArea extractedTextArea;

    // Section D: Staff Evaluation Action Buttons
    private JButton analyzeCertButton;
    private JButton approveButton;
    private JButton rejectButton;
    private final UserSession currentSession;

    public SAPManagement() {
        this(null);
    }

    public SAPManagement(UserSession session) {
        this.currentSession = session;
        setTitle("KEC ACADEMIX — Student Activity Points (SAP) Evaluation & Verification");
        setSize(1360, 840);
        setMinimumSize(new Dimension(1080, 720));
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        buildUI();
        loadStudentsInBackground();
        loadActivitiesInBackground();

        if (currentSession != null && currentSession.isStudent()) {
            if (studentBox != null) studentBox.setEnabled(false);
            if (approveButton != null) {
                approveButton.setEnabled(false);
                approveButton.setToolTipText("Students cannot review or approve activities.");
            }
            if (rejectButton != null) {
                rejectButton.setEnabled(false);
                rejectButton.setToolTipText("Students cannot review or reject activities.");
            }
        }

        setVisible(true);
    }

    private Connection getConnection() throws SQLException {
        return DriverManager.getConnection(
                getDbUrl(), getDbUser(), getDbPassword()
        );
    }

    private void buildUI() {
        JPanel root = new JPanel(new BorderLayout(0, 10));
        root.setBackground(COLOR_BG);
        root.setBorder(new EmptyBorder(14, 18, 14, 18));
        setContentPane(root);

        // ------------------ Top Header & Summary Cards ------------------
        JPanel topContainer = new JPanel(new BorderLayout(0, 12));
        topContainer.setOpaque(false);

        // Page Heading Banner with Official KEC Logo
        JPanel headerPanel = new JPanel(new BorderLayout(14, 5));
        headerPanel.setOpaque(false);

        ImageIcon logoIcon = KECTheme.getKECLogo(150, 48);
        if (logoIcon != null) {
            JLabel logoLbl = new JLabel(logoIcon);
            headerPanel.add(logoLbl, BorderLayout.WEST);
        }

        JPanel titleBlock = new JPanel();
        titleBlock.setLayout(new BoxLayout(titleBlock, BoxLayout.Y_AXIS));
        titleBlock.setOpaque(false);

        JLabel heading = new JLabel("Student Activity Points — Evaluation & Verification");
        heading.setFont(FONT_TITLE);
        heading.setForeground(COLOR_NAVY);
        heading.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel subHeading = new JLabel("KEC ACADEMIX | Kongu Engineering College (Autonomous) • Staff Verification & Approval Portal");
        subHeading.setFont(FONT_SUBTITLE);
        subHeading.setForeground(COLOR_TEXT_MUTED);
        subHeading.setAlignmentX(Component.LEFT_ALIGNMENT);

        titleBlock.add(heading);
        titleBlock.add(Box.createVerticalStrut(4));
        titleBlock.add(subHeading);

        String badgeText = "  SAP PORTAL  ";
        if (currentSession != null) {
            badgeText = "  " + currentSession.getUsername() + " (" + currentSession.getRole() + ")  ";
        }
        JLabel badge = new JLabel(badgeText);
        badge.setOpaque(true);
        badge.setBackground(KECTheme.KEC_CYAN_LIGHT);
        badge.setForeground(KECTheme.DEEP_NAVY);
        badge.setFont(new Font("Segoe UI", Font.BOLD, 12));
        badge.setBorder(new EmptyBorder(6, 12, 6, 12));

        headerPanel.add(titleBlock, BorderLayout.CENTER);
        headerPanel.add(badge, BorderLayout.EAST);
        topContainer.add(headerPanel, BorderLayout.NORTH);

        // Summary Metric Cards (5 Cards: Selected Student, Approved Points, Bonus Level, Pending Reviews, Approved Activities)
        JPanel summaryCardsPanel = new JPanel(new GridLayout(1, 5, 12, 0));
        summaryCardsPanel.setOpaque(false);
        summaryCardsPanel.setPreferredSize(new Dimension(0, 78));

        cardStudentValue = new JLabel("All Students");
        cardStudentSub = new JLabel("Select a student");
        summaryCardsPanel.add(createMetricCard("Selected Student", cardStudentValue, cardStudentSub, KECTheme.DEEP_NAVY));

        cardPointsValue = new JLabel("0 pts");
        cardPointsSub = new JLabel("Approved points");
        summaryCardsPanel.add(createMetricCard("Approved Points", cardPointsValue, cardPointsSub, KECTheme.KEC_CYAN));

        cardBonusValue = new JLabel("Level 0");
        cardBonusSub = new JLabel("No bonus rule matched");
        summaryCardsPanel.add(createMetricCard("Bonus Level", cardBonusValue, cardBonusSub, KECTheme.KEC_GREEN));

        cardPendingValue = new JLabel("0");
        cardPendingSub = new JLabel("Awaiting review");
        summaryCardsPanel.add(createMetricCard("Pending Reviews", cardPendingValue, cardPendingSub, KECTheme.WARNING_AMBER));

        cardApprovedValue = new JLabel("0");
        cardApprovedSub = new JLabel("Verified activities");
        summaryCardsPanel.add(createMetricCard("Approved Activities", cardApprovedValue, cardApprovedSub, KECTheme.SUCCESS_GREEN));

        topContainer.add(summaryCardsPanel, BorderLayout.SOUTH);
        root.add(topContainer, BorderLayout.NORTH);

        // ------------------ Main Workspace Layout ------------------
        // Left: Section A (Submit Activity Form)
        // Right: Split Pane with Section B (Submissions Table) + Section C (Certificate Analysis) + Section D (Staff Actions)

        JPanel formSection = buildSubmitSection();
        JScrollPane formScroll = new JScrollPane(formSection);
        formScroll.setBorder(BorderFactory.createLineBorder(COLOR_BORDER, 1));
        formScroll.getVerticalScrollBar().setUnitIncrement(14);
        formScroll.setPreferredSize(new Dimension(390, 0));

        JPanel rightContainer = buildRightWorkspace();

        JSplitPane mainSplit = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, formScroll, rightContainer);
        mainSplit.setDividerLocation(395);
        mainSplit.setResizeWeight(0.28);
        mainSplit.setBorder(null);
        mainSplit.setBackground(COLOR_BG);
        root.add(mainSplit, BorderLayout.CENTER);

        // Footer Note
        JLabel footerNote = new JLabel(
                "  Security Note: Status transitions are restricted to PENDING_REVIEW -> APPROVED / REJECTED. Automated analysis is preliminary; staff verification is required."
        );
        footerNote.setFont(FONT_SMALL);
        footerNote.setForeground(COLOR_TEXT_MUTED);
        footerNote.setBorder(new EmptyBorder(4, 8, 2, 8));
        root.add(footerNote, BorderLayout.SOUTH);
    }

    private JPanel createMetricCard(String title, JLabel valueLabel, JLabel subtitleLabel, Color accentColor) {
        JPanel card = new JPanel(new BorderLayout(4, 3));
        card.setBackground(COLOR_WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(COLOR_BORDER, 1, true),
                new EmptyBorder(10, 14, 10, 14)
        ));

        JLabel titleLabel = new JLabel(title.toUpperCase());
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 11));
        titleLabel.setForeground(COLOR_TEXT_MUTED);

        valueLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        valueLabel.setForeground(accentColor != null ? accentColor : COLOR_NAVY);

        subtitleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        subtitleLabel.setForeground(COLOR_TEXT_MUTED);

        card.add(titleLabel, BorderLayout.NORTH);
        card.add(valueLabel, BorderLayout.CENTER);
        card.add(subtitleLabel, BorderLayout.SOUTH);
        return card;
    }

    // ------------------ Section A: Submit Activity ------------------

    private JPanel buildSubmitSection() {
        JPanel form = new JPanel(new GridBagLayout());
        form.setBackground(COLOR_WHITE);
        form.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(COLOR_BORDER, 1),
                new EmptyBorder(16, 16, 16, 16)
        ));

        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(5, 4, 5, 4);
        g.fill = GridBagConstraints.HORIZONTAL;
        g.anchor = GridBagConstraints.WEST;

        int currentGridY = 0;

        // Section Title
        JLabel secTitle = new JLabel("A. SUBMIT ACTIVITY");
        secTitle.setFont(FONT_SECTION);
        secTitle.setForeground(COLOR_NAVY);
        g.gridx = 0;
        g.gridy = currentGridY++;
        g.gridwidth = 2;
        form.add(secTitle, g);

        JLabel secDesc = new JLabel("Upload student certificate and claim co-curricular SAP points");
        secDesc.setFont(FONT_SMALL);
        secDesc.setForeground(COLOR_TEXT_MUTED);
        g.gridy = currentGridY++;
        form.add(secDesc, g);

        studentBox = new JComboBox<>();
        styleControl(studentBox, 38);
        studentBox.addActionListener(e -> updateBonusLevelForSelectedStudent());

        activityField = new JTextField(18);
        styleControl(activityField, 38);

        typeBox = new JComboBox<>(new String[]{
                "TECHNICAL", "NON_TECHNICAL", "WORKSHOP", "OTHER"
        });
        styleControl(typeBox, 38);

        dateField = new JTextField(java.time.LocalDate.now().toString(), 10);
        styleControl(dateField, 38);

        organizerField = new JTextField(18);
        styleControl(organizerField, 38);

        pointsField = new JTextField("0", 6);
        styleControl(pointsField, 38);

        descriptionArea = new JTextArea(3, 18);
        descriptionArea.setFont(FONT_REGULAR);
        descriptionArea.setLineWrap(true);
        descriptionArea.setWrapStyleWord(true);
        descriptionArea.setBorder(new EmptyBorder(6, 8, 6, 8));
        JScrollPane descScroll = new JScrollPane(descriptionArea);
        descScroll.setBorder(BorderFactory.createLineBorder(new Color(195, 207, 222)));

        certFileField = new JTextField("No file chosen", 12);
        certFileField.setEditable(false);
        styleControl(certFileField, 38);

        browseCertButton = KECTheme.createSecondaryButton("Browse...");
        browseCertButton.setPreferredSize(new Dimension(95, 38));
        browseCertButton.addActionListener(e -> chooseCertificateFile());

        JPanel certPanel = new JPanel(new BorderLayout(6, 0));
        certPanel.setOpaque(false);
        certPanel.add(certFileField, BorderLayout.CENTER);
        certPanel.add(browseCertButton, BorderLayout.EAST);

        currentGridY = addFormFieldSequential(form, g, currentGridY, "Student (Roll & Name):", studentBox);
        currentGridY = addFormFieldSequential(form, g, currentGridY, "Activity Name:", activityField);
        currentGridY = addFormFieldSequential(form, g, currentGridY, "Activity Category:", typeBox);
        currentGridY = addFormFieldSequential(form, g, currentGridY, "Activity Date (YYYY-MM-DD):", dateField);
        currentGridY = addFormFieldSequential(form, g, currentGridY, "Organizer:", organizerField);
        currentGridY = addFormFieldSequential(form, g, currentGridY, "Claimed Points (0-150):", pointsField);
        currentGridY = addFormFieldSequential(form, g, currentGridY, "Certificate Document (PDF/Img):", certPanel);
        currentGridY = addFormFieldSequential(form, g, currentGridY, "Description / Details:", descScroll);

        JButton submitButton = KECTheme.createPrimaryButton("Submit for Review");
        submitButton.setPreferredSize(new Dimension(180, 42));
        submitButton.addActionListener(e -> submitActivity());

        JButton refreshButton = KECTheme.createSecondaryButton("Refresh");
        refreshButton.setPreferredSize(new Dimension(95, 42));
        refreshButton.addActionListener(e -> {
            loadStudentsInBackground();
            loadActivitiesInBackground();
        });

        JButton testDbButton = KECTheme.createSecondaryButton("Test DB Connection");
        testDbButton.setPreferredSize(new Dimension(160, 36));
        testDbButton.addActionListener(e -> testDatabaseConnection());

        JPanel actionRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 8));
        actionRow.setOpaque(false);
        actionRow.add(submitButton);
        actionRow.add(refreshButton);

        JPanel testRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        testRow.setOpaque(false);
        testRow.add(testDbButton);

        g.gridx = 0;
        g.gridwidth = 2;
        g.gridy = currentGridY++;
        form.add(Box.createVerticalStrut(8), g);

        g.gridy = currentGridY++;
        form.add(actionRow, g);

        g.gridy = currentGridY++;
        form.add(testRow, g);

        return form;
    }

    private int addFormFieldSequential(JPanel panel, GridBagConstraints g, int currentY, String label, JComponent field) {
        g.gridwidth = 2;
        g.gridx = 0;
        g.gridy = currentY++;
        g.weightx = 1.0;

        JLabel lbl = new JLabel(label);
        lbl.setFont(FONT_BOLD);
        lbl.setForeground(COLOR_TEXT_DARK);
        panel.add(lbl, g);

        g.gridy = currentY++;
        panel.add(field, g);

        return currentY;
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
            comp.setPreferredSize(new Dimension(300, height));
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

    // ------------------ Right Workspace: Section B, C, D ------------------

    private JPanel buildRightWorkspace() {
        JPanel container = new JPanel(new BorderLayout(0, 8));
        container.setOpaque(false);

        // Section B: Activity Submissions Table
        JPanel tableCard = new JPanel(new BorderLayout(0, 6));
        tableCard.setBackground(COLOR_WHITE);
        tableCard.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(COLOR_BORDER, 1),
                new EmptyBorder(10, 12, 10, 12)
        ));

        JPanel tableHeaderPanel = new JPanel(new BorderLayout());
        tableHeaderPanel.setOpaque(false);
        JLabel tableTitle = new JLabel("B. ACTIVITY SUBMISSIONS");
        tableTitle.setFont(FONT_SECTION);
        tableTitle.setForeground(COLOR_NAVY);
        JLabel tableHint = new JLabel("Select an activity row to view certificate pre-check analysis & evaluate");
        tableHint.setFont(FONT_SMALL);
        tableHint.setForeground(COLOR_TEXT_MUTED);
        tableHeaderPanel.add(tableTitle, BorderLayout.WEST);
        tableHeaderPanel.add(tableHint, BorderLayout.EAST);
        tableCard.add(tableHeaderPanel, BorderLayout.NORTH);

        tableModel = new DefaultTableModel(
                new String[]{
                        "ID", "Roll No", "Student Name", "Activity Name",
                        "Category", "Date", "Claimed", "Approved", "Status",
                        "Analysis", "Eligibility"
                }, 0
        ) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        activityTable = new JTable(tableModel);
        activityTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        KECTheme.styleTable(activityTable);

        // Column widths
        int[] widths = {45, 100, 140, 220, 100, 95, 65, 65, 125, 115, 115};
        for (int i = 0; i < widths.length && i < activityTable.getColumnCount(); i++) {
            activityTable.getColumnModel().getColumn(i).setPreferredWidth(widths[i]);
        }

        // Custom renderers for status badges
        activityTable.getColumnModel().getColumn(8).setCellRenderer(new StatusBadgeRenderer());
        activityTable.getColumnModel().getColumn(9).setCellRenderer(new AnalysisBadgeRenderer());
        activityTable.getColumnModel().getColumn(10).setCellRenderer(new EligibilityBadgeRenderer());

        activityTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                int viewRow = activityTable.getSelectedRow();
                if (viewRow >= 0) {
                    int row = activityTable.convertRowIndexToModel(viewRow);
                    int activityId = (int) tableModel.getValueAt(row, 0);
                    loadSelectedActivityDetails(activityId);
                }
            }
        });

        JScrollPane tableScroll = new JScrollPane(activityTable);
        tableScroll.setBorder(BorderFactory.createLineBorder(COLOR_BORDER, 1));
        tableScroll.getViewport().setBackground(COLOR_WHITE);
        tableCard.add(tableScroll, BorderLayout.CENTER);

        // Section C: Certificate Analysis & Eligibility Panel
        JPanel analysisCard = buildCertificateAnalysisSection();

        // Section D: Staff Evaluation Actions Toolbar
        JPanel actionToolbar = buildStaffEvaluationToolbar();

        JPanel lowerPanel = new JPanel(new BorderLayout(0, 8));
        lowerPanel.setOpaque(false);
        lowerPanel.add(analysisCard, BorderLayout.CENTER);
        lowerPanel.add(actionToolbar, BorderLayout.SOUTH);

        JSplitPane rightSplit = new JSplitPane(JSplitPane.VERTICAL_SPLIT, tableCard, lowerPanel);
        rightSplit.setDividerLocation(260);
        rightSplit.setResizeWeight(0.42);
        rightSplit.setBorder(null);
        rightSplit.setBackground(COLOR_BG);

        container.add(rightSplit, BorderLayout.CENTER);
        return container;
    }

    // ------------------ Section C: Certificate Analysis & Eligibility ------------------

    private JPanel buildCertificateAnalysisSection() {
        JPanel panel = new JPanel(new BorderLayout(8, 8));
        panel.setBackground(COLOR_WHITE);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(COLOR_BORDER, 1),
                new EmptyBorder(10, 14, 10, 14)
        ));

        // Header + Disclaimer Banner
        JPanel topBlock = new JPanel(new BorderLayout(0, 6));
        topBlock.setOpaque(false);

        JLabel secTitle = new JLabel("C. CERTIFICATE TEXT EXTRACTION & ELIGIBILITY PRE-CHECK");
        secTitle.setFont(FONT_SECTION);
        secTitle.setForeground(COLOR_NAVY);
        topBlock.add(secTitle, BorderLayout.NORTH);

        // Advisory Warning Banner
        JPanel bannerPanel = new JPanel(new BorderLayout(6, 0));
        bannerPanel.setBackground(COLOR_AMBER_BG);
        bannerPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(241, 196, 15), 1),
                new EmptyBorder(6, 12, 6, 12)
        ));
        JLabel warningLabel = new JLabel(
                "⚠️ Preliminary Automated Pre-Check: Advisory only. Does NOT certify document authenticity or detect fraud. Final points must be verified and awarded by authorized staff."
        );
        warningLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        warningLabel.setForeground(new Color(130, 75, 0));
        bannerPanel.add(warningLabel, BorderLayout.CENTER);
        topBlock.add(bannerPanel, BorderLayout.SOUTH);

        panel.add(topBlock, BorderLayout.NORTH);

        // Content: Left Details Grid, Right Extracted Text ScrollPane
        JPanel contentGrid = new JPanel(new GridLayout(1, 2, 12, 0));
        contentGrid.setOpaque(false);

        // Left Details
        JPanel metaPanel = new JPanel(new GridBagLayout());
        metaPanel.setBackground(COLOR_WHITE);
        GridBagConstraints mg = new GridBagConstraints();
        mg.insets = new Insets(3, 4, 3, 4);
        mg.fill = GridBagConstraints.HORIZONTAL;

        analysisStatusLabel = new JLabel("NOT_ANALYZED");
        analysisStatusLabel.setFont(FONT_BOLD);
        analysisStatusLabel.setForeground(COLOR_GRAY);

        eligibilityLabel = new JLabel("UNCERTAIN");
        eligibilityLabel.setFont(FONT_BOLD);
        eligibilityLabel.setForeground(COLOR_AMBER);

        confidenceLabel = new JLabel("-");
        confidenceLabel.setFont(FONT_BOLD);
        confidenceLabel.setForeground(COLOR_NAVY);

        duplicateLabel = new JLabel("None detected");
        duplicateLabel.setFont(FONT_BOLD);
        duplicateLabel.setForeground(COLOR_GREEN);

        detectedTitleField = new JTextField(15);
        detectedTitleField.setEditable(false);
        styleControl(detectedTitleField, 32);

        detectedOrganizerField = new JTextField(15);
        detectedOrganizerField.setEditable(false);
        styleControl(detectedOrganizerField, 32);

        detectedDateField = new JTextField(15);
        detectedDateField.setEditable(false);
        styleControl(detectedDateField, 32);

        exclusionReasonLabel = new JLabel("None");
        exclusionReasonLabel.setFont(FONT_REGULAR);
        exclusionReasonLabel.setForeground(COLOR_RED);

        analysisNotesArea = new JTextArea(4, 18);
        analysisNotesArea.setLineWrap(true);
        analysisNotesArea.setWrapStyleWord(true);
        analysisNotesArea.setEditable(false);
        analysisNotesArea.setFont(FONT_SMALL);
        analysisNotesArea.setBackground(COLOR_BG);
        JScrollPane notesScroll = new JScrollPane(analysisNotesArea);
        notesScroll.setBorder(BorderFactory.createLineBorder(COLOR_BORDER, 1));

        addMetaRow(metaPanel, mg, 0, "Analysis Status:", analysisStatusLabel);
        addMetaRow(metaPanel, mg, 1, "Eligibility Pre-Check:", eligibilityLabel);
        addMetaRow(metaPanel, mg, 2, "Confidence Level:", confidenceLabel);
        addMetaRow(metaPanel, mg, 3, "Duplicate Hash Check:", duplicateLabel);
        addMetaRow(metaPanel, mg, 4, "Detected Title (Suggestion):", detectedTitleField);
        addMetaRow(metaPanel, mg, 5, "Detected Organizer (Suggestion):", detectedOrganizerField);
        addMetaRow(metaPanel, mg, 6, "Detected Event Date (Suggestion):", detectedDateField);
        addMetaRow(metaPanel, mg, 7, "Exclusion Reason:", exclusionReasonLabel);
        addMetaRow(metaPanel, mg, 8, "Pre-Check Notes & Flags:", notesScroll);

        contentGrid.add(metaPanel);

        // Right Extracted Certificate Text Area
        extractedTextArea = new JTextArea();
        extractedTextArea.setLineWrap(true);
        extractedTextArea.setWrapStyleWord(true);
        extractedTextArea.setEditable(false);
        extractedTextArea.setFont(FONT_MONO);
        extractedTextArea.setText("[Select an activity row to view extracted certificate text]");
        extractedTextArea.setBackground(new Color(250, 251, 254));

        JScrollPane textScroll = new JScrollPane(extractedTextArea);
        textScroll.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(COLOR_BORDER, 1),
                "Extracted Certificate Text (PDFBox Local Layer)",
                0, 0, FONT_BOLD, COLOR_NAVY
        ));
        contentGrid.add(textScroll);

        panel.add(contentGrid, BorderLayout.CENTER);
        return panel;
    }

    private void addMetaRow(JPanel panel, GridBagConstraints g, int row, String label, JComponent comp) {
        g.gridx = 0;
        g.gridy = row;
        g.weightx = 0.38;
        JLabel lbl = new JLabel(label);
        lbl.setFont(FONT_SMALL);
        lbl.setForeground(COLOR_TEXT_MUTED);
        panel.add(lbl, g);

        g.gridx = 1;
        g.weightx = 0.62;
        panel.add(comp, g);
    }

    // ------------------ Section D: Staff Evaluation Actions ------------------

    private JPanel buildStaffEvaluationToolbar() {
        JPanel bar = new JPanel(new BorderLayout());
        bar.setBackground(COLOR_WHITE);
        bar.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(COLOR_BORDER, 1),
                new EmptyBorder(8, 14, 8, 14)
        ));

        JLabel actionTitle = new JLabel("D. STAFF EVALUATION ACTIONS");
        actionTitle.setFont(FONT_BOLD);
        actionTitle.setForeground(COLOR_NAVY);
        bar.add(actionTitle, BorderLayout.WEST);

        analyzeCertButton = KECTheme.createPrimaryButton("Analyze Certificate");
        analyzeCertButton.setPreferredSize(new Dimension(180, 42));
        analyzeCertButton.setToolTipText("Run text extraction and preliminary eligibility pre-checks on selected certificate");
        analyzeCertButton.addActionListener(e -> analyzeSelectedActivity());

        approveButton = KECTheme.createSuccessButton("Approve Selected");
        approveButton.setPreferredSize(new Dimension(170, 42));
        approveButton.addActionListener(e -> reviewActivity(true));

        rejectButton = KECTheme.createDangerButton("Reject Selected");
        rejectButton.setPreferredSize(new Dimension(160, 42));
        rejectButton.addActionListener(e -> reviewActivity(false));

        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        btnRow.setOpaque(false);
        btnRow.add(analyzeCertButton);
        btnRow.add(approveButton);
        btnRow.add(rejectButton);

        bar.add(btnRow, BorderLayout.EAST);
        return bar;
    }

    // ------------------ Table Cell Badge Renderers ------------------

    private static class StatusBadgeRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int col) {
            JLabel label = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, col);
            label.setHorizontalAlignment(SwingConstants.CENTER);
            label.setFont(new Font("Segoe UI", Font.BOLD, 12));
            label.setBorder(new EmptyBorder(4, 8, 4, 8));

            String status = value != null ? value.toString() : "";
            if ("APPROVED".equals(status)) {
                label.setForeground(COLOR_GREEN);
                label.setBackground(isSelected ? table.getSelectionBackground() : COLOR_GREEN_BG);
            } else if ("REJECTED".equals(status)) {
                label.setForeground(COLOR_RED);
                label.setBackground(isSelected ? table.getSelectionBackground() : COLOR_RED_BG);
            } else if ("PENDING_REVIEW".equals(status) || "PENDING_ANALYSIS".equals(status)) {
                label.setForeground(COLOR_AMBER);
                label.setBackground(isSelected ? table.getSelectionBackground() : COLOR_AMBER_BG);
            } else {
                label.setForeground(COLOR_GRAY);
                label.setBackground(isSelected ? table.getSelectionBackground() : COLOR_GRAY_BG);
            }
            label.setOpaque(true);
            return label;
        }
    }

    private static class AnalysisBadgeRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int col) {
            JLabel label = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, col);
            label.setHorizontalAlignment(SwingConstants.CENTER);
            label.setFont(new Font("Segoe UI", Font.BOLD, 12));
            label.setBorder(new EmptyBorder(4, 8, 4, 8));

            String st = value != null ? value.toString() : "";
            if ("ANALYZED".equals(st)) {
                label.setForeground(COLOR_GREEN);
                label.setBackground(isSelected ? table.getSelectionBackground() : COLOR_GREEN_BG);
            } else if ("ANALYSIS_FAILED".equals(st)) {
                label.setForeground(COLOR_RED);
                label.setBackground(isSelected ? table.getSelectionBackground() : COLOR_RED_BG);
            } else {
                label.setForeground(COLOR_GRAY);
                label.setBackground(isSelected ? table.getSelectionBackground() : COLOR_GRAY_BG);
            }
            label.setOpaque(true);
            return label;
        }
    }

    private static class EligibilityBadgeRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int col) {
            JLabel label = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, col);
            label.setHorizontalAlignment(SwingConstants.CENTER);
            label.setFont(new Font("Segoe UI", Font.BOLD, 12));
            label.setBorder(new EmptyBorder(4, 8, 4, 8));

            String el = value != null ? value.toString() : "";
            if ("ELIGIBLE".equals(el)) {
                label.setForeground(COLOR_GREEN);
                label.setBackground(isSelected ? table.getSelectionBackground() : COLOR_GREEN_BG);
            } else if ("INELIGIBLE".equals(el)) {
                label.setForeground(COLOR_RED);
                label.setBackground(isSelected ? table.getSelectionBackground() : COLOR_RED_BG);
            } else {
                label.setForeground(COLOR_AMBER);
                label.setBackground(isSelected ? table.getSelectionBackground() : COLOR_AMBER_BG);
            }
            label.setOpaque(true);
            return label;
        }
    }

    // ------------------ File & Certificate Chooser ------------------

    private void chooseCertificateFile() {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Select Certificate Document (PDF / Image)");
        int res = chooser.showOpenDialog(this);
        if (res == JFileChooser.APPROVE_OPTION) {
            chosenCertFile = chooser.getSelectedFile();
            certFileField.setText(chosenCertFile.getName() + " (" + chosenCertFile.length() + " bytes)");
        }
    }

    public void setChosenCertificateFile(File file) {
        this.chosenCertFile = file;
        if (file != null) {
            this.certFileField.setText(file.getName() + " (" + file.length() + " bytes)");
        } else {
            this.certFileField.setText("No file chosen");
        }
    }

    // ------------------ Certificate Analysis Engine ------------------

    public static CertificateAnalysisResult performAnalysis(
            File certFile, String claimedDate, String sha256, int currentActivityId, Connection con
    ) {
        CertificateAnalysisResult res = new CertificateAnalysisResult();

        // 1. Check Duplicate Certificate Hash
        if (sha256 != null && !sha256.trim().isEmpty() && con != null) {
            String dupSql = "SELECT activity_id, activity_name, student_id FROM sap_activities WHERE certificate_sha256 = ? AND activity_id != ? LIMIT 1";
            try (PreparedStatement dupPs = con.prepareStatement(dupSql)) {
                dupPs.setString(1, sha256.trim());
                dupPs.setInt(2, currentActivityId);
                try (ResultSet dupRs = dupPs.executeQuery()) {
                    if (dupRs.next()) {
                        res.duplicateFlag = true;
                        res.eligibilityStatus = "INELIGIBLE";
                        int dupId = dupRs.getInt("activity_id");
                        String dupName = dupRs.getString("activity_name");
                        res.exclusionReason = "Duplicate certificate file hash detected (Identical to Activity #" + dupId + ": \"" + dupName + "\").";
                        res.confidence = 98.0;
                        res.analysisNotes = "Duplicate Hash Alert: Identical file hash was previously submitted under Activity #" + dupId + ". Flagged as duplicate.";
                    }
                }
            } catch (SQLException ex) {
                res.analysisNotes = appendNote(res.analysisNotes, "Database warning: Could not verify duplicate hash (" + sanitizeError(ex.getMessage()) + ").");
            }
        }

        // 2. Validate File Existence & Format
        if (certFile == null || !certFile.exists()) {
            res.analysisStatus = "ANALYSIS_FAILED";
            if (!res.duplicateFlag) res.eligibilityStatus = "UNCERTAIN";
            res.analysisNotes = appendNote(res.analysisNotes, "Certificate file not found on disk. Cannot extract text; requires manual staff review.");
            return res;
        }

        String fileName = certFile.getName().toLowerCase();

        // 3. Process PDF via PDFBox
        if (fileName.endsWith(".pdf")) {
            try (PDDocument doc = PDDocument.load(certFile)) {
                if (doc.isEncrypted()) {
                    res.analysisStatus = "ANALYSIS_FAILED";
                    if (!res.duplicateFlag) res.eligibilityStatus = "UNCERTAIN";
                    res.analysisNotes = appendNote(res.analysisNotes, "PDF is password-protected/encrypted. Text could not be extracted; routed for manual staff review.");
                } else {
                    PDFTextStripper stripper = new PDFTextStripper();
                    stripper.setSortByPosition(true);
                    String text = stripper.getText(doc);

                    if (text == null || text.trim().isEmpty()) {
                        res.analysisStatus = "ANALYSIS_FAILED";
                        if (!res.duplicateFlag) res.eligibilityStatus = "UNCERTAIN";
                        res.analysisNotes = appendNote(res.analysisNotes, "Scanned or image-only PDF detected with no selectable text layer. OCR engine (Tesseract) not configured in this environment; routed for manual staff review.");
                    } else {
                        res.extractedText = text.trim();
                        res.analysisStatus = "ANALYZED";
                    }
                }
            } catch (Exception ex) {
                res.analysisStatus = "ANALYSIS_FAILED";
                if (!res.duplicateFlag) res.eligibilityStatus = "UNCERTAIN";
                res.analysisNotes = appendNote(res.analysisNotes, "PDF parsing failed (" + ex.getClass().getSimpleName() + "). Routed for manual staff review.");
            }
        } else if (fileName.endsWith(".png") || fileName.endsWith(".jpg") || fileName.endsWith(".jpeg")) {
            res.analysisStatus = "ANALYSIS_FAILED";
            if (!res.duplicateFlag) res.eligibilityStatus = "UNCERTAIN";
            res.analysisNotes = appendNote(res.analysisNotes, "Image certificate (" + getFileExtension(fileName).toUpperCase() + ") detected. Local OCR engine (Tesseract) is not configured in this environment; routed for manual staff review.");
        } else {
            res.analysisStatus = "ANALYSIS_FAILED";
            if (!res.duplicateFlag) res.eligibilityStatus = "UNCERTAIN";
            res.analysisNotes = appendNote(res.analysisNotes, "Unsupported certificate format (" + fileName + "). Requires manual staff review.");
        }

        // 4. Suggestion Extraction & Eligibility Classification
        if ("ANALYZED".equals(res.analysisStatus) && res.extractedText != null && !res.extractedText.isEmpty()) {
            res.detectedDate = extractDetectedDate(res.extractedText);
            res.detectedOrganizer = extractDetectedOrganizer(res.extractedText);
            res.detectedTitle = extractDetectedTitle(res.extractedText);

            if (!res.duplicateFlag) {
                classifyEligibility(res);
            }

            // Date Consistency Check
            if (res.detectedDate.isEmpty()) {
                res.analysisNotes = appendNote(res.analysisNotes, "[Date Check] Event date is missing or unclear in certificate text. Marked UNCERTAIN pending visual staff verification.");
                if ("ELIGIBLE".equals(res.eligibilityStatus)) {
                    res.eligibilityStatus = "UNCERTAIN";
                }
            } else if (claimedDate != null && !claimedDate.trim().isEmpty()) {
                if (!datesAppearConsistent(claimedDate, res.detectedDate)) {
                    res.analysisNotes = appendNote(res.analysisNotes, "[Date Notice] Claimed activity date (" + claimedDate + ") differs from detected certificate date (" + res.detectedDate + "); staff must verify.");
                }
            }
        }

        // Mandatory Authenticity Disclaimer
        res.analysisNotes = appendNote(res.analysisNotes, "[Authenticity Disclaimer] Extracted text and automated pre-checks are preliminary suggestions only and must never be treated as proof of certificate authenticity, seal validity, or fraud detection. All submissions require visual verification and final decision by authorized staff.");

        return res;
    }

    private static void classifyEligibility(CertificateAnalysisResult res) {
        String lower = res.extractedText.toLowerCase();

        // 1. Regular Academic / Curricular Examination Documents -> INELIGIBLE
        if (lower.contains("end semester examination") ||
                lower.contains("semester examination") ||
                lower.contains("grade sheet") ||
                lower.contains("statement of marks") ||
                lower.contains("mark sheet") ||
                lower.contains("marksheet") ||
                lower.contains("hall ticket") ||
                lower.contains("consolidated mark sheet") ||
                lower.contains("controller of examinations") ||
                lower.contains("provisional certificate") ||
                lower.contains("degree certificate") ||
                lower.contains("academic transcript") ||
                lower.contains("grade card") ||
                lower.contains("examination results")) {
            res.eligibilityStatus = "INELIGIBLE";
            res.exclusionReason = "Curricular examination or marksheet document detected. Regular academic examinations and grade cards do not qualify for SAP co-curricular activity points.";
            res.confidence = 92.0;
            res.analysisNotes = appendNote(res.analysisNotes, "Pre-Check Policy: Curricular academic examination / grade document detected. Ineligible under KEC SAP policy.");
            return;
        }

        // 2. NPTEL Exam / Result Certificates -> INELIGIBLE under current project policy
        boolean mentionsNptel = lower.contains("nptel") ||
                lower.contains("swayam") ||
                lower.contains("national programme on technology enhanced learning");

        if (mentionsNptel) {
            boolean isNptelExam = lower.contains("exam") ||
                    lower.contains("examination") ||
                    lower.contains("proctored") ||
                    lower.contains("score") ||
                    lower.contains("marks") ||
                    lower.contains("passing score") ||
                    lower.contains("consolidated score") ||
                    lower.contains("marks obtained") ||
                    lower.contains("elite") ||
                    lower.contains("gold") ||
                    lower.contains("silver") ||
                    lower.contains("online certification") ||
                    lower.contains("successfully completed the course") ||
                    lower.contains("roll no:");

            boolean isNptelEvent = (lower.contains("workshop") || lower.contains("hackathon") ||
                    lower.contains("conference") || lower.contains("symposium") ||
                    lower.contains("faculty development") || lower.contains("fdp") ||
                    lower.contains("seminar")) && !lower.contains("proctored exam") && !lower.contains("examination");

            if (isNptelExam && !isNptelEvent) {
                res.eligibilityStatus = "INELIGIBLE";
                res.exclusionReason = "NPTEL exam / certification score document detected. Under current project policy, NPTEL exam certificates are ineligible for SAP activity points.";
                res.confidence = 90.0;
                res.analysisNotes = appendNote(res.analysisNotes, "Pre-Check Policy: NPTEL exam / assessment result certificate detected. Ineligible under current project policy.");
                return;
            }
        }

        // 3. Online-Course Completion Certificates -> INELIGIBLE under current project policy
        String matchedPlatform = getOnlineCoursePlatform(lower);
        if (matchedPlatform != null) {
            res.eligibilityStatus = "INELIGIBLE";
            res.exclusionReason = "Online-course completion certificate detected (" + matchedPlatform + "). Under current project policy, self-paced online course completions are ineligible for SAP activity points.";
            res.confidence = 88.0;
            res.analysisNotes = appendNote(res.analysisNotes, "Pre-Check Policy: Self-paced online course completion certificate detected (" + matchedPlatform + "). Ineligible under current project policy.");
            return;
        }

        // 4. Qualifying Activities (Workshops, Conferences, Paper Presentations, Hackathons, Competitions) -> ELIGIBLE
        if (lower.contains("workshop") ||
                lower.contains("paper presentation") ||
                lower.contains("conference") ||
                lower.contains("symposium") ||
                lower.contains("hackathon") ||
                lower.contains("project expo") ||
                lower.contains("project competition") ||
                lower.contains("seminar") ||
                lower.contains("webinar") ||
                lower.contains("technical event") ||
                lower.contains("certificate of participation") ||
                lower.contains("certificate of merit") ||
                lower.contains("participated in") ||
                lower.contains("has participated") ||
                lower.contains("presented a paper") ||
                lower.contains("first prize") ||
                lower.contains("second prize") ||
                lower.contains("third prize") ||
                lower.contains("winner") ||
                lower.contains("runner")) {
            res.eligibilityStatus = "ELIGIBLE";
            res.exclusionReason = null;
            res.confidence = 82.0;
            res.analysisNotes = appendNote(res.analysisNotes, "Preliminary Candidate: Qualifying co-curricular activity (workshop/conference/presentation/competition) detected. Staff must verify certificate authenticity and determine point award.");
            return;
        }

        // 5. Ambiguous / Unclear Category -> UNCERTAIN
        res.eligibilityStatus = "UNCERTAIN";
        res.exclusionReason = null;
        res.confidence = 50.0;
        res.analysisNotes = appendNote(res.analysisNotes, "Ambiguity Notice: Document text was extracted, but the certificate type or activity category is ambiguous or unrecognized under standard rules. Requires manual staff review and classification.");
    }

    private static String getOnlineCoursePlatform(String lower) {
        if (lower.contains("coursera")) return "Coursera";
        if (lower.contains("udemy")) return "Udemy";
        if (lower.contains("edx")) return "edX";
        if (lower.contains("simplilearn")) return "Simplilearn";
        if (lower.contains("great learning")) return "Great Learning";
        if (lower.contains("hackerrank")) return "HackerRank";
        if (lower.contains("datacamp")) return "DataCamp";
        if (lower.contains("udacity")) return "Udacity";
        if (lower.contains("guvi")) return "GUVI";
        if (lower.contains("linkedin learning")) return "LinkedIn Learning";
        if (lower.contains("pluralsight")) return "Pluralsight";
        if (lower.contains("codecademy")) return "Codecademy";
        if (lower.contains("futurelearn")) return "FutureLearn";
        if (lower.contains("alison")) return "Alison";
        if (lower.contains("khan academy")) return "Khan Academy";
        if (lower.contains("online course completion") ||
                lower.contains("course completion certificate") ||
                lower.contains("completed the online course") ||
                lower.contains("self-paced course")) {
            return "Online Course Provider";
        }
        return null;
    }

    public static void saveAnalysisResult(int activityId, CertificateAnalysisResult res, Connection con) throws SQLException {
        String updateSql = """
                UPDATE sap_activities
                SET analysis_status = ?,
                    eligibility_status = ?,
                    extracted_text = ?,
                    exclusion_reason = ?,
                    duplicate_flag = ?,
                    analysis_confidence = ?,
                    analysis_notes = ?,
                    updated_at = NOW()
                WHERE activity_id = ?
                """;
        try (PreparedStatement ps = con.prepareStatement(updateSql)) {
            ps.setString(1, res.analysisStatus);
            ps.setString(2, res.eligibilityStatus);
            ps.setString(3, res.extractedText);
            ps.setString(4, res.exclusionReason);
            ps.setBoolean(5, res.duplicateFlag);
            ps.setBigDecimal(6, BigDecimal.valueOf(res.confidence));
            ps.setString(7, res.analysisNotes);
            ps.setInt(8, activityId);
            ps.executeUpdate();
        }
    }

    // ------------------ Actions: Analyze, Submit, Review ------------------

    private void analyzeSelectedActivity() {
        int viewRow = activityTable.getSelectedRow();
        if (viewRow < 0) {
            JOptionPane.showMessageDialog(this,
                    "Please select an activity from the Activity Submissions table first.",
                    "Selection Required", JOptionPane.WARNING_MESSAGE);
            return;
        }
        int row = activityTable.convertRowIndexToModel(viewRow);
        int activityId = (int) tableModel.getValueAt(row, 0);

        analyzeCertButton.setEnabled(false);
        analyzeCertButton.setText("Analyzing...");

        SwingWorker<CertificateAnalysisResult, Void> worker = new SwingWorker<>() {
            @Override
            protected CertificateAnalysisResult doInBackground() throws Exception {
                try (Connection con = getConnection()) {
                    String sql = "SELECT certificate_path, activity_date, certificate_sha256 FROM sap_activities WHERE activity_id = ?";
                    try (PreparedStatement ps = con.prepareStatement(sql)) {
                        ps.setInt(1, activityId);
                        try (ResultSet rs = ps.executeQuery()) {
                            if (!rs.next()) {
                                throw new SQLException("Activity #" + activityId + " not found.");
                            }
                            String certPath = rs.getString("certificate_path");
                            Date actDate = rs.getDate("activity_date");
                            String claimedDate = (actDate != null) ? actDate.toString() : "";
                            String sha256 = rs.getString("certificate_sha256");

                            File certFile = (certPath != null) ? new File(certPath) : null;
                            CertificateAnalysisResult res = performAnalysis(certFile, claimedDate, sha256, activityId, con);
                            saveAnalysisResult(activityId, res, con);
                            return res;
                        }
                    }
                }
            }

            @Override
            protected void done() {
                analyzeCertButton.setEnabled(true);
                analyzeCertButton.setText("Analyze Certificate");
                try {
                    CertificateAnalysisResult res = get();
                    loadActivitiesInBackground();
                    loadSelectedActivityDetails(activityId);

                    String summary = String.format(
                            "Certificate Analysis Completed!\n\n" +
                                    "Activity ID: #%d\n" +
                                    "Analysis Status: %s\n" +
                                    "Eligibility Pre-Check: %s\n" +
                                    "Confidence Level: %.1f%%\n" +
                                    "Duplicate Hash Flag: %s\n\n" +
                                    "Note: %s\n\n" +
                                    "Authorized staff must review details and make the final decision.",
                            activityId,
                            res.analysisStatus,
                            res.eligibilityStatus,
                            res.confidence,
                            res.duplicateFlag ? "YES (Duplicate Detected)" : "None",
                            (res.exclusionReason != null ? res.exclusionReason : (res.analysisNotes != null ? res.analysisNotes : "Complete"))
                    );
                    JOptionPane.showMessageDialog(SAPManagement.this, summary, "Analysis Finished", JOptionPane.INFORMATION_MESSAGE);
                } catch (Exception ex) {
                    showError("Could not analyze certificate", ex);
                }
            }
        };
        worker.execute();
    }

    private void submitActivity() {
        StudentItem student = (StudentItem) studentBox.getSelectedItem();
        String name = activityField.getText().trim();
        String type = (String) typeBox.getSelectedItem();
        String dateText = dateField.getText().trim();
        String organizer = organizerField.getText().trim();
        String desc = descriptionArea.getText().trim();

        if (student == null) {
            JOptionPane.showMessageDialog(this, "Please select a student.", "Validation Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (name.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter an activity name.", "Validation Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int claimedPoints;
        try {
            claimedPoints = Integer.parseInt(pointsField.getText().trim());
            if (claimedPoints < 0 || claimedPoints > 150) {
                throw new NumberFormatException();
            }
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Claimed points must be a whole number from 0 to 150.", "Validation Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        Date activityDate = null;
        if (!dateText.isEmpty()) {
            try {
                activityDate = Date.valueOf(dateText);
            } catch (IllegalArgumentException ex) {
                JOptionPane.showMessageDialog(this, "Invalid date format. Please use YYYY-MM-DD.", "Validation Error", JOptionPane.WARNING_MESSAGE);
                return;
            }
        }

        if (chosenCertFile == null || !chosenCertFile.exists()) {
            JOptionPane.showMessageDialog(this,
                    "A certificate document is required to complete this submission.\nPlease click 'Browse...' and select a certificate file.",
                    "Certificate Required", JOptionPane.WARNING_MESSAGE);
            return;
        }

        long fileSizeBytes = chosenCertFile.length();
        if (fileSizeBytes <= 0 || fileSizeBytes > 10485760) {
            JOptionPane.showMessageDialog(this,
                    "Certificate file must be between 1 byte and 10 MB.",
                    "Invalid File Size", JOptionPane.WARNING_MESSAGE);
            return;
        }

        File fileToUpload = chosenCertFile;
        final Date finalDate = activityDate;

        SwingWorker<Integer, Void> worker = new SwingWorker<>() {
            @Override
            protected Integer doInBackground() throws Exception {
                File uploadsDir = new File("uploads");
                if (!uploadsDir.exists()) {
                    uploadsDir.mkdirs();
                }

                String originalName = fileToUpload.getName();
                String storedName = student.rollNo + "_" + System.currentTimeMillis() + "_" + originalName;
                File targetFile = new File(uploadsDir, storedName);
                Files.copy(fileToUpload.toPath(), targetFile.toPath(), StandardCopyOption.REPLACE_EXISTING);

                MessageDigest md = MessageDigest.getInstance("SHA-256");
                byte[] hashBytes = md.digest(Files.readAllBytes(targetFile.toPath()));
                StringBuilder sb = new StringBuilder();
                for (byte b : hashBytes) {
                    sb.append(String.format("%02x", b));
                }
                String sha256 = sb.toString();

                String mimeType = Files.probeContentType(targetFile.toPath());
                if (mimeType == null || mimeType.trim().isEmpty()) {
                    mimeType = "application/octet-stream";
                }

                String insertSql = """
                        INSERT INTO sap_activities
                        (student_id, activity_name, activity_type, activity_date,
                         organizer, description, certificate_original_name,
                         certificate_path, certificate_mime_type, certificate_size_bytes,
                         certificate_sha256, claimed_points, recommended_points,
                         approved_points, status)
                        VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, 0, 0, 'PENDING_REVIEW')
                        """;

                try (Connection con = getConnection();
                     PreparedStatement ps = con.prepareStatement(insertSql, Statement.RETURN_GENERATED_KEYS)) {
                    ps.setInt(1, student.studentId);
                    ps.setString(2, name);
                    ps.setString(3, type);
                    if (finalDate != null) {
                        ps.setDate(4, finalDate);
                    } else {
                        ps.setNull(4, Types.DATE);
                    }
                    ps.setString(5, organizer.isEmpty() ? null : organizer);
                    ps.setString(6, desc.isEmpty() ? null : desc);
                    ps.setString(7, originalName);
                    ps.setString(8, "uploads/" + storedName);
                    ps.setString(9, mimeType);
                    ps.setLong(10, fileSizeBytes);
                    ps.setString(11, sha256);
                    ps.setInt(12, claimedPoints);

                    ps.executeUpdate();

                    int newId = -1;
                    try (ResultSet gk = ps.getGeneratedKeys()) {
                        if (gk.next()) {
                            newId = gk.getInt(1);
                        }
                    }

                    // Automatically perform initial local pre-check extraction & save to database
                    if (newId > 0) {
                        String dateStr = (finalDate != null) ? finalDate.toString() : "";
                        CertificateAnalysisResult analysisRes = performAnalysis(targetFile, dateStr, sha256, newId, con);
                        saveAnalysisResult(newId, analysisRes, con);
                    }

                    return newId;
                }
            }

            @Override
            protected void done() {
                try {
                    int activityId = get();
                    if (activityId > 0) {
                        JOptionPane.showMessageDialog(SAPManagement.this,
                                "Activity #" + activityId + " submitted successfully.\nPreliminary certificate pre-check performed.\nStatus: PENDING_REVIEW (awaits staff approval).",
                                "Submission Saved", JOptionPane.INFORMATION_MESSAGE);

                        activityField.setText("");
                        organizerField.setText("");
                        pointsField.setText("0");
                        descriptionArea.setText("");
                        chosenCertFile = null;
                        certFileField.setText("No file chosen");

                        loadActivitiesInBackground();
                        updateBonusLevelForSelectedStudent();
                        loadSelectedActivityDetails(activityId);
                    }
                } catch (Exception ex) {
                    showError("Could not submit activity", ex);
                }
            }
        };
        worker.execute();
    }

    private void reviewActivity(boolean approve) {
        if (currentSession != null && !currentSession.canReviewSAP()) {
            JOptionPane.showMessageDialog(this,
                    "Unauthorized: Student accounts are not permitted to review SAP activities or award points.",
                    "Access Denied", JOptionPane.ERROR_MESSAGE);
            throw new SecurityException("Unauthorized review action attempted by user: " + currentSession.getUsername());
        }

        int viewRow = activityTable.getSelectedRow();
        if (viewRow < 0) {
            JOptionPane.showMessageDialog(this,
                    "Please select an activity row from the table first.",
                    "Selection Required", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int row = activityTable.convertRowIndexToModel(viewRow);
        int activityId = (int) tableModel.getValueAt(row, 0);
        String studentName = String.valueOf(tableModel.getValueAt(row, 2));
        String activityName = String.valueOf(tableModel.getValueAt(row, 3));
        int claimed = (int) tableModel.getValueAt(row, 6);
        String currentStatus = String.valueOf(tableModel.getValueAt(row, 8));

        if (!"PENDING_REVIEW".equals(currentStatus) && !"PENDING_ANALYSIS".equals(currentStatus)) {
            JOptionPane.showMessageDialog(this,
                    "This activity has already been reviewed (Current Status: " + currentStatus + ").",
                    "Already Processed", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int approvedPoints = 0;
        String comments = "";

        if (approve) {
            String entered = JOptionPane.showInputDialog(
                    this,
                    String.format("Approve Activity #%d for %s\nActivity: %s\nClaimed Points: %d\n\nEnter points approved by staff (0–150):",
                            activityId, studentName, activityName, claimed),
                    String.valueOf(claimed)
            );
            if (entered == null) return;

            try {
                approvedPoints = Integer.parseInt(entered.trim());
                if (approvedPoints < 0 || approvedPoints > 150) {
                    throw new NumberFormatException();
                }
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this,
                        "Approved points must be a whole number between 0 and 150.",
                        "Invalid Points", JOptionPane.WARNING_MESSAGE);
                return;
            }

            String c = JOptionPane.showInputDialog(this, "Review comments (optional):");
            if (c != null) comments = c.trim();

            int confirm = JOptionPane.showConfirmDialog(
                    this,
                    String.format("Please confirm approval:\n\nActivity ID: #%d\nStudent: %s\nApproved Points: %d\nComments: %s\n\nProceed with recording approval in database?",
                            activityId, studentName, approvedPoints, comments.isEmpty() ? "(None)" : comments),
                    "Confirm Staff Approval",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.QUESTION_MESSAGE
            );
            if (confirm != JOptionPane.YES_OPTION) return;

        } else {
            String c = JOptionPane.showInputDialog(
                    this,
                    String.format("Reject Activity #%d for %s\nActivity: %s\n\nPlease enter the reason for rejection (Required):",
                            activityId, studentName, activityName)
            );
            if (c == null) return;
            comments = c.trim();
            if (comments.isEmpty()) {
                JOptionPane.showMessageDialog(this,
                        "Please provide a rejection reason for the student.",
                        "Reason Required", JOptionPane.WARNING_MESSAGE);
                return;
            }

            int confirm = JOptionPane.showConfirmDialog(
                    this,
                    String.format("Are you sure you want to REJECT Activity #%d?\n\nReason: %s\n\nThis will record rejection in the database.",
                            activityId, comments),
                    "Confirm Staff Rejection",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.WARNING_MESSAGE
            );
            if (confirm != JOptionPane.YES_OPTION) return;
        }

        final int finalApproved = approvedPoints;
        final String finalComments = comments;
        final String action = approve ? "APPROVED" : "REJECTED";
        final String newStatus = approve ? "APPROVED" : "REJECTED";
        final String reviewerId = (currentSession != null) ? currentSession.getUsername() : "STAFF_REVIEWER";

        SwingWorker<Boolean, Void> worker = new SwingWorker<>() {
            @Override
            protected Boolean doInBackground() throws Exception {
                String updateSql = """
                        UPDATE sap_activities
                        SET approved_points = ?, status = ?,
                            reviewer_comments = ?, reviewed_by = ?,
                            reviewed_at = NOW()
                        WHERE activity_id = ?
                          AND status IN ('PENDING_REVIEW', 'PENDING_ANALYSIS')
                        """;

                String reviewSql = """
                        INSERT INTO sap_activity_reviews
                        (activity_id, reviewer_id, action,
                         previous_points, final_points, comments, reviewed_at)
                        VALUES (?, ?, ?, ?, ?, ?, NOW())
                        """;

                try (Connection con = getConnection()) {
                    con.setAutoCommit(false);
                    try (PreparedStatement update = con.prepareStatement(updateSql);
                         PreparedStatement review = con.prepareStatement(reviewSql)) {

                        update.setInt(1, finalApproved);
                        update.setString(2, newStatus);
                        update.setString(3, finalComments);
                        update.setString(4, reviewerId);
                        update.setInt(5, activityId);

                        int changed = update.executeUpdate();
                        if (changed != 1) {
                            con.rollback();
                            return false;
                        }

                        review.setInt(1, activityId);
                        review.setString(2, reviewerId);
                        review.setString(3, action);
                        review.setInt(4, claimed);
                        review.setInt(5, finalApproved);
                        review.setString(6, finalComments);
                        review.executeUpdate();

                        con.commit();
                        return true;
                    } catch (SQLException ex) {
                        con.rollback();
                        throw ex;
                    } finally {
                        con.setAutoCommit(true);
                    }
                }
            }

            @Override
            protected void done() {
                try {
                    boolean success = get();
                    if (success) {
                        JOptionPane.showMessageDialog(SAPManagement.this,
                                String.format("Review recorded successfully in database transaction.\nActivity #%d\nNew Status: %s\nApproved Points: %d",
                                        activityId, newStatus, finalApproved),
                                "Review Saved", JOptionPane.INFORMATION_MESSAGE);
                        loadActivitiesInBackground();
                        updateBonusLevelForSelectedStudent();
                        loadSelectedActivityDetails(activityId);
                    } else {
                        JOptionPane.showMessageDialog(SAPManagement.this,
                                "The activity status was modified by another transaction. Please refresh and retry.",
                                "Conflict", JOptionPane.WARNING_MESSAGE);
                    }
                } catch (Exception ex) {
                    showError("Could not save review", ex);
                }
            }
        };
        worker.execute();
    }

    // ------------------ Data Loading: Students, Activities, Bonus ------------------

    private void loadStudentsInBackground() {
        boolean isStudent = (currentSession != null && currentSession.isStudent());
        SwingWorker<List<StudentItem>, Void> worker = new SwingWorker<>() {
            @Override
            protected List<StudentItem> doInBackground() throws Exception {
                List<StudentItem> list = new ArrayList<>();
                String sql = isStudent
                        ? "SELECT student_id, roll_no, student_name FROM students WHERE student_id = ? ORDER BY roll_no"
                        : "SELECT student_id, roll_no, student_name FROM students ORDER BY roll_no";
                try (Connection con = getConnection();
                     PreparedStatement ps = con.prepareStatement(sql)) {
                    if (isStudent) {
                        ps.setInt(1, currentSession.getStudentId());
                    }
                    try (ResultSet rs = ps.executeQuery()) {
                        while (rs.next()) {
                            list.add(new StudentItem(
                                    rs.getInt("student_id"),
                                    rs.getString("roll_no"),
                                    rs.getString("student_name")
                            ));
                        }
                    }
                }
                return list;
            }

            @Override
            protected void done() {
                try {
                    List<StudentItem> students = get();
                    studentBox.removeAllItems();
                    for (StudentItem s : students) {
                        studentBox.addItem(s);
                    }
                    updateBonusLevelForSelectedStudent();
                } catch (Exception ex) {
                    showError("Could not load students", ex);
                }
            }
        };
        worker.execute();
    }

    private void updateBonusLevelForSelectedStudent() {
        StudentItem student = (StudentItem) studentBox.getSelectedItem();
        if (student == null) {
            cardStudentValue.setText("None Selected");
            cardStudentSub.setText("Select from list");
            cardPointsValue.setText("0 pts");
            cardBonusValue.setText("Level 0");
            cardBonusSub.setText("No rule matched");
            return;
        }

        cardStudentValue.setText(student.rollNo);
        cardStudentSub.setText(student.studentName);

        SwingWorker<int[], Void> worker = new SwingWorker<>() {
            String bonusDesc = "No rule matched";

            @Override
            protected int[] doInBackground() throws Exception {
                int approvedPoints = 0;
                int bonusLevel = 0;

                try (Connection con = getConnection()) {
                    String sumSql = "SELECT COALESCE(SUM(approved_points), 0) FROM sap_activities WHERE student_id = ? AND status = 'APPROVED'";
                    try (PreparedStatement ps = con.prepareStatement(sumSql)) {
                        ps.setInt(1, student.studentId);
                        try (ResultSet rs = ps.executeQuery()) {
                            if (rs.next()) {
                                approvedPoints = rs.getInt(1);
                            }
                        }
                    }

                    String ruleSql = """
                            SELECT bonus_level, rule_description
                            FROM sap_bonus_rules
                            WHERE active = 1
                              AND ? >= minimum_points
                              AND (? <= maximum_points OR maximum_points IS NULL)
                            ORDER BY bonus_level DESC
                            LIMIT 1
                            """;
                    try (PreparedStatement ps = con.prepareStatement(ruleSql)) {
                        ps.setInt(1, approvedPoints);
                        ps.setInt(2, approvedPoints);
                        try (ResultSet rs = ps.executeQuery()) {
                            if (rs.next()) {
                                bonusLevel = rs.getInt("bonus_level");
                                bonusDesc = rs.getString("rule_description");
                            }
                        }
                    }
                }
                return new int[]{approvedPoints, bonusLevel};
            }

            @Override
            protected void done() {
                try {
                    int[] data = get();
                    cardPointsValue.setText(data[0] + " pts");
                    cardPointsSub.setText("Approved SAP points");
                    cardBonusValue.setText("Level " + data[1]);
                    cardBonusSub.setText(bonusDesc);
                } catch (Exception ex) {
                    cardBonusSub.setText("Calc error");
                }
            }
        };
        worker.execute();
    }

    private void loadActivitiesInBackground() {
        if (tableModel == null) return;

        boolean isStudent = (currentSession != null && currentSession.isStudent());
        SwingWorker<List<Object[]>, Void> worker = new SwingWorker<>() {
            int pendingCount = 0;
            int approvedCount = 0;

            @Override
            protected List<Object[]> doInBackground() throws Exception {
                List<Object[]> rows = new ArrayList<>();
                String sql = "SELECT a.activity_id, s.roll_no, s.student_name, " +
                        "a.activity_name, a.activity_type, a.activity_date, " +
                        "a.claimed_points, a.approved_points, a.status, " +
                        "a.analysis_status, a.eligibility_status " +
                        "FROM sap_activities a " +
                        "JOIN students s ON s.student_id = a.student_id " +
                        (isStudent ? "WHERE a.student_id = ? " : "") +
                        "ORDER BY a.created_at DESC";

                try (Connection con = getConnection();
                     PreparedStatement ps = con.prepareStatement(sql)) {
                    if (isStudent) {
                        ps.setInt(1, currentSession.getStudentId());
                    }
                    try (ResultSet rs = ps.executeQuery()) {
                        while (rs.next()) {
                            String st = rs.getString("status");
                            if ("PENDING_REVIEW".equals(st) || "PENDING_ANALYSIS".equals(st)) {
                                pendingCount++;
                            } else if ("APPROVED".equals(st)) {
                                approvedCount++;
                            }

                            rows.add(new Object[]{
                                    rs.getInt("activity_id"),
                                    rs.getString("roll_no"),
                                    rs.getString("student_name"),
                                    rs.getString("activity_name"),
                                    rs.getString("activity_type"),
                                    rs.getDate("activity_date") != null ? rs.getDate("activity_date").toString() : "-",
                                    rs.getInt("claimed_points"),
                                    rs.getInt("approved_points"),
                                    rs.getString("status"),
                                    rs.getString("analysis_status"),
                                    rs.getString("eligibility_status")
                            });
                        }
                    }
                }
                return rows;
            }

            @Override
            protected void done() {
                try {
                    List<Object[]> rows = get();
                    tableModel.setRowCount(0);
                    for (Object[] row : rows) {
                        tableModel.addRow(row);
                    }
                    cardPendingValue.setText(String.valueOf(pendingCount));
                    cardPendingSub.setText(pendingCount == 1 ? "1 activity pending" : pendingCount + " activities pending");
                    cardApprovedValue.setText(String.valueOf(approvedCount));
                    cardApprovedSub.setText(approvedCount == 1 ? "1 activity approved" : approvedCount + " activities approved");
                } catch (Exception ex) {
                    showError("Could not load activities", ex);
                }
            }
        };
        worker.execute();
    }

    private void loadSelectedActivityDetails(int activityId) {
        SwingWorker<Object[], Void> worker = new SwingWorker<>() {
            @Override
            protected Object[] doInBackground() throws Exception {
                String sql = """
                        SELECT a.activity_id, a.activity_name, a.activity_date, a.organizer,
                               a.certificate_path, a.certificate_original_name,
                               a.analysis_status, a.eligibility_status, a.extracted_text,
                               a.exclusion_reason, a.duplicate_flag, a.analysis_confidence,
                               a.analysis_notes
                        FROM sap_activities a
                        WHERE a.activity_id = ?
                        """;
                try (Connection con = getConnection();
                     PreparedStatement ps = con.prepareStatement(sql)) {
                    ps.setInt(1, activityId);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (rs.next()) {
                            return new Object[]{
                                    rs.getInt("activity_id"),
                                    rs.getString("activity_name"),
                                    rs.getString("analysis_status"),
                                    rs.getString("eligibility_status"),
                                    rs.getString("extracted_text"),
                                    rs.getString("exclusion_reason"),
                                    rs.getBoolean("duplicate_flag"),
                                    rs.getBigDecimal("analysis_confidence"),
                                    rs.getString("analysis_notes"),
                                    rs.getString("certificate_original_name")
                            };
                        }
                    }
                }
                return null;
            }

            @Override
            protected void done() {
                try {
                    Object[] data = get();
                    if (data != null) {
                        String aStatus = (String) data[2];
                        String eStatus = (String) data[3];
                        String text = (String) data[4];
                        String excl = (String) data[5];
                        boolean dup = (Boolean) data[6];
                        BigDecimal conf = (BigDecimal) data[7];
                        String notes = (String) data[8];

                        analysisStatusLabel.setText(aStatus != null ? aStatus : "NOT_ANALYZED");
                        if ("ANALYZED".equals(aStatus)) {
                            analysisStatusLabel.setForeground(COLOR_GREEN);
                        } else if ("ANALYSIS_FAILED".equals(aStatus)) {
                            analysisStatusLabel.setForeground(COLOR_RED);
                        } else {
                            analysisStatusLabel.setForeground(COLOR_GRAY);
                        }

                        eligibilityLabel.setText(eStatus != null ? eStatus : "UNCERTAIN");
                        if ("ELIGIBLE".equals(eStatus)) {
                            eligibilityLabel.setForeground(COLOR_GREEN);
                        } else if ("INELIGIBLE".equals(eStatus)) {
                            eligibilityLabel.setForeground(COLOR_RED);
                        } else {
                            eligibilityLabel.setForeground(COLOR_AMBER);
                        }

                        confidenceLabel.setText(conf != null ? String.format("%.1f%%", conf.doubleValue()) : "-");
                        duplicateLabel.setText(dup ? "⚠️ DUPLICATE FILE DETECTED" : "None detected");
                        duplicateLabel.setForeground(dup ? COLOR_RED : COLOR_GREEN);

                        exclusionReasonLabel.setText(excl != null && !excl.isEmpty() ? excl : "None");
                        analysisNotesArea.setText(notes != null ? notes : "");
                        analysisNotesArea.setCaretPosition(0);

                        extractedTextArea.setText(text != null && !text.isEmpty() ? text : "[No text extracted or analysis not yet performed]");
                        extractedTextArea.setCaretPosition(0);

                        if (text != null && !text.isEmpty()) {
                            detectedTitleField.setText(extractDetectedTitle(text));
                            detectedOrganizerField.setText(extractDetectedOrganizer(text));
                            detectedDateField.setText(extractDetectedDate(text));
                        } else {
                            detectedTitleField.setText("-");
                            detectedOrganizerField.setText("-");
                            detectedDateField.setText("-");
                        }
                    }
                } catch (Exception ignored) {
                }
            }
        };
        worker.execute();
    }

    // ------------------ Helper Parsing & Diagnostics ------------------

    private static String appendNote(String original, String addition) {
        if (original == null || original.trim().isEmpty()) {
            return addition;
        }
        return original + "\n" + addition;
    }

    private static String getFileExtension(String name) {
        int idx = name.lastIndexOf('.');
        return idx > 0 ? name.substring(idx + 1) : "";
    }

    public static String extractDetectedDate(String text) {
        if (text == null) return "";
        Pattern pIso = Pattern.compile("\\b(20\\d{2}[-/.]\\d{1,2}[-/.]\\d{1,2})\\b");
        Matcher m = pIso.matcher(text);
        if (m.find()) return m.group(1);

        Pattern pInd = Pattern.compile("\\b(\\d{1,2}[-/.]\\d{1,2}[-/.]20\\d{2})\\b");
        m = pInd.matcher(text);
        if (m.find()) return m.group(1);

        Pattern pTxt1 = Pattern.compile("(?i)\\b(\\d{1,2}(?:st|nd|rd|th)?\\s+(?:January|February|March|April|May|June|July|August|September|October|November|December|Jan|Feb|Mar|Apr|May|Jun|Jul|Aug|Sep|Oct|Nov|Dec)[a-z]*\\s*,?\\s*20\\d{2})\\b");
        m = pTxt1.matcher(text);
        if (m.find()) return m.group(1);

        Pattern pTxt2 = Pattern.compile("(?i)\\b((?:January|February|March|April|May|June|July|August|September|October|November|December|Jan|Feb|Mar|Apr|May|Jun|Jul|Aug|Sep|Oct|Nov|Dec)[a-z]*\\s+\\d{1,2}(?:st|nd|rd|th)?\\s*,?\\s*20\\d{2})\\b");
        m = pTxt2.matcher(text);
        if (m.find()) return m.group(1);

        return "";
    }

    public static String extractDetectedOrganizer(String text) {
        if (text == null) return "";
        String[] known = {
                "Kongu Engineering College", "IIT Madras", "IIT Bombay", "IIT Delhi", "IIT Kharagpur",
                "IIT Roorkee", "IIT Kanpur", "IIT Guwahati", "IISc Bangalore", "NPTEL", "SWAYAM",
                "IEEE", "ACM", "CSI", "Anna University", "Coursera", "Udemy"
        };
        for (String k : known) {
            if (Pattern.compile("(?i)\\b" + Pattern.quote(k) + "\\b").matcher(text).find()) {
                return k;
            }
        }

        Pattern pOrg = Pattern.compile("(?i)(?:organized\\s+by|conducted\\s+by|offered\\s+by|hosted\\s+by|presented\\s+by)[:\\s]+([A-Za-z0-9&,.\t -]{3,50})");
        Matcher m = pOrg.matcher(text);
        if (m.find()) {
            String org = m.group(1).trim();
            int nl = org.indexOf('\n');
            if (nl > 0) org = org.substring(0, nl).trim();
            return org;
        }
        return "";
    }

    public static String extractDetectedTitle(String text) {
        if (text == null) return "";
        Pattern pTitle = Pattern.compile("(?i)(?:workshop\\s+on|seminar\\s+on|webinar\\s+on|course\\s+on|training\\s+on|symposium\\s+on|conference\\s+on|paper\\s+presentation\\s+on|participated\\s+in\\s+|completed\\s+(?:the\\s+course\\s+)?|titled\\s+[\"']?)([\"']?[A-Za-z0-9&,.:'\\- ]{3,60}[\"']?)");
        Matcher m = pTitle.matcher(text);
        if (m.find()) {
            String t = m.group(1).trim();
            int nl = t.indexOf('\n');
            if (nl > 0) t = t.substring(0, nl).trim();
            t = t.replaceAll("^[\"']+|[\"']+$", "").trim();
            return t;
        }

        String[] lines = text.split("\\r?\\n");
        for (int i = 0; i < lines.length - 1; i++) {
            String line = lines[i].trim();
            if (line.matches("(?i).*certificate\\s+of\\s+(?:completion|participation|merit).*")) {
                for (int j = i + 1; j < Math.min(lines.length, i + 4); j++) {
                    String next = lines[j].trim();
                    if (!next.isEmpty() && next.length() > 5 && !next.matches("(?i).*(certify|presented|awarded).*")) {
                        return next;
                    }
                }
            }
        }
        return "";
    }

    public static boolean datesAppearConsistent(String claimedDate, String detectedDate) {
        if (claimedDate == null || claimedDate.trim().isEmpty() || detectedDate == null || detectedDate.trim().isEmpty()) {
            return true;
        }
        String cYear = "";
        String dYear = "";
        Matcher mc = Pattern.compile("(20\\d{2})").matcher(claimedDate);
        if (mc.find()) cYear = mc.group(1);

        Matcher md = Pattern.compile("(20\\d{2})").matcher(detectedDate);
        if (md.find()) dYear = md.group(1);

        if (!cYear.isEmpty() && !dYear.isEmpty() && !cYear.equals(dYear)) {
            return false;
        }
        return true;
    }

    public boolean testDatabaseConnection() {
        try (Connection con = getConnection();
             Statement stmt = con.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT 1")) {

            if (rs.next()) {
                JOptionPane.showMessageDialog(
                        this,
                        "Successfully connected to MySQL database 'kec_academic' and executed SELECT 1.",
                        "Database Connection Test",
                        JOptionPane.INFORMATION_MESSAGE
                );
                return true;
            }
        } catch (SQLException ex) {
            String sanitized = sanitizeError(ex.getMessage());
            showError("Database connection test failed", new SQLException(sanitized));
            return false;
        }
        return false;
    }

    private static String sanitizeError(String message) {
        if (message == null) return "Unknown error";
        String pwd = getDbPassword();
        if (pwd != null && !pwd.isEmpty()) {
            message = message.replace(pwd, "******");
        }
        return message;
    }

    private void showError(String message, Exception ex) {
        String detail = (ex != null && ex.getMessage() != null) ? sanitizeError(ex.getMessage()) : "";
        JOptionPane.showMessageDialog(
                this,
                message + (detail.isEmpty() ? "" : ":\n" + detail),
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
            }

            new SAPManagement().setVisible(true);
        });
    }
}
