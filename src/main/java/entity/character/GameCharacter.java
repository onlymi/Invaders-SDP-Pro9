package entity.character;

import engine.AssetManager.SpriteType;
import engine.Core;
import engine.InputManager;
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
import screen.Screen;

public abstract class GameCharacter extends Entity {
    
    protected Team team;
    protected CharacterType characterType;
    protected int playerId;
    // Character status
    protected ArrayList<Skill> skills;
    protected List<Buff> activeBuffs;
    protected boolean unlocked;
    
    // Default stats value that only change at level-up
    protected final CharacterStats baseStats;
    // Current stats value that can change always
    protected CharacterStats currentStats;
    
    protected int currentHealthPoints;
    protected int currentManaPoints;
    
    private int leftKey;
    private int rightKey;
    private int upKey;
    private int downKey;
    private int defaultAttackKey;
    
    private static final float DIAGONAL_CORRECTION_FACTOR = (float) (1.0 / Math.sqrt(2));
    protected boolean isMoving;
    protected boolean isFacingLeft;
    protected boolean isFacingRight;
    protected boolean isFacingFront;
    protected boolean isFacingBack;
    
    private static final int DESTRUCTION_COOLDOWN = 1000;
    private Cooldown shootingCooldown;
    private final Cooldown destructionCooldown;
    
    protected SpriteType projectileSpriteType; // Image type of projectile
    protected int projectileWidth;
    protected int projectileHeight;
    protected int projectileSpeed;
    
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
        // 업그레이드 스탯 적용
        applyUserUpgrades();
        recalculateStats();
        
        this.leftKey = KeyEvent.VK_A;
        this.rightKey = KeyEvent.VK_D;
        this.upKey = KeyEvent.VK_W;
        this.downKey = KeyEvent.VK_S;
        this.defaultAttackKey = KeyEvent.VK_SPACE;
        
        this.isMoving = false;
        this.isFacingLeft = false;
        this.isFacingRight = true;
        this.isFacingFront = false;
        this.isFacingBack = false;
        
        // Reset cool time
        this.shootingCooldown = Core.getCooldown((int) this.currentStats.attackSpeed * 500);
        this.shootingCooldown.reset();
        this.destructionCooldown = Core.getCooldown(DESTRUCTION_COOLDOWN);
        
