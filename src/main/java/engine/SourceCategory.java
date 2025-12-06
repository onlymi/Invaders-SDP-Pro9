package engine;

/**
 * Enum to distinguish between graphical source files.
 */
public enum SourceCategory {
    CHARACTER("image/character/"),
    WEAPON("image/weapon/"),
    PLAYERSHIP("graphics/playersShip_graphics"),
    ENEMY("image/enemy/"),
    BOSS("graphics/boss_graphics"),
    BULLET("graphics/bullet_graphics"),
    MUTUAL("graphics/mutual_graphics"),
    ITEM_IMAGE("graphics/item/");
    
    private final String filePath;
    
    SourceCategory(String path) {
        this.filePath = path;
    }
    
    public String getFilePath() {
        return this.filePath;
    }
}

