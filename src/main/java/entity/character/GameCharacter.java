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
        
        recalculateStats();
        
        this.leftKey = KeyEvent.VK_A;
        this.rightKey = KeyEvent.VK_D;
        this.upKey = KeyEvent.VK_W;
        this.downKey = KeyEvent.VK_S;
        this.defaultAttackKey = KeyEvent.VK_SPACE;
        
        // Reset cool time
        this.shootingCooldown = Core.getCooldown((int) this.currentStats.attackSpeed);
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
        int movementAmount = (int) (this.currentStats.movementSpeed * 150 * deltaTime);
        if (movementAmount == 0 && this.currentStats.movementSpeed > 0) {
            movementAmount = 1;
        }
        
        boolean isLeftBorder = (this.positionX - movementAmount) < 1;
        if (inputManager.isKeyDown(this.leftKey) && !isLeftBorder) {
            this.positionX -= movementAmount;
        }
        
        boolean isRightBorder =
            (this.positionX + this.width + movementAmount) > (screen.getWidth() - 1);
        if (inputManager.isKeyDown(this.rightKey) && !isRightBorder) {
            this.positionX += movementAmount;
        }
        
        boolean isUpBorder = (this.positionY - movementAmount) < 1;
        if (inputManager.isKeyDown(this.upKey) && !isUpBorder) {
            this.positionY -= movementAmount;
        }
        
        boolean isDownBorder =
            (this.positionY + this.height + movementAmount) > (screen.getHeight() - 1);
        if (inputManager.isKeyDown(this.downKey) && !isDownBorder) {
            this.positionY += movementAmount;
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
}
