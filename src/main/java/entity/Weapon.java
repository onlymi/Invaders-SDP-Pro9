package entity;

import engine.AssetManager.SpriteType;
import engine.Core;
import engine.utils.Cooldown;
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
    private int speedX = 0;
    
    private int velocityX = 0;
    private int velocityY = 0;
    
    private static final int ATTACK_RANGE_FACTOR = 30;
    private static final int OFF_SCREEN_Y = 2000;
    private int initialX;
    private int initialY;
    private float maxRange = -1; // -1이면 사거리 제한 없음 (기본값)
    
    private GameCharacter target;
    private boolean isHoming = false;
    private static final double HOMING_AGILITY = 4.0;
    private Cooldown homingTimer;
    
    /**
     * 2P mode: id number to specifying who fired the bullet - 0 = enemy, 1 = P1, 2 = P2.
     **/
    private int ownerPlayerId = 0;
    
    // standardised for DrawManager scaling
    private int playerId = 0;
    
    private int damage = 0;
    
    private GameCharacter character = null;
    
    private boolean isBossBullet = false;
    private boolean isBigLaser = false;
    private boolean isBossSkull = false;
    
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
        setSpriteMap();
    }
    
    public final void setCharacter(GameCharacter character) {
        this.character = character;
        
        this.velocityX = 0;
        this.velocityY = 0;
        this.speedX = 0;
        
        if (this.character != null) {
            
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
        
        if (this.velocityX != 0 || this.velocityY != 0) {
            this.rotation = Math.toDegrees(Math.atan2(this.velocityY, this.velocityX)) + 90;
            // this.rotation = Math.atan2(this.velocityY, this.velocityX) + Math.PI / 2;
        } else {
            this.rotation = 0;
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
        this.spriteType = SpriteType.PlayerBullet; // player bullet fired, team remains NEUTRAL
        
        if (this.isBossBullet) {
            this.spriteType = SpriteType.BossBullet;
        }
        
        if (this.isBigLaser) {
            this.spriteType = SpriteType.BigLaserBeam;
        } else if (this.speed == 0) {
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
    
    public void setBossSkull(boolean isBossSkull) {
        this.isBossSkull = isBossSkull;
    }
    
    public boolean isBossSkull() {
        return this.isBossSkull;
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
     * 무기의 최대 사거리를 설정합니다.
     *
     * @param range 사거리 (픽셀 단위, 혹은 게임 내 거리 단위)
     */
    public void setRange(float range) {
        this.maxRange = range;
        // 사거리 체크를 위해 현재 위치를 발사 원점으로 기록
        this.initialX = this.positionX;
        this.initialY = this.positionY;
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
            
            if (this.target != null && !this.target.isInvincible()) {
                double dx = (target.getPositionX() + target.getWidth() / 2.0)
                    - (this.positionX + this.width / 2.0);
                double dy = (target.getPositionY() + target.getHeight() / 2.0)
                    - (this.positionY + this.height / 2.0);
                double angle = Math.atan2(dy, dx);
                
                this.velocityX = (int) (HOMING_AGILITY * Math.cos(angle));
                this.velocityY = (int) (HOMING_AGILITY * Math.sin(angle));
                this.rotation = Math.toDegrees(angle) - 90;
            } else {
                if (this.velocityX == 0 && this.velocityY == 0) {
                    this.velocityY = this.speed;
                }
            }
        }
        this.positionY += this.velocityY;
        this.positionX += this.velocityX;
        
        if (this.maxRange > 0) {
            double distanceTraveled = Math.sqrt(
                Math.pow(this.positionX - this.initialX, 2)
                    + Math.pow(this.positionY - this.initialY, 2)
            );
            if (distanceTraveled >= this.maxRange * ATTACK_RANGE_FACTOR) {
                this.positionY = OFF_SCREEN_Y;
            }
        }
    }
    
    /**
     * Setter of the speed of the weapon.
     *
     * @param speed New speed of the weapon.
     */
    public final void setSpeed(final int speed) {
        this.speed = speed;
        this.velocityY = speed;
        this.velocityX = 0;
    }
    
    public void setSpeedX(int speedX) {
        this.speedX = speedX;
        this.velocityX = speedX;
    }
    
    /**
     * Getter for the speed of the weapon.
     *
     * @return Speed of the weapon.
     */
    public final int getSpeed() {
        return this.speed;
    }
    
    
    
    public int getSpeedX() {
        return this.speedX;
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
    
    public void setHoming(GameCharacter target) {
        this.target = target;
        this.isHoming = true;
        this.homingTimer = Core.getCooldown(5000);
        this.homingTimer.reset();
    }
    
    public void resetHoming() {
        this.target = null;
        this.isHoming = false;
        this.rotation = 0;
        this.homingTimer = null;
    }
}