package engine.renderer;

import engine.AssetManager;
import screen.Screen;

import java.awt.*;

public class TitleScreenRenderer {

    private CommonRenderer commonRenderer;
    private AssetManager assetManager;
    /** Font properties. */
    private static FontMetrics fontMetrics;

    // 메뉴 배경 애니메이션 (원래 DrawManager에 있던 것)
    private animations.MenuSpace menuSpace = new animations.MenuSpace(50);

    public TitleScreenRenderer(CommonRenderer commonRenderer) {
        this.commonRenderer = commonRenderer;
        this.assetManager = AssetManager.getInstance();
    }

    /**
     * Draws game title.
     *
     * @param screen
     *               Screen to draw on.
     */
    public void drawTitle(Graphics g, final Screen screen) {
        String titleString = "Invaders";
        String instructionsString = "select with w+s / arrows, confirm with space";

        g.setColor(Color.GRAY);
        commonRenderer.drawCenteredRegularString(g, screen, instructionsString, screen.getHeight() / 2);

        g.setColor(Color.GREEN);
        commonRenderer.drawCenteredBigString(g, screen, titleString, screen.getHeight() / 3);
    }

    /**
     * Draws the main menu stars background animation
     */
    public void updateMenuSpace(Graphics g){
        menuSpace.updateStars();

        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        g.setColor(Color.WHITE);
        int[][] positions = menuSpace.getStarLocations();

        for(int i = 0; i < menuSpace.getNumStars(); i++){

            int size = 1;
            int radius = size * 2;

            float[] dist = {0.0f, 1.0f};
            Color[] colors = {
                    menuSpace.getColor(),
                    new Color(255, 255, 200, 0)
            };

            RadialGradientPaint paint = new RadialGradientPaint(
                    new Point(positions[i][0], positions[i][1]),
                    radius,
                    dist,
                    colors
            );
            g2d.setPaint(paint);
            g2d.fillOval(positions[i][0] - radius / 2, positions[i][1] - radius / 2, radius, radius);


            g.fillOval(positions[i][0], positions[i][1], size, size);
        }
    }

    /**
     * Draws main menu. - remodified for 2P mode, using string array for efficiency
     *
     * @param screen
     *               Screen to draw on.
     * @param selectedIndex
     *               Option selected.
     */
    public void drawMenu(Graphics g, final Screen screen, final int option, final Integer hoverOption, final int selectedIndex) {
        g.setFont(commonRenderer.getFontRegular());
        fontMetrics = g.getFontMetrics(commonRenderer.getFontRegular());

        String[] items = {"Play", "Achievements", "High scores", "Settings", "Exit"};

        int baseY = screen.getHeight() / 3 * 2 - 20; // Adjust spacing due to high society button addition
        int spacing = (int) (fontMetrics.getHeight() * 1.5);
        for (int i = 0; i < items.length; i++) {
            boolean highlight = (hoverOption != null) ? (i == hoverOption) : (i == selectedIndex);
            g.setColor(highlight ? Color.GREEN : Color.WHITE);
            commonRenderer.drawCenteredRegularString(g, screen, items[i], baseY + spacing * i);
        }
    }

    public void menuHover(final int state){
        menuSpace.setColor(state);
        menuSpace.setSpeed(state == 4);
    }
}
