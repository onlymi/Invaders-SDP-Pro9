package entity.character;

import engine.AssetManager.SpriteType;

public class WizardCharacter extends GameCharacter {
    
    /**
     * Constructor, establishes the entity's generic properties.
     *
     * @param positionX Initial position of the entity in the X axis
     * @param positionY Initial position of the entity in the Y axis
     * @param team      Team of character
     * @param playerId  ID of Player
     */
    public WizardCharacter(int positionX, int positionY, Team team, int playerId) {
        super(CharacterType.WIZARD, positionX, positionY,
            SpriteType.CharacterWizardBasic.getWidth(),
            SpriteType.CharacterWizardBasic.getHeight(), team, playerId);
        this.spriteType = SpriteType.CharacterWizardBasic;
    }
}
