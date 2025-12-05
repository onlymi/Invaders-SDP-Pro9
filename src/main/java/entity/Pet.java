package entity;

import engine.AssetManager;
import engine.GameState;
import entity.character.GameCharacter;
import java.awt.Color;
import java.util.Set;

/**
 * Implements a support pet that can follow the owner and (later) fire projectiles.
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
     * Interval between shots (milliseconds). (Currently unused until firing logic is re-added with
     * Weapon.)
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
     * Updates the pet: keeps its position near the owner. (Weapon firing logic will be added later
     * using Weapon/WeaponPool.)
     */
    public void update(Set<Weapon> weapons, GameCharacter owner) {
        if (owner == null || dead || isExpired()) {
            return;
        }
        
        // Follow owner: slightly to the right and above (adjust as needed)
        int offsetX = 10;
        int offsetY = -10;
        
        this.positionX = owner.getPositionX() + owner.getWidth() + offsetX;
        this.positionY = owner.getPositionY() + offsetY;
        
        // TODO: In future, implement firing logic using Weapon & owner facing.
        // long now = System.currentTimeMillis();
        // if (now - lastShotAtMs >= shotIntervalMs) {
        //     lastShotAtMs = now;
        //     switch (kind) {
        //         case GUN -> fireGun(weapons, owner);
        //         default -> { }
        //     }
        // }
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