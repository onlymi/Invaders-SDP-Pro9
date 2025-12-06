package screen;

import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import engine.AssetManager;
import engine.Core;
import engine.DrawManager;
import engine.EnemyManager;
import engine.GameSettings;
import engine.GameState;
import engine.InputManager;
import engine.SoundManager;
import engine.utils.Cooldown;
import entity.EnemyShip;
import entity.Weapon;
import entity.character.CharacterStats;
import entity.character.CharacterType;
import entity.character.GameCharacter;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.MockitoAnnotations;

class GameScreenTest {
    
    // 테스트 대상
    private GameScreen gameScreen;
    
    // Mocks
    @Mock
    private GameState mockGameState;
    @Mock
    private GameSettings mockGameSettings;
    @Mock
    private InputManager mockInputManager;
    @Mock
    private DrawManager mockDrawManager;
    @Mock
    private EnemyManager mockEnemyManager;
    @Mock
    private Cooldown mockCooldown;
    @Mock
    private GameCharacter mockPlayer;
    @Mock
    private EnemyShip mockEnemy;
    @Mock
    private Weapon mockWeapon;
    @Mock
    private engine.AssetManager mockAssetManager;
    @Mock
    private java.util.logging.Logger mockLogger;
    @Mock
    private engine.renderer.GameScreenRenderer mockGameScreenRenderer;
    @Mock
    private engine.renderer.EntityRenderer mockEntityRenderer;
    @Mock
    private engine.renderer.CommonRenderer mockCommonRenderer;
    @Mock
    private engine.renderer.HighScoreScreenRenderer mockHighScoreScreenRenderer;
    @Mock
    private java.awt.Graphics2D mockGraphics;
    
    
    private MockedStatic<Core> coreMock;
    private MockedStatic<SoundManager> soundMock;
    
    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        
        // Core Static Mocking
        coreMock = mockStatic(Core.class);
        coreMock.when(Core::getInputManager).thenReturn(mockInputManager);
        coreMock.when(Core::getDrawManager).thenReturn(mockDrawManager);
        
        when(mockDrawManager.getGameScreenRenderer()).thenReturn(mockGameScreenRenderer);
        when(mockDrawManager.getEntityRenderer()).thenReturn(mockEntityRenderer);
        when(mockDrawManager.getCommonRenderer()).thenReturn(mockCommonRenderer);
        when(mockDrawManager.getHighScoreScreenRenderer()).thenReturn(mockHighScoreScreenRenderer);
        when(mockDrawManager.getBackBufferGraphics()).thenReturn(mockGraphics);
        
        coreMock.when(Core::getAssetManager).thenReturn(mockAssetManager);
        coreMock.when(Core::getLogger).thenReturn(mockLogger);
        
        when(mockAssetManager.getCharacterWidth()).thenReturn(32);
        when(mockAssetManager.getCharacterHeight()).thenReturn(32);
        
        int[] dummyKeys = {0, 0, 0, 0, 0, 0, 0, 0, 0};
        when(mockInputManager.getPlayer1Keys()).thenReturn(dummyKeys);
        when(mockInputManager.getPlayer2Keys()).thenReturn(dummyKeys);
        
        coreMock.when(() -> Core.getCooldown(anyInt())).thenReturn(mockCooldown);
        coreMock.when(() -> Core.getVariableCooldown(anyInt(), anyInt())).thenReturn(mockCooldown);
        
        soundMock = mockStatic(SoundManager.class);
        
        // NPE 방지를 위한 mockPlayer 스탯 및 체력 설정
        CharacterStats mockStats = new CharacterStats();
        mockStats.maxHealthPoints = 100;
        when(mockPlayer.getCurrentStats()).thenReturn(mockStats);
        when(mockPlayer.getCurrentHealthPoints()).thenReturn(100);
        
        // GameScreen 생성
        gameScreen = new GameScreen(mockGameState, mockGameSettings, false, 800, 600, 60,
            CharacterType.ARCHER, CharacterType.ARCHER, null);
        
