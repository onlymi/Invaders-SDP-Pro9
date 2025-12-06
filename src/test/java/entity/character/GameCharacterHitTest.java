package entity.character;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import engine.Core;
import engine.UserStats;
import engine.utils.Cooldown;
import entity.Entity.Team;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.MockitoAnnotations;

class GameCharacterHitTest {
    
    private MockedStatic<Core> coreMock;
    
    @Mock
    private UserStats userStats;
    @Mock
    private Cooldown shootingCooldown;
    @Mock
    private Cooldown destructionCooldown; // 피격 무적 시간 관리용 쿨다운
    
    private ArcherCharacter character; // GameCharacter의 구현체로 테스트
    private final int MAX_HP = 90; // Archer 기본 HP
    
    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        
        coreMock = mockStatic(Core.class);
        coreMock.when(Core::getUserStats).thenReturn(userStats);
        
        // 생성자에서 두 번의 getCooldown 호출이 발생함 (shooting, destruction)
        // 첫 번째 호출(Shooting)과 두 번째 호출(Destruction)을 구분하여 Mock 객체 반환
        coreMock.when(() -> Core.getCooldown(anyInt()))
            .thenReturn(shootingCooldown)
            .thenReturn(shootingCooldown)
            .thenReturn(destructionCooldown);
        
        character = new ArcherCharacter(0, 0, Team.PLAYER1, 1);
    }
    
    @AfterEach
    void tearDown() {
        coreMock.close();
    }
    
    @Test
    void testTakeDamage_DecreasesHealth() {
        // Given
        int damage = 10;
        int initialHp = character.getCurrentHealthPoints();
        
        // When
        character.takeDamage(damage);
        
        // Then
        assertEquals(initialHp - damage, character.getCurrentHealthPoints(),
            "피격 시 체력이 데미지만큼 감소해야 합니다.");
    }
    
    @Test
    void testTakeDamage_TriggersInvincibility() {
        // When: 데미지를 입음
        character.takeDamage(10);
        
        // Then: 무적 쿨다운(destructionCooldown)이 리셋되어야 함
        verify(destructionCooldown, times(1)).reset();
    }
    
    @Test
    void testIsInvincible() {
        // Case 1: 쿨다운이 끝나지 않음 (무적 상태)
        when(destructionCooldown.checkFinished()).thenReturn(false);
        assertTrue(character.isInvincible(),
            "쿨다운이 끝나지 않았으면 무적 상태여야 합니다.");
        
        // Case 2: 쿨다운이 끝남 (무적 해제)
        when(destructionCooldown.checkFinished()).thenReturn(true);
        assertFalse(character.isInvincible(),
            "쿨다운이 끝났으면 무적 상태가 아니어야 합니다.");
    }
    
    @Test
    void testUpdate_SetsIsDie_WhenHealthIsZero() {
        // Given: 체력을 0으로 만듦
        character.takeDamage(MAX_HP);
        assertEquals(0, character.getCurrentHealthPoints());
        assertFalse(character.isDie(), "update 호출 전에는 아직 사망 상태가 아니어야 합니다.");
        
        // When: 게임 프레임 업데이트
        character.update(0.1f);
        
        // Then: 사망 플래그가 true로 변경되어야 함
        assertTrue(character.isDie(),
            "체력이 0 이하일 때 update를 호출하면 사망 상태가 되어야 합니다.");
    }
}