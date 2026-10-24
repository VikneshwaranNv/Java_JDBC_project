import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.border.MatteBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;
import java.awt.*;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.image.BufferedImage;
import java.io.File;
import javax.imageio.ImageIO;

/**
 * KEC ACADEMIX Design System & Brand Identity for Kongu Engineering College.
 * Provides unified color palette, typography, custom buttons with accessible focus rings
 * and hover states, high-contrast table header & cell renderers with subtle row-hover effects,
 * interactive card containers, and official KEC logo loading.
 */
public final class KECTheme {

    private KECTheme() {
        // Utility class
    }

    // =========================================================================
    // 1. OFFICIAL KEC COLOR PALETTE
    // =========================================================================

    /** KEC Green: highlights, selected navigation states, small accents (#8BC34A) */
    public static final Color KEC_GREEN = new Color(139, 195, 74);
    public static final Color KEC_GREEN_HOVER = new Color(124, 179, 66);
    public static final Color KEC_GREEN_PRESSED = new Color(104, 159, 56);
    public static final Color KEC_GREEN_LIGHT = new Color(241, 248, 233);

    /** KEC Cyan Blue: primary actions, links, active indicators (#00A5D8) */
    public static final Color KEC_CYAN = new Color(0, 165, 216);
    public static final Color KEC_CYAN_HOVER = new Color(0, 144, 188);
    public static final Color KEC_CYAN_PRESSED = new Color(0, 120, 156);
    public static final Color KEC_CYAN_LIGHT = new Color(230, 247, 253);

    /** Deep Navy: main headings, sidebar background, strong text (#17365D) */
    public static final Color DEEP_NAVY = new Color(23, 54, 93);
    public static final Color DEEP_NAVY_HOVER = new Color(32, 72, 122);
    public static final Color DEEP_NAVY_PRESSED = new Color(16, 38, 66);
    public static final Color DEEP_NAVY_LIGHT = new Color(235, 242, 250);

    /** White: main panels, forms, cards, dialogs (#FFFFFF) */
    public static final Color WHITE = Color.WHITE;

    /** Light Grey: application background, table surroundings (#F4F7F9) */
    public static final Color BG_LIGHT = new Color(244, 247, 249);

    /** Border Grey: subtle borders and separators (#DCE4E9) */
    public static final Color BORDER_GREY = new Color(220, 228, 233);
    public static final Color BORDER_DARK = new Color(195, 207, 222);

    /** Text colors */
    public static final Color TEXT_DARK = new Color(23, 54, 93);
    public static final Color TEXT_MUTED = new Color(100, 116, 139);
    public static final Color TEXT_LIGHT = new Color(241, 245, 249);

    /** Status Colors */
    public static final Color SUCCESS_GREEN = new Color(39, 132, 91);   // #27845B
    public static final Color SUCCESS_BG = new Color(235, 247, 240);
    public static final Color WARNING_AMBER = new Color(217, 154, 37);  // #D99A25
    public static final Color WARNING_BG = new Color(254, 247, 235);
    public static final Color ERROR_RED = new Color(196, 69, 69);       // #C44545
    public static final Color ERROR_BG = new Color(253, 238, 238);

    // =========================================================================
    // 2. TYPOGRAPHY
    // =========================================================================

    public static final Font FONT_TITLE = new Font("Segoe UI", Font.BOLD, 22);
    public static final Font FONT_PAGE_TITLE = new Font("Segoe UI", Font.BOLD, 18);
    public static final Font FONT_SECTION = new Font("Segoe UI", Font.BOLD, 15);
    public static final Font FONT_BOLD = new Font("Segoe UI", Font.BOLD, 14);
    public static final Font FONT_REGULAR = new Font("Segoe UI", Font.PLAIN, 14);
    public static final Font FONT_SMALL = new Font("Segoe UI", Font.PLAIN, 12);
    public static final Font FONT_SMALL_BOLD = new Font("Segoe UI", Font.BOLD, 12);
    public static final Font FONT_MONO = new Font("Consolas", Font.PLAIN, 13);

