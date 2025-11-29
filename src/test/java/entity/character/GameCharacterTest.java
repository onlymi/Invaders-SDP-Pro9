package entity.character;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

import engine.Core;
import engine.UserStats;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.MockitoAnnotations;

class GameCharacterTest {
    
    @Mock
    private UserStats userStats;
    
    private MockedStatic<Core> coreMock;
    
    // 테스트를 위한 기본값 설정
    private static class TestCharacter extends GameCharacter {
        
        // 테스트용 구현체
        public TestCharacter() {
            super(CharacterType.ARCHER, 0, 0, 10, 10, Entity.Team.PLAYER1, 1); // ARCHER 타입을 베이스로 하되 값은 검증 시 조정
            // 강제로 기본값 재설정
            this.baseStats.maxHealthPoints = 100;
            this.baseStats.maxManaPoints = 100;
            this.baseStats.movementSpeed = 1.0f;
            this.baseStats.physicalDamage = 10;
            this.baseStats.magicalDamage = 10;
            this.baseStats.attackSpeed = 1.0f;
            this.baseStats.attackRange = 10.0f;
            this.baseStats.critChance = 0.05f;
            this.baseStats.physicalDefense = 0;
            
            this.currentHealthPoints = this.baseStats.maxHealthPoints;
            this.currentManaPoints = this.baseStats.maxManaPoints;
            this.currentStats = new CharacterStats(this.baseStats);
        }
        
        // 생성자에서 실행되는 로직 차단
        // 아무것도 하지 않음
        @Override
        protected void applyUserUpgrades() {
            // Do nothing intentionally
        }
        
        // 테스트에서 원할 때 부모의 메소드를 실행하는 트리거
        public void runUpgradeLogic() {
            super.applyUserUpgrades();
        }
    }
    
    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        coreMock = mockStatic(Core.class);
        coreMock.when(Core::getUserStats).thenReturn(userStats);
        coreMock.when(Core::getLogger).thenReturn(java.util.logging.Logger.getAnonymousLogger());
        
        // 기본적으로 모든 스탯 레벨은 0으로 가정
        // 필요한 것만 개별 테스트에서 덮어씀
        for (int i = 0; i < 8; i++) {
            when(userStats.getStatLevel(i)).thenReturn(0);
        }
    }
    
    @AfterEach
    void tearDown() {
        coreMock.close();
    }
    
    @Test
    void testNoUpgrades() {
        TestCharacter character = new TestCharacter();
        
        // 스탯 레벨 0 Test
        assertEquals(100, character.getBaseStats().maxHealthPoints);
        assertEquals(100, character.getCurrentHealthPoints());
        assertEquals(100, character.getBaseStats().maxManaPoints);
        assertEquals(100, character.getCurrentManaPoints());
        assertEquals(1.0f, character.getBaseStats().movementSpeed);
        assertEquals(10, character.getBaseStats().physicalDamage);
        assertEquals(10, character.getBaseStats().magicalDamage);
        assertEquals(1.0f, character.getBaseStats().attackSpeed);
        assertEquals(10.0f, character.getBaseStats().attackRange);
        assertEquals(0.05f, character.getBaseStats().critChance);
        assertEquals(0, character.getBaseStats().physicalDefense);
    }
    
    @Test
    void testHealthUpgrades() {
        // 1레벨 증가 Test
        when(userStats.getStatLevel(0)).thenReturn(1);  // HP +20%
        TestCharacter character = new TestCharacter();
        
        character.runUpgradeLogic();
        
        assertEquals(120, character.getBaseStats().maxHealthPoints);
        assertEquals(120, character.getCurrentHealthPoints());
        
        // 2레벨 증가 Test
        when(userStats.getStatLevel(0)).thenReturn(2);  // HP +40%
        character = new TestCharacter();
        character.runUpgradeLogic();
        
        assertEquals(140, character.getBaseStats().maxHealthPoints);
        assertEquals(140, character.getCurrentHealthPoints());
        
        // 이외의 스탯은 변경되지 않아야 함
        assertEquals(100, character.getBaseStats().maxManaPoints);
        assertEquals(1.0f, character.getBaseStats().movementSpeed);
        assertEquals(10, character.getBaseStats().physicalDamage);
        assertEquals(10, character.getBaseStats().magicalDamage);
    }
    
    @Test
    void testManaUpgrades() {
        when(userStats.getStatLevel(1)).thenReturn(1);  // MP +20%
        TestCharacter character = new TestCharacter();
        
        character.runUpgradeLogic();
        
        assertEquals(120, character.getBaseStats().maxManaPoints);
        assertEquals(120, character.getCurrentManaPoints());
        
        when(userStats.getStatLevel(1)).thenReturn(3);  // MP +60%
        character = new TestCharacter();
        
        character.runUpgradeLogic();
        
        assertEquals(160, character.getBaseStats().maxManaPoints);
        assertEquals(160, character.getCurrentManaPoints());
    }
    
    @Test
    void testAllUpgrades() {
        for (int i = 0; i < 8; i++) {
            when(userStats.getStatLevel(i)).thenReturn(1);
        }
        TestCharacter character = new TestCharacter();
        
        character.runUpgradeLogic();
        
        assertEquals(120, character.getBaseStats().maxHealthPoints); // HP +20%
        assertEquals(120, character.getBaseStats().maxManaPoints);   // MP +20%
        assertEquals(1.1f, character.getBaseStats().movementSpeed, 0.001f); // Speed +10%
        assertEquals(12, character.getBaseStats().physicalDamage);   // PhysicalDmg +20%
        assertEquals(12, character.getBaseStats().magicalDamage);    // MagicalDmg + 20%
        assertEquals(1.1f, character.getBaseStats().attackSpeed, 0.001f); // AS +10%
        assertEquals(11.0f, character.getBaseStats().attackRange, 0.001f); // Range +10%
        assertEquals(0.1f, character.getBaseStats().critChance, 0.001f); // Crit +5%p (0.05 + 0.05)
        assertEquals(2, character.getBaseStats().physicalDefense);   // Def +2
    }
    
    @Test
    void testLowDamageUpgrades() {
        // 기본 데미지가 매우 낮은 경우 Test
        
        // Damage Lv 1 (+20%)
        when(userStats.getStatLevel(3)).thenReturn(1);  // Damage +20%
        TestCharacter character = new TestCharacter();
        // 데미지를 낮은 값으로 설정하고 테스트 실행
        character.getBaseStats().physicalDamage = 1;
        character.getBaseStats().magicalDamage = 2;
        
        character.runUpgradeLogic();
        
        // Math.ceil(1 * (1 + 0.1)) = 2.0 -> int 형변환 -> 2
        // Math.ceil(2 * (1 + 0.2)) = 3.0 -> int 형변환 -> 3
        assertEquals(2, character.getBaseStats().physicalDamage);
        assertEquals(3, character.getBaseStats().magicalDamage);
    }
}
