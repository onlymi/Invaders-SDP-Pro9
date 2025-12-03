package entity;

import engine.AssetManager.SpriteType;
import engine.Core;
import engine.GameState;
import engine.utils.Cooldown;
import entity.character.GameCharacter;
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
     * Floating animation variable.
     */
    private double floatingPhase;
    private static final double FLOATING_AMPLITUDE = 5.0;
    private static final double FLOATING_SPEED = 0.005;
    boolean isFacingRight;
    
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
    
    protected double preciseX;
    protected double preciseY;
    
    /**
     * variable for enemy knockback.
     */
    protected double knockbackX = 0;
    protected double knockbackY = 0;
    protected double knockbackDecay = 0.9; // 넉벡 감쇠값
    
    protected GameState gameState;
    
    /**
     * Constructor, establishes the ship's properties. Used by EnemyShipFormation and BossShip.
     *
     * @param positionX  Initial position of the ship in the X axis.
     * @param positionY  Initial position of the ship in the Y axis.
     * @param spriteType Sprite type, image corresponding to the ship.
     */
    public EnemyShip(int positionX, int positionY, SpriteType spriteType) {
        super(positionX, positionY, 24, 36, Color.WHITE);
        this.spriteType = spriteType;
        this.animationCooldown = Core.getCooldown(500);
        this.isDestroyed = false;
        this.preciseX = positionX;
        this.preciseY = positionY;
        this.floatingPhase = Math.random() * Math.PI * 2;
        
        initializeStats();
    }
    
    protected void initializeStats() {
        switch (this.spriteType) {
            case EnemyA_Move:
            case EnemyA_Attack:
                this.pointValue = A_TYPE_POINTS;
                this.coinValue = A_TYPE_COINS;
                break;
            case EnemyB_Move:
            case EnemyB_Attack:
                this.pointValue = B_TYPE_POINTS;
                this.coinValue = B_TYPE_COINS;
                break;
            case EnemyC_move:
            case EnemyC_attack:
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
    
    public void update(GameCharacter player) {
        update();
        
        if (player != null && !player.isDestroyed()) {
            moveTowards(player);
        }
        
        long currentTime = System.currentTimeMillis();
        double floatingOffset =
            Math.sin(currentTime * FLOATING_SPEED + this.floatingPhase) * FLOATING_AMPLITUDE;
        
        // 넉벡 적용
        this.preciseX += knockbackX;
        this.preciseY += knockbackY;
        
        // 넉벡 감쇠
        this.knockbackX *= knockbackDecay;
        this.knockbackY *= knockbackDecay;
        if (Math.abs(this.knockbackX) < 0.1) {
            this.knockbackX = 0;
        }
        if (Math.abs(this.knockbackY) < 0.1) {
            this.knockbackY = 0;
        }
        this.positionX = (int) preciseX;
        this.positionY = (int) (preciseY + floatingOffset);
    }
    
    private void moveTowards(GameCharacter player) {
        double targetX = player.positionX;
        double targetY = player.positionY;
        
        double dirX = targetX - this.positionX;
        double dirY = targetY - this.positionY;
        
        if (dirX < 0) {
            this.isFacingRight = false;
        } else {
            this.isFacingRight = true;
        }
        
        double distance = Math.sqrt(dirX * dirX + dirY * dirY);
        
        if (distance > 0) {
            // 단위 벡터로 만들기
            dirX /= distance;
            dirY /= distance;
            
            // 속도 적용
            double speed = 1.0;
            
            // if (type == Type.B && distance < 200) { speed = -speed; }
            this.preciseX += dirX * speed;
            this.preciseY += dirY * speed;
        }
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
        if (spriteType == SpriteType.EnemyA_Move) {
            spriteType = SpriteType.EnemyA_Attack;
        } else if (spriteType == SpriteType.EnemyA_Attack) {
            spriteType = SpriteType.EnemyA_Move;
        } else if (spriteType == SpriteType.EnemyB_Move) {
            spriteType = SpriteType.EnemyB_Attack;
        } else if (spriteType == SpriteType.EnemyB_Attack) {
            spriteType = SpriteType.EnemyB_Move;
        } else if (spriteType == SpriteType.EnemyC_move) {
            spriteType = SpriteType.EnemyC_attack;
        } else if (spriteType == SpriteType.EnemyC_attack) {
            spriteType = SpriteType.EnemyC_move;
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
     * applying a knockback force.
     *
     * @param dx
     * @param dy
     */
    public void pushBack(double dx, double dy) {
        this.knockbackX = dx;
        this.knockbackY = dy;
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
    
    public boolean isFacingRight() {
        return this.isFacingRight;
    }
}
