import javax.swing.*;
import java.awt.*;

/**
 * คลาสสำหรับสร้างป้ายกำกับการคูณ (Multiplier Badge)
 * แสดงผลเป็นป้ายสี่เหลี่ยมด้านขนาน (Parallelogram) ตามมุมที่กำหนด
 */
public class FestivalMultiplyPanel extends JPanel {
    private int multiplier;
    private double angle; // มุมในการเอียง (องศา)

    /**
     * @param multiplier ตัวคูณ (เช่น 2, 4)
     * @param angle มุมเอียงของสี่เหลี่ยมด้านขนาน (เช่น 15.0 สำหรับเอียงขวา, -15.0 สำหรับเอียงซ้าย)
     */
    public FestivalMultiplyPanel(int multiplier, double angle) {
        this.multiplier = multiplier;
        this.angle = angle;
        
        // ทำให้พื้นหลังโปร่งใส เพื่อให้เราวาดรูปทรงเองได้
        setOpaque(false); 
        // ไม่ต้องใช้ JLabel แล้ว เราจะวาดข้อความเองใน paintComponent
    }

    /**
     * อัปเดตตัวเลขตัวคูณบนป้าย
     */
    public void setMultiplier(int multiplier) {
        this.multiplier = multiplier;
        repaint();
    }

    /**
     * อัปเดตมุมเอียง (มีประโยชน์ถ้าช่องแต่ละฝั่งของกระดานต้องการองศาเอียงไม่เหมือนกัน)
     */
    public void setAngle(double angle) {
        this.angle = angle;
        repaint();
    }

    /**
     * วาดกราฟิกของป้าย (สี่เหลี่ยมด้านขนาน พื้นสีแดง ขอบสีขาว)
     */
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        
        // เปิดโหมดลบรอยหยักให้ขอบและตัวอักษรดูเนียน
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        int w = getWidth();
        int h = getHeight();

        // คำนวณระยะการเยื้อง (Offset) ของแกน X ตามมุมองศา (ใช้หลักการ tan(theta) = ข้าม/ชิด)
        int offset = (int) (h * Math.tan(Math.toRadians(Math.abs(angle))));
        
        // ป้องกันไม่ให้มุมเอียงมากเกินไปจนวาดล้นกรอบ Panel (จำกัดเยื้องสูงสุดที่ไม่เกินครึ่งหนึ่งของความกว้าง)
        offset = Math.min(offset, w / 2 - 2);

        // สร้างรูปทรงสี่เหลี่ยมด้านขนาน
        Polygon p = new Polygon();
        if (angle >= 0) {
            // เอียงขวา ( / )
            p.addPoint(offset, 1);                         // บนซ้าย
            p.addPoint(w - 2, 1);                          // บนขวา
            p.addPoint(w - 2 - offset, h - 2);             // ล่างขวา
            p.addPoint(1, h - 2);                          // ล่างซ้าย
        } else {
            // เอียงซ้าย ( \ )
            p.addPoint(1, 1);                              // บนซ้าย
            p.addPoint(w - 2 - offset, 1);                 // บนขวา
            p.addPoint(w - 2, h - 2);                      // ล่างขวา
            p.addPoint(offset, h - 2);                     // ล่างซ้าย
        }

        // 1. วาดพื้นหลังสีแดง (Crimson)
        g2.setColor(new Color(220, 20, 60)); 
        g2.fillPolygon(p);

        // 2. วาดเส้นขอบสีขาว
        g2.setColor(Color.WHITE);
        g2.setStroke(new BasicStroke(2f));
        g2.drawPolygon(p);

        // 3. วาดตัวหนังสือ x2, x4 ตรงกลาง
        String text = "x" + multiplier;
        g2.setFont(new Font("Arial", Font.BOLD, 14));
        FontMetrics fm = g2.getFontMetrics();
        
        // คำนวณหาจุดกึ่งกลางของ Panel สำหรับวาดข้อความ
        int textX = (w - fm.stringWidth(text)) / 2;
        int textY = (h - fm.getHeight()) / 2 + fm.getAscent();
        
        g2.drawString(text, textX, textY);

        g2.dispose();
    }
}