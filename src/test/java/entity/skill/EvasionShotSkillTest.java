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
        
        // Core Static 메서드 Mocking (getCooldown, getLogger 등)
        coreMock = mockStatic(Core.class);
        coreMock.when(() -> Core.getCooldown(anyInt())).thenReturn(cooldown);
        coreMock.when(Core::getLogger).thenReturn(java.util.logging.Logger.getGlobal());
        
        // EvasionShotSkill 인스턴스 생성
        evasionShotSkill = new EvasionShotSkill();
    }
    
    @AfterEach
    void tearDown() {
        coreMock.close();
    }
    
    @Test
    void testConstructorProperties() {
        // 스킬 기본 속성 검증
        assertEquals("Evasion Shot", evasionShotSkill.getName());
        assertEquals(25, evasionShotSkill.getManaCost());
    }
    
    @Test
    void testDoJump_RightBoundaryCheck() {
        // Given: 캐릭터가 오른쪽을 보고 있을 때 (회피 사격은 뒤로 이동하므로 왼쪽으로 이동해야 함)
        // 현재 위치 (100, 100)
        when(attacker.getPositionX()).thenReturn(100);
        when(attacker.getPositionY()).thenReturn(100);
        
        when(attacker.isFacingRight()).thenReturn(true);
        when(attacker.isFacingLeft()).thenReturn(false);
        when(attacker.isFacingFront()).thenReturn(false);
        when(attacker.isFacingBack()).thenReturn(false);
        
        // When: 점프 수행
        evasionShotSkill.doJump(attacker);
        
        // Then: 왼쪽으로 150만큼 이동해야 함. (100 - 150 = -50)
        // 화면 경계(0)보다 작으므로 0으로 보정되어야 함.
        verify(attacker).setPositionX(0);
    }
    
    @Test
    void testDoJump_LeftNormal() {
        // Given: 캐릭터가 왼쪽을 보고 있을 때 (오른쪽으로 회피)
        // 현재 위치 (200, 100)
        when(attacker.getPositionX()).thenReturn(200);
        when(attacker.getPositionY()).thenReturn(100);
        
        when(attacker.isFacingLeft()).thenReturn(true);
        when(attacker.isFacingRight()).thenReturn(false);
        
        // When: 점프 수행
        evasionShotSkill.doJump(attacker);
        
        // Then: 오른쪽으로 150만큼 이동 (200 + 150 = 350)
        verify(attacker).setPositionX(350);
    }
    
    @Test
    void testDoJump_Diagonal() {
        // Given: 오른쪽(Right) + 아래쪽(Front)을 보고 있을 때 (왼쪽 위로 회피)
        // 현재 위치 (500, 500)
        when(attacker.getPositionX()).thenReturn(500);
        when(attacker.getPositionY()).thenReturn(500);
        
        when(attacker.isFacingRight()).thenReturn(true);
        when(attacker.isFacingFront()).thenReturn(true);
        
        // When: 점프 수행
        evasionShotSkill.doJump(attacker);
        
        // Then: 대각선 이동 보정 적용
        // dx = -1, dy = -1
        // moveAmount = 150 * DIAGONAL_CORRECTION_FACTOR(약 0.707) ≈ 106
        // Expected X = 500 - 106 = 394
        // Expected Y = 500 - 106 = 394
        
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
        stats.physicalDamage = 10; // 기본 데미지 10
        
        // Attacker Mock 설정
        when(attacker.getCurrentStats()).thenReturn(stats);
        when(attacker.createWeapon(any())).thenReturn(mockWeapon); // 무기 생성 시 Mock 반환
        
        // Attacker 위치 및 방향 설정 (무기 생성 위치 계산용)
        when(attacker.getPositionX()).thenReturn(100);
        when(attacker.getPositionY()).thenReturn(100);
        when(attacker.getWidth()).thenReturn(50);
        when(attacker.getHeight()).thenReturn(50);
        when(attacker.isFacingRight()).thenReturn(true);
        
        // Weapon Mock 설정
        when(mockWeapon.getWidth()).thenReturn(10);
        when(mockWeapon.getHeight()).thenReturn(10);
        when(mockWeapon.getSpeed()).thenReturn(5);
        
        // When: 스킬 시전
        evasionShotSkill.performSkill(attacker, weapons);
        
        // Then
        // 1. 이동(doJump) 확인: 오른쪽을 보므로 왼쪽(0 방향)으로 이동 시도
        verify(attacker).setPositionX(0);
        
        // 2. 투사체 속성 변경 확인
        // 데미지 1.5배: 10 * 1.5 = 15
        verify(mockWeapon).setDamage(15);
        // 속도 2배: 5 * 2 = 10
        verify(mockWeapon).setSpeed(10);
        // 스프라이트 변경
        verify(mockWeapon).setSpriteImage(SpriteType.CharacterArcherSecondSkill);
        
        // 3. 투사체 위치 설정 확인 (캐릭터 오른쪽 끝)
        // X: 100 + 50 = 150
        // Y: 100 + (50 - 10) / 2 = 120
        verify(mockWeapon).setPositionX(150);
        verify(mockWeapon).setPositionY(120);
        
        // 4. 스턴 적용 확인 (0.5초 = 500ms)
        verify(attacker).stun(500);
    }
}