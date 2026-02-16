/**
 * Main entry point for the Monopoly game
 */
public class Main {
    
    /**
     * Main method to start the game
     * @param args Command line arguments
     */
    public static void main(String[] args) {
        // Create game configuration
        GameConfig config = new GameConfig.Builder()
                .initialMoney(1500)
                .maxTurns(100)
                .mapName("default")
                .humanCount(1)
                .botCount(3)
                .botDifficulty(DifficultyLevel.NORMAL)
                .passGoSalary(200)
                .taxPercentage(10)
                .build();
        
        // Initialize and start game
        GameController controller = new GameController();
        controller.startGame(config);
        
        System.out.println("Monopoly Game Started!");
    }
}
