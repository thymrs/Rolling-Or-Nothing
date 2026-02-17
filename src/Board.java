package Rollblabla;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Board {
    private final List<Tile> tiles;
    private final int totalTiles;

    public Board(List<Tile> tiles) {
        if (tiles == null || tiles.isEmpty())
            throw new IllegalArgumentException();
        this.tiles = new ArrayList<>(tiles);
        this.totalTiles = this.tiles.size();
    }

    public Tile getTile(int index) {
        int i = mod(index, totalTiles);
        return tiles.get(i);
    }

    public int getNextIndex(int current, int steps) {
        return mod(current + steps, totalTiles);
    }

    public List<PropertyTile> getPropertiesOwnedBy(Player player) {
        if (player == null) return List.of();
        List<PropertyTile> owned = new ArrayList<>();
        for (Tile t : tiles) {
            if (t instanceof PropertyTile p) {
                if (player.equals(p.getOwner())) owned.add(p);
            }
        }
        return owned;
    }

    public int size() {
        return totalTiles;
    }

    public List<Tile> getTilesReadOnly() {
        return Collections.unmodifiableList(tiles);
    }

    private static int mod(int x, int m) {
        int r = x % m;
        return (r < 0) ? r + m : r;
    }
}