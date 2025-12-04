package entity.character;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

import engine.Core;
import engine.UserStats;
import engine.utils.Cooldown;
import entity.Entity;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.MockitoAnnotations;

class StatsUpgradeTest {
    
    @Mock
    private UserStats userStats;
    
    private MockedStatic<Core> coreMock;
    
    // 테스트를 위한 구체적인 구현체
    private static class TestCharacter extends GameCharacter {
        
        public TestCharacter() {
            // 1. 부모 생성자 호출 (내부에서 applyUserUpgrades가 호출되지만, 아래에서 오버라이드했으므로 무시됨)
            super(CharacterType.ARCHER, 0, 0, 10, 10, Entity.Team.PLAYER1, 1);
            
            // 2. 테스트를 위해 스탯 강제 재설정
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
            // baseStats 변경 사항을 반영하여 currentStats 재생성
            this.currentStats = new CharacterStats(this.baseStats);
        }
        
        // 생성자 시점에는 업그레이드를 적용하지 않음 (순수 기본값 테스트를 위해)
        @Override
        protected void applyUserUpgrades() {
            // Do nothing intentionally
        }
        
        // 테스트 코드에서 명시적으로 업그레이드 로직을 수행하기 위한 메서드
        public void runUpgradeLogic() {
            super.applyUserUpgrades(); // 부모의 원본 로직 호출
            this.currentStats = new CharacterStats(this.baseStats); // 변경된 baseStats 반영
        }
    }
    
    @BeforeEach
    void setUp() {
        // Mock 어노테이션 초기화
        MockitoAnnotations.openMocks(this);
        
        // Core 클래스의 정적 메서드 Mocking 시작
        coreMock = mockStatic(Core.class);
        coreMock.when(Core::getUserStats).thenReturn(userStats);
        coreMock.when(Core::getLogger).thenReturn(java.util.logging.Logger.getAnonymousLogger());
        
        // Cooldown 생성 로직은 실제 객체를 반환하도록 설정
        coreMock.when(() -> Core.getCooldown(anyInt()))
            .thenAnswer(invocation -> new Cooldown(invocation.getArgument(0)));
        
        // 기본적으로 모든 스탯 레벨은 0으로 가정
        for (int i = 0; i < 8; i++) {
            when(userStats.getStatLevel(i)).thenReturn(0);
        }
    }
    
    @AfterEach
    void tearDown() {
        // 정적 Mock 해제 (필수)
        if (coreMock != null) {
            coreMock.close();
        }
    }
    
    @Test
    void testNoUpgrades() {
        TestCharacter character = new TestCharacter();
        
        // 스탯 레벨 0일 때 기본값 확인
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
        // Health(인덱스 0) 1레벨 증가 (+20%)
        when(userStats.getStatLevel(0)).thenReturn(1);
        TestCharacter character = new TestCharacter();
        
        character.runUpgradeLogic();
        
        assertEquals(120, character.getBaseStats().maxHealthPoints);
        assertEquals(120, character.getCurrentHealthPoints()); // 최대 체력 증가 시 현재 체력도 보정되는지 확인
        
        // Health(인덱스 0) 2레벨 증가 (+40%)
        when(userStats.getStatLevel(0)).thenReturn(2);
        character = new TestCharacter(); // 새 캐릭터로 다시 테스트
        character.runUpgradeLogic();
        
        assertEquals(140, character.getBaseStats().maxHealthPoints);
        assertEquals(140, character.getCurrentHealthPoints());
    }
    
    @Test
    void testManaUpgrades() {
        // Mana(인덱스 1) 1레벨 증가 (+20%)
        when(userStats.getStatLevel(1)).thenReturn(1);
        TestCharacter character = new TestCharacter();
        
        character.runUpgradeLogic();
        
        assertEquals(120, character.getBaseStats().maxManaPoints);
        assertEquals(120, character.getCurrentManaPoints());
    }
    
    @Test
    void testAllUpgrades() {
        // 모든 스탯 1레벨 증가
        for (int i = 0; i < 8; i++) {
            when(userStats.getStatLevel(i)).thenReturn(1);
        }
        TestCharacter character = new TestCharacter();
        
        character.runUpgradeLogic();
        
        assertEquals(120, character.getBaseStats().maxHealthPoints); // HP +20%
        assertEquals(120, character.getBaseStats().maxManaPoints);   // MP +20%
        assertEquals(1.1f, character.getBaseStats().movementSpeed, 0.001f); // Speed +10%
        assertEquals(12, character.getBaseStats().physicalDamage);   // PhysicalDmg +20%
        assertEquals(12, character.getBaseStats().magicalDamage);    // MagicalDmg +20%
        assertEquals(1.1f, character.getBaseStats().attackSpeed, 0.001f); // AS +10%
        assertEquals(11.0f, character.getBaseStats().attackRange, 0.001f); // Range +10%
        assertEquals(0.1f, character.getBaseStats().critChance, 0.001f); // Crit +5%p
        assertEquals(2, character.getBaseStats().physicalDefense);   // Def +2
    }
    
    @Test
    void testLowDamageUpgrades() {
        // Damage(인덱스 3) 1레벨 증가 (+20%)
        when(userStats.getStatLevel(3)).thenReturn(1);
        TestCharacter character = new TestCharacter();
        
        // 기본 데미지가 매우 낮은 경우의 올림(Math.ceil) 처리 테스트
        character.getBaseStats().physicalDamage = 1;
        character.getBaseStats().magicalDamage = 2;
        
        character.runUpgradeLogic();
        
        // 계산: 1 * 1.2 = 1.2 -> ceil(1.2) = 2.0 -> 2
        assertEquals(2, character.getBaseStats().physicalDamage);
        
        // 계산: 2 * 1.2 = 2.4 -> ceil(2.4) = 3.0 -> 3
        assertEquals(3, character.getBaseStats().magicalDamage);
    }
}