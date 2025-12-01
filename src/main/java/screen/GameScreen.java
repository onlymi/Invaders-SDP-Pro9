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
import entity.EnemyShip;
import entity.EnemyShipFormation;
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
import java.awt.RenderingHints;
import java.awt.event.KeyEvent;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * Implements the game screen, where the action happens (supports co-op with shared team lives).
 *
 * @author <a href="mailto:RobertoIA1987@gmail.com">Roberto Izquierdo Amo</a>
 */
public class GameScreen extends Screen {
    
    private static final int INPUT_DELAY = 6000;
    private static final int LIFE_SCORE = 100;
    private static final int BONUS_SHIP_INTERVAL = 20000;
    private static final int BONUS_SHIP_VARIANCE = 10000;
    private static final int BONUS_SHIP_EXPLOSION = 500;
    private static final int SCREEN_CHANGE_INTERVAL = 1500;
    private static final int SEPARATION_LINE_HEIGHT = 68;
    private static final int HIGH_SCORE_NOTICE_DURATION = 2000;
    private static boolean sessionHighScoreNotified = false;
    
    private AchievementManager achievementManager;
    private GameSettings gameSettings;
    private BasicGameSpace basicGameSpace;
    private EnemyShip bossShip;
    private int level;
    private EnemyShipFormation enemyShipFormation;
    private EnemyShip enemyShipSpecial;
    
    /**
     * Characters (Players) in the game. Replaces the old 'ships' array.
     */
    private GameCharacter[] characters;
    
    private Cooldown enemyShipSpecialCooldown;
    private Cooldown enemyShipSpecialExplosionCooldown;
    private Cooldown screenFinishedCooldown;
    private Set<Weapon> weapons;
    private Set<Item> items;
    private long gameStartTime;
    private boolean levelFinished;
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
        
