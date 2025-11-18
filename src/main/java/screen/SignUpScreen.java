package screen;

import engine.Core;
import engine.InputManager;
import engine.utils.Cooldown;
import java.awt.event.KeyEvent;

/**
 * Implements the Sign-Up screen. Handles user input for ID and Password.
 *
 * @author Seungju Yoon <yunseungju6@gmail.com>
 */
public class SignUpScreen extends Screen {
    
    /**
     * Milliseconds between menu selection changes.
     */
    private static final int SELECTION_TIME = 200;
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
     * Currently active field. 0: ID, 1: Password, 2: Submit, 3: Back.
     */
    private int activeField;
    
    /**
     * Message to display (e.g., "Sign Up Successful").
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
     * Save sign up success flag.
     */
    private boolean signUpSuccess = false;
    
    /**
     * Constructor, establishes the properties of the screen.
     *
     * @param width  Screen width.
     * @param height Screen height.
     * @param fps    Frames per second, frame rate at which the game is run.
     */
    public SignUpScreen(final int width, final int height, final int fps) {
        super(width, height, fps);
        this.idInput = new StringBuilder();
        this.passwordInput = new StringBuilder();
        this.activeField = 0; // Default to ID field
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
        // Sign-up was successful and message time is over
        if (this.signUpSuccess && this.messageCooldown.checkFinished()) {
            this.returnCode = 9;    // AuthScreen
            this.isRunning = false;
        }
        
        // Don't process input if success message is showing
        if (this.signUpSuccess) {
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
        // Get typed character
        char typedChar = InputManager.getLastChar();
        // Check if it's a valid alphanumeric or symbol character
        if (typedChar >= ' ' && typedChar <= '~') {
            // User restarts the input, clear the previous error message
            if (this.message != null && !this.signUpSuccess) {
                this.message = null;
            }
            if (this.activeField == 0) {
                this.idInput.append(typedChar);
            } else if (this.activeField == 1) {
                this.passwordInput.append(typedChar);
            }
            this.selectionCooldown.reset();
        } else if (inputManager.isKeyDown(KeyEvent.VK_BACK_SPACE)) { // Backspace logic
            // User restarts the input, clear the previous error message
            if (this.message != null && !this.signUpSuccess) {
                this.message = null;
            }
            if (this.activeField == 0 && !this.idInput.isEmpty()) {
                this.idInput.deleteCharAt(this.idInput.length() - 1);
            } else if (this.activeField == 1 && !this.passwordInput.isEmpty()) {
                this.passwordInput.deleteCharAt(this.passwordInput.length() - 1);
            }
            this.selectionCooldown.reset();
        }
    }
    
    /**
     * Handles confirm action (Enter/Space) based on the active field.
     */
    private void handleConfirm() {
        switch (this.activeField) {
            case 0: // ID field
            case 1: // Password field
                nextField();
                break;
            case 2: // Submit button
                submitForm();
                break;
            case 3: // Back button
                this.returnCode = 9;
                this.isRunning = false;
                break;
            default:
                break;
        }
    }
    
    /**
     * Attempts to save the user data using FileManager.
     */
    private void submitForm() {
        String id = this.idInput.toString().trim();
        String password = this.passwordInput.toString();
        
        if (id.isEmpty() || password.isEmpty()) {
            this.message = "ID and Password cannot be empty";
            this.messageCooldown.reset();
            return;
        }
        
        try {
            boolean success = this.fileManager.saveUser(id, password);
            if (success) {
                this.message = "Sign Up Successful! Returning to login...";
                this.signUpSuccess = true;
            } else {
                this.message = "This ID already exists! Try another ID.";
                this.idInput.setLength(0);
                this.passwordInput.setLength(0);
            }
        } catch (Exception e) {
            this.message = "Sign up Failed! Could not save user";
            this.idInput.setLength(0);
            this.passwordInput.setLength(0);
            LOGGER.warning("Error saving user: " + e.getMessage());
        }
        this.messageCooldown.reset();
    }
    
    /**
     * Moves focus to the next field.
     */
    private void nextField() {
        this.activeField = (this.activeField + 1) % 4;
    }
    
    /**
     * Moves focus to the previous field.
     */
    private void previousField() {
        this.activeField = (this.activeField - 1 + 4) % 4;
    }
    
    /**
     * Draws the elements associated with the screen.
     */
    private void drawScreen() {
        drawManager.initDrawing(this);
        drawManager.getSignUpScreenRenderer().draw(drawManager.getBackBufferGraphics(), this,
            this.activeField, this.idInput.toString(), this.passwordInput.toString(), this.message);
        drawManager.completeDrawing(this);
    }
}
