import javax.swing.*;
import java.awt.*;

public class GameWindow extends JFrame {
    private static final String TITLE = "RollingOrNothing - The Board Game";
    private static final int WIDTH = 1600;
    private static final int HEIGHT = 900;
    private static final String SCREEN_MENU = "MENU";
    private static final String SCREEN_GAME = "GAME";

    private JPanel mainContainer;
    private CardLayout cardLayout;
    private GameMenuSystem menuSystem;
    private JPanel gameplayPanel;
    private BoardPanel boardPanel;
    private ControlPanel controlPanel;

    public static void main(String[] args) {
        GameTheme.setupGameFont();
        SwingUtilities.invokeLater(() -> new GameWindow().setVisible(true));
    }

    public GameWindow() {
        configureWindow();
        initMainContainer();
    }

    private void configureWindow() {
        setTitle(TITLE);
        setSize(WIDTH, HEIGHT);
        setResizable(false);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }

    private void initMainContainer() {
        cardLayout = new CardLayout();
        mainContainer = new JPanel(cardLayout);

        initializeMenu();
        initializeGameplay();

        add(mainContainer);
        showScreen(SCREEN_MENU);
    }

    private void initializeMenu() {
        menuSystem = new GameMenuSystem(this);
        mainContainer.add(menuSystem.getMainPanel(), SCREEN_MENU);
    }

    private void initializeGameplay() {
        gameplayPanel = new JPanel(new BorderLayout());
        boardPanel = new BoardPanel();
        controlPanel = new ControlPanel();
        gameplayPanel.add(boardPanel, BorderLayout.CENTER);
        gameplayPanel.add(controlPanel, BorderLayout.EAST);
        mainContainer.add(gameplayPanel, SCREEN_GAME);
    }

    private void showScreen(String name) {
        cardLayout.show(mainContainer, name);
    }

    public void startGame(int human, int bot, String diff) {
        System.out.println(String.format("Starting: H=%d, B=%d, D=%s", human, bot, diff));
        // TODO: Pass config to BoardPanel/Controller
        showScreen(SCREEN_GAME);
    }

    public void returnToMenu() {
        showScreen(SCREEN_MENU);
    }
}