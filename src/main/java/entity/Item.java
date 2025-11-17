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

        super(positionX, positionY, 3 * 2, 5 * 2, Color.WHITE);

        logger = Core.getLogger();

        this.type = itemType;
        this.itemSpeed = speed;

        setSprite();
    }

    public Item(final ItemData data, final int positionX, final int positionY, final int speed) {
        super(positionX, positionY, 3 * 2, 5 * 2, Color.WHITE);
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
            // fallback: legacy path that re-reads from DB using 'type'
            ItemDB itemDB = new ItemDB();
            ItemData d = itemDB.getItemData(this.type);
            if (d != null) {
                this.data = d;                 // cache it to avoid reloading later
                spriteName = d.getSpriteType();
            }
        }

        if (spriteName != null) {
            try {
                this.spriteType = SpriteType.valueOf(spriteName);
            } catch (IllegalArgumentException e) {
                this.spriteType = SpriteType.ItemScore; // safe default
                this.logger.warning(
                    "[Item]: Unknown sprite type: " + spriteName + ", using default.");
            }
        } else {
            this.spriteType = SpriteType.ItemScore; // safe default if nothing found
        }
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
}