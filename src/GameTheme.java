import javax.swing.*;
import java.awt.*;

public class GameMenuSystem implements PageNavigator {

    private JPanel mainContainer;
    private CardLayout cardLayout;
    private GameWindow gameWindow;

    public GameMenuSystem(GameWindow gameWindow) {
        this.gameWindow = gameWindow;
        createInterface();
    }

    public JPanel getMainPanel() {
        return mainContainer;
    }

    private void createInterface() {
        cardLayout = new CardLayout();
        mainContainer = new JPanel(cardLayout);

        mainContainer.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(GameTheme.WOOD_BORDER, 5),
                BorderFactory.createMatteBorder(20, 20, 20, 20, GameTheme.WOOD_COLOR)));

        // แก้ไข: เรียกใช้ Static Inner Class จาก MenuPanels อย่างถูกต้อง
        mainContainer.add(new MenuPanels.MainMenuPanel(this), "Menu");
        mainContainer.add(new MenuPanels.SetupPanel(this), "Setup");
        mainContainer.add(new MenuPanels.SettingsPanel(this), "Settings");
        mainContainer.add(new HowToPlayPanel(this), "HowToPlay");

        navigateTo("Menu");
    }

    @Override
    public void navigateTo(String pageName) {
        cardLayout.show(mainContainer, pageName);
    }

    @Override
    public void startGame(int human, int bot, String diff) {
        gameWindow.startGame(human, bot, diff);
    }
}