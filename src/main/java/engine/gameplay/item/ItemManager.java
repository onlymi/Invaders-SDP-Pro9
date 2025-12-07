package engine.gameplay.item;

import engine.Core;
import entity.EnemyShip;
import entity.Item;
import entity.ItemPool;
import java.util.Optional;
import java.util.Random;
import java.util.logging.Logger;

/**
 * Responsible for item drop decisions and applying item effects. Implemented as a singleton for
 * easy access.
 */
public final class ItemManager {
    
    private static ItemManager instance;
    
    /**
     * Debug logger init
     */
    private Logger logger;
    
    private ItemManager() {
        logger = Core.getLogger();
    }
    
    public static ItemManager getInstance() {
        if (instance == null) {
            instance = new ItemManager();
        }
        return instance;
    }
    
    /**
     * Random Roll for item
     */
    private final Random itemRoll = new Random();
    
    
    /**
     * Item database loaded from CSV.
     */
    private final ItemDB itemDB = new ItemDB();
    
    // Toast state: last picked item and when it was picked
    private Item lastPickedItem = null;
    private long lastPickupAtMs = 0L;
    
    // How long the toast should be visible
    private static final long TOAST_DURATION_MS = 3000L;
    /** -------------------------- ITEM DATA -------------------------- **/
    
    /**
     * ITEM Drop probability, Item Drop particle color
     **/
    public static enum DropTier {
        // DEBUG    (500.0),
        COMMON(25.0),
        UNCOMMON(25.0),
        RARE(25.0),
        EPIC(15.0),
        LEGENDARY(500.0);
        
        
        public final double tierWeight;
        
        DropTier(double weight) {
            this.tierWeight = Math.max(0.0, weight);
        }
    }
    
    /** -------------------------- INIT -------------------------- **/
    
    /**
     * Total weight of all item tiers except NONE.
     */
    private static final double ITEM_WEIGHT;
    
    static {
        double sum = 0.0;
        for (DropTier t : DropTier.values()) {
            sum += t.tierWeight;
        }
        ITEM_WEIGHT = sum;
    }
    
    /** -------------------------- MAIN -------------------------- **/
    
    /**
     * Determines and returns the item dropped by the given enemy.
     *
     * @param enemy enemy ship that was defeated.
     * @return dropped Item, or null if no item is dropped.
     */
    public Item obtainDrop(final EnemyShip enemy) {
        if (enemy == null) {
            return null;
        }
        
        // Always drop something: roll among COMMON ~ LEGENDARY.
        double dropRoll = itemRoll.nextDouble() * ITEM_WEIGHT;
        this.logger.info(String.format("[ItemManager]: DropRoll %.3f", dropRoll));
        
        DropTier chosenTier = DropTier.COMMON;
        double acc = 0.0;
        
        for (DropTier tier : DropTier.values()) {
            double weight = tier.tierWeight;
            acc += weight;
            
            if (dropRoll < acc) {
                chosenTier = tier;
                break;
            }
        }
        
        java.util.List<ItemData> candidates = new java.util.ArrayList<>();
        for (ItemData data : itemDB.getAllItems()) {
            if (data.getDropTier() != null
                && data.getDropTier().equalsIgnoreCase(chosenTier.name())) {
                candidates.add(data);
            }
        }
        
        if (candidates.isEmpty()) {
            logger.warning("[ItemManager]: No items defined for tier " + chosenTier);
            return null;
        }
        
        ItemData chosenData = candidates.get(itemRoll.nextInt(candidates.size()));
        
        int centerX = enemy.getPositionX() + enemy.getWidth() / 2;
        int centerY = enemy.getPositionY() + enemy.getHeight() / 2;
        
        int itemSpeed = 0;
        Item drop = ItemPool.getItem(chosenData, centerX, centerY, itemSpeed);
        
        if (drop == null) {
            logger.warning("[ItemManager]: Failed to create item: " + chosenData.getType());
            return null;
        }
        
        this.logger.info(
            "[ItemManager]: created item " + drop.getType() + " at (" + drop.getPositionX()
                + ", " + drop.getPositionY() + "), enemy center=(" + centerX + ", " + centerY
                + ")");
        
        return drop;
    }
    
    /**
     * Record the last picked item and timestamp so the renderer can show a toast.
     */
    public void onPickup(final Item item) {
        if (item == null) {
            return;
        }
        this.lastPickedItem = item;
        this.lastPickupAtMs = System.currentTimeMillis();
    }
    
    /**
     * Returns the last picked item if it is still within the toast window; otherwise empty.
     */
    public Optional<Item> getItemToDescribe() {
        if (lastPickedItem == null) {
            return Optional.empty();
        }
        long elapsed = System.currentTimeMillis() - lastPickupAtMs;
        if (elapsed <= TOAST_DURATION_MS) {
            return Optional.of(lastPickedItem);
        }
        return Optional.empty();
    }
}
