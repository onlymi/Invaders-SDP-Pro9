package screen;

import java.awt.*;
import java.awt.event.KeyEvent;
import engine.utils.Cooldown;
import engine.Core;
import entity.Entity;
import entity.Ship;

public class ShipSelectionScreen extends Screen {

    private static final int SELECTION_TIME = 200;
    private Cooldown selectionCooldown;
    private int selectedShipIndex = 0; // 0: NORMAL, 1: BIG_SHOT, 2: DOUBLE_SHOT, 3: MOVE_FAST
    private Ship[] shipExamples = new Ship[4];
    Ship.ShipType[] shipType;

    private int player;
    private boolean backSelected = false; // If current state is on the back button, can't select ship

    public ShipSelectionScreen(final int width, final int height, final int fps, final int player) {
        super(width, height, fps);
        this.player = player;
        this.selectionCooldown = Core.getCooldown(SELECTION_TIME);
        this.selectionCooldown.reset();

        shipType = new Ship.ShipType[]{Ship.ShipType.NORMAL, Ship.ShipType.BIG_SHOT, Ship.ShipType.DOUBLE_SHOT, Ship.ShipType.MOVE_FAST};
        int initialX_width = -100;

        for (int i = 0; i < shipExamples.length; i++) {
            int positionX = width / 2 + (initialX_width + (200 / (shipExamples.length - 1)) * i);
            if (player == 1) {
                shipExamples[i] = new Ship(positionX, height / 2, Entity.Team.PLAYER1, shipType[i], null);
            } else if (player == 2) {
                shipExamples[i] = new Ship(positionX, height / 2, Entity.Team.PLAYER2, shipType[i], null);
            }
        }
    }

    /**
     * Returns the selected ship type to Core.
     *
     * @return The selected ShipType enum.
     */
    public Ship.ShipType getSelectedShipType() {
        return switch (this.selectedShipIndex) {
            case 1 -> Ship.ShipType.BIG_SHOT;
            case 2 -> Ship.ShipType.DOUBLE_SHOT;
            case 3 -> Ship.ShipType.MOVE_FAST;
            default -> Ship.ShipType.NORMAL;
        };
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
            if (inputManager.isKeyDown(KeyEvent.VK_DOWN) ||  inputManager.isKeyDown(KeyEvent.VK_S)) {
                backSelected = false;
                selectionCooldown.reset();
            }
            if (!backSelected) {
                if (inputManager.isKeyDown(KeyEvent.VK_LEFT) || inputManager.isKeyDown(KeyEvent.VK_A)) {
                    this.selectedShipIndex = this.selectedShipIndex - 1;
                    if (this.selectedShipIndex < 0) {
                        this.selectedShipIndex += 4;
                    }
                    this.selectedShipIndex = this.selectedShipIndex % 4;
                    this.selectionCooldown.reset();
                }
                if (inputManager.isKeyDown(KeyEvent.VK_RIGHT) || inputManager.isKeyDown(KeyEvent.VK_D)) {
                    this.selectedShipIndex = (this.selectedShipIndex + 1) % 4;
                    this.selectionCooldown.reset();
                }
            }
            if (inputManager.isKeyDown(KeyEvent.VK_SPACE)) {
                switch (player) {
                    case 1 -> this.returnCode = backSelected ? 5 : 6;
                    case 2 -> this.returnCode = backSelected ? 6 : 2;
                }
                this.isRunning = false;
            }
            int mx = inputManager.getMouseX();
            int my = inputManager.getMouseY();
            boolean clicked = inputManager.isMouseClicked();

            Rectangle backBox = Core.getHitboxManager().getBackButtonHitbox(drawManager.getBackBufferGraphics(), this);

            if (clicked && backBox.contains(mx, my)) {
                if (player == 1) this.returnCode = 5;
                else if (player == 2) this.returnCode = 6;
                this.isRunning = false;

            }
        }
    }

    private void draw() {
        drawManager.initDrawing(this);

        drawManager.getShipSelectionMenuRenderer().drawShipSelectionMenu(drawManager.getBackBufferGraphics(), this, shipExamples, this.selectedShipIndex, this.player);

        // hover highlight
        int mx = inputManager.getMouseX();
        int my = inputManager.getMouseY();
        Rectangle backBox = Core.getHitboxManager().getBackButtonHitbox(drawManager.getBackBufferGraphics(), this);
        boolean backHover = backBox.contains(mx, my);
        drawManager.getCommonRenderer().drawBackButton(drawManager.getBackBufferGraphics(), this, backHover || backSelected);

        drawManager.completeDrawing(this);
    }
}