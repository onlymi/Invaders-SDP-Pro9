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
        // 1. 화면 제목 출력
        String screenTitle = "PLAYER " + playerIndex + " : CHOOSE YOUR SHIP";
        g.setColor(Color.GREEN);
        commonRenderer.drawCenteredBigString(g, screen, screenTitle, screen.getHeight() / 8);
        
        // 2. 모든 캐릭터 순회하며 그리기
        int characterTypeCount = characterSamples.length;
        
        for (int i = 0; i < characterTypeCount; i++) {
            GameCharacter currentCharacter = characterSamples[i];
            
            // 위치 보정: 캐릭터의 중심이 아닌 좌측 상단 좌표를 기준으로 그리기 때문에 계산 필요
            // GameCharacter의 positionX는 이미 생성 시 gap을 고려해 설정되어 있음
            int drawX = currentCharacter.getPositionX() - (currentCharacter.getWidth() / 2);
            int drawY = currentCharacter.getPositionY();
            
            if (i == selectedShipIndex) {
                // 선택된 캐릭터: 원본 색상 (Color.WHITE 또는 null)
                // EntityRenderer.drawEntity 메서드를 통해 그리기
                entityRenderer.drawEntity(g, currentCharacter, drawX, drawY, Color.WHITE);
            } else {
                // 선택되지 않은 캐릭터: 그림자/실루엣 처리 (Color.DARK_GRAY)
                // EntityRenderer에서 DARK_GRAY일 때 어두운 마스크를 씌우도록 구현되어 있음
                entityRenderer.drawEntity(g, currentCharacter, drawX, drawY, Color.DARK_GRAY);
            }
        }
        
        // 3. 선택된 캐릭터 정보 텍스트 출력
        String selectedCharacterName = CharacterType.values()[selectedShipIndex].toString();
        
        g.setColor(Color.WHITE);
        commonRenderer.drawCenteredRegularString(g, screen,
            " > " + selectedCharacterName + " < ", screen.getHeight() / 2 + 100);
        
        g.setColor(Color.GRAY);
        commonRenderer.drawCenteredRegularString(g, screen, "Press SPACE to Select",
            screen.getHeight() - 50);
    }
}
