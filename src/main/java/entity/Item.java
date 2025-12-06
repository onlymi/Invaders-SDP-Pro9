package entity;

import engine.AssetManager.SpriteType;
import engine.Core;
import engine.GameState;
import engine.gameplay.item.ActivationType;
import engine.gameplay.item.ItemDB;
import engine.gameplay.item.ItemData;
import engine.gameplay.item.ItemEffect;
import java.awt.Color;
import java.util.logging.Logger;

/**
 * Implements Item that moves vertically down.
 */
public class Item extends Entity {
    
    /**
     * Logger instance for logging purposes.
     */
    private Logger logger;
    
    /**
     * Type of Item.
     */
    private String type;
    
    /**
     * Item Movement Speed.
     */
    private int itemSpeed;
    
    /**
     * Hold the item data UI effects can access data without reloading DB
     */
    private ItemData data;
    
    /**
     * Constructor, establishes the Item's properties.
     *
     * @param itemType  Type of Item being spawned
     * @param positionX Initial position of the Item in the X axis.
     * @param positionY Initial position of the Item in the Y axis.
     * @param speed     Speed of the Item, positive or negative depending on direction - positive is
     *                  down.
     */
    
    public Item(String itemType, final int positionX, final int positionY, final int speed) {
        
        super(positionX, positionY, 5 * 2, 5 * 2, Color.WHITE);
        
        logger = Core.getLogger();
        
        this.type = itemType;
        this.itemSpeed = speed;
        
        setSprite();
    }
    
    public Item(final ItemData data, final int positionX, final int positionY, final int speed) {
        super(positionX, positionY, 5 * 2, 5 * 2, Color.WHITE);
        logger = Core.getLogger();
        this.data = data;
        this.type = (data != null) ? data.getType() : null;
        this.itemSpeed = speed;
        
        setSprite();
    }
    
    /**
     * Setter for the sprite of the Item using data from ItemDB.
     */
    public final void setSprite() {
        String spriteName = null;
        
        if (this.data != null) {
            spriteName = this.data.getSpriteType();
        } else {
            // if there is no data, load it from DB.
            if (this.type != null) {
                ItemDB itemDB = new ItemDB();
                ItemData d = itemDB.getItemData(this.type);
                if (d != null) {
                    this.data = d;
                    spriteName = d.getSpriteType();
                }
            }
        }
        
        SpriteType resolved = null;
        
        if (spriteName != null && !spriteName.isEmpty()) {
            try {
                resolved = SpriteType.valueOf(spriteName);
            } catch (IllegalArgumentException e) {
                logger.warning("[Item]: Unknown sprite type in DB: "
                    + spriteName + " for type=" + this.type);
            }
        }
        
        if (resolved == null) {
            resolved = mapTypeToSprite(this.type);
        }
        
        this.spriteType = resolved;
        
    }
    
    /**
     * Maps an item type string (e.g., "COIN", "HEAL") to its corresponding SpriteType.
     *
     * @param type A string representing the logical item type Must not be null. If null is
     *             provided, a safe default sprite is used.
     * @return The SpriteType corresponding to the given item type. If no match exists, returns a
     * default SpriteType (ItemScore).
     */
    private SpriteType mapTypeToSprite(final String type) {
        if (type == null) {
            logger.warning("[Item]: null type, using default ItemScore sprite.");
            return SpriteType.ItemScore;
        }
        
        return switch (type) {
            case "COIN" -> SpriteType.ItemCoin;
            case "HEAL" -> SpriteType.ItemHeal;
            case "SCORE" -> SpriteType.ItemScore;
            case "SCOREBOOST" -> SpriteType.ItemScoreBooster;
            case "MOVE_SPEED_UP" -> SpriteType.ItemMoveSpeedUp;
            case "TIME_FREEZE" -> SpriteType.ItemTimeFreeze;
            case "TIME_SLOW" -> SpriteType.ItemTimeSlow;
            case "DASH" -> SpriteType.ItemDash;
            case "PET_GUN" -> SpriteType.ItemPetGun;
            case "SHIELD" -> SpriteType.ItemShield;
            default -> {
                logger.warning("[Item]: No sprite mapping for type "
                    + type + ", using default ItemScore sprite.");
                yield SpriteType.ItemScore;
            }
        };
    }
    
    /**
     * Updates the Item's position.
     */
    public final void update() {
        this.positionY += this.itemSpeed;
    }
    
