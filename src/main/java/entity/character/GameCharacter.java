package entity.character;

import entity.Entity;
import entity.buff.Buff;
import entity.skill.Skill;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

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
            // 예: tempAttackSpeed *= (1 + buff.getSpeedMultiplier());
            // 예: tempPhysDamage += buff.getBonusDamage();
        }
        
        this.currentStats.attackSpeed = tempAttackSpeed;
        this.currentStats.physicalDamage = tempPhysDamage;
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
}
