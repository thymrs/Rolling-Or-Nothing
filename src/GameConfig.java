import java.util.Objects;

public class GameConfig {
    private final int initialMoney;
    private final int maxTurns;
    private final String mapName;

    private final int humanCount;
    private final int botCount;
    private final DifficultyLevel botDifficulty;

    private final int passGoSalary;
    private final int taxPercentage;

    public GameConfig(Builder b) {
        this.initialMoney = b.initialMoney;
        this.maxTurns = b.maxTurns;
        this.mapName = b.mapName;
        this.humanCount = b.humanCount;
        this.botCount = b.botCount;
        this.botDifficulty = b.botDifficulty;
        this.passGoSalary = b.passGoSalary;
        this.taxPercentage = b.taxPercentage;
        validate();
    }

    private void validate() {
        if (initialMoney < 0) throw new IllegalArgumentException();
        if (maxTurns <= 0) throw new IllegalArgumentException();
        if (humanCount < 0) throw new IllegalArgumentException();
        if (botCount < 0) throw new IllegalArgumentException();
        if (humanCount + botCount <= 0) throw new IllegalArgumentException();
        if (passGoSalary < 0) throw new IllegalArgumentException();
        if (taxPercentage < 0 || taxPercentage > 100) throw new IllegalArgumentException();
        Objects.requireNonNull(mapName);
        if (mapName.isBlank()) throw new IllegalArgumentException();
        Objects.requireNonNull(botDifficulty);
    }

    public int getInitialMoney() { return initialMoney; }
    public int getMaxTurns() { return maxTurns; }
    public String getMapName() { return mapName; }
    public int getHumanCount() { return humanCount; }
    public int getBotCount() { return botCount; }
    public DifficultyLevel getBotDifficulty() { return botDifficulty; }
    public int getPassGoSalary() { return passGoSalary; }
    public int getTaxPercentage() { return taxPercentage; }

    public static class Builder {
        private int initialMoney = 1500;
        private int maxTurns = 50;
        private String mapName = "default";
        private int humanCount = 1;
        private int botCount = 1;
        private DifficultyLevel botDifficulty = DifficultyLevel.NORMAL;
        private int passGoSalary = 200;
        private int taxPercentage = 10;

        public Builder initialMoney(int v) { this.initialMoney = v; return this; }
        public Builder maxTurns(int v) { this.maxTurns = v; return this; }
        public Builder mapName(String v) { this.mapName = v; return this; }
        public Builder humanCount(int v) { this.humanCount = v; return this; }
        public Builder botCount(int v) { this.botCount = v; return this; }
        public Builder botDifficulty(DifficultyLevel v) { this.botDifficulty = v; return this; }
        public Builder passGoSalary(int v) { this.passGoSalary = v; return this; }
        public Builder taxPercentage(int v) { this.taxPercentage = v; return this; }
        public GameConfig build() { return new GameConfig(this); }
    }
}

enum DifficultyLevel { EASY, NORMAL, HARD }