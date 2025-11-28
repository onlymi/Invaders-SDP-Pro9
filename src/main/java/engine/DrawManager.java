package engine;

import animations.Explosion;
import engine.gameplay.item.ItemManager;
import engine.renderer.AchievementScreenRenderer;
import engine.renderer.AuthScreenRenderer;
import engine.renderer.CommonRenderer;
import engine.renderer.EntityRenderer;
import engine.renderer.GameScreenRenderer;
import engine.renderer.HighScoreScreenRenderer;
import engine.renderer.LogInScreenRenderer;
import engine.renderer.PlayModeSelectionScreenRenderer;
import engine.renderer.PlayerSelectionScreenRenderer;
import engine.renderer.ScoreScreenRenderer;
import engine.renderer.SettingScreenRenderer;
import engine.renderer.SignUpScreenRenderer;
import engine.renderer.StoreScreenRenderer;
import engine.renderer.TitleScreenRenderer;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;
import screen.Screen;

/**
 * Manages screen drawing.
 *
 * @author <a href="mailto:RobertoIA1987@gmail.com">Roberto Izquierdo Amo</a>
 *
 */
public final class DrawManager {
    
    /**
     * Singleton instance of the class.
     */
    private static DrawManager instance;
    /**
     * Current frame.
     */
    private static Frame frame;
    /**
     * FileManager instance.
     */
    private static FileManager fileManager;
    /**
     * Application logger.
     */
    private final Logger LOGGER;
    /**
     * Graphics context.
     */
    private static Graphics graphics;
    /**
     * Buffer Graphics.
     */
    private static Graphics backBufferGraphics;
    /**
     * Buffer image.
     */
    private static BufferedImage backBuffer;
    /**
     * Normal-sized font.
     */
    private static Font fontRegular;
    /**
     * Normal-sized font properties.
     */
    private static FontMetrics fontRegularMetrics;
    /**
     * Big sized font.
     */
    private static Font fontBig;
    /**
     * Big sized font properties.
     */
    private static FontMetrics fontBigMetrics;
    
    private AssetManager assetManager;
    // Common Renderer
    private CommonRenderer commonRenderer;
    private EntityRenderer entityRenderer;
    // Screen Renderer
    private TitleScreenRenderer titleScreenRenderer;
    private AchievementScreenRenderer achievementScreenRenderer;
    private HighScoreScreenRenderer highScoreScreenRenderer;
    private SettingScreenRenderer settingScreenRenderer;
    private PlayModeSelectionScreenRenderer playModeSelectionScreenRenderer;
    private PlayerSelectionScreenRenderer playerSelectionScreenRenderer;
    private GameScreenRenderer gameScreenRenderer;
    private ScoreScreenRenderer scoreScreenRenderer;
    private AuthScreenRenderer authScreenRenderer;
    private SignUpScreenRenderer signUpScreenRenderer;
    private LogInScreenRenderer logInScreenRenderer;
    private StoreScreenRenderer storeScreenRenderer;
    
    private final List<Explosion> explosions = new ArrayList<>();
    
    /**
     * Stars background animations for both game and main menu Star density specified as argument.
     */
    int explosion_size = 2;
    
    /**
     * Private constructor.
     */
    private DrawManager() {
        // Assets(resources) loader
        this.assetManager = AssetManager.getInstance();
        // Common Renderer of all screen
        this.commonRenderer = new CommonRenderer();
        // Renderer of entity
        this.entityRenderer = new EntityRenderer(this.commonRenderer);
        // Renderer of each screens
        this.titleScreenRenderer = new TitleScreenRenderer(this.commonRenderer);
        this.achievementScreenRenderer = new AchievementScreenRenderer(this.commonRenderer);
        this.highScoreScreenRenderer = new HighScoreScreenRenderer(this.commonRenderer);
        this.settingScreenRenderer = new SettingScreenRenderer(this.commonRenderer);
        this.playModeSelectionScreenRenderer = new PlayModeSelectionScreenRenderer(
            this.commonRenderer);
        this.playerSelectionScreenRenderer = new PlayerSelectionScreenRenderer(this.commonRenderer);
        this.gameScreenRenderer = new GameScreenRenderer(this.commonRenderer,
            ItemManager.getInstance());
        this.scoreScreenRenderer = new ScoreScreenRenderer(this.commonRenderer);
        this.authScreenRenderer = new AuthScreenRenderer(this.commonRenderer);
        this.signUpScreenRenderer = new SignUpScreenRenderer(this.commonRenderer);
        this.logInScreenRenderer = new LogInScreenRenderer(this.commonRenderer);
        this.storeScreenRenderer = new StoreScreenRenderer(this.commonRenderer);
        
        fontRegular = this.assetManager.getFontRegular();
        fontBig = this.assetManager.getFontBig();
        
        this.LOGGER = Core.getLogger();
        LOGGER.info("Started loading resources.");
    }
    
