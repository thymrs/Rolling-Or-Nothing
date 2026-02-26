import javax.swing.*;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;

public class PlayerStatusPanel extends JPanel {

    private String playerName;
    private Color playerColor;

    private JLabel nameLabel;
    private JLabel cashLabel;
    private JLabel assetsLabel;
    private JLabel statusAndBuffLabel;
    private JLabel CardLabel; // เพิ่ม: ป้ายแสดงบัฟพิเศษ
    private JLabel positionLabel; // เพิ่ม: ป้ายแสดงตำแหน่งปัจจุบันของผู้เล่น
    private JLabel avatarLabel; // เพิ่ม: ป้ายแสดงรูปประจำตัว (ถ้าต้องการใช้)

    private boolean isBankrupt = false; // เพิ่ม: เก็บสถานะล้มละลายเพื่อเปลี่ยนสีพื้นหลัง

    public PlayerStatusPanel(String playerName, Color playerColor) {
        this.playerName = playerName;
        this.playerColor = playerColor;

        // ทำให้พื้นหลังเดิมโปร่งใส เพื่อที่เราจะวาดกล่องขอบมนเองใน paintComponent
        setOpaque(false);
        setLayout(new BorderLayout());

        avatarLabel = new JLabel();
        avatarLabel.setPreferredSize(new Dimension(90, 90)); // ขนาดกล่องรูป
        avatarLabel.setHorizontalAlignment(SwingConstants.CENTER);
        avatarLabel.setVerticalAlignment(SwingConstants.CENTER);

        // แผงข้อมูล (เว้นขอบซ้ายเยอะหน่อยเพื่อเว้นที่ให้แถบสีประจำตัว)
        JPanel dataPanel = new JPanel();
        dataPanel.setLayout(new GridLayout(6, 1, 0, 2)); // เปลี่ยนจาก 5 เป็น 6 แถว
        dataPanel.setOpaque(false);
        dataPanel.setBorder(BorderFactory.createEmptyBorder(10, 5, 10, 15));

        // 1. ชื่อผู้เล่น
        nameLabel = new JLabel(playerName);
        nameLabel.setFont(new Font("SansSerif", Font.BOLD, 18));
        nameLabel.setForeground(playerColor); // ให้ชื่อเป็นสีเดียวกับสีประจำตัว

        // 2. เงินสด
        cashLabel = new JLabel("Cash: 0");
        cashLabel.setFont(new Font("SansSerif", Font.BOLD, 10));
        cashLabel.setForeground(new Color(80, 200, 120)); // สีเขียวสว่าง

        // 3. ทรัพย์สินรวม
        assetsLabel = new JLabel("Assets: 0");
        assetsLabel.setFont(new Font("SansSerif", Font.BOLD, 10));
        assetsLabel.setForeground(new Color(200, 200, 200)); // สีเทาอ่อน

        // 4. สถานะ (เช่น ปกติ, ติดคุก, ล้มละลายมีการ์ดนางฟ้า, โล่)
        statusAndBuffLabel = new JLabel("Status: Normal");
        statusAndBuffLabel.setFont(new Font("SansSerif", Font.BOLD, 10));
        statusAndBuffLabel.setForeground(new Color(200, 200, 200));

        // 5. บอกว่าถือการ์ดพิเศษอะไรอยู่อันเดียว(ถ้ามี)
        CardLabel = new JLabel("Cards: 0");
        CardLabel.setFont(new Font("SansSerif", Font.BOLD, 10));
        CardLabel.setForeground(new Color(255, 204, 102)); // สีส้มทอง (หรือเปลี่ยนสีตามใจชอบ)

        // 6. ตำแหน่งช่องที่อยู่
        positionLabel = new JLabel("Position: Start");
        positionLabel.setFont(new Font("SansSerif", Font.BOLD, 10));
        positionLabel.setForeground(new Color(255, 204, 102)); // สีส้มทอง (หรือเปลี่ยนสีตามใจชอบ)

        dataPanel.add(nameLabel);
        dataPanel.add(positionLabel);
        dataPanel.add(cashLabel);
        dataPanel.add(assetsLabel);
        dataPanel.add(statusAndBuffLabel);
        dataPanel.add(CardLabel);

        add(avatarLabel, BorderLayout.WEST);
        add(dataPanel, BorderLayout.CENTER);
    }

    public void setAvatarImage(ImageIcon icon) {
        if (icon != null) {
            Image img = icon.getImage().getScaledInstance(70, 70, Image.SCALE_SMOOTH);
            avatarLabel.setIcon(new ImageIcon(img));
            avatarLabel.setText(""); // ลบตัวอักษรเริ่มต้นออก
        }
    }

    public void updateData(Player p, String posName, String status) {
        this.isBankrupt = p.isBankrupt();
        positionLabel.setText("POSITION: " + posName + " [" + p.getPosition() + "]"); // อัปเดตตำแหน่งพร้อมชื่อช่อง

        int totalAssets = p.getMoney();
        for (PropertyTile land : p.getOwnedLands()) {
            totalAssets += land.getPurchasePrice(); // หรือราคาซื้อรวมเลเวลบ้าน
        }
        if (p.getHeldCard() == null) {
            CardLabel.setText("CARDS: NONE ");
        } else {
            CardLabel.setText("CARDS: " + p.getHeldCard());
        }

        if (isBankrupt) {
            cashLabel.setText("CASH: Bankrupt");
            assetsLabel.setText("ASSETS: 0");
            statusAndBuffLabel.setText("STATUS: BANKRUPT");
            statusAndBuffLabel.setForeground(new Color(255, 100, 100)); // สีแดงเตือน
            CardLabel.setText("CARDS: 0"); // ล้มละลายแล้วไม่มีการ์ด
        } else {
            cashLabel.setText("CASH: " + p.getMoney());
            assetsLabel.setText("ASSETS: " + totalAssets); // อัปเดตทรัพย์สินรวม (ตอนนี้ยังใช้เงินสดเป็นตัวแทนทั้งหมด)
            statusAndBuffLabel.setText("STATUS: " + status);

            // เปลี่ยนสีถ้าติดคุก
            if (p.getIsJailed()) {
                statusAndBuffLabel.setForeground(new Color(255, 100, 100)); // สีแดงเตือน
            } else {
                statusAndBuffLabel.setForeground(new Color(200, 200, 200)); // กลับเป็นสีปกติ
            }

            // อัปเดตข้อความ Buff
            String buffText = "";
            if (p.hasShield)
                buffText += "[SHIELD] ";
            if (p.isTollFree)
                buffText += "[TOLL FREE] ";
            if (p.getDiscountRate() > 0)
                buffText += "[DISCOUNT " + p.getDiscountRate() + "%]";
            statusAndBuffLabel.setText("STATUS: " + status + " " + buffText);
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