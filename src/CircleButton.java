import javax.swing.*;
import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Point2D;

public class CircleButton extends JButton {
    public CircleButton(String label) {
        super(label);
        setContentAreaFilled(false); // ปิดการวาดพื้นหลังปกติ
        setFocusPainted(false);
        setBorderPainted(false);
        setFont(new Font("SansSerif", Font.BOLD, 20));
        setForeground(Color.WHITE);
        setCursor(new Cursor(Cursor.HAND_CURSOR));
        setFocusable(true);
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        // เปลี่ยนสีเมื่อเอาเมาส์ไปวาง หรือ กด
        if (getModel().isArmed()) {
            g2.setColor(new Color(200, 150, 0)); // สีเข้มตอนกด
        } else if (isEnabled()) {
            g2.setColor(new Color(255, 180, 50)); // สีปกติ (สีส้มทอง)
        } else {
            g2.setColor(Color.GRAY); // สีเมื่อกดไม่ได้
        }

        g2.fill(new Ellipse2D.Double(0, 0, getWidth() - 1, getHeight() - 1));
        g2.dispose();
        super.paintComponent(g);
    }

    // กำหนดขอบเขตการคลิกให้เป็นวงกลมจริงๆ
    @Override
    public boolean contains(int x, int y) {
        double radius = getWidth() / 2.0;
        return Point2D.distance(x, y, radius, radius) <= radius;
    }
}