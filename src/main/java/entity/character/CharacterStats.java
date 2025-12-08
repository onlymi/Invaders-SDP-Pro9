package entity.character;

public class CharacterStats {
    
    public int maxHealthPoints;
    public int maxManaPoints;
    public float movementSpeed;
    
    public int physicalDamage;
    public int magicalDamage;
    public float attackSpeed;
    public float attackRange;
    
    public float critChance;
    public float critDamageMultiplier;
    
    public int physicalDefense;
    
    public CharacterStats(int maxHealthPoints, int maxManaPoints, float movementSpeed,
        int physicalDamage, int magicalDamage, float attackSpeed, float attackRange,
        float critChance, float critDamageMultiplier, int physicalDefense) {
        // Initial basic stat
        this.maxHealthPoints = maxHealthPoints;
        this.maxManaPoints = maxManaPoints;
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
    }
    
    public CharacterStats(CharacterStats other) {
        this.maxHealthPoints = other.maxHealthPoints;
        this.maxManaPoints = other.maxManaPoints;
        this.movementSpeed = other.movementSpeed;
        this.physicalDamage = other.physicalDamage;
        this.magicalDamage = other.magicalDamage;
        this.attackSpeed = other.attackSpeed;
        this.attackRange = other.attackRange;
        this.critChance = other.critChance;
        this.critDamageMultiplier = other.critDamageMultiplier;
        this.physicalDefense = other.physicalDefense;
    }
    
    public CharacterStats() {
    }
}
