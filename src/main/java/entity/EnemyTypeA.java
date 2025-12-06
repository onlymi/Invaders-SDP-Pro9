package entity;

import engine.AssetManager.SpriteType;
import engine.Core;
import engine.utils.Cooldown;
import entity.character.GameCharacter;
import java.util.List;
import java.util.Set;

public class EnemyTypeA extends EnemyShip {
    
    // 적의 상태 정의
    private enum State {
        MOVING,   // 기본 이동 상태
        ATTACKING // 공격 모션 취하는 상태
    }
    
    private static final SpriteType SPRITE_MOVE = SpriteType.EnemyA_Move;
    private static final SpriteType SPRITE_ATTACK = SpriteType.EnemyA_Attack;
    
    /**
     * variable for move.
     */
    private static final double MOVE_SPEED = 1.5; // 이동 속도
    private static final double SEPARATION_RADIUS = 60.0; // 서로 밀어내는 거리
    private static final double SEPARATION_FORCE = 0.5; // 밀어내는 힘의 세기
    
    /**
     * variable for attack.
     */
    private static final double ATTACK_RANGE = 80.0; // 공격 사거리
    private static final int DEFAULT_ATTACK_DAMAGE = 10;
    private static final int ATTACK_COOLDOWN = 1500; // 공격 쿨다운
    private static final int ATTACK_ANIMATION_DURATION = 500; // 공격 모션 유지 시간
    
    private Cooldown attackCooldown;
    private double floatingPhase;
    private Cooldown attackAnimationTimer;
    private State state;
    
    protected int attackDamage;
    
    /**
     * Constructor, establishes the ship's properties. Used by EnemyShipFormation and BossShip.
     *
     * @param positionX  Initial position of the ship in the X axis.
     * @param positionY  Initial position of the ship in the Y axis.
     * @param spriteType Sprite type, image corresponding to the ship.
     */
    public EnemyTypeA(int positionX, int positionY, SpriteType spriteType) {
        super(positionX, positionY, SpriteType.EnemyA_Move);
        this.health = 19;
        this.initialHealth = this.health;
        this.pointValue = 30;
        this.coinValue = 10;
        this.attackDamage = DEFAULT_ATTACK_DAMAGE;
        this.attackCooldown = Core.getCooldown(ATTACK_COOLDOWN);
        this.attackAnimationTimer = Core.getCooldown(ATTACK_ANIMATION_DURATION);
        this.state = State.MOVING; // 초기 상태 = 이동
        this.floatingPhase = Math.random() * Math.PI * 2;
        
        this.preciseX = positionX;
        this.preciseY = positionY;
    }
    
    /**
     * Type A AI logic.
     */
    public void update(GameCharacter player, List<EnemyShip> allEnemies) {
        if (this.isDestroyed) {
            return;
        }
        
        if (this.state == State.ATTACKING) {
            if (this.attackAnimationTimer.checkFinished()) {
                this.state = State.MOVING;
                this.spriteType = SPRITE_MOVE;
            } else {
                return;
            }
        }
        // 벡터 로직
        double dx = 0, dy = 0;
        // 플레이어 찾기
        if (player != null && !player.isDie()) {
            double tx = player.getPositionX();
            double ty = player.getPositionY();
            double dirX = tx - this.preciseX;
            double dirY = ty - this.preciseY;
            double dist = Math.sqrt(dirX * dirX + dirY * dirY);
            if (dist > 0) {
                dx += (dirX / dist) * MOVE_SPEED;
                dy += (dirY / dist) * MOVE_SPEED;
            }
            this.isFacingRight = (dirX >= 0);
        }
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
        this.preciseX += dx + this.knockbackX;
        this.preciseY += dy + this.knockbackY;
        
        this.knockbackX *= this.knockbackDecay;
        this.knockbackY *= this.knockbackDecay;
        
        // floating movement
        long currentTime = System.currentTimeMillis();
        double floatingOffset = Math.sin(currentTime * 0.005 + this.floatingPhase) * 5.0;
        
        this.positionX = (int) this.preciseX;
        this.positionY = (int) (this.preciseY + floatingOffset);
    }
    
    public void tryAttack(GameCharacter player, Set<Weapon> weapons) {
        if (this.isDestroyed || player == null || player.isDie()) {
            return;
        }
        double dist = Math.sqrt(Math.pow(player.getPositionX() - this.positionX, 2) + Math.pow(
            player.getPositionY() - this.positionY, 2));
        
        if (dist <= ATTACK_RANGE && this.attackCooldown.checkFinished()) {
            this.attackCooldown.reset();
            performMeleeAttack(weapons, player);
            
            this.state = State.ATTACKING;
            this.spriteType = SPRITE_ATTACK; // 공격 이미지로 교체
            this.attackAnimationTimer.reset(); // 0.5초 카운트 시작
        }
    }
    
    private void performMeleeAttack(Set<Weapon> weapons, GameCharacter player) {
        SpriteType weaponSprite = SpriteType.EnemyA_Weapon;
        int attackImageWidth = weaponSprite.getWidth();
        int attackImageHeight = weaponSprite.getHeight();
        int hitboxWidth = 16;
        int hitboxHeight = 32;
        int centerX = this.positionX + this.width / 2;
        int centerY = this.positionY + this.height / 2;
        int weaponX = centerX - hitboxWidth / 2;
        int weaponY = centerY - hitboxHeight / 2;
        double rotation = 0;
        int offsetX = 20;
        
        if (player != null && !player.isDie()) {
            if (player.getPositionX() < centerX) {
                rotation = 180;
                weaponX -= offsetX;
            } else {
                rotation = 0;
                weaponX += offsetX;
            }
        }
        Weapon enemyWeaponA = new Weapon(weaponX, weaponY, hitboxWidth, hitboxHeight, 0,
            this.attackDamage);
        
        enemyWeaponA.setSpriteImage(weaponSprite);
        enemyWeaponA.setSize(hitboxWidth, hitboxHeight);
        enemyWeaponA.setTeam(Team.ENEMY);
        enemyWeaponA.setDuration(ATTACK_ANIMATION_DURATION);
        enemyWeaponA.setRotation(rotation);
        
        enemyWeaponA.setRotation(rotation);
        weapons.add(enemyWeaponA);
        engine.SoundManager.playOnce("enemy_A_attack_sound");
    }
    
    public void setAttackDamage(int damage) {
        this.attackDamage = damage;
    }
}
