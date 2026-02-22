import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.geom.Ellipse2D;

public class ControlPanel extends JPanel {

    private CustomShapeButton rollButton;
    private JButton buyButton;
    private JButton useCardButton;
    private JButton endTurnButton;

    public ControlPanel() {
        setPreferredSize(new Dimension(300, 0));
        setBackground(new Color(30, 35, 45));
        setLayout(new GridBagLayout());

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(15, 10, 15, 10);
        gbc.gridx = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // 1. ปุ่มทอยเต๋า
        rollButton = new CustomShapeButton("ROLL", 28);
        rollButton.setBackground(new Color(255, 180, 50));
        rollButton.setForeground(Color.DARK_GRAY);
        rollButton.setPreferredSize(new Dimension(150, 150));
        rollButton.setShape(new Ellipse2D.Double(0, 0, 150, 150));
        rollButton.setActionCommand("ROLL"); // กำหนด Command ให้ตรงกับ switch-case ใน Controller

        // 2. ปุ่มซื้อที่ดิน
        buyButton = new JButton("Buy Property");
        buyButton.setFont(new Font("Arial", Font.BOLD, 16));
        buyButton.setPreferredSize(new Dimension(150, 40));
        buyButton.setActionCommand("BUY");

        // 3. ปุ่มใช้การ์ด
        useCardButton = new JButton("Use Card");
        useCardButton.setFont(new Font("Arial", Font.BOLD, 16));
        useCardButton.setPreferredSize(new Dimension(150, 40));
        useCardButton.setActionCommand("USE_CARD");

        // 4. ปุ่มจบเทิร์น
        endTurnButton = new JButton("End Turn");
        endTurnButton.setFont(new Font("Arial", Font.BOLD, 16));
        endTurnButton.setPreferredSize(new Dimension(150, 40));
        endTurnButton.setActionCommand("END_TURN");

        // นำปุ่มจัดเรียงลง Panel
        gbc.gridy = 0;
        add(rollButton, gbc);
        gbc.gridy = 1;
        add(buyButton, gbc);
        gbc.gridy = 2;
        add(useCardButton, gbc);
        gbc.gridy = 3;
        add(endTurnButton, gbc);
    }

    // เมธอดสำหรับให้ GameWindow นำ Listener จาก GameController มาผูกกับทุกปุ่ม
    public void addActionListener(ActionListener listener) {
        rollButton.addActionListener(listener);
        buyButton.addActionListener(listener);
        useCardButton.addActionListener(listener);
        endTurnButton.addActionListener(listener);
    }

    // เมธอดเปิด/ปิดปุ่มตาม Phase ที่ Controller สั่ง
    public void setButtonsEnabled(boolean roll, boolean buy, boolean useCard, boolean endTurn) {
        rollButton.setEnabled(roll);
        buyButton.setEnabled(buy);
        useCardButton.setEnabled(useCard);
        endTurnButton.setEnabled(endTurn);
    }
}