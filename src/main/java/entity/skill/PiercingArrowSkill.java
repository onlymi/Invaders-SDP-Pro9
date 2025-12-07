package entity.skill;

import engine.AssetManager.SpriteType;
import engine.Core;
import entity.Weapon;
import entity.character.GameCharacter;
import java.util.Set;

public class PiercingArrowSkill extends Skill {
    
    private static final int MANA_COST = 1;
    private static final float COOLDOWN_SECOND = 1.0f;
    
    public PiercingArrowSkill() {
        super("Piercing Arrow", MANA_COST, (int) (COOLDOWN_SECOND * 1000));
    }
    
    @Override
    public void performSkill(GameCharacter attacker, Set<Weapon> weapons) {
        Weapon arrow = attacker.createWeapon(weapons);
        
        if (arrow != null) {
            int skillDamage = (int) (attacker.getCurrentStats().physicalDamage * 4.0);
            arrow.setDamage(skillDamage);
            arrow.setRange(Math.min(Core.getFrameWidth(), Core.getFrameHeight()));
            arrow.setCharacter(attacker);
            arrow.setSpriteImage(SpriteType.CharacterArcherUltimateSkill);
            
            int arrowHeight = arrow.getHeight();
            
            int charCenterX = attacker.getPositionX() + attacker.getWidth() / 2;
            int charCenterY = attacker.getPositionY() + attacker.getHeight() / 2;
            
            int offsetDist = (attacker.getWidth() + arrowHeight) / 2;
            
            boolean isDiagonal = (attacker.isFacingLeft() || attacker.isFacingRight())
                && (attacker.isFacingFront() || attacker.isFacingBack());
            
            if (isDiagonal) {
                offsetDist = (int) (offsetDist * GameCharacter.DIAGONAL_CORRECTION_FACTOR);
            }
            
            int weaponCenterX = charCenterX;
            int weaponCenterY = charCenterY;
            
            if (attacker.isFacingRight()) {
                weaponCenterX += offsetDist;
            } else if (attacker.isFacingLeft()) {
                weaponCenterX -= offsetDist;
            }
            
            if (attacker.isFacingFront()) {
                weaponCenterY += offsetDist;
            } else if (attacker.isFacingBack()) {
                weaponCenterY -= offsetDist;
            }
            
            int arrowWidth = arrow.getWidth();
            
            arrow.setPositionX(weaponCenterX - arrowWidth / 2);
            arrow.setPositionY(weaponCenterY - arrowHeight / 2);
            
            // 슬로우 버프 탑재 (Weapon에 setOnHitBuff가 구현되어 있다고 가정)
            // arrow.setOnHitBuff(new EvasionShotSkillBuff(3.0f));
        }
    }
}
