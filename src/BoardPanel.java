import javax.swing.*;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;

public class BoardPanel extends JPanel {

    // UI Overlays ที่แปะบนกระดาน
    private TurnDisplayPanel turnDisplay;
    private PlayerStatusPanel[] playerStatusPanels = new PlayerStatusPanel[4];
    
    // (สมมติ) ตัวแปรเก็บปุ่มกระดานของคุณ
    // private CustomShapeButton[] tiles = new CustomShapeButton[32];

    public BoardPanel() {
        setBackground(new Color(40, 45, 55)); // สีพื้นหลังบอร์ด
        setLayout(null); // ใช้ Absolute Layout เพื่อจัดตำแหน่งเองไม่ให้ทับกัน

        // 1. สร้างป้ายแสดงเทิร์น (ไว้ตรงกลางบน)
        turnDisplay = new TurnDisplayPanel();
        add(turnDisplay);

        // 2. สร้างแผงสถานะผู้เล่น 4 มุม
        Color[] playerColors = {
            new Color(255, 50, 50), new Color(50, 255, 50), 
            new Color(255, 215, 0), new Color(50, 200, 255)
        };
        String[] playerNames = {"Player 1", "Player 2", "Player 3", "Player 4"};

        for (int i = 0; i < 4; i++) {
            playerStatusPanels[i] = new PlayerStatusPanel(playerNames[i], playerColors[i]);
            add(playerStatusPanels[i]);
        }

        // 3. ระบบคำนวณตำแหน่งอัตโนมัติเมื่อย่อขยายหน้าจอ ป้องกันการทับซ้อน
        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                recalculateLayouts();
            }
        });
    }

    private void recalculateLayouts() {
        int w = getWidth();
        int h = getHeight();
        int margin = 20;
        int statusW = 300;
        int statusH = 100;

        // จัดตำแหน่ง Turn Display (ตรงกลางบน)
        turnDisplay.setBounds((w - 200) / 2, margin, 200, 60);

        // จัดตำแหน่ง Player Status (4 มุมพอดีเป๊ะ)
        if(playerStatusPanels[0] != null) playerStatusPanels[0].setBounds(margin, margin, statusW, statusH);
        if(playerStatusPanels[1] != null) playerStatusPanels[1].setBounds(w - statusW - margin, margin, statusW, statusH);
        if(playerStatusPanels[2] != null) playerStatusPanels[2].setBounds(margin, h - statusH - margin, statusW, statusH);
        if(playerStatusPanels[3] != null) playerStatusPanels[3].setBounds(w - statusW - margin, h - statusH - margin, statusW, statusH);

        // =========================================================
        // โซนปลอดภัยสำหรับวาด Isometric Board (กระดานจะไม่ทับกับ UI)
        // =========================================================
        int boardSafeY = margin + statusH + 20; // เริ่มวาดใต้ Status Panel
        int boardSafeHeight = h - (margin * 2) - (statusH * 2) - 40; 
        
        // TODO: นำโค้ดวาด IsometricBoard เดิมของคุณ มาคำนวณพิกัดโดยใช้ boardSafeY และ boardSafeHeight เป็นกรอบจำกัด
        // เพื่อให้กระดานบีบตัวเองอยู่ตรงกลางโดยไม่โดน Panel 4 มุมบัง
    }

    // เมธอดรับคำสั่งจาก GameWindow เพื่อกระจายต่อให้ UI ย่อย
    public void updateTurn(int current, int max) {
        turnDisplay.updateTurn(current, max);
    }

    public void updatePlayerStatus(int index, int cash, int assets, String status, boolean isJailed) {
        if (index >= 0 && index < 4) {
            playerStatusPanels[index].updateData(cash, assets, status, isJailed);
        }
    }

    public void updatePlayerPosition(int playerIndex, int newPosition) {
        // TODO: อัปเดตพิกัดจุดวงกลมตัวละครบนกระดาน
    }
}