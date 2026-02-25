import java.awt.*;

/**
 * Global unified color palette for the entire application
 * Ensures consistent theming across all screens
 */
public class UIConstants {
    // Primary Colors
    public static final Color PRIMARY_DARK = new Color(40, 44, 52);
    public static final Color PRIMARY_LIGHT = new Color(30, 35, 45);
    
    // Accent Colors
    public static final Color ACCENT_SUCCESS = new Color(80, 200, 120);    // Green
    public static final Color ACCENT_DANGER = new Color(255, 100, 100);   // Red
    public static final Color ACCENT_WARNING = new Color(255, 180, 50);   // Orange
    public static final Color ACCENT_INFO = new Color(100, 150, 200);     // Blue
    
    // Text Colors
    public static final Color TEXT_PRIMARY = Color.WHITE;
    public static final Color TEXT_SECONDARY = new Color(200, 200, 200);
    public static final Color TEXT_MUTED = new Color(120, 120, 120);
    
    // Background Colors
    public static final Color BG_MAIN = new Color(40, 45, 55);
    public static final Color BG_PANEL = new Color(30, 35, 45);
    public static final Color BG_HEADER = new Color(20, 25, 35);
    
    // Player Colors
    public static final Color[] PLAYER_COLORS = {
        new Color(255, 50, 50),      // Player 1 (Red)
        new Color(50, 255, 50),      // Player 2 (Green)
        new Color(255, 215, 0),      // Player 3 (Gold)
        new Color(50, 200, 255)      // Player 4 (Blue)
    };
    
    // Status Colors
    public static final Color STATUS_ACTIVE = ACCENT_SUCCESS;
    public static final Color STATUS_INACTIVE = TEXT_MUTED;
    public static final Color STATUS_BANKRUPT = ACCENT_DANGER;
    public static final Color STATUS_JAIL = ACCENT_WARNING;
    
    // Tile Selection
    public static final Color TILE_SELECTION_HIGHLIGHT = new Color(150, 200, 255);
    public static final Color TILE_CORNER = new Color(255, 120, 120);
    public static final Color TILE_NORMAL = new Color(220, 220, 220);
}
