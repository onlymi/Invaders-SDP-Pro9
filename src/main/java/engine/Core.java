package engine;

import engine.gameplay.achievement.AchievementManager;
import engine.hitbox.HitboxManager;
import engine.utils.Cooldown;
import engine.utils.MinimalFormatter;
import entity.Ship;
import screen.*;

import java.io.IOException;
import java.util.List;
import java.util.logging.*;


/**
 * Implements core game logic.
 *
 * @author <a href="mailto:RobertoIA1987@gmail.com">Roberto Izquierdo Amo</a>
 *
 */
public final class Core {

    public static final int WIDTH = 1200;
    public static final int HEIGHT = 800;
    private static final int FPS = 60;

    /**
     * Lives per player (used to compute team pool in shared mode).
     */
    private static final int MAX_LIVES = 3;
    private static final int EXTRA_LIFE_FREQUENCY = 3;

    /**
     * Frame to draw the screen on.
     */
    private static Frame frame;
    private static Screen currentScreen;
    private static List<GameSettings> gameSettings;
    private static final Logger LOGGER = Logger.getLogger(Core.class.getSimpleName());
    private static Handler fileHandler;
    private static ConsoleHandler consoleHandler;

    /**
     * Test implementation.
     *
     * @param args Program args, ignored.
     */
    public static void main(final String[] args) throws IOException {
        try {
            LOGGER.setUseParentHandlers(false);
            fileHandler = new FileHandler("log");
            fileHandler.setFormatter(new MinimalFormatter());
            consoleHandler = new ConsoleHandler();
            consoleHandler.setFormatter(new MinimalFormatter());
            LOGGER.addHandler(fileHandler);
            LOGGER.addHandler(consoleHandler);
            LOGGER.setLevel(Level.ALL);
        } catch (Exception e) {
            e.printStackTrace();
        }

        frame = new Frame(WIDTH, HEIGHT);
        InputManager input = InputManager.getInstance();
        frame.addKeyListener(
                input); // Register an instance to allow the window to receive keyboard event information
        DrawManager.getInstance().setFrame(frame);
        int width = frame.getWidth();
        int height = frame.getHeight();

        gameSettings = GameSettings.getGameSettings();
        int NUM_LEVELS = gameSettings.size(); // Initialize total number of levels

        // 2P mode: modified to null to allow for switch between 2 modes
        GameState gameState = null;
        boolean coopSelected = false; // false = 1-player mode, true = 2-player mode

        int returnCode = 9;

        Ship.ShipType shipTypeP1 = Ship.ShipType.NORMAL; // Player 1 Ship Type
        Ship.ShipType shipTypeP2 = Ship.ShipType.NORMAL; // Player 2 Ship Type
        SystemData systemData;
        do {
            // Game Start
            switch (returnCode) {
                case 1:
                    // Title Screen
                    systemData = titleSystem(width, height);
                    returnCode = systemData.returnCode;
                    coopSelected = systemData.coopSelected;
                    break;
                case 2:
                    // In game screen
                    systemData = gamePlaySystem(width, height, coopSelected, shipTypeP1,
                            shipTypeP2);
                    coopSelected = systemData.coopSelected;
                    shipTypeP1 = systemData.shipTypeP1;
                    shipTypeP2 = systemData.shipTypeP2;
                    returnCode = systemData.returnCode;
                    LOGGER.info("Closing game screen.");
                    break;
                case 3:
                    // Achievement screen
                    returnCode = achievementSystem(width, height);
                    LOGGER.info("Closing achievement screen.");
                    break;
                case 4:
                    // Setting screen
                    returnCode = settingSystem(width, height);
                    LOGGER.info("Closing setting screen.");
                    break;
                case 5:
                    // Play mode selection screen about 1 player mode or 2 player mode
                    systemData = playModeSelectionSystem(width, height);
                    returnCode = systemData.returnCode;
                    coopSelected = systemData.coopSelected;
                    LOGGER.info("Closing play screen.");
                    break;
                case 6:
                    // Ship selection for Player 1
                    systemData = shipSelectionSystem(width, height, 1, coopSelected);
                    shipTypeP1 = systemData.shipTypeP1;
                    returnCode = systemData.returnCode;
                    LOGGER.info("Closing first player ship selection screen.");
                    break;
                case 7:
                    // Ship selection for Player 2
                    systemData = shipSelectionSystem(width, height, 2, coopSelected);
                    shipTypeP2 = systemData.shipTypeP2;
                    returnCode = systemData.returnCode;
                    LOGGER.info("Closing second player ship selection screen.");
                    break;
                case 8:
                    // High score screen
                    returnCode = highScoreSystem(width, height);
                    LOGGER.info("Closing high score screen.");
                    break;
                case 9:
                    // Auth screen (Sign up / Log in)
                    returnCode = authSystem(width, height);
                    LOGGER.info("Closing auth system screen.");
                    break;
                case 10:
                    // Sign Up screen
                    returnCode = signUpSystem(width, height);
                    LOGGER.info("Closing sign up screen.");
                    break;
                default:
                    break;
            }

        } while (returnCode != 0);

        fileHandler.flush();
        fileHandler.close();
        System.exit(0);
    }

