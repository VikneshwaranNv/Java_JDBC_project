import javax.swing.*;
import javax.swing.table.DefaultTableModel;
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

    private JLabel bonusStatusLabel;
    private JTable activityTable;
    private DefaultTableModel tableModel;

    // Certificate Analysis & Pre-Check UI Components
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
    private JButton analyzeCertButton;

    public SAPManagement() {
        setTitle("KEC ACADEMIX - SAP Activity & Certificate Verification");
        setSize(1240, 780);
        setMinimumSize(new Dimension(1000, 650));
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        buildUI();
        loadStudentsInBackground();
        loadActivitiesInBackground();

        setVisible(true);
    }

    private Connection getConnection() throws SQLException {
        return DriverManager.getConnection(
                getDbUrl(), getDbUser(), getDbPassword()
        );
    }

    private void buildUI() {
        setLayout(new BorderLayout(8, 8));

        JPanel topPanel = new JPanel(new BorderLayout(5, 5));
        topPanel.setBorder(BorderFactory.createEmptyBorder(10, 16, 4, 16));

        JLabel heading = new JLabel(
                "Student Activity Points (SAP) - Activity & Certificate Review",
                SwingConstants.LEFT
        );
        heading.setFont(new Font("SansSerif", Font.BOLD, 20));
        heading.setForeground(new Color(23, 43, 77));

        bonusStatusLabel = new JLabel("Select a student to check approved SAP points & bonus level.");
        bonusStatusLabel.setFont(new Font("SansSerif", Font.PLAIN, 13));
        bonusStatusLabel.setForeground(new Color(70, 85, 110));

        topPanel.add(heading, BorderLayout.NORTH);
        topPanel.add(bonusStatusLabel, BorderLayout.SOUTH);
        add(topPanel, BorderLayout.NORTH);

        // ------------------ Left Panel: Submission Form ------------------
        JPanel form = new JPanel(new GridBagLayout());
        form.setBorder(BorderFactory.createTitledBorder("Submit Activity"));

        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(4, 8, 4, 8);
        g.fill = GridBagConstraints.HORIZONTAL;

        studentBox = new JComboBox<>();
        studentBox.addActionListener(e -> updateBonusLevelForSelectedStudent());

        activityField = new JTextField(18);
        typeBox = new JComboBox<>(new String[]{
                "TECHNICAL", "NON_TECHNICAL", "WORKSHOP", "OTHER"
        });
        dateField = new JTextField(java.time.LocalDate.now().toString(), 10);
        organizerField = new JTextField(18);
        pointsField = new JTextField("0", 6);
        descriptionArea = new JTextArea(3, 18);
        descriptionArea.setLineWrap(true);
        descriptionArea.setWrapStyleWord(true);

        certFileField = new JTextField("No file chosen", 12);
        certFileField.setEditable(false);
        browseCertButton = new JButton("Browse...");
        browseCertButton.addActionListener(e -> chooseCertificateFile());

        JPanel certPanel = new JPanel(new BorderLayout(5, 0));
        certPanel.add(certFileField, BorderLayout.CENTER);
        certPanel.add(browseCertButton, BorderLayout.EAST);

        addField(form, g, 0, "Student:", studentBox);
        addField(form, g, 1, "Activity Name:", activityField);
        addField(form, g, 2, "Activity Type:", typeBox);
        addField(form, g, 3, "Activity Date (YYYY-MM-DD):", dateField);
        addField(form, g, 4, "Organizer:", organizerField);
        addField(form, g, 5, "Claimed Points (0-150):", pointsField);
        addField(form, g, 6, "Certificate File (PDF/Img):", certPanel);
        addField(form, g, 7, "Description:", new JScrollPane(descriptionArea));

        JButton submitButton = new JButton("Submit for Review");
        submitButton.setFont(new Font("SansSerif", Font.BOLD, 12));
        submitButton.addActionListener(e -> submitActivity());

        JButton refreshButton = new JButton("Refresh");
        refreshButton.addActionListener(e -> {
            loadStudentsInBackground();
            loadActivitiesInBackground();
        });

        JButton testDbButton = new JButton("Test Database Connection");
        testDbButton.addActionListener(e -> testDatabaseConnection());

        JPanel formButtons = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 6));
        formButtons.add(submitButton);
        formButtons.add(refreshButton);
        formButtons.add(testDbButton);

        g.gridx = 0;
        g.gridy = 8;
        g.gridwidth = 2;
        form.add(formButtons, g);

        // ------------------ Right Panel: Table + Certificate Pre-Check Details ------------------
        tableModel = new DefaultTableModel(
                new String[]{
                        "ID", "Roll No", "Student", "Activity",
                        "Type", "Date", "Claimed", "Approved", "Status",
                        "Analysis", "Eligibility"
                }, 0
        ) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        activityTable = new JTable(tableModel);
        activityTable.setRowHeight(22);
        activityTable.setAutoCreateRowSorter(true);
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
        tableScroll.setBorder(BorderFactory.createTitledBorder("Activity Submissions"));
        tableScroll.setPreferredSize(new Dimension(700, 210));

        // Certificate Analysis & Pre-Check Details Panel
        JPanel analysisPanel = buildAnalysisPanel();

        JSplitPane rightSplit = new JSplitPane(JSplitPane.VERTICAL_SPLIT, tableScroll, analysisPanel);
        rightSplit.setResizeWeight(0.40);
        rightSplit.setDividerLocation(220);

        // Review Action Buttons Toolbar
        analyzeCertButton = new JButton("Analyze Certificate");
        analyzeCertButton.setFont(new Font("SansSerif", Font.BOLD, 12));
        analyzeCertButton.setToolTipText("Run text extraction and preliminary eligibility pre-checks on selected certificate");
        analyzeCertButton.addActionListener(e -> analyzeSelectedActivity());

        JButton approveButton = new JButton("Approve Selected");
        approveButton.setFont(new Font("SansSerif", Font.BOLD, 12));
        approveButton.addActionListener(e -> reviewActivity(true));

        JButton rejectButton = new JButton("Reject Selected");
        rejectButton.setFont(new Font("SansSerif", Font.PLAIN, 12));
        rejectButton.addActionListener(e -> reviewActivity(false));

        JPanel reviewButtons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 6));
        reviewButtons.add(analyzeCertButton);
        reviewButtons.add(Box.createHorizontalStrut(15));
        reviewButtons.add(approveButton);
        reviewButtons.add(rejectButton);

        JPanel rightContainer = new JPanel(new BorderLayout(5, 5));
        rightContainer.add(rightSplit, BorderLayout.CENTER);
        rightContainer.add(reviewButtons, BorderLayout.SOUTH);

        JSplitPane mainSplit = new JSplitPane(
                JSplitPane.HORIZONTAL_SPLIT, form, rightContainer
        );
        mainSplit.setResizeWeight(0.32);
        add(mainSplit, BorderLayout.CENTER);

        JLabel footerNote = new JLabel(
                "  Status transitions: PENDING_REVIEW -> APPROVED / REJECTED. Automated analysis is preliminary; staff verification is required."
        );
        footerNote.setFont(new Font("SansSerif", Font.PLAIN, 11));
        footerNote.setBorder(BorderFactory.createEmptyBorder(4, 10, 6, 10));
        add(footerNote, BorderLayout.SOUTH);
    }

    private JPanel buildAnalysisPanel() {
        JPanel panel = new JPanel(new BorderLayout(6, 6));
        panel.setBorder(BorderFactory.createTitledBorder("Certificate Text Extraction & Eligibility Pre-Check"));

        // Warning & Disclaimer Banner
        JPanel bannerPanel = new JPanel(new BorderLayout());
        bannerPanel.setBackground(new Color(254, 249, 231));
        bannerPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(241, 196, 15), 1),
                BorderFactory.createEmptyBorder(5, 10, 5, 10)
        ));
        JLabel warningLabel = new JLabel(
                "⚠️ Preliminary Automated Pre-Check: Advisory only. Does NOT certify document authenticity or detect fraud. Final points must be verified and awarded by authorized staff."
        );
        warningLabel.setFont(new Font("SansSerif", Font.BOLD, 11));
        warningLabel.setForeground(new Color(125, 102, 8));
        bannerPanel.add(warningLabel, BorderLayout.CENTER);
        panel.add(bannerPanel, BorderLayout.NORTH);

        // Content area: Left metadata grid, Right extracted text
        JPanel contentPanel = new JPanel(new GridLayout(1, 2, 8, 8));

        // Metadata & Pre-Check Status
        JPanel metaPanel = new JPanel(new GridBagLayout());
        GridBagConstraints mg = new GridBagConstraints();
        mg.insets = new Insets(2, 6, 2, 6);
        mg.fill = GridBagConstraints.HORIZONTAL;

        analysisStatusLabel = new JLabel("NOT_ANALYZED");
        analysisStatusLabel.setFont(new Font("SansSerif", Font.BOLD, 12));
        analysisStatusLabel.setForeground(Color.GRAY);

        eligibilityLabel = new JLabel("UNCERTAIN");
        eligibilityLabel.setFont(new Font("SansSerif", Font.BOLD, 12));
        eligibilityLabel.setForeground(Color.GRAY);

        confidenceLabel = new JLabel("-");
        confidenceLabel.setFont(new Font("SansSerif", Font.PLAIN, 12));

        duplicateLabel = new JLabel("None detected");
        duplicateLabel.setFont(new Font("SansSerif", Font.BOLD, 12));
        duplicateLabel.setForeground(new Color(20, 140, 60));

        detectedTitleField = new JTextField(15);
        detectedTitleField.setEditable(false);

        detectedOrganizerField = new JTextField(15);
        detectedOrganizerField.setEditable(false);

        detectedDateField = new JTextField(15);
        detectedDateField.setEditable(false);

        exclusionReasonLabel = new JLabel("None");
        exclusionReasonLabel.setFont(new Font("SansSerif", Font.PLAIN, 11));
        exclusionReasonLabel.setForeground(new Color(160, 40, 40));

        analysisNotesArea = new JTextArea(4, 18);
        analysisNotesArea.setLineWrap(true);
        analysisNotesArea.setWrapStyleWord(true);
        analysisNotesArea.setEditable(false);
        analysisNotesArea.setFont(new Font("SansSerif", Font.PLAIN, 11));

        addField(metaPanel, mg, 0, "Analysis Status:", analysisStatusLabel);
        addField(metaPanel, mg, 1, "Eligibility Pre-Check:", eligibilityLabel);
        addField(metaPanel, mg, 2, "Confidence Level:", confidenceLabel);
        addField(metaPanel, mg, 3, "Duplicate Hash Check:", duplicateLabel);
        addField(metaPanel, mg, 4, "Detected Title (Suggestion):", detectedTitleField);
        addField(metaPanel, mg, 5, "Detected Organizer (Suggestion):", detectedOrganizerField);
        addField(metaPanel, mg, 6, "Detected Event Date (Suggestion):", detectedDateField);
        addField(metaPanel, mg, 7, "Exclusion Reason:", exclusionReasonLabel);
        addField(metaPanel, mg, 8, "Pre-Check Notes:", new JScrollPane(analysisNotesArea));

        contentPanel.add(metaPanel);

        // Extracted Certificate Text Area
        extractedTextArea = new JTextArea();
        extractedTextArea.setLineWrap(true);
        extractedTextArea.setWrapStyleWord(true);
        extractedTextArea.setEditable(false);
        extractedTextArea.setFont(new Font("Monospaced", Font.PLAIN, 11));
        extractedTextArea.setText("[Select an activity row to view extracted certificate text]");

        JScrollPane textScroll = new JScrollPane(extractedTextArea);
        textScroll.setBorder(BorderFactory.createTitledBorder("Extracted Certificate Text (PDFBox Local Layer)"));
        contentPanel.add(textScroll);

        panel.add(contentPanel, BorderLayout.CENTER);
        return panel;
    }

    private void addField(
            JPanel panel, GridBagConstraints g,
            int row, String label, Component field
    ) {
        g.gridwidth = 1;
        g.gridx = 0;
        g.gridy = row;
        g.weightx = 0;
        JLabel lbl = new JLabel(label);
        lbl.setFont(new Font("SansSerif", Font.PLAIN, 12));
        panel.add(lbl, g);

        g.gridx = 1;
        g.weightx = 1;
        panel.add(field, g);
    }

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
                        // Scanned / raster image PDF with no selectable text layer
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
            // Image certificate: OCR not installed
            res.analysisStatus = "ANALYSIS_FAILED";
            if (!res.duplicateFlag) res.eligibilityStatus = "UNCERTAIN";
            res.analysisNotes = appendNote(res.analysisNotes, "Image certificate (" + getFileExtension(fileName).toUpperCase() + ") detected. Local OCR engine (Tesseract) is not configured in this environment; routed for manual staff review.");
        } else {
            // Unsupported file format
            res.analysisStatus = "ANALYSIS_FAILED";
            if (!res.duplicateFlag) res.eligibilityStatus = "UNCERTAIN";
            res.analysisNotes = appendNote(res.analysisNotes, "Unsupported certificate format (" + fileName + "). Requires manual staff review.");
        }

        // 4. If Text Extracted, Run Suggestion Extraction & Eligibility Classification
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

        // Append mandatory authenticity disclaimer (extracted text / OCR is never proof of authenticity)
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
        // Check context to distinguish exam/result documents from qualifying events like workshops or hackathons
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
            // If it is an NPTEL qualifying event (e.g. workshop or hackathon), it will fall through to Category 4 below
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

    private void analyzeSelectedActivity() {
        int viewRow = activityTable.getSelectedRow();
        if (viewRow < 0) {
            JOptionPane.showMessageDialog(this, "Please select an activity from the table to analyze.", "Selection Required", JOptionPane.WARNING_MESSAGE);
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
                                    "Analysis Status: %s\n" +
                                    "Eligibility Pre-Check: %s\n" +
                                    "Confidence: %.1f%%\n" +
                                    "Duplicate Flag: %s\n\n" +
                                    "Staff must review details and make the final decision.",
                            res.analysisStatus,
                            res.eligibilityStatus,
                            res.confidence,
                            res.duplicateFlag ? "YES (Duplicate Detected)" : "None"
                    );
                    JOptionPane.showMessageDialog(SAPManagement.this, summary, "Analysis Finished", JOptionPane.INFORMATION_MESSAGE);
                } catch (Exception ex) {
                    showError("Could not analyze certificate", ex);
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
                            analysisStatusLabel.setForeground(new Color(20, 140, 60));
                        } else if ("ANALYSIS_FAILED".equals(aStatus)) {
                            analysisStatusLabel.setForeground(new Color(190, 40, 40));
                        } else {
                            analysisStatusLabel.setForeground(Color.GRAY);
                        }

                        eligibilityLabel.setText(eStatus != null ? eStatus : "UNCERTAIN");
                        if ("ELIGIBLE".equals(eStatus)) {
                            eligibilityLabel.setForeground(new Color(20, 140, 60));
                        } else if ("INELIGIBLE".equals(eStatus)) {
                            eligibilityLabel.setForeground(new Color(190, 40, 40));
                        } else {
                            eligibilityLabel.setForeground(new Color(200, 120, 0));
                        }

                        confidenceLabel.setText(conf != null ? String.format("%.1f%%", conf.doubleValue()) : "-");
                        duplicateLabel.setText(dup ? "⚠️ DUPLICATE FILE DETECTED" : "No duplicate detected");
                        duplicateLabel.setForeground(dup ? new Color(190, 40, 40) : new Color(20, 140, 60));

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

    // ------------------ Helper Parsing Methods ------------------

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
        // ISO format: 2026-03-15 or 2026/03/15
        Pattern pIso = Pattern.compile("\\b(20\\d{2}[-/.]\\d{1,2}[-/.]\\d{1,2})\\b");
        Matcher m = pIso.matcher(text);
        if (m.find()) return m.group(1);

        // British/Indian format: 15/03/2026 or 15-03-2026
        Pattern pInd = Pattern.compile("\\b(\\d{1,2}[-/.]\\d{1,2}[-/.]20\\d{2})\\b");
        m = pInd.matcher(text);
        if (m.find()) return m.group(1);

        // Textual: 15th March 2026 or March 15, 2026
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

    // ------------------ Student & Bonus Loading ------------------

    private void loadStudentsInBackground() {
        SwingWorker<List<StudentItem>, Void> worker = new SwingWorker<>() {
            @Override
            protected List<StudentItem> doInBackground() throws Exception {
                List<StudentItem> list = new ArrayList<>();
                try (Connection con = getConnection();
                     PreparedStatement ps = con.prepareStatement(
                             "SELECT student_id, roll_no, student_name FROM students ORDER BY roll_no");
                     ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        list.add(new StudentItem(
                                rs.getInt("student_id"),
                                rs.getString("roll_no"),
                                rs.getString("student_name")
                        ));
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
            bonusStatusLabel.setText("Select a student to check approved SAP points & bonus level.");
            return;
        }

        SwingWorker<String, Void> worker = new SwingWorker<>() {
            @Override
            protected String doInBackground() throws Exception {
                int approvedPoints = 0;
                String bonusDesc = "No rule matched";
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

                return String.format(
                        "Student: %s (%s)  |  Approved SAP Points: %d  |  Bonus: Level %d (%s)",
                        student.studentName, student.rollNo, approvedPoints, bonusLevel, bonusDesc
                );
            }

            @Override
            protected void done() {
                try {
                    bonusStatusLabel.setText(get());
                } catch (Exception ex) {
                    bonusStatusLabel.setText("Could not calculate bonus level: " + sanitizeError(ex.getMessage()));
                }
            }
        };
        worker.execute();
    }

    // ------------------ Submission Workflow ------------------

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

                    // Automatically perform local pre-check extraction & save to database
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

    private void loadActivitiesInBackground() {
        if (tableModel == null) return;

        SwingWorker<List<Object[]>, Void> worker = new SwingWorker<>() {
            @Override
            protected List<Object[]> doInBackground() throws Exception {
                List<Object[]> rows = new ArrayList<>();
                String sql = """
                        SELECT a.activity_id, s.roll_no, s.student_name,
                               a.activity_name, a.activity_type, a.activity_date,
                               a.claimed_points, a.approved_points, a.status,
                               a.analysis_status, a.eligibility_status
                        FROM sap_activities a
                        JOIN students s ON s.student_id = a.student_id
                        ORDER BY a.created_at DESC
                        """;

                try (Connection con = getConnection();
                     PreparedStatement ps = con.prepareStatement(sql);
                     ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
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
                } catch (Exception ex) {
                    showError("Could not load activities", ex);
                }
            }
        };
        worker.execute();
    }

    // ------------------ Staff Review Workflow ------------------

    private void reviewActivity(boolean approve) {
        int viewRow = activityTable.getSelectedRow();
        if (viewRow < 0) {
            JOptionPane.showMessageDialog(this, "Select an activity row from the table first.", "Selection Required", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int row = activityTable.convertRowIndexToModel(viewRow);
        int activityId = (int) tableModel.getValueAt(row, 0);
        int claimed = (int) tableModel.getValueAt(row, 6);
        String currentStatus = String.valueOf(tableModel.getValueAt(row, 8));

        if (!"PENDING_REVIEW".equals(currentStatus) && !"PENDING_ANALYSIS".equals(currentStatus)) {
            JOptionPane.showMessageDialog(this, "This activity has already been reviewed (Status: " + currentStatus + ").", "Already Processed", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int approvedPoints = 0;
        if (approve) {
            String entered = JOptionPane.showInputDialog(
                    this,
                    "Enter points approved by staff (0–150):",
                    String.valueOf(claimed)
            );
            if (entered == null) return;

            try {
                approvedPoints = Integer.parseInt(entered.trim());
                if (approvedPoints < 0 || approvedPoints > 150) {
                    throw new NumberFormatException();
                }
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Approved points must be a whole number between 0 and 150.", "Invalid Points", JOptionPane.WARNING_MESSAGE);
                return;
            }
        }

        String comments = JOptionPane.showInputDialog(this, "Review comments (optional):");
        if (comments == null) comments = "";

        final int finalApproved = approvedPoints;
        final String finalComments = comments;
        final String action = approve ? "APPROVED" : "REJECTED";
        final String newStatus = approve ? "APPROVED" : "REJECTED";
        final String reviewerId = "STAFF_REVIEWER";

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
                                "Review recorded successfully in database transaction.\nNew Status: " + newStatus,
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

    // ------------------ DB Diagnostics & Utilities ------------------

    public boolean testDatabaseConnection() {
        try (Connection con = getConnection();
             Statement stmt = con.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT 1")) {

            if (rs.next()) {
                JOptionPane.showMessageDialog(
                        this,
                        "Successfully connected to kec_academic and executed SELECT 1.",
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
