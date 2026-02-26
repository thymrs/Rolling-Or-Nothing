
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.EnumMap;
import java.util.Map;
import java.util.regex.Pattern;
import javax.sound.sampled.*;

/*
 * AudioManager
 * - แยกดูแลเสียง BGM / SFX ออกจาก Game logic
 * - ฟัง event จาก GameState ผ่าน GameEventListener
 *
 * จุดที่ต้องต่อ (hook):
 * - Main.java : state.addGameEventListener(new AudioManager());
 *
 * แหล่ง event ที่เกมยิงอยู่แล้ว:
 * - GameState.setCurrentPhase(...) -> onPhaseChanged(...)
 * - GameState.notifyMessage(...)   -> onGameMessage(...)
 *
 * หมายเหตุ:
 * - ตอนนี้เราใช้ "ข้อความ" (notifyMessage) เป็น trigger หลักสำหรับ SFX เพื่อแก้โค้ดเดิมให้น้อยที่สุด
 * - ถ้าอนาคตอยากชัวร์/สะอาด ให้เรียกเมธอด onDiceRolled(), onCardUse() ฯลฯ ตรง ๆ จาก controller/tile
 */
public class AudioManager implements GameEventListener {

    /*
     * โครงไฟล์เสียงแนะนำ (resources):
     * src/main/resources/audio/bgm/...
     * src/main/resources/audio/sfx/...
    *
    * ใช้ wav จะตรงกับ javax.sound.sampled ง่ายสุด
    */
    private static AudioManager INSTANCE;
    public AudioManager() {
        INSTANCE = this;
    }
    public static AudioManager getInstance() {
        return INSTANCE;
    }
    public enum BgmTrack {
        GAMEPLAY("audio/bgm/main.wav", true),
        MAIN("audio/bgm/gameplay.wav", true),
        DECISION("audio/bgm/decision.wav", true),
        // ยังไม่มีเพลง travel แยก ใช้ gameplay ไปก่อน
        TRAVEL("audio/bgm/gameplay.wav", true),
        DANGER("audio/bgm/danger.wav", true),
        // เพลงจบเกมปกติไม่ต้องวน (อยากวนเปลี่ยนเป็น true ได้)
        VICTORY("audio/bgm/victory.wav", false);

        public final String path;
        public final boolean loop;

        BgmTrack(String path, boolean loop) {
            this.path = path;
            this.loop = loop;
        }
    }

    public enum SfxEvent {
        // UI / popup
        UI_CLICK("audio/sfx/Event/Click.wav"),
        ALERT_POPUP("audio/sfx/Event/AlertPopup.wav"),
        // core actions
        DICE_ROLL("audio/sfx/main/DiceRoll.wav"),
        MOVE_STEP("audio/sfx/main/Walking.wav"),
        BUY_PROPERTY("audio/sfx/main/Buy.wav"),
        UPGRADE_PROPERTY("audio/sfx/main/Buy.wav"),
        PAY("audio/sfx/main/Pay.wav"),
        PASS_START_SALARY("audio/sfx/main/Pay.wav"),
        // event tiles
        GO_JAIL("audio/sfx/Event/Jail.wav"),
        WORLD_TRAVEL("audio/sfx/Event/WorldTravel.wav"),
        FESTIVAL("audio/sfx/Event/Festival.wav"),
        // cards
        CARD_DRAW("audio/sfx/card/GetCard.wav"),
        CARD_USE("audio/sfx/card/UseGeneralCard.wav"),
        CARD_USE_POSITIVE("audio/sfx/card/UsePositiveCard.wav"),
        CARD_USE_NEGATIVE("audio/sfx/card/UseNegativeCard.wav"),
        CARD_DISABLE("audio/sfx/card/DisableCard.wav"),
        CARD_KEEP("audio/sfx/Event/Click.wav"),
        CARD_DISCARD("audio/sfx/Event/Click.wav"),
        // ไม่มีไฟล์เฉพาะตอนนี้ ให้ใช้เสียงเตือนแทนไปก่อน
        PAY_RENT("audio/sfx/main/Pay.wav"),
        PAY_TAX("audio/sfx/main/Pay.wav"),
        ISLAND_BUFF("audio/sfx/main/Multiply.wav"),
        FREEZE("audio/sfx/Event/AlertPopup.wav"),
        BLACKOUT("audio/sfx/Event/AlertPopup.wav"),
        OLYMPIC("audio/sfx/Event/AlertPopup.wav"),
        VICTORY_FANFARE("audio/sfx/Event/AlertPopup.wav");

