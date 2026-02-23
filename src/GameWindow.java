import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
// ... (import อื่นๆ)

public class GameWindow extends JFrame {

    private BoardPanel boardPanel;
    private ControlPanel controlPanel;
    private EventLogPanel eventLogPanel; // 1. ประกาศตัวแปรใหม่

    public static final Color THEME_BG = new Color(30, 35, 45);

    public GameWindow(int maxTurns) {
        setTitle("Isometric Monopoly Game");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        getContentPane().setPreferredSize(new Dimension(1600, 900));
        getContentPane().setBackground(THEME_BG);
        setLayout(new BorderLayout());
        // สร้างหน้าจอแสดงเทิร์น
        TurnDisplayPanel turnDisplay = new TurnDisplayPanel();
        boardPanel.add(turnDisplay);
        boardPanel.setComponentZOrder(turnDisplay, 0); // ให้อยู่เลเยอร์หน้าสุด

        // สร้าง 3 Component หลัก
        boardPanel = new BoardPanel();
        controlPanel = new ControlPanel();
        eventLogPanel = new EventLogPanel(); // 2. สร้าง Object

        // จัดวาง Layout (ซ้าย กลาง ขวา)
        add(controlPanel, BorderLayout.WEST);
        add(boardPanel, BorderLayout.CENTER);
        add(eventLogPanel, BorderLayout.EAST); // 3. แปะไว้ฝั่งขวา

        pack();
        setLocationRelativeTo(null);
        setExtendedState(JFrame.MAXIMIZED_BOTH);
    }

    // เมธอดใหม่: ให้ Controller สั่งพิมพ์ Log ได้
    public void logEvent(String message) {
        eventLogPanel.addLog(message);
    }

    // ... (เมธอดอื่นๆ คงเดิม: setControlsEnabled, updateView, showPopup)
}