    /**
     * Constructor, not called.
     */
    private Core() {

    }

    /**
     * Controls access to the logger.
     *
     * @return Application logger.
     */
    public static Logger getLogger() {
        return LOGGER;
    }

    /**
     * Controls access to the draw manager.
     *
     * @return Application draw manager.
     */
    public static DrawManager getDrawManager() {
        return DrawManager.getInstance();
    }

    /**
     * Controls access to the input manager.
     *
     * @return Application input manager.
     */
    public static InputManager getInputManager() {
        return InputManager.getInstance();
    }

    /**
     * Controls access to the file manager.
     *
     * @return Application file manager.
     */
    public static FileManager getFileManager() {
        return FileManager.getInstance();
    }

    /**
     * Controls access to the sound manager.
     *
     * @return Application sound manager.
     */
    public static SoundManager getSoundManager() {
        return SoundManager.getInstance();
    }

    /**
     * Controls access to the asset manager.
     *
     * @return Application asset manager.
     */
    public static AssetManager getAssetManager() {
        return AssetManager.getInstance();
    }

    /**
     * Controls access to the achievement manager.
     *
     * @return Application achievement manager.
     */
    public static AchievementManager getAchievementManager() {
        return AchievementManager.getInstance();
    }

    /**
     * Controls access to the hitboxManager manager.
     *
     * @return Application hitboxManager manager.
     */
    public static HitboxManager getHitboxManager() {
        return HitboxManager.getInstance();
    }

    /**
     * Controls creation of new cooldowns.
     *
     * @param milliseconds Duration of the cooldown.
     * @return A new cooldown.
     */
    public static Cooldown getCooldown(final int milliseconds) {
        return new Cooldown(milliseconds);
    }

    /**
     * Controls creation of new cooldowns with variance.
     *
     * @param milliseconds Duration of the cooldown.
     * @param variance     Variation in the cooldown duration.
     * @return A new cooldown with variance.
     */
    public static Cooldown getVariableCooldown(final int milliseconds, final int variance) {
        return new Cooldown(milliseconds, variance);
    }

    private static int volumeLevel = 50;

    public static int getVolumeLevel() {
        return volumeLevel;
    }

    public static void setVolumeLevel(int v) {
        volumeLevel = Math.max(0, Math.min(100, v));
    }

    // Class for screen system
    private static class SystemData {

        int returnCode;
        boolean coopSelected;
        Ship.ShipType shipTypeP1;
        Ship.ShipType shipTypeP2;

        public SystemData(int returnCode, boolean coopSelected, Ship.ShipType shipTypeP1,
                          Ship.ShipType shipTypeP2) {
            this.returnCode = returnCode;
            this.coopSelected = coopSelected;
            this.shipTypeP1 = shipTypeP1;
            this.shipTypeP2 = shipTypeP2;
        }
    }

