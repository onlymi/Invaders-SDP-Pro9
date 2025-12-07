package entity.skill;

import engine.AssetManager.SpriteType;
import engine.Core;
import entity.Weapon;
import entity.character.GameCharacter;
import java.util.Set;
import screen.GameScreen;

public class EvasionShotSkill extends Skill {
    
    private static final int MANA_COST = 25;
    private static final float COOLDOWN_SECOND = 10.0f;
    private static final int JUMP_DISTANCE = 150;
    private static final float STUN_DURATION = 0.5f;
    
    public EvasionShotSkill() {
        super("Evasion Shot", MANA_COST, (int) (COOLDOWN_SECOND * 1000));
    }
    
    public void doJump(GameCharacter attacker) {
        // 이동 방향 벡터 계산 (dx, dy)
        float dx = 0;
        float dy = 0;
        
        if (attacker.isFacingRight()) {
            dx -= 1;
        } else if (attacker.isFacingLeft()) {
            dx += 1;
        }
        
        if (attacker.isFacingFront()) {
            dy -= 1;
        } else if (attacker.isFacingBack()) {
            dy += 1;
        }
        
        if (dx != 0 && dy != 0) {
            float correctionFactor = GameCharacter.DIAGONAL_CORRECTION_FACTOR;
            dx *= correctionFactor;
            dy *= correctionFactor;
        }
        
        int currentX = attacker.getPositionX();
        int currentY = attacker.getPositionY();
        
        int newX = currentX + (int) (dx * JUMP_DISTANCE);
        int newY = currentY + (int) (dy * JUMP_DISTANCE);
        
        // 화면 밖으로 나가지 않도록 경계 처리 (Boundary Check)
        if (newX < 1) {
            newX = 1;
        } else if (newX > Core.getFrameWidth() - attacker.getWidth() - 1) {
            newX = Core.getFrameWidth() - attacker.getWidth() - 1;
        }
        
        if (newY < GameScreen.SEPARATION_LINE_HEIGHT + 1) {
            newY = GameScreen.SEPARATION_LINE_HEIGHT + 1;
        } else if (newY > Core.getFrameHeight() - attacker.getHeight() - 30) {
            newY = Core.getFrameHeight() - attacker.getHeight() - 30;
        }
        
        attacker.setPositionX(newX);
        attacker.setPositionY(newY);
    }
    
    /**
     * 투사체 발사를 포함한 스킬 수행 메서드입니다.
     */
    @Override
    public void performSkill(GameCharacter attacker, Set<Weapon> weapons) {
        // 이동 수행
        doJump(attacker);
        
        Weapon arrow = attacker.createWeapon(weapons);
        
        // 스킬 전용 효과 적용
        if (arrow != null) {
            // 데미지 1.5배 적용
            int skillDamage = (int) (attacker.getCurrentStats().physicalDamage * 1.5);
            arrow.setDamage(skillDamage);
            System.out.println(arrow.getSpeed());
            arrow.setSpeed(arrow.getSpeed() * 2);
            System.out.println(arrow.getSpeed());
            // 투사체 이미지 변경
            arrow.setSpriteImage(SpriteType.CharacterArcherSecondSkill);
            int newArrowWidth = arrow.getWidth();
            int newArrowHeight = arrow.getHeight();
            int charX = attacker.getPositionX();
            int charY = attacker.getPositionY();
            int charW = attacker.getWidth();
            int charH = attacker.getHeight();
            
            if (attacker.isFacingLeft()) {
                arrow.setPositionX(charX - arrow.getWidth() / 2);
                arrow.setPositionY(charX + (charW - newArrowWidth) / 2);
            } else if (attacker.isFacingRight()) {
                arrow.setPositionX(charX + charW + newArrowWidth / 2);
                arrow.setPositionY(charX + (charW - newArrowWidth) / 2);
            } else if (attacker.isFacingFront()) {
                arrow.setPositionX(charX + newArrowWidth / 2);
                arrow.setPositionY(charY + charH);
            } else if (attacker.isFacingBack()) {
                arrow.setPositionX(charX + newArrowWidth / 2);
                arrow.setPositionY(charY - newArrowHeight);
            } else {
                arrow.setPositionX(charX + newArrowWidth / 2);
                arrow.setPositionY(charY - newArrowHeight);
            }
            // 슬로우 버프 탑재 (Weapon에 setOnHitBuff가 구현되어 있다고 가정)
            // arrow.setOnHitBuff(new EvasionShotSkillBuff(3.0f));
        }
        attacker.stun((int) (STUN_DURATION * 1000));
    }
}