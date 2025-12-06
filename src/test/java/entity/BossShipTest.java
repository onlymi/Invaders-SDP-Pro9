package entity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import engine.AssetManager.SpriteType;
import engine.Core;
import engine.SoundManager;
import engine.utils.Cooldown;
import entity.character.GameCharacter;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class BossShipTest {
    
    private BossShip boss;
    
    private static final int BOSS_INITIAL_HEALTH = 500;
    private static final int SCREEN_WIDTH = 1200;
    private static final int INITIAL_POS_Y = 80;
    private static final int VISUAL_WIDTH = 360;
    
    // Mock 객체
    @Mock
    private Cooldown mockCooldown;
    @Mock
    private GameCharacter mockPlayer;
    
    // Static Mock 객체
    private MockedStatic<Core> coreMock;
    private MockedStatic<SoundManager> soundManagerMock;
    
    @BeforeEach
    void setUp() {
        // 1. Static Class Mocking 시작
        coreMock = mockStatic(Core.class);
        soundManagerMock = mockStatic(SoundManager.class);
        
        // 2. Mocking Core Dependencies
        coreMock.when(() -> Core.getCooldown(anyInt())).thenReturn(mockCooldown);
        coreMock.when(Core::getFrameWidth).thenReturn(SCREEN_WIDTH);
        coreMock.when(Core::getFrameHeight).thenReturn(800);
        
        // 3. Mocking SoundManager
        soundManagerMock.when(() -> SoundManager.playOnce(anyString())).thenAnswer(invocation -> null);
        
        // 4. BossShip 생성 (초기 X = 420, Y = 80)
        int initialX = SCREEN_WIDTH / 2 - VISUAL_WIDTH / 2;
        boss = new BossShip(initialX, INITIAL_POS_Y);
        
        // 5. Cooldown Mock 설정: 기본적으로 모든 쿨다운은 완료된 상태로 시작 (테스트 제어용)
        when(mockCooldown.checkFinished()).thenReturn(true);
    }
    
    @AfterEach
    void tearDown() {
        // Static Mock 해제 (필수)
        coreMock.close();
        soundManagerMock.close();
    }
    
    // ---------------------- Getter Tests ----------------------
    
    @Test
    void getHealth() {
        assertEquals(BOSS_INITIAL_HEALTH, boss.getHealth());
    }
    
    @Test
    void getAttackPhase() {
        // 초기 단계는 ATTACK_HOMING_MISSILE (1)
        assertEquals(1, boss.getAttackPhase());
    }
    
    @Test
    void isAttackEnabled() {
        assertTrue(boss.isAttackEnabled());
    }
    
    @Test
    void isMovingRight() {
        // 초기 상태: 오른쪽 이동 중
        assertTrue(boss.isMovingRight());
    }
    
    @Test
    void getLaserChargeTimer() {
        // 초기 상태: 레이저 단계가 아니므로 0
        assertEquals(0, boss.getLaserChargeTimer());
    }
    
    @Test
    void readChargeTimer() {
        // 초기 상태: 스프레드 단계가 아니므로 0
        assertEquals(0, boss.readChargeTimer());
    }
    
    @Test
    void getProjectiles() {
        assertNotNull(boss.getProjectiles());
        assertTrue(boss.getProjectiles().isEmpty());
    }
    
    // ---------------------- Hitbox Test ----------------------
    
    @Test
    void getHitboxRectangles() {
        // 초기 X: 420, Y: 80
        int initialX = SCREEN_WIDTH / 2 - VISUAL_WIDTH / 2;
        
        List<Rectangle> hitboxes = boss.getHitboxRectangles();
        assertEquals(2, hitboxes.size(), "히트박스는 2개여야 합니다.");
        
        // 히트박스 1: 메인 몸통 (120x190, X: +120, Y: +50)
        Rectangle body = hitboxes.get(0);
        assertEquals(120, body.width);
        assertEquals(190, body.height);
        // X = 420 + 120 = 540
        assertEquals(initialX + 120, body.x, "몸통 히트박스 X 위치가 올바르게 보정되어야 합니다.");
        // Y = 80 + 50 = 130
        assertEquals(INITIAL_POS_Y + 50, body.y, "몸통 히트박스 Y 위치가 올바르게 설정되어야 합니다.");
        
        // 히트박스 2: 뿔 부분 (320x50, X: +20, Y: +0)
        Rectangle horn = hitboxes.get(1);
        assertEquals(320, horn.width);
        assertEquals(120, horn.height);
        assertEquals(initialX + 20, horn.x, "뿔 히트박스 X 위치는 보정되지 않아야 합니다.");
        assertEquals(INITIAL_POS_Y, horn.y, "뿔 히트박스 Y 위치는 보정되지 않아야 합니다.");
    }
    
    // ---------------------- Movement Test (X-only) ----------------------
    
    @Test
    void move() {
        int initialX = boss.getPositionX();
        
        // 초기 상태: movingRight = true, currentSpeedX = 2
        boss.move(0, 0);
        assertEquals(initialX + 2, boss.getPositionX(), "오른쪽으로 2픽셀 이동해야 합니다.");
        
        // 방향 변경 시뮬레이션
        boss.move(0, 0);
        boss.movingRight = false;
        
        // 왼쪽 이동 확인
        boss.move(0, 0);
        assertEquals(initialX + 2, boss.getPositionX(), "왼쪽으로 2픽셀 이동해야 합니다.");
    }
    
    @Test
    void update_BoundaryFlip_Horizontal() {
        // [수정된 테스트] 오른쪽 경계에서 방향 전환 및 위치 제약 검증
        
        // BossShip의 X축 이동 속도 (BossShip.BOSS_BASE_SPEED_X = 2)
        final int SPEED_X = 2;
        
        // 경계선: SCREEN_WIDTH - boss.width = 1200 - 360 = 840
        // 경계 제약값: SCREEN_WIDTH - boss.width - 1 = 839
        final int CONSTRAIN_X = SCREEN_WIDTH - boss.width - 1; // 839
        
        // Given: 보스를 오른쪽 경계에 도달하는 위치(840)로 설정합니다.
        boss.positionX = CONSTRAIN_X + 1; // 840
        boss.movingRight = true;
        
        // When: 업데이트 실행
        boss.update();
        
        // Then:
        // 1. 방향 전환 확인
        assertFalse(boss.isMovingRight(), "오른쪽 경계(840)에서 왼쪽으로 방향을 전환해야 합니다.");
        
        // 2. 최종 위치 확인: 위치 제약(839)이 적용된 후, 새 방향(-2)으로 이동해야 합니다.
        // 최종 위치 = 제약값(839) - SPEED_X(2) = 837
        assertEquals(CONSTRAIN_X - SPEED_X, boss.getPositionX(), "X 위치가 경계 제약 후 새 방향으로 이동해야 합니다."); // 837
    }
    
    // ---------------------- Health & Destruction Test ----------------------
    
    @Test
    void hit() {
        // Given: 보스 체력 500
        // When: 100 데미지 피격
        boss.hit(100);
        
        // Then: 체력 감소 및 파괴 아님
        assertEquals(400, boss.getHealth());
        assertFalse(boss.isDestroyed());
        
        // When: 마지막 피격
        boss.hit(400);
        
        // Then: 파괴 상태 전환 및 스프라이트 변경
        assertEquals(0, boss.getHealth());
        assertTrue(boss.isDestroyed());
        assertEquals(SpriteType.Explosion, boss.getSpriteType());
    }
    
    // ---------------------- Attack Pattern Test ----------------------
    
    @Test
    void updateAttackPattern_PhaseTransition() {
        // 초기 Phase: 1 (HOMING_MISSILE)
        assertEquals(1, boss.getAttackPhase());
        
        // Mock Player 설정 (타겟 필요)
        when(mockPlayer.getPositionX()).thenReturn(500);
        when(mockPlayer.getPositionY()).thenReturn(500);
        when(mockPlayer.getCurrentHealthPoints()).thenReturn(100);
        
        GameCharacter[] players = {mockPlayer};
        
        // 1. Phase 1 실행 (쿨다운 완료 상태)
        boss.updateAttackPattern(players);
        
        // 검증: HOMING_MISSILE 발사됨
        assertEquals(1, boss.getProjectiles().size());
        
        // 검증: Phase 2 (LASER_CHARGE)로 전환
        assertEquals(2, boss.getAttackPhase());
        assertNotEquals(0, boss.getLaserChargeTimer(), "레이저 충전 타이머가 시작되어야 합니다.");
        
        // 2. Phase 2 실행 (충전/발사 쿨다운은 완료 상태)
        // 충전 완료 -> 해골 소환 및 레이저 발사 딜레이 시작
        boss.updateAttackPattern(players);
        // 검증: 해골이 소환되어야 함 (총알 목록이 1 + 2 = 3개여야 함)
        assertEquals(3, boss.getProjectiles().size());
        
        // 3. Phase 2 - 레이저 발사 딜레이 완료 후 레이저 발사
        // laserFireDelayCooldown만 완료
        boss.updateAttackPattern(players);
        // 검증: 레이저가 발사되어야 함 (3 + 6 = 9개)
        assertEquals(9, boss.getProjectiles().size());
        
        // 4. Phase 2 - 레이저 활성화 기간 완료 후 Phase 3로 전환
        boss.updateAttackPattern(players);
        
        // 검증: Phase 3 (SPREAD_CHARGE)로 전환
        assertEquals(3, boss.getAttackPhase());
        
        // 5. Phase 3 실행 (쿨다운 완료 상태)
        boss.updateAttackPattern(players);
        
        // 검증: Phase 1 (HOMING_MISSILE)로 루프 전환
        assertEquals(1, boss.getAttackPhase());
    }
}