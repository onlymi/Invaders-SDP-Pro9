package screen;

import animations.BasicGameSpace;
import engine.AssetManager.SpriteType;
import engine.Core;
import engine.EnemyManager;
import engine.GameSettings;
import engine.GameState;
import engine.SoundManager;
import engine.gameplay.achievement.AchievementManager;
import engine.gameplay.item.ActivationType;
import engine.gameplay.item.ItemManager;
import engine.utils.Cooldown;
import entity.BossShip;
import entity.EnemyShip;
import entity.Entity;
import entity.Item;
import entity.ItemPool;
import entity.Weapon;
import entity.WeaponPool;
import entity.character.CharacterSpawner;
import entity.character.CharacterType;
import entity.character.GameCharacter;
import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.RadialGradientPaint;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.event.KeyEvent;
import java.awt.geom.AffineTransform;
import java.awt.geom.Area;
import java.util.Collections;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;

/**
 * Implements the game screen, where the action happens (supports co-op with shared team lives).
 *
 * @author <a href="mailto:RobertoIA1987@gmail.com">Roberto Izquierdo Amo</a>
 */
public class GameScreen extends Screen {
    
    private static final int INPUT_DELAY = 6000;
    private static final int LIFE_SCORE = 100;
    /**
     * Time from finishing the level to screen change.
     */
    private static final int SCREEN_CHANGE_INTERVAL = 1500;
    /**
     * Height of the interface separation line.
     */
    public static final int SEPARATION_LINE_HEIGHT = 68;
    private static final int HIGH_SCORE_NOTICE_DURATION = 2000;
    private static boolean sessionHighScoreNotified = false;
    
    private AchievementManager achievementManager;
    private GameSettings gameSettings;
    private BasicGameSpace basicGameSpace;
    /**
     * EnemyShip for Multi-hit.
     */
    private BossShip bossShip;
    /**
     * Current difficulty level number.
     */
    private final int level;
    /**
     * Formation of enemy.
     */
    private EnemyManager enemyManager;
    /**
     * Characters (Players) in the game. Replaces the old 'ships' array.
     */
    private GameCharacter[] characters;
    /**
     * Time from finishing the level to screen change.
     */
    private Cooldown screenFinishedCooldown;
    private Set<Weapon> weapons;
    private Set<Item> items;
    private long gameStartTime;
    /**
     * Checks if the level is finished.
     */
    private boolean levelFinished;
    /**
     * [New] Checks if the level is cleared (win condition met).
     */
    private boolean levelCleared;
    
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
    
    private boolean tookDamageThisLevel;
    private boolean countdownSoundPlayed = false;
    
    private final GameState state;
    
    private CharacterType characterTypeP1;
    private CharacterType characterTypeP2;
    
    private int killsToWin;
    private int enemyKillCount;
    
