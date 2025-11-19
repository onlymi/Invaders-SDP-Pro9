package screen;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.anyInt;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import engine.Core;
import engine.DrawManager;
import engine.FileManager;
import engine.InputManager;
import engine.SoundManager;
import engine.renderer.LogInScreenRenderer;
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
 * LogInScreen Test
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class LogInScreenTest {
    
    @Mock
    private InputManager inputManager;
    @Mock
    private DrawManager drawManager;
    @Mock
    private SoundManager soundManager;
    @Mock
    private FileManager fileManager;
    @Mock
    private LogInScreenRenderer logInScreenRenderer;
    @Mock
    private Cooldown mockCooldown;
    
    private MockedStatic<Core> coreMock;
    private MockedStatic<InputManager> inputManagerStaticMock;
    private MockedStatic<SoundManager> soundManagerMock;
    private MockedStatic<FileManager> fileManagerMock;
    
    private LogInScreen logInScreen;
    
    @BeforeEach
    void setUp() throws Exception {
        coreMock = mockStatic(Core.class);
        inputManagerStaticMock = mockStatic(InputManager.class);
        soundManagerMock = mockStatic(SoundManager.class);
        fileManagerMock = mockStatic(FileManager.class);
        
        coreMock.when(Core::getInputManager).thenReturn(inputManager);
        coreMock.when(Core::getDrawManager).thenReturn(drawManager);
        coreMock.when(Core::getFileManager).thenReturn(fileManager);
        coreMock.when(Core::getSoundManager).thenReturn(soundManager);
        coreMock.when(Core::getLogger).thenReturn(java.util.logging.Logger.getAnonymousLogger());
        
        // Cooldown Mock 설정
        coreMock.when(() -> Core.getCooldown(anyInt())).thenReturn(mockCooldown);
        when(mockCooldown.checkFinished()).thenReturn(true);
        
        inputManagerStaticMock.when(InputManager::getInstance).thenReturn(inputManager);
        soundManagerMock.when(SoundManager::getInstance).thenReturn(soundManager);
        fileManagerMock.when(FileManager::getInstance).thenReturn(fileManager);
        
        when(drawManager.getLogInScreenRenderer()).thenReturn(logInScreenRenderer);
        inputManagerStaticMock.when(InputManager::getLastChar).thenReturn('\0');
        
        logInScreen = new LogInScreen(448, 520, 60);
        logInScreen.isRunning = true;
    }
    
    @AfterEach
    void tearDown() {
        coreMock.close();
        inputManagerStaticMock.close();
        soundManagerMock.close();
        fileManagerMock.close();
    }
    
    private String getIdInput() throws Exception {
        java.lang.reflect.Field field = LogInScreen.class.getDeclaredField("idInput");
        field.setAccessible(true);
        return ((StringBuilder) field.get(logInScreen)).toString();
    }
    
    private String getPasswordInput() throws Exception {
        java.lang.reflect.Field field = LogInScreen.class.getDeclaredField("passwordInput");
        field.setAccessible(true);
        return ((StringBuilder) field.get(logInScreen)).toString();
    }
    
    private String getMessage() throws Exception {
        java.lang.reflect.Field field = LogInScreen.class.getDeclaredField("message");
        field.setAccessible(true);
        return (String) field.get(logInScreen);
    }
    
    @Test
    void testInitialState() {   // Test 1. 로그인 화면 실행 확인
        assertTrue(logInScreen.getIsRunning());
    }
    
    @Test
    void testTextInput_ID() throws Exception {  // Test 2. ID 입력 테스트
        inputManagerStaticMock.when(InputManager::getLastChar).thenReturn('u');
        logInScreen.update();
        inputManagerStaticMock.when(InputManager::getLastChar).thenReturn('\0');
        
        assertEquals("u", getIdInput());
    }
    
    @Test
    void testSubmit_Success() throws Exception {    // Test 3. Submit 버튼 테스트
        // 1. ID/PW 입력 시뮬레이션
        inputManagerStaticMock.when(InputManager::getLastChar).thenReturn('u');
        logInScreen.update(); // ID: u
        inputManagerStaticMock.when(InputManager::getLastChar).thenReturn('\0');
        
        when(inputManager.isKeyDown(KeyEvent.VK_DOWN)).thenReturn(true);
        logInScreen.update(); // Move to PW
        when(inputManager.isKeyDown(KeyEvent.VK_DOWN)).thenReturn(false);
        
        inputManagerStaticMock.when(InputManager::getLastChar).thenReturn('p');
        logInScreen.update(); // PW: p
        inputManagerStaticMock.when(InputManager::getLastChar).thenReturn('\0');
        
        // 2. Submit 버튼으로 이동
        when(inputManager.isKeyDown(KeyEvent.VK_DOWN)).thenReturn(true);
        logInScreen.update();
        when(inputManager.isKeyDown(KeyEvent.VK_DOWN)).thenReturn(false);
        
        // 3. FileManager가 SUCCESS를 반환하도록 설정
        when(fileManager.validateUser("u", "p")).thenReturn(FileManager.LoginResult.SUCCESS);
        
        // 4. 제출
        when(inputManager.isKeyDown(KeyEvent.VK_SPACE)).thenReturn(true);
        logInScreen.update();
        
        // 5. 검증
        verify(fileManager).validateUser("u", "p");
        assertEquals("Log In Successful! Starting game...", getMessage());
    }
    
    @Test
    void testSubmit_Failure_IdNotFound() throws Exception {    // Test 4. ID mismatch 및 입력창 초기화 테스트
        // 1. ID/PW 입력 및 제출 위치로 이동
        inputManagerStaticMock.when(InputManager::getLastChar).thenReturn('x'); // ID: x
        logInScreen.update();
        inputManagerStaticMock.when(InputManager::getLastChar).thenReturn('\0');
        
        when(inputManager.isKeyDown(KeyEvent.VK_DOWN)).thenReturn(true);
        logInScreen.update(); // PW
        when(inputManager.isKeyDown(KeyEvent.VK_DOWN)).thenReturn(false);
        
        inputManagerStaticMock.when(InputManager::getLastChar).thenReturn('p'); // PW: p
        logInScreen.update();
        inputManagerStaticMock.when(InputManager::getLastChar).thenReturn('\0');
        
        when(inputManager.isKeyDown(KeyEvent.VK_DOWN)).thenReturn(true);
        logInScreen.update(); // Submit
        when(inputManager.isKeyDown(KeyEvent.VK_DOWN)).thenReturn(false);
        
        // 2. FileManager가 ID_NOT_FOUND 반환
        when(fileManager.validateUser("x", "p")).thenReturn(FileManager.LoginResult.ID_NOT_FOUND);
        
        // 3. 제출
        when(inputManager.isKeyDown(KeyEvent.VK_SPACE)).thenReturn(true);
        logInScreen.update();
        
        // 4. 검증
        assertEquals("This ID does not exist.", getMessage());
        assertEquals("x", getIdInput());    // ID는 유지
        assertEquals("", getPasswordInput());   // password는 초기화
    }
    
    @Test
    void testSubmit_Failure_PasswordMismatch()
        throws Exception {   // Test 5. Password mismatch 및 입력창 초기화 테스트
        // 1. ID/PW 입력
        inputManagerStaticMock.when(InputManager::getLastChar).thenReturn('u');
        logInScreen.update(); // ID: u
        inputManagerStaticMock.when(InputManager::getLastChar).thenReturn('\0');
        
        when(inputManager.isKeyDown(KeyEvent.VK_DOWN)).thenReturn(true);
        logInScreen.update(); // PW
        when(inputManager.isKeyDown(KeyEvent.VK_DOWN)).thenReturn(false);
        
        inputManagerStaticMock.when(InputManager::getLastChar).thenReturn('p');
        logInScreen.update(); // PW: p
        inputManagerStaticMock.when(InputManager::getLastChar).thenReturn('\0');
        
        when(inputManager.isKeyDown(KeyEvent.VK_DOWN)).thenReturn(true);
        logInScreen.update(); // Submit
        when(inputManager.isKeyDown(KeyEvent.VK_DOWN)).thenReturn(false);
        
        // 2. FileManager가 PASSWORD_MISMATCH 반환
        when(fileManager.validateUser("u", "p")).thenReturn(
            FileManager.LoginResult.PASSWORD_MISMATCH);
        
        // 3. 제출
        when(inputManager.isKeyDown(KeyEvent.VK_SPACE)).thenReturn(true);
        logInScreen.update();
        
        // 4. 검증
        assertEquals("Password is incorrect!", getMessage());
        assertEquals("u", getIdInput()); // ID는 유지
        assertEquals("", getPasswordInput()); // Password는 초기화
    }
    
    @Test
    void testSubmit_EmptyFields() throws Exception {    // Test 6. 빈 입력창 submit 테스트
        // 1. 빈 값으로 바로 Submit 이동
        when(inputManager.isKeyDown(KeyEvent.VK_DOWN)).thenReturn(true);
        logInScreen.update(); // PW
        logInScreen.update(); // Submit
        when(inputManager.isKeyDown(KeyEvent.VK_DOWN)).thenReturn(false);
        
        // 2. 제출
        when(inputManager.isKeyDown(KeyEvent.VK_SPACE)).thenReturn(true);
        logInScreen.update();
        
        // 3. 검증
        assertEquals("ID and Password cannot be empty.", getMessage());
        verify(fileManager, never()).validateUser(anyString(), anyString());
    }
    
    @Test
    void testNavigation_Back() throws Exception {   // Test 7. Back 버튼 테스트
        // 1. Back 버튼(3번 인덱스)으로 이동
        when(inputManager.isKeyDown(KeyEvent.VK_UP)).thenReturn(true); // 위로 한번 누르면 Back
        logInScreen.update();
        when(inputManager.isKeyDown(KeyEvent.VK_UP)).thenReturn(false);
        
        // 2. 선택
        when(inputManager.isKeyDown(KeyEvent.VK_SPACE)).thenReturn(true);
        logInScreen.update();
        
        // 3. 검증
        assertFalse(logInScreen.getIsRunning()); // 화면 종료
        assertEquals(9, logInScreen.getReturnCode()); // AuthScreen(9) 반환
    }
}