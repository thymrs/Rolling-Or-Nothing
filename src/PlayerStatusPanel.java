import javax.swing.*;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;

public class PlayerStatusPanel extends JPanel {

    private String playerName;
    private Color playerColor;
    
    private JLabel nameLabel;
    private JLabel cashLabel;
    private JLabel assetsLabel;
    private JLabel statusLabel;

    public PlayerStatusPanel(String playerName, Color playerColor) {
        this.playerName = playerName;
        this.playerColor = playerColor;
        
        // ทำให้พื้นหลังเดิมโปร่งใส เพื่อที่เราจะวาดกล่องขอบมนเองใน paintComponent
        setOpaque(false); 
        setLayout(new BorderLayout());

        // แผงข้อมูล (เว้นขอบซ้ายเยอะหน่อยเพื่อเว้นที่ให้แถบสีประจำตัว)
        JPanel dataPanel = new JPanel();
        dataPanel.setLayout(new GridLayout(4, 1, 0, 2));
        dataPanel.setOpaque(false);
        dataPanel.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 10));

        // 1. ชื่อผู้เล่น
        nameLabel = new JLabel(playerName);
        nameLabel.setFont(new Font("SansSerif", Font.BOLD, 18));
        nameLabel.setForeground(playerColor); // ให้ชื่อเป็นสีเดียวกับสีประจำตัว

        // 2. เงินสด
        cashLabel = new JLabel("เงินสด: 0");
        cashLabel.setFont(new Font("SansSerif", Font.BOLD, 14));
        cashLabel.setForeground(new Color(80, 200, 120)); // สีเขียวสว่าง

        // 3. ทรัพย์สินรวม
        assetsLabel = new JLabel("ทรัพย์สินรวม: 0");
        assetsLabel.setFont(new Font("SansSerif", Font.PLAIN, 14));
        assetsLabel.setForeground(new Color(200, 200, 200)); // สีเทาอ่อน

        // 4. สถานะ (เช่น ปกติ, ติดคุก, ล้มละลาย)
        statusLabel = new JLabel("สถานะ: ปกติ");
        statusLabel.setFont(new Font("SansSerif", Font.PLAIN, 14));
        statusLabel.setForeground(new Color(200, 200, 200));

        dataPanel.add(nameLabel);
        dataPanel.add(cashLabel);
        dataPanel.add(assetsLabel);
        dataPanel.add(statusLabel);

        add(dataPanel, BorderLayout.CENTER);
    }

    // เมธอดสำหรับรับข้อมูลอัปเดตจาก GameWindow / BoardPanel
    public void updateData(int cash, int assets, String status, boolean isJailed) {
        cashLabel.setText("เงินสด: " + cash);
        assetsLabel.setText("ทรัพย์สินรวม: " + assets);
        statusLabel.setText("สถานะ: " + status);
        
        // เปลี่ยนสีถ้าติดคุกหรือล้มละลาย
        if (isJailed || status.equals("ล้มละลาย")) {
            statusLabel.setForeground(new Color(255, 100, 100)); // สีแดงเตือน
        } else {
            statusLabel.setForeground(new Color(200, 200, 200)); // กลับเป็นสีปกติ
        }
        
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int w = getWidth();
        int h = getHeight();

        // 1. วาดพื้นหลังกล่อง (สีเทาเข้มกึ่งโปร่งใส)
        g2.setColor(new Color(40, 45, 55, 230));
        g2.fill(new RoundRectangle2D.Double(0, 0, w, h, 15, 15));

        // 2. วาดแถบสีประจำตัวที่ขอบซ้าย (Color Bar)
        g2.setColor(playerColor);
        g2.fill(new RoundRectangle2D.Double(0, 0, 8, h, 15, 15)); // วาดขอบมน
        g2.fillRect(4, 0, 4, h); // วาดสี่เหลี่ยมทับครึ่งขวาของแถบ เพื่อให้มนแค่ฝั่งซ้าย

        // 3. วาดเส้นขอบรวมๆ
        g2.setColor(new Color(100, 100, 100, 100));
        g2.setStroke(new BasicStroke(1.5f));
        g2.draw(new RoundRectangle2D.Double(1, 1, w - 2, h - 2, 15, 15));

        g2.dispose();
    }
}