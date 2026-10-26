import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.sql.SQLException;

/**
 * Professional Login, Student Registration, and Setup Portal for KEC ACADEMIX.
 * Engineered for high-contrast visibility on Windows Look & Feel, prestigious academic branding,
 * full support for usernames with spaces, and real-time student enrollment verification.
 */
public class AuthDialog extends JDialog {

    // Official KEC Color Palette
    public static final Color COLOR_NAVY = KECTheme.DEEP_NAVY;
    public static final Color COLOR_NAVY_HOVER = KECTheme.DEEP_NAVY_HOVER;
    public static final Color COLOR_NAVY_PRESSED = KECTheme.DEEP_NAVY_PRESSED;
    public static final Color COLOR_CYAN = KECTheme.KEC_CYAN;
    public static final Color COLOR_CYAN_HOVER = KECTheme.KEC_CYAN_HOVER;
    public static final Color COLOR_CYAN_PRESSED = KECTheme.KEC_CYAN_PRESSED;
    public static final Color COLOR_GOLD = new Color(212, 160, 23);
    public static final Color COLOR_GOLD_LIGHT = new Color(254, 243, 199);
    public static final Color COLOR_MUTED_BLUE = KECTheme.KEC_CYAN;
    public static final Color COLOR_WHITE = KECTheme.WHITE;
    public static final Color COLOR_BG = KECTheme.BG_LIGHT;
    public static final Color COLOR_CARD_BG = Color.WHITE;
    public static final Color COLOR_BORDER = KECTheme.BORDER_GREY;
    public static final Color COLOR_BORDER_FOCUS = KECTheme.KEC_CYAN;
    public static final Color COLOR_TEXT_DARK = KECTheme.TEXT_DARK;
    public static final Color COLOR_TEXT_MUTED = KECTheme.TEXT_MUTED;

    public static final Color COLOR_GREEN = KECTheme.KEC_GREEN;
    public static final Color COLOR_GREEN_HOVER = KECTheme.KEC_GREEN_HOVER;
    public static final Color COLOR_GREEN_BG = KECTheme.KEC_GREEN_LIGHT;
    public static final Color COLOR_GREEN_BORDER = new Color(139, 195, 74, 180);

    public static final Color COLOR_RED = KECTheme.ERROR_RED;
    public static final Color COLOR_RED_BG = KECTheme.ERROR_BG;
    public static final Color COLOR_RED_BORDER = new Color(245, 178, 176);

    public static final Color COLOR_AMBER = KECTheme.WARNING_AMBER;
    public static final Color COLOR_AMBER_BG = KECTheme.WARNING_BG;
    public static final Color COLOR_AMBER_BORDER = new Color(252, 211, 77);

    // Typography
    public static final Font FONT_HEADER_TITLE = new Font("Segoe UI", Font.BOLD, 23);
    public static final Font FONT_HEADER_SUB = new Font("Segoe UI", Font.PLAIN, 12);
    public static final Font FONT_SECTION = new Font("Segoe UI", Font.BOLD, 16);
    public static final Font FONT_BOLD = new Font("Segoe UI", Font.BOLD, 13);
    public static final Font FONT_REGULAR = new Font("Segoe UI", Font.PLAIN, 13);
    public static final Font FONT_SMALL = new Font("Segoe UI", Font.PLAIN, 12);
    public static final Font FONT_BADGE = new Font("Segoe UI", Font.BOLD, 11);

    private final CardLayout cardLayout = new CardLayout();
    private final JPanel cardsPanel = new JPanel(cardLayout);

    // Login Form Components
    private JTextField loginUserField;
    private JPasswordField loginPassField;
    private PrimaryButton loginButton;
    private JLabel loginStatusLabel;
    private JPanel firstRunAdminBanner;

    // Student Signup Form Components
    private JTextField signupUserField;
    private JTextField signupRollField;
    private JPanel studentVerificationCard;
    private JLabel verifyTitleLabel;
    private JLabel verifyDetailsLabel;
    private JPasswordField signupPassField;
    private JPasswordField signupConfirmField;
    private PrimaryButton signupButton;
    private JLabel signupStatusLabel;

    // First-Run Admin Setup Components
    private JTextField adminUserField;
    private JPasswordField adminPassField;
    private JPasswordField adminConfirmField;
    private PrimaryButton adminSetupButton;
    private JLabel adminStatusLabel;

    private UserSession authenticatedSession = null;

    /**
     * Custom Primary Button guaranteeing rich solid cyan background and crisp white text
     * on all operating systems and Look and Feels (completely prevents native Windows L&F wash-out).
     */
    public static class PrimaryButton extends JButton {
        private final Color normalBg;
        private final Color hoverBg;
        private final Color pressedBg;

        public PrimaryButton(String text) {
            this(text, COLOR_CYAN, COLOR_CYAN_HOVER, COLOR_CYAN_PRESSED);
        }