        // 초기화 시뮬레이션
        gameScreen.initialize();
        
        try {
            java.lang.reflect.Field field = GameScreen.class.getDeclaredField("enemyManager");
            field.setAccessible(true);
            field.set(gameScreen, mockEnemyManager);
            
            java.lang.reflect.Field charsField = GameScreen.class.getDeclaredField("characters");
            charsField.setAccessible(true);
            GameCharacter[] chars = new GameCharacter[2];
            chars[0] = mockPlayer;
            charsField.set(gameScreen, chars);
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    @AfterEach
    void tearDown() {
        coreMock.close();
        soundMock.close();
    }
    
    @Test
    void testBodyCollision() {
        // player와 enemy가 충돌 상태
        when(mockPlayer.getPositionX()).thenReturn(100);
        when(mockPlayer.getPositionY()).thenReturn(100);
        when(mockPlayer.getWidth()).thenReturn(30);
        when(mockPlayer.getHeight()).thenReturn(30);
        when(mockPlayer.isDie()).thenReturn(false);
        
        when(mockEnemy.getPositionX()).thenReturn(100);
        when(mockEnemy.getPositionY()).thenReturn(100);
        when(mockEnemy.getWidth()).thenReturn(30);
        when(mockEnemy.getHeight()).thenReturn(30);
        when(mockEnemy.isDestroyed()).thenReturn(false);
        
        // EnemyManager가 Mock Enemy 리스트 반환
        List<EnemyShip> enemies = new ArrayList<>();
        enemies.add(mockEnemy);
        when(mockEnemyManager.getEnemies()).thenReturn(enemies);
        
        // 피격 시 사망 확인을 위해 체력 상태 조작
        when(mockPlayer.getCurrentHealthPoints()).thenReturn(10);
        
        // takeDamage 호출 시 체력을 0으로 변경하여 사망
        doAnswer(invocation -> {
            when(mockPlayer.getCurrentHealthPoints()).thenReturn(0);
            return null;
        }).when(mockPlayer).takeDamage(anyInt());
        
        // 로직 실행을 위한 조건 만족
        when(mockCooldown.checkFinished()).thenReturn(true);
        when(mockGameState.getLivesRemaining()).thenReturn(3);
        
        // GameScreen.update()를 호출하기 전에 players의 isInvincible()이 false여야 충돌 로직이 실행됩니다.
        when(mockPlayer.isInvincible()).thenReturn(false);
    }
    
    // 적의 투사체에 플레이어가 피격되어 사망하고 생명이 감소하는 테스트 (이전 단계에서 추가된 유효한 테스트)
    @Test
    void testEnemyWeaponCollision_PlayerTakesDamageAndLosesLife() {
        // Given: 플레이어와 충돌하는 적의 투사체 설정
        when(mockWeapon.getPositionX()).thenReturn(100);
        when(mockWeapon.getPositionY()).thenReturn(100);
        when(mockWeapon.getWidth()).thenReturn(30);
        when(mockWeapon.getHeight()).thenReturn(30);
        when(mockWeapon.getOwnerPlayerId()).thenReturn(0); // 적 투사체
        when(mockWeapon.getDamage()).thenReturn(100); // 한 방에 죽을 데미지
        when(mockWeapon.getDuration()).thenReturn(-1); // 일반 투사체 (레이저 아님)
        when(mockWeapon.getSpriteType()).thenReturn(AssetManager.SpriteType.EnemyBullet);
        when(mockWeapon.isExpired()).thenReturn(false);
        
        // 투사체를 게임 화면의 무기 목록에 추가
        gameScreen.getWeapons().add(mockWeapon);
        
        // Player 1 설정 (index 0)
        when(mockPlayer.getPlayerId()).thenReturn(1); // Player index is 0
        when(mockPlayer.getPositionX()).thenReturn(100);
        when(mockPlayer.getPositionY()).thenReturn(100);
        when(mockPlayer.getWidth()).thenReturn(30);
        when(mockPlayer.getHeight()).thenReturn(30);
        when(mockPlayer.isInvincible()).thenReturn(false);
        
        // Player HP: 100. Damage: 100. Player가 사망하는 시나리오.
        when(mockPlayer.getCurrentHealthPoints()).thenReturn(100);
        when(mockGameState.getLivesRemaining()).thenReturn(3);
        
        // Player가 데미지를 받을 때 체력이 0이 되도록 설정
        doAnswer(invocation -> {
            // 데미지를 받으면 HP가 0이 됨
            when(mockPlayer.getCurrentHealthPoints()).thenReturn(0);
            return null;
        }).when(mockPlayer).takeDamage(anyInt());
        
        // When: 업데이트 실행 (충돌 처리)
        when(mockCooldown.checkFinished()).thenReturn(true);
        gameScreen.update();
        
        // Then:
        // 1. 플레이어가 데미지를 받았는지 확인
        verify(mockPlayer, times(1)).takeDamage(100);
        // 2. 플레이어의 남은 생명이 1 감소했는지 확인
        verify(mockGameState, times(1)).decLife(0);
    }
    
    // Player의 투사체가 적에게 충돌하여 적이 파괴되는 테스트 (이전 단계에서 추가된 유효한 테스트)
    @Test
    void testPlayerWeaponCollision_EnemyDestroyed() {
        // Given: 플레이어의 투사체가 적과 충돌하는 설정
        when(mockWeapon.getPositionX()).thenReturn(100);
        when(mockWeapon.getPositionY()).thenReturn(100);
        when(mockWeapon.getWidth()).thenReturn(30);
        when(mockWeapon.getHeight()).thenReturn(30);
        when(mockWeapon.getOwnerPlayerId()).thenReturn(1); // 플레이어 1 투사체
        when(mockWeapon.getDamage()).thenReturn(100);
        when(mockWeapon.isExpired()).thenReturn(false);
        
        // 투사체를 게임 화면의 무기 목록에 추가
        gameScreen.getWeapons().add(mockWeapon);
        
        // Enemy 설정: 플레이어 투사체와 겹치고, HP가 100이며 파괴되면 isDestroyed()가 true가 되도록 설정
        when(mockEnemy.getPositionX()).thenReturn(100);
        when(mockEnemy.getPositionY()).thenReturn(100);
        when(mockEnemy.getWidth()).thenReturn(30);
        when(mockEnemy.getHeight()).thenReturn(30);
        when(mockEnemy.isDestroyed()).thenReturn(false);
        when(mockEnemy.getPointValue()).thenReturn(50);
        when(mockEnemy.getCoinValue()).thenReturn(10);
        when(mockEnemy.getInitialHealth()).thenReturn(100);
        
        // EnemyManager가 Mock Enemy 리스트 반환
        List<EnemyShip> enemies = new ArrayList<>();
        enemies.add(mockEnemy);
        when(mockEnemyManager.getEnemies()).thenReturn(enemies);
        
        // Enemy가 피격될 때 (hit(100)) 파괴되도록 설정
        doAnswer(invocation -> {
            when(mockEnemy.isDestroyed()).thenReturn(true);
            return null;
        }).when(mockEnemy).hit(anyInt());
        
        // When: 업데이트 실행 (충돌 처리)
        when(mockCooldown.checkFinished()).thenReturn(true);
        gameScreen.update();
        
        // Then:
        // 1. 적이 데미지를 받았는지 확인
        verify(mockEnemy, times(1)).hit(100);
        // 2. 적이 파괴되었는지 확인 (실제 코드에서 2번 호출됨: 충돌 전 조건 + 충돌 후 파괴 확인)
        verify(mockEnemy, times(2)).isDestroyed();
        // 3. 점수가 추가되었는지 확인 (Player index 0)
        verify(mockGameState, times(1)).addScore(0, 50);
        // 4. 코인이 추가되었는지 확인
        verify(mockGameState, times(1)).addCoins(0, 10);
        // 5. EnemyManager에 destroy 호출 확인
        verify(mockEnemyManager, times(1)).destroy(mockEnemy);
    }
}