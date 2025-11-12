package engine.renderer;

import engine.AssetManager;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import screen.Screen;

/**
 * 공통적으로 사용되는 그리기 유틸리티 클래스
 */
public class CommonRenderer {
    
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
    
    public CommonRenderer() {
        fontRegular = AssetManager.getInstance().getFontRegular();
        fontBig = AssetManager.getInstance().getFontBig();
    }
    
    public Font getFontRegular() {
        return this.fontRegular;
    }
    
    public Font getFontBig() {
        return this.fontBig;
    }
    
    /**
     * Draws a centered string on regular font.
     *
     * @param screen Screen to draw on.
     * @param string String to draw.
     * @param height Height of the drawing.
     */
    public void drawCenteredRegularString(Graphics g, final Screen screen, final String string,
        final int height) {
        g.setFont(fontRegular);
        fontMetrics = g.getFontMetrics();
        g.drawString(string, screen.getWidth() / 2 - fontMetrics.stringWidth(string) / 2, height);
    }
    
    /**
     * Draws a centered string on regular font at a specific coordinate.
     *
     * @param string String to draw.
     * @param x      X coordinate to center the string on.
     * @param y      Y coordinate of the drawing.
     */
    public void drawCenteredRegularString(Graphics g, final String string, final int x,
        final int y) {
        g.setFont(fontRegular);
        fontMetrics = g.getFontMetrics(fontRegular);
        g.drawString(string, x - fontMetrics.stringWidth(string) / 2, y);
    }
    
    /**
     * Draws a centered string on big font.
     *
     * @param screen Screen to draw on.
     * @param string String to draw.
     * @param height Height of the drawing.
     */
    public void drawCenteredBigString(Graphics g, final Screen screen, final String string,
        final int height) {
        g.setFont(fontBig);
        fontMetrics = g.getFontMetrics(fontBig);
        g.drawString(string, screen.getWidth() / 2 - fontMetrics.stringWidth(string) / 2, height);
    }
    
    /**
     * Draws a centered string on big font at a specific coordinate.
     *
     * @param string String to draw.
     * @param x      X coordinate to center the string on.
     * @param y      Y coordinate of the drawing.
     */
    public void drawCenteredBigString(Graphics g, final String string, final int x, final int y) {
        g.setFont(fontBig);
        fontMetrics = g.getFontMetrics(fontBig);
        g.drawString(string, x - fontMetrics.stringWidth(string) / 2, y);
    }
    
    // Draw a "BACK_LABEL" button in the top-left corner.
    public void drawBackButton(Graphics g, final Screen screen, final boolean highlighted) {
        g.setFont(fontRegular);
        fontMetrics = g.getFontMetrics(fontRegular);
        
        g.setColor(highlighted ? Color.GREEN : Color.WHITE);
        int margin = screen.getWidth() / 15;
        int ascent = fontMetrics.getAscent();
        g.drawString(BACK_LABEL, margin, margin + ascent);
    }
    
    public void drawPauseOverlay(Graphics g, final Screen screen) {
        g.setColor(new Color(0, 0, 0, 200));
        g.fillRect(0, 0, screen.getWidth(), screen.getHeight());
        
        String pauseString = "PAUSED";
        g.setFont(this.fontBig);
        g.setColor(Color.WHITE);
        drawCenteredBigString(g, screen, pauseString, screen.getHeight() / 2);
        
        String returnMenu = "PRESS BACKSPACE TO RETURN TO TITLE";
        g.setFont(this.fontRegular);
        g.setColor(Color.WHITE);
        drawCenteredRegularString(g, screen, returnMenu, screen.getHeight() - 50);
    }
    
    /**
     * Draws a thick line from side to side of the screen.
     *
     * @param screen    Screen to draw on.
     * @param positionY Y coordinate of the line.
     */
    public void drawHorizontalLine(Graphics g, final Screen screen, final int positionY) {
        g.setColor(Color.GREEN);
        g.drawLine(0, positionY, screen.getWidth(), positionY);
        g.drawLine(0, positionY + 1, screen.getWidth(), positionY + 1);
    }
}
