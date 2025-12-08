package entity.skill;

import engine.AssetManager.SpriteType;
import entity.Weapon;
import entity.buff.IronSkinSkillBuff;
import entity.character.GameCharacter;
import java.util.Set;

public class IronSkinSkill extends Skill {
    
    private static final int MANA_COST = 25;
    private static final float COOLDOWN_SECOND = 15.0f;
    private static final float ACTIVE_DURATION_SECOND = 5.0f;
    
    public IronSkinSkill() {
        super("Iron Skin", MANA_COST, (int) (COOLDOWN_SECOND * 1000));
        this.spriteType = SpriteType.CharacterWarriorSecondSkill;
    }
    
    @Override
    public void performSkill(GameCharacter attacker, Set<Weapon> weapons) {
        IronSkinSkillBuff buff = new IronSkinSkillBuff((int) (ACTIVE_DURATION_SECOND));
        attacker.addBuff(buff);
    }
}
