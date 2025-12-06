package engine.gameplay.item;

import engine.Core;
import engine.GameState;
import java.util.logging.Logger;

public class ItemEffect {
    
    private static final Logger logger = Core.getLogger();
    
    public enum ItemEffectType {
        SCOREBOOST,
        MOVE_SPEED_UP,
        ENEMY_HP_DOWN,
        TIME_SLOW,
        DASH,
        SHIELD,
        PET_SUPPORT
    }
    
    /**=========================SINGLE USE=================================**/
    
    /**
     * Applies the coin item effect to the specified player.
     *
     * @param gameState  current game state instance.
     * @param playerId   ID of the player to apply the effect to.
     * @param coinAmount amount of coins to add.
     */
    public static void applyCoinItem(final GameState gameState, final int playerId,
        int coinAmount) {
        if (gameState == null) {
            return;
        }
        final int playerIndex = getPlayerIndex(playerId);
        final int beforeCoin = gameState.getCoins();
        
        gameState.addCoins(playerIndex, coinAmount);
        
        logger.info("Player " + playerId + " added " + coinAmount + " coins. before : " + beforeCoin
            + ", after : " + gameState.getCoins());
    }
    
    /**
     * Applies the heal item effect to the specified player.
     *
     * @param gameState  current game state instance.
     * @param playerId   ID of the player to apply the effect to.
     * @param lifeAmount amount of lives to add.
     */
    public static void applyHealItem(final GameState gameState, final int playerId,
        int lifeAmount) {
        if (gameState == null) {
            return;
        }
        final int beforeLife = gameState.getLivesRemaining();
        final int playerIndex = getPlayerIndex(playerId);
        
        // if 2p mode
        if (gameState.isCoop()) {
            if (gameState.getTeamLives() + lifeAmount > gameState.getTeamLivesCap()) {
                // if adding life exceeds max, add score and coin instead
                gameState.addCoins(playerIndex, lifeAmount * 20);
                gameState.addScore(playerIndex, lifeAmount * 20);
            } else {
                gameState.addLife(playerIndex, lifeAmount);
            }
        } else { // 1p mode
            if (gameState.get1PlayerLives() + lifeAmount > 3) {
                // if adding life exceeds max, add score and coin instead
                gameState.addScore(playerIndex, lifeAmount * 20);
                gameState.addCoins(playerIndex, lifeAmount * 20);
            } else {
                gameState.addLife(playerIndex, lifeAmount);
            }
        }
        
        logger.info("Player added " + lifeAmount + " lives. before : " + beforeLife + ", after : "
            + gameState.getLivesRemaining());
    }
    
    /**
     * Applies the score item effect to the specified player.
     *
     * @param gameState   current game state instance.
     * @param playerId    ID of the player to apply the effect to.
     * @param scoreAmount amount of score to add.
     */
    public static void applyScoreItem(final GameState gameState, final int playerId,
        int scoreAmount) {
        if (gameState == null) {
            return;
        }
        final int playerIndex = getPlayerIndex(playerId);
        final int beforeScore = gameState.getScore(playerIndex);
        
        gameState.addScore(playerIndex, scoreAmount);
        
        logger.info(
            "[ItemEffect - SCORE] Player " + playerId + " : " + beforeScore + " + " + scoreAmount
                + " -> " + gameState.getScore(playerIndex));
    }
    
    /**========================= DURATION ITEM =================================**/
    /**
     * Applies the ScoreBoost timed effect to the specified player.
     *
     * @param gameState   current game state instance.
     * @param playerIndex index of the player to apply the effect to (0-based).
     * @param effectValue effect value (e.g., multiply score by this).
     * @param duration    duration in seconds.
     */
    public static boolean applyScoreBoost(final GameState gameState, final int playerIndex,
        int effectValue, int duration) {
        if (gameState == null) {
            return false;
        }
        
        gameState.addEffect(playerIndex, ItemEffectType.SCOREBOOST, effectValue, duration);
        int playerId = playerIndex + 1;
        logger.info("[ItemEffect - SCOREBOOST] Player " + playerId + " applied for " + duration
            + "s. Score gain will be multiplied by " + effectValue + ".");
        
        return true;
    }
    
