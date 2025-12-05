package entity.skill;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;

import engine.Core;
import engine.utils.Cooldown;
import entity.buff.RapidFireSkillBuff;
import entity.character.GameCharacter;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.MockitoAnnotations;

class RapidFireSkillTest {
    
    @Mock
    private GameCharacter attacker;
    @Mock
    private Cooldown cooldown;
    
    private MockedStatic<Core> coreMock;
    private RapidFireSkill rapidFireSkill;
    
    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        
        // Core.getCooldown 정적 메서드 모킹
        coreMock = mockStatic(Core.class);
        coreMock.when(() -> Core.getCooldown(anyInt())).thenReturn(cooldown);
        coreMock.when(Core::getLogger).thenReturn(java.util.logging.Logger.getGlobal());
        
        // RapidFireSkill 인스턴스 생성
        rapidFireSkill = new RapidFireSkill();
    }
    
    @AfterEach
    void tearDown() {
        coreMock.close();
    }
    
    @Test
    void testConstructorValues() {
        // RapidFireSkill의 상수 값 검증
        // MANA_COST = 30, NAME = "Rapid Fire"
        assertEquals("Rapid Fire", rapidFireSkill.getName());
        assertEquals(30, rapidFireSkill.getManaCost());
        
        // Cooldown 생성 시 12초(12000ms)가 전달되었는지 확인하고 싶지만
        // 생성자 내부 호출이라 ArgumentCaptor를 setUp에서 미리 걸거나
        // Core.getCooldown 호출 인자를 검증해야 함.
        // 여기서는 간단히 객체 생성 후 속성 확인에 집중.
    }
    
    @Test
    void testPerformSkill_ShouldAddRapidFireBuffToAttacker() {
        // When
        rapidFireSkill.performSkill(attacker, null);
        
        // Then
        // attacker.addBuff()가 호출되었는지, 그리고 전달된 버프가 RapidFireSkillBuff 타입인지 검증
        ArgumentCaptor<entity.buff.Buff> buffCaptor = ArgumentCaptor.forClass(
            entity.buff.Buff.class);
        verify(attacker).addBuff(buffCaptor.capture());
        
        entity.buff.Buff capturedBuff = buffCaptor.getValue();
        assertTrue(capturedBuff instanceof RapidFireSkillBuff,
            "RapidFireSkill은 RapidFireSkillBuff를 캐릭터에게 부여해야 합니다.");
    }
}