public interface GameEventListener {
    void onPhaseChanged(String playerName, TurnPhase newPhase);
    void onGameMessage(String message);
}