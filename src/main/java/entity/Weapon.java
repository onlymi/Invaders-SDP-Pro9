package entity;

import engine.AssetManager.SpriteType;
import java.awt.Color;
import engine.Core;
import engine.utils.Cooldown;

/**
 * Implements a bullet that moves vertically up or down.
 *
 * @author <a href="mailto:RobertoIA1987@gmail.com">Roberto Izquierdo Amo</a>
 *
 */
public class Weapon extends Entity {
    
    /**
     * Speed of the bullet, positive or negative depending on direction - positive is down.
     */
    private int speed;

    private int speedX = 0;
    private int speedY = 0;
    
    private Ship target;
    private boolean isHoming = false;
    private static final double HOMING_AGILITY = 4.0;
    private Cooldown homingTimer;

    /**
     * 2P mode: id number to specifying who fired the bullet - 0 = enemy, 1 = P1, 2 = P2
     **/
    private int ownerPlayerId = 0;
    
    // standardised for DrawManager scaling
    private int playerId = 0;
    
    private int damage = 0;
    
    private boolean isBossBullet = false;
    private boolean isBigLaser = false;
    
    /**
     * Constructor, establishes the bullet's properties.
     *
     * @param positionX Initial position of the bullet in the X axis.
     * @param positionY Initial position of the bullet in the Y axis.
     * @param speed     Speed of the bullet, positive or negative depending on direction - positive
     *                  is down.
     */
    public Weapon(final int positionX, final int positionY, final int width, final int height,
        final int speed) {
        super(positionX, positionY, width, height, Color.WHITE);
        this.speed = speed;
    }
    
    /**
     * Constructor, establishes the bullet's properties.
     *
     * @param positionX Initial position of the weapon in the X axis.
     * @param positionY Initial position of the weapon in the Y axis.
     * @param speed     Speed of the weapon, positive or negative depending on direction - positive
     *                  is down.
     */
    public Weapon(final int positionX, final int positionY, final int width, final int height,
        final int speed, final int damage) {
        super(positionX, positionY, width, height, Color.WHITE);
        this.speed = speed;
        this.damage = damage;
    }
    
    // reset the size when recycling bullets
    public final void setSize(final int width, final int height) {
        this.width = width;
        this.height = height;
    }
    
    /**
     * Sets correct sprite for the bullet, based on speed.
     */
    public final void setSpriteMap() {
        if (this.isBossBullet) {
            this.spriteType = SpriteType.BossBullet;
        }
        
        if (this.isBigLaser) {
            this.spriteType = SpriteType.BigLaserBeam;
        }
        else if (this.speed < 0) {
            this.spriteType = SpriteType.PlayerBullet; // player bullet fired, team remains NEUTRAL
        } else {
            this.spriteType = SpriteType.EnemyBullet; // enemy fired bullet
        }
    }
    
    public void setBossBullet(boolean isBoss) {
        this.isBossBullet = isBoss;
        setSpriteMap();
        if (isBoss) {
            this.width = 5 * 2;
            this.height = 5 * 2;
        }
    }
    
    public void setBigLaser(boolean isBigLaser) {
        this.isBigLaser = isBigLaser;
        setSpriteMap();
    }
    
    public void setSpriteType(SpriteType spriteType) {
        this.spriteType = spriteType;
    }
    
    /**
     * Sets the sprite type for the weapon.
     *
     * @param spriteType New sprite type to use.
     */
    public final void setSpriteImage(final SpriteType spriteType) {
        this.spriteType = spriteType;
        this.width = spriteType.getWidth();
        this.height = spriteType.getHeight();
    }
    
    /**
     * Updates the weapon's position.
     */
    public final void update() {
        if (this.isHoming) {
            if (this.homingTimer != null && this.homingTimer.checkFinished()) {
                this.positionY = 2000;
                return;
            }
            
            if (this.target != null && !this.target.isDestroyed()) {
                double dx = (target.getPositionX() + target.getWidth() / 2.0) - (this.positionX + this.width / 2.0);
                double dy = (target.getPositionY() + target.getHeight() / 2.0) - (this.positionY + this.height / 2.0);
                double angle = Math.atan2(dy, dx);
                
                this.speedX = (int) (HOMING_AGILITY * Math.cos(angle));
                this.speed = (int) (HOMING_AGILITY * Math.sin(angle));
                this.rotation = Math.toDegrees(angle) - 90;
            }
        }
        this.positionY += this.speed;
        this.positionX += this.speedX;
    }
    
    /**
     * Setter of the speed of the weapon.
     *
     * @param speed New speed of the weapon.
     */
    public final void setSpeed(final int speed) {
        this.speed = speed;
    }
    
    /**
     * Getter for the speed of the weapon.
     *
     * @return Speed of the weapon.
     */
    public final int getSpeed() {
        return this.speed;
    }
    
    public void setSpeedX(int speedX) {
        this.speedX = speedX;
    }
    
    public int getSpeedX() {
        return this.speedX;
    }
    
    public final void setOwnerPlayerId(final int ownerPlayerId) {
        this.ownerPlayerId = ownerPlayerId;
    }
    
    // 2P mode: adding owner API, standardised player API
    public final int getOwnerPlayerId() {
        return ownerPlayerId;
    }
    
    public void setPlayerId(int playerId) {
        this.playerId = playerId;
        this.ownerPlayerId = playerId;
    }
    
    public int getPlayerId() {
        return this.playerId;
    }
    
    public void setDamage(int damage) {
        this.damage = damage;
    }
    
    public int getDamage() {
        return this.damage;
    }
    public void setHoming(Ship target) {
        this.target = target;
        this.isHoming = true;
        this.homingTimer = Core.getCooldown(9000);
        this.homingTimer.reset();
    }
    
    public void resetHoming() {
        this.target = null;
        this.isHoming = false;
        this.rotation = 0;
        this.homingTimer = null;
    }
}