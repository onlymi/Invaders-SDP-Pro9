package engine.renderer;

import animations.MenuSpace;
import engine.AssetManager;
import engine.AssetManager.SpriteType;
import java.awt.Color;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import screen.Screen;

public class TitleScreenRenderer {
    
    private CommonRenderer commonRenderer;
    private AssetManager assetManager;
    /**
     * Font properties.
     */
    private static FontMetrics fontMetrics;
    
    
    public TitleScreenRenderer(CommonRenderer commonRenderer) {
        this.commonRenderer = commonRenderer;
        this.assetManager = AssetManager.getInstance();
    }
    
    /**
     * Draws game title.
     *
     * @param screen Screen to draw on.
     */
    public void drawTitle(Graphics g, final Screen screen) {
        String instructionsString = "select with w+s / arrows, confirm with space";
        
        BufferedImage bgImage = assetManager.getSpriteImage(SpriteType.BackgroundTitle);
        if (bgImage != null) {
            g.drawImage(bgImage, 0, 0, screen.getWidth(), screen.getHeight(), null);
        } else {
            g.setColor(Color.BLACK);
            g.fillRect(0, 0, screen.getWidth(), screen.getHeight());
        }
        
        g.setColor(new Color(0, 0, 0, 150));
        
        int boxWidth = screen.getWidth() * 2 / 5;
        int boxHeight = screen.getHeight() * 3 / 5;
        int boxX = (screen.getWidth() - boxWidth) / 2;
        int boxY = (screen.getHeight() - boxHeight) * 4 / 5;
        
        g.fillRoundRect(boxX, boxY, boxWidth, boxHeight, 30, 30);
        
        int centerY = boxY + boxHeight / 2;
        
        g.setColor(Color.GRAY);
        commonRenderer.drawCenteredRegularString(g, screen, instructionsString,
            centerY - 100);
    }
    
    /**
     * Draws main menu. - remodified for 2P mode, using string array for efficiency
     *
     * @param screen        Screen to draw on.
     * @param selectedIndex Option selected.
     */
    public void drawMenu(Graphics g, final Screen screen, final int option,
        final Integer hoverOption, final int selectedIndex) {
        g.setFont(commonRenderer.getFontRegular());
        fontMetrics = g.getFontMetrics(commonRenderer.getFontRegular());
        
        String[] items = {"Play", "Store", "Achievements", "High scores", "Settings", "Logout",
            "Exit"};
        
        int baseY =
            screen.getHeight() / 3 * 2 - 60; // Adjust spacing due to high society button addition
        int spacing = (int) (fontMetrics.getHeight() * 1.5);
        for (int i = 0; i < items.length; i++) {
            boolean highlight = (hoverOption != null) ? (i == hoverOption) : (i == selectedIndex);
            g.setColor(highlight ? Color.GREEN : Color.WHITE);
            commonRenderer.drawCenteredRegularString(g, screen, items[i], baseY + spacing * i);
        }
    }
    
    public void menuHover(MenuSpace menuSpace, final int state) {
        menuSpace.setColor(state);
        menuSpace.setSpeed(state == 4);
    }
}
