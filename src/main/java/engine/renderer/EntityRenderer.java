package engine.renderer;

import engine.AssetManager;
import engine.AssetManager.SpriteType;
import engine.Core;
import entity.Bullet;
import entity.EnemyShip;
import entity.Entity;
import entity.Ship;
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
        // 기존 메서드는 그대로 두고, 아래의 색상 지정 메서드를 호출하도록 위임합니다.
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
        SpriteType type = entity.getSpriteType();
        
        if (type.isImage()) {
            drawEntityAsImage(g, type, positionX, positionY, color);
        } else {
            drawEntityAsSprite(g, type, positionX, positionY, color);
        }
    }
    
    private void drawEntityAsImage(Graphics g, SpriteType type, int x, int y, Color color) {
        BufferedImage image = assetManager.getSpriteImage(type);
        if (image == null) {
            return;
        }
        
        g.drawImage(image, x, y, null);
        
        if (color == Color.DARK_GRAY) {
            g.setColor(new Color(0, 0, 0, 200));
            g.fillRect(x, y, image.getWidth(), image.getHeight());
        }
    }
    
    /**
     * 스프라이트(boolean 배열) 타입의 엔티티를 그립니다. (예: 픽셀 아트)
     */
    private void drawEntityAsSprite(Graphics g, SpriteType type, int x, int y, Color color) {
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
                    g.fillRect(x + i, y + j, 1, 1);
                }
            }
        }
    }
    
    /**
     * 특정 스케일로 엔티티를 그립니다. (UI 등에서 사용)
     */
    public void drawEntityByScale(Graphics g, Entity entity, int positionX, int positionY,
        int scale) {
        boolean[][] sprite = assetManager.getSpriteMap(entity.getSpriteType());
        
        if (sprite == null) {
            drawMissingTexturePlaceholder(g, entity, positionX, positionY);
            return;
        }
        
        g.setColor(getEntityColor(entity));
        for (int row = 0; row < entity.getWidth(); row++) {
            for (int col = 0; col < entity.getHeight(); col++) {
                if (sprite[row][col]) {
                    // 주의: 기존 코드의 row/col 인덱스 순서 유지 (width가 row 인덱스)
                    g.fillRect(positionX + (col * scale), positionY + (row * scale), scale, scale);
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
            case Bullet bullet ->
                getPlayerColor(bullet.getPlayerId(), Color.CYAN, Color.MAGENTA, baseColor);
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
