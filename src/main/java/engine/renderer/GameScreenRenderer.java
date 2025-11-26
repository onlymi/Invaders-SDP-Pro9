package engine.renderer;

import animations.Explosion;
import engine.AssetManager;
import engine.Core;
import engine.GameState;
import engine.gameplay.achievement.Achievement;
import engine.gameplay.item.ItemData;
import engine.gameplay.item.ItemManager;
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

    // to query last picked item for toast
    private final ItemManager itemManager;
    private final java.util.Set<String> warnedSpriteTypes = new java.util.HashSet<>();

    /**
     * Font properties.
     */
    private static FontMetrics fontMetrics;
    private final List<Explosion> explosions = new java.util.ArrayList<>();

    public GameScreenRenderer(CommonRenderer commonRenderer, ItemManager itemManager) {
        LOGGER = Core.getLogger();
        this.commonRenderer = commonRenderer;
        this.entityRenderer = new EntityRenderer(commonRenderer);
        this.itemManager = itemManager;
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

    /**
     * Draw a short-lived toast showing the last picked item's name/description.
     */
    public void drawItemToast(Graphics g, final Screen screen) {
        if (itemManager == null) {
            return;
        }

        itemManager.getItemToDescribe().ifPresent(item -> {
            String name = item.getDisplayName();
            String baseDesc = item.getData().getDescription();

            ItemData data = item.getData();
            int cost = 0;
            if (item.getData() != null) {
                cost = item.getData().getCost();
            }

            StringBuilder descBuilder = new StringBuilder();
            if (baseDesc != null && !baseDesc.isEmpty()) {
                descBuilder.append(baseDesc);
            }

            if (cost == 0) {
                if (descBuilder.length() > 0) {
                    descBuilder.append("\n");
                }
                descBuilder.append("No cost required.");
            } else {
                if (descBuilder.length() > 0) {
                    descBuilder.append("\n");
                }
                descBuilder.append("Cost: ").append(cost);
            }
            String desc = descBuilder.toString();

            Graphics2D g2d = (Graphics2D) g.create();
            try {
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON);

                // Layout
                int padding = 12;
                int maxTextWidth = 420;
                int margin = 20;
                int x = margin;
                int y = screen.getHeight() - 110 - margin; // bottom-left

                // Fonts
                Font titleFont = commonRenderer.getFontBig();
                Font bodyFont = commonRenderer.getFontRegular();
                FontMetrics titleFm = g2d.getFontMetrics(titleFont);
                FontMetrics bodyFm = g2d.getFontMetrics(bodyFont);

                // Wrap lines
                java.util.List<String> nameLines = wrapText(name, titleFm, maxTextWidth);
                java.util.List<String> descLines = wrapText(desc, bodyFm, maxTextWidth);

                int textH = nameLines.size() * titleFm.getHeight()
                    + 6
                    + descLines.size() * bodyFm.getHeight();
                int textW = Math.max(measureMaxWidth(nameLines, titleFm),
                    measureMaxWidth(descLines, bodyFm));
                int boxW = Math.max(220, textW + padding * 2);
                int boxH = textH + padding * 2;

                // Background (translucent) + border
                g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.65f));
                g2d.setColor(Color.BLACK);
                g2d.fillRoundRect(x, y, boxW, boxH, 14, 14);

                g2d.setComposite(AlphaComposite.SrcOver);
                g2d.setColor(new Color(255, 255, 255, 180));
                g2d.setStroke(new BasicStroke(1.5f));
                g2d.drawRoundRect(x, y, boxW, boxH, 14, 14);

                // Text
                int tx = x + padding;
                int ty = y + padding + titleFm.getAscent();

                g2d.setFont(titleFont);
                g2d.setColor(Color.WHITE);
                for (String ln : nameLines) {
                    drawShadowString(g2d, ln, tx, ty);
                    ty += titleFm.getHeight();
                }

                ty += 2; // small gap

                g2d.setFont(bodyFont);
                g2d.setColor(new Color(230, 230, 230));
                for (String ln : descLines) {
                    drawShadowString(g2d, ln, tx, ty);
                    ty += bodyFm.getHeight();
                }
            } finally {
                g2d.dispose();
            }
        });
    }

    // ---------- helpers ----------

    private void drawShadowString(Graphics2D g2d, String s, int x, int y) {
        Color old = g2d.getColor();
        g2d.setColor(new Color(0, 0, 0, 160));
        g2d.drawString(s, x + 1, y + 1);
        g2d.setColor(old);
        g2d.drawString(s, x, y);
    }

    private java.util.List<String> wrapText(String text, FontMetrics fm, int maxWidth) {
        java.util.List<String> lines = new java.util.ArrayList<>();
        if (text == null || text.isEmpty()) {
            lines.add("");
            return lines;
        }
        String[] words = text.split("\\s+");
        StringBuilder cur = new StringBuilder();
        for (String w : words) {
            String tryLine = cur.length() == 0 ? w : cur + " " + w;
            if (fm.stringWidth(tryLine) <= maxWidth) {
                cur = new StringBuilder(tryLine);
            } else {
                if (cur.length() > 0) {
                    lines.add(cur.toString());
                }
                if (fm.stringWidth(w) > maxWidth) {
                    lines.addAll(hardWrapWord(w, fm, maxWidth));
                    cur = new StringBuilder();
                } else {
                    cur = new StringBuilder(w);
                }
            }
        }
        if (cur.length() > 0) {
            lines.add(cur.toString());
        }
        return lines;
    }

    private java.util.List<String> hardWrapWord(String word, FontMetrics fm, int maxWidth) {
        java.util.List<String> out = new java.util.ArrayList<>();
        StringBuilder buf = new StringBuilder();
        for (char c : word.toCharArray()) {
            if (fm.stringWidth(buf.toString() + c) > maxWidth) {
                out.add(buf.toString());
                buf = new StringBuilder();
            }
            buf.append(c);
        }
        if (buf.length() > 0) {
            out.add(buf.toString());
        }
        return out;
    }

    private int measureMaxWidth(java.util.List<String> lines, FontMetrics fm) {
        int w = 0;
        for (String ln : lines) {
            w = Math.max(w, fm.stringWidth(ln));
        }
        return w;
    }

    public void drawActiveItemSlots(Graphics g, final Screen screen, final GameState gameState) {
        if (gameState == null) {
            return;
        }

        Graphics2D g2d = (Graphics2D) g.create();
        try {
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);

            int slotSize = 46;
            int padding = 10;
            int margin = 18;

            int y = screen.getHeight() - margin - slotSize;

            int xP1 = screen.getWidth() - margin - slotSize * 2 - padding;

            drawOneActiveSlot(g2d, screen, gameState, 0, xP1, y, slotSize, "P1");

            // --- P2 slot: Right ---
            if (gameState.isCoop()) {
                int xP2 = screen.getWidth() - margin - slotSize;
                drawOneActiveSlot(g2d, screen, gameState, 1, xP2, y, slotSize, "P2");
            }

        } finally {
            g2d.dispose();
        }
    }

    private void drawOneActiveSlot(Graphics2D g2d, Screen screen, GameState gameState,
        int playerIndex, int x, int y, int size, String label) {

        // slot box
        g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.55f));
        g2d.setColor(Color.BLACK);
        g2d.fillRoundRect(x, y, size, size, 8, 8);

        g2d.setComposite(AlphaComposite.SrcOver);
        g2d.setColor(Color.WHITE);
        g2d.setStroke(new BasicStroke(1.5f));
        g2d.drawRoundRect(x, y, size, size, 8, 8);

        // guide label for press key
        g2d.setFont(commonRenderer.getFontRegular());
        g2d.setColor(Color.WHITE);
        String keyLabel = (playerIndex == 0) ? "Q" : "/";
        g2d.drawString(keyLabel, x + 4, y - 2);

        // active item
        List<ItemData> actives = gameState.getActiveItemData(playerIndex);
        if (actives == null || actives.isEmpty()) {
            return;
        }

        ItemData active = actives.get(0);

        AssetManager.SpriteType st = parseSpriteType(active.getSpriteType());
        if (st == null) {
            return;
        }

        boolean[][] sprite = AssetManager.getInstance().getSprite(st);
        if (sprite == null) {
            return;
        }

        int tier = parseDropTier(active.getDropTier());

        Color uiColor = colorByTier(tier);
        drawSpriteFitBox(g2d, sprite, x, y, size, uiColor);
    }

    /**
     * helper: draw a sprite to size box
     */
    private void drawSpriteFitBox(Graphics2D g2d, boolean[][] sprite,
        int x, int y, int boxSize, Color color) {
        if (sprite == null) {
            return;
        }

        int h = sprite.length;
        int w = sprite[0].length;

        int px = Math.max(1, boxSize / Math.max(w, h));

        int startX = x + (boxSize - w * px) / 2;
        int startY = y + (boxSize - h * px) / 2;

        g2d.setColor(color != null ? color : Color.GREEN);

        for (int r = 0; r < h; r++) {
            for (int c = 0; c < w; c++) {
                if (sprite[r][c]) {
                    g2d.fillRect(startX + c * px, startY + r * px, px, px);
                }
            }
        }
    }

    private AssetManager.SpriteType parseSpriteType(String raw) {
        if (raw == null) {
            return null;
        }
        raw = raw.trim();

        // DEFAULT means "no sprite" for this item. We ignore it silently.
        if (raw.equalsIgnoreCase("DEFAULT")) {
            return null;
        }

        try {
            return AssetManager.SpriteType.valueOf(raw);
        } catch (IllegalArgumentException e) {

            // Print the error only once to avoid spamming logs every frame.
            if (!warnedSpriteTypes.contains(raw)) {
                warnedSpriteTypes.add(raw);
                System.err.println("[Renderer] Unknown SpriteType string: " + raw);
            }
            return null;
        }
    }

    private int parseDropTier(String raw) {
        if (raw == null) {
            return 0;
        }

        String t = raw.trim().toUpperCase();

        // First try numeric tier
        try {
            return Integer.parseInt(t);
        } catch (NumberFormatException ignored) {
            // Not numeric, fall through to named tiers
        }

        return switch (t) {
            case "COMMON" -> 0;
            case "UNCOMMON" -> 1;
            case "RARE" -> 2;
            case "EPIC" -> 3;
            case "LEGENDARY" -> 4;
            default -> 0;
        };
    }

    private Color colorByTier(int dropTier) {
        return switch (dropTier) {
            case 0 -> Color.WHITE;
            case 1 -> Color.GREEN;
            case 2 -> Color.BLUE;
            case 3 -> Color.MAGENTA;
            case 4 -> Color.ORANGE;
            default -> Color.WHITE;
        };
    }
}
