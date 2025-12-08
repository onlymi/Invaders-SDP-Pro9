package screen;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.anyInt;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import engine.Core;
import engine.DrawManager;
import engine.FileManager;
import engine.InputManager;
import engine.SoundManager;
import engine.renderer.SignUpScreenRenderer;
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
 * SignUpScreen Test.
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT) // isKeyDown() 관련 경고 무시
class SignUpScreenTest {
    
    // 가짜(Mock) 객체 생성
    @Mock
    private InputManager inputManager;
    @Mock
    private DrawManager drawManager;
    @Mock
    private SoundManager soundManager;
    @Mock
    private FileManager fileManager;
    @Mock
    private SignUpScreenRenderer signUpScreenRenderer;
    @Mock
    private Cooldown mockCooldown;
    
    // Static 메소드 Mock
    private MockedStatic<Core> coreMock;
    private MockedStatic<InputManager> inputManagerStaticMock;
    private MockedStatic<SoundManager> soundManagerMock;
    private MockedStatic<FileManager> fileManagerMock;
    
    private SignUpScreen signUpScreen;
    
    @BeforeEach
    void setUp() throws Exception {
        // 모든 Static 메소드 Mock 준비
        coreMock = mockStatic(Core.class);
        inputManagerStaticMock = mockStatic(InputManager.class);
        soundManagerMock = mockStatic(SoundManager.class);
        fileManagerMock = mockStatic(FileManager.class);
        
        // Core.getter method 가 호출될 때, 가짜 객체를 반환하도록 설정
        coreMock.when(Core::getInputManager).thenReturn(inputManager);
        coreMock.when(Core::getDrawManager).thenReturn(drawManager);
        coreMock.when(Core::getFileManager).thenReturn(fileManager);
        coreMock.when(Core::getSoundManager).thenReturn(soundManager);
        coreMock.when(Core::getLogger).thenReturn(java.util.logging.Logger.getAnonymousLogger());
        coreMock.when(() -> Core.getCooldown(anyInt())).thenReturn(mockCooldown);
        when(mockCooldown.checkFinished()).thenReturn(true);
        // 다른 Manager들의 getInstance()가 @Mock 객체를 반환하도록 설정
        inputManagerStaticMock.when(InputManager::getInstance).thenReturn(inputManager);
        soundManagerMock.when(SoundManager::getInstance).thenReturn(soundManager);
        fileManagerMock.when(FileManager::getInstance).thenReturn(fileManager);
        
        // DrawManager가 가짜 렌더러를 반환하도록 설정
        when(drawManager.getSignUpScreenRenderer()).thenReturn(signUpScreenRenderer);
        
        // [수정] getLastChar의 기본값을 \0으로 설정
        inputManagerStaticMock.when(InputManager::getLastChar).thenReturn('\0');
        
        // 가짜 객체들로 SignUpScreen 생성
        signUpScreen = new SignUpScreen(448, 520, 60);
        signUpScreen.isRunning = true;
    }
    
    @AfterEach
    void tearDown() {
        // 모든 Static Mock 해제
        coreMock.close();
        inputManagerStaticMock.close();
        soundManagerMock.close();
        fileManagerMock.close();
    }
    
    // Helper 메소드 (Reflection)
    private String getIdInput() throws Exception {
        java.lang.reflect.Field field = SignUpScreen.class.getDeclaredField("idInput");
        field.setAccessible(true);
        return ((StringBuilder) field.get(signUpScreen)).toString();
    }
    
    private void setIdInput(String text) throws Exception {
        java.lang.reflect.Field field = SignUpScreen.class.getDeclaredField("idInput");
        field.setAccessible(true);
        StringBuilder idBuilder = (StringBuilder) field.get(signUpScreen);
        idBuilder.setLength(0);
        idBuilder.append(text);
    }
    
    private void setPasswordInput(String text) throws Exception {
        java.lang.reflect.Field field = SignUpScreen.class.getDeclaredField("passwordInput");
        field.setAccessible(true);
        StringBuilder pwBuilder = (StringBuilder) field.get(signUpScreen);
        pwBuilder.setLength(0);
        pwBuilder.append(text);
    }
    
    private String getPasswordInput() throws Exception {
        java.lang.reflect.Field field = SignUpScreen.class.getDeclaredField("passwordInput");
        field.setAccessible(true);
        return ((StringBuilder) field.get(signUpScreen)).toString();
    }
    
