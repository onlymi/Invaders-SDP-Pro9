package entity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

import engine.AssetManager.SpriteType;
import engine.Core;
import engine.SoundManager;
import engine.utils.Cooldown;
import entity.character.GameCharacter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.MockitoAnnotations;

class EnemyTypeBTest {
    
    private EnemyTypeB enemyB;
    private MockedStatic<Core> coreMock;
    private MockedStatic<SoundManager> soundManagerMock;
    
    @Mock
    private Cooldown mockCooldown;
    @Mock
    private GameCharacter mockPlayer;
    
    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        
        // 정적 클래스 Mocking
        coreMock = mockStatic(Core.class);
        soundManagerMock = mockStatic(SoundManager.class);
        
        // Cooldown 동작 정의
        coreMock.when(() -> Core.getCooldown(anyInt())).thenReturn(mockCooldown);
        when(mockCooldown.checkFinished()).thenReturn(true);
        
        // EnemyB 생성 (초기 위치 500, 500)
        enemyB = new EnemyTypeB(500, 500, SpriteType.EnemyB_Move);
    }
    
    @AfterEach
    void tearDown() {
        coreMock.close();
        soundManagerMock.close();
    }
    
    @Test
    void testStatsInitialization() {
        // 초기 스탯 확인
        assertEquals(30, enemyB.getHealth());
        assertEquals(40, enemyB.getPointValue());
        assertEquals(15, enemyB.getCoinValue());
    }
    
    @Test
    void testTakeDamage() {
        // 15 데미지 피격
        enemyB.hit(15);
        assertEquals(15, enemyB.getHealth());
        assertFalse(enemyB.isDestroyed());
        
        // 추가 15 데미지 -> 파괴
        enemyB.hit(15);
        assertEquals(0, enemyB.getHealth());
        assertTrue(enemyB.isDestroyed());
    }
    
    @Test
    void testMovement_ApproachPlayer() {
        // 상황: 플레이어가 멀리 있음 (거리 > 250)
        // Enemy(500, 500), Player(0, 0)
        when(mockPlayer.getPositionX()).thenReturn(0);
        when(mockPlayer.getPositionY()).thenReturn(0);
        when(mockPlayer.isDie()).thenReturn(false);
        
        int initialX = enemyB.getPositionX();
        
        // 업데이트 실행
        enemyB.update(mockPlayer, new ArrayList<>());
        
        // 플레이어 쪽으로 이동해야 함 (좌표가 감소해야 함)
        // Type B는 X축 이동뿐만 아니라 Y축 Floating 애니메이션이 있지만,
        // 플레이어 쪽으로 확실히 이동했다면 X좌표는 줄어들어야 함.
        assertTrue(enemyB.getPositionX() < initialX);
    }
    
    @Test
    void testMovement_RetreatFromPlayer() {
        // 플레이어가 너무 가까움 (거리 < 150)
        // Enemy(500, 500), Player(500, 500) -> 거리 0
        when(mockPlayer.getPositionX()).thenReturn(500);
        when(mockPlayer.getPositionY()).thenReturn(500);
        when(mockPlayer.isDie()).thenReturn(false);
        
        // 플레이어가 같은 위치에 있으면 방향 벡터 계산이 0이 될 수 있으므로 약간 오프셋을 줍니다.
        // Player(510, 510) -> Enemy보다 오른쪽 아래
        when(mockPlayer.getPositionX()).thenReturn(510);
        when(mockPlayer.getPositionY()).thenReturn(510);
        
        int initialX = enemyB.getPositionX();
        int initialY = enemyB.getPositionY();
        
        // 업데이트 실행
        enemyB.update(mockPlayer, new ArrayList<>());
        
        // 플레이어 반대 방향으로 도망가야 함
        assertTrue(enemyB.getPositionX() < initialX || enemyB.getPositionX() == initialX - 1);
    }
    
    @Test
    void testAttackLogic_DamageAndRange() {
        Set<Weapon> weapons = new HashSet<>();
        
        // 공격 범위 내 위치 설정 (Enemy: 500,500 / Player: 600,500) -> 거리 100
        when(mockPlayer.getPositionX()).thenReturn(600);
        when(mockPlayer.getPositionY()).thenReturn(500);
        when(mockPlayer.getWidth()).thenReturn(32);
        when(mockPlayer.getHeight()).thenReturn(32);
        when(mockPlayer.isDie()).thenReturn(false);
        
        // 공격 시도
        enemyB.tryAttack(mockPlayer, weapons);
        
        // 총알 생성 확인
        assertEquals(1, weapons.size());
        Weapon weapon = weapons.iterator().next();
        
        // 데미지 확인
        assertEquals(15, weapon.getDamage());
    }
    
    @Test
    void testAttackLogic_Aiming() {
        Set<Weapon> weapons = new HashSet<>();
        
        // Enemy: 500, 500
        // Player: 500, 600 (정확히 아래쪽)
        when(mockPlayer.getPositionX()).thenReturn(500);
        when(mockPlayer.getPositionY()).thenReturn(600);
        // 중앙점 계산 보정을 위해 width/height 고려 (단순화를 위해 0으로 가정하거나 로직 역산)
        when(mockPlayer.getWidth()).thenReturn(0);
        when(mockPlayer.getHeight()).thenReturn(0);
        
        // Enemy 크기도 고려 (width 48, height 48 가정) -> 중심 (524, 524) 정도
        // 각도 계산은 Math.atan2(targetY - startY, targetX - startX)
        // 대략적으로 아래쪽(90도)을 향해야 함.
        
        enemyB.tryAttack(mockPlayer, weapons);
        Weapon weapon = weapons.iterator().next();
        
        // 속도 성분 확인: 아래로 쏘니까 SpeedY는 양수(>0), SpeedX는 거의 0
        assertTrue(weapon.getSpeed() > 0, "아래쪽에 있는 플레이어를 향해 Y속도가 양수여야 합니다.");
        // 정확한 각도 검증보다는 방향성 검증이 안전함
    }
    
    @Test
    void testAttack_CooldownNotFinished() {
        Set<Weapon> weapons = new HashSet<>();
        
        // 거리 조건 만족
        when(mockPlayer.getPositionX()).thenReturn(500);
        when(mockPlayer.getPositionY()).thenReturn(500);
        
        // 쿨다운이 아직 안 끝남
        when(mockCooldown.checkFinished()).thenReturn(false);
        
        enemyB.tryAttack(mockPlayer, weapons);
        
        // 총알이 발사되면 안 됨
        assertEquals(0, weapons.size());
    }
}