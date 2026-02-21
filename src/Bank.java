public class Bank {

    public boolean processPurchase(Player player, PropertyTile tile) {
        if (tile.getOwner() == null && player.getMoney() >= tile.getPurchasePrice()) {
            boolean success = player.pay(tile.getPurchasePrice());
            if (success) {
                tile.setOwner(player);
                player.addAsset(tile);
                return true;
            }
        }
        return false;
    }   

    public boolean processPurchase(Player player, PropertyTile tile, Player target) {
        int takeoverPrice = tile.getPurchasePrice() * 2;
        if (tile.getOwner() == target && player.getMoney() >= takeoverPrice) {
            boolean success = player.pay(takeoverPrice);
            if (success) {
                target.receiveMoney(takeoverPrice);
                target.removeAsset(tile);
                tile.setOwner(player);
                player.addAsset(tile);
                return true;
            }
        }
        return false;
    }

    public boolean processUpgrade(Player player, PropertyTile tile) {
        if (tile.getOwner() == player && tile.getBuildingLevel() < 3) {
            int upgradeCost = tile.getPurchasePrice(); 
            if (player.getMoney() >= upgradeCost) {
                boolean success = player.pay(upgradeCost);
                if (success) {
                    tile.upgradeLevel();
                    return true;
                }
            }
        }
        return false;
    }

    public boolean collectRent(Player payer, PropertyTile tile) {
        Player owner = tile.getOwner();
        if (owner == null || owner == payer) {
            return false;
        }

        int rentAmount = tile.calculateRent();
        if (payer.isTollFree) {
            return true; 
        }
        boolean canPay = payer.pay(rentAmount);
        if (canPay) {
            owner.receiveMoney(rentAmount);
            return true;
        } else {
            owner.receiveMoney(payer.getMoney());
            payer.pay(payer.getMoney());
            return false; 
        }
    }

    public void paySalary(Player player, int passGoSalary) {
        player.receiveMoney(passGoSalary);
    }

    public boolean mortgageProperty(PropertyTile tile) {
        Player owner = tile.getOwner();
        if (owner != null && !tile.isMortgaged()) { 
            int mortgageValue = tile.getPurchasePrice() / 2;
            owner.receiveMoney(mortgageValue);
            tile.setMortgaged(true);
            return true;
        }
        return false;
    }

    public boolean payTax(Player player, int amount) {
        boolean canPay = player.pay(amount);
        if (canPay) {
            return true;
        } else {
            player.pay(player.getMoney());
            return false;
        }
    }

    public void confiscateAssets(Player bankruptPlayer) {
        if (bankruptPlayer.getOwnedLands() != null && !bankruptPlayer.getOwnedLands().isEmpty()) {
            for (PropertyTile tile : bankruptPlayer.getOwnedLands()) {
                tile.setOwner(null);         
                tile.upgradeLevel();    
                tile.setMortgaged(false);  
            bankruptPlayer.getOwnedLands().clear();
            }
        }
    }
}