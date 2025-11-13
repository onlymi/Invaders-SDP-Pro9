package entity.skill;

import entity.character.Character;

public abstract class Skill {
    
    private String name;
    private int manaCost;
    private float coolTime;
    
    public Skill(final String name, final int manaCost, final float coolTime) {
        this.name = name;
        this.manaCost = manaCost;
        this.coolTime = coolTime;
    }
    
    public abstract void activate(Character attacker);
}
