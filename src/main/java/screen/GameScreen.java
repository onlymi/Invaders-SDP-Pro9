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
import engine.gameplay.item.ItemEffect;
import engine.gameplay.item.ItemManager;
import engine.utils.Cooldown;
import entity.BossShip;
import entity.EnemyShip;
import entity.Entity;
import entity.Item;
import entity.ItemPool;
import entity.Pet;
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
    public static final int SEPARATION_LINE_HEIGHT = 70;
    private static final int HIGH_SCORE_NOTICE_DURATION = 2000;
    private static boolean sessionHighScoreNotified = false;
    
    private AchievementManager achievementManager;
    private GameSettings gameSettings;
    private BasicGameSpace basicGameSpace;
    /**
     * Boss ship for boss stage.
     */
    private BossShip bossShip;
    /**
     * Current difficulty level number.
     */
    private final int level;
    /**
     * Enemy manager.
     */
    private EnemyManager enemyManager;
    /**
     * Characters (Players) in the game.
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
    
    /**
     * checks if player took damage 2025-10-02 add new variable
     */
    private boolean tookDamageThisLevel;
    private boolean countdownSoundPlayed = false;
    
    private final GameState state;
    
    private CharacterType characterTypeP1;
    private CharacterType characterTypeP2;
    
    private int killsToWin;
    private int enemyKillCount;
    
    private final Set<Pet> pets = new HashSet<>();
    
    private Cooldown inputDelay;
    
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
        
        // Background
        this.basicGameSpace = new BasicGameSpace(100, this.width, this.height);
        
        this.enemyManager = new EnemyManager(this);
        this.enemyKillCount = 0;
        // 각 스테이지 별 필요 kill 수
        this.killsToWin = 10 + (this.level * 5);
        this.levelCleared = false; // Initialize to false
        
        // --- Character Initialization & Control Setup ---
        this.enemyManager = new EnemyManager(this);
        if (this.level == 6) {
            int bossWidth = 480;
            this.bossShip = new BossShip(this.width / 2 - bossWidth / 2, 40);
            this.LOGGER.info("Boss Stage Initialized!");
            this.basicGameSpace.setBossStage(true);
        } else {
            this.bossShip = null;
        }
        
        int startX = this.width / 2 - Core.getAssetManager().getCharacterWidth() / 2;
        int startY = this.height - Core.getAssetManager().getCharacterHeight() - 10;
        int gapBetweenCharacters = 64;
        
        // Characters & controls
        if (state.isCoop()) {
            this.characters[0] = CharacterSpawner.createCharacter(this.characterTypeP1,
                startX - gapBetweenCharacters, startY, Entity.Team.PLAYER1, 1);
            this.characters[0].setGameState(this.state);
            
            this.characters[1] = CharacterSpawner.createCharacter(this.characterTypeP2,
                startX + gapBetweenCharacters, startY, Entity.Team.PLAYER2, 2);
            this.characters[1].setGameState(this.state);
            
            this.characters[1].setControlKeys(Core.getInputManager().getPlayer2Keys());
        } else {
            this.characters[0] = CharacterSpawner.createCharacter(this.characterTypeP1,
                startX, startY, Entity.Team.PLAYER1, 1);
            this.characters[0].setGameState(this.state);
            this.characters[0].setControlKeys(Core.getInputManager().getPlayer1Keys());
            this.characters[1] = null;
        }
        // P1 Controls
        this.characters[0].setControlKeys(Core.getInputManager().getPlayer1Keys());
        
        if (state.getLevel() > 1) {
            // Player 1 체력 불러오기
            if (this.characters[0] != null) {
                this.characters[0].setCurrentHealthPoints(state.getPlayerHealth(0));
            }
            // Player 2 체력 불러오기
            if (this.characters[1] != null) {
                this.characters[1].setCurrentHealthPoints(state.getPlayerHealth(1));
            }
        }
        
        this.screenFinishedCooldown = Core.getCooldown(SCREEN_CHANGE_INTERVAL);
        this.weapons = new HashSet<Weapon>();
        this.items = new HashSet<Item>();
        this.basicGameSpace = new BasicGameSpace(100, this.width, this.height);
        this.pets.clear();
        
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
        
        checkAchievement();
        
        // Pause toggle
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
        
        // Return to menu
        if (this.isPaused && inputManager.isKeyDown(KeyEvent.VK_BACK_SPACE)
            && this.returnMenuCooldown.checkFinished()) {
            SoundManager.playOnce("select");
            SoundManager.stopAllMusic();
            returnCode = 1;
            this.isRunning = false;
        }
        
        if (!this.isPaused) {
            if (this.inputDelay.checkFinished() && !this.levelFinished) {
                
                float deltaTime = 1.0f / this.fps;
                int lastPressed = inputManager.getLastPressedKey();
                
                // Characters update
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
                    
                    character.handleKeyboard(inputManager, this, this.weapons, deltaTime);
                    // Handle Input (Movement & Shooting)
                    boolean shotFired = character.isFiring();
                    
                    if (shotFired) {
                        SoundManager.playOnce("shoot");
                        state.incBulletsShot(p);
                    }
                }
                
                // despawn based on active effect
                updatePetsFromEffects();
                
                // Boss or enemy manager
                if (this.bossShip != null) {
                    this.bossShip.update();
                    if (!this.bossShip.isDestroyed()) {
                        this.bossShip.updateAttackPattern(this.characters);
                    }
                } else {
                    this.enemyManager.update();
                }
                
                // Enemy shooting (respecting freeze if GameState uses it)
                if (this.state == null || !this.state.areEnemiesFrozen()) {
                    int bulletsBefore = this.weapons.size();
                    // this.enemyManager.shoot(this.weapons); // Assuming handled inside manager or uncomment if needed
                    if (this.weapons.size() > bulletsBefore) {
                        SoundManager.playOnce("shoot_enemies");
                    }
                }
                
                updatePetsLogic();
                manageCollisions();
                cleanBullets();
                
                cleanItems();
                manageItemPickups();
            }
            
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
            if ((this.enemyKillCount >= this.killsToWin || (!state.teamAlive() && !teamAlive))
                && !this.levelFinished) {
                
                WeaponPool.recycle(this.weapons);
                this.weapons.removeAll(this.weapons);
                ItemPool.recycle(items);
                this.items.removeAll(this.items);
                
                this.levelFinished = true;
                this.screenFinishedCooldown.reset();
                
                // Set levelCleared only if objective met
                if (this.enemyKillCount >= this.killsToWin) {
                    this.levelCleared = true;
                    
                    if (this.characters[0] != null) {
                        state.setPlayerHealth(0, this.characters[0].getCurrentHealthPoints());
                    }
                    if (state.isCoop() && this.characters[1] != null) {
                        state.setPlayerHealth(1, this.characters[1].getCurrentHealthPoints());
                    }
                    
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
        
        // Characters
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
        
        // Pets
        for (Pet pet : this.pets) {
            if (pet.isDead() || pet.isExpired()) {
                continue;
            }
            
            drawManager.getEntityRenderer()
                .drawEntity(drawManager.getBackBufferGraphics(), pet,
                    pet.getPositionX(), pet.getPositionY());
        }
        
        // Boss
        if (this.bossShip != null) {
            drawManager.getEntityRenderer()
                .drawEntity(drawManager.getBackBufferGraphics(), this.bossShip,
                    this.bossShip.getPositionX(), this.bossShip.getPositionY());
            for (Weapon bossWeapon : this.bossShip.getProjectiles()) {
                drawManager.getEntityRenderer().drawEntity(
                    drawManager.getBackBufferGraphics(), bossWeapon,
                    bossWeapon.getPositionX(), bossWeapon.getPositionY());
            }
            drawManager.drawBossHpBar(this.bossShip, this);
        }
        
        // Enemies
        this.enemyManager.draw();
        
        // Weapons
        for (Weapon weapon : this.weapons) {
            drawManager.getEntityRenderer()
                .drawEntity(drawManager.getBackBufferGraphics(), weapon, weapon.getPositionX(),
                    weapon.getPositionY());
        }
        
        // Items
        for (Item item : this.items) {
            drawManager.getEntityRenderer()
                .drawEntity(drawManager.getBackBufferGraphics(), item, item.getPositionX(),
                    item.getPositionY());
        }
        
        // Aggregate UI
        drawManager.getGameScreenRenderer()
            .drawScore(drawManager.getBackBufferGraphics(), this, this.state.getScore());
        // drawManager.getGameScreenRenderer()
        //     .drawLives(drawManager.getBackBufferGraphics(), this, state.getLivesRemaining(),
        //         state.isCoop());
        drawManager.getGameScreenRenderer()
            .drawCoins(drawManager.getBackBufferGraphics(), this, this.state.getCoins());
        drawManager.getGameScreenRenderer()
            .drawLevel(drawManager.getBackBufferGraphics(), this, this.state.getLevel());
        drawManager.getCommonRenderer()
            .drawHorizontalLine(drawManager.getBackBufferGraphics(), this,
                SEPARATION_LINE_HEIGHT - 1);
        
        int remainingKills = Math.max(0, this.killsToWin - this.enemyKillCount);
        drawManager.getGameScreenRenderer().drawShipCount(drawManager.getBackBufferGraphics(),
            this, remainingKills);
        drawManager.getGameScreenRenderer()
            .drawItemToast(drawManager.getBackBufferGraphics(), this);
        drawManager.getGameScreenRenderer()
            .drawActiveItemSlots(drawManager.getBackBufferGraphics(), this, this.state);
        drawManager.getGameScreenRenderer()
            .drawCharacterSkillSlots(drawManager.getBackBufferGraphics(), this, this.state,
                this.characters);
        
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
        
        // TIME FREEZE overlay
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
            
            // 보스 패턴 무기(해골, 레이저)는 화면 밖으로 나가도 삭제하지 않음
            boolean isBossPatternWeapon = (weapon.getSpriteType() == SpriteType.GasterBlaster
                || weapon.getSpriteType() == SpriteType.BigLaserBeam);
            
            boolean isPiercingArrow
                = (weapon.getSpriteType() == SpriteType.CharacterArcherUltimateSkill);
            
            boolean isOffScreenY;
            if (isPiercingArrow) {
                // 궁극기 화살은 꼬리까지 완전히 화면 밖으로 나갔을 때 삭제 (Y + 높이가 0보다 작을 때)
                isOffScreenY = (weapon.getPositionY() + weapon.getHeight() < 0)
                    || weapon.getPositionY() > this.height;
            } else {
                isOffScreenY = weapon.getPositionY() < SEPARATION_LINE_HEIGHT
                    || weapon.getPositionY() > this.height;
            }
            
            boolean isOffScreenX = weapon.getPositionX() < 0
                || weapon.getPositionX() > this.width;
            
            boolean isOffScreen = isOffScreenY || isOffScreenX;
            
            // 삭제 조건: 보스 무기가 아니고 화면 밖으로 나갔거나, 수명이 다한 경우
            if ((!isBossPatternWeapon && isOffScreen) || weapon.isExpired()) {
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
            
            boolean offScreen =
                item.getPositionY() > this.height
                    || item.getPositionY() < SEPARATION_LINE_HEIGHT
                    || item.getPositionX() < 0
                    || item.getPositionX() > this.width;
            if (item.isExpired() || offScreen) {
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
                        playerIndex = 0;
                    }
                    
                    ActivationType activationType = item.getActivationType();
                    boolean autoUseOnPickup = item.isAutoUseOnPickup();
                    String itemType = item.getType();
                    
                    switch (activationType) {
                        case INSTANT_ON_PICKUP:
                        case TEMPORARY_BUFF:
                            if (autoUseOnPickup) {
                                if ("HEAL".equals(itemType)) {
                                    // CSV에서 회복량 가져오기 (없으면 기본 1)
                                    int healAmount = 1;
                                    if (item.getData() != null) {
                                        healAmount = item.getData().getEffectValue();
                                    }
                                    
                                    ItemEffect.applyHealToCharacter(
                                        getGameState(),
                                        character,
                                        healAmount
                                    );
                                } else {
                                    // 나머지 인스턴트 아이템은 기존 로직 유지
                                    boolean applied = item.applyEffect(
                                        getGameState(),
                                        character.getPlayerId()
                                    );
                                }
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
     * Enemy bullets hit players/pets → decrement TEAM lives / damage pets; player bullets hit
     * enemies → add score.
     */
    private void manageCollisions() {
        Set<Weapon> recyclable = new HashSet<Weapon>();
        for (Weapon weapon : this.weapons) {
            if (weapon.getOwnerPlayerId() == 0) {
                // Enemy weapon vs players / pets
                
                // 가스터 블래스터(해골)는 충돌/피격 판정이 없으므로 무시
                if (weapon.getSpriteType() == SpriteType.GasterBlaster) {
                    continue;
                }
                
                // 레이저 여부 확인
                boolean isLaser = (weapon.getSpriteType() == SpriteType.BigLaserBeam);
                
                boolean handled = false;
                
                for (int p = 0; p < GameState.NUM_PLAYERS; p++) {
                    GameCharacter character = this.characters[p];
                    if (character != null && character.getCurrentHealthPoints() > 0
                        && checkCollision(weapon, character) && !this.levelFinished) {
                        
                        // 1. 무적 시간 확인
                        if (character.isInvincible()) {
                            continue;
                        }
                        // 2. 단발성 무기 중복 피격 방지 (레이저는 제외)
                        if (!isLaser && weapon.getDuration() == -1 && weapon.isHitPlayer(p)) {
                            continue;
                        }
                        // 3. 쉴드 효과 확인
                        boolean hasShieldEffect =
                            state != null && state.hasEffect(p,
                                engine.gameplay.item.ItemEffect.ItemEffectType.SHIELD
                            );
                        
                        if (hasShieldEffect) {
                            LOGGER.info("[GameScreen] Shield blocked damage for player " + (p + 1));
                            if (!isLaser) {
                                recyclable.add(weapon);
                            }
                            // 쉴드가 공격을 막았으므로, 더 이상 피해 처리 로직을 진행하지 않고 다음 플레이어로 넘어갑니다.
                            // 레이저는 파괴되지 않으므로, 이 시점에서 루프를 빠져나갈지 여부를 결정해야 합니다.
                            // 현재 코드는 `break`가 없으므로 다음 플레이어에게도 쉴드가 있는지 검사합니다.
                            continue; // 현재 플레이어는 처리 완료.
                        }
                        
                        // 4. 피해 처리
                        character.takeDamage(weapon.getDamage());
                        
                        if (character.getCurrentHealthPoints() <= 0) {
                            this.state.decLife(p);
                            this.LOGGER.info("Player " + (p + 1) + " died. Lives remaining: "
                                + state.getLivesRemaining());
                            
                            handled = true;
                            break;
                            
                        }
                        
                        this.drawManager.getGameScreenRenderer()
                            .triggerExplosion(
                                character.getPositionX(),
                                character.getPositionY(),
                                false,
                                state.getLivesRemaining() == 1
                            );
                        SoundManager.playOnce("explosion");
                        this.tookDamageThisLevel = true;
                        this.basicGameSpace.setLastLife(state.getLivesRemaining() == 1);
                        
                        // 5. 무기 제거 또는 히트 기록
                        // 단발성 무기 (일반 투사체)는 제거
                        if (!isLaser && weapon.getDuration() == -1) {
                            recyclable.add(weapon);
                        } else {
                            // 레이저나 근접 무기처럼 지속되는 무기는 피격 기록만 추가
                            weapon.addHitPlayer(p);
                        }
                        // 단발성 무기는 한 번의 충돌로 제거되므로 break
                        if (!isLaser && weapon.getDuration() == -1) {
                            break;
                        }
                        
                    }
                }
                
                if (handled) {
                    continue;
                }
                
                // Pet 충돌 로직 (레이저에 펫이 죽게 할지 여부는 선택사항, 여기선 기존 로직 유지하되 레이저 보호)
                for (Pet pet : pets) {
                    if (pet.isDead() || pet.isExpired()) {
                        continue;
                    }
                    
                    if (checkCollision(weapon, pet) && !this.levelFinished) {
                        // 레이저는 펫을 뚫고 지나감 (삭제 안 함)
                        if (!isLaser) {
                            recyclable.add(weapon);
                        }
                        pet.takeDamage(1);
                        
                        this.LOGGER.info("[GameScreen] Pet hit by enemy weapon. owner="
                            + pet.getOwnerPlayerId());
                        
                        break;
                    }
                }
                
            } else {
                // Player weapon vs enemies
                final int ownerId = weapon.getOwnerPlayerId();
                final int pIdx = (ownerId == 2) ? 1 : 0;
                boolean finalShip = this.enemyManager.lastShip();
                
                final boolean isExplosiveWeapon = weapon.isExplosive();
                
                for (EnemyShip enemyShip : this.enemyManager.getEnemies()) {
                    if (!enemyShip.isDestroyed() && checkCollision(weapon, enemyShip)) {
                        if (isExplosiveWeapon) {
                            // Rocket splash damage
                            applyExplosiveDamage(weapon, pIdx);
                        } else {
                            boolean isPiercing = (weapon.getSpriteType()
                                == SpriteType.CharacterArcherUltimateSkill);
                            
                            if (isPiercing) {
                                if (weapon.isHitEnemy(enemyShip)) {
                                    continue;
                                }
                                weapon.addHitEnemy(enemyShip);
                            } else {
                                recyclable.add(weapon);
                            }
                            
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
                                        "Spawned " + drop.getType() + " at " + drop.getPositionX()
                                            + "," + drop.getPositionY());
                                }
                                
                                this.enemyManager.destroy(enemyShip);
                                SoundManager.playOnce("invader_killed");
                                this.LOGGER.info("Hit on enemy.");
                                
                                checkAchievement();
                            }
                        }
                        break;
                    }
                }
                
                if (this.bossShip != null
                    && !this.bossShip.isDestroyed()
                    && checkCollision(weapon, this.bossShip)) {
                    this.bossShip.hit(weapon.getDamage());
                    recyclable.add(weapon);
                    
                    if (this.bossShip.isDestroyed()) {
                        int points = this.bossShip.getPointValue();
                        state.addCoins(pIdx, this.bossShip.getCoinValue());
                        state.addScore(pIdx, points);
                        state.incShipsDestroyed(pIdx);
                        
                        SoundManager.loopStop(); // Stop boss BGM
                        SoundManager.playOnce("explosion");
                        drawManager.getGameScreenRenderer()
                            .triggerExplosion(this.bossShip.getPositionX(),
                                this.bossShip.getPositionY(), true, true);
                        Random rand = new Random();
                        for (int i = 0; i < 10; i++) {
                            int offsetX =
                                rand.nextInt(this.bossShip.getWidth()) - this.bossShip.getWidth()
                                    / 2;
                            int offsetY =
                                rand.nextInt(this.bossShip.getHeight()) - this.bossShip.getHeight()
                                    / 2;
                            
                            Color explosionColor = new Color(255, rand.nextInt(150), 0);
                            
                            drawManager.getGameScreenRenderer().triggerCustomExplosion(
                                this.bossShip.getPositionX()
                                    + this.bossShip.getWidth() / 2 + offsetX,
                                this.bossShip.getPositionY()
                                    + this.bossShip.getHeight() / 2 + offsetY,
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
                }
            }
        }
        if (this.bossShip != null && !this.bossShip.isDestroyed()) {
            for (Weapon bossWeapon : this.bossShip.getProjectiles()) {
                // 가스터 블래스터(해골)는 충돌 무시
                if (bossWeapon.getSpriteType() == SpriteType.GasterBlaster) {
                    continue;
                }
                
                boolean isLaser = (bossWeapon.getSpriteType() == SpriteType.BigLaserBeam);
                
                for (int p = 0; p < GameState.NUM_PLAYERS; p++) {
                    GameCharacter player = this.characters[p];
                    if (player == null || player.getCurrentHealthPoints() <= 0
                        || player.isInvincible()) {
                        continue;
                    }
                    
                    // [START: 누락된 보스 무기 피해 로직 복원]
                    if (checkCollision(bossWeapon, player)) {
                        
                        // 단발성 무기 중복 피격 방지 (레이저는 제외)
                        if (!isLaser && bossWeapon.getDuration() == -1 && bossWeapon.isHitPlayer(
                            p)) {
                            continue;
                        }
                        
                        // 쉴드 효과 검사 (아이템팀이 이 로직을 여기에 넣지 않고 윗단에만 넣었을 가능성이 있음)
                        boolean hasShieldEffect =
                            state != null && state.hasEffect(p,
                                engine.gameplay.item.ItemEffect.ItemEffectType.SHIELD
                            );
                        
                        if (hasShieldEffect) {
                            LOGGER.info(
                                "[GameScreen] Shield blocked damage for player (Boss Weapon) " + (p
                                    + 1));
                            if (!isLaser) {
                                bossWeapon.setDuration(0); // 총알 제거
                            }
                            continue;
                        }
                        
                        // 1. 데미지 처리
                        player.takeDamage(bossWeapon.getDamage());
                        
                        // 2. 생명력 감소 및 사망 처리
                        if (player.getCurrentHealthPoints() <= 0) {
                            this.state.decLife(p);
                        }
                        
                        // 3. 이펙트 및 사운드
                        this.drawManager.getGameScreenRenderer().triggerExplosion(
                            player.getPositionX(), player.getPositionY(), false, false);
                        SoundManager.playOnce("explosion");
                        
                        // 4. 무기 제거 또는 히트 기록
                        if (!isLaser && bossWeapon.getDuration() == -1) {
                            // 일반 탄막: 즉시 만료 처리하여 BossShip 내부의 updateProjectiles()에서 제거되도록 함
                            bossWeapon.setDuration(0);
                        } else {
                            // 지속형 무기: 히트 기록 (레이저는 연속 타격을 위해 이 로직을 건너뛰어야 함)
                            if (!isLaser) {
                                bossWeapon.addHitPlayer(p);
                            }
                            
                            this.LOGGER.info(
                                "Collision! Player " + (p + 1) + " hit by enemy body.");
                        }
                    }
                    // [END: 누락된 보스 무기 피해 로직 복원]
                    
                }
            }
        }
        for (int p = 0; p < GameState.NUM_PLAYERS; p++) {
            GameCharacter player = this.characters[p];
            if (player == null || player.getCurrentHealthPoints() <= 0 || player.isInvincible()) {
                continue;
            }
            
            for (EnemyShip enemy : this.enemyManager.getEnemies()) {
                if (!enemy.isDestroyed() && checkCollision(player, enemy)) {
                    // 실드 아이템 로직
                    boolean hasShieldEffect = state != null && state.hasEffect(p,
                        engine.gameplay.item.ItemEffect.ItemEffectType.SHIELD);
                    if (hasShieldEffect) {
                        state.clearEffect(p, engine.gameplay.item.ItemEffect.ItemEffectType.SHIELD);
                        // 넉백 처리
                        double dx = enemy.getPositionX() - player.getPositionX();
                        double dy = enemy.getPositionY() - player.getPositionY();
                        double dist = Math.sqrt(dx * dx + dy * dy);
                        if (dist > 0) {
                            enemy.pushBack((dx / dist) * 10.0, (dy / dist) * 10.0);
                        }
                        continue;
                    }
                    
                    player.takeDamage(enemy.getCollisionDamage());
                    if (enemy instanceof entity.EnemyTypeC && enemy.getCollisionDamage() > 5) {
                        engine.SoundManager.playOnce("stabbing");
                    }
                    
                    // 사망 처리
                    if (player.getCurrentHealthPoints() <= 0) {
                        this.state.decLife(p);
                    }
                    
                    // 충돌 넉백 효과
                    double dx = enemy.getPositionX() - player.getPositionX();
                    double dy = enemy.getPositionY() - player.getPositionY();
                    double dist = Math.sqrt(dx * dx + dy * dy);
                    if (dist > 0) {
                        enemy.pushBack((dx / dist) * 10.0, (dy / dist) * 10.0);
                    }
                    
                    this.LOGGER.info("Collision! Player " + (p + 1) + " hit by enemy body.");
                }
            }
        }
        
        this.weapons.removeAll(recyclable);
        WeaponPool.recycle(recyclable);
    }
    
    private void applyExplosiveDamage(Weapon weapon, int pIdx) {
        float radius = weapon.getExplosionRadius();
        if (radius <= 0f) {
            return;
        }
        
        int centerX = weapon.getPositionX() + weapon.getWidth() / 2;
        int centerY = weapon.getPositionY() + weapon.getHeight() / 2;
        float radiusSq = radius * radius;
        
        boolean finalShip = this.enemyManager.lastShip();
        boolean anyKill = false;
        
        // ConcurrentModification 방지
        java.util.List<EnemyShip> enemiesSnapshot =
            new java.util.ArrayList<>(this.enemyManager.getEnemies());
        
        for (EnemyShip enemyShip : enemiesSnapshot) {
            if (enemyShip.isDestroyed()) {
                continue;
            }
            
            int ex = enemyShip.getPositionX() + enemyShip.getWidth() / 2;
            int ey = enemyShip.getPositionY() + enemyShip.getHeight() / 2;
            float dx = ex - centerX;
            float dy = ey - centerY;
            float distSq = dx * dx + dy * dy;
            
            if (distSq <= radiusSq) {
                enemyShip.hit(weapon.getDamage());
                
                if (enemyShip.isDestroyed()) {
                    anyKill = true;
                    
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
                            "Spawned " + drop.getType() + " at " + drop.getPositionX()
                                + "," + drop.getPositionY());
                    }
                    
                    this.enemyManager.destroy(enemyShip);
                    SoundManager.playOnce("invader_killed");
                    this.LOGGER.info("Hit on enemy (explosive).");
                }
            }
        }
        
        drawManager.getGameScreenRenderer().triggerCustomExplosion(
            centerX,
            centerY,
            new Color(255, 180, 80)
        );
        
        if (anyKill) {
            checkAchievement();
        }
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
        if (b instanceof BossShip boss) {
            for (java.awt.Rectangle r2 : boss.getHitboxRectangles()) {
                
                // BossShip의 개별 히트박스와 무기(a)의 충돌 검사
                if (a.getRotation() != 0) {
                    // 무기(a)가 회전된 경우: Area 사용
                    Area areaA = new Area(r1);
                    AffineTransform atA = new AffineTransform();
                    
                    double anchorX = r1.getCenterX();
                    double anchorY = r1.getCenterY();
                    
                    if (a.getSpriteType() == SpriteType.BigLaserBeam) {
                        anchorY = r1.getY();
                    }
                    
                    atA.rotate(Math.toRadians(a.getRotation()), anchorX, anchorY);
                    areaA.transform(atA);
                    
                    Area areaB = new Area(r2); // BossShip의 히트박스는 회전하지 않음
                    areaA.intersect(areaB);
                    if (!areaA.isEmpty()) {
                        return true; // 충돌 발견 시 즉시 반환
                    }
                } else {
                    // 무기(a)가 회전되지 않은 경우: 단순 intersects 사용
                    if (r1.intersects(r2)) {
                        return true; // 충돌 발견 시 즉시 반환
                    }
                }
            }
            // 모든 히트박스와 충돌하지 않은 경우
            return false;
        }
        
        Rectangle r2 = new Rectangle(b.getPositionX(), b.getPositionY(),
            b.getWidth(), b.getHeight());
        
        if (a.getRotation() == 0 && b.getRotation() == 0) {
            return r1.intersects(r2);
        }
        
        // 3. When there is rotation: Precise shape collision detection (using Area)
        Area areaA = new Area(r1);
        Area areaB = new Area(r2);
        
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
        
        if (b.getRotation() != 0) {
            AffineTransform atB = new AffineTransform();
            atB.rotate(Math.toRadians(b.getRotation()), r2.getCenterX(), r2.getCenterY());
            areaB.transform(atB);
        }
        
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
     * Spawns or removes pets based on the PET_SUPPORT effect.
     */
    private void updatePetsFromEffects() {
        for (int p = 0; p < GameState.NUM_PLAYERS; p++) {
            GameCharacter owner = this.characters[p];
            if (owner == null || owner.isInvincible()) {
                continue;
            }
            
            boolean hasGunPet =
                state != null && state.hasEffect(
                    p,
                    engine.gameplay.item.ItemEffect.ItemEffectType.PET_SUPPORT
                );
            
            boolean hasRocketPet =
                state != null && state.hasEffect(
                    p,
                    engine.gameplay.item.ItemEffect.ItemEffectType.PET_ROCKET_SUPPORT
                );
            
            if (!hasGunPet && !hasRocketPet) {
                continue;
            }
            
            int playerId = p + 1;
            
            Pet.PetKind kind = hasRocketPet ? Pet.PetKind.ROCKET : Pet.PetKind.GUN;
            
            spawnPetForPlayer(playerId, owner, kind);
            
            if (hasRocketPet) {
                state.clearEffect(
                    p,
                    engine.gameplay.item.ItemEffect.ItemEffectType.PET_ROCKET_SUPPORT
                );
            }
            if (hasGunPet) {
                state.clearEffect(
                    p,
                    engine.gameplay.item.ItemEffect.ItemEffectType.PET_SUPPORT
                );
            }
            
            Core.getLogger().info("[GameScreen] Spawned PET (" + kind
                + ") for player " + playerId + " (multi-pet enabled)");
        }
    }
    
    private boolean hasPetForPlayer(int playerId) {
        for (Pet pet : pets) {
            if (pet.getOwnerPlayerId() == playerId && !pet.isExpired()) {
                return true;
            }
        }
        return false;
    }
    
    private void removePetForPlayer(int playerId) {
        Set<Pet> toRemove = new HashSet<>();
        for (Pet pet : pets) {
            if (pet.getOwnerPlayerId() == playerId) {
                toRemove.add(pet);
            }
        }
        pets.removeAll(toRemove);
    }
    
    private void spawnPetForPlayer(int playerId, GameCharacter owner, Pet.PetKind kind) {
        int startX = owner.getPositionX() + owner.getWidth() / 2;
        int startY = owner.getPositionY() + owner.getHeight() / 2;
        
        int petWidth = 16;
        int petHeight = 16;
        Color petColor = Color.CYAN;
        
        long lifetimeMs = 7000L;
        
        int playerIndex = playerId - 1;
        Integer effectValue = null;
        
        if (kind == Pet.PetKind.GUN) {
            effectValue = state.getEffectValue(
                playerIndex,
                engine.gameplay.item.ItemEffect.ItemEffectType.PET_SUPPORT
            );
        } else if (kind == Pet.PetKind.ROCKET) {
            effectValue = state.getEffectValue(
                playerIndex,
                engine.gameplay.item.ItemEffect.ItemEffectType.PET_ROCKET_SUPPORT
            );
        }
        
        long shotIntervalMs;
        if (effectValue != null && effectValue > 0) {
            shotIntervalMs = 1000L / effectValue;
        } else {
            shotIntervalMs = 0L;
        }
        
        int dirX = 0;
        int dirY = -1;
        
        if (owner.isFacingLeft()) {
            dirX = -1;
            dirY = 0;
        } else if (owner.isFacingRight()) {
            dirX = 1;
            dirY = 0;
        } else if (owner.isFacingFront()) {
            dirX = 0;
            dirY = 1;
        } else if (owner.isFacingBack()) {
            dirX = 0;
            dirY = -1;
        }
        
        Pet pet = new Pet(
            startX,
            startY,
            petWidth,
            petHeight,
            petColor,
            playerId,
            kind,
            this.state,
            lifetimeMs,
            shotIntervalMs,
            dirX,
            dirY
        );
        
        pets.add(pet);
        
        Core.getLogger().info("[GameScreen] Spawned PET-GUN for player " + playerId
            + " at (" + startX + "," + startY + ")");
    }
    
    /**
     * Updates all active pets: follow their owner and fire bullets if needed.
     */
    private void updatePetsLogic() {
        Set<Pet> toRemove = new HashSet<>();
        
        for (Pet pet : pets) {
            if (pet.isExpired()) {
                toRemove.add(pet);
                continue;
            }
            
            int ownerId = pet.getOwnerPlayerId();
            int idx = ownerId - 1;
            GameCharacter owner =
                (idx >= 0 && idx < GameState.NUM_PLAYERS) ? characters[idx] : null;
            
            if (owner == null || owner.isInvincible()) {
                continue;
            }
            
            pet.update(this.weapons, owner);
        }
        
        pets.removeAll(toRemove);
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
    
    public Set<Weapon> getWeapons() {
        return this.weapons;
    }
}