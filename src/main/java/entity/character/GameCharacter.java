package entity.character;

import engine.AssetManager.SpriteType;
import engine.Core;
import engine.GameState;
import engine.InputManager;
import engine.UserStats;
import engine.gameplay.item.ItemEffect.ItemEffectType;
import engine.utils.Cooldown;
import entity.Entity;
import entity.Weapon;
import entity.WeaponPool;
import entity.buff.Buff;
import entity.skill.Skill;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import screen.GameScreen;
import screen.Screen;

public abstract class GameCharacter extends Entity {
    
    protected Team team;
    protected CharacterType characterType;
    protected int playerId;
    // Character status
    protected ArrayList<Skill> skills;
    protected List<Buff> activeBuffs;
    protected boolean unlocked;
    
    protected GameState gameState;
    private boolean moveSpeedUpActivePrev = false;
    private boolean dashActivePrev = false;
    private int lastMoveDirX = 0;
    private int lastMoveDirY = 0;
    
    // Default stats value that only change at level-up
    protected final CharacterStats baseStats;
    // Current stats value that can change always
    protected CharacterStats currentStats;
    
    protected int currentHealthPoints;
    protected int currentManaPoints;
    private float manaRegenAccumulator = 0f;
    
    private boolean isDie;
    public boolean isInSelectScreen;
    public boolean isFiring;
    
    private int leftKey;
    private int rightKey;
    private int upKey;
    private int downKey;
    private int defaultAttackKey;
    private int firstSkillKey;
    private int secondSkillKey;
    private int ultimateSkillKey;
    
    private static final float DIAGONAL_CORRECTION_FACTOR = (float) (1.0 / Math.sqrt(2));
    protected boolean isAttacking;
    protected boolean isMoving;
    protected boolean isFacingLeft;
    protected boolean isFacingRight;
    protected boolean isFacingFront;
    protected boolean isFacingBack;
    
    private static final int DESTRUCTION_COOLDOWN = 1000;
    private static final int BASE_SHOOTING_COOLDOWN = 1500;
    private Cooldown shootingCooldown;
    private final Cooldown destructionCooldown;
    
    protected SpriteType projectileSpriteType; // Image type of projectile
    protected int projectileWidth;
    protected int projectileHeight;
    protected int projectileSpeed;
    
    protected static final int MOVEMENT_SPEED_FACTOR = 150;
    protected static final int ATTACK_SPEED_FACTOR = 10;
    protected static final int SHOOTING_COOLDOWN_FACTOR = 500;
    
    /**
     * Constructor, establishes the entity's generic properties.
     *
     * @param positionX Initial position of the entity in the X axis
     * @param positionY Initial position of the entity in the Y axis
     * @param width     Width of character
     * @param height    Height of character
     * @param team      Team of character
     * @param type      Type of character
     * @param playerId  ID of Player
     */
    public GameCharacter(CharacterType type, int positionX, int positionY, int width, int height,
        Team team, int playerId) {
        super(positionX, positionY, width, height);
        this.team = team;
        this.characterType = type;
        this.playerId = playerId;
        // Character skills
        this.skills = new ArrayList<>();
        this.activeBuffs = new ArrayList<>();
        // Initial base stats
        this.baseStats = new CharacterStats(type.getBaseStats());
        this.currentStats = new CharacterStats(this.baseStats);
        // Initial current stats
        this.currentHealthPoints = baseStats.maxHealthPoints;
        this.currentManaPoints = baseStats.maxManaPoints;
        // Initial character unlocked stat
        this.unlocked = type.isUnlocked();
        
        this.isDie = false;
        this.isInSelectScreen = false;
        this.isFiring = false;
        // 업그레이드 스탯 적용
        applyUserUpgrades();
        recalculateStats();
        
        this.leftKey = KeyEvent.VK_A;
        this.rightKey = KeyEvent.VK_D;
        this.upKey = KeyEvent.VK_W;
        this.downKey = KeyEvent.VK_S;
        this.defaultAttackKey = KeyEvent.VK_SPACE;
        this.firstSkillKey = KeyEvent.VK_1;
        this.secondSkillKey = KeyEvent.VK_2;
        this.ultimateSkillKey = KeyEvent.VK_3;
        
        initializeKeyboardPressing();
        
        // Reset cool time
        this.shootingCooldown = Core.getCooldown((int) (BASE_SHOOTING_COOLDOWN
            - this.currentStats.attackSpeed * SHOOTING_COOLDOWN_FACTOR));
        this.shootingCooldown.reset();
        this.destructionCooldown = Core.getCooldown(DESTRUCTION_COOLDOWN);
        
        this.projectileSpriteType = SpriteType.PlayerBullet;
        this.projectileWidth = 3;
        this.projectileHeight = 5;
        this.projectileSpeed = 1;
    }
    
