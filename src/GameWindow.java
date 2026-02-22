import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.util.List;

public class GameWindow extends JFrame {

    private BoardPanel boardPanel;
    private ControlPanel controlPanel;

    // ลบการสร้าง GameController ออกจากที่นี่ เพราะตามหลัก MVC
    // ตัว Main.java จะเป็นคนสร้าง GameWindow และ GameController
    // แล้วเชื่อมเข้าด้วยกัน

    public GameWindow(Board board) {
        setTitle("Isometric Board Game - MVC Architecture");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        getContentPane().setPreferredSize(new Dimension(1600, 900));
        setLayout(new BorderLayout());

        // ส่ง Board ไปให้ BoardPanel วาด
        boardPanel = new BoardPanel(board);
        controlPanel = new ControlPanel();

        add(boardPanel, BorderLayout.CENTER);
        add(controlPanel, BorderLayout.EAST);

        pack();
        setLocationRelativeTo(null);
        setExtendedState(JFrame.MAXIMIZED_BOTH);
    }

    // --- เมธอดที่ GameController จำเป็นต้องเรียกใช้ ---

    public void setActionListener(ActionListener listener) {
        // ส่งต่อ Listener ไปผูกกับปุ่มใน ControlPanel
        controlPanel.addActionListener(listener);
    }

    public ControlPanel getControlPanel() {
        return controlPanel;
    }

    public void updateView(GameState state) {
        // อัปเดตตำแหน่งของผู้เล่นทุกคนบน BoardPanel
        for (Player p : state.getPlayers()) {
            boardPanel.updatePlayerUI(p.getId(), p.getPosition());
        }
        boardPanel.repaint();
    }

    public void showPopup(String message) {
        JOptionPane.showMessageDialog(this, message, "System Event", JOptionPane.INFORMATION_MESSAGE);
    }

    public Player showSelectTargetDialog(List<Player> opponents) {
        if (opponents == null || opponents.isEmpty())
            return null;

        // แปลงเป็น Array เพื่อใส่ลงใน Dropdown ของ JOptionPane
        Player[] oppArray = opponents.toArray(new Player[0]);

        return (Player) JOptionPane.showInputDialog(
                this,
                "Select a target player:",
                "Use Card",
                JOptionPane.QUESTION_MESSAGE,
                null,
                oppArray,
                oppArray[0]);
    }
}