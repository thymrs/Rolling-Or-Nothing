import java.util.*;
import java.util.stream.Collectors;

public class VictoryChecker {

    private final int LINE_VICTORY_REQ = 8;
    private final int TOURISM_VICTORY_REQ = 5;   
    private final int TRIPLE_VICTORY_REQ = 3;    

    public VictoryType checkWinCondition(GameState state) {
        Player player = state.getCurrentPlayer();
        Board board = state.getBoard();

        if (checkLineVictory(board, player)) return VictoryType.LINE_VICTORY;
        if (checkTripleVictory(board, player)) return VictoryType.TRIPLE_VICTORY;
        if (checkTourismVictory(board, player)) return VictoryType.TOURISM_VICTORY;
 
        if (player.getMoney() < 0 && player.getOwnedLands().isEmpty()) return VictoryType.BANKRUPTCY; //ล้มละลายยยย เงินเบิ่ด

        return VictoryType.NONE;
    }

    public boolean isPlayerCloseToVictory(Board board, Player player) {
        return isCloseToLineVictory(board, player) || 
               isCloseToTripleVictory(board, player) || 
               isCloseToTourismVictory(board, player);
    }

    private boolean checkLineVictory(Board board, Player player) {
        List<PropertyTile> ownedProps = board.getPropertiesOwnedBy(player);
        Map<Integer, Long> sideCount = ownedProps.stream()
            .collect(Collectors.groupingBy(PropertyTile::getBoardSide, Collectors.counting()));

        for (Long count : sideCount.values()) {
            if (count >= LINE_VICTORY_REQ) return true; // ครองครบ 8 ช่อง
        }
        return false;
    }

   private boolean checkTripleVictory(Board board, Player player) {
        return countCompletedColors(board, player) >= TRIPLE_VICTORY_REQ; // ครองครบ 3 สี
    }

    private boolean checkTourismVictory(Board board, Player player) {
        List<PropertyTile> ownedProps = board.getPropertiesOwnedBy(player);
        long tourismCount = ownedProps.stream().filter(PropertyTile::isTourism).count();
        
        return tourismCount >= TOURISM_VICTORY_REQ; // ครองเกาะครบ
    }

    private boolean isCloseToLineVictory(Board board, Player player) {
        List<PropertyTile> ownedProps = board.getPropertiesOwnedBy(player);
        Map<Integer, Long> sideCount = ownedProps.stream()
            .collect(Collectors.groupingBy(PropertyTile::getBoardSide, Collectors.counting()));

        for (Long count : sideCount.values()) {
            if (count == LINE_VICTORY_REQ - 1) return true; // ขาด 1 ช่อง
        }
        return false;
    }

    private boolean isCloseToTourismVictory(Board board, Player player) {
        List<PropertyTile> ownedProps = board.getPropertiesOwnedBy(player);
        long ownedTourism = ownedProps.stream().filter(PropertyTile::isTourism).count();
        
        return ownedTourism == TOURISM_VICTORY_REQ - 1; // ขาด 1 เกาะ
    }

    private boolean isCloseToTripleVictory(Board board, Player player) {
        return countCompletedColors(board, player) == TRIPLE_VICTORY_REQ - 1;  // ขาด 1 สี
    }

  // method เก้บสีสำหรับไปเชคทริปเปลคัลเลอ
    private int countCompletedColors(Board board, Player player) {
        Map<String, Long> colorCount = player.getOwnedLands().stream()
            .filter(p -> !p.isTourism())
            .filter(p -> p.getColorGroup() != null)
            .collect(Collectors.groupingBy(PropertyTile::getColorGroup, Collectors.counting()));

        int completedColors = 0;
        for (Map.Entry<String, Long> entry : colorCount.entrySet()) {
            String color = entry.getKey();
            long ownedCount = entry.getValue();
    
            long totalInColor = board.getTotalTilesByColor(color);

            if (ownedCount == totalInColor && totalInColor > 0) { 
                completedColors++;
            }
        }
        return completedColors;
    }

    public String getWinner(GameState state) {
        Player winner = null;
        int maxNetWorth = -1;

        for (Player p : state.getPlayers()) {
            if (!p.isBankrupt()) { //
                int netWorth = p.getMoney(); //
                
                for (PropertyTile land : p.getOwnedLands()) {
                    netWorth += land.getPurchasePrice(); 
                }

                if (netWorth > maxNetWorth) {
                    maxNetWorth = netWorth;
                    winner = p;
                }
            }
        }

        if (winner != null) {
            return winner.getName() + " (ทรัพย์สิน: $" + maxNetWorth + ")";
        }
        return "ไม่มีผู้ชนะ";
    }
}