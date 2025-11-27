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
            super(0, 0, 10, 10, CharacterType.ARCHER); // ARCHER 타입을 베이스로 하되 값은 검증 시 조정
            // 강제로 기본값 재설정
            this.maxHealthPoints = 100;
            this.currentHealthPoints = 100;
            this.maxManaPoints = 100;
            this.currentManaPoints = 100;
            this.movementSpeed = 1.0f;
            this.physicalDamage = 10;
            this.magicalDamage = 10;
            this.attackSpeed = 1.0f;
            this.attackRange = 10.0f;
            this.critChance = 0.05f;
            this.physicalDefense = 0;
            
            this.currentHealthPoints = this.maxHealthPoints;
            this.currentManaPoints = this.maxManaPoints;
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
        assertEquals(100, character.getMaxHealthPoints());
        assertEquals(100, character.getCurrentHealthPoints());
        assertEquals(100, character.getMaxManaPoints());
        assertEquals(100, character.getCurrentManaPoints());
        assertEquals(1.0f, character.getMovementSpeed());
        assertEquals(10, character.getPhysicalDamage());
        assertEquals(10, character.getMagicalDamage());
        assertEquals(1.0f, character.getAttackSpeed());
        assertEquals(10.0f, character.getAttackRange());
        assertEquals(0.05f, character.getCritChance());
        assertEquals(0, character.getPhysicalDefense());
    }
    
    @Test
    void testHealthUpgrades() {
        // 1레벨 증가 Test
        when(userStats.getStatLevel(0)).thenReturn(1);  // HP +20%
        TestCharacter character = new TestCharacter();
        
        character.runUpgradeLogic();
        
        assertEquals(120, character.getMaxHealthPoints());
        assertEquals(120, character.getCurrentHealthPoints());
        
        // 2레벨 증가 Test
        when(userStats.getStatLevel(0)).thenReturn(2);  // HP +40%
        character = new TestCharacter();
        character.runUpgradeLogic();
        
        assertEquals(140, character.getMaxHealthPoints());
        assertEquals(140, character.getCurrentHealthPoints());
        
        // 이외의 스탯은 변경되지 않아야 함
        assertEquals(100, character.getMaxManaPoints());
        assertEquals(1.0f, character.getMovementSpeed());
        assertEquals(10, character.getPhysicalDamage());
        assertEquals(10, character.getMagicalDamage());
    }
    
    @Test
    void testManaUpgrades() {
        when(userStats.getStatLevel(1)).thenReturn(1);  // MP +20%
        TestCharacter character = new TestCharacter();
        
        character.runUpgradeLogic();
        
        assertEquals(120, character.getMaxManaPoints());
        assertEquals(120, character.getCurrentManaPoints());
        
        when(userStats.getStatLevel(1)).thenReturn(3);  // MP +60%
        character = new TestCharacter();
        
        character.runUpgradeLogic();
        
        assertEquals(160, character.getMaxManaPoints());
        assertEquals(160, character.getCurrentManaPoints());
    }
    
    @Test
    void testAllUpgrades() {
        for (int i = 0; i < 8; i++) {
            when(userStats.getStatLevel(i)).thenReturn(1);
        }
        TestCharacter character = new TestCharacter();
        
        character.runUpgradeLogic();
        
        assertEquals(120, character.getMaxHealthPoints()); // HP +20%
        assertEquals(120, character.getMaxManaPoints());   // MP +20%
        assertEquals(1.1f, character.getMovementSpeed(), 0.001f); // Speed +10%
        assertEquals(12, character.getPhysicalDamage());   // PhysicalDmg +20%
        assertEquals(12, character.getMagicalDamage());    // MagicalDmg + 20%
        assertEquals(1.1f, character.getAttackSpeed(), 0.001f); // AS +10%
        assertEquals(11.0f, character.getAttackRange(), 0.001f); // Range +10%
        assertEquals(0.1f, character.getCritChance(), 0.001f); // Crit +5%p (0.05 + 0.05)
        assertEquals(2, character.getPhysicalDefense());   // Def +2
    }
    
    @Test
    void testLowDamageUpgrades() {
        // 기본 데미지가 매우 낮은 경우 Test
        
        // Damage Lv 1 (+20%)
        when(userStats.getStatLevel(3)).thenReturn(1);  // Damage +20%
        TestCharacter character = new TestCharacter();
        // 데미지를 낮은 값으로 설정하고 테스트 실행
        character.physicalDamage = 1;
        character.magicalDamage = 2;
        
        character.runUpgradeLogic();
        
        // Math.ceil(1 * (1 + 0.1)) = 2.0 -> int 형변환 -> 2
        // Math.ceil(2 * (1 + 0.2)) = 3.0 -> int 형변환 -> 3
        assertEquals(2, character.getPhysicalDamage());
        assertEquals(3, character.getMagicalDamage());
    }
}