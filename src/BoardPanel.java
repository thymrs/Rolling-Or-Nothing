import javax.swing.*;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.util.List;

public class BoardPanel extends JPanel {

    // 1. UI Overlays (แผงควบคุมและข้อมูล)
    private TurnDisplayPanel turnDisplay;
    private PlayerStatusPanel[] playerStatusPanels = new PlayerStatusPanel[4];

    // 2. Isometric Board (ช่องกระดาน)
    private CustomShapeButton[] tiles = new CustomShapeButton[32];
    
    // 3. Player Markers (ตัวละครผู้เล่นบนกระดาน)
    private JPanel[] playerMarkers = new JPanel[4];
    private int[] playerPositions = new int[4]; // เก็บตำแหน่งปัจจุบันของผู้เล่น

    // สีประจำตัวผู้เล่น
    private final Color[] defaultColors = {
        new Color(255, 50, 50),   // Player 1 (Red)
        new Color(50, 255, 50),   // Player 2 (Green)
        new Color(255, 215, 0),   // Player 3 (Gold)
        new Color(50, 200, 255)   // Player 4 (Blue)
    };

    public BoardPanel() {
        setBackground(new Color(40, 45, 55)); // สีพื้นหลังบอร์ด
        setLayout(null); // ใช้ Absolute Layout เพื่ออิสระในการวางพิกัด

        // สร้าง UI แถบแสดง Turn ตรงกลาง
        turnDisplay = new TurnDisplayPanel();
        add(turnDisplay);

        // สร้าง Player Status Panels
        for (int i = 0; i < 4; i++) {
            playerStatusPanels[i] = new PlayerStatusPanel("Player " + (i + 1), defaultColors[i]);
            add(playerStatusPanels[i]);
        }

        // สำคัญมาก: ต้องสร้างและ Add ตัวผู้เล่นก่อน Tile เพื่อให้ Z-Order อยู่บนสุด (ดัชนี 0)
        for (int i = 0; i < 4; i++) {
            playerMarkers[i] = new JPanel();
            playerMarkers[i].setBackground(defaultColors[i]);
            playerMarkers[i].setBorder(BorderFactory.createLineBorder(Color.WHITE, 2));
            playerMarkers[i].setVisible(false); // ซ่อนไว้ก่อนจนกว่าจะเริ่มเกม
            add(playerMarkers[i]);
            setComponentZOrder(playerMarkers[i], 0); // บังคับให้อยู่บนสุด
        }

        // สร้าง Tiles 32 ช่อง
        for (int i = 0; i < 32; i++) {
            tiles[i] = new CustomShapeButton("T" + i, 12);
            tiles[i].setBackground(new Color(220, 220, 220));
            add(tiles[i]);
        }

        // รับ Event เวลาหน้าต่างถูก Resize จะได้ขยายกระดานตาม
        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                relayoutBoard();
            }
        });
    }

    public void updateBoard(GameState state) {
        if (state == null) return;

        List<Player> players = state.getPlayers();
        Board board = state.getBoard();

        // 1. อัปเดตข้อมูลและตำแหน่งผู้เล่น
        for (int i = 0; i < 4; i++) {
            if (i < players.size()) {
                Player p = players.get(i);
                playerStatusPanels[i].setVisible(true);
                
                // อัปเดตตำแหน่ง
                playerMarkers[i].setVisible(true);
                playerPositions[i] = p.getPosition();
            } else {
                playerStatusPanels[i].setVisible(false);
                playerMarkers[i].setVisible(false);
            }
        }

        // 2. อัปเดตสีของช่องที่ดินเวลามีคนซื้อไปแล้ว
        if (board != null) {
            for (int i = 0; i < 32; i++) {
                Tile t = board.getTile(i);
                tiles[i].setText(t.getName());

                if (t instanceof PropertyTile) {
                    Player owner = ((PropertyTile) t).getOwner();
                    if (owner != null) {
                        int ownerIndex = players.indexOf(owner);
                        if (ownerIndex != -1) {
                            tiles[i].setBackground(defaultColors[ownerIndex]); // เปลี่ยนเป็นสีเจ้าของ
                        }
                    } else {
                        tiles[i].setBackground(new Color(220, 220, 220)); // สีช่องว่างปกติ
                    }
                } else if (t instanceof ActionTile) {
                    tiles[i].setBackground(new Color(255, 200, 100)); // สีช่อง Action
                } else {
                    tiles[i].setBackground(new Color(150, 200, 255)); // สีช่องพิเศษอื่นๆ
                }
            }
        }

        // คำนวณพิกัดใหม่ทุกครั้งที่อัปเดตกระดาน
        relayoutBoard();
        repaint();
    }

    // เมธอดคำนวณพิกัดช่องตารางและหมากผู้เล่นให้อยู่ถูกที่
    private void relayoutBoard() {
        int w = getWidth();
        int h = getHeight();
        if (w == 0 || h == 0) return;

        int margin = 30;
        int boardW = w - (2 * margin);
        int boardH = h - (2 * margin);
        
        // แบ่งกระดานเป็น 9x9 Grid (ด้านละ 9 ช่อง)
        int cellW = boardW / 9;
        int cellH = boardH / 9;

        // วางช่องที่ดินทั้ง 32 ช่อง
        for (int i = 0; i < 32; i++) {
            int gx = getGridX(i);
            int gy = getGridY(i);
            tiles[i].setBounds(margin + (gx * cellW), margin + (gy * cellH), cellW, cellH);
        }

        // วางหมากผู้เล่นให้อยู่ในช่องตาม Position ปัจจุบัน
        for (int i = 0; i < 4; i++) {
            if (playerMarkers[i].isVisible()) {
                int pos = playerPositions[i];
                if (pos >= 0 && pos < 32) {
                    Rectangle tb = tiles[pos].getBounds();
                    
                    // ขนาดตัวผู้เล่น (ประมาณ 1 ใน 3 ของช่อง)
                    int mw = cellW / 3;
                    int mh = cellH / 3;
                    
                    // หากมีผู้เล่นตกช่องเดียวกัน ให้วางเฉียงๆ ไม่บังกัน
                    int ox = (i % 2 == 0) ? 5 : tb.width - mw - 5;
                    int oy = (i < 2) ? 5 : tb.height - mh - 5;
                    
                    playerMarkers[i].setBounds(tb.x + ox, tb.y + oy, mw, mh);
                }
            }
        }

        // จัดวาง Status Panels และ Turn Display ไว้ตรงกลาง
        int centerX = margin + (2 * cellW);
        int centerY = 20;
        int centerW = 5 * cellW;
        int centerH = 5 * cellH;

        if (turnDisplay != null) {
            turnDisplay.setBounds(centerX + 250, centerY, centerW - 550, 50);
        }

        int spW = centerW / 2 - 10;
        int spH = (centerH - 70) / 2 - 10;
        
        if (playerStatusPanels[0] != null) playerStatusPanels[0].setBounds(centerX/4 - 70, centerY + 15, spW, spH);
        if (playerStatusPanels[1] != null) playerStatusPanels[1].setBounds(centerX*2 + centerW/2 + 50, centerY + 15, spW, spH);
        if (playerStatusPanels[2] != null) playerStatusPanels[2].setBounds(centerX/4 - 70, centerY + 550 + spH , spW, spH);
        if (playerStatusPanels[3] != null) playerStatusPanels[3].setBounds(centerX*2 + centerW/2 + 50, centerY + 550 + spH , spW, spH);
    }

    // Helper: แปลง Index (0-31) เป็นพิกัดแกน X บนตาราง 9x9 (เดินทวนเข็มนาฬิกา)
    private int getGridX(int i) {
        if (i >= 0 && i <= 8) return 8 - i;         // แถวล่าง (ขวาไปซ้าย)
        if (i >= 9 && i <= 16) return 0;            // แถวซ้าย (ล่างขึ้นบน)
        if (i >= 17 && i <= 24) return i - 16;      // แถวบน (ซ้ายไปขวา)
        if (i >= 25 && i <= 31) return 8;           // แถวขวา (บนลงล่าง)
        return 0;
    }

    // Helper: แปลง Index (0-31) เป็นพิกัดแกน Y บนตาราง 9x9
    private int getGridY(int i) {
        if (i >= 0 && i <= 8) return 8;
        if (i >= 9 && i <= 16) return 8 - (i - 8);
        if (i >= 17 && i <= 24) return 0;
        if (i >= 25 && i <= 31) return i - 24;
        return 0;
    }
}