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
        GUN,
        ROCKET
    }
    
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
        long shotIntervalMsFromCsv,
        int dirX,
        int dirY
    ) {
        super(x, y, w, h, color);
        this.ownerPlayerId = ownerPlayerId;
        this.kind = kind;
        this.gameState = gameState;
        
        this.lifetimeMs = lifetimeMs;
        this.spawnedAtMs = System.currentTimeMillis();
        
        long defaultInterval;
        if (kind == PetKind.GUN) {
            // GUN 기본: 0.4초당 1발 (원하면 여기 값 바꿔도 됨)
            defaultInterval = 400L;
        } else {
            // ROCKET 기본: 0.9초당 1발
            defaultInterval = 900L;
        }
        
        if (shotIntervalMsFromCsv > 0) {
            this.shotIntervalMs = shotIntervalMsFromCsv;
        } else {
            this.shotIntervalMs = defaultInterval;
        }
        
        // 기본 방향: 위쪽
        if (dirX == 0 && dirY == 0) {
            this.dirX = 0;
            this.dirY = -1;
        } else {
            this.dirX = dirX;
            this.dirY = dirY;
        }
        
        if (kind == PetKind.ROCKET) {
            this.spriteType = AssetManager.SpriteType.ItemPetRocket;
        } else {
            this.spriteType = AssetManager.SpriteType.ItemPetGun;
        }
        
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
        // 대기
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
        
        switch (kind) {
            case GUN -> fireGunProjectile(weapons, owner);
            case ROCKET -> fireRocketProjectile(weapons, owner);
            default -> { /* no-op */ }
        }
    }
    
    // Pet gun logic
    private void fireGunProjectile(Set<Weapon> weapons, GameCharacter owner) {
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
        
        weapon.setDamage(20);
        weapon.setRange(bulletRange);
        
        weapon.setDirection(this.dirX, this.dirY);
        
        weapons.add(weapon);
    }
    
    // Pet Rocket logic
    private void fireRocketProjectile(Set<Weapon> weapons, GameCharacter owner) {
        int centerX = this.positionX + this.width / 2;
        int centerY = this.positionY + this.height / 2;
        
        AssetManager.SpriteType bulletSprite = AssetManager.SpriteType.PetRocketProjectile;
        
        int bulletWidth = bulletSprite.getWidth();
        int bulletHeight = bulletSprite.getHeight();
        
        int baseSpeed = owner.getProjectileSpeed();
        int bulletSpeed = Math.max(1, baseSpeed / 2);
        
        int spriteW = bulletSprite.getWidth();
        int spriteH = bulletSprite.getHeight();
        
        float bulletRange = 18.0f;
        
        Weapon weapon = WeaponPool.getWeapon(
            centerX,
            centerY,
            bulletSpeed,
            bulletWidth,
            bulletHeight,
            this.getTeam()
        );
        
        weapon.setSize(spriteW * 4, spriteH * 4);
        weapon.setSpriteImage(bulletSprite);
        
        weapon.setOwnerPlayerId(this.ownerPlayerId);
        weapon.setPlayerId(this.ownerPlayerId);
        
        weapon.setDamage(30);
        weapon.setRange(bulletRange);
        
        weapon.setDirection(this.dirX, this.dirY);
        
        weapon.setExplosive(true);
        weapon.setExplosionRadius(48.0f);
        
        weapons.add(weapon);
    }
    
    public int getOwnerPlayerId() {
        return ownerPlayerId;
    }
    
    public PetKind getKind() {
        return kind;
    }
    
    public boolean isDead() {
        return false;
    }
}