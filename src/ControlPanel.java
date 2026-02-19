import javax.swing.*;
import java.awt.*;

public class ControlPanel extends JPanel {
    
    private JLabel turnLabel;
    private JLabel moneyLabel;
    private GameUIComponents.WoodButton btnRoll;
    private GameUIComponents.WoodButton btnEndTurn;
    private GameUIComponents.WoodButton btnMenu;
    
    private GameController controller; // อ้างอิง Controller

    public ControlPanel() {
        setPreferredSize(new Dimension(300, 0));
        setBackground(new Color(30, 30, 30));
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createMatteBorder(0, 5, 0, 0, GameTheme.WOOD_BORDER));
        initUI();
    }
    
    public void setController(GameController controller) {
        this.controller = controller;
    }

    private void initUI() {
        JPanel infoPanel = new JPanel(new GridLayout(2, 1)); 
        infoPanel.setOpaque(false);
        infoPanel.setBorder(BorderFactory.createEmptyBorder(20, 10, 20, 10));
        
        turnLabel = new JLabel("Waiting to start...", SwingConstants.CENTER);
        String fontName = (GameTheme.THAI_FONT != null) ? GameTheme.THAI_FONT : "SansSerif";
        turnLabel.setFont(new Font(fontName, Font.BOLD, 24));
        turnLabel.setForeground(GameTheme.GOLD_COLOR);
        
        moneyLabel = new JLabel("$0", SwingConstants.CENTER);
        moneyLabel.setFont(new Font(fontName, Font.PLAIN, 20));
        moneyLabel.setForeground(Color.WHITE);
        
        infoPanel.add(turnLabel);
        infoPanel.add(moneyLabel);
        add(infoPanel, BorderLayout.NORTH);

        JPanel actionPanel = new JPanel(new GridLayout(3, 1, 10, 10));
        actionPanel.setOpaque(false);
        actionPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        btnRoll = new GameUIComponents.WoodButton("Roll Dice");
        btnEndTurn = new GameUIComponents.WoodButton("End Turn");
        btnMenu = new GameUIComponents.WoodButton("Surrender");

        btnRoll.addActionListener(e -> {
            if (controller != null) controller.rollDice();
        });
        
        btnEndTurn.addActionListener(e -> {
            if (controller != null) controller.endTurn();
        });

        btnMenu.addActionListener(e -> {
            Window w = SwingUtilities.getWindowAncestor(this);
            if (w instanceof GameWindow) ((GameWindow) w).returnToMenu();
        });
        
        btnRoll.setEnabled(false);
        btnEndTurn.setEnabled(false);

        actionPanel.add(btnRoll); 
        actionPanel.add(btnEndTurn); 
        actionPanel.add(btnMenu);
        add(actionPanel, BorderLayout.SOUTH);
    }
    
    public void updatePlayerInfo(String name, int money, Color color) {
        turnLabel.setText(name + "'s Turn");
        turnLabel.setForeground(color);
        moneyLabel.setText("$" + money);
    }
    
    public void setRollEnabled(boolean enabled) {
        btnRoll.setEnabled(enabled);
    }
    
    public void setEndTurnEnabled(boolean enabled) {
        btnEndTurn.setEnabled(enabled);
    }
}