    /**
     * Returns shared instance of DrawManager.
     *
     * @return Shared instance of DrawManager.
     */
    public static DrawManager getInstance() {
        if (instance == null) {
            instance = new DrawManager();
        }
        return instance;
    }
    
    public static Frame getFrame() {
        return frame;
    }
    
    public Graphics getBackBufferGraphics() {
        return backBufferGraphics;
    }
    
    public CommonRenderer getCommonRenderer() {
        return this.commonRenderer;
    }
    
    public EntityRenderer getEntityRenderer() {
        return this.entityRenderer;
    }
    
    public TitleScreenRenderer getTitleScreenRenderer() {
        return this.titleScreenRenderer;
    }
    
    public AchievementScreenRenderer getAchievementScreenRenderer() {
        return this.achievementScreenRenderer;
    }
    
    public HighScoreScreenRenderer getHighScoreScreenRenderer() {
        return this.highScoreScreenRenderer;
    }
    
    public SettingScreenRenderer getSettingScreenRenderer() {
        return this.settingScreenRenderer;
    }
    
    public PlayModeSelectionScreenRenderer getPlayModeSelectionScreenRenderer() {
        return this.playModeSelectionScreenRenderer;
    }
    
    public PlayerSelectionScreenRenderer getShipSelectionMenuRenderer() {
        return this.playerSelectionScreenRenderer;
    }
    
    public GameScreenRenderer getGameScreenRenderer() {
        return this.gameScreenRenderer;
    }
    
    public ScoreScreenRenderer getScoreScreenRenderer() {
        return this.scoreScreenRenderer;
    }
    
    public AuthScreenRenderer getAuthScreenRenderer() {
        return this.authScreenRenderer;
    }
    
    public SignUpScreenRenderer getSignUpScreenRenderer() {
        return this.signUpScreenRenderer;
    }
    
    public LogInScreenRenderer getLogInScreenRenderer() {
        return this.logInScreenRenderer;
    }
    
    public StoreScreenRenderer getStoreScreenRenderer() {
        return this.storeScreenRenderer;
    }
    
    /**
     * Sets the frame to draw the image on.
     *
     * @param currentFrame Frame to draw on.
     */
    public void setFrame(final Frame currentFrame) {
        frame = currentFrame;
    }
    
    /**
     * First part of the drawing process. Initialises buffers, draws the background and prepares the
     * images.
     *
     * @param screen Screen to draw in.
     */
    public void initDrawing(final Screen screen) {
        backBuffer = new BufferedImage(screen.getWidth(), screen.getHeight(),
            BufferedImage.TYPE_INT_RGB);
        
        graphics = frame.getGraphics();
        backBufferGraphics = backBuffer.getGraphics();
        
        backBufferGraphics.setColor(Color.BLACK);
        backBufferGraphics.fillRect(0, 0, screen.getWidth(), screen.getHeight());
        
        fontRegularMetrics = backBufferGraphics.getFontMetrics(fontRegular);
        fontBigMetrics = backBufferGraphics.getFontMetrics(fontBig);
        
        // drawBorders(screen);
        // drawGrid(screen);
    }
    
    /**
     * Draws the completed drawing on screen.
     *
     * @param screen Screen to draw on.
     */
    public void completeDrawing(final Screen screen) {
        graphics.drawImage(backBuffer, frame.getInsets().left, frame.getInsets().top, frame);
    }
    
    
    /**
     * For debugging purposes, draws the canvas borders.
     *
     * @param screen Screen to draw in.
     */
    @SuppressWarnings("unused")
    private void drawBorders(final Screen screen) {
        backBufferGraphics.setColor(Color.GREEN);
        backBufferGraphics.drawLine(0, 0, screen.getWidth() - 1, 0);
        backBufferGraphics.drawLine(0, 0, 0, screen.getHeight() - 1);
        backBufferGraphics.drawLine(screen.getWidth() - 1, 0, screen.getWidth() - 1,
            screen.getHeight() - 1);
        backBufferGraphics.drawLine(0, screen.getHeight() - 1, screen.getWidth() - 1,
            screen.getHeight() - 1);
    }
    
    /**
     * For debugging purposes, draws a grid over the canvas.
     *
     * @param screen Screen to draw in.
     */
    @SuppressWarnings("unused")
    private void drawGrid(final Screen screen) {
        backBufferGraphics.setColor(Color.DARK_GRAY);
        for (int i = 0; i < screen.getHeight() - 1; i += 2) {
            backBufferGraphics.drawLine(0, i, screen.getWidth() - 1, i);
        }
        for (int j = 0; j < screen.getWidth() - 1; j += 2) {
            backBufferGraphics.drawLine(j, 0, j, screen.getHeight() - 1);
        }
    }
}
