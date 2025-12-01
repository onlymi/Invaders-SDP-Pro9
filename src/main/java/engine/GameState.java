// engine/GameState.java

package engine;

import engine.gameplay.item.ActivationType;
import engine.gameplay.item.ItemData;
import engine.gameplay.item.ItemEffect;
import engine.gameplay.item.ItemEffect.ItemEffectType;
import engine.utils.Cooldown;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Implements an object that stores the state of the game between levels - supports 2-player co-op
 * with shared lives.
 *
 * @author <a href="mailto:RobertoIA1987@gmail.com">Roberto Izquierdo Amo</a>
 *
 *
 */
public class GameState {
    
    private static final java.util.logging.Logger logger = Core.getLogger();
    
    // 2P mode: number of players used for shared lives in co-op
    public static final int NUM_PLAYERS = 2; // adjust later if needed
    
    // 2P mode: true if in co-op mode
    private final boolean coop;
    
    /**
     * Current game level.
     */
    private int level;
    
    // 2P mode: if true, lives are shared in a team pool; else per-player lives
    private final boolean sharedLives;
    
    // team life pool and cap (used when sharedLives == true).
    private int teamLives;
    private int teamLivesCap;
    
    private boolean enemiesFrozen = false;
    private long enemiesFrozenUntilMillis = 0;
    
    /**
     * Current coin count.
     */ // ADD THIS LINE
    private static int coins = 0; // ADD THIS LINE - edited for 2P mode
    
    private static class EffectState {
        
        Cooldown cooldown;
        boolean active;
        Integer effectValue;
        
        EffectState() {
            this.cooldown = null;
            this.active = false;
            this.effectValue = null;
        }
    }
    
    /**
     * Holds a single active item instance for a player, including remaining charges and per-item
     * cooldown.
     */
    private static class ActiveItemInstance {
        
        final ItemData data;
        int remainingCharges;
        Cooldown cooldown;
        
        ActiveItemInstance(final ItemData data) {
            this.data = data;
            int maxCharges = Math.max(0, data.getMaxCharges());
            // 0 or negative means "no explicit limit" → treat as infinite
            this.remainingCharges = (maxCharges == 0) ? Integer.MAX_VALUE : maxCharges;
            this.cooldown = null;
        }
        
        boolean isOnCooldown() {
            return cooldown != null && !cooldown.checkFinished();
        }
        
        void startCooldown() {
            int cd = Math.max(0, data.getCooldownSec());
            if (cd <= 0) {
                this.cooldown = null;
            } else {
                this.cooldown = Core.getCooldown(cd * 1000);
                this.cooldown.reset();
            }
        }
    }
    
    /**
     * Per-player container for passive and active items.
     */
    private static class PlayerItemState {
        
        final List<ItemData> passiveItems = new ArrayList<>();
        final List<ActiveItemInstance> activeItems = new ArrayList<>();
    }
    
    /**
     * Each player has all effect types always initialized (inactive at start).
     */
    private final Map<Integer, Map<ItemEffectType, EffectState>> playerEffects = new HashMap<>();
    
    /**
     * Per-player item inventories (passive + active).
     */
    private final PlayerItemState[] playerItems = new PlayerItemState[NUM_PLAYERS];
    
    // 2P mode: co-op aware constructor used by the updated Core loop - livesEach
    // applies per-player; co-op uses shared pool.
    public GameState(final int level, final int livesEach, final boolean coop, final int coin) {
        this.level = level;
        this.coop = coop;
        this.coins = coin;
        
        if (coop) {
            this.sharedLives = true;
            this.teamLives = Math.max(0, livesEach * NUM_PLAYERS);
            this.teamLivesCap = this.teamLives;
        } else {
            this.sharedLives = false;
            this.teamLives = 0;
            this.teamLivesCap = 0;
            // legacy: put all lives on P1
            lives[0] = Math.max(0, livesEach);
        }
        
        initializeEffectStates();
        initializePlayerItemStates();
    }
    
