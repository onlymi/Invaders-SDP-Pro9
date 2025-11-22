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
    private boolean backSelected = false; // If current state is on the back button, can't select ship
    
    public PlayerSelectionScreen(final int width, final int height, final int fps,
        final int playerId) {
        super(width, height, fps);
        this.playerId = playerId;
        this.selectionCooldown = Core.getCooldown(SELECTION_TIME);
        this.selectionCooldown.reset();
        this.characterSamples = new GameCharacter[characterTypeCount];
        int initialX_width = -100;
        for (int i = 0; i < characterSamples.length; i++) {
            CharacterType characterTypes = CharacterType.values()[i];
            int positionX =
                width / 2 + (initialX_width + (200 / (characterSamples.length - 1)) * i);
            characterSamples[i] = CharacterSpawner.createCharacter(characterTypes, positionX,
                height / 2, Entity.Team.PLAYER1, playerId);
        }
    }
    
    public final int run() {
        super.run();
        return this.returnCode;
    }
    
    protected final void update() {
        super.update();
        draw();
        if (this.selectionCooldown.checkFinished() && this.inputDelay.checkFinished()) {
            if (inputManager.isKeyDown(KeyEvent.VK_UP) || inputManager.isKeyDown(KeyEvent.VK_W)) {
                backSelected = true;
                selectionCooldown.reset();
            }
            if (inputManager.isKeyDown(KeyEvent.VK_DOWN) || inputManager.isKeyDown(KeyEvent.VK_S)) {
                backSelected = false;
                selectionCooldown.reset();
            }
            if (!backSelected) {
                if (inputManager.isKeyDown(KeyEvent.VK_LEFT) || inputManager.isKeyDown(
                    KeyEvent.VK_A)) {
                    this.selectedShipIndex = this.selectedShipIndex - 1;
                    if (this.selectedShipIndex < 0) {
                        this.selectedShipIndex += characterTypeCount;
                    }
                    this.selectedShipIndex = this.selectedShipIndex % characterTypeCount;
                    this.selectionCooldown.reset();
                }
                if (inputManager.isKeyDown(KeyEvent.VK_RIGHT) || inputManager.isKeyDown(
                    KeyEvent.VK_D)) {
                    this.selectedShipIndex = (this.selectedShipIndex + 1) % characterTypeCount;
                    this.selectionCooldown.reset();
                }
            }
            if (inputManager.isKeyDown(KeyEvent.VK_SPACE)) {
                switch (this.playerId) {
                    case 1 -> this.returnCode = backSelected ? 5 : 6;
                    case 2 -> this.returnCode = backSelected ? 6 : 2;
                }
                this.isRunning = false;
            }
            int mx = inputManager.getMouseX();
            int my = inputManager.getMouseY();
            boolean clicked = inputManager.isMouseClicked();
            
            Rectangle backBox = Core.getHitboxManager()
                .getBackButtonHitbox(drawManager.getBackBufferGraphics(), this);
            
            if (clicked && backBox.contains(mx, my)) {
                if (playerId == 1) {
                    this.returnCode = 5;
                } else if (playerId == 2) {
                    this.returnCode = 6;
                }
                this.isRunning = false;
                
            }
        }
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
            .drawBackButton(drawManager.getBackBufferGraphics(), this, backHover || backSelected);
        
        drawManager.completeDrawing(this);
    }
}