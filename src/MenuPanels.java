import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeListener;
import java.awt.*;

public class MenuPanels {

    // --- 1. Main Menu Panel (Static) ---
    public static class MainMenuPanel extends JPanel {
        public MainMenuPanel(PageNavigator navigator) {
            setBackground(GameTheme.BG_COLOR);
            setLayout(new GridBagLayout());
            GridBagConstraints gbc = new GridBagConstraints();

            gbc.gridx = 0;
            gbc.gridy = 0;
            gbc.insets = new Insets(0, 0, 20, 0);
            add(new GameUIComponents.TwoDicePanel(), gbc);

            JLabel titleLabel = new JLabel("RollingOrNothing");
            titleLabel.setFont(new Font(GameTheme.THAI_FONT, Font.BOLD, 80));
            titleLabel.setForeground(GameTheme.WOOD_COLOR);
            gbc.gridy = 1;
            gbc.insets = new Insets(0, 0, 40, 0);
            add(titleLabel, gbc);

            gbc.fill = GridBagConstraints.HORIZONTAL;
            gbc.insets = new Insets(10, 0, 10, 0);

            addButton("Start Game", e -> navigator.navigateTo("Setup"), gbc, 2);
            addButton("Settings", e -> navigator.navigateTo("Settings"), gbc, 3);
            addButton("How to Play", e -> navigator.navigateTo("HowToPlay"), gbc, 4);
            addButton("Exit", e -> System.exit(0), gbc, 5);
        }

        private void addButton(String text, java.awt.event.ActionListener action, GridBagConstraints gbc, int gridy) {
            GameUIComponents.WoodButton btn = new GameUIComponents.WoodButton(text);
            btn.addActionListener(action);
            gbc.gridy = gridy;
            add(btn, gbc);
        }
    }

    // --- 2. Setup Panel (Static) ---
    public static class SetupPanel extends JPanel {
        private JSpinner humanSpinner, botSpinner;
        private JComboBox<String> difficultySelector;
        private PageNavigator navigator;

        public SetupPanel(PageNavigator navigator) {
            this.navigator = navigator;
            setLayout(new BorderLayout());
            setBackground(GameTheme.BG_COLOR);

            add(createTopBar("Game Setup", navigator), BorderLayout.NORTH);
            add(createConfigPanel(), BorderLayout.CENTER);

            JPanel bottom = new JPanel(new FlowLayout(FlowLayout.CENTER));
            bottom.setBackground(GameTheme.BG_COLOR);
            bottom.setBorder(BorderFactory.createEmptyBorder(0, 0, 30, 0));

            GameUIComponents.CircularButton btnRoll = new GameUIComponents.CircularButton("Let's Roll!");
            btnRoll.addActionListener(e -> startGame());
            bottom.add(btnRoll);
            add(bottom, BorderLayout.SOUTH);
        }

        private JPanel createConfigPanel() {
            JPanel center = new JPanel(new GridBagLayout());
            center.setOpaque(false);
            JPanel group = createGroupPanel("Lobby Configuration");
            group.setPreferredSize(new Dimension(500, 300));

            group.add(createLabel("Human Players:"));
            humanSpinner = new JSpinner(new SpinnerNumberModel(1, 1, 4, 1));
            styleSpinner(humanSpinner);
            group.add(humanSpinner);

            group.add(createLabel("Bot Opponents:"));
            botSpinner = new JSpinner(new SpinnerNumberModel(1, 0, 3, 1));
            styleSpinner(botSpinner);
            group.add(botSpinner);

            ChangeListener limit = e -> {
                int h = (int) humanSpinner.getValue(), b = (int) botSpinner.getValue();
                if (h + b > 4) {
                    if (e.getSource() == humanSpinner)
                        botSpinner.setValue(4 - h);
                    else
                        humanSpinner.setValue(Math.max(1, 4 - b));
                }
            };
            humanSpinner.addChangeListener(limit);
            botSpinner.addChangeListener(limit);

            group.add(createLabel("Bot Difficulty:"));
            difficultySelector = new JComboBox<>(new String[] { "Random", "Easy", "Normal", "Hard" });
            styleComboBox(difficultySelector);
            difficultySelector.setSelectedIndex(2);
            group.add(difficultySelector);

            center.add(group);
            return center;
        }

