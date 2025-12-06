package entity.buff;

import entity.character.CharacterStats;

public class EvasionShotSkillBuff extends Buff {
    
    public EvasionShotSkillBuff(float duration) {
        super(duration);
    }
    
    @Override
    public void applyToStats(CharacterStats characterStats) {
        // 이동 속도를 50% 감소시킴
        characterStats.movementSpeed *= 0.5f;
    }
}
