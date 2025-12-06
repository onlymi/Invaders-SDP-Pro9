package screen;

import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import engine.Core;
import engine.DrawManager;
import engine.EnemyManager;
import engine.GameSettings;
import engine.GameState;
import engine.InputManager;
import engine.SoundManager;
import engine.utils.Cooldown;
import entity.EnemyShip;
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

class GameScreenCollisionTest {
    
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
        
        int[] dummyKeys = {0, 0, 0, 0, 0};
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
        gameScreen.update();
        
        //플레이어 생명 감소 호출 확인
        verify(mockGameState, times(1)).decLife(0);
    }
}