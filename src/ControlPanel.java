import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionListener;

public class ControlPanel extends JPanel {

    private JButton btnRoll;
    private JButton btnBuy;
    private JButton btnUseCard;
    private JButton btnEndTurn;

    public ControlPanel() {
        setLayout(new GridLayout(4, 1, 15, 15));
        setBorder(new EmptyBorder(20, 20, 20, 20));
        setBackground(new Color(30, 35, 45)); // Dark Theme
        setPreferredSize(new Dimension(250, getHeight()));

        // ==============================================================
        // สังเกต ActionCommand (พารามิเตอร์ที่ 2) ต้องตรงกับ Switch Case ใน Controller ของเพื่อนเป๊ะๆ
        // "ROLL", "BUY", "USE_CARD", "END_TURN"
        // ==============================================================
        
        btnRoll = createButton("🎲 ทอยลูกเต๋า", "ROLL", new Color(80, 200, 120));
        btnBuy = createButton("🏠 ซื้อ / อัปเกรด", "BUY", new Color(50, 150, 255));
        btnUseCard = createButton("🃏 ใช้การ์ด", "USE_CARD", new Color(255, 150, 50));
        btnEndTurn = createButton("⏳ จบเทิร์น", "END_TURN", new Color(255, 100, 100));

        add(btnRoll);
        add(btnBuy);
        add(btnUseCard);
        add(btnEndTurn);
    }

    // Method Helper สำหรับตกแต่งปุ่มให้สวยงาม
    private JButton createButton(String text, String command, Color bgColor) {
        JButton btn = new JButton(text);
        btn.setActionCommand(command); // สำคัญมาก! ตัวนี้คือคำสั่งที่จะส่งไปให้ GameController
        btn.setBackground(bgColor);
        btn.setForeground(Color.WHITE);
        btn.setFont(new Font("SansSerif", Font.BOLD, 18));
        btn.setFocusPainted(false);
        return btn;
    }

    // รับ Controller มาแอบฟังว่าปุ่มโดนกดไหม
    public void setActionListener(ActionListener listener) {
        btnRoll.addActionListener(listener);
        btnBuy.addActionListener(listener);
        btnUseCard.addActionListener(listener);
        btnEndTurn.addActionListener(listener);
    }

    // Method นี้สร้างมาเพื่อให้ GameController ของเพื่อนเรียกใช้ได้ตรงๆ 
    // ตัวอย่างของเพื่อน: view.getControlPanel().setButtonsEnabled(true, false, false, true);
    public void setButtonsEnabled(boolean roll, boolean buy, boolean useCard, boolean endTurn) {
        btnRoll.setEnabled(roll);
        btnBuy.setEnabled(buy);
        btnUseCard.setEnabled(useCard);
        btnEndTurn.setEnabled(endTurn);
    }
}