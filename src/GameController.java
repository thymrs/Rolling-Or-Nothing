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
        this.view.setTileActionListener(this);
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
        // view.showPopup("Welcome");

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
        if (command != null && command.startsWith("TILE_")) {
            int tileIndex = Integer.parseInt(command.substring(5)); // ดึงเอาเฉพาะตัวเลขออกมา
            handleTileClick(tileIndex);
            return; // จบการทำงาน ไม่ต้องลงไปเข้า switch
        }

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

        switch (currentPhase) {
            case GAME_OVER -> {
                System.out.println("▶ [DEBUG] game over! enable button false");
                view.getControlPanel().setButtonsEnabled(false); 
                view.setRollEnabled(false);
                view.showPopup("End Game! Winner " + victoryChecker.getWinner(state));
                return;
            }
            case READY_TO_ROLL -> {

                // 1. ลดเวลา EXPO
                for (PropertyTile land : currentPlayer.getOwnedLands()) {
                    land.decreaseExpoTurn();
                }

                // ระบบเช็คคุก
                if (currentPlayer.getJailTurnCount() > 0) {
                    currentPlayer.decreaseJailTurn(); // ลดจำนวนตา

                    // แจ้งเตือนตามประเภทผู้เล่น
                    if (currentPlayer instanceof BotPlayer) {
                        state.notifyMessage("👮 Bot " + currentPlayer.getName() + " is in jail! ("
                                + currentPlayer.getJailTurnCount() + " turns remaining...)");
                    } else {
                        view.showPopup("Opps! you are in jail, " + currentPlayer.getJailTurnCount() + " turns left");
                    }

                    // ถ้าตาติดคุกหมดแล้ว เอาออก
                    if (currentPlayer.getJailTurnCount() <= 0) {
                        currentPlayer.setIsJailed(false);
                    }

                    // สั่งข้ามเทิร์นทันที
                    state.setCurrentPhase(TurnPhase.END_TURN);
                    processPhase();
                    return;
                }

                // ถ้าไม่ติดคุก ก็ให้เล่นตามปกติ
                if (currentPlayer instanceof BotPlayer) {
                    view.getControlPanel().setButtonsEnabled(false);
                    view.setRollEnabled(false);
                    state.notifyMessage(currentPlayer.getName() + " is playing...");
                    handleBotTurn();
                } else {
                    view.getControlPanel().setButtonsEnabled(true);
                    view.setRollEnabled(true);
                    view.showPopup("It's now your turn " + currentPlayer.getName());
                }
            }
            case ACTION_REQUIRED -> {
                // เช็คว่าบอทไม่มีอะไรให้ทำแล้ว
                if (currentPlayer instanceof BotPlayer) {
                    endTurnLogic(); // บอทจบเทิร์นเลย
                } else {
                    // สำหรับ human player ให้เช็คว่าจริง ๆ มีอะไรให้ทำหรือไม่
                    Tile currentTile = state.getBoard().getTile(currentPlayer.getPosition());

                    boolean hasAction = false;
                    if (currentTile instanceof PropertyTile property) {
                        if (property.getOwner() == null) {
                            // มีที่ดินที่ยังไม่มี owner สามารถซื้อได้
                            hasAction = true;
                        } else if (property.getOwner().equals(currentPlayer) && property.getBuildingLevel() < 3) {
                            // เป็นที่ของตัวเอง และ level < 3 สามารถอัปเกรดได้
                            hasAction = true;
                        } else if (!property.getOwner().equals(currentPlayer) && property.getBuildingLevel() < 3) {
                            // ของคนอื่น และ level < 3 สามารถเทคโอเวอร์ได้
                            hasAction = true;
                        }
                    }

                    if (!hasAction) {
                        // ไม่มี action ให้ทำ จบเทิร์นเลย
                        state.setCurrentPhase(TurnPhase.END_TURN);
                        processPhase();
                    }
                    // ถ้า hasAction = true รอให้ผู้เล่นกดปุ่ม
                }
            }
            case MOVING -> {
                // โหมด MOVING
                // จะถูกใช้ในกรณีที่มีกำหนดที่หมายปลายทายหรือเอฟเฟกต์ที่ทำให้ผู้เล่นต้องเลือกที่จะย้ายไปยังตำแหน่งอื่นบนบอร์ด
                // (เช่น ROLL WORLD TOUR)
                // ในโหมดนี้ ผู้เล่นจะไม่สามารถกดปุ่ม ROLL ได้ แต่จะเกิดการเรียก highlightTile()
                // เพื่อโชว์ตำแหน่งที่กำลังจะไป และทำซ้ำ การเดิน จนกว่าจะถึงตำแหน่งที่เลือก
                // และเร่งความเร็วการกระโดดไป เริ่มจาก 0.5 เข้าใกล้ 0.2 กระโดดสามครั้ง ถึง 12
                // ครั้ง หรือถ้าผู้เล่นเป็นบอท จะมีการตัดสินใจเลือกตำแหน่งปลายทางโดยอัตโนมัติ
                // และเรียก handleWorldTourFlight() เพื่อย้ายไปยังตำแหน่งนั้นทันที
                // หลังจากที่ผู้เล่นย้ายไปยังตำแหน่งที่เลือกแล้ว จะมีการเรียก onPlayerEnter()
                // ของ Tile นั้นๆ เพื่อให้เกิดผลกระทบตามประเภทของ Tile และจบเทิร์นด้วยการเปลี่ยน
                // Phase ไปที่ END_TURN

            }
            case END_TURN -> {
                state.incrementTurn();
                processPhase();
            }
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
                Thread.sleep(3000);

                if (bot.getHeldCard() != null) {
                    boolean wantToUseCard = bot.makeDecision(DecisionType.USE_CARD, null, state);

                    if (wantToUseCard) {
                        handleCardAction();
                        Thread.sleep(3000);
                    }
                }

                handleRollDice(); //
                Thread.sleep(3000);

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
        boolean isDouble = state.getDice().isDouble();

        state.notifyMessage("🎲 " + player.getName() + " rolls for " + steps);
        GameDialogManager.showDiceRollDialog(view, state.getDice().getDie1(), state.getDice().getDie2());

        int currentPos = player.getPosition();
        int targetPos = (currentPos + steps) % 32;
        // int oldPos = player.getPosition();
        // int newPos = state.getBoard().getNextIndex(oldPos, steps);
        // player.setPosition(newPos);
        startMovement(player.getId(), currentPos, targetPos);

        state.notifyMessage("🏃 " + player.getName() + " move to " + targetPos);

        // เช็คการทอยเบิ้ล
        if (isDouble) {
            view.setRollEnabled(isDouble);
            player.incrementDoubleRollCount();
        } else {
            player.resetDoubleRollCount(); // ถ้าไม่เบิ้ล ให้รีเซ็ตเลย
        }
        if (player.getDoubleRollCount() >= 3) {
            state.notifyMessage("🚔 Got 3 Double in a roll! " + player.getName() + " Jailed immedietely!");
            player.setIsJailed(true);
            player.addJailTurnCount(3);
            player.setPosition(8);
            player.resetDoubleRollCount(); // ล้างค่าเบิ้ล

            view.updateView(state);
            state.setCurrentPhase(TurnPhase.END_TURN); // จบเทิร์นทันที
            processPhase();
            return;
        }

        // เช็คว่าผ่านจุดเริ่มต้นหรือไม่ (ถ้า targetPos < currentPos แปลว่าผ่านแน่นอน)
        if (targetPos < currentPos) {
            state.getBank().paySalary(player, 5000);
            view.showPopup(player.getName() + " Pass the Start! Receive 5000!");
            state.notifyMessage("💰 " + player.getName() + " Receive salary for 5000");
        }

        // หลังจากได้ตำแหน่งปลายทางแล้ว ให้เรียก onPlayerEnter() ของ Tile นั้นๆ
        // เพื่อให้เกิดผลกระทบตามประเภทของ Tile
        Tile currentTile = state.getBoard().getTile(targetPos);

        // เรียกใช้ onPlayerEnter() ของ Tile ที่ผู้เล่นเดินไปถึง
        // เพื่อให้เกิดผลกระทบตามประเภทของ Tile นั้นๆ
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
            int beforeMoney = player.getMoney();
            int activeDiscount = player.getDiscountRate();

            int afterMoney = player.getMoney();

            if (beforeMoney > afterMoney) {
                int lost = beforeMoney - afterMoney;

                // ตรวจสอบว่าเสียเงิน และตอนแรกมีส่วนลดอยู่ แปลว่าส่วนลดทำงานแล้ว
                if (activeDiscount > 0) {
                    String msg = "🎟️ " + player.getName() + " use discount card " + activeDiscount
                            + "%! final amount is " + lost + "!";
                    if (player instanceof BotPlayer) {
                        state.notifyMessage(msg);
                    } else {
                        view.showPopup(msg);
                        state.notifyMessage(msg);
                    }
                } else {
                    String msg = "💸 " + player.getName() + " lost money " + lost + "amount!";
                    if (player instanceof BotPlayer) {
                        state.notifyMessage(msg);
                    } else {
                        view.showPopup("You paid / lost " + lost + "!");
                    }
                }
            } else if (afterMoney > beforeMoney) {
                int gained = afterMoney - beforeMoney;
                String msg = "🎉 " + player.getName() + " receive money " + gained + "amount!";
                if (player instanceof BotPlayer) {
                    state.notifyMessage(msg);
                } else {
                    view.showPopup("You received " + gained + "!");
                }
            }

            if (currentTile instanceof PropertyTile property) {
                // เคสของบอท
                if (player instanceof BotPlayer bot) {
                    
                    // ที่ดินเปล่าตกครั้งแรกให้ซื้อมากสุดได้แค่เวล 3
                    if (property.getOwner() == null) {
                        boolean wantToBuy = bot.makeDecision(DecisionType.BUY_LAND, property, state);
                        if (wantToBuy && bot.getMoney() >= property.getPurchasePrice()) {
                            bot.pay(property.getPurchasePrice());
                            property.setOwner(bot);
                            bot.addAsset(property);

                            while (property.getBuildingLevel() < 3) {
                                int upgradeCost = property.getUpgradeCost(1);
                                boolean wantToUpgrade = bot.makeDecision(DecisionType.UPGRADE, property, state);
                                
                                if (wantToUpgrade && bot.getMoney() - upgradeCost >= 500) { 
                                    bot.pay(upgradeCost);
                                    property.upgradeLevel(); 
                                } else {
                                    break; 
                                }
                            }

                            if (property.getBuildingLevel() > 0) {
                                state.notifyMessage("🏗️ " + bot.getName() + " buy and upgrade " + property.getName() 
                                        + " to level " + property.getBuildingLevel() + "!");
                                view.showPopup(bot.getName() + " bought & upgraded " + property.getName() + "!");
                            } else {
                                state.notifyMessage(bot.getName() + " buy " + property.getName());
                                view.showPopup(bot.getName() + " buy " + property.getName() + "!");
                            }
                        }
                    } 
                    
                    // ตกที่ดินตัวเอง อัปเป็นแลนด์มาร์กได้
                    else if (property.getOwner().equals(bot) && property.getBuildingLevel() < 4) {
                        System.out.println("▶ [DEBUG] Bot is on its land, deciding on upgrade...");
                        boolean wantToUpgrade = bot.makeDecision(DecisionType.UPGRADE, property, state);

                        if (wantToUpgrade) {
                            int currentLevel = property.getBuildingLevel();
                            int maxPossibleUpgrades = 4 - currentLevel; 
                            
                            int targetUpgradeLevels = 0;
                            int finalCost = 0;

                            for (int i = maxPossibleUpgrades; i >= 1; i--) {
                                int cost = property.getUpgradeCost(i);
                                if (bot.getMoney() - cost >= 500) {
                                    targetUpgradeLevels = i;
                                    finalCost = cost;
                                    break; // พอเจอเลเวลที่จ่ายไหวจะหยุด
                                }
                            }

                            // ถ้ามีเงินพออัปเกรดอย่างน้อย 1 ขั้น
                            if (targetUpgradeLevels > 0) {
                                bot.pay(finalCost);

                                // วนลูปอัปเกรดตามจำนวนขั้นที่บอทจ่ายเงินไป
                                for (int i = 0; i < targetUpgradeLevels; i++) {
                                    property.upgradeLevel();
                                }

                                // เช็คว่าเป็นแลนด์มาร์กไหม
                                if (property.getBuildingLevel() == 4) {
                                    state.notifyMessage("🏰 " + bot.getName() + " built a LANDMARK at " + property.getName() + "!");
                                } else {
                                    state.notifyMessage("🏗️ " + bot.getName() + " upgrade " + property.getName() 
                                            + " to reach level " + property.getBuildingLevel() + "!");
                                }
                            } else {
                                System.out.println("▶ [DEBUG] Bot want to upgrade but didn't have enough money (or scared of losing all money)");
                            }
                        }
                    

                    } else if (!property.getOwner().equals(bot) && property.getBuildingLevel() < 3) {
                        int takeoverPrice = property.getTotalValue() * 2;
                        System.out.println("▶ [DEBUG] Bot " + bot.getName() + " is on " + property.getName());
                        System.out.println("▶ [DEBUG] current Bot money: " + bot.getMoney() + " | takeover price: "
                                + takeoverPrice);

                        boolean wantToTakeover = bot.makeDecision(DecisionType.BUY_LAND, property, state);

                        System.out.println(
                                "▶ [DEBUG] Does Bot want to buy? (Roll System/Calculate Money): " + wantToTakeover);
                        if (wantToTakeover && bot.getMoney() >= takeoverPrice) {
                            Player owner = property.getOwner();
                            bot.pay(takeoverPrice);
                            owner.receiveMoney(takeoverPrice);
                            owner.removeAsset(property);
                            property.setOwner(bot);
                            bot.addAsset(property);
                            state.notifyMessage(
                                    "😈 " + bot.getName() + " takeover the property of " + owner.getName() + "!");
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
                        // เทคโอเวอร์ของคน
                        Player owner = property.getOwner();
                        if (property.getBuildingLevel() < 3) {
                            int takeoverPrice = property.getTotalValue() * 2;
                            if (player.getMoney() >= takeoverPrice) {
                                int choice = javax.swing.JOptionPane.showConfirmDialog(null,
                                        "Do you want to takeover " + property.getName() + " of " + owner.getName()
                                                + "\nfor the price of " + takeoverPrice + " or not?",
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
                            } else {
                                view.showPopup("You don't have enough money to takeover! Need: " + takeoverPrice);
                            }
                        } else {
                            view.showPopup("Cannot takeover! This property is fully upgraded (Level 3).");
                        }
                        state.setCurrentPhase(TurnPhase.END_TURN);
                    }
                }
            } else if (currentTile instanceof ActionTile actionTile) {
                if (actionTile.getType() == ActionType.WORLD_TRAVEL) {
                    if (player instanceof BotPlayer bot) {
                        System.out.println("▶ [DEBUG] Bot is on World Tour! Selecting target...");

                        int targetTileIndex = bot.chooseWorldTourDestination(state);

                        handleWorldTourFlight(targetTileIndex);
                    } else {
                        // ให้เลือกช่องได้เหมือน FESTIVAL
                        view.showPopup(
                                "✈️ You landed on WORLD TOUR!\nPlease click on a tile on the board to fly there.");
                        state.setCurrentPhase(TurnPhase.SELECTING_DESTINATION);
                    }
                } else {
                    // Action อื่นๆ (JAIL, TAX, START) ให้ทำงานตามปกติ
                    actionTile.onPlayerEnter(player, state);
                    state.setCurrentPhase(TurnPhase.END_TURN);
                    processPhase();
                }
            } else if (currentTile instanceof SpecialTile specialTile) {
                if (specialTile.getEffect() == EffectType.FESTIVAL) {
                    if (player instanceof BotPlayer bot) {
                        System.out.println("▶ [DEBUG] Bot move to tile FESTIVAL (EXPO) selecting...");
                        if (!bot.getOwnedLands().isEmpty()) {
                            PropertyTile firstLand = bot.getOwnedLands().get(0);
                            handleExpoSelection(firstLand);
                        } else {
                            state.setCurrentPhase(TurnPhase.END_TURN);
                        }
                    } else {
                        // --- แก้ไขโค้ดของคนเล่นตรงนี้ ---
                        if (player.getOwnedLands().isEmpty()) {
                            view.showPopup("You don't have any property to host the Festival.");
                            state.setCurrentPhase(TurnPhase.END_TURN);
                        } else {
                            // แจ้งให้ผู้เล่นทราบ แล้วเปลี่ยน Phase ของเกมไปรอรับการคลิก
                            view.showPopup(
                                    "You landed on FESTIVAL!\nPlease click on your property on the board to host the event.");
                            state.setCurrentPhase(TurnPhase.SELECTING_DESTINATION);
                        }
                        // --------------------------------
                    }
                } else {
                    specialTile.onPlayerEnter(player, state);
                    state.setCurrentPhase(TurnPhase.END_TURN);
                }
            } else {
                state.setCurrentPhase(TurnPhase.END_TURN);
            }
        }
        view.updateView(state);
    }

    public void startMovement(int playerId, int currentPos, int targetPos) {
        // 1. เปลี่ยน State ล็อกเกมไว้ที่ MOVING
        state.setCurrentPhase(TurnPhase.MOVING);

        // 2. ปิดปุ่มลูกเต๋าใน UI กันผู้เล่นกดซ้ำ
        view.setRollEnabled(false);

        // 3. สร้างเส้นทางการเดิน
        List<Integer> path = new ArrayList<>();
        int totalTiles = 32;

        int tempPos = currentPos;
        // จะลูปเพิ่มทีละช่องจนกว่าจะถึงเป้าหมาย
        while (tempPos != targetPos) {
            tempPos = (tempPos + 1) % totalTiles;
            path.add(tempPos);
        }

        // 4. สั่ง BoardPanel ให้เริ่ม Animate พร้อมตั้ง Callback เมื่อจบ
        view.boardPanel.animatePlayerMovement(playerId, path, () -> {
            System.out.println("▶ [DEBUG] Movement animation finished for player " + playerId + " to position " + targetPos);
            onMovementFinished(playerId, targetPos); // เมื่อ UI ขยับเสร็จ ให้เรียก Method นี้
        });
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

                        state.notifyMessage(player.getName() + " build/upgrade " + property.getName() + " to level "
                                + selectedLevel);
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

    /**
     * Core logic for ending a turn - checks win condition and advances turn
     */
    private void endTurnLogic() {
        VictoryType vType = victoryChecker.checkWinCondition(state);

        if (null == vType) {
            state.incrementTurn();
        } else
            switch (vType) {
                case LINE_VICTORY -> {
                    view.showPopup("Congrats! " + state.getCurrentPlayer().getName() + " you win LINE VICTORY!");
                    state.setCurrentPhase(TurnPhase.GAME_OVER);
                }
                case TRIPLE_VICTORY -> {
                    view.showPopup("Congrats! " + state.getCurrentPlayer().getName() + " you win TRIPLE VICTORY!");
                    state.setCurrentPhase(TurnPhase.GAME_OVER);
                }
                case TOURISM_VICTORY -> {
                    view.showPopup("Congrats! " + state.getCurrentPlayer().getName() + " you win TOURISM VICTORY!");
                    state.setCurrentPhase(TurnPhase.GAME_OVER);
                }
                default -> state.incrementTurn();
            }
        processPhase();
    }

    /**
     * Handles end turn button click from UI
     */
    private void handleEndTurn() {
        endTurnLogic();
    }

    public void handleExpoSelection(PropertyTile selectedProperty) {
        Player currentPlayer = state.getCurrentPlayer();

        if (selectedProperty != null) {
            selectedProperty.setExpo(2, 3);

            state.notifyMessage("🎪 " + currentPlayer.getName() + " holds Expo at " + selectedProperty.getName()
                    + "! (Rent increase!)");
            view.showPopup("Expo successfully hosted at " + selectedProperty.getName() + "!");
        } else {
            // ดักไว้เผื่อคนเล่นกดยกเลิก
            view.showPopup("No property selected for Expo.");
        }

        // จบเทิร์น
        state.setCurrentPhase(TurnPhase.END_TURN);
        processPhase(); // รันเทิร์นต่อไปทันที
    }

    public void handleWorldTourFlight(int targetTileId) {
        Player player = state.getCurrentPlayer();
        int oldPos = player.getPosition();

        startMovement(player.getId(), oldPos, targetTileId);
        state.notifyMessage("✈️ " + player.getName() + " fly to target " + targetTileId + "!");

        if (targetTileId < oldPos) {
            state.getBank().paySalary(player, 5000);
            view.showPopup(player.getName() + " Pass the Start! Receive 5000!");
            state.notifyMessage("💰 " + player.getName() + " Receive salary for 5000");
        }

        Tile currentTile = state.getBoard().getTile(targetTileId);
        currentTile.onPlayerEnter(player, state);

        if (currentTile instanceof PropertyTile property) {

            // กรณี: บอทเป็นคนบินมาตก
            if (player instanceof BotPlayer bot) {
                if (property.getOwner() == null) {
                    // บอทซื้อที่ดิน
                    boolean wantToBuy = bot.makeDecision(DecisionType.BUY_LAND, property, state);
                    if (wantToBuy && bot.getMoney() >= property.getPurchasePrice()) {
                        bot.pay(property.getPurchasePrice());
                        property.setOwner(bot);
                        bot.addAsset(property);
                        state.notifyMessage("🤖 " + bot.getName() + " buy land " + property.getName());
                    }
                } else if (!property.getOwner().equals(bot) && property.getBuildingLevel() < 3) {
                    // บอทเทคโอเวอร์
                    int takeoverPrice = property.getTotalValue() * 2;
                    boolean wantToTakeover = bot.makeDecision(DecisionType.BUY_LAND, property, state);
                    if (wantToTakeover && bot.getMoney() >= takeoverPrice) {
                        Player owner = property.getOwner();
                        bot.pay(takeoverPrice);
                        owner.receiveMoney(takeoverPrice);
                        owner.removeAsset(property);
                        property.setOwner(bot);
                        bot.addAsset(property);
                        state.notifyMessage("😈 " + bot.getName() + " takeover " + property.getName());
                    }
                }
                // บอทบินเสร็จ ตัดจบเทิร์นเลย ไม่ต้องไป ACTION_REQUIRED
                state.setCurrentPhase(TurnPhase.END_TURN);
            }

            // กรณี: คนเล่น (มนุษย์) เป็นคนบินมาตก
            else {
                if (property.getOwner() == null) {
                    state.setCurrentPhase(TurnPhase.ACTION_REQUIRED); // รอให้คนกดปุ่มซื้อ
                } else if (property.getOwner().equals(player)) {
                    if (property.getBuildingLevel() < 3) {
                        state.setCurrentPhase(TurnPhase.ACTION_REQUIRED); // รอให้คนกดปุ่มอัปเกรด
                    } else {
                        state.setCurrentPhase(TurnPhase.END_TURN);
                    }
                } else {
                    if (property.getBuildingLevel() < 3) {
                        int takeoverPrice = property.getTotalValue() * 2;
                        if (player.getMoney() >= takeoverPrice) {
                            int choice = javax.swing.JOptionPane.showConfirmDialog(null,
                                    "Do you want to takeover " + property.getName() + " of "
                                            + property.getOwner().getName() + "\nfor the price of " + takeoverPrice
                                            + "?",
                                    "Takeover", javax.swing.JOptionPane.YES_NO_OPTION);
                            if (choice == javax.swing.JOptionPane.YES_OPTION) {
                            }
                        }
                    }
                    state.setCurrentPhase(TurnPhase.END_TURN);
                }
            }
        } else {
            state.setCurrentPhase(TurnPhase.END_TURN);
        }

        view.updateView(state);
        if (state.getCurrentPhase() == TurnPhase.END_TURN) {
            processPhase();
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

    private void handleTileClick(int tileIndex) {
        Player player = state.getCurrentPlayer();
        if (player instanceof BotPlayer)
            return; // บอทไม่ต้องสนใจการคลิกนี้

        // ตรวจสอบว่า ตอนนี้เกมกำลังรอให้ผู้เล่นคลิกกระดานอยู่หรือเปล่า
        if (state.getCurrentPhase() == TurnPhase.SELECTING_DESTINATION) {
            Tile currentTile = state.getBoard().getTile(player.getPosition());

            // กรณี 1: ผู้เล่นยืนอยู่ที่ช่อง FESTIVAL
            if (currentTile instanceof SpecialTile specialTile && specialTile.getEffect() == EffectType.FESTIVAL) {
                Tile clickedTile = state.getBoard().getTile(tileIndex);

                if (clickedTile instanceof PropertyTile property) {
                    // ตรวจสอบว่าเป็นที่ดินตัวเองหรือไม่
                    if (property.getOwner() != null && property.getOwner().equals(player)) {
                        handleExpoSelection(property); // จัดงานได้เลย!
                    } else {
                        view.showPopup("▶ Please select your OWN property!");
                    }
                } else {
                    view.showPopup("▶ You cannot host Festival here!");
                }
            }
            // กรณี 2: ผู้เล่นยืนอยู่ช่อง WORLD TRAVEL
            // (ได้ฟังก์ชันคลิกกระดานเพื่อบินรอบโลกแถมไปด้วยเลย!)
            else if (currentTile instanceof ActionTile actionTile && actionTile.getType() == ActionType.WORLD_TRAVEL) {
                handleWorldTourFlight(tileIndex);
            }
        }
    }

    // ฟังก์ชันนี้จะถูกเรียกเมื่อการเดินของผู้เล่นเสร็จสมบูรณ์แล้ว (หลังจาก Animation)
    private void onMovementFinished(int playerId, int finalPos) {
        Player currentPlayer = state.getCurrentPlayer();

        // 1. เซ็ตค่าตำแหน่งจริงลงใน Data (Model)
        currentPlayer.setPosition(finalPos);

        // 2. ดึงข้อมูล Tile ที่ตกลงไป
        Tile targetTile = state.getBoard().getTile(finalPos);

        // 3. เรียก Effect ของช่องให้ทำงาน (onPlayerEnter)
        targetTile.onPlayerEnter(currentPlayer, state);

        // *หมายเหตุ* // การเปลี่ยนเป็น Phase.END_TURN ควรเกิดขึ้นภายใน
        targetTile.onPlayerEnter(currentPlayer, state);
        state.setCurrentPhase(TurnPhase.END_TURN);
        view.updateView(state);
        processPhase();
    }

}