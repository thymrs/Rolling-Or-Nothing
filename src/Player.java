import java.util.*;


public abstract class Player {
    private String name;
    private int money;
    private int position;
    private List<PropertyTile> ownedLands;
    protected boolean isJailed;
    protected int jailTurnCount;
    protected boolean hasShield;
    protected Card heldCard;
    protected boolean isBankrupt;
    protected boolean isTollFree;
    protected int discountRate;

    public Player(String name, int playerMoney){
        this.name = name;
        this.money = playerMoney;
        this.ownedLands = new ArrayList<>();
    }

    public void move(int steps){
        this.position += steps;
    }

    public boolean pay(int amount){
        if(this.money >= amount){
            this.money -= amount;
            return true;
        }
        
        return false;
    }

    public void receiveMoney(int amount){
        this.money += amount;
    }

    public Card swapCard(Card newcard){
        Card oldCard = this.heldCard;
        this.heldCard = newcard;
        return oldCard;
    }

    public Card useCard(){
        Card cardToUse = this.heldCard;
        this.heldCard = null;
        return cardToUse;
    }

    public boolean hasCard(){
        return this.heldCard != null;
    }

    public boolean receiveCard(Card c){
        if(!hasCard()){
            this.heldCard = c;
            return true;
        }

        return false;

    }

    public abstract boolean makeDecision(DecisionType type, PropertyTile tile, GameState state);

    public void setIsJailed(boolean isJailed){
        this.isJailed = isJailed;
    }

    public boolean getIsJailed(){
        return this.isJailed;
    }

    public int getJailTurnCount(){
        return jailTurnCount;
    }

    public void addJailTurnCount(int turn){
        this.jailTurnCount += turn;
    }

    public int getPosition(){
        return this.position;
    }

     public void setPosition(int position){
        this.position = position;
    }

    public void addAsset(PropertyTile tile){
        this.ownedLands.add(tile);
    }

    public void removeAsset(PropertyTile tile){
        this.ownedLands.remove(tile);
    }

    public void setTollFree(boolean setToll){
        this.isTollFree = setToll;
    }

    public int getMoney(){
        return this.money;
    }

    public void setDiscountRate(int rate){
        this.discountRate = rate;
    }

    public boolean getHasShield(){
        return this.hasShield;
    }

    public void setHasShield(boolean status){
        this.hasShield = status;
    }

    public boolean getIsTollFree() {
        return this.isTollFree;
    }

    public int getDiscountRate() {
        return this.discountRate;
    }
    
    public String getName() {
        return this.name; 
    }

    public Card getHeldCard() {
        return heldCard;
    }

    public void setHeldCard(Card heldCard) {
        this.heldCard = heldCard;
    }

    public List<PropertyTile> getOwnedLands(){
        return this.ownedLands;
    }

}
