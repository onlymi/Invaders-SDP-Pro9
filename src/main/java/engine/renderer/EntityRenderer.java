package engine.renderer;

import engine.AssetManager;
import engine.AssetManager.SpriteType;
import engine.Core;
import engine.renderer.character.ArcherCharacterRenderer;
import entity.EnemyShip;
import entity.Entity;
import entity.Ship;
import entity.Weapon;
import entity.character.ArcherCharacter;
import entity.character.GameCharacter;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;

public class EntityRenderer {
    
    private final CommonRenderer commonRenderer;
    private final AssetManager assetManager;
    private final ArcherCharacterRenderer archerCharacterRenderer;
    
    // 디버깅용 히트박스 표시 여부
    private static final boolean SHOW_HITBOX = false;
    
    public EntityRenderer(CommonRenderer commonRenderer) {
        this.commonRenderer = commonRenderer;
        this.assetManager = Core.getAssetManager();
        this.archerCharacterRenderer = new ArcherCharacterRenderer();
    }
    
    /**
     * Entity의 중앙을 기준으로 x, y 좌표를 사용해 출력합니다.
     */
    public void drawEntityWithCenterPoint(Graphics g, final Entity entity,
        final int centerX, final int centerY, final Color color) {
        // 중앙 좌표를 좌상단 좌표로 변환
        int topLeftX = centerX - entity.getWidth() / 2;
        int topLeftY = centerY - entity.getHeight() / 2;
        drawEntity(g, entity, topLeftX, topLeftY, color, 1);
    }
    
    /**
     * 스케일을 지정하여 엔티티를 그립니다.
     */
    public void drawEntityByScale(Graphics g, final Entity entity, final int positionX,
        final int positionY, int scale) {
        drawEntity(g, entity, positionX, positionY, getEntityColor(entity), scale);
    }
    
    public void drawEntityByScale(Graphics g, final Entity entity, final int positionX,
        final int positionY, final Color color, final int scale) {
        drawEntity(g, entity, positionX, positionY, color, scale);
    }
    
    /**
     * 기본 크기(scale=1)로 엔티티를 그립니다.
     */
    public void drawEntity(Graphics g, final Entity entity, final int positionX,
        final int positionY) {
        drawEntity(g, entity, positionX, positionY, getEntityColor(entity), 1);
    }
    
    /**
     * 지정된 색상으로 엔티티를 그립니다.
     */
    public void drawEntity(Graphics g, final Entity entity, final int positionX,
        final int positionY, final Color color) {
        drawEntity(g, entity, positionX, positionY, color, 1);
    }
    
    /**
     * 실제 렌더링을 수행하는 메인 메서드입니다. (내부용)
     */
    private void drawEntity(Graphics g, final Entity entity, final int x,
        final int y, final Color color, final int scale) {
        
        if (entity instanceof GameCharacter && !((GameCharacter) entity).isInSelectScreen()) {
            drawHealthBar(g, entity, x, y);
        }
        
        // 특수 캐릭터 처리 - 스케일 정보 전달
        if (entity instanceof ArcherCharacter) {
            if (SHOW_HITBOX) {
                Color prev = g.getColor();
                g.setColor(new Color(255, 0, 0, 128));
                g.fillRect(x, y, entity.getWidth() * scale, entity.getHeight() * scale);
                g.setColor(prev);
            }
            archerCharacterRenderer.draw(g, (ArcherCharacter) entity, x, y, color, scale);
            return;
        }
        
        Graphics2D g2d = (Graphics2D) g;
        AffineTransform originalTransform = g2d.getTransform(); // 변환 상태 저장
        
        // 회전 처리 (Weapon 또는 회전값이 있는 엔티티)
        if (entity instanceof Weapon || entity.getRotation() != 0) {
            double centerX = x + (entity.getWidth() * scale) / 2.0;
            double centerY = y + (entity.getHeight() * scale) / 2.0;
            
            // getRotation()은 라디안 값을 반환하므로 Math.toRadians()를 제거함
            g2d.rotate(Math.toRadians(entity.getRotation()), centerX, centerY);
        }
        
        // 디버그: 히트박스 그리기
        if (SHOW_HITBOX) {
            Color prev = g.getColor();
            g.setColor(new Color(255, 0, 0, 128));
            g.fillRect(x, y, entity.getWidth() * scale, entity.getHeight() * scale);
            g.setColor(prev);
        }
        
        // 타입에 따른 그리기 (이미지 vs 스프라이트)
        SpriteType type = entity.getSpriteType();
        if (type.isImage()) {
            drawEntityAsImage(g, entity, x, y, color, scale);
        } else {
            drawEntityAsSprite(g, entity, x, y, color, scale);
        }
        
        g2d.setTransform(originalTransform); // 변환 상태 복구
    }
    
