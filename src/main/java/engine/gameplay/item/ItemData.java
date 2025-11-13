package engine.gameplay.item;

/**
 * Represents the data for an item, including its type, sprite, tier, effect value, and duration.
 */
public class ItemData {

    /**
     * Unique identifier for the item (e.g. "COIN", "HEAL", "SCORE").
     */
    private String type;

    /**
     * sprite type (e.g. "ItemScore", "ItemHeal").
     */
    private String spriteType;

    /**
     * rarity tier (e.g. "COMMON", "UNCOMMON", "RARE").
     */
    private String dropTier;

    /**
     * numerical value of the item effect (e.g. heal amount, score amount).
     */
    private int effectValue;

    /**
     * duration that the effect remains active.
     */
    private int effectDuration;

    /**
     * cost in coins required to activate this item on pickup (0 = free).
     */
    private int cost;

    /**
     * Unique identifier for the item
     */
    private final String id;

    /**
     * Name displayed in the game UI for this item
     */
    private final String displayName;

    /**
     * Text description explaining what the item does or how it works
     */
    private final String description;

    /**
     * Constructs an ItemData object.
     *
     * @param type           Unique identifier for the item.
     * @param spriteType     sprite type.
     * @param dropTier       rarity tier.
     * @param effectValue    numerical value of the item's effect.
     * @param effectDuration duration the effect remains active.
     * @param id             The unique identifier for this item.
     * @param displayName    The readable name shown to the player.
     * @param description    A short explanation of the item's effect or purpose.
     */
    public ItemData(String type, String spriteType, String dropTier, int effectValue,
        int effectDuration, int cost, String id, String displayName, String description) {
        // Unique identifier for the item (e.g "COIN", "HEAL", "SCORE").
        this.type = type;
        // The sprite type (e.g "ItemScore, ItemHeal", etc).
        this.spriteType = spriteType;
        // The rarity tier (e.g "COMMON", "UNCOMMON", "RARE").
        this.dropTier = dropTier;
        // The numerical value of the item's effect (e.g. heal amount, score amount).
        this.effectValue = effectValue;
        // The duration (in seconds or frames) that the effect remains active.
        this.effectDuration = effectDuration;
        // 0 = free
        this.cost = Math.max(0, cost);
        this.id = id;
        // Display Item Name in the game UI
        this.displayName = displayName;
        // Display Itme description
        this.description = description;
    }

    /**
     * default constructor (no cost) — sets cost to 0.
     */
    public ItemData(String type, String spriteType, String dropTier, int effectValue,
        int effectDuration, String id, String displayName, String description) {
        this(type, spriteType, dropTier, effectValue, effectDuration, 0, id, displayName,
            description);
    }

    /**
     * Getter for item type.
     *
     * @return item type.
     */
    public String getType() {
        return type;
    }

    /**
     * Getter for sprite type of the item.
     *
     * @return sprite type.
     */
    public String getSpriteType() {
        return spriteType;
    }

    /**
     * Getter for drop tier.
     *
     * @return drop tier.
     */
    public String getDropTier() {
        return dropTier;
    }

    /**
     * Getter for the numerical value of the item effect.
     *
     * @return effect value.
     */
    public int getEffectValue() {
        return effectValue;
    }

    /**
     * Getter for the duration that the effect remains active.
     *
     * @return effect duration.
     */
    public int getEffectDuration() {
        return effectDuration;
    }

    // Getter for item cost
    public int getCost() {
        return cost;
    }

    // Return the unique item ID
    public String getId() {
        return id;
    }

    // Return the name displayed in the game UI
    public String getDisplayName() {
        return displayName;
    }

    // Return the Item description text
    public String getDescription() {
        return description;
    }
}
