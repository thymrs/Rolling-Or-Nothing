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

enum ActionType { START, JAIL, WORLD_TRAVEL, TAX_OFFICE, ISLAND }
enum EffectType { OLYMPIC, FESTIVAL, BLACKOUT, FREEZE }