        public final String path;

        SfxEvent(String path) {
            this.path = path;
        }
    }

    // cache clip ไว้กันกระตุก + ไม่ต้องโหลดซ้ำ
    private final Map<BgmTrack, Clip> bgmCache = new EnumMap<>(BgmTrack.class);
    private final Map<SfxEvent, Clip> sfxCache = new EnumMap<>(SfxEvent.class);

    private Clip currentBgm;
    private BgmTrack currentBgmTrack;

    /*
     * Trigger จากข้อความที่ยิงใน notifyMessage()
     * อิงจากข้อความใน controller ตอนนี้
     *
     * ข้อดี: ต่อเร็ว แทบไม่ต้องแก้เกม
     * ข้อเสีย: ถ้าข้อความเปลี่ยน pattern ก็ต้องปรับ regex
     */
    private static final Pattern P_ROLL = Pattern.compile("\\broll\\b.*\\bdice\\b|\\brolls\\b.*\\bfor\\b|\\broll for\\b", Pattern.CASE_INSENSITIVE);
    private static final Pattern P_MOVE = Pattern.compile("\\bmove to\\b", Pattern.CASE_INSENSITIVE);
    private static final Pattern P_PASS_START = Pattern.compile("reach the start", Pattern.CASE_INSENSITIVE);
    private static final Pattern P_BUY = Pattern.compile("\\bbuy\\b", Pattern.CASE_INSENSITIVE);
    private static final Pattern P_UPGRADE = Pattern.compile("\\bupgrade\\b", Pattern.CASE_INSENSITIVE);
    private static final Pattern P_USE_CARD = Pattern.compile("\\buse card\\b", Pattern.CASE_INSENSITIVE);
    // private static final Pattern P_FESTIVAL = Pattern.compile("\\bexpo\\b|\\bfestival\\b|holds\\s+expo", Pattern.CASE_INSENSITIVE);
    private static final Pattern P_FESTIVAL = Pattern.compile("\\bexpo\\b|\\bfestival\\b", Pattern.CASE_INSENSITIVE);
    private static final Pattern P_ALERT = Pattern.compile("action required|warning|error|cannot|not enough|invalid|alert|popup", Pattern.CASE_INSENSITIVE);

    // อันนี้ “เผื่อไว้” ถ้ามี notifyMessage เพิ่มภายหลัง
    private static final Pattern P_RENT = Pattern.compile("\\brent\\b|\\btoll\\b", Pattern.CASE_INSENSITIVE);
    private static final Pattern P_TAX = Pattern.compile("\\btax\\b", Pattern.CASE_INSENSITIVE);
    private static final Pattern P_JAIL = Pattern.compile("\\bjail\\b", Pattern.CASE_INSENSITIVE);
    private static final Pattern P_TRAVEL = Pattern.compile("world\\s*tour|world[_\\s]*tour|selecting[_\\s]*destination|destination|fly\\s+to|worldtravel", Pattern.CASE_INSENSITIVE);

    @Override
    public void onPhaseChanged(String playerName, TurnPhase newPhase) {
        // phase = mood หลักของเกม (เหมาะกับ BGM)
        switch (newPhase) {

            case READY_TO_ROLL ->
                ensureBgm(BgmTrack.GAMEPLAY);

            case ACTION_REQUIRED ->
                ensureBgm(BgmTrack.DECISION);

            case SELECTING_DESTINATION -> {
                ensureBgm(BgmTrack.TRAVEL);
                playSfx(SfxEvent.WORLD_TRAVEL);
            }

            case GAME_OVER -> {
                ensureBgm(BgmTrack.VICTORY);
                playSfx(SfxEvent.VICTORY_FANFARE);
            }

            default -> {
            }
        }
    }