    /**
     * Activate title screen system.
     *
     * @param width  Title screen contents box width
     * @param height Title screen contents box height
     * @return Next return code and coop selected
     */
    public static SystemData titleSystem(int width, int height) {
        SystemData systemData = new SystemData(0, false, null, null);
        currentScreen = new TitleScreen(width, height, FPS);
        LOGGER.info("Starting " + WIDTH + "x" + HEIGHT + " title screen at " + FPS + " fps.");
        systemData.returnCode = frame.setScreen(currentScreen);
        LOGGER.info("Closing title screen.");

        if (systemData.returnCode == 2) {
            currentScreen = new PlayModeSelectionScreen(width, height, FPS);
            systemData.returnCode = frame.setScreen(currentScreen);
            systemData.coopSelected = ((PlayModeSelectionScreen) currentScreen).isCoopSelected();
        }
        return systemData;
    }

    /**
     * Activate achievement screen system.
     *
     * @param width  Achievement screen contents box width
     * @param height Achievement screen contents box height
     * @return Next return code
     */
    public static int achievementSystem(int width, int height) {
        currentScreen = new AchievementScreen(width, height, FPS);
        LOGGER.info("Starting " + WIDTH + "x" + HEIGHT
                + " achievements screen at " + FPS + " fps.");
        return frame.setScreen(currentScreen);
    }

    /**
     * Activate ship selection screen system.
     *
     * @param width        Ship selection screen contents box width
     * @param height       Ship selection screen contents box height
     * @param coopSelected 2 player mode or not
     * @param shipTypeP1   Ship type of player 1
     * @param shipTypeP2   Ship type of player 2
     * @return Next return code and initial coop and ship type
     */
    public static SystemData gamePlaySystem(int width, int height, boolean coopSelected,
                                            Ship.ShipType shipTypeP1, Ship.ShipType shipTypeP2) throws IOException {
        SystemData systemData = new SystemData(0, coopSelected, shipTypeP1, shipTypeP2);
        GameState gameState = new GameState(1, MAX_LIVES, coopSelected, 0);
        AchievementManager achievementManager = new AchievementManager(); // 1p, 2p achievement manager

        do {
            int teamCap = gameState.isCoop() ? (MAX_LIVES * GameState.NUM_PLAYERS) : MAX_LIVES;
            boolean bonusLife = gameState.getLevel() % EXTRA_LIFE_FREQUENCY == 0
                    && gameState.getLivesRemaining() < teamCap;

            currentScreen = new GameScreen(gameState, gameSettings.get(gameState.getLevel() - 1),
                    bonusLife, width, height, FPS,
                    shipTypeP1, shipTypeP2, achievementManager);

            LOGGER.info("Starting " + WIDTH + "x" + HEIGHT + " game screen at " + FPS + " fps.");
            systemData.returnCode = frame.setScreen(currentScreen);
            LOGGER.info("Closing game screen.");
            if (systemData.returnCode == 1) {
                break;
            }

            gameState = ((GameScreen) currentScreen).getGameState();

            if (gameState.teamAlive()) {
                gameState.nextLevel();
            }

        } while (gameState.teamAlive() && gameState.getLevel() <= gameSettings.size());

        if (systemData.returnCode == 1) {
            systemData.shipTypeP1 = Ship.ShipType.NORMAL;
            systemData.shipTypeP2 = Ship.ShipType.NORMAL;
            systemData.coopSelected = false;
            return systemData;
        }

        LOGGER.info("Starting " + WIDTH + "x" + HEIGHT + " score screen at " + FPS
                + " fps, with a score of "
                + gameState.getScore() + ", "
                + gameState.getLivesRemaining() + " lives remaining, "
                + gameState.getBulletsShot() + " bullets shot and "
                + gameState.getShipsDestroyed() + " ships destroyed.");
        currentScreen = new ScoreScreen(width, height, FPS, gameState, achievementManager);
        systemData.returnCode = frame.setScreen(currentScreen);

        return systemData;
    }

