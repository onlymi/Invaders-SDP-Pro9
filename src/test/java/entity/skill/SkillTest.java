package entity.skill;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import engine.Core;
import engine.utils.Cooldown;
import entity.Weapon;
import entity.character.GameCharacter;
import java.util.Set;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.MockitoAnnotations;

class SkillTest {
    
    @Mock
    private GameCharacter attacker;
    @Mock
    private Cooldown cooldown;
    
    private MockedStatic<Core> coreMock;
    private TestSkill skill;
    
    // Skill 추상 클래스를 테스트하기 위한 구체적인 구현체
    private static class TestSkill extends Skill {
        
        boolean performed = false;
        
        public TestSkill(Cooldown cooldown) {
            super("Test Skill", 50, 5000); // Mana: 50, Cool: 5s
            this.coolDown = cooldown; // Mock Cooldown 주입
        }
        
        @Override
        public void performSkill(GameCharacter attacker, Set<Weapon> weapons) {
            this.performed = true;
        }
    }
    
    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        
        // Core.getCooldown 정적 메서드 모킹
        coreMock = mockStatic(Core.class);
        coreMock.when(() -> Core.getCooldown(anyInt())).thenReturn(cooldown);
        coreMock.when(Core::getLogger).thenReturn(java.util.logging.Logger.getGlobal());
        
        // 테스트용 스킬 생성
        skill = new TestSkill(cooldown);
    }
    
    @AfterEach
    void tearDown() {
        coreMock.close();
    }
    
    @Test
    void testCanActivate_WhenReadyAndManaEnough_ReturnsTrue() {
        // Given
        when(cooldown.checkFinished()).thenReturn(true);
        when(attacker.getCurrentManaPoints()).thenReturn(50); // 마나 충분
        
        // When
        boolean result = skill.canActivate(attacker);
        
        // Then
        assertTrue(result, "쿨타임이 끝났고 마나가 충분하면 true여야 합니다.");
    }
    
    @Test
    void testCanActivate_WhenCooldownNotFinished_ReturnsFalse() {
        // Given
        when(cooldown.checkFinished()).thenReturn(false);
        when(attacker.getCurrentManaPoints()).thenReturn(100);
        
        // When
        boolean result = skill.canActivate(attacker);
        
        // Then
        assertFalse(result, "쿨타임 중이면 false여야 합니다.");
    }
    
    @Test
    void testCanActivate_WhenManaNotEnough_ReturnsFalse() {
        // Given
        when(cooldown.checkFinished()).thenReturn(true);
        when(attacker.getCurrentManaPoints()).thenReturn(10); // 마나 부족 (필요: 50)
        
        // When
        boolean result = skill.canActivate(attacker);
        
        // Then
        assertFalse(result, "마나가 부족하면 false여야 합니다.");
    }
    
    @Test
    void testActivate_WhenCanActivate_ShouldPerformSkillAndConsumeResources() {
        // Given
        when(cooldown.checkFinished()).thenReturn(true);
        when(attacker.getCurrentManaPoints()).thenReturn(50);
        
        // When
        Set<Weapon> weapons = null;
        skill.activate(attacker, null);
        
        // Then
        assertTrue(skill.performed, "스킬 로직(performSkill)이 실행되어야 합니다.");
        verify(attacker).decreaseMana(50); // 마나 차감 검증
        verify(cooldown).reset(); // 쿨타임 리셋 검증
    }
    
    @Test
    void testActivate_WhenCannotActivate_ShouldDoNothing() {
        // Given: 쿨타임 중
        when(cooldown.checkFinished()).thenReturn(false);
        
        // When
        Set<Weapon> weapons = null;
        skill.activate(attacker, null);
        
        // Then
        assertFalse(skill.performed, "조건이 맞지 않으면 스킬이 실행되지 않아야 합니다.");
        verify(attacker, never()).decreaseMana(anyInt()); // 마나 차감 없어야 함
        verify(cooldown, never()).reset(); // 쿨타임 리셋 없어야 함
    }
    
    @Test
    void testActivate_WhenAttackerIsNull_ShouldDoNothing() {
        // When
        Set<Weapon> weapons = null;
        skill.activate(attacker, null);
        
        // Then
        assertFalse(skill.performed);
    }
    
    @Test
    void testGetters() {
        assertEquals("Test Skill", skill.getName());
        assertEquals(50, skill.getManaCost());
    }
}