    // =========================================================================
    // 3. LOGO ASSET LOADER
    // =========================================================================

    private static BufferedImage cachedLogo = null;

    /**
     * Loads the official Kongu Engineering College logo from assets/kec_logo.png.
     * Preserves exact proportions and uses high-quality smooth bilinear scaling.
     * Returns an ImageIcon scaled to fit within (maxWidth x maxHeight).
     */
    public static ImageIcon getKECLogo(int maxWidth, int maxHeight) {
        if (cachedLogo == null) {
            String[] searchPaths = {
                    "assets/kec_logo.png",
                    "../assets/kec_logo.png",
                    "D:/KEC_Java_Project/assets/kec_logo.png",
                    "assets/logo.png"
            };
            for (String p : searchPaths) {
                File f = new File(p);
                if (f.exists() && f.canRead()) {
                    try {
                        cachedLogo = ImageIO.read(f);
                        if (cachedLogo != null) break;
                    } catch (Exception ignored) {
                    }
                }
            }
        }

        if (cachedLogo == null) {
            return null;
        }

        int originalWidth = cachedLogo.getWidth();
        int originalHeight = cachedLogo.getHeight();
        if (originalWidth <= 0 || originalHeight <= 0) return null;

        double scale = Math.min((double) maxWidth / originalWidth, (double) maxHeight / originalHeight);
        int targetWidth = Math.max(1, (int) Math.round(originalWidth * scale));
        int targetHeight = Math.max(1, (int) Math.round(originalHeight * scale));

        BufferedImage scaled = new BufferedImage(targetWidth, targetHeight, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = scaled.createGraphics();
        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.drawImage(cachedLogo, 0, 0, targetWidth, targetHeight, null);
        g2.dispose();

        return new ImageIcon(scaled);
    }

    // =========================================================================
    // 4. CUSTOM BUTTON COMPONENTS
    // =========================================================================

    /**
     * Primary action button with smooth hover, pressed feedback, accessible focus outline,
     * and precise text/icon layout with zero jitter during state changes.
     */
    public static class KECButton extends JButton {
        private final Color normalBg;
        private final Color hoverBg;
        private final Color pressedBg;
        private final Color textColor;
        private final Color borderColor;
        private final int cornerRadius;

        public KECButton(String text, Color normalBg, Color hoverBg, Color pressedBg, Color textColor, Color borderColor) {
            this(text, normalBg, hoverBg, pressedBg, textColor, borderColor, 8);
        }

        public KECButton(String text, Color normalBg, Color hoverBg, Color pressedBg, Color textColor, Color borderColor, int cornerRadius) {
            super(text);
            this.normalBg = normalBg;
            this.hoverBg = hoverBg;
            this.pressedBg = pressedBg;
            this.textColor = textColor;
            this.borderColor = borderColor;
            this.cornerRadius = cornerRadius;

            setFont(FONT_BOLD);
            setForeground(textColor);
            setContentAreaFilled(false);
            setFocusPainted(false);
            setBorderPainted(false);
            setOpaque(false);
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            setBorder(new EmptyBorder(8, 16, 8, 16));
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

            int w = getWidth();
            int h = getHeight();

            if (!isEnabled()) {
                g2.setColor(new Color(228, 234, 240));
            } else if (getModel().isPressed()) {
                g2.setColor(pressedBg);
            } else if (getModel().isRollover()) {
                g2.setColor(hoverBg);
            } else {
                g2.setColor(normalBg);
            }

            g2.fillRoundRect(0, 0, w - 1, h - 1, cornerRadius, cornerRadius);

            if (borderColor != null) {
                g2.setColor(isEnabled() ? (getModel().isRollover() ? borderColor.darker() : borderColor) : new Color(215, 222, 230));
                g2.setStroke(new BasicStroke(1.2f));
                g2.drawRoundRect(0, 0, w - 1, h - 1, cornerRadius, cornerRadius);
            }

            // Accessible keyboard focus ring
            if (isFocusOwner()) {
                g2.setColor(new Color(0, 165, 216, 175));
                g2.setStroke(new BasicStroke(1.8f));
                g2.drawRoundRect(2, 2, w - 5, h - 5, cornerRadius - 1, cornerRadius - 1);
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
                g2.setColor(isEnabled() ? textColor : new Color(150, 165, 180));
                g2.drawString(layoutText, textRect.x, textRect.y + fm.getAscent());
            }

            g2.dispose();
        }
    }

    /** Primary Cyan Action Button (#00A5D8) */
    public static KECButton createPrimaryButton(String text) {
        return new KECButton(text, KEC_CYAN, KEC_CYAN_HOVER, KEC_CYAN_PRESSED, WHITE, null);
    }

    /** Accent Green Action Button (#8BC34A) */
    public static KECButton createAccentButton(String text) {
        return new KECButton(text, KEC_GREEN, KEC_GREEN_HOVER, KEC_GREEN_PRESSED, WHITE, null);
    }

    /** Success Green Button (#27845B) */
    public static KECButton createSuccessButton(String text) {
        return new KECButton(text, SUCCESS_GREEN, new Color(31, 110, 75), new Color(22, 85, 57), WHITE, null);
    }

    /** Danger Red Button (#C44545) */
    public static KECButton createDangerButton(String text) {
        return new KECButton(text, ERROR_RED, new Color(175, 55, 55), new Color(150, 40, 40), WHITE, null);
    }

    /** Deep Navy Button (#17365D) */
    public static KECButton createNavyButton(String text) {
        return new KECButton(text, DEEP_NAVY, DEEP_NAVY_HOVER, DEEP_NAVY_PRESSED, WHITE, null);
    }

    /** Secondary White Button with Gray Border & Navy Text */
    public static KECButton createSecondaryButton(String text) {
        return new KECButton(text, WHITE, BG_LIGHT, new Color(230, 237, 244), DEEP_NAVY, BORDER_GREY);
    }

    // =========================================================================
    // 5. TABLE HEADER RENDERER & CELL STYLING WITH ROW HOVER
    // =========================================================================

    /**
     * Custom Table Header Renderer that guarantees 100% visible, bold white text
     * on Deep Navy (#17365D) background, overriding native Windows Look and Feel.
     */
    public static class KECTableHeaderRenderer implements TableCellRenderer {
        private final DefaultTableCellRenderer defaultRenderer = new DefaultTableCellRenderer();

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                                                       boolean isSelected, boolean hasFocus, int row, int col) {
            JLabel label = (JLabel) defaultRenderer.getTableCellRendererComponent(
                    table, value, isSelected, hasFocus, row, col);

            label.setFont(new Font("Segoe UI", Font.BOLD, 13));
            label.setBackground(DEEP_NAVY);
            label.setForeground(WHITE);
            label.setOpaque(true);
            label.setHorizontalAlignment(SwingConstants.LEFT);
            label.setBorder(new CompoundBorder(
                    new MatteBorder(0, 0, 2, 1, new Color(42, 75, 117)),
                    new EmptyBorder(8, 12, 8, 12)
            ));

            return label;
        }
    }

