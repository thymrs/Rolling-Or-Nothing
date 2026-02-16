package src.model.entities.player;

import enums.*;
import src.model.tiles.PropertyTile;
import java.util.Random;

import DecisionType;

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
            case EASY :
                threshold = 30;
                break;
            case NORMAL :
                threshold = 60;
                break;
            case HARD :
                threshold = 90;
                break;
        }

        return chance < threshold;
    }

    private boolean chance(int percentage){
        return random.nextInt(100) < percentage;
    }

    public boolean makeDecision(DecisionType type) {
        switch (type) {
            case BUY_LAND:
                return evaluateBuyLand();

            case UPGRADE:
                return evaluateUpgrade();

            case USE_CARD:
                return this.hasCard();
            
            case PAY_TOLL:
                return true;

            case SURRENDER:
                return this.money < 0;

            case SWAP_CARD:
                return evaluateSwapCard();

            default:
                return false;

        }
    }

    private boolean evaluateTrade(PropertyTile tile) {
        return shouldPerformAction();
    }

    private boolean evaluateBuyLand(){
        return shouldPerformAction();
    }

    private boolean evaluateUpgrade(){
        return shouldPerformAction();
    }

    public boolean evaluateSwapCard(){
        return shouldPerformAction();
    }
}
