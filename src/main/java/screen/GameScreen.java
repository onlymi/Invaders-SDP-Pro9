package screen;

import animations.BasicGameSpace;
import engine.Core;
import engine.GameSettings;
import engine.GameState;
import engine.SoundManager;
import engine.gameplay.achievement.AchievementManager;
import engine.gameplay.item.ActivationType;
import engine.gameplay.item.ItemManager;
import engine.utils.Cooldown;
import entity.Bullet;
import entity.BulletPool;
import entity.EnemyShip;
import entity.EnemyShipFormation;
import entity.Entity;
import entity.Item;
import entity.ItemPool;
import entity.Pet;
import entity.Ship;
import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.RadialGradientPaint;
import java.awt.RenderingHints;
import java.awt.event.KeyEvent;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * Implements the game screen, where the action happens.(supports co-op with shared team lives)
 *
 * @author <a href="mailto:RobertoIA1987@gmail.com">Roberto Izquierdo Amo</a>
 *
 */
public class GameScreen extends Screen {
    
    /**
     * Milliseconds until the screen accepts user input.
     */
    private static final int INPUT_DELAY = 6000;
    /**
     * Bonus score for each life remaining at the end of the level.
     */
    private static final int LIFE_SCORE = 100;
    /**
     * Minimum time between bonus ship's appearances.
     */
    private static final int BONUS_SHIP_INTERVAL = 20000;
    /**
     * Maximum variance in the time between bonus ship's appearances.
     */
    private static final int BONUS_SHIP_VARIANCE = 10000;
    /**
     * Time until bonus ship explosion disappears.
     */
    private static final int BONUS_SHIP_EXPLOSION = 500;
    /**
     * Time from finishing the level to screen change.
     */
    private static final int SCREEN_CHANGE_INTERVAL = 1500;
    /**
     * Height of the interface separation line.
     */
    private static final int SEPARATION_LINE_HEIGHT = 68;
    private static final int HIGH_SCORE_NOTICE_DURATION = 2000;
    private static boolean sessionHighScoreNotified = false;
    
    /**
     * For Check Achievement 2015-10-02 add new
     */
    private AchievementManager achievementManager;
    /**
     * Current game difficulty settings.
     */
    private GameSettings gameSettings;
    /**
     * BasicGameSpace for screen resizing.
     */
    private BasicGameSpace basicGameSpace;
    /**
     * EnemyShip for Multi-hit.
     */
    private EnemyShip bossShip;
    /**
     * Current difficulty level number.
     */
    private int level;
    /**
     * Formation of enemy ships.
     */
    private EnemyShipFormation enemyShipFormation;
    private EnemyShip enemyShipSpecial;
    /**
     * Formation of player ships.
     */
    private Ship[] ships = new Ship[GameState.NUM_PLAYERS];
    /**
     * Minimum time between bonus ship appearances.
     */
    private Cooldown enemyShipSpecialCooldown;
    /**
     * Time until bonus ship explosion disappears.
     */
    private Cooldown enemyShipSpecialExplosionCooldown;
    /**
     * Time from finishing the level to screen change.
     */
    private Cooldown screenFinishedCooldown;
    /**
     * Set of all bullets fired by on screen ships.
     */
    private Set<Bullet> bullets;
    /**
     * Set of all items spawned.
     */
    private Set<Item> items;
    private long gameStartTime;
    /**
     * Checks if the level is finished.
     */
    private boolean levelFinished;
    /**
     * Checks if a bonus life is received.
     */
    private boolean bonusLife;
    private int topScore;
    private boolean highScoreNotified;
    private long highScoreNoticeStartTime;
    
    private boolean isPaused;
    private Cooldown pauseCooldown;
    private Cooldown returnMenuCooldown;
    
    private int score;
    private int lives;
    private int bulletsShot;
    private int shipsDestroyed;
    private Ship ship;
    
    /**
     * checks if player took damage 2025-10-02 add new variable
     *
     */
    private boolean tookDamageThisLevel;
    private boolean countdownSoundPlayed = false;
    
    private final GameState state;
    
    private Ship.ShipType shipTypeP1;
    private Ship.ShipType shipTypeP2;
    
    private final java.util.Set<Pet> pets = new java.util.HashSet<>();
    
    private Ship shipP1;
    private Ship shipP2;
    
