package engine.renderer;

import engine.AssetManager;
import entity.Bullet;
import entity.Entity;
import entity.Ship;
import java.awt.Color;
import java.awt.Graphics;

public class EntityRenderer {
    
    private CommonRenderer commonRenderer;
    
    public EntityRenderer(CommonRenderer commonRenderer) {
        this.commonRenderer = commonRenderer;
    }
    
    /**
     * Draws an entity, using the appropriate image.
     *
     * @param entity    Entity to be drawn.
     * @param positionX Coordinates for the left side of the image.
     * @param positionY Coordinates for the upper side of the image.
     */
    public void drawEntity(Graphics g, final Entity entity, final int positionX,
        final int positionY) {
        // [수정] AssetManager 인스턴스를 직접 가져와 spriteMap을 대체합니다.
        AssetManager assetManager = AssetManager.getInstance();
        boolean[][] image = assetManager.getSprite(entity.getSpriteType());
        
        // [추가] 스프라이트를 찾지 못했을 때 오류가 나지 않도록 방어 코드 추가
        if (image == null) {
            g.setColor(Color.PINK); // 누락된 스프라이트를 쉽게 식별하도록 분홍색으로 표시
            g.fillRect(positionX, positionY, entity.getWidth(), entity.getHeight());
            System.err.println("EntityRenderer: Can't find sprite about " + entity.getSpriteType());
            return;
        }
        
        // 2P mode: start with the entity's own color
        final Color color = getColor(entity);
        
        // --- Scaling logic ---
        // Original sprite dimensions
        int spriteWidth = image.length;
        int spriteHeight = image[0].length;
        
        // Entity dimensions (modified via Bullet constructor or other entities)
        int entityWidth = entity.getWidth();
        int entityHeight = entity.getHeight();
        
        // Calculate scaling ratios compared to original sprite
        float widthRatio = (float) entityWidth / (spriteWidth * 2);
        float heightRatio = (float) entityHeight / (spriteHeight * 2);
        // --- End of scaling logic ---
        
        // Set drawing color again
        g.setColor(color);
        // Draw the sprite with scaling applied
        for (int i = 0; i < spriteWidth; i++) {
            for (int j = 0; j < spriteHeight; j++) {
                if (image[i][j]) {
                    // Apply calculated scaling ratio to pixel positions and size
                    g.fillRect(positionX + (int) (i * 2 * widthRatio),
                        positionY + (int) (j * 2 * heightRatio),
                        (int) Math.ceil(widthRatio * 2), // Adjust the width of the pixel
                        (int) Math.ceil(heightRatio * 2) // Adjust the height of the pixel
                    );
                }
            }
        }
    }
    
    private static Color getColor(Entity entity) {
        Color color = entity.getColor();
        
        // Color-code by player when applicable
        if (entity instanceof Ship) {
            Ship ship = (Ship) entity;
            int pid = ship.getPlayerId(); // requires Ship.getPlayerId()
            if (pid == 1) {
                color = Color.BLUE; // P1 ship
            } else if (pid == 2) {
                color = Color.RED; // P2 ship
            }
            
            // else leave default (e.g., green) for legacy/unknown
        } else if (entity instanceof Bullet) {
            Bullet bullet = (Bullet) entity;
            int pid = bullet.getPlayerId(); // requires Bullet.getPlayerId()
            if (pid == 1) {
                color = Color.CYAN; // P1 bullet
            } else if (pid == 2) {
                color = Color.MAGENTA; // P2 bullet
            }
            // enemy bullets will keep their default color from the entity
        }
        
        if (entity instanceof entity.EnemyShip) {
            entity.EnemyShip enemy = (entity.EnemyShip) entity;
            if ((enemy.getSpriteType() == AssetManager.SpriteType.EnemyShipA1
                || enemy.getSpriteType() == AssetManager.SpriteType.EnemyShipA2)
                && enemy.getHealth() == 1) {
                color = new Color(color.getRed(), color.getGreen(), color.getBlue(), 32);
            }
        }
        return color;
    }
}
