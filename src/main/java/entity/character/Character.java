package entity.character;

import entity.Entity;
import entity.skill.Skill;
import java.util.ArrayList;

public abstract class Character extends Entity {
    
    protected ArrayList<Skill> skills;
    
    protected int maxHealthPoints;
    protected int healthPoints;
    protected int maxManaPoints;
    protected int manaPoints;
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
        private final int healthPoints;
        private final int maxManaPoints;
        private final int manaPoints;
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
            this.healthPoints = maxHealthPoints;
            this.maxManaPoints = maxManaPoints;
            this.manaPoints = maxManaPoints;
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
    public Character(int positionX, int positionY, int width, int height,
        CharacterType characterType) {
        super(positionX, positionY, width, height);
        // Initial basic stat
        this.maxHealthPoints = characterType.maxHealthPoints;
        this.healthPoints = characterType.healthPoints;
        this.maxManaPoints = characterType.maxManaPoints;
        this.manaPoints = characterType.manaPoints;
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
    }
    
    public ArrayList<Skill> getSkills() {
        return this.skills;
    }
    
    public int getMaxHealthPoints() {
        return this.maxHealthPoints;
    }
    
    public int getHealthPoints() {
        return this.healthPoints;
    }
    
    public int getMaxManaPoints() {
        return this.maxManaPoints;
    }
    
    public int getManaPoints() {
        return this.manaPoints;
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
