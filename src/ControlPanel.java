import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.geom.Ellipse2D;

public class ControlPanel extends JPanel {

    private CustomShapeButton rollButton;
    private JButton useCardButton;
    private JButton endTurnButton;
    private JButton surrenderButton; // เพิ่มปุ่มยอมแพ้

    public ControlPanel() {
        setPreferredSize(new Dimension(280, 0));
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
        rollButton.setPreferredSize(new Dimension(160, 160));
        rollButton.setShape(new Ellipse2D.Double(0, 0, 160, 160));
        rollButton.setActionCommand("ROLL");

        // 2. ปุ่มใช้การ์ด
        useCardButton = new JButton("Use Card");
        styleButton(useCardButton, new Color(100, 150, 255));
        useCardButton.setActionCommand("USE_CARD");

        // 3. ปุ่มจบเทิร์น
        endTurnButton = new JButton("End Turn");
        styleButton(endTurnButton, new Color(255, 100, 100));
        endTurnButton.setActionCommand("END_TURN");

        // 4. ปุ่มยอมแพ้ (เพิ่มใหม่)
        surrenderButton = new JButton("Surrender");
        styleButton(surrenderButton, new Color(150, 50, 50)); // ใช้สีแดงเข้ม/มืด
        surrenderButton.setActionCommand("SURRENDER");

        // จัดเรียงปุ่มลง Layout
        gbc.gridy = 0; gbc.insets = new Insets(30, 10, 40, 10); 
        add(rollButton, gbc);

        gbc.gridy = 1; gbc.insets = new Insets(10, 10, 10, 10);
        add(useCardButton, gbc);

        gbc.gridy = 2; 
        add(endTurnButton, gbc);

        // ดันปุ่มยอมแพ้ให้ห่างจากปุ่มปกติเล็กน้อย เผื่อกดพลาด
        gbc.gridy = 3; gbc.insets = new Insets(40, 10, 10, 10); 
        add(surrenderButton, gbc);
    }

    private void styleButton(JButton btn, Color bgColor) {
        btn.setFont(new Font("SansSerif", Font.BOLD, 18));
        btn.setBackground(bgColor);
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setPreferredSize(new Dimension(200, 50));
    }

    public void addActionListener(ActionListener listener) {
        rollButton.addActionListener(listener);
        useCardButton.addActionListener(listener);
        endTurnButton.addActionListener(listener);
        surrenderButton.addActionListener(listener); // สมัคร Listener ให้ปุ่มใหม่
    }

    // อัปเดตเมธอดเปิด/ปิดปุ่ม (เพิ่มพารามิเตอร์ surrender)
    public void setButtonsEnabled(boolean roll, boolean useCard, boolean endTurn, boolean surrender) {
        rollButton.setEnabled(roll);
        useCardButton.setEnabled(useCard);
        endTurnButton.setEnabled(endTurn);
        surrenderButton.setEnabled(surrender);
    }
}