        private void startGame() {
            navigator.startGame((int) humanSpinner.getValue(), (int) botSpinner.getValue(),
                    (String) difficultySelector.getSelectedItem());
        }

        // Helpers
        private void styleSpinner(JSpinner s) {
            if (s.getEditor() instanceof JSpinner.DefaultEditor) {
                JTextField tf = ((JSpinner.DefaultEditor) s.getEditor()).getTextField();
                tf.setHorizontalAlignment(JTextField.CENTER);
                tf.setFont(new Font(GameTheme.THAI_FONT, Font.BOLD, 18));
                tf.setBackground(new Color(255, 248, 220));
            }
        }

        private void styleComboBox(JComboBox box) {
            box.setBackground(new Color(255, 248, 220));
            box.setFont(new Font(GameTheme.THAI_FONT, Font.PLAIN, 16));
        }
    }

    // --- 3. Settings Panel (Static) ---
    public static class SettingsPanel extends JPanel {
        public SettingsPanel(PageNavigator navigator) {
            setLayout(new BorderLayout());
            setBackground(GameTheme.BG_COLOR);

            add(createTopBar("Settings", navigator), BorderLayout.NORTH);

            JPanel content = new JPanel(new GridBagLayout());
            content.setOpaque(false);
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.insets = new Insets(10, 10, 10, 10);
            gbc.fill = GridBagConstraints.HORIZONTAL;

            JPanel audioPanel = createGroupPanel("Audio");
            audioPanel.add(createLabel("Master Volume:"));
            JSlider slider = new JSlider(0, 100, 50);
            slider.setBackground(GameTheme.BG_COLOR);
            audioPanel.add(slider);
            gbc.gridx = 0;
            gbc.gridy = 0;
            content.add(audioPanel, gbc);

            JPanel gamePanel = createGroupPanel("Gameplay");
            gamePanel.add(createLabel("Speed:"));
            JComboBox<String> speedBox = new JComboBox<>(new String[] { "Slow", "Normal", "Fast" });
            speedBox.setBackground(new Color(255, 248, 220));
            speedBox.setFont(new Font(GameTheme.THAI_FONT, Font.PLAIN, 16));
            gamePanel.add(speedBox);
            gbc.gridy = 1;
            content.add(gamePanel, gbc);

            add(content, BorderLayout.CENTER);
        }
    }

    // --- Shared Helpers ---
    private static JPanel createTopBar(String titleStr, PageNavigator nav) {
        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(GameTheme.BG_COLOR);
        p.setBorder(BorderFactory.createEmptyBorder(20, 20, 0, 20));
        GameUIComponents.BackButton back = new GameUIComponents.BackButton();
        back.addActionListener(e -> nav.navigateTo("Menu"));

        JLabel title = new JLabel(titleStr, SwingConstants.CENTER);
        title.setFont(new Font(GameTheme.THAI_FONT, Font.BOLD, 50));
        title.setForeground(GameTheme.WOOD_COLOR);

        JPanel l = new JPanel(new FlowLayout(FlowLayout.LEFT));
        l.setOpaque(false);
        l.add(back);
        p.add(l, BorderLayout.WEST);
        p.add(title, BorderLayout.CENTER);
        JPanel r = new JPanel();
        r.setPreferredSize(back.getPreferredSize());
        r.setOpaque(false);
        p.add(r, BorderLayout.EAST);
        return p;
    }

    private static JPanel createGroupPanel(String t) {
        JPanel p = new JPanel(new GridLayout(0, 2, 10, 15));
        p.setBackground(GameTheme.BG_COLOR);
        TitledBorder b = BorderFactory.createTitledBorder(BorderFactory.createLineBorder(GameTheme.WOOD_COLOR), t);
        b.setTitleColor(GameTheme.WOOD_COLOR);
        b.setTitleFont(new Font(GameTheme.THAI_FONT, Font.BOLD, 18));
        p.setBorder(BorderFactory.createCompoundBorder(b, BorderFactory.createEmptyBorder(15, 15, 15, 15)));
        return p;
    }

    private static JLabel createLabel(String t) {
        JLabel l = new JLabel(t);
        l.setForeground(GameTheme.TEXT_COLOR);
        l.setFont(new Font("SansSerif", Font.PLAIN, 18));
        return l;
    }
}