    /**
     * Check the time for each frame.
     *
     * @param deltaTime The time it took from the last frame to the present
     */
    public void update(float deltaTime) {
        float manaRegenRate = 2f;
        this.manaRegenAccumulator += manaRegenRate * deltaTime;
        
        if (this.manaRegenAccumulator >= 1.0f) {
            int manaToAdd = (int) this.manaRegenAccumulator;
            
            // 현재 마나가 최대 마나보다 적을 때만 회복
            if (this.currentManaPoints < this.baseStats.maxManaPoints) {
                this.currentManaPoints = Math.min(
                    this.currentManaPoints + manaToAdd,
                    this.baseStats.maxManaPoints
                );
            }
            
            // 반영한 정수만큼 누적값에서 뺌 (남은 소수점은 다음 프레임으로 이월)
            this.manaRegenAccumulator -= manaToAdd;
        }
        
        Iterator<Buff> iterator = activeBuffs.iterator();
        
        while (iterator.hasNext()) {
            Buff buff = iterator.next();
            buff.update(deltaTime);
            
            if (!buff.isActive()) {
                iterator.remove();
                recalculateStats();
            }
        }
        
        this.isDie = this.currentHealthPoints <= 0;
        
        if (this.gameState != null) {
            int index = this.playerId - 1;
            if (index >= 0 && index < GameState.NUM_PLAYERS) {
                boolean dashNow = this.gameState.hasEffect(index, ItemEffectType.DASH);
                
                if (dashNow && !this.dashActivePrev) {
                    int dirX = this.lastMoveDirX;
                    int dirY = this.lastMoveDirY;
                    
                    if (dirX == 0 && dirY == 0) {
                        dirX = 1;
                    }
                    
                    int dashMultiplier = 1;
                    Integer dashVal = this.gameState.getEffectValue(index, ItemEffectType.DASH);
                    if (dashVal != null && dashVal > 0) {
                        dashMultiplier = dashVal;
                    }
                    
                    int baseStep = (int) Math.round(this.currentStats.movementSpeed);
                    int dashDistance = baseStep * dashMultiplier * 48;
                    
                    double len = Math.sqrt(dirX * dirX + dirY * dirY);
                    int shiftX;
                    int shiftY;
                    if (len == 0.0) {
                        shiftX = dashDistance;
                        shiftY = 0;
                    } else {
                        shiftX = (int) Math.round(dashDistance * dirX / len);
                        shiftY = (int) Math.round(dashDistance * dirY / len);
                    }
                    
                    this.positionX += shiftX;
                    this.positionY += shiftY;
                    
                    Core.getLogger().info("[GameCharacter] DASH burst applied: dirX=" + dirX
                        + ", dirY=" + dirY
                        + ", step=" + baseStep
                        + ", mul=" + dashMultiplier
                        + ", dist=" + dashDistance
                        + ", shiftX=" + shiftX
                        + ", shiftY=" + shiftY);
                }
                
                this.dashActivePrev = dashNow;
            }
        }
    }
    
