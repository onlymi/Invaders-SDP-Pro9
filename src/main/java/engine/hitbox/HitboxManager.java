package engine.hitbox;

import engine.AssetManager;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Rectangle;
import screen.Screen;

public class HitboxManager {
    
    private static HitboxManager instance;
    
    /**
     * Normal-sized font.
     */
    private final Font fontRegular;
    /**
     * Big sized font.
     */
    private final Font fontBig;
    /**
     * Font properties.
     */
    private static FontMetrics fontMetrics;
    
    private static final String BACK_LABEL = "< Back";
    private static final int VOLUME_HITBOX_OFFSET = 20;
    
    private HitboxManager() {
        this.fontRegular = AssetManager.getInstance().getFontRegular();
        this.fontBig = AssetManager.getInstance().getFontBig();
    }
    
    public static HitboxManager getInstance() {
        if (instance == null) {
            instance = new HitboxManager();
        }
        return instance;
    }
    
    /**
     * hitbox coordinate function
     *
     * @param g
     * @param screen
     * @return
     */
    public Rectangle[] getMenuHitboxes(Graphics g, final Screen screen) {
        if (fontMetrics == null) {
            g.setFont(this.fontRegular);
            fontMetrics = g.getFontMetrics(this.fontRegular);
        }
        
        final String[] buttons = {"Play", "Achievement", "High scores", "Settings", "Exit"};
        
        int baseY = screen.getHeight() / 3 * 2 - 20;
        int spacing = (int) (fontMetrics.getHeight() * 1.5);
        Rectangle[] boxes = new Rectangle[buttons.length];
        
        for (int i = 0; i < buttons.length; i++) {
            int baseline = baseY + spacing * i;
            boxes[i] = centeredStringBounds(g, screen, buttons[i], baseline);
        }
        
        return boxes;
    }
    
    public Rectangle[] getPlayModeSelectionMenuHitboxes(Graphics g, final Screen screen) {
        if (fontMetrics == null) {
            g.setFont(fontRegular);
            fontMetrics = g.getFontMetrics(fontRegular);
        }
        
        final String[] items = {"1 Player", "2 Players"};
        int baseY = screen.getHeight() / 2 - 20;
        Rectangle[] boxes = new Rectangle[items.length];
        
        for (int i = 0; i < items.length; i++) {
            int baselineY = baseY + fontMetrics.getHeight() * 3 * i;
            boxes[i] = centeredStringBounds(g, screen, items[i], baselineY);
        }
        
        return boxes;
    }
    
    public Rectangle getVolumeBarHitbox(final Screen screen) {
        int bar_startWidth = screen.getWidth() / 2;
        int bar_endWidth = screen.getWidth() - 40;
        int barHeight = screen.getHeight() * 3 / 10;
        
        int barThickness = 20;
        
        int centerY = barHeight + VOLUME_HITBOX_OFFSET;
        
        int x = bar_startWidth;
        int y = centerY - barThickness;
        int width = bar_endWidth - bar_startWidth;
        int height = barThickness * 2;
        
        return new Rectangle(x, y, width, height);
    }
    
    /**
     * hitbox for Back button
     */
    public Rectangle getBackButtonHitbox(Graphics g, final Screen screen) {
        g.setFont(fontRegular);
        fontMetrics = g.getFontMetrics(fontRegular);
        
        int margin = screen.getWidth() / 15;
        int ascent = fontMetrics.getAscent();
        int descent = fontMetrics.getDescent();
        int padTop = 2;
        
        int y = margin - padTop;
        int w = fontMetrics.stringWidth(BACK_LABEL);
        int h = ascent + descent + 25;
        
        return new Rectangle(margin, y, w, h);
    }
    
    /**
     * When a given string is aligned in the middle of the screen, the pixel area occupied by the
     * string is calculated as Rectangle and returned
     */
    public Rectangle centeredStringBounds(Graphics g, final Screen screen, final String string,
        final int baselineY) {
        g.setFont(fontRegular);
        fontMetrics = g.getFontMetrics(fontRegular);
        // Variables for hitbox fine-tuning
        int menuHitboxOffset = 20;
        
        final int pad = 4;
        int textWidth = fontMetrics.stringWidth(string);
        int ascent = fontMetrics.getAscent();
        int descent = fontMetrics.getDescent();
        
        int x = screen.getWidth() / 2 - textWidth / 2;
        int y = baselineY - ascent + menuHitboxOffset - pad / 2;
        int h = ascent + descent + pad;
        
        return new Rectangle(x, y, textWidth, h);
    }
}
