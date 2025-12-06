package entity;

import engine.AssetManager.SpriteType;
import engine.Core;
import engine.SoundManager;
import engine.utils.Cooldown;
import entity.character.GameCharacter;
import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Implements a boss ship, to be destroyed by the player. Extends EnemyShip with boss-specific
 * logic.
 */
public class BossShip extends EnemyShip {
    
    private static final int BOSS_INITIAL_HEALTH = 500;
    private static final int BOSS_POINTS = 5000;
    private static final int BOSS_COINS = 5000;
    
    // Attack Phases
    private static final int ATTACK_HOMING_MISSILE = 1;
    private static final int ATTACK_LASER_CHARGE = 2;
    private static final int ATTACK_SPREAD_CHARGE = 3;
    
    // Phase Intervals & Durations
    private static final int HOMING_MISSILE_INTERVAL = 3000;
    private static final int LASER_CHARGE_TIME = 1000;
    private static final int SPREAD_CHARGE_TIME = 500;
    private static final int LASER_FIRE_DELAY = 1000;
    private static final int LASER_DURATION = 700;
    
    // Projectile Stats
    private static final int MISSILE_SPEED = 4;
    private static final int SPREAD_SPEED = 4;
    private static final int SPREAD_BULLETS = 16;
    private static final int BOSS_BULLET_WIDTH = 20;
    private static final int BOSS_BULLET_HEIGHT = 20;
    private static final int SKULL_WIDTH = 128;
    private static final int SKULL_HEIGHT = 128;
    
    // Boss Movement
    private static final int BOSS_BASE_SPEED_X = 2;
    private static final int BOSS_BASE_SPEED_Y = 1;
    private static final int TOP_BOUNDARY = 68;
    private static final int BOSS_MAX_Y = 340;
    
    private int currentSpeedX;
    private int currentSpeedY;
    boolean movingRight;
    private boolean movingDown;
    
    // Attack State
    private int attackPhase;
    private boolean isAttackEnabled;
    private boolean isFiring;
    private boolean hasSpawnedSkulls;
    
    // Cooldowns
    private Cooldown attackCooldown;
    private Cooldown laserChargeCooldown;
    private Cooldown spreadChargeCooldown;
    private Cooldown laserFireDelayCooldown;
    private Cooldown laserActiveCooldown;
    
    // Timers for UI
    private int laserChargeTimer;
    private int spreadChargeTimer;
    
    // Active Projectiles Management (Boss owned list)
    private final List<Weapon> bossProjectiles;
    
    // References for Logic Control
    private Weapon activeLeftSkull;
    private Weapon activeRightSkull;
    private final List<Weapon> activeLeftLasers = new ArrayList<>();
    private final List<Weapon> activeRightLasers = new ArrayList<>();
    
    private double lockedAngleLeft;
    private double lockedAngleRight;
    
    final int screenWidth = Core.getFrameWidth();
    final int screenHeight = Core.getFrameHeight();
    
    public BossShip(final int positionX, final int positionY) {
        super(positionX, positionY, SpriteType.BossMainBody);
        
        this.width = 240;
        this.height = 160;
        
        this.health = BOSS_INITIAL_HEALTH;
        this.initialHealth = BOSS_INITIAL_HEALTH;
        this.pointValue = BOSS_POINTS;
        this.coinValue = BOSS_COINS;
        
        this.changeColor(Color.CYAN);
        
        this.currentSpeedX = BOSS_BASE_SPEED_X;
        this.currentSpeedY = BOSS_BASE_SPEED_Y;
        this.movingRight = true;
        this.movingDown = true;
        
        this.attackPhase = ATTACK_HOMING_MISSILE;
        
        // Cooldown Initialization
        this.attackCooldown = Core.getCooldown(HOMING_MISSILE_INTERVAL);
        this.attackCooldown.reset();
        this.laserChargeCooldown = Core.getCooldown(LASER_CHARGE_TIME);
        this.spreadChargeCooldown = Core.getCooldown(SPREAD_CHARGE_TIME);
        this.laserFireDelayCooldown = Core.getCooldown(LASER_FIRE_DELAY);
        this.laserActiveCooldown = Core.getCooldown(LASER_DURATION);
        this.bossAnimationCooldown = Core.getCooldown(500);
        this.bossAnimationCooldown.reset();
        
        this.laserChargeTimer = 0;
        this.spreadChargeTimer = 0;
        this.isAttackEnabled = true;
        
        // Initialize Boss-Exclusive Projectile List
        this.bossProjectiles = new ArrayList<>();
    }
    
    /**
     * Updates attributes for boss movement and phases.
     */
    @Override
    public final void update() {
        if (this.isDestroyed) return;
        
        updateMovement();
        updateTimers();
        
        // Update all boss projectiles (movement, animation)
        updateProjectiles();
    }
    
