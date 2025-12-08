package entity;

import engine.AssetManager.SpriteType;
import engine.Core;
import engine.utils.Cooldown;
import entity.character.GameCharacter;
import java.util.List;
import java.util.Set;

public class EnemyTypeB extends EnemyShip {
    
    /**
     * Movement variables.
     */
    private static final double MOVE_SPEED = 1.0;
    private static final double SEPARATION_RADIUS = 30.0;
    private static final double SEPARATION_FORCE = 0.5;
    
    private static final double PREFERRED_DISTANCE = 200.0;
    private static final double DISTANCE_BUFFER = 50.0;
    
    private static final double ATTACK_RANGE = 500.0;
    private static final double WEAPON_RANGE = 100.0;
    private static final int DEFAULT_ATTACK_DAMAGE = 15;
    private static final int ATTACK_COOLDOWN = 2000;
    private static final int WEAPON_SPEED = 4;
    
    private Cooldown attackCooldown;
    private double floatingPhase;
    
    protected int attackDamage;
    
    /**
     * Constructor, establishes the ship's properties. Used by EnemyShipFormation and BossShip.
     *
     * @param positionX  Initial position of the ship in the X axis.
     * @param positionY  Initial position of the ship in the Y axis.
     * @param spriteType Sprite type, image corresponding to the ship.
     */
    public EnemyTypeB(int positionX, int positionY, SpriteType spriteType) {
        super(positionX, positionY, spriteType);
        
        this.health = 30;
        this.initialHealth = this.health;
        this.pointValue = 40;
        this.coinValue = 15;
        
        this.attackDamage = DEFAULT_ATTACK_DAMAGE;
        this.attackCooldown = Core.getCooldown(ATTACK_COOLDOWN);
        this.floatingPhase = Math.random() * Math.PI * 2;
        
        this.preciseX = positionX;
        this.preciseY = positionY;
    }
    
    /**
     * Type B AI logic.
     */
    public void update(GameCharacter player, List<EnemyShip> allEnemies) {
        if (this.isDestroyed) {
            return;
        }
        
        double dx = 0, dy = 0;
        
        if (player != null && !player.isDie()) {
            double tx = player.getPositionX();
            double ty = player.getPositionY();
            
            double dirX = tx - this.preciseX;
            double dirY = ty - this.preciseY;
            double dist = Math.sqrt(dirX * dirX + dirY * dirY);
            
            if (dist > 0) {
                dirX /= dist;
                dirY /= dist;
            }
            
            // 접근 기준: PREFERRED_DISTANCE +- DISTANCE_BUFFER
            if (dist > PREFERRED_DISTANCE + DISTANCE_BUFFER) {
                // player와 멀면 접근
                dx += dirX * MOVE_SPEED;
                dy += dirY * MOVE_SPEED;
            } else if (dist < PREFERRED_DISTANCE - DISTANCE_BUFFER) {
                // player와 가까우면 후퇴
                dx -= dirX * MOVE_SPEED;
                dy -= dirY * MOVE_SPEED;
            }
            this.isFacingRight = (player.getPositionX() - this.preciseX >= 0);
        }
        // 적끼리 겹치지 않게 분리 로직
        if (allEnemies != null) {
            for (EnemyShip other : allEnemies) {
                if (other == this || other.isDestroyed()) {
                    continue;
                }
                double diffX = this.preciseX - other.getPositionX();
                double diffY = this.preciseY - other.getPositionY();
                double distSq = diffX * diffX + diffY * diffY;
                
                if (distSq < SEPARATION_RADIUS * SEPARATION_RADIUS && distSq > 0) {
                    double dist = Math.sqrt(distSq);
                    dx += (diffX / dist) * SEPARATION_FORCE * (SEPARATION_RADIUS / dist);
                    dy += (diffY / dist) * SEPARATION_FORCE * (SEPARATION_RADIUS / dist);
                }
            }
        }
        // 넉백 포함 위치 적용
        this.preciseX += dx + this.knockbackX;
        this.preciseY += dy + this.knockbackY;
        
        this.knockbackX *= this.knockbackDecay;
        this.knockbackY *= this.knockbackDecay;
        
        // Floating animation
        long currentTime = System.currentTimeMillis();
        double floatingOffset = Math.sin(currentTime * 0.005 + this.floatingPhase) * 5.0;
        
        this.positionX = (int) this.preciseX;
        this.positionY = (int) (this.preciseY + floatingOffset);
    }
    
    /**
     * enemy B attack logic.
     */
    public void tryAttack(GameCharacter player, Set<Weapon> weapons) {
        if (this.isDestroyed || player == null || player.isDie()) {
            return;
        }
        
        double dist = Math.sqrt(Math.pow(player.getPositionX() - this.positionX, 2) + Math.pow(
            player.getPositionY() - this.positionY, 2));
        
        if (dist <= ATTACK_RANGE && this.attackCooldown.checkFinished()) {
            this.attackCooldown.reset();
            performRangedAttack(weapons, player);
        }
    }
    
    private void performRangedAttack(Set<Weapon> weapons, GameCharacter player) {
        SpriteType weaponSprite = SpriteType.EnemyB_Weapon;
        
        int bulletHitboxW = weaponSprite.getWidth();
        int bulletHitboxH = weaponSprite.getHeight();
        
        // 발사 시작 위치
        int startX = this.positionX + this.width / 2;
        int startY = this.positionY + this.height / 2;
        
        // 플레이어 중심 좌표 계산
        double targetX = player.getPositionX() + player.getWidth() / 2.0;
        double targetY = player.getPositionY() + player.getHeight() / 2.0;
        
        double angle = Math.atan2(targetY - startY, targetX - startX);
        
        int velX = (int) (WEAPON_SPEED * Math.cos(angle));
        int velY = (int) (WEAPON_SPEED * Math.sin(angle));
        
        Weapon bullet = WeaponPool.getWeapon(
            startX,
            startY,
            bulletHitboxW,
            bulletHitboxH,
            velY,
            this.attackDamage,
            Team.ENEMY
        );
        
        bullet.setSpriteImage(weaponSprite);
        bullet.setSpeedX(velX);
        bullet.setRotation(Math.toDegrees(angle));
        
        bullet.setRange((float) WEAPON_RANGE);
        
        weapons.add(bullet);
        
        engine.SoundManager.playOnce("shoot_enemies");
    }
}
