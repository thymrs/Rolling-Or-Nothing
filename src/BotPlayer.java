import java.util.List;
import java.util.Random;

public class BotPlayer extends Player {
    private final DifficultyLevel difficulty;
    private Random random = new Random();

    public BotPlayer(int id, String name, int initialMoney, DifficultyLevel difficulty) {
        super(id, name, initialMoney);
        this.difficulty = difficulty;
    }

    private boolean shouldPerformAction(){
        int chance = random.nextInt(100);
        int threshold = 0;

        switch(this.difficulty){
            case EASY -> threshold = 30;
            case NORMAL -> threshold = 60;
            case HARD -> threshold = 90;
        }

        return chance < threshold;
    }

    private boolean chance(int percentage){
        return random.nextInt(100) < percentage;
    }

    @Override
    public boolean makeDecision(DecisionType type, PropertyTile currentTile, GameState state) {
        return switch (this.difficulty) {
            case EASY -> handleEasyDecision(type, currentTile, state);
            case NORMAL -> handleNormalDecision(type, currentTile, state);
            case HARD -> handleHardDecision(type, currentTile, state);
            default -> false;
        };
    }

    public boolean handleEasyDecision(DecisionType type, PropertyTile tile, GameState state){
        return switch (type) {
            case BUY_LAND -> evaluateEasyBuyLand(tile);
            case USE_CARD -> evaluateEasyUseCard();
            case PAY_TOLL -> true;
            case SURRENDER -> this.getMoney() < 0;
            default -> false;
        };
    }

    public boolean handleNormalDecision(DecisionType type, PropertyTile tile, GameState state) {
    int salary = state.getConfig().getPassGoSalary();
    int initialMoney = state.getConfig().getInitialMoney();

    return switch (type) {
        case BUY_LAND -> evaluateNormalBuyLand(tile, state);
        case USE_CARD -> evaluateNormalUseCard(state);
        case UPGRADE -> (this.getMoney() > salary * 2) && chance(60); // ต้องมีเงินมากกว่า 2 เท่าของเงินเดือนถึงจะอัปเกรด
        case PAY_TOLL -> true;
        case SURRENDER -> this.getMoney() < 0;
        default -> false;
    };
}

    public boolean handleHardDecision(DecisionType type, PropertyTile tile, GameState state){
        switch(type){
            case BUY_LAND -> {
                return evaluateHardBuyLand(tile, state);
            }
            case USE_CARD -> {
                return evaluateHardUseCard(state);
            }
            case UPGRADE -> {
                int maxRent = state.getBoard().getMaxRentOnBoard();
                return (this.getMoney() > maxRent) && chance(90);
            }
            case PAY_TOLL -> {
                return true;
            }
            case SURRENDER -> {
                return this.getMoney() < -500;
            }
            default -> {
                return false;
            }
        }
    }

    private boolean evaluateEasyBuyLand(PropertyTile tile) {
        boolean isTakeover = (tile.getOwner() != null);
        int price = isTakeover ? tile.getTotalValue() * 2 : tile.getPurchasePrice();

        if (this.getMoney() < price) return false;

        if (!isTakeover) return true;
        
        return shouldPerformAction(); 
    }

    private boolean evaluateEasyUseCard(){
        boolean isBestCase = shouldPerformAction();
        if(isBestCase) return (this.getMoney() < 200 || this.isJailed);
        else return chance(50);
    }

    private int getEasyWorldTourDestination(GameState state) {
        return random.nextInt(state.getBoard().size());
    }

    private boolean evaluateNormalBuyLand(PropertyTile tile, GameState state) {
    int salary = state.getConfig().getPassGoSalary();
    boolean isTakeover = (tile.getOwner() != null);
    int price = isTakeover ? tile.getTotalValue() * 2 : tile.getPurchasePrice();

    if (this.getMoney() < price) return false;

    if (!isTakeover) {
        return true; 
    } else {
        return chance(60) && (this.getMoney() - price) >= salary;
    }
}

    private boolean evaluateNormalUseCard(GameState state) {
    int initialMoney = state.getConfig().getInitialMoney();
    boolean isBestCase = chance(60);
    if (isBestCase) {
        return (this.getMoney() < initialMoney * 0.1 || this.isJailed);
    }
    else return this.hasCard();
}

    private int getNormalWorldTourDestination(GameState state) {
        List<PropertyTile> unownedLands = new java.util.ArrayList<>();
        
        for (int i = 0; i < state.getBoard().size(); i++) {
            Tile tile = state.getBoard().getTile(i);
            if (tile instanceof PropertyTile prop && prop.getOwner() == null) {
                unownedLands.add(prop);
            }
        }
        
        if (!unownedLands.isEmpty()) {
            // สุ่มเลือกจากที่ดินว่างที่มี
            PropertyTile chosen = unownedLands.get(random.nextInt(unownedLands.size()));
            return chosen.getIndex();
        }
        return getEasyWorldTourDestination(state); // ถ้าที่ดินเต็มหมด สุ่มมั่ว
    }

    private boolean evaluateHardBuyLand(PropertyTile tile, GameState state) {
        boolean isTakeover = (tile.getOwner() != null);
        int price = isTakeover ? tile.getTotalValue() * 2 : tile.getPurchasePrice();

        if (this.getMoney() < price) return false;

        if (!isTakeover) {
            if (willCompleteSet(tile)) return true;
            return (this.getMoney() - price) >= 500;
        } else {
            // เทคโอเวอร์
            if (willCompleteSet(tile) || willBlockOpponent(tile, state)) return true;
            if (isRiskAhead(state)) return false;
            return chance(90) && (this.getMoney() - price) >= 1000;
        }
    }

