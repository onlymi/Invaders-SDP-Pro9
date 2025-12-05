package entity.character;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.clearInvocations;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import engine.Core;
import engine.InputManager;
import engine.UserStats;
import engine.utils.Cooldown;
import entity.Entity;
import entity.Weapon;
import java.awt.event.KeyEvent;
import java.util.HashSet;
import java.util.Set;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.MockitoAnnotations;
import screen.Screen;

class GameCharacterAttackTest {
    
    @Mock
    private UserStats userStats;
    @Mock
    private InputManager inputManager;
    @Mock
    private Screen screen;
    @Mock
    private Cooldown shootingCooldown;
    @Mock
    private Cooldown destructionCooldown;
    
    private MockedStatic<Core> coreMock;
    private TestCharacter character;
    private Set<Weapon> weapons;
    
    // 테스트용 구체 클래스 정의 (GameCharacter는 추상 클래스이므로)
    private static class TestCharacter extends GameCharacter {
        
        public TestCharacter(int startX, int startY) {
            super(CharacterType.ARCHER, startX, startY, 20, 20, Entity.Team.PLAYER1, 1);
            
            this.baseStats.attackSpeed = 1.0f;
            this.baseStats.attackRange = 100f;
            this.currentStats = new CharacterStats(this.baseStats);
            
            // 키 설정: 0:Left, 1:Right, 2:Up, 3:Down, 4:Attack (GameCharacter.setControlKeys 로직에 따름)
            int[] keys = {
                KeyEvent.VK_LEFT,   // 0
                KeyEvent.VK_RIGHT,  // 1
                KeyEvent.VK_UP,     // 2
                KeyEvent.VK_DOWN,   // 3
                KeyEvent.VK_SPACE,  // 4
                0,                  // 5 (사용 안 함)
                KeyEvent.VK_1,      // 6
                KeyEvent.VK_2,      // 7
                KeyEvent.VK_3       // 8
            };
            this.setControlKeys(keys);
        }
        
        @Override
        protected void applyUserUpgrades() {
            // 테스트에서는 별도의 업그레이드 로직 없이 기본 스탯 사용
        }
        
        // 방향 상태를 강제로 설정하기 위한 헬퍼 메서드
        public void setFacingState(boolean left, boolean right, boolean up, boolean down) {
            this.isFacingLeft = left;
            this.isFacingRight = right;
            this.isFacingBack = up;
            this.isFacingFront = down;
        }
    }
    
    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        
        // Core의 정적 메서드 Mocking
        coreMock = mockStatic(Core.class);
        coreMock.when(Core::getUserStats).thenReturn(userStats);
        
        // Cooldown 생성 시 Mock 객체 반환
        coreMock.when(() -> Core.getCooldown(anyInt()))
            .thenReturn(shootingCooldown)      // recalculateStats() 내부 호출용
            .thenReturn(shootingCooldown)      // shootingCooldown용
            .thenReturn(destructionCooldown);  // destructionCooldown용
        
        weapons = new HashSet<>();
        character = new TestCharacter(100, 100);
        
