public class HumanPlayer extends Player {
    public HumanPlayer(int id, String name, int initialMoney, DinoType dinoType) {
        super(id, name, initialMoney, dinoType);
    }

    @Override
    public boolean makeDecision(DecisionType type, PropertyTile tile, GameState state) {
        return false;
    }
}