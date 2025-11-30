package entity.character;

public enum CharacterType {
    WARRIOR(new CharacterStats(150, 100, 1.0f, 25, 0, 0.8f, 1.5f, 0.05f, 1.5f, 30), true),
    // Quick long-range attack
    ARCHER(new CharacterStats(90, 100, 1.2f, 18, 0, 1.5f, 12.0f, 0.15f, 2.0f, 8), true),
    // Powerful long-range attack
    WIZARD(new CharacterStats(70, 200, 0.9f, 2, 30, 0.6f, 10.0f, 0.01f, 1.5f, 5), false),
    // Laser attack
    LASER(new CharacterStats(80, 250, 0.8f, 1, 28, 1.0f, 10.0f, 0.01f, 1.5f, 6), false),
    // Electric attack
    ELECTRIC(new CharacterStats(85, 180, 1.0f, 3, 25, 0.7f, 10.0f, 0.05f, 1.5f, 7), false),
    // Bomb-throwing attack
    BOMBER(new CharacterStats(110, 100, 1.1f, 5, 22, 0.5f, 8.0f, 0.05f, 1.5f, 12), false),
    // Bomb-throwing attack
    HEALER(new CharacterStats(100, 200, 1.0f, 5, 30, 0.8f, 8.0f, 0.01f, 1.5f, 10), false);
    
    private final CharacterStats baseStats;
    public final boolean unlocked;
    
    CharacterType(CharacterStats baseStats, boolean unlocked) {
        this.baseStats = baseStats;
        this.unlocked = unlocked;
    }
    
    public CharacterStats getBaseStats() {
        return baseStats;
    }
    
    public boolean isUnlocked() {
        return unlocked;
    }
}