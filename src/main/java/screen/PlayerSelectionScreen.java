package screen;

import engine.Core;
import engine.utils.Cooldown;
import entity.Entity;
import entity.character.CharacterSpawner;
import entity.character.CharacterType;
import entity.character.GameCharacter;
import java.awt.Rectangle;
import java.awt.event.KeyEvent;

public class PlayerSelectionScreen extends Screen {
    
    private static final int SELECTION_TIME = 200;
    private Cooldown selectionCooldown;
    private int selectedShipIndex = 0;
    private GameCharacter[] characterSamples;
    private final int characterTypeCount = CharacterType.values().length;
    
    private int playerId;
    private boolean backButtonSelected = false;
    
    public PlayerSelectionScreen(final int width, final int height, final int fps,
        final int playerId) {
        super(width, height, fps);
        this.playerId = playerId;
        this.selectionCooldown = Core.getCooldown(SELECTION_TIME);
        this.selectionCooldown.reset();
        this.characterSamples = new GameCharacter[characterTypeCount];
        int gap = width / (characterTypeCount + 1);
        int startX = gap;
        int positionY = height / 2;
        for (int i = 0; i < characterSamples.length; i++) {
            CharacterType characterType = CharacterType.values()[i];
            int positionX = startX + (i * gap);
            characterSamples[i] = CharacterSpawner.createCharacter(characterType, positionX,
                positionY, Entity.Team.PLAYER1, playerId);
        }
    }
    
    public final int run() {
        super.run();
        return this.returnCode;
    }
    
    @Override
    protected final void update() {
        super.update();
        draw();
        
        // 1. 쿨타임 체크 (준비되지 않았으면 업데이트 중단)
        if (!this.selectionCooldown.checkFinished() || !this.inputDelay.checkFinished()) {
            return;
        }
        
        // 2. 입력 처리 분리
        handleKeyboardInput();
        handleMouseInput();
    }
    
    /**
     * 키보드 입력을 처리합니다.
     */
    private void handleKeyboardInput() {
        // 상/하 이동 (Back 버튼 포커스)
        if (isInputUp()) {
            backButtonSelected = true;
            selectionCooldown.reset();
        } else if (isInputDown()) {
            backButtonSelected = false;
            selectionCooldown.reset();
        } else if (!backButtonSelected) { // 좌/우 이동 (캐릭터 선택 - Back 버튼이 아닐 때만)
            handleCharacterRotation();
        }
        
        // 선택 확정 (Space)
        if (inputManager.isKeyDown(KeyEvent.VK_SPACE)) {
            confirmSelection(backButtonSelected); // 키보드는 현재 포커스 상태에 따라 결정
        }
    }
    
    /**
     * 캐릭터 선택 인덱스 변경 (좌/우)
     */
    private void handleCharacterRotation() {
        if (inputManager.isKeyDown(KeyEvent.VK_LEFT) || inputManager.isKeyDown(KeyEvent.VK_A)) {
            // (현재 - 1 + 전체개수) % 전체개수 -> 음수 방지 순환 로직
            this.selectedShipIndex =
                (this.selectedShipIndex - 1 + characterTypeCount) % characterTypeCount;
            this.selectionCooldown.reset();
        } else if (inputManager.isKeyDown(KeyEvent.VK_RIGHT) || inputManager.isKeyDown(
            KeyEvent.VK_D)) {
            this.selectedShipIndex = (this.selectedShipIndex + 1) % characterTypeCount;
            this.selectionCooldown.reset();
        }
    }
    
    /**
     * 마우스 입력을 처리합니다.
     */
    private void handleMouseInput() {
        if (!inputManager.isMouseClicked()) {
            return;
        }
        
        int mx = inputManager.getMouseX();
        int my = inputManager.getMouseY();
        Rectangle backButtonHitbox = Core.getHitboxManager()
            .getBackButtonHitbox(drawManager.getBackBufferGraphics(), this);
        
        if (backButtonHitbox.contains(mx, my)) {
            confirmSelection(true); // 마우스로 Back 버튼을 누름 -> backSelected = true 취급
        }
    }
    
    /**
     * 선택을 확정하고 다음 화면 코드를 설정합니다.
     *
     * @param isBackSelected 뒤로가기가 선택되었는지 여부
     */
    private void confirmSelection(boolean isBackSelected) {
        if (this.playerId == 1) {
            this.returnCode = isBackSelected ? 5 : 6; // 5: PlayModeSelect, 6: P2 Select
        } else if (this.playerId == 2) {
            this.returnCode = isBackSelected ? 6 : 2; // 6: P1 Select(Back), 2: GameStart
        }
        this.isRunning = false;
    }
    
    // 키 입력 헬퍼 메서드
    private boolean isInputUp() {
        return inputManager.isKeyDown(KeyEvent.VK_UP) || inputManager.isKeyDown(KeyEvent.VK_W);
    }
    
    private boolean isInputDown() {
        return inputManager.isKeyDown(KeyEvent.VK_DOWN) || inputManager.isKeyDown(KeyEvent.VK_S);
    }
    
    private void draw() {
        drawManager.initDrawing(this);
        
        drawManager.getShipSelectionMenuRenderer()
            .drawPlayerSelectionMenu(drawManager.getBackBufferGraphics(), this,
                this.characterSamples,
                this.selectedShipIndex, this.playerId);
        
        // hover highlight
        int mx = inputManager.getMouseX();
        int my = inputManager.getMouseY();
        Rectangle backBox = Core.getHitboxManager()
            .getBackButtonHitbox(drawManager.getBackBufferGraphics(), this);
        boolean backHover = backBox.contains(mx, my);
        drawManager.getCommonRenderer()
            .drawBackButton(drawManager.getBackBufferGraphics(), this,
                backHover || backButtonSelected);
        
        drawManager.completeDrawing(this);
    }
    
    /**
     * Returns the selected character type to Core.
     *
     * @return The selected CharacterType enum.
     */
    public CharacterType getSelectedCharacterType() {
        return switch (this.selectedShipIndex) {
            case 1 -> CharacterType.WARRIOR;
            case 2 -> CharacterType.ARCHER;
            case 3 -> CharacterType.WIZARD;
            case 4 -> CharacterType.LASER;
            case 5 -> CharacterType.ELECTRIC;
            case 6 -> CharacterType.BOMBER;
            case 7 -> CharacterType.HEALER;
            default -> CharacterType.ARCHER;
        };
    }
}