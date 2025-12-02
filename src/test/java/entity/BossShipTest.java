package entity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

import engine.Core;
import engine.SoundManager;
import engine.AssetManager.SpriteType;
import engine.utils.Cooldown;
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
    private static final int TOP_BOUNDARY = 68;
    private static final int BOSS_MAX_Y = 340;
    private static final int SCREEN_WIDTH = 1200;
    
    // [수정] Core와 SoundManager를 가로채기 위한 Mock 객체 선언
    @Mock
    private Cooldown mockCooldown;
    
    private MockedStatic<Core> coreMock;
    private MockedStatic<SoundManager> soundManagerMock;
    
    @BeforeEach
    void setUp() {
        // 1. Static Class Mocking 시작
        coreMock = mockStatic(Core.class);
        soundManagerMock = mockStatic(SoundManager.class);
        
        // 2. Core.getCooldown() 호출 시 가짜 Cooldown 반환 (시간 체크 무시하고 항상 true 반환)
        coreMock.when(() -> Core.getCooldown(anyInt())).thenReturn(mockCooldown);
        when(mockCooldown.checkFinished()).thenReturn(true);
        
        // 3. 화면 크기 가짜 반환
        coreMock.when(Core::getFrameWidth).thenReturn(SCREEN_WIDTH);
        coreMock.when(Core::getFrameHeight).thenReturn(800);
        
        // 4. [중요] SoundManager.playOnce()가 호출되어도 아무 일도 안 일어나게 막음 (오디오 장치 없음 오류 방지)
        soundManagerMock.when(() -> SoundManager.playOnce(anyString())).thenAnswer(invocation -> null);
        
        // 5. BossShip 생성 (이제 내부에서 Core나 SoundManager를 호출해도 안전함)
        boss = new BossShip(100, TOP_BOUNDARY + 10);
    }
    
    @AfterEach
    void tearDown() {
        // Mock 해제 (메모리 누수 방지)
        coreMock.close();
        soundManagerMock.close();
    }
    
    // --- 1. 스탯 및 파괴 테스트 ---
    
    @Test
    void initialStatsAreSetCorrectly() {
        assertEquals(BOSS_INITIAL_HEALTH, boss.getHealth(), "Initial health must be 500");
        assertEquals(BOSS_INITIAL_HEALTH / 2, boss.getAttackHpThreshold(), "Attack threshold must be 50% of initial health.");
        assertEquals(5000, boss.getPointValue(), "Point value must be 5000");
        
        assertTrue(boss.isAttackEnabled(), "Attack must be enabled initially (by default implementation).");
    }
    
    @Test
    void healthDecrementsAndDestroys() {
        // 데미지를 입힐 때 내부적으로 SoundManager.playOnce()가 호출되지만, 위에서 Mock 처리했으므로 오류가 안 남
        boss.getDamage(boss.getHealth() - 1);
        assertFalse(boss.isDestroyed(), "Boss should not be destroyed yet (HP=1)");
        
        boss.hit();
        assertTrue(boss.isDestroyed(), "Boss must be destroyed after the final hit.");
    }
    
    // --- 2. 움직임 및 경계 테스트 ---
    
    @Test
    void initialMovementDirectionIsCorrect() {
        assertTrue(boss.isMovingRight(), "Boss must start moving right.");
        assertTrue(boss.isMovingDown(), "Boss must start moving down.");
    }
    
    @Test
    void movementFlipsAtHorizontalBoundary() {
        // 우측 경계 테스트
        boss.setPositionX(SCREEN_WIDTH - boss.getWidth() + 1);
        
        boss.update();
        assertFalse(boss.isMovingRight(), "Direction must flip to Left at right boundary.");
        
        // 좌측 경계 테스트
        boss.setPositionX(0);
        
        boss.update();
        assertTrue(boss.isMovingRight(), "Direction must flip to Right at left boundary.");
    }
    
    @Test
    void movementFlipsAtVerticalBoundary() {
        // 하단 경계 테스트
        boss.setPositionY(BOSS_MAX_Y - boss.getHeight());
        
        boss.update();
        assertFalse(boss.isMovingDown(), "Direction must flip to Up at BOSS_MAX_Y boundary.");
        
        // 상단 경계 테스트
        boss.setPositionY(TOP_BOUNDARY);
        
        boss.update();
        assertTrue(boss.isMovingDown(), "Direction must flip to Down at TOP_BOUNDARY.");
    }
    
    // --- 3. 공격 활성화 및 패턴 전환 테스트 ---
    
    @Test
    void attackEnablesAtThreshold() {
        assertTrue(boss.isAttackEnabled(), "Attack is enabled initially.");
        
        // HP를 임계값 + 1로 드롭
        boss.getDamage(boss.getHealth() - (boss.getAttackHpThreshold() + 1));
        boss.update();
        assertTrue(boss.isAttackEnabled(), "Attack remains enabled above threshold.");
        
        // HP를 임계값으로 드롭
        boss.hit();
        boss.update();
        assertTrue(boss.isAttackEnabled(), "Attack remains enabled at threshold.");
    }
    
    @Test
    void animationCycleIsCorrect() {
        assertEquals(SpriteType.BossShip1, boss.getSpriteType());
    }
}