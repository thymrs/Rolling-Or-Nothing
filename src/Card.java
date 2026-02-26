public class Card {
    private CardType type;
    private int value;

    public Card(CardType type, int value) {
        this.type = type;
        this.value = value;
    }

    public CardType getType() {
        return this.type;
    }

    public void applyEffect(Player player, Player target, GameState state) {
        switch (this.type) {
            case ANGEL:
                player.setTollFree(true);
                state.notifyMessage("👼 " + player.getName() + " Use Angel Card! No paying in this turn!");

                Tile currentTile = state.getBoard().getTile(player.getPosition());

                if (currentTile instanceof PropertyTile property) {
                    Player owner = property.getOwner();

                    if (owner != null && owner != player) {
                        int takeOverPrice = property.getPurchasePrice() * 2;

                        if (player.getMoney() >= takeOverPrice) {
                            player.pay(takeOverPrice);
                            owner.receiveMoney(takeOverPrice);

                            owner.removeAsset(property);
                            property.setOwner(player);
                            player.addAsset(property);
                        }
                    }
                }

                break;
            case SHIELD:
                player.setHasShield(true);
                state.notifyMessage("🛡️ " + player.getName() + " Shield on! Prevent abnormal status effects once!");
                break;

            case DISCOUNT:
                player.setDiscountRate(this.value);
                state.notifyMessage(
                        "🎟️ " + player.getName() + " Got a dicount " + this.value + "% for the next payment!");
                break;

            case ESCAPE:
                player.setIsJailed(false);
                state.notifyMessage("🚁 " + player.getName() + " use Escape! Freedom now!");
                break;

            case FORCE_SELL:
                if (target != null) {
                    state.notifyMessage(
                            "💥 " + player.getName() + " force " + target.getName() + " to sell their lands!");
                    currentTile = state.getBoard().getTile(target.getPosition());

                    if (currentTile instanceof PropertyTile targetProperty) {
                        if (targetProperty.getOwner() == target) {
                            state.getBank().processPurchase(player, targetProperty, target);
                        }
                    }
                }
                break;

            case REWARD:
                player.receiveMoney(this.value);
                state.notifyMessage("🎁 " + player.getName() + " got bonus for " + this.value + "!");
                break;

            case PUNISH:
                if (target != null) {
                    if (target.getIsJailed()) {
                        target.addJailTurnCount(1);
                        state.notifyMessage(
                                "⚡ " + player.getName() + " punish " + target.getName() + " to be in jail for 1 turn!");
                    } else {
                        target.setIsJailed(true);
                        target.addJailTurnCount(1);
                        state.notifyMessage(
                                "⚡ " + player.getName() + " punish " + target.getName() + " to be in jail for 1 turn!");
                    }
                }
                break;

        }
    }

    public boolean requiresTarget() {
        return this.type == CardType.FORCE_SELL || this.type == CardType.PUNISH;
    }

    public CardType getTypeCard() {
        return this.type;
    }

    @Override
    public String toString() {
        return this.type.name();
    }
}