    /**
     * High-contrast Table Cell Renderer with alternating rows, row-hover tinting,
     * and crisp text alignment.
     */
    public static class KECTableCellRenderer extends DefaultTableCellRenderer {
        private final boolean center;

        public KECTableCellRenderer(boolean center) {
            this.center = center;
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                                                       boolean isSelected, boolean hasFocus, int row, int col) {
            super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, col);

            setFont(FONT_REGULAR);
            setBorder(new EmptyBorder(4, 12, 4, 12));
            if (center) {
                setHorizontalAlignment(SwingConstants.CENTER);
            } else {
                setHorizontalAlignment(SwingConstants.LEFT);
            }

            Integer hoveredRow = (Integer) table.getClientProperty("HOVERED_ROW");
            int hoverIndex = (hoveredRow != null) ? hoveredRow : -1;

            if (isSelected) {
                setBackground(KEC_CYAN_LIGHT);
                setForeground(DEEP_NAVY);
            } else if (row == hoverIndex) {
                setBackground(new Color(245, 250, 254)); // Soft row-hover highlight
                setForeground(TEXT_DARK);
            } else {
                if (row % 2 == 1) {
                    setBackground(new Color(250, 252, 255)); // Alternating row
                } else {
                    setBackground(WHITE);
                }
                setForeground(TEXT_DARK);
            }

