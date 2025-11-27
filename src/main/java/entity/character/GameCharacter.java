package entity.character;

import entity.Entity;
import entity.buff.Buff;
import entity.skill.Skill;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public abstract class GameCharacter extends Entity {
    
    protected ArrayList<Skill> skills;
    protected List<Buff> activeBuffs;
    
    protected int maxHealthPoints;
    protected int currentHealthPoints;
    protected int maxManaPoints;
    protected int currentManaPoints;
    protected float movementSpeed;
    
    protected int physicalDamage;
    protected int magicalDamage;
    protected float attackSpeed;
    protected float attackRange;
    
    protected float critChance;
    protected float critDamageMultiplier;
    
    protected int physicalDefense;
    
    protected boolean unlocked;
    
    public enum CharacterType {
        WARRIOR(150, 100, 1.0f, 25, 0, 0.8f, 1.5f, 0.05f, 1.5f, 30, true),
        // Quick long-range attack
        ARCHER(90, 100, 1.2f, 18, 0, 1.5f, 12.0f, 0.15f, 2.0f, 8, true),
        // Powerful long-range attack
        WIZARD(70, 200, 0.9f, 2, 30, 0.6f, 10.0f, 0.01f, 1.5f, 5, false),
        // Laser attack
        LASER(80, 250, 0.8f, 1, 28, 1.0f, 10.0f, 0.01f, 1.5f, 6, false),
        // Electric attack
        ELECTRIC(85, 180, 1.0f, 3, 25, 0.7f, 10.0f, 0.05f, 1.5f, 7, false),
        // Bomb-throwing attack
        BOMBER(110, 100, 1.1f, 5, 22, 0.5f, 8.0f, 0.05f, 1.5f, 12, false),
        // Bomb-throwing attack
        HEALER(100, 200, 1.0f, 5, 30, 0.8f, 8.0f, 0.01f, 1.5f, 10, false);
        
        private final int maxHealthPoints;
        private final int currentHealthPoints;
        private final int maxManaPoints;
        private final int currentManaPoints;
        private final float movementSpeed;
        
        private final int physicalDamage;
        private final int magicalDamage;
        private final float attackSpeed;
        private final float attackRange;
        
        private final float critChance;
        private final float critDamageMultiplier;
        
        private final int physicalDefense;
        
        private final boolean unlocked;
        
        CharacterType(int maxHealthPoints, int maxManaPoints, float movementSpeed,
            int physicalDamage, int magicalDamage, float attackSpeed, float attackRange,
            float critChance, float critDamageMultiplier, int physicalDefense, boolean unlocked) {
            // Initial basic stat
            this.maxHealthPoints = maxHealthPoints;
            this.currentHealthPoints = maxHealthPoints;
            this.maxManaPoints = maxManaPoints;
            this.currentManaPoints = maxManaPoints;
            this.movementSpeed = movementSpeed;
            // Initial attack stat
            this.physicalDamage = physicalDamage;
            this.magicalDamage = magicalDamage;
            this.attackSpeed = attackSpeed;
            this.attackRange = attackRange;
            // Initial critical stat
            this.critChance = critChance;
            this.critDamageMultiplier = critDamageMultiplier;
            // Initial defense stat
            this.physicalDefense = physicalDefense;
            // Initial character unlocked stat
            this.unlocked = unlocked;
        }
    }
    
    /**
     * Constructor, establishes the entity's generic properties.
     *
     * @param positionX     Initial position of the entity in the X axis.
     * @param positionY     Initial position of the entity in the Y axis.
     * @param characterType Type of character.
     */
    public GameCharacter(int positionX, int positionY, int width, int height,
        CharacterType characterType) {
        super(positionX, positionY, width, height);
        this.skills = new ArrayList<>();
        this.activeBuffs = new ArrayList<>();
        // Initial basic stat
        this.maxHealthPoints = characterType.maxHealthPoints;
        this.currentHealthPoints = characterType.currentHealthPoints;
        this.maxManaPoints = characterType.maxManaPoints;
        this.currentManaPoints = characterType.currentManaPoints;
        this.movementSpeed = characterType.movementSpeed;
        // Initial attack stat
        this.physicalDamage = characterType.physicalDamage;
        this.magicalDamage = characterType.magicalDamage;
        this.attackSpeed = characterType.attackSpeed;
        this.attackRange = characterType.attackRange;
        // Initial critical stat
        this.critChance = characterType.critChance;
        this.critDamageMultiplier = characterType.critDamageMultiplier;
        // Initial defense stat
        this.physicalDefense = characterType.physicalDefense;
        // Initial character unlocked stat
        this.unlocked = characterType.unlocked;
        // 업그레이드 스탯 적용
        applyUserUpgrades();
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
            buff.update(deltaTime, this);
            
            if (!buff.isActive()) {
                iterator.remove();
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
            this.maxHealthPoints = (int) (this.maxHealthPoints * (1 + 0.2 * stats.getStatLevel(0)));
            this.currentHealthPoints = this.maxHealthPoints; // Reset current HP to new max
        }
        
        // 1: Mana (20% per level)
        if (stats.getStatLevel(1) > 0) {
            this.maxManaPoints = (int) (this.maxManaPoints * (1 + 0.2 * stats.getStatLevel(1)));
            this.currentManaPoints = this.maxManaPoints; // Reset current MP to new max
        }
        
        // 2: Speed (10% per level)
        if (stats.getStatLevel(2) > 0) {
            this.movementSpeed = this.movementSpeed * (1 + 0.1f * stats.getStatLevel(2));
        }
        
        // 3: Damage (20% per level) - Applies to both Physical and Magical
        if (stats.getStatLevel(3) > 0) {
            double multiplier = 1 + 0.2 * stats.getStatLevel(3);
            this.physicalDamage = (int) (Math.ceil(this.physicalDamage * multiplier));
            this.magicalDamage = (int) (Math.ceil(this.magicalDamage * multiplier));
        }
        
        // 4: Attack Speed (10% per level)
        if (stats.getStatLevel(4) > 0) {
            this.attackSpeed = this.attackSpeed * (1 + 0.1f * stats.getStatLevel(4));
        }
        
        // 5: Attack Range (10% per level)
        if (stats.getStatLevel(5) > 0) {
            this.attackRange = this.attackRange * (1 + 0.1f * stats.getStatLevel(5));
        }
        
        // 6: Critical Chance (Add 5% per level)
        if (stats.getStatLevel(6) > 0) {
            this.critChance += 0.05f * stats.getStatLevel(6);
        }
        
        // 7: Defence (Add 2 per level)
        if (stats.getStatLevel(7) > 0) {
            this.physicalDefense += 2 * stats.getStatLevel(7);
        }
    }
    
    /**
     * Add buff to this character.
     *
     * @param buff Buff to apply to characters
     */
    public void addBuff(Buff buff) {
        buff.apply(this);
        this.activeBuffs.add(buff);
    }
    
    public void decreaseMana(int manaCost) {
        this.currentManaPoints = Math.max(0, this.currentManaPoints - manaCost);
    }
    
    public void modifyAttackSpeed(float multiplier) {
        this.attackSpeed *= (1 + multiplier);
    }
    
    public void resetAttackSpeed(float multiplier) {
        this.attackSpeed /= (1 + multiplier);
    }
    
    public void modifyPhysicalDamage(float multiplier) {
        this.physicalDamage = (int) (this.physicalDamage * (1 + multiplier));
    }
    
    public void resetPhysicalDamage(float multiplier) {
        this.physicalDamage = (int) (this.physicalDamage / (1 + multiplier));
    }
    
    public ArrayList<Skill> getSkills() {
        return this.skills;
    }
    
    public int getMaxHealthPoints() {
        return this.maxHealthPoints;
    }
    
    public int getCurrentHealthPoints() {
        return this.currentHealthPoints;
    }
    
    public int getMaxManaPoints() {
        return this.maxManaPoints;
    }
    
    public int getCurrentManaPoints() {
        return this.currentManaPoints;
    }
    
    public float getMovementSpeed() {
        return this.movementSpeed;
    }
    
    public int getPhysicalDamage() {
        return this.physicalDamage;
    }
    
    public int getMagicalDamage() {
        return this.magicalDamage;
    }
    
    public float getAttackSpeed() {
        return this.attackSpeed;
    }
    
    public float getAttackRange() {
        return this.attackRange;
    }
    
    public float getCritChance() {
        return this.critChance;
    }
    
    public float getCritDamageMultiplier() {
        return this.critDamageMultiplier;
    }
    
    public int getPhysicalDefense() {
        return this.physicalDefense;
    }
    
    public boolean isUnlocked() {
        return this.unlocked;
    }
    
}
