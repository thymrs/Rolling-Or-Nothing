import java.util.List;

public class VictoryChecker {
    public VictoryType checkWinCondition(GameState state) {
        Player currentPlayer = state.getCurrentPlayer();
        Board board = state.getBoard();
        if (currentPlayer.getMoney() <= 0 && currentPlayer.ownedLands.isEmpty()) {
            return VictoryType.BANKRUPTCY;
        }
        if (checkLineVictory(board, currentPlayer)) {
            return VictoryType.LINE_VICTORY;
        }
        if (checkTourismVictory(board, currentPlayer)) {
            return VictoryType.TOURISM_VICTORY;
        }
        if (checkTripleVictory(board, currentPlayer)) {
            return VictoryType.TRIPLE_VICTORY;
        }
        return VictoryType.NONE;
    }

    private boolean checkLineVictory(Board board, Player player) {
        List<PropertyTile> ownedProps = board.getPropertiesOwnedBy(player);
        return false; 
    }

    private boolean checkTripleVictory(Board board, Player player) {
         List<PropertyTile> ownedProps = board.getPropertiesOwnedBy(player);
        return false;
    }

    private boolean checkTourismVictory(Board board, Player player) {
        List<PropertyTile> ownedProps = board.getPropertiesOwnedBy(player);
        return false;
    }
}