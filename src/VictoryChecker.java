import java.util.*;
import java.util.stream.Collectors;

public class VictoryChecker {

    private final int LINE_VICTORY_REQ = 8;
    private final int TOURISM_VICTORY_REQ = 5;   
    private final int TRIPLE_VICTORY_REQ = 3;
    private VictoryType lastWinCondition = VictoryType.NONE;
    private Player lastWinner = null;

    public VictoryType checkWinCondition(GameState state) {
        Player player = state.getCurrentPlayer();
        Board board = state.getBoard();

        if (checkLineVictory(board, player)) {
            lastWinCondition = VictoryType.LINE_MONOPOLY;
            lastWinner = player;
            return VictoryType.LINE_MONOPOLY;
        }
        if (checkTripleVictory(board, player)) {
            lastWinCondition = VictoryType.TRIPLE_MONOPOLY;
            lastWinner = player;
            return VictoryType.TRIPLE_MONOPOLY;
        }
        if (checkTourismVictory(board, player)) {
            lastWinCondition = VictoryType.TOURISM_MONOPOLY;
            lastWinner = player;
            return VictoryType.TOURISM_MONOPOLY;
        }
 
        if (player.getMoney() < 0 && player.getOwnedLands().isEmpty()) {
            lastWinCondition = VictoryType.LAST_PLAYER_STANDING;
            lastWinner = getWinnerPlayer(state);
            return VictoryType.LAST_PLAYER_STANDING;
        }

        return VictoryType.NONE;
    }
    
    public VictoryType getLastWinCondition() {
        return lastWinCondition;
    }
    
    public Player getWinner(GameState state) {
        return getWinnerPlayer(state);
    }
    
    public String getWinnerString(GameState state) {
        Player winner = getWinnerPlayer(state);
        if (winner != null) {
            return winner.getName() + " (ทรัพย์สิน: $" + winner.getTotalAssetsValue() + ")";
        }
        return "ไม่มีผู้ชนะ";
    }
    
    private Player getWinnerPlayer(GameState state) {
        Player winner = null;
        int maxNetWorth = -1;

        for (Player p : state.getPlayers()) {
            if (!p.isBankrupt()) {
                int netWorth = p.getTotalAssetsValue();

                if (netWorth > maxNetWorth) {
                    maxNetWorth = netWorth;
                    winner = p;
                }
            }
        }

        return winner;
    }

    public boolean isPlayerCloseToVictory(Board board, Player player) {
        return isCloseToLineVictory(board, player) || isCloseToTripleVictory(board, player) || isCloseToTourismVictory(board, player);
    }

    private boolean checkLineVictory(Board board, Player player) {
        List<PropertyTile> ownedProps = board.getPropertiesOwnedBy(player);
        Map<Integer, Long> sideCount = ownedProps.stream()
            .collect(Collectors.groupingBy(PropertyTile::getBoardSide, Collectors.counting()));

        for (Long count : sideCount.values()) {
            if (count >= LINE_VICTORY_REQ) return true;
        }
        return false;
    }

   private boolean checkTripleVictory(Board board, Player player) {
        return countCompletedColors(board, player) >= TRIPLE_VICTORY_REQ;
    }

    private boolean checkTourismVictory(Board board, Player player) {
        List<PropertyTile> ownedProps = board.getPropertiesOwnedBy(player);
        long tourismCount = ownedProps.stream().filter(PropertyTile::isTourism).count();
        
        return tourismCount >= TOURISM_VICTORY_REQ;
    }

    private boolean isCloseToLineVictory(Board board, Player player) {
        List<PropertyTile> ownedProps = board.getPropertiesOwnedBy(player);
        Map<Integer, Long> sideCount = ownedProps.stream()
            .collect(Collectors.groupingBy(PropertyTile::getBoardSide, Collectors.counting()));

        long currentMax = sideCount.values().stream().max(Long::compareTo).orElse(0L);
        return currentMax >= (LINE_VICTORY_REQ - 1);
    }

    private boolean isCloseToTripleVictory(Board board, Player player) {
        int completedColors = countCompletedColors(board, player);
        return completedColors >= (TRIPLE_VICTORY_REQ - 1);
    }

    private boolean isCloseToTourismVictory(Board board, Player player) {
        List<PropertyTile> ownedProps = board.getPropertiesOwnedBy(player);
        long tourismCount = ownedProps.stream().filter(PropertyTile::isTourism).count();
        
        return tourismCount >= (TOURISM_VICTORY_REQ - 1);
    }

    private int countCompletedColors(Board board, Player player) {
        List<PropertyTile> ownedProps = board.getPropertiesOwnedBy(player);
        Map<String, Long> colorCount = ownedProps.stream()
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
}