    @Override
    public void onGameMessage(String message) {
        if (message == null) {
            return;
        }
        if (P_FESTIVAL.matcher(message).find()) {
            playSfx(SfxEvent.FESTIVAL);
            return;
        }
        if (P_ALERT.matcher(message).find()) {
            playSfx(SfxEvent.ALERT_POPUP);
            return;
        }
        if (P_FESTIVAL.matcher(message).find()) {
            playSfx(SfxEvent.FESTIVAL);
            return;
        }
        if (P_ROLL.matcher(message).find()) {
            playSfx(SfxEvent.DICE_ROLL);
            return;
        }
        if (P_MOVE.matcher(message).find()) {
            playSfx(SfxEvent.MOVE_STEP);
            return;
        }
        if (P_PASS_START.matcher(message).find()) {
            playSfx(SfxEvent.PASS_START_SALARY);
            return;
        }
        if (P_UPGRADE.matcher(message).find()) {
            playSfx(SfxEvent.UPGRADE_PROPERTY);
            return;
        }
        if (P_BUY.matcher(message).find()) {
            playSfx(SfxEvent.BUY_PROPERTY);
            return;
        }
        if (P_USE_CARD.matcher(message).find()) {
            playSfx(SfxEvent.CARD_USE);
            return;
        }

        if (P_RENT.matcher(message).find()) {
            playSfx(SfxEvent.PAY_RENT);
            return;
        }
        if (P_TAX.matcher(message).find()) {
            playSfx(SfxEvent.PAY_TAX);
            return;
        }

        if (P_JAIL.matcher(message).find()) {
            ensureBgm(BgmTrack.DANGER);
            playSfx(SfxEvent.GO_JAIL);
            return;
        }

        if (P_TRAVEL.matcher(message).find()) {
            playSfx(SfxEvent.WORLD_TRAVEL);
            ensureBgm(BgmTrack.TRAVEL); // หรือปล่อยถ้าไม่อยากสลับเพลง
            return;
        }
    }

    /*
     * เมธอดด้านล่างไว้เรียกตรง ๆ จาก controller/tile (ถ้าอยากเลิกใช้ regex)
     * ชื่อเมธอดตั้งใจให้ “อ่านแล้วรู้เลยว่าเกิดอะไรขึ้น”
     */
    public void onUiClick() {
        playSfx(SfxEvent.UI_CLICK);
    }

    public void onDiceRolled() {
        playSfx(SfxEvent.DICE_ROLL);
    }

    public void onMoveStep() {
        playSfx(SfxEvent.MOVE_STEP);
    }

    public void onPassStartSalary() {
        playSfx(SfxEvent.PASS_START_SALARY);
    }

    public void onPropertyBought() {
        playSfx(SfxEvent.BUY_PROPERTY);
    }

    public void onPropertyUpgraded() {
        playSfx(SfxEvent.UPGRADE_PROPERTY);
    }

    public void onPayRent() {
        playSfx(SfxEvent.PAY_RENT);
    }

    public void onPayTax() {
        playSfx(SfxEvent.PAY_TAX);
    }

    public void onJail() {
        ensureBgm(BgmTrack.DANGER);
        playSfx(SfxEvent.GO_JAIL);
    }

    public void onWorldTravel() {
        ensureBgm(BgmTrack.TRAVEL);
        playSfx(SfxEvent.WORLD_TRAVEL);
    }

    public void onIslandBuff() {
        playSfx(SfxEvent.ISLAND_BUFF);
    }

    public void onCardDraw() {
        playSfx(SfxEvent.CARD_DRAW);
    }

    public void onCardKeep() {
        playSfx(SfxEvent.CARD_KEEP);
    }

    public void onCardDiscard() {
        playSfx(SfxEvent.CARD_DISCARD);
    }

    public void onCardUse() {
        playSfx(SfxEvent.CARD_USE);
    }

    public void onSpecialEffect(EffectType effect) {
        if (effect == null) {
            return;
        }

        // effect ที่แรงหน่อย เปลี่ยน mood เป็น danger
        switch (effect) {
            case FREEZE -> {
                ensureBgm(BgmTrack.DANGER);
                playSfx(SfxEvent.FREEZE);
            }
            case BLACKOUT -> {
                ensureBgm(BgmTrack.DANGER);
                playSfx(SfxEvent.BLACKOUT);
            }
            case FESTIVAL ->
                playSfx(SfxEvent.FESTIVAL);
            case OLYMPIC ->
                playSfx(SfxEvent.OLYMPIC);
        }
    }

    // ---------- BGM ----------
    public void ensureBgm(BgmTrack track) {
        if (track == null) {
            return;
        }

        // กัน restart ซ้ำ ๆ เวลา phase เปลี่ยนไปมา
        if (track == currentBgmTrack && currentBgm != null && currentBgm.isRunning()) {
            return;
        }

        stopBgm();

        Clip clip = getOrLoadBgm(track);
        if (clip == null) {
            return;
        }

        currentBgm = clip;
        currentBgmTrack = track;

        clip.setFramePosition(0);
        try {
            FloatControl gain = (FloatControl) clip.getControl(FloatControl.Type.MASTER_GAIN);
            gain.setValue(Math.min(0.0f, gain.getMaximum())); // 0 dB หรือ max ถ้า 0 เกิน
        } catch (Exception ignored) {
        }

        if (track.loop) {
            clip.loop(Clip.LOOP_CONTINUOUSLY);
        } else {
            clip.start();
        }
    }

