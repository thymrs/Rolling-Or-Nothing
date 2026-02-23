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
    private JLabel buffLabel; // เพิ่ม: ป้ายแสดงบัฟพิเศษ

    private boolean isBankrupt = false; // เพิ่ม: เก็บสถานะล้มละลายเพื่อเปลี่ยนสีพื้นหลัง

    public PlayerStatusPanel(String playerName, Color playerColor) {
        this.playerName = playerName;
        this.playerColor = playerColor;
        
        // ทำให้พื้นหลังเดิมโปร่งใส เพื่อที่เราจะวาดกล่องขอบมนเองใน paintComponent
        setOpaque(false); 
        setLayout(new BorderLayout());

        // แผงข้อมูล (เว้นขอบซ้ายเยอะหน่อยเพื่อเว้นที่ให้แถบสีประจำตัว)
        JPanel dataPanel = new JPanel();
        dataPanel.setLayout(new GridLayout(5, 1, 0, 2)); // เปลี่ยนจาก 4 เป็น 5 แถว
        dataPanel.setOpaque(false);
        dataPanel.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 10));

        // 1. ชื่อผู้เล่น
        nameLabel = new JLabel(playerName);
        nameLabel.setFont(new Font("SansSerif", Font.BOLD, 18));
        nameLabel.setForeground(playerColor); // ให้ชื่อเป็นสีเดียวกับสีประจำตัว

        // 2. เงินสด
        cashLabel = new JLabel("Cash: 0");
        cashLabel.setFont(new Font("SansSerif", Font.BOLD, 14));
        cashLabel.setForeground(new Color(80, 200, 120)); // สีเขียวสว่าง

        // 3. ทรัพย์สินรวม
        assetsLabel = new JLabel("Assets: 0");
        assetsLabel.setFont(new Font("SansSerif", Font.PLAIN, 14));
        assetsLabel.setForeground(new Color(200, 200, 200)); // สีเทาอ่อน

        // 4. สถานะ (เช่น ปกติ, ติดคุก, ล้มละลาย)
        statusLabel = new JLabel("Status: Normal");
        statusLabel.setFont(new Font("SansSerif", Font.PLAIN, 14));
        statusLabel.setForeground(new Color(200, 200, 200));

        // 5. บัฟพิเศษ (เช่น มีการ์ดนางฟ้า, โล่)
        buffLabel = new JLabel(""); 
        buffLabel.setFont(new Font("SansSerif", Font.ITALIC, 12));
        buffLabel.setForeground(new Color(100, 255, 100)); // สีเขียวสว่าง

        dataPanel.add(nameLabel);
        dataPanel.add(cashLabel);
        dataPanel.add(assetsLabel);
        dataPanel.add(statusLabel);
        dataPanel.add(buffLabel);

        add(dataPanel, BorderLayout.CENTER);
    }

    public void updateData(int cash, int assets, String status, boolean isJailed, boolean isBankrupt, boolean hasShield, boolean isTollFree) {
        this.isBankrupt = isBankrupt;

        if (isBankrupt) {
            cashLabel.setText("Cash: Bankrupt");
            assetsLabel.setText("Assets: 0");
            statusLabel.setText("Status: " + status);
            statusLabel.setForeground(new Color(255, 100, 100)); // สีแดงเตือน
            buffLabel.setText(""); // ล้มละลายแล้วไม่มีบัฟ
        } else {
            cashLabel.setText("Cash: " + cash);
            assetsLabel.setText("Assets: " + assets);
            statusLabel.setText("Status: " + status);
            
            // เปลี่ยนสีถ้าติดคุก
            if (isJailed) {
                statusLabel.setForeground(new Color(255, 100, 100)); // สีแดงเตือน
            } else {
                statusLabel.setForeground(new Color(200, 200, 200)); // กลับเป็นสีปกติ
            }

            // อัปเดตข้อความ Buff
            String buffText = "";
            if (hasShield) buffText += "[Shield] ";
            if (isTollFree) buffText += "[Toll Free] ";
            buffLabel.setText(buffText);
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
        if (isBankrupt) {
            g2.setColor(new Color(30, 30, 30, 200)); // ถ้าล้มละลายให้กล่องมืดลง
        } else {
            g2.setColor(new Color(40, 45, 55, 230)); // สีปกติ
        }
        g2.fill(new RoundRectangle2D.Double(0, 0, w, h, 15, 15));

        // 2. วาดแถบสีประจำตัวที่ขอบซ้าย (Color Bar)
        if (isBankrupt) {
            g2.setColor(Color.DARK_GRAY); // ล้มละลาย สีประจำตัวกลายเป็นสีเทา
        } else {
            g2.setColor(playerColor);
        }
        g2.fill(new RoundRectangle2D.Double(0, 0, 10, h, 15, 15));
        g2.fillRect(5, 0, 5, h); // ทำให้ฝั่งขวาของแถบสีไม่โค้งเหลี่ยมตรง

        g2.dispose();
    }
}