    /**
     * Applies stat upgrades from the user's store purchases. Upgrades are applied as percentage or
     * additive bonuses to the base stats.
     */
    protected void applyUserUpgrades() {
        UserStats stats = Core.getUserStats();
        if (stats == null) {
            return;
        }
        // 0: Health (20% per level)
        if (stats.getStatLevel(0) > 0) {
            this.baseStats.maxHealthPoints = (int) (
                this.baseStats.maxHealthPoints * (1 + 0.2 * stats.getStatLevel(0)));
            this.currentHealthPoints = this.baseStats.maxHealthPoints; // Reset current HP to new max
        }
        
        // 1: Mana (20% per level)
        if (stats.getStatLevel(1) > 0) {
            this.baseStats.maxManaPoints = (int) (
                this.baseStats.maxManaPoints * (1 + 0.2 * stats.getStatLevel(1)));
            this.currentManaPoints = this.baseStats.maxManaPoints; // Reset current MP to new max
        }
        
        // 2: Speed (10% per level)
        if (stats.getStatLevel(2) > 0) {
            this.baseStats.movementSpeed =
                this.baseStats.movementSpeed * (1 + 0.1f * stats.getStatLevel(2));
        }
        
        // 3: Damage (20% per level) - Applies to both Physical and Magical
        if (stats.getStatLevel(3) > 0) {
            double multiplier = 1 + 0.2 * stats.getStatLevel(3);
            this.baseStats.physicalDamage = (int) (Math.ceil(
                this.baseStats.physicalDamage * multiplier));
            this.baseStats.magicalDamage = (int) (Math.ceil(
                this.baseStats.magicalDamage * multiplier));
        }
        
        // 4: Attack Speed (10% per level)
        if (stats.getStatLevel(4) > 0) {
            this.baseStats.attackSpeed =
                this.baseStats.attackSpeed * (1 + 0.1f * stats.getStatLevel(4));
        }
        
        // 5: Attack Range (10% per level)
        if (stats.getStatLevel(5) > 0) {
            this.baseStats.attackRange =
                this.baseStats.attackRange * (1 + 0.1f * stats.getStatLevel(5));
        }
        
        // 6: Critical Chance (Add 5% per level)
        if (stats.getStatLevel(6) > 0) {
            this.baseStats.critChance += 0.05f * stats.getStatLevel(6);
        }
        
        // 7: Defence (Add 2 per level)
        if (stats.getStatLevel(7) > 0) {
            this.baseStats.physicalDefense += 2 * stats.getStatLevel(7);
        }
        
        this.currentStats = new CharacterStats(this.baseStats);
    }
    
    /**
     * Add buff to this character.
     *
     * @param buff Buff to apply to characters
     */
    public void addBuff(Buff buff) {
        buff.applyToStats(currentStats);
        this.activeBuffs.add(buff);
        recalculateStats();
    }
    
    public void decreaseMana(int manaCost) {
        this.currentManaPoints = Math.max(0, this.currentManaPoints - manaCost);
    }
    
    /**
     * Reapply all buffs that are currently active, starting with the base stat.
     */
    public void recalculateStats() {
        this.currentStats.attackSpeed = this.baseStats.attackSpeed;
        this.currentStats.physicalDamage = this.baseStats.physicalDamage;
        this.currentStats.movementSpeed = this.baseStats.movementSpeed;
        
        for (Buff buff : activeBuffs) {
            buff.applyToStats(this.currentStats);
        }
        
        int newCooldownTime = (int) (BASE_SHOOTING_COOLDOWN
            - this.currentStats.attackSpeed * SHOOTING_COOLDOWN_FACTOR);
        this.shootingCooldown = Core.getCooldown(newCooldownTime);
        this.projectileSpeed = (int) (this.currentStats.attackSpeed * ATTACK_SPEED_FACTOR);
    }
    
    /**
     * Sets the control keys for this character.
     *
     * @param keys Key code for player
     */
    public void setControlKeys(int[] keys) {
        this.leftKey = keys[0];
        this.rightKey = keys[1];
        this.upKey = keys[2];
        this.downKey = keys[3];
        this.defaultAttackKey = keys[4];
        this.firstSkillKey = keys[6];
        this.secondSkillKey = keys[7];
        this.ultimateSkillKey = keys[8];
    }
    
    public void setGameState(GameState gameState) {
        this.gameState = gameState;
    }
    
    public void handleKeyboard(InputManager inputManager, Screen screen, Set<Weapon> weapons,
        float deltaTime) {
        initializeKeyboardPressing();
        
        handleMovement(inputManager, screen, deltaTime);
        handleAttack(inputManager, weapons);
    }
    