    // 2P mode: per-player tallies (used for stats/scoring; lives[] unused in shared
    // mode).
    private final int[] score = new int[NUM_PLAYERS];
    private final int[] lives = new int[NUM_PLAYERS];
    private final int[] bulletsShot = new int[NUM_PLAYERS];
    private final int[] shipsDestroyed = new int[NUM_PLAYERS];
    
    /* ---------- Constructors ---------- */
    
    /** Legacy 6-arg - kept for old call sites */
    /**
     * Constructor.
     *
     * @param level          Current game level.
     * @param score          Current score.
     * @param livesRemaining Lives currently remaining.
     * @param bulletsShot    Bullets shot until now.
     * @param shipsDestroyed Ships destroyed until now.
     * @param coins          // ADD THIS LINE Current coin count. // ADD THIS LINE
     */
    public GameState(final int level, final int score,
        final int livesRemaining, final int bulletsShot,
        final int shipsDestroyed, final int coins) { // MODIFY THIS LINE
        this.level = level;
        this.sharedLives = false;
        this.teamLives = 0;
        this.teamLivesCap = 0;
        
        this.score[0] = score;
        this.lives[0] = livesRemaining;
        this.bulletsShot[0] = bulletsShot;
        this.shipsDestroyed[0] = shipsDestroyed;
        
        this.coins = coins; // ADD THIS LINE - edited for 2P mode
        this.coop = false; // 2P: single-player mode
        
        initializeEffectStates();
        initializePlayerItemStates();
    }
    
    /* ------- 2P mode: aggregate totals used by Core/ScoreScreen/UI------- */
    public int getScore() {
        int t = 0;
        for (int p = 0; p < NUM_PLAYERS; p++) {
            t += score[p];
        }
        return t;
    }
    
    public int getLivesRemaining() {
        return sharedLives ? teamLives : (lives[0] + lives[1]);
    }
    
    public int getBulletsShot() {
        int t = 0;
        for (int p = 0; p < NUM_PLAYERS; p++) {
            t += bulletsShot[p];
        }
        return t;
    }
    
    public int getShipsDestroyed() {
        int t = 0;
        for (int p = 0; p < NUM_PLAYERS; p++) {
            t += shipsDestroyed[p];
        }
        return t;
        
    }
    
    
    /* ----- Per-player getters (needed by Score.java) ----- */
    public int getScore(final int p) {
        return (p >= 0 && p < NUM_PLAYERS) ? score[p] : 0;
    }
    
    public int getBulletsShot(final int p) {
        return (p >= 0 && p < NUM_PLAYERS) ? bulletsShot[p] : 0;
    }
    
    public int getShipsDestroyed(final int p) {
        return (p >= 0 && p < NUM_PLAYERS) ? shipsDestroyed[p] : 0;
    }
    
    public void addScore(final int p, final int delta) {
        int realDelta = delta;
        // If ScoreBoost item active, score gain is doubled.
        Integer multiplier = getEffectValue(p, ItemEffect.ItemEffectType.SCOREBOOST);
        if (multiplier != null) {
            realDelta = delta * multiplier;
            logger.info("[GameState] Player " + (p + 1) + " ScoreBoost active (x" + multiplier
                + "). Score changed from " + delta + " to " + realDelta);
        }
        score[p] += realDelta;
    }
    
    public void incBulletsShot(final int p) {
        bulletsShot[p]++;
    }
    
    public void incShipsDestroyed(final int p) {
        shipsDestroyed[p]++;
    }
    
    public boolean getCoop() {
        return this.coop;
    }
    
    // 2P mode: per-player coin tracking
    public int getCoins() {
        return coins;
    } // legacy total for ScoreScreen
    
    public void addCoins(final int p, final int delta) {
        if (p >= 0 && p < NUM_PLAYERS && delta > 0) {
            coins = Math.max(0, coins + delta);
        }
    }
    
