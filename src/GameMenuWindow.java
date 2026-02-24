import javax.swing.*;
import java.awt.*;

public class GameMenuWindow extends JFrame {

    private CardLayout cardLayout;
    private JPanel mainContainer;

    // สี Theme หลัก
    private final Color BG_COLOR = new Color(40, 44, 52);
    private final Color TEXT_COLOR = Color.WHITE;

    public GameMenuWindow() {
        setTitle("Monopoly - Game Menu");
        setSize(1024, 768);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);

        cardLayout = new CardLayout();
        mainContainer = new JPanel(cardLayout);

        mainContainer.add(createMainMenu(), "MENU");
        mainContainer.add(createSetupMenu(), "SETUP");
        mainContainer.add(createHowToPlayMenu(), "HOWTOPLAY");

        add(mainContainer);
        cardLayout.show(mainContainer, "MENU");
    }

    // ==========================================
    // 1. หน้าต่างเมนูหลัก
    // ==========================================
    private JPanel createMainMenu() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(BG_COLOR);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(15, 15, 15, 15);
        gbc.gridx = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JLabel titleLabel = new JLabel("MONOPOLY", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 64));
        titleLabel.setForeground(TEXT_COLOR);

        JButton btnPlay = createStyledButton("Play");
        JButton btnHowToPlay = createStyledButton("How to Play");
        JButton btnSettings = createStyledButton("Settings");
        JButton btnExit = createStyledButton("Exit");

        // กำหนด Event ปุ่ม
        btnPlay.addActionListener(e -> cardLayout.show(mainContainer, "SETUP"));
        btnHowToPlay.addActionListener(e -> cardLayout.show(mainContainer, "HOWTOPLAY"));
        btnSettings.addActionListener(e -> JOptionPane.showMessageDialog(this, "Settings Menu"));
        btnExit.addActionListener(e -> System.exit(0));

        gbc.gridy = 0;
        panel.add(titleLabel, gbc);
        gbc.gridy = 1;
        gbc.insets = new Insets(40, 15, 10, 15);
        panel.add(btnPlay, gbc);
        gbc.gridy = 2;
        gbc.insets = new Insets(10, 15, 10, 15);
        panel.add(btnHowToPlay, gbc);
        gbc.gridy = 3;
        panel.add(btnSettings, gbc);
        gbc.gridy = 4;
        panel.add(btnExit, gbc);

        return panel;
    }

    // ==========================================
    // 2. หน้าต่าง Setup รับค่าและเริ่มเกม
    // ==========================================
    private JPanel createSetupMenu() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(BG_COLOR);

        JLabel title = new JLabel("Game Setup", SwingConstants.CENTER);
        title.setFont(new Font("Arial", Font.BOLD, 48));
        title.setForeground(TEXT_COLOR);
        title.setBorder(BorderFactory.createEmptyBorder(40, 0, 20, 0));

        JPanel formPanel = new JPanel(new GridLayout(2, 2, 20, 20));
        formPanel.setOpaque(false);
        formPanel.setBorder(BorderFactory.createEmptyBorder(80, 250, 150, 250));

        JComboBox<Integer> botCountCombo = new JComboBox<>(new Integer[] { 1, 2, 3 });
        botCountCombo.setSelectedItem(3);

        JComboBox<String> diffCombo = new JComboBox<>(new String[] { "EASY", "NORMAL", "HARD" });
        diffCombo.setSelectedItem("HARD");

        JLabel botLabel = new JLabel("จำนวนบอท (Bot Count):");
        botLabel.setFont(new Font("Tahoma", Font.BOLD, 20));
        botLabel.setForeground(TEXT_COLOR);

        JLabel diffLabel = new JLabel("ระดับความยาก (Difficulty):");
        diffLabel.setFont(new Font("Tahoma", Font.BOLD, 20));
        diffLabel.setForeground(TEXT_COLOR);

        formPanel.add(botLabel);
        formPanel.add(botCountCombo);
        formPanel.add(diffLabel);
        formPanel.add(diffCombo);

        JPanel bottomPanel = new JPanel();
        bottomPanel.setOpaque(false);
        bottomPanel.setBorder(BorderFactory.createEmptyBorder(20, 0, 50, 0));

        JButton btnBack = createStyledButton("Back");
        JButton btnStart = createStyledButton("Start Game!");
        btnStart.setBackground(new Color(46, 139, 87));
        btnStart.setForeground(Color.WHITE);

        btnBack.addActionListener(e -> cardLayout.show(mainContainer, "MENU"));

        btnStart.addActionListener(e -> {
            try {
                int selectedBotCount = (Integer) botCountCombo.getSelectedItem();
                String selectedDiffStr = (String) diffCombo.getSelectedItem();

                DifficultyLevel selectedDifficulty = DifficultyLevel.valueOf(selectedDiffStr);
                
                GameConfig config = new GameConfig.Builder()
                    .initialMoney(200000)
                    .maxTurns(50)
                    .mapName("default")
                    .humanCount(1)
                    .botCount(selectedBotCount)
                    .botDifficulty(selectedDifficulty)
                    .passGoSalary(15000)
                    .taxPercentage(10)
                    .build();
                
                MapLoader loader = new MapLoader();
                Board board = loader.loadMap(config.getMapName());
                
                GameWindow view = new GameWindow();
                GameState state = new GameState();
                state.setBoard(board);
                state.addGameEventListener(GameWindow.getEventLogPanel());
                
                GameController controller = new GameController(view, state);
                
                view.setVisible(true);
                controller.startGame(config);
                
                System.out.println("Monopoly Game Started with User Configuration!");
                this.dispose();

            } catch (Exception ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(this, "Error starting game: " + ex.getMessage(), "Error",
                        JOptionPane.ERROR_MESSAGE);
            }
        });

        bottomPanel.add(btnBack);
        bottomPanel.add(btnStart);

        panel.add(title, BorderLayout.NORTH);
        panel.add(formPanel, BorderLayout.CENTER);
        panel.add(bottomPanel, BorderLayout.SOUTH);

        return panel;
    }

    // ==========================================
    // 3. หน้าต่างวิธีเล่น (How to Play) แบบมีเมนูด้านซ้ายและข้อความยาวๆ
    // ==========================================
    private JPanel createHowToPlayMenu() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(BG_COLOR);

        JLabel title = new JLabel("How to Play", SwingConstants.CENTER);
        title.setFont(new Font("Arial", Font.BOLD, 48));
        title.setForeground(TEXT_COLOR);
        title.setBorder(BorderFactory.createEmptyBorder(20, 0, 20, 0));

        // คอนเทนเนอร์หลักสำหรับแบ่งซ้าย-ขวา
        JPanel splitContainer = new JPanel(new BorderLayout(10, 0));
        splitContainer.setOpaque(false);
        splitContainer.setBorder(BorderFactory.createEmptyBorder(0, 20, 0, 20));

        // --- ด้านซ้าย: เมนูปุ่มกดเพื่อเลื่อนไปยังหัวข้อ (Sidebar) ---
        JPanel sideMenu = new JPanel(new GridLayout(7, 1, 5, 10)); // 7 หัวข้อ
        sideMenu.setOpaque(false);
        sideMenu.setPreferredSize(new Dimension(250, 0));

        // --- ด้านขวา: หน้าต่างเนื้อหา (JEditorPane แบบ HTML) ---
        JEditorPane contentArea = new JEditorPane();
        contentArea.setContentType("text/html");
        contentArea.setEditable(false);
        contentArea.setBackground(new Color(60, 63, 65));

        // รวบรวมหัวข้อและสร้าง HTML
        String[] topics = { "Objective", "Setup", "TurnFlow", "Tiles", "Cards", "Effects", "GameOver" };
        String[] topicNames = { "1. เป้าหมาย", "2. การเตรียมตัว", "3. ลำดับการเล่น", "4. ประเภทช่องเดิน",
                "5. ระบบการ์ด", "6. เอฟเฟกต์พิเศษ", "7. การแพ้ชนะ" };

        // สร้างเนื้อหา HTML แบบกำหนดสีและฟอนต์
        StringBuilder html = new StringBuilder(
                "<html><body style='padding:15px; font-family:tahoma, sans-serif; font-size:16px; color:white;'>");

        for (int i = 0; i < topics.length; i++) {
            String topicKey = topics[i];
            String topicTitle = topicNames[i];

            // 1. สร้างปุ่มเมนูด้านซ้าย
            JButton btnMenu = createStyledButton(topicTitle);
            btnMenu.setFont(new Font("Tahoma", Font.BOLD, 18));
            btnMenu.setPreferredSize(new Dimension(250, 50));
            // เมื่อกดปุ่ม ให้เลื่อนหน้าจอไปยัง anchor (name) ที่กำหนดไว้ใน HTML
            btnMenu.addActionListener(e -> contentArea.scrollToReference(topicKey));
            sideMenu.add(btnMenu);

            // 2. สร้างเนื้อหา HTML ด้านขวาและใส่ Anchor Tag (<a name="...">)
            html.append("<a name='").append(topicKey).append("'></a>");
            html.append("<h2 style='color:#FFD700;'>").append(topicTitle).append("</h2>"); // หัวข้อสีทอง
            html.append(getSectionContent(topicKey));
            html.append("<hr style='border: 1px solid #888;'>");
        }
        html.append("</body></html>");
        contentArea.setText(html.toString());

        JScrollPane scrollPane = new JScrollPane(contentArea);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16); // ทำให้เลื่อนเมาส์สมูทขึ้น

        // นำซ้ายและขวาใส่ลงใน Container
        splitContainer.add(sideMenu, BorderLayout.WEST);
        splitContainer.add(scrollPane, BorderLayout.CENTER);

        // --- ปุ่มย้อนกลับด้านล่าง ---
        JPanel bottomPanel = new JPanel();
        bottomPanel.setOpaque(false);
        bottomPanel.setBorder(BorderFactory.createEmptyBorder(20, 0, 30, 0));
        JButton btnBack = createStyledButton("Back to Menu");
        btnBack.addActionListener(e -> cardLayout.show(mainContainer, "MENU"));
        bottomPanel.add(btnBack);

        panel.add(title, BorderLayout.NORTH);
        panel.add(splitContainer, BorderLayout.CENTER);
        panel.add(bottomPanel, BorderLayout.SOUTH);

        return panel;
    }

    // ฟังก์ชันดึงเนื้อหาย่อยของหน้า How to play (นำมาจากโค้ดเก่าของคุณ)
    private String getSectionContent(String topic) {
        switch (topic) {
            case "Objective":
                return "<p>เป้าหมายหลักคือการทำให้ผู้เล่นคนอื่นล้มละลาย หรือทำตามเงื่อนไขพิเศษเพื่อคว้าชัยชนะให้ได้ก่อนใคร!</p>";
            case "Setup":
                return "<p>ผู้เล่นแต่ละคนจะเริ่มต้นด้วยเงินทุนจำนวนหนึ่ง (เช่น 200,000) ที่จุด Start และจะสลับกันทอยลูกเต๋าเพื่อเดินไปตามช่องต่างๆ</p>";
            case "TurnFlow":
                return "<ol>" +
                        "<li>ทอยเต๋าเพื่อหาจำนวนช่องที่ต้องเดิน</li>" +
                        "<li>เดินตามจำนวนลูกเต๋าที่ทอยได้</li>" +
                        "<li>ทำ Action ประจำช่อง (ซื้อที่ดิน, จ่ายค่าผ่านทาง, เปิดการ์ดดวง ฯลฯ)</li>" +
                        "</ol>";
            case "Tiles":
                return "<ul>" +
                        "<li><b>ที่ดิน (Property):</b> สามารถซื้อและอัพเกรดเป็น บ้าน 1-3 หลัง และ แลนด์มาร์ค</li>" +
                        "<li><b>ช่องพิเศษ (Special):</b> จุดเริ่มต้น, คุก, ท่องเที่ยว, จัดเทศกาล</li>" +
                        "<li><b>เสี่ยงดวง (Chance):</b> เปิดการ์ดดวงเพื่อรับผลลัพธ์แบบสุ่ม</li>" +
                        "</ul>";
            case "Cards":
                return "<ul>" +
                        "<li><b>Angel Card:</b> ป้องกันการเสียเงินค่าผ่านทาง 1 ครั้ง</li>" +
                        "<li><b>Shield Card:</b> ป้องกันการโจมตีจากผู้เล่นอื่น</li>" +
                        "</ul>";
            case "Effects":
                return "<ul>" +
                        "<li><b>Olympic/Festival:</b> เพิ่มค่าผ่านทาง x2 ในพื้นที่ที่เลือก</li>" +
                        "<li><b>Blackout:</b> ทำให้พื้นที่นั้นค่าผ่านทางเป็น 0 ชั่วคราว</li>" +
                        "</ul>";
            case "GameOver":
                return "<ul>" +
                        "<li><b>Bankruptcy:</b> ล้มละลาย (เงินหมดตัวและไม่มีที่ดินให้ขาย)</li>" +
                        "<li><b>Line Victory:</b> ครอบครองที่ดินสีเดียวกันทั้งแถว</li>" +
                        "<li><b>Triple Victory:</b> ครอบครองที่ดินครบสี 3 กลุ่ม</li>" +
                        "<li><b>Tourism Victory:</b> ครอบครองเกาะครบ 5 แห่ง</li>" +
                        "</ul>";
            default:
                return "<p>ไม่มีข้อมูล</p>";
        }
    }

    // ==========================================
    // Utility Methods
    // ==========================================
    private JButton createStyledButton(String text) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Arial", Font.BOLD, 22));
        btn.setPreferredSize(new Dimension(300, 60));
        btn.setFocusPainted(false);
        btn.setBackground(Color.LIGHT_GRAY);
        btn.setForeground(Color.BLACK);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return btn;
    }
}