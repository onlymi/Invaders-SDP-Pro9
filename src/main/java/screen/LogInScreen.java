package screen;

import engine.Core;
import engine.FileManager;
import engine.InputManager;
import engine.utils.Cooldown;
import java.awt.event.KeyEvent;

/**
 * Implements the Log In screen. Handles user input for ID and Password verification.
 */
public class LogInScreen extends Screen {
    
    /**
     * Milliseconds between menu selection changes.
     */
    private static final int SELECTION_TIME = 100;
    /**
     * Cooldown timer for menu selections.
     */
    private Cooldown selectionCooldown;
    /**
     * Stores the ID typed by the user.
     */
    private StringBuilder idInput;
    /**
     * Stores the Password typed by the user.
     */
    private StringBuilder passwordInput;
    /**
     * 0: ID, 1: Password, 2: Submit, 3: Back
     */
    private int activeField;
    /**
     * Message to display (e.g., "Log In Successful").
     */
    private String message;
    /**
     * Cooldown for displaying the message.
     */
    private Cooldown messageCooldown;
    /**
     * Milliseconds to display message.
     */
    private static final int MESSAGE_DURATION = 2000;
    /**
     * Flag to save login success
     */
    private boolean logInSuccess = false;
    
    /**
     * Constructor, establishes the properties of the screen.
     *
     * @param width  Screen width.
     * @param height Screen height.
     * @param fps    Frames per second, frame rate at which the game is run.
     */
    public LogInScreen(int width, int height, int fps) {
        super(width, height, fps);
        this.idInput = new StringBuilder();
        this.passwordInput = new StringBuilder();
        this.activeField = 0;
        this.selectionCooldown = Core.getCooldown(SELECTION_TIME);
        this.selectionCooldown.reset();
        this.messageCooldown = Core.getCooldown(MESSAGE_DURATION);
    }
    
    /**
     * Updates the elements on screen and checks for events.
     */
    @Override
    protected final void update() {
        super.update();
        drawScreen();
        
        // 로그인 성공 시, TitleScreen(1)으로 이동
        if (this.logInSuccess && this.messageCooldown.checkFinished()) {
            this.returnCode = 1;    // TitleScreen
            this.isRunning = false;
        }
        
        //  로그인 성공 시, 입력 처리 안함
        if (this.logInSuccess) {
            return;
        }
        
        if (this.selectionCooldown.checkFinished() && this.inputDelay.checkFinished()) {
            if (inputManager.isKeyDown(KeyEvent.VK_UP) || inputManager.isKeyDown(KeyEvent.VK_TAB)) {
                this.soundManager.playOnce("hover");
                previousField();
                this.selectionCooldown.reset();
            }
            if (inputManager.isKeyDown(KeyEvent.VK_DOWN)) {
                this.soundManager.playOnce("hover");
                nextField();
                this.selectionCooldown.reset();
            }
            handleTextInput();
            
            if (inputManager.isKeyDown(KeyEvent.VK_ENTER) || inputManager.isKeyDown(
                KeyEvent.VK_SPACE)) {
                this.soundManager.playOnce("select");
                handleConfirm();
                this.selectionCooldown.reset();
            }
        }
    }
    
    /**
     * Handles text input for ID and Password fields.
     */
    private void handleTextInput() {
        // Backspace 구현
        if (inputManager.isKeyDown(KeyEvent.VK_BACK_SPACE)) {
            clearFailureMessage();
            if (this.activeField == 0 && !this.idInput.isEmpty()) {
                this.idInput.deleteCharAt(idInput.length() - 1);
            } else if (this.activeField == 1 && !this.passwordInput.isEmpty()) {
                this.passwordInput.deleteCharAt(passwordInput.length() - 1);
            }
            this.selectionCooldown.reset();
            return;
        }
        
        // Character 입력
        char typedChar = InputManager.getLastChar();
        if (typedChar >= ' ' && typedChar <= '~') {
            clearFailureMessage();
            if (this.activeField == 0) {
                this.idInput.append(typedChar);
            } else if (this.activeField == 1) {
                this.passwordInput.append(typedChar);
            }
            this.selectionCooldown.reset();
        }
    }
    
    /**
     * When the user restarts the input, clear the failure message.
     */
    private void clearFailureMessage() {
        if (this.message != null && !this.logInSuccess) {
            this.message = null;
        }
    }
    
    /**
     * Handles confirm action (Enter/Space) based on the active field.
     */
    private void handleConfirm() {
        switch (this.activeField) {
            case 0:    // ID field
            case 1:    // Password field
                nextField();
                break;
            case 2:    // Submit button
                submitForm();
                break;
            case 3:    // Back button
                this.returnCode = 9;   // Auth screen
                this.isRunning = false;
                break;
            default:
                break;
        }
    }
    
    /**
     * Attempts to validate the user data using FileManager.
     */
    private void submitForm() {
        String id = this.idInput.toString().trim();
        String password = this.passwordInput.toString();
        
        if (id.isEmpty() || password.isEmpty()) {
            this.message = "ID and Password cannot be empty.";
            this.messageCooldown.reset();
            return;
        }
        
        try {
            FileManager.LoginResult result = this.fileManager.validateUser(id, password);
            
            switch (result) {
                case SUCCESS:
                    this.message = "Log In Successful! Starting game...";
                    this.logInSuccess = true;
                    // TODO     Core에 로그인한 계정 저장. 스탯 구매 구현 시 수정 예정
                    break;
                case PASSWORD_MISMATCH:
                    this.message = "Password is incorrect!";
                    this.passwordInput.setLength(0);
                    break;
                case ID_NOT_FOUND:
                    this.message = "This ID does not exist.";
                    this.passwordInput.setLength(0);
                    break;
            }
        } catch (Exception e) {
            this.message = "Error: Could not validate user.";
            this.idInput.setLength(0);
            this.passwordInput.setLength(0);
            LOGGER.warning("Error validating user: " + e.getMessage());
        }
        this.messageCooldown.reset();
    }
    
    /**
     * Moves focus to the next field.
     */
    private void nextField() {
        this.activeField = (activeField + 1) % 4;
    }
    
    /**
     * Moves focus to the previous field.
     */
    private void previousField() {
        this.activeField = (activeField + 3) % 4;
    }
    
    /**
     * Draws the elements associated with the screen.
     */
    private void drawScreen() {
        drawManager.initDrawing(this);
        drawManager.getLogInScreenRenderer().draw(
            drawManager.getBackBufferGraphics(),
            this,
            this.activeField, this.idInput.toString(),
            this.passwordInput.toString(), this.message, this.logInSuccess);
        drawManager.completeDrawing(this);
    }
}