    public boolean spendCoins(final int p, final int amount) {
        if (p < 0 || p >= NUM_PLAYERS || amount < 0) {
            return false;
        }
        if (coins < amount) {
            return false;
        }
        coins -= amount;
        return true;
    }
    
    // ===== Mode / life-pool helpers expected elsewhere =====
    public boolean isCoop() {
        return coop;
    }
    
    public boolean isSharedLives() {
        return sharedLives;
    }
    
    public int getTeamLives() {
        return teamLives;
    }
    
    public void addTeamLife(final int n) {
        if (sharedLives) {
            teamLives = Math.min(teamLivesCap, teamLives + Math.max(0, n));
        }
    }
    
    private void decTeamLife(final int n) {
        if (sharedLives) {
            teamLives = Math.max(0, teamLives - Math.max(0, n));
        }
    }
    
    // 2P mode: decrement life (shared pool if enabled; otherwise per player). */
    public void decLife(final int p) {
        if (sharedLives) {
            decTeamLife(1);
        } else if (p >= 0 && p < NUM_PLAYERS && lives[p] > 0) {
            lives[p]--;
        }
    }
    
    // for bonusLife, balance out decLife (+/- life)
    public void addLife(final int p, final int n) {
        if (sharedLives) {
            addTeamLife(n);
        } else if (p >= 0 && p < NUM_PLAYERS) {
            lives[p] = Math.max(0, lives[p] + Math.max(0, n));
        }
    }
    
    
    public int getLevel() {
        return level;
    }
    
    public void nextLevel() {
        level++;
    }
    
    // Team alive if pool > 0 (shared) or any player has lives (separate).
    public boolean teamAlive() {
        return sharedLives ? (teamLives > 0) : (lives[0] > 0 || lives[1] > 0);
    }
    
    // for ItemEffect.java
    public int getTeamLivesCap() {
        return teamLivesCap;
    }
    
    // for ItemEffect.java
    public int get1PlayerLives() {
        
        return lives[0];
    }
    
    /** ---------- Item effects status methods ---------- **/
    
    /**
     * Initialize all possible effects for every player (inactive).
     */
    private void initializeEffectStates() {
        for (int p = 0; p < NUM_PLAYERS; p++) {
            Map<ItemEffectType, EffectState> effectMap = new HashMap<>();
            for (ItemEffectType type : ItemEffectType.values()) {
                effectMap.put(type, new EffectState());
            }
            playerEffects.put(p, effectMap);
        }
    }
    
    /**
     * Initializes per-player item inventories.
     */
    private void initializePlayerItemStates() {
        for (int p = 0; p < NUM_PLAYERS; p++) {
            playerItems[p] = new PlayerItemState();
        }
    }
    
    public void addEffect(int playerIndex, ItemEffectType type, Integer effectValue,
        int durationSeconds) {
        if (playerIndex < 0 || playerIndex >= NUM_PLAYERS) {
            return;
        }
        
        Map<ItemEffectType, EffectState> effects = playerEffects.get(playerIndex);
        if (effects == null) {
            return;
        }
        
        EffectState state = effects.get(type);
        if (state == null) {
            return;
        }
        
        String valueStr = (effectValue != null) ? " (value: " + effectValue + ")" : "";
        
        if (state.active && state.cooldown != null) {
            // Extend existing effect
            state.cooldown.addTime(durationSeconds * 1000);
            
            state.effectValue = effectValue;
            
            logger.info("[GameState] Player " + playerIndex + " extended " + type
                + valueStr + ") by " + durationSeconds + "s to " + state.cooldown.getDuration());
        } else {
            // Start new effect
            state.cooldown = Core.getCooldown(durationSeconds * 1000);
            state.cooldown.reset();
            state.active = true;
            
            state.effectValue = effectValue;
            
            logger.info("[GameState] Player " + playerIndex + " started " + type
                + valueStr + ") for " + durationSeconds + "s");
        }
    }
    
