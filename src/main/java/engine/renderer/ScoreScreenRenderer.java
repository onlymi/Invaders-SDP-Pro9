package engine.renderer;

import java.awt.Color;
import java.awt.FontMetrics;
import java.awt.Graphics;
import screen.Screen;

public class ScoreScreenRenderer {
    
    private CommonRenderer commonRenderer;
    /**
     * Font properties.
     */
    private static FontMetrics fontMetrics;
    
    public ScoreScreenRenderer(CommonRenderer commonRenderer) {
        this.commonRenderer = commonRenderer;
    }
    
    /**
     * Draws basic content of game over screen.
     *
     * @param screen       Screen to draw on.
     * @param acceptsInput If the screen accepts input.
     */
    public void drawGameOver(Graphics g, final Screen screen, final boolean acceptsInput) {
        String gameOverString = "Game Over";
        String continueOrExitString = "Press Space to play again, Escape to exit";
        
        int height = 4;
        
        g.setFont(commonRenderer.getFontRegular());
        fontMetrics = g.getFontMetrics(commonRenderer.getFontRegular());
        g.setColor(Color.GREEN);
        commonRenderer.drawCenteredBigString(g, screen, gameOverString,
            screen.getHeight() / height - fontMetrics.getHeight() * 2);
        
        if (acceptsInput) {
            g.setColor(Color.GREEN);
        } else {
            g.setColor(Color.GRAY);
        }
        commonRenderer.drawCenteredRegularString(g, screen, continueOrExitString,
            screen.getHeight() / 2 + fontMetrics.getHeight() * 10);
    }
    
    /**
     * Draws interactive characters for name input.
     *
     * @param screen Screen to draw on.
     * @param name   Current name inserted.
     */
    public void drawNameInput(Graphics g, final Screen screen, final StringBuilder name,
        boolean isNewRecord) {
        g.setFont(commonRenderer.getFontRegular());
        fontMetrics = g.getFontMetrics(commonRenderer.getFontRegular());
        
        String newRecordString = "New Record!";
        String introduceNameString = "Name: ";
        String nameStr = name.toString();
        
        if (isNewRecord) {
            g.setColor(Color.GREEN);
            commonRenderer.drawCenteredRegularString(g, screen, newRecordString,
                screen.getHeight() / 4 + fontMetrics.getHeight() * 11);
        }
        
        // Draw the current name with blinking cursor
        String displayName = name.isEmpty() ? "" : nameStr;
        
        // Cursor blinks every 500ms
        boolean showCursor = (System.currentTimeMillis() / 500) % 2 == 0;
        String cursor = showCursor ? "|" : " ";
        
        String displayText = introduceNameString + displayName + cursor;
        
        g.setColor(Color.WHITE);
        commonRenderer.drawCenteredRegularString(g, screen, displayText,
            screen.getHeight() / 4 + fontMetrics.getHeight() * 12);
    }
    
    public void drawNameInputError(Graphics g, Screen screen) {
        g.setFont(commonRenderer.getFontRegular());
        
        String alert = "Enter at least 3 chars!"; // "Name too short!"
        
        g.setColor(Color.YELLOW);
        commonRenderer.drawCenteredRegularString(g, screen, alert,
            screen.getHeight() / 4 + fontMetrics.getHeight() * 13);
    }
    
    /**
     * Draws game results.
     *
     * @param screen         Screen to draw on.
     * @param score          Score obtained.
     * @param coins          Coins obtained.
     * @param shipsDestroyed Total ships destroyed.
     * @param accuracy       Total accuracy.
     * @param isNewRecord    If the score is a new high score.
     */
    public void drawResults(Graphics g, final Screen screen, final int score,
        final int coins, final int livesRemaining, final int shipsDestroyed,
        final float accuracy, final boolean isNewRecord, final boolean accuracy1P) {
        g.setFont(commonRenderer.getFontRegular());
        
        String scoreString = String.format("score %04d", score);
        String coinString = String.format("coins %04d", coins);
        String livesRemainingString = String.format("lives remaining %d", livesRemaining);
        String shipsDestroyedString = "enemies destroyed " + shipsDestroyed;
        String accuracyString = String.format("accuracy %.2f%%",
            Float.isNaN(accuracy) ? 0.0 : accuracy * 100);
        
        int height = 4;
        
        if (isNewRecord) {
            g.setColor(Color.RED);
        } else {
            g.setColor(Color.WHITE);
        }
        
        fontMetrics = g.getFontMetrics(commonRenderer.getFontRegular());
        
        commonRenderer.drawCenteredRegularString(g, screen, scoreString,
            screen.getHeight() / height);
        commonRenderer.drawCenteredRegularString(g, screen, coinString,
            screen.getHeight() / height + fontMetrics.getHeight() * 2);
        commonRenderer.drawCenteredRegularString(g, screen, livesRemainingString,
            screen.getHeight() / height + fontMetrics.getHeight() * 4);
        commonRenderer.drawCenteredRegularString(g, screen, shipsDestroyedString,
            screen.getHeight() / height + fontMetrics.getHeight() * 6);
        // Draw accuracy for player in 1P mode
        if (accuracy1P) {
            commonRenderer.drawCenteredRegularString(g, screen, accuracyString,
                screen.getHeight() / height + fontMetrics.getHeight() * 8);
        }
    }
    
}
