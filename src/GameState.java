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
    private GameConfig config;
    private TurnPhase currentPhase;
    private int currentPlayerIndex;
    private int turnCount;
    
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
        this.config = new GameConfig(Builder());

        this.players = new ArrayList<>();
        this.board = null;
    }
    
    /**
     * Gets the current active player
     * @return Current Player object
     */
    public Player getCurrentPlayer() {
        return players.get(currentPlayerIndex);
    }
    
    /**
     * Advances to the next player's turn
     */
    public void incrementTurn() {
        currentPlayerIndex = (currentPlayerIndex + 1) % players.size();
        if (currentPlayerIndex == 0) {
            turnCount++;
        }
        currentPhase = TurnPhase.READY_TO_ROLL;
    }
    
    // Getters and setters
    public Board getBoard() { return board; }
    public void setBoard(Board board) { this.board = board; }
    
    public List<Player> getPlayers() { return players; }
    public void setPlayers(List<Player> players) { this.players = players; }
    
    public Bank getBank() { return bank; }
    public void setBank(Bank bank) { this.bank = bank; }
    
    public CardDeck getDeck() { return deck; }
    public void setDeck(CardDeck deck) { this.deck = deck; }
    
    public Dice getDice() { return dice; }
    public void setDice(Dice dice) { this.dice = dice; }
    
    public TurnPhase getCurrentPhase() { return currentPhase; }
    public void setCurrentPhase(TurnPhase phase) { this.currentPhase = phase; }
    
    public int getTurnCount() { return turnCount; }

    public GameConfig getConfig() { return config; }
}