package entity.character;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

import entity.Entity.Team;
import entity.skill.EvasionShotSkill;
import entity.skill.PiercingArrowSkill;
import entity.skill.RapidFireSkill;
import entity.skill.Skill;
import java.util.ArrayList;
import org.junit.jupiter.api.Test;

public class ArcherCharacterTest {
    
    @Test
    public void testCharacter_ArcherCharacter_healthPoints() {
        ArcherCharacter character = new ArcherCharacter(0, 0, Team.PLAYER1, 1);
        
        int[] expectedValue = {90, 90};
        int[] actualValue = {character.getBaseStats().maxHealthPoints,
            character.currentHealthPoints};
        
        assertArrayEquals(expectedValue, actualValue,
            "Archer character health points is set incorrectly.");
    }
    
    @Test
    public void testCharacter_ArcherCharacter_manaPoints() {
        ArcherCharacter character = new ArcherCharacter(0, 0, Team.PLAYER1, 1);
        
        int[] expectedValue = {100, 100};
        int[] actualValue = {character.getBaseStats().maxManaPoints, character.currentManaPoints};
        
        assertArrayEquals(expectedValue, actualValue,
            "Archer character mana points is set incorrectly.");
    }
    
    @Test
    public void testCharacter_ArcherCharacter_movementSpeed() {
        ArcherCharacter character = new ArcherCharacter(0, 0, Team.PLAYER1, 1);
        
        float expectedValue = 1.2f;
        float actualValue = character.getBaseStats().movementSpeed;
        
        assertEquals(expectedValue, actualValue,
            "Archer character movement speed is set incorrectly.");
    }
    
    @Test
    public void testCharacter_ArcherCharacter_damage() {
        ArcherCharacter character = new ArcherCharacter(0, 0, Team.PLAYER1, 1);
        
        int[] expectedValue = {18, 0};
        int[] actualValue = {character.getBaseStats().physicalDamage,
            character.getBaseStats().magicalDamage};
        
        assertArrayEquals(expectedValue, actualValue,
            "Archer character damage stat is set incorrectly.");
    }
    
    @Test
    public void testCharacter_ArcherCharacter_attackSpeed() {
        ArcherCharacter character = new ArcherCharacter(0, 0, Team.PLAYER1, 1);
        
        float expectedValue = 1.5f;
        float actualValue = character.getBaseStats().attackSpeed;
        
        assertEquals(expectedValue, actualValue,
            "Archer character attack speed is set incorrectly.");
    }
    
    @Test
    public void testCharacter_ArcherCharacter_attackRange() {
        ArcherCharacter character = new ArcherCharacter(0, 0, Team.PLAYER1, 1);
        
        float expectedValue = 12.0f;
        float actualValue = character.getBaseStats().attackRange;
        
        assertEquals(expectedValue, actualValue,
            "Archer character attack range is set incorrectly.");
    }
    
    @Test
    public void testCharacter_ArcherCharacter_critical() {
        ArcherCharacter character = new ArcherCharacter(0, 0, Team.PLAYER1, 1);
        
        float[] expectedValue = {0.15f, 2.0f};
        float[] actualValue = {character.getBaseStats().critChance,
            character.getBaseStats().critDamageMultiplier};
        
        assertArrayEquals(expectedValue, actualValue,
            "Archer character critical stat is set incorrectly.");
    }
    
    @Test
    public void testCharacter_ArcherCharacter_physicalDefense() {
        ArcherCharacter character = new ArcherCharacter(0, 0, Team.PLAYER1, 1);
        
        int expectedValue = 8;
        int actualValue = character.getBaseStats().physicalDefense;
        
        assertEquals(expectedValue, actualValue,
            "Archer character physical defense is set incorrectly.");
    }
    
    @Test
    public void testCharacter_ArcherCharacter_unlocked() {
        ArcherCharacter character = new ArcherCharacter(0, 0, Team.PLAYER1, 1);
        
        boolean expectedValue = true;
        boolean actualValue = character.unlocked;
        
        assertEquals(expectedValue, actualValue,
            "Archer character unlock or not is set incorrectly.");
    }
    
    @Test
    public void testCharacter_ArcherCharacter_skills() {
        ArcherCharacter character = new ArcherCharacter(0, 0, Team.PLAYER1, 1);
        ArrayList<Skill> actualValue = character.skills;
        
        assertEquals(3, actualValue.size(),
            "Archer character skill count is incorrect. Expected 3.");
        assertInstanceOf(RapidFireSkill.class, actualValue.get(0),
            "Skill at index 0 is not RapidFireSkill.");
        assertInstanceOf(EvasionShotSkill.class, actualValue.get(1),
            "Skill at index 1 is not EvasionShotSkill.");
        assertInstanceOf(PiercingArrowSkill.class, actualValue.get(2),
            "Skill at index 2 is not PiercingArrowSkill.");
    }
    
}
