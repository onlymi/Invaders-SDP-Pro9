package entity;

import java.awt.Color;

import engine.Core;
import engine.AssetManager.SpriteType;

/**
 * Implements a boss ship, to be destroyed by the player.
 * Extends EnemyShip with boss-specific logic.
 */
public class BossShip extends EnemyShip {

    private static final int BOSS_INITIAL_HEALTH = 500;
    private static final int BOSS_POINTS = 5000;
    private static final int BOSS_COINS = 5000;

    /**
     * Constructor, establishes the boss ship's properties.
     * Initializes with SpriteType.BossEnemy1.
     *
     * @param positionX Initial position of the ship in the X axis.
     * @param positionY Initial position of the ship in the Y axis.
     */
    public BossShip(final int positionX, final int positionY) {
        super(positionX, positionY, SpriteType.BossEnemy1);

        // Apply boss-specific, high stats.
        this.health = BOSS_INITIAL_HEALTH;
        this.initialHealth = BOSS_INITIAL_HEALTH;
        this.pointValue = BOSS_POINTS;
        this.coinValue = BOSS_COINS;

        // Set a prominent default color.
        this.changeColor(Color.CYAN);
    }

    /**
     * Updates attributes for boss movement and phases.
     * Custom boss logic goes here.
     */
    @Override
    public final void update() {
        // Implement complex movement or phase transitions here.
    }

    /**
     * Reduces enemy health by 1.
     */
    @Override
    public final void hit() {
        super.hit();

        // Add special logic for boss damage.
    }
}