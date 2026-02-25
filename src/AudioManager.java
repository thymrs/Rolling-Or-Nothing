import javax.sound.sampled.*;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.util.EnumMap;
import java.util.Map;
import java.util.regex.Pattern;

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
    public enum BgmTrack {
        MAIN("audio/bgm/main.wav"),
        GAMEPLAY("audio/bgm/gameplay.wav"),
        DECISION("audio/bgm/decision.wav"),
        TRAVEL("audio/bgm/travel.wav"),
        DANGER("audio/bgm/danger.wav"),
        VICTORY("audio/bgm/victory.wav");

        public final String path;
        BgmTrack(String path) { this.path = path; }
    }

    public enum SfxEvent {
        UI_CLICK("audio/sfx/ui_click.wav"),
        DICE_ROLL("audio/sfx/dice.wav"),
        MOVE_STEP("audio/sfx/move.wav"),
        PASS_START_SALARY("audio/sfx/coin.wav"),
        BUY_PROPERTY("audio/sfx/buy.wav"),
        UPGRADE_PROPERTY("audio/sfx/upgrade.wav"),
        PAY_RENT("audio/sfx/rent.wav"),
        PAY_TAX("audio/sfx/tax.wav"),
        GO_JAIL("audio/sfx/jail.wav"),
        WORLD_TRAVEL("audio/sfx/travel.wav"),
        ISLAND_BUFF("audio/sfx/buff.wav"),
        CARD_DRAW("audio/sfx/card_draw.wav"),
        CARD_KEEP("audio/sfx/card_keep.wav"),
        CARD_DISCARD("audio/sfx/card_discard.wav"),
        CARD_USE("audio/sfx/card_use.wav"),
        FREEZE("audio/sfx/freeze.wav"),
        BLACKOUT("audio/sfx/blackout.wav"),
        FESTIVAL("audio/sfx/festival.wav"),
        OLYMPIC("audio/sfx/olympic.wav"),
        VICTORY_FANFARE("audio/sfx/victory.wav");

        public final String path;
        SfxEvent(String path) { this.path = path; }
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
    private static final Pattern P_ROLL = Pattern.compile("\\broll for\\b", Pattern.CASE_INSENSITIVE);
    private static final Pattern P_MOVE = Pattern.compile("\\bmove to\\b", Pattern.CASE_INSENSITIVE);
    private static final Pattern P_PASS_START = Pattern.compile("reach the start", Pattern.CASE_INSENSITIVE);
    private static final Pattern P_BUY = Pattern.compile("\\bbuy\\b", Pattern.CASE_INSENSITIVE);
    private static final Pattern P_UPGRADE = Pattern.compile("\\bupgrade\\b", Pattern.CASE_INSENSITIVE);
    private static final Pattern P_USE_CARD = Pattern.compile("\\buse card\\b", Pattern.CASE_INSENSITIVE);

    // อันนี้ “เผื่อไว้” ถ้ามี notifyMessage เพิ่มภายหลัง
    private static final Pattern P_RENT = Pattern.compile("\\brent\\b|\\btoll\\b", Pattern.CASE_INSENSITIVE);
    private static final Pattern P_TAX = Pattern.compile("\\btax\\b", Pattern.CASE_INSENSITIVE);
    private static final Pattern P_JAIL = Pattern.compile("\\bjail\\b", Pattern.CASE_INSENSITIVE);
    private static final Pattern P_TRAVEL = Pattern.compile("world tour|destination|travel", Pattern.CASE_INSENSITIVE);

    public AudioManager() {
        // ถ้าเจออาการดีเลย์ตอนกดครั้งแรก ค่อย preload ตัวที่ใช้บ่อย
        // preloadBgm(BgmTrack.GAMEPLAY);
        // preloadSfx(SfxEvent.DICE_ROLL);
    }

    @Override
    public void onPhaseChanged(String playerName, TurnPhase newPhase) {
        // phase = mood หลักของเกม (เหมาะกับ BGM)
        switch (newPhase) {
            case READY_TO_ROLL -> ensureBgm(BgmTrack.GAMEPLAY);
            case ACTION_REQUIRED -> ensureBgm(BgmTrack.DECISION);
            case SELECTING_DESTINATION -> ensureBgm(BgmTrack.TRAVEL);
            case GAME_OVER -> {
                ensureBgm(BgmTrack.VICTORY);
                playSfx(SfxEvent.VICTORY_FANFARE);
            }
            default -> {
                // MOVING / END_TURN ส่วนใหญ่ปล่อยให้ BGM เดิมเล่นต่อ
            }
        }
    }

    @Override
    public void onGameMessage(String message) {
        // ยิง SFX แบบเร็ว ๆ จากข้อความ (แก้โค้ดเดิมให้น้อยที่สุด)
        if (message == null) return;

        if (P_ROLL.matcher(message).find()) { playSfx(SfxEvent.DICE_ROLL); return; }
        if (P_MOVE.matcher(message).find()) { playSfx(SfxEvent.MOVE_STEP); return; }
        if (P_PASS_START.matcher(message).find()) { playSfx(SfxEvent.PASS_START_SALARY); return; }
        if (P_UPGRADE.matcher(message).find()) { playSfx(SfxEvent.UPGRADE_PROPERTY); return; }
        if (P_BUY.matcher(message).find()) { playSfx(SfxEvent.BUY_PROPERTY); return; }
        if (P_USE_CARD.matcher(message).find()) { playSfx(SfxEvent.CARD_USE); return; }

        if (P_RENT.matcher(message).find()) { playSfx(SfxEvent.PAY_RENT); return; }
        if (P_TAX.matcher(message).find()) { playSfx(SfxEvent.PAY_TAX); return; }

        if (P_JAIL.matcher(message).find()) {
            ensureBgm(BgmTrack.DANGER);
            playSfx(SfxEvent.GO_JAIL);
            return;
        }

        if (P_TRAVEL.matcher(message).find()) {
            ensureBgm(BgmTrack.TRAVEL);
            playSfx(SfxEvent.WORLD_TRAVEL);
        }
    }

    /*
     * เมธอดด้านล่างไว้เรียกตรง ๆ จาก controller/tile (ถ้าอยากเลิกใช้ regex)
     * ชื่อเมธอดตั้งใจให้ “อ่านแล้วรู้เลยว่าเกิดอะไรขึ้น”
     */
    public void onUiClick() { playSfx(SfxEvent.UI_CLICK); }
    public void onDiceRolled() { playSfx(SfxEvent.DICE_ROLL); }
    public void onMoveStep() { playSfx(SfxEvent.MOVE_STEP); }
    public void onPassStartSalary() { playSfx(SfxEvent.PASS_START_SALARY); }
    public void onPropertyBought() { playSfx(SfxEvent.BUY_PROPERTY); }
    public void onPropertyUpgraded() { playSfx(SfxEvent.UPGRADE_PROPERTY); }
    public void onPayRent() { playSfx(SfxEvent.PAY_RENT); }
    public void onPayTax() { playSfx(SfxEvent.PAY_TAX); }

    public void onJail() {
        ensureBgm(BgmTrack.DANGER);
        playSfx(SfxEvent.GO_JAIL);
    }

    public void onWorldTravel() {
        ensureBgm(BgmTrack.TRAVEL);
        playSfx(SfxEvent.WORLD_TRAVEL);
    }

    public void onIslandBuff() { playSfx(SfxEvent.ISLAND_BUFF); }
    public void onCardDraw() { playSfx(SfxEvent.CARD_DRAW); }
    public void onCardKeep() { playSfx(SfxEvent.CARD_KEEP); }
    public void onCardDiscard() { playSfx(SfxEvent.CARD_DISCARD); }
    public void onCardUse() { playSfx(SfxEvent.CARD_USE); }

    public void onSpecialEffect(EffectType effect) {
        if (effect == null) return;

        // effect ที่แรงหน่อย เปลี่ยน mood เป็น danger
        switch (effect) {
            case FREEZE -> { ensureBgm(BgmTrack.DANGER); playSfx(SfxEvent.FREEZE); }
            case BLACKOUT -> { ensureBgm(BgmTrack.DANGER); playSfx(SfxEvent.BLACKOUT); }
            case FESTIVAL -> playSfx(SfxEvent.FESTIVAL);
            case OLYMPIC -> playSfx(SfxEvent.OLYMPIC);
        }
    }

    // ---------- BGM ----------
    public void ensureBgm(BgmTrack track) {
        if (track == null) return;

        // กัน restart ซ้ำ ๆ เวลา phase เปลี่ยนไปมา
        if (track == currentBgmTrack && currentBgm != null && currentBgm.isRunning()) return;

        stopBgm();

        Clip clip = getOrLoadBgm(track);
        if (clip == null) return;

        currentBgm = clip;
        currentBgmTrack = track;

        clip.setFramePosition(0);
        clip.loop(Clip.LOOP_CONTINUOUSLY);
        clip.start();
    }

    public void stopBgm() {
        if (currentBgm != null) currentBgm.stop();
        currentBgm = null;
        currentBgmTrack = null;
    }

    // ---------- SFX ----------
    public void playSfx(SfxEvent sfx) {
        if (sfx == null) return;

        Clip clip = getOrLoadSfx(sfx);
        if (clip == null) return;

        // เล่นซ้ำเร็ว ๆ ได้ (กดรัว)
        if (clip.isRunning()) clip.stop();
        clip.setFramePosition(0);
        clip.start();
    }

    public void preloadBgm(BgmTrack track) { getOrLoadBgm(track); }
    public void preloadSfx(SfxEvent sfx) { getOrLoadSfx(sfx); }

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
        try (BufferedInputStream bis = new BufferedInputStream(
                AudioManager.class.getClassLoader().getResourceAsStream(resourcePath)
        )) {
            if (bis == null) {
                System.err.println("[AudioManager] Missing resource: " + resourcePath);
                return null;
            }

            AudioInputStream ais = AudioSystem.getAudioInputStream(bis);
            Clip clip = AudioSystem.getClip();
            clip.open(ais);
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
}