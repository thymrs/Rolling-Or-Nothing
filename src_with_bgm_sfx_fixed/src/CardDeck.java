import java.util.*;

public class CardDeck {
    private Queue<Card> drawPile;
    private List<Card> discardPile;

    public CardDeck() {
        this.drawPile = new LinkedList<>();
        this.discardPile = new ArrayList<>();
        initializeCards();
    }

    private void initializeCards() {
        for (CardType type : CardType.values()) {
            for (int i = 0; i < 5; i++) {
                int value = 0;
                if (type == CardType.REWARD) value = 15000;
                if (type == CardType.DISCOUNT) value = 50;
                
                drawPile.add(new Card(type, value));
            }
        }
        shuffle();
    }

    public Card draw() {
        if (drawPile.isEmpty()) {
            recycleDiscards();
        }
        return drawPile.poll();
    }

    public void discard(Card card) {
        discardPile.add(card);
    }
    
    private void recycleDiscards() {
        if (!discardPile.isEmpty()) {
            drawPile.addAll(discardPile);
            discardPile.clear();
            shuffle();
            System.out.println("Out of card! Shuffling...");
        }
    }
    
    public void shuffle() {
        Collections.shuffle((List<?>) drawPile);
    }
}