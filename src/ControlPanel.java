import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionListener;

public class ControlPanel extends JPanel {

    private JButton btnBuy;
    private JButton btnUseCard;
    private JButton btnEndTurn;
    private JButton btnSurrender;

    public ControlPanel() {
        setLayout(new GridLayout(4, 1, 15, 15));
        setBorder(new EmptyBorder(20, 20, 20, 20));
        setBackground(new Color(30, 35, 45)); // Dark Theme
        setPreferredSize(new Dimension(250, getHeight()));

        // ==============================================================
        // สังเกต ActionCommand (พารามิเตอร์ที่ 2) ต้องตรงกับ Switch Case ใน Controller
        // ของเพื่อนเป๊ะๆ
        // "ROLL", "BUY", "USE_CARD", "END_TURN"
        // ==============================================================

        btnBuy = createButton("🏠 Buy / Upgrade", "BUY", new Color(50, 150, 255));
        btnUseCard = createButton("🃏 Use Card", "USE_CARD", new Color(255, 150, 50));
        btnEndTurn = createButton("⏳ End Turn", "END_TURN", new Color(255, 100, 100));
        btnSurrender = createButton("🏳️ Surrender", "SURRENDER", new Color(200, 50, 50));

        add(btnBuy);
        add(btnUseCard);
        add(btnEndTurn);
        add(btnSurrender);
    }

    // Method Helper สำหรับตกแต่งปุ่มให้สวยงาม
    private JButton createButton(String text, String command, Color bgColor) {
        JButton btn = new JButton(text);
        btn.setActionCommand(command); // สำคัญมาก! ตัวนี้คือคำสั่งที่จะส่งไปให้ GameController
        btn.setBackground(bgColor);
        btn.setForeground(Color.WHITE);
        btn.setFont(new Font("SansSerif", Font.BOLD, 18));
        btn.setFocusable(false);
        btn.setFocusPainted(false);
        return btn;
    }

    // รับ Controller มาแอบฟังว่าปุ่มโดนกดไหม
    public void setActionListener(ActionListener listener) {

        btnBuy.addActionListener(listener);
        btnUseCard.addActionListener(listener);
        btnEndTurn.addActionListener(listener);
        btnSurrender.addActionListener(listener);
    }

    // Method นี้สร้างมาเพื่อให้ GameController ของเพื่อนเรียกใช้ได้ตรงๆ
    public void setButtonsEnabled(boolean isMyTurn) {

        btnBuy.setEnabled(isMyTurn);
        btnUseCard.setEnabled(isMyTurn);
        btnEndTurn.setEnabled(isMyTurn);
        btnSurrender.setEnabled(isMyTurn);
    }
}