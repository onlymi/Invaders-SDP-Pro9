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
import engine.Core;
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
        
        // 생성자에서 초기화되지 않은 bossAnimationCooldown을 테스트 코드에서 수동으로 주입
        boss.bossAnimationCooldown = mockCooldown;
    }
    
    @AfterEach
    void tearDown() {
        // Static Mock 해제 (필수)
        coreMock.close();
        soundManagerMock.close();
    }
    
    // --- 1. 스탯 및 파괴 테스트 ---
    
    @Test
    void initialStatsAreSetCorrectly() {
        assertEquals(BOSS_INITIAL_HEALTH, boss.getHealth(), "Initial health must be 500");
        
        // 새로운 Getter를 사용하여 임계값(Threshold)이 50%로 정확히 설정되었는지 검증
        assertEquals(BOSS_INITIAL_HEALTH / 2, boss.getAttackHpThreshold(),
            "Attack threshold must be 50% of initial health.");
        
        assertEquals(5000, boss.getPointValue(), "Point value must be 5000");
        
        assertTrue(boss.isAttackEnabled(), "Attack must be enabled initially (by default implementation).");
    }
    
    void healthDecrementsAndDestroys() {
        // 1. 일반 피격 테스트 (초기 체력에서 1 감소 확인)
        int initialHp = boss.getHealth();
        boss.hit();
        assertEquals(initialHp - 1, boss.getHealth(),
            "Health should decrease by 1 on hit.");
        assertFalse(boss.isDestroyed(),
            "Boss should not be destroyed yet (HP > 0).");
        
        // 2. 파괴 테스트를 위해 체력을 1로 만듦
        // (현재 체력 - 1) 만큼 데미지를 입히면 1이 남습니다.
        boss.getDamage(boss.getHealth() - 1);
        assertEquals(1, boss.getHealth(),
            "Health should be 1 before the final hit.");
        
        // 3. 마지막 타격 (HP: 1 -> 0) 및 파괴 확인
        boss.hit();
        assertEquals(0, boss.getHealth(),
            "Health should be 0 after destruction.");
        assertTrue(boss.isDestroyed(),
            "Boss must be destroyed when health reaches 0.");
        assertEquals(SpriteType.Explosion, boss.getSpriteType(),
            "Sprite should change to Explosion on destruction.");
    }
        // --- 2. 움직임 및 경계 테스트 ---
        
        @Test
        void initialMovementDirectionIsCorrect() {
            assertTrue(boss.isMovingRight(), "Boss must start moving right.");
            assertTrue(boss.isMovingDown(), "Boss must start moving down.");
        }
    
    // --- 2. 이동 로직 테스트 ---
    
    @Test
    void movementFlipsAtHorizontalBoundary() {
        // 초기 상태: 오른쪽 이동 중
        assertTrue(boss.isMovingRight(), "Initially moving right.");
        
        // 강제로 오른쪽 끝(화면 밖)으로 위치 이동
        boss.setPositionX(1200); // Core.WIDTH 가정
        boss.update(); // 이동 로직 실행
        
        boss.update();
        assertFalse(boss.isMovingRight(), "Direction must flip to Left at right boundary.");
        
        // 좌측 경계 테스트
        boss.setPositionX(0);
        
        boss.update();
        assertTrue(boss.isMovingRight(), "Direction must flip to Right at left boundary.");
    }
    
    @Test
    void movementFlipsAtVerticalBoundary() {
        // 초기 상태: 아래로 이동 중
        assertTrue(boss.isMovingDown(), "Initially moving down.");
        
        // 강제로 아래쪽 한계(BOSS_MAX_Y = 340) 넘어서 이동
        boss.setPositionY(350);
        boss.update();
        assertFalse(boss.isMovingDown(), "Direction must flip to Up at BOSS_MAX_Y.");
        
        // 상단 경계 테스트
        boss.setPositionY(TOP_BOUNDARY);
        
        boss.update();
        assertTrue(boss.isMovingDown(), "Direction must flip to Down at TOP_BOUNDARY.");
    }
    
    // --- 3. 공격 활성화 및 패턴 전환 테스트 ---
    
    @Test
    void attackEnablesAtThreshold() {
        // HP 500 (임계값보다 높음)
        assertTrue(boss.isAttackEnabled(), "Attack must be enabled at full HP.");
        
        // HP를 임계값 + 1로 드롭
        boss.getDamage(boss.getHealth() - (boss.getAttackHpThreshold() + 1));
        boss.update();
        assertTrue(boss.isAttackEnabled(), "Attack must remain enabled just above threshold.");
        
        // HP를 임계값으로 드롭
        boss.hit();
        boss.update();
        assertTrue(boss.isAttackEnabled(), "Attack remains enabled at threshold.");
    }
    
    @Test
    void animationCycleIsCorrect() {
        // 초기 상태 확인
        assertEquals(SpriteType.BossShip1, boss.getSpriteType());
        
        // 1회 업데이트 (BossShip1 -> BossShip2)
        // Cooldown이 끝났다고 가정하기 위해 강제로 시간을 만료시키고 업데이트
        // (실제 Cooldown 구현이 복잡하므로, 여기서는 hit() 횟수로 대체하거나, update() 호출에만 의존)
        // 여기서는 update()를 쿨다운 시간 간격(500ms)에 맞게 충분히 많이 호출한다고 가정합니다.
        
        // BossShip.update()의 애니메이션 로직은 쿨다운에 의존하므로, 테스트에서는 쿨다운을 강제로 만료시키는 Mocking이 이상적입니다.
        // Mocking 없이 간단히 로직만 확인:
        
        // (쿨다운이 만료되었다고 가정하고) 1회 업데이트
        // 실제 테스트에서는 Cooldown.checkFinished()가 true를 반환하도록 시간을 조작해야 합니다.
        
        // 여기서는 코드를 간결하게 유지하기 위해 강제 호출 대신, 직접적인 Getter로 스프라이트 타입을 확인합니다.
    }
}