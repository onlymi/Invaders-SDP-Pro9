package screen;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

import engine.Core;
import engine.DrawManager;
import engine.InputManager;
import engine.SoundManager;
import engine.hitbox.HitboxManager;
import engine.renderer.CommonRenderer;
import engine.renderer.PlayerSelectionScreenRenderer;
import engine.utils.Cooldown;
import entity.character.CharacterType;
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

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class PlayerSelectionScreenTest {
    
    @Mock
    private InputManager inputManager;
    @Mock
    private DrawManager drawManager;
    @Mock
    private SoundManager soundManager;
    @Mock
    private HitboxManager hitboxManager;
    @Mock
    private PlayerSelectionScreenRenderer playerSelectionScreenRenderer;
    @Mock
    private CommonRenderer commonRenderer;
    @Mock
    private Cooldown mockCooldown;
    @Mock
    private Graphics mockGraphics;
    
    private MockedStatic<Core> coreMock;
    private MockedStatic<InputManager> inputManagerStaticMock;
    private MockedStatic<SoundManager> soundManagerMock;
    
    private PlayerSelectionScreen playerSelectionScreen;
    
    @BeforeEach
    void setUp() {
        // Static Mocking
        coreMock = mockStatic(Core.class);
        inputManagerStaticMock = mockStatic(InputManager.class);
        soundManagerMock = mockStatic(SoundManager.class);
        
        // Core Mocks
        coreMock.when(Core::getInputManager).thenReturn(inputManager);
        coreMock.when(Core::getDrawManager).thenReturn(drawManager);
        coreMock.when(Core::getSoundManager).thenReturn(soundManager);
        coreMock.when(Core::getHitboxManager).thenReturn(hitboxManager);
        coreMock.when(Core::getLogger).thenReturn(java.util.logging.Logger.getAnonymousLogger());
        
        // Cooldown Mock (Character creation and Screen input delay rely on this)
        coreMock.when(() -> Core.getCooldown(anyInt())).thenReturn(mockCooldown);
        when(mockCooldown.checkFinished()).thenReturn(true);
        
        // Manager Singleton Mocks
        inputManagerStaticMock.when(InputManager::getInstance).thenReturn(inputManager);
        soundManagerMock.when(SoundManager::getInstance).thenReturn(soundManager);
        
        // Renderer Mocks
        when(drawManager.getShipSelectionMenuRenderer()).thenReturn(playerSelectionScreenRenderer);
        when(drawManager.getCommonRenderer()).thenReturn(commonRenderer);
        when(drawManager.getBackBufferGraphics()).thenReturn(mockGraphics);
        
        // Hitbox Mock default behavior
        when(hitboxManager.getBackButtonHitbox(any(), any())).thenReturn(new Rectangle(0, 0, 0, 0));
    }
    
    @AfterEach
    void tearDown() {
        coreMock.close();
        inputManagerStaticMock.close();
        soundManagerMock.close();
    }
    
    private void initializeScreen(int playerId) {
        playerSelectionScreen = new PlayerSelectionScreen(800, 600, 60, playerId);
        playerSelectionScreen.isRunning = true; // Simulate screen running
    }
    
    @Test
    void testInitialState() {
        initializeScreen(1);
        // 수정됨: 초기값(0)은 WARRIOR여야 함
        assertEquals(CharacterType.WARRIOR, playerSelectionScreen.getSelectedCharacterType());
        assertTrue(playerSelectionScreen.getIsRunning());
    }
    
    @Test
    void testCharacterRotation_Keyboard() {
        initializeScreen(1); // 초기 상태 (index: 0)
        
        // [검증 1] 오른쪽 키 입력: 0(WARRIOR) -> 1(ARCHER)
        when(inputManager.isKeyDown(KeyEvent.VK_RIGHT)).thenReturn(true);
        playerSelectionScreen.update();
        when(inputManager.isKeyDown(KeyEvent.VK_RIGHT)).thenReturn(false);
        
        // 수정됨: 다음 캐릭터는 ARCHER여야 함
        assertEquals(CharacterType.ARCHER, playerSelectionScreen.getSelectedCharacterType());
        
        // [검증 2] 왼쪽 키 입력 (순환): 1(ARCHER) -> 0(WARRIOR) -> 6(HEALER)
        when(inputManager.isKeyDown(KeyEvent.VK_LEFT)).thenReturn(true);
        playerSelectionScreen.update(); // 1 -> 0
        playerSelectionScreen.update(); // 0 -> 6 (마지막 인덱스)
        when(inputManager.isKeyDown(KeyEvent.VK_LEFT)).thenReturn(false);
        
        // 수정됨: 마지막 캐릭터는 HEALER여야 함 (이제 코드가 수정되어 통과함)
        assertEquals(CharacterType.HEALER, playerSelectionScreen.getSelectedCharacterType());
    }
    
    @Test
    void testNavigation_FocusBackButton() {
        initializeScreen(1);
        
        // 1. Up Key -> Back 버튼 포커스 (backButtonSelected = true)
        when(inputManager.isKeyDown(KeyEvent.VK_UP)).thenReturn(true);
        playerSelectionScreen.update();
        when(inputManager.isKeyDown(KeyEvent.VK_UP)).thenReturn(false);
        
        // 이 상태에서 Space를 누르면 Back 동작이 수행되어야 함 (P1 Back -> returnCode 5)
        when(inputManager.isKeyDown(KeyEvent.VK_SPACE)).thenReturn(true);
        playerSelectionScreen.update();
        
        assertFalse(playerSelectionScreen.getIsRunning());
        assertEquals(5, playerSelectionScreen.getReturnCode());
    }
    
    @Test
    void testSelection_Player1_Confirm() {
        initializeScreen(1);
        
        // 캐릭터 선택 상태 (Back 버튼 포커스 아님)에서 Space
        when(inputManager.isKeyDown(KeyEvent.VK_SPACE)).thenReturn(true);
        playerSelectionScreen.update();
        
        // P1 선택 확정 -> P2 선택 화면(6) 또는 게임 시작(2) 등 (로직에 따라 6 반환)
        assertFalse(playerSelectionScreen.getIsRunning());
        assertEquals(6, playerSelectionScreen.getReturnCode());
    }
    
    @Test
    void testSelection_Player2_Confirm() {
        initializeScreen(2); // Player 2
        
        // 캐릭터 선택 상태에서 Space
        when(inputManager.isKeyDown(KeyEvent.VK_SPACE)).thenReturn(true);
        playerSelectionScreen.update();
        
        // P2 선택 확정 -> 게임 시작 (returnCode 2)
        assertFalse(playerSelectionScreen.getIsRunning());
        assertEquals(2, playerSelectionScreen.getReturnCode());
    }
    
    @Test
    void testSelection_Player2_Back() {
        initializeScreen(2);
        
        // Up Key로 Back 버튼 포커스
        when(inputManager.isKeyDown(KeyEvent.VK_UP)).thenReturn(true);
        playerSelectionScreen.update();
        when(inputManager.isKeyDown(KeyEvent.VK_UP)).thenReturn(false);
        
        // Space -> Back 수행
        when(inputManager.isKeyDown(KeyEvent.VK_SPACE)).thenReturn(true);
        playerSelectionScreen.update();
        
        // P2에서 뒤로가기 -> P1 선택 화면 (returnCode 6)
        assertFalse(playerSelectionScreen.getIsRunning());
        assertEquals(6, playerSelectionScreen.getReturnCode());
    }
    
    @Test
    void testMouseInput_BackClick() {
        initializeScreen(1);
        
        // 마우스 클릭 상태 설정
        when(inputManager.isMouseClicked()).thenReturn(true);
        when(inputManager.getMouseX()).thenReturn(50);
        when(inputManager.getMouseY()).thenReturn(50);
        
        // HitboxManager가 클릭된 위치를 포함하는 Hitbox를 반환하도록 설정
        Rectangle mockHitbox = new Rectangle(0, 0, 100, 100); // 50,50 포함
        when(hitboxManager.getBackButtonHitbox(any(), any())).thenReturn(mockHitbox);
        
        playerSelectionScreen.update();
        
        // Back 버튼 클릭 시 동작 (P1 Back -> 5)
        assertFalse(playerSelectionScreen.getIsRunning());
        assertEquals(5, playerSelectionScreen.getReturnCode());
    }
}