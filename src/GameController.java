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
        this.view.setTileSelectionListener(this);
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
        //view.showPopup("Welcome");

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
            case "SURRENDER" -> { 
                boolean confirm = GameDialogManager.showSurrenderDialog(view);
                if (confirm) {
                    state.getCurrentPlayer().declareBankruptcy();
                    state.notifyMessage("🏳️ " + state.getCurrentPlayer().getName() + " Surrender and leave the game!");
                    state.setCurrentPhase(TurnPhase.END_TURN);
                    processPhase();
                }
            }
            case "END_TURN" -> handleEndTurn();
            default -> {
                // Handle tile selection
                if (command.startsWith("TILE_")) {
                    handleTileSelection(command);
                }
            }
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
        
        // Check if player is selecting a tile (Festival or Travel)
        // BOT AUTONOMOUS SELECTION: Completely bypass UI for bots
        if (state.isSelectingTile()) {
            String mode = state.getSelectionMode();
            
            if (currentPlayer instanceof BotPlayer bot) {
                // Bot auto-selects without showing UI
                new Thread(() -> {
                    try {
                        Thread.sleep(800); // Simulate thinking time
                        
                        int selectedTile = -1;
                        if ("FESTIVAL".equals(mode)) {
                            PropertyTile bestProperty = bot.chooseFestivalLocation(state);
                            if (bestProperty != null) {
                                selectedTile = bestProperty.getIndex();
                                state.notifyMessage("🤖 " + bot.getName() + " places Festival on " + bestProperty.getName());
                            }
                        } else if ("TRAVEL".equals(mode)) {
                            selectedTile = bot.getRandom().nextInt(32);
                            Tile destTile = state.getBoard().getTile(selectedTile);
                            state.notifyMessage("🤖 " + bot.getName() + " travels to " + destTile.getName());
                        }
                        
                        if (selectedTile >= 0) {
                            handleTileSelection("TILE_" + selectedTile);
                        }
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }).start();
                return;
            }
            
            // Human player selection UI
            if ("FESTIVAL".equals(mode)) {
                AutoDismissPopup.showInfo(view, "Festival", "🎉 Festival mode! Select a property to double its rent for next visitor");
            } else if ("TRAVEL".equals(mode)) {
                AutoDismissPopup.showInfo(view, "Travel", "✈️ World Travel mode! Select a tile to travel to");
            }
            view.enableTileSelection(true);
            view.setRollEnabled(false);
            return;
        }

        if (currentPhase == TurnPhase.READY_TO_ROLL) {
            if (currentPlayer.isFrozen()) {
                AutoDismissPopup.showWarning(view, "Frozen", "❄️ " + currentPlayer.getName() + " Freeze! skip the turns...");
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
                if (currentPlayer.getJailTurnCount() > 0) {
                    currentPlayer.decreaseJailTurn();
                    AutoDismissPopup.showWarning(view, "Jail", "👮 " + currentPlayer.getName() + " ติดคุกอยู่! (เหลืออีก " + currentPlayer.getJailTurnCount() + " ตา)");
                    
                    state.setCurrentPhase(TurnPhase.END_TURN);
                    processPhase();
                    return;
                }
                if (currentPlayer instanceof BotPlayer) {
                    view.getControlPanel().setButtonsEnabled(false);
                    state.notifyMessage("🤖 ถึงตาของบอท " + currentPlayer.getName() + " กำลังตัดสินใจ...");
                    handleBotTurn();
                } else {
                    view.setRollEnabled(true);
                    AutoDismissPopup.showInfo(view, "Turn", "It's now your turn " + currentPlayer.getName());
                }
            }
            case MOVING -> {
            }
            case END_TURN -> {
                state.incrementTurn();
                processPhase();
            }
            case GAME_OVER -> {
                VictoryType vType = victoryChecker.getLastWinCondition();
                Player winner = victoryChecker.getWinner(state);
                view.showGameOver(vType, winner);
            }
            case SELECTING_DESTINATION -> AutoDismissPopup.showInfo(view, "Travel", "You're on a World Tour! Please select your destination");
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
                Thread.sleep(2000);

                if (bot.getHeldCard() != null) {
                    boolean wantToUseCard = bot.makeDecision(DecisionType.USE_CARD, null, state);

                    if (wantToUseCard) {
                        handleCardAction();
                        Thread.sleep(2000);
                    }
                }

                handleRollDice(); //
                Thread.sleep(2000);

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
        Card card = player.getHeldCard(); 

        if (card != null) {
            Player target = null;

            if (card.requiresTarget()) {
                List<Player> opponents = getOpponents(player); 
                if (opponents.isEmpty()) {
                    view.showPopup("No target to use card!");
                    return;
                }

                if (player instanceof BotPlayer bot) {
                    target = opponents.get(0); // ให้บอทสุ่มเป้าหมายคนแรกไปก่อน
                    view.showPopup("Bot " + bot.getName() + " target to " + target.getName() + "!");
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
        view.setRollEnabled(false);
        Player player = state.getCurrentPlayer();
        state.getDice().roll();
        int steps = state.getDice().getTotal();
        boolean isDoubles = state.getDice().getDie1() == state.getDice().getDie2();

        state.notifyMessage("🎲 " + player.getName() + " rolls for " + steps);
        GameDialogManager.showDiceRollDialog(view, state.getDice().getDie1(), state.getDice().getDie2());

        int oldPos = player.getPosition();
        int newPos = state.getBoard().getNextIndex(oldPos, steps);
        player.setPosition(newPos);

        state.notifyMessage("🏃 " + player.getName() + " move to " + newPos);

        if (newPos < oldPos) {
            state.getBank().paySalary(player, 15000);
            AutoDismissPopup.showSuccess(view, "Salary", player.getName() + " Pass the Start! Receive 15000!");
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
                        AutoDismissPopup.showInfo(view, "Card", "🤖 " + player.getName() + " Got item: " + type);
                        state.setCurrentPhase(TurnPhase.END_TURN);
                    } else {
                        int choice = javax.swing.JOptionPane.showConfirmDialog(null,
                                "You got card: " + type + "\nDo you want to keep it?",
                                "You got card!", javax.swing.JOptionPane.YES_NO_OPTION);
                        if (choice == javax.swing.JOptionPane.YES_OPTION) {
                            if (player.receiveCard(c)) {
                                AutoDismissPopup.showSuccess(view, "Card", "Collect card " + type + " successfully!");
                            } else {
                                AutoDismissPopup.showWarning(view, "Card", "inventory full! discard the card...");
                                state.getDeck().discard(c);
                            }
                        } else {
                            state.getDeck().discard(c);
                            AutoDismissPopup.showInfo(view, "Card", "You select to discard the card " + type);
                        }
                        state.setCurrentPhase(TurnPhase.END_TURN);
                    }
                } else {
                    if (c.requiresTarget()) {
                        player.setHeldCard(c);
                        state.setCurrentPhase(TurnPhase.ACTION_REQUIRED);
                        AutoDismissPopup.showInfo(view, "Card", "You got an Attack card! Please use and select target.");
                    } else {
                        c.applyEffect(player, null, state);
                        state.getDeck().discard(c);
                        AutoDismissPopup.showInfo(view, "Card", "Force to use card " + type + " automatically!");
                        state.setCurrentPhase(TurnPhase.END_TURN);
                    }
                }
            } else {
                state.setCurrentPhase(TurnPhase.END_TURN);
            }
        } else {
            currentTile.onPlayerEnter(player, state);

            if (currentTile instanceof PropertyTile property) {
                // เคสของบอท
                if (player instanceof BotPlayer bot) {
                    if (property.getOwner() == null) {
                        boolean wantToBuy = bot.makeDecision(DecisionType.BUY_LAND, property, state);
                        if (wantToBuy && bot.getMoney() >= property.getPurchasePrice()) {
                            bot.pay(property.getPurchasePrice());
                            property.setOwner(bot);
                            bot.addAsset(property);
                            state.notifyMessage(bot.getName() + " buy " + property.getName());
                            AutoDismissPopup.showInfo(view, "Purchase", bot.getName() + " buy " + property.getName() + "!");
                        }
                    } else if (property.getOwner().equals(bot) && property.getBuildingLevel() < 3) {
                        System.out.println("▶ [DEBUG] บอทตกที่ตัวเอง กำลังตัดสินใจอัปเกรด...");
                        boolean wantToUpgrade = bot.makeDecision(DecisionType.UPGRADE, property, state);
                        
                        if (wantToUpgrade) {
                            int currentLevel = property.getBuildingLevel();
                            int maxPossibleUpgrades = 3 - currentLevel;
                            int targetUpgradeLevels = 0;
                            int finalCost = 0;

                            for (int i = maxPossibleUpgrades; i >= 1; i--) {
                                int cost = property.getUpgradeCost(i);
                                if (bot.getMoney() - cost >= 500) { 
                                    targetUpgradeLevels = i;
                                    finalCost = cost;
                                    break;
                                }
                            }

                            if (targetUpgradeLevels > 0) {
                                bot.pay(finalCost);
                                for (int i = 0; i < targetUpgradeLevels; i++) {
                                    property.upgradeLevel();
                                }
                                state.notifyMessage("🏗️ 🤖 " + bot.getName() + " upgrade " + property.getName() + " to reach " + targetUpgradeLevels + " level!");
                            }
                        }
                        
                    } else if (!property.getOwner().equals(bot) && property.getBuildingLevel() < 3) {
                        int takeoverPrice = property.getTotalValue() * 2;
                        System.out.println("▶ [DEBUG] Bot " + bot.getName() + " is on " + property.getName());
                        System.out.println("▶ [DEBUG] current Bot money: " + bot.getMoney() + " | takeover price: " + takeoverPrice);
                        
                        boolean wantToTakeover = bot.makeDecision(DecisionType.BUY_LAND, property, state);
                        System.out.println("▶ [DEBUG] Does Bot want to buy? (Roll System/Calculate Money): " + wantToTakeover);
                        if (wantToTakeover && bot.getMoney() >= takeoverPrice) {
                            Player owner = property.getOwner();
                            bot.pay(takeoverPrice);
                            owner.receiveMoney(takeoverPrice);
                            owner.removeAsset(property);
                            property.setOwner(bot);
                            bot.addAsset(property);
                            state.notifyMessage("😈 🤖 " + bot.getName() + " takeover the property of " + owner.getName() + "!");
                        }
                    }
                    state.setCurrentPhase(TurnPhase.END_TURN);
                
                // เคสของคนเล่น
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
                        Player owner = property.getOwner();
                        if (property.getBuildingLevel() < 3) {
                            int takeoverPrice = property.getTotalValue() * 2;
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
                                    AutoDismissPopup.showSuccess(view, "Takeover", "Takeover Successfully!");
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
        
        // DOUBLE ROLL FIX: Check if rolled doubles
        if (isDoubles && state.getCurrentPhase() == TurnPhase.END_TURN) {
            state.notifyMessage("🎲 " + player.getName() + " rolled doubles! Extra roll!");
            
            // For human players, re-enable ROLL button for extra roll
            if (!(player instanceof BotPlayer)) {
                view.setRollEnabled(true);
                state.setCurrentPhase(TurnPhase.READY_TO_ROLL);
            } else {
                // Bot will roll automatically in next processPhase
                state.setCurrentPhase(TurnPhase.READY_TO_ROLL);
            }
        }
        
        view.updateView(state);
    }

    private void handleBuyProperty() {
        Player player = state.getCurrentPlayer();
        Tile currentTile = state.getBoard().getTile(player.getPosition());

        if (currentTile instanceof PropertyTile property) {

            int selectedLevel = GameDialogManager.showBuyPropertyDialog(view, property);

            if (selectedLevel != -1) {
                int currentLevel = property.getBuildingLevel();
                int levelsToUpgrade = selectedLevel - currentLevel;
                int totalCost = property.getUpgradeCost(levelsToUpgrade);

                if (player.getMoney() >= totalCost) {

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
    }

    private void handleEndTurn() {
        VictoryType vType = victoryChecker.checkWinCondition(state);

        if (vType == null || vType == VictoryType.NONE) {
            state.incrementTurn();
        } else {
            switch (vType) {
                case LINE_MONOPOLY -> {
                    AutoDismissPopup.showSuccess(view, "Victory!", "🎉 Congrats! " + state.getCurrentPlayer().getName() + " you win LINE MONOPOLY!");
                    state.setCurrentPhase(TurnPhase.GAME_OVER);
                }
                case TRIPLE_MONOPOLY -> {
                    AutoDismissPopup.showSuccess(view, "Victory!", "🎉 Congrats! " + state.getCurrentPlayer().getName() + " you win TRIPLE MONOPOLY!");
                    state.setCurrentPhase(TurnPhase.GAME_OVER);
                }
                case TOURISM_MONOPOLY -> {
                    AutoDismissPopup.showSuccess(view, "Victory!", "🎉 Congrats! " + state.getCurrentPlayer().getName() + " you win TOURISM MONOPOLY!");
                    state.setCurrentPhase(TurnPhase.GAME_OVER);
                }
                case LAST_PLAYER_STANDING -> {
                    AutoDismissPopup.showSuccess(view, "Victory!", "🎉 " + state.getCurrentPlayer().getName() + " is the LAST PLAYER STANDING!");
                    state.setCurrentPhase(TurnPhase.GAME_OVER);
                }
                default -> state.incrementTurn();
            }
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
    
    private void handleTileSelection(String command) {
        if (!state.isSelectingTile()) return;
        
        // Extract tile index from command (e.g., "TILE_5" -> 5)
        int tileIndex = Integer.parseInt(command.substring(5));
        Player currentPlayer = state.getCurrentPlayer();
        Board board = state.getBoard();
        
        if (board == null) return;
        
        String mode = state.getSelectionMode();
        
        if ("FESTIVAL".equals(mode)) {
            // Double rent on selected property
            Tile selectedTile = board.getTile(tileIndex);
            if (selectedTile instanceof PropertyTile property) {
                property.setDoubleRent(true);
                state.notifyMessage("🎉 " + currentPlayer.getName() + " activated Festival effect on " + property.getName() + "! Rent is now DOUBLED!");
            } else {
                state.notifyMessage("❌ You can only double rent on properties!");
                return; // Don't exit selection mode
            }
        } else if ("TRAVEL".equals(mode)) {
            // Travel to selected tile
            currentPlayer.setPosition(tileIndex);
            Tile destinationTile = board.getTile(tileIndex);
            state.notifyMessage("✈️ " + currentPlayer.getName() + " flew to " + destinationTile.getName() + "!");
            
            // Trigger tile effect
            if (destinationTile != null) {
                destinationTile.onPlayerEnter(currentPlayer, state);
            }
        }
        
        // Exit selection mode and continue
        state.setSelectingTile(false, "");
        view.enableTileSelection(false);
        view.updateView(state);
        processPhase();
    }
}
