package entity.buff;

import entity.character.GameCharacter;

public class RapidFireSkillBuff extends Buff {
    
    private final float attackSpeedMultiplier = 0.5f;    // 50% increase in attack speed
    private final float physicalDamageMultiplier = -0.2f; // 20% decrease in attack damage
    
    public RapidFireSkillBuff(float duration) {
        super(duration);
    }
    
    @Override
    public void apply(GameCharacter character) {
        character.modifyAttackSpeed(attackSpeedMultiplier);
        character.modifyPhysicalDamage(physicalDamageMultiplier);
    }
    
    @Override
    public void remove(GameCharacter character) {
        character.resetAttackSpeed(attackSpeedMultiplier);
        character.resetPhysicalDamage(physicalDamageMultiplier);
    }
}
