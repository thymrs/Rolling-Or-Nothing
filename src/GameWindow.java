import java.awt.*;
import java.awt.event.ActionListener;
import java.util.List;
import javax.swing.*;

public class GameWindow extends JFrame {

    private ControlPanel controlPanel;
    private BoardPanel boardPanel;
    private static EventLogPanel eventLogPanel;

    public GameWindow() {
        setTitle("Rolling Or Nothing");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setExtendedState(JFrame.MAXIMIZED_BOTH); // เปิดมาเต็มจอ
        setLayout(new BorderLayout());

        // สร้างและจัดวาง Panel
        controlPanel = new ControlPanel();
        add(controlPanel, BorderLayout.WEST);

        // เพิ่ม Board (กระดานเกม) ไว้ตรงกลาง
        boardPanel = new BoardPanel();
        add(boardPanel, BorderLayout.CENTER);

        // เพิ่ม Event Log (ประวัติเหตุการณ์) ไว้ด้านขวา
        eventLogPanel = new EventLogPanel();
        add(eventLogPanel, BorderLayout.EAST);
    }

    // --- เมธอดที่ GameController เรียกใช้งาน ---
    public void setRollEnabled(boolean enabled) {
        boardPanel.setRollEnabled(enabled);
    }

    public void setActionListener(ActionListener listener) {
    controlPanel.setActionListener(listener);
    boardPanel.setRollActionListener(listener); 
}

    public void setTileSelectionListener(ActionListener listener) {
        boardPanel.setTileSelectionListener(listener);
    }

    public void enableTileSelection(boolean enabled) {
        boardPanel.enableTileSelection(enabled);
    }

    public ControlPanel getControlPanel() {
        return controlPanel;
    }

    public static EventLogPanel getEventLogPanel() {
        return eventLogPanel;
    }

    // เมธอดสำหรับเพิ่มข้อความลงใน Event Log
    public void addLog(String message) {
        if (eventLogPanel != null) {
            eventLogPanel.addLog(message);
        }
    }

    public void updateView(GameState state) {
        // อัปเดตข้อมูลบนกระดาน เช่น ตำแหน่งตัวละคร, เงิน
        if (boardPanel != null) {
            // ถ้าใน BoardPanel ของคุณใช้ชื่อเมธอดอื่นในการอัปเดต ให้เปลี่ยนชื่อตรงนี้นะครับ
            boardPanel.updateBoard(state);
        }
        TurnDisplayPanel.updateTurn(state.getTurnCount());
        repaint();
    }

    public void showPopup(String message) {
        // ป้องกัน UI ค้างหากถูกเรียกจาก Thread ของ Bot
        if (!SwingUtilities.isEventDispatchThread()) {
            SwingUtilities.invokeLater(() -> showPopup(message));
            return;
        }
        JOptionPane.showMessageDialog(this, message, "Alert", JOptionPane.INFORMATION_MESSAGE);
    }

    public Player showSelectTargetDialog(List<Player> opponents) {
        // คืนค่า null ไว้ก่อนชั่วคราว หรือใส่ Logic เลือกเป้าหมายของคุณ
        return null;
    }
    
