import javax.swing.*;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.util.ArrayList;
import java.util.List;

public class BoardPanel extends JPanel {

    private static final int TILES_PER_SIDE = 6; 
    private static final int CORNER_SIZE = 110; 
    private static final int TILE_WIDTH = 75;   
    private static final int TILE_HEIGHT = 110; 
    
    private List<GameTile> tiles;
    private List<Rectangle> tileBounds;
    private List<Player> players;

    public BoardPanel() {
        setBackground(GameTheme.BG_COLOR);
        setLayout(null);
        initTiles();
        calculateTileBounds();
    }
    
    public void setPlayers(List<Player> players) {
        this.players = players;
        repaint(); 
    }

    private void initTiles() {
        tiles = new ArrayList<>();
        // Bottom
        tiles.add(new GameTile("START", "Go", GameTile.Type.START, Color.WHITE)); 
        tiles.add(new GameTile("Bangkok", "$100", GameTile.Type.PROPERTY, new Color(255, 102, 102)));
        tiles.add(new GameTile("Chance", "?", GameTile.Type.CHANCE, Color.ORANGE));
        tiles.add(new GameTile("Phuket", "$120", GameTile.Type.PROPERTY, new Color(255, 102, 102)));
        tiles.add(new GameTile("Chiang Mai", "$140", GameTile.Type.PROPERTY, new Color(255, 102, 102)));
        tiles.add(new GameTile("Tax", "10%", GameTile.Type.SPECIAL, Color.LIGHT_GRAY));
        tiles.add(new GameTile("Pattaya", "$160", GameTile.Type.PROPERTY, new Color(255, 102, 102)));
        // Left
        tiles.add(new GameTile("JAIL", "Visit", GameTile.Type.CORNER, Color.GRAY));
        tiles.add(new GameTile("Hanoi", "$180", GameTile.Type.PROPERTY, new Color(255, 204, 51)));
        tiles.add(new GameTile("Electr.", "$150", GameTile.Type.SPECIAL, Color.WHITE));
        tiles.add(new GameTile("Jakarta", "$200", GameTile.Type.PROPERTY, new Color(255, 204, 51)));
        tiles.add(new GameTile("Chance", "?", GameTile.Type.CHANCE, Color.ORANGE));
        tiles.add(new GameTile("Manila", "$220", GameTile.Type.PROPERTY, new Color(255, 204, 51)));
        tiles.add(new GameTile("KL", "$240", GameTile.Type.PROPERTY, new Color(255, 204, 51)));
        // Top
        tiles.add(new GameTile("OLYMPIC", "Event", GameTile.Type.CORNER, Color.CYAN));
        tiles.add(new GameTile("Beijing", "$260", GameTile.Type.PROPERTY, new Color(51, 204, 51)));
        tiles.add(new GameTile("Chance", "?", GameTile.Type.CHANCE, Color.ORANGE));
        tiles.add(new GameTile("Shanghai", "$280", GameTile.Type.PROPERTY, new Color(51, 204, 51)));
        tiles.add(new GameTile("HK", "$300", GameTile.Type.PROPERTY, new Color(51, 204, 51)));
        tiles.add(new GameTile("Train", "$200", GameTile.Type.SPECIAL, Color.WHITE));
        tiles.add(new GameTile("Seoul", "$320", GameTile.Type.PROPERTY, new Color(51, 204, 51)));
        // Right
        tiles.add(new GameTile("TRAVEL", "Fly", GameTile.Type.CORNER, Color.MAGENTA));
        tiles.add(new GameTile("Tokyo", "$350", GameTile.Type.PROPERTY, new Color(51, 153, 255)));
        tiles.add(new GameTile("Chance", "?", GameTile.Type.CHANCE, Color.ORANGE));
        tiles.add(new GameTile("Osaka", "$400", GameTile.Type.PROPERTY, new Color(51, 153, 255)));
        tiles.add(new GameTile("Tax", "$200", GameTile.Type.SPECIAL, Color.LIGHT_GRAY));
        tiles.add(new GameTile("Sydney", "$450", GameTile.Type.PROPERTY, new Color(51, 153, 255)));
        tiles.add(new GameTile("Melb.", "$500", GameTile.Type.PROPERTY, new Color(51, 153, 255)));
    }

