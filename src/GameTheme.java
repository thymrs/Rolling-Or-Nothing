import javax.swing.*;
import java.awt.*;
import java.io.File;

public class GameTheme {
    // --- Constants: Colors ---
    public static final Color BG_COLOR = new Color(39, 119, 20); // เขียวสักหลาด
    public static final Color WOOD_COLOR = new Color(233, 198, 150); // ไม้ครีม
    public static final Color WOOD_BORDER = new Color(139, 90, 43); // ขอบไม้เข้ม
    public static final Color TEXT_COLOR = Color.WHITE;
    public static final Color GOLD_COLOR = new Color(255, 215, 0); // สีทอง
    public static final Color ACTIVE_MENU_COLOR = new Color(255, 240, 200); // สีปุ่มตอนเลือก

    // --- Fonts ---
    public static String THAI_FONT = "Mali"; 

    public static void setupGameFont() {
        try {
            // ตรวจสอบ Path ไฟล์ฟอนต์ (ต้องสร้างโฟลเดอร์ assets ใน src และวางไฟล์ Mali-Bold.ttf)
            // ถ้าไม่เจอไฟล์ จะ fallback ไปใช้ Tahoma อัตโนมัติ
            File fontFile = new File("src/assets/Mali-Bold.ttf"); 
            
            if (fontFile.exists()) {
                Font gameFont = Font.createFont(Font.TRUETYPE_FONT, fontFile).deriveFont(16f);
                GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
                ge.registerFont(gameFont);
                THAI_FONT = gameFont.getFamily(); 
                updateUIManager(gameFont);
                System.out.println("Font Loaded Successfully: " + THAI_FONT);
            } else {
                System.err.println("Font file not found at: " + fontFile.getAbsolutePath());
                useFallbackFont();
            }
        } catch (Exception e) {
            System.err.println("Font Load Failed: " + e.getMessage());
            useFallbackFont();
        }
    }

    private static void useFallbackFont() {
        THAI_FONT = "Tahoma"; // Fallback Font ที่อ่านไทยได้แน่นอน
        Font fallback = new Font("Tahoma", Font.PLAIN, 14);
        updateUIManager(fallback);
        System.out.println("Using Fallback Font: " + THAI_FONT);
    }

    private static void updateUIManager(Font font) {
        java.util.Enumeration<Object> keys = UIManager.getDefaults().keys();
        while (keys.hasMoreElements()) {
            Object key = keys.nextElement();
            Object value = UIManager.get(key);
            if (value instanceof javax.swing.plaf.FontUIResource) {
                UIManager.put(key, font);
            }
        }
    }
}