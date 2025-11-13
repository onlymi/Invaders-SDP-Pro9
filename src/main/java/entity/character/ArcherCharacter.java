package entity.character;

public class ArcherCharacter extends Character {
    
    /**
     * Constructor, establishes the entity's generic properties.
     *
     * @param positionX Initial position of the entity in the X axis.
     * @param positionY Initial position of the entity in the Y axis.
     * @param width     Width of the entity.
     * @param height    Height of the entity.
     */
    public ArcherCharacter(int positionX, int positionY, int width, int height,
        CharacterType characterType) {
        super(positionX, positionY, width, height, characterType);
    }
}
