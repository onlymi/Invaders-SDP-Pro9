package entity.character;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

import engine.Core;
import engine.UserStats;
import engine.utils.Cooldown;
import entity.Entity.Team;
import entity.Weapon;
import entity.WeaponPool;
import entity.skill.EvasionShotSkill;
import entity.skill.PiercingArrowSkill;
import entity.skill.RapidFireSkill;
import entity.skill.Skill;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.MockitoAnnotations;

class ArcherCharacterTest {
    
    private MockedStatic<Core> coreMock;
    
    @Mock
    private UserStats userStats;
    @Mock
    private Cooldown mockCooldown;
    
    // 공격 테스트용 인스턴스 및 상수
    private ArcherCharacter archerForAttackTest;
    private final int START_X = 100;
    private final int START_Y = 500;
    private final int PLAYER_ID = 1;
    
    @BeforeEach
    void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);
        
        // 1. WeaponPool 초기화 (테스트 간 간섭 제거)
        Field poolField = WeaponPool.class.getDeclaredField("pool");
        poolField.setAccessible(true);
        ((Set<?>) poolField.get(null)).clear();
        
        // 2. Core 및 Static 메서드 Mocking
        // GameCharacter 생성자에서 Core.getCooldown() 및 Core.getUserStats()를 호출하므로 필수
        coreMock = mockStatic(Core.class);
        coreMock.when(() -> Core.getCooldown(anyInt())).thenReturn(mockCooldown);
        coreMock.when(Core::getUserStats).thenReturn(userStats);
        
        // 공격 테스트를 위한 전용 인스턴스 생성
        archerForAttackTest = new ArcherCharacter(START_X, START_Y, Team.PLAYER1, PLAYER_ID);
    }
    
    @AfterEach
    void tearDown() {
        // Static Mock 해제 (메모리 누수 방지 및 다른 테스트 간섭 방지)
        coreMock.close();
    }
    
    // =========================================================
    //               Section 1: 스탯(Stats) 테스트
    // =========================================================
    
    @Test
    void testCharacter_ArcherCharacter_healthPoints() {
        ArcherCharacter character = new ArcherCharacter(0, 0, Team.PLAYER1, 1);
        
        int[] expectedValue = {90, 90};
        int[] actualValue = {character.getBaseStats().maxHealthPoints,
            character.currentHealthPoints};
        
        assertArrayEquals(expectedValue, actualValue,
            "Archer character health points is set incorrectly.");
    }
    
    @Test
    void testCharacter_ArcherCharacter_manaPoints() {
        ArcherCharacter character = new ArcherCharacter(0, 0, Team.PLAYER1, 1);
        
        int[] expectedValue = {100, 100};
        int[] actualValue = {character.getBaseStats().maxManaPoints, character.currentManaPoints};
        
        assertArrayEquals(expectedValue, actualValue,
            "Archer character mana points is set incorrectly.");
    }
    
    @Test
    void testCharacter_ArcherCharacter_movementSpeed() {
        ArcherCharacter character = new ArcherCharacter(0, 0, Team.PLAYER1, 1);
        
        float expectedValue = 1.2f;
        float actualValue = character.getBaseStats().movementSpeed;
        
        assertEquals(expectedValue, actualValue,
            "Archer character movement speed is set incorrectly.");
    }
    
    @Test
    void testCharacter_ArcherCharacter_damage() {
        ArcherCharacter character = new ArcherCharacter(0, 0, Team.PLAYER1, 1);
        
        int[] expectedValue = {18, 0};
        int[] actualValue = {character.getBaseStats().physicalDamage,
            character.getBaseStats().magicalDamage};
        
        assertArrayEquals(expectedValue, actualValue,
            "Archer character damage stat is set incorrectly.");
    }
    
    @Test
    void testCharacter_ArcherCharacter_attackSpeed() {
        ArcherCharacter character = new ArcherCharacter(0, 0, Team.PLAYER1, 1);
        
        float expectedValue = 1.5f;
        float actualValue = character.getBaseStats().attackSpeed;
        
        assertEquals(expectedValue, actualValue,
            "Archer character attack speed is set incorrectly.");
    }
    
    @Test
    void testCharacter_ArcherCharacter_attackRange() {
        ArcherCharacter character = new ArcherCharacter(0, 0, Team.PLAYER1, 1);
        
        float expectedValue = 12.0f;
        float actualValue = character.getBaseStats().attackRange;
        
        assertEquals(expectedValue, actualValue,
            "Archer character attack range is set incorrectly.");
    }
    
    @Test
    void testCharacter_ArcherCharacter_critical() {
        ArcherCharacter character = new ArcherCharacter(0, 0, Team.PLAYER1, 1);
        
        float[] expectedValue = {0.15f, 2.0f};
        float[] actualValue = {character.getBaseStats().critChance,
            character.getBaseStats().critDamageMultiplier};
        
        assertArrayEquals(expectedValue, actualValue,
            "Archer character critical stat is set incorrectly.");
    }
    
    @Test
    void testCharacter_ArcherCharacter_physicalDefense() {
        ArcherCharacter character = new ArcherCharacter(0, 0, Team.PLAYER1, 1);
        
        int expectedValue = 8;
        int actualValue = character.getBaseStats().physicalDefense;
        
        assertEquals(expectedValue, actualValue,
            "Archer character physical defense is set incorrectly.");
    }
    
    @Test
    void testCharacter_ArcherCharacter_unlocked() {
        ArcherCharacter character = new ArcherCharacter(0, 0, Team.PLAYER1, 1);
        
        boolean expectedValue = true;
        boolean actualValue = character.unlocked;
        
        assertEquals(expectedValue, actualValue,
            "Archer character unlock or not is set incorrectly.");
    }
    
    @Test
    void testCharacter_ArcherCharacter_skills() {
        ArcherCharacter character = new ArcherCharacter(0, 0, Team.PLAYER1, 1);
        ArrayList<Skill> actualValue = character.skills;
        
        assertEquals(3, actualValue.size(),
            "Archer character skill count is incorrect. Expected 3.");
        assertInstanceOf(RapidFireSkill.class, actualValue.get(0),
            "Skill at index 0 is not RapidFireSkill.");
        assertInstanceOf(EvasionShotSkill.class, actualValue.get(1),
            "Skill at index 1 is not EvasionShotSkill.");
        assertInstanceOf(PiercingArrowSkill.class, actualValue.get(2),
            "Skill at index 2 is not PiercingArrowSkill.");
    }
    
    // =========================================================
    //           Section 2: 기본 공격(Basic Attack) 테스트
    // =========================================================
    
    @Test
    void testLaunchBasicAttack_Success() {
        // Given: 쿨다운이 끝난 상태라고 가정
        Set<Weapon> weapons = new HashSet<>();
        when(mockCooldown.checkFinished()).thenReturn(true);
        
        // When: 기본 공격 실행
        boolean isFired = archerForAttackTest.launchBasicAttack(weapons);
        
        // Then: 발사 성공 및 Weapon 생성 확인
        assertTrue(isFired, "쿨다운이 끝났으므로 공격이 발사되어야 합니다.");
        assertEquals(1, weapons.size(), "Weapons Set에 총알이 1개 추가되어야 합니다.");
        
        // 생성된 총알 속성 검증
        Weapon weapon = weapons.iterator().next();
        assertNotNull(weapon);
        assertEquals(Team.PLAYER1, weapon.getTeam(), "플레이어 1의 팀이어야 합니다.");
        assertEquals(PLAYER_ID, weapon.getPlayerId(), "플레이어 ID가 일치해야 합니다.");
        
        // 위치 검증 (캐릭터보다 위쪽에 생성되어야 함 - Y좌표가 작아야 함)
        assertTrue(weapon.getPositionY() < archerForAttackTest.getPositionY(),
            "총알은 캐릭터보다 위쪽(Y값이 작은 쪽)에 생성되어야 합니다.");
    }
    
    @Test
    void testLaunchBasicAttack_CooldownNotFinished() {
        // Given: 쿨다운이 아직 끝나지 않은 상태
        Set<Weapon> weapons = new HashSet<>();
        when(mockCooldown.checkFinished()).thenReturn(false);
        
        // When: 기본 공격 실행
        boolean isFired = archerForAttackTest.launchBasicAttack(weapons);
        
        // Then: 발사 실패 확인
        assertFalse(isFired, "쿨다운 중에는 공격이 발사되지 않아야 합니다.");
        assertEquals(0, weapons.size(), "Weapons Set에 총알이 추가되지 않아야 합니다.");
    }
}