    /**
     * Constructor, establishes the properties of the screen.
     *
     * @param gameState          Current game state.
     * @param gameSettings       Current game settings.
     * @param bonusLife          Checks if a bonus life is awarded this level.
     * @param width              Screen width.
     * @param height             Screen height.
     * @param fps                Frames per second, frame rate at which the game is run.
     * @param characterTypeP1    Player 1's character type.
     * @param characterTypeP2    Player 2's character type.
     * @param achievementManager Achievement manager instance.
     */
    public GameScreen(final GameState gameState, final GameSettings gameSettings,
        final boolean bonusLife, final int width, final int height, final int fps,
        final CharacterType characterTypeP1, final CharacterType characterTypeP2,
        final AchievementManager achievementManager) {
        super(width, height, fps);
        
        this.characters = new GameCharacter[GameState.NUM_PLAYERS];
        
        this.state = gameState;
        this.gameSettings = gameSettings;
        this.bonusLife = bonusLife;
        this.characterTypeP1 = characterTypeP1;
        this.characterTypeP2 = characterTypeP2;
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
                state.addLife(0, 1); // single player
            }
        }
        if (this.achievementManager == null) {
            this.achievementManager = new AchievementManager();
        }
    }
    
    public static void resetSessionHighScoreNotified() {
        sessionHighScoreNotified = false;
    }
    
    /**
     * Initializes basic screen properties, and adds necessary elements.
     */
    public final void initialize() {
        super.initialize();
        
        state.clearAllEffects();
        soundManager.playLoop("game_theme");
        
        // New BasicGameSpace Code
        this.basicGameSpace = new BasicGameSpace(100, this.width, this.height);
        
        this.enemyManager = new EnemyManager(this);
        this.enemyKillCount = 0;
        // 각 스테이지 별 필요 kill 수
        this.killsToWin = 10 + (this.level * 5);
        this.levelCleared = false; // Initialize to false
        
        // --- Character Initialization & Control Setup ---
        this.enemyManager = new EnemyManager(this);
        if (this.level == 6) {
            int bossWidth = 240;
            this.bossShip = new BossShip(this.width / 2 - bossWidth / 2, 120);
            this.LOGGER.info("Boss Stage Initialized!");
            this.basicGameSpace.setBossStage(true);
        } else {
            this.bossShip = null;
        }
        
        // Player 1
        int startX = this.width / 2 - Core.getAssetManager().getCharacterWidth() / 2;
        int startY = this.height - Core.getAssetManager().getCharacterHeight() - 10;
        int gapBetweenCharacters = 64;
        
        // Player 2
        if (state.isCoop()) {
            this.characters[0] = CharacterSpawner.createCharacter(this.characterTypeP1,
                startX - gapBetweenCharacters, startY, Entity.Team.PLAYER1, 1);
            this.characters[1] = CharacterSpawner.createCharacter(this.characterTypeP2,
                startX + gapBetweenCharacters, startY, Entity.Team.PLAYER2, 2);
            // P2 Controls
            this.characters[1].setControlKeys(Core.getInputManager().getPlayer2Keys());
        } else {
            this.characters[0] = CharacterSpawner.createCharacter(this.characterTypeP1,
                startX, startY, Entity.Team.PLAYER1, 1);
            this.characters[1] = null;
        }
        // P1 Controls
        this.characters[0].setControlKeys(Core.getInputManager().getPlayer1Keys());
        
        this.screenFinishedCooldown = Core.getCooldown(SCREEN_CHANGE_INTERVAL);
        this.weapons = new HashSet<Weapon>();
        this.items = new HashSet<Item>();
        this.basicGameSpace = new BasicGameSpace(100, this.width, this.height);
        
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
        
        state.addScore(0, LIFE_SCORE * state.getLivesRemaining());
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
        
        if (this.getGameState().areEnemiesFrozen()) {
            return;
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
                
                // Calculate deltaTime (seconds per frame)
                // Assuming fixed FPS from Screen.fps
                float deltaTime = 1.0f / this.fps;
                
                int lastPressed = inputManager.getLastPressedKey();
                
                // --- Character Update Loop ---
                for (int p = 0; p < GameState.NUM_PLAYERS; p++) {
                    GameCharacter character = this.characters[p];
                    
                    if (character == null) {
                        continue;
                    }
                    
                    // Update Character State
                    character.update(deltaTime);
                    
                    if (character.getCurrentHealthPoints() <= 0) {
                        continue;
                    }
                    
                    // Active Item Input
                    if (p == 0 && lastPressed == KeyEvent.VK_Q) {
                        state.useFirstActiveItem(0);
                    }
                    if (p == 1 && lastPressed == KeyEvent.VK_SLASH) {
                        state.useFirstActiveItem(1);
                    }
                    
                    // Handle Input (Movement & Shooting)
                    boolean shotFired = character.handleKeyboard(inputManager, this, this.weapons,
                        deltaTime);
                    
                    if (shotFired) {
                        SoundManager.playOnce("shoot");
                        state.incBulletsShot(p);
                    }
                }
                // -----------------------------
                
                // Update bossShip
                if (this.bossShip != null) {
                    this.bossShip.update();
                    if (!this.bossShip.isDestroyed()) {
                        this.bossShip.shoot(this.weapons, this.characters);
                    }
                    if (!this.state.areEnemiesFrozen()) {
                        this.bossShip.update();
                    }
                } else {
                    this.enemyManager.update();
                }
                // Block enemy shooting while global freeze is active.
                if (this.state == null || !this.state.areEnemiesFrozen()) {
                    int bulletsBefore = this.weapons.size();
                    // this.enemyManager.shoot(this.weapons); // Assuming handled inside manager or uncomment if needed
                    if (this.weapons.size() > bulletsBefore) {
                        SoundManager.playOnce("shoot_enemies");
                    }
                }
            }
            
            manageCollisions();
            cleanBullets();
            
            // Item Entity Code
            cleanItems();
            manageItemPickups();
            
            // check active item affects
            state.updateEffects();
            boolean lowHealth = false;
            for (GameCharacter c : characters) {
                if (c != null
                    && c.getCurrentHealthPoints() <= c.getCurrentStats().maxHealthPoints * 0.2) {
                    lowHealth = true;
                    break;
                }
            }
            this.basicGameSpace.setLastLife(lowHealth);
            draw();
            
            if (!sessionHighScoreNotified && this.state.getScore() > this.topScore) {
                sessionHighScoreNotified = true;
                this.highScoreNotified = true;
                this.highScoreNoticeStartTime = System.currentTimeMillis();
            }
            
            boolean bossDestroyed = (this.bossShip != null && this.bossShip.isDestroyed());
            
            boolean teamAlive = false;
            for (GameCharacter c : characters) {
                if (c != null && c.getCurrentHealthPoints() > 0) {
                    teamAlive = true;
                    break;
                }
            }
            
            // End condition: achieved kill count or TEAM lives exhausted.
            if ((this.enemyKillCount >= this.killsToWin || !state.teamAlive())
                && !this.levelFinished) {
                // The object managed by the object pool pattern must be recycled at the end of the level.
                WeaponPool.recycle(this.weapons);
                this.weapons.removeAll(this.weapons);
                ItemPool.recycle(items);
                this.items.removeAll(this.items);
                
                this.levelFinished = true;
                this.screenFinishedCooldown.reset();
                
                // Set levelCleared only if objective met
                if (this.enemyKillCount >= this.killsToWin) {
                    this.levelCleared = true;
                    
                    if (!this.tookDamageThisLevel) {
                        achievementManager.unlock("Survivor");
                    }
                    if (state.getLevel() == Core.NUM_LEVELS) {
                        achievementManager.unlock("Clear");
                    }
                    checkAchievement();
                } else {
                    this.levelCleared = false; // Game Over or Level Failed
                }
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
        
        // Draw Characters
        for (GameCharacter character : this.characters) {
            if (character != null) {
                if (character.getCurrentHealthPoints() <= 0 || !character.isInvincible()
                    || (System.currentTimeMillis() % 200 > 100)) {
                    drawManager.getEntityRenderer()
                        .drawEntity(drawManager.getBackBufferGraphics(), character,
                            character.getPositionX(), character.getPositionY());
                }
            }
        }
        
        if (this.bossShip != null) {
            drawManager.getEntityRenderer()
                .drawEntity(drawManager.getBackBufferGraphics(), this.bossShip,
                    this.bossShip.getPositionX(), this.bossShip.getPositionY());
        }
        
        this.enemyManager.draw();
        
        for (Weapon weapon : this.weapons) {
            drawManager.getEntityRenderer()
                .drawEntity(drawManager.getBackBufferGraphics(), weapon, weapon.getPositionX(),
                    weapon.getPositionY());
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
        // drawManager.getGameScreenRenderer()
        //     .drawLives(drawManager.getBackBufferGraphics(), this, state.getLivesRemaining(),
        //         state.isCoop());
        drawManager.getGameScreenRenderer()
            .drawCoins(drawManager.getBackBufferGraphics(), this, state.getCoins());
        drawManager.getGameScreenRenderer()
            .drawLevel(drawManager.getBackBufferGraphics(), this, this.state.getLevel());
        drawManager.getCommonRenderer()
            .drawHorizontalLine(drawManager.getBackBufferGraphics(), this,
                SEPARATION_LINE_HEIGHT - 1);
        // Remaining Kills
        int remainingKills = Math.max(0, this.killsToWin - this.enemyKillCount);
        drawManager.getGameScreenRenderer().drawShipCount(drawManager.getBackBufferGraphics(), this,
            remainingKills);
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
        if (this.highScoreNotified
            && System.currentTimeMillis() - this.highScoreNoticeStartTime
            < HIGH_SCORE_NOTICE_DURATION) {
            drawManager.getHighScoreScreenRenderer().drawNewHighScoreNotice(this);
        }
        
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
        Set<Weapon> recyclable = new HashSet<Weapon>();
        for (Weapon weapon : this.weapons) {
            weapon.update();
            boolean isOffScreenY = weapon.getPositionY() < SEPARATION_LINE_HEIGHT
                || weapon.getPositionY() > this.height;
            boolean isOffScreenX = weapon.getPositionX() < 0
                || weapon.getPositionX() > this.width;
            if (isOffScreenY || isOffScreenX) {
                recyclable.add(weapon);
            }
        }
        this.weapons.removeAll(recyclable);
        WeaponPool.recycle(recyclable);
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
            for (GameCharacter character : this.characters) {
                if (character == null) {
                    continue;
                }
                if (checkCollision(item, character) && !collected.contains(item)) {
                    collected.add(item);
                    LOGGER.info(
                        "Player " + character.getPlayerId() + " picked up item: " + item.getType());
                    SoundManager.playOnce("hover");
                    
                    ItemManager.getInstance().onPickup(item);
                    
                    int playerIndex = character.getPlayerId() - 1;
                    if (playerIndex < 0 || playerIndex >= GameState.NUM_PLAYERS) {
                        playerIndex = 0; // fallback
                    }
                    
                    ActivationType activationType = item.getActivationType();
                    boolean autoUseOnPickup = item.isAutoUseOnPickup();
                    
                    switch (activationType) {
                        case INSTANT_ON_PICKUP:
                        case TEMPORARY_BUFF:
                            if (autoUseOnPickup) {
                                boolean applied = item.applyEffect(getGameState(),
                                    character.getPlayerId());
                            } else {
                                getGameState().addActiveItem(playerIndex, item.getData());
                            }
                            break;
                        case ACTIVE_ON_KEY:
                            getGameState().addActiveItem(playerIndex, item.getData());
                            break;
                        case PASSIVE:
                            getGameState().addPassiveItem(playerIndex, item.getData());
                            break;
                        default:
                            item.applyEffect(getGameState(), character.getPlayerId());
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
        Set<Weapon> recyclable = new HashSet<Weapon>();
        for (Weapon weapon : this.weapons) {
            if (weapon.getOwnerPlayerId() == 0) {
                // Enemy bullet vs Players
                for (int p = 0; p < GameState.NUM_PLAYERS; p++) {
                    GameCharacter character = this.characters[p];
                    if (character != null && character.getCurrentHealthPoints() > 0
                        && checkCollision(weapon, character) && !this.levelFinished) {
                        if (character.isInvincible()) {
                            continue;
                        }
                        
                        recyclable.add(weapon);
                        
                        this.drawManager.getGameScreenRenderer()
                            .triggerExplosion(character.getPositionX(), character.getPositionY(),
                                false, state.getLivesRemaining() == 1);
                        
                        character.takeDamage(weapon.getDamage());
                        
                        // Decrement life if HP reaches 0
                        if (character.getCurrentHealthPoints() <= 0) {
                            // this.state.decLife(p);
                            this.LOGGER.info("Player " + (p + 1) + " died. Lives remaining: "
                                + state.getLivesRemaining());
                        }
                        
                        SoundManager.playOnce("explosion");
                        // this.state.decLife(p);
                        
                        // Record damage for Survivor achievement check
                        this.tookDamageThisLevel = true;
                        this.basicGameSpace.setLastLife(state.getLivesRemaining() == 1);
                        
                        this.LOGGER.info("Hit on player " + (p + 1) + ", team lives now: "
                            + state.getLivesRemaining());
                        break;
                    }
                }
            } else {
                // Player bullet vs enemies
                // map Bullet owner id (1 or 2) to per-player index (0 or 1)
                final int ownerId = weapon.getOwnerPlayerId(); // 1 or 2 (0 if unset)
                final int pIdx = (ownerId == 2) ? 1 : 0; // default to P1 when unset
                boolean finalShip = this.enemyManager.lastShip();
                
                // Check collision with formation enemies
                for (EnemyShip enemyShip : this.enemyManager.getEnemies()) {
                    if (!enemyShip.isDestroyed() && checkCollision(weapon, enemyShip)) {
                        recyclable.add(weapon);
                        enemyShip.hit(weapon.getDamage());
                        
                        if (enemyShip.isDestroyed()) {
                            int points = enemyShip.getPointValue();
                            state.addCoins(pIdx, enemyShip.getCoinValue());
                            drawManager.getGameScreenRenderer()
                                .triggerExplosion(enemyShip.getPositionX(),
                                    enemyShip.getPositionY(), true, finalShip);
                            state.addScore(pIdx, points);
                            state.incShipsDestroyed(pIdx);
                            this.enemyKillCount++;
                            
                            Item drop = ItemManager.getInstance().obtainDrop(enemyShip);
                            if (drop != null) {
                                this.items.add(drop);
                                this.LOGGER.info(
                                    "Spawned " + drop.getType() + " at " + drop.getPositionX() + ","
                                        + drop.getPositionY());
                            }
                            
                            this.enemyManager.destroy(enemyShip);
                            SoundManager.playOnce("invader_killed");
                            this.LOGGER.info("Hit on enemy.");
                            
                            checkAchievement();
                        }
                        break;
                    }
                }
                
                if (this.bossShip != null
                    && !this.bossShip.isDestroyed()
                    && checkCollision(weapon, this.bossShip)) {
                    this.bossShip.hit();
                    recyclable.add(weapon);
                    
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
                        Random rand = new Random();
                        for (int i = 0; i < 10; i++) {
                            int offsetX = rand.nextInt(this.bossShip.getWidth())
                                - this.bossShip.getWidth() / 2;
                            int offsetY = rand.nextInt(this.bossShip.getHeight())
                                - this.bossShip.getHeight() / 2;
                            
                            Color explosionColor = new Color(255, rand.nextInt(150), 0);
                            
                            drawManager.getGameScreenRenderer().triggerCustomExplosion(
                                this.bossShip.getPositionX() + this.bossShip.getWidth() / 2
                                    + offsetX,
                                this.bossShip.getPositionY() + this.bossShip.getHeight() / 2
                                    + offsetY,
                                explosionColor
                            );
                        }
                    } else {
                        SoundManager.playOnce("boss_hit");
                        
                        drawManager.getGameScreenRenderer().triggerCustomExplosion(
                            weapon.getPositionX() + weapon.getWidth() / 2,
                            weapon.getPositionY(),
                            new Color(255, 50, 50));
                    }
                    // Since the Boss is a single target, break is omitted to continue with the next bullet/enemy check.
                }
            }
        }
        
        // player vs enemy body collision
        for (int p = 0; p < GameState.NUM_PLAYERS; p++) {
            GameCharacter player = this.characters[p];
            if (player == null || player.getCurrentHealthPoints() <= 0 || player.isInvincible()) {
                continue;
            }
            
            for (EnemyShip enemy : this.enemyManager.getEnemies()) {
                if (!enemy.isDestroyed() && checkCollision(player, enemy)) {
                    player.takeDamage(5);
                    
                    // [FIXED] Decrement life on collision death
                    if (player.getCurrentHealthPoints() <= 0) {
                        this.state.decLife(p);
                    }
                    
                    // enemy knockback
                    double dx = enemy.getPositionX() - player.getPositionX();
                    double dy = enemy.getPositionY() - player.getPositionY();
                    double dist = Math.sqrt(dx * dx + dy * dy);
                    
                    if (dist > 0) {
                        enemy.pushBack((dx / dist) * 10.0, (dy / dist) * 10.0);
                    }
                    
                    this.LOGGER.info(
                        "Collision! Player " + (p + 1) + " hit by enemy body.");
                }
            }
        }
        
        this.weapons.removeAll(recyclable);
        WeaponPool.recycle(recyclable);
    }
    
    /**
     * Checks if two entities are colliding.
     *
     * @param a First entity, the bullet.
     * @param b Second entity, the ship.
     * @return Result of the collision test.
     */
    private boolean checkCollision(final Entity a, final Entity b) {
        // 1. Create a basic rectangle (based on current position and size)
        Rectangle r1 = new Rectangle(a.getPositionX(), a.getPositionY(),
            a.getWidth(), a.getHeight());
        Rectangle r2 = new Rectangle(b.getPositionX(), b.getPositionY(),
            b.getWidth(), b.getHeight());
        
        // 2. Without rotation (optimization): Perform fast quadrilateral collision detection as before.
        if (a.getRotation() == 0 && b.getRotation() == 0) {
            return r1.intersects(r2);
        }
        
        // 3. When there is rotation: Precise shape collision detection (using Area)
        Area areaA = new Area(r1);
        Area areaB = new Area(r2);
        
        // Apply rotation to an entity (usually a laser)
        if (a.getRotation() != 0) {
            AffineTransform atA = new AffineTransform();
            
            double anchorX = r1.getCenterX();
            double anchorY = r1.getCenterY();
            
            if (a.getSpriteType() == SpriteType.BigLaserBeam) {
                anchorY = r1.getY();
            }
            
            atA.rotate(Math.toRadians(a.getRotation()), anchorX, anchorY);
            areaA.transform(atA);
        }
        
        // Apply rotation to b entity (mainly player) (possibly for scalability)
        if (b.getRotation() != 0) {
            AffineTransform atB = new AffineTransform();
            atB.rotate(Math.toRadians(b.getRotation()), r2.getCenterX(), r2.getCenterY());
            areaB.transform(atB);
        }
        
        // Check if there is an intersection (overlapping area) between two shapes
        areaA.intersect(areaB);
        return !areaA.isEmpty();
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
     * Returns whether the level was cleared successfully (win condition met).
     *
     * @return true if cleared, false otherwise.
     */
    public boolean isLevelCleared() {
        return this.levelCleared;
    }
    
    public void checkAchievement() {
        // First Blood
        if (state.getShipsDestroyed() == 1) {
            achievementManager.unlock("First Blood");
        }
        // Clear
        if (levelFinished && this.enemyManager.isEmpty() && state.getLevel() == 5) {
            achievementManager.unlock("Clear");
            float p1Acc = state.getBulletsShot(0) > 0
                ? (float) state.getShipsDestroyed(0) / state.getBulletsShot(0) * 100 : 0f;
            float p2Acc = state.getBulletsShot(1) > 0
                ? (float) state.getShipsDestroyed(1) / state.getBulletsShot(1) * 100 : 0f;
            if (!this.tookDamageThisLevel) {
                achievementManager.unlock("Survivor");
            }
            // Sharpshooter
            if (p1Acc >= 80) {
                // 1p
                achievementManager.unlock("Sharpshooter");
                // coop
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
            if (basicGameSpace.isBossStage()) {
                colors[0] = new Color(200, 0, 255, 150);
                colors[1] = new Color(255, 50, 50, 100);
            } else if (basicGameSpace.isLastLife()) {
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
     * Returns the height of the separation line.
     *
     * @return Height of the UI separation line.
     */
    public int getSeparationLineHeight() {
        return SEPARATION_LINE_HEIGHT;
    }
    
    public GameCharacter[] getCharacters() {
        return this.characters;
    }
}