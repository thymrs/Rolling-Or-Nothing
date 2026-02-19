import javax.swing.*;
import java.awt.*;

public class BoardPanel extends JPanel {
    public BoardPanel() {
        setBackground(GameTheme.BG_COLOR);
        setLayout(new GridBagLayout());
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int w = getWidth(), h = getHeight();
        g2.setColor(new Color(0, 0, 0, 50));
        g2.fillRect(50, 50, w - 100, h - 100);
        g2.setColor(new Color(255, 255, 255, 20));
        g2.drawRect(50, 50, w - 100, h - 100);

        g2.setColor(GameTheme.WOOD_COLOR);
        g2.setFont(new Font(GameTheme.THAI_FONT, Font.BOLD, 40));
        String msg = "Game Board Area";
        FontMetrics fm = g2.getFontMetrics();
        g2.drawString(msg, (w - fm.stringWidth(msg)) / 2, h / 2);
    }
}