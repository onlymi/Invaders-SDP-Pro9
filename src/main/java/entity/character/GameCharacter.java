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
    
    /**
     * Returns the selected character type to Core.
     *
     * @return The selected CharacterType enum.
     */
    public CharacterType getSelectedCharacterType(int index) {
        return switch (index) {
            // case 1 -> CharacterType.WIZARD;
            case 2 -> CharacterType.ARCHER;
            // case 3 -> CharacterType.WIZARD;
            // case 4 -> CharacterType.LASER;
            // case 5 -> CharacterType.ELECTRIC;
            // case 6 -> CharacterType.BOMBER;
            // case 7 -> CharacterType.HEALER;
            default -> CharacterType.ARCHER;
        };
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