    private void calculateTileBounds() {
        tileBounds = new ArrayList<>();
        int panelW = 1250; int panelH = 850;
        int boardW = (CORNER_SIZE * 2) + (TILE_WIDTH * TILES_PER_SIDE);
        int boardH = (CORNER_SIZE * 2) + (TILE_WIDTH * TILES_PER_SIDE);
        int startX = (panelW - boardW) / 2; int startY = (panelH - boardH) / 2;
        if (startX < 10) startX = 10; if (startY < 10) startY = 10;

        // Bottom (Right->Left)
        tileBounds.add(new Rectangle(startX + boardW - CORNER_SIZE, startY + boardH - CORNER_SIZE, CORNER_SIZE, CORNER_SIZE));
        for (int i = 0; i < TILES_PER_SIDE; i++) tileBounds.add(new Rectangle(startX + boardW - CORNER_SIZE - ((i + 1) * TILE_WIDTH), startY + boardH - TILE_HEIGHT, TILE_WIDTH, TILE_HEIGHT));
        // Left (Bottom->Top)
        tileBounds.add(new Rectangle(startX, startY + boardH - CORNER_SIZE, CORNER_SIZE, CORNER_SIZE));
        for (int i = 0; i < TILES_PER_SIDE; i++) tileBounds.add(new Rectangle(startX, startY + boardH - CORNER_SIZE - ((i + 1) * TILE_WIDTH), TILE_HEIGHT, TILE_WIDTH));
        // Top (Left->Right)
        tileBounds.add(new Rectangle(startX, startY, CORNER_SIZE, CORNER_SIZE));
        for (int i = 0; i < TILES_PER_SIDE; i++) tileBounds.add(new Rectangle(startX + CORNER_SIZE + (i * TILE_WIDTH), startY, TILE_WIDTH, TILE_HEIGHT));
        // Right (Top->Bottom)
        tileBounds.add(new Rectangle(startX + boardW - CORNER_SIZE, startY, CORNER_SIZE, CORNER_SIZE));
        for (int i = 0; i < TILES_PER_SIDE; i++) tileBounds.add(new Rectangle(startX + boardW - TILE_HEIGHT, startY + CORNER_SIZE + (i * TILE_WIDTH), TILE_HEIGHT, TILE_WIDTH));
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (tileBounds == null || tileBounds.isEmpty()) calculateTileBounds();

        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // วาด Tile
        for (int i = 0; i < tiles.size(); i++) {
            if (i >= tileBounds.size()) break;
            drawSingleTile(g2, tiles.get(i), tileBounds.get(i), i);
        }
        drawCenterBoard(g2);
        
        // วาดผู้เล่น (Player Tokens)
        if (players != null) {
            drawPlayers(g2);
        }
    }

    private void drawPlayers(Graphics2D g2) {
        for (int i = 0; i < players.size(); i++) {
            Player p = players.get(i);
            Point center = getTileCenter(p.getPosition());
            
            int offset = (i * 10) - 15; 
            int size = 20;
            
            g2.setColor(p.getColor());
            g2.fillOval(center.x + offset, center.y + offset, size, size);
            g2.setColor(Color.BLACK);
            g2.setStroke(new BasicStroke(2));
            g2.drawOval(center.x + offset, center.y + offset, size, size);
        }
    }

