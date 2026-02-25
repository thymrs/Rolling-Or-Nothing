import java.awt.Color;

/*
 * UIConstants
 * - รวมค่าสี/สไตล์ของ UI ไว้ที่เดียว จะได้ไม่ hardcode กระจาย
 * - ตอนนี้ใช้ใน AutoDismissPopup เป็นหลัก
 */
public class UIConstants {

    // โทนพื้นหลัง (dark UI)
    public static final Color PRIMARY_DARK = new Color(20, 20, 24);

    // สีตัวอักษรหลัก
    public static final Color TEXT_PRIMARY = new Color(235, 235, 245);

    // สี accent ตามประเภทข้อความ
    public static final Color ACCENT_INFO    = new Color(80, 160, 255);
    public static final Color ACCENT_SUCCESS = new Color(80, 200, 120);
    public static final Color ACCENT_WARNING = new Color(255, 190, 80);
    public static final Color ACCENT_DANGER  = new Color(255, 90, 90);

    private UIConstants() {}
}
