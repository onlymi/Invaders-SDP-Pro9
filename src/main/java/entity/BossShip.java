package entity;

import engine.AssetManager.SpriteType;
import engine.Core;
import engine.SoundManager;
import engine.utils.Cooldown;
import entity.character.GameCharacter;

import java.util.ArrayList;
import java.util.List;
import java.awt.Color;
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
    private static final int HOMING_MISSILE_INTERVAL = 3000;
    private static final int LASER_CHARGE_TIME = 1000;
    private static final int MISSILE_SPEED = 4;
    private static final int ATTACK_SPREAD_CHARGE = 3;
    private static final int SPREAD_CHARGE_TIME = 500;
    private static final int SPREAD_BULLETS = 16;
    private static final int SPREAD_SPEED = 4;
    private static final int BOSS_BULLET_WIDTH = 20;
    private static final int BOSS_BULLET_HEIGHT = 20;
    private static final int SKULL_WIDTH = 128;
    private static final int SKULL_HEIGHT = 128;
    private static final int LASER_FIRE_DELAY = 1000;
    private static final int LASER_DURATION = 700;
    
    
    
    
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
    private Cooldown laserFireDelayCooldown;
    private boolean hasSpawnedSkulls;
    private Weapon activeLeftSkull;
    private Weapon activeRightSkull;
    private double lockedAngleLeft;
    private double lockedAngleRight;
    private Cooldown laserActiveCooldown;
    private boolean isFiring;
    private List<Weapon> activeLeftLasers = new ArrayList<>();
    private List<Weapon> activeRightLasers = new ArrayList<>();
    
    private boolean isAttackEnabled = true;
    
    final int screenWidth = Core.getFrameWidth();
    final int screenHeight = Core.getFrameHeight();
    
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

        this.attackPhase = ATTACK_HOMING_MISSILE;
        this.attackCooldown = Core.getCooldown(HOMING_MISSILE_INTERVAL);
        this.attackCooldown.reset();
        this.laserChargeCooldown = Core.getCooldown(LASER_CHARGE_TIME);
        this.spreadChargeCooldown = Core.getCooldown(SPREAD_CHARGE_TIME);
        this.laserFireDelayCooldown = Core.getCooldown(1000);
        this.laserActiveCooldown = Core.getCooldown(700);
        if (this.bossAnimationCooldown == null) {
            this.bossAnimationCooldown = Core.getCooldown(500);
        }
        this.isAttackEnabled = true;
        
    }

    /**
     * New shoot method to manage attacks
     */
    public final void shoot(final Set<Weapon> weapons, GameCharacter[] players) {
        
        // Calculate the center position of the boss for spawning projectiles
        int spawnX = this.positionX + this.width / 2;
        int spawnY = this.positionY + this.height;
        
        // Find the nearest player to target
        GameCharacter target = getNearestTarget(players, spawnX, spawnY);
        
        
        // === Phase 1: Homing Missile ===
        if (this.attackPhase == ATTACK_HOMING_MISSILE) {
            // Missile Interval Cooldown Check
            if (this.attackCooldown.checkFinished()) {
                
                SoundManager.playOnce("shoot_enemies");
                
                // Create a missile projectile
                Weapon missile = WeaponPool.getWeapon(spawnX, spawnY,
                    MISSILE_SPEED, BOSS_BULLET_WIDTH, BOSS_BULLET_HEIGHT, Entity.Team.ENEMY);
                weapons.add(missile);
                
                // If a target exists, enable homing behavior on the missile
                if (target != null) {
                    missile.setHoming(target);
                }
                
                // Transition to the Laser Charge phase
                this.attackPhase = ATTACK_LASER_CHARGE;
                this.laserChargeCooldown.reset();
                this.laserChargeTimer = LASER_CHARGE_TIME;
                this.changeColor(Color.RED); // Visual indicator for charging
                
                // Reset laser pattern flags
                this.hasSpawnedSkulls = false;
                this.isFiring = false;
            }
        }
        // === Phase 2: Gaster Blaster Laser (Skull Laser) ===
        else if (this.attackPhase == ATTACK_LASER_CHARGE) {
            // Wait for the initial charge cooldown (red color phase) to finish
            if (this.laserChargeCooldown.checkFinished()) {
                
                // Constants for positioning
                int xOffset = 100; // Horizontal distance from boss center
                int skullY = spawnY - 40; // Y position for skulls
                double laserY = spawnY + (SKULL_HEIGHT / 2.0); // Laser origin Y (center of skull)
                
                
                // [Step 1] Spawn Skulls & Aim (Warning Phase)
                if (!this.hasSpawnedSkulls) {
                    SoundManager.playOnce("laser_big");
                    
                    // Determine target coordinates (default to bottom center if no player alive)
                    double targetX = (target != null) ? target.getPositionX() + target.getWidth() / 2.0 : spawnX;
                    double targetY = (target != null) ? target.getPositionY() + target.getHeight() / 2.0 : spawnY + 600;
                    
                    // Calculate angle for the Left Skull to aim at the target
                    double startXLeft = (spawnX - xOffset);
                    this.lockedAngleLeft = Math.atan2(targetY - laserY, targetX - startXLeft);
                    
                    // Calculate angle for the Right Skull to aim at the target
                    double startXRight = (spawnX + xOffset);
                    this.lockedAngleRight = Math.atan2(targetY - laserY, targetX - startXRight);
                    
                    
                    // Spawn Left Skull and rotate it towards the target
                    this.activeLeftSkull = WeaponPool.getWeapon(spawnX - xOffset, skullY, 0, SKULL_WIDTH, SKULL_HEIGHT, Entity.Team.ENEMY);
                    this.activeLeftSkull.setSpriteType(SpriteType.GasterBlaster);
                    this.activeLeftSkull.setRotation(Math.toDegrees(this.lockedAngleLeft));
                    weapons.add(this.activeLeftSkull);
                    
                    // Spawn Right Skull and rotate it towards the target
                    this.activeRightSkull = WeaponPool.getWeapon(spawnX + xOffset, skullY, 0, SKULL_WIDTH, SKULL_HEIGHT, Entity.Team.ENEMY);
                    this.activeRightSkull.setSpriteType(SpriteType.GasterBlaster);
                    this.activeRightSkull.setRotation(Math.toDegrees(this.lockedAngleRight));
                    weapons.add(this.activeRightSkull);
                    
                    // Update flags and start the delay timer before firing
                    this.hasSpawnedSkulls = true;
                    this.laserFireDelayCooldown.reset();
                }
                // [Step 2] Fire Lasers (Active Phase)
                else if (!this.isFiring) {
                    // Check if the warning delay has passed
                    if (this.laserFireDelayCooldown.checkFinished()) {
                        SoundManager.playOnce("laser_big");
                        
                        double laserSpeed = 0; // Static beam (speed 0)
                        int laserLength = 2000; // Very long length to cover the screen
                        
                        // Create Left Lasers (Stack 3 beams for persistence against single hits)
                        double startXLeft = spawnX - xOffset;
                        for (int i = 0; i < 3; i++) {
                            Weapon leftLaser = WeaponPool.getWeapon((int)startXLeft, (int)laserY, 0, 11 * 4, laserLength, Entity.Team.ENEMY);
                            leftLaser.setBigLaser(true);
                            leftLaser.setBossBullet(false);
                            leftLaser.setSpeedX(0); // No horizontal movement
                            leftLaser.setSpeed(0);  // No vertical movement
                            // Rotate laser to match the aimed angle (-90 correction for vertical sprite)
                            leftLaser.setRotation(Math.toDegrees(this.lockedAngleLeft) - 90);
                            weapons.add(leftLaser);
                            this.activeLeftLasers.add(leftLaser);
                        }
                        
                        // Create Right Lasers (Stack 3 beams)
                        double startXRight = spawnX + xOffset;
                        for (int i = 0; i < 3; i++) {
                            Weapon rightLaser = WeaponPool.getWeapon((int)startXRight, (int)laserY, 0, 11 * 4, laserLength, Entity.Team.ENEMY);
                            rightLaser.setBigLaser(true);
                            rightLaser.setBossBullet(false);
                            rightLaser.setSpeedX(0);
                            rightLaser.setSpeed(0);
                            rightLaser.setRotation(Math.toDegrees(this.lockedAngleRight) - 90);
                            weapons.add(rightLaser);
                            this.activeRightLasers.add(rightLaser);
                        }
                        
                        // Set firing flag and start the duration timer
                        this.isFiring = true;
                        this.laserActiveCooldown.reset();
                    }
                }
                // [Step 3] Cleanup Phase
                else {
                    // Check if the laser duration has expired
                    if (this.laserActiveCooldown.checkFinished()) {
                        
                        // Remove all active Left Lasers
                        for (Weapon w : this.activeLeftLasers) {
                            if (weapons.contains(w)) {
                                weapons.remove(w);
                                WeaponPool.recycle(java.util.Collections.singleton(w));
                            }
                        }
                        this.activeLeftLasers.clear();
                        
                        // Remove all active Right Lasers
                        for (Weapon w : this.activeRightLasers) {
                            if (weapons.contains(w)) {
                                weapons.remove(w);
                                WeaponPool.recycle(java.util.Collections.singleton(w));
                            }
                        }
                        this.activeRightLasers.clear();
                        
                        // Remove Left Skull
                        if (this.activeLeftSkull != null) {
                            weapons.remove(this.activeLeftSkull);
                            WeaponPool.recycle(java.util.Collections.singleton(this.activeLeftSkull));
                            this.activeLeftSkull = null;
                        }
                        // Remove Right Skull
                        if (this.activeRightSkull != null) {
                            weapons.remove(this.activeRightSkull);
                            WeaponPool.recycle(java.util.Collections.singleton(this.activeRightSkull));
                            this.activeRightSkull = null;
                        }
                        
                        // Transition to the next phase (Spread Charge)
                        this.attackPhase = ATTACK_SPREAD_CHARGE;
                        this.spreadChargeCooldown.reset();
                        this.changeColor(Color.ORANGE); // Visual indicator
                        this.hasSpawnedSkulls = false;
                        this.isFiring = false;
                    }
                }
            }
        }
        // === Phase 3: Spread Charge (360-degree Attack) ===
        else if (this.attackPhase == ATTACK_SPREAD_CHARGE) {
            if (this.spreadChargeCooldown.checkFinished()) {
                
                SoundManager.playOnce("shoot_enemies");
                
                int bulletCount = 36;
                int bulletSpeed = 5;
                
                // Fire bullets in a full circle
                for (int i = 0; i < bulletCount; i++) {
                    double angle = Math.PI * i / (bulletCount - 1);
                    
                    int velX = (int) (bulletSpeed * Math.cos(angle));
                    int velY = (int) (bulletSpeed * Math.sin(angle));
                    
                    Weapon spreadBullet = WeaponPool.getWeapon(
                        spawnX,
                        spawnY,
                        velY,
                        BOSS_BULLET_WIDTH, BOSS_BULLET_HEIGHT, Entity.Team.ENEMY);
                    
                    spreadBullet.setSpeedX(velX);
                    spreadBullet.setBossBullet(true);
                    
                    weapons.add(spreadBullet);
                }
                
                // Loop back to Phase 1 (Homing Missile)
                this.attackPhase = ATTACK_HOMING_MISSILE;
                this.attackCooldown.reset();
                this.changeColor(Color.CYAN); // Reset to neutral color
            }
        }
    }

    /**
     * Updates attributes for boss movement and phases. Custom boss logic goes here.
     */
    @Override
    public final void update() {
        

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
            if (this.positionX <= 0) {
                this.positionX = 1;
            }
            if (this.positionX + this.width >= screenWidth) {
                this.positionX = screenWidth - this.width - 1;
            }
        }

        // Check Vertical Boundary
        if (this.positionY + this.height >= BOSS_MAX_Y || this.positionY <= TOP_BOUNDARY) {
            this.movingDown = !this.movingDown;
            if (this.positionY <= TOP_BOUNDARY) {
                this.positionY = TOP_BOUNDARY + 1;
            }
            if (this.positionY + this.height >= BOSS_MAX_Y) {
                this.positionY = BOSS_MAX_Y - this.height - 1;
            }
        }
        
        // Attack Pattern Logic
        if (this.attackPhase == ATTACK_HOMING_MISSILE) {
            // Missile Interval Cooldown
            if (this.attackCooldown.checkFinished()) {
                // **Placeholder for Homing Missile Logic**
                
                // Switch to Laser Charge phase
                this.attackPhase = ATTACK_LASER_CHARGE;
                this.laserChargeCooldown.reset();
                this.laserChargeTimer = LASER_CHARGE_TIME; // Start charge timer
                
                // Visual feedback for charge (changes color to RED)
                this.changeColor(Color.RED);
            }
        } else if (this.attackPhase == ATTACK_LASER_CHARGE) {
            // Update the remaining charge time for rendering the charge bar
            this.laserChargeTimer = this.laserChargeCooldown.getDuration();
            
            // Laser Charge Finished
            if (this.laserChargeCooldown.checkFinished()) {
                // **Placeholder for Laser Fire Logic**
                
                // Switch back to Homing Missile phase and reset colors
                this.attackPhase = ATTACK_HOMING_MISSILE;
                this.attackCooldown.reset();
                this.changeColor(Color.CYAN);
            }
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
        } else{
            SoundManager.playOnce("boss_hit");
        }
    }
    
    private GameCharacter getNearestTarget(GameCharacter[] players, int x, int y) {
        GameCharacter nearest = null;
        double minDist = Double.MAX_VALUE;
        if (players == null) return null;
        for (GameCharacter p : players) {
            if (p != null && !p.isDestroyed()) {
                double dist = Math.pow(p.getPositionX() - x, 2) + Math.pow(p.getPositionY() - y, 2);
                if (dist < minDist) {
                    minDist = dist;
                    nearest = p;
                }
            }
        }
        return nearest;
    }
    
    /**
     * Returns the current attack phase.
     */
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

    /** Returns the charge timer read. */
    public int readChargeTimer() {
        return this.spreadChargeTimer; }
}