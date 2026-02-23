import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.util.List;

public class GameWindow extends JFrame {
    
    private ControlPanel controlPanel;
    // private BoardPanel boardPanel; 

    public GameWindow() {
        setTitle("Rolling Or Nothing");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setExtendedState(JFrame.MAXIMIZED_BOTH); // เปิดมาเต็มจอ
        setLayout(new BorderLayout());

        // สร้างและจัดวาง Panel
        controlPanel = new ControlPanel();
        add(controlPanel, BorderLayout.WEST);
        
        // ถ้าคุณมี BoardPanel แล้ว ให้ Uncomment ด้านล่างนี้
        // boardPanel = new BoardPanel();
        // add(boardPanel, BorderLayout.CENTER);
    }

    // --- เมธอดที่ GameController เรียกใช้งาน ---

    public void setActionListener(ActionListener listener) {
        controlPanel.setActionListener(listener);
    }

    public ControlPanel getControlPanel() {
        return controlPanel;
    }

    public void updateView(GameState state) {
        // อัปเดตข้อมูลบนกระดาน เช่น ตำแหน่งตัวละคร, เงิน
        // if (boardPanel != null) boardPanel.updateBoard(state);
        repaint();
    }

    public void showPopup(String message) {
        // ป้องกัน UI ค้างหากถูกเรียกจาก Thread ของ Bot
        if (!SwingUtilities.isEventDispatchThread()) {
            SwingUtilities.invokeLater(() -> showPopup(message));
            return;
        }
        JOptionPane.showMessageDialog(this, message, "แจ้งเตือน", JOptionPane.INFORMATION_MESSAGE);
    }

    public String showSelectTargetDialog(List<String> opponentNames) {
        // เรียกใช้ Dialog Manager ให้ผู้เล่นเลือกเป้าหมาย โดยส่งไปแค่รายชื่อ
        return GameDialogManager.showTargetSelection(this, opponentNames);
    }
}