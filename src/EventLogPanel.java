import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.border.CompoundBorder; // เพิ่มบรรทัดนี้เพื่อแก้ปัญหาตัวแดง
import java.awt.*;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public class EventLogPanel extends JPanel {
    private JTextArea logArea;
    private JScrollPane scrollPane;
    private DateTimeFormatter timeFormatter;

    public EventLogPanel() {
        // ตั้งค่าขนาดและสีพื้นหลังของ Panel
        setPreferredSize(new Dimension(280, 0));
        setBackground(new Color(30, 35, 45));
        setLayout(new BorderLayout());
        timeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss");

        // สร้างพื้นที่สำหรับแสดงข้อความ Log
        logArea = new JTextArea();
        logArea.setEditable(false);
        logArea.setBackground(new Color(20, 25, 30));
        logArea.setForeground(new Color(200, 200, 200));
        logArea.setFont(new Font("Monospaced", Font.PLAIN, 13));
        logArea.setLineWrap(true);
        logArea.setWrapStyleWord(true);
        
        // ใส่ ScrollPane เพื่อให้เลื่อนดู Log ย้อนหลังได้
        scrollPane = new JScrollPane(logArea);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        
        // สร้างขอบที่มีชื่อกำกับ (TitledBorder)
        TitledBorder titledBorder = BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(100, 100, 100)), "Game Events");
        titledBorder.setTitleColor(new Color(255, 215, 0)); // สีเหลืองทอง
        
        // ใช้ CompoundBorder เพื่อรวม EmptyBorder (เป็น Padding) เข้ากับ TitledBorder
        // ขอบนอกเป็นช่องว่าง 10px, ขอบในเป็นเส้นที่มีชื่อ
        setBorder(new CompoundBorder(new EmptyBorder(10, 10, 10, 10), titledBorder));
        
        add(scrollPane, BorderLayout.CENTER);
    }

    /**
     * เพิ่มข้อความใหม่ลงใน Log พร้อมระบุเวลา
     */
    public void addLog(String message) {
        String time = LocalTime.now().format(timeFormatter);
        logArea.append("[" + time + "] " + message + "\n");
        // เลื่อนหน้าจอลงไปล่างสุดอัตโนมัติเพื่อให้เห็นข้อความล่าสุด
        logArea.setCaretPosition(logArea.getDocument().getLength());
    }

    /**
     * จัดรูปแบบข้อความ Log ตามสถานะของเกม (Phase)
     */
    public void logPhaseChange(String playerName, TurnPhase phase) {
        if (phase == null) return;
        
        switch (phase) {
            case READY_TO_ROLL -> addLog(">> [" + playerName + "] It's your turn! Roll the dice.");
            case MOVING -> addLog(".. [" + playerName + "] is moving...");
            case ACTION_REQUIRED -> addLog("!! [" + playerName + "] Action required!");
            case END_TURN -> addLog("-- [" + playerName + "] finished turn.");
            case GAME_OVER -> addLog("========== GAME OVER ==========");
            default -> addLog("SYSTEM: Phase changed to " + phase);
        }
    }
}