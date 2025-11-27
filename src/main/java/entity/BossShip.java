package entity;

import engine.AssetManager.SpriteType;
import engine.Core;
import engine.SoundManager;
import engine.utils.Cooldown;

import java.awt.*;
import java.util.Set;

/**
 * Implements a boss ship, to be destroyed by the player.
 * Extends EnemyShip with boss-specific logic.
 */
public class BossShip extends EnemyShip {

    private static final int BOSS_INITIAL_HEALTH = 500;
    private static final int BOSS_POINTS = 5000;
    private static final int BOSS_COINS = 5000;

    private static final int ATTACK_HOMING_MISSILE = 1;
    private static final int ATTACK_LASER_CHARGE = 2;
    private static final int HOMING_MISSILE_INTERVAL = 1000;
    private static final int LASER_CHARGE_TIME = 1000;
    private static final int MISSILE_SPEED = 4;
    private static final int ATTACK_SPREAD_CHARGE = 3;
    private static final int SPREAD_CHARGE_TIME = 500;
    private static final int SPREAD_BULLETS = 16;
    private static final int SPREAD_SPEED = 4;
    private static final int BOSS_BULLET_WIDTH = 20;
    private static final int BOSS_BULLET_HEIGHT = 20;

    /**
     * Boss-specific movement properties
     */
    private static final int BOSS_BASE_SPEED_X = 2;
    private static final int BOSS_BASE_SPEED_Y = 1;
    private static final int TOP_BOUNDARY = 68;
    private static final int BOSS_MAX_Y = 340;

    private int currentSpeedX;
    private int currentSpeedY;

    boolean movingRight;
    private boolean movingDown;

    private int attackPhase;
    private int laserChargeTimer;
    private int spreadChargeTimer;
    private Cooldown attackCooldown;
    private Cooldown laserChargeCooldown;
    private Cooldown spreadChargeCooldown;

    private final int BOSS_ATTACK_HP_THRESHOLD;
    private boolean isAttackEnabled;

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

        // Set a BOSS_ATTACK_HP_THRESHOLD.
        this.BOSS_ATTACK_HP_THRESHOLD = this.initialHealth / 2;

        // Set a prominent default color.
        this.changeColor(Color.CYAN);

        //Initialize movement state
        this.currentSpeedX = BOSS_BASE_SPEED_X;
        this.currentSpeedY = BOSS_BASE_SPEED_Y;
        this.movingRight = true;
        this.movingDown = true;

