package engine.renderer;

import entity.character.CharacterType;
import entity.character.GameCharacter;
import java.awt.Color;
import java.awt.Graphics;
import screen.Screen;

public class PlayerSelectionScreenRenderer {
    
    private final CommonRenderer commonRenderer;
    private final EntityRenderer entityRenderer;
    
    public PlayerSelectionScreenRenderer(CommonRenderer commonRenderer) {
        this.commonRenderer = commonRenderer;
        this.entityRenderer = new EntityRenderer(commonRenderer);
    }
    
    public void drawPlayerSelectionMenu(Graphics g, final Screen screen,
        final GameCharacter[] characterSamples, final int selectedShipIndex,
        final int playerIndex) {
        // 화면 제목 출력
        String screenTitle = "PLAYER " + playerIndex + " : CHOOSE YOUR PLAYER";
        g.setColor(Color.GREEN);
        commonRenderer.drawCenteredBigString(g, screen, screenTitle, screen.getHeight() / 8);
        
        // 모든 캐릭터 그리기
        int characterTypeCount = characterSamples.length;
        
        for (int i = 0; i < characterTypeCount; i++) {
            GameCharacter currentCharacter = characterSamples[i];
            currentCharacter.isInSelectScreen = true;
            
            int imageScale = 2;
            
            int drawX =
                currentCharacter.getPositionX() - currentCharacter.getWidth();
            int drawY = currentCharacter.getPositionY();
            
            if (i == selectedShipIndex) {
                entityRenderer.drawEntityByScale(g, currentCharacter, drawX, drawY, Color.WHITE,
                    imageScale);
            } else {
                entityRenderer.drawEntityByScale(g, currentCharacter, drawX, drawY, Color.DARK_GRAY,
                    imageScale);
            }
        }
        
        // 선택된 캐릭터 정보 텍스트 출력
        String selectedCharacterName = CharacterType.values()[selectedShipIndex].toString();
        
        g.setColor(Color.WHITE);
        commonRenderer.drawCenteredRegularString(g, screen,
            " > " + selectedCharacterName + " < ", screen.getHeight() / 3);
        
        g.setColor(Color.GRAY);
        commonRenderer.drawCenteredRegularString(g, screen, "Press SPACE to Select",
            screen.getHeight() - 50);
    }
}