    public boolean hasEffect(int playerIndex, ItemEffectType type) {
        if (playerIndex < 0 || playerIndex >= NUM_PLAYERS) {
            return false;
        }
        
        Map<ItemEffectType, EffectState> effects = playerEffects.get(playerIndex);
        if (effects == null) {
            return false;
        }
        
        EffectState state = effects.get(type);
        if (state == null || !state.active) {
            return false;
        }
        
        return !state.cooldown.checkFinished();
    }
    
    /**
     * Gets the effect value for a specific player and effect type
     *
     * @param playerIndex Index of the player (0 or 1)
     * @param type        Type of effect to check
     * @return Effect value if active, null otherwise
     */
    public Integer getEffectValue(int playerIndex, ItemEffectType type) {
        if (playerIndex < 0 || playerIndex >= NUM_PLAYERS) {
            return null;
        }
        
        Map<ItemEffectType, EffectState> effects = playerEffects.get(playerIndex);
        if (effects == null) {
            return null;
        }
        
        EffectState state = effects.get(type);
        if (state == null || !state.active) {
            return null;
        }
        
        // Check if effect is still valid (not expired)
        if (state.cooldown != null && state.cooldown.checkFinished()) {
            return null;
        }
        
        return state.effectValue;
    }
    
    /**
     * Call this each frame to clean up expired effects
     */
    public void updateEffects() {
        for (int p = 0; p < NUM_PLAYERS; p++) {
            Map<ItemEffectType, EffectState> effects = playerEffects.get(p);
            if (effects == null) {
                continue;
            }
            
            for (Map.Entry<ItemEffectType, EffectState> entry : effects.entrySet()) {
                EffectState state = entry.getValue();
                if (state.active && state.cooldown != null && state.cooldown.checkFinished()) {
                    logger.info(
                        "[GameState] Player " + p + " effect " + entry.getKey() + " expired.");
                    state.active = false;
                    state.cooldown = null;  // Release reference
                    state.effectValue = null;
                }
            }
        }
        
        // Also update cooldowns for active items (per-player).
        updateActiveItemCooldowns();
    }
    
    /**
     * Clear all active effects for a specific player
     */
    public void clearEffects(int playerIndex) {
        //
        if (playerIndex < 0 || playerIndex >= NUM_PLAYERS) {
            return;
        }
        
        Map<ItemEffectType, EffectState> effects = playerEffects.get(playerIndex);
        if (effects == null) {
            return;
        }
        
        // for - all effect types for this player
        for (Map.Entry<ItemEffectType, EffectState> entry : effects.entrySet()) {
            // get effect state
            EffectState state = entry.getValue();
            // if state active then false
            if (state.active) {
                state.active = false;
                state.cooldown = null;
                state.effectValue = null;
            }
        }
        logger.info("[GameState] Player " + playerIndex + ": All effects cleared.");
    }
    
    /**
     * Clear all active effects for all players
     */
    public void clearAllEffects() {
        for (int p = 0; p < NUM_PLAYERS; p++) {
            clearEffects(p);
        }
    }
    
    /**
     * Adds a passive item to the given player's inventory. Passive effects are meant to be handled
     * by game logic / renderers using this list.
     */
    public void addPassiveItem(final int playerIndex, final ItemData data) {
        if (data == null || playerIndex < 0 || playerIndex >= NUM_PLAYERS) {
            return;
        }
        PlayerItemState pis = playerItems[playerIndex];
        if (pis == null) {
            return;
        }
        pis.passiveItems.add(data);
        logger.info("[GameState] Player " + (playerIndex + 1)
            + " gained passive item: " + data.getId());
    }
    
