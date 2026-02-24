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
            case EASY -> handleEasyDecision(type, currentTile);
            case NORMAL -> handleNormalDecision(type, currentTile);
            case HARD -> handleHardDecision(type, currentTile, state);
            default -> false;
        };
    }

    public boolean handleEasyDecision(DecisionType type, PropertyTile tile){
        return switch (type) {
            case BUY_LAND -> evaluateEasyBuyLand(tile);
            case USE_CARD -> evaluateEasyUseCard();
            case PAY_TOLL -> true;
            case SURRENDER -> this.getMoney() < 0;
            default -> false;
        };
    }

    public boolean handleNormalDecision(DecisionType type, PropertyTile tile){
        return switch (type) {
            case BUY_LAND -> evaluateNormalBuyLand(tile);
            case USE_CARD -> evaluateNormalUseCard();
            case UPGRADE -> (this.getMoney() > 1000) && chance(60);
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

    private boolean evaluateNormalBuyLand(PropertyTile tile) {
        boolean isTakeover = (tile.getOwner() != null);
        int price = isTakeover ? tile.getTotalValue() * 2 : tile.getPurchasePrice();

        if (this.getMoney() < price) return false;

        if (!isTakeover) {
            return true; 
        } else {
            return chance(60) && (this.getMoney() - price) >= 500;
        }
    }

    private boolean evaluateNormalUseCard(){
        boolean isBestCase = chance(60);
        if(isBestCase) return (this.getMoney() < 200 || this.isJailed);
        else return this.hasCard();
    }

    private boolean evaluateHardBuyLand(PropertyTile tile, GameState state) {
        boolean isTakeover = (tile.getOwner() != null);
        int price = isTakeover ? tile.getTotalValue() * 2 : tile.getPurchasePrice();

        if (this.getMoney() < price) return false;

        // HIGH PRIORITY: Tourism acquisition
        if (tile.isTourism()) {
            return evaluateTourismBuy(tile, state);
        }

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

    public boolean evaluateSwapCard(){
        return shouldPerformAction();
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
        VictoryChecker vc = state.getVictoryChecker();
        
        for (Player opponent : state.getPlayers()) {
            if (opponent == this) continue;

            if (vc.isPlayerCloseToVictory(state.getBoard(), opponent)) {
                return true; 
            }

            if (opponent.getMoney() > 3000 || opponent.getOwnedLands().size() > 5) {
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

    // ===== TOURISM ACQUISITION LOGIC =====
    /**
     * Aggressively evaluate tourism property purchases
     * Tourism monopoly is a win condition - high priority
     */
    public boolean evaluateTourismBuy(PropertyTile touristProperty, GameState state) {
        int requiredCash = touristProperty.getPurchasePrice();
        
        // Must have 20% buffer for strategy flexibility
        if (this.getMoney() < requiredCash * 1.2) {
            return false;
        }
        
        // Count owned tourism tiles
        int ownedTouristCount = (int) this.getOwnedLands().stream()
            .filter(t -> t.isTourism())
            .count();
        
        // Check opponent's tourism progress (threat assessment)
        int maxOpponentTourist = state.getPlayers().stream()
            .filter(p -> p != this && !p.isBankrupt())
            .mapToInt(p -> (int) p.getOwnedLands().stream()
                .filter(t -> t.isTourism())
                .count())
            .max()
            .orElse(0);
        
        // PRIORITY SCORING (0-100)
        int priority = 0;
        
        // +40 if we're close to monopoly (2+ tiles)
        if (ownedTouristCount >= 2) {
            priority += 40;
        }
        // +35 if opponent threatens monopoly - MUST BLOCK
        else if (maxOpponentTourist >= 2) {
            priority += 35;
        }
        // +20 for normal acquisition
        else {
            priority += 20;
        }
        
        // +25 if property is uncontested (few options left)
        long unownedTourist = state.getBoard().getTilesReadOnly().stream()
            .filter(t -> t instanceof PropertyTile)
            .map(t -> (PropertyTile) t)
            .filter(t -> t.isTourism() && t.getOwner() == null)
            .count();
        if (unownedTourist <= 2) {
            priority += 25;
        }
        
        return priority >= 60; // Threshold for purchase
    }

    // ===== FESTIVAL PLACEMENT LOGIC =====
    /**
     * Selects best owned property to place festival double-rent effect
     * Maximizes ROI based on rent + traffic probability + building level
     */
    public PropertyTile chooseFestivalLocation(GameState state) {
        List<PropertyTile> ownedProperties = this.getOwnedLands();
        
        if (ownedProperties.isEmpty()) {
            return null;
        }
        
        PropertyTile bestProperty = null;
        double highestScore = -1;
        
        for (PropertyTile property : ownedProperties) {
            double score = calculateFestivalROI(property, state);
            
            if (score > highestScore) {
                highestScore = score;
                bestProperty = property;
            }
        }
        
        return bestProperty;
    }

    /**
     * Calculates ROI score for placing festival on a property
     * Factors: Rent value, building level, board position traffic, color group
     */
    private double calculateFestivalROI(PropertyTile property, GameState state) {
        double score = 0;
        
        // FACTOR 1: Rent multiplier (doubled rent value)
        int baseRent = property.calculateRent();
        score += baseRent * 2.0; // Double rent potential
        
        // FACTOR 2: Building level bonus (built properties attract more visits)
        // Landmarks (Level 3) have highest ROI
        int buildingLevel = property.getBuildingLevel();
        score += buildingLevel * 150.0; // Each level adds 150 base score
        
        // FACTOR 3: Board position traffic probability
        int tileIndex = property.getIndex();
        double trafficMultiplier = calculateTrafficWeight(tileIndex);
        score *= trafficMultiplier;
        
        // FACTOR 4: Color group premium (high-value groups get boost)
        String colorGroup = property.getColorGroup();
        if ("RED".equalsIgnoreCase(colorGroup) || 
            "DARK_BLUE".equalsIgnoreCase(colorGroup) ||
            "MAGENTA".equalsIgnoreCase(colorGroup)) {
            score *= 1.3;
        }
        
        // FACTOR 5: Penalty if already has double rent active
        if (property.hasDoubleRent()) {
            score *= 0.5; // No benefit if already active
        }
        
        // FACTOR 6: Monopoly bonus (if owns full color group)
        if (isMonopolyOwned(property, state)) {
            score *= 1.8; // Huge boost for monopolies
        }
        
        return score;
    }

    /**
     * Traffic weight by board position (accounts for dice probability)
     * Corners and popular tiles get higher multipliers
     */
    private double calculateTrafficWeight(int tileIndex) {
        // Corners get highest traffic (position 0, 8, 16, 24)
        if (tileIndex % 8 == 0) return 3.0;
        
        // Adjacent to corners (common landing spots after corner rolls)
        if ((tileIndex - 1) % 8 == 0 || (tileIndex + 1) % 8 == 0) return 1.8;
        
        // Chance tiles attract indirect traffic
        if (tileIndex == 3 || tileIndex == 13 || tileIndex == 21 || tileIndex == 29) {
            return 1.5;
        }
        
        // Default traffic weight for mid-board positions
        return 1.0;
    }

    /**
     * Checks if bot owns full color group (monopoly condition)
     */
    private boolean isMonopolyOwned(PropertyTile property, GameState state) {
        String colorGroup = property.getColorGroup();
        Board board = state.getBoard();
        
        // Count total properties in color group on board
        long totalInGroup = board.getTilesReadOnly().stream()
            .filter(t -> t instanceof PropertyTile)
            .map(t -> (PropertyTile) t)
            .filter(t -> colorGroup.equalsIgnoreCase(t.getColorGroup()))
            .count();
        
        // Count how many we own
        long ownedInGroup = this.getOwnedLands().stream()
            .filter(t -> colorGroup.equalsIgnoreCase(t.getColorGroup()))
            .count();
        
        // Monopoly if we own all properties in the group
        return ownedInGroup >= totalInGroup && totalInGroup > 0;
    }
}
