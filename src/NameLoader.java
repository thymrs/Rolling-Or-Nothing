import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.nio.charset.StandardCharsets;

public class NameLoader {
    private List<String> names;
    private Random random;

    public NameLoader(String fileName) {
        names = new ArrayList<>();
        random = new Random();
        loadNames(fileName);
    }

    private void loadNames(String fileName) {
        // ใช้ getResourceAsStream หากไฟล์อยู่ในโฟลเดอร์ resources หรือ src
        try (BufferedReader br = new BufferedReader(
                new InputStreamReader(getClass().getResourceAsStream("/" + fileName), StandardCharsets.UTF_8))) {

            String line;
            while ((line = br.readLine()) != null) {
                if (!line.trim().isEmpty()) {
                    // สมมติว่าใน CSV มีชื่อ 1 ชื่อต่อ 1 บรรทัด
                    names.add(line.trim());
                }
            }
        } catch (Exception e) {
            System.err.println("Error loading names: " + e.getMessage());
            // ใส่ชื่อสำรองไว้กันโปรแกรมค้าง
            names.add("Player 1");
        }
    }

    public String getRandomName() {
        if (names.isEmpty())
            return "Unknown";
        return names.get(random.nextInt(names.size()));
    }

    public String getRandomUniqueName() {
        // ถ้าชื่อในไฟล์หมด หรือหาไฟล์ไม่เจอ ให้ใช้ชื่อ Bot ตามด้วยเลขสุ่มแทน
        if (names.isEmpty()) {
            return "Bot " + (random.nextInt(99) + 1);
        }
        // สุ่มตำแหน่ง index, ดึงชื่อออกมา, และลบออกจาก List เพื่อไม่ให้ซ้ำ
        int index = random.nextInt(names.size());
        return names.remove(index);
    }
}
