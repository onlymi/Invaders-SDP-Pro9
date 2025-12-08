package entity.buff;

import entity.character.CharacterStats;

public class BerserkerModeSkillBuff extends Buff {
    
    private final float critChanceMultiplier = 0.2f;
    private final float critDamageMultiplierMulti = 0.5f;
    
    public BerserkerModeSkillBuff(float duration) {
        super(duration);
    }
    
    @Override
    public void applyToStats(CharacterStats stats) {
        stats.critChance += critChanceMultiplier;
        stats.critDamageMultiplier
            = (int) (stats.physicalDamage * (1.0f + critDamageMultiplierMulti));
    }
}
