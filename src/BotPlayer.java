import java.util.List;
import java.util.Random;

public class BotPlayer extends Player {
    private DifficultyLevel difficulty;
    private Random random = new Random();

    public BotPlayer(String name, int initialMoney, DifficultyLevel difficulty) {
        super(name, initialMoney);
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

    public boolean makeDecision(DecisionType type, PropertyTile currentTile, GameState state) {
        switch (this.difficulty) {
            case EASY:
                return handleEasyDecision(type, currentTile);
            case NORMAL:
                return handleNormalDecision(type, currentTile);
            case HARD:
                return handleHardDecision(type, currentTile, state);
            default:
                return false;

        }
    }

    public boolean handleEasyDecision(DecisionType type, PropertyTile tile){
        switch(type){
            case BUY_LAND: return evaluateEasyBuyLand(tile);
            case USE_CARD: return evaluateEasyUseCard();
            case PAY_TOLL : return true;
            case SURRENDER: return this.getMoney() < 0;
            default: return false;
        }
    }

    public boolean handleNormalDecision(DecisionType type, PropertyTile tile){
        switch(type){
            case BUY_LAND: return evaluateNormalBuyLand(tile);
            case USE_CARD: return evaluateNormalUseCard();
            case UPGRADE: return (this.getMoney() > 1000) && chance(60);
            case PAY_TOLL : return true;
            case SURRENDER: return this.getMoney() < 0;
            default: return false;
        }
    }

    public boolean handleHardDecision(DecisionType type, PropertyTile tile, GameState state){
        switch(type){
            case BUY_LAND: return evaluateHardBuyLand(tile, state);
            case USE_CARD: return evaluateHardUseCard(state);
            case UPGRADE:
                int maxRent = state.getBoard().getMaxRentOnBoard();
                return (this.getMoney() > maxRent) && chance(90);
            case PAY_TOLL : return true;
            case SURRENDER: return this.getMoney() < -500;
            default: return false;
        }
    }

    private boolean evaluateEasyBuyLand(PropertyTile tile){
        boolean isBestCase = shouldPerformAction();

        if(isBestCase) return this.getMoney() >= tile.getPurchasePrice();
        else return true;

    }

    private boolean evaluateEasyUseCard(){
        boolean isBestCase = shouldPerformAction();
        if(isBestCase) return (this.getMoney() < 200 || this.isJailed);
        else return chance(50);
    }

    private boolean evaluateNormalBuyLand(PropertyTile tile){
        boolean isBestCase = chance(60);
        int safetyMargin = 500;

        if(isBestCase) return (this.getMoney() - tile.getPurchasePrice()) >= safetyMargin;
        else return this.getMoney() >= tile.getPurchasePrice();
    }

    private boolean evaluateNormalUseCard(){
        boolean isBestCase = chance(60);
        if(isBestCase) return (this.getMoney() < 200 || this.isJailed);
        else return this.hasCard();
    }

    private boolean evaluateHardBuyLand(PropertyTile tile, GameState state){
        boolean isBestCase = chance(90);

        if(isBestCase){
            if(willCompleteSet(tile) || willBlockOpponent(tile, state)) return true;

            if(isRiskAhead(state)) return false;

            return this.getMoney() >= tile.getPurchasePrice();
        }
        else return false;
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
            case EASY:
                return opponents.get(random.nextInt(opponents.size()));

            case NORMAL:
                Player richest = opponents.get(0);
                for (Player p : opponents) {
                    if (p.getMoney() > richest.getMoney()) {
                        richest = p;
                    }
                }
                if (chance(80)) return richest; 
                else return opponents.get(random.nextInt(opponents.size()));

            case HARD:
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

            default:
                return opponents.get(0);
        }
    }

    private boolean willCompleteSet(PropertyTile tile){
        String color = tile.getColorGroup();
        int countInSet = 0;

        for(PropertyTile land : this.getOwnedLands()){
            if(land.getColorGroup().equls(color)) countInSet++;
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
        VictoryChecker vc = new VictoryChecker();
        
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
}
