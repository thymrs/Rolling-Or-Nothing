import javax.swing.*;
import java.awt.*;

public class ControlPanel extends JPanel {
    public ControlPanel() {
        setPreferredSize(new Dimension(300, 0));
        setBackground(new Color(30, 30, 30));
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createMatteBorder(0, 5, 0, 0, GameTheme.WOOD_BORDER));
        initUI();
    }

    private void initUI() {
        JPanel infoPanel = new JPanel();
        infoPanel.setOpaque(false);
        infoPanel.setBorder(BorderFactory.createEmptyBorder(20, 10, 20, 10));
        JLabel turnLabel = new JLabel("Player 1's Turn");
        turnLabel.setFont(new Font(GameTheme.THAI_FONT, Font.BOLD, 24));
        turnLabel.setForeground(GameTheme.GOLD_COLOR);
        infoPanel.add(turnLabel);
        add(infoPanel, BorderLayout.NORTH);

        JPanel actionPanel = new JPanel(new GridLayout(3, 1, 10, 10));
        actionPanel.setOpaque(false);
        actionPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        GameUIComponents.WoodButton btnRoll = new GameUIComponents.WoodButton("Roll Dice");
        GameUIComponents.WoodButton btnEndTurn = new GameUIComponents.WoodButton("End Turn");
        GameUIComponents.WoodButton btnMenu = new GameUIComponents.WoodButton("Surrender");

        btnMenu.addActionListener(e -> {
            Window w = SwingUtilities.getWindowAncestor(this);
            if (w instanceof GameWindow)
                ((GameWindow) w).returnToMenu();
        });

        actionPanel.add(btnRoll);
        actionPanel.add(btnEndTurn);
        actionPanel.add(btnMenu);
        add(actionPanel, BorderLayout.SOUTH);
    }
}