    private void drawSingleTile(Graphics2D g2, GameTile tile, Rectangle r, int index) {
        g2.setColor(GameTheme.WOOD_COLOR);
        g2.fillRect(r.x, r.y, r.width, r.height);
        g2.setColor(GameTheme.WOOD_BORDER);
        g2.setStroke(new BasicStroke(2));
        g2.drawRect(r.x, r.y, r.width, r.height);

        if (tile.type == GameTile.Type.PROPERTY) {
            int strip = 25;
            g2.setColor(tile.colorStrip);
            if (index > 0 && index < 7) g2.fillRect(r.x+1, r.y, r.width-2, strip);
            else if (index > 7 && index < 14) g2.fillRect(r.x+r.width-strip, r.y+1, strip, r.height-2);
            else if (index > 14 && index < 21) g2.fillRect(r.x+1, r.y+r.height-strip, r.width-2, strip);
            else if (index > 21) g2.fillRect(r.x, r.y+1, strip, r.height-2);
        }

        g2.setColor(Color.BLACK);
        AffineTransform orig = g2.getTransform();
        String fontName = (GameTheme.THAI_FONT != null) ? GameTheme.THAI_FONT : "SansSerif";
        Font nf = new Font(fontName, Font.BOLD, 12);
        Font pf = new Font(fontName, Font.PLAIN, 10);
        
        if (tile.type == GameTile.Type.CORNER || tile.type == GameTile.Type.START) {
            g2.setFont(new Font(fontName, Font.BOLD, 14));
            drawCenteredString(g2, tile.name, r, -10);
        } else {
            if (index > 0 && index < 7) { g2.setFont(nf); drawCenteredString(g2, tile.name, r, 10); g2.setFont(pf); drawCenteredString(g2, tile.price, r, 35); }
            else if (index > 7 && index < 14) { g2.rotate(Math.toRadians(90), r.getCenterX(), r.getCenterY()); g2.setFont(nf); drawCenteredString(g2, tile.name, r, 10); g2.setFont(pf); drawCenteredString(g2, tile.price, r, 35); }
            else if (index > 14 && index < 21) { g2.rotate(Math.toRadians(180), r.getCenterX(), r.getCenterY()); g2.setFont(nf); drawCenteredString(g2, tile.name, r, 10); g2.setFont(pf); drawCenteredString(g2, tile.price, r, 35); }
            else if (index > 21) { g2.rotate(Math.toRadians(-90), r.getCenterX(), r.getCenterY()); g2.setFont(nf); drawCenteredString(g2, tile.name, r, 10); g2.setFont(pf); drawCenteredString(g2, tile.price, r, 35); }
        }
        g2.setTransform(orig);
    }

    private void drawCenterBoard(Graphics2D g2) {
        if (tileBounds.size() < 20) return;
        int topY = tileBounds.get(14).y + tileBounds.get(14).height;
        int botY = tileBounds.get(0).y;
        int leftX = tileBounds.get(7).x + tileBounds.get(7).width;
        int rightX = tileBounds.get(0).x;
        int w = rightX - leftX, h = botY - topY;
        
        g2.setColor(new Color(0, 0, 0, 20));
        String fontName = (GameTheme.THAI_FONT != null) ? GameTheme.THAI_FONT : "SansSerif";
        g2.setFont(new Font(fontName, Font.BOLD, 60));
        String logo = "RollingOrNothing";
        FontMetrics fm = g2.getFontMetrics();
        g2.drawString(logo, leftX + (w - fm.stringWidth(logo))/2, topY + h/2);
    }

    private void drawCenteredString(Graphics2D g, String text, Rectangle rect, int yOffset) {
        FontMetrics m = g.getFontMetrics(g.getFont());
        int x = rect.x + (rect.width - m.stringWidth(text)) / 2;
        int y = rect.y + ((rect.height - m.getHeight()) / 2) + m.getAscent() + yOffset;
        g.drawString(text, x, y);
    }

    public Point getTileCenter(int index) {
        if (tileBounds == null || index < 0 || index >= tileBounds.size()) return new Point(0,0);
        Rectangle r = tileBounds.get(index);
        return new Point(r.x + r.width/2, r.y + r.height/2);
    }

    private static class GameTile {
        enum Type { START, PROPERTY, CHANCE, CORNER, SPECIAL }
        String name, price; Type type; Color colorStrip;
        public GameTile(String n, String p, Type t, Color c) { name=n; price=p; type=t; colorStrip=c; }
    }
}