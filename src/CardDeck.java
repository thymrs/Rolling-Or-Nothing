package src.model.entities.card;
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
        // TODO: สร้างการ์ด 20-30 ใบ ใส่ drawPile
        // drawPile.add(new Card(CardType.ANGEL));
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
        // TODO: เอา discardPile กลับไปใส่ drawPile แล้ว shuffle
    }
    
    public void shuffle() {
        Collections.shuffle((List<?>) drawPile);
    }
}