    /**
     * Adds an active (key-activated) item instance for the given player.
     */
    public void addActiveItem(final int playerIndex, final ItemData data) {
        if (data == null || playerIndex < 0 || playerIndex >= NUM_PLAYERS) {
            return;
        }
        
        if (data.getActivationType() != ActivationType.ACTIVE_ON_KEY) {
            logger.warning("[GameState] addActiveItem called with non-ACTIVE_ON_KEY item: type="
                + data.getType() + ", activation=" + data.getActivationType());
        }
        
        PlayerItemState pis = playerItems[playerIndex];
        if (pis == null) {
            return;
        }
        
        setActiveSlot(playerIndex, new ActiveItemInstance(data));
        
        logger.info("[GameState] Player " + (playerIndex + 1)
            + " gained active item: " + data.getId());
    }
    
    /**
     * Uses the first available active item for this player. (Simple v1: no slot UI, no selection)
     */
    public void useFirstActiveItem(final int playerIndex) {
        if (playerIndex < 0 || playerIndex >= NUM_PLAYERS) {
            return;
        }
        
        ActiveItemInstance inst = getActiveSlot(playerIndex);
        if (inst == null) {
            return;
        }
        
        if (inst.isOnCooldown()) {
            logger.info("[GameState] Active item on cooldown: " + inst.data.getId());
            return;
        }
        
        ItemData data = inst.data;
        String type = data.getType().toUpperCase();
        
        boolean applied = false;
        
        switch (type) {
            case "MOVE_SPEED_UP" -> {
                addEffect(
                    playerIndex,
                    ItemEffectType.MOVE_SPEED_UP,
                    data.getEffectValue(),
                    data.getEffectDuration()
                );
                logger.info("[GameState] MOVE_SPEED_UP activated!");
                applied = true;
            }
            
            case "TIME_FREEZE" -> {
                int playerId = playerIndex + 1;
                boolean ok = ItemEffect.applyTimeFreeze(
                    this,
                    playerId,
                    data.getEffectValue(),
                    data.getEffectDuration()
                );
                if (ok) {
                    logger.info("[GameState] TIME_FREEZE activated by P" + playerId
                        + " for " + data.getEffectDuration() + "s.");
                    applied = true;
                } else {
                    logger.info(
                        "[GameState] TIME_FREEZE failed to activate (maybe null GameState).");
                }
            }
            
            case "TIME_SLOW" -> {
                int playerId = playerIndex + 1;
                boolean ok = ItemEffect.applyTimeSlow(
                    this,
                    playerId,
                    data.getEffectValue(),      // slow % (e.g., 50)
                    data.getEffectDuration()    // seconds
                );
                if (ok) {
                    logger.info("[GameState] TIME_SLOW activated by P" + playerId
                        + " (value=" + data.getEffectValue()
                        + ", duration=" + data.getEffectDuration() + "s)");
                    applied = true;
                }
            }
            
            case "DASH" -> {
                int playerId = playerIndex + 1;
                boolean ok = ItemEffect.applyDash(
                    this,
                    playerId,
                    data.getEffectValue(),
                    data.getEffectDuration()
                );
                if (ok) {
                    logger.info("[GameState] DASH activated by P" + playerId
                        + " x" + data.getEffectValue()
                        + " for " + data.getEffectDuration() + "s.");
                    applied = true;
                }
            }
            
            default -> {
                // Handle PET_* family
                if (type.startsWith("PET_")) {
                    addEffect(
                        playerIndex,
                        ItemEffectType.PET_SUPPORT,
                        data.getEffectValue(),
                        data.getEffectDuration()
                    );
                    logger.info("[GameState] PET item activated: " + type
                        + " (value=" + data.getEffectValue()
                        + ", duration=" + data.getEffectDuration() + "s)");
                    applied = true;
                } else {
                    logger.info("[GameState] Active item type not handled: " + type);
                }
            }
        }
        // TODO: 다른 active 아이템들 추가 예정
        
        if (applied) {
            clearActiveSlot(playerIndex);
            logger.info("[GameState] Player " + (playerIndex + 1)
                + " used active item and cleared slot: " + data.getId());
        }
    }
    
