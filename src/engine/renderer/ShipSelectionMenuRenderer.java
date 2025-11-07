package engine.renderer;

import entity.Ship;
import screen.Screen;

import java.awt.*;

public class ShipSelectionMenuRenderer {

    private CommonRenderer commonRenderer;
    private EntityRenderer entityRenderer;

    public ShipSelectionMenuRenderer(CommonRenderer commonRenderer) {
        this.commonRenderer = commonRenderer;
        this.entityRenderer = new EntityRenderer(commonRenderer);
    }

    public void drawShipSelectionMenu(Graphics g, final Screen screen, final Ship[] shipExamples, final int selectedShipIndex, final int playerIndex) {
        Ship ship = shipExamples[selectedShipIndex];
        int centerX = ship.getPositionX();

        String screenTitle = "PLAYER " + playerIndex + " : CHOOSE YOUR SHIP";

        // Ship Type Info
        String[] shipNames = {"Normal Type", "Big Shot Type", "Double Shot Type", "Speed Type"};
        String[] shipSpeeds = {"SPEED: NORMAL", "SPEED: SLOW", "SPEED: SLOW", "SPEED: FAST"};
        String[] shipFireRates = {"FIRE RATE: NORMAL", "FIRE RATE: NORMAL", "FIRE RATE: NORMAL", "FIRE RATE: SLOW"};

        entityRenderer.drawEntity(g, ship, ship.getPositionX() - ship.getWidth() / 2, ship.getPositionY());
//        for (int i = 0; i < 4; i++) {
//            // Draw Player Ship
//            drawManager.drawEntity(ship, ship.getPositionX() - ship.getWidth()/2, ship.getPositionY());
//        }

        // Draw Selected Player Page Title
        g.setColor(Color.GREEN);
        commonRenderer.drawCenteredBigString(g, screen, screenTitle, screen.getHeight() / 4);
        // Draw Selected Player Ship Type
        g.setColor(Color.white);
        commonRenderer.drawCenteredRegularString(g, screen, " > " + shipNames[selectedShipIndex] + " < ", screen.getHeight() / 2 - 40);
        // Draw Selected Player Ship Info
        g.setColor(Color.WHITE);
//        drawCenteredRegularString(shipSpeeds[selectedShipIndex], centerX, screen.getHeight() / 2 + 60);
//        drawCenteredRegularString(shipFireRates[selectedShipIndex], centerX, screen.getHeight() / 2 + 80);
        commonRenderer.drawCenteredRegularString(g, screen, shipSpeeds[selectedShipIndex], screen.getHeight() / 2 + 60);
        commonRenderer.drawCenteredRegularString(g, screen, shipFireRates[selectedShipIndex], screen.getHeight() / 2 + 80);

        g.setColor(Color.GRAY);
        commonRenderer.drawCenteredRegularString(g, screen, "Press SPACE to Select", screen.getHeight() - 50);
    }
}
