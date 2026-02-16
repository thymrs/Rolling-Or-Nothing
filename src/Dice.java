package RollingOrNothing.src.model.entities.board;

package model.items;

import java.util.Random;

/**
 * Represents game dice for rolling
 */
public class Dice {
    private Random random;
    private int die1;
    private int die2;
    
    /**
     * Constructor for Dice
     */
    public Dice() {
        this.random = new Random();
        this.die1 = 1;
        this.die2 = 1;
    }
    
    /**
     * Rolls both dice and returns the sum
     * @return Sum of both dice (2-12)
     */
    public int roll() {
        die1 = random.nextInt(6) + 1; // 1-6
        die2 = random.nextInt(6) + 1; // 1-6
        return die1 + die2;
    }
    
    /**
     * Checks if the last roll was a double
     * @return true if both dice show the same number
     */
    public boolean isDouble() {
        return die1 == die2;
    }
    
    /**
     * Gets the value of die 1
     * @return Die 1 value
     */
    public int getDie1() {
        return die1;
    }
    
    /**
     * Gets the value of die 2
     * @return Die 2 value
     */
    public int getDie2() {
        return die2;
    }
    
    /**
     * Gets the sum of the last roll
     * @return Sum of both dice
     */
    public int getTotal() {
        return die1 + die2;
    }
}