            return this;
        }
    }

    /**
     * Applies full KEC institutional styling to any JTable.
     * Sets row height to 34px, comfortable fonts, light cyan selection,
     * smooth row-hover tracking, and the custom KECTableHeaderRenderer.
     */
    public static void styleTable(JTable table) {
        table.setFont(FONT_REGULAR);
        table.setForeground(TEXT_DARK);
        table.setBackground(WHITE);
        table.setSelectionBackground(KEC_CYAN_LIGHT);
        table.setSelectionForeground(DEEP_NAVY);
        table.setRowHeight(34);
        table.setGridColor(new Color(235, 240, 245));
        table.setShowVerticalLines(false);
        table.setShowHorizontalLines(true);
        table.setFillsViewportHeight(true);
        table.setAutoCreateRowSorter(true);

        JTableHeader header = table.getTableHeader();
        header.setDefaultRenderer(new KECTableHeaderRenderer());
        header.setPreferredSize(new Dimension(0, 38));
        header.setReorderingAllowed(false);

        KECTableCellRenderer leftRenderer = new KECTableCellRenderer(false);
        KECTableCellRenderer centerRenderer = new KECTableCellRenderer(true);

        table.setDefaultRenderer(Object.class, leftRenderer);
        table.setDefaultRenderer(String.class, leftRenderer);
        table.setDefaultRenderer(Number.class, centerRenderer);
        table.setDefaultRenderer(Integer.class, centerRenderer);
        table.setDefaultRenderer(Double.class, centerRenderer);

        // Subtle row-hover tracking
        table.addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                int row = table.rowAtPoint(e.getPoint());
                Integer prev = (Integer) table.getClientProperty("HOVERED_ROW");
                if (prev == null || prev != row) {
                    table.putClientProperty("HOVERED_ROW", row);
                    table.repaint();
                }
            }
        });

        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseExited(MouseEvent e) {
                table.putClientProperty("HOVERED_ROW", -1);
                table.repaint();
            }
        });
    }

    // =========================================================================
    // 6. COMPONENT STYLING HELPERS & INTERACTIVE CARDS
    // =========================================================================

    /**
     * Styles a text field with clean institutional border, focus highlight, and padding.
     * Prevents text jitter on focus by adjusting padding proportionally.
     */
    public static void styleTextField(JTextField field, int height) {
        field.setFont(FONT_REGULAR);
        field.setForeground(TEXT_DARK);
        field.setBackground(WHITE);
        field.setCaretColor(DEEP_NAVY);
        field.setPreferredSize(new Dimension(field.getPreferredSize().width, height));
        field.setBorder(new CompoundBorder(
                new LineBorder(BORDER_GREY, 1, true),
                new EmptyBorder(6, 12, 6, 12)
        ));

        field.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                field.setBorder(new CompoundBorder(
                        new LineBorder(KEC_CYAN, 2, true),
                        new EmptyBorder(5, 11, 5, 11)
                ));
            }

            @Override
            public void focusLost(FocusEvent e) {
                field.setBorder(new CompoundBorder(
                        new LineBorder(BORDER_GREY, 1, true),
                        new EmptyBorder(6, 12, 6, 12)
                ));
            }
        });
    }

    /**
     * Styles a JComboBox with consistent padding and fonts.
     */
    public static void styleComboBox(JComboBox<?> box, int height) {
        box.setFont(FONT_REGULAR);
        box.setForeground(TEXT_DARK);
        box.setBackground(WHITE);
        box.setPreferredSize(new Dimension(box.getPreferredSize().width, height));
    }

    /**
     * Creates a static white card panel with a subtle border and generous padding.
     */
    public static JPanel createCardPanel(Border padding) {
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(WHITE);
        card.setBorder(new CompoundBorder(
                new LineBorder(BORDER_GREY, 1, true),
                padding != null ? padding : new EmptyBorder(16, 20, 16, 20)
        ));
        return card;
    }

    /**
     * Creates an interactive card panel that displays subtle hover outline feedback
     * and triggers an action on click, with zero layout shift.
     */
    public static JPanel createInteractiveCard(Border padding, Runnable onClick) {
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(WHITE);
        Border normalBorder = new CompoundBorder(
                new LineBorder(BORDER_GREY, 1, true),
                padding != null ? padding : new EmptyBorder(16, 18, 16, 18)
        );
        Border hoverBorder = new CompoundBorder(
                new LineBorder(KEC_CYAN, 1, true),
                padding != null ? padding : new EmptyBorder(16, 18, 16, 18)
        );
        card.setBorder(normalBorder);
        card.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        card.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                card.setBorder(hoverBorder);
                card.setBackground(new Color(252, 254, 255));
            }

            @Override
            public void mouseExited(MouseEvent e) {
                card.setBorder(normalBorder);
                card.setBackground(WHITE);
            }

            @Override
            public void mouseClicked(MouseEvent e) {
                if (onClick != null) {
                    onClick.run();
                }
            }
        });

        return card;
    }

    /**
     * Creates a top header panel with KEC logo, application title, college name,
     * and a dual-colored brand accent bottom bar (KEC Green + KEC Cyan).
     */
    public static JPanel createHeaderBanner(String moduleTitle, String moduleSubtitle, JComponent rightComponent) {
        JPanel banner = new JPanel(new BorderLayout(14, 0));
        banner.setBackground(WHITE);

        // Content panel
        JPanel inner = new JPanel(new BorderLayout(16, 0));
        inner.setOpaque(false);
        inner.setBorder(new EmptyBorder(12, 24, 12, 24));

        // Left brand block: Logo + Titles
        JPanel leftBrand = new JPanel(new FlowLayout(FlowLayout.LEFT, 14, 0));
        leftBrand.setOpaque(false);

        ImageIcon logoIcon = getKECLogo(150, 48);
        if (logoIcon != null) {
            JLabel logoLabel = new JLabel(logoIcon);
            leftBrand.add(logoLabel);
        }

        JPanel textBlock = new JPanel();
        textBlock.setLayout(new BoxLayout(textBlock, BoxLayout.Y_AXIS));
        textBlock.setOpaque(false);

        JLabel appTitle = new JLabel("KEC ACADEMIX");
        appTitle.setFont(new Font("Segoe UI", Font.BOLD, 17));
        appTitle.setForeground(DEEP_NAVY);

        JLabel subTitle = new JLabel(moduleTitle != null ? moduleTitle : "Academic & Continuous Evaluation Management System");
        subTitle.setFont(FONT_SMALL);
        subTitle.setForeground(TEXT_MUTED);

        textBlock.add(appTitle);
        textBlock.add(Box.createVerticalStrut(2));
        textBlock.add(subTitle);
        leftBrand.add(textBlock);

        inner.add(leftBrand, BorderLayout.WEST);

        if (rightComponent != null) {
            inner.add(rightComponent, BorderLayout.EAST);
        }

        banner.add(inner, BorderLayout.CENTER);

        // Dual brand accent bottom stripe: Green (30%) + Cyan (70%)
        JPanel accentStripe = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                int w = getWidth();
                int h = getHeight();
                int greenW = (int) (w * 0.28);
                g.setColor(KEC_GREEN);
                g.fillRect(0, 0, greenW, h);
                g.setColor(KEC_CYAN);
                g.fillRect(greenW, 0, w - greenW, h);
            }
        };
        accentStripe.setPreferredSize(new Dimension(0, 4));
        banner.add(accentStripe, BorderLayout.SOUTH);

        return banner;
    }
}
