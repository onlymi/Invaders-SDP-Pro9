package entity.skill;

import engine.Core;
import entity.buff.RapidFireSkillBuff;
import entity.character.GameCharacter;
import java.util.logging.Logger;

public class RapidFireSkill extends Skill {
    
    private static final Logger LOGGER = Core.getLogger();
    
    private static final int MANA_COST = 30;
    private static final float COOLDOWN_SECOND = 12.0f;
    private static final float DURATION_SECOND = 5.0f;
    
    public RapidFireSkill() {
        super("Rapid Fire", MANA_COST, (int) (COOLDOWN_SECOND * 1000));
    }
    
    @Override
    public void performSkill(GameCharacter attacker) {
        RapidFireSkillBuff buff = new RapidFireSkillBuff((int) (DURATION_SECOND));
        attacker.addBuff(buff);
    }
}
