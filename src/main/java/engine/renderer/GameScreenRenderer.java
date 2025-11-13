package engine.renderer;

import animations.BasicGameSpace;
import animations.Explosion;
import engine.AssetManager;
import engine.Core;
import engine.gameplay.achievement.Achievement;
import entity.Entity;
import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.RadialGradientPaint;
import java.awt.RenderingHints;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.logging.Logger;
import screen.Screen;

public class GameScreenRenderer {
    
    private static Logger LOGGER = null;
    private CommonRenderer commonRenderer;
    private EntityRenderer entityRenderer;
    /**
     * Font properties.
     */
    private static FontMetrics fontMetrics;
    
    private final List<Explosion> explosions = new java.util.ArrayList<>();
    BasicGameSpace basicGameSpace = new BasicGameSpace(100);
    
    public GameScreenRenderer(CommonRenderer commonRenderer) {
        LOGGER = Core.getLogger();
        this.commonRenderer = commonRenderer;
        this.entityRenderer = new EntityRenderer(commonRenderer);
    }
    
    public void setLastLife(boolean status) {
        basicGameSpace.setLastLife(status);
    }
    
    /**
     * Countdown to game start.
     *
     * @param screen    Screen to draw on.
     * @param level     Game difficulty level.
     * @param number    Countdown number.
     * @param bonusLife Checks if a bonus life is received.
     */
    public void drawCountDown(Graphics g, final Screen screen, final int level, final int number,
        final boolean bonusLife) {
        int rectWidth = screen.getWidth();
        int rectHeight = screen.getHeight() / 6;
        g.setColor(Color.BLACK);
        g.fillRect(0, screen.getHeight() / 2 - rectHeight / 2, rectWidth, rectHeight);
        g.setColor(Color.GREEN);
        fontMetrics = g.getFontMetrics(commonRenderer.getFontBig());
        if (number >= 4) {
            if (!bonusLife) {
                commonRenderer.drawCenteredBigString(g, screen, "Level " + level,
                    screen.getHeight() / 2 + fontMetrics.getHeight() / 3);
            } else {
                commonRenderer.drawCenteredBigString(g, screen, "Level " + level + " - Bonus life!",
                    screen.getHeight() / 2 + fontMetrics.getHeight() / 3);
            }
        } else if (number != 0) {
            commonRenderer.drawCenteredBigString(g, screen, Integer.toString(number),
                screen.getHeight() / 2 + fontMetrics.getHeight() / 3);
        } else {
            commonRenderer.drawCenteredBigString(g, screen, "GO!",
                screen.getHeight() / 2 + fontMetrics.getHeight() / 3);
        }
    }
    
