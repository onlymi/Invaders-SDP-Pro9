package entity.character;

import engine.AssetManager.SpriteType;
import entity.skill.EvasionShotSkill;
import entity.skill.PiercingArrowSkill;
import entity.skill.RapidFireSkill;
import java.util.ArrayList;

public class ArcherCharacter extends GameCharacter {
    
    /**
     * Constructor, establishes the entity's generic properties.
     *
     * @param positionX Initial position of the entity in the X axis.
     * @param positionY Initial position of the entity in the Y axis.
     */
    public ArcherCharacter(int positionX, int positionY) {
        super(positionX, positionY, SpriteType.CharacterArcherBasic.getWidth(),
            SpriteType.CharacterArcherBasic.getHeight(), CharacterType.ARCHER);
        this.skills = new ArrayList<>();
        this.skills.add(new RapidFireSkill()); // First skill
        this.skills.add(new EvasionShotSkill()); // Second skill
        this.skills.add(new PiercingArrowSkill()); // Ultimate skill
    }
}
