package RollingOrNothing.src.controller;

package controller;

import model.GameState;
import model.utilities.GameConfig;
import model.utilities.MapLoader;
import model.utilities.VictoryChecker;
import model.entities.board.Board;
import model.entities.player.Player;
import model.entities.player.HumanPlayer;
import model.entities.player.BotPlayer;
import view.GameWindow;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

import RollingOrNothing.src.view.GameWindow;

import RollingOrNothing.src.model.cores.GameConfig;

import RollingOrNothing.src.model.cores.GameState;

import RollingOrNothing.src.model.entities.board.Board;

import java.util.ArrayList;

import RollingOrNothing.src.enums.TurnPhase;

import RollingOrNothing.src.model.entities.player.BotPlayer;

/**
 * Main controller that manages game flow and coordinates between model and view
 */
public class GameController implements ActionListener {
    private GameWindow view;
    private model.cores.GameState state;
    private VictoryChecker victoryChecker;
    private MapLoader mapLoader;
    
    /**
     * Constructor for GameController
     */
    public GameController(GameWindow view) {
        // TODO: Initialize components (finished)
        this.view = view;

        this.mapLoader = new MapLoader();
        this.victoryChecker = new VictoryChecker();

        this.view.setActionListener(this);
    }
    
    /**
     * Starts a new game with given configuration
     * @param config Game configuration settings
     */
    public void startGame(GameConfig config) {
        // TODO: Initialize game with config (finished)
        System.out.println("Game Starting...");
        initGame(config);

        view.updateView(state);
        view.showPopup("Welcome");

        processPhase();
    }
    
    /**
     * Handles action events from UI
     * @param event The action event
     */
    @Override
    public void actionPerformed(ActionEvent event) {
        // TODO: Route actions to appropriate handlers (finished)
        String command = event.getActionCommand();

        System.out.println("User pressed: " + command);
    
        switch (command) {
            case "ROLL":
                handleRollDice();
                break;
            case "BUY":
                handleBuyProperty();
                break;
            case "END_TURN":
                handleEndTurn();
                break;
            case "USE_CARD":
                handleCardAction();
                break;
        }

        processPhase();
    }
    
    /**
     * Initializes the game state
     */
    private void initGame(GameConfig config) {
        // TODO: Set up board, players, and initial state (finished)
        this.state = new GameState();

        Board board = mapLoader.loadMap(config.getMapName());
        state.setBoard(board);

        List<Player> players = new ArrayList<>();
        int startMoney = config.getInitialMoney();

        for (int i = 0; i < config.getHumanCount(); i++) {
            players.add(new HumanPlayer("Player " + (i + 1), startMoney));
        }

        for (int i = 0; i < config.getBotCount(); i++) {
            players.add(new BotPlayer("Bot " + (i + 1), startMoney, config.getBotDifficulty()));
        }

        state.setPlayers(players);

        System.out.println("สร้างผู้เล่นเสร็จสิ้น: " + players.size() + " คน");
    }
    
    /**
     * Processes the current turn phase
     */
    private void processPhase() {
        // TODO: Handle current phase logic
        Player currentPlayer = state.getCurrentPlayer();
        TurnPhase currentPhase = state.getCurrentPhase();

        if (currentPlayer instanceof BotPlayer) {
            handleBotTurn();
            return;
        }

        switch (currentPhase) {
            case READY_TO_ROLL:
                view.getControlPanel().setButtonsEnabled(true, false, false, true);
                view.showPopup("ตาของคุณแล้ว " + currentPlayer.getName());
                break;
            case MOVING:
                break;
            case ACTION_REQUIRED:
                view.getControlPanel().setButtonEnabled(false, true, true, true);
                break;
            case END_TURN:
                state.incrementTurn();
                processPhase();
                break;
            case GAME_OVER:
                view.showPopup("จบเกม! ผู้ชนะคือ " + victoryChecker.getWinner(state));
                break;
        }

        view.updateView(state);
    }
    
    /**
     * Handles bot player's turn
     */
    private void handleBotTurn() {
        // TODO: Execute bot AI decisions
    }
    
    /**
     * Handles card-related actions
     */
    private void handleCardAction() {
        // TODO: Process card usage
    }
    
    private void handleRollDice() {

    }

    private void handleBuyProperty() {

    }

    private void handleEndTurn() {

    }

    /**
     * Processes financial transactions
     */
    private void processTransaction() {
        // TODO: Handle money transfers
    }
}
