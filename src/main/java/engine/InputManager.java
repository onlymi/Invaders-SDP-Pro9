package engine;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;


/**
 * Manages keyboard input for the provided screen.
 *
 * @author <a href="mailto:RobertoIA1987@gmail.com">Roberto Izquierdo Amo</a>
 *
 */

public final class InputManager implements KeyListener, MouseListener,
    MouseMotionListener { // add MouseListener, MouseMotionListener param
    
    
    /**
     * Number of recognised keys.
     */
    private static final int NUM_KEYS = 256;
    /**
     * Array with the keys marked as pressed or not.
     */
    private static boolean[] keys;
    
    /**
     * Mouse pressed state.
     */
    private static boolean mousePressed; // add this line
    
    /**
     * Singleton instance of the class.
     */
    private static InputManager instance;
    /**
     * Last character typed.
     */
    private static char lastCharTyped;
    /**
     * Flag to check if a character was typed.
     */
    private static boolean charTyped;
    
    // add three variable
    
    private static int mouseX;
    private static int mouseY;
    private static boolean mouseClicked;
    
    /**
     * Declare variables to save and return input keys
     */
    private int lastPressedKey = -1;
    private static final String KEY_CONFIG_FILE = "game_data/keyconfig.csv";
    
    protected static int[] player1Keys;
    protected static int[] player2Keys;
    
    public void setPlayer1Keys(int[] newKeys) {
        player1Keys = newKeys.clone();
    }
    
    public int[] getPlayer1Keys() {
        return player1Keys.clone();
    }
    
    public void setPlayer2Keys(int[] newKeys) {
        player2Keys = newKeys.clone();
    }
    
    public int[] getPlayer2Keys() {
        return player2Keys.clone();
    }
    
    /**
     * Private constructor.
     */
    private InputManager() {
        keys = new boolean[NUM_KEYS];
        lastCharTyped = '\0';
        charTyped = false;
    }
    
    // Player 1 item use key (player1Keys[3])
    public boolean isP1UseItemPressed() {
        return isKeyDown(player1Keys[3]);
    }
    
    // Player 2 item use key (player2Keys[3])
    public boolean isP2UseItemPressed() {
        return isKeyDown(player2Keys[3]);
    }
    
    /**
     * Returns shared instance of InputManager.
     *
     * @return Shared instance of InputManager.
     */
    public static InputManager getInstance() {
        if (instance == null) {
            instance = new InputManager();
        }
        return instance;
    }
    
    /**
     * Returns the last character typed and resets the flag.
     *
     * @return Last character typed, or '\0' if none.
     */
    public char getLastCharTyped() {
        if (charTyped) {
            charTyped = false;
            return lastCharTyped;
        }
        return '\0';
    }
    
    /**
     * Returns the last character typed by the user and consumes it.
     *
     * @return The last typed character, or '\0' (null char) if none.
     */
    public static char getLastChar() {
        char typedChar = lastCharTyped;
        lastCharTyped = '\0';
        return typedChar;
    }
    
    
    /**
     * Returns true if the provided key is currently pressed.
     *
     * @param keyCode Key number to check.
     * @return Key state.
     */
    public boolean isKeyDown(final int keyCode) {
        return keys[keyCode];
    }
    
    // === PLAYER 1 CONTROLS (Existing functionality) ===
    // Player 1 uses WASD + Spacebar configuration
    
    /**
     * Checks if Player 1's move left key (player1Keys[0]) is pressed.
     *
     * @return True if Player 1 is moving left
     */
    
    public boolean isP1LeftPressed() {
        return isKeyDown(player1Keys[0]);
    }
    
    /**
     * Checks if Player 1's move right key (player1Keys[1]) is pressed.
     *
     * @return True if Player 1 is moving right
     */
    
    public boolean isP1RightPressed() {
        return isKeyDown(player1Keys[1]);
    }
    
    /**
     * Checks if Player 1's shoot key (player1keys[2]) is pressed.
     *
     * @return True if Player 1 is shooting
     */
    
    public boolean isP1ShootPressed() {
        return isKeyDown(player1Keys[2]);
    }
    
    // ==================== PLAYER 2 CONTROLS ====================
    // Player 2 uses Arrow Keys + Enter configuration
    // Added for two-player mode implementation
    
    /**
     * Checks if Player 2's move left key (player2keys[0] is pressed.
     *
     * @return True if Player 2 is moving left
     */
    
    public boolean isP2LeftPressed() {
        return isKeyDown(player2Keys[0]);
    }
    
    /**
     * Checks if Player 2's move right key (player2keys[1]) is pressed.
     *
     * @return True if Player 2 is moving right
     */
    
    public boolean isP2RightPressed() {
        return isKeyDown(player2Keys[1]);
    }
    
    /**
     * Checks if Player 2's shoot key (player2Keys[2]) is pressed.
     *
     * @return True if Player 2 is shooting
     */
    public boolean isP2ShootPressed() {
        return isKeyDown(player2Keys[2]);
    }
    
    /**
     * Changes the state of the key to pressed.
     *
     * @param key Key pressed.
     */
    @Override
    public void keyPressed(final KeyEvent key) {
        int code = key.getKeyCode();
        if (code >= 0 && code < NUM_KEYS) {
            
            if (!keys[code]) {
                lastPressedKey = code;
            }
            
            keys[code] = true;
        }
    }
    
    /**
     * Changes the state of the key to not pressed.
     *
     * @param key Key released.
     */
    @Override
    public void keyReleased(final KeyEvent key) {
        if (key.getKeyCode() >= 0 && key.getKeyCode() < NUM_KEYS) {
            keys[key.getKeyCode()] = false;
        }
    }
    
    /**
     * Does nothing.
     *
     * @param key Key typed.
     */
    @Override
    public void keyTyped(final KeyEvent key) {
        lastCharTyped = key.getKeyChar();
        charTyped = true;
    }
    
    // Save and return the last pressed key
    public int getLastPressedKey() {
        int temp = lastPressedKey;
        lastPressedKey = -1;
        return temp;
    }
    
    /**
     * Clears any pending key or character input. (Prevents unintended key carry-over between
     * screens)
     */
    public void clearLastKey() {
        lastCharTyped = '\0';
        charTyped = false;
    }
    
    // Create and return a project path/res/keyconfig.csv file object
    private File getKeyConfigFile() {
        String projectPath = System.getProperty("user.dir");
        String filePath = projectPath + File.separator + KEY_CONFIG_FILE;
        
        File file = new File(filePath);
        File folder = file.getParentFile(); // "game_data" 폴더
        if (folder != null && !folder.exists()) {
            folder.mkdirs(); // "game_data" 폴더가 없으면 생성
        }
        
        return file;
    }
    
    // write a key code in a keyconfig.csv file
    public void saveKeyConfig() {
        try {
            File file = getKeyConfigFile();
            File folder = file.getParentFile();
            if (!folder.exists()) {
                folder.mkdirs();
            }
            
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
                writer.write(player1Keys[0] + "," + player1Keys[1] + "," + player1Keys[2] + ","
                    + player1Keys[3]);
                writer.newLine();
                writer.write(player2Keys[0] + "," + player2Keys[1] + "," + player2Keys[2] + ","
                    + player2Keys[3]);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    // Import a file and change the saved input key code
    public void loadKeyConfig() {
        File file = getKeyConfigFile();
        
        if (!file.exists()) {
            return;
        }
        
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line1 = reader.readLine();
            String line2 = reader.readLine();
            if (line1 != null) {
                String[] parts = line1.split(",");
                if (parts.length >= 4) {
                    for (int i = 0; i < 4; i++) {
                        player1Keys[i] = Integer.parseInt(parts[i]);
                    }
                }
            }
            if (line2 != null) {
                String[] parts = line2.split(",");
                if (parts.length >= 4) {
                    for (int i = 0; i < 4; i++) {
                        player2Keys[i] = Integer.parseInt(parts[i]);
                    }
                }
            }
            
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    /**
     * After setting the default, import the saved key settings from the file and cover the default values
     */
    static {
        instance = new InputManager();
        player1Keys = new int[]{KeyEvent.VK_A, KeyEvent.VK_D, KeyEvent.VK_SPACE, KeyEvent.VK_Q};
        player2Keys = new int[]{KeyEvent.VK_LEFT, KeyEvent.VK_RIGHT, KeyEvent.VK_ENTER,
            KeyEvent.VK_SLASH};
        
        instance.loadKeyConfig();
    }
    
    /**
     * Resets all key states to not pressed.
     */
    public static void resetKeys() {
        for (int i = 0; i < NUM_KEYS; i++) {
            keys[i] = false;
        }
    }
    
    
    public int getMouseX() {
        return mouseX;
    } // add this function
    
    public int getMouseY() {
        return mouseY;
    } // add this function
    
    public boolean isMouseClicked() { // add this function
        if (mouseClicked) {
            mouseClicked = false;
            return true;
        }
        return false;
    }
    
    @Override
    public void mouseClicked(final MouseEvent e) { // add this function
        // Can be left empty or used if needed
    }
    
    @Override
    public void mousePressed(final MouseEvent e) { // add this function
        mousePressed = true;
        mouseX = e.getX();
        mouseY = e.getY();
    }
    
    @Override
    public void mouseReleased(final MouseEvent e) { // add this function
        mousePressed = false;
        mouseX = e.getX();
        mouseY = e.getY();
        mouseClicked = true;
    }
    
    @Override
    public void mouseEntered(final MouseEvent e) { // add this function
    
    }
    
    @Override
    public void mouseExited(final MouseEvent e) { // add this function
    
    }
    
    /**
     * Added mouse move/drag event to update mouse position right now
     */
    @Override
    public void mouseMoved(final MouseEvent e) {
        mouseX = e.getX();
        mouseY = e.getY();
    }
    
    @Override
    public void mouseDragged(final MouseEvent e) {
        mouseX = e.getX();
        mouseY = e.getY();
    }
    
    public boolean isMousePressed() {
        return mousePressed;
    }
    
    public String getKeyString(int keyCode) {
        switch (keyCode) {
            case KeyEvent.VK_LEFT:
                return "LEFT";
            case KeyEvent.VK_RIGHT:
                return "RIGHT";
            case KeyEvent.VK_UP:
                return "UP";
            case KeyEvent.VK_DOWN:
                return "DOWN";
            case KeyEvent.VK_SPACE:
                return "SPACE";
            case KeyEvent.VK_ENTER:
                return "ENTER";
            case KeyEvent.VK_BACK_SPACE:
                return "BACK_SPACE";
            case KeyEvent.VK_TAB:
                return "TAB";
            case KeyEvent.VK_CANCEL:
                return "CANCEL";
            case KeyEvent.VK_F1:
                return "F1";
            case KeyEvent.VK_F2:
                return "F2";
            case KeyEvent.VK_F3:
                return "F3";
            case KeyEvent.VK_F4:
                return "F4";
            case KeyEvent.VK_F5:
                return "F5";
            case KeyEvent.VK_F6:
                return "F6";
            case KeyEvent.VK_F7:
                return "F7";
            case KeyEvent.VK_F8:
                return "F8";
            case KeyEvent.VK_F9:
                return "F9";
            case KeyEvent.VK_F10:
                return "F10";
            case KeyEvent.VK_F11:
                return "F11";
            case KeyEvent.VK_F12:
                return "F12";
            default:
                return KeyEvent.getKeyText(keyCode).toUpperCase();
        }
    }
    
}