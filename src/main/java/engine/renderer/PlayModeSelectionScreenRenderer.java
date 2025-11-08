package engine.renderer;

import screen.Screen;

import java.awt.*;

public class PlayModeSelectionScreenRenderer {

    private CommonRenderer commonRenderer;
    /** Font properties. */
    private static FontMetrics fontMetrics;

    public PlayModeSelectionScreenRenderer(CommonRenderer commonRenderer) {
        this.commonRenderer = commonRenderer;
    }

    /**
     * Draws the play mode selection menu (1P / 2P / Back).
     *
     * @param screen
     *                  Screen to draw on.
     * @param selectedIndex
     *                  Currently selected option (0: 1P, 1: 2P, 2: Back).
     */
    public void drawPlayModeSelectionMenu(Graphics g, final Screen screen, final Integer hoverOption, final int selectedIndex) {
        String[] items = {"1 Player", "2 Players"};
        // Removed center back button

        // draw back button at top-left corner\, Set the selectedIndex to Highlight the Back Button
        commonRenderer.drawBackButton(g, screen, selectedIndex == 2);

        int baseY = screen.getHeight() / 2 - 20; // Modified the position with the choice reduced to two
        for (int i = 0; i < items.length; i++) {
            boolean highlight = (hoverOption != null) ? (i == hoverOption) : (i == selectedIndex);
            g.setColor(highlight ? Color.GREEN : Color.WHITE);
            fontMetrics = g.getFontMetrics(commonRenderer.getFontRegular());
            commonRenderer.drawCenteredRegularString(g, screen, items[i], baseY + fontMetrics.getHeight() * 3 * i);
        }
    }
}
