import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;

public class GameUIComponents {

    // --- 1. ปุ่มสไตล์ไม้ (Wood Button) ---
    public static class WoodButton extends JButton {
        public WoodButton(String text) {
            super(text);
            setPreferredSize(new Dimension(180, 50));
            // ป้องกัน null pointer กรณีเรียกใช้ก่อนโหลดฟอนต์
            String fontName = (GameTheme.THAI_FONT != null) ? GameTheme.THAI_FONT : "SansSerif";
            setFont(new Font(fontName, Font.BOLD, 22));

            setBackground(GameTheme.WOOD_COLOR);
            setForeground(new Color(60, 40, 20));
            setFocusPainted(false);
            setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(GameTheme.WOOD_BORDER, 2),
                    BorderFactory.createEmptyBorder(5, 10, 5, 10)));
            addMouseListener(new MouseAdapter() {
                public void mouseEntered(MouseEvent evt) {
                    setBackground(new Color(255, 240, 200));
                }

                public void mouseExited(MouseEvent evt) {
                    setBackground(GameTheme.WOOD_COLOR);
                }
            });
        }
    }

    // --- 2. ปุ่มย้อนกลับ (Back Button) ---
    public static class BackButton extends JButton {
        public BackButton() {
            super(" < Back ");
            setPreferredSize(new Dimension(100, 40));
            String fontName = (GameTheme.THAI_FONT != null) ? GameTheme.THAI_FONT : "SansSerif";
            setFont(new Font(fontName, Font.BOLD, 16));

            setBackground(GameTheme.WOOD_COLOR);
            setForeground(new Color(60, 40, 20));
            setFocusPainted(false);
            setBorder(BorderFactory.createLineBorder(GameTheme.WOOD_BORDER, 2));
        }
    }

    // --- 3. ปุ่มวงกลม (Circular Button) ---
    public static class CircularButton extends JButton {
        public CircularButton(String text) {
            super(text);
            setPreferredSize(new Dimension(120, 120));
            setContentAreaFilled(false);
            setFocusPainted(false);
            setBorderPainted(false);
            String fontName = (GameTheme.THAI_FONT != null) ? GameTheme.THAI_FONT : "SansSerif";
            setFont(new Font(fontName, Font.BOLD, 20));
            setForeground(new Color(101, 67, 33));
        }

        @Override
        protected void paintComponent(Graphics g) {
            if (getModel().isArmed())
                g.setColor(new Color(255, 230, 150));
            else
                g.setColor(GameTheme.GOLD_COLOR);
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.fillOval(0, 0, getSize().width - 1, getSize().height - 1);
            g2.setColor(new Color(139, 90, 43));
            g2.setStroke(new BasicStroke(3));
            g2.drawOval(0, 0, getSize().width - 2, getSize().height - 2);
            super.paintComponent(g);
        }

        @Override
        public boolean contains(int x, int y) {
            Ellipse2D shape = new Ellipse2D.Float(0, 0, getWidth(), getHeight());
            return shape.contains(x, y);
        }
    }

    // --- 4. โลโก้ลูกเต๋า (Dice Panel) ---
    public static class TwoDicePanel extends JPanel {
        public TwoDicePanel() {
            setPreferredSize(new Dimension(250, 140));
            setOpaque(false);
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            drawRotatedDie(g2, 60, 60, 80, 4, -15);
            drawRotatedDie(g2, 140, 60, 80, 6, 20);
        }

        private void drawRotatedDie(Graphics2D g2, int x, int y, int size, int value, double angleDeg) {
            AffineTransform old = g2.getTransform();
            g2.translate(x, y);
            g2.rotate(Math.toRadians(angleDeg));
            g2.translate(-size / 2, -size / 2);
            g2.setColor(Color.WHITE);
            g2.fillRoundRect(0, 0, size, size, 20, 20);
            g2.setColor(Color.LIGHT_GRAY);
            g2.setStroke(new BasicStroke(2));
            g2.drawRoundRect(0, 0, size, size, 20, 20);
            g2.setColor(Color.BLACK);
            drawDots(g2, size, value);
            g2.setTransform(old);
        }

        private void drawDots(Graphics2D g2, int size, int value) {
            int dot = size / 5, c = size / 2, l = size / 4, r = size * 3 / 4;
            if (value % 2 != 0)
                g2.fillOval(c - dot / 2, c - dot / 2, dot, dot);
            if (value > 1) {
                g2.fillOval(l - dot / 2, l - dot / 2, dot, dot);
                g2.fillOval(r - dot / 2, r - dot / 2, dot, dot);
            }
            if (value > 3) {
                g2.fillOval(r - dot / 2, l - dot / 2, dot, dot);
                g2.fillOval(l - dot / 2, r - dot / 2, dot, dot);
            }
            if (value == 6) {
                g2.fillOval(l - dot / 2, c - dot / 2, dot, dot);
                g2.fillOval(r - dot / 2, c - dot / 2, dot, dot);
            }
        }
    }
}