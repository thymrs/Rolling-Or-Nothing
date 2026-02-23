import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public class EventLogPanel extends JPanel {

    private JTextArea logArea;
    private JScrollPane scrollPane;
    private DateTimeFormatter timeFormatter;

    public EventLogPanel() {
        // กำหนดขนาดฝั่งขวาให้สมดุลกับฝั่งซ้าย (กว้าง 280 เท่ากัน)
        setPreferredSize(new Dimension(280, 0));
        setBackground(new Color(30, 35, 45));
        setLayout(new BorderLayout());

        // กำหนดรูปแบบเวลาที่แสดง (เช่น 10:30:15)
        timeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss");

        // สร้างพื้นที่แสดงข้อความ (JTextArea)
        logArea = new JTextArea();
        logArea.setEditable(false);
        logArea.setBackground(new Color(20, 25, 30)); // สีพื้นหลังช่องแชทจะเข้มกว่าขอบ
        logArea.setForeground(new Color(200, 200, 200)); // สีข้อความเทาอ่อน
        logArea.setFont(new Font("SansSerif", Font.PLAIN, 14));
        logArea.setLineWrap(true);
        logArea.setWrapStyleWord(true);
        logArea.setMargin(new Insets(10, 10, 10, 10));

        // ใส่ ScrollPane ให้เลื่อนดูประวัติได้
        scrollPane = new JScrollPane(logArea);
        scrollPane.setBorder(BorderFactory.createEmptyBorder()); // เอาขอบที่ดูเกะกะออก
        scrollPane.getVerticalScrollBar().setPreferredSize(new Dimension(8, 0)); // ทำ Scrollbar ให้บางๆ

        // สร้างขอบและหัวข้อ "Event Log" สไตล์ Dark Theme
        TitledBorder titledBorder = BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(100, 100, 100)), "Event Log");
        titledBorder.setTitleColor(new Color(255, 215, 0)); // หัวข้อสีทอง
        titledBorder.setTitleFont(new Font("SansSerif", Font.BOLD, 14));
        
        JPanel wrapperPanel = new JPanel(new BorderLayout());
        wrapperPanel.setBackground(new Color(30, 35, 45));
        wrapperPanel.setBorder(BorderFactory.createCompoundBorder(
                new EmptyBorder(10, 10, 10, 10), // ขอบนอก
                titledBorder                     // ขอบในที่มีชื่อ
        ));
        
        wrapperPanel.add(scrollPane, BorderLayout.CENTER);
        add(wrapperPanel, BorderLayout.CENTER);

        // ข้อความต้อนรับ
        addLog("GAME START! GGHF");
    }

    // เมธอดหลักที่ Controller/GameWindow จะเรียกใช้เพื่อเพิ่มข้อความใหม่
    public void addLog(String message) {
        String time = LocalTime.now().format(timeFormatter);
        // เพิ่มข้อความพร้อมเวลา [เวลา] ข้อความ
        logArea.append("[" + time + "] " + message + "\n\n");
        
        // เลื่อน Scrollbar ลงมาล่างสุดอัตโนมัติ
        logArea.setCaretPosition(logArea.getDocument().getLength());
    }
}