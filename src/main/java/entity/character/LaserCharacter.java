package entity.character;

import engine.AssetManager.SpriteType;

public class LaserCharacter extends GameCharacter {
    
    /**
     * Constructor, establishes the entity's generic properties.
     *
     * @param positionX Initial position of the entity in the X axis
     * @param positionY Initial position of the entity in the Y axis
     * @param team      Team of character
     * @param playerId  ID of Player
     */
    public LaserCharacter(int positionX, int positionY, Team team, int playerId) {
        super(CharacterType.LASER, positionX, positionY,
            SpriteType.CharacterLaserBasic.getWidth(),
            SpriteType.CharacterLaserBasic.getHeight(), team, playerId);
        this.spriteType = SpriteType.CharacterLaserBasic;
    }
}
