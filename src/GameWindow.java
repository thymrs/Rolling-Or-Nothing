import java.awt.*;
import java.awt.event.ActionListener;
import java.util.List;
import javax.swing.*;

public class GameWindow extends JFrame {

    private ControlPanel controlPanel;
    public BoardPanel boardPanel;
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

    public void setTileActionListener(ActionListener listener) {
        boardPanel.setTileActionListener(listener);
    }
}