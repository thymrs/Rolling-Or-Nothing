import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.border.CompoundBorder;
import java.awt.*;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

// เพิ่ม implements GameEventListener
public class EventLogPanel extends JPanel implements GameEventListener {
    private JTextArea logArea;
    private JScrollPane scrollPane;
    private DateTimeFormatter timeFormatter;

    public EventLogPanel() {
        setPreferredSize(new Dimension(280, 0));
        setBackground(new Color(30, 35, 45));
        setLayout(new BorderLayout());
        timeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss");

        logArea = new JTextArea();
        logArea.setEditable(false);
        logArea.setBackground(new Color(20, 25, 30));
        logArea.setForeground(new Color(200, 200, 200));
        logArea.setFont(new Font("Monospaced", Font.PLAIN, 13));
        logArea.setLineWrap(true);
        logArea.setWrapStyleWord(true);
        
        scrollPane = new JScrollPane(logArea);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        
        TitledBorder titledBorder = BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(100, 100, 100)), "Game Events");
        titledBorder.setTitleColor(new Color(255, 215, 0)); 
        
        setBorder(new CompoundBorder(new EmptyBorder(10, 10, 10, 10), titledBorder));
        add(scrollPane, BorderLayout.CENTER);
    }

    public void addLog(String message) {
    SwingUtilities.invokeLater(() -> {
        logArea.append(message + "\n");
        logArea.setCaretPosition(logArea.getDocument().getLength());
    });
}

    // ----- Implement Methods จาก GameEventListener -----
    
    @Override
    public void onPhaseChanged(String playerName, TurnPhase newPhase) {
        if (newPhase == null) return;
        
        switch (newPhase) {
            case READY_TO_ROLL -> addLog(">> [" + playerName + "] It's your turn! Roll the dice.");
            case MOVING -> addLog(".. [" + playerName + "] is moving...");
            case ACTION_REQUIRED -> addLog("!! [" + playerName + "] Action required!");
            case END_TURN -> addLog("-- [" + playerName + "] plse end your turn. you have nothing to do.");
            case GAME_OVER -> addLog("========== GAME OVER ==========");
            default -> addLog("SYSTEM: Phase changed to " + newPhase);
        }
    }

    @Override
    public void onGameMessage(String message) {
        // เมื่อมีการเรียก state.notifyMessage() ให้ดึงข้อความมาแสดงที่นี่
        addLog(message); 
    }
}