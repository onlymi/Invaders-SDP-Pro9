package entity.character;

import entity.Entity.Team;

public class CharacterSpawner {
    
    public CharacterSpawner() {
    
    }
    
    public static GameCharacter createCharacter(CharacterType characterType, int positionX,
        int positionY, Team team, int playerId) {
        return switch (characterType) {
            case WARRIOR -> new WarriorCharacter(positionX, positionY, team, playerId);
            case ARCHER -> new ArcherCharacter(positionX, positionY, team, playerId);
            case WIZARD -> new WizardCharacter(positionX, positionY, team, playerId);
            case LASER -> new LaserCharacter(positionX, positionY, team, playerId);
            case ELECTRIC -> new ElectricCharacter(positionX, positionY, team, playerId);
            case BOMBER -> new BomberCharacter(positionX, positionY, team, playerId);
            case HEALER -> new HealerCharacter(positionX, positionY, team, playerId);
        };
    }
    
}
