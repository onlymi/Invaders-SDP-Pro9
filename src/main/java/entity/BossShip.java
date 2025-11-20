package entity;

import engine.AssetManager.SpriteType;
import engine.Core;

import java.awt.*;

/**
 * Implements a boss ship, to be destroyed by the player. Extends EnemyShip with boss-specific
 * logic.
 */
public class BossShip extends EnemyShip {

    private static final int BOSS_INITIAL_HEALTH = 500;
    private static final int BOSS_POINTS = 5000;
    private static final int BOSS_COINS = 5000;

    /**
     * Boss-specific movement properties
     */
    private static final int BOSS_BASE_SPEED_X = 2;
    private static final int BOSS_BASE_SPEED_Y = 1;
    private static final int TOP_BOUNDARY = 68;
    private static final int BOSS_MAX_Y = 220;

    private int currentSpeedX;
    private int currentSpeedY;

    private boolean movingRight;
    private boolean movingDown;

    final int screenWidth = Core.WIDTH;
    final int screenHeight = Core.HEIGHT;

    /**
     * Constructor, establishes the boss ship's properties. Initializes with SpriteType.BossEnemy1.
     *
     * @param positionX Initial position of the ship in the X axis.
     * @param positionY Initial position of the ship in the Y axis.
     */
    public BossShip(final int positionX, final int positionY) {
        super(positionX, positionY, SpriteType.BossShip1);

        // Set dimensions to match BossEnemy sprite (21x10, scaled by 2 = 42x20)
        this.width = 21 * 2;
        this.height = 10 * 2;

        // Apply boss-specific, high stats.
        this.health = BOSS_INITIAL_HEALTH;
        this.initialHealth = BOSS_INITIAL_HEALTH;
        this.pointValue = BOSS_POINTS;
        this.coinValue = BOSS_COINS;

        // Set a prominent default color.
        this.changeColor(Color.CYAN);

        //Initialize movement state
        this.currentSpeedX = BOSS_BASE_SPEED_X;
        this.currentSpeedY = BOSS_BASE_SPEED_Y;
        this.movingRight = true;
        this.movingDown = true;
    }

    /**
     * Updates attributes for boss movement and phases. Custom boss logic goes here.
     */
    @Override
    public final void update() {

        // Check Horizontal Boundary
        if (this.positionX + this.width >= screenWidth || this.positionX <= 0) {
            this.movingRight = !this.movingRight;
            if (this.positionX <= 0) this.positionX = 1;
            if (this.positionX + this.width >= screenWidth) this.positionX = screenWidth - this.width - 1;
        }

        // Check Vertical Boundary
        if (this.positionY + this.height >= BOSS_MAX_Y || this.positionY <= TOP_BOUNDARY) {
            this.movingDown = !this.movingDown;
            if (this.positionY <= TOP_BOUNDARY) this.positionY = TOP_BOUNDARY + 1;
            if (this.positionY + this.height >= BOSS_MAX_Y) this.positionY = BOSS_MAX_Y - this.height - 1;
        }

        // Inherited from EnemyShip, checks if 500ms animation interval is finished.
        if (this.bossAnimationCooldown.checkFinished()) {
            this.bossAnimationCooldown.reset();

            // Cycles through BossShip1, BossShip2, BossShip3 for animation
            switch (this.spriteType) {
                case BossShip1:
                    this.spriteType = SpriteType.BossShip1;
                    break;
                case BossShip2:
                    this.spriteType = SpriteType.BossShip2;
                    break;
                case BossShip3:
                    this.spriteType = SpriteType.BossShip3;
                    break;
                default:
                    // Reverts to base sprite if an unknown sprite is encountered
                    this.spriteType = SpriteType.BossShip1;
                    break;
            }
        }
    }

    /**
     * Moves the boss based on its internal speed and direction.
     */
    @Override
    public final void move(final int distanceX, final int distanceY) {
        // The distanceX/Y arguments from EnemyShipFormation are ignored.
        // Boss moves based on its internal state.

        int movementX = this.movingRight ? this.currentSpeedX : -this.currentSpeedX;
        int movementY = this.movingDown ? this.currentSpeedY : -this.currentSpeedY;

        this.positionX += movementX;
        this.positionY += movementY;
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
        }
        // Note: No sprite flipping or animation logic is applied for the boss in hit().

    }
}