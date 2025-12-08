package entity.buff;

import entity.character.CharacterStats;

public class IronSkinSkillBuff extends Buff {
    
    private final float physicalDefenseMultiplier = 1.0f;
    
    public IronSkinSkillBuff(float duration) {
        super(duration);
    }
    
    @Override
    public void applyToStats(CharacterStats stats) {
        stats.physicalDefense = (int) (stats.physicalDefense * (1.0f + physicalDefenseMultiplier));
    }
    
}