    /**
     * Constructor, establishes the properties of the screen.
     *
     * @param gameState          Current game state.
     * @param gameSettings       Current game settings.
     * @param bonusLife          Checks if a bonus life is awarded this level.
     * @param width              Screen width.
     * @param height             Screen height.
     * @param fps                Frames per second, frame rate at which the game is run.
     * @param shipTypeP1         Player 1's ship type.
     * @param shipTypeP2         Player 2's ship type.
     * @param achievementManager Achievement manager instance used to track and save player
     *                           achievements. 2025-10-03 add generator parameter and comment
     */
    public GameScreen(final GameState gameState,
        final GameSettings gameSettings, final boolean bonusLife,
        final int width, final int height, final int fps, final Ship.ShipType shipTypeP1,
        final Ship.ShipType shipTypeP2, final AchievementManager achievementManager) {
        super(width, height, fps);
        
        this.state = gameState;
        this.gameSettings = gameSettings;
        this.bonusLife = bonusLife;
        this.shipTypeP1 = shipTypeP1;
        this.shipTypeP2 = shipTypeP2;
        this.level = gameState.getLevel();
        this.score = gameState.getScore();
        this.lives = gameState.getLivesRemaining();
        if (this.bonusLife) {
            this.lives++;
        }
        this.bulletsShot = gameState.getBulletsShot();
        this.shipsDestroyed = gameState.getShipsDestroyed();
        
        this.achievementManager = achievementManager;
        this.tookDamageThisLevel = false;
        
        this.highScoreNotified = false;
        this.highScoreNoticeStartTime = 0;
        
        // 2P: bonus life adds to team pool + singleplayer mode
        if (this.bonusLife) {
            if (state.isSharedLives()) {
                state.addTeamLife(1); // two player
            } else {
                // 1P legacy: grant to P1
                state.addLife(0, 1);  // singleplayer
            }
        }
        // Ensure achievementManager is not null for popup system
        if (this.achievementManager == null) {
            this.achievementManager = new AchievementManager();
        }
    }
    
    /**
     * Resets the session high score notification flag. Should be called when a new game starts from
     * the main menu.
     */
    public static void resetSessionHighScoreNotified() {
        sessionHighScoreNotified = false;
    }
    
    /**
     * Initializes basic screen properties, and adds necessary elements.
     */
    public final void initialize() {
        super.initialize();
        
        state.clearAllEffects();
        
        // Start background music for gameplay
        soundManager.playLoop("game_theme");
        
        enemyShipFormation = new EnemyShipFormation(this.gameSettings);
        enemyShipFormation.attach(this);
        
        // 2P mode: create both ships, tagged to their respective teams
        this.ships[0] = new Ship(this.width / 2 - 60, this.height - 30, Entity.Team.PLAYER1,
            shipTypeP1, this.state); // P1
        this.ships[0].setPlayerId(1);
        
        // only allowing second ship to spawn when 2P mode is chosen
        if (state.isCoop()) {
            this.ships[1] = new Ship(this.width / 2 + 60, this.height - 30, Entity.Team.PLAYER2,
                shipTypeP2, this.state); // P2
            
            this.ships[1].setPlayerId(2);
        } else {
            this.ships[1] = null; // ensuring there's no P2 ship in 1P mode
        }
        
        this.enemyShipSpecialCooldown = Core.getVariableCooldown(BONUS_SHIP_INTERVAL,
            BONUS_SHIP_VARIANCE);
        this.enemyShipSpecialCooldown.reset();
        this.enemyShipSpecialExplosionCooldown = Core.getCooldown(BONUS_SHIP_EXPLOSION);
        this.screenFinishedCooldown = Core.getCooldown(SCREEN_CHANGE_INTERVAL);
        this.bullets = new HashSet<Bullet>();
        
        // New Item Code
        this.items = new HashSet<Item>();
        
        // New BasicGameSpace Code
        this.basicGameSpace = new BasicGameSpace(100, this.width, this.height);
        this.pets.clear();
        
        // Special input delay / countdown.
        this.gameStartTime = System.currentTimeMillis();
        this.inputDelay = Core.getCooldown(INPUT_DELAY);
        this.inputDelay.reset();
        
        this.isPaused = false;
        this.pauseCooldown = Core.getCooldown(300);
        this.returnMenuCooldown = Core.getCooldown(300);
    }
    
    /**
     * Starts the action.
     *
     * @return Next screen code.
     */
    public final int run() {
        super.run();
        
        // 2P mode: award bonus score for remaining TEAM lives
        state.addScore(0, LIFE_SCORE * state.getLivesRemaining());
        
        // Stop all music on exiting this screen
        SoundManager.stopAllMusic();
        
        this.LOGGER.info("Screen cleared with a score of " + state.getScore());
        return this.returnCode;
    }
    
