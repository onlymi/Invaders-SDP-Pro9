package entity;

import engine.AssetManager.SpriteType;
import entity.character.GameCharacter;
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
    private static final float DIAGONAL_CORRECTION_FACTOR = (float) (1.0 / Math.sqrt(2));
    private int speed;
    private int velocityX = 0;
    private int velocityY = 0;
    
    /**
     * 2P mode: id number to specifying who fired the bullet - 0 = enemy, 1 = P1, 2 = P2.
     **/
    private int ownerPlayerId = 0;
    
    // standardised for DrawManager scaling
    private int playerId = 0;
    
    private int damage = 0;
    
    private GameCharacter character = null;
    
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
        this.velocityX = 0;
        this.velocityY = speed;
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
        this.velocityX = 0;
        this.velocityY = speed;
    }
    
    public final void setCharacter(GameCharacter character) {
        this.character = character;
        if (this.character != null) {
            this.velocityX = 0;
            this.velocityY = 0;
            
            if (this.character.isFacingLeft()) {
                this.velocityX = -this.speed;
            } else if (this.character.isFacingRight()) {
                this.velocityX = this.speed;
            }
            
            if (this.character.isFacingFront()) { // 아래쪽
                this.velocityY = this.speed;
            } else if (this.character.isFacingBack()) { // 위쪽
                this.velocityY = -this.speed;
            }
            
            if (this.velocityX == 0 && this.velocityY == 0) {
                // 기본값 (위로 발사)
                this.velocityY = -this.speed;
            }
            
            if (this.velocityX != 0 && this.velocityY != 0) {
                this.velocityX = (int) (this.velocityX * DIAGONAL_CORRECTION_FACTOR);
                this.velocityY = (int) (this.velocityY * DIAGONAL_CORRECTION_FACTOR);
            }
        } else {
            // 캐릭터가 null이면(적 총알 등), 기본 수직 속도로 초기화
            this.velocityX = 0;
            this.velocityY = this.speed;
        }
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
        if (this.playerId == 0) {
            this.spriteType = SpriteType.EnemyBullet; // enemy fired bullet
        } else {
            this.spriteType = SpriteType.PlayerBullet; // player bullet fired, team remains NEUTRAL
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
        this.positionX += this.velocityX;
        this.positionY += this.velocityY;
    }
    
    /**
     * Setter of the speed of the weapon.
     *
     * @param speed New speed of the weapon.
     */
    public final void setSpeed(final int speed) {
        this.speed = speed;
        if (this.character == null) {
            this.velocityY = speed;
            this.velocityX = 0;
        }
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
        if (ownerPlayerId == 0) {
            removeCharacter();
        }
    }
    
    // 2P mode: adding owner API, standardised player API
    public final int getOwnerPlayerId() {
        return ownerPlayerId;
    }
    
    public void setPlayerId(int playerId) {
        this.playerId = playerId;
        this.ownerPlayerId = playerId;
        if (playerId == 0) {
            removeCharacter();
        }
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
    
    public void removeCharacter() {
        this.character = null;
        this.velocityY = this.speed;
        this.velocityX = 0;
    }
}