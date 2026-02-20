<<<<<<< HEAD
class SpecialTile extends Tile {
=======
public class SpecialTile extends Tile {
>>>>>>> f1b2a67b667dcb9c466699003c9fd517cf9c6142
    private final EffectType effect;

    public SpecialTile(int index, String name, EffectType effect) {
        super(index, name);
        this.effect = effect;
    }

    public EffectType getEffect() { return effect; }

    public void triggerEffect(Player player, GameState state) {
        if (player == null || state == null) return;
        switch (effect) {
            case FESTIVAL -> player.receiveMoney(100);
            case BLACKOUT -> player.pay(50);
            case FREEZE -> player.setFrozenTurns(1);
            case OLYMPIC -> player.receiveMoney(200);
        }
    }

    @Override
    public void onPlayerEnter(Player player, GameState state) {
        triggerEffect(player, state);
    }
}

