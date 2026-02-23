import javax.swing.*;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.geom.Path2D;
import java.awt.geom.Point2D;
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
            new Color(255, 50, 50), // Player 1 (Red)
            new Color(50, 255, 50), // Player 2 (Green)
            new Color(255, 215, 0), // Player 3 (Gold)
            new Color(50, 200, 255) // Player 4 (Blue)
    };

    public BoardPanel() {
        setBackground(new Color(40, 45, 55)); // สีพื้นหลังบอร์ด
        setLayout(null); // ใช้ Absolute Layout

        // สร้าง UI แถบแสดง Turn
        turnDisplay = new TurnDisplayPanel();
        add(turnDisplay);

        // สร้าง Player Status Panels
        for (int i = 0; i < 4; i++) {
            playerStatusPanels[i] = new PlayerStatusPanel("Player " + (i + 1), defaultColors[i]);
            add(playerStatusPanels[i]);
        }

        // สำคัญมาก: ต้องสร้างและ Add ตัวผู้เล่นก่อน Tile เพื่อให้ Z-Order อยู่บนสุด
        for (int i = 0; i < 4; i++) {
            playerMarkers[i] = new JPanel();
            playerMarkers[i].setBackground(defaultColors[i]);
            playerMarkers[i].setBorder(BorderFactory.createLineBorder(Color.WHITE, 2));
            playerMarkers[i].setVisible(false); // ซ่อนไว้ก่อน
            add(playerMarkers[i]);
            setComponentZOrder(playerMarkers[i], 0);
        }

        // สร้าง Tiles 32 ช่อง
        for (int i = 0; i < 32; i++) {
            boolean isCorner = (i % 8 == 0);
            String label;
            int fontSize = 18;

            // ตรวจสอบว่าเป็นมุมไหน และกำหนดข้อความ/ขนาดฟอนต์
            if (isCorner) {
                fontSize = 22;
                switch (i) {
                    case 0:
                        label = "START";
                        break;
                    case 8:
                        label = "JAIL";
                        break;
                    case 16:
                        label = "FESTIVAL";
                        break;
                    case 24:
                        label = "TRAVEL";
                        break;
                    default:
                        label = "" + i; // กันเหนียวไว้
                }
            } else {
                label = "T" + i; // ช่องปกติใช้ T ตามด้วยหมายเลข
            }

            // สร้างปุ่มเพียงครั้งเดียว
            tiles[i] = new CustomShapeButton(label, fontSize);

            // ตั้งค่าสีพื้นหลังปกติ
            tiles[i].setBackground(new Color(220, 220, 220));

            // ถ้าเป็นมุม ให้เปลี่ยนสีพื้นหลังและสีตัวอักษร
            if (isCorner) {
                tiles[i].setBackground(new Color(255, 120, 120)); // สีแดงช่องมุม
                tiles[i].setForeground(Color.WHITE);
            }

            add(tiles[i]);
        }

        // Event เมื่อหน้าต่างถูก Resize
        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                relayoutBoard();
            }
        });
    }

    public void updateBoard(GameState state) {
        if (state == null)
            return;

        List<Player> players = state.getPlayers();
        Board board = state.getBoard();

        // 1. อัปเดตข้อมูลและตำแหน่งผู้เล่น
        for (int i = 0; i < 4; i++) {
            if (i < players.size()) {
                Player p = players.get(i);
                playerStatusPanels[i].setVisible(true);
                playerMarkers[i].setVisible(true);
                playerPositions[i] = p.getPosition();
            } else {
                playerStatusPanels[i].setVisible(false);
                playerMarkers[i].setVisible(false);
            }
        }

        // 2. อัปเดตสีช่องกระดาน
        if (board != null) {
            for (int i = 0; i < 32; i++) {
                Tile t = board.getTile(i);
                tiles[i].setText(t.getName());

                if (t instanceof PropertyTile) {
                    Player owner = ((PropertyTile) t).getOwner();
                    if (owner != null) {
                        int ownerIndex = players.indexOf(owner);
                        if (ownerIndex != -1) {
                            tiles[i].setBackground(defaultColors[ownerIndex]);
                        }
                    } else {
                        tiles[i].setBackground(new Color(220, 220, 220));
                    }
                } else if (t instanceof ActionTile) {
                    tiles[i].setBackground(new Color(255, 200, 100));
                } else {
                    tiles[i].setBackground(new Color(150, 200, 255));
                }
            }
        }

        relayoutBoard();
        repaint();
    }

    // เมธอดสำหรับสร้างรูปทรงข้าวหลามตัด (ดึงมาจาก IsometricBoardGame)
    private Path2D createIsometricPath(int width, int height) {
        Path2D path = new Path2D.Double();
        path.moveTo(width / 2.0, 0);
        path.lineTo(width, height / 2.0);
        path.lineTo(width / 2.0, height);
        path.lineTo(0, height / 2.0);
        path.closePath();
        return path;
    }

    // เมธอดจัด Layout วางพิกัดกระดานและผู้เล่น
    private void relayoutBoard() {
        int panelW = getWidth();
        int panelH = getHeight();
        if (panelW == 0 || panelH == 0)
            return;

        // 1. จัดตำแหน่ง Turn Display ไว้ตรงกลางบน
        if (turnDisplay != null) {
            turnDisplay.setBounds((panelW - 200) / 2, 20, 200, 50);
        }

        // 2. ตั้งค่าขนาดสำหรับคำนวณบอร์ด (ปรับ scale และค่า Y
        // เพื่อให้บอร์ดใหญ่และไม่แบน)
        double scale = 0.72; // ปรับขนาดบอร์ดโดยรวม (1.0 - 1.5)
        double D = 130.0 * scale; // ขนาดช่องมุม
        double W = 100.0 * scale; // ขนาดช่องปกติ (ปรับเพิ่มจาก 75 เป็น 100 เพื่อให้ดูไม่ผอม)
        double G = (2 * D) + (7 * W); // ขนาดรวมในแกน 2D

        // คำนวณจุดเริ่มต้น (กึ่งกลางจอ)
        double boardScreenW = 2 * G;
        double boardScreenH = G;
        double startX = (panelW - boardScreenW) / 2.0 + G;
        double startY = (panelH - boardScreenH) / 2.0 - 20; // +50 เพื่อขยับบอร์ดลงมาจาก UI ด้านบน

        for (int i = 0; i < 32; i++) {
            double gx = 0, gy = 0, gw = 0, gh = 0;

            // กำหนดพิกัด 2D (Logic เดียวกับที่คุณส่งมา)
            if (i == 0) {
                gx = D + 7 * W;
                gy = D + 7 * W;
                gw = D;
                gh = D;
            } else if (i >= 1 && i <= 7) {
                gx = D + (7 - i) * W;
                gy = D + 7 * W;
                gw = W;
                gh = D;
            } else if (i == 8) {
                gx = 0;
                gy = D + 7 * W;
                gw = D;
                gh = D;
            } else if (i >= 9 && i <= 15) {
                gx = 0;
                gy = D + (15 - i) * W;
                gw = D;
                gh = W;
            } else if (i == 16) {
                gx = 0;
                gy = 0;
                gw = D;
                gh = D;
            } else if (i >= 17 && i <= 23) {
                gx = D + (i - 17) * W;
                gy = 0;
                gw = W;
                gh = D;
            } else if (i == 24) {
                gx = D + 7 * W;
                gy = 0;
                gw = D;
                gh = D;
            } else if (i >= 25 && i <= 31) {
                gx = D + 7 * W;
                gy = D + (i - 25) * W;
                gw = D;
                gh = W;
            }

            // แปลงเป็นพิกัด Isometric (ใช้ค่า 0.6 เพื่อให้อวบขึ้น ไม่แบนแบบ 0.5)
            Point2D.Double p1 = iso(startX, startY, gx, gy);
            Point2D.Double p2 = iso(startX, startY, gx + gw, gy);
            Point2D.Double p3 = iso(startX, startY, gx + gw, gy + gh);
            Point2D.Double p4 = iso(startX, startY, gx, gy + gh);

            // คำนวณ Bounding Box
            int minX = (int) Math.min(Math.min(p1.x, p2.x), Math.min(p3.x, p4.x));
            int minY = (int) Math.min(Math.min(p1.y, p2.y), Math.min(p3.y, p4.y));
            int maxX = (int) Math.max(Math.max(p1.x, p2.x), Math.max(p3.x, p4.x));
            int maxY = (int) Math.max(Math.max(p1.y, p2.y), Math.max(p3.y, p4.y));

            tiles[i].setBounds(minX, minY, maxX - minX, maxY - minY);

            // สร้าง Polygon สำหรับปุ่ม
            Polygon poly = new Polygon();
            poly.addPoint((int) (p1.x - minX), (int) (p1.y - minY));
            poly.addPoint((int) (p2.x - minX), (int) (p2.y - minY));
            poly.addPoint((int) (p3.x - minX), (int) (p3.y - minY));
            poly.addPoint((int) (p4.x - minX), (int) (p4.y - minY));
            tiles[i].setShape(poly);
        }

        // 3. จัดตำแหน่งหมากผู้เล่นให้สัมพันธ์กับช่องใหม่
        for (int i = 0; i < 4; i++) {
            if (playerMarkers[i] != null && playerMarkers[i].isVisible()) {
                int pos = playerPositions[i];
                Rectangle tb = tiles[pos].getBounds();
                int mw = 25, mh = 25;
                int ox = (i % 2 == 0) ? -20 : 10;
                int oy = (i < 2) ? -20 : 10;
                playerMarkers[i].setBounds(tb.x + tb.width / 2 - mw / 2 + ox, tb.y + tb.height / 2 - mh / 2 + oy, mw,
                        mh);
            }
        }

        // 4. จัดวาง Player Status Panels ไว้ตามมุมจอ
        int spW = 220, spH = 130, margin = 30;
        if (playerStatusPanels[0] != null)
            playerStatusPanels[0].setBounds(margin, margin, spW, spH);
        if (playerStatusPanels[1] != null)
            playerStatusPanels[1].setBounds(panelW - spW - margin, margin, spW, spH);
        if (playerStatusPanels[2] != null)
            playerStatusPanels[2].setBounds(margin, panelH - spH - margin, spW, spH);
        if (playerStatusPanels[3] != null)
            playerStatusPanels[3].setBounds(panelW - spW - margin, panelH - spH - margin, spW, spH);
    }

    // เพิ่มฟังก์ชัน iso ไว้ท้ายไฟล์ BoardPanel.java (ถ้ายังไม่มี)
    private Point2D.Double iso(double startX, double startY, double x, double y) {
        return new Point2D.Double(startX + (x - y), startY + (x + y) * 0.6); // ใช้ 0.6 เพื่อความ "อวบ"
    }
}