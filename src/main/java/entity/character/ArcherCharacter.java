package entity.character;

import entity.skill.EvasionShotSkill;
import entity.skill.PiercingArrowSkill;
import entity.skill.RapidFireSkill;
import java.util.ArrayList;

public class ArcherCharacter extends Character {
    
    /**
     * Constructor, establishes the entity's generic properties.
     *
     * @param positionX Initial position of the entity in the X axis.
     * @param positionY Initial position of the entity in the Y axis.
     * @param width     Width of the entity.
     * @param height    Height of the entity.
     */
    public ArcherCharacter(int positionX, int positionY, int width, int height) {
        super(positionX, positionY, width, height, CharacterType.ARCHER);
        this.skills = new ArrayList<>();
        this.skills.add(new RapidFireSkill()); // First skill
        this.skills.add(new EvasionShotSkill()); // Second skill
        this.skills.add(new PiercingArrowSkill()); // Ultimate skill
    }
}
