/*
 * Main entry point for the Monopoly game
 */

import java.util.jar.Attributes.Name;

public class Main {

    /**
     * Main method to start the game
     * 
     * @param args Command line arguments
     */
    public static void main(String[] args) {

        GameConfig config = new GameConfig.Builder()
                .initialMoney(35000)
                .maxTurns(50)
                .mapName("default")
                .humanCount(1)
                .botCount(2)
                .botDifficulty(DifficultyLevel.HARD)
                .passGoSalary(5000)
                .build();

        MapLoader loader = new MapLoader();

        Board board = loader.loadMap(config.getMapName());
        NameLoader nameLoader = new NameLoader("NameBot.csv");
        
        GameWindow view = new GameWindow();
        GameState state = new GameState();
        state.setBoard(board);
        state.addGameEventListener(GameWindow.getEventLogPanel());

        GameController controller = new GameController(view, state); 
        
        view.setVisible(true);
        controller.startGame(config);

        System.out.println("Monopoly Game Started!");

    }
}

// public class Main {
// public static void main(String[] args) {
// GameConfig config = new GameConfig.Builder()
// .initialMoney(1500)
// .mapName("default")
// .humanCount(1)
// .botCount(1) // แนะนำให้ลองบอท 1 ตัวก่อนตอนรันทดสอบ
// .build();

// // 1. สร้าง View แบบ Console
// ConsoleView view = new ConsoleView();

// // 2. ส่ง View ให้ Controller
// GameControllerTest controller = new GameControllerTest(view);

// // 3. ผูก Controller กลับไปให้ View ด้วย (เพื่อให้ View สั่งงานได้)
// view.setController(controller);

// System.out.println("🚀 Monopoly Game (Terminal Mode) Started!\n");

// // 4. เริ่มเกม!
// controller.startGame(config);
// }
// }