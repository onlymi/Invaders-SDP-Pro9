package entity.character;

import engine.AssetManager.SpriteType;
import entity.skill.EvasionShotSkill;
import entity.skill.PiercingArrowSkill;
import entity.skill.RapidFireSkill;

public class ArcherCharacter extends GameCharacter {
    
    /**
     * Constructor, establishes the entity's generic properties.
     *
     * @param positionX Initial position of the entity in the X axis
     * @param positionY Initial position of the entity in the Y axis
     * @param team      Team of character
     * @param playerId  ID of Player
     */
    public ArcherCharacter(int positionX, int positionY, Team team, int playerId) {
        super(CharacterType.ARCHER, positionX, positionY,
            SpriteType.CharacterArcherBasic.getWidth(),
            SpriteType.CharacterArcherBasic.getHeight(), team, playerId);
        this.spriteType = SpriteType.CharacterArcherBasic;
        this.projectileSpriteType = SpriteType.CharacterArcherDefaultProjectile;
        this.projectileWidth = projectileSpriteType.getWidth(); // 화살 이미지 너비
        this.projectileHeight = projectileSpriteType.getWidth(); // 화살 이미지 높이
        this.projectileSpeed = -(int) this.baseStats.attackSpeed * 10; // 아처는 투사체가 좀 더 빠름
        this.skills.add(new RapidFireSkill()); // First skill
        this.skills.add(new EvasionShotSkill()); // Second skill
        this.skills.add(new PiercingArrowSkill()); // Ultimate skill
    }
}