    /**
     * Applies the effect of the Item to the player.
     *
     * @param gameState current game state instance.
     * @param playerId  ID of the player to apply the effect to.
     */
    public boolean applyEffect(final GameState gameState, final int playerId) {
        ItemData data = this.data;
        
        if (data == null) {
            // fallback for legacy-constructed Items (string-only constructor)
            ItemDB itemDB = new ItemDB();
            data = itemDB.getItemData(this.type);
            if (data == null) {
                return false;
            }
            this.data = data; // cache for next time
        }
        
        int value = data.getEffectValue();
        int duration = data.getEffectDuration();
        
        boolean applied = false;
        
        switch (data.getType()) { // use data.getType() to be consistent
            case "COIN":
                ItemEffect.applyCoinItem(gameState, playerId, value);
                applied = true;
                break;
            case "HEAL":
                ItemEffect.applyHealItem(gameState, playerId, value);
                applied = true;
                break;
            case "SCORE":
                ItemEffect.applyScoreItem(gameState, playerId, value);
                applied = true;
                break;
            case "SCOREBOOST":
                applied = ItemEffect.applyScoreBoost(gameState, playerId, value, duration);
                break;
            case "TIME_FREEZE":
                applied = ItemEffect.applyTimeFreeze(gameState, playerId, value, duration);
                break;
            case "TIME_SLOW":
                applied = ItemEffect.applyTimeSlow(gameState, playerId, value, duration);
                break;
            case "DASH":
                applied = ItemEffect.applyDash(gameState, playerId, value, duration);
                break;
            case "PET_GUN":
                applied = ItemEffect.applyPetSupport(gameState, playerId, duration);
                break;
            default:
                this.logger.warning("[Item]: No ItemEffect for type " + data.getType());
                applied = false;
                break;
        }
        
        if (!applied) {
            logger.info(
                "[Item]: Effect for player " + playerId + " could not be applied. type="
                    + data.getType());
        }
        
        return applied;
    }
    
    /**
     * Setter of the speed of the Item.
     *
     * @param itemSpeed New speed of the Item.
     */
    public final void setItemSpeed(final int itemSpeed) {
        this.itemSpeed = itemSpeed;
    }
    
    /**
     * Getter for Item Movement Speed.
     *
     * @return speed of the Item.
     */
    public final int getItemSpeed() {
        return this.itemSpeed;
    }
    
    /**
     * Reset the Item. Set the item type and sprite to newType, and the speed to 0.
     *
     */
    public final void reset(final ItemData newData) {
        this.data = newData;
        this.type = (newData != null) ? newData.getType() : null;
        this.itemSpeed = 0;
        setSprite();
    }
    
    /**
     * Getter for the type of the Item.
     *
     * @return type of the Item.
     */
    public final String getType() {
        return this.type;
    }
    
    public ItemData getData() {
        return this.data;
    }
    
    public String getDisplayName() {
        return (this.data != null) ? this.data.getDisplayName() : this.type;
    }
    
    /**
     * Returns how this item is activated (instant on pickup, active on key, etc.). Falls back to
     * INSTANT_ON_PICKUP if data is missing.
     */
    public ActivationType getActivationType() {
        ensureDataLoaded();
        if (this.data != null) {
            return this.data.getActivationType();
        }
        return ActivationType.INSTANT_ON_PICKUP;
    }
    
    /**
     * Whether this item should automatically be used when picked up. Uses ItemData.autoUseOnPickup
     * when available; otherwise defaults to true to preserve legacy behavior.
     */
    public boolean isAutoUseOnPickup() {
        ensureDataLoaded();
        if (this.data != null) {
            return this.data.isAutoUseOnPickup();
        }
        return true;
    }
    
    public boolean isActiveType() {
        return this.data.getActivationType() == ActivationType.ACTIVE_ON_KEY;
    }
    
    /**
     * Maximum number of uses for ACTIVE_ON_KEY items. Returns 0 if not defined.
     */
    public int getMaxCharges() {
        ensureDataLoaded();
        if (this.data != null) {
            return this.data.getMaxCharges();
        }
        return 0;
    }
    
    /**
     * Cooldown time in seconds between uses for ACTIVE_ON_KEY items. Returns 0 if not defined.
     */
    public int getCooldownSec() {
        ensureDataLoaded();
        if (this.data != null) {
            return this.data.getCooldownSec();
        }
        return 0;
    }
    
    /**
     * Returns a full description string for this item. Currently this is just the base description
     * from ItemData.
     */
    public String getFullDescription(int playerCoins) {
        ensureDataLoaded();
        if (this.data == null) {
            return "";
        }
        
        String base = this.data.getDescription();
        return (base != null) ? base : "";
    }
    
    private void ensureDataLoaded() {
        if (this.data == null && this.type != null) {
            try {
                ItemDB itemDB = new ItemDB();
                ItemData d = itemDB.getItemData(this.type);
                if (d != null) {
                    this.data = d;
                } else {
                    logger.warning(
                        "[Item] ensureDataLoaded(): ItemData not found for type=" + this.type);
                }
            } catch (Exception e) {
                logger.warning("[Item] ensureDataLoaded() failed for type=" + this.type
                    + " (" + e.getMessage() + ")");
            }
        }
    }
}