    private void updateMovement() {
        if (this.positionX + this.width >= screenWidth || this.positionX <= 0) {
            this.movingRight = !this.movingRight;
            if (this.positionX <= 0) this.positionX = 1;
            if (this.positionX + this.width >= screenWidth) this.positionX = screenWidth - this.width - 1;
        }
        
        if (this.positionY + this.height >= BOSS_MAX_Y || this.positionY <= TOP_BOUNDARY) {
            this.movingDown = !this.movingDown;
            if (this.positionY <= TOP_BOUNDARY) this.positionY = TOP_BOUNDARY + 1;
            if (this.positionY + this.height >= BOSS_MAX_Y) this.positionY = BOSS_MAX_Y - this.height - 1;
        }
        
        move(0, 0); // Apply internal speed
    }
    
    private void updateTimers() {
        if (this.isAttackEnabled) {
            if (this.attackPhase == ATTACK_LASER_CHARGE) {
                this.laserChargeTimer = this.laserChargeCooldown.getDuration();
            } else if (this.attackPhase == ATTACK_SPREAD_CHARGE) {
                this.spreadChargeTimer = this.spreadChargeCooldown.getDuration();
            }
        }
    }
    
    @Override
    public final void move(final int distanceX, final int distanceY) {
        int movementX = this.movingRight ? this.currentSpeedX : -this.currentSpeedX;
        int movementY = this.movingDown ? this.currentSpeedY : -this.currentSpeedY;
        this.positionX += movementX;
        this.positionY += movementY;
    }
    
    /**
     * Main attack logic loop. Called from GameScreen.
     */
    public final void updateAttackPattern(GameCharacter[] players) {
        if (this.isDestroyed) return;
        
        int spawnX = this.positionX + this.width / 2;
        int spawnY = this.positionY + this.height;
        GameCharacter target = getNearestTarget(players, spawnX, spawnY);
        
        switch (this.attackPhase) {
            case ATTACK_HOMING_MISSILE -> handleHomingMissilePhase(spawnX, spawnY, target);
            case ATTACK_LASER_CHARGE -> handleLaserChargePhase(spawnX, spawnY, target);
            case ATTACK_SPREAD_CHARGE -> handleSpreadChargePhase(spawnX, spawnY);
        }
    }
    
    private void handleHomingMissilePhase(int spawnX, int spawnY, GameCharacter target) {
        if (this.attackCooldown.checkFinished()) {
            SoundManager.playOnce("shoot_enemies");
            
            Weapon missile = WeaponPool.getWeapon(spawnX, spawnY,
                MISSILE_SPEED, BOSS_BULLET_WIDTH, BOSS_BULLET_HEIGHT, Entity.Team.ENEMY);
            if (target != null) {
                missile.setHoming(target);
            }
            
            missile.changeColor(Color.RED);
            // [추가] 미사일 공격력 설정 (1 데미지)
            missile.setDamage(30);
            
            this.bossProjectiles.add(missile);
            
            this.attackPhase = ATTACK_LASER_CHARGE;
            this.laserChargeCooldown.reset();
            this.laserChargeTimer = LASER_CHARGE_TIME;
            this.changeColor(Color.RED);
            this.hasSpawnedSkulls = false;
            this.isFiring = false;
        }
    }
    
    private void handleLaserChargePhase(int spawnX, int spawnY, GameCharacter target) {
        if (!this.laserChargeCooldown.checkFinished()) return;
        
        int xOffset = 180;
        // [수정] 해골의 Y 위치를 명확하게 정의 (보스 본체 위쪽)
        int skullY = spawnY - 100;
        
        // Step 1: Spawn Skulls & Aim
        if (!this.hasSpawnedSkulls) {
            // [수정] 인자 단순화 (계산된 좌표를 내부에서 일관되게 사용)
            spawnSkulls(spawnX, xOffset, skullY, target);
            this.hasSpawnedSkulls = true;
            this.laserFireDelayCooldown.reset();
        }
        // Step 2: Fire Lasers
        else if (!this.isFiring) {
            if (this.laserFireDelayCooldown.checkFinished()) {
                // [수정] 저장된 해골 객체의 위치를 그대로 사용하여 발사
                fireLasers();
                this.isFiring = true;
                this.laserActiveCooldown.reset();
            }
        }
        // Step 3: Cleanup & Transition
        else {
            if (this.laserActiveCooldown.checkFinished()) {
                cleanupLaserPhase();
                
                this.attackPhase = ATTACK_SPREAD_CHARGE;
                this.spreadChargeCooldown.reset();
                this.changeColor(Color.ORANGE);
                this.hasSpawnedSkulls = false;
                this.isFiring = false;
            }
        }
    }
    
