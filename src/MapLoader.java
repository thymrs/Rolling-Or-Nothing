
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class MapLoader {

    public Board loadMap(String mapName) {
        String relPath = "maps/" + mapName + ".csv";
        List<String> lines = readMapFile(relPath);

        List<Tile> tiles = new ArrayList<>();
        for (String line : lines) {
            if (line == null) continue;

            String trimmed = line.trim();
            if (trimmed.isEmpty()) continue;
            if (trimmed.startsWith("#")) continue;

            Tile t = parseTile(trimmed);
            if (t != null) tiles.add(t);
        }

        tiles.sort(Comparator.comparingInt(Tile::getIndex));

        if (!validateBoardSize(tiles)) {
            throw new IllegalStateException("Invalid tiles for board.");
        }

        return new Board(tiles);
    }

    public Tile[][] loadMapSegments(String mapName) {
        String relPath = "maps/" + mapName + ".csv";
        List<String> lines = readMapFile(relPath);

        List<Tile> tiles = new ArrayList<>();
        for (String line : lines) {
            if (line == null) continue;

            String trimmed = line.trim();
            if (trimmed.isEmpty()) continue;
            if (trimmed.startsWith("#")) continue;

            Tile t = parseTile(trimmed);
            if (t != null) tiles.add(t);
        }

        tiles.sort(Comparator.comparingInt(Tile::getIndex));

        if (!validateBoardSize(tiles)) {
            throw new IllegalStateException("Invalid tiles for board.");
        }

        int total = tiles.size();
        if (total % 4 != 0) {
            throw new IllegalStateException("Total tiles must be divisible by 4.");
        }

        int sideLen = total / 4;
        int edgeLen = sideLen - 1;

        Tile[][] seg = new Tile[8][];

        seg[0] = new Tile[] { tiles.get(0) };
        seg[2] = new Tile[] { tiles.get(sideLen) };
        seg[4] = new Tile[] { tiles.get(sideLen * 2) };
        seg[6] = new Tile[] { tiles.get(sideLen * 3) };

        seg[1] = slice(tiles, 1, edgeLen);
        seg[3] = slice(tiles, sideLen + 1, edgeLen);
        seg[5] = slice(tiles, sideLen * 2 + 1, edgeLen);
        seg[7] = slice(tiles, sideLen * 3 + 1, edgeLen);

        return seg;
    }

    private Tile[] slice(List<Tile> tiles, int startInclusive, int length) {
        Tile[] out = new Tile[length];
        for (int i = 0; i < length; i++) {
            out[i] = tiles.get(startInclusive + i);
        }
        return out;
    }

    private List<String> readMapFile(String relPath) {
        Path diskPath = Paths.get("src").resolve(relPath);
        if (Files.exists(diskPath)) {
            try {
                return Files.readAllLines(diskPath, StandardCharsets.UTF_8);
            } catch (IOException e) {
                throw new RuntimeException("Failed to read map file from disk: " + diskPath, e);
            }
        }

        InputStream is = getClass().getClassLoader().getResourceAsStream(relPath);
        if (is == null) {
            throw new IllegalArgumentException(
                "Map file not found. Tried disk: " + diskPath + " and classpath: " + relPath
            );
        }

        try (BufferedReader br = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
            List<String> out = new ArrayList<>();
            String line;
            while ((line = br.readLine()) != null) out.add(line);
            return out;
        } catch (IOException e) {
            throw new RuntimeException("Failed to read map file from classpath: " + relPath, e);
        }
    }

    private Tile parseTile(String csvLine) {
        String[] data = csvLine.split("\\s*,\\s*");
        if (data.length < 3) {
            throw new IllegalArgumentException("Invalid CSV line (need at least 3 columns): " + csvLine);
        }

        String kind = data[0].trim().toUpperCase();
        return switch (kind) {
            case "PROPERTY" -> createPropertyTile(data);
            case "SPECIAL"  -> createSpecialTile(data);
            case "CHANCE"   -> createChanceTile(data);
            case "ACTION"   -> createActionTile(data);
            default -> throw new IllegalArgumentException("Unknown tile type: " + data[0]);
        };
    }

    private PropertyTile createPropertyTile(String[] data) {
        int index = parseInt(data, 1);
        String name = data[2];
        int purchasePrice = (data.length > 3) ? parseInt(data, 3) : 0;
        int baseRent = (data.length > 4) ? parseInt(data, 4) : 0;
        return new PropertyTile(index, name, purchasePrice, baseRent);
    }

    private SpecialTile createSpecialTile(String[] data) {
        int index = parseInt(data, 1);
        String name = data[2];
        EffectType effect = (data.length > 3)
                ? EffectType.valueOf(data[3].trim().toUpperCase())
                : EffectType.FESTIVAL;
        return new SpecialTile(index, name, effect);
    }

    private ChanceTile createChanceTile(String[] data) {
        int index = parseInt(data, 1);
        String name = data[2];
        return new ChanceTile(index, name);
    }

    private ActionTile createActionTile(String[] data) {
        int index = parseInt(data, 1);
        String name = data[2];
        ActionType type = (data.length > 3)
                ? ActionType.valueOf(data[3].trim().toUpperCase())
                : ActionType.START;
        return new ActionTile(index, name, type);
    }

    private boolean validateBoardSize(List<Tile> tiles) {
        if (tiles == null || tiles.size() < 4) return false;
        for (Tile t : tiles) {
            if (t.getIndex() < 0) return false;
        }
        return true;
    }

    private int parseInt(String[] data, int i) {
        if (i >= data.length) {
            throw new IllegalArgumentException("Missing int column at index " + i);
        }
        try {
            return Integer.parseInt(data[i].trim());
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid number at column " + i + ": " + data[i], e);
        }
    }
}