    private boolean evaluateHardUseCard(GameState state){
        boolean isBestCase = chance(90);
        if(isBestCase) {
            if(isOpponentCloseToVictory(state)) return true;

            return this.hasShield && hasExpensiveAssets();
        }
        else return this.hasCard();
    }

    private int getHardWorldTourDestination(GameState state) {
        PropertyTile bestUnowned = null;
        PropertyTile bestOwnedToUpgrade = null;
        int maxPrice = -1;
        int maxUpgradePrice = -1;

        for (int i = 0; i < state.getBoard().size(); i++) {
            Tile tile = state.getBoard().getTile(i);
            if (tile instanceof PropertyTile prop) {
                if (prop.getOwner() == null) {
                    if (prop.getPurchasePrice() > maxPrice && this.getMoney() >= prop.getPurchasePrice()) {
                        bestUnowned = prop;
                        maxPrice = prop.getPurchasePrice();
                    }
                } 
                else if (prop.getOwner().equals(this) && prop.getBuildingLevel() < 3) {
                    if (prop.getPurchasePrice() > maxUpgradePrice && this.getMoney() >= prop.getUpgradeCost(1)) {
                        bestOwnedToUpgrade = prop;
                        maxUpgradePrice = prop.getPurchasePrice();
                    }
                }
            }
        }

        if (bestUnowned != null) return bestUnowned.getIndex();
        if (bestOwnedToUpgrade != null) return bestOwnedToUpgrade.getIndex();

        for (int i = 0; i < state.getBoard().size(); i++) {
            Tile tile = state.getBoard().getTile(i);
            if (tile instanceof SpecialTile special && special.getEffect() == EffectType.FESTIVAL) {
                return tile.getIndex();
            }
        }

        return getEasyWorldTourDestination(state);
    }

    public boolean evaluateSwapCard(){
        return shouldPerformAction();
    }

    public int chooseWorldTourDestination(GameState state) {
        return switch (this.difficulty) {
            case EASY -> getEasyWorldTourDestination(state);
            case NORMAL -> getNormalWorldTourDestination(state);
            case HARD -> getHardWorldTourDestination(state);
            default -> getEasyWorldTourDestination(state);
        };
    }

    public Player chooseTarget(List<Player> opponents, GameState state) {
        if (opponents == null || opponents.isEmpty()) return null;

        switch (this.difficulty) {
            case EASY -> {
                return opponents.get(random.nextInt(opponents.size()));
            }

            case NORMAL -> {
                Player richest = opponents.get(0);
                for (Player p : opponents) {
                    if (p.getMoney() > richest.getMoney()) {
                        richest = p;
                    }
                }
                if (chance(80)) return richest; 
                else return opponents.get(random.nextInt(opponents.size()));
            }

            case HARD -> {
                Player biggestThreat = opponents.get(0);
                int maxThreatScore = -1;
                
                VictoryChecker vc = state.getVictoryChecker();
                
                for (Player p : opponents) {
                    int threatScore = p.getMoney() + (p.getOwnedLands().size() * 500); 
                    
                    if (vc.isPlayerCloseToVictory(state.getBoard(), p)) {
                        threatScore += 10000; 
                    }
                    
                    if (threatScore > maxThreatScore) {
                        maxThreatScore = threatScore;
                        biggestThreat = p;
                    }
                }
                return biggestThreat;
            }

            default -> {
                return opponents.get(0);
            }
        }
    }

    private boolean willCompleteSet(PropertyTile tile){
        String color = tile.getColorGroup();
        int countInSet = 0;

        for(PropertyTile land : this.getOwnedLands()){
            if(land.getColorGroup().equals(color)) countInSet++;
        }

        return countInSet == 2;
    }

    private boolean willBlockOpponent(PropertyTile tile, GameState state){
        String color = tile.getColorGroup();

        for(Player opponent : state.getPlayers()){
            if(opponent == this) continue;

            int opponentHas = 0;
            for(PropertyTile land : opponent.getOwnedLands()){
                if(land.getColorGroup().equals(color)) opponentHas++;
            }

            if(opponentHas == 2) return true;
        }

        return false;
    }

    private boolean isRiskAhead(GameState state){
        int currentPos = this.getPosition();
        Board board = state.getBoard();

        for(int i =2; i<= 12; i++){
            int targetIdx = (currentPos + i) % board.size();
            Tile targetTile = board.getTile(targetIdx);

            if(targetTile instanceof PropertyTile property){
                Player owner = property.getOwner();

                if(owner != null && owner != this && !property.isMortgaged()){
                    if(property.calculateRent() > (this.getMoney() * 0.2)) return true;
                }
            }
        }

        return false;
    }

    private boolean isOpponentCloseToVictory(GameState state) {
    int initialMoney = state.getConfig().getInitialMoney();
    VictoryChecker vc = state.getVictoryChecker();
    
    for (Player opponent : state.getPlayers()) {
        if (opponent == this) continue;

        if (vc.isPlayerCloseToVictory(state.getBoard(), opponent)) return true; 

        if (opponent.getMoney() > initialMoney * 2 || opponent.getOwnedLands().size() > 5) {
            return true;
        }
    }
    return false;
}

    private boolean hasExpensiveAssets(){
        for(PropertyTile land : this.getOwnedLands()){
            if(land.getBuildingLevel() >= 2) return true;
        }

        return false;
    }

    public Random getRandom() {
        return random;
    }

    public void setRandom(Random random) {
        this.random = random;
    }
}
