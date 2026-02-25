import javax.swing.*;
import java.awt.*;

/**
 * Utility for auto-dismissing popups (non-blocking)
 * Replaces manual JOptionPane.showMessageDialog() calls
 */
public class AutoDismissPopup {
    
    /**
     * Show auto-dismissing info popup (1.5 seconds)
     */
    public static void showInfo(JFrame parent, String title, String message) {
        showPopup(parent, title, message, 1500, UIConstants.ACCENT_INFO);
    }
    
    /**
     * Show auto-dismissing success popup (1.5 seconds)
     */
    public static void showSuccess(JFrame parent, String title, String message) {
        showPopup(parent, title, message, 1500, UIConstants.ACCENT_SUCCESS);
    }
    
    /**
     * Show auto-dismissing error popup (2.5 seconds)
     */
    public static void showError(JFrame parent, String title, String message) {
        showPopup(parent, title, message, 2500, UIConstants.ACCENT_DANGER);
    }
    
    /**
     * Show auto-dismissing warning popup (2 seconds)
     */
    public static void showWarning(JFrame parent, String title, String message) {
        showPopup(parent, title, message, 2000, UIConstants.ACCENT_WARNING);
    }
    
    /**
     * Generic auto-dismiss popup with custom timing
     */
    private static void showPopup(JFrame parent, String title, String message, int delayMs, Color accentColor) {
        // Create dialog
        JDialog dialog = new JDialog(parent, title, false);
        dialog.setLayout(new BoxLayout(dialog.getContentPane(), BoxLayout.Y_AXIS));
        dialog.getContentPane().setBackground(UIConstants.PRIMARY_DARK);
        
        // Message label
        JLabel messageLabel = new JLabel("<html>" + message + "</html>");
        messageLabel.setFont(new Font("SansSerif", Font.PLAIN, 14));
        messageLabel.setForeground(UIConstants.TEXT_PRIMARY);
        messageLabel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        dialog.add(messageLabel);
        
        // Color border for visual feedback
        dialog.getRootPane().setBorder(BorderFactory.createLineBorder(accentColor, 3));
        
        dialog.setSize(400, 150);
        dialog.setLocationRelativeTo(parent);
        dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        dialog.setVisible(true);
        
        // Auto-dismiss timer
        Timer dismissTimer = new Timer(delayMs, e -> dialog.dispose());
        dismissTimer.setRepeats(false);
        dismissTimer.start();
    }
}
