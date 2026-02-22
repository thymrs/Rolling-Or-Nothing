public class ChanceTile extends Tile {
    public ChanceTile(int index, String name) {
        super(index, name);
    }

    public Card drawCard(GameState state) {
        if (state == null || state.getDeck() == null) return null;
        return state.getDeck().draw();
    }

    @Override
    public void onPlayerEnter(Player player, GameState state) {
        
    }
}