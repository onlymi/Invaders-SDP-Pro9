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
    
    private static class TestCharacter extends GameCharacter {
        
        public TestCharacter(int startX, int startY) {
            super(CharacterType.ARCHER, startX, startY, 20, 20, Entity.Team.PLAYER1, 1);
            
            this.baseStats.attackSpeed = 1.0f;
            this.baseStats.attackRange = 100f;
            this.currentStats = new CharacterStats(this.baseStats);
            
            int[] keys = {KeyEvent.VK_LEFT, KeyEvent.VK_RIGHT, KeyEvent.VK_UP, KeyEvent.VK_DOWN,
                KeyEvent.VK_SPACE};
            this.setControlKeys(keys);
        }
        
        @Override
        protected void applyUserUpgrades() {
        }
        
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
        
        coreMock = mockStatic(Core.class);
        coreMock.when(Core::getUserStats).thenReturn(userStats);
        
        // 생성자에서 호출되는 getCooldown에 대해 Mock 객체 반환
        coreMock.when(() -> Core.getCooldown(anyInt()))
            .thenReturn(shootingCooldown)
            .thenReturn(destructionCooldown);
        
        weapons = new HashSet<>();
        character = new TestCharacter(100, 100);
        
        // [핵심 수정] 생성자 실행 중 발생한 shootingCooldown.reset() 호출 기록을 지웁니다.
        // 이렇게 해야 각 테스트 메서드에서 발생한 호출만 카운트할 수 있습니다.
        clearInvocations(shootingCooldown, destructionCooldown);
    }
    
    @AfterEach
    void tearDown() {
        coreMock.close();
    }
    
    @Test
    void testLaunchBasicAttack_WhenCooldownFinished_ShouldFire() {
        // Given
        when(shootingCooldown.checkFinished()).thenReturn(true);
        character.setFacingState(false, false, true, false);
        
        // When
        boolean result = character.launchBasicAttack(weapons);
        
        // Then
        assertTrue(result, "쿨타임이 끝났으면 공격이 성공해야 합니다.");
        assertEquals(1, weapons.size(), "생성된 총알이 무기 목록에 추가되어야 합니다.");
        
        // 생성자 호출분은 clearInvocations로 지워졌으므로,
        // 여기서 발생한 1번의 reset만 검증됩니다.
        verify(shootingCooldown).reset();
    }
    
    @Test
    void testLaunchBasicAttack_WhenCooldownNotFinished_ShouldNotFire() {
        // Given
        when(shootingCooldown.checkFinished()).thenReturn(false);
        
        // When
        boolean result = character.launchBasicAttack(weapons);
        
        // Then
        assertFalse(result, "쿨타임 중일 때는 공격이 실패해야 합니다.");
        assertTrue(weapons.isEmpty(), "총알이 생성되지 않아야 합니다.");
        verify(shootingCooldown, never()).reset();
    }
    
    @Test
    void testAttackDirection_FacingLeft() {
        // Given
        when(shootingCooldown.checkFinished()).thenReturn(true);
        character.setFacingState(true, false, false, false);
        
        // When
        character.launchBasicAttack(weapons);
        
        // Then
        Weapon firedWeapon = weapons.iterator().next();
        
        // Weapon의 초기 속도(velocityX) 검증 (왼쪽이므로 음수여야 함)
        // Weapon 클래스 로직에 따라 getSpeedX() 등을 확인하거나
        // 혹은 위치 변화를 통해 간접 확인할 수 있습니다.
        assertTrue(firedWeapon.getSpeedX() <= 0, "왼쪽을 보고 쏘면 총알 속도가 왼쪽 방향이어야 합니다.");
    }
    
    @Test
    void testAttackDirection_FacingRight() {
        // Given
        when(shootingCooldown.checkFinished()).thenReturn(true);
        character.setFacingState(false, true, false, false);
        
        // When
        character.launchBasicAttack(weapons);
        
        // Then
        Weapon firedWeapon = weapons.iterator().next();
        assertEquals(character.getPlayerId(), firedWeapon.getPlayerId());
    }
    
    @Test
    void testHandleMovement_WhenSpacePressed_TriggersAttack() {
        // Given
        when(inputManager.isKeyDown(KeyEvent.VK_SPACE)).thenReturn(true);
        when(shootingCooldown.checkFinished()).thenReturn(true);
        
        // When
        character.handleMovement(inputManager, screen, weapons, 1.0f);
        
        // Then
        assertFalse(weapons.isEmpty(), "공격 키(Space)를 누르면 총알이 발사되어야 합니다.");
        assertTrue(character.isAttacking(), "캐릭터의 공격 상태 플래그가 true여야 합니다.");
    }
    
    @Test
    void testHandleMovement_WhenSpaceNotPressed_NoAttack() {
        // Given
        when(inputManager.isKeyDown(KeyEvent.VK_SPACE)).thenReturn(false);
        when(shootingCooldown.checkFinished()).thenReturn(true);
        
        // When
        character.handleMovement(inputManager, screen, weapons, 1.0f);
        
        // Then
        assertTrue(weapons.isEmpty(), "공격 키를 누르지 않으면 총알이 발사되지 않아야 합니다.");
        assertFalse(character.isAttacking(), "캐릭터의 공격 상태 플래그가 false여야 합니다.");
    }
}