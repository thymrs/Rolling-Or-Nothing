import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.Point2D;

public class BoardPanel extends JPanel {

    private CustomShapeButton[] tilesUI = new CustomShapeButton[32];
    private JPanel[] playerMarkers = new JPanel[2]; // สมมติว่ามี 2 ผู้เล่น
    private int[] currentVisualPositions = { 0, 0 };

    // เก็บอ้างอิงถึง Board ของ Backend
    private Board gameBoard;

    public BoardPanel(Board gameBoard) {
        this.gameBoard = gameBoard;
        setLayout(null);
        setBackground(new Color(40, 45, 55));

        initPlayerMarkers();
        initTiles();

        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                recalculateIsometricLayout();
            }
        });
    }

    private void initPlayerMarkers() {
        Color[] playerColors = { new Color(0, 255, 255), new Color(255, 50, 50) };
        for (int i = 0; i < playerMarkers.length; i++) {
            final Color color = playerColors[i];
            playerMarkers[i] = new JPanel() {
                @Override
                protected void paintComponent(Graphics g) {
                    super.paintComponent(g);
                    Graphics2D g2 = (Graphics2D) g;
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    g2.setColor(color);
                    g2.fillOval(0, 0, getWidth(), getHeight());
                    g2.setColor(Color.WHITE);
                    g2.setStroke(new BasicStroke(2f));
                    g2.drawOval(1, 1, getWidth() - 3, getHeight() - 3);
                }
            };
            playerMarkers[i].setOpaque(false);
            playerMarkers[i].setSize(40, 40);
            add(playerMarkers[i]);
        }
    }

    private void initTiles() {
        for (int i = 0; i < 32; i++) {
            boolean isCorner = (i % 8 == 0);
            int fontSize = isCorner ? 26 : 18;

            // ดึงชื่อช่องจาก Backend
            Tile backendTile = gameBoard.getTile(i);
            String tileName = (backendTile != null) ? backendTile.getName() : String.valueOf(i);

            tilesUI[i] = new CustomShapeButton(" " + tileName + " ", fontSize);
            tilesUI[i].setBackground(isCorner ? new Color(255, 120, 120) : new Color(240, 240, 245));
            tilesUI[i].setForeground(isCorner ? Color.WHITE : Color.BLACK);

            final int tileIndex = i;
            tilesUI[i].addActionListener(e -> {
                // เรียกข้อมูลจาก Backend มาแสดงเมื่อคลิกที่ช่อง
                Tile clickedTile = gameBoard.getTile(tileIndex);
                String info = "Index: " + clickedTile.getIndex() + "\nName: " + clickedTile.getName();

                if (clickedTile instanceof PropertyTile) {
                    PropertyTile prop = (PropertyTile) clickedTile;
                    info += "\nPrice: " + prop.getPurchasePrice();
                    info += "\nRent: " + prop.getBaseRent();
                }

                JOptionPane.showMessageDialog(this, info, "Tile Info", JOptionPane.INFORMATION_MESSAGE);
            });

            add(tilesUI[i]);
            if (isCorner) {
                setComponentZOrder(tilesUI[i], 0);
            }
        }
        for (JPanel marker : playerMarkers) {
            setComponentZOrder(marker, 0);
        }
    }

    public void updatePlayerUI(int playerId, int tileIndex) {
        if (playerId >= 0 && playerId < playerMarkers.length) {
            currentVisualPositions[playerId] = tileIndex;
            repositionPlayerMarker(playerId);
        }
    }

    private void recalculateIsometricLayout() {
        int panelW = getWidth();
        int panelH = getHeight();

        double scale = 1.0;
        double D = 130.0 * scale;
        double W = 75.0 * scale;
        double G = (2 * D) + (7 * W);

        double boardScreenW = 2 * G;
        double boardScreenH = G;
        double startX = (panelW - boardScreenW) / 2.0 + G;
        double startY = (panelH - boardScreenH) / 2.0;

        for (int i = 0; i < 32; i++) {
            double gx = 0, gy = 0, gw = 0, gh = 0;

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

            Point2D.Double p1 = iso(startX, startY, gx, gy);
            Point2D.Double p2 = iso(startX, startY, gx + gw, gy);
            Point2D.Double p3 = iso(startX, startY, gx + gw, gy + gh);
            Point2D.Double p4 = iso(startX, startY, gx, gy + gh);

            int minX = (int) Math.min(Math.min(p1.x, p2.x), Math.min(p3.x, p4.x));
            int minY = (int) Math.min(Math.min(p1.y, p2.y), Math.min(p3.y, p4.y));
            int maxX = (int) Math.max(Math.max(p1.x, p2.x), Math.max(p3.x, p4.x));
            int maxY = (int) Math.max(Math.max(p1.y, p2.y), Math.max(p3.y, p4.y));

            tilesUI[i].setBounds(minX, minY, maxX - minX, maxY - minY);

            Polygon poly = new Polygon();
            poly.addPoint((int) (p1.x - minX), (int) (p1.y - minY));
            poly.addPoint((int) (p2.x - minX), (int) (p2.y - minY));
            poly.addPoint((int) (p3.x - minX), (int) (p3.y - minY));
            poly.addPoint((int) (p4.x - minX), (int) (p4.y - minY));

            tilesUI[i].setShape(poly);
        }

        for (int p = 0; p < playerMarkers.length; p++) {
            repositionPlayerMarker(p);
        }
    }

    private void repositionPlayerMarker(int playerIndex) {
        int pos = currentVisualPositions[playerIndex];
        if (tilesUI[pos] != null && tilesUI[pos].getWidth() > 0) {
            int cx = tilesUI[pos].getX() + (tilesUI[pos].getWidth() / 2);
            int cy = tilesUI[pos].getY() + (tilesUI[pos].getHeight() / 2);

            // ขยับจุดให้ผู้เล่น 2 คนไม่ทับกันสนิท
            int offsetX = (playerIndex == 0) ? -10 : 10;

            int markerX = cx - (playerMarkers[playerIndex].getWidth() / 2) + offsetX;
            int markerY = cy - (playerMarkers[playerIndex].getHeight() / 2);

            playerMarkers[playerIndex].setLocation(markerX, markerY);
        }
    }

    private Point2D.Double iso(double startX, double startY, double x, double y) {
        double isoX = startX + (x - y);
        double isoY = startY + (x + y) * 0.5;
        return new Point2D.Double(isoX, isoY);
    }
}