    /**
     * Updates the elements on screen and checks for events.
     */
    protected final void update() {
        super.update();
        
        // Countdown beep once during pre-start
        if (!this.inputDelay.checkFinished() && !countdownSoundPlayed) {
            long elapsed = System.currentTimeMillis() - this.gameStartTime;
            if (elapsed > 1750) {
                SoundManager.playOnce("count_down_sound");
                countdownSoundPlayed = true;
            }
        }
        
        checkAchievement();
        if (this.inputDelay.checkFinished() && inputManager.isKeyDown(KeyEvent.VK_ESCAPE)
            && this.pauseCooldown.checkFinished()) {
            this.isPaused = !this.isPaused;
            this.pauseCooldown.reset();
            
            if (this.isPaused) {
                // Pause game music when pausing - no sound during pause
                SoundManager.loopStop();
            } else {
                // Resume game music when unpausing
                SoundManager.playLoop("game_theme");
            }
        }
        
        if (this.isPaused && inputManager.isKeyDown(KeyEvent.VK_BACK_SPACE)
            && this.returnMenuCooldown.checkFinished()) {
            SoundManager.playOnce("select");
            SoundManager.stopAllMusic(); // Stop all music before returning to menu
            returnCode = 1;
            this.isRunning = false;
        }
        
        if (!this.isPaused) {
            if (this.inputDelay.checkFinished() && !this.levelFinished) {
                
                int lastPressed = inputManager.getLastPressedKey();
                
                // Per-player input/move/shoot
                for (int p = 0; p < GameState.NUM_PLAYERS; p++) {
                    Ship ship = this.ships[p];
                    
                    if (ship == null || ship.isDestroyed()) {
                        continue;
                    }
                    
                    // === Active item use (one-press) ===
                    if (p == 0 && lastPressed == KeyEvent.VK_Q) {
                        state.useFirstActiveItem(0);   // P1 uses Q
                    }
                    if (p == 1 && lastPressed == KeyEvent.VK_SLASH) {
                        state.useFirstActiveItem(1);   // P2 uses '/'
                    }
                    
                    boolean moveRight, moveLeft, fire;
                    // Get player key input status
                    if (p == 0) {
                        moveRight = inputManager.isP1RightPressed();
                        moveLeft = inputManager.isP1LeftPressed();
                        fire = inputManager.isP1ShootPressed();
                    } else {
                        moveRight = inputManager.isP2RightPressed();
                        moveLeft = inputManager.isP2LeftPressed();
                        fire = inputManager.isP2ShootPressed();
                    }
                    
                    boolean isRightBorder =
                        ship.getPositionX() + ship.getWidth() + ship.getSpeed() > this.width - 1;
                    
                    boolean isLeftBorder = ship.getPositionX() - ship.getSpeed() < 1;
                    
                    if (moveRight && !isRightBorder) {
                        ship.moveRight();
                    }
                    if (moveLeft && !isLeftBorder) {
                        ship.moveLeft();
                    }
                    
                    fire = (p == 0)
                        ? inputManager.isKeyDown(KeyEvent.VK_SPACE)
                        : inputManager.isKeyDown(KeyEvent.VK_ENTER);
                    
                    if (fire && ship.shoot(this.bullets)) {
                        SoundManager.playOnce("shoot");
                        
                        state.incBulletsShot(p); // 2P mode: increments per-player bullet shots
                        
                    }
                }
                
                // despawn based on active effect
                updatePetsFromEffects();
                
                // Special ship lifecycle
                if (!this.state.areEnemiesFrozen()) {
                    if (this.enemyShipSpecial != null) {
                        if (!this.enemyShipSpecial.isDestroyed()) {
                            this.enemyShipSpecial.move(2, 0);
                        } else if (this.enemyShipSpecialExplosionCooldown.checkFinished()) {
                            this.enemyShipSpecial = null;
                        }
                    }
                    
                    if (this.enemyShipSpecial == null
                        && this.enemyShipSpecialCooldown.checkFinished()) {
                        this.enemyShipSpecial = new EnemyShip();
                        this.enemyShipSpecialCooldown.reset();
                        SoundManager.playLoop("special_ship_sound");
                        this.LOGGER.info("A special ship appears");
                    }
                    
                    if (this.enemyShipSpecial != null
                        && this.enemyShipSpecial.getPositionX() > this.width) {
                        this.enemyShipSpecial = null;
                        SoundManager.loopStop();
                        this.LOGGER.info("The special ship has escaped");
                    }
                }
                
                // Update ships & enemies
                for (Ship s : this.ships) {
                    if (s != null) {
                        s.update();
                    }
                }
                
                // Update bossShip
                if (this.bossShip != null) {
                    if (!this.state.areEnemiesFrozen()) {
                        this.bossShip.update();
                    }
                }
                
                this.enemyShipFormation.update(this.state);
                // Block enemy shooting while global freeze is active.
                if (this.state == null || !this.state.areEnemiesFrozen()) {
                    int bulletsBefore = this.bullets.size();
                    this.enemyShipFormation.shoot(this.bullets);
                    if (this.bullets.size() > bulletsBefore) {
                        // At least one enemy bullet added
                        SoundManager.playOnce("shoot_enemies");
                    }
                }
            }
            
            updatePetsLogic();
            manageCollisions();
            cleanBullets();
            
            // Item Entity Code
            cleanItems();
            manageItemPickups();
            
            // check active item affects
            state.updateEffects();
            this.basicGameSpace.setLastLife(state.getLivesRemaining() == 1);
            
            if (!sessionHighScoreNotified && this.state.getScore() > this.topScore) {
                sessionHighScoreNotified = true;
                this.highScoreNotified = true;
                this.highScoreNoticeStartTime = System.currentTimeMillis();
            }
            
            // Check if the boss is present and destroyed.
            boolean bossDestroyed = (this.bossShip != null && this.bossShip.isDestroyed());
            
            // End condition: formation cleared or TEAM lives exhausted.
            if ((this.enemyShipFormation.isEmpty() || !state.teamAlive()) && !this.levelFinished) {
                // The object managed by the object pool pattern must be recycled at the end of the level.
                BulletPool.recycle(this.bullets);
                this.bullets.removeAll(this.bullets);
                ItemPool.recycle(items);
                this.items.removeAll(this.items);
                
                this.levelFinished = true;
                this.screenFinishedCooldown.reset();
                
                if (enemyShipFormation.getShipCount() == 0 && state.getBulletsShot() > 0
                    && state.getBulletsShot() == state.getShipsDestroyed()) {
                    achievementManager.unlock("Perfect Shooter");
                }
                if (enemyShipFormation.getShipCount() == 0 && !this.tookDamageThisLevel) {
                    achievementManager.unlock("Survivor");
                }
                if (enemyShipFormation.getShipCount() == 0 & state.getLevel() == 5) {
                    achievementManager.unlock("Clear");
                }
                checkAchievement();
            }
            
            if (this.levelFinished && this.screenFinishedCooldown.checkFinished()) {
                if (!achievementManager.hasPendingToasts()) {
                    this.isRunning = false;
                }
            }
            
            if (this.achievementManager != null) {
                this.achievementManager.update();
            }
        }
        
        draw();
    }
    