        // 생성자에서 호출된 reset() 기록 초기화 (테스트 간 간섭 방지)
        clearInvocations(shootingCooldown, destructionCooldown);
    }
    
    @AfterEach
    void tearDown() {
        coreMock.close();
    }
    
    @Test
    void testLaunchBasicAttack_WhenCooldownFinished_ShouldFire() {
        // Given: 쿨타임이 끝난 상태
        when(shootingCooldown.checkFinished()).thenReturn(true);
        character.setFacingState(false, false, true, false); // 위쪽을 봄
        
        // When: 기본 공격 시도
        boolean result = character.launchBasicAttack(weapons);
        
        // Then
        assertTrue(result, "쿨타임이 끝났으면 발사에 성공(true)해야 합니다.");
        assertEquals(1, weapons.size(), "무기 목록에 총알이 1개 추가되어야 합니다.");
        verify(shootingCooldown).reset(); // 쿨다운 리셋 호출 검증
    }
    
    @Test
    void testLaunchBasicAttack_WhenCooldownNotFinished_ShouldNotFire() {
        // Given: 쿨타임이 아직 안 끝난 상태
        when(shootingCooldown.checkFinished()).thenReturn(false);
        
        // When: 기본 공격 시도
        boolean result = character.launchBasicAttack(weapons);
        
        // Then
        assertFalse(result, "쿨타임 중이면 발사에 실패(false)해야 합니다.");
        assertTrue(weapons.isEmpty(), "총알이 생성되지 않아야 합니다.");
        verify(shootingCooldown, never()).reset(); // 쿨다운 리셋이 호출되지 않아야 함
    }
    
    @Test
    void testAttackDirection_FacingLeft() {
        // Given: 왼쪽을 보고 있음
        when(shootingCooldown.checkFinished()).thenReturn(true);
        character.setFacingState(true, false, false, false);
        
        // When
        character.launchBasicAttack(weapons);
        
        // Then
        assertFalse(weapons.isEmpty());
        Weapon firedWeapon = weapons.iterator().next();
        // 왼쪽 발사 시 X 위치가 캐릭터보다 왼쪽에 있거나 속도가 음수여야 함을 검증할 수 있음.
        // 여기서는 Weapon이 생성되었는지 정도만 확인합니다.
        assertTrue(firedWeapon.getPositionX() < character.getPositionX(),
            "왼쪽 발사 시 총알 시작 위치는 캐릭터보다 왼쪽이어야 합니다.");
    }
    
    @Test
    void testAttackDirection_FacingRight() {
        // Given: 오른쪽을 보고 있음
        when(shootingCooldown.checkFinished()).thenReturn(true);
        character.setFacingState(false, true, false, false);
        
        // When
        character.launchBasicAttack(weapons);
        
        // Then
        assertFalse(weapons.isEmpty());
        Weapon firedWeapon = weapons.iterator().next();
        assertEquals(character.getPlayerId(), firedWeapon.getPlayerId(),
            "발사된 총알의 소유자는 캐릭터 ID와 같아야 합니다.");
    }
    
    @Test
    void testHandleMovement_WhenSpacePressed_AndCooldownFinished_ShouldFire() {
        // Given: 공격 키 누름 + 쿨타임 끝남
        when(inputManager.isKeyDown(KeyEvent.VK_SPACE)).thenReturn(true);
        when(shootingCooldown.checkFinished()).thenReturn(true);
        
        // When
        character.handleKeyboard(inputManager, screen, weapons, 1.0f);
        
        // Then
        assertFalse(weapons.isEmpty(), "총알이 발사되어야 합니다.");
        assertTrue(character.isAttacking(), "공격 키를 눌렀으므로 공격 모션 상태(isAttacking)여야 합니다.");
        assertTrue(character.isFiring(), "실제로 발사되었으므로 발사 상태(isFiring)여야 합니다.");
    }
    
    @Test
    void testHandleMovement_WhenSpacePressed_ButCooldownNotFinished_ShouldNotFire() {
        // Given: 공격 키 누름 + 하지만 쿨타임 중 (버그 수정 검증의 핵심 케이스)
        when(inputManager.isKeyDown(KeyEvent.VK_SPACE)).thenReturn(true);
        when(shootingCooldown.checkFinished()).thenReturn(false);
        
        // When
        character.handleKeyboard(inputManager, screen, weapons, 1.0f);
        
        // Then
        assertTrue(weapons.isEmpty(), "쿨타임 중이므로 총알이 나가지 않아야 합니다.");
        assertTrue(character.isAttacking(), "키를 누르고 있으므로 공격 모션(isAttacking)은 유지되어야 합니다.");
        assertFalse(character.isFiring(), "총알이 나가지 않았으므로 발사 판정(isFiring)은 false여야 합니다.");
        // -> 이 부분이 false여야 소리 재생 및 카운트 증가가 발생하지 않음
    }
    
    @Test
    void testHandleMovement_WhenSpaceNotPressed_NoAttack() {
        // Given: 공격 키 안 누름
        when(inputManager.isKeyDown(KeyEvent.VK_SPACE)).thenReturn(false);
        when(shootingCooldown.checkFinished()).thenReturn(true);
        
        // When
        character.handleKeyboard(inputManager, screen, weapons, 1.0f);
        
        // Then
        assertTrue(weapons.isEmpty());
        assertFalse(character.isAttacking());
        assertFalse(character.isFiring());
    }
}