    /**
     * Applies the MoveSpeedUp timed effect to the specified player.
     *
     * @param gameState   current game state instance.
     * @param playerIndex index of the player to apply the effect to (0-based).
     * @param effectValue effect value (e.g., speed multiplier or delta).
     * @param duration    duration in seconds.
     */
    public static boolean applyMoveSpeedUp(final GameState gameState, final int playerIndex,
        int effectValue, int duration) {
        if (gameState == null) {
            return false;
        }
        
        gameState.addEffect(playerIndex, ItemEffectType.MOVE_SPEED_UP, effectValue, duration);
        int playerId = playerIndex + 1;
        logger.info(
            "[ItemEffect - MOVE_SPEED_UP] Player " + playerId + " applied for " + duration + "s.");
        
        return true;
    }
    
    /**
     * Applies a global time-freeze effect. Duration is given in seconds.
     */
    public static boolean applyTimeFreeze(
        GameState gameState,
        int playerIndex,
        int value,
        int duration
    ) {
        if (gameState == null) {
            return false;
        }
        
        int safeDuration = Math.max(1, duration);
        long durationMillis = safeDuration * 1000L;
        
        gameState.applyGlobalFreeze(durationMillis);
        
        int playerId = playerIndex + 1;
        logger.info("[ItemEffect - TIME_FREEZE] Player " + playerId + " froze enemies for "
            + safeDuration + "s.");
        
        return true;
    }
    
    /**
     * Applies a global time-slow effect to enemies. value: slow percentage (e.g., 50 -> enemies
     * move at 50% speed) duration: seconds
     */
    public static boolean applyTimeSlow(
        GameState gameState,
        int playerIndex,
        int value,
        int duration
    ) {
        if (gameState == null) {
            return false;
        }
        
        int safeDuration = Math.max(1, duration);
        int slowPercent = Math.max(0, Math.min(100, value));
        
        gameState.addEffect(
            playerIndex,
            ItemEffectType.TIME_SLOW,
            slowPercent,
            safeDuration
        );
        
        int playerId = playerIndex + 1;
        logger.info("[ItemEffect - TIME_SLOW] Player " + playerId
            + " slowed enemies by " + slowPercent + "% for " + safeDuration + "s.");
        
        return true;
    }
    
    public static boolean applyDash(
        final GameState gameState,
        final int playerIndex,
        final int speedMultiplier,
        final int durationSeconds
    ) {
        if (gameState == null) {
            return false;
        }
        
        int safeDuration = Math.max(1, durationSeconds);
        
        gameState.addEffect(
            playerIndex,
            ItemEffectType.DASH,
            speedMultiplier,
            safeDuration
        );
        
        int playerId = playerIndex + 1;
        logger.info("[ItemEffect - DASH] Player " + playerId
            + " dash x" + speedMultiplier
            + " for " + safeDuration + "s.");
        
        return true;
    }
    
    public static boolean applyPetSupport(
        final GameState gameState,
        final int playerId,
        final int duration
    ) {
        if (gameState == null) {
            return false;
        }
        
        int safeDuration = Math.max(1, duration);
        int playerIndex = getPlayerIndex(playerId);
        
        gameState.addEffect(
            playerIndex,
            ItemEffectType.PET_SUPPORT,
            null,
            safeDuration
        );
        
        logger.info("[ItemEffect - PET_SUPPORT] Player " + playerId
            + " spawned pet support for " + safeDuration + "s.");
        
        return true;
    }
    
    /**
     * Converts a playerId (unknown : 0, player1 : 1, player2 : 2) to the corresponding array
     * index.
     *
     * @param playerId ID of the player (0, 1, 2)
     * @return array index (player1 or unknown : 0, player2 : 1)
     */
    private static int getPlayerIndex(final int playerId) {
        return (playerId == 2) ? 1 : 0;
    }
}