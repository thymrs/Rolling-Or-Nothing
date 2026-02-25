import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.geom.Path2D;
import java.awt.geom.Point2D;
import java.util.List;
import javax.swing.*;
import java.util.ArrayList;


public class BoardPanel extends JPanel {
    // ปุ่ม ROLL ใหญ่ๆ ตรงกลาง (แยกเป็น CircleButton เพื่อความสวยงาม)
    private CircleButton btnRoll;

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

        // สร้างปุ่ม ROLL ใหญ่ๆ ตรงกลาง
        btnRoll = new CircleButton("ROLL");
        btnRoll.setActionCommand("ROLL");
        add(btnRoll);
        setComponentZOrder(btnRoll, 0);

        // ตั้งค่าให้กดปุ่ม ROLL ด้วย Spacebar ได้ (สำหรับผู้เล่นที่ใช้คีย์บอร์ด)
        setupSpacebarRoll();

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
            tiles[i].setActionCommand("TILE_" + i);
            add(tiles[i]);

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

    //สร้าง highlight effect ให้กับช่องที่ถูกเลือกโดยการกดปุ่มจาก tile โดยตรงนี้จะถูกเรียกจาก GameController เมื่อมีการเลือกช่อง
    public void highlightTile(int tileIndex) {
        if (tileIndex >= 0 && tileIndex < 32) {
            tiles[tileIndex].setBorder(BorderFactory.createLineBorder(Color.YELLOW, 4));
        }
    }

