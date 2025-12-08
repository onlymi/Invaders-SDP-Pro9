package screen;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import engine.Core;
import engine.DrawManager;
import engine.FileManager;
import engine.GameState;
import engine.InputManager;
import engine.SoundManager;
import engine.UserStats;
import engine.gameplay.achievement.AchievementManager;
import engine.renderer.ScoreScreenRenderer;
import engine.utils.Cooldown;
import java.util.Collections;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class ScoreScreenTest {
    
    @Mock
    private InputManager inputManager;
    @Mock
    private DrawManager drawManager;
    @Mock
    private FileManager fileManager;
    @Mock
    private SoundManager soundManager;
    @Mock
    private GameState gameState;
    @Mock
    private AchievementManager achievementManager;
    @Mock
    private UserStats userStats;
    @Mock
    private ScoreScreenRenderer scoreScreenRenderer;
    @Mock
    private Cooldown cooldown;
    
    private MockedStatic<Core> coreMock;
    private MockedStatic<InputManager> inputManagerMock;
    private MockedStatic<SoundManager> soundManagerMock;
    
    @BeforeEach
    void setUp() throws Exception {
        // Static Mocking
        coreMock = mockStatic(Core.class);
        inputManagerMock = mockStatic(InputManager.class);
        soundManagerMock = mockStatic(SoundManager.class); // 추가
        
        // Core Dependencies
        coreMock.when(Core::getInputManager).thenReturn(inputManager);
        coreMock.when(Core::getDrawManager).thenReturn(drawManager);
        coreMock.when(Core::getFileManager).thenReturn(fileManager);
        coreMock.when(Core::getSoundManager).thenReturn(soundManager); // SoundManager 연결
        coreMock.when(Core::getUserStats).thenReturn(userStats);
        coreMock.when(Core::getLogger).thenReturn(java.util.logging.Logger.getAnonymousLogger());
        coreMock.when(() -> Core.getCooldown(anyInt())).thenReturn(cooldown);
        
        // SoundManager Singleton Mocking (ScoreScreen 내부에서 SoundManager.playOnce 호출 등 대비)
        soundManagerMock.when(SoundManager::getInstance).thenReturn(soundManager);
        
        // InputManager Singleton Mocking
        inputManagerMock.when(InputManager::getInstance).thenReturn(inputManager);
        
        // DrawManager Renderer Mocking
        when(drawManager.getScoreScreenRenderer()).thenReturn(scoreScreenRenderer);
        
        // FileManager HighScores Mocking (생성자에서 호출됨)
        when(fileManager.loadHighScores(any())).thenReturn(Collections.emptyList());
    }
    
    @AfterEach
    void tearDown() {
        coreMock.close();
        inputManagerMock.close();
        soundManagerMock.close();
    }
    
    @Test
    void testCoinsSavedOnScoreScreenCreation() throws Exception {
        // 게임에서 500 코인을 획득함
        when(gameState.getCoins()).thenReturn(500);
        when(gameState.getCoop()).thenReturn(false);
        
        // ScoreScreen이 생성될 때 (게임 종료)
        new ScoreScreen(800, 600, 60, gameState, achievementManager);
        
        // UserStats에 코인이 추가되어야 함
        verify(userStats).addCoin(500);
        // FileManager를 통해 파일 저장이 호출되어야 함
        verify(fileManager).saveUserStats(userStats);
    }
    
    @Test
    void testNoSaveIfNoCoinsEarned() throws Exception {
        // 코인 획득 0
        when(gameState.getCoins()).thenReturn(0);
        when(gameState.getCoop()).thenReturn(false);
        
        new ScoreScreen(800, 600, 60, gameState, achievementManager);
        
        // 0원이면 저장 로직을 타지 않음
        verify(fileManager, never()).saveUserStats(any());
    }
}