    private void drawEntityAsImage(Graphics g, final Entity entity, int x, int y,
        Color color, int scale) {
        BufferedImage image = assetManager.getSpriteImage(entity.getSpriteType());
        if (image == null) {
            drawMissingTexturePlaceholder(g, entity, x, y);
            return;
        }
        
        int entityWidthByScale = image.getWidth() * scale;
        int entityHeightByScale = image.getHeight() * scale;
        
        if (entity instanceof EnemyShip enemy) {
            boolean flip = !enemy.isFacingRight();
            
            int drawX = x + (entity.getWidth() - entityWidthByScale) / 2;
            int drawY = y + (entity.getHeight() - entityHeightByScale) / 2;
            
            if (flip) {
                g.drawImage(image, drawX + entityWidthByScale, drawY, -entityWidthByScale,
                    entityHeightByScale, null);
            } else {
                g.drawImage(image, drawX, drawY, entityWidthByScale, entityHeightByScale, null);
            }
            
            return;
        }
        
        g.drawImage(image, x, y, entityWidthByScale, entityHeightByScale, null);
        
        if (color == Color.DARK_GRAY) {
            g.setColor(new Color(0, 0, 0, 200));
            g.fillRect(x, y, entityWidthByScale, entityHeightByScale);
        }
    }
    
    private void drawEntityAsSprite(Graphics g, Entity entity, int x, int y,
        Color color, int scale) {
        boolean[][] spriteMap = assetManager.getSpriteMap(entity.getSpriteType());
        if (spriteMap == null) {
            return;
        }
        
        g.setColor(color);
        
        if (entity.getSpriteType() == SpriteType.BigLaserBeam) {
            // 레이저 빔 특수 처리
            int w = entity.getWidth();
            int h = entity.getHeight();
            g.fillRect(x, y, w, h);
            g.setColor(Color.WHITE);
            g.fillRect(x + w / 4, y, w / 2, h);
        } else {
            // 픽셀 스프라이트 그리기
            for (int i = 0; i < spriteMap.length; i++) {
                for (int j = 0; j < spriteMap[0].length; j++) {
                    if (spriteMap[i][j]) {
                        g.fillRect(x + (i * scale), y + (j * scale), scale, scale);
                    }
                }
            }
        }
    }
    
    public void drawHealthBar(Graphics g, final Entity entity, int x, int y) {
        int maxHealth = ((GameCharacter) entity).getCurrentStats().maxHealthPoints;
        int currentHealth = ((GameCharacter) entity).getCurrentHealthPoints();
        double healthRatio = (double) currentHealth / maxHealth;
        
        if (healthRatio > 1) {
            healthRatio = 1;
        } else if (healthRatio < 0) {
            healthRatio = 0;
        }
        
        int healthBarHeight = 10;
        g.setColor(new Color(0, 255, 0, 110));
        g.drawRect(x - 1, y - healthBarHeight - 4, entity.getWidth() + 1, healthBarHeight + 1);
        g.setColor(new Color(255, 0, 0, 110));
        g.fillRect(x, y - healthBarHeight - 3,
            (int) (entity.getWidth() * healthRatio), healthBarHeight);
    }
    
    private void drawMissingTexturePlaceholder(Graphics g, Entity entity, int x, int y) {
        g.setColor(Color.PINK);
        g.fillRect(x, y, entity.getWidth(), entity.getHeight());
    }
    
    /**
     * 엔티티의 상태(체력, 플레이어 ID 등)에 따른 색상을 결정합니다.
     */
    private static Color getEntityColor(Entity entity) {
        Color baseColor = entity.getColor();
        
        return switch (entity) {
            case Ship ship -> getPlayerColor(ship.getPlayerId(), Color.BLUE, Color.RED, baseColor);
            case Weapon weapon -> getPlayerColor(weapon.getPlayerId(), Color.CYAN, Color.MAGENTA,
                baseColor);
            case EnemyShip enemyShip -> calculateDamageAlpha(enemyShip, baseColor);
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
            float healthRatio = (float) currentHp / maxHp;
            int alpha = (int) (70 + 150 * healthRatio);
            alpha = Math.max(0, Math.min(255, alpha));
            
            return new Color(baseColor.getRed(), baseColor.getGreen(), baseColor.getBlue(), alpha);
        }
        return baseColor;
    }
}