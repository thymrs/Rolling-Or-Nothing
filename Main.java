import enums.CardType;
import src.*;

public class Main {
    public static void main(String[] args) {
        Card c1 = new Card(CardType.ANGEL, 100);
        System.out.println(c1);
    }
}
