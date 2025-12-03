package engine.renderer.character;

import engine.AssetManager;
import engine.AssetManager.SpriteType;
import entity.character.ArcherCharacter;
import java.awt.Graphics;
import java.awt.image.BufferedImage;

public class ArcherCharacterRenderer {
    
    public void draw(Graphics g, ArcherCharacter character) {
        AssetManager assetManager = AssetManager.getInstance();
        BufferedImage currentSprite;
        
        if (character.isMoving()) {
            // 캐릭터가 바라보는 방향에 따라 애니메이션 선택
            BufferedImage[] walkFrames;
            if (character.isFacingRight()) {
                walkFrames = assetManager.getAnimation(
                    SpriteType.CharacterArcherRightWalk); // 오른쪽 걷기
            } else if (character.isFacingLeft()) {
                walkFrames = assetManager.getAnimation(
                    SpriteType.CharacterArcherLeftWalk); // 왼쪽 걷기
            } else if (character.isFacingFront()) {
                walkFrames = assetManager.getAnimation(
                    SpriteType.CharacterArcherFrontWalk); // 앞을 보면서(아래로) 걷기
            } else if (character.isFacingBack()) {
                walkFrames = assetManager.getAnimation(
                    SpriteType.CharacterArcherBackWalk); // 뒤를 보면서(위로) 걷기
            } else {
                walkFrames = assetManager.getAnimation(
                    SpriteType.CharacterArcherRightWalk); // 오른쪽 걷기
            }
            
            // 현재 시간을 기준으로 프레임 인덱스 계산 (속도 조절 가능)
            int frameSwitchTime = 100;
            long frameIndex = (System.currentTimeMillis() / frameSwitchTime) % walkFrames.length;
            currentSprite = walkFrames[(int) frameIndex];
        } else {
            // 움직이지 않을 때는 기본(정지) 이미지 사용
            currentSprite = assetManager.getSpriteImage(SpriteType.CharacterArcherStand);
        }
        
        g.drawImage(currentSprite, character.getPositionX(), character.getPositionY(), null);
    }
}