    public void stopBgm() {
        if (currentBgm != null) {
            currentBgm.stop();
        }
        currentBgm = null;
        currentBgmTrack = null;
    }

    // ---------- SFX ----------
    public void playSfx(SfxEvent sfx) {
        if (sfx == null) {
            return;
        }

        Clip clip = getOrLoadSfx(sfx);
        if (clip == null) {
            return;
        }

        // เล่นซ้ำเร็ว ๆ ได้ (กดรัว)
        if (clip.isRunning()) {
            clip.stop();
        }
        clip.setFramePosition(0);
        try {
            FloatControl gain = (FloatControl) clip.getControl(FloatControl.Type.MASTER_GAIN);
            gain.setValue(Math.min(0.0f, gain.getMaximum())); // 0 dB หรือ max ถ้า 0 เกิน
        } catch (Exception ignored) {
        }
        clip.start();
    }

    public void preloadBgm(BgmTrack track) {
        getOrLoadBgm(track);
    }

    public void preloadSfx(SfxEvent sfx) {
        getOrLoadSfx(sfx);
    }

    private Clip getOrLoadBgm(BgmTrack track) {
        return bgmCache.computeIfAbsent(track, t -> loadClip(t.path));
    }

    private Clip getOrLoadSfx(SfxEvent sfx) {
        return sfxCache.computeIfAbsent(sfx, s -> loadClip(s.path));
    }

    /*
     * โหลดจาก classpath (resources)
     * ถ้าไฟล์หาย/ชื่อผิด จะ print เตือนใน console
     */
    private Clip loadClip(String resourcePath) {
        /*
         * พยายามโหลดจาก 2 ทาง:
         * 1) classpath (กรณีโปรเจกต์มี resources จริง)
         * 2) ไฟล์บนดิสก์ (กรณีรันแบบโฟลเดอร์ธรรมดา)
         */
        var url = AudioManager.class.getClassLoader().getResource(resourcePath);
        System.out.println("[AudioManager] resourcePath=" + resourcePath);
        System.out.println("[AudioManager] classpath url=" + url);
        System.out.println("[AudioManager] cwd=" + new java.io.File(".").getAbsolutePath());
        System.out.println("[AudioManager] file exists=" + new java.io.File(resourcePath).exists());
        System.out.println("[AudioManager] file exists ./=" + new java.io.File("./" + resourcePath).exists());

        try (BufferedInputStream bis = openAudioStream(resourcePath)) {
            if (bis == null) {
                System.err.println("[AudioManager] Missing audio file: " + resourcePath);
                return null;
            }

            AudioInputStream ais = AudioSystem.getAudioInputStream(bis);
            Clip clip = AudioSystem.getClip();
            clip.open(ais);
            System.out.println("[AudioManager] OPEN OK: " + resourcePath
                    + " | frames=" + clip.getFrameLength()
                    + " | ms=" + (clip.getMicrosecondLength() / 1000));
            return clip;

        } catch (UnsupportedAudioFileException e) {
            System.err.println("[AudioManager] Unsupported audio: " + resourcePath);
        } catch (LineUnavailableException e) {
            System.err.println("[AudioManager] Audio line unavailable: " + resourcePath);
        } catch (IOException e) {
            System.err.println("[AudioManager] IO error: " + resourcePath);
        } catch (Exception e) {
            System.err.println("[AudioManager] Load failed: " + resourcePath + " :: " + e.getMessage());
        }
        return null;
    }

    private BufferedInputStream openAudioStream(String resourcePath) {
        // 1) classpath
        try {
            var in = AudioManager.class.getClassLoader().getResourceAsStream(resourcePath);
            if (in != null) {
                return new BufferedInputStream(in);
            }
        } catch (Exception ignored) {
            // ไปลอง filesystem ต่อ
        }

        // 2) filesystem (ลองหลาย root เผื่อรันจาก IDE/terminal ต่างกัน)
        String[] candidates = new String[]{
            resourcePath,
            "./" + resourcePath,
            "src/" + resourcePath,
            "src/main/resources/" + resourcePath
        };

        for (String p : candidates) {
            try {
                File f = new File(p);
                if (f.exists() && f.isFile()) {
                    return new BufferedInputStream(new FileInputStream(f));
                }
            } catch (Exception ignored) {
                // ลอง path ถัดไป
            }
        }

        return null;
    }
    
}
