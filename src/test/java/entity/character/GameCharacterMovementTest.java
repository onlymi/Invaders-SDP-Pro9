package entity.character;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

import engine.Core;
import engine.InputManager;
import engine.UserStats;
import engine.utils.Cooldown;
import entity.Entity;
import java.awt.event.KeyEvent;
import java.util.HashSet;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.MockitoAnnotations;
import screen.Screen;

/**
 * GameCharacter의 이동 로직(handleMovement) 전용 테스트 클래스
 */
class GameCharacterMovementTest {
    
    @Mock
    private UserStats userStats;
    @Mock
    private InputManager inputManager;
    @Mock
    private Screen screen;
    
    private MockedStatic<Core> coreMock;
    
    private static class TestCharacter extends GameCharacter {
        
        public TestCharacter(int startX, int startY) {
            super(CharacterType.ARCHER, startX, startY, 10, 10, Entity.Team.PLAYER1, 1);
            
            this.baseStats.movementSpeed = 1.0f;
            this.currentStats = new CharacterStats(this.baseStats);
            
            int[] keys = {KeyEvent.VK_LEFT, KeyEvent.VK_RIGHT, KeyEvent.VK_UP, KeyEvent.VK_DOWN,
                KeyEvent.VK_SPACE};
            this.setControlKeys(keys);
        }
        
        @Override
        protected void applyUserUpgrades() {
        }
    }
    
    private TestCharacter character;
    private final int START_X = 200;
    private final int START_Y = 200;
    private final int SCREEN_WIDTH = 800;
    private final int SCREEN_HEIGHT = 600;
    
    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        
        coreMock = mockStatic(Core.class);
        coreMock.when(Core::getUserStats).thenReturn(userStats);
        coreMock.when(Core::getLogger).thenReturn(java.util.logging.Logger.getAnonymousLogger());
        coreMock.when(() -> Core.getCooldown(anyInt()))
            .thenAnswer(invocation -> new Cooldown(invocation.getArgument(0)));
        
        when(screen.getWidth()).thenReturn(SCREEN_WIDTH);
        when(screen.getHeight()).thenReturn(SCREEN_HEIGHT);
        
        character = new TestCharacter(START_X, START_Y);
    }
    
    @AfterEach
    void tearDown() {
        coreMock.close();
    }
    
    @Test
    void testNoMovement_WhenNoKeysPressed() {
        character.handleMovement(inputManager, screen, new HashSet<>(), 1.0f);
        
        assertEquals(START_X, character.getPositionX());
        assertEquals(START_Y, character.getPositionY());
        assertTrue(!character.isMoving());
    }
    
    @Test
    void testMoveLeft() {
        when(inputManager.isKeyDown(KeyEvent.VK_LEFT)).thenReturn(true);
        
        // 200 - 150 = 50 (화면 안쪽이므로 이동 성공)
        character.handleMovement(inputManager, screen, new HashSet<>(), 1.0f);
        
        assertEquals(START_X - 150, character.getPositionX());
        assertEquals(START_Y, character.getPositionY());
        assertTrue(character.isFacingLeft());
        assertTrue(character.isMoving());
    }
    
    @Test
    void testMoveRight() {
        when(inputManager.isKeyDown(KeyEvent.VK_RIGHT)).thenReturn(true);
        
        character.handleMovement(inputManager, screen, new HashSet<>(), 1.0f);
        
        assertEquals(START_X + 150, character.getPositionX());
        assertEquals(START_Y, character.getPositionY());
        assertTrue(character.isFacingRight());
    }
    
    @Test
    void testMoveUp() {
        when(inputManager.isKeyDown(KeyEvent.VK_UP)).thenReturn(true);
        
        // 200 - 150 = 50 (화면 안쪽이므로 이동 성공)
        character.handleMovement(inputManager, screen, new HashSet<>(), 1.0f);
        
        assertEquals(START_X, character.getPositionX());
        assertEquals(START_Y - 150, character.getPositionY());
        assertTrue(character.isFacingBack());
    }
    
    @Test
    void testMoveDown() {
        when(inputManager.isKeyDown(KeyEvent.VK_DOWN)).thenReturn(true);
        
        character.handleMovement(inputManager, screen, new HashSet<>(), 1.0f);
        
        assertEquals(START_X, character.getPositionX());
        assertEquals(START_Y + 150, character.getPositionY());
        assertTrue(character.isFacingFront());
    }
    
    @Test
    void testDiagonalMovement_SpeedNormalization() {
        when(inputManager.isKeyDown(KeyEvent.VK_RIGHT)).thenReturn(true);
        when(inputManager.isKeyDown(KeyEvent.VK_DOWN)).thenReturn(true);
        
        character.handleMovement(inputManager, screen, new HashSet<>(), 1.0f);
        
        int movedX = character.getPositionX() - START_X;
        int movedY = character.getPositionY() - START_Y;
        
        int expectedMove = (int) (150 * (1 / Math.sqrt(2)));
        
        assertEquals(expectedMove, movedX, 1);
        assertEquals(expectedMove, movedY, 1);
        
        double distance = Math.sqrt(movedX * movedX + movedY * movedY);
        assertEquals(150.0, distance, 2.0);
    }
    
    @Test
    void testOppositeKeys_CancelMovement() {
        when(inputManager.isKeyDown(KeyEvent.VK_LEFT)).thenReturn(true);
        when(inputManager.isKeyDown(KeyEvent.VK_RIGHT)).thenReturn(true);
        
        character.handleMovement(inputManager, screen, new HashSet<>(), 1.0f);
        
        assertEquals(START_X, character.getPositionX());
    }
    
    @Test
    void testBoundaryCheck_Left() {
        // [테스트를 위해 강제로 왼쪽 끝으로 이동]
        character.setPositionX(1);
        when(inputManager.isKeyDown(KeyEvent.VK_LEFT)).thenReturn(true);
        
        character.handleMovement(inputManager, screen, new HashSet<>(), 1.0f);
        
        assertEquals(1, character.getPositionX());
    }
    
    @Test
    void testBoundaryCheck_Right() {
        int edgeX = SCREEN_WIDTH - character.getWidth() - 1;
        character.setPositionX(edgeX);
        
        when(inputManager.isKeyDown(KeyEvent.VK_RIGHT)).thenReturn(true);
        
        character.handleMovement(inputManager, screen, new HashSet<>(), 1.0f);
        
        assertEquals(edgeX, character.getPositionX());
    }
    
    @Test
    void testBoundaryCheck_Top() {
        character.setPositionY(1);
        when(inputManager.isKeyDown(KeyEvent.VK_UP)).thenReturn(true);
        
        character.handleMovement(inputManager, screen, new HashSet<>(), 1.0f);
        
        assertEquals(1, character.getPositionY());
    }
    
    @Test
    void testBoundaryCheck_Bottom() {
        int edgeY = SCREEN_HEIGHT - character.getHeight() - 1;
        character.setPositionY(edgeY);
        
        when(inputManager.isKeyDown(KeyEvent.VK_DOWN)).thenReturn(true);
        
        character.handleMovement(inputManager, screen, new HashSet<>(), 1.0f);
        
        assertEquals(edgeY, character.getPositionY());
    }
}