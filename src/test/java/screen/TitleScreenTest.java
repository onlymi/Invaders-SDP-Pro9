package screen;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import engine.Core;
import engine.DrawManager;
import engine.InputManager;
import engine.SoundManager;
import engine.hitbox.HitboxManager;
import engine.renderer.TitleScreenRenderer;
import engine.utils.Cooldown;
import java.awt.Graphics;
import java.awt.Rectangle;
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
 * TitleScreen Test
 */

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class TitleScreenTest {
    
    @Mock
    private InputManager inputManager;
    @Mock
    private DrawManager drawManager;
    @Mock
    private SoundManager soundManager;
    @Mock
    private TitleScreenRenderer titleScreenRenderer;
    @Mock
    private Cooldown mockCooldown;
    @Mock
    private HitboxManager hitboxManager;
    @Mock
    private Graphics graphics;
    
    private MockedStatic<Core> coreMock;
    private MockedStatic<InputManager> inputManagerStaticMock;
    private MockedStatic<SoundManager> soundManagerMock;
    
    private TitleScreen titleScreen;
    
    @BeforeEach
    void setUp() throws Exception {
        // Static Mock 설정
        coreMock = mockStatic(Core.class);
        inputManagerStaticMock = mockStatic(InputManager.class);
        soundManagerMock = mockStatic(SoundManager.class);
        
        // Core Getter Mocking
        coreMock.when(Core::getInputManager).thenReturn(inputManager);
        coreMock.when(Core::getDrawManager).thenReturn(drawManager);
        coreMock.when(Core::getSoundManager).thenReturn(soundManager);
        coreMock.when(Core::getHitboxManager).thenReturn(hitboxManager);
        coreMock.when(Core::getLogger).thenReturn(java.util.logging.Logger.getAnonymousLogger());
        
        // 딜레이 없이 즉시 실행
        coreMock.when(() -> Core.getCooldown(anyInt())).thenReturn(mockCooldown);
        when(mockCooldown.checkFinished()).thenReturn(true);
        
        // Singleton Instance Mocking
        inputManagerStaticMock.when(InputManager::getInstance).thenReturn(inputManager);
        soundManagerMock.when(SoundManager::getInstance).thenReturn(soundManager);
        
        // Renderer Mocking
        when(drawManager.getTitleScreenRenderer()).thenReturn(titleScreenRenderer);
        when(drawManager.getBackBufferGraphics()).thenReturn(graphics);
        
        // 마우스 동작 테스트용 더미 박스
        Rectangle[] dummyHitboxes = new Rectangle[5];
        for (int i = 0; i < 5; i++) {
            // 각 메뉴 항목의 위치를 시뮬레이션 (5개의 박스)
            dummyHitboxes[i] = new Rectangle(100, 100 + (i * 50), 200, 40);
        }
        when(hitboxManager.getMenuHitboxes(any(), any())).thenReturn(dummyHitboxes);
        // Back button hitbox (필요 시)
        when(hitboxManager.getBackButtonHitbox(any(), any())).thenReturn(new Rectangle(0, 0, 0, 0));
        
        // TitleScreen 생성
        titleScreen = new TitleScreen(800, 600, 60);
        // 테스트를 위해 강제로 running 상태로 설정 (update 루프 진입 가능하게)
        setField(titleScreen, "isRunning", true);
    }
    
    @AfterEach
    void tearDown() {
        coreMock.close();
        inputManagerStaticMock.close();
        soundManagerMock.close();
    }
    
    // Reflection Helper Methods
    private int getMenuIndex() throws Exception {
        java.lang.reflect.Field field = TitleScreen.class.getDeclaredField("menuIndex");
        field.setAccessible(true);
        return (int) field.get(titleScreen);
    }
    
    private void setField(Object target, String fieldName, Object value) throws Exception {
        java.lang.reflect.Field field = Screen.class.getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(target, value);
    }
    
    @Test
    void testInitialState() { // Test 1. titleScreen return code 테스트
        // 생성 직후 기본 returnCode는 1이어야 함
        assertEquals(1, titleScreen.getReturnCode());
        assertTrue(titleScreen.getIsRunning());
    }
    
    @Test
    void testNavigation_Down() throws Exception { // Test 2. menu 아래 방향키 테스트
        // 초기 상태: menuIndex 0 (Play)
        assertEquals(0, getMenuIndex());
        
        // 아래 방향키 입력
        when(inputManager.isKeyDown(KeyEvent.VK_DOWN)).thenReturn(true);
        titleScreen.update();
        // 키 떼기
        when(inputManager.isKeyDown(KeyEvent.VK_DOWN)).thenReturn(false);
        
        // menuIndex가 1(Achievements)로 변경되었는지 확인
        assertEquals(1, getMenuIndex());
    }
    
    @Test
    void testNavigation_Up_WrapAround() throws Exception { // Test 3. menu 위 방향키 테스트
        // 초기 상태: menuIndex 0
        // 위 방향키 입력
        when(inputManager.isKeyDown(KeyEvent.VK_UP)).thenReturn(true);
        titleScreen.update();
        when(inputManager.isKeyDown(KeyEvent.VK_UP)).thenReturn(false);
        
        // menuIndex가 5(Exit)로 변경되었는지 확인
        assertEquals(5, getMenuIndex());
    }
    
    @Test
    void testSelect_Play() throws Exception { // Test 4. Play 버튼 테스트
        // 메뉴 0 (Play) 선택
        when(inputManager.isKeyDown(KeyEvent.VK_SPACE)).thenReturn(true);
        
        titleScreen.update();
        
        // returnCode가 5 (PlayModeSelectionScreen)로 설정되고 화면이 종료되어야 함
        assertEquals(5, titleScreen.getReturnCode());
        assertFalse(titleScreen.getIsRunning());
    }
    
    @Test
    void testSelect_HighScores() throws Exception { // Test 5. High Scores 버튼 테스트
        // High Scores(2)로 이동
        when(inputManager.isKeyDown(KeyEvent.VK_DOWN)).thenReturn(true);
        titleScreen.update(); // 0 -> 1
        titleScreen.update(); // 1 -> 2
        when(inputManager.isKeyDown(KeyEvent.VK_DOWN)).thenReturn(false);
        
        assertEquals(2, getMenuIndex());
        
        // 선택 (Space)
        when(inputManager.isKeyDown(KeyEvent.VK_SPACE)).thenReturn(true);
        titleScreen.update();
        
        // returnCode가 8 (High Scores)로 설정되어야 함
        assertEquals(8, titleScreen.getReturnCode());
        assertFalse(titleScreen.getIsRunning());
    }
    
    @Test
    void testSelect_Settings() throws Exception { // Test 6. Settings 버튼 테스트
        // Settings(3)로 이동
        when(inputManager.isKeyDown(KeyEvent.VK_DOWN)).thenReturn(true);
        titleScreen.update(); // 0 -> 1
        titleScreen.update(); // 1 -> 2
        titleScreen.update(); // 2 -> 3
        when(inputManager.isKeyDown(KeyEvent.VK_DOWN)).thenReturn(false);
        
        assertEquals(3, getMenuIndex());
        
        // 선택 (Space)
        when(inputManager.isKeyDown(KeyEvent.VK_SPACE)).thenReturn(true);
        titleScreen.update();
        
        // returnCode가 4 (Settings)로 설정되어야 함
        assertEquals(4, titleScreen.getReturnCode());
        assertFalse(titleScreen.getIsRunning());
    }
    
    @Test
    void testSelect_Logout() throws Exception { // Test 7. Logout 버튼 테스트
        // Logout(4)로 이동
        when(inputManager.isKeyDown(KeyEvent.VK_UP)).thenReturn(true);
        titleScreen.update(); // 0 -> 5
        titleScreen.update(); // 5 -> 4
        when(inputManager.isKeyDown(KeyEvent.VK_UP)).thenReturn(false);
        
        assertEquals(4, getMenuIndex());
        
        // 선택 (Space)
        when(inputManager.isKeyDown(KeyEvent.VK_SPACE)).thenReturn(true);
        titleScreen.update();
        
        // returnCode가 9 (AuthScreen)로 설정되어야 함
        assertEquals(9, titleScreen.getReturnCode());
        assertFalse(titleScreen.getIsRunning());
    }
    
    @Test
    void testSelect_Exit() throws Exception { // Test 8. Exit 버튼 테스트
        // Exit(5)로 이동: 위로 1번
        when(inputManager.isKeyDown(KeyEvent.VK_UP)).thenReturn(true);
        titleScreen.update(); // 0 -> 5
        when(inputManager.isKeyDown(KeyEvent.VK_UP)).thenReturn(false);
        
        assertEquals(5, getMenuIndex());
        
        // 선택 (Space)
        when(inputManager.isKeyDown(KeyEvent.VK_SPACE)).thenReturn(true);
        titleScreen.update();
        
        // returnCode가 0 (Exit)으로 설정되어야 함
        assertEquals(0, titleScreen.getReturnCode());
        assertFalse(titleScreen.getIsRunning());
    }
    
    @Test
    void testMouseHover() throws Exception { // Test 9. 마우스 선택 하이라이트 테스트
        // 마우스 위치 설정 (첫 번째 메뉴 항목 위: 150, 120)
        // setUp에서 더미 Hitbox[0]은 (100, 100, 200, 40)임
        when(inputManager.getMouseX()).thenReturn(150);
        when(inputManager.getMouseY()).thenReturn(120);
        
        titleScreen.update();
        
        // drawMenu 호출 시 hoverOption이 0이어야 함
        verify(titleScreenRenderer).drawMenu(any(), eq(titleScreen), anyInt(), eq(0), anyInt());
    }
    
    @Test
    void testMouseClick_Settings() { // Test 10. 마우스 클릭 테스트
        // 마우스 위치 설정 (네 번째 메뉴 항목 Settings: 인덱스 3)
        // Hitbox[3] = (100, 100 + 3*50, 200, 40) = (100, 250, 200, 40)
        when(inputManager.getMouseX()).thenReturn(150);
        when(inputManager.getMouseY()).thenReturn(270);
        
        // 클릭 상태 설정
        when(inputManager.isMouseClicked()).thenReturn(true);
        
        titleScreen.update();
        
        // Settings 화면 코드(4) 반환 확인
        assertEquals(4, titleScreen.getReturnCode());
        assertFalse(titleScreen.getIsRunning());
    }
}