import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class VictoryChecker {
    public VictoryType checkWinCondition(GameState state) {
        Player player = state.getCurrentPlayer();
        Board board = state.getBoard();

        if (checkLineVictory(board, player)) return VictoryType.LINE_VICTORY;
        if (checkTripleVictory(board, player)) return VictoryType.TRIPLE_VICTORY;
        if (checkTourismVictory(board, player)) return VictoryType.TOURISM_VICTORY;
        if (player.getMoney() < 0 && player.ownedLands.isEmpty()) return VictoryType.BANKRUPTCY;

        return VictoryType.NONE;
    }

    public boolean isPlayerCloseToVictory(Board board, Player player) {
        return isCloseToLineVictory(board, player) || 
               isCloseToTripleVictory(board, player) || 
               isCloseToTourismVictory(board, player);
    }

    private boolean isCloseToLineVictory(Board board, Player player) {
        List<PropertyTile> ownedProps = board.getPropertiesOwnedBy(player);
        Map<Integer, Long> sideCount = ownedProps.stream()
            .collect(Collectors.groupingBy(PropertyTile::getBoardSide, Collectors.counting()));
        int PROPS_PER_SIDE = 8; //รอว่าแถวนึงมีกี่ช่อง
        for (Long count : sideCount.values()) {
            if (count == PROPS_PER_SIDE - 1) {
                return true;
            }
        }
        return false;
    }

    private boolean isCloseToTourismVictory(Board board, Player player) {
        List<PropertyTile> ownedProps = board.getPropertiesOwnedBy(player);
        long ownedTourism = ownedProps.stream().filter(PropertyTile::isTourism).count();
        int TOTAL_TOURISM = 5; //รอว่าแถวนึงมีกี่ช่อง
        return ownedTourism == TOTAL_TOURISM - 1;
    }

    // private int countCompletedColorSets(Board board, List<PropertyTile> ownedProps) {
    //     // จัดกลุ่มที่ดินที่ผู้เล่นครองตามสี
    //     Map<String, Long> playerColorCount = ownedProps.stream()
    //             .filter(p -> !p.isTourism())
    //             .collect(Collectors.groupingBy(PropertyTile::getColorGroup, Collectors.counting()));

    //     int completedSets = 0;
        
    //     // วนลูปเช็คทีละสีที่ผู้เล่นมี
    //     for (String color : playerColorCount.keySet()) {
    //         long ownedCount = playerColorCount.get(color);
    //         long totalInColor = board.getTotalTilesByColor(color); // ต้องมี Method นี้ใน Board
            
    //         if (ownedCount == totalInColor && totalInColor > 0) {
    //             completedSets++;
    //         }
    //     }
    //     return completedSets;
    // }

    private boolean checkLineVictory(Board board, Player player) {
        List<PropertyTile> ownedProps = board.getPropertiesOwnedBy(player);

        Map<Integer, Long> sideCount = ownedProps.stream()
            .collect(Collectors.groupingBy(PropertyTile::getBoardSide, Collectors.counting()));

        for (Long count : sideCount.values()) {
            if (count >= 6) { 
                return true;
            }
        }
        return false;
    }

    private boolean checkTripleVictory(Board board, Player player) {
        List<PropertyTile> ownedProps = board.getPropertiesOwnedBy(player);

        Map<String, Long> colorCount = ownedProps.stream()
            .filter(p -> !p.isTourism())
            .collect(Collectors.groupingBy(PropertyTile::getColorGroup, Collectors.counting()));

        int completedColors = 0;
        for (Map.Entry<String, Long> entry : colorCount.entrySet()) {
            if (entry.getValue() >= 2) { 
                completedColors++;
            }
        }
        
        return completedColors >= 3;
    }

    private boolean checkTourismVictory(Board board, Player player) {
        List<PropertyTile> ownedProps = board.getPropertiesOwnedBy(player);
        
        long tourismCount = ownedProps.stream()
            .filter(PropertyTile::isTourism)
            .count();
        return tourismCount >= 4;
    }
}