        if (this.bonusLife) {
            if (state.isSharedLives()) {
                state.addTeamLife(1);
            } else {
                state.addLife(0, 1);
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
        
        enemyShipFormation = new EnemyShipFormation(this.gameSettings);
        enemyShipFormation.attach(this);
        
        // --- Character Initialization & Control Setup ---
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
            // P2 Controls: A, D, Enter (Shoot) - Adjust keys as per your KeyConfig if needed
            this.characters[1].setControlKeys(Core.getInputManager().getPlayer2Keys());
        } else {
            this.characters[0] = CharacterSpawner.createCharacter(this.characterTypeP1,
                startX, startY, Entity.Team.PLAYER1, 1);
            this.characters[1] = null;
        }
        // P1 Controls: Left, Right, Space (Shoot)
        this.characters[0].setControlKeys(Core.getInputManager().getPlayer1Keys());
        
        this.enemyShipSpecialCooldown = Core.getVariableCooldown(BONUS_SHIP_INTERVAL,
            BONUS_SHIP_VARIANCE);
        this.enemyShipSpecialCooldown.reset();
        this.enemyShipSpecialExplosionCooldown = Core.getCooldown(BONUS_SHIP_EXPLOSION);
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
                SoundManager.loopStop();
            } else {
                SoundManager.playLoop("game_theme");
            }
        }
        
        if (this.isPaused && inputManager.isKeyDown(KeyEvent.VK_BACK_SPACE)
            && this.returnMenuCooldown.checkFinished()) {
            SoundManager.playOnce("select");
            SoundManager.stopAllMusic();
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
                    
                    if (character == null || character.isDestroyed()) {
                        continue;
                    }
                    
                    // Active Item Input (Handled here as it interacts with GameState directly)
                    if (p == 0 && lastPressed == KeyEvent.VK_Q) {
                        state.useFirstActiveItem(0);
                    }
                    if (p == 1 && lastPressed == KeyEvent.VK_SLASH) {
                        state.useFirstActiveItem(1);
                    }
                    
                    // Handle Input (Movement & Shooting) via Character class
                    // Pass deltaTime for smooth movement calculation
                    boolean shotFired = character.handleMovement(inputManager, this, this.weapons,
                        deltaTime);
                    
                    if (shotFired) {
                        SoundManager.playOnce("shoot");
                        state.incBulletsShot(p);
                    }
                    
                    // Update Character State (Buffs, Cooldowns, etc.)
                    character.update(deltaTime);
                }
                // -----------------------------
                
                // Special Enemy Logic
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
                
                // Boss Update
                if (this.bossShip != null) {
                    if (!this.state.areEnemiesFrozen()) {
                        this.bossShip.update();
                    }
                }
                
                // Formation Update
                this.enemyShipFormation.update(this.state);
                if (this.state == null || !this.state.areEnemiesFrozen()) {
                    int bulletsBefore = this.weapons.size();
                    this.enemyShipFormation.shoot(this.weapons);
                    if (this.weapons.size() > bulletsBefore) {
                        SoundManager.playOnce("shoot_enemies");
                    }
                }
            }
            
            manageCollisions();
            cleanBullets();
            cleanItems();
            manageItemPickups();
            
            state.updateEffects();
            this.basicGameSpace.setLastLife(state.getLivesRemaining() == 1);
            draw();
            
            if (!sessionHighScoreNotified && this.state.getScore() > this.topScore) {
                sessionHighScoreNotified = true;
                this.highScoreNotified = true;
                this.highScoreNoticeStartTime = System.currentTimeMillis();
            }
            
            boolean bossDestroyed = (this.bossShip != null && this.bossShip.isDestroyed());
            
            if ((this.enemyShipFormation.isEmpty() || !state.teamAlive()) && !this.levelFinished) {
                WeaponPool.recycle(this.weapons);
                this.weapons.removeAll(this.weapons);
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
        
        // Draw Characters
        for (GameCharacter character : this.characters) {
            if (character != null) {
                drawManager.getEntityRenderer()
                    .drawEntity(drawManager.getBackBufferGraphics(), character,
                        character.getPositionX(),
                        character.getPositionY());
            }
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
        
        for (Weapon weapon : this.weapons) {
            drawManager.getEntityRenderer()
                .drawEntity(drawManager.getBackBufferGraphics(), weapon, weapon.getPositionX(),
                    weapon.getPositionY());
        }
        
        for (Item item : this.items) {
            drawManager.getEntityRenderer()
                .drawEntity(drawManager.getBackBufferGraphics(), item, item.getPositionX(),
                    item.getPositionY());
        }
        
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
        
        drawManager.getGameScreenRenderer()
            .drawAchievementToasts(drawManager.getBackBufferGraphics(), this,
                (this.achievementManager != null) ? this.achievementManager.getActiveToasts()
                    : Collections.emptyList());
        
        if (this.state.areEnemiesFrozen()) {
            Graphics2D g2d = (Graphics2D) drawManager.getBackBufferGraphics().create();
            try {
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON);
                
                String text = "TIME FREEZE";
                g2d.setFont(drawManager.getCommonRenderer().getFontBig());
                FontMetrics fm = g2d.getFontMetrics();
                int textWidth = fm.stringWidth(text);
                int textHeight = fm.getHeight();
                int boxWidth = textWidth + 40;
                int boxHeight = textHeight + 20;
                int x = (this.getWidth() - boxWidth) / 2;
                int y = (this.getHeight() - boxHeight) / 2;
                g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.20f));
                g2d.setColor(Color.BLACK);
                g2d.fillRoundRect(x, y, boxWidth, boxHeight, 16, 16);
                g2d.setComposite(AlphaComposite.SrcOver);
                g2d.setColor(new Color(0, 255, 255, 140));
                g2d.setStroke(new BasicStroke(2f));
                g2d.drawRoundRect(x, y, boxWidth, boxHeight, 16, 16);
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
    
    private void cleanBullets() {
        Set<Weapon> recyclable = new HashSet<Weapon>();
        for (Weapon weapon : this.weapons) {
            weapon.update();
            if (weapon.getPositionY() < SEPARATION_LINE_HEIGHT
                || weapon.getPositionY() > this.height) {
                recyclable.add(weapon);
            }
        }
        this.weapons.removeAll(recyclable);
        WeaponPool.recycle(recyclable);
    }
    
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
                        playerIndex = 0;
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
    
    private void manageCollisions() {
        Set<Weapon> recyclable = new HashSet<Weapon>();
        for (Weapon weapon : this.weapons) {
            if (weapon.getOwnerPlayerId() == 0) {
                // Enemy bullet vs Players
                for (int p = 0; p < GameState.NUM_PLAYERS; p++) {
                    GameCharacter character = this.characters[p];
                    if (character != null && !character.isDestroyed()
                        && checkCollision(weapon, character) && !this.levelFinished) {
                        recyclable.add(weapon);
                        
                        this.drawManager.getGameScreenRenderer()
                            .triggerExplosion(character.getPositionX(), character.getPositionY(),
                                false, state.getLivesRemaining() == 1);
                        
                        character.destroy(); // Or implement HP reduction logic in GameCharacter
                        
                        SoundManager.playOnce("explosion");
                        this.state.decLife(p);
                        
                        this.tookDamageThisLevel = true;
                        this.basicGameSpace.setLastLife(state.getLivesRemaining() == 1);
                        this.LOGGER.info("Hit on player " + (p + 1) + ", team lives now: "
                            + state.getLivesRemaining());
                        break;
                    }
                }
            } else {
                // Player bullet vs Enemies
                final int ownerId = weapon.getOwnerPlayerId();
                final int pIdx = (ownerId == 2) ? 1 : 0;
                boolean finalShip = this.enemyShipFormation.lastShip();
                
                for (EnemyShip enemyShip : this.enemyShipFormation) {
                    if (!enemyShip.isDestroyed() && checkCollision(weapon, enemyShip)) {
                        recyclable.add(weapon);
                        enemyShip.hit();
                        
                        if (enemyShip.isDestroyed()) {
                            int points = enemyShip.getPointValue();
                            state.addCoins(pIdx, enemyShip.getCoinValue());
                            drawManager.getGameScreenRenderer()
                                .triggerExplosion(enemyShip.getPositionX(),
                                    enemyShip.getPositionY(), true, finalShip);
                            state.addScore(pIdx, points);
                            state.incShipsDestroyed(pIdx);
                            
                            Item drop = ItemManager.getInstance().obtainDrop(enemyShip);
                            if (drop != null) {
                                this.items.add(drop);
                                this.LOGGER.info(
                                    "Spawned " + drop.getType() + " at " + drop.getPositionX() + ","
                                        + drop.getPositionY());
                            }
                            
                            this.enemyShipFormation.destroy(enemyShip);
                            SoundManager.playOnce("invader_killed");
                            checkAchievement();
                        }
                        break;
                    }
                }
                
                if (this.enemyShipSpecial != null
                    && !this.enemyShipSpecial.isDestroyed()
                    && checkCollision(weapon, this.enemyShipSpecial)) {
                    int points = this.enemyShipSpecial.getPointValue();
                    state.addCoins(pIdx, this.enemyShipSpecial.getCoinValue());
                    state.addScore(pIdx, points);
                    state.incShipsDestroyed(pIdx);
                    
                    this.enemyShipSpecial.destroy();
                    SoundManager.loopStop();
                    SoundManager.playOnce("explosion");
                    drawManager.getGameScreenRenderer()
                        .triggerExplosion(this.enemyShipSpecial.getPositionX(),
                            this.enemyShipSpecial.getPositionY(), true, true);
                    this.enemyShipSpecialExplosionCooldown.reset();
                    recyclable.add(weapon);
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
                        
                        SoundManager.loopStop();
                        SoundManager.playOnce("explosion");
                        drawManager.getGameScreenRenderer()
                            .triggerExplosion(this.bossShip.getPositionX(),
                                this.bossShip.getPositionY(), true, true);
                    }
                }
            }
        }
        this.weapons.removeAll(recyclable);
        WeaponPool.recycle(recyclable);
    }
    
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
    
    public final GameState getGameState() {
        return this.state;
    }
    
    public void checkAchievement() {
        if (state.getShipsDestroyed() == 1) {
            achievementManager.unlock("First Blood");
        }
        if (levelFinished && this.enemyShipFormation.isEmpty() && state.getLevel() == 5) {
            achievementManager.unlock("Clear");
            float p1Acc = state.getBulletsShot(0) > 0
                ? (float) state.getShipsDestroyed(0) / state.getBulletsShot(0) * 100 : 0f;
            float p2Acc = state.getBulletsShot(1) > 0
                ? (float) state.getShipsDestroyed(1) / state.getBulletsShot(1) * 100 : 0f;
            if (!this.tookDamageThisLevel) {
                achievementManager.unlock("Survivor");
            }
            if (p1Acc >= 80) {
                achievementManager.unlock("Sharpshooter");
                if (p2Acc >= 80) {
                    achievementManager.unlock("Sharpshooter");
                }
            }
        }
        if (state.getBulletsShot() >= 50) {
            achievementManager.unlock("50 Bullets");
        }
        if (state.getScore() >= 3000) {
            achievementManager.unlock("Get 3000 Score");
        }
    }
    
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
}