import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.util.List;

/**
 * User and Role Management interface for KEC ACADEMIX Administrators.
 * Allows administrators to review all user accounts, provision new Staff/Admin accounts,
 * and toggle active status.
 */
public class UserManagementDialog extends JDialog {

    // Palette
    public static final Color COLOR_NAVY = new Color(23, 43, 77);         // #172B4D
    public static final Color COLOR_NAVY_HOVER = new Color(34, 62, 108);  // #223E6C
    public static final Color COLOR_MUTED_BLUE = new Color(53, 92, 138);   // #355C8A
    public static final Color COLOR_WHITE = Color.WHITE;                  // #FFFFFF
    public static final Color COLOR_BG = new Color(246, 248, 251);         // #F6F8FB
    public static final Color COLOR_BORDER = new Color(227, 232, 239);     // #E3E8EF
    public static final Color COLOR_TEXT_DARK = new Color(23, 43, 77);
    public static final Color COLOR_TEXT_MUTED = new Color(100, 115, 135);

    public static final Color COLOR_GREEN = new Color(16, 124, 65);       // #107C41
    public static final Color COLOR_GREEN_BG = new Color(227, 245, 233);
    public static final Color COLOR_RED = new Color(197, 34, 31);         // #C5221F
    public static final Color COLOR_RED_BG = new Color(252, 232, 230);
    public static final Color COLOR_PURPLE = new Color(109, 40, 217);

    public static final Font FONT_TITLE = new Font("Segoe UI", Font.BOLD, 20);
    public static final Font FONT_SECTION = new Font("Segoe UI", Font.BOLD, 14);
    public static final Font FONT_BOLD = new Font("Segoe UI", Font.BOLD, 13);
    public static final Font FONT_REGULAR = new Font("Segoe UI", Font.PLAIN, 13);
    public static final Font FONT_SMALL = new Font("Segoe UI", Font.PLAIN, 12);

    private final UserSession currentSession;
    private JTable userTable;
    private DefaultTableModel tableModel;
    private JLabel statusLabel;

    public UserManagementDialog(Frame parent, UserSession session) {
        super(parent, "KEC ACADEMIX — User & Role Administration", true);
        if (session == null || !session.isAdministrator()) {
            throw new SecurityException("Unauthorized access: Administrator role required.");
        }
        this.currentSession = session;

        setSize(1000, 680);
        setMinimumSize(new Dimension(850, 550));
        setLocationRelativeTo(parent);

        buildUI();
        loadUsers();
    }

    private void buildUI() {
        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(COLOR_BG);

        // Header
        JPanel header = new JPanel(new BorderLayout(14, 0));
        header.setBackground(KECTheme.DEEP_NAVY);
        header.setBorder(new EmptyBorder(16, 24, 16, 24));

        ImageIcon logoIcon = KECTheme.getKECLogo(140, 44);
        if (logoIcon != null) {
            header.add(new JLabel(logoIcon), BorderLayout.WEST);
        }

        JPanel titlePanel = new JPanel();
        titlePanel.setLayout(new BoxLayout(titlePanel, BoxLayout.Y_AXIS));
        titlePanel.setOpaque(false);

        JLabel title = new JLabel("User Accounts & Role Administration");
        title.setFont(FONT_TITLE);
        title.setForeground(COLOR_WHITE);

        JLabel sub = new JLabel("Authorized Administrator: " + currentSession.getUsername() + " • Kongu Engineering College");
        sub.setFont(FONT_SMALL);
        sub.setForeground(new Color(190, 207, 235));

        titlePanel.add(title);
        titlePanel.add(Box.createVerticalStrut(2));
        titlePanel.add(sub);

        header.add(titlePanel, BorderLayout.CENTER);
        root.add(header, BorderLayout.NORTH);

        // Main Content: Split into User Table and Creation Panel
        JPanel content = new JPanel(new BorderLayout(16, 16));
        content.setBackground(COLOR_BG);
        content.setBorder(new EmptyBorder(16, 20, 16, 20));

        // Table Panel
        JPanel tablePanel = new JPanel(new BorderLayout(0, 8));
        tablePanel.setBackground(COLOR_WHITE);
        tablePanel.setBorder(new CompoundBorder(
                new LineBorder(COLOR_BORDER, 1),
                new EmptyBorder(14, 16, 14, 16)
        ));

        JLabel tableTitle = new JLabel("Registered System Accounts");
        tableTitle.setFont(FONT_SECTION);
        tableTitle.setForeground(COLOR_TEXT_DARK);
        tablePanel.add(tableTitle, BorderLayout.NORTH);

        String[] cols = {"User ID", "Username", "Role", "Student Roll No", "Linked Student Name", "Status", "Created At"};
        tableModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        userTable = new JTable(tableModel);
        userTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        KECTheme.styleTable(userTable);

        DefaultTableCellRenderer center = new DefaultTableCellRenderer();
        center.setHorizontalAlignment(SwingConstants.CENTER);
        userTable.getColumnModel().getColumn(0).setCellRenderer(center);
        userTable.getColumnModel().getColumn(2).setCellRenderer(center);
        userTable.getColumnModel().getColumn(3).setCellRenderer(center);
        userTable.getColumnModel().getColumn(5).setCellRenderer(center);

        userTable.getColumnModel().getColumn(0).setPreferredWidth(60);
        userTable.getColumnModel().getColumn(1).setPreferredWidth(130);
        userTable.getColumnModel().getColumn(2).setPreferredWidth(120);
        userTable.getColumnModel().getColumn(3).setPreferredWidth(120);
        userTable.getColumnModel().getColumn(4).setPreferredWidth(160);
        userTable.getColumnModel().getColumn(5).setPreferredWidth(90);
        userTable.getColumnModel().getColumn(6).setPreferredWidth(140);

        tablePanel.add(new JScrollPane(userTable), BorderLayout.CENTER);

        // Table Actions Bar
        JPanel tableActions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        tableActions.setOpaque(false);

        JButton refreshBtn = KECTheme.createSecondaryButton("Refresh");
        refreshBtn.addActionListener(e -> loadUsers());

        JButton toggleActiveBtn = KECTheme.createSecondaryButton("Toggle Account Active / Disabled");
        toggleActiveBtn.addActionListener(e -> toggleSelectedUserStatus());

        JButton addStaffBtn = KECTheme.createPrimaryButton("+ Create Staff / Admin Account");
        addStaffBtn.addActionListener(e -> showCreateStaffDialog());

        tableActions.add(refreshBtn);
        tableActions.add(toggleActiveBtn);
        tableActions.add(addStaffBtn);

        tablePanel.add(tableActions, BorderLayout.SOUTH);
        content.add(tablePanel, BorderLayout.CENTER);

        root.add(content, BorderLayout.CENTER);

        // Status bar
        JPanel statusPanel = new JPanel(new BorderLayout());
        statusPanel.setBackground(COLOR_WHITE);
        statusPanel.setBorder(new CompoundBorder(
                new LineBorder(COLOR_BORDER, 1),
                new EmptyBorder(8, 20, 8, 20)
        ));

        statusLabel = new JLabel("Ready");
        statusLabel.setFont(FONT_SMALL);
        statusLabel.setForeground(COLOR_TEXT_MUTED);

        statusPanel.add(statusLabel, BorderLayout.WEST);
        root.add(statusPanel, BorderLayout.SOUTH);

        setContentPane(root);
    }

