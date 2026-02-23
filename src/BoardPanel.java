import javax.swing.*;
import java.awt.*;
import java.util.List;

public class BoardPanel extends JPanel {

    // UI Overlays ที่แปะบนกระดาน
    private TurnDisplayPanel turnDisplay;
    private PlayerStatusPanel[] playerStatusPanels = new PlayerStatusPanel[4];

    public BoardPanel() {
        setBackground(new Color(40, 45, 55)); // สีพื้นหลังบอร์ด
        setLayout(null); // ใช้ Absolute Layout เพื่อจัดตำแหน่งเองไม่ให้ทับกัน

        // 1. สร้างป้ายแสดงเทิร์น (ไว้ตรงกลางบน)
        turnDisplay = new TurnDisplayPanel();
        add(turnDisplay);

        // 2. สร้างแผงสถานะผู้เล่น 4 มุม (รอรับข้อมูลจริงตอน update)
        Color[] defaultColors = {
            new Color(255, 50, 50), new Color(50, 255, 50), 
            new Color(255, 215, 0), new Color(50, 200, 255)
        };
        
        // สร้าง Panel ว่างๆ รอไว้ก่อน 4 มุม
        for (int i = 0; i < 4; i++) {
            playerStatusPanels[i] = new PlayerStatusPanel("Player " + (i+1), defaultColors[i]);
            add(playerStatusPanels[i]);
        }
    }

    @Override
    public void doLayout() {
        super.doLayout();
        int w = getWidth();
        int h = getHeight();

        // จัดตำแหน่ง TurnDisplay (ตรงกลางบน)
        int turnW = 200;
        int turnH = 60;
        turnDisplay.setBounds((w - turnW) / 2, 20, turnW, turnH);

        // จัดตำแหน่ง PlayerStatus 4 มุม
        int statusW = 240;
        int statusH = 130;
        int margin = 20;

        if(playerStatusPanels[0] != null) playerStatusPanels[0].setBounds(margin, margin, statusW, statusH);
        if(playerStatusPanels[1] != null) playerStatusPanels[1].setBounds(w - statusW - margin, margin, statusW, statusH);
        if(playerStatusPanels[2] != null) playerStatusPanels[2].setBounds(margin, h - statusH - margin, statusW, statusH);
        if(playerStatusPanels[3] != null) playerStatusPanels[3].setBounds(w - statusW - margin, h - statusH - margin, statusW, statusH);
    }

    /**
     * 🟢 เมธอดสำคัญ: อัปเดต UI ทั้งหมดบนกระดาน โดยดึงข้อมูลตรงจาก GameState
     */
    public void updateFromGameState(GameState state) {
        if (state == null) return;

        // --- 1. อัปเดตป้ายบอกเทิร์น ---
        // หมายเหตุ: ต้องมีเมธอด getTurnCount() ใน GameState และ getMaxTurns() ใน GameConfig
        int currentTurn = state.getTurnCount(); 
        int maxTurns = state.getConfig().getMaxTurns(); 
        turnDisplay.updateTurn(currentTurn, maxTurns);

        // --- 2. อัปเดตสถานะผู้เล่นทั้ง 4 มุม ---
        List<Player> players = state.getPlayers();
        for (int i = 0; i < players.size(); i++) {
            if (i < 4 && playerStatusPanels[i] != null) {
                Player p = players.get(i);
                
                int money = p.getMoney();
                
                // คำนวณทรัพย์สิน: ราคาที่ดิน + ราคาบ้าน (ปรับสูตรตามคลาส PropertyTile ของคุณได้เลย)
                int totalAssets = 0;
                if (p.getOwnedLands() != null) {
                    for (PropertyTile tile : p.getOwnedLands()) {
                        // สมมติว่าทรัพย์สิน = ราคาซื้อที่ดิน + (เลเวลบ้าน * ราคาอัปเกรด)
                        totalAssets += tile.getPurchasePrice() + (tile.getBuildingLevel() * tile.getPurchasePrice()); 
                    }
                }
                
                boolean isJailed = p.getIsJailed();

                // สั่งอัปเดตไปที่ UI มุมนั้นๆ
                playerStatusPanels[i].updateData(money, totalAssets, isJailed);
                
                // ทำไฮไลต์ให้คนที่กำลังเป็น Turn ปัจจุบัน (เพื่อให้รู้ว่าตาใครเล่น)
                if (state.getCurrentPlayer() == p) {
                    playerStatusPanels[i].setBorder(BorderFactory.createLineBorder(Color.WHITE, 3));
                } else {
                    playerStatusPanels[i].setBorder(BorderFactory.createLineBorder(Color.DARK_GRAY, 1));
                }
            }
        }
        
        // บังคับให้หน้าจอวาดตัวเองใหม่
        repaint();
    }
}