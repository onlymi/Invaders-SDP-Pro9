package screen;

import animations.MenuSpace;
import engine.Core;
import engine.utils.Cooldown;
import java.awt.event.KeyEvent;

/**
 * Implements the Authentication screen. Handles user input for logging in or signing up.
 *
 * @author Seungju Yoon <yunseungju6@gmail.com>
 */
public class AuthScreen extends Screen {
    
    /**
     * Milliseconds between menu selection changes.
     */
    private static final int SELECTION_TIME = 200;
    
    /**
     * Cooldown timer for menu selections.
     */
    private Cooldown selectionCooldown;
    
    /**
     *
     */
    private MenuSpace menuSpace;
    
    /**
     * Current menu item index. 0: Log In, 1: Sign Up.
     */
    private int menuIndex;
    
    /**
     * Constructor, establishes the properties of the screen.
     *
     * @param width  Screen width.
     * @param height Screen height.
     * @param fps    Frames per second, frame rate at which the game is run.
     */
    public AuthScreen(final int width, final int height, final int fps) {
        super(width, height, fps);
        
        // Defaults to Log In.
        this.returnCode = 1; // 1 = Go to TitleScreen
        this.menuIndex = 0;
        this.selectionCooldown = Core.getCooldown(SELECTION_TIME);
        this.selectionCooldown.reset();
        this.menuSpace = new MenuSpace(50, this.width, this.height);
        
        // Start menu music loop
        this.soundManager.playLoop("title_sound");
    }
    
    /**
     * Starts the action.
     *
     * @return Next screen code.
     */
    @Override
    public final int run() {
        super.run();
        // Stop menu music when leaving this screen
        this.soundManager.loopStop();
        return this.returnCode;
    }
    
    /**
     * Updates the elements on screen and checks for events.
     */
    @Override
    protected final void update() {
        super.update();
        drawScreen();
        
        if (this.selectionCooldown.checkFinished() && this.inputDelay.checkFinished()) {
            if (inputManager.isKeyDown(KeyEvent.VK_UP) || inputManager.isKeyDown(KeyEvent.VK_W)) {
                this.soundManager.playOnce("hover");
                moveToPreviousMenuItem();
                this.selectionCooldown.reset();
            }
            if (inputManager.isKeyDown(KeyEvent.VK_DOWN) || inputManager.isKeyDown(KeyEvent.VK_S)) {
                this.soundManager.playOnce("hover");
                moveToNextMenuItem();
                this.selectionCooldown.reset();
            }
            
            if (inputManager.isKeyDown(KeyEvent.VK_SPACE) || inputManager.isKeyDown(
                KeyEvent.VK_ENTER)) {
                this.soundManager.playOnce("select");
                switch (this.menuIndex) {
                    case 0: // Log In
                        // 11: 로그인 화면(LogInScreen)으로 이동
                        this.returnCode = 11;
                        this.isRunning = false;
                        break;
                    case 1: // Sign Up
                        // 10: 회원가입 화면(SignUpScreen)으로 이동
                        this.returnCode = 10;
                        this.isRunning = false;
                        break;
                    case 2: // Exit
                        this.returnCode = 0;
                        this.isRunning = false;
                        break;
                    default:
                        break;
                }
            }
        }
    }
    
    /**
     * Shifts the focus to the next menu item.
     */
    private void moveToNextMenuItem() {
        this.menuIndex = (this.menuIndex + 1) % 3; // 3개의 메뉴만 순환
        drawManager.getAuthScreenRenderer().menuHover(this.menuSpace, this.menuIndex);
    }
    
    /**
     * Shifts the focus to the previous menu item.
     */
    private void moveToPreviousMenuItem() {
        this.menuIndex = (this.menuIndex + 2) % 3;
        drawManager.getAuthScreenRenderer().menuHover(this.menuSpace, this.menuIndex);
    }
    
    /**
     * Draws the elements associated with the screen.
     */
    private void drawScreen() {
        drawManager.initDrawing(this);
        
        // 배경 애니메이션
        drawManager.getAuthScreenRenderer()
            .updateMenuSpace(drawManager.getBackBufferGraphics(), this.menuSpace);
        
        // 타이틀
        drawManager.getAuthScreenRenderer().drawTitle(drawManager.getBackBufferGraphics(), this);
        
        // 메뉴 (Log In / Sign Up)
        drawManager.getAuthScreenRenderer().drawMenu(drawManager.getBackBufferGraphics(),
            this, this.menuIndex, null, this.menuIndex);
        
        drawManager.completeDrawing(this);
    }
    
    /**
     * (for test) getter menuInder
     *
     * @return meunIndex
     */
    public int getMenuIndex() {
        return menuIndex;
    }
}