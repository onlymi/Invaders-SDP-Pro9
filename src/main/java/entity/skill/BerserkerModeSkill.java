package entity.skill;

import engine.AssetManager.SpriteType;
import entity.Weapon;
import entity.buff.BerserkerModeSkillBuff;
import entity.character.GameCharacter;
import java.util.Set;

public class BerserkerModeSkill extends Skill {
    
    private static final int MANA_COST = 40;
    private static final float COOLDOWN_SECOND = 60.0f;
    private static final float ACTIVE_DURATION_SECOND = 5.0f;
    
    public BerserkerModeSkill() {
        super("Berserker Mode", MANA_COST, (int) (COOLDOWN_SECOND * 1000));
        this.spriteType = SpriteType.CharacterWarriorUltimateSkill;
    }
    
    @Override
    public void performSkill(GameCharacter attacker, Set<Weapon> weapons) {
        BerserkerModeSkillBuff buff = new BerserkerModeSkillBuff((int) (ACTIVE_DURATION_SECOND));
        attacker.addBuff(buff);
    }
}
