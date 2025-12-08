package entity.skill;

import engine.AssetManager.SpriteType;
import entity.Weapon;
import entity.character.GameCharacter;
import java.util.Set;

public class SeismicSlashSkill extends Skill {
    
    private static final int MANA_COST = 20;
    private static final float COOLDOWN_SECOND = 8.0f;
    
    public SeismicSlashSkill() {
        super("Seismic Slash", MANA_COST, (int) (COOLDOWN_SECOND * 1000));
        this.spriteType = SpriteType.CharacterWarriorFirstSkill;
    }
    
    @Override
    public void performSkill(GameCharacter attacker, Set<Weapon> weapons) {
    
    }
}
