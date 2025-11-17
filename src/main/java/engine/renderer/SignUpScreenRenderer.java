package engine.renderer;

import java.awt.Color;
import java.awt.FontMetrics;
import java.awt.Graphics;
import screen.Screen;

/**
 * Renders the Sign Up screen. Responsible for drawing all UI elements for the SignUpScreen.
 *
 * @author Seungju Yoon <yunseungju@gmail.com>
 */
public class SignUpScreenRenderer {
    
    /**
     * Common renderer for shared drawing functions.
     */
    private CommonRenderer commonRenderer;
    /**
     * Font properties.
     */
    private static FontMetrics fontMetrics;
    
    /**
     * Constructor for SignUpScreenRenderer.
     *
     * @param commonRenderer A CommonRenderer instance for shared drawing methods.
     */
    public SignUpScreenRenderer(CommonRenderer commonRenderer) {
        this.commonRenderer = commonRenderer;
    }
    
    /**
     * Draws the sign-up screen elements.
     *
     * @param graphics      Graphics context to draw on.
     * @param screen        Screen to draw on.
     * @param activeField   Currently selected field (0:ID, 1:PW, 2:Submit, 3:Back)
     * @param idInput       Current text in the ID field.
     * @param passwordInput Current text in the Password field.
     * @param message       A message to display (e.g., error or success).
     */
    @SuppressWarnings({"checkstyle:LeftCurly", "checkstyle:RightCurly"})
    public void draw(final Graphics graphics, final Screen screen,
        final int activeField, final String idInput,
        final String passwordInput, final String message) {
        
        // Draw background
        graphics.setColor(Color.BLACK);
        graphics.fillRect(0, 0, screen.getWidth(), screen.getHeight());
        
        // Draw Title
        graphics.setFont(commonRenderer.getFontBig());
        graphics.setColor(Color.GREEN);
        commonRenderer.drawCenteredBigString(graphics, screen, "Sign Up", screen.getHeight() / 4);
        
        graphics.setFont(commonRenderer.getFontRegular());
        fontMetrics = graphics.getFontMetrics(commonRenderer.getFontRegular());
        
        int boxWidth = 200;
        int boxHeight = 30;
        int fieldX = (screen.getWidth() - boxWidth) / 2;
        int idFieldY = screen.getHeight() / 2 - 50;
        int pwFieldY = screen.getHeight() / 2 + 20;
        
        // Draw ID Field
        graphics.setColor(Color.WHITE);
        graphics.drawString("ID:", fieldX - 100, idFieldY + (boxHeight / 2) + 5);
        graphics.drawRect(fieldX, idFieldY, boxWidth, boxHeight);
        graphics.drawString(idInput, fieldX + 5, idFieldY + (boxHeight / 2) + 5);
        if (activeField == 0) {
            graphics.setColor(Color.GREEN); // Highlight box
            graphics.drawRect(fieldX - 1, idFieldY - 1, boxWidth + 2, boxHeight + 2);
        }
        
        // Draw Password Field
        graphics.setColor(Color.WHITE);
        graphics.drawString("Password:", fieldX - 100, pwFieldY + (boxHeight / 2) + 5);
        graphics.drawRect(fieldX, pwFieldY, boxWidth, boxHeight);
        StringBuilder maskedPassword = new StringBuilder();
        for (int i = 0; i < passwordInput.length(); i++) {
            maskedPassword.append("*");
        }
        graphics.drawString(maskedPassword.toString(), fieldX + 5, pwFieldY + (boxHeight / 2) + 5);
        if (activeField == 1) {
            graphics.setColor(Color.GREEN); // Highlight box
            graphics.drawRect(fieldX - 1, pwFieldY - 1, boxWidth + 2, boxHeight + 2);
        }
        
        // Draw Buttons
        int buttonY = pwFieldY + 80;
        String submitString = "Submit";
        String backString = "Back";
        
        graphics.setColor(activeField == 2 ? Color.GREEN : Color.WHITE);
        commonRenderer.drawCenteredRegularString(graphics, screen, submitString, buttonY);
        
        graphics.setColor(activeField == 3 ? Color.GREEN : Color.WHITE);
        commonRenderer.drawCenteredRegularString(graphics, screen, backString, buttonY + 30);
        
        // Draw Message (if any)
        if (message != null) {
            graphics.setColor(Color.RED);
            commonRenderer.drawCenteredRegularString(graphics, screen, message, buttonY + 80);
        }
    }
}