    private void loadUsers() {
        statusLabel.setText("Loading user accounts from database...");
        SwingWorker<List<AuthService.UserAccountSummary>, Void> worker = new SwingWorker<>() {
            @Override
            protected List<AuthService.UserAccountSummary> doInBackground() throws Exception {
                return AuthService.listUsers(currentSession);
            }

            @Override
            protected void done() {
                try {
                    List<AuthService.UserAccountSummary> users = get();
                    tableModel.setRowCount(0);
                    for (AuthService.UserAccountSummary u : users) {
                        tableModel.addRow(new Object[]{
                                u.userId,
                                u.username,
                                u.role,
                                u.studentRoll != null ? u.studentRoll : "—",
                                u.studentName != null ? u.studentName : "—",
                                u.active ? "ACTIVE" : "DISABLED",
                                u.createdAt != null ? u.createdAt.toString() : "—"
                        });
                    }
                    statusLabel.setText(String.format("Loaded %d user accounts.", users.size()));
                } catch (Exception ex) {
                    statusLabel.setText("Failed to load users: " + ex.getMessage());
                    JOptionPane.showMessageDialog(UserManagementDialog.this,
                            "Error loading user accounts:\n" + ex.getMessage(),
                            "Database Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        };
        worker.execute();
    }

    private void toggleSelectedUserStatus() {
        int selected = userTable.getSelectedRow();
        if (selected < 0) {
            JOptionPane.showMessageDialog(this,
                    "Please select a user account in the table first.",
                    "Selection Required", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int targetUserId = (int) tableModel.getValueAt(selected, 0);
        String targetUsername = (String) tableModel.getValueAt(selected, 1);
        String currentStatus = (String) tableModel.getValueAt(selected, 5);
        boolean currentlyActive = "ACTIVE".equals(currentStatus);
        boolean newActive = !currentlyActive;

        if (targetUserId == currentSession.getUserId()) {
            JOptionPane.showMessageDialog(this,
                    "You cannot disable your own active administrator account.",
                    "Operation Prohibited", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(this,
                String.format("Are you sure you want to %s account for '%s' (User ID: %d)?",
                        newActive ? "ENABLE" : "DISABLE", targetUsername, targetUserId),
                "Confirm Account Status Change",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE);

        if (confirm != JOptionPane.YES_OPTION) return;

        SwingWorker<Void, Void> worker = new SwingWorker<>() {
            @Override
            protected Void doInBackground() throws Exception {
                AuthService.setUserActive(currentSession, targetUserId, newActive);
                return null;
            }

            @Override
            protected void done() {
                try {
                    get();
                    loadUsers();
                    JOptionPane.showMessageDialog(UserManagementDialog.this,
                            String.format("Account '%s' is now %s.", targetUsername, newActive ? "ACTIVE" : "DISABLED"),
                            "Status Updated", JOptionPane.INFORMATION_MESSAGE);
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(UserManagementDialog.this,
                            "Failed to update user status:\n" + ex.getMessage(),
                            "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        };
        worker.execute();
    }

    private void showCreateStaffDialog() {
        JDialog dlg = new JDialog(this, "Provision Staff or Administrator Account", true);
        dlg.setSize(440, 420);
        dlg.setLocationRelativeTo(this);
        dlg.setResizable(false);

        JPanel p = new JPanel();
        p.setBackground(COLOR_WHITE);
        p.setBorder(new EmptyBorder(20, 24, 20, 24));
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));

        JLabel title = new JLabel("Create Privileged Account");
        title.setFont(FONT_SECTION);
        title.setForeground(COLOR_TEXT_DARK);
        p.add(title);
        p.add(Box.createVerticalStrut(14));

        JLabel userLbl = new JLabel("Username (3-30 chars):");
        userLbl.setFont(FONT_BOLD);
        p.add(userLbl);
        JTextField userField = new JTextField();
        KECTheme.styleTextField(userField, 36);
        userField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        p.add(userField);
        p.add(Box.createVerticalStrut(12));

        JLabel roleLbl = new JLabel("Assigned Role:");
        roleLbl.setFont(FONT_BOLD);
        p.add(roleLbl);
        JComboBox<String> roleBox = new JComboBox<>(new String[]{"STAFF", "ADMINISTRATOR"});
        KECTheme.styleComboBox(roleBox, 36);
        roleBox.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        p.add(roleBox);
        p.add(Box.createVerticalStrut(12));

        JLabel passLbl = new JLabel("Password (Min 8 chars, letters and digits):");
        passLbl.setFont(FONT_BOLD);
        p.add(passLbl);
        JPasswordField passField = new JPasswordField();
        KECTheme.styleTextField(passField, 36);
        passField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        p.add(passField);
        p.add(Box.createVerticalStrut(12));

        JLabel confirmLbl = new JLabel("Confirm Password:");
        confirmLbl.setFont(FONT_BOLD);
        p.add(confirmLbl);
        JPasswordField confirmField = new JPasswordField();
        KECTheme.styleTextField(confirmField, 36);
        confirmField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        p.add(confirmField);
        p.add(Box.createVerticalStrut(18));

        JButton saveBtn = new AuthDialog.PrimaryButton("Create Account");
        saveBtn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        saveBtn.addActionListener(e -> {
            String u = userField.getText().trim();
            String r = (String) roleBox.getSelectedItem();
            String pwd = new String(passField.getPassword());
            String cpwd = new String(confirmField.getPassword());

            if (u.isEmpty() || pwd.isEmpty() || cpwd.isEmpty()) {
                JOptionPane.showMessageDialog(dlg, "All fields are required.", "Validation Error", JOptionPane.WARNING_MESSAGE);
                return;
            }
            if (!pwd.equals(cpwd)) {
                JOptionPane.showMessageDialog(dlg, "Passwords do not match.", "Validation Error", JOptionPane.WARNING_MESSAGE);
                return;
            }

            saveBtn.setEnabled(false);
            SwingWorker<Void, Void> worker = new SwingWorker<>() {
                @Override
                protected Void doInBackground() throws Exception {
                    AuthService.createStaffOrAdmin(currentSession, u, pwd, r);
                    return null;
                }

                @Override
                protected void done() {
                    saveBtn.setEnabled(true);
                    try {
                        get();
                        dlg.dispose();
                        loadUsers();
                        JOptionPane.showMessageDialog(UserManagementDialog.this,
                                String.format("Account '%s' with role %s created successfully!", u, r),
                                "Account Created", JOptionPane.INFORMATION_MESSAGE);
                    } catch (Exception ex) {
                        Throwable cause = ex.getCause() != null ? ex.getCause() : ex;
                        JOptionPane.showMessageDialog(dlg,
                                "Error creating account: " + cause.getMessage(),
                                "Error", JOptionPane.ERROR_MESSAGE);
                    }
                }
            };
            worker.execute();
        });
        p.add(saveBtn);

        dlg.setContentPane(p);
        dlg.setVisible(true);
    }

    private void stylePrimaryButton(JButton btn) {
        btn.setFont(FONT_BOLD);
        btn.setBackground(COLOR_NAVY);
        btn.setForeground(COLOR_WHITE);
        btn.setFocusPainted(false);
        btn.setBorder(new EmptyBorder(8, 14, 8, 14));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
    }

    private void styleSecondaryButton(JButton btn) {
        btn.setFont(FONT_BOLD);
        btn.setBackground(COLOR_WHITE);
        btn.setForeground(COLOR_NAVY);
        btn.setFocusPainted(false);
        btn.setBorder(new CompoundBorder(
                new LineBorder(COLOR_BORDER, 1),
                new EmptyBorder(6, 12, 6, 12)
        ));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
    }
}

