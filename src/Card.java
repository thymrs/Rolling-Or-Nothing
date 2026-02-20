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
                break;

            case DISCOUNT:
                player.setDiscountRate(this.value);
                break;

            case ESCAPE:
                player.setIsJailed(false);
                break;

            case FORCE_SELL:
                if (target != null) {
                    PropertyTile targetTile = state.getBoard().getTile(target.getPosition());
                    if (targetTile instanceof PropertyTile targetProperty) {
                        if (targetProperty.getOwner() == target) {
                            target.removeAsset(targetProperty);
                            targetProperty.setOwner(player);
                            target.addAsset(targetProperty);
                        }
                    }
                }
                ;
                break;

            case REWARD:
                player.receiveMoney(this.value);
                break;

            case PUNISH:
                if (target != null) {
                    if (target.getIsJailed()) {
                        target.addJailTurnCount(1);
                    } else
                        target.setIsJailed(true);
                }
                break;

        }
    }

    public CardType getTypeCard(){
        return this.type;
    }

    public boolean requiresTarget() {
    return switch (this.type) {
        case FORCE_SELL, PUNISH -> true;
        default -> false;
    };
}
}
