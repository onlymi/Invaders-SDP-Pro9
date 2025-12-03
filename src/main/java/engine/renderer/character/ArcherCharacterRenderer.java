package engine.renderer.character;

import engine.AssetManager;
import engine.AssetManager.SpriteType;
import entity.character.ArcherCharacter;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.image.BufferedImage;

public class ArcherCharacterRenderer {
    
    /**
     * 아처 캐릭터를 그립니다.
     *
     * @param g         그래픽 컨텍스트
     * @param character 아처 캐릭터 엔티티
     * @param x         그릴 X 좌표
     * @param y         그릴 Y 좌표
     * @param scale     확대/축소 비율 (캐릭터 선택 화면 등에서 사용)
     */
    public void draw(Graphics g, ArcherCharacter character, int x, int y, Color color, int scale) {
        AssetManager assetManager = AssetManager.getInstance();
        BufferedImage currentSprite = assetManager.getSpriteImage(SpriteType.CharacterArcherStand);
        
        if (character.isInSelectScreen()) {
            currentSprite = assetManager.getSpriteImage(SpriteType.CharacterArcherBasic);
        }
        
        if (character.isMoving()) {
            // 캐릭터가 바라보는 방향에 따라 애니메이션 선택
            BufferedImage[] walkFrames;
            if (character.isFacingLeft()) {
                walkFrames = assetManager.getAnimation(SpriteType.CharacterArcherLeftWalk);
            } else if (character.isFacingRight()) {
                walkFrames = assetManager.getAnimation(SpriteType.CharacterArcherRightWalk);
            } else if (character.isFacingFront()) {
                walkFrames = assetManager.getAnimation(SpriteType.CharacterArcherFrontWalk);
            } else if (character.isFacingBack()) {
                walkFrames = assetManager.getAnimation(SpriteType.CharacterArcherBackWalk);
            } else {
                walkFrames = assetManager.getAnimation(SpriteType.CharacterArcherRightWalk);
            }
            
            // 현재 시간을 기준으로 프레임 인덱스 계산 (속도 조절 가능)
            double speed = character.getCurrentStats().movementSpeed;
            int frameSwitchTime = (int) (speed * 100);
            if (frameSwitchTime <= 0) {
                frameSwitchTime = 100; // 0으로 나누기 방지
            }
            
            int frameIndex =
                (int) (System.currentTimeMillis() / frameSwitchTime) % walkFrames.length;
            currentSprite = walkFrames[frameIndex];
        }
        
        if (character.isAttacking()) {
            if (character.isFacingLeft()) {
                currentSprite = assetManager.getSpriteImage(SpriteType.CharacterArcherLeftAttack);
            } else if (character.isFacingRight()) {
                currentSprite = assetManager.getSpriteImage(SpriteType.CharacterArcherRightAttack);
            }
        }
        
        // 이미지 그리기 (스케일 적용)
        if (currentSprite != null) {
            int width = currentSprite.getWidth() * scale;
            int height = currentSprite.getHeight() * scale;
            
            // character.getPositionX() 대신 인자로 받은 x, y 사용 (렌더러에서 좌표 보정을 할 수 있으므로)
            g.drawImage(currentSprite, x, y, width, height, null);
            
            if (color == Color.DARK_GRAY) {
                g.setColor(new Color(0, 0, 0, 200));
                g.fillRect(x, y, width, height);
            }
        }
    }
}