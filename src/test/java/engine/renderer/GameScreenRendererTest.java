package engine.renderer;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.awt.Color;
import java.lang.reflect.Method;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for GameScreenRenderer tier parsing and tier-based color mapping.
 */
class GameScreenRendererTest {

    /**
     * Creates a new GameScreenRenderer instance for testing. CommonRenderer and ItemManager are not
     * used by the tested methods, so null can be passed safely.
     */
    private GameScreenRenderer newRenderer() {
        return new GameScreenRenderer(null, null);
    }

    // Helpers

    private int invokeParseDropTier(GameScreenRenderer renderer, String raw) throws Exception {
        Method m = GameScreenRenderer.class.getDeclaredMethod("parseDropTier", String.class);
        m.setAccessible(true);
        return (int) m.invoke(renderer, raw);
    }

    private Color invokeColorByTier(GameScreenRenderer renderer, int tier) throws Exception {
        Method m = GameScreenRenderer.class.getDeclaredMethod("colorByTier", int.class);
        m.setAccessible(true);
        return (Color) m.invoke(renderer, tier);
    }


    @Test
    void parseDropTier_namedTierValues() throws Exception {
        GameScreenRenderer r = newRenderer();

        assertEquals(0, invokeParseDropTier(r, "COMMON"));
        assertEquals(1, invokeParseDropTier(r, "UNCOMMON"));
        assertEquals(2, invokeParseDropTier(r, "RARE"));
        assertEquals(3, invokeParseDropTier(r, "EPIC"));
        assertEquals(4, invokeParseDropTier(r, "LEGENDARY"));
    }

    @Test
    void parseDropTier_numericValues() throws Exception {
        GameScreenRenderer r = newRenderer();

        assertEquals(0, invokeParseDropTier(r, "0"));
        assertEquals(2, invokeParseDropTier(r, "2"));
        assertEquals(4, invokeParseDropTier(r, "4"));
    }

    @Test
    void parseDropTier_invalidValuesReturnZero() throws Exception {
        GameScreenRenderer r = newRenderer();

        assertEquals(0, invokeParseDropTier(r, "???"));
        assertEquals(0, invokeParseDropTier(r, " "));
        assertEquals(0, invokeParseDropTier(r, "invalid-tier"));
        assertEquals(0, invokeParseDropTier(r, null));
    }

    @Test
    void colorByTier_returnsCorrectColor() throws Exception {
        GameScreenRenderer r = newRenderer();

        assertEquals(Color.WHITE, invokeColorByTier(r, 0));
        assertEquals(Color.GREEN, invokeColorByTier(r, 1));
        assertEquals(Color.BLUE, invokeColorByTier(r, 2));
        assertEquals(Color.MAGENTA, invokeColorByTier(r, 3));
        assertEquals(Color.ORANGE, invokeColorByTier(r, 4));
    }

    @Test
    void colorByTier_outOfRangeDefaultsToWhite() throws Exception {
        GameScreenRenderer r = newRenderer();

        assertEquals(Color.WHITE, invokeColorByTier(r, -1));
        assertEquals(Color.WHITE, invokeColorByTier(r, 999));
    }
}