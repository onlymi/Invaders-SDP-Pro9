package entity.character;

import engine.AssetManager.SpriteType;
import entity.skill.BerserkerModeSkill;
import entity.skill.IronSkinSkill;
import entity.skill.SeismicSlashSkill;

public class WarriorCharacter extends GameCharacter {
    
    /**
     * Constructor, establishes the entity's generic properties.
     *
     * @param positionX Initial position of the entity in the X axis
     * @param positionY Initial position of the entity in the Y axis
     * @param team      Team of character
     * @param playerId  ID of Player
     */
    public WarriorCharacter(int positionX, int positionY, Team team, int playerId) {
        super(CharacterType.WARRIOR, positionX, positionY,
            SpriteType.CharacterWarriorBasic.getWidth(),
            SpriteType.CharacterWarriorBasic.getHeight(), team, playerId);
        this.spriteType = SpriteType.CharacterWarriorBasic;
        this.projectileSpriteType = SpriteType.CharacterWarriorDefaultProjectile;
        this.projectileWidth = projectileSpriteType.getWidth();
        this.projectileHeight = projectileSpriteType.getHeight();
        this.skills.add(new SeismicSlashSkill()); // First skill
        this.skills.add(new IronSkinSkill()); // Second skill
        this.skills.add(new BerserkerModeSkill()); // Ultimate skill
    }
}
