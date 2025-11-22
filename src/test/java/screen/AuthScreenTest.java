package screen;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.anyInt;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

import engine.Core;
import engine.DrawManager;
import engine.InputManager;
import engine.SoundManager;
import engine.renderer.AuthScreenRenderer;
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
 * AuthScreen Test.
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class AuthScreenTest {
    
    // 가짜(Mock) 객체 생성
    @Mock
    private InputManager inputManager;
    @Mock
    private DrawManager drawManager;
    @Mock
    private SoundManager soundManager;
    @Mock
    private AuthScreenRenderer authScreenRenderer;
    
    private AuthScreen authScreen;
    private MockedStatic<Core> coreMock;
    
    private MockedStatic<SoundManager> soundManagerMock;
    
    @BeforeEach
    void setUp() {
        // Core.getter method 호출을 가로챔
        coreMock = mockStatic(Core.class);
        soundManagerMock = mockStatic(SoundManager.class);
        // Core.getter method 가 호출될 때, 가짜 객체를 반환하도록 설정
        coreMock.when(Core::getInputManager).thenReturn(inputManager);
        coreMock.when(Core::getDrawManager).thenReturn(drawManager);
        soundManagerMock.when(SoundManager::getInstance).thenReturn(soundManager);
        coreMock.when(Core::getSoundManager).thenReturn(soundManager);
        coreMock.when(Core::getLogger).thenReturn(java.util.logging.Logger.getAnonymousLogger());
        
        // Cooldown(1000)을 Cooldown(0)으로 바꿔서 inputDelay를 즉시 통과시킴
        coreMock.when(() -> Core.getCooldown(anyInt()))
            .thenReturn(new Cooldown(0));
        
        // drawManager.getAuthScreenRenderer()가 null을 반환하지 않도록 설정
        when(drawManager.getAuthScreenRenderer()).thenReturn(authScreenRenderer);
        
        // 가짜 객체들로 AuthScreen 생성
        authScreen = new AuthScreen(448, 520, 60);
        authScreen.isRunning = true;
    }
    
    @AfterEach
    void tearDown() {
        // Static Mock을 해제
        coreMock.close();
        soundManagerMock.close();
    }
    
    @Test
    void testInitialState() {
        // 1. 테스트: 초기 상태 확인
        // AuthScreen이 생성되면 menuIndex는 0 (Log In)이어야 함
        assertEquals(0, authScreen.getMenuIndex());
        assertTrue(authScreen.getIsRunning());
    }
    
    @Test
    void testMenuNavigation() {
        // 2. 테스트: 메뉴 이동 (아래)
        // inputManager.isKeyDown(VK_DOWN)이 true를 반환하도록 설정
        when(inputManager.isKeyDown(KeyEvent.VK_DOWN)).thenReturn(true);
        
        authScreen.update(); // update() 실행
        
        // menuIndex가 0에서 1 (Sign Up)로 변경되어야 함
        assertEquals(1, authScreen.getMenuIndex());
        
        // 3. 테스트: 메뉴 이동
        // 키를 뗐다가 다시 누름
        when(inputManager.isKeyDown(KeyEvent.VK_DOWN)).thenReturn(false);
        authScreen.update(); // 키 떼기
        when(inputManager.isKeyDown(KeyEvent.VK_DOWN)).thenReturn(true);
        authScreen.update(); // 다시 누름
        // menuIndex가 1에서 2 (Exit)
        assertEquals(2, authScreen.getMenuIndex());
        
        when(inputManager.isKeyDown(KeyEvent.VK_DOWN)).thenReturn(false);
        authScreen.update(); // 키 떼기
        when(inputManager.isKeyDown(KeyEvent.VK_DOWN)).thenReturn(true);
        authScreen.update(); // 다시 누름
        // menuIndex가 2에서 0 (Log In)
        assertEquals(0, authScreen.getMenuIndex());
        
        // 4. 테스트: 메뉴 이동 (위)
        when(inputManager.isKeyDown(KeyEvent.VK_DOWN)).thenReturn(false);
        when(inputManager.isKeyDown(KeyEvent.VK_UP)).thenReturn(true);
        authScreen.update();
        
        // menuIndex가 0에서 2 (Exit)
        assertEquals(2, authScreen.getMenuIndex());
    }
    
    @Test
    void testSelectLogIn() {
        // 5. 테스트: 'Log In' 선택
        // 초기 상태(menuIndex = 0)에서 스페이스바를 누름
        when(inputManager.isKeyDown(KeyEvent.VK_SPACE)).thenReturn(true);
        
        authScreen.update(); // update() 실행
        
        // returnCode가 11이 되고, 화면이 종료(isRunning=false)되어야 함
        assertEquals(11, authScreen.getReturnCode());
        assertFalse(authScreen.getIsRunning());
    }
    
    @Test
    void testSelectSignUp() {
        // 6. 테스트: 'Sign Up' 선택
        // 6.1. 먼저 'Sign Up'으로 이동
        when(inputManager.isKeyDown(KeyEvent.VK_DOWN)).thenReturn(true);
        authScreen.update();
        assertEquals(1, authScreen.getMenuIndex()); // Sign Up으로 이동 확인
        
        // 6.2. 스페이스바 누름
        when(inputManager.isKeyDown(KeyEvent.VK_DOWN)).thenReturn(false);
        when(inputManager.isKeyDown(KeyEvent.VK_SPACE)).thenReturn(true);
        authScreen.update();
        
        // returnCode가 10이 되고, 화면이 종료(isRunning=false)되어야 함
        assertEquals(10, authScreen.getReturnCode());
        assertFalse(authScreen.getIsRunning());
    }
    
    @Test
    void testSelectExit() {
        // 7. 테스트: 'Exit' 선택
        // 7.1. Exit 메뉴(인덱스 2)로 이동
        when(inputManager.isKeyDown(KeyEvent.VK_DOWN)).thenReturn(true);
        authScreen.update(); // 0 -> 1
        when(inputManager.isKeyDown(KeyEvent.VK_DOWN)).thenReturn(false);
        authScreen.update();
        when(inputManager.isKeyDown(KeyEvent.VK_DOWN)).thenReturn(true);
        authScreen.update(); // 1 -> 2
        
        assertEquals(2, authScreen.getMenuIndex());
        
        // 7.2. 스페이스바 누름 (선택)
        when(inputManager.isKeyDown(KeyEvent.VK_DOWN)).thenReturn(false);
        when(inputManager.isKeyDown(KeyEvent.VK_SPACE)).thenReturn(true);
        authScreen.update();
        
        // 7.3. 검증: returnCode는 0(종료)이어야 함
        assertEquals(0, authScreen.getReturnCode());
        assertFalse(authScreen.getIsRunning());
    }
}