    /**
     * Activate setting screen system.
     *
     * @param width  Setting screen contents box width
     * @param height Setting screen contents box height
     * @return Next return code
     */
    public static int settingSystem(int width, int height) {
        currentScreen = new SettingScreen(width, height, FPS);
        LOGGER.info("Starting " + WIDTH + "x" + HEIGHT
                + " setting screen at " + FPS + " fps.");
        frame.removeKeyListener(InputManager.getInstance());
        frame.addKeyListener(InputManager.getInstance());
        return frame.setScreen(currentScreen);
    }

    /**
     * Activate play mode selection screen system.
     *
     * @param width  Play mode selection screen contents box width
     * @param height Play mode selection screen contents box height
     * @return Next return code and coop selected
     */
    public static SystemData playModeSelectionSystem(int width, int height) {
        // Play : Use the play to decide 1p and 2p
        SystemData systemData = new SystemData(0, false, null, null);
        currentScreen = new PlayModeSelectionScreen(width, height, FPS);
        LOGGER.info("Starting " + WIDTH + "x" + HEIGHT + " play screen at " + FPS + " fps.");
        systemData.returnCode = frame.setScreen(currentScreen);
        systemData.coopSelected = ((PlayModeSelectionScreen) currentScreen).isCoopSelected();

        // Play screen -> Ship selection screen
        if (systemData.returnCode == 2) {
            systemData.returnCode = 6;
        }

        return systemData;
    }

    /**
     * Activate ship selection screen system.
     *
     * @param width        Ship selection screen contents box width
     * @param height       Ship selection screen contents box height
     * @param player_num   Number of player count
     * @param coopSelected 2 player mode or not
     * @return Next return code
     */
    public static SystemData shipSelectionSystem(int width, int height, int player_num,
                                                 boolean coopSelected) {
        SystemData systemData = new SystemData(0, coopSelected, null, null);

        currentScreen = new ShipSelectionScreen(width, height, FPS, player_num);
        systemData.returnCode = frame.setScreen(currentScreen);
        // Ship selection for Player 1.
        if (player_num == 1) {
            // If clicked back button, go back to the screen 1P screen -> Player select screen
            if (systemData.returnCode == 5) {
                return systemData;
            }

            systemData.shipTypeP1 = ((ShipSelectionScreen) currentScreen).getSelectedShipType();
            if (coopSelected) {
                systemData.returnCode = 7; // Go to Player 2 selection.
            } else {
                systemData.returnCode = 2; // Start game.
            }
        }

        // Ship selection for Player 2.
        if (player_num == 2) {
            // If clicked back button, go back to the screen 2P screen -> 1P screen
            if (systemData.returnCode == 6) {
                return systemData;
            }

            systemData.shipTypeP2 = ((ShipSelectionScreen) currentScreen).getSelectedShipType();
            systemData.returnCode = 2; // Start game.
        }

        return systemData;
    }

    /**
     * Activate high score screen system.
     *
     * @param width  High score screen contents box width
     * @param height High score screen contents box height
     * @return Next return code
     */
    public static int highScoreSystem(int width, int height) {
        currentScreen = new HighScoreScreen(width, height, FPS);
        LOGGER.info("Starting " + WIDTH + "x" + HEIGHT
                + " high score screen at " + FPS + " fps.");
        return frame.setScreen(currentScreen);
    }

    /**
     * Activate auth screen system.
     *
     * @param width  Auth screen contents box width
     * @param height Auth screen contents box height
     * @return Next return code
     */
    public static int authSystem(int width, int height) {
        currentScreen = new AuthScreen(width, height, FPS);
        LOGGER.info("Starting " + WIDTH + "x" + HEIGHT
                + " auth screen at " + FPS + " fps.");
        return frame.setScreen(currentScreen);
    }

    /**
     * Activate sign up screen system.
     *
     * @ param width Sign up screen contents box width
     * @ param height Sign up screen contents box height
     * @ return Next return code
     */
    public static int signUpSystem(int width, int height) {
        currentScreen = new SignUpScreen(width, height, FPS);
        LOGGER.info("Starting " + WIDTH + "x" + HEIGHT
                + " sign up screen at " + FPS + " fps.");
        return frame.setScreen(currentScreen);
    }
}