package entity;

import engine.AssetManager;
import engine.GameState;
import entity.character.GameCharacter;
import java.awt.Color;
import java.util.Set;

/**
 * Implements a support pet that can follow the owner and fire projectiles.
 */
public class Pet extends Entity {
    
    public enum PetKind {
        GUN
        // later: LASER, ROCKET, Buff etc.
    }
    
    private int hp = 1;
    private boolean dead = false;
    private final int ownerPlayerId;
    private final PetKind kind;
    private final GameState gameState;
    
    private final int dirX;
    private final int dirY;
    
    /**
     * How long this pet lives (milliseconds).
     */
    private final long lifetimeMs;
    private long spawnedAtMs;
    
    /**
     * Interval between shots (milliseconds).
     */
    private final long shotIntervalMs;
    private long lastShotAtMs = 0L;
    
    public Pet(
        int x,
        int y,
        int w,
        int h,
        Color color,
        int ownerPlayerId,
        PetKind kind,
        GameState gameState,
        long lifetimeMs,
        long shotIntervalMs,
        int dirX,
        int dirY
    ) {
        super(x, y, w, h, color);
        this.ownerPlayerId = ownerPlayerId;
        this.kind = kind;
        this.gameState = gameState;
        
        this.lifetimeMs = lifetimeMs;
        this.shotIntervalMs = shotIntervalMs;
        this.spawnedAtMs = System.currentTimeMillis();
        
        // 기본 방향: 위쪽
        if (dirX == 0 && dirY == 0) {
            this.dirX = 0;
            this.dirY = -1;
        } else {
            this.dirX = dirX;
            this.dirY = dirY;
        }
        
        // 펫 자체 스프라이트 (다람쥐)
        this.spriteType = AssetManager.SpriteType.ItemPetGun;
        
        // 팀 세팅 (P1 / P2)
        this.setTeam(ownerPlayerId == 2 ? Team.PLAYER2 : Team.PLAYER1);
    }
    
    public boolean isExpired() {
        if (dead) {
            return true;
        }
        return System.currentTimeMillis() - spawnedAtMs >= lifetimeMs;
    }
    
    public void takeDamage(int damage) {
        if (dead) {
            return;
        }
        hp -= damage;
        if (hp <= 0) {
            dead = true;
        }
    }
    
    /**
     * Updates the pet: handles firing logic using Weapon/WeaponPool.
     */
    public void update(Set<Weapon> weapons, GameCharacter owner) {
        if (dead || isExpired()) {
            return;
        }
        
        long now = System.currentTimeMillis();
        if (now - lastShotAtMs < shotIntervalMs) {
            return;
        }
        lastShotAtMs = now;
        
        int centerX = this.positionX + this.width / 2;
        int centerY = this.positionY + this.height / 2;
        
        AssetManager.SpriteType bulletSprite = AssetManager.SpriteType.PetGunProjectile;
        int bulletWidth = bulletSprite.getWidth();
        int bulletHeight = bulletSprite.getHeight();
        
        int bulletSpeed = owner.getProjectileSpeed();
        
        int spriteW = bulletSprite.getWidth();
        int spriteH = bulletSprite.getHeight();
        
        float bulletRange = 14.0f;
        
        Weapon weapon = WeaponPool.getWeapon(
            centerX,
            centerY,
            bulletSpeed,
            bulletWidth,
            bulletHeight,
            this.getTeam()
        );
        weapon.setSize(spriteW * 2, spriteH * 2);
        
        weapon.setSpriteImage(bulletSprite);
        
        weapon.setOwnerPlayerId(this.ownerPlayerId);
        weapon.setPlayerId(this.ownerPlayerId);
        
        weapon.setDamage(1);
        weapon.setRange(bulletRange);
        
        weapon.setDirection(this.dirX, this.dirY);
        
        weapons.add(weapon);
    }
    
    public int getOwnerPlayerId() {
        return ownerPlayerId;
    }
    
    public PetKind getKind() {
        return kind;
    }
    
    public boolean isDead() {
        return dead;
    }
}