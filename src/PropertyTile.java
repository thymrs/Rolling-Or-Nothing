public class PropertyTile extends Tile {
    private final int purchasePrice;
    private final int baseRent;
    private int buildingLevel;
    private Player owner;
    private boolean isMortgaged;
    private final String colorGroup;
    private boolean hasDoubleRent = false; // Festival effect: double rent

    public PropertyTile(int index, String name, int purchasePrice, int baseRent, String colorGroup) {
        super(index, name);
        if (purchasePrice < 0)
            throw new IllegalArgumentException();
        if (baseRent < 0)
            throw new IllegalArgumentException();
        this.purchasePrice = purchasePrice;
        this.baseRent = baseRent;
        this.buildingLevel = 0;
        this.owner = null;
        this.isMortgaged = false;
        this.colorGroup = colorGroup;
    }

    public int getPurchasePrice() {
        return purchasePrice;
    }

    public int getBaseRent() {
        return baseRent;
    }

    public int getBuildingLevel() {
        return buildingLevel;
    }

    public boolean isMortgaged() {
        return isMortgaged;
    }

    public int calculateRent() {
        int rent = baseRent * (1 + Math.max(0, buildingLevel));
        if (hasDoubleRent) {
            rent *= 2;
        }
        return Math.max(rent, 0);
    }

    public boolean upgradeLevel() {
        if (isMortgaged)
            return false;
        if (buildingLevel >= 3)
            return false;
        buildingLevel++;
        return true;
    }

    public void resetBuildingLevel() {
        this.buildingLevel = 0;
    }

    public Player getOwner() {
        return owner;
    }

    public String getColorGroup() {
        return this.colorGroup;
    }

    public void setOwner(Player owner) {
        this.owner = owner;
    }

    public void setMortgaged(boolean mortgaged) {
        this.isMortgaged = mortgaged;
    }

    public boolean isTourism() {
        return "TOURISM".equalsIgnoreCase(this.colorGroup);
    }

    public int getBoardSide() {
        int tilesPerSide = 8;
        return (this.index / tilesPerSide) + 1;
    }

    public int getTotalValue() {
        return this.purchasePrice + (this.purchasePrice * this.buildingLevel); 
    }

    public int getUpgradeCost(int levelsToUpgrade) {
        return this.getPurchasePrice() * levelsToUpgrade;
    }
    
    public boolean hasDoubleRent() {
        return hasDoubleRent;
    }
    
    public void setDoubleRent(boolean doubleRent) {
        this.hasDoubleRent = doubleRent;
    }
    
    public void resetDoubleRent() {
        this.hasDoubleRent = false;
    }

    @Override
    public void onPlayerEnter(Player player, GameState state) {
        if (player == null || state == null)
            return;
        Player currentOwner = this.owner;
        if (currentOwner != null && !currentOwner.equals(player) && !isMortgaged) {
            if (state.getBank() != null) {
                state.getBank().collectRent(player, this);
            }
        }
    }
}
