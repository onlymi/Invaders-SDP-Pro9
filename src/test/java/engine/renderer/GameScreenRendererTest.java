package engine.renderer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import engine.AssetManager;
import engine.AssetManager.SpriteType;
import engine.GameState;
import entity.character.GameCharacter;
import entity.skill.Skill;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import screen.GameScreen;

/**
 * Unit tests for GameScreenRenderer. Includes tests for tier parsing, color mapping, and skill HUD
 * rendering.
 */
class GameScreenRendererTest {
    
    // --- Mock Objects for Skill HUD Tests ---
    private GameScreenRenderer renderer;
    private CommonRenderer mockCommonRenderer;
    private AssetManager mockAssetManager;
    private Graphics2D mockGraphics;
    private GameScreen mockScreen;
    private GameState mockGameState;
    private GameCharacter mockCharacterP1;
    private Skill mockSkill1;
    
    // Backup for Singleton Restoration
    private Object originalAssetManagerInstance;
    
    @BeforeEach
    void setUp() throws Exception {
        // 1. AssetManager 싱글톤을 Mock으로 교체
        Field instanceField = AssetManager.class.getDeclaredField("instance");
        instanceField.setAccessible(true);
        originalAssetManagerInstance = instanceField.get(null);
        mockAssetManager = mock(AssetManager.class);
        instanceField.set(null, mockAssetManager);
        
        // InputManager 초기화 시 NPE 방지 (key_config 파일 로드)
        when(mockAssetManager.getCsvData("key_config")).thenReturn(
            new File("dummy_path/key_config.csv"));
        
        // 2. CommonRenderer Mock 설정
        mockCommonRenderer = mock(CommonRenderer.class);
        Font dummyFont = new Font("Arial", Font.PLAIN, 12);
        when(mockCommonRenderer.getFontRegular()).thenReturn(dummyFont);
        when(mockCommonRenderer.getFontBig()).thenReturn(dummyFont);
        
        // 3. GameScreenRenderer 초기화
        renderer = new GameScreenRenderer(mockCommonRenderer, null);
        
        // 4. Graphics2D Mock 설정 및 NPE 방지 코드 추가
        mockGraphics = mock(Graphics2D.class);
        // create() 호출 시 자기 자신을 반환하도록 설정하여 NPE 방지
        when(mockGraphics.create()).thenReturn(mockGraphics);
        
        mockScreen = mock(GameScreen.class);
        mockGameState = mock(GameState.class);
        mockCharacterP1 = mock(GameCharacter.class);
        
        // FontMetrics Mocking
        FontMetrics mockFontMetrics = mock(FontMetrics.class);
        when(mockGraphics.getFontMetrics()).thenReturn(mockFontMetrics);
        when(mockGraphics.getFontMetrics(any())).thenReturn(mockFontMetrics);
        when(mockFontMetrics.stringWidth(any(String.class))).thenReturn(10);
        when(mockFontMetrics.getHeight()).thenReturn(10);
        when(mockFontMetrics.getAscent()).thenReturn(8);
        
        // 기본 GameScreen 설정
        GameCharacter[] characters = new GameCharacter[]{mockCharacterP1, null};
        when(mockScreen.getCharacters()).thenReturn(characters);
        
        // 기본 Skill 설정 (Skill 1)
        mockSkill1 = mock(Skill.class);
        when(mockSkill1.getSpriteType()).thenReturn(SpriteType.CharacterWarriorFirstSkill);
        when(mockSkill1.getManaCost()).thenReturn(10);
        when(mockSkill1.getRemainingCooldown()).thenReturn(0);
        
        // [중요 수정] Renderer는 스킬이 3개라고 가정하므로, 3개의 스킬을 가진 리스트를 반환해야 함
        Skill mockSkill2 = mock(Skill.class);
        when(mockSkill2.getSpriteType()).thenReturn(SpriteType.CharacterWarriorSecondSkill);
        Skill mockSkill3 = mock(Skill.class);
        when(mockSkill3.getSpriteType()).thenReturn(SpriteType.CharacterWarriorUltimateSkill);
        
        ArrayList<Skill> skills = new ArrayList<>();
        skills.add(mockSkill1);
        skills.add(mockSkill2);
        skills.add(mockSkill3);
        
        // 캐릭터 스킬 리스트 설정
        when(mockCharacterP1.getSkills()).thenReturn(skills);
        
        // 키 바인딩 설정
        mockCharacterP1.firstSkillKey = java.awt.event.KeyEvent.VK_1;
        mockCharacterP1.secondSkillKey = java.awt.event.KeyEvent.VK_2;
        mockCharacterP1.ultimateSkillKey = java.awt.event.KeyEvent.VK_3;
        
        // 캐릭터 마나 충분 상태로 설정
        when(mockCharacterP1.getCurrentManaPoints()).thenReturn(100);
    }
    
