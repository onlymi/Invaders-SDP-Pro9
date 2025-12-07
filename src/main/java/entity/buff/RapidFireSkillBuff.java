package entity.buff;

import entity.character.CharacterStats;

public class RapidFireSkillBuff extends Buff {
    
    private final float attackSpeedMultiplier = 0.5f; // 50% increase in attack speed
    private final float physicalDamageMultiplier = -0.2f; // 20% decrease in attack damage
    
    public RapidFireSkillBuff(float duration) {
        super(duration);
    }
    
    @Override
    public void applyToStats(CharacterStats stats) {
        stats.attackSpeed *= (1.0f + attackSpeedMultiplier);
        stats.physicalDamage = (int) (stats.physicalDamage * (1.0f + physicalDamageMultiplier));
    }
}
