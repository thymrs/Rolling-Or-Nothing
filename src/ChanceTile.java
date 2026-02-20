public class ChanceTile extends Tile {
    public ChanceTile(int index, String name) {
        super(index, name);
    }

    public void drawCard(GameState state) {
        if (state == null || state.getDeck() == null) return;

        Card c = state.getDeck().draw();
        if (c == null) return;

        Player p = state.getCurrentPlayer();
        if (p != null) {
			CardType type = c.getType();

			if (type == CardType.ANGEL || type == CardType.SHIELD || 
                type == CardType.DISCOUNT || type == CardType.ESCAPE) {
                
                p.setHeldCard(c);
                System.out.println(p.getName() + " ได้รับไอเทม: " + c.getName());
                
            } else {
				if (c.requiresTarget()) {
					p.setHeldCard(c);
					state.setCurrentPhase(TurnPhase.ACTION_REQUIRED);
				} else {
					c.applyEffect(p, null, state);
					state.getDeck().discard(c);
				}
			}
        }
    }

    @Override
    public void onPlayerEnter(Player player, GameState state) {
        drawCard(state);
    }
}