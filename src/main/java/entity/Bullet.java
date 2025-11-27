package entity;

import engine.AssetManager.SpriteType;
import java.awt.Color;

/**
 * Implements a bullet that moves vertically up or down.
 *
 * @author <a href="mailto:RobertoIA1987@gmail.com">Roberto Izquierdo Amo</a>
 *
 */
public class Bullet extends Entity {
    
    /**
     * Speed of the bullet, positive or negative depending on direction - positive is down.
     */
    private int speed;

    private int speedX = 0;
    private int speedY = 0;

    /**
     * 2P mode: id number to specifying who fired the bullet - 0 = enemy, 1 = P1, 2 = P2
     **/
    private int ownerPlayerId = 0;
    
    // standardised for DrawManager scaling
    private int playerId = 0;
    
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
    // change the constructor to receive width and height
    public Bullet(final int positionX, final int positionY, final int width, final int height,
        final int speed) {
        super(positionX, positionY, 0, 0, Color.WHITE);
        this.speed = speed;
    }
    
    // reset the size when recycling bullets
    public final void setSize(final int width, final int height) {
        this.width = width;
        this.height = height;
    }
    
    /**
     * Sets correct sprite for the bullet, based on speed.
     */
    public final void setSprite() {
        if (this.isBossBullet) {
            this.spriteType = SpriteType.BossBullet;
        }
        
        if (this.isBigLaser) {
            this.spriteType = SpriteType.BigLaserBeam;
        }
        else if (this.speed < 0) {
            this.spriteType = SpriteType.Bullet;
        } else if (!this.isBigLaser && !this.isBossBullet) {
            this.spriteType = SpriteType.EnemyBullet;
        }
    }
    
    public void setBossBullet(boolean isBoss) {
        this.isBossBullet = isBoss;
        setSprite();
        if (isBoss) {
            this.width = 5 * 2;
            this.height = 5 * 2;
        }
    }
    
    public void setBigLaser(boolean isBigLaser) {
        this.isBigLaser = isBigLaser;
        setSprite();
    }
    
    public void setSpriteType(SpriteType spriteType) {
        this.spriteType = spriteType;
    }
    
    /**
     * Updates the bullet's position.
     */
    public final void update() {
        this.positionY += this.speed;
        this.positionX += this.speedX;
    }
    /**
     * Setter of the speed of the bullet.
     *
     * @param speed New speed of the bullet.
     */
    public final void setSpeed(final int speed) {
        this.speed = speed;
    }
    /**
     * Getter for the speed of the bullet.
     *
     * @return Speed of the bullet.
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
    
    // 2P mode: adding owner API, standardised player API
    public final int getOwnerPlayerId() {
        return ownerPlayerId;
    }
    
    public final void setOwnerPlayerId(final int ownerPlayerId) {
        this.ownerPlayerId = ownerPlayerId;
    }
    
    public int getPlayerId() {
        return this.playerId;
    }
    
    public void setPlayerId(int playerId) {
        this.playerId = playerId;
        this.ownerPlayerId = playerId; // keep them in sync
    }
    
    
}