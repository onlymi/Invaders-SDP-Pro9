package entity;

import engine.AssetManager.SpriteType;
import engine.Core;
import engine.GameState;
import engine.utils.Cooldown;
import java.awt.Color;


/**
 * Implements an enemy ship, to be destroyed by the player.
 *
 * @author <a href="mailto:RobertoIA1987@gmail.com">Roberto Izquierdo Amo</a>
 *
 */
public class EnemyShip extends Entity {
    
    /**
     * Point value & Coin of enemy type.
     */
    private static final int A_TYPE_POINTS = 10;
    private static final int B_TYPE_POINTS = 20;
    private static final int C_TYPE_POINTS = 30;
    
    private static final int A_TYPE_COINS = 2;
    private static final int B_TYPE_COINS = 3;
    private static final int C_TYPE_COINS = 5;
    /**
     * Checks if the ship has been hit by a bullet.
     */
    protected boolean isDestroyed;
    /**
     * Cooldown between sprite changes.
     */
    protected Cooldown animationCooldown;
    /**
     * Values of the ship, in points, when destroyed.
     */
    protected int pointValue;
    protected int coinValue;
    protected Cooldown bossAnimationCooldown;
    /**
     * Current health of the enemy ship.
     */
    protected int health;
    protected int initialHealth;
    
    protected GameState gameState;
    
    /**
     * [Legacy Support] Constructor for Special Ship (Bonus Enemy).
     */
    public EnemyShip() {
        this(0, 0, SpriteType.EnemyShipSpecial);
    }
    
    /**
     * Constructor, establishes the ship's properties. Used by EnemyShipFormation and BossShip.
     *
     * @param positionX  Initial position of the ship in the X axis.
     * @param positionY  Initial position of the ship in the Y axis.
     * @param spriteType Sprite type, image corresponding to the ship.
     */
    public EnemyShip(int positionX, int positionY, SpriteType spriteType) {
        super(positionX, positionY, 12 * 2, 8 * 2, Color.WHITE);
        this.spriteType = spriteType;
        this.animationCooldown = Core.getCooldown(500);
        this.isDestroyed = false;
        
        initializeStats();
    }
    
    protected void initializeStats() {
        switch (this.spriteType) {
            case EnemyShipA1:
            case EnemyShipA2:
                this.pointValue = A_TYPE_POINTS;
                this.coinValue = A_TYPE_COINS;
                break;
            case EnemyShipB1:
            case EnemyShipB2:
                this.pointValue = B_TYPE_POINTS;
                this.coinValue = B_TYPE_COINS;
                break;
            case EnemyShipC1:
            case EnemyShipC2:
                this.pointValue = C_TYPE_POINTS;
                this.coinValue = C_TYPE_COINS;
                break;
            case BossShip1:
            case BossShip2:
            case BossShip3:
                this.pointValue = 1000;
                this.coinValue = 1000;
                this.health = 50;
                break;
            default:
                this.pointValue = 0;
                this.coinValue = 0;
                break;
        }
    }

//    public void changeShip(GameSettings.ChangeData changeData) {
//        this.health *= changeData.hp;
//        this.initialHealth = this.health;
//
//        this.changeColor(changeData.color);
//
//        this.pointValue *= changeData.multiplier;
//        this.coinValue *= changeData.multiplier;
//    }
    
    /**
     * Getter for the score bonus if this ship is destroyed.
     *
     * @return Value of the ship.
     */
    public final int getPointValue() {
        return this.pointValue;
    }
    
    public void move() {
        this.positionX += 2;
    }
    
    /**
     * Moves the ship the specified distance.
     *
     * @param distanceX Distance to move in the X axis.
     * @param distanceY Distance to move in the Y axis.
     */
    public void move(final int distanceX, final int distanceY) {
        this.positionX += distanceX;
        this.positionY += distanceY;
    }
    
    /**
     * Updates attributes, mainly used for animation purposes.
     */
    public void update() {
        if (this.isDestroyed) {
            return;
        }
        if (this.animationCooldown.checkFinished()) {
            this.animationCooldown.reset();
            changeAnimationSprite();
        }
        move();
    }
    
    /**
     * Updates the enemy ship with support for global freeze effects. If the GameState indicates
     * that enemies are frozen, the ship will not update.
     */
    public void update(engine.GameState gameState) {
        if (gameState != null && gameState.areEnemiesFrozen()) {
            // Skip animation update while enemies are frozen.
            return;
        }
        // Fallback to the original update logic.
        update();
    }
    
    /**
     * Returns the current health of the enemy ship.
     */
    public int getHealth() {
        return this.health;
    }
    
    /**
     * Reduces enemy health by 1 and handles destruction or damage animation if health drops to 0.
     */
    
    public void hit() {
        hit(1);
    }
    
    public void hit(int damage) {
        if (this.isDestroyed) {
            return;
        }
        this.health -= damage;
        if (this.health <= 0) {
            destroy();
        }
    }
    
    private void changeAnimationSprite() {
        if (spriteType == SpriteType.EnemyShipA1) {
            spriteType = SpriteType.EnemyShipA2;
        } else if (spriteType == SpriteType.EnemyShipA2) {
            spriteType = SpriteType.EnemyShipA1;
        } else if (spriteType == SpriteType.EnemyShipB1) {
            spriteType = SpriteType.EnemyShipB2;
        } else if (spriteType == SpriteType.EnemyShipB2) {
            spriteType = SpriteType.EnemyShipB1;
        } else if (spriteType == SpriteType.EnemyShipC1) {
            spriteType = SpriteType.EnemyShipC2;
        } else if (spriteType == SpriteType.EnemyShipC2) {
            spriteType = SpriteType.EnemyShipC1;
        }
    }
    
    public final int getDamage(int dmg) {
        this.health -= dmg;
        return this.health;
    }
    
    /**
     * Destroys the ship, causing an explosion.
     */
    public final void destroy() {
        this.isDestroyed = true;
        this.spriteType = SpriteType.Explosion;
    }
    
    /**
     * Checks if the ship has been destroyed.
     *
     * @return True if the ship has been destroyed.
     */
    public boolean isDestroyed() {
        return this.isDestroyed;
    }
    
    public int getCoinValue() {
        return this.coinValue;
    }
    
    public int getInitialHealth() {
        return this.initialHealth;
    }
}
