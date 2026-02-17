class ChanceTile extends Tile {
    public ChanceTile(int index, String name) {
        super(index, name);
    }

    public void drawCard(GameState state) {
        if (state == null) return;
        if (state.getDeck() == null) return;
        Card c = state.getDeck().draw();
        if (c == null) return;
        Player p = state.getCurrentPlayer();
        if (p != null) {
            c.applyEffect(p, state);
        }
        state.getDeck().discard(c);
    }

    @Override
    public void onPlayerEnter(Player player, GameState state) {
        drawCard(state);
    }
}