    private void setActiveField(int index) throws Exception {
        java.lang.reflect.Field field = SignUpScreen.class.getDeclaredField("activeField");
        field.setAccessible(true);
        field.set(signUpScreen, index);
    }
    
    private String getMessage() throws Exception {
        java.lang.reflect.Field field = SignUpScreen.class.getDeclaredField("message");
        field.setAccessible(true);
        return (String) field.get(signUpScreen);
    }
    
    private int getActiveField() throws Exception {
        java.lang.reflect.Field field = SignUpScreen.class.getDeclaredField("activeField");
        field.setAccessible(true);
        return (int) field.get(signUpScreen);
    }
    
    @Test
    void testInitialState() throws Exception {
        assertTrue(signUpScreen.getIsRunning());
        assertEquals(0, getActiveField());
    }
    
    @Test
    void testNavigation() throws Exception {
        // 1. 0 (ID) -> 1 (Password)
        when(inputManager.isKeyDown(KeyEvent.VK_DOWN)).thenReturn(true);
        signUpScreen.update();
        when(inputManager.isKeyDown(KeyEvent.VK_DOWN)).thenReturn(false);
        assertEquals(1, getActiveField());
        
        // 2. 1 (Password) -> 2 (Submit)
        when(inputManager.isKeyDown(KeyEvent.VK_DOWN)).thenReturn(true);
        signUpScreen.update();
        when(inputManager.isKeyDown(KeyEvent.VK_DOWN)).thenReturn(false);
        assertEquals(2, getActiveField());
        
        // 3. 2 (Submit) -> 3 (Back)
        when(inputManager.isKeyDown(KeyEvent.VK_DOWN)).thenReturn(true);
        signUpScreen.update();
        when(inputManager.isKeyDown(KeyEvent.VK_DOWN)).thenReturn(false);
        assertEquals(3, getActiveField());
        
        // 4. 'Back' 버튼에서 Space 키 (종료 확인)
        when(inputManager.isKeyDown(KeyEvent.VK_SPACE)).thenReturn(true);
        signUpScreen.update();
        
        assertEquals(9, signUpScreen.getReturnCode());
        assertFalse(signUpScreen.getIsRunning());
    }
    
    @Test
    void testTextInput_ID_and_Backspace() throws Exception {
        // 'a' 키 입력
        inputManagerStaticMock.when(InputManager::getLastChar).thenReturn('a');
        signUpScreen.update();
        inputManagerStaticMock.when(InputManager::getLastChar).thenReturn('\0');
        assertEquals("a", getIdInput());
        
        // 'b' 키 입력
        inputManagerStaticMock.when(InputManager::getLastChar).thenReturn('b');
        signUpScreen.update();
        inputManagerStaticMock.when(InputManager::getLastChar).thenReturn('\0');
        assertEquals("ab", getIdInput());
        
        // Backspace
        when(inputManager.isKeyDown(KeyEvent.VK_BACK_SPACE)).thenReturn(true);
        signUpScreen.update();
        when(inputManager.isKeyDown(KeyEvent.VK_BACK_SPACE)).thenReturn(false);
        assertEquals("a", getIdInput());
    }
    
    @Test
    void testTextInput_Password() throws Exception {
        // Password 필드로 이동
        when(inputManager.isKeyDown(KeyEvent.VK_DOWN)).thenReturn(true);
        signUpScreen.update();
        when(inputManager.isKeyDown(KeyEvent.VK_DOWN)).thenReturn(false);
        assertEquals(1, getActiveField());
        
        // 'p' 키 입력
        inputManagerStaticMock.when(InputManager::getLastChar).thenReturn('p');
        signUpScreen.update();
        inputManagerStaticMock.when(InputManager::getLastChar).thenReturn('\0');
        
        assertEquals("p", getPasswordInput());
        assertEquals("", getIdInput()); // ID 필드는 비어있어야 함
    }
    