    /**
     * Draws the elements associated with the screen.
     */
    private void draw() {
        drawManager.initDrawing(this);
        
        drawManager.getGameScreenRenderer()
            .drawExplosions(drawManager.getBackBufferGraphics(), this);
        updateGameSpace(drawManager.getBackBufferGraphics());
        
        for (Ship s : this.ships) {
            if (s != null) {
                drawManager.getEntityRenderer()
                    .drawEntity(drawManager.getBackBufferGraphics(), s, s.getPositionX(),
                        s.getPositionY());
            }
        }
        
        for (Pet pet : this.pets) {
            if (pet.isDead() || pet.isExpired()) {
                continue;
            }
            
            drawManager.getEntityRenderer()
                .drawEntity(drawManager.getBackBufferGraphics(), pet,
                    pet.getPositionX(), pet.getPositionY());
        }
        
        if (this.enemyShipSpecial != null) {
            drawManager.getEntityRenderer()
                .drawEntity(drawManager.getBackBufferGraphics(), this.enemyShipSpecial,
                    this.enemyShipSpecial.getPositionX(), this.enemyShipSpecial.getPositionY());
        }
        
        if (this.bossShip != null) {
            drawManager.getEntityRenderer()
                .drawEntity(drawManager.getBackBufferGraphics(), this.bossShip,
                    this.bossShip.getPositionX(), this.bossShip.getPositionY());
        }
        
        enemyShipFormation.draw();
        
        for (Bullet bullet : this.bullets) {
            drawManager.getEntityRenderer()
                .drawEntity(drawManager.getBackBufferGraphics(), bullet, bullet.getPositionX(),
                    bullet.getPositionY());
        }
        
        // draw items
        for (Item item : this.items) {
            drawManager.getEntityRenderer()
                .drawEntity(drawManager.getBackBufferGraphics(), item, item.getPositionX(),
                    item.getPositionY());
        }
        
        // Aggregate UI (team score & team lives)
        drawManager.getGameScreenRenderer()
            .drawScore(drawManager.getBackBufferGraphics(), this, state.getScore());
        drawManager.getGameScreenRenderer()
            .drawLives(drawManager.getBackBufferGraphics(), this, state.getLivesRemaining(),
                state.isCoop());
        drawManager.getGameScreenRenderer()
            .drawCoins(drawManager.getBackBufferGraphics(), this, state.getCoins());
        drawManager.getGameScreenRenderer()
            .drawLevel(drawManager.getBackBufferGraphics(), this, this.state.getLevel());
        drawManager.getCommonRenderer()
            .drawHorizontalLine(drawManager.getBackBufferGraphics(), this,
                SEPARATION_LINE_HEIGHT - 1);
        drawManager.getGameScreenRenderer().drawShipCount(drawManager.getBackBufferGraphics(), this,
            enemyShipFormation.getShipCount());
        drawManager.getGameScreenRenderer()
            .drawItemToast(drawManager.getBackBufferGraphics(), this);
        drawManager.getGameScreenRenderer()
            .drawActiveItemSlots(drawManager.getBackBufferGraphics(), this, state);
        
        if (!this.inputDelay.checkFinished()) {
            int countdown = (int) ((INPUT_DELAY - (System.currentTimeMillis() - this.gameStartTime))
                / 1000);
            drawManager.getGameScreenRenderer()
                .drawCountDown(drawManager.getBackBufferGraphics(), this, this.state.getLevel(),
                    countdown, this.bonusLife);
            drawManager.getCommonRenderer()
                .drawHorizontalLine(drawManager.getBackBufferGraphics(), this,
                    this.height / 2 - this.height / 12);
            drawManager.getCommonRenderer()
                .drawHorizontalLine(drawManager.getBackBufferGraphics(), this,
                    this.height / 2 + this.height / 12);
        }
        if (this.highScoreNotified &&
            System.currentTimeMillis() - this.highScoreNoticeStartTime
                < HIGH_SCORE_NOTICE_DURATION) {
            drawManager.getHighScoreScreenRenderer().drawNewHighScoreNotice(this);
        }
        
        // [ADD] draw achievement popups right before completing the frame
        drawManager.getGameScreenRenderer()
            .drawAchievementToasts(drawManager.getBackBufferGraphics(), this,
                (this.achievementManager != null) ? this.achievementManager.getActiveToasts()
                    : Collections.emptyList());
        
        // === TIME FREEZE overlay ===
        if (this.state.areEnemiesFrozen()) {
            Graphics2D g2d = (Graphics2D) drawManager.getBackBufferGraphics().create();
            try {
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON);
                
                String text = "TIME FREEZE";
                
                // Use CommonRenderer's big font
                g2d.setFont(drawManager.getCommonRenderer().getFontBig());
                FontMetrics fm = g2d.getFontMetrics();
                
                int textWidth = fm.stringWidth(text);
                int textHeight = fm.getHeight();
                
                int boxWidth = textWidth + 40;
                int boxHeight = textHeight + 20;
                
                int x = (this.getWidth() - boxWidth) / 2;
                int y = (this.getHeight() - boxHeight) / 2;
                
                // Translucent black background
                g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.20f));
                g2d.setColor(Color.BLACK);
                g2d.fillRoundRect(x, y, boxWidth, boxHeight, 16, 16);
                
