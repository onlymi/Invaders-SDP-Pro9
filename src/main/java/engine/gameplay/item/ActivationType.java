package engine.gameplay.item;

/**
 * How an item is activated.
 */
public enum ActivationType {
    /**
     * Item is used immediately when picked up. (ex: COIN, HEAL, SCORE)
     */
    INSTANT_ON_PICKUP,

    /**
     * Item is stored and must be triggered by a key (Q/E).
     */
    ACTIVE_ON_KEY,

    /**
     * Passive effect – applied as long as the item is owned.
     */
    PASSIVE,

    /**
     * Temporary buff that starts immediately on pickup and expires after a duration.
     */
    TEMPORARY_BUFF
}