    @AfterEach
    void tearDown() throws Exception {
        // AssetManager 싱글톤 복구
        Field instanceField = AssetManager.class.getDeclaredField("instance");
        instanceField.setAccessible(true);
        instanceField.set(null, originalAssetManagerInstance);
    }
    
    // =================================================================================
    // Part 1: Tier Parsing & Color Mapping Tests
    // =================================================================================
    
    private GameScreenRenderer newRenderer() {
        return new GameScreenRenderer(mockCommonRenderer, null);
    }
    
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
    @DisplayName("Tier Parsing: Named Values")
    void parseDropTier_namedTierValues() throws Exception {
        GameScreenRenderer r = newRenderer();
        assertEquals(0, invokeParseDropTier(r, "COMMON"));
        assertEquals(1, invokeParseDropTier(r, "UNCOMMON"));
        assertEquals(2, invokeParseDropTier(r, "RARE"));
        assertEquals(3, invokeParseDropTier(r, "EPIC"));
        assertEquals(4, invokeParseDropTier(r, "LEGENDARY"));
    }
    
    @Test
    @DisplayName("Tier Parsing: Numeric Values")
    void parseDropTier_numericValues() throws Exception {
        GameScreenRenderer r = newRenderer();
        assertEquals(0, invokeParseDropTier(r, "0"));
        assertEquals(2, invokeParseDropTier(r, "2"));
        assertEquals(4, invokeParseDropTier(r, "4"));
    }
    
    @Test
    @DisplayName("Tier Parsing: Invalid Values")
    void parseDropTier_invalidValuesReturnZero() throws Exception {
        GameScreenRenderer r = newRenderer();
        assertEquals(0, invokeParseDropTier(r, "???"));
        assertEquals(0, invokeParseDropTier(r, " "));
        assertEquals(0, invokeParseDropTier(r, "invalid-tier"));
        assertEquals(0, invokeParseDropTier(r, null));
    }
    
    @Test
    @DisplayName("Color Mapping: Tier to Color")
    void colorByTier_returnsCorrectColor() throws Exception {
        GameScreenRenderer r = newRenderer();
        assertEquals(Color.WHITE, invokeColorByTier(r, 0));
        assertEquals(Color.GREEN, invokeColorByTier(r, 1));
        assertEquals(Color.BLUE, invokeColorByTier(r, 2));
        assertEquals(Color.MAGENTA, invokeColorByTier(r, 3));
        assertEquals(Color.ORANGE, invokeColorByTier(r, 4));
    }
    
    @Test
    @DisplayName("Color Mapping: Out of Range")
    void colorByTier_outOfRangeDefaultsToWhite() throws Exception {
        GameScreenRenderer r = newRenderer();
        assertEquals(Color.WHITE, invokeColorByTier(r, -1));
        assertEquals(Color.WHITE, invokeColorByTier(r, 999));
    }
    
    // =================================================================================
    // Part 2: Skill HUD Drawing Tests
    // =================================================================================
    
    @Test
    @DisplayName("Skill Slot Drawing: With Image")
    void testDrawSkillSlots_Basic() {
        // Given
        BufferedImage mockImage = mock(BufferedImage.class);
        when(mockAssetManager.getSpriteImage(any(SpriteType.class))).thenReturn(mockImage);
        
        // When
        renderer.drawCharacterSkillSlots(mockGraphics, mockScreen, mockGameState,
            mockScreen.getCharacters());
        
        // Then
        verify(mockGraphics, atLeastOnce()).drawImage(eq(mockImage), anyInt(), anyInt(), anyInt(),
            anyInt(), any());
        verify(mockGraphics, atLeastOnce()).drawString(any(String.class), anyInt(), anyInt());
        verify(mockGraphics, atLeastOnce()).drawRoundRect(anyInt(), anyInt(), anyInt(), anyInt(),
            anyInt(), anyInt());
    }
    
