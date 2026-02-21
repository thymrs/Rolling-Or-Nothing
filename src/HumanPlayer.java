public class HumanPlayer extends Player {

    public HumanPlayer(String name, int initialMoney) {
        super(name, initialMoney);
    }

    @Override
    public boolean makeDecision(DecisionType type){
        return false;
    }
}
