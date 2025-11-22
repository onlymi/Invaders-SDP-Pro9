package engine.renderer;

import entity.character.CharacterType;
import entity.character.GameCharacter;
import java.awt.Color;
import java.awt.Graphics;
import screen.Screen;

public class PlayerSelectionMenuRenderer {
    
    private final CommonRenderer commonRenderer;
    private final EntityRenderer entityRenderer;
    
    public PlayerSelectionMenuRenderer(CommonRenderer commonRenderer) {
        this.commonRenderer = commonRenderer;
        this.entityRenderer = new EntityRenderer(commonRenderer);
    }
    
    public void drawPlayerSelectionMenu(Graphics g, final Screen screen,
        final GameCharacter[] characterSamples, final int selectedShipIndex,
        final int playerIndex) {
        GameCharacter character = characterSamples[selectedShipIndex];
        int characterTypeCount = CharacterType.values().length;
        int centerX = character.getPositionX();
        
        String screenTitle = "PLAYER " + playerIndex + " : CHOOSE YOUR SHIP";
        
        // Ship Type Info
        String[] characterNames = new String[characterTypeCount];
        
        for (int i = 0; i < characterTypeCount; i++) {
            characterNames[i] = CharacterType.values()[i].toString();
            entityRenderer.drawEntity(g, character,
                character.getPositionX() - character.getWidth() / 2, character.getPositionY());
        }
        
        // Draw Selected Player Page Title
        g.setColor(Color.GREEN);
        commonRenderer.drawCenteredBigString(g, screen, screenTitle, screen.getHeight() / 4);
        // Draw Selected Player Ship Type
        g.setColor(Color.white);
        commonRenderer.drawCenteredRegularString(g, screen,
            " > " + characterNames[selectedShipIndex] + " < ", screen.getHeight() / 2 - 40);
        
        g.setColor(Color.GRAY);
        commonRenderer.drawCenteredRegularString(g, screen, "Press SPACE to Select",
            screen.getHeight() - 50);
    }
}
