import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

/**
 * Main controller that manages game flow and coordinates between model and view
 */
public class GameController implements ActionListener {
    private final GameWindow view;
    private final GameState state;
    private VictoryChecker victoryChecker;
    private MapLoader mapLoader;

    /**
     * Constructor for GameController
     */
    public GameController(GameWindow view, GameState state) {
        this.view = view;
        this.state = state; // <--- ใช้ State ตัวที่ส่งเข้ามาแทน
        this.mapLoader = new MapLoader();
        this.victoryChecker = state.getVictoryChecker();
        this.view.setActionListener(this);
    }

    /**
     * Starts a new game with given configuration
     * 
     * @param config Game configuration settings
     */
    public void startGame(GameConfig config) {
        System.out.println("Game Starting...");
        initGame(config);

        view.updateView(state);
        view.showPopup("Welcome");

        processPhase();
    }

    /**
     * Handles action events from UI
     * 
     * @param event The action event
     */
    @Override
    public void actionPerformed(ActionEvent event) {
        String command = event.getActionCommand();
        TurnDisplayPanel.updateTurn(state.getTurnCount());
        System.out.println("User pressed: " + command);

        switch (command) {
            case "ROLL" -> handleRollDice();
            case "BUY" -> handleBuyProperty();
            case "END_TURN" -> handleEndTurn();
            case "USE_CARD" -> handleCardAction();
        }

        processPhase();
    }

    /**
     * Initializes the game state
     */
    private void initGame(GameConfig config) {
        List<Player> players = new ArrayList<>();
        int currentId = 0;

        for (int i = 0; i < config.getHumanCount(); i++) {
            players.add(new HumanPlayer(currentId++, "Player " + (i + 1), config.getInitialMoney()));
        }

        for (int i = 0; i < config.getBotCount(); i++) {
            players.add(
                    new BotPlayer(currentId++, "Bot " + (i + 1), config.getInitialMoney(), config.getBotDifficulty()));
        }

        this.victoryChecker = new VictoryChecker();

        state.setPlayers(players);
        state.setBoard(mapLoader.loadMap(config.getMapName()));

        view.updateView(state);
    }

