package engine.renderer;

import engine.gameplay.achievement.Achievement;
import java.awt.Color;
import java.awt.Graphics;
import java.util.ArrayList;
import java.util.List;
import screen.Screen;

public class AchievementScreenRenderer {
    
    private CommonRenderer commonRenderer;
    
    public AchievementScreenRenderer(CommonRenderer commonRenderer) {
        this.commonRenderer = commonRenderer;
    }
    
    /**
     * Draws high scores.
     *
     * @param screen    Screen to draw on.
     * @param completer List of completer
     */
    public void drawAchievementMenu(Graphics g, final Screen screen, Achievement achievement,
        List<String> completer) {
        String achievementsTitle = "Achievements";
        String instructionString = "Press ESC to return";
        String playerModeString = "              1P                                      2P              ";
        String prevNextString = "PREV                                                              NEXT";
        String achievementName = achievement.getName();
        String descriptionString = achievement.getDescription();
        
        g.setFont(commonRenderer.getFontRegular());
        // Draw the title, achievement name, and description
        g.setColor(Color.GREEN);
        commonRenderer.drawCenteredBigString(g, screen, achievementsTitle, screen.getHeight() / 10);
        commonRenderer.drawCenteredRegularString(g, screen, achievementName,
            screen.getHeight() / 7);
        g.setColor(Color.GRAY);
        commonRenderer.drawCenteredRegularString(g, screen, descriptionString,
            screen.getHeight() / 5);
        g.setColor(Color.GREEN);
        commonRenderer.drawCenteredRegularString(g, screen, playerModeString,
            screen.getHeight() / 4);
        g.setColor(Color.GRAY);
        commonRenderer.drawCenteredRegularString(g, screen, instructionString,
            (int) (screen.getHeight() * 0.9));
        
        // Starting Y position for player names
        int startY = (int) (screen.getHeight() * 0.3);
        int lineHeight = 25;
        
        // X positions for the 1P and 2P columns
        int leftX = screen.getWidth() / 4;      // 1P column
        int rightX = screen.getWidth() * 2 / 3; // 2P column
        
        List<String> team1 = new ArrayList<>();
        List<String> team2 = new ArrayList<>();
        
        // Separate completer into 1P and 2P teams based on the mode prefix
        if (completer != null && !completer.isEmpty()) {
            for (String entry : completer) {
                String[] parts = entry.split(":");
                if (parts.length == 2) {
                    String modeString = parts[0].trim(); // e.g., "2P"
                    String numericPart = modeString.replaceAll("[^0-9]",
                        ""); // Extract numeric part: "2"
                    int mode = Integer.parseInt(numericPart);
                    String name = parts[1].trim();
                    if (mode == 1) {
                        team1.add(name);
                    } else if (mode == 2) {
                        team2.add(name);
                    }
                }
            }
            
            // Draw names in each column, up to the max number of lines
            int maxLines = Math.max(team1.size(), team2.size());
            for (int i = 0; i < maxLines; i++) {
                int y = startY + i * lineHeight;
                if (i < team1.size()) {
                    g.setColor(Color.WHITE);
                    g.drawString(team1.get(i), leftX, y);
                }
                if (i < team2.size()) {
                    g.setColor(Color.WHITE);
                    g.drawString(team2.get(i), rightX, y);
                }
            }
            
        } else {
            // Display placeholder text if no achievers were found
            g.setColor(Color.GREEN);
            commonRenderer.drawCenteredBigString(g, screen, "No achievers found.",
                (int) (screen.getHeight() * 0.5));
        }
        
        // Draw prev/next navigation buttons at the bottom
        g.setColor(Color.GREEN);
        commonRenderer.drawCenteredRegularString(g, screen, prevNextString,
            (int) (screen.getHeight() * 0.8));
        
        // Draw back button in the top-left corner
        commonRenderer.drawBackButton(g, screen, false);
    }
}
