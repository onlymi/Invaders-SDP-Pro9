package engine.renderer;

import engine.AssetManager;
import engine.AssetManager.SpriteType;
import engine.Core;
import entity.EnemyShip;
import entity.Entity;
import entity.Ship;
import entity.Weapon;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.image.BufferedImage;

public class EntityRenderer {
    
    private CommonRenderer commonRenderer;
    private AssetManager assetManager;
    
    public EntityRenderer(CommonRenderer commonRenderer) {
        this.commonRenderer = commonRenderer;
        this.assetManager = Core.getAssetManager();
    }
    
    /**
     * Draws an entity, using the appropriate image.
     */
    public void drawEntity(Graphics g, final Entity entity, final int positionX,
        final int positionY) {
        drawEntity(g, entity, positionX, positionY, getEntityColor(entity));
    }
    
    /**
     * 지정된 색상으로 엔티티를 그립니다.
     *
     * @param entity    그릴 엔티티
     * @param positionX X 좌표
     * @param positionY Y 좌표
     * @param color     적용할 색상
     */
    public void drawEntity(Graphics g, final Entity entity, final int positionX,
        final int positionY, final Color color) {
        
        // [DEBUG] 히트박스 시각화 (작업 후 주석 처리)
//        Color debugColor = g.getColor();
//        g.setColor(new Color(255, 0, 0, 128));
//        g.fillRect(positionX, positionY, entity.getWidth(), entity.getHeight());
//        g.setColor(debugColor); // 원래 색상 복구
        // [DEBUG END]
        
        SpriteType type = entity.getSpriteType();
        
        if (type.isImage()) {
            drawEntityAsImage(g, entity, positionX, positionY, color, 1);
        } else {
            drawEntityAsSprite(g, type, positionX, positionY, color, 1);
        }
    }
    
    public void drawEntityByScale(Graphics g, final Entity entity, final int positionX,
        final int positionY, int scale) {
        drawEntityByScale(g, entity, positionX, positionY, getEntityColor(entity), scale);
    }
    
    public void drawEntityByScale(Graphics g, final Entity entity, final int positionX,
        final int positionY, final Color color, final int scale) {
        
        // [DEBUG] 히트박스 시각화 (작업 후 주석 처리)
//        Color debugColor = g.getColor();
//        g.setColor(new Color(255, 0, 0, 128));
//        히트박스는 렌더링 스케일(scale)과 무관하게 엔티티의 실제 크기(width, height)를 따릅니다.
//        g.fillRect(positionX, positionY, entity.getWidth(), entity.getHeight());
//        g.setColor(debugColor);
        // [DEBUG END]
        
        SpriteType type = entity.getSpriteType();
        
        if (type.isImage()) {
            drawEntityAsImage(g, entity, positionX, positionY, color, scale);
        } else {
            drawEntityAsSprite(g, type, positionX, positionY, color, scale);
        }
    }
    
    private void drawEntityAsImage(Graphics g, final Entity entity, final int positionX,
        final int positionY, Color color, final int scale) {
        BufferedImage image = assetManager.getSpriteImage(entity.getSpriteType());
        if (image == null) {
            return;
        }
        
        int imageWidth = image.getWidth() * scale;
        int imageHeight = image.getWidth() * scale;
        
        int drawX = positionX + (entity.getWidth() - imageWidth) / 2;
        int drawY = positionY + (entity.getHeight() - imageHeight) / 2;
        
        boolean flip = false;
        if (entity instanceof EnemyShip) {
            // 왼쪽을 보고 있다면 반전
            if (!((EnemyShip) entity).isFacingRight()) {
                flip = true;
            }
        }
        
        if (flip) {
            g.drawImage(image, drawX + imageWidth, drawY, -imageWidth, imageHeight, null);
        } else {
            g.drawImage(image, drawX, drawY, imageWidth, imageHeight, null);
        }
        
        if (color == Color.DARK_GRAY) {
            g.setColor(new Color(0, 0, 0, 200));
            g.fillRect(drawX, drawY, imageWidth, imageHeight);
        }
    }
    
    /**
     * 스프라이트(boolean 배열) 타입의 엔티티를 그립니다.
     */
    private void drawEntityAsSprite(Graphics g, SpriteType type, int positionX, int positionY,
        Color color, int scale) {
        boolean[][] spriteMap = assetManager.getSpriteMap(type);
        if (spriteMap == null) {
            return;
        }
        
        int spriteWidth = spriteMap.length;
        int spriteHeight = spriteMap[0].length;
        
        g.setColor(color);
        for (int i = 0; i < spriteWidth; i++) {
            for (int j = 0; j < spriteHeight; j++) {
                if (spriteMap[i][j]) {
                    g.fillRect(positionX + (i * scale), positionY + (j * scale), scale, scale);
                }
            }
        }
    }
    
    private void drawMissingTexturePlaceholder(Graphics g, Entity entity, int x, int y) {
        g.setColor(Color.PINK);
        g.fillRect(x, y, entity.getWidth(), entity.getHeight());
        System.err.println("EntityRenderer: Can't find sprite for " + entity.getSpriteType());
    }
    
    /**
     * 엔티티의 상태(체력, 플레이어 ID 등)에 따른 색상을 결정합니다.
     */
    private static Color getEntityColor(Entity entity) {
        Color baseColor = entity.getColor();
        
        return switch (entity) {
            case Ship ship -> getPlayerColor(ship.getPlayerId(), Color.BLUE, Color.RED, baseColor);
            case Weapon weapon ->
                getPlayerColor(weapon.getPlayerId(), Color.CYAN, Color.MAGENTA, baseColor);
            case EnemyShip enemy -> calculateDamageAlpha(enemy, baseColor);
            default -> baseColor;
        };
    }
    
    private static Color getPlayerColor(int playerId, Color p1Color, Color p2Color,
        Color defaultColor) {
        if (playerId == 1) {
            return p1Color;
        }
        if (playerId == 2) {
            return p2Color;
        }
        return defaultColor;
    }
    
    private static Color calculateDamageAlpha(EnemyShip enemy, Color baseColor) {
        int currentHp = enemy.getHealth();
        int maxHp = enemy.getInitialHealth();
        
        if (currentHp > 0 && maxHp > 0) {
            // 체력에 비례하여 투명도(Alpha) 조정 (체력이 낮을수록 투명해짐 혹은 그 반대 로직)
            // 기존 로직: 70 + 150 * (비율)
            float healthRatio = (float) currentHp / maxHp;
            int alpha = (int) (70 + 150 * healthRatio);
            alpha = Math.max(0, Math.min(255, alpha)); // Clamp 0~255
            
            return new Color(baseColor.getRed(), baseColor.getGreen(), baseColor.getBlue(), alpha);
        }
        return baseColor;
    }
}