    /**
     * Returns an unmodifiable view of the player's passive items.
     */
    public List<ItemData> getPassiveItems(final int playerIndex) {
        if (playerIndex < 0 || playerIndex >= NUM_PLAYERS) {
            return Collections.emptyList();
        }
        PlayerItemState pis = playerItems[playerIndex];
        if (pis == null) {
            return Collections.emptyList();
        }
        return Collections.unmodifiableList(pis.passiveItems);
    }
    
    /**
     * Returns an unmodifiable view of the player's active items as ItemData list. (Internal
     * ActiveItemInstance details are kept private to GameState.)
     */
    public List<ItemData> getActiveItemData(final int playerIndex) {
        if (playerIndex < 0 || playerIndex >= NUM_PLAYERS) {
            return Collections.emptyList();
        }
        PlayerItemState pis = playerItems[playerIndex];
        if (pis == null) {
            return Collections.emptyList();
        }
        
        List<ItemData> result = new ArrayList<>();
        for (ActiveItemInstance inst : pis.activeItems) {
            result.add(inst.data);
        }
        return Collections.unmodifiableList(result);
    }
    
    /**
     * Updates per-item cooldowns for all active items. This can be extended later when we add
     * "useActiveItem" logic.
     */
    private void updateActiveItemCooldowns() {
        for (int p = 0; p < NUM_PLAYERS; p++) {
            PlayerItemState pis = playerItems[p];
            if (pis == null) {
                continue;
            }
            
            for (ActiveItemInstance inst : pis.activeItems) {
                if (inst.cooldown != null && inst.cooldown.checkFinished()) {
                    inst.cooldown = null;
                }
            }
        }
    }
    
    private ActiveItemInstance getActiveSlot(final int playerIndex) {
        PlayerItemState pis = playerItems[playerIndex];
        if (pis == null || pis.activeItems.isEmpty()) {
            return null;
        }
        return pis.activeItems.get(0);
    }
    
    private void setActiveSlot(final int playerIndex, final ActiveItemInstance inst) {
        PlayerItemState pis = playerItems[playerIndex];
        if (pis == null) {
            return;
        }
        
        pis.activeItems.clear();
        if (inst != null) {
            pis.activeItems.add(inst);
        }
    }
    
    private void clearActiveSlot(final int playerIndex) {
        PlayerItemState pis = playerItems[playerIndex];
        if (pis == null) {
            return;
        }
        pis.activeItems.clear();
    }
    
    /**
     * Apply a global freeze effect that will expire after the given duration.
     */
    public void applyGlobalFreeze(long durationMillis) {
        long now = System.currentTimeMillis();
        this.enemiesFrozen = true;
        this.enemiesFrozenUntilMillis = now + durationMillis;
    }
    
    /**
     * Returns whether enemies are currently frozen. Also clears the flag automatically when time
     * has passed.
     */
    public boolean areEnemiesFrozen() {
        if (!enemiesFrozen) {
            return false;
        }
        long now = System.currentTimeMillis();
        if (now >= enemiesFrozenUntilMillis) {
            enemiesFrozen = false;
            return false;
        }
        return true;
    }
    
    /**
     * Returns a global enemy speed multiplier based on TIME_SLOW effects.
     */
    public double getEnemySpeedMultiplier() {
        int maxSlowPercent = 0;
        
        for (int p = 0; p < NUM_PLAYERS; p++) {
            Integer slow = getEffectValue(p, ItemEffect.ItemEffectType.TIME_SLOW);
            if (slow != null) {
                maxSlowPercent = Math.max(maxSlowPercent, slow);
            }
        }
        
        if (maxSlowPercent <= 0) {
            return 1.0;
        }
        
        double factor = (100 - maxSlowPercent) / 100.0;
        if (factor < 0.0) {
            factor = 0.0;
        }
        return factor;
    }
}
