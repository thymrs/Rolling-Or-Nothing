import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

class GameDialogManager {

    // สีหลักสำหรับ Theme
    private static final Color BG_COLOR = new Color(40, 45, 55);
    private static final Color PANEL_COLOR = new Color(30, 35, 45);
    private static final Color TEXT_COLOR = Color.WHITE;
    private static final Color YES_COLOR = new Color(80, 200, 120);
    private static final Color NO_COLOR = new Color(255, 100, 100);
    private static final Color DISABLED_COLOR = new Color(100, 100, 100);

    // =========================================================================
    // โครงสร้างหลักของ Dialog (Base Dialog)
    // =========================================================================
    private static JDialog createBaseDialog(JFrame parent, String title, JPanel contentPanel, ActionListener onYes, ActionListener onNo) {
        JDialog dialog = new JDialog(parent, title, true); // true = Modal (บล็อกหน้าต่างอื่นจนกว่าจะปิด)
        dialog.setLayout(new BorderLayout());
        dialog.getContentPane().setBackground(BG_COLOR);

        // ใส่ Content (ส่วนเนื้อหาที่ต่างกันไปในแต่ละประเภท)
        contentPanel.setBackground(BG_COLOR);
        contentPanel.setBorder(new EmptyBorder(20, 20, 20, 20));
        dialog.add(contentPanel, BorderLayout.CENTER);

        // สร้างแผงปุ่ม YES / NO ด้านล่าง
        JPanel buttonPanel = new JPanel(new GridLayout(1, 2, 10, 0));
        buttonPanel.setBackground(BG_COLOR);
        buttonPanel.setBorder(new EmptyBorder(10, 20, 20, 20));

        JButton btnYes = styleButton(new JButton("YES"), YES_COLOR);
        JButton btnNo = styleButton(new JButton("NO"), NO_COLOR);

        btnYes.addActionListener(e -> {
            if (onYes != null) onYes.actionPerformed(e);
            dialog.dispose();
        });

        btnNo.addActionListener(e -> {
            if (onNo != null) onNo.actionPerformed(e);
            dialog.dispose();
        });

        buttonPanel.add(btnYes);
        buttonPanel.add(btnNo);
        dialog.add(buttonPanel, BorderLayout.SOUTH);

        return dialog;
    }

    // =========================================================================
    // 1. Dialog ซื้อ/อัปเกรดบ้าน (เลือกได้ 1 อย่าง)
    // =========================================================================
    public static int showBuyPropertyDialog(JFrame parent, PropertyTile tile) {
        final int[] selectedLevel = {-1}; // -1 คือยกเลิก/ไม่ซื้อ

        JPanel panel = new JPanel(new GridLayout(1, 4, 10, 0));
        ButtonGroup group = new ButtonGroup();

        String[] labels = {"Lands", "House", "Apartment", "Mansion"};
        int currentLevel = tile.getBuildingLevel(); // สมมติ 0=ว่าง, 1=ที่ดิน, 2=บ้าน1 ...

        for (int i = 0; i < 4; i++) {
            JToggleButton btn = new JToggleButton(labels[i]);
            btn.setFont(new Font("SansSerif", Font.BOLD, 14));
            btn.setFocusPainted(false);
            btn.setBackground(PANEL_COLOR);
            btn.setForeground(TEXT_COLOR);

            // เงื่อนไข: ถ้าระดับปัจจุบันมากกว่าหรือเท่ากับปุ่มนี้ แปลว่าซื้อไปแล้ว ให้เป็นสีเทาและกดไม่ได้
            if (currentLevel > i) {
                btn.setEnabled(false);
                btn.setBackground(DISABLED_COLOR);
                btn.setText(labels[i] + " (Owned)");
            } else {
                final int levelValue = i + 1;
                btn.addActionListener(e -> selectedLevel[0] = levelValue);
                group.add(btn);
            }
            panel.add(btn);
        }

        JDialog dialog = createBaseDialog(parent, "Upgrade property: " + tile.getName(), panel, 
            e -> {}, // Yes = ปล่อยให้คืนค่า selectedLevel
            e -> selectedLevel[0] = -1 // No = ยกเลิก
        );

        dialog.setSize(500, 200);
        dialog.setLocationRelativeTo(parent);
        dialog.setVisible(true);

        return selectedLevel[0];
    }

    // =========================================================================
    // 2. Dialog การ์ดป้องกัน (ถามแค่ YES/NO)
    // =========================================================================
    public static boolean showDefenseCardDialog(JFrame parent, Card card) {
        final boolean[] result = {false};

        JPanel panel = new JPanel(new BorderLayout());
        JLabel label = new JLabel("Do you want to use Shield [" + card.getTypeCard() + "] or not?", SwingConstants.CENTER);
        label.setFont(new Font("SansSerif", Font.BOLD, 16));
        label.setForeground(TEXT_COLOR);
        panel.add(label, BorderLayout.CENTER);

        JDialog dialog = createBaseDialog(parent, "Use Shield!", panel,
            e -> result[0] = true,
            e -> result[0] = false
        );

        dialog.setSize(400, 180);
        dialog.setLocationRelativeTo(parent);
        dialog.setVisible(true);

        return result[0];
    }

