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
    @Mock
    private Cooldown stunCooldown; // Stun Cooldown용 Mock 추가
    
    private MockedStatic<Core> coreMock;
    private TestCharacter character;
    private Set<Weapon> weapons;
    
    private static class TestCharacter extends GameCharacter {
        
        public TestCharacter(int startX, int startY) {
            super(CharacterType.ARCHER, startX, startY, 20, 20, Entity.Team.PLAYER1, 1);
            
            this.baseStats.attackSpeed = 1.0f;
            this.baseStats.attackRange = 100f;
            this.currentStats = new CharacterStats(this.baseStats);
            
            int[] keys = {
                KeyEvent.VK_LEFT,   // 0
                KeyEvent.VK_RIGHT,  // 1
                KeyEvent.VK_UP,     // 2
                KeyEvent.VK_DOWN,   // 3
                KeyEvent.VK_SPACE,  // 4
                0,                  // 5
                KeyEvent.VK_1,      // 6
                KeyEvent.VK_2,      // 7
                KeyEvent.VK_3       // 8
            };
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
        
        // 1. recalculateStats() -> shootingCooldown
        // 2. this.shootingCooldown 생성 -> shootingCooldown
        // 3. this.destructionCooldown 생성 -> destructionCooldown
        // 4. this.stunCooldown 생성 -> stunCooldown
        coreMock.when(() -> Core.getCooldown(anyInt()))
            .thenReturn(shootingCooldown)
            .thenReturn(shootingCooldown)
            .thenReturn(destructionCooldown)
            .thenReturn(stunCooldown);
        
        when(stunCooldown.checkFinished()).thenReturn(true);
        
        weapons = new HashSet<>();
        character = new TestCharacter(100, 100);
        
        clearInvocations(shootingCooldown, destructionCooldown, stunCooldown);
    }
    
    @AfterEach
    void tearDown() {
        coreMock.close();
    }
    
    @Test
    void testLaunchBasicAttack_WhenCooldownFinished_ShouldFire() {
        when(shootingCooldown.checkFinished()).thenReturn(true);
        character.setFacingState(false, false, true, false);
        
        boolean result = character.launchBasicAttack(weapons);
        
        assertTrue(result, "쿨타임이 끝났으면 발사에 성공(true)해야 합니다.");
        assertEquals(1, weapons.size(), "무기 목록에 총알이 1개 추가되어야 합니다.");
        verify(shootingCooldown).reset();
    }
    
    @Test
    void testLaunchBasicAttack_WhenCooldownNotFinished_ShouldNotFire() {
        when(shootingCooldown.checkFinished()).thenReturn(false);
        
        boolean result = character.launchBasicAttack(weapons);
        
        assertFalse(result, "쿨타임 중이면 발사에 실패(false)해야 합니다.");
        assertTrue(weapons.isEmpty(), "총알이 생성되지 않아야 합니다.");
        verify(shootingCooldown, never()).reset();
    }
    
    @Test
    void testAttackDirection_FacingLeft() {
        when(shootingCooldown.checkFinished()).thenReturn(true);
        character.setFacingState(true, false, false, false);
        
        character.launchBasicAttack(weapons);
        
        assertFalse(weapons.isEmpty());
        Weapon firedWeapon = weapons.iterator().next();
        assertTrue(firedWeapon.getPositionX() < character.getPositionX(),
            "왼쪽 발사 시 총알 시작 위치는 캐릭터보다 왼쪽이어야 합니다.");
    }
    
    @Test
    void testAttackDirection_FacingRight() {
        when(shootingCooldown.checkFinished()).thenReturn(true);
        character.setFacingState(false, true, false, false);
        
        character.launchBasicAttack(weapons);
        
        assertFalse(weapons.isEmpty());
        Weapon firedWeapon = weapons.iterator().next();
        assertEquals(character.getPlayerId(), firedWeapon.getPlayerId(),
            "발사된 총알의 소유자는 캐릭터 ID와 같아야 합니다.");
    }
    
    @Test
    void testHandleMovement_WhenSpacePressed_AndCooldownFinished_ShouldFire() {
        when(inputManager.isKeyDown(KeyEvent.VK_SPACE)).thenReturn(true);
        when(shootingCooldown.checkFinished()).thenReturn(true);
        
        character.handleKeyboard(inputManager, screen, weapons, 1.0f);
        
        assertFalse(weapons.isEmpty(), "총알이 발사되어야 합니다.");
        assertTrue(character.isAttacking(), "공격 키를 눌렀으므로 공격 모션 상태(isAttacking)여야 합니다.");
        assertTrue(character.isFiring(), "실제로 발사되었으므로 발사 상태(isFiring)여야 합니다.");
    }
    
    @Test
    void testHandleMovement_WhenSpacePressed_ButCooldownNotFinished_ShouldNotFire() {
        when(inputManager.isKeyDown(KeyEvent.VK_SPACE)).thenReturn(true);
        when(shootingCooldown.checkFinished()).thenReturn(false);
        
        character.handleKeyboard(inputManager, screen, weapons, 1.0f);
        
        assertTrue(weapons.isEmpty(), "쿨타임 중이므로 총알이 나가지 않아야 합니다.");
        assertTrue(character.isAttacking(), "키를 누르고 있으므로 공격 모션(isAttacking)은 유지되어야 합니다.");
        assertFalse(character.isFiring(), "총알이 나가지 않았으므로 발사 판정(isFiring)은 false여야 합니다.");
    }
    
    @Test
    void testHandleMovement_WhenSpaceNotPressed_NoAttack() {
        when(inputManager.isKeyDown(KeyEvent.VK_SPACE)).thenReturn(false);
        when(shootingCooldown.checkFinished()).thenReturn(true);
        
        character.handleKeyboard(inputManager, screen, weapons, 1.0f);
        
        assertTrue(weapons.isEmpty());
        assertFalse(character.isAttacking());
        assertFalse(character.isFiring());
    }
}