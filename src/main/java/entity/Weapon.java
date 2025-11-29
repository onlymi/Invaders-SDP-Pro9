package entity;

import engine.AssetManager.SpriteType;
import java.awt.Color;

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
    
    /**
     * 2P mode: id number to specifying who fired the bullet - 0 = enemy, 1 = P1, 2 = P2.
     **/
    private int ownerPlayerId = 0;
    
    // standardised for DrawManager scaling
    private int playerId = 0;
    
    private int damage = 0;
    
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
    
    // reset the size when recycling weapons
    public final void setSize(final int width, final int height) {
        this.width = width;
        this.height = height;
    }
    
    /**
     * Sets correct sprite for the weapon, based on speed.
     */
    public final void setSpriteMap() {
        if (this.speed < 0) {
            this.spriteType = SpriteType.PlayerBullet; // player bullet fired, team remains NEUTRAL
        } else {
            this.spriteType = SpriteType.EnemyBullet; // enemy fired bullet
        }
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
        this.positionY += this.speed;
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
}