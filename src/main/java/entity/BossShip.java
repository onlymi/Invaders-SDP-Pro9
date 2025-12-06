package entity;

import engine.AssetManager.SpriteType;
import engine.Core;
import engine.SoundManager;
import engine.utils.Cooldown;
import entity.character.GameCharacter;
import java.awt.Color;
import java.util.ArrayList;
import java.util.HashSet;
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
    
    // Skull & Laser Stats
    private static final int SKULL_WIDTH = 128;
    private static final int SKULL_HEIGHT = 128;
    
    // Boss Movement
    private static final int BOSS_BASE_SPEED_X = 2;
    private static final int TOP_BOUNDARY = 68;
    private static final int BOSS_MAX_Y = 340;
    private static final int VISUAL_WIDTH = 360;
    private static final int VISUAL_HEIGHT = 240;
    private static final int EFFECTIVE_HITBOX_WIDTH = 240;
    private static final int X_OFFSET_COMPENSATION = (VISUAL_WIDTH - EFFECTIVE_HITBOX_WIDTH) / 2;
    
    private int currentSpeedX;
    boolean movingRight;
    
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
        // 480x320 사이즈의 보스 스프라이트 사용
        super(positionX, positionY, SpriteType.BossMainBody);
        
        this.width = VISUAL_WIDTH;
        this.height = VISUAL_HEIGHT;
        
        this.health = BOSS_INITIAL_HEALTH;
        this.initialHealth = BOSS_INITIAL_HEALTH;
        this.pointValue = BOSS_POINTS;
        this.coinValue = BOSS_COINS;
        
        this.changeColor(Color.CYAN);
        
        this.currentSpeedX = BOSS_BASE_SPEED_X;
        this.movingRight = true;
        
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
    public List<java.awt.Rectangle> getHitboxRectangles() {
        List<java.awt.Rectangle> hitboxes = new ArrayList<>();
        
        // 보스 너비 360, 높이 240 기준 (보스의 positionX, positionY는 좌상단)
        
        // 히트박스 1: 메인 스컬 몸통 (뿔 제외, 주로 공격이 집중되는 중앙 하단 영역)
        int body_offset_x = 120;
        int body_offset_y = 50;
        int body_width = 120;
        int body_height = 190;
        
        hitboxes.add(new java.awt.Rectangle(
            this.positionX + body_offset_x,
            this.positionY + body_offset_y,
            body_width,
            body_height
        ));
        
        // 히트박스 2: 뿔 부분 (상단 얇은 영역)
        // 뿔의 끝까지 포함하여 상단 영역을 정의합니다.
        int horn_offset_x = 20;
        int horn_offset_y = 0;
        int horn_width = 320;
        int horn_height = 120;
        
        hitboxes.add(new java.awt.Rectangle(
            this.positionX + horn_offset_x,
            this.positionY + horn_offset_y,
            horn_width,
            horn_height
        ));
        
        return hitboxes;
    }
    /**
     * Updates attributes for boss movement and phases.
     */
    @Override
    public final void update() {
        if (this.isDestroyed) return;
        
        updateMovement();
        updateTimers();
        
        // Update all boss projectiles (movement, animation, cleanup)
        updateProjectiles();
    }
    
    private void updateMovement() {
        if (this.positionX + this.width >= screenWidth || this.positionX <= 0) {
            this.movingRight = !this.movingRight;
            if (this.positionX <= 0) this.positionX = 1;
            if (this.positionX + this.width >= screenWidth) this.positionX = screenWidth - this.width - 1;
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
    
    private void updateProjectiles() {
        List<Weapon> toRecycle = new ArrayList<>();
        for (Weapon w : bossProjectiles) {
            w.update();
            
            // 화면 밖으로 나갔거나(레이저/해골 제외), 충돌/수명만료로 인해 만료된 경우(duration=0 or expired)
            boolean isLaserOrSkull = (w.getSpriteType() == SpriteType.GasterBlaster || w.getSpriteType() == SpriteType.BigLaserBeam);
            
            // 화면 밖 삭제 (일반 탄막만 해당)
            boolean offScreen = w.getPositionY() > screenHeight || w.getPositionY() < 0
                || w.getPositionX() < 0 || w.getPositionX() > screenWidth;
            
            if ((!isLaserOrSkull && offScreen) || w.isExpired()) {
                toRecycle.add(w);
            }
        }
        recycleWeapons(toRecycle);
    }
    
    @Override
    public final void move(final int distanceX, final int distanceY) {
        int movementX = this.movingRight ? this.currentSpeedX : -this.currentSpeedX;
        this.positionX += movementX;
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
    
    // --- Phase 1: Homing Missile ---
    private void handleHomingMissilePhase(int spawnX, int spawnY, GameCharacter target) {
        if (this.attackCooldown.checkFinished()) {
            SoundManager.playOnce("shoot_enemies");
            
            Weapon missile = WeaponPool.getWeapon(spawnX, spawnY,
                MISSILE_SPEED, BOSS_BULLET_WIDTH, BOSS_BULLET_HEIGHT, Entity.Team.ENEMY);
            
            if (target != null) {
                missile.setHoming(target);
            }
            
            // 미사일 설정
            missile.changeColor(Color.RED);
            missile.setDamage(20);
            
            this.bossProjectiles.add(missile);
            
            // Transition
            this.attackPhase = ATTACK_LASER_CHARGE;
            this.laserChargeCooldown.reset();
            this.laserChargeTimer = LASER_CHARGE_TIME;
            this.changeColor(Color.RED);
            this.hasSpawnedSkulls = false;
            this.isFiring = false;
        }
    }
    
    // --- Phase 2: Laser Charge ---
    private void handleLaserChargePhase(int spawnX, int spawnY, GameCharacter target) {
        if (!this.laserChargeCooldown.checkFinished()) return;
        
        int xOffset = 360; // 480 폭에 맞게 조정
        int skullY = spawnY - 100;
        
        // Step 1: Spawn Skulls & Aim
        if (!this.hasSpawnedSkulls) {
            // [수정] spawnY 인자 추가 전달
            spawnSkulls(spawnX, spawnY, xOffset, skullY, target);
            this.hasSpawnedSkulls = true;
            this.laserFireDelayCooldown.reset();
        }
        // Step 2: Fire Lasers
        else if (!this.isFiring) {
            if (this.laserFireDelayCooldown.checkFinished()) {
                // [수정] fireLasers 인자 제거
                fireLasers();
                this.isFiring = true;
                this.laserActiveCooldown.reset();
            }
        }
        // Step 3: Cleanup & Transition
        else {
            if (this.laserActiveCooldown.checkFinished()) {
                cleanupLaserPhase();
                
                // Transition
                this.attackPhase = ATTACK_SPREAD_CHARGE;
                this.spreadChargeCooldown.reset();
                this.changeColor(Color.ORANGE);
                this.hasSpawnedSkulls = false;
                this.isFiring = false;
            }
        }
    }
    
    // [수정] 조준 및 발사 위치 보정 로직 통합
    private void spawnSkulls(int spawnX, int spawnY, int xOffset, int skullY, GameCharacter target) {
        SoundManager.playOnce("laser_big");
        
        double targetX = (target != null) ? target.getPositionX() + target.getWidth() / 2.0 : spawnX;
        // [수정] 조준 기준점: 플레이어 중심 (또는 발밑)
        double targetY = (target != null) ? target.getPositionY() + target.getHeight() / 2.0 : spawnY + 600;
        
        // --- Left Skull ---
        double leftSkullCenterX = (spawnX - xOffset) + (SKULL_WIDTH / 2.0);
        double leftSkullCenterY = skullY + (SKULL_HEIGHT / 2.0);
        this.lockedAngleLeft = Math.atan2(targetY - leftSkullCenterY, targetX - leftSkullCenterX);
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
        Weapon skull = WeaponPool.getWeapon(x, y, 0, SKULL_WIDTH, SKULL_HEIGHT, Entity.Team.ENEMY);
        skull.setSpriteType(SpriteType.GasterBlaster);
        // 각도 설정 (라디안 -> 도)
        skull.setRotation(Math.toDegrees(angle));
        skull.setDamage(0); // 해골 자체는 데미지 없음
        return skull;
    }
    
    // [수정] 인자 제거 및 발사 위치 보정 로직
    private void fireLasers() {
        int laserLength = 2000;
        
        // 왼쪽 레이저 발사
        if (this.activeLeftSkull != null) {
            double originX = this.activeLeftSkull.getPositionX() + (SKULL_WIDTH / 2.0);
            double originY = this.activeLeftSkull.getPositionY() + (SKULL_HEIGHT / 2.0);
            
            createLaserBeam(originX, originY, laserLength, this.lockedAngleLeft, this.activeLeftLasers);
        }
        
        // 오른쪽 레이저 발사
        if (this.activeRightSkull != null) {
            double originX = this.activeRightSkull.getPositionX() + (SKULL_WIDTH / 2.0);
            double originY = this.activeRightSkull.getPositionY() + (SKULL_HEIGHT / 2.0);
            
            createLaserBeam(originX, originY, laserLength, this.lockedAngleRight, this.activeRightLasers);
        }
    }
    
    private void createLaserBeam(double originX, double originY, int length, double angle, List<Weapon> trackList) {
        int laserWidth = 11 * 4;
        
        // spawnX: 레이저의 X축 중앙이 해골 중심(originX)에 오도록 재설정
        int spawnX = (int) (originX - (laserWidth / 2.0));
        
        // spawnY: 레이저 상단 Y좌표가 해골 중심 Y에 오도록 설정
        int spawnY = (int) originY;
        
        // [수정] 조준 오차 보정: 3.0도로 상향 조정하여 조준을 오른쪽으로 미세하게 이동
        double angleOffsetDegrees = -7.0;
        
        for (int i = 0; i < 3; i++) {
            Weapon laser = WeaponPool.getWeapon(spawnX, spawnY, 0, laserWidth, length, Entity.Team.ENEMY);
            
            laser.setBigLaser(true);
            laser.setBossBullet(false);
            laser.setSpeed(0);
            
            // 최종 회전 각도: 계산된 각도 + 90(스프라이트 보정) + 180(방향 반전) + 3.0도 (조준 오차 보정)
            laser.setRotation(Math.toDegrees(angle) + 90 + 180 + angleOffsetDegrees);
            
            // 공격력 설정
            laser.setDamage(50);
            
            this.bossProjectiles.add(laser);
            trackList.add(laser);
        }
    }
    
    private void cleanupLaserPhase() {
        recycleWeapons(this.activeLeftLasers);
        recycleWeapons(this.activeRightLasers);
        
        if (this.activeLeftSkull != null) recycleWeapon(this.activeLeftSkull);
        if (this.activeRightSkull != null) recycleWeapon(this.activeRightSkull);
        
        this.activeLeftSkull = null;
        this.activeRightSkull = null;
    }
    
    // --- Phase 3: Spread Charge ---
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
                
                // 탄막 공격력 설정
                spreadBullet.setDamage(10);
                
                this.bossProjectiles.add(spreadBullet);
            }
            
            // Loop back to Phase 1
            this.attackPhase = ATTACK_HOMING_MISSILE;
            this.attackCooldown.reset();
            this.changeColor(Color.CYAN);
        }
    }
    
    // --- Helper Methods ---
    
    private void recycleWeapons(List<Weapon> weaponsToRecycle) {
        if (weaponsToRecycle == null || weaponsToRecycle.isEmpty()) return;
        
        this.bossProjectiles.removeAll(weaponsToRecycle);
        WeaponPool.recycle(new HashSet<>(weaponsToRecycle));
        weaponsToRecycle.clear();
    }
    
    private void recycleWeapon(Weapon w) {
        if (w == null) return;
        this.bossProjectiles.remove(w);
        WeaponPool.recycle(Set.of(w));
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
    
    @Override
    public final int getHealth() { return this.health; }
    public final int getAttackPhase() { return this.attackPhase; }
    public final boolean isAttackEnabled() { return this.isAttackEnabled; }
    public final boolean isMovingRight() { return this.movingRight; }
    public final int getLaserChargeTimer() { return this.laserChargeTimer; }
    public int readChargeTimer() { return this.spreadChargeTimer; }
}