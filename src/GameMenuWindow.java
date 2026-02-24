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

        gbc.gridy = 0; panel.add(titleLabel, gbc);
        gbc.gridy = 1; gbc.insets = new Insets(40, 15, 10, 15); panel.add(btnPlay, gbc);
        gbc.gridy = 2; gbc.insets = new Insets(10, 15, 10, 15); panel.add(btnHowToPlay, gbc);
        gbc.gridy = 3; panel.add(btnSettings, gbc);
        gbc.gridy = 4; panel.add(btnExit, gbc);

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

        // Component สำหรับรับค่าจากผู้เล่น
        JComboBox<Integer> botCountCombo = new JComboBox<>(new Integer[]{1, 2, 3});
        botCountCombo.setSelectedItem(3); // ค่า Default

        // สมมติว่ามี Enum ชื่อ DifficultyLevel ตามโค้ดที่คุณส่งมา
        JComboBox<String> diffCombo = new JComboBox<>(new String[]{"EASY", "NORMAL", "HARD"});
        diffCombo.setSelectedItem("HARD"); // ค่า Default

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

        // ========================================================
        // ลอจิกการเริ่มเกมจาก Main.java ถูกย้ายมาทำงานที่นี่
        // ========================================================
        btnStart.addActionListener(e -> {
            try {
                // 1. ดึงค่าที่ผู้เล่นเลือกจากหน้าจอ UI
                int selectedBotCount = (Integer) botCountCombo.getSelectedItem();
                String selectedDiffStr = (String) diffCombo.getSelectedItem();
                
                // แปลง String กลับไปเป็น Enum DifficultyLevel ของคุณ
                DifficultyLevel selectedDifficulty = DifficultyLevel.valueOf(selectedDiffStr);

                // 2. สร้าง GameConfig ตามค่าที่ได้รับ
                GameConfig config = new GameConfig.Builder()
                        .initialMoney(200000)
                        .maxTurns(50)
                        .mapName("default")
                        .humanCount(1)
                        .botCount(selectedBotCount)          // นำค่าไปใส่ Config
                        .botDifficulty(selectedDifficulty)   // นำค่าไปใส่ Config
                        .passGoSalary(15000)
                        .taxPercentage(10)
                        .build();

                // 3. เริ่มกระบวนการโหลดบอร์ดและคลาสต่างๆ
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

                // 4. ปิดหน้าต่าง Menu ทิ้ง เพราะเกมเริ่มแล้ว
                this.dispose(); 

            } catch (Exception ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(this, "Error starting game: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
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
    // 3. หน้าต่างวิธีเล่น
    // ==========================================
    private JPanel createHowToPlayMenu() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(BG_COLOR);

        JLabel title = new JLabel("How to Play", SwingConstants.CENTER);
        title.setFont(new Font("Arial", Font.BOLD, 48));
        title.setForeground(TEXT_COLOR);
        title.setBorder(BorderFactory.createEmptyBorder(40, 0, 20, 0));

        JTextArea textArea = new JTextArea("\n  วิธีเล่น:\n  1. ทอยเต๋าเพื่อเดิน\n  2. ซื้อที่ดิน\n  3. เก็บค่าผ่านทาง");
        textArea.setFont(new Font("Tahoma", Font.PLAIN, 24));
        textArea.setForeground(TEXT_COLOR);
        textArea.setBackground(new Color(60, 63, 65));
        textArea.setEditable(false);
        textArea.setMargin(new Insets(20, 20, 20, 20));

        JPanel bottomPanel = new JPanel();
        bottomPanel.setOpaque(false);
        bottomPanel.setBorder(BorderFactory.createEmptyBorder(20, 0, 50, 0));
        JButton btnBack = createStyledButton("Back");
        btnBack.addActionListener(e -> cardLayout.show(mainContainer, "MENU"));
        bottomPanel.add(btnBack);

        panel.add(title, BorderLayout.NORTH);
        panel.add(new JScrollPane(textArea), BorderLayout.CENTER);
        panel.add(bottomPanel, BorderLayout.SOUTH);

        return panel;
    }

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