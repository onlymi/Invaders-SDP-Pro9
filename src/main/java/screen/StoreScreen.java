package screen;

import engine.Core;
import engine.UserStats;
import engine.utils.Cooldown;
import java.awt.event.KeyEvent;

public class StoreScreen extends Screen {
    
    private static final int SELECTION_TIME = 200;
    private Cooldown selectionCooldown;
    private int menuIndex = 0; // Stats 0~7
    private UserStats userStats;
    
    private final String[] statNames = {"Health", "Mana", "Speed", "Damage", "Atk Speed",
        "Atk Range", "Critical Chance", "Defence"};
    
    private final int MAX_LEVEL = 3;
    
    private final int BASE_COST = 100;
    
    /**
     * Constructor, establishes the properties of the screen.
     *
     * @param width  Screen width.
     * @param height Screen height.
     * @param fps    Frames per second, frame rate at which the game is run.
     */
    public StoreScreen(int width, int height, int fps) {
        super(width, height, fps);
        this.returnCode = 1;
        this.selectionCooldown = Core.getCooldown(SELECTION_TIME);
        this.selectionCooldown.reset();
        this.userStats = Core.getUserStats();
    }
    
    public int getMaxStatLevel() {
        return MAX_LEVEL;
    }
    
    public int getBaseCost(int currentLevel) {
        return BASE_COST * (currentLevel + 1);
    }
    
    public final int run() {
        super.run();
        return this.returnCode;
    }
    
    protected final void update() {
        super.update();
        draw();
        
        if (this.selectionCooldown.checkFinished() && this.inputDelay.checkFinished()) {
            if (inputManager.isKeyDown(KeyEvent.VK_LEFT) || inputManager.isKeyDown(KeyEvent.VK_A)) {
                if (menuIndex > 0) {
                    menuIndex--;
                } else {
                    menuIndex = 7;
                }
                selectionCooldown.reset();
                this.soundManager.playOnce("hover");
            }
            if (inputManager.isKeyDown(KeyEvent.VK_RIGHT) || inputManager.isKeyDown(
                KeyEvent.VK_D)) {
                if (menuIndex < statNames.length - 1) {
                    menuIndex++;
                } else {
                    menuIndex = 0;
                }
                selectionCooldown.reset();
                this.soundManager.playOnce("hover");
            }
            if (inputManager.isKeyDown(KeyEvent.VK_UP) || inputManager.isKeyDown(KeyEvent.VK_W)
                || inputManager.isKeyDown(KeyEvent.VK_DOWN) || inputManager.isKeyDown(
                KeyEvent.VK_S)) {
                if (menuIndex >= statNames.length / 2) {
                    menuIndex -= (statNames.length / 2);
                } else {
                    menuIndex += statNames.length / 2;
                }
                selectionCooldown.reset();
                this.soundManager.playOnce("hover");
            }
            // 구매 시도
            if (inputManager.isKeyDown(KeyEvent.VK_ENTER) || inputManager.isKeyDown(
                KeyEvent.VK_SPACE)) {
                tryPurchase(menuIndex);
                selectionCooldown.reset();
                this.soundManager.playOnce("hover");
            }
            
            if (inputManager.isKeyDown(KeyEvent.VK_ESCAPE)) {
                this.returnCode = 1;
                isRunning = false;
            }
        }
    }
    
    private void tryPurchase(int menuIndex) {
        int currentLevel = userStats.getStatLevel(menuIndex);
        if (currentLevel >= getMaxStatLevel()) {
            return;
        }
        
        int cost = getBaseCost(currentLevel);
        if (userStats.spendCoin(cost)) {
            userStats.upgradeStat(menuIndex);
            this.soundManager.playOnce("achievement");
            try {
                Core.getFileManager().saveUserStats(userStats);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            this.soundManager.playOnce("lose");
        }
    }
    
    private void draw() {
        drawManager.initDrawing(this);
        drawManager.getStoreScreenRenderer()
            .draw(drawManager.getBackBufferGraphics(), this, userStats, menuIndex);
        drawManager.completeDrawing(this);
    }
}