        public PrimaryButton(String text, Color normal, Color hover, Color pressed) {
            super(text);
            this.normalBg = normal;
            this.hoverBg = hover;
            this.pressedBg = pressed;
            setFont(new Font("Segoe UI", Font.BOLD, 14));
            setForeground(COLOR_WHITE);
            setContentAreaFilled(false);
            setFocusPainted(false);
            setBorderPainted(false);
            setOpaque(false);
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            setBorder(new EmptyBorder(10, 18, 10, 18));
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

            int w = getWidth();
            int h = getHeight();

            if (!isEnabled()) {
                g2.setColor(new Color(225, 230, 236));
            } else if (getModel().isPressed()) {
                g2.setColor(pressedBg);
            } else if (getModel().isRollover()) {
                g2.setColor(hoverBg);
            } else {
                g2.setColor(normalBg);
            }

            g2.fillRoundRect(0, 0, w, h, 10, 10);

            // Accessible focus outline
            if (isFocusOwner()) {
                g2.setColor(new Color(255, 255, 255, 180));
                g2.setStroke(new BasicStroke(1.8f));
                g2.drawRoundRect(2, 2, w - 5, h - 5, 8, 8);
            }

            FontMetrics fm = g2.getFontMetrics(getFont());
            Rectangle viewRect = new Rectangle(0, 0, w, h);
            Rectangle iconRect = new Rectangle();
            Rectangle textRect = new Rectangle();

            String layoutText = SwingUtilities.layoutCompoundLabel(
                    this, fm, getText(), getIcon(),
                    getVerticalAlignment(), getHorizontalAlignment(),
                    getVerticalTextPosition(), getHorizontalTextPosition(),
                    viewRect, iconRect, textRect,
                    getIconTextGap()
            );

            if (getIcon() != null) {
                getIcon().paintIcon(this, g2, iconRect.x, iconRect.y);
            }

            if (layoutText != null && !layoutText.isEmpty()) {
                g2.setFont(getFont());
                g2.setColor(isEnabled() ? getForeground() : new Color(150, 165, 180));
                g2.drawString(layoutText, textRect.x, textRect.y + fm.getAscent());
            }

            g2.dispose();
        }
    }

    /**
     * Custom Secondary Button with crisp border, clean white fill, and navy text.
     */
    public static class SecondaryButton extends JButton {
        public SecondaryButton(String text) {
            super(text);
            setFont(new Font("Segoe UI", Font.BOLD, 13));
            setForeground(COLOR_NAVY);
            setContentAreaFilled(false);
            setFocusPainted(false);
            setBorderPainted(false);
            setOpaque(false);
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            setBorder(new EmptyBorder(8, 14, 8, 14));
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

            int w = getWidth();
            int h = getHeight();

            if (!isEnabled()) {
                g2.setColor(new Color(245, 247, 250));
            } else if (getModel().isPressed()) {
                g2.setColor(new Color(225, 233, 245));
            } else if (getModel().isRollover()) {
                g2.setColor(new Color(240, 244, 250));
            } else {
                g2.setColor(COLOR_WHITE);
            }

            g2.fillRoundRect(0, 0, w - 1, h - 1, 10, 10);

            // Border
            g2.setColor(!isEnabled() ? new Color(220, 226, 235) : (getModel().isRollover() ? COLOR_NAVY : new Color(205, 215, 228)));
            g2.setStroke(new BasicStroke(1.2f));
            g2.drawRoundRect(0, 0, w - 1, h - 1, 10, 10);

            // Accessible focus outline
            if (isFocusOwner()) {
                g2.setColor(new Color(0, 165, 216, 160));
                g2.setStroke(new BasicStroke(1.8f));
                g2.drawRoundRect(2, 2, w - 5, h - 5, 8, 8);
            }

            FontMetrics fm = g2.getFontMetrics(getFont());
            Rectangle viewRect = new Rectangle(0, 0, w, h);
            Rectangle iconRect = new Rectangle();
            Rectangle textRect = new Rectangle();

            String layoutText = SwingUtilities.layoutCompoundLabel(
                    this, fm, getText(), getIcon(),
                    getVerticalAlignment(), getHorizontalAlignment(),
                    getVerticalTextPosition(), getHorizontalTextPosition(),
                    viewRect, iconRect, textRect,
                    getIconTextGap()
            );

            if (getIcon() != null) {
                getIcon().paintIcon(this, g2, iconRect.x, iconRect.y);
            }

            if (layoutText != null && !layoutText.isEmpty()) {
                g2.setFont(getFont());
                g2.setColor(isEnabled() ? getForeground() : new Color(160, 172, 188));
                g2.drawString(layoutText, textRect.x, textRect.y + fm.getAscent());
            }

            g2.dispose();
        }
    }

    public AuthDialog(Frame parent) {
        super(parent, "KEC ACADEMIX — Secure Portal Authentication", true);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setSize(580, 740);
        setMinimumSize(new Dimension(520, 680));
        setLocationRelativeTo(parent);
        setResizable(true);

        buildUI();
        checkFirstRunAdminStatus();
    }