    // =========================================================================
    // 3. Dialog การ์ดโจมตี (เลือกเป้าหมาย)
    // =========================================================================
    public static Player showAttackCardDialog(JFrame parent, Card card, List<Player> opponents) {
        final Player[] target = {null};

        JPanel panel = new JPanel(new BorderLayout(0, 15));
        
        JLabel label = new JLabel("Select Target [" + card.getTypeCard() + "]:", SwingConstants.CENTER);
        label.setFont(new Font("SansSerif", Font.BOLD, 14));
        label.setForeground(TEXT_COLOR);
        panel.add(label, BorderLayout.NORTH);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 0));
        btnPanel.setBackground(BG_COLOR);
        ButtonGroup group = new ButtonGroup();

        for (Player opp : opponents) {
            JToggleButton btn = new JToggleButton(opp.getName());
            btn.setPreferredSize(new Dimension(100, 40));
            btn.setFont(new Font("SansSerif", Font.BOLD, 14));
            btn.setBackground(PANEL_COLOR);
            btn.setForeground(TEXT_COLOR);
            btn.setFocusPainted(false);
            
            btn.addActionListener(e -> target[0] = opp);
            group.add(btn);
            btnPanel.add(btn);
        }
        panel.add(btnPanel, BorderLayout.CENTER);

        JDialog dialog = createBaseDialog(parent, "Attack others player", panel,
            e -> {
                if (target[0] == null) {
                    JOptionPane.showMessageDialog(parent, "Please select target!", "Warning", JOptionPane.WARNING_MESSAGE);
                }
            },
            e -> target[0] = null // Cancel
        );

        dialog.setSize(450, 220);
        dialog.setLocationRelativeTo(parent);
        dialog.setVisible(true);

        return target[0];
    }

    // =========================================================================
    // 4. Dialog ขายบ้าน (เลือกหลายรายการ Checkbox)
    // =========================================================================
    public static List<PropertyTile> showSellPropertyDialog(JFrame parent, List<PropertyTile> ownedProperties) {
        List<PropertyTile> selectedToSell = new ArrayList<>();

        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        
        JLabel label = new JLabel("Select your own property (Bankrupt):");
        label.setFont(new Font("SansSerif", Font.BOLD, 16));
        label.setForeground(NO_COLOR);
        panel.add(label);
        panel.add(Box.createVerticalStrut(10));

        List<JCheckBox> checkBoxes = new ArrayList<>();
        
        for (PropertyTile prop : ownedProperties) {
            JCheckBox cb = new JCheckBox(prop.getName() + " (Sell Price: " + (prop.getPurchasePrice() / 2) + ")");
            cb.setFont(new Font("SansSerif", Font.PLAIN, 14));
            cb.setForeground(TEXT_COLOR);
            cb.setBackground(BG_COLOR);
            cb.setFocusPainted(false);
            checkBoxes.add(cb);
            panel.add(cb);
        }

        // ใส่ ScrollPane เผื่อมีบ้านเยอะ
        JScrollPane scrollPane = new JScrollPane(panel);
        scrollPane.setBorder(null);
        scrollPane.getViewport().setBackground(BG_COLOR);

        JPanel wrapperPanel = new JPanel(new BorderLayout());
        wrapperPanel.add(scrollPane, BorderLayout.CENTER);

        JDialog dialog = createBaseDialog(parent, "Sell Property", wrapperPanel,
            e -> {
                for (int i = 0; i < checkBoxes.size(); i++) {
                    if (checkBoxes.get(i).isSelected()) {
                        selectedToSell.add(ownedProperties.get(i));
                    }
                }
            },
            e -> selectedToSell.clear() // Cancel
        );

        dialog.setSize(400, 300);
        dialog.setLocationRelativeTo(parent);
        dialog.setVisible(true);

        return selectedToSell;
    }

    // Helper Method สำหรับตกแต่งปุ่ม YES/NO
    private static JButton styleButton(JButton btn, Color bgColor) {
        btn.setBackground(bgColor);
        btn.setForeground(Color.WHITE);
        btn.setFont(new Font("SansSerif", Font.BOLD, 14));
        btn.setFocusPainted(false);
        btn.setBorder(new EmptyBorder(10, 10, 10, 10));
        return btn;
    }

    

// =========================================================================
    // 5. Dialog ยืนยันการยอมแพ้ (ถามแค่ YES/NO)
    // =========================================================================
    public static boolean showSurrenderDialog(JFrame parent) {
        final boolean[] result = {false};

        JPanel panel = new JPanel(new BorderLayout());
        JLabel label = new JLabel("Are you sure you want to 'surrender' and leave the game?", SwingConstants.CENTER);
        label.setFont(new Font("SansSerif", Font.BOLD, 16));
        label.setForeground(new Color(255, 100, 100)); // ใช้สีแดงเตือน
        panel.add(label, BorderLayout.CENTER);

        // ใช้ createBaseDialog (เมธอดหลักของคลาสที่เราทำไว้) 
        JDialog dialog = createBaseDialog(parent, "Confirming on Surrender.", panel,
            e -> result[0] = true,  // กด YES คืนค่า true
            e -> result[0] = false  // กด NO คืนค่า false
        );

        dialog.setSize(400, 150);
        dialog.setLocationRelativeTo(parent);
        dialog.setVisible(true);

        return result[0];
    }
}