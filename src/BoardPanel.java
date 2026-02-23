import javax.swing.*;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.geom.Path2D;
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
                fontSize = 14;
                switch (i) {
                    case 0:
                        label = "FESTIVAL";
                        break;
                    case 8:
                        label = "JAIL";
                        break;
                    case 16:
                        label = "START";
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
        int w = getWidth();
        int h = getHeight();
        if (w == 0 || h == 0)
            return;

        // ---------------------------------------------------------
        // 1. คำนวณพิกัดกระดาน Isometric
        // ---------------------------------------------------------
        int tileWidth = 150; // ขนาดกว้างของช่อง
        int tileHeight = 75; // ขนาดสูงของช่อง (ครึ่งนึงของกว้าง)
        int startX = w / 2 - tileWidth / 2;
        int startY = 250; // จุดเริ่มต้นแกน Y (เลื่อนลงมานิดหน่อยไม่ให้ชนขอบบน)

        for (int i = 0; i < 32; i++) {
            int row = 0, col = 0;

            // แบ่งตำแหน่งกระดาน (8 ช่อง x 4 ด้าน = 32)
            if (i < 9) {
                row = i;
                col = 0;
            } else if (i < 17) {
                row = 8;
                col = i - 8;
            } else if (i < 25) {
                row = 24 - i;
                col = 8;
            } else {
                row = 0;
                col = 32 - i;
            }

            // สมการแปลงแกน 2D เป็น Isometric
            int x = startX + (col - row) * (tileWidth / 2);
            int y = startY + (col + row) * (tileHeight / 2);

            tiles[i].setShape(createIsometricPath(tileWidth, tileHeight));
            tiles[i].setBounds(x, y, tileWidth, tileHeight);
        }

        // ---------------------------------------------------------
        // 2. จัดวางหมากผู้เล่น (ให้อยู่กลางช่องข้าวหลามตัด)
        // ---------------------------------------------------------
        for (int i = 0; i < 4; i++) {
            if (playerMarkers[i].isVisible()) {
                int pos = playerPositions[i];
                if (pos >= 0 && pos < 32) {
                    Rectangle tb = tiles[pos].getBounds();
                    int mw = 20; // ขนาดหมากกว้าง
                    int mh = 20; // ขนาดหมากสูง

                    // กระจายตัวผู้เล่นไม่ให้ทับกันตรงๆ (อิงตาม index ผู้เล่น)
                    int ox = (i % 2 == 0) ? -15 : 5;
                    int oy = (i < 2) ? -15 : 5;

                    int px = tb.x + (tb.width / 2) - (mw / 2) + ox;
                    int py = tb.y + (tb.height / 2) - (mh / 2) + oy;

                    playerMarkers[i].setBounds(px, py, mw, mh);
                }
            }
        }

        // ---------------------------------------------------------
        // 3. จัดวาง UI (Turn Display & Status Panel)
        // ---------------------------------------------------------
        if (turnDisplay != null) {
            turnDisplay.setBounds((w - 200) / 2, 20, 200, 50); // วางไว้ตรงกลางขอบบนสุด
        }

        // วางกล่องผู้เล่นไว้ตามมุม 4 มุม หลบกระดานตรงกลาง
        int spW = 220, spH = 130;
        int margin = 30;

        if (playerStatusPanels[0] != null)
            playerStatusPanels[0].setBounds(margin, margin, spW, spH); // ซ้ายบน
        if (playerStatusPanels[1] != null)
            playerStatusPanels[1].setBounds(w - spW - margin, margin, spW, spH); // ขวาบน
        if (playerStatusPanels[2] != null)
            playerStatusPanels[2].setBounds(margin, h - spH - margin, spW, spH); // ซ้ายล่าง
        if (playerStatusPanels[3] != null)
            playerStatusPanels[3].setBounds(w - spW - margin, h - spH - margin, spW, spH); // ขวาล่าง
    }
}