
public abstract class Tile {
    protected final int index;
    protected final String name;

    protected Tile(int index, String name) {
        if (index < 0) throw new IllegalArgumentException();
        if (name == null || name.isBlank()) throw new IllegalArgumentException();
        this.index = index;
        this.name = name;
    }

    public int getIndex() { return index; }
    public String getName() { return name; }
    public abstract void onPlayerEnter(Player player, GameState state);
}

class PropertyTile extends Tile {
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

class ActionTile extends Tile {
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

class SpecialTile extends Tile {
    private final EffectType effect;

    public SpecialTile(int index, String name, EffectType effect) {
        super(index, name);
        this.effect = effect;
    }

    public EffectType getEffect() { return effect; }

    public void triggerEffect(Player player, GameState state) {
        if (player == null || state == null) return;
        switch (effect) {
            case FESTIVAL -> player.receiveMoney(100);
            case BLACKOUT -> player.pay(50);
            case FREEZE -> player.setFrozenTurns(1);
            case OLYMPIC -> player.receiveMoney(200);
        }
    }

    @Override
    public void onPlayerEnter(Player player, GameState state) {
        triggerEffect(player, state);
    }
}

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

enum ActionType { START, JAIL, WORLD_TRAVEL, TAX_OFFICE, ISLAND }
enum EffectType { OLYMPIC, FESTIVAL, BLACKOUT, FREEZE }