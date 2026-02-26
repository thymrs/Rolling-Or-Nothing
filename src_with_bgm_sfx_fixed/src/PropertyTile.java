public class PropertyTile extends Tile {
    private final int purchasePrice;
    private final int baseRent;
    private int buildingLevel;
    private Player owner;
    private boolean isMortgaged;
    private final String colorGroup;
    private int tollMultiplier = 1;
    private int expoTurnLeft = 0;

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
        int rent = 0;
        rent = this.baseRent * (1 + Math.max(0, buildingLevel));
        return rent * this.tollMultiplier;
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

    public void setExpo(int multiplier, int durationTurns) {
        this.tollMultiplier = multiplier;
        this.expoTurnLeft = durationTurns;
    }

    public void decreaseExpoTurn() {
        if (this.expoTurnLeft > 0) {
            this.expoTurnLeft--;
            if (this.expoTurnLeft == 0) {
                this.tollMultiplier = 1;
                System.out.println("▶ [DEBUG] EXPO at " + this.getName() + " has finished!");
            }
        }
    }

    public int getTollMultiplier() {
        return this.tollMultiplier;
    }

    @Override
    public void onPlayerEnter(Player player, GameState state) {
        if (player == null || state == null) return;
        
        Player currentOwner = this.owner;
        
        if (currentOwner != null && !currentOwner.equals(player) && !this.isMortgaged) {
            
            int rent = this.calculateRent();
            int actualPaid = rent;
            
            if (player.getIsTollFree()) {
                System.out.println("▶ [DEBUG] 👼 " + player.getName() + " Use Angel Card! Don't have to pay " + rent);
                player.setTollFree(false); // ใช้แล้วริบการ์ดคืน
                return;
            }

            if (player.getDiscountRate() > 0) {
                int discount = (rent * player.getDiscountRate()) / 100;
                actualPaid = rent - discount;
                System.out.println("▶ [DEBUG] 🎟️ " + player.getName() + " Use discount " + player.getDiscountRate() + "% (final amount is " + actualPaid + ")");
                player.setDiscountRate(0); // ใช้ส่วนลดแล้วริบการ์ดคืน
            }

            if (player.getMoney() >= actualPaid) {
                player.pay(actualPaid); 
                currentOwner.receiveMoney(actualPaid);
            
                if (this.getTollMultiplier() > 1) {
                    this.setExpo(1, 0); // เคลียร์ตัวคูณกลับเป็น 1 และเวลาเหลือ 0
                }
            } else {
                int allMoneyLeft = player.getMoney();
                player.pay(allMoneyLeft);
                currentOwner.receiveMoney(allMoneyLeft); // เจ้าของได้เงินเท่าที่คนตกเหลืออยู่
                System.out.println("▶ [DEBUG] 💀 " + player.getName() + " gone Bankruptcy!");
            }
        }
    }
}
