public class HumanPlayer extends Player {

    public HumanPlayer(int id, String name, int initialMoney) {
        super(id, name, initialMoney);
    }

    @Override
    public boolean makeDecision(DecisionType type, PropertyTile tile, GameState state) {
        return false;
    }
}