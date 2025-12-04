package entity.character;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

import engine.AssetManager.SpriteType;
import engine.Core;
import engine.UserStats;
import engine.utils.Cooldown;
import entity.Entity.Team;
import entity.Weapon;
import entity.WeaponPool;
import java.lang.reflect.Field;
import java.util.HashSet;
import java.util.Set;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.MockitoAnnotations;

class ArcherCharacterAttackTest {
    
    private MockedStatic<Core> coreMock;
    
    @Mock
    private UserStats userStats;
    @Mock
    private Cooldown mockCooldown;
    
    private ArcherCharacter archer;
    private final int START_X = 100;
    private final int START_Y = 500;
    private final int PLAYER_ID = 1;
    
    @BeforeEach
    void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);
        
        // WeaponPool 초기화
        Field poolField = WeaponPool.class.getDeclaredField("pool");
        poolField.setAccessible(true);
        ((Set<?>) poolField.get(null)).clear();
        
        // Core Mocking
        coreMock = mockStatic(Core.class);
        coreMock.when(() -> Core.getCooldown(anyInt())).thenReturn(mockCooldown);
        coreMock.when(Core::getUserStats).thenReturn(userStats);
        
        archer = new ArcherCharacter(START_X, START_Y, Team.PLAYER1, PLAYER_ID);
    }
    
    @AfterEach
    void tearDown() {
        coreMock.close();
    }
    
    @Test
    void testArcherProjectileProperties() {
        // Given: 쿨다운이 완료된 상태
        Set<Weapon> weapons = new HashSet<>();
        when(mockCooldown.checkFinished()).thenReturn(true);
        
        // When: 공격 실행
        archer.launchBasicAttack(weapons);
        boolean isFired = archer.isAttacking;
        
        // Then: 발사된 투사체의 속성이 아처 전용 설정과 일치하는지 검증
        assertTrue(isFired, "공격이 발사되어야 합니다.");
        assertEquals(1, weapons.size());
        
        Weapon weapon = weapons.iterator().next();
        
        // ArcherCharacter 생성자에서 설정된 투사체 속성 검증
        // this.projectileSpriteType = SpriteType.CharacterArcherDefaultProjectile;
        assertEquals(SpriteType.CharacterArcherDefaultProjectile, weapon.getSpriteType(),
            "투사체 이미지가 아처 전용 화살이어야 합니다.");
        
        // ArcherCharacter 생성자 로직: projectileSpeed = (int) (attackSpeed * 10)
        // 기본 attackSpeed가 1.5f 이므로 15가 되어야 함 (CharacterType.ARCHER 기준)
        int expectedSpeed = (int) (archer.getCurrentStats().attackSpeed * 10);
        assertEquals(expectedSpeed, weapon.getSpeed(),
            "투사체 속도가 스탯에 비례하여 올바르게 설정되어야 합니다.");
        
        // 투사체 크기 검증
        assertEquals(SpriteType.CharacterArcherDefaultProjectile.getWidth(), weapon.getWidth());
        assertEquals(SpriteType.CharacterArcherDefaultProjectile.getHeight(), weapon.getHeight());
    }
}