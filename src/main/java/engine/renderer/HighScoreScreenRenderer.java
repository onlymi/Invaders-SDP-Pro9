package engine.renderer;

import engine.Score;
import java.awt.Color;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.util.List;
import screen.Screen;

public class HighScoreScreenRenderer {
    
    CommonRenderer commonRenderer;
    FontMetrics fontMetrics;
    
    public HighScoreScreenRenderer(CommonRenderer commonRenderer) {
        this.commonRenderer = commonRenderer;
    }
    
    /**
     * Draws high score screen title and instructions.
     *
     * @param screen Screen to draw on.
     */
    public void drawHighScoreMenu(Graphics g, final Screen screen) {
        String highScoreString = "High Scores";
        String instructionsString = "Press ESC to return";
        
        int midX = screen.getWidth() / 2;
        int startY = screen.getHeight() / 3;
        
        g.setColor(Color.GREEN);
        commonRenderer.drawCenteredBigString(g, screen, highScoreString, screen.getHeight() / 8);
        
        g.setColor(Color.GRAY);
        commonRenderer.drawCenteredRegularString(g, screen, instructionsString,
            screen.getHeight() / 5);
        
        g.setColor(Color.GREEN);
        fontMetrics = g.getFontMetrics(commonRenderer.getFontBig());
        g.drawString("1-PLAYER MODE", midX / 2 - fontMetrics.stringWidth("1-PLAYER MODE") / 2 + 40,
            startY);
        g.drawString("2-PLAYER MODE",
            midX + midX / 2 - fontMetrics.stringWidth("2-PLAYER MODE") / 2 + 40, startY);
        
        // draw back button at top-left
        commonRenderer.drawBackButton(g, screen, false);
    }
    
    /**
     * Draws high scores.
     *
     * @param screen     Screen to draw on.
     * @param highScores List of high scores.
     */
    public void drawHighScores(Graphics g, final Screen screen, final List<Score> highScores,
        final String mode) {
        g.setColor(Color.WHITE);
        int i = 0;
        String scoreString = "";
        
        int midX = screen.getWidth() / 2;
        fontMetrics = g.getFontMetrics(commonRenderer.getFontBig());
        int startY = screen.getHeight() / 3 + fontMetrics.getHeight() + 20;
        fontMetrics = g.getFontMetrics(commonRenderer.getFontRegular());
        int lineHeight = fontMetrics.getHeight() + 5;
        
        for (Score score : highScores) {
            scoreString = String.format("%s        %04d", score.getName(), score.getScore());
            int x;
            if (mode.equals("1P")) {
                // Left column(1P)
                x = midX / 2 - fontMetrics.stringWidth(scoreString) / 2;
            } else {
                // Right column(2P)
                x = midX + midX / 2 - fontMetrics.stringWidth(scoreString) / 2;
            }
            g.drawString(scoreString, x, startY + lineHeight * i);
            i++;
        }
    }
    
    public void drawNewHighScoreNotice(final Screen screen) {
        String message = "NEW HIGH SCORE!";
        // backBufferGraphics.setColor(Color.YELLOW);
        // drawCenteredBigString(screen, message, screen.getHeight() / 4);
    }
}