    /**
     * Draws achievement toasts.
     *
     * @param screen Screen to draw on.
     * @param toasts List of toasts to draw.
     */
    public void drawAchievementToasts(Graphics g, final Screen screen,
        final List<Achievement> toasts) {
        if (toasts == null || toasts.isEmpty()) {
            return;
        }
        
        Achievement achievement = toasts.getLast();
        Graphics2D g2d = (Graphics2D) g.create();
        
        try {
            int box_width = 350;
            int box_height = 110;
            int cornerRadius = 15;
            
            int x = (screen.getWidth() - box_width) / 2;
            int y = (screen.getHeight() - box_height) / 2;
            
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);
            
            g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.3f));
            g2d.setColor(Color.BLACK);
            g2d.fillRoundRect(x, y, box_width, box_height, cornerRadius, cornerRadius);
            
            g2d.setColor(Color.GREEN);
            g2d.setStroke(new BasicStroke(2));
            g2d.drawRoundRect(x, y, box_width, box_height, cornerRadius, cornerRadius);
            
            g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.7f));
            
            g2d.setFont(commonRenderer.getFontBig());
            g2d.setColor(Color.YELLOW);
            FontMetrics bigMetrics = g2d.getFontMetrics(commonRenderer.getFontBig());
            int titleWidth = bigMetrics.stringWidth("Achievement Clear!");
            g2d.drawString("Achievement Clear!", (screen.getWidth() - titleWidth) / 2, y + 35);
            
            g2d.setFont(commonRenderer.getFontRegular());
            g2d.setColor(Color.WHITE);
            FontMetrics regularMetrics = g2d.getFontMetrics(commonRenderer.getFontRegular());
            int nameWidth = regularMetrics.stringWidth(achievement.getName());
            g2d.drawString(achievement.getName(), (screen.getWidth() - nameWidth) / 2, y + 60);
            
            g2d.setColor(Color.LIGHT_GRAY);
            
            if (achievement.getDescription().length() < 30) {
                int descWidth = regularMetrics.stringWidth(achievement.getDescription());
                g2d.drawString(achievement.getDescription(), (screen.getWidth() - descWidth) / 2,
                    y + 80 + regularMetrics.getHeight() / 2);
            } else {
                // 30 characters or more to handle the wrap
                String line1 = achievement.getDescription()
                    .substring(0, achievement.getDescription().length() / 2);
                String line2 = achievement.getDescription()
                    .substring(achievement.getDescription().length() / 2);
                
                // first line
                int line1_width = regularMetrics.stringWidth(line1);
                g2d.drawString(line1, (screen.getWidth() - line1_width) / 2, y + 80);
                
                // second line
                int line2_width = regularMetrics.stringWidth(line2);
                g2d.drawString(line2, (screen.getWidth() - line2_width) / 2,
                    y + 80 + regularMetrics.getHeight());
            }
        } finally {
            g2d.dispose();
        }
    }
    
    /**
     * Draws current score on screen.
     *
     * @param screen Screen to draw on.
     * @param score  Current score.
     */
    public void drawScore(Graphics g, final Screen screen, final int score) {
        Font font = AssetManager.getInstance().getFontRegular();
        g.setFont(font);
        g.setColor(Color.WHITE);
        String scoreString = String.format("%04d", score);
        g.drawString(scoreString, screen.getWidth() - 60, 25);
    }
    
    /**
     * Draws number of remaining lives on screen.
     *
     * @param screen Screen to draw on.
     * @param lives  Whether the game is in co-op mode.
     */
    
    
    public void drawLives(Graphics g, final Screen screen, final int lives, final boolean isCoop) {
        g.setFont(commonRenderer.getFontRegular());
        g.setColor(Color.WHITE);
        
        Entity heart = new Entity(0, 0, 11 * 2, 10 * 2, Color.RED) {
            {
                this.spriteType = AssetManager.SpriteType.Heart;
            }
        };
        
        if (isCoop) {
            g.drawString(Integer.toString(lives), 20, 25);
            for (int i = 0; i < lives; i++) {
                if (i < 3) {
                    entityRenderer.drawEntity(g, heart, 40 + 35 * i, 9);
                } else {
                    entityRenderer.drawEntity(g, heart, 40 + 35 * (i - 3), 9 + 25);
                }
            }
        } else {
            g.drawString(Integer.toString(lives), 20, 40);
            for (int i = 0; i < lives; i++) {
                entityRenderer.drawEntity(g, heart, 40 + 35 * i, 23);
            }
        }
    }
    
    /**
     * Draws current coin count on screen.
     *
     * @param screen Screen to draw on.
     * @param coins  Current coin count.
     */
    public void drawCoins(Graphics g, final Screen screen, final int coins) {
        g.setFont(commonRenderer.getFontRegular());
        g.setColor(Color.YELLOW);
        g.drawString(String.format("%04d", coins), screen.getWidth() - 60, 52);
        g.drawString("COIN : ", screen.getWidth() - 115, 52);
    }
    
    // 2P mode: drawCoins method but for both players, but separate coin counts
    public void drawCoins(Graphics g, final Screen screen, final int coinsP1, final int coinsP2) {
        g.setFont(commonRenderer.getFontRegular());
        g.setColor(Color.YELLOW);
        g.drawString("P1: " + String.format("%04d", coinsP1), screen.getWidth() - 200, 25);
        g.drawString("P2: " + String.format("%04d", coinsP2), screen.getWidth() - 100, 25);
    }
    
    public void drawLevel(Graphics g, final Screen screen, final int level) {
        g.setColor(Color.WHITE);
        String levelString = "Level " + level;
        g.drawString(levelString, screen.getWidth() - 250, 25);
    }
    
    public void drawShipCount(Graphics g, final Screen screen, final int shipCount) {
        g.setColor(Color.GREEN);
        Entity enemyIcon = new Entity(0, 0, 12 * 2, 8 * 2, Color.GREEN) {
            {
                this.spriteType = AssetManager.SpriteType.EnemyShipB2;
            }
        };
        int iconX = screen.getWidth() - 252;
        int iconY = 37;
        entityRenderer.drawEntity(g, enemyIcon, iconX, iconY);
        String shipString = ": " + shipCount;
        g.drawString(shipString, iconX + 30, 52);
    }
    
    public void triggerExplosion(int x, int y, boolean enemy, boolean finalExplosion) {
        LOGGER.info("Enemy: " + enemy);
        LOGGER.info("final: " + finalExplosion);
        explosions.add(new Explosion(x, y, enemy, finalExplosion));
    }
    
    public void drawExplosions(Graphics g, Screen screen) {
        
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        g2d.setColor(Color.WHITE);
        
        Iterator<Explosion> iterator = explosions.iterator();
        
        while (iterator.hasNext()) {
            Explosion e = iterator.next();
            e.update();
            
            if (!e.isActive()) {
                iterator.remove();
                continue;
            }
            
            for (Explosion.Particle p : e.getParticles()) {
                if (!p.active) {
                    continue;
                }
                
                int baseSize;
                
                Random random = new Random();
                if (e.getSize() == 4) {
                    baseSize = random.nextInt(5) + 2;
                } else {
                    baseSize = random.nextInt(6) + 18;
                }
                
                int flickerAlpha = Math.max(0,
                    Math.min(255, p.color.getAlpha() - (int) (Math.random() * 50)));
                
                float[] dist = {0.0f, 0.3f, 0.7f, 1.0f};
                Color[] colors;
                if (e.enemy()) {
                    colors = new Color[]{
                        new Color(255, 255, 250, flickerAlpha),
                        new Color(255, 250, 180, flickerAlpha),
                        new Color(255, 200, 220, flickerAlpha / 2),
                        new Color(0, 0, 0, 0)
                    };
                } else {
                    colors = new Color[]{
                        new Color(255, 255, 180, flickerAlpha),
                        new Color(255, 200, 0, flickerAlpha),
                        new Color(255, 80, 0, flickerAlpha / 2),
                        new Color(0, 0, 0, 0)
                    };
                }
                
                RadialGradientPaint paint = new RadialGradientPaint(
                    new Point((int) p.x, (int) p.y),
                    baseSize,
                    dist,
                    colors
                );
                
                g2d.setPaint(paint);
                
                int offsetX = (int) (Math.random() * 4 - 2);
                int offsetY = (int) (Math.random() * 4 - 2);
                
                g2d.fillOval(
                    (int) (p.x - baseSize / 2 + offsetX),
                    (int) (p.y - baseSize / 2 + offsetY),
                    baseSize,
                    baseSize
                );
            }
        }
    }
    
}