                // Border
                g2d.setComposite(AlphaComposite.SrcOver);
                g2d.setColor(new Color(0, 255, 255, 140));
                g2d.setStroke(new BasicStroke(2f));
                g2d.drawRoundRect(x, y, boxWidth, boxHeight, 16, 16);
                
                // Text with slight shadow
                int textX = x + (boxWidth - textWidth) / 2;
                int textY = y + (boxHeight + fm.getAscent()) / 2 - 4;
                
                g2d.setColor(new Color(0, 0, 0, 40));
                g2d.drawString(text, textX + 2, textY + 2);
                
                g2d.setColor(new Color(200, 255, 255, 70));
                g2d.drawString(text, textX, textY);
                
            } finally {
                g2d.dispose();
            }
        }
        
        if (this.isPaused) {
            drawManager.getCommonRenderer()
                .drawPauseOverlay(drawManager.getBackBufferGraphics(), this);
        }
        
        drawManager.completeDrawing(this);
    }
    
    /**
     * Cleans bullets that go off-screen.
     */
    private void cleanBullets() {
        Set<Bullet> recyclable = new HashSet<Bullet>();
        for (Bullet bullet : this.bullets) {
            bullet.update();
            if (bullet.getPositionY() < SEPARATION_LINE_HEIGHT
                || bullet.getPositionY() > this.height) {
                recyclable.add(bullet);
            }
        }
        this.bullets.removeAll(recyclable);
        BulletPool.recycle(recyclable);
    }
    
    /**
     * Cleans items that go off-screen.
     */
    private void cleanItems() {
        Set<Item> recyclableItems = new HashSet<Item>();
        for (Item item : this.items) {
            item.update();
            if (item.getPositionY() > this.height) {
                recyclableItems.add(item);
            }
        }
        this.items.removeAll(recyclableItems);
        ItemPool.recycle(recyclableItems);
    }
    
    /**
     * Manages pickups between player and items.
     */
    private void manageItemPickups() {
        Set<Item> collected = new HashSet<Item>();
        for (Item item : this.items) {
            
            for (Ship ship : this.ships) {
                if (ship == null) {
                    continue;
                }
                
                if (checkCollision(item, ship) && !collected.contains(item)) {
                    collected.add(item);
                    LOGGER.info(
                        "Player " + ship.getPlayerId() + " picked up item: " + item.getType());
                    SoundManager.playOnce("hover");
                    
                    ItemManager.getInstance().onPickup(item);
                    
                    // Convert 1-based playerId (Ship) → 0-based index (GameState).
                    int playerIndex = ship.getPlayerId() - 1;
                    if (playerIndex < 0 || playerIndex >= GameState.NUM_PLAYERS) {
                        playerIndex = 0; // fallback
                    }
                    
                    ActivationType activationType = item.getActivationType();
                    boolean autoUseOnPickup = item.isAutoUseOnPickup();
                    
                    switch (activationType) {
                        case INSTANT_ON_PICKUP:
                        case TEMPORARY_BUFF:
                            // Legacy behavior: apply the effect immediately on pickup.
                            if (autoUseOnPickup) {
                                boolean applied =
                                    item.applyEffect(getGameState(), ship.getPlayerId());
                                
                                // If applied is false (e.g. not enough coins),
                                // the item is still considered collected and removed.
                            } else {
                                // Store the item for a later instant use.
                                getGameState().addActiveItem(playerIndex, item.getData());
                            }
                            break;
                        
                        case ACTIVE_ON_KEY:
                            // Store as an active (key-activated) item.
                            getGameState().addActiveItem(playerIndex, item.getData());
                            break;
                        
                        case PASSIVE:
                            // Register as a passive item on the player.
                            getGameState().addPassiveItem(playerIndex, item.getData());
                            break;
                        
                        default:
                            // Fallback to legacy behavior if activation type is unknown.
                            item.applyEffect(getGameState(), ship.getPlayerId());
                            break;
                    }
                }
            }
        }
        this.items.removeAll(collected);
        ItemPool.recycle(collected);
    }
    
    /**
     * Enemy bullets hit players → decrement TEAM lives; player bullets hit enemies → add score.
     */
    private void manageCollisions() {
        Set<Bullet> recyclable = new HashSet<Bullet>();
        for (Bullet bullet : this.bullets) {
            if (bullet.getSpeed() > 0) {
                // Enemy bullet vs both players
                
                boolean hitSomething = false;
                for (int p = 0; p < GameState.NUM_PLAYERS; p++) {
                    Ship ship = this.ships[p];
                    if (ship != null && !ship.isDestroyed()
                        && checkCollision(bullet, ship) && !this.levelFinished) {
                        recyclable.add(bullet);
                        hitSomething = true;
                        
                        this.drawManager.getGameScreenRenderer()
                            .triggerExplosion(ship.getPositionX(), ship.getPositionY(), false,
                                state.getLivesRemaining() == 1);
                        ship.addHit();
                        
                        ship.destroy(); // explosion/respawn handled by Ship.update()
                        SoundManager.playOnce("explosion");
                        this.state.decLife(p); // decrement shared/team lives by 1
                        
                        // Record damage for Survivor achievement check
                        this.tookDamageThisLevel = true;
                        
                        this.drawManager.getGameScreenRenderer();
                        this.basicGameSpace.setLastLife(state.getLivesRemaining() == 1);
                        
                        this.LOGGER.info("Hit on player " + (p + 1) + ", team lives now: "
                            + state.getLivesRemaining());
                        break;
                    }
                }
                
                if (hitSomething) {
                    continue;
                }
                
                for (Pet pet : pets) {
                    if (pet.isDead() || pet.isExpired()) {
                        continue;
                    }
                    
                    if (checkCollision(bullet, pet) && !this.levelFinished) {
                        recyclable.add(bullet);
                        
                        pet.takeDamage(1);
                        
                        this.LOGGER.info("[GameScreen] Pet hit by enemy bullet. owner="
                            + pet.getOwnerPlayerId());
                        
                        break;
                    }
                }
                
            } else {
                // Player bullet vs enemies
                // map Bullet owner id (1 or 2) to per-player index (0 or 1)
                final int ownerId = bullet.getOwnerPlayerId(); // 1 or 2 (0 if unset)
                final int pIdx = (ownerId == 2) ? 1 : 0; // default to P1 when unset
                
                boolean finalShip = this.enemyShipFormation.lastShip();
                
                // Check collision with formation enemies
                for (EnemyShip enemyShip : this.enemyShipFormation) {
                    if (!enemyShip.isDestroyed() && checkCollision(bullet, enemyShip)) {
                        recyclable.add(bullet);
                        enemyShip.hit();
                        
                        if (enemyShip.isDestroyed()) {
                            int points = enemyShip.getPointValue();
                            state.addCoins(pIdx,
                                enemyShip.getCoinValue()); // 2P mode: modified to per-player coins
                            
                            drawManager.getGameScreenRenderer()
                                .triggerExplosion(enemyShip.getPositionX(),
                                    enemyShip.getPositionY(), true, finalShip);
                            state.addScore(pIdx,
                                points); // 2P mode: modified to add to P1 score for now
                            state.incShipsDestroyed(pIdx);
                            
                            // obtain drop from ItemManager (may return null)
                            Item drop = ItemManager.getInstance().obtainDrop(enemyShip);
                            if (drop != null) {
                                this.items.add(drop);
                                this.LOGGER.info(
                                    "Spawned " + drop.getType() + " at " + drop.getPositionX() + ","
                                        + drop.getPositionY());
                            }
                            
                            this.enemyShipFormation.destroy(enemyShip);
                            SoundManager.playOnce("invader_killed");
                            this.LOGGER.info("Hit on enemy ship.");
                            
                            checkAchievement();
                        }
                        break;
                    }
                }
                
                if (this.enemyShipSpecial != null
                    && !this.enemyShipSpecial.isDestroyed()
                    && checkCollision(bullet, this.enemyShipSpecial)) {
                    int points = this.enemyShipSpecial.getPointValue();
                    
                    state.addCoins(pIdx,
                        this.enemyShipSpecial.getCoinValue()); // 2P mode: modified to per-player coins
                    
                    state.addScore(pIdx, points);
                    state.incShipsDestroyed(pIdx); // 2P mode: modified incrementing ships destroyed
                    
                    this.enemyShipSpecial.destroy();
                    SoundManager.loopStop();
                    SoundManager.playOnce("explosion");
                    drawManager.getGameScreenRenderer()
                        .triggerExplosion(this.enemyShipSpecial.getPositionX(),
                            this.enemyShipSpecial.getPositionY(), true, true);
                    this.enemyShipSpecialExplosionCooldown.reset();
                    recyclable.add(bullet);
                }
                
                if (this.bossShip != null
                    && !this.bossShip.isDestroyed()
                    && checkCollision(bullet, this.bossShip)) {
                    
                    this.bossShip.hit(); // Apply damage to the boss (decrement health by 1)
                    recyclable.add(bullet); // Recycle the bullet
                    
                    if (this.bossShip.isDestroyed()) {
                        int points = this.bossShip.getPointValue();
                        state.addCoins(pIdx, this.bossShip.getCoinValue());
                        state.addScore(pIdx, points);
                        state.incShipsDestroyed(pIdx);
                        
                        SoundManager.loopStop(); // Stop boss BGM
                        SoundManager.playOnce("explosion");
                        // Boss explosion is always large and final (true)
                        drawManager.getGameScreenRenderer()
                            .triggerExplosion(this.bossShip.getPositionX(),
                                this.bossShip.getPositionY(), true, true);
                    }
                    // Since the Boss is a single target, break is omitted to continue with the next bullet/enemy check.
                }
            }
        }
        this.bullets.removeAll(recyclable);
        BulletPool.recycle(recyclable);
    }
    
    /**
     * Checks if two entities are colliding.
     *
     * @param a First entity, the bullet.
     * @param b Second entity, the ship.
     * @return Result of the collision test.
     */
    private boolean checkCollision(final Entity a, final Entity b) {
        int centerAX = a.getPositionX() + a.getWidth() / 2;
        int centerAY = a.getPositionY() + a.getHeight() / 2;
        int centerBX = b.getPositionX() + b.getWidth() / 2;
        int centerBY = b.getPositionY() + b.getHeight() / 2;
        int maxDistanceX = a.getWidth() / 2 + b.getWidth() / 2;
        int maxDistanceY = a.getHeight() / 2 + b.getHeight() / 2;
        int distanceX = Math.abs(centerAX - centerBX);
        int distanceY = Math.abs(centerAY - centerBY);
        return distanceX < maxDistanceX && distanceY < maxDistanceY;
    }
    
    /**
     * Returns a GameState object representing the status of the game.
     *
     * @return Current game state.
     */
    public final GameState getGameState() {
        return this.state;
    }
    
    /**
     * check Achievement released;
     */
    public void checkAchievement() {
        // First Blood
        if (state.getShipsDestroyed() == 1) {
            achievementManager.unlock("First Blood");
        }
        // Clear
        if (levelFinished && this.enemyShipFormation.isEmpty() && state.getLevel() == 5) {
            achievementManager.unlock("Clear");
            float p1Acc = state.getBulletsShot(0) > 0 ?
                (float) state.getShipsDestroyed(0) / state.getBulletsShot(0) * 100 : 0f;
            float p2Acc = state.getBulletsShot(1) > 0 ?
                (float) state.getShipsDestroyed(1) / state.getBulletsShot(1) * 100 : 0f;
            // Survivor
            if (!this.tookDamageThisLevel) {
                achievementManager.unlock("Survivor");
            }
            //Sharpshooter
            if (p1Acc >= 80) {
                //1p
                achievementManager.unlock("Sharpshooter");
                //coop
                if (p2Acc >= 80) {
                    achievementManager.unlock("Sharpshooter");
                }
            }
        }
        
        //50 Bullets
        if (state.getBulletsShot() >= 50) {
            achievementManager.unlock("50 Bullets");
        }
        //Get 3000 Score
        if (state.getScore() >= 3000) {
            achievementManager.unlock("Get 3000 Score");
        }
    }
    
    /**
     * Draws the stars background animation during the game
     */
    public void updateGameSpace(Graphics g) {
        basicGameSpace.update();
        
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        g.setColor(Color.WHITE);
        int[][] positions = basicGameSpace.getStarLocations();
        for (int i = 0; i < basicGameSpace.getNumStars(); i++) {
            
            int size = (positions[i][2] < 2) ? 2 : 1;
            int radius = size * 2;
            
            float[] dist = {0.0f, 1.0f};
            Color[] colors = new Color[2];
            if (basicGameSpace.isLastLife()) {
                colors[0] = new Color(255, 0, 0, 100);
                colors[1] = new Color(255, 0, 0, 50);
            } else {
                colors[0] = new Color(255, 255, 200, 50);
                colors[1] = new Color(255, 255, 200, 50);
            }
            
            RadialGradientPaint paint = new RadialGradientPaint(
                new Point(positions[i][0] + size / 2, positions[i][1] + size / 2),
                radius, dist, colors);
            g2d.setPaint(paint);
            g2d.fillOval(positions[i][0] - radius / 2, positions[i][1] - radius / 2, radius,
                radius);
            g.fillOval(positions[i][0], positions[i][1], size, size);
        }
    }
    
    /**
     * Spawns or removes pets based on the PET_SUPPORT effect.
     */
    private void updatePetsFromEffects() {
        // For now, assume at most one pet per player.
        for (int p = 0; p < GameState.NUM_PLAYERS; p++) {
            Ship owner = this.ships[p];
            if (owner == null) {
                continue;
            }
            
            boolean hasPetEffect =
                state != null && state.hasEffect(p,
                    engine.gameplay.item.ItemEffect.ItemEffectType.PET_SUPPORT);
            
            boolean hasPetEntity = hasPetForPlayer(p + 1);
            
            if (hasPetEffect && !hasPetEntity) {
                spawnPetForPlayer(p + 1, owner);
            } else if (!hasPetEffect && hasPetEntity) {
                removePetForPlayer(p + 1);
            }
        }
    }
    
    /**
     * Returns true if there is a pet currently bound to the given playerId (1-based).
     */
    private boolean hasPetForPlayer(int playerId) {
        for (Pet pet : pets) {
            if (pet.getOwnerPlayerId() == playerId) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * Removes all pets belonging to the given playerId (1-based).
     */
    private void removePetForPlayer(int playerId) {
        java.util.Set<Pet> toRemove = new java.util.HashSet<>();
        for (Pet pet : pets) {
            if (pet.getOwnerPlayerId() == playerId) {
                toRemove.add(pet);
            }
        }
        pets.removeAll(toRemove);
    }
    
    /**
     * Spawns a new pet bound to the given player.
     */
    private void spawnPetForPlayer(int playerId, Ship owner) {
        // Initial pet position: near the owner (slightly to the right and above)
        int startX = owner.getPositionX() + owner.getWidth() + 10;
        int startY = owner.getPositionY() - 10;
        
        int petWidth = 16;
        int petHeight = 16;
        Color petColor = Color.CYAN;
        
        long lifetimeMs = 6000L;
        long shotIntervalMs = 1000L;
        
        Pet pet = new Pet(
            startX,
            startY,
            petWidth,
            petHeight,
            petColor,
            playerId,
            Pet.PetKind.GUN,
            this.state,
            lifetimeMs,
            shotIntervalMs
        );
        
        pets.add(pet);
        
        Core.getLogger().info("[GameScreen] Spawned PET-GUN for player " + playerId
            + " at (" + startX + "," + startY + ")");
    }
    
    /**
     * Updates all active pets: follow their owner and fire bullets if needed.
     */
    private void updatePetsLogic() {
        for (Pet pet : pets) {
            
            if (pet.isDead() || pet.isExpired()) {
                continue;
            }
            
            int ownerId = pet.getOwnerPlayerId();
            int idx = ownerId - 1;
            Ship owner = (idx >= 0 && idx < GameState.NUM_PLAYERS) ? ships[idx] : null;
            
            if (owner == null || owner.isDestroyed()) {
                continue;
            }
            
            pet.update(this.bullets, owner);
        }
    }
}
