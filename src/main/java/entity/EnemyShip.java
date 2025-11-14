package entity;

import engine.AssetManager.SpriteType;
import engine.Core;
import engine.GameSettings;
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
     * Point value of a type A enemy.
     */
    private static final int A_TYPE_POINTS = 10;
    /**
     * Point value of a type B enemy.
     */
    private static final int B_TYPE_POINTS = 20;
    /**
     * Point value of a type C enemy.
     */
    private static final int C_TYPE_POINTS = 30;
    /**
     * Point value of a bonus enemy.
     */
    private static final int BONUS_TYPE_POINTS = 100;
    
    private static final int A_TYPE_COINS = 2;
    private static final int B_TYPE_COINS = 3;
    private static final int C_TYPE_COINS = 5;
    private static final int BONUS_TYPE_COINS = 10;
    
    /**
     * Cooldown between sprite changes.
     */
    private Cooldown animationCooldown;
    private Cooldown bossAnimationCooldown;
    /**
     * Checks if the ship has been hit by a bullet.
     */
    protected boolean isDestroyed;
    /**
     * Values of the ship, in points, when destroyed.
     */
    protected int pointValue;
    
    protected int coinValue;
    
    /**
     * Current health of the enemy ship
     */
    protected int health;
    protected int initialHealth;
    
    /**
     * Constructor, establishes the ship's properties.
     *
     * @param positionX  Initial position of the ship in the X axis.
     * @param positionY  Initial position of the ship in the Y axis.
     * @param spriteType Sprite type, image corresponding to the ship.
     */
    public EnemyShip(final int positionX, final int positionY,
        final SpriteType spriteType) {
        super(positionX, positionY, 12 * 2, 8 * 2, Color.WHITE);
        
        this.spriteType = spriteType;
        this.animationCooldown = Core.getCooldown(500);
        this.bossAnimationCooldown = Core.getCooldown(500);
        this.isDestroyed = false;
        
        switch (this.spriteType) {
            case EnemyShipA1:
            case EnemyShipA2:
                this.pointValue = A_TYPE_POINTS;
                this.coinValue = A_TYPE_COINS;
                this.health = 2;
                break;
            case EnemyShipB1:
            case EnemyShipB2:
                this.pointValue = B_TYPE_POINTS;
                this.coinValue = B_TYPE_COINS;
                this.health = 1;
                break;
            case EnemyShipC1:
            case EnemyShipC2:
                this.pointValue = C_TYPE_POINTS;
                this.coinValue = C_TYPE_COINS;
                this.health = 1;
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
                this.health = 1;
                break;
        }
        
        this.initialHealth = this.health;
    }
    
    public void changeShip(GameSettings.ChangeData changeData) {
        this.health *= changeData.hp;
        this.initialHealth = this.health;
        
        this.changeColor(changeData.color);
        
        this.pointValue *= changeData.multiplier;
        this.coinValue *= changeData.multiplier;
    }
    
    /**
     * Constructor, establishes the ship's properties for a special ship, with known starting
     * properties.
     */
    public EnemyShip() {
        super(-32, 80, 16 * 2, 7 * 2, Color.RED);
        
        this.spriteType = SpriteType.EnemyShipSpecial;
        this.isDestroyed = false;
        this.pointValue = BONUS_TYPE_POINTS;
        this.coinValue = BONUS_TYPE_COINS;
        this.health = 1;
    }
    
    /**
     * Getter for the score bonus if this ship is destroyed.
     *
     * @return Value of the ship.
     */
    public final int getPointValue() {
        return this.pointValue;
    }
    
    /**
     * Moves the ship the specified distance.
     *
     * @param distanceX Distance to move in the X axis.
     * @param distanceY Distance to move in the Y axis.
     */
    public final void move(final int distanceX, final int distanceY) {
        this.positionX += distanceX;
        this.positionY += distanceY;
    }
    
    /**
     * Updates attributes, mainly used for animation purposes.
     */
    public void update() {
        if (this.animationCooldown.checkFinished()) {
            this.animationCooldown.reset();
            
            switch (this.spriteType) {
                case EnemyShipA1:
                    this.spriteType = SpriteType.EnemyShipA2;
                    break;
                case EnemyShipA2:
                    this.spriteType = SpriteType.EnemyShipA1;
                    break;
                case EnemyShipB1:
                    this.spriteType = SpriteType.EnemyShipB2;
                    break;
                case EnemyShipB2:
                    this.spriteType = SpriteType.EnemyShipB1;
                    break;
                case EnemyShipC1:
                    this.spriteType = SpriteType.EnemyShipC2;
                    break;
                case EnemyShipC2:
                    this.spriteType = SpriteType.EnemyShipC1;
                    break;
                default:
                    break;
            }
        }
    }
    
    /**
     * Returns the current health of the enemy ship
     */
    public int getHealth() {
        return this.health;
    }
    
    /**
     * Reduces enemy health by 1 and handles destruction or damage animation if health drops to 0
     */
    
    public void hit() {
        this.health--;
        if (this.health <= 0) {
            this.isDestroyed = true;
            this.spriteType = SpriteType.Explosion;
            Color color = this.getColor();
            color = new Color(color.getRed(), color.getGreen(), color.getBlue(), 255);
            changeColor(color);
        } else {
            switch (this.spriteType) {
                case EnemyShipA1:
                case EnemyShipA2:
                    this.spriteType = SpriteType.EnemyShipA2;
                    break;
                case EnemyShipB1:
                case EnemyShipB2:
                    this.spriteType = SpriteType.EnemyShipB2;
                    break;
                case EnemyShipC1:
                case EnemyShipC2:
                    this.spriteType = SpriteType.EnemyShipC2;
                    break;
                default:
                    break;
            }
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
    
    /**
     * Returns the initial health of the enemy ship
     */
    public final int getInitialHealth() {
        return this.initialHealth;
    }
}
