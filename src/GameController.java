import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

/**
 * Main controller that manages game flow and coordinates between model and view
 */
public class GameController implements ActionListener {
    private GameWindow view;
    private GameState state;
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

        if (currentPhase == TurnPhase.READY_TO_ROLL) {
            if (currentPlayer.isFrozen()) {
                view.showPopup("❄️ " + currentPlayer.getName() + " ถูกแช่แข็ง! ต้องข้ามเทิร์นนี้");
                
                currentPlayer.decrementFrozenTurns(); 
                
                state.setCurrentPhase(TurnPhase.END_TURN);
                view.updateView(state);
                return;
            }
        }
        
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
            case SELECTING_DESTINATION:
                view.showPopup("✈️ คุณได้สิทธิ์ท่องเที่ยวรอบโลก! โปรดเลือกช่องที่จะไป");
                break;
        }

        view.updateView(state);
    }
    
    /**
     * Handles bot player's turn
     */
    private void handleBotTurn() {
        BotPlayer bot = (BotPlayer) state.getCurrentPlayer(); //

        new Thread(() -> {
            try {
                Thread.sleep(1000);

                if (bot.getHeldCard() != null) {
                    boolean wantToUseCard = bot.makeDecision(DecisionType.USE_CARD, null, state);
                    
                    if (wantToUseCard) {
                        handleCardAction();
                        Thread.sleep(1000);
                    }
                }

                handleRollDice(); //
                Thread.sleep(1000);

                Tile tile = state.getBoard().getTile(bot.getPosition());
                if (tile instanceof PropertyTile property && property.getOwner() == null) {
                    if (bot.makeDecision(DecisionType.BUY_LAND, property, state)) {
                        handleBuyProperty();
                    }
                }
                
                Thread.sleep(1000);
                javax.swing.SwingUtilities.invokeLater(() -> {
                    handleEndTurn(); //
                });

            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }).start(); //
    }
    
    /**
     * Handles card-related actions
     */
    private void handleCardAction() {
        Player player = state.getCurrentPlayer();
        Card card = player.getHeldCard(); //

        if (card != null) {
            Player target = null;

            if (card.requiresTarget()) {
                List<Player> opponents = getOpponents(player); //
                
                if (player instanceof BotPlayer bot) {
                    target = bot.chooseTarget(opponents, state);
                    view.showPopup("🤖 บอท " + bot.getName() + " เล็งเป้าไปที่ " + target.getName() + "!");
                } else {
                    target = view.showSelectTargetDialog(opponents);
                }

                if (target == null) {
                    return; // ยกเลิกการใช้
                }
            }
                
            player.useCard();
            card.applyEffect(player, target, state);

            state.getDeck().discard(card);

            view.showPopup("ใช้การ์ด " + card.getType() + " แล้ว!");
            view.updateView(state);
        }

    }
    
    private void handleRollDice() {
        Player player = state.getCurrentPlayer();

        state.getDice().roll();
        int steps = state.getDice().getTotal();

        int oldPos = player.getPosition();
        int newPos = state.getBoard().getNextIndex(oldPos, steps);
        player.setPosition(newPos);

        if (newPos < oldPos) {
            state.getBank().paySalary(player, 2000);
            view.showPopup(player.getName() + "เดินครบรอบ รับเงินเดือน");
        }
        Tile tile = state.getBoard().getTile(player.getPosition());
        Tile currentTile = state.getBoard().getTile(newPos);
        currentTile.onPlayerEnter(player, state);

        if (currentTile instanceof PropertyTile && ((PropertyTile) currentTile).getOwner() == null) {
            state.setCurrentPhase(TurnPhase.ACTION_REQUIRED);
        } else {
            state.setCurrentPhase(TurnPhase.END_TURN);
        }

        view.updateView(state);
    }

    private void handleBuyProperty() {
        Player player = state.getCurrentPlayer();
        Tile tile = state.getBoard().getTile(player.getPosition());

        if  (tile instanceof PropertyTile property) {
            boolean success = state.getBank().processPurchase(player, property);

            if (success) {
            view.showPopup("ซื้อที่ดิน" + property.getName() + " เรียบร้อย!");
            state.setCurrentPhase(TurnPhase.END_TURN);
            } else {
                view.showPopup("เงินไม่พอ");
            }
        }
        view.updateView(state);
    }

    private void handleEndTurn() {
        VictoryType vType = victoryChecker.checkWinCondition(state);
        
        if (vType == VictoryType.LINE_VICTORY) {
            view.showPopup("🎉 ยินดีด้วย! " + state.getCurrentPlayer().getName() + " ชนะแบบ LINE VICTORY!");
            state.setCurrentPhase(TurnPhase.GAME_OVER);
        } else if (vType == VictoryType.TRIPLE_VICTORY) {
            view.showPopup("🎉 ยินดีด้วย! " + state.getCurrentPlayer().getName() + " ชนะแบบ TRIPLE VICTORY!");
            state.setCurrentPhase(TurnPhase.GAME_OVER);
        } else if (vType == VictoryType.TOURISM_VICTORY) {
            view.showPopup("🎉 ยินดีด้วย! " + state.getCurrentPlayer().getName() + " ชนะแบบ TOURISM VICTORY!");
            state.setCurrentPhase(TurnPhase.GAME_OVER);
        } else {
            state.incrementTurn();
        }
        processPhase();
    }

    /**
     * Processes financial transactions
     */
    private void processTransaction(Player payer, Player receiver, int amount) {
        boolean paid = payer.pay(amount);

        if (paid) {
            if (receiver != null) {
                receiver.receiveMoney(amount);
            }
            view.showPopup(payer.getName() + " จ่ายเงิน" + amount + " ให้ " + (receiver != null ? receiver.getName() : "กองกลาง"));
        } else {
            view.showPopup("เงินไม่พอจ่าย! เลือกขายสินทรัพย์หรือล้มละลาย");
            payer.declareBankruptcy();
            victoryChecker.checkWinCondition(state);
        }
    }

    private List<Player> getOpponents(Player currentPlayer) {
        List<Player> opponents = new ArrayList<>();
        for (Player p : state.getPlayers()) {
            if (p != currentPlayer && !p.isBankrupt()) {
                opponents.add(p);
            }
        }
        return opponents;
    }
}
