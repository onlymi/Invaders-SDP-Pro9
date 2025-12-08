package screen;


import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import engine.Core;
import engine.DrawManager;
import engine.FileManager;
import engine.InputManager;
import engine.SoundManager;
import engine.UserStats;
import engine.renderer.StoreScreenRenderer;
import engine.utils.Cooldown;
import java.awt.event.KeyEvent;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

/**
 * StoreScreen Test.
 */

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class StoreScreenTest {
    
    @Mock
    private InputManager inputManager;
    @Mock
    private DrawManager drawManager;
    @Mock
    private SoundManager soundManager;
    @Mock
    private FileManager fileManager;
    @Mock
    private StoreScreenRenderer storeScreenRenderer;
    @Mock
    private Cooldown mockCooldown;
    
    private MockedStatic<Core> coreMock;
    private MockedStatic<InputManager> inputManagerStaticMock;
    private MockedStatic<SoundManager> soundManagerMock;
    private MockedStatic<FileManager> fileManagerMock;
    
    private StoreScreen storeScreen;
    private UserStats testUserStats;
    
    @BeforeEach
    void setUp() {
        // Core 및 Static Mock 설정
        coreMock = mockStatic(Core.class);
        inputManagerStaticMock = mockStatic(InputManager.class);
        soundManagerMock = mockStatic(SoundManager.class);
        fileManagerMock = mockStatic(FileManager.class);
        
        // 테스트용 UserStats 생성 (코인 1000으로 시작)
        testUserStats = new UserStats("testPlayer");
        testUserStats.setCoin(1000);
        
        // Core 의존성 Mocking
        coreMock.when(Core::getInputManager).thenReturn(inputManager);
        coreMock.when(Core::getDrawManager).thenReturn(drawManager);
        coreMock.when(Core::getSoundManager).thenReturn(soundManager);
        coreMock.when(Core::getFileManager).thenReturn(fileManager);
        coreMock.when(Core::getUserStats).thenReturn(testUserStats); // UserStats 주입
        coreMock.when(Core::getLogger).thenReturn(java.util.logging.Logger.getAnonymousLogger());
        
        // Cooldown 즉시 완료 설정
        coreMock.when(() -> Core.getCooldown(anyInt())).thenReturn(mockCooldown);
        when(mockCooldown.checkFinished()).thenReturn(true);
        
        // Singleton Mocking
        inputManagerStaticMock.when(InputManager::getInstance).thenReturn(inputManager);
        soundManagerMock.when(SoundManager::getInstance).thenReturn(soundManager);
        fileManagerMock.when(FileManager::getInstance).thenReturn(fileManager);
        
        // Renderer Mocking
        when(drawManager.getStoreScreenRenderer()).thenReturn(storeScreenRenderer);
        
        // StoreScreen 생성
        storeScreen = new StoreScreen(800, 600, 60);
        storeScreen.isRunning = true;
    }
    
    @AfterEach
    void tearDown() {
        coreMock.close();
        inputManagerStaticMock.close();
        soundManagerMock.close();
        fileManagerMock.close();
    }
    
    // 메뉴 인덱스를 확인하기 위한 Helper 메소드 (Reflection 사용)
    private int getMenuIndex() throws Exception {
        java.lang.reflect.Field field = StoreScreen.class.getDeclaredField("menuIndex");
        field.setAccessible(true);
        return (int) field.get(storeScreen);
    }
    
    @Test
    void testInitialState() {
        assertEquals(1, storeScreen.getReturnCode());
        assertTrue(storeScreen.getIsRunning());
    }
    
    @Test
    void testNavigation() throws Exception {
        // 초기 인덱스 0
        assertEquals(0, getMenuIndex());
        
        // 오른쪽 화살표 키 입력 -> 인덱스 1
        when(inputManager.isKeyDown(KeyEvent.VK_RIGHT)).thenReturn(true);
        storeScreen.update();
        when(inputManager.isKeyDown(KeyEvent.VK_RIGHT)).thenReturn(false);
        assertEquals(1, getMenuIndex());
        
        // 아래쪽 화살표 키 입력 -> 인덱스 5
        when(inputManager.isKeyDown(KeyEvent.VK_DOWN)).thenReturn(true);
        storeScreen.update();
        when(inputManager.isKeyDown(KeyEvent.VK_DOWN)).thenReturn(false);
        assertEquals(5, getMenuIndex());
        
        // D키 입력 -> 인덱스 6
        when(inputManager.isKeyDown(KeyEvent.VK_D)).thenReturn(true);
        storeScreen.update();
        when(inputManager.isKeyDown(KeyEvent.VK_D)).thenReturn(false);
        assertEquals(6, getMenuIndex());
        
        // S키 입력 -> 인덱스 2
        when(inputManager.isKeyDown(KeyEvent.VK_S)).thenReturn(true);
        storeScreen.update();
        when(inputManager.isKeyDown(KeyEvent.VK_S)).thenReturn(false);
        assertEquals(2, getMenuIndex());
        
        // 왼쪽 화살표 키 입력 -> 인덱스 1
        when(inputManager.isKeyDown(KeyEvent.VK_LEFT)).thenReturn(true);
        storeScreen.update();
        when(inputManager.isKeyDown(KeyEvent.VK_LEFT)).thenReturn(false);
        assertEquals(1, getMenuIndex());
        
        // 위쪽 화살표 키 입력 -> 인덱스 5
        when(inputManager.isKeyDown(KeyEvent.VK_UP)).thenReturn(true);
        storeScreen.update();
        when(inputManager.isKeyDown(KeyEvent.VK_UP)).thenReturn(false);
        assertEquals(5, getMenuIndex());
        
        // A키 입력 -> 인덱스 0
        when(inputManager.isKeyDown(KeyEvent.VK_A)).thenReturn(true);
        storeScreen.update();
        when(inputManager.isKeyDown(KeyEvent.VK_A)).thenReturn(false);
        assertEquals(4, getMenuIndex());
        
        // W키 입력 -> 인덱스
        when(inputManager.isKeyDown(KeyEvent.VK_W)).thenReturn(true);
        storeScreen.update();
        when(inputManager.isKeyDown(KeyEvent.VK_W)).thenReturn(false);
        assertEquals(0, getMenuIndex());
    }
    
    @Test
    void testPurchaseSuccess() throws Exception {
        // 초기 코인 1000, 레벨 0, 비용 100
        assertEquals(0, testUserStats.getStatLevel(0));
        
        // 구매 시도
        when(inputManager.isKeyDown(KeyEvent.VK_SPACE)).thenReturn(true);
        storeScreen.update();
        when(inputManager.isKeyDown(KeyEvent.VK_SPACE)).thenReturn(false);
        
        // 검증
        // 1. 코인 차감 테스트
        assertEquals(900, testUserStats.getCoin());
        // 2. 레벨 상승 테스트
        assertEquals(1, testUserStats.getStatLevel(0));
        // 3. 파일 저장 테스트
        verify(fileManager).saveUserStats(testUserStats);
    }
    
    @Test
    void testPurchaseFailure_InsufficientCoins() throws Exception {
        // 코인을 0으로 설정
        testUserStats.setCoin(0);
        
        // 구매 시도
        when(inputManager.isKeyDown(KeyEvent.VK_SPACE)).thenReturn(true);
        storeScreen.update();
        when(inputManager.isKeyDown(KeyEvent.VK_SPACE)).thenReturn(false);
        
        // 검증
        assertEquals(0, testUserStats.getCoin()); // 코인 변동 없음
        assertEquals(0, testUserStats.getStatLevel(0)); // 레벨 변동 없음
        verify(fileManager, never()).saveUserStats(any()); // 저장 호출 안 됨
    }
    
    @Test
    void testPurchaseFailure_MaxLevel() throws Exception {
        // 레벨을 최대로 설정
        for (int i = 0; i < 3; i++) {
            testUserStats.upgradeStat(0);
        }
        assertEquals(3, testUserStats.getStatLevel(0));
        
        // 구매 시도
        when(inputManager.isKeyDown(KeyEvent.VK_SPACE)).thenReturn(true);
        storeScreen.update();
        
        // 검증
        assertEquals(1000, testUserStats.getCoin()); // 코인 변동 없음
        assertEquals(3, testUserStats.getStatLevel(0)); // 레벨 유지
        verify(fileManager, never()).saveUserStats(any()); // 저장 호출 안 됨
    }
    
    @Test
    void testExit() {
        // ESC 입력
        when(inputManager.isKeyDown(KeyEvent.VK_ESCAPE)).thenReturn(true);
        storeScreen.update();
        
        // TitleScreen(1)으로 돌아가며 종료
        assertEquals(1, storeScreen.getReturnCode());
        assertFalse(storeScreen.getIsRunning());
    }
}
