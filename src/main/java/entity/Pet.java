package entity;

import engine.AssetManager;
import engine.GameState;
import java.awt.Color;
import java.util.Set;

/**
 * Implements a support pet that fires projectiles in the owner's facing direction.
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
        long shotIntervalMs
    ) {
        super(x, y, w, h, color);
        this.ownerPlayerId = ownerPlayerId;
        this.kind = kind;
        this.gameState = gameState;
        
        this.lifetimeMs = lifetimeMs;
        this.shotIntervalMs = shotIntervalMs;
        this.spawnedAtMs = System.currentTimeMillis();
        
        this.spriteType = AssetManager.SpriteType.ItemPetGun;
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
     * Updates the pet: keeps its position and fires bullets periodically.
     */
    public void update(Set<Bullet> bullets, Ship ownerShip) {
        if (ownerShip == null) {
            return;
        }
        
        long now = System.currentTimeMillis();
        if (now - lastShotAtMs < shotIntervalMs) {
            return;
        }
        lastShotAtMs = now;
        
        switch (kind) {
            case GUN -> fireGun(bullets, ownerShip);
            default -> {
            }
        }
    }
    
    private void fireGun(Set<Bullet> bullets, Ship ownerShip) {
        Ship.Facing facing = ownerShip.getFacing();
        
        int centerX = this.positionX + this.width / 2;
        int centerY = this.positionY + this.height / 2;
        
        int vx = 0;
        int vy = 0;
        int speed = 6; // adjust for balance
        
        switch (facing) {
            case UP -> vy = -speed;
            case DOWN -> vy = speed;
            case LEFT -> vx = -speed;
            case RIGHT -> vx = speed;
        }
        
        // NOTE: if current Bullet only supports vertical speed,
        // you might need to extend it later to (vx, vy).
        Bullet b = BulletPool.getBullet(
            centerX,
            centerY,
            vy,          // original code uses "speed" as vertical
            6,
            10,
            ownerShip.getTeam()
        );
        // if Bullet has velocity setters, use them here:
        // b.setVelocity(vx, vy);
        
        b.setOwnerPlayerId(ownerShip.getPlayerId());
        bullets.add(b);
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