import java.util.Scanner;
import java.util.List;
import model.GameState;
import model.enums.TurnPhase;
import model.entities.player.Player;
import model.entities.player.BotPlayer;

public class ConsoleView {
    private Scanner scanner = new Scanner(System.in);
    private GameController controller;

    public void setController(GameControllerTest controller2) {
        this.controller = controller2;
    }

    // เทียบเท่ากับ updateView ของ GUI แต่ใช้ Print แทน
    public void updateView(GameState state) {
        System.out.println("\n==================================");
        System.out.println("รอบที่: " + state.getTurnCount() + " | Phase: " + state.getCurrentPhase());
        System.out.println("ตาของ: " + state.getCurrentPlayer().getName());
        System.out.println("เงิน: $" + state.getCurrentPlayer().getMoney());
        System.out.println("ตำแหน่ง: ช่องที่ " + state.getCurrentPlayer().getPosition());
        System.out.println("==================================");

        // ถ้าไม่ใช่บอท ให้แสดงเมนูให้ผู้เล่นพิมพ์เลือก
        if (!(state.getCurrentPlayer() instanceof BotPlayer)) {
            showMenuAndPrompt(state.getCurrentPhase());
        }
    }

    public void showPopup(String message) {
        System.out.println("\n📢 [ประกาศ]: " + message + "\n");
    }

    // แสดงตัวเลือกตาม Phase ปัจจุบัน
    private void showMenuAndPrompt(TurnPhase phase) {
        System.out.println("โปรดเลือกคำสั่ง:");
        
        if (phase == TurnPhase.READY_TO_ROLL) {
            System.out.println("1. ทอยเต๋า (ROLL)");
            System.out.println("2. ใช้การ์ด (USE_CARD)");
        } else if (phase == TurnPhase.ACTION_REQUIRED) {
            System.out.println("3. ซื้อที่ดิน (BUY)");
            System.out.println("4. จบเทิร์น (END_TURN)");
        } else if (phase == TurnPhase.END_TURN) {
            System.out.println("4. จบเทิร์น (END_TURN)");
        }

        System.out.print(">> พิมพ์ตัวเลข: ");
        String input = scanner.nextLine();

        // แปลงตัวเลขเป็น Command ส่งให้ Controller
        switch (input) {
            case "1": controller.processCommand("ROLL"); break;
            case "2": controller.processCommand("USE_CARD"); break;
            case "3": controller.processCommand("BUY"); break;
            case "4": controller.processCommand("END_TURN"); break;
            default: 
                System.out.println("❌ ไม่เข้าใจคำสั่ง ลองใหม่");
                showMenuAndPrompt(phase); // ให้พิมพ์ใหม่
        }
    }

    // จำลองหน้าต่างเลือกคนตอนใช้การ์ดปล้น
    public Player showSelectTargetDialog(List<Player> opponents) {
        System.out.println("โปรดเลือกเป้าหมาย:");
        for (int i = 0; i < opponents.size(); i++) {
            System.out.println((i + 1) + ". " + opponents.get(i).getName());
        }
        System.out.println("0. ยกเลิก");
        System.out.print(">> พิมพ์ตัวเลขเป้าหมาย: ");
        
        int choice = scanner.nextInt();
        scanner.nextLine(); // clear buffer
        
        if (choice == 0 || choice > opponents.size()) return null;
        return opponents.get(choice - 1);
    }
}