    @Test
    void testSubmit_WithSpaces_ShouldTrimID() throws Exception {
        // 1.Reflection을 사용해 ID/PW 필드에 공백이 포함된 텍스트를 직접 설정
        setIdInput("  testtrim  ");
        setPasswordInput("pw123");
        setActiveField(2); // 'Submit' 버튼이 활성화된 상태로 설정
        
        // FileManager.saveUser가 "testtrim" (공백 제거된 ID)로 호출될 때
        // true를 반환하도록 모의(Mock) 설정
        when(fileManager.saveUser("testtrim", "pw123")).thenReturn(true);
        
        // 2. 실행
        when(inputManager.isKeyDown(KeyEvent.VK_SPACE)).thenReturn(true);
        signUpScreen.update();
        
        // 3. 검증
        verify(fileManager).saveUser("testtrim", "pw123");
        assertEquals("Sign Up Successful! Returning to login...", getMessage());
    }
    
    @Test
    void testSubmit_Success() throws Exception {
        // 7.1. ID와 PW 입력
        inputManagerStaticMock.when(InputManager::getLastChar).thenReturn('y');
        signUpScreen.update();
        inputManagerStaticMock.when(InputManager::getLastChar).thenReturn('\0');
        
        when(inputManager.isKeyDown(KeyEvent.VK_DOWN)).thenReturn(true);
        signUpScreen.update(); // 0 -> 1
        when(inputManager.isKeyDown(KeyEvent.VK_DOWN)).thenReturn(false);
        
        inputManagerStaticMock.when(InputManager::getLastChar).thenReturn('p');
        signUpScreen.update();
        inputManagerStaticMock.when(InputManager::getLastChar).thenReturn('\0');
        
        assertEquals("y", getIdInput());
        assertEquals("p", getPasswordInput());
        
        // 7.2. fileManager가 성공(true)을 반환하도록 설정
        when(fileManager.saveUser("y", "p")).thenReturn(true);
        
        // 7.3. Submit 버튼으로 이동
        when(inputManager.isKeyDown(KeyEvent.VK_DOWN)).thenReturn(true);
        signUpScreen.update(); // 1 -> 2
        when(inputManager.isKeyDown(KeyEvent.VK_DOWN)).thenReturn(false);
        assertEquals(2, getActiveField());
        
        // 7.4. 제출 (Space)
        when(inputManager.isKeyDown(KeyEvent.VK_SPACE)).thenReturn(true);
        signUpScreen.update();
        
        // 7.5. 검증
        verify(fileManager).saveUser("y", "p");
        assertEquals("Sign Up Successful! Returning to login...", getMessage());
        assertTrue(signUpScreen.getIsRunning());
    }
    
    @Test
    void testSubmit_Failure_IDExists() throws Exception {
        // 8.1. ID/PW 입력
        inputManagerStaticMock.when(InputManager::getLastChar).thenReturn('a');
        signUpScreen.update();
        inputManagerStaticMock.when(InputManager::getLastChar).thenReturn('\0');
        
        when(inputManager.isKeyDown(KeyEvent.VK_DOWN)).thenReturn(true);
        signUpScreen.update(); // 0 -> 1
        when(inputManager.isKeyDown(KeyEvent.VK_DOWN)).thenReturn(false);
        
        inputManagerStaticMock.when(InputManager::getLastChar).thenReturn('b');
        signUpScreen.update();
        inputManagerStaticMock.when(InputManager::getLastChar).thenReturn('\0');
        
        assertEquals("a", getIdInput());
        assertEquals("b", getPasswordInput());
        
        // 8.2. fileManager가 실패(false)를 반환하도록 설정
        when(fileManager.saveUser("a", "b")).thenReturn(false);
        
        // 8.3. Submit 버튼으로 이동
        when(inputManager.isKeyDown(KeyEvent.VK_DOWN)).thenReturn(true);
        signUpScreen.update(); // 1 -> 2
        when(inputManager.isKeyDown(KeyEvent.VK_DOWN)).thenReturn(false);
        assertEquals(2, getActiveField());
        
        // 8.4. 제출 (Space)
        when(inputManager.isKeyDown(KeyEvent.VK_SPACE)).thenReturn(true);
        signUpScreen.update();
        when(inputManager.isKeyDown(KeyEvent.VK_SPACE)).thenReturn(false);
        
        // 8.5. 검증
        verify(fileManager).saveUser("a", "b");
        assertEquals("This ID already exists! Try another ID.", getMessage());
        assertEquals("", getIdInput()); // 입력 필드가 초기화되었는지 확인
        assertEquals("", getPasswordInput());
    }
}