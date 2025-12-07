package entity.skill;

import engine.Core;
import engine.utils.Cooldown;
import entity.Weapon;
import entity.character.GameCharacter;
import java.util.Set;
import java.util.logging.Logger;

public abstract class Skill {
    
    private final Logger logger;
    
    protected String name;
    protected int manaCost;
    protected Cooldown coolDown;
    
    /**
     * Constructor of Skill.
     *
     * @param name     Skill name
     * @param manaCost Mana consumption
     * @param coolTime Cool time (in milliseconds)
     */
    public Skill(final String name, final int manaCost, final int coolTime) {
        this.name = name;
        this.manaCost = manaCost;
        this.coolDown = Core.getCooldown(coolTime);
        this.logger = Core.getLogger();
    }
    
    /**
     * 스킬을 활성화합니다.
     *
     * @param attacker 스킬을 사용하는 캐릭터
     * @param weapons  투사체를 등록할 게임 내 무기 리스트 (추가됨)
     */
    public void activate(GameCharacter attacker, Set<Weapon> weapons) {
        if (attacker == null) {
            logger.warning(name + " skill requires an attacker.");
            return;
        }
        if (!canActivate(attacker)) {
            return;
        }
        
        this.consumeResources(attacker);
        performSkill(attacker, weapons);
    }
    
    /**
     * 각 스킬의 구체적인 동작을 구현하는 추상 메서드.
     *
     * @param attacker 스킬 시전 캐릭터
     * @param weapons  투사체 리스트
     */
    public abstract void performSkill(GameCharacter attacker, Set<Weapon> weapons);
    
    /**
     * Verify the skill is available.
     *
     * @param attacker Characters who want to use the skill
     * @return Whether to use the skill
     */
    public boolean canActivate(GameCharacter attacker) {
        boolean isCooldownReady = this.coolDown.checkFinished();
        boolean isManaEnough = attacker.getCurrentManaPoints() >= this.manaCost;
        
        return isCooldownReady && isManaEnough;
    }
    
    /**
     * Method responsible for mana consumption and cool-time reset.
     *
     * @param attacker Characters who want to use the skill
     */
    protected void consumeResources(GameCharacter attacker) {
        attacker.decreaseMana(this.manaCost);
        this.coolDown.reset();
    }
    
    public String getName() {
        return this.name;
    }
    
    public int getManaCost() {
        return this.manaCost;
    }
    
    /**
     * Returns the remaining time, in milliseconds, until the cool time is over.
     *
     * @return Cool time remaining (in milliseconds)
     */
    public int getRemainingCooldown() {
        return this.coolDown.getDuration();
    }
}