    @Test
    @DisplayName("Skill Slot Drawing: Cooldown Overlay")
    void testDrawSkillSlots_Cooldown() {
        // Given
        int remainingCooldown = 5000; // 5초
        when(mockSkill1.getRemainingCooldown()).thenReturn(remainingCooldown);
        
        // When
        renderer.drawCharacterSkillSlots(mockGraphics, mockScreen, mockGameState,
            mockScreen.getCharacters());
        
        // Then
        // 쿨타임 오버레이(검정 투명)
        verify(mockGraphics, atLeastOnce()).setColor(eq(new Color(0, 0, 0, 180)));
        verify(mockGraphics, atLeastOnce()).fillRoundRect(anyInt(), anyInt(), anyInt(), anyInt(),
            anyInt(), anyInt());
        
        // 남은 시간 텍스트
        verify(mockGraphics, atLeastOnce()).setColor(Color.YELLOW);
        verify(mockGraphics, atLeastOnce()).drawString(eq("6"), anyInt(), anyInt());
    }
    
    @Test
    @DisplayName("Skill Slot Drawing: Mana Shortage Overlay")
    void testDrawSkillSlots_NoMana() {
        // Given
        when(mockCharacterP1.getCurrentManaPoints()).thenReturn(0);
        when(mockSkill1.getManaCost()).thenReturn(10);
        
        // When
        renderer.drawCharacterSkillSlots(mockGraphics, mockScreen, mockGameState,
            mockScreen.getCharacters());
        
        // Then
        // 마나 부족 오버레이(붉은 투명)
        verify(mockGraphics, atLeastOnce()).setColor(eq(new Color(200, 0, 0, 100)));
        verify(mockGraphics, atLeastOnce()).fillRoundRect(anyInt(), anyInt(), anyInt(), anyInt(),
            anyInt(), anyInt());
    }
    
    @Test
    @DisplayName("Skill Slot Drawing: Fallback ('?') when no image")
    void testDrawSkillSlots_NoImage_Fallback() {
        // Given
        when(mockAssetManager.getSpriteImage(any())).thenReturn(null);
        when(mockAssetManager.getSpriteMap(any())).thenReturn(null);
        
        // When
        renderer.drawCharacterSkillSlots(mockGraphics, mockScreen, mockGameState,
            mockScreen.getCharacters());
        
        // Then
        verify(mockGraphics, atLeastOnce()).setColor(Color.DARK_GRAY);
        verify(mockGraphics, atLeastOnce()).fillRect(anyInt(), anyInt(), anyInt(), anyInt());
        verify(mockGraphics, atLeastOnce()).drawString(eq("?"), anyInt(), anyInt());
    }
    
    @Test
    @DisplayName("Skill Slot Drawing: 2P Co-op Mode")
    void testDrawSkillSlots_Coop() {
        // Given
        when(mockGameState.isCoop()).thenReturn(true);
        GameCharacter mockCharacterP2 = mock(GameCharacter.class);
        
        // P2 Skill Setup
        Skill mockSkillP2_1 = mock(Skill.class);
        when(mockSkillP2_1.getSpriteType()).thenReturn(SpriteType.CharacterArcherFirstSkill);
        
        // [중요 수정] P2도 스킬이 3개여야 함
        ArrayList<Skill> skillsP2 = new ArrayList<>();
        skillsP2.add(mockSkillP2_1);
        skillsP2.add(mock(Skill.class)); // Dummy 2nd
        skillsP2.add(mock(Skill.class)); // Dummy Ultimate
        
        when(mockCharacterP2.getSkills()).thenReturn(skillsP2);
        
        GameCharacter[] characters = new GameCharacter[]{mockCharacterP1, mockCharacterP2};
        when(mockScreen.getCharacters()).thenReturn(characters);
        
        // When
        renderer.drawCharacterSkillSlots(mockGraphics, mockScreen, mockGameState, characters);
        
        // Then
        verify(mockGraphics, atLeastOnce()).setColor(Color.GREEN);
        verify(mockGraphics, atLeastOnce()).drawString(eq("P1"), anyInt(), anyInt());
        
        verify(mockGraphics, atLeastOnce()).setColor(Color.RED);
        verify(mockGraphics, atLeastOnce()).drawString(eq("P2"), anyInt(), anyInt());
    }
}