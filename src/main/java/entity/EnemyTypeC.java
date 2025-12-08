package entity;

import engine.AssetManager.SpriteType;
import engine.Core;
import engine.utils.Cooldown;
import entity.character.GameCharacter;
import java.util.List;

public class EnemyTypeC extends EnemyShip {
    
    private enum State {
        TRACKING,   // 일반 상태: 느리게 추적
        PREPARE,    // 돌진 준비: 짧은 대기
        CHARGING,   // 돌진 중: 방향 고정, 빠른 속도
        COOLDOWN    // 돌진 후 멈춤: 이동/공격 불가
    }
    
    private State state;
    
    private static final double BASE_SPEED = 1.0;
    private static final double CHARGE_SPEED = 10.0;
    private static final double DETECTION_RADIUS = 300.0; // 감지 반경
    
    private static final int PREPARE_TIME = 500;    // 돌진 준비 시간
    private static final int CHARGE_DURATION = 1500; // 1.5초간 돌진
    private static final int COOLDOWN_TIME = 2000;  // 돌진 후 멈춤
    
    // 돌진 방향 고정용 벡터
    private double chargeDirX;
    private double chargeDirY;
    
    private Cooldown stateTimer;
    
    /**
     * Constructor, establishes the ship's properties. Used by EnemyShipFormation and BossShip.
     *
     * @param positionX  Initial position of the ship in the X axis.
     * @param positionY  Initial position of the ship in the Y axis.
     * @param spriteType Sprite type, image corresponding to the ship.
     */
    public EnemyTypeC(int positionX, int positionY, SpriteType spriteType) {
        super(positionX, positionY, spriteType);
        
        this.width = 48;
        this.height = 48;
        
        this.health = 25; // 체력 설정
        this.initialHealth = this.health;
        this.pointValue = 50;
        this.coinValue = 20;
        
        this.state = State.TRACKING;
        this.spriteType = SpriteType.EnemyC_move;
        
        // 정확한 좌표 초기화
        this.preciseX = positionX;
        this.preciseY = positionY;
    }
    
    @Override
    public int getCollisionDamage() {
        // CHARGING 상태에서만 충돌 데미지가 30으로 변경
        if (this.state == State.CHARGING) {
            return 30;
        }
        return 5;
    }
    
    public void update(GameCharacter player, List<EnemyShip> allEnemies) {
        if (this.isDestroyed) {
            return;
        }
        
        // 상태별 동작 처리
        switch (this.state) {
            case TRACKING:
                handleTracking(player);
                break;
            case PREPARE:
                if (stateTimer.checkFinished()) {
                    startCharge(player);
                }
                break;
            case CHARGING:
                handleCharging();
                break;
            case COOLDOWN:
                if (stateTimer.checkFinished()) {
                    this.state = State.TRACKING;
                    this.spriteType = SpriteType.EnemyC_move;
                }
                break;
        }
    }
    
    private void handleTracking(GameCharacter player) {
        if (player == null || player.isDie()) {
            return;
        }
        
        double dx = player.getPositionX() - this.preciseX;
        double dy = player.getPositionY() - this.preciseY;
        double dist = Math.sqrt(dx * dx + dy * dy);
        
        this.isFacingRight = (dx >= 0);
        
        if (dist <= DETECTION_RADIUS) {
            this.state = State.PREPARE;
            this.spriteType = SpriteType.EnemyC_attack; // 공격 스프라이트로 변경
            this.stateTimer = Core.getCooldown(PREPARE_TIME);
            this.stateTimer.reset();
            return;
        }
        if (dist > 0) {
            this.preciseX += (dx / dist) * BASE_SPEED;
            this.preciseY += (dy / dist) * BASE_SPEED;
        }
    }
    
    private void startCharge(GameCharacter player) {
        this.state = State.CHARGING;
        this.stateTimer = Core.getCooldown(CHARGE_DURATION);
        this.stateTimer.reset();
        engine.SoundManager.playOnce("booster");
        
        // 돌진 시작 시점의 플레이어 방향으로 벡터 고정 (도중에 방향 못 바꿈)
        if (player != null) {
            double dx = player.getPositionX() - this.preciseX;
            double dy = player.getPositionY() - this.preciseY;
            double dist = Math.sqrt(dx * dx + dy * dy);
            
            if (dist > 0) {
                this.chargeDirX = (dx / dist);
                this.chargeDirY = (dy / dist);
            } else {
                this.chargeDirX = 0;
                this.chargeDirY = 1; // 바로 아래로
            }
        }
    }
    
    private void handleCharging() {
        // 고정된 방향으로 돌진
        this.preciseX += this.chargeDirX * CHARGE_SPEED;
        this.preciseY += this.chargeDirY * CHARGE_SPEED;
        
        // 돌진 시간 끝나면 쿨다운 진입
        if (stateTimer.checkFinished()) {
            this.state = State.COOLDOWN;
            this.stateTimer = Core.getCooldown(COOLDOWN_TIME); // 2초 휴식
            this.spriteType = SpriteType.EnemyC_move;
            this.stateTimer.reset();
            
        }
    }
    
    private void applyPhysics() {
        // 넉백 적용
        this.preciseX += this.knockbackX;
        this.preciseY += this.knockbackY;
        
        this.knockbackX *= this.knockbackDecay;
        this.knockbackY *= this.knockbackDecay;
        
        // 정수 좌표 변환
        this.positionX = (int) this.preciseX;
        this.positionY = (int) this.preciseY;
    }
}
