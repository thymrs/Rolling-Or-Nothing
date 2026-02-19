import javax.swing.*;
import java.awt.*;
import java.util.HashMap;
import java.util.Map;

public class HowToPlayPanel extends JPanel {
    private JEditorPane howToContentArea;
    private Map<String, JButton> sideMenuButtons = new HashMap<>();
    private PageNavigator navigator;

    public HowToPlayPanel(PageNavigator navigator) {
        this.navigator = navigator;
        setLayout(new BorderLayout());
        setBackground(GameTheme.BG_COLOR);
        createTopBar();
        createContentArea();
    }

    private void createTopBar() {
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setBackground(GameTheme.BG_COLOR);
        topPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 10, 20));
        
        GameUIComponents.BackButton btnBack = new GameUIComponents.BackButton();
        btnBack.addActionListener(e -> navigator.navigateTo("Menu"));
        
        JLabel title = new JLabel("How to Play", SwingConstants.CENTER);
        title.setFont(new Font(GameTheme.THAI_FONT, Font.BOLD, 40));
        title.setForeground(GameTheme.WOOD_COLOR);
        
        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT)); left.setOpaque(false); left.add(btnBack);
        topPanel.add(left, BorderLayout.WEST); topPanel.add(title, BorderLayout.CENTER);
        JPanel right = new JPanel(); right.setPreferredSize(btnBack.getPreferredSize()); right.setOpaque(false); topPanel.add(right, BorderLayout.EAST);
        add(topPanel, BorderLayout.NORTH);
    }

    private void createContentArea() {
        JPanel contentContainer = new JPanel(new BorderLayout(20, 0));
        contentContainer.setOpaque(false);
        contentContainer.setBorder(BorderFactory.createEmptyBorder(20, 50, 40, 50));

        JPanel sideMenu = new JPanel(); sideMenu.setLayout(new BoxLayout(sideMenu, BoxLayout.Y_AXIS));
        sideMenu.setOpaque(false); sideMenu.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));

        String[] topics = {"Objective", "Setup", "TurnFlow", "Tiles", "Cards", "Effects", "GameOver"};
        String[] titles = {"เป้าหมาย (Objective)", "เตรียมตัว (Setup)", "ลำดับการเล่น (Turn)", "ช่องเดิน (Tiles)", "การ์ด (Cards)", "เอฟเฟกต์ (Effects)", "แพ้ชนะ (Game Over)"};

        for (int i = 0; i < topics.length; i++) {
            JButton btn = createSideMenuButton(titles[i], topics[i]);
            sideMenu.add(btn); sideMenu.add(Box.createVerticalStrut(10));
            sideMenuButtons.put(topics[i], btn);
        }

        JScrollPane menuScroll = new JScrollPane(sideMenu);
        menuScroll.setBorder(null); menuScroll.getViewport().setOpaque(false); menuScroll.setOpaque(false);
        menuScroll.setPreferredSize(new Dimension(280, 0));

        howToContentArea = new JEditorPane(); howToContentArea.setContentType("text/html");
        howToContentArea.setEditable(false); howToContentArea.setBackground(new Color(255, 255, 255, 20));
        howToContentArea.setOpaque(false); howToContentArea.putClientProperty(JEditorPane.HONOR_DISPLAY_PROPERTIES, Boolean.TRUE);

        StringBuilder content = new StringBuilder();
        String fontName = (GameTheme.THAI_FONT != null) ? GameTheme.THAI_FONT : "Tahoma";
        String css = "<style>body { font-family: " + fontName + ", sans-serif; color: white; margin: 20px; font-size: 14px; }" +
                     "h2 { color: #FFD700; border-bottom: 1px solid #FFD700; padding-bottom: 5px; margin-top: 30px; }" +
                     "b { color: #87CEEB; } li { margin-bottom: 5px; } hr { border: 0; height: 1px; background: #8B5A2B; margin: 20px 0; }</style>";
        content.append("<html>").append(css).append("<body>");
        for (String topic : topics) {
            content.append("<a name='").append(topic).append("'></a>").append(getSectionContent(topic)).append("<br><hr><br>");
        }
        content.append("<br><br><br><br><br><br><br><br><br><br><br><br><br><br><br><br></body></html>");
        howToContentArea.setText(content.toString()); howToContentArea.setCaretPosition(0);

        JScrollPane contentScroll = new JScrollPane(howToContentArea);
        contentScroll.setBorder(BorderFactory.createLineBorder(GameTheme.WOOD_BORDER, 2));
        contentScroll.getViewport().setBackground(new Color(0, 0, 0, 100)); contentScroll.setOpaque(false);

        contentContainer.add(menuScroll, BorderLayout.WEST); contentContainer.add(contentScroll, BorderLayout.CENTER);
        add(contentContainer, BorderLayout.CENTER);
    }

    private JButton createSideMenuButton(String label, String topicKey) {
        JButton btn = new JButton(label);
        btn.setPreferredSize(new Dimension(250, 50)); btn.setMaximumSize(new Dimension(250, 50));
        String fontName = (GameTheme.THAI_FONT != null) ? GameTheme.THAI_FONT : "SansSerif";
        btn.setFont(new Font(fontName, Font.BOLD, 16)); 
        btn.setHorizontalAlignment(SwingConstants.LEFT);
        btn.setFocusPainted(false);
        btn.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(GameTheme.WOOD_BORDER, 2), BorderFactory.createEmptyBorder(0, 15, 0, 0)));
        btn.setBackground(GameTheme.WOOD_COLOR); btn.setForeground(new Color(60, 40, 20));
        btn.addActionListener(e -> scrollToSection(topicKey));
        return btn;
    }

    private void scrollToSection(String anchorName) {
        for (Map.Entry<String, JButton> entry : sideMenuButtons.entrySet()) {
            if (entry.getKey().equals(anchorName)) entry.getValue().setBackground(GameTheme.ACTIVE_MENU_COLOR);
            else entry.getValue().setBackground(GameTheme.WOOD_COLOR);
        }
        howToContentArea.scrollToReference(anchorName);
    }

    private String getSectionContent(String topic) {
        switch (topic) {
            case "Objective": return "<h2>1. เป้าหมายของเกม (Objective)</h2><p>ผู้เล่นต้องพยายามเอาชนะคู่แข่ง...</p>";
            case "Setup": return "<h2>2. การเตรียมตัวเริ่มเกม</h2><ul><li>เงินตั้งต้น</li><li>รอบสูงสุด</li></ul>";
            case "TurnFlow": return "<h2>3. ลำดับการเล่น</h2><ol><li>ทอยเต๋า</li><li>เดิน</li><li>Action</li></ol>";
            case "Tiles": return "<h2>4. ประเภทช่องเดิน</h2><ul><li>ที่ดิน</li><li>ช่องพิเศษ</li><li>เสี่ยงดวง</li></ul>";
            case "Cards": return "<h2>5. ระบบการ์ด</h2><ul><li>Angel</li><li>Shield</li><li>...</li></ul>";
            case "Effects": return "<h2>6. เอฟเฟกต์พิเศษ</h2><ul><li>Olympic</li><li>Blackout</li></ul>";
            case "GameOver": return "<h2>7. การแพ้ชนะ</h2><ul><li>Victory Condition</li><li>Bankruptcy</li></ul>";
            default: return "<h2>" + topic + "</h2><p>Content goes here...</p>";
        }
    }
}