        this.projectileSpriteType = SpriteType.PlayerBullet;
        this.projectileWidth = 3;
        this.projectileHeight = 5;
        this.projectileSpeed = -6;
    }
    
    /**
     * Check the time for each frame.
     *
     * @param deltaTime The time it took from the last frame to the present
     */
    public void update(float deltaTime) {
        Iterator<Buff> iterator = activeBuffs.iterator();
        
        while (iterator.hasNext()) {
            Buff buff = iterator.next();
            buff.update(deltaTime);
            
            if (!buff.isActive()) {
                iterator.remove();
                recalculateStats();
            }
        }
    }
    
    /**
     * Applies stat upgrades from the user's store purchases. Upgrades are applied as percentage or
     * additive bonuses to the base stats.
     */
    protected void applyUserUpgrades() { // Test에서 오버라이딩 및 호출이 가능하도록 protected로 변경.
        engine.UserStats stats = engine.Core.getUserStats();
        if (stats == null) {
            return;
        }
        // 0: Health (20% per level)
        if (stats.getStatLevel(0) > 0) {
            this.baseStats.maxHealthPoints = (int) (this.baseStats.maxHealthPoints * (1
                + 0.2 * stats.getStatLevel(0)));
            this.currentHealthPoints = this.baseStats.maxHealthPoints; // Reset current HP to new max
        }
        
        // 1: Mana (20% per level)
        if (stats.getStatLevel(1) > 0) {
            this.baseStats.maxManaPoints = (int) (this.baseStats.maxManaPoints * (1
                + 0.2 * stats.getStatLevel(1)));
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
    }
    
    public void decreaseMana(int manaCost) {
        this.currentManaPoints = Math.max(0, this.currentManaPoints - manaCost);
    }
    
    /**
     * Reapply all buffs that are currently active, starting with the base stat.
     */
    public void recalculateStats() {
        float tempAttackSpeed = baseStats.attackSpeed;
        int tempPhysDamage = baseStats.physicalDamage;
        
        for (Buff buff : activeBuffs) {
            buff.applyToStats(this.currentStats);
        }
        
        this.currentStats.attackSpeed = tempAttackSpeed;
        this.currentStats.physicalDamage = tempPhysDamage;
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
    }
    
    public boolean handleMovement(InputManager inputManager, Screen screen, Set<Weapon> weapons,
        float deltaTime) {
        this.isMoving = false;
        
        this.isFacingLeft = false;
        this.isFacingRight = false;
        this.isFacingBack = false;
        this.isFacingFront = false;
        
        float dx = 0;
        float dy = 0;
        
        if (inputManager.isKeyDown(this.leftKey)) {
            dx -= 1;
            this.isFacingLeft = true;
        }
        if (inputManager.isKeyDown(this.rightKey)) {
            dx += 1;
            this.isFacingRight = true;
        }
        if (inputManager.isKeyDown(this.upKey)) {
            dy -= 1;
            this.isFacingBack = true;
        }
        if (inputManager.isKeyDown(this.downKey)) {
            dy += 1;
            this.isFacingFront = true;
        }
        
        if (dx != 0 || dy != 0) {
            this.isMoving = true;
            
            // 대각선 이동 시 벡터 정규화 (속도 보정)
            if (dx != 0 && dy != 0) {
                dx *= DIAGONAL_CORRECTION_FACTOR;
                dy *= DIAGONAL_CORRECTION_FACTOR;
            }
            
            // 프레임 단위 이동 거리 계산
            float speed = this.currentStats.movementSpeed * 150 * deltaTime;
            int movementX = Math.round(dx * speed);
            int movementY = Math.round(dy * speed);
            
            // 속도가 낮아도 입력이 있으면 최소 1픽셀 이동 보장
            if (movementX == 0 && dx != 0) {
                movementX = (dx > 0) ? 1 : -1;
            }
            if (movementY == 0 && dy != 0) {
                movementY = (dy > 0) ? 1 : -1;
            }
            
            // X축 이동 및 경계 체크
            int nextX = this.positionX + movementX;
            boolean isValidX = (nextX >= 1) && ((nextX + this.width) <= screen.getWidth() - 1);
            if (isValidX) {
                this.positionX += movementX;
            }
            
            // Y축 이동 및 경계 체크
            int nextY = this.positionY + movementY;
            boolean isValidY = (nextY >= 1) && ((nextY + this.height) <= screen.getHeight() - 1);
            if (isValidY) {
                this.positionY += movementY;
            }
        }
        
        if (inputManager.isKeyDown(this.defaultAttackKey)) {
            return shoot(weapons);
        }
        return false;
    }
    
    /**
     * Shoots a weapon.
     *
     * @param weapons The set of weapons to add the new weapon to.
     * @return True if a weapon was fired, false if on cooldown.
     */
    public boolean shoot(Set<Weapon> weapons) {
        if (this.shootingCooldown.checkFinished()) {
            this.shootingCooldown.reset();
            
            int launchX = this.positionX + this.width / 2 - (this.projectileWidth / 2);
            int launchY = this.positionY;
            
            Weapon weapon = WeaponPool.getWeapon(launchX, launchY,
                this.projectileWidth, this.projectileHeight, this.projectileSpeed, this.team);
            
            weapon.setSpriteImage(this.projectileSpriteType);
            weapon.setOwnerPlayerId(this.playerId);
            weapons.add(weapon);
            return true;
        }
        return false;
    }
    
    /**
     * Switches the ship to its destroyed state.
     */
    public final void destroy() {
        this.destructionCooldown.reset();
    }
    
    /**
     * Checks if the ship is destroyed.
     *
     * @return True if the ship is currently destroyed.
     */
    public final boolean isDestroyed() {
        return !this.destructionCooldown.checkFinished();
    }
    
    public CharacterStats getBaseStats() {
        return this.baseStats;
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
}
