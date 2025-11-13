package engine.renderer;

import engine.AssetManager;
import java.awt.Color;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.RadialGradientPaint;
import java.awt.RenderingHints;
import screen.Screen;

/**
 * Renders the Authentication screen. Responsible for drawing all UI elements for the AuthScreen.
 *
 * @author Seungju Yoon <ysj5450@hanyang.ac.kr>
 */
public class AuthScreenRenderer {
    
    /**
     * Common renderer for shared drawing functions.
     */
    private CommonRenderer commonRenderer;
    /**
     * Asset manager for loading resources.
     */
    private AssetManager assetManager;
    /**
     * Font properties.
     */
    private static FontMetrics fontMetrics;
    
    /**
     * Menu background star animation.
     */
    private animations.MenuSpace menuSpace = new animations.MenuSpace(50);
    
    /**
     * Constructor for AuthScreenRenderer.
     *
     * @param commonRenderer A CommonRenderer instance for shared drawing methods.
     */
    public AuthScreenRenderer(CommonRenderer commonRenderer) {
        this.commonRenderer = commonRenderer;
        this.assetManager = AssetManager.getInstance();
    }
    
    /**
     * Draws the game title.
     *
     * @param graphics Graphics context to draw on.
     * @param screen   Screen to draw on.
     */
    public void drawTitle(Graphics graphics, final Screen screen) {
        String titleString = "Invaders";
        String instructionsString = "select with w+s / arrows, confirm with space";
        
        graphics.setColor(Color.GRAY);
        commonRenderer.drawCenteredRegularString(graphics, screen,
            instructionsString, screen.getHeight() / 2);
        
        graphics.setColor(Color.GREEN);
        commonRenderer.drawCenteredBigString(graphics, screen,
            titleString, screen.getHeight() / 3);
    }
    
    /**
     * Draws the main menu stars background animation.
     *
     * @param graphics Graphics context to draw on.
     */
    public void updateMenuSpace(Graphics graphics) {
        menuSpace.updateStars();
        
        Graphics2D g2d = (Graphics2D) graphics;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        g2d.setColor(Color.WHITE);
        int[][] positions = menuSpace.getStarLocations();
        
        for (int i = 0; i < menuSpace.getNumStars(); i++) {
            int size = 1;
            int radius = size * 2;
            
            float[] dist = {0.0f, 1.0f};
            Color[] colors = {
                menuSpace.getColor(),
                new Color(255, 255, 200, 0)
            };
            
            RadialGradientPaint paint = new RadialGradientPaint(
                new Point(positions[i][0], positions[i][1]),
                radius,
                dist,
                colors);
            g2d.setPaint(paint);
            g2d.fillOval(positions[i][0] - radius / 2, positions[i][1] - radius / 2,
                radius, radius);
            
            g2d.fillOval(positions[i][0], positions[i][1], size, size);
        }
    }
    
    /**
     * Draws the authentication menu (Log In / Sign Up).
     *
     * @param graphics      Graphics context to draw on.
     * @param screen        Screen to draw on.
     * @param option        Currently selected keyboard option.
     * @param hoverOption   Currently hovered mouse option.
     * @param selectedIndex Final selected index.
     */
    public void drawMenu(Graphics graphics, final Screen screen, final int option,
        final Integer hoverOption, final int selectedIndex) {
        graphics.setFont(commonRenderer.getFontRegular());
        fontMetrics = graphics.getFontMetrics(commonRenderer.getFontRegular());
        
        String[] items = {"Log In", "Sign Up"}; // 2개 버튼
        
        int baseY = screen.getHeight() / 3 * 2;
        int spacing = (int) (fontMetrics.getHeight() * 1.5);
        for (int i = 0; i < items.length; i++) {
            boolean highlight = (hoverOption != null) ? (i == hoverOption) : (i == selectedIndex);
            graphics.setColor(highlight ? Color.GREEN : Color.WHITE);
            commonRenderer.drawCenteredRegularString(graphics, screen,
                items[i], baseY + spacing * i);
        }
    }
    
    /**
     * Changes the color of the menu background animation based on the selected item.
     *
     * @param state The index of the selected menu item.
     */
    public void menuHover(final int state) {
        menuSpace.setColor(state);
        menuSpace.setSpeed(state == 4); // state == 4 is Exit, but we only have 0 and 1.
    }
}