    public void handleMovement(InputManager inputManager, Screen screen, float deltaTime) {
        float dx = 0;
        float dy = 0;
        
        if (inputManager.isKeyDown(this.leftKey)) {
            dx -= 1;
            this.isFacingLeft = true;
            this.lastMoveDirX = -1;
        }
        if (inputManager.isKeyDown(this.rightKey)) {
            dx += 1;
            this.isFacingRight = true;
            this.lastMoveDirX = 1;
        }
        if (inputManager.isKeyDown(this.upKey)) {
            dy -= 1;
            this.isFacingBack = true;
            this.lastMoveDirY = -1;
        }
        if (inputManager.isKeyDown(this.downKey)) {
            dy += 1;
            this.isFacingFront = true;
            this.lastMoveDirY = 1;
        }
        
        if (dx == 0) {
            this.lastMoveDirX = 0;
        }
        if (dy == 0) {
            this.lastMoveDirY = 0;
        }
        
        if (dx != 0 || dy != 0) {
            this.isMoving = true;
            
            // 대각선 이동 시 속도 보정
            if (dx != 0 && dy != 0) {
                dx *= DIAGONAL_CORRECTION_FACTOR;
                dy *= DIAGONAL_CORRECTION_FACTOR;
            }
            
            float baseSpeed = this.currentStats.movementSpeed;
            if (this.gameState != null) {
                int index = this.playerId - 1;
                if (index >= 0 && index < GameState.NUM_PLAYERS) {
                    Integer percent = this.gameState.getEffectValue(index,
                        ItemEffectType.MOVE_SPEED_UP);
                    if (percent != null) {
                        baseSpeed = (float) Math.round(baseSpeed * (1.0 + percent / 100.0));
                    }
                }
            }
            
            float speed = baseSpeed * MOVEMENT_SPEED_FACTOR * deltaTime;
            int movementX = Math.round(dx * speed);
            int movementY = Math.round(dy * speed);
            
            if (movementX == 0 && dx != 0) {
                movementX = (dx > 0) ? 1 : -1;
            }
            if (movementY == 0 && dy != 0) {
                movementY = (dy > 0) ? 1 : -1;
            }
            
            int nextX = this.positionX + movementX;
            boolean isValidX = (nextX >= 1)
                && ((nextX + this.width) <= screen.getWidth() - 1);
            if (isValidX) {
                this.positionX += movementX;
            }
            
            int nextY = this.positionY + movementY;
            boolean isValidY = (nextY >= GameScreen.SEPARATION_LINE_HEIGHT + 1)
                && ((nextY + this.height) <= screen.getHeight() - 1);
            if (isValidY) {
                this.positionY += movementY;
            }
        }
    }
    
    public void handleAttack(InputManager inputManager, Set<Weapon> weapons) {
        this.isFiring = false;
        this.isAttacking = false;
        if (inputManager.isKeyDown(this.defaultAttackKey)) {
            this.isFiring = launchBasicAttack(weapons);
            this.isAttacking = true;
        }
        if (inputManager.isKeyDown(this.firstSkillKey)) {
            if (!skills.isEmpty()) {
                skills.get(0).activate(this);
            }
            this.isAttacking = true;
        }
        if (inputManager.isKeyDown(this.secondSkillKey)) {
            if (skills.size() > 1) {
                skills.get(1).activate(this);
            }
            this.isAttacking = true;
        }
        if (inputManager.isKeyDown(this.ultimateSkillKey)) {
            if (skills.size() > 2) {
                skills.get(2).activate(this);
            }
            this.isAttacking = true;
        }
    }
    
