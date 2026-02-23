import javax.swing.*;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;

public class TurnDisplayPanel extends JPanel {
    private int currentTurn = 1;
    private int maxTurns = 50; // ค่าเริ่มต้น (จะอัปเดตจาก GameConfig)
    private JLabel turnLabel;

    public TurnDisplayPanel() {
        setOpaque(false);
        setPreferredSize(new Dimension(200, 60));
        setLayout(new BorderLayout());

        turnLabel = new JLabel("TURN: 1 / 50", SwingConstants.CENTER);
        turnLabel.setFont(new Font("SansSerif", Font.BOLD, 22));
        turnLabel.setForeground(new Color(255, 215, 0)); // สีทอง
        add(turnLabel, BorderLayout.CENTER);
    }

    // เมธอดสำหรับอัปเดตค่าจาก GameState และ GameConfig 
    public void updateTurn(int current, int max) {
        this.currentTurn = current;
        this.maxTurns = max;
        turnLabel.setText("TURN: " + currentTurn + " / " + maxTurns);
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // วาดพื้นหลังกล่องดีไซน์โค้งมน
        g2.setColor(new Color(30, 35, 45, 200));
        g2.fill(new RoundRectangle2D.Double(0, 0, getWidth(), getHeight(), 15, 15));
        
        // วาดเส้นขอบ
        g2.setColor(new Color(255, 215, 0, 150));
        g2.setStroke(new BasicStroke(2f));
        g2.draw(new RoundRectangle2D.Double(2, 2, getWidth()-4, getHeight()-4, 15, 15));

        g2.dispose();
    }
}