    // ฟังก์ชันสำหรับตั้งค่าให้กด Spacebar เพื่อคลิกปุ่ม ROLL ได้
    private void setupSpacebarRoll() {
        InputMap im = getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
        ActionMap am = getActionMap();

        // ใช้คำสั่ง "released" เพื่อให้ทำงานครั้งเดียวตอนปล่อยปุ่ม ลดปัญหาการกดค้าง
        im.put(KeyStroke.getKeyStroke("released SPACE"), "rollAction");

        am.put("rollAction", new AbstractAction() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                // เช็คทั้ง IsEnabled และ IsVisible เพื่อความชัวร์
                if (btnRoll != null && btnRoll.isEnabled() && btnRoll.isVisible()) {
                    btnRoll.doClick(); // สั่งคลิกแค่ปุ่ม Roll เท่านั้น
                }
            }
        });
    }

    public void setRollActionListener(java.awt.event.ActionListener listener) {
        btnRoll.addActionListener(listener);
    }

    public void setRollEnabled(boolean enabled) {
        btnRoll.setEnabled(enabled);
    }

    // ฟังก์ชันสำหรับทำ Animation การเดินของผู้เล่น (เรียกจาก Controller เมื่อผู้เล่นเดิน)
    public void animatePlayerMovement(int playerId, List<Integer> path, Runnable onComplete) {
        if (path == null || path.isEmpty()) {
            if (onComplete != null) onComplete.run();
            return;
        }

        final int[] step = {0};
        final int[] delay = {500}; // ความเร็วเริ่มต้น 0.5 วินาที (500ms)
        final int minDelay = 200;  // ความเร็วสูงสุดที่เข้าใกล้ 0.2 วินาที (200ms)

        // สร้าง Timer สำหรับทำ Animation โดยไม่ทำให้หน้าจอค้าง
        Timer timer = new Timer(delay[0], null);
        timer.addActionListener(e -> {
            
            // 1. เปลี่ยนตำแหน่งใน Array (อ้างอิงจากตัวแปร playerPositions ในโค้ดของคุณ)
            int nextTileIndex = path.get(step[0]);
            playerPositions[playerId] = nextTileIndex; 

            // 2. สั่งให้วาดกระดานใหม่ (มันจะไปเรียกโค้ดจัด setBounds ที่คุณเขียนไว้เอง)
            revalidate();
            repaint();

            // 3. เร่งความเร็วการกระโดดในครั้งต่อไป
            if (delay[0] > minDelay) {
                delay[0] -= 50; // ลดลงทีละ 50ms (จะเร่งความเร็วขึ้น)
                timer.setDelay(delay[0]);
            }

            step[0]++;

            // 4. เช็คว่าเดินครบตามเส้นทางหรือยัง
            if (step[0] >= path.size()) {
                timer.stop();
                if (onComplete != null) {
                    onComplete.run(); // แจ้ง Controller ว่าเดินเสร็จแล้ว!
                }
            }
        });
        
        timer.start(); // เริ่มกระโดด
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

                // --- ส่วนที่ต้องเพิ่ม: ส่งข้อมูลจริงจาก Player เข้าสู่ UI ---
                String posName = board.getTile(p.getPosition()).getName();

                // คำนวณมูลค่าทรัพย์สินรวม (เงินสด + ราคาที่ดินที่ครอบครอง)
                

                // ตัดสินข้อความ Status
                String status = "Normal";
                if (p.isBankrupt())
                    status = "Bankrupt";
                else if (p.getIsJailed())
                    status = "In Jail (" + p.getJailTurnCount() + ")";
                else if (p.isFrozen())
                    status = "Frozen";

                // เรียก updateData เพื่อเปลี่ยนข้อความบนจอ
                playerStatusPanels[i].updateData(p, posName, status);
                        // p.getMoney(),
                        // totalAssets,
                        // status,
                        // p.getIsJailed(),
                        // p.isBankrupt(),
                        // p.getHasShield(),
                        // p.getIsTollFree(),
                        // posName);
                // --------------------------------------------------

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

        // 1. จัดตำแหน่ง Turn Display (ปรับให้ยืดหยุ่นตามความกว้าง)
        if (turnDisplay != null) {
            int tdW = Math.min(300, (int) (panelW * 0.3));
            turnDisplay.setBounds((panelW - tdW) / 2, 20, tdW, 50);
        }

        // --- 2. คำนวณ Dynamic Scale เพื่อให้ "ชิด" ขอบพื้นที่ที่สุด ---

        // ขนาดตรรกะพื้นฐานของช่อง (D = มุม, W = ปกติ)
        double baseD = 130.0;
        double baseW = 100.0;
        double baseG = (2 * baseD) + (7 * baseW); // ความกว้างในแกน 2D = 960.0

        // ความกว้าง/สูงจริงของบอร์ดในโหมด Isometric (0.6 ratio)
        // Width = 2 * G, Height = 1.2 * G
        double boardBaseWidth = 2 * baseG;
        double boardBaseHeight = 1.2 * baseG;

        // เผื่อ Margin 10% เพื่อไม่ให้ทับกับแผง Status ที่มุมจอ
        double margin = 0.9;
        double scaleW = (panelW * margin) / boardBaseWidth;
        double scaleH = (panelH * margin) / boardBaseHeight;

        // เลือก Scale ที่ดีที่สุด (ทำให้บอร์ดใหญ่ที่สุดเท่าที่จะทำได้)
        double dynamicScale = Math.min(scaleW, scaleH);

        // ปรับค่าจริงตาม Scale
        double D = baseD * dynamicScale;
        double W = baseW * dynamicScale;
        double G = (2 * D) + (7 * W);

        // คำนวณจุดกึ่งกลางที่แม่นยำ (เพื่อให้มุมบน-ล่าง-ซ้าย-ขวา อยู่กึ่งกลางพื้นที่)
        double startX = panelW / 2.0;
        double startY = (panelH - (1.2 * G)) / 2.0;

        // จัดตำแหน่งปุ่ม ROLL ใหญ่ๆ ตรงกลางแต่ค่อนมาด้านล่าง (responsive กับ dynamicScale)
        int rollSize = (int) (240 * dynamicScale); // ขนาดวงกลมปรับตามขนาดจอ
        // ตำแหน่ง Y: อยู่ต่ำกว่ากึ่งกลางจอเล็กน้อย โดยสัมพันธ์กับขนาดบอร์ด (เช่น 30% จากขอบบนถึงกึ่งกลางบอร์ด)
        int centerY = (int) (panelH / 2 + (G * 0.25));
        btnRoll.setBounds((panelW - rollSize) / 2, centerY - rollSize / 2, rollSize, rollSize);

        // --- 3. วางตำแหน่ง Tiles และปรับขนาด Font ให้ Responsive ---
        for (int i = 0; i < 32; i++) {
            double gx = 0, gy = 0, gw = 0, gh = 0;
            boolean isCorner = (i % 8 == 0);

            // Logic กำหนดตำแหน่ง gx, gy (พิกัด 0 ถึง G)
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

            // แปลงเป็นพิกัด Isometric
            Point2D.Double p1 = iso(startX, startY, gx, gy);
            Point2D.Double p2 = iso(startX, startY, gx + gw, gy);
            Point2D.Double p3 = iso(startX, startY, gx + gw, gy + gh);
            Point2D.Double p4 = iso(startX, startY, gx, gy + gh);

            int minX = (int) Math.min(Math.min(p1.x, p2.x), Math.min(p3.x, p4.x));
            int minY = (int) Math.min(Math.min(p1.y, p2.y), Math.min(p3.y, p4.y));
            int maxX = (int) Math.max(Math.max(p1.x, p2.x), Math.max(p3.x, p4.x));
            int maxY = (int) Math.max(Math.max(p1.y, p2.y), Math.max(p3.y, p4.y));

            tiles[i].setBounds(minX, minY, maxX - minX, maxY - minY);

            // ปรับ Font ให้ขยายตามขนาดช่อง
            float fSize = (float) ((isCorner ? 20 : 16) * dynamicScale);
            tiles[i].setFont(tiles[i].getFont().deriveFont(Math.max(9f, fSize)));

            Polygon poly = new Polygon();
            poly.addPoint((int) (p1.x - minX), (int) (p1.y - minY));
            poly.addPoint((int) (p2.x - minX), (int) (p2.y - minY));
            poly.addPoint((int) (p3.x - minX), (int) (p3.y - minY));
            poly.addPoint((int) (p4.x - minX), (int) (p4.y - minY));
            tiles[i].setShape(poly);
        }

        // --- 4. จัดตำแหน่งหมากผู้เล่น (Scale ตามช่อง) ---
        int mw = (int) (32 * dynamicScale);
        int mh = mw;
        for (int i = 0; i < 4; i++) {
            if (playerMarkers[i] != null && playerMarkers[i].isVisible()) {
                int pos = playerPositions[i];
                Rectangle tb = tiles[pos].getBounds();
                // Offset ให้หมากกระจายตัวในช่อง
                int ox = (int) (((i % 2 == 0) ? -15 : 10) * dynamicScale);
                int oy = (int) (((i < 2) ? -15 : 10) * dynamicScale);
                playerMarkers[i].setBounds(tb.x + tb.width / 2 - mw / 2 + ox,
                        tb.y + tb.height / 2 - mh / 2 + oy, mw, mh);
            }
        }

        // --- 5. จัด Player Status Panels ให้อยู่มุมจอเสมอ ---
        int spW = (int) (panelW * 0.2); // กว้าง 20% ของจอ
        int spH = (int) (panelH * 0.16); // สูง 16% ของจอ
        int pad = 25;
        if (playerStatusPanels[0] != null)
            playerStatusPanels[0].setBounds(pad, pad, spW, spH);
        if (playerStatusPanels[1] != null)
            playerStatusPanels[1].setBounds(panelW - spW - pad, pad, spW, spH);
        if (playerStatusPanels[2] != null)
            playerStatusPanels[2].setBounds(pad, panelH - spH - pad, spW, spH);
        if (playerStatusPanels[3] != null)
            playerStatusPanels[3].setBounds(panelW - spW - pad, panelH - spH - pad, spW, spH);
    }

    // เพิ่มฟังก์ชัน iso ไว้ท้ายไฟล์ BoardPanel.java (ถ้ายังไม่มี)
    private Point2D.Double iso(double startX, double startY, double x, double y) {
        return new Point2D.Double(startX + (x - y), startY + (x + y) * 0.6); // ใช้ 0.6 เพื่อความ "อวบ"
    }

    public void setTileActionListener(java.awt.event.ActionListener listener) {
        for (int i = 0; i < 32; i++) {
            tiles[i].addActionListener(listener);
        }
    }
}