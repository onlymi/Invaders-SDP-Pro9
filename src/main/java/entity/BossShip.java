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
     * Returns the current health of the boss ship.
     */
    @Override
    public final int getHealth() {
        return this.health;
    }

    /**
     * Reduces boss health by 1 and handles destruction or damage animation based on remaining HP.
     */
    @Override
    public final void hit() {
        this.health--;
        if (this.health <= 0) {
            this.isDestroyed = true;
            this.spriteType = SpriteType.Explosion;
            Color color = this.getColor();
            // Ensure full alpha upon destruction for explosion effect
            color = new Color(color.getRed(), color.getGreen(), color.getBlue(), 255);
            changeColor(color);
        } else {
            // Apply color change logic based on HP ratio for damage feedback
            Color color = this.getColor();
            if(initialHealth != 0) {
                // Alpha range from a minimum visible (70) to full (255) based on remaining HP
                int rawAlpha = (int)(70 + 150 * (float)health / initialHealth);
                int alpha = Math.max(0, Math.min(255, rawAlpha));

                color = new Color(color.getRed(), color.getGreen(), color.getBlue(), alpha);
                changeColor(color);
            }
            // Note: No sprite flipping or animation logic is applied for the boss in hit().
        }
    }
}