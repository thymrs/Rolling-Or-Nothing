import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.util.List;

public class GameWindow extends JFrame {
    
    // UI Components
    private ControlPanel controlPanel;
    // private BoardPanel boardPanel;      // คุณน่าจะมีคลาสนี้อยู่แล้ว
    // private EventLogPanel eventLogPanel; // คุณน่าจะมีคลาสนี้อยู่แล้ว

    public GameWindow() {
        setTitle("Rolling Or Nothing");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setExtendedState(JFrame.MAXIMIZED_BOTH); // เปิดมาเต็มจออัตโนมัติ
        setLayout(new BorderLayout());

        // 1. สร้าง Panels
        controlPanel = new ControlPanel();
        
        // *หมายเหตุ: ถ้าคุณมี BoardPanel และ EventLogPanel แล้ว ให้ Uncomment ด้านล่างนี้*
        // boardPanel = new BoardPanel();
        // eventLogPanel = new EventLogPanel();

        // 2. จัดวางลงหน้าต่าง
        add(controlPanel, BorderLayout.WEST);
        
        // add(boardPanel, BorderLayout.CENTER);
        // add(eventLogPanel, BorderLayout.EAST);
    }

    // =========================================================
    // Methods ที่ GameController ของเพื่อนเรียกใช้งาน
    // =========================================================

    /**
     * รับ ActionListener จาก GameController ไปผูกกับปุ่มใน ControlPanel
     */
    public void setActionListener(ActionListener listener) {
        controlPanel.setActionListener(listener);
    }

    /**
     * คืนค่า ControlPanel ให้ Controller สั่งเปิด/ปิดปุ่มได้
     * (ตรงกับที่เพื่อนเรียก: view.getControlPanel().setButtonsEnabled(...))
     */
    public ControlPanel getControlPanel() {
        return controlPanel;
    }

    /**
     * อัปเดตหน้าจอทั้งหมดเมื่อ GameState เปลี่ยนแปลง
     */
    public void updateView(GameState state) {
        // boardPanel.updateBoard(state); // ส่ง state ไปให้กระดานวาดใหม่
        repaint();
    }

    /**
     * แสดง Pop-up ข้อความแจ้งเตือนต่างๆ 
     */
    public void showPopup(String message) {
        // เด้ง Dialog ให้ผู้เล่นเห็น
        JOptionPane.showMessageDialog(this, message, "แจ้งเตือน", JOptionPane.INFORMATION_MESSAGE);
        
        // *ถ้ามี EventLogPanel ก็สั่งพิมพ์ลง Log ทางขวาด้วย จะดูดีมาก*
        // if (eventLogPanel != null) {
        //     eventLogPanel.logEvent(message);
        // }
    }

    /**
     * เปิดหน้าต่างเลือกเป้าหมายโจมตี สำหรับการ์ดที่ต้องการเป้าหมาย
     * (ตรงกับที่เพื่อนเรียก: target = view.showSelectTargetDialog(opponents);)
     */
    public Player showSelectTargetDialog(List<Player> opponents) {
        // เรียกใช้ GameDialogManager ที่เราเพิ่งเขียนไปมาแสดงผล
        // (เราสร้าง Card จำลองขึ้นมาเป็น Title เฉยๆ เพราะ Controller เพื่อนไม่ได้ส่งชื่อการ์ดมาด้วย)
        Card dummyCard = new Card("เลือกเป้าหมาย"); 
        return GameDialogManager.showAttackCardDialog(this, dummyCard, opponents);
    }
}