    // [수정] spawnY 파라미터 추가
    private void spawnSkulls(int spawnX, int xOffset, int skullY, GameCharacter target) {
        SoundManager.playOnce("laser_big");
        
        double targetX = (target != null) ? target.getPositionX() + target.getWidth() / 2.0 : spawnX;
        double targetY = (target != null) ? target.getPositionY() + target.getHeight() / 2.0 : spawnY + 600;
        
        // --- Left Skull ---
        // 실제 생성될 해골의 중심 좌표 계산
        double leftSkullCenterX = (spawnX - xOffset) + (SKULL_WIDTH / 2.0);
        double leftSkullCenterY = skullY + (SKULL_HEIGHT / 2.0);
        
        // 중심 좌표 기준으로 각도 계산
        this.lockedAngleLeft = Math.atan2(targetY - leftSkullCenterY, targetX - leftSkullCenterX);
        
        // 생성 (좌상단 좌표 기준)
        this.activeLeftSkull = createSkull(spawnX - xOffset, skullY, this.lockedAngleLeft);
        this.bossProjectiles.add(this.activeLeftSkull);
        
        // --- Right Skull ---
        double rightSkullCenterX = (spawnX + xOffset) + (SKULL_WIDTH / 2.0);
        double rightSkullCenterY = skullY + (SKULL_HEIGHT / 2.0);
        
        this.lockedAngleRight = Math.atan2(targetY - rightSkullCenterY, targetX - rightSkullCenterX);
        
        this.activeRightSkull = createSkull(spawnX + xOffset, skullY, this.lockedAngleRight);
        this.bossProjectiles.add(this.activeRightSkull);
    }
    
    private Weapon createSkull(int x, int y, double angle) {
        // SKULL_WIDTH와 SKULL_HEIGHT는 클래스 상단에 상수로 정의되어 있어야 합니다 (128).
        Weapon skull = WeaponPool.getWeapon(x, y, 0, SKULL_WIDTH, SKULL_HEIGHT, Entity.Team.ENEMY);
        
        skull.setSpriteType(SpriteType.GasterBlaster);
        
        // 각도 설정 (라디안 -> 도 변환)
        skull.setRotation(Math.toDegrees(angle));
        
        // 해골은 데미지를 주지 않는 배경/발사대 역할이므로 데미지 0
        skull.setDamage(0);
        
        return skull;
    }
    
    private void fireLasers() {
        SoundManager.playOnce("laser_big");
        int laserLength = 2000;
        
        // 왼쪽 레이저
        if (this.activeLeftSkull != null) {
            double originX = this.activeLeftSkull.getPositionX() + (SKULL_WIDTH / 2.0);
            double originY = this.activeLeftSkull.getPositionY() + (SKULL_HEIGHT / 2.0);
            
            createLaserBeam(originX, originY, laserLength, this.lockedAngleLeft, this.activeLeftLasers);
        }
        
        // 오른쪽 레이저
        if (this.activeRightSkull != null) {
            double originX = this.activeRightSkull.getPositionX() + (SKULL_WIDTH / 2.0);
            double originY = this.activeRightSkull.getPositionY() + (SKULL_HEIGHT / 2.0);
            
            createLaserBeam(originX, originY, laserLength, this.lockedAngleRight, this.activeRightLasers);
        }
    }
    
    private void createLaserBeam(double originX, double originY, int length, double angle, List<Weapon> trackList) {
        int laserWidth = 11 * 4; // 레이저 너비 (스프라이트 크기 * 스케일)
        double halfLength = length / 2.0;
        
        // [중요] 레이저의 "물리적 중심점(Center)" 계산
        // 해골 중심(origin)에서 발사 각도(angle) 방향으로, 레이저 길이의 절반만큼 나아간 지점이
        // 레이저 엔티티의 중심점이 되어야 합니다.
        double centerX = originX + Math.cos(angle) * halfLength;
        double centerY = originY + Math.sin(angle) * halfLength;
        
        // [중요] WeaponPool은 "좌측 상단(Top-Left)" 좌표를 원하므로 변환
        int spawnX = (int) (centerX - laserWidth / 2.0);
        int spawnY = (int) (centerY - halfLength);
        
        for (int i = 0; i < 3; i++) {
            Weapon laser = WeaponPool.getWeapon(spawnX, spawnY, 0, laserWidth, length, Entity.Team.ENEMY);
            
            laser.setBigLaser(true);
            laser.setBossBullet(false);
            laser.setSpeed(0);
            
            // 수직 스프라이트이므로 -90도 회전 보정
            laser.setRotation(Math.toDegrees(angle) - 90);
            
            // [요청하신 공격력 추가]
            laser.setDamage(1);
            
            this.bossProjectiles.add(laser);
            trackList.add(laser);
        }
    }
    
