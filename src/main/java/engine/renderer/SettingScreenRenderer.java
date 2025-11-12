package engine.renderer;

import engine.AssetManager;
import engine.Core;
import engine.DrawManager;
import engine.InputManager;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Insets;
import java.awt.event.KeyEvent;
import screen.Screen;

public class SettingScreenRenderer {
    
    CommonRenderer commonRenderer;
    
    public SettingScreenRenderer(CommonRenderer commonRenderer) {
        this.commonRenderer = commonRenderer;
    }
    
    public void drawSettingLayout(Graphics g, final Screen screen, final String[] menuItems,
        final int selectedMenuItems) {
        Font font = AssetManager.getInstance().getFontRegular();
        g.setFont(font);
        
        int splitPointX = screen.getWidth() * 3 / 10;
        int menuY = screen.getHeight() * 3 / 10;
        for (int i = 0; i < menuItems.length; i++) {
            if (i == selectedMenuItems) {
                g.setColor(Color.GREEN);
            } else {
                g.setColor(Color.WHITE);
            }
            g.drawString(menuItems[i], 30, menuY + (i * 60));
            g.setColor(Color.GREEN);
        }
        g.drawLine(splitPointX, screen.getHeight() / 4, splitPointX,
            (menuY + menuItems.length * 60));
    }
    
    public void drawVolumeBar(Graphics g, final Screen screen, final int volumeLevel,
        final boolean dragging) {
        Font font = AssetManager.getInstance().getFontRegular();
        
        int bar_startWidth = screen.getWidth() / 2;
        int bar_endWidth = screen.getWidth() - 40;
        int barHeight = screen.getHeight() * 3 / 10;
        
        String volume_label = "Volume";
        
        g.setFont(font);
        g.setColor(Color.WHITE);
        g.drawLine(bar_startWidth, barHeight, bar_endWidth, barHeight);
        
        g.setColor(Color.WHITE);
        g.drawString(volume_label, bar_startWidth - 80, barHeight + 7);

//		change this line to get indicator center position
        int size = 14;
        double ratio = volumeLevel / 100.0;
        int centerX = bar_startWidth + (int) ((bar_endWidth - bar_startWidth) * ratio);
        int indicatorX = centerX - size / 2 - 3;
        int indicatorY = barHeight - size / 2;
        
        int rawX = Core.getInputManager().getMouseX();
        int rawY = Core.getInputManager().getMouseY();
        Insets insets = DrawManager.getFrame().getInsets();
        int mouseX = rawX - insets.left;
        int mouseY = rawY - insets.top;
        
        boolean hoverIndicator = mouseX >= indicatorX && mouseX <= indicatorX + size &&
            mouseY >= indicatorY && mouseY <= indicatorY + size;
        
        if (hoverIndicator || dragging) {
            g.setColor(Color.GREEN);
        } else {
            g.setColor(Color.WHITE);
        }
        
        g.fillRect(indicatorX, indicatorY, size, size);
        
        g.setColor(Color.WHITE);
        String volumeText = Integer.toString(volumeLevel);
        g.drawString(volumeText, bar_endWidth + 10, barHeight + 7);
        
    }
    
    public void drawSettingMenu(Graphics g, final Screen screen) {
        Font font = AssetManager.getInstance().getFontRegular();
        g.setFont(font);
        FontMetrics metrics = g.getFontMetrics(font);
        
        String settingsString = "Settings";
        String instructionsString = "Press ESC to return";
        
        g.setColor(Color.GREEN);
        commonRenderer.drawCenteredBigString(g, screen, settingsString, screen.getHeight() / 8);
        g.setFont(font);
        g.setColor(Color.GRAY);
        commonRenderer.drawCenteredRegularString(g, screen, instructionsString,
            screen.getHeight() / 6);
    }
    
    public void drawKeySettings(Graphics g, final Screen screen, int playerNum, int selectedSection,
        int selectedKeyIndex, boolean[] keySelected, int[] currentKeys) {
        int panelWidth = 220;
        int panelHeight = 180;
        int x = screen.getWidth() - panelWidth - 50;
        int y = screen.getHeight() / 4;
        
        String[] labels = {"MOVE LEFT :", "MOVE RIGHT:", "ATTACK :"};
        String[] keys = new String[3];
        String[] keys_string = new String[3];
        
        for (int i = 0; i < labels.length; i++) {
            int textY = y + 70 + (i * 50);
            keys[i] = KeyEvent.getKeyText(currentKeys[i]); // Convert set key codes to characters
            keys_string[i] = InputManager.getInstance().getKeyString(currentKeys[i]);
            // draw the dividing line
            if (i < labels.length - 1) {
                g.setColor(Color.DARK_GRAY);
                g.drawLine(x + 20, textY + 20, x + panelWidth - 20, textY + 20);
            }
            
            // Verify that the current item is in key selection (waiting) status and select color
            if (keySelected[i]) {
                g.setColor(Color.YELLOW);
            } else if (selectedSection == 1 && selectedKeyIndex == i) {
                g.setColor(Color.GREEN);
            } else {
                g.setColor(Color.LIGHT_GRAY);
            }
            // draw key
            g.drawString(labels[i], x + 30, textY);
            g.setColor(Color.WHITE);
            g.drawString(keys_string[i], x + 150, textY);
        }
        
    }
}