    /**
     * Launch a basic attack.
     *
     * @param weapons The set of weapons to add the new weapon to.
     * @return true if the attack was actually launched (cooldown finished), false otherwise.
     */
    public boolean launchBasicAttack(Set<Weapon> weapons) {
        if (this.shootingCooldown.checkFinished()) {
            this.shootingCooldown.reset();
            
            int launchX;
            int launchY;
            
            if (isFacingLeft) {
                launchX = this.positionX - (this.projectileWidth / 2);
                launchY = this.positionY + (this.height / 2) - (this.projectileHeight / 2);
            } else if (isFacingRight) {
                launchX = this.positionX + this.width + (this.projectileWidth / 2);
                launchY = this.positionY + (this.height / 2) - (this.projectileHeight / 2);
            } else if (isFacingFront) {
                launchX = this.positionX + (this.width / 2);
                launchY = this.positionY + this.height;
            } else if (isFacingBack) {
                launchX = this.positionX + (this.width / 2);
                launchY = this.positionY - this.projectileHeight;
            } else {
                launchX = this.positionX + (this.width / 2);
                launchY = this.positionY - this.projectileHeight;
            }
            
            Weapon weapon = WeaponPool.getWeapon(launchX, launchY, this.projectileSpeed,
                this.projectileWidth, this.projectileHeight, this.team);
            
            weapon.reset();
            weapon.setCharacter(this);
            weapon.setSpriteImage(this.projectileSpriteType);
            weapon.setPlayerId(this.playerId);
            weapon.setRange(this.currentStats.attackRange);
            weapon.setDamage(this.currentStats.physicalDamage);
            weapons.add(weapon);
            return true;
        }
        return false;
    }
    
    /**
     * Switches the ship to its destroyed state.
     */
    public final void takeDamage(int damage) {
        currentHealthPoints -= damage;
        this.destructionCooldown.reset();
    }
    
    /**
     * Checks if the ship is destroyed.
     *
     * @return True if the character is currently attacked.
     */
    public final boolean isInvincible() {
        return !this.destructionCooldown.checkFinished();
    }
    
    public void initializeKeyboardPressing() {
        this.isAttacking = false;
        this.isMoving = false;
        this.isFacingLeft = false;
        this.isFacingRight = false;
        this.isFacingFront = false;
        this.isFacingBack = false;
    }
    
    public void setProjectile(int projectileWidth, int projectileHeight, int projectileSpeed) {
        this.projectileWidth = projectileWidth;
        this.projectileHeight = projectileHeight;
        this.projectileSpeed = projectileSpeed;
    }
    
    public void setCurrentHealthPoints(int playerHealth) {
        this.currentHealthPoints = playerHealth;
    }
    
    public CharacterStats getBaseStats() {
        return this.baseStats;
    }
    
    public CharacterStats getCurrentStats() {
        return this.currentStats;
    }
    
    public ArrayList<Skill> getSkills() {
        return this.skills;
    }
    
    public int getCurrentHealthPoints() {
        return this.currentHealthPoints;
    }
    
    public int getCurrentManaPoints() {
        return this.currentManaPoints;
    }
    
    public int getPlayerId() {
        return this.playerId;
    }
    
    public boolean isInSelectScreen() {
        return this.isInSelectScreen;
    }
    
    public boolean isDie() {
        return isDie;
    }
    
    public boolean isFiring() {
        return this.isFiring;
    }
    
    public boolean isAttacking() {
        return this.isAttacking;
    }
    
    public boolean isMoving() {
        return this.isMoving;
    }
    
    public boolean isFacingLeft() {
        return isFacingLeft;
    }
    
    public boolean isFacingRight() {
        return this.isFacingRight;
    }
    
    public boolean isFacingFront() {
        return isFacingFront;
    }
    
    public boolean isFacingBack() {
        return isFacingBack;
    }
    
    public SpriteType getProjectileSpriteType() {
        return this.projectileSpriteType;
    }
    
    public int getProjectileWidth() {
        return this.projectileWidth;
    }
    
    public int getProjectileHeight() {
        return this.projectileHeight;
    }
    
    public int getProjectileSpeed() {
        return this.projectileSpeed;
    }
    
    /**
     * 특정 타입의 버프를 가지고 있는지 확인합니다.
     *
     * @param buffClass 확인할 버프 클래스
     * @return 해당 버프가 활성화되어 있으면 true
     */
    public boolean hasBuff(Class<? extends Buff> buffClass) {
        for (Buff buff : activeBuffs) {
            if (buffClass.isInstance(buff)) {
                return true;
            }
        }
        return false;
    }
}
