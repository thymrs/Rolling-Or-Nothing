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
        this.state = state;
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

        switch (command) {
            case "ROLL" -> handleRollDice();
            case "BUY" -> handleBuyProperty();
            case "USE_CARD" -> handleCardAction();
            case "SURRENDER" -> {   // 🏳️ ปุ่มยอมแพ้
                boolean confirm = GameDialogManager.showSurrenderDialog(view);
                if (confirm) {
                    state.getCurrentPlayer().declareBankruptcy();
                    state.notifyMessage("🏳️ " + state.getCurrentPlayer().getName() + " ขอยอมแพ้ออกจากเกม!");
                    state.setCurrentPhase(TurnPhase.END_TURN);
                    processPhase();
                }
            }
            case "END_TURN" -> handleEndTurn();
        }
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
            players.add(new BotPlayer(currentId++, "Bot " + (i + 1), config.getInitialMoney(), config.getBotDifficulty()));
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
                // แจ้งเตือนสถานะผิดปกติ
                state.notifyMessage("❄️ " + currentPlayer.getName() + " Unable to roll the dice because you're freeze!");
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
                if (opponents.isEmpty()) {
                    view.showPopup("ไม่มีเป้าหมายให้ใช้การ์ด!");
                    return;
                }

                if (player instanceof BotPlayer bot) {
                    target = opponents.get(0); // ให้บอทสุ่มเป้าหมายคนแรกไปก่อน
                    view.showPopup("Bot " + bot.getName() + " เล็งเป้าไปที่ " + target.getName() + "!");
                } else {
                    // ใช้หน้าต่างจากทีม UI
                    target = GameDialogManager.showAttackCardDialog(view, card, opponents);
                }

                // ถ้ากดยกเลิกการเลือกเป้าหมาย ให้ Return ออกไปเลย การ์ดจะไม่หาย!
                if (target == null) {
                    return; 
                }
            }

            player.useCard(); 
            card.applyEffect(player, target, state);
            state.getDeck().discard(card); // ทิ้งการ์ดลงกอง

            if (target != null) {
                state.notifyMessage(player.getName() + " use card on " + target.getName() + "!");
            } else {
                state.notifyMessage(player.getName() + " use an item card!");
            }

            view.showPopup("successfully used card!");
            view.updateView(state);
        } else {
            view.showPopup("your hand is empty!");
        }
    }

    private void handleRollDice() {
        Player player = state.getCurrentPlayer();
        state.getDice().roll();
        int steps = state.getDice().getTotal();

        state.notifyMessage("🎲 " + player.getName() + " rolls for " + steps);

        int oldPos = player.getPosition();
        int newPos = state.getBoard().getNextIndex(oldPos, steps);
        player.setPosition(newPos);

        state.notifyMessage("🏃 " + player.getName() + " move to " + newPos);

        if (newPos < oldPos) {
            state.getBank().paySalary(player, 15000);
            view.showPopup(player.getName() + " Pass the Start! Receive 15000!");
            state.notifyMessage("💰 " + player.getName() + " Receive salary for 15000");
        }

        Tile currentTile = state.getBoard().getTile(newPos);

        if (currentTile instanceof ChanceTile chanceTile) {
            Card c = chanceTile.drawCard(state);
            if (c != null) {
                CardType type = c.getType();
                if (type == CardType.ANGEL || type == CardType.SHIELD ||
                        type == CardType.DISCOUNT || type == CardType.ESCAPE) {
                    if (player instanceof BotPlayer) {
                        player.receiveCard(c);
                        view.showPopup("🤖 " + player.getName() + " Got item: " + type);
                        state.setCurrentPhase(TurnPhase.END_TURN);
                    } else {
                        int choice = javax.swing.JOptionPane.showConfirmDialog(null,
                                "You got card: " + type + "\nDo you want to keep it?",
                                "You got card!", javax.swing.JOptionPane.YES_NO_OPTION);
                        if (choice == javax.swing.JOptionPane.YES_OPTION) {
                            if (player.receiveCard(c)) {
                                view.showPopup("Collect card " + type + " successfully!");
                            } else {
                                view.showPopup("inventory full! discard the card...");
                                state.getDeck().discard(c);
                            }
                        } else {
                            state.getDeck().discard(c);
                            view.showPopup("You select to discard the card " + type);
                        }
                        state.setCurrentPhase(TurnPhase.END_TURN);
                    }
                } else {
                    if (c.requiresTarget()) {
                        player.setHeldCard(c);
                        state.setCurrentPhase(TurnPhase.ACTION_REQUIRED);
                        view.showPopup("You got an Attack card! Please use and select target.");
                    } else {
                        c.applyEffect(player, null, state);
                        state.getDeck().discard(c);
                        view.showPopup("Force to use card " + type + " automatically!");
                        state.setCurrentPhase(TurnPhase.END_TURN);
                    }
                }
            } else {
                state.setCurrentPhase(TurnPhase.END_TURN);
            }
        } else {
            currentTile.onPlayerEnter(player, state);

            if (currentTile instanceof PropertyTile property) {
                // 🤖 เคสของบอท
                if (player instanceof BotPlayer bot) {
                    if (property.getOwner() == null) {
                        boolean wantToBuy = bot.makeDecision(DecisionType.BUY_LAND, property, state);
                        if (wantToBuy && bot.getMoney() >= property.getPurchasePrice()) {
                            bot.pay(property.getPurchasePrice());
                            property.setOwner(bot);
                            bot.addAsset(property);
                            state.notifyMessage(bot.getName() + " buy " + property.getName());
                            view.showPopup(bot.getName() + " buy " + property.getName() + "!");
                        }
                    } else if (property.getOwner().equals(bot) && property.getBuildingLevel() < 3) {
                        boolean wantToUpgrade = bot.makeDecision(DecisionType.UPGRADE, property, state);
                        int upgradeCost = property.getPurchasePrice();
                        if (wantToUpgrade && bot.getMoney() >= upgradeCost) {
                            bot.pay(upgradeCost);
                            property.upgradeLevel();
                            state.notifyMessage(bot.getName() + " upgrade " + property.getName());
                        }
                    } else if (!property.getOwner().equals(bot) && property.getBuildingLevel() < 3) {
                        // บอทเทคโอเวอร์
                        int takeoverPrice = property.getPurchasePrice() * 2;
                        if (bot.getMoney() >= takeoverPrice && bot.makeDecision(DecisionType.BUY_LAND, property, state)) {
                            Player owner = property.getOwner();
                            bot.pay(takeoverPrice);
                            owner.receiveMoney(takeoverPrice);
                            owner.removeAsset(property);
                            property.setOwner(bot);
                            bot.addAsset(property);
                            state.notifyMessage(bot.getName() + " takeover property of " + owner.getName() + "!");
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
                        // เทคโอเวอร์ของคน
                        Player owner = property.getOwner();
                        if (property.getBuildingLevel() < 3) {
                            int takeoverPrice = property.getPurchasePrice() * 2;
                            if (player.getMoney() >= takeoverPrice) {
                                int choice = javax.swing.JOptionPane.showConfirmDialog(null,
                                        "Do you want to takeover " + property.getName() + " of " + owner.getName() + "\nfor the price of " + takeoverPrice + " or not?",
                                        "Takeover", javax.swing.JOptionPane.YES_NO_OPTION);
                                if (choice == javax.swing.JOptionPane.YES_OPTION) {
                                    player.pay(takeoverPrice);
                                    owner.receiveMoney(takeoverPrice);
                                    owner.removeAsset(property);
                                    property.setOwner(player);
                                    player.addAsset(property);
                                    state.notifyMessage(player.getName() + " takeover " + property.getName() + "!");
                                    view.showPopup("Takeover Successfully!");
                                }
                            }
                        }
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
            // ใช้หน้าต่างจากทีม UI
            int selectedLevel = GameDialogManager.showBuyPropertyDialog(view, property);

            if (selectedLevel != -1) {
                int currentLevel = property.getBuildingLevel();
                int levelsToUpgrade = selectedLevel - currentLevel;
                int totalCost = property.getPurchasePrice() * levelsToUpgrade;

                if (player.getMoney() >= totalCost) {
                    player.pay(totalCost);
                    
                    if (property.getOwner() == null) {
                        property.setOwner(player);
                        player.addAsset(property);
                    }
                    
                    for (int i = 0; i < levelsToUpgrade; i++) {
                        property.upgradeLevel();
                    }
                    
                    state.notifyMessage(player.getName() + " buile/upgrade " + property.getName() + " to level " + selectedLevel);
                    view.showPopup("transection complete for price " + totalCost + "!");
                    state.setCurrentPhase(TurnPhase.END_TURN);
                } else {
                    view.showPopup("Not enough money! should have " + totalCost + " more!");
                }
            }
        }
        view.updateView(state);
    }

    private void handleEndTurn() {
        VictoryType vType = victoryChecker.checkWinCondition(state);

        if (null == vType) {
            state.incrementTurn();
        } else
            switch (vType) {
                case LINE_VICTORY -> { view.showPopup("Congrats! " + state.getCurrentPlayer().getName() + " you win LINE VICTORY!"); 
                    state.setCurrentPhase(TurnPhase.GAME_OVER);}
                case TRIPLE_VICTORY -> { view.showPopup("Congrats! " + state.getCurrentPlayer().getName() + " you win TRIPLE VICTORY!"); 
                    state.setCurrentPhase(TurnPhase.GAME_OVER); }
                case TOURISM_VICTORY -> { view.showPopup("Congrats! " + state.getCurrentPlayer().getName() + " you win TOURISM VICTORY!"); 
                    state.setCurrentPhase(TurnPhase.GAME_OVER); }
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
