package entity;

import engine.AssetManager.SpriteType;
import engine.Core;
import engine.GameState;
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
            case "TRIPLESHOT" -> SpriteType.ItemTripleShot;
            case "SCOREBOOST" -> SpriteType.ItemScoreBooster;
            case "BULLETSPEEDUP" -> SpriteType.ItemBulletSpeedUp;
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
        int cost = data.getCost();

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
            case "TRIPLESHOT":
                applied = ItemEffect.applyTripleShot(gameState, playerId, value, duration, cost);
                break;
            case "SCOREBOOST":
                applied = ItemEffect.applyScoreBoost(gameState, playerId, value, duration, cost);
                break;
            case "BULLETSPEEDUP":
                applied = ItemEffect.applyBulletSpeedUp(gameState, playerId, value, duration, cost);
                break;
            default:
                this.logger.warning("[Item]: No ItemEffect for type " + data.getType());
                applied = false;
                break;
        }

        if (!applied) {
            logger.info(
                "[Item]: Player " + playerId + " couldn't afford " + data.getType() + " (cost="
                    + cost + ")");
        }

        return applied;
    }

    ;

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
     * Getter for the speed of the Item.
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

    public String getDescription() {
        return (this.data != null) ? this.data.getDescription() : "";
    }

    /**
     * Returns full description including additional info based on item cost or player
     * affordability. - If cost == 0 → add "No cost required." - If cost > 0  → add "(Cost: X)" and
     * possibly "Not enough coins." UI will always show this full message when the item is picked
     * up.
     */

    public String getFullDescription(int playerCoins) {
        ensureDataLoaded();
        if (this.data == null) {
            return "";
        }

        StringBuilder sb = new StringBuilder();

        // Basic Item description
        String base = this.data.getDescription();
        sb.append(base != null ? base : "");

        int cost = this.data.getCost();

        // case: cost == 0
        if (cost == 0) {
            sb.append("\n No cost required.");
        }

        // case: cost > 0
        else {
            sb.append("\n (Cost: ").append(cost).append(")");

            if (playerCoins < cost) {
                sb.append("\nNot enough coins to activate this item.");
            }
        }

        return sb.toString();
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