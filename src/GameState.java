
/**
 * Represents the complete state of the game at any moment
 */
import java.util.*;

public class GameState {
    private Board board;
    private List<Player> players;
    private Bank bank;
    private CardDeck deck;
    private Dice dice;
    final private GameConfig config;
    private VictoryChecker victoryChecker;
    private TurnPhase currentPhase;
    private int currentPlayerIndex;
    private int turnCount;
    private boolean isSelectingTile = false;
    private int selectedTileIndex = -1;
    private String selectionMode = ""; // "FESTIVAL" or "TRAVEL"
    

    /**
     * Constructor for GameState
     */
    public GameState() {
        this.currentPlayerIndex = 0;
        this.turnCount = 1;
        this.currentPhase = TurnPhase.READY_TO_ROLL;
        // TODO: Initialize other components (finished)
        this.dice = new Dice();
        this.bank = new Bank();
        this.deck = new CardDeck();
        this.config = new GameConfig.Builder().build();
        this.victoryChecker = new VictoryChecker();

        this.players = new ArrayList<>();
        this.board = null;
    }

    /**
     * Gets the current active player
     * 
     * @return Current Player object
     */
    public Player getCurrentPlayer() {
        return (players == null || players.isEmpty()) ? null : players.get(currentPlayerIndex);
    }
    
    

    // Getters and setters
    public Board getBoard() {
        return board;
    }

    public void setBoard(Board board) {
        this.board = board;
    }

    public List<Player> getPlayers() {
        return players;
    }

    public void setPlayers(List<Player> players) {
        this.players = players;
    }

    public Bank getBank() {
        return bank;
    }

    public void setBank(Bank bank) {
        this.bank = bank;
    }

    public CardDeck getDeck() {
        return deck;
    }

    public void setDeck(CardDeck deck) {
        this.deck = deck;
    }

    public Dice getDice() {
        return dice;
    }

    public void setDice(Dice dice) {
        this.dice = dice;
    }

    public TurnPhase getCurrentPhase() {
        return currentPhase;
    }

    

    public int getTurnCount() {
        return turnCount;
    }

    public GameConfig getConfig() {
        return config;
    }

    public VictoryChecker getVictoryChecker() {
        return victoryChecker;
    }
    
    public boolean isSelectingTile() {
        return isSelectingTile;
    }
    
    public void setSelectingTile(boolean selecting, String mode) {
        this.isSelectingTile = selecting;
        this.selectionMode = mode;
        this.selectedTileIndex = -1;
    }
    
    public int getSelectedTileIndex() {
        return selectedTileIndex;
    }
    
    public void setSelectedTileIndex(int index) {
        this.selectedTileIndex = index;
    }
    
    public String getSelectionMode() {
        return selectionMode;
    }
    private List<GameEventListener> listeners = new ArrayList<>();

    public void addGameEventListener(GameEventListener listener) {
        listeners.add(listener);
    }

    public void setCurrentPhase(TurnPhase phase) {
        this.currentPhase = phase;
        if (players != null && !players.isEmpty()) {
            String playerName = getCurrentPlayer().getName();
            for (GameEventListener listener : listeners) {
                listener.onPhaseChanged(playerName, phase);
            }
        }
    }

    // แก้ไข incrementTurn ให้ใช้ setCurrentPhase เพื่อให้เกิด Log
    /**
     * Advances to the next player's turn
     */
    public void incrementTurn() {
        currentPlayerIndex = (currentPlayerIndex + 1) % players.size();
        if (currentPlayerIndex == 0) {
            turnCount++;
        }
        setCurrentPhase(TurnPhase.READY_TO_ROLL);
    }
    
    // เมธอดสำหรับส่งข้อความทั่วไปเข้า Log
    public void notifyMessage(String message) {
        for (GameEventListener listener : listeners) {
            listener.onGameMessage(message);
        }
    }
}