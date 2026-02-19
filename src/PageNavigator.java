/**
 * Interface สำหรับการนำทางระหว่างหน้าจอ
 * ช่วยให้ Panel ย่อย (เช่น Setup, MainMenu) สามารถสั่งเปลี่ยนหน้าได้
 * โดยไม่ต้องรู้จักคลาส GameMenuSystem หรือ GameWindow โดยตรง (Decoupling)
 */
public interface PageNavigator {
    // สั่งเปลี่ยนไปหน้าจอตามชื่อ (เช่น "Menu", "Setup", "HowToPlay")
    void navigateTo(String pageName);

    // สั่งเริ่มเกมพร้อมส่งค่า Config (จำนวนคน, บอท, ความยาก)
    void startGame(int human, int bot, String diff);
}