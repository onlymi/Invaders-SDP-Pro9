package entity.skill;

import engine.AssetManager.SpriteType;
import engine.Core;
import entity.Weapon;
import entity.character.GameCharacter;
import java.util.Set;

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
        if (newX < 0) {
            newX = 0;
        } else if (newX > Core.WIDTH - attacker.getWidth()) {
            newX = Core.WIDTH - attacker.getWidth();
        }
        
        if (newY < 0) {
            newY = 0;
        } else if (newY > Core.HEIGHT - attacker.getHeight()) {
            newY = Core.HEIGHT - attacker.getHeight();
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
            // 투사체 속도 증가 (선택 사항)
            arrow.setSpeed(arrow.getSpeed() * 2);
            // 투사체 이미지 변경
            arrow.setSpriteImage(SpriteType.CharacterArcherSecondSkill);
            int newWidth = arrow.getWidth();
            int newHeight = arrow.getHeight();
            int charX = attacker.getPositionX();
            int charY = attacker.getPositionY();
            int charW = attacker.getWidth();
            int charH = attacker.getHeight();
            // 캐릭터가 보는 방향에 따라 위치 재설정
            if (attacker.isFacingRight()) {
                arrow.setPositionX(charX + charW);
                arrow.setPositionY(charY + (charH - newHeight) / 2);
            } else if (attacker.isFacingLeft()) {
                arrow.setPositionX(charX - newWidth);
                arrow.setPositionY(charY + (charH - newHeight) / 2);
            } else if (attacker.isFacingBack()) {
                arrow.setPositionX(charX + (charW - newWidth) / 2);
                arrow.setPositionY(charY - newHeight);
            } else if (attacker.isFacingFront()) {
                arrow.setPositionX(charX + (charW - newWidth) / 2);
                arrow.setPositionY(charY + charH);
            }
            // 슬로우 버프 탑재 (Weapon에 setOnHitBuff가 구현되어 있다고 가정)
            // arrow.setOnHitBuff(new EvasionShotSkillBuff(3.0f));
        }
        attacker.stun((int) (STUN_DURATION * 1000));
    }
}