        this.attackPhase = ATTACK_HOMING_MISSILE;
        this.attackCooldown = Core.getCooldown(HOMING_MISSILE_INTERVAL);
        this.attackCooldown.reset();
        this.laserChargeCooldown = Core.getCooldown(LASER_CHARGE_TIME);
        this.spreadChargeCooldown = Core.getCooldown(SPREAD_CHARGE_TIME);
        this.laserChargeTimer = 0;
        this.spreadChargeTimer = 0;
        this.isAttackEnabled = true;
    }

    /**
     * New shoot method to manage attacks
     */
    public final void shoot(final Set<Bullet> bullets) {
        if (!this.isAttackEnabled) return;

        int spawnX = this.positionX + this.width / 2;
        int spawnY = this.positionY + this.height;

        if (this.attackPhase == ATTACK_HOMING_MISSILE) {
            // Missile Interval Cooldown Check
            if (this.attackCooldown.checkFinished()) {
                // Homing Missile Fire Logic


                // Placeholder: Firing a generic bullet as a missile for now
                Bullet missile = BulletPool.getBullet(spawnX, spawnY,
                    MISSILE_SPEED, BOSS_BULLET_WIDTH, BOSS_BULLET_HEIGHT, Entity.Team.ENEMY);
                // **Needs dedicated HomingBullet type for actual tracking logic**
                bullets.add(missile);

                // Switch to Laser Charge phase
                this.attackPhase = ATTACK_LASER_CHARGE;
                this.laserChargeCooldown.reset();
                this.laserChargeTimer = LASER_CHARGE_TIME;
                this.changeColor(Color.RED); // Visual feedback for charge
            }
        } else if (this.attackPhase == ATTACK_LASER_CHARGE) {
            // Laser Charge Finished
            if (this.laserChargeCooldown.checkFinished()) {
                
                Core.getDrawManager().getGameScreenRenderer().triggerExplosion(
                    spawnX, spawnY - 20,
                    false, false
                );
                SoundManager.playOnce("laser_big");
                
                Bullet skull = BulletPool.getBullet(spawnX, spawnY - 40, 0, 22*2, 18*2, Entity.Team.ENEMY);
                skull.setSpriteType(SpriteType.GasterBlaster);
                bullets.add(skull);
                
                // Laser Fire Logic
                // **Placeholder: Firing a wide, fast bullet as a Laser**
                Bullet laser = BulletPool.getBullet(spawnX, spawnY,
                    15, 11 * 4, 20 * 40, Entity.Team.ENEMY);
                
                laser.setBigLaser(true);
                laser.setBossBullet(false);
                laser.setSpeedX(0);
                
                bullets.add(laser);

                // Switch back to Homing Missile phase and reset colors
                this.attackPhase = ATTACK_SPREAD_CHARGE;
                this.spreadChargeCooldown.reset();
                this.changeColor(Color.ORANGE);
            }
        } else if (this.attackPhase == ATTACK_SPREAD_CHARGE) {
            if (this.spreadChargeCooldown.checkFinished()) {
                
                int bulletCount = 36;
                int bulletSpeed = 5;
                
                for (int i = 0; i < bulletCount; i++) {
                    double angle = Math.PI * i / (bulletCount - 1);
                    
                    int velX = (int) (bulletSpeed * Math.cos(angle));
                    int velY = (int) (bulletSpeed * Math.sin(angle));
                    
                    Bullet spreadBullet = BulletPool.getBullet(
                        spawnX,
                        spawnY,
                        velY,
                        BOSS_BULLET_WIDTH, BOSS_BULLET_HEIGHT, Entity.Team.ENEMY);
                    
                    spreadBullet.setSpeedX(velX);
                    spreadBullet.setBossBullet(true);
                    
                    bullets.add(spreadBullet);
                }
                
                this.attackPhase = ATTACK_HOMING_MISSILE;
                this.attackCooldown.reset();
                this.changeColor(Color.CYAN);
            }
        }
    }

    /**
     * Updates attributes for boss movement and phases. Custom boss logic goes here.
     */
    @Override
    public final void update() {
        
        // Use the remaining time as a wide-ranging mode.
        if (this.health <= BOSS_ATTACK_HP_THRESHOLD && !this.isAttackEnabled) {
            this.isAttackEnabled = true;
            this.attackCooldown.reset(); // Start attack cycle immediately
        }

        if (this.isAttackEnabled) {
            if (this.attackPhase == ATTACK_LASER_CHARGE) {
                this.laserChargeTimer = this.laserChargeCooldown.getDuration();
            } else if (this.attackPhase == ATTACK_SPREAD_CHARGE) {
                this.spreadChargeTimer = this.spreadChargeCooldown.getDuration();
            }
        }

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

    /** Returns the current attack phase. */
    public final int getAttackPhase() {
        return this.attackPhase;
    }

    /** Returns whether the boss attack logic is currently enabled. */
    public final boolean isAttackEnabled() {
        return this.isAttackEnabled;
    }

    /** Returns whether the boss is currently moving right. */
    public final boolean isMovingRight() {
        return this.movingRight;
    }

    /** Returns whether the boss is currently moving down. */
    public final boolean isMovingDown() {
        return this.movingDown;
    }

    /** Returns the laser charge timer value. */
    public final int getLaserChargeTimer() {
        return this.laserChargeTimer;
    }

    /** Returns the attack HP threshold. */
    public final int getAttackHpThreshold() {
        return this.BOSS_ATTACK_HP_THRESHOLD;
    }

    /** Returns the charge timer read. */
    public int readChargeTimer() {
        return this.spreadChargeTimer; }
}