    private void cleanupLaserPhase() {
        // Recycle Lasers
        recycleWeapons(this.activeLeftLasers);
        recycleWeapons(this.activeRightLasers);
        
        // Recycle Skulls
        if (this.activeLeftSkull != null) recycleWeapon(this.activeLeftSkull);
        if (this.activeRightSkull != null) recycleWeapon(this.activeRightSkull);
        
        this.activeLeftSkull = null;
        this.activeRightSkull = null;
    }
    
    private void handleSpreadChargePhase(int spawnX, int spawnY) {
        if (this.spreadChargeCooldown.checkFinished()) {
            SoundManager.playOnce("shoot_enemies");
            
            int bulletCount = 36;
            int bulletSpeed = 5;
            
            for (int i = 0; i < bulletCount; i++) {
                double angle = Math.PI * i / (bulletCount - 1);
                int velX = (int) (bulletSpeed * Math.cos(angle));
                int velY = (int) (bulletSpeed * Math.sin(angle));
                
                Weapon spreadBullet = WeaponPool.getWeapon(spawnX, spawnY, velY,
                    BOSS_BULLET_WIDTH, BOSS_BULLET_HEIGHT, Entity.Team.ENEMY);
                spreadBullet.setSpeedX(velX);
                spreadBullet.setBossBullet(true);
                
                // [추가] 탄막 공격력 설정 (1 데미지)
                spreadBullet.setDamage(20);
                
                this.bossProjectiles.add(spreadBullet);
            }
            
            this.attackPhase = ATTACK_HOMING_MISSILE;
            this.attackCooldown.reset();
            this.changeColor(Color.CYAN);
        }
    }
    
    // --- Helper Methods ---
    
    /**
     * Safely recycles a list of weapons and removes them from the boss's active list.
     */
    private void recycleWeapons(List<Weapon> weaponsToRecycle) {
        if (weaponsToRecycle == null || weaponsToRecycle.isEmpty()) return;
        
        this.bossProjectiles.removeAll(weaponsToRecycle); // Remove from update/draw list
        WeaponPool.recycle(new java.util.HashSet<>(weaponsToRecycle)); // Return to pool
        weaponsToRecycle.clear();
    }
    
    private void recycleWeapon(Weapon w) {
        if (w == null) return;
        this.bossProjectiles.remove(w);
        WeaponPool.recycle(java.util.Set.of(w));
    }
    
    private GameCharacter getNearestTarget(GameCharacter[] players, int x, int y) {
        GameCharacter nearest = null;
        double minDist = Double.MAX_VALUE;
        if (players == null) return null;
        for (GameCharacter p : players) {
            if (p != null && !p.isInvincible() && p.getCurrentHealthPoints() > 0) {
                double dist = Math.pow(p.getPositionX() - x, 2) + Math.pow(p.getPositionY() - y, 2);
                if (dist < minDist) {
                    minDist = dist;
                    nearest = p;
                }
            }
        }
        return nearest;
    }
    
    /**
     * Returns the list of projectiles currently active and owned by the boss.
     * GameScreen should use this for collision detection and drawing.
     */
    public List<Weapon> getProjectiles() {
        return this.bossProjectiles;
    }
    
    @Override
    public final void hit() {
        this.health--;
        if (this.health <= 0) {
            this.isDestroyed = true;
            this.spriteType = SpriteType.Explosion;
            Color color = this.getColor();
            color = new Color(color.getRed(), color.getGreen(), color.getBlue(), 255);
            changeColor(color);
            // Cleanup all projectiles on death
            recycleWeapons(new ArrayList<>(this.bossProjectiles));
        } else {
            SoundManager.playOnce("boss_hit");
        }
    }
    
    private void updateProjectiles() {
        List<Weapon> toRecycle = new ArrayList<>();
        for (Weapon w : bossProjectiles) {
            w.update();
            // 화면 밖으로 나갔거나(레이저 제외), 충돌로 인해 만료된 경우(duration=0)
            boolean isLaserOrSkull = (w.getSpriteType() == SpriteType.GasterBlaster || w.getSpriteType() == SpriteType.BigLaserBeam);
            
            // 화면 밖 삭제 (일반 탄막만)
            boolean offScreen = w.getPositionY() > screenHeight || w.getPositionY() < 0 || w.getPositionX() < 0 || w.getPositionX() > screenWidth;
            
            if ((!isLaserOrSkull && offScreen) || w.isExpired()) {
                toRecycle.add(w);
            }
        }
        recycleWeapons(toRecycle);
    }
    
    @Override
    public final int getHealth() { return this.health; }
    public final int getAttackPhase() { return this.attackPhase; }
    public final boolean isAttackEnabled() { return this.isAttackEnabled; }
    public final boolean isMovingRight() { return this.movingRight; }
    public final boolean isMovingDown() { return this.movingDown; }
    public final int getLaserChargeTimer() { return this.laserChargeTimer; }
    public int readChargeTimer() { return this.spreadChargeTimer; }
}