    /**
     * Display game-over screen with win condition details
     */
    public void showGameOver(VictoryType victoryType, Player winner) {
        JFrame gameOverFrame = new JFrame("GAME OVER");
        gameOverFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        gameOverFrame.setExtendedState(JFrame.MAXIMIZED_BOTH);
        gameOverFrame.setLayout(new BorderLayout());
        gameOverFrame.getContentPane().setBackground(UIConstants.PRIMARY_DARK);
        
        // Header Panel
        JPanel headerPanel = new JPanel();
        headerPanel.setBackground(UIConstants.BG_HEADER);
        headerPanel.setBorder(BorderFactory.createEmptyBorder(40, 0, 40, 0));
        
        JLabel titleLabel = new JLabel("🏆 GAME OVER 🏆");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 64));
        titleLabel.setForeground(UIConstants.ACCENT_WARNING);
        titleLabel.setHorizontalAlignment(SwingConstants.CENTER);
        headerPanel.add(titleLabel);
        gameOverFrame.add(headerPanel, BorderLayout.NORTH);
        
        // Content Panel
        JPanel contentPanel = new JPanel();
        contentPanel.setBackground(UIConstants.PRIMARY_DARK);
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setBorder(BorderFactory.createEmptyBorder(20, 100, 20, 100));
        
        // Winner info
        JLabel winnerLabel = new JLabel("Winner: " + winner.getName());
        winnerLabel.setFont(new Font("Arial", Font.BOLD, 40));
        winnerLabel.setForeground(UIConstants.ACCENT_SUCCESS);
        winnerLabel.setAlignmentX(JComponent.CENTER_ALIGNMENT);
        contentPanel.add(winnerLabel);
        contentPanel.add(Box.createVerticalStrut(20));
        
        // Victory condition details
        String victoryText = getVictoryConditionText(victoryType, winner);
        JLabel victoryLabel = new JLabel("<html><center>" + victoryText + "</center></html>");
        victoryLabel.setFont(new Font("Arial", Font.PLAIN, 24));
        victoryLabel.setForeground(UIConstants.TEXT_PRIMARY);
        victoryLabel.setAlignmentX(JComponent.CENTER_ALIGNMENT);
        contentPanel.add(victoryLabel);
        contentPanel.add(Box.createVerticalStrut(30));
        
        // Placeholder for victory graphics (future enhancement)
        JPanel graphicsPlaceholder = new JPanel();
        graphicsPlaceholder.setBackground(UIConstants.BG_PANEL);
        graphicsPlaceholder.setPreferredSize(new Dimension(300, 200));
        graphicsPlaceholder.setBorder(BorderFactory.createLineBorder(UIConstants.ACCENT_INFO, 2));
        
        JLabel graphicsLabel = new JLabel("[Victory Graphics - " + victoryType + "]");
        graphicsLabel.setForeground(UIConstants.TEXT_MUTED);
        graphicsPlaceholder.add(graphicsLabel);
        graphicsPlaceholder.setAlignmentX(JComponent.CENTER_ALIGNMENT);
        contentPanel.add(graphicsPlaceholder);
        
        // Winner stats
        contentPanel.add(Box.createVerticalStrut(20));
        String statsText = String.format(
            "<html><center>Total Assets: $%,d | Properties: %d | Buildings: %d</center></html>",
            winner.getTotalAssetsValue(),
            winner.getOwnedLands().size(),
            winner.getOwnedLands().stream().mapToInt(PropertyTile::getBuildingLevel).sum()
        );
        JLabel statsLabel = new JLabel(statsText);
        statsLabel.setFont(new Font("Arial", Font.PLAIN, 16));
        statsLabel.setForeground(UIConstants.TEXT_SECONDARY);
        statsLabel.setAlignmentX(JComponent.CENTER_ALIGNMENT);
        contentPanel.add(statsLabel);
        
        gameOverFrame.add(contentPanel, BorderLayout.CENTER);
        
        // Footer Panel
        JPanel footerPanel = new JPanel();
        footerPanel.setBackground(UIConstants.BG_HEADER);
        footerPanel.setBorder(BorderFactory.createEmptyBorder(20, 0, 20, 0));
        
        JButton exitButton = new JButton("EXIT");
        exitButton.setFont(new Font("Arial", Font.BOLD, 18));
        exitButton.setBackground(UIConstants.ACCENT_DANGER);
        exitButton.setForeground(UIConstants.TEXT_PRIMARY);
        exitButton.setFocusPainted(false);
        exitButton.addActionListener(e -> System.exit(0));
        footerPanel.add(exitButton);
        
        gameOverFrame.add(footerPanel, BorderLayout.SOUTH);
        gameOverFrame.setVisible(true);
    }
    
    /**
     * Get victory condition description text
     */
    private String getVictoryConditionText(VictoryType victoryType, Player winner) {
        return switch (victoryType) {
            case LAST_PLAYER_STANDING -> 
                "Only player remaining!<br>All opponents filed for bankruptcy.";
            case LINE_MONOPOLY -> 
                "Achieved Line Monopoly!<br>Completed one full color group.";
            case TRIPLE_MONOPOLY -> 
                "Achieved Triple Monopoly!<br>Controls 3 color groups simultaneously.";
            case TOURISM_MONOPOLY -> 
                "Tourism Monopoly Victory!<br>Owns all tourism destination properties.";
            default -> "Victory achieved!";
        };
    }
}