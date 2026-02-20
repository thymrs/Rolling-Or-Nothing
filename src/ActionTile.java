public class ActionTile extends Tile {
    private final ActionType type;

    public ActionTile(int index, String name, ActionType type) {
        super(index, name);
        this.type = type;
    }

    public ActionType getType() { return type; }

    public void performAction(Player player, GameState state) {
        if (player == null || state == null) return;
        switch (type) {
            case START -> { }
            case JAIL -> {
                player.setJailed(true);
                player.setJailTurnCount(0);
            }
            case WORLD_TRAVEL -> player.setPosition(0);
            case TAX_OFFICE -> {
                int tax = (int) Math.round(player.getMoney() * (state.getConfig().getTaxPercentage() / 100.0));
                player.pay(tax);
                if (state.getBank() != null) state.getBank().receiveTax(tax);
            }
            case ISLAND -> player.setTollFree(true);
        }
    }

    @Override
    public void onPlayerEnter(Player player, GameState state) {
        performAction(player, state);
    }
}
