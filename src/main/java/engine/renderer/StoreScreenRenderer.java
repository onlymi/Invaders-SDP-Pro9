package engine.renderer;

import engine.UserStats;
import java.awt.Color;
import java.awt.Graphics;
import screen.Screen;
import screen.StoreScreen;

public class StoreScreenRenderer {
    
    private CommonRenderer commonRenderer;
    private final String[] statNames = {"Health", "Mana", "Speed", "Damage", "Atk Speed",
        "Atk Range", "Critical Chance", "Defence"};
    
    private final String[] descriptions = {
        "Increases Max HP", "Increases Max Mana", "Increases Movement Speed",
        "Increases Physical Damage", "Increases Attack Speed", "Increases Attack Range",
        "Increases Critical Chance", "Increases Physical Defence"
    };
    
    public StoreScreenRenderer(CommonRenderer commonRenderer) {
        this.commonRenderer = commonRenderer;
    }
    
    public void draw(Graphics g, Screen screen, UserStats userStats, int menuIndex) {
        int width = screen.getWidth();
        int height = screen.getHeight();
        StoreScreen storeScreen = (StoreScreen) screen;
        
        // Title
        g.setColor(Color.GREEN);
        commonRenderer.drawCenteredBigString(g, screen, "UPGRADE STORE", height / 8);
        
        // 현재 Coin
        g.setColor(Color.YELLOW);
        commonRenderer.drawCenteredRegularString(g, screen, "COINS: " + userStats.getCoin(),
            height / 5);
        
        // 스탯 선택 격자 레이아웃
        int startX = width / 10;
        int startY = height / 3;
        int boxWidth = 200;
        int boxHeight = 120;
        int gapX = 50;
        int gapY = 50;
        
        for (int i = 0; i < statNames.length; i++) {
            int row = i / (statNames.length / 2);
            int col = i % (statNames.length / 2);
            int x = startX + col * (boxWidth + gapX);
            int y = startY + row * (boxHeight + gapY);
            drawItemBox(g, x, y, boxWidth, boxHeight, i, userStats, i == menuIndex, storeScreen);
        }
        
        g.setColor(Color.GRAY);
        commonRenderer.drawCenteredRegularString(g, screen, descriptions[menuIndex], height - 150);
        commonRenderer.drawCenteredRegularString(g, screen, "Space or Enter: Buy    |   ESC: Exit",
            height - 50);
    }
    
    private void drawItemBox(Graphics g, int x, int y, int w, int h, int index, UserStats userStats,
        boolean selected, StoreScreen storeScreen) {
        if (selected) {
            g.setColor(Color.GREEN);
        } else {
            g.setColor(Color.WHITE);
        }
        g.drawRect(x, y, w, h);
        int maxLevel = storeScreen.getMaxStatLevel();
        int currentLevel = userStats.getStatLevel(index);
        int cost = storeScreen.getBaseCost(currentLevel);
        
        g.drawString(statNames[index], x + 20, y + 30);
        
        // Draw Level Circles
        for (int k = 0; k < maxLevel; k++) {
            if (k < currentLevel) {
                g.setColor(Color.YELLOW); // Purchased
            } else {
                g.setColor(Color.GRAY); // Empty
            }
            g.fillOval(x + 20 + (k * 25), y + 50, 15, 15);
        }
        
        // Cost
        if (currentLevel < maxLevel) {
            if (selected) {
                g.setColor(Color.GREEN);
            } else {
                g.setColor(Color.WHITE);
            }
            g.drawString("Cost: " + cost, x + 20, y + 90);
        } else {
            g.setColor(Color.RED);
            g.drawString("MAX LEVEL", x + 20, y + 90);
        }
    }
}
