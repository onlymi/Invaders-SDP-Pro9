package entity.skill;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import engine.AssetManager.SpriteType;
import engine.Core;
import engine.SoundManager;
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

class PiercingArrowSkillTest {
    
    @Mock
    private GameCharacter attacker;
    @Mock
    private Cooldown cooldown;
    @Mock
    private Weapon mockArrow;
    
    private MockedStatic<Core> coreMock;
    private PiercingArrowSkill skill;
    private MockedStatic<SoundManager> soundManagerMock;
    
    
    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        
        // Core의 정적 메서드 Mocking
        coreMock = mockStatic(Core.class);
        coreMock.when(() -> Core.getCooldown(anyInt())).thenReturn(cooldown);
        coreMock.when(Core::getLogger).thenReturn(java.util.logging.Logger.getGlobal());
        // 화면 크기 Mocking (사거리 계산용)
        coreMock.when(Core::getFrameWidth).thenReturn(800);
        coreMock.when(Core::getFrameHeight).thenReturn(600);
        soundManagerMock = mockStatic(SoundManager.class);
        
        skill = new PiercingArrowSkill();
    }
    
    @AfterEach
    void tearDown() {
        coreMock.close();
        soundManagerMock.close();
    }
    
    @Test
    void testConstructorProperties() {
        // 생성자에서 설정된 값 검증
        assertEquals("Piercing Arrow", skill.getName());
        assertEquals(60, skill.getManaCost());
        // 쿨다운은 Core.getCooldown 호출을 통해 설정되므로, 여기서는 값 확인이 어려울 수 있음 (1.0f * 1000)
    }
    
    @Test
    void testPerformSkill_RightDirection() {
        // Given
        Set<Weapon> weapons = new HashSet<>();
        
        // Attacker Stats 설정
        CharacterStats stats = new CharacterStats();
        stats.physicalDamage = 10;
        when(attacker.getCurrentStats()).thenReturn(stats);
        
        // createWeapon 호출 시 Mock 무기 반환
        when(attacker.createWeapon(any())).thenReturn(mockArrow);
        
        // Attacker 위치 및 크기 설정
        when(attacker.getPositionX()).thenReturn(100);
        when(attacker.getPositionY()).thenReturn(100);
        when(attacker.getWidth()).thenReturn(32);
        when(attacker.getHeight()).thenReturn(32);
        
        // 방향 설정 (오른쪽을 바라봄)
        when(attacker.isFacingRight()).thenReturn(true);
        when(attacker.isFacingLeft()).thenReturn(false);
        when(attacker.isFacingFront()).thenReturn(false);
        when(attacker.isFacingBack()).thenReturn(false);
        
        // Mock Arrow 크기 설정 (setSpriteImage 호출 후 크기 가정)
        // SpriteType.CharacterArcherUltimateSkill의 실제 크기는 76(W) x 256(H) 이지만
        // 테스트 편의를 위해 임의의 값 사용 혹은 로직 검증에 집중
        when(mockArrow.getWidth()).thenReturn(76);
        when(mockArrow.getHeight()).thenReturn(256);
        
        // When
        skill.performSkill(attacker, weapons);
        
        // Then
        // 데미지 검증: 10 * 4.0 = 40
        verify(mockArrow).setDamage(40);
        
        // 사거리 검증: min(800, 600) = 600
        verify(mockArrow).setRange(600);
        
        // 스프라이트 변경 검증
        verify(mockArrow).setSpriteImage(SpriteType.CharacterArcherUltimateSkill);
        
        // 위치 계산 검증
        // - charCenterX = 100 + 16 = 116
        // - charCenterY = 100 + 16 = 116
        // - offsetDist = (32 + 256) / 2 = 144
        // - Facing Right 이므로:
        //   weaponCenterX = 116 + 144 = 260
        //   weaponCenterY = 116
        // - 최종 setPosition:
        //   x = 260 - (76 / 2) = 260 - 38 = 222
        //   y = 116 - (256 / 2) = 116 - 128 = -12
        
        verify(mockArrow).setPositionX(222);
        verify(mockArrow).setPositionY(-12);
    }
}