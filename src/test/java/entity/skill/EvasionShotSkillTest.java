package entity.skill;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import engine.AssetManager.SpriteType;
import engine.Core;
import engine.utils.Cooldown;
import entity.Weapon;
import entity.character.CharacterStats;
import entity.character.GameCharacter;
import java.util.HashSet;
import java.util.Set;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.MockitoAnnotations;

class EvasionShotSkillTest {
    
    @Mock
    private GameCharacter attacker;
    @Mock
    private Cooldown cooldown;
    @Mock
    private Weapon mockWeapon;
    
    private MockedStatic<Core> coreMock;
    private EvasionShotSkill evasionShotSkill;
    
    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        coreMock = mockStatic(Core.class);
        coreMock.when(() -> Core.getCooldown(anyInt())).thenReturn(cooldown);
        coreMock.when(Core::getLogger).thenReturn(java.util.logging.Logger.getGlobal());
        
        evasionShotSkill = new EvasionShotSkill();
    }
    
    @AfterEach
    void tearDown() {
        coreMock.close();
    }
    
    @Test
    void testConstructorProperties() {
        assertEquals("Evasion Shot", evasionShotSkill.getName());
        // [수정] 코드에 설정된 MANA_COST = 25 반영
        assertEquals(25, evasionShotSkill.getManaCost());
    }
    
    @Test
    void testDoJump_RightBoundaryCheck() {
        // Given: (100, 100)에서 오른쪽 봄 -> 왼쪽(Backstep)으로 150 이동 시도 -> -50
        when(attacker.getPositionX()).thenReturn(100);
        when(attacker.getPositionY()).thenReturn(100);
        
        when(attacker.isFacingRight()).thenReturn(true);
        when(attacker.isFacingLeft()).thenReturn(false);
        when(attacker.isFacingFront()).thenReturn(false);
        when(attacker.isFacingBack()).thenReturn(false);
        
        // When
        evasionShotSkill.doJump(attacker);
        
        // Then: -50은 1보다 작으므로 1로 보정되어야 함 (코드: if (newX < 1) newX = 1)
        verify(attacker).setPositionX(1);
    }
    
    @Test
    void testDoJump_BottomBoundaryCheck() {
        // Given: 아래쪽 경계 테스트
        // Core.HEIGHT = 800 (가정), 캐릭터 높이 50
        // 경계값: 800 - 50 - 30 = 720
        // 현재 Y=700, 뒤(Back/위)를 봄 -> 앞(Front/아래)으로 Backstep (+150) -> 850
        
        when(attacker.getPositionX()).thenReturn(100);
        when(attacker.getPositionY()).thenReturn(700);
        when(attacker.getHeight()).thenReturn(50);
        
        when(attacker.isFacingBack()).thenReturn(true); // 위를 봄 -> 아래로 점프
        when(attacker.isFacingFront()).thenReturn(false);
        
        // When
        evasionShotSkill.doJump(attacker);
        
        // Then: 850 > 720 이므로 720으로 보정되어야 함
        int expectedY = Core.HEIGHT - 50 - 30; // 720
        verify(attacker).setPositionY(expectedY);
    }
    
    @Test
    void testDoJump_Diagonal() {
        // Given: (500, 500)에서 오른쪽+아래 -> 왼쪽+위(-1, -1)로 이동
        when(attacker.getPositionX()).thenReturn(500);
        when(attacker.getPositionY()).thenReturn(500);
        
        when(attacker.isFacingRight()).thenReturn(true);
        when(attacker.isFacingFront()).thenReturn(true);
        
        // When
        evasionShotSkill.doJump(attacker);
        
        // Then: 대각선 보정 적용 확인
        int jumpDist = 150;
        int diagonalMove = (int) (jumpDist * GameCharacter.DIAGONAL_CORRECTION_FACTOR);
        
        verify(attacker).setPositionX(500 - diagonalMove);
        verify(attacker).setPositionY(500 - diagonalMove);
    }
    
    @Test
    void testPerformSkill_ActivatesEffects() {
        // Given
        Set<Weapon> weapons = new HashSet<>();
        CharacterStats stats = new CharacterStats();
        stats.physicalDamage = 10;
        
        when(attacker.getCurrentStats()).thenReturn(stats);
        when(attacker.createWeapon(any())).thenReturn(mockWeapon);
        
        // Mock 위치 설정
        when(attacker.getPositionX()).thenReturn(100);
        when(attacker.getPositionY()).thenReturn(100);
        when(attacker.getWidth()).thenReturn(50);
        when(attacker.getHeight()).thenReturn(50);
        when(attacker.isFacingRight()).thenReturn(true);
        
        when(mockWeapon.getWidth()).thenReturn(10);
        when(mockWeapon.getHeight()).thenReturn(10);
        when(mockWeapon.getSpeed()).thenReturn(5);
        
        // When
        evasionShotSkill.performSkill(attacker, weapons);
        
        // Then
        
        // 1. 이동 확인 (오른쪽 봄 -> 왼쪽 이동 -> -50 -> 1로 보정)
        verify(attacker).setPositionX(1);
        
        // 2. 무기 효과 확인
        verify(mockWeapon).setDamage(15); // 10 * 1.5
        verify(mockWeapon).setSpeed(10);  // 5 * 2
        verify(mockWeapon).setSpriteImage(SpriteType.CharacterArcherSecondSkill);
        
        // 3. 무기 위치 확인
        // 주의: Mock 객체(attacker)는 doJump 호출 후에도 getPositionX() 값이 100으로 유지됨.
        // performSkill 코드: charX(100) + charW(50) = 150
        verify(mockWeapon).setPositionX(150);
        
        // 4. 스턴 확인 (0.5초)
        verify(attacker).stun(500);
    }
}