    /**
     * Processes the current turn phase
     */
    private void processPhase() {
        Player currentPlayer = state.getCurrentPlayer();
        TurnPhase currentPhase = state.getCurrentPhase();

        if (currentPhase == TurnPhase.READY_TO_ROLL) {
            if (currentPlayer.isFrozen()) {
                view.showPopup("❄️ " + currentPlayer.getName() + " Freeze! skip the turns...");
                state.notifyMessage(
                        "❄️ " + currentPlayer.getName() + " Unable to roll the dice because you're freeze!");
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
            case READY_TO_ROLL -> {
                view.getControlPanel().setButtonsEnabled(true);
                view.showPopup("It's now your turn " + currentPlayer.getName());
            }
            case MOVING -> {
            }
            // case ACTION_REQUIRED -> view.getControlPanel().setButtonEnabled(false, true,
            // true, true);
            case END_TURN -> {
                state.incrementTurn();
                processPhase();
            }
            case GAME_OVER -> view.showPopup("End Game! Winner " + victoryChecker.getWinner(state));
            case SELECTING_DESTINATION -> view.showPopup("You're on a Wolrd Tour! Please select your destination");
        }

        view.updateView(state);
    }

    /**
     * Handles bot player's turn
     */
    private void handleBotTurn() {
        BotPlayer bot = (BotPlayer) state.getCurrentPlayer();

        new Thread(() -> {
            try {
                Thread.sleep(2500);

                if (bot.getHeldCard() != null) {
                    boolean wantToUseCard = bot.makeDecision(DecisionType.USE_CARD, null, state);

                    if (wantToUseCard) {
                        handleCardAction();
                        Thread.sleep(2500);
                    }
                }

                handleRollDice(); //
                javax.swing.SwingUtilities.invokeLater(() -> {
                    view.updateView(state);
                });
                Thread.sleep(2500);

                Tile tile = state.getBoard().getTile(bot.getPosition());
                if (tile instanceof PropertyTile property && property.getOwner() == null) {
                    if (bot.makeDecision(DecisionType.BUY_LAND, property, state)) {
                        handleBuyProperty();
                    }
                }

                Thread.sleep(2500);
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
        Card card = player.getHeldCard();

        if (card != null) {
            Player target = null;

            if (card.requiresTarget()) {
                List<Player> opponents = getOpponents(player);

                if (player instanceof BotPlayer bot) {
                    target = bot.chooseTarget(opponents, state);
                    if (target != null) {
                        view.showPopup("Bot " + bot.getName() + " select " + target.getName() + "!");
                    }
                } else {
                    target = view.showSelectTargetDialog(opponents);
                }

                if (target == null) {
                    return;
                }
            }

            player.useCard();
            card.applyEffect(player, target, state);
            state.getDeck().discard(card);

            if (target != null) {
                state.notifyMessage(player.getName() + " use card " + card.getType() + " on " + target.getName() + "!");
            } else {
                state.notifyMessage(player.getName() + " use card " + card.getType() + "!");
            }

            view.showPopup("use card " + card.getType() + " !");
            view.updateView(state);
        }
    }

    private void handleRollDice() {
        Player player = state.getCurrentPlayer();

        state.getDice().roll();
        int steps = state.getDice().getTotal();

        // แจ้งเตือนการทอยเต๋า
        state.notifyMessage(player.getName() + " roll for " + steps);

        int oldPos = player.getPosition();
        int newPos = state.getBoard().getNextIndex(oldPos, steps);
        player.setPosition(newPos);

        // แจ้งเตือนการเดินของผู้เล่น
        state.notifyMessage(player.getName() + " move to " + newPos);

        if (newPos < oldPos) {
            state.getBank().paySalary(player, 2000);
            view.showPopup(player.getName() + " reach the start get salary for 2000!");
            state.notifyMessage(player.getName() + " reach the start receive money 2000");
        }

        Tile currentTile = state.getBoard().getTile(newPos);

        if (currentTile instanceof ChanceTile chanceTile) {
            Card c = chanceTile.drawCard(state);

            if (c != null) {
                CardType type = c.getType(); //

                if (type == CardType.ANGEL || type == CardType.SHIELD ||
                        type == CardType.DISCOUNT || type == CardType.ESCAPE) {

                    if (player instanceof BotPlayer) {
                        player.receiveCard(c); //
                        view.showPopup(player.getName() + " got item: " + type);
                        state.setCurrentPhase(TurnPhase.END_TURN);
                    } else {
                        int choice = javax.swing.JOptionPane.showConfirmDialog(null,
                                "you got a: " + type + "\ndo you want to keep it?",
                                "you got a card!", javax.swing.JOptionPane.YES_NO_OPTION);

                        if (choice == javax.swing.JOptionPane.YES_OPTION) {
                            if (player.receiveCard(c)) { //
                                view.showPopup("keep the card" + type + " successfully!");
                            } else {
                                view.showPopup("inventory full! discard the card!");
                                state.getDeck().discard(c);
                            }
                        } else {
                            state.getDeck().discard(c);
                            view.showPopup("you select to discard " + type);
                        }
                        state.setCurrentPhase(TurnPhase.END_TURN);
                    }
                } else {
                    if (c.requiresTarget()) {
                        player.setHeldCard(c); //
                        state.setCurrentPhase(TurnPhase.ACTION_REQUIRED);
                        view.showPopup("you got an attack card! Please use and select target.");
                    } else {
                        c.applyEffect(player, null, state); //
                        state.getDeck().discard(c);
                        view.showPopup("force to use " + type + " automatically!");
                        state.setCurrentPhase(TurnPhase.END_TURN);
                    }
                }
            } else {
                state.setCurrentPhase(TurnPhase.END_TURN); // กองการ์ดหมด
            }
        } else {
            currentTile.onPlayerEnter(player, state);

            if (currentTile instanceof PropertyTile property) {

                // เคสของบอท
                if (player instanceof BotPlayer bot) {
                    if (property.getOwner() == null) {
                        // บอทตัดสินใจว่าจะซื้อที่ดินไหม
                        boolean wantToBuy = bot.makeDecision(DecisionType.BUY_LAND, property, state);
                        if (wantToBuy && bot.getMoney() >= property.getPurchasePrice()) {
                            bot.pay(property.getPurchasePrice());
                            property.setOwner(bot);
                            bot.addAsset(property);
                            state.notifyMessage(bot.getName() + " decide to buy " + property.getName());
                            view.showPopup(bot.getName() + " buy " + property.getName() + " !");
                        }
                    } else if (property.getOwner().equals(bot) && property.getBuildingLevel() < 3) {
                        // บอทตัดสินใจอัปเกรดบ้าน
                        boolean wantToUpgrade = bot.makeDecision(DecisionType.UPGRADE, property, state);
                        int upgradeCost = property.getPurchasePrice();

                        if (wantToUpgrade && bot.getMoney() >= upgradeCost) {
                            bot.pay(upgradeCost);
                            property.upgradeLevel();

                            state.notifyMessage(bot.getName() + " upgrade " + property.getName() + " to level "
                                    + property.getBuildingLevel());
                            view.showPopup(bot.getName() + " upgrade " + property.getName() + " successfully!");
                        }
                    }

                    state.setCurrentPhase(TurnPhase.END_TURN);

                    // 👤 เคสของคนเล่น
                } else {
                    if (property.getOwner() == null) {
                        state.setCurrentPhase(TurnPhase.ACTION_REQUIRED);
                    } else if (property.getOwner().equals(player)) {
                        if (property.getBuildingLevel() < 3) {
                            state.setCurrentPhase(TurnPhase.ACTION_REQUIRED);
                        } else {
                            state.setCurrentPhase(TurnPhase.END_TURN);
                        }
                    } else {
                        state.setCurrentPhase(TurnPhase.END_TURN);
                    }
                }
            } else {
                state.setCurrentPhase(TurnPhase.END_TURN);
            }
        }
        view.updateView(state);
    }

    private void handleBuyProperty() {
        Player player = state.getCurrentPlayer();
        Tile currentTile = state.getBoard().getTile(player.getPosition());

        if (currentTile instanceof PropertyTile property) {
            int price = property.getPurchasePrice();

            if (property.getOwner() == null) {
                if (player.getMoney() >= price) {
                    player.pay(price);
                    property.setOwner(player);
                    player.addAsset(property);

                    state.notifyMessage(player.getName() + " buy " + property.getName());
                    view.showPopup("buy " + property.getName() + " successfully!");
                    state.setCurrentPhase(TurnPhase.END_TURN); // ซื้อเสร็จ จบเทิร์น
                } else {
                    state.notifyMessage(player.getName() + " not enough money to buy " + property.getName());
                    view.showPopup("not enough money to buy this property!");
                }

            } else if (property.getOwner().equals(player)) {
                if (property.getBuildingLevel() < 3) {
                    if (player.getMoney() >= price) {
                        player.pay(price);
                        property.upgradeLevel();

                        state.notifyMessage(player.getName() + " upgrade " + property.getName() + " to level "
                                + property.getBuildingLevel());
                        view.showPopup("upgrade " + property.getName() + " successfully!");
                        state.setCurrentPhase(TurnPhase.END_TURN); // อัปเกรดเสร็จ จบเทิร์น
                    } else {
                        state.notifyMessage(player.getName() + " not enough money to upgrade " + property.getName());
                        view.showPopup("not enough money to upgrade!");
                    }
                } else {
                    view.showPopup("this property is fully upgraded! (max 3)");
                    state.setCurrentPhase(TurnPhase.END_TURN);
                }
            }
        }

        // อัปเดตหน้าจอให้เงินลด และวาดบ้านเพิ่ม
        view.updateView(state);
    }

    private void handleEndTurn() {
        VictoryType vType = victoryChecker.checkWinCondition(state);

        if (null == vType) {
            state.incrementTurn();
        } else
            switch (vType) {
                case LINE_VICTORY -> {
                    view.showPopup("Congrats! " + state.getCurrentPlayer().getName() + " win LINE VICTORY!");
                    state.setCurrentPhase(TurnPhase.GAME_OVER);
                }
                case TRIPLE_VICTORY -> {
                    view.showPopup("Congrats! " + state.getCurrentPlayer().getName() + " win TRIPLE VICTORY!");
                    state.setCurrentPhase(TurnPhase.GAME_OVER);
                }
                case TOURISM_VICTORY -> {
                    view.showPopup("Congrats! " + state.getCurrentPlayer().getName() + " win TOURISM VICTORY!");
                    state.setCurrentPhase(TurnPhase.GAME_OVER);
                }
                default -> state.incrementTurn();
            }
        processPhase();
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
