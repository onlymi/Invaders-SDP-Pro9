package engine.gameplay.item;

import engine.Core;
import engine.GameState;
import java.util.logging.Logger;

public class ItemEffect {
    
    private static final Logger logger = Core.getLogger();
    
    public enum ItemEffectType {
        TRIPLESHOT,
        SCOREBOOST,
        BULLETSPEEDUP,
        MOVE_SPEED_UP,
        ENEMY_HP_DOWN,
        TIME_SLOW,
        DASH
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
        
        // if 2p mode
        if (gameState.isCoop()) {
            if (gameState.getTeamLives() + lifeAmount > gameState.getTeamLivesCap()) {
                // if adding life exceeds max, add score and coin instead
                gameState.addCoins(getPlayerIndex(playerId), lifeAmount * 20);
                gameState.addScore(getPlayerIndex(playerId), lifeAmount * 20);
            } else {
                gameState.addLife(getPlayerIndex(playerId), lifeAmount);
            }
        } else { // 1p mode
            if (gameState.get1PlayerLives() + lifeAmount > 3) {
                // if adding life exceeds max, add score and coin instead
                gameState.addScore(getPlayerIndex(playerId), lifeAmount * 20);
                gameState.addCoins(getPlayerIndex(playerId), lifeAmount * 20);
            } else {
                gameState.addLife(getPlayerIndex(playerId), lifeAmount);
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
     * Applies the TripleShot timed effect to the specified player. Cost/coins are no longer
     * required – effect is always applied if gameState is valid.
     */
    public static boolean applyTripleShot(final GameState gameState, final int playerId,
        int effectValue, int duration) {
        if (gameState == null) {
            return false;
        }
        
        int playerIndex = getPlayerIndex(playerId);
        
        gameState.addEffect(playerIndex, ItemEffectType.TRIPLESHOT, effectValue, duration);
        logger.info(
            "[ItemEffect - TRIPLESHOT] Player " + playerId + " applied for " + duration + "s.");
        
        return true;
    }
    
    /**
     * Applies the ScoreBoost timed effect to the specified player.
     */
    public static boolean applyScoreBoost(final GameState gameState, final int playerId,
        int effectValue, int duration) {
        if (gameState == null) {
            return false;
        }
        
        final int playerIndex = getPlayerIndex(playerId);
        
        gameState.addEffect(playerIndex, ItemEffectType.SCOREBOOST, effectValue, duration);
        logger.info("[ItemEffect - SCOREBOOST] Player " + playerId + " applied for " + duration
            + "s. Score gain will be multiplied by " + effectValue + ".");
        
        return true;
    }
    
    /**
     * Applies the BulletSpeedUp timed effect to the specified player.
     */
    public static boolean applyBulletSpeedUp(final GameState gameState, final int playerId,
        int effectValue, int duration) {
        if (gameState == null) {
            return false;
        }
        
        int playerIndex = getPlayerIndex(playerId);
        
        gameState.addEffect(playerIndex, ItemEffectType.BULLETSPEEDUP, effectValue, duration);
        logger.info(
            "[ItemEffect - BULLETSPEEDUP] Player " + playerId + " applied for " + duration + "s.");
        
        return true;
    }
    
    /**
     * Applies the MoveSpeedUp timed effect to the specified player.
     */
    public static boolean applyMoveSpeedUp(final GameState gameState, final int playerId,
        int effectValue, int duration) {
        if (gameState == null) {
            return false;
        }
        
        int playerIndex = getPlayerIndex(playerId);
        
        gameState.addEffect(playerIndex, ItemEffectType.MOVE_SPEED_UP, effectValue, duration);
        logger.info(
            "[ItemEffect - MOVE_SPEED_UP] Player " + playerId + " applied for " + duration + "s.");
        
        return true;
    }
    
    /**
     * Applies a global time-freeze effect. Duration is given in seconds.
     */
    public static boolean applyTimeFreeze(
        GameState gameState,
        int playerId,
        int value,
        int duration
    ) {
        if (gameState == null) {
            return false;
        }
        
        // Duration is given in seconds
        int safeDuration = Math.max(1, duration);
        long durationMillis = safeDuration * 1000L;
        
        // Apply freeze to the whole enemy system
        gameState.applyGlobalFreeze(durationMillis);
        
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
        int playerId,
        int value,
        int duration
    ) {
        if (gameState == null) {
            return false;
        }
        
        int safeDuration = Math.max(1, duration);
        int slowPercent = Math.max(0, Math.min(100, value));
        
        int playerIndex = (playerId == 2) ? 1 : 0;
        
        gameState.addEffect(
            playerIndex,
            ItemEffectType.TIME_SLOW,
            slowPercent,
            safeDuration
        );
        
        logger.info("[ItemEffect - TIME_SLOW] Player " + playerId
            + " slowed enemies by " + slowPercent + "% for " + safeDuration + "s.");
        
        return true;
    }
    
    public static boolean applyDash(
        final GameState gameState,
        final int playerId,
        final int speedMultiplier,
        final int durationSeconds
    ) {
        if (gameState == null) {
            return false;
        }
        
        int safeDuration = Math.max(1, durationSeconds);
        
        int playerIndex = getPlayerIndex(playerId);
        
        // Effect Value (2배, 3배)
        gameState.addEffect(
            playerIndex,
            ItemEffectType.DASH,
            speedMultiplier,
            safeDuration
        );
        
        logger.info("[ItemEffect - DASH] Player " + playerId
            + " dash x" + speedMultiplier
            + " for " + safeDuration + "s.");
        
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