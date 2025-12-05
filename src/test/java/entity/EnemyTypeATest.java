package entity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

import engine.AssetManager.SpriteType;
import engine.Core;
import engine.SoundManager;
import engine.utils.Cooldown;
import entity.character.GameCharacter;
import java.util.HashSet;
import java.util.Set;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.MockitoAnnotations;

class EnemyTypeATest {
    
    private EnemyTypeA enemyA;
    private MockedStatic<Core> coreMock;
    private MockedStatic<SoundManager> soundManagerMock;
    
    @Mock
    private Cooldown mockCooldown;
    @Mock
    private GameCharacter mockPlayer;
    
    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        
        coreMock = mockStatic(Core.class);
        soundManagerMock = mockStatic(SoundManager.class);
        
        coreMock.when(() -> Core.getCooldown(anyInt())).thenReturn(mockCooldown);
        when(mockCooldown.checkFinished()).thenReturn(true);
        
        // 기본 생성
        enemyA = new EnemyTypeA(100, 100, SpriteType.EnemyA_Move);
    }
    
    @AfterEach
    void tearDown() {
        coreMock.close();
        soundManagerMock.close();
    }
    
    @Test
    void testCustomHealth() {
        // 체력을 100으로 변경
        enemyA.health = 100;
        enemyA.initialHealth = 100;
        
        // 10 데미지 피격
        enemyA.hit(10);
        
        // 검증
        assertEquals(90, enemyA.getHealth());
        assertEquals(100, enemyA.getInitialHealth());
    }
    
    @Test
    void testCustomAttackDamage() {
        enemyA.setAttackDamage(50);
        
        Set<Weapon> weapons = new HashSet<>();
        when(mockPlayer.getPositionX()).thenReturn(100);
        when(mockPlayer.getPositionY()).thenReturn(100);
        when(mockPlayer.isDestroyed()).thenReturn(false); // 플레이어 생존
        
        enemyA.tryAttack(mockPlayer, weapons);
        
        // 생성된 무기 확인
        assertEquals(1, weapons.size());
        Weapon weapon = weapons.iterator().next();
        
        // 무기의 데미지가 50인지 검증
        assertEquals(50, weapon.getDamage());
    }
    
    @Test
    void testCustomScore() {
        enemyA.pointValue = 500;
        
        assertEquals(500, enemyA.getPointValue());
    }
}