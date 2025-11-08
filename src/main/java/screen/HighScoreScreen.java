package screen;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.io.IOException;
import java.util.List;

import engine.Core;
import engine.Score;
import engine.SoundManager;

/**
 * Implements the high scores screen, it shows player records.
 *
 * @author <a href="mailto:RobertoIA1987@gmail.com">Roberto Izquierdo Amo</a>
 *
 */
public class HighScoreScreen extends Screen {

    /** List of past high scores. */
    private List<Score> highScores1P, highScores2P;

    /**
     * Constructor, establishes the properties of the screen.
     *
     * @param width
     *            Screen width.
     * @param height
     *            Screen height.
     * @param fps
     *            Frames per second, frame rate at which the game is run.
     */
    public HighScoreScreen(final int width, final int height, final int fps) {
        super(width, height, fps);
        SoundManager.playLoop("title_sound");

        this.returnCode = 1;

        try {
            this.highScores1P = this.fileManager.loadHighScores("1p");
            this.highScores2P = this.fileManager.loadHighScores("2p");
            //상위 7명만 남기기
            this.highScores1P.sort((a, b) -> b.getScore() - a.getScore());
            if (this.highScores1P.size() > 7) this.highScores1P = this.highScores1P.subList(0, 7);

            this.highScores2P.sort((a, b) -> b.getScore() - a.getScore());
            if (this.highScores2P.size() > 7) this.highScores2P = this.highScores2P.subList(0, 7);

        } catch (NumberFormatException | IOException e) {
            LOGGER.warning("Couldn't load high scores!");
        }
    }

    /**
     * Starts the action.
     *
     * @return Next screen code.
     */
    public final int run() {
        super.run();
        SoundManager.playOnce("select");

        return this.returnCode;
    }

    /**
     * Updates the elements on screen and checks for events.
     */
    protected final void update() {
        super.update();

        draw();
        if (this.inputManager.isKeyDown(KeyEvent.VK_ESCAPE)
                && this.inputDelay.checkFinished())
            this.isRunning = false;

        // back button click event
        if (this.inputManager.isMouseClicked()) {
            int mx = this.inputManager.getMouseX();
            int my = this.inputManager.getMouseY();
            Rectangle backBox = Core.getHitboxManager().getBackButtonHitbox(drawManager.getBackBufferGraphics(), this);

            if (backBox.contains(mx, my)) {
                this.returnCode = 1;
                this.isRunning = false;
            }
        }
    }
    private List<Score> getPlayerScores(String mode) {
        return mode.equals("1P") ? this.highScores1P : this.highScores2P;
    }
    /**
     * Draws the elements associated with the screen.
     */
    private void draw() {
        drawManager.initDrawing(this);

        drawManager.getHighScoreScreenRenderer().drawHighScoreMenu(drawManager.getBackBufferGraphics(), this);
        drawManager.getHighScoreScreenRenderer().drawHighScores(drawManager.getBackBufferGraphics(), this, getPlayerScores("1P"), "1P"); // Left column
        drawManager.getHighScoreScreenRenderer().drawHighScores(drawManager.getBackBufferGraphics(), this, getPlayerScores("2P"), "2P"); // Right column

        // hover highlight
        int mx = inputManager.getMouseX();
        int my = inputManager.getMouseY();
        java.awt.Rectangle backBox = Core.getHitboxManager().getBackButtonHitbox(drawManager.getBackBufferGraphics(), this);

        if (backBox.contains(mx, my)) {
            drawManager.getCommonRenderer().drawBackButton(drawManager.getBackBufferGraphics(), this, true);
        }

        drawManager.completeDrawing(this);
    }
}