    private void buildUI() {
        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(COLOR_BG);

        // Header Panel (Navy Blue with KEC Logo and Branding)
        JPanel header = new JPanel();
        header.setLayout(new BoxLayout(header, BoxLayout.Y_AXIS));
        header.setBackground(COLOR_NAVY);
        header.setBorder(new EmptyBorder(16, 28, 14, 28));

        ImageIcon logoIcon = KECTheme.getKECLogo(180, 56);
        if (logoIcon != null) {
            JLabel logoLabel = new JLabel(logoIcon);
            logoLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
            header.add(logoLabel);
            header.add(Box.createVerticalStrut(10));
        }

        JLabel logo = new JLabel("KEC ACADEMIX");
        logo.setFont(FONT_HEADER_TITLE);
        logo.setForeground(COLOR_WHITE);
        logo.setAlignmentX(Component.CENTER_ALIGNMENT);
        header.add(logo);
        header.add(Box.createVerticalStrut(3));

        JLabel sub = new JLabel("Academic & Continuous Evaluation Management System");
        sub.setFont(new Font("Segoe UI", Font.BOLD, 13));
        sub.setForeground(COLOR_GREEN);
        sub.setAlignmentX(Component.CENTER_ALIGNMENT);
        header.add(sub);
        header.add(Box.createVerticalStrut(4));

        JLabel affiliation = new JLabel("Kongu Engineering College (Autonomous) • Affiliated to Anna University");
        affiliation.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        affiliation.setForeground(new Color(190, 210, 238));
        affiliation.setAlignmentX(Component.CENTER_ALIGNMENT);
        header.add(affiliation);

        // Dual brand accent stripe (Green + Cyan)
        JPanel headerWrapper = new JPanel(new BorderLayout());
        headerWrapper.add(header, BorderLayout.CENTER);

        JPanel accentStripe = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                int w = getWidth();
                int h = getHeight();
                int greenW = (int) (w * 0.35);
                g.setColor(COLOR_GREEN);
                g.fillRect(0, 0, greenW, h);
                g.setColor(COLOR_CYAN);
                g.fillRect(greenW, 0, w - greenW, h);
            }
        };
        accentStripe.setPreferredSize(new Dimension(0, 4));
        headerWrapper.add(accentStripe, BorderLayout.SOUTH);

        root.add(headerWrapper, BorderLayout.NORTH);

        // Cards Panel
        cardsPanel.setBackground(COLOR_BG);
        cardsPanel.setBorder(new EmptyBorder(14, 22, 16, 22));

        cardsPanel.add(wrapInScroll(buildLoginCard()), "LOGIN");
        cardsPanel.add(wrapInScroll(buildSignupCard()), "SIGNUP");
        cardsPanel.add(wrapInScroll(buildAdminSetupCard()), "ADMIN_SETUP");

        root.add(cardsPanel, BorderLayout.CENTER);

        setContentPane(root);
    }

    private JScrollPane wrapInScroll(JPanel panel) {
        JScrollPane sp = new JScrollPane(panel);
        sp.setBorder(null);
        sp.setBackground(COLOR_BG);
        sp.getViewport().setBackground(COLOR_BG);
        sp.getVerticalScrollBar().setUnitIncrement(16);
        return sp;
    }

    private JPanel buildLoginCard() {
        JPanel card = createCardContainer();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));

        // Academic Evaluation Role Indicators
        JPanel rolesRow = new JPanel(new FlowLayout(FlowLayout.CENTER, 8, 0));
        rolesRow.setOpaque(false);
        rolesRow.setAlignmentX(Component.LEFT_ALIGNMENT);
        rolesRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 26));

        rolesRow.add(createRolePill("🎓 Student", new Color(227, 242, 253), new Color(13, 71, 161)));
        rolesRow.add(createRolePill("👨‍🏫 Faculty / Staff", new Color(243, 232, 255), new Color(107, 33, 168)));
        rolesRow.add(createRolePill("⚙ Administrator", new Color(254, 243, 199), new Color(146, 64, 14)));
        card.add(rolesRow);
        card.add(Box.createVerticalStrut(14));

        JLabel title = new JLabel("Sign In to Your Academic Account");
        title.setFont(FONT_SECTION);
        title.setForeground(COLOR_TEXT_DARK);
        title.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.add(title);
        card.add(Box.createVerticalStrut(3));

        JLabel desc = new JLabel("Enter your username and password to access evaluations, marks, and SAP.");
        desc.setFont(FONT_SMALL);
        desc.setForeground(COLOR_TEXT_MUTED);
        desc.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.add(desc);

        // First-Run Admin notice banner (hidden by default)
        firstRunAdminBanner = new JPanel(new BorderLayout(8, 0));
        firstRunAdminBanner.setBackground(COLOR_AMBER_BG);
        firstRunAdminBanner.setBorder(new CompoundBorder(
                new LineBorder(COLOR_AMBER_BORDER, 1, true),
                new EmptyBorder(8, 12, 8, 12)
        ));
        firstRunAdminBanner.setAlignmentX(Component.LEFT_ALIGNMENT);
        firstRunAdminBanner.setMaximumSize(new Dimension(Integer.MAX_VALUE, 48));

        JLabel bannerText = new JLabel("No Administrator found. Initial setup required.");
        bannerText.setFont(new Font("Segoe UI", Font.BOLD, 12));
        bannerText.setForeground(COLOR_AMBER);

        SecondaryButton setupBtn = new SecondaryButton("Set Up Admin");
        setupBtn.addActionListener(e -> cardLayout.show(cardsPanel, "ADMIN_SETUP"));

        firstRunAdminBanner.add(bannerText, BorderLayout.CENTER);
        firstRunAdminBanner.add(setupBtn, BorderLayout.EAST);
        firstRunAdminBanner.setVisible(false);

        card.add(Box.createVerticalStrut(12));
        card.add(firstRunAdminBanner);
        card.add(Box.createVerticalStrut(14));

        // Username
        card.add(createFieldLabel("Username (or Full Name)"));
        loginUserField = createTextField("Enter username or name (spaces allowed)");
        card.add(loginUserField);
        card.add(Box.createVerticalStrut(12));

        // Password
        card.add(createFieldLabel("Password"));
        loginPassField = createPasswordField();
        card.add(loginPassField);
        card.add(Box.createVerticalStrut(14));

        // Status Label
        loginStatusLabel = new JLabel(" ");
        loginStatusLabel.setFont(FONT_SMALL);
        loginStatusLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.add(loginStatusLabel);
        card.add(Box.createVerticalStrut(10));

        // Login Button - High Contrast Custom Primary Button
        loginButton = new PrimaryButton("Sign In to Portal  ➔");
        loginButton.setAlignmentX(Component.LEFT_ALIGNMENT);
        loginButton.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
        loginButton.setPreferredSize(new Dimension(300, 44));
        loginButton.addActionListener(e -> executeLogin());
        card.add(loginButton);

        KeyAdapter enterListener = new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    executeLogin();
                }
            }
        };
        loginUserField.addKeyListener(enterListener);
        loginPassField.addKeyListener(enterListener);

        card.add(Box.createVerticalStrut(20));
        JSeparator sep = new JSeparator();
        sep.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));
        sep.setForeground(COLOR_BORDER);
        card.add(sep);
        card.add(Box.createVerticalStrut(14));

        // Switch to Student Signup
        JPanel signupPrompt = new JPanel(new FlowLayout(FlowLayout.CENTER, 6, 0));
        signupPrompt.setOpaque(false);
        signupPrompt.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel promptLabel = new JLabel("Enrolled student without an account?");
        promptLabel.setFont(FONT_REGULAR);
        promptLabel.setForeground(COLOR_TEXT_MUTED);

        JButton goToSignup = new JButton("Register Student Account");
        styleLinkButton(goToSignup);
        goToSignup.addActionListener(e -> {
            clearSignupFields();
            cardLayout.show(cardsPanel, "SIGNUP");
            signupUserField.requestFocusInWindow();
        });

        signupPrompt.add(promptLabel);
        signupPrompt.add(goToSignup);
        card.add(signupPrompt);

        card.add(Box.createVerticalGlue());
        return card;
    }

    private JPanel buildSignupCard() {
        JPanel card = createCardContainer();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));

        JLabel title = new JLabel("Student Account Registration");
        title.setFont(FONT_SECTION);
        title.setForeground(COLOR_TEXT_DARK);
        title.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.add(title);
        card.add(Box.createVerticalStrut(2));

        JLabel desc = new JLabel("Public registration is strictly permitted for enrolled KEC students.");
        desc.setFont(FONT_SMALL);
        desc.setForeground(COLOR_TEXT_MUTED);
        desc.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.add(desc);

        JLabel subNote = new JLabel("Faculty, Staff, and Admin accounts are provisioned directly by College Administration.");
        subNote.setFont(new Font("Segoe UI", Font.ITALIC, 11));
        subNote.setForeground(COLOR_MUTED_BLUE);
        subNote.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.add(subNote);
        card.add(Box.createVerticalStrut(12));

        // Username (Explicitly stating spaces are permitted)
        card.add(createFieldLabel("Desired Username (Full Name or ID — spaces allowed)"));
        signupUserField = createTextField("e.g., Vikneshwaran N or viknesh_n (3-40 characters)");
        card.add(signupUserField);
        card.add(Box.createVerticalStrut(10));

        // Roll Number with Verification feedback
        JPanel rollLabelPanel = new JPanel(new BorderLayout());
        rollLabelPanel.setOpaque(false);
        rollLabelPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        rollLabelPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 20));

        JLabel rollLabel = new JLabel("KEC Student Roll Number");
        rollLabel.setFont(FONT_BOLD);
        rollLabel.setForeground(COLOR_TEXT_DARK);

        JLabel rollHelp = new JLabel("e.g., 25ITR001–25ITR182, or Lateral 25ITL123");
        rollHelp.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        rollHelp.setForeground(COLOR_TEXT_MUTED);

        rollLabelPanel.add(rollLabel, BorderLayout.WEST);
        rollLabelPanel.add(rollHelp, BorderLayout.EAST);
        card.add(rollLabelPanel);
        card.add(Box.createVerticalStrut(3));

        signupRollField = createTextField("e.g., 25ITR161 or 25ITL123");
        card.add(signupRollField);

        // Dynamic Student Verification Feedback Card
        studentVerificationCard = new JPanel();
        studentVerificationCard.setLayout(new BoxLayout(studentVerificationCard, BoxLayout.Y_AXIS));
        studentVerificationCard.setBackground(COLOR_GREEN_BG);
        studentVerificationCard.setBorder(new CompoundBorder(
                new LineBorder(COLOR_GREEN_BORDER, 1, true),
                new EmptyBorder(6, 12, 6, 12)
        ));
        studentVerificationCard.setAlignmentX(Component.LEFT_ALIGNMENT);
        studentVerificationCard.setMaximumSize(new Dimension(Integer.MAX_VALUE, 50));
        studentVerificationCard.setVisible(false);

        verifyTitleLabel = new JLabel("✔ Verified Student Record");
        verifyTitleLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        verifyTitleLabel.setForeground(COLOR_GREEN);

        verifyDetailsLabel = new JLabel("");
        verifyDetailsLabel.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        verifyDetailsLabel.setForeground(COLOR_TEXT_DARK);

        studentVerificationCard.add(verifyTitleLabel);
        studentVerificationCard.add(Box.createVerticalStrut(2));
        studentVerificationCard.add(verifyDetailsLabel);

        card.add(Box.createVerticalStrut(4));
        card.add(studentVerificationCard);
        card.add(Box.createVerticalStrut(8));

        // Wire live verification when student tabs out or presses Enter on roll number field
        signupRollField.addFocusListener(new FocusAdapter() {
            @Override
            public void focusLost(FocusEvent e) {
                verifyRollNumberLive();
            }
        });
        signupRollField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    verifyRollNumberLive();
                    signupPassField.requestFocusInWindow();
                }
            }
        });

        // Password
        card.add(createFieldLabel("Password (Min 8 chars, letters and digits)"));
        signupPassField = createPasswordField();
        card.add(signupPassField);
        card.add(Box.createVerticalStrut(10));

        // Confirm Password
        card.add(createFieldLabel("Confirm Password"));
        signupConfirmField = createPasswordField();
        card.add(signupConfirmField);
        card.add(Box.createVerticalStrut(10));

        // Status Label
        signupStatusLabel = new JLabel(" ");
        signupStatusLabel.setFont(FONT_SMALL);
        signupStatusLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.add(signupStatusLabel);
        card.add(Box.createVerticalStrut(8));

        // Primary Signup Button - High Contrast Custom Primary Button
        signupButton = new PrimaryButton("✔  Register Student Account");
        signupButton.setAlignmentX(Component.LEFT_ALIGNMENT);
        signupButton.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
        signupButton.setPreferredSize(new Dimension(300, 44));
        signupButton.addActionListener(e -> executeStudentSignup());
        card.add(signupButton);

        card.add(Box.createVerticalStrut(8));

        // Secondary Back Button
        SecondaryButton backBtn = new SecondaryButton("←  Back to Sign In");
        backBtn.setAlignmentX(Component.LEFT_ALIGNMENT);
        backBtn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        backBtn.setPreferredSize(new Dimension(300, 38));
        backBtn.addActionListener(e -> {
            cardLayout.show(cardsPanel, "LOGIN");
            loginUserField.requestFocusInWindow();
        });
        card.add(backBtn);

        card.add(Box.createVerticalGlue());
        return card;
    }

    private JPanel buildAdminSetupCard() {
        JPanel card = createCardContainer();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));

        JLabel title = new JLabel("Initial Administrator Setup");
        title.setFont(FONT_SECTION);
        title.setForeground(COLOR_TEXT_DARK);
        title.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.add(title);
        card.add(Box.createVerticalStrut(2));

        JLabel desc = new JLabel("No active administrator was detected. Provision the primary system administrator.");
        desc.setFont(FONT_SMALL);
        desc.setForeground(COLOR_TEXT_MUTED);
        desc.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.add(desc);
        card.add(Box.createVerticalStrut(14));

        // Username
        card.add(createFieldLabel("Administrator Username (spaces allowed)"));
        adminUserField = createTextField("e.g., admin or Academic Admin");
        card.add(adminUserField);
        card.add(Box.createVerticalStrut(12));

        // Password
        card.add(createFieldLabel("Administrator Password (Min 8 chars, letters and digits)"));
        adminPassField = createPasswordField();
        card.add(adminPassField);
        card.add(Box.createVerticalStrut(12));

        // Confirm
        card.add(createFieldLabel("Confirm Password"));
        adminConfirmField = createPasswordField();
        card.add(adminConfirmField);
        card.add(Box.createVerticalStrut(12));

        adminStatusLabel = new JLabel(" ");
        adminStatusLabel.setFont(FONT_SMALL);
        adminStatusLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.add(adminStatusLabel);
        card.add(Box.createVerticalStrut(10));

        adminSetupButton = new PrimaryButton("Create Primary Administrator  ➔");
        adminSetupButton.setAlignmentX(Component.LEFT_ALIGNMENT);
        adminSetupButton.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
        adminSetupButton.setPreferredSize(new Dimension(300, 44));
        adminSetupButton.addActionListener(e -> executeAdminSetup());
        card.add(adminSetupButton);

        card.add(Box.createVerticalStrut(8));

        SecondaryButton backBtn = new SecondaryButton("← Cancel & Back to Sign In");
        backBtn.setAlignmentX(Component.LEFT_ALIGNMENT);
        backBtn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        backBtn.setPreferredSize(new Dimension(300, 38));
        backBtn.addActionListener(e -> cardLayout.show(cardsPanel, "LOGIN"));
        card.add(backBtn);

        card.add(Box.createVerticalGlue());
        return card;
    }

    /**
     * Performs a live lookup of the entered student roll number to preview enrollment data.
     */
    private void verifyRollNumberLive() {
        String roll = signupRollField.getText().trim();
        if (roll.isEmpty()) {
            studentVerificationCard.setVisible(false);
            return;
        }

        SwingWorker<AuthService.StudentVerificationResult, Void> worker = new SwingWorker<>() {
            @Override
            protected AuthService.StudentVerificationResult doInBackground() throws Exception {
                return AuthService.verifyStudentRoll(roll);
            }

            @Override
            protected void done() {
                try {
                    AuthService.StudentVerificationResult res = get();
                    if (res == null) {
                        studentVerificationCard.setVisible(false);
                        return;
                    }
                    if (res.found && !res.alreadyRegistered) {
                        studentVerificationCard.setBackground(COLOR_GREEN_BG);
                        studentVerificationCard.setBorder(new CompoundBorder(
                                new LineBorder(COLOR_GREEN_BORDER, 1, true),
                                new EmptyBorder(6, 12, 6, 12)
                        ));
                        if (res.isNewEnrollment || res.studentName == null) {
                            String cat = (res.category != null) ? res.category : "Enrolled";
                            verifyTitleLabel.setText("✔ Eligible KEC Student Roll: " + res.rollNo);
                            verifyTitleLabel.setForeground(COLOR_GREEN);
                            verifyDetailsLabel.setText(String.format("Department: %s  |  %s  |  Semester %d (Ready to Register)",
                                    res.department, cat, res.semester));
                        } else {
                            verifyTitleLabel.setText("✔ Verified KEC Student Record");
                            verifyTitleLabel.setForeground(COLOR_GREEN);
                            verifyDetailsLabel.setText(String.format("Name: %s  |  Dept: %s  |  Semester %d",
                                    res.studentName, res.department, res.semester));
                            if (signupUserField.getText().trim().isEmpty()) {
                                signupUserField.setText(res.studentName);
                            }
                        }
                        studentVerificationCard.setVisible(true);

                    } else if (res.found && res.alreadyRegistered) {
                        studentVerificationCard.setBackground(COLOR_AMBER_BG);
                        studentVerificationCard.setBorder(new CompoundBorder(
                                new LineBorder(COLOR_AMBER_BORDER, 1, true),
                                new EmptyBorder(6, 12, 6, 12)
                        ));
                        verifyTitleLabel.setText("⚠ Account Already Registered");
                        verifyTitleLabel.setForeground(COLOR_AMBER);
                        String sName = (res.studentName != null && !res.studentName.isEmpty())
                                ? ("Student '" + res.studentName + "'")
                                : ("Roll '" + res.rollNo + "'");
                        verifyDetailsLabel.setText(String.format("%s is already linked to user '%s'. Please sign in.",
                                sName, res.existingUsername));
                        studentVerificationCard.setVisible(true);

                    } else {
                        studentVerificationCard.setBackground(COLOR_RED_BG);
                        studentVerificationCard.setBorder(new CompoundBorder(
                                new LineBorder(COLOR_RED_BORDER, 1, true),
                                new EmptyBorder(6, 12, 6, 12)
                        ));
                        verifyTitleLabel.setText("✖ Roll Number Not Eligible");
                        verifyTitleLabel.setForeground(COLOR_RED);
                        String msg = (res.errorMessage != null && !res.errorMessage.isEmpty())
                                ? res.errorMessage
                                : ("No record matches '" + roll + "'. Allowed: Regular 25ITR001–25ITR182, or Lateral Entry 25ITL (e.g. 25ITL123).");
                        verifyDetailsLabel.setText(msg);
                        studentVerificationCard.setVisible(true);
                    }
                } catch (Exception ex) {
                    studentVerificationCard.setVisible(false);
                }
            }
        };
        worker.execute();
    }

    private void checkFirstRunAdminStatus() {
        SwingWorker<Boolean, Void> worker = new SwingWorker<>() {
            @Override
            protected Boolean doInBackground() throws Exception {
                return AuthService.hasAdministrator();
            }

            @Override
            protected void done() {
                try {
                    boolean hasAdmin = get();
                    firstRunAdminBanner.setVisible(!hasAdmin);
                } catch (Exception ignored) {
                }
            }
        };
        worker.execute();
    }

    private void executeLogin() {
        final String user = loginUserField.getText().trim();
        final String pass = new String(loginPassField.getPassword());

        if (user.isEmpty() || pass.isEmpty()) {
            showLoginStatus("Please enter both username and password.", COLOR_RED);
            return;
        }

        loginButton.setEnabled(false);
        showLoginStatus("Authenticating credentials...", COLOR_MUTED_BLUE);

        SwingWorker<UserSession, Void> worker = new SwingWorker<>() {
            @Override
            protected UserSession doInBackground() throws Exception {
                return AuthService.authenticate(user, pass);
            }

            @Override
            protected void done() {
                loginButton.setEnabled(true);
                try {
                    authenticatedSession = get();
                    showLoginStatus("Sign in successful. Opening portal...", COLOR_GREEN);
                    Timer t = new Timer(250, e -> dispose());
                    t.setRepeats(false);
                    t.start();
                } catch (Exception ex) {
                    Throwable cause = ex.getCause() != null ? ex.getCause() : ex;
                    if (cause instanceof AuthService.AuthException) {
                        showLoginStatus(cause.getMessage(), COLOR_RED);
                    } else if (cause instanceof SQLException) {
                        showLoginStatus("Database connection failed. Please verify MySQL service.", COLOR_RED);
                    } else {
                        showLoginStatus("Authentication failed: " + cause.getMessage(), COLOR_RED);
                    }
                }
            }
        };
        worker.execute();
    }

    private void executeStudentSignup() {
        final String user = signupUserField.getText().trim();
        final String roll = signupRollField.getText().trim();
        final String pass = new String(signupPassField.getPassword());
        final String confirm = new String(signupConfirmField.getPassword());

        if (user.isEmpty() || roll.isEmpty() || pass.isEmpty() || confirm.isEmpty()) {
            showSignupStatus("All fields are required for student registration.", COLOR_RED);
            return;
        }

        signupButton.setEnabled(false);
        showSignupStatus("Verifying student roll and registering account...", COLOR_MUTED_BLUE);

        SwingWorker<Void, Void> worker = new SwingWorker<>() {
            @Override
            protected Void doInBackground() throws Exception {
                AuthService.signupStudent(user, pass, confirm, roll);
                return null;
            }

            @Override
            protected void done() {
                signupButton.setEnabled(true);
                try {
                    get();
                    showSignupStatus("Account registered successfully! Redirecting to sign in...", COLOR_GREEN);
                    JOptionPane.showMessageDialog(AuthDialog.this,
                            String.format("Student account '%s' created successfully!\nYou may now sign in.", user),
                            "Registration Successful",
                            JOptionPane.INFORMATION_MESSAGE);
                    cardLayout.show(cardsPanel, "LOGIN");
                    loginUserField.setText(user);
                    loginPassField.setText("");
                    loginPassField.requestFocusInWindow();
                } catch (Exception ex) {
                    Throwable cause = ex.getCause() != null ? ex.getCause() : ex;
                    if (cause instanceof AuthService.ValidationException) {
                        showSignupStatus(cause.getMessage(), COLOR_RED);
                    } else if (cause instanceof SQLException) {
                        showSignupStatus("Database error during registration: " + cause.getMessage(), COLOR_RED);
                    } else {
                        showSignupStatus("Registration error: " + cause.getMessage(), COLOR_RED);
                    }
                }
            }
        };
        worker.execute();
    }

    private void executeAdminSetup() {
        final String user = adminUserField.getText().trim();
        final String pass = new String(adminPassField.getPassword());
        final String confirm = new String(adminConfirmField.getPassword());

        if (user.isEmpty() || pass.isEmpty() || confirm.isEmpty()) {
            showAdminStatus("All fields are required.", COLOR_RED);
            return;
        }

        adminSetupButton.setEnabled(false);
        showAdminStatus("Setting up primary administrator account...", COLOR_MUTED_BLUE);

        SwingWorker<Void, Void> worker = new SwingWorker<>() {
            @Override
            protected Void doInBackground() throws Exception {
                AuthService.setupInitialAdministrator(user, pass, confirm);
                return null;
            }

            @Override
            protected void done() {
                adminSetupButton.setEnabled(true);
                try {
                    get();
                    showAdminStatus("Administrator account created successfully!", COLOR_GREEN);
                    JOptionPane.showMessageDialog(AuthDialog.this,
                            String.format("Primary Administrator '%s' initialized.\nPlease sign in to manage the portal.", user),
                            "Setup Complete",
                            JOptionPane.INFORMATION_MESSAGE);
                    firstRunAdminBanner.setVisible(false);
                    cardLayout.show(cardsPanel, "LOGIN");
                    loginUserField.setText(user);
                    loginPassField.setText("");
                    loginPassField.requestFocusInWindow();
                } catch (Exception ex) {
                    Throwable cause = ex.getCause() != null ? ex.getCause() : ex;
                    if (cause instanceof AuthService.ValidationException || cause instanceof SecurityException) {
                        showAdminStatus(cause.getMessage(), COLOR_RED);
                    } else {
                        showAdminStatus("Failed to create administrator: " + cause.getMessage(), COLOR_RED);
                    }
                }
            }
        };
        worker.execute();
    }

    private void showLoginStatus(String msg, Color color) {
        loginStatusLabel.setText(msg);
        loginStatusLabel.setForeground(color);
    }

    private void showSignupStatus(String msg, Color color) {
        signupStatusLabel.setText(msg);
        signupStatusLabel.setForeground(color);
    }

    private void showAdminStatus(String msg, Color color) {
        adminStatusLabel.setText(msg);
        adminStatusLabel.setForeground(color);
    }

    private void clearSignupFields() {
        signupUserField.setText("");
        signupRollField.setText("");
        signupPassField.setText("");
        signupConfirmField.setText("");
        studentVerificationCard.setVisible(false);
        signupStatusLabel.setText(" ");
    }

    public UserSession getAuthenticatedSession() {
        return authenticatedSession;
    }

    // Helper UI Builders
    private JPanel createCardContainer() {
        JPanel card = new JPanel();
        card.setBackground(COLOR_CARD_BG);
        card.setBorder(new CompoundBorder(
                new LineBorder(COLOR_BORDER, 1, true),
                new EmptyBorder(20, 24, 20, 24)
        ));
        return card;
    }

    private JLabel createFieldLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(FONT_BOLD);
        label.setForeground(COLOR_TEXT_DARK);
        label.setAlignmentX(Component.LEFT_ALIGNMENT);
        return label;
    }

    private JTextField createTextField(String toolTip) {
        JTextField tf = new JTextField();
        tf.setFont(FONT_REGULAR);
        tf.setForeground(COLOR_TEXT_DARK);
        tf.setCaretColor(COLOR_NAVY);
        tf.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        tf.setPreferredSize(new Dimension(tf.getPreferredSize().width, 40));
        tf.setToolTipText(toolTip);

        LineBorder normalBorder = new LineBorder(COLOR_BORDER, 1, true);
        LineBorder focusedBorder = new LineBorder(COLOR_BORDER_FOCUS, 2, true);
        tf.setBorder(new CompoundBorder(normalBorder, new EmptyBorder(6, 12, 6, 12)));

        tf.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                tf.setBorder(new CompoundBorder(focusedBorder, new EmptyBorder(5, 11, 5, 11)));
            }

            @Override
            public void focusLost(FocusEvent e) {
                tf.setBorder(new CompoundBorder(normalBorder, new EmptyBorder(6, 12, 6, 12)));
            }
        });

        tf.setAlignmentX(Component.LEFT_ALIGNMENT);
        return tf;
    }

    private JPasswordField createPasswordField() {
        JPasswordField pf = new JPasswordField();
        pf.setFont(FONT_REGULAR);
        pf.setForeground(COLOR_TEXT_DARK);
        pf.setCaretColor(COLOR_NAVY);
        pf.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        pf.setPreferredSize(new Dimension(pf.getPreferredSize().width, 40));

        LineBorder normalBorder = new LineBorder(COLOR_BORDER, 1, true);
        LineBorder focusedBorder = new LineBorder(COLOR_BORDER_FOCUS, 2, true);
        pf.setBorder(new CompoundBorder(normalBorder, new EmptyBorder(6, 12, 6, 12)));

        pf.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                pf.setBorder(new CompoundBorder(focusedBorder, new EmptyBorder(5, 11, 5, 11)));
            }

            @Override
            public void focusLost(FocusEvent e) {
                pf.setBorder(new CompoundBorder(normalBorder, new EmptyBorder(6, 12, 6, 12)));
            }
        });

        pf.setAlignmentX(Component.LEFT_ALIGNMENT);
        return pf;
    }

    private JLabel createRolePill(String text, Color bg, Color fg) {
        JLabel pill = new JLabel("  " + text + "  ");
        pill.setFont(FONT_BADGE);
        pill.setOpaque(true);
        pill.setBackground(bg);
        pill.setForeground(fg);
        pill.setBorder(new CompoundBorder(
                new LineBorder(new Color(fg.getRed(), fg.getGreen(), fg.getBlue(), 80), 1, true),
                new EmptyBorder(3, 6, 3, 6)
        ));
        return pill;
    }

    private void styleLinkButton(JButton btn) {
        btn.setFont(FONT_BOLD);
        btn.setForeground(COLOR_MUTED_BLUE);
        btn.setOpaque(false);
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent e) {
                btn.setForeground(COLOR_NAVY);
            }

            @Override
            public void mouseExited(java.awt.event.MouseEvent e) {
                btn.setForeground(COLOR_MUTED_BLUE);
            }
        });
    }
}
