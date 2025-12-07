package entity.skill;

import entity.Weapon;
import entity.character.GameCharacter;
import java.util.Set;

public class PiercingArrowSkill extends Skill {
    
    public PiercingArrowSkill() {
        super("Piercing Arrow", 60, 50);
    }
    
    @Override
    public void performSkill(GameCharacter attacker, Set<Weapon> weapons) {
    
    }
}
