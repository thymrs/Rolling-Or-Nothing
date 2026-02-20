public class PropertyTile extends Tile {
    private final int purchasePrice;
    private final int baseRent;
    private int buildingLevel;
    private Player owner;
    private boolean isMortgaged;

    public PropertyTile(int index, String name, int purchasePrice, int baseRent) {
        super(index, name);
        if (purchasePrice < 0) throw new IllegalArgumentException();
        if (baseRent < 0) throw new IllegalArgumentException();
        this.purchasePrice = purchasePrice;
        this.baseRent = baseRent;
        this.buildingLevel = 0;
        this.owner = null;
        this.isMortgaged = false;
    }

    public int getPurchasePrice() { return purchasePrice; }
    public int getBaseRent() { return baseRent; }
    public int getBuildingLevel() { return buildingLevel; }
    public boolean isMortgaged() { return isMortgaged; }

    public int calculateRent() {
        int rent = baseRent * (1 + Math.max(0, buildingLevel));
        return Math.max(rent, 0);
    }

    public boolean upgradeLevel() {
        if (isMortgaged) return false;
        if (buildingLevel >= 3) return false;
        buildingLevel++;
        return true;
    }

    public Player getOwner() { return owner; }
    public void setOwner(Player owner) { this.owner = owner; }
    public void setMortgaged(boolean mortgaged) { this.isMortgaged = mortgaged; }

    @Override
    public void onPlayerEnter(Player player, GameState state) {
        if (player == null || state == null) return;
        Player currentOwner = this.owner;
        if (currentOwner != null && !currentOwner.equals(player) && !isMortgaged) {
            if (state.getBank() != null) {
                state.getBank().collectRent(player, this);
            }
        }
    }
}
