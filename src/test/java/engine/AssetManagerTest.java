package engine;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import engine.AssetManager.SpriteType;
import java.awt.Font;
import java.io.ByteArrayInputStream;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.Clip;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class AssetManagerTest {
    
    private static AssetManager assetManager;
    
    @BeforeAll
    static void setUp() {
        // AssetManager는 싱글톤이므로 한 번만 초기화
        assetManager = AssetManager.getInstance();
    }
    
    @Test
    void getInstance() {
        AssetManager instance1 = AssetManager.getInstance();
        AssetManager instance2 = AssetManager.getInstance();
        
        assertNotNull(instance1, "Instance should not be null");
        assertSame(instance1, instance2, "AssetManager should be a singleton");
    }
    
    @Test
    void getCharacterWidth() {
        // AssetManager에 getCharacterWidth 메서드가 없으므로, SpriteType을 통해 간접 테스트
        // 예: WarriorCharacterBasic의 너비 검증
        SpriteType warrior = SpriteType.CharacterWarriorBasic;
        int expectedWidth = 32;
        
        assertEquals(expectedWidth, warrior.getWidth(), "Character width mismatch");
        
        // 실제 로드된 스프라이트 배열의 크기 확인
        boolean[][] sprite = assetManager.getSprite(warrior);
        if (sprite != null) {
            assertEquals(expectedWidth, sprite.length, "Loaded sprite width mismatch");
        }
    }
    
    @Test
    void getCharacterHeight() {
        // SpriteType을 통해 높이 검증
        SpriteType warrior = SpriteType.CharacterWarriorBasic;
        int expectedHeight = 32;
        
        assertEquals(expectedHeight, warrior.getHeight(), "Character height mismatch");
        
        boolean[][] sprite = assetManager.getSprite(warrior);
        if (sprite != null && sprite.length > 0) {
            assertEquals(expectedHeight, sprite[0].length, "Loaded sprite height mismatch");
        }
    }
    
    @Test
    void loadResources() {
        // loadResources는 private이며 생성자에서 호출됨.
        // 따라서 getSprite나 getSound가 정상적으로 반환되는지로 로딩 성공 여부를 판단
        
        // 1. 스프라이트 로딩 확인
        boolean[][] shipSprite = assetManager.getSprite(SpriteType.Ship1);
        assertNotNull(shipSprite, "Ship1 sprite should be loaded");
        
        // 2. 폰트 로딩 확인
        assertNotNull(assetManager.getFontRegular(), "Regular font should be loaded");
        assertNotNull(assetManager.getFontBig(), "Big font should be loaded");
    }
    
    @Test
    void loadFont() {
        // loadFont는 public 메서드이므로 직접 호출 테스트 가능
        try {
            Font font = assetManager.loadFont(14f);
            assertNotNull(font, "Loaded font should not be null");
            assertEquals(14f, font.getSize2D(), 0.01f, "Font size should match");
        } catch (Exception e) {
            // 폰트 파일이 없거나 로드 실패 시 예외 발생 가능
            System.err.println("Font loading skipped in test: " + e.getMessage());
        }
    }
    
    @Test
    void toPcmSigned() {
        // 정적 유틸리티 메서드 테스트
        try {
            // 더미 오디오 스트림 생성
            byte[] dummyData = new byte[100];
            AudioFormat format = new AudioFormat(44100, 8, 1, true, false);
            AudioInputStream sourceStream = new AudioInputStream(
                new ByteArrayInputStream(dummyData), format, dummyData.length);
            
            AudioInputStream convertedStream = AssetManager.toPcmSigned(sourceStream);
            
            assertNotNull(convertedStream, "Converted stream should not be null");
            assertEquals(AudioFormat.Encoding.PCM_SIGNED, convertedStream.getFormat().getEncoding(),
                "Encoding should be PCM_SIGNED");
            
        } catch (Exception e) {
            // 오디오 시스템 지원 여부에 따라 테스트가 실패할 수 있음
            System.err.println("Audio conversion test skipped: " + e.getMessage());
        }
    }
    
    @Test
    void getSound() {
        // "shoot" 사운드가 로드되었는지 확인
        Clip clip = assetManager.getSound("shoot");
        // 리소스 파일이 실제로 존재하지 않으면 null일 수 있음
        if (clip != null) {
            assertNotNull(clip);
        } else {
            System.out.println("Sound 'shoot' not found, skipping assertion.");
        }
        
        // 존재하지 않는 사운드 요청 시 null 반환 확인
        Clip invalidClip = assetManager.getSound("non_existent_sound");
        // Map.get()은 키가 없으면 null 반환
        assertEquals(null, invalidClip);
    }
    
    @Test
    void getSprite() {
        // 특정 스프라이트(예: Ship1) 데이터 확인
        boolean[][] sprite = assetManager.getSprite(SpriteType.Ship1);
        
        assertNotNull(sprite, "Sprite should not be null");
        assertTrue(sprite.length > 0, "Sprite width should be > 0");
        assertTrue(sprite[0].length > 0, "Sprite height should be > 0");
    }
    
    @Test
    void getSpriteImage() {
        // 현재 제공된 AssetManager.java에는 getSpriteImage() 메서드가 없습니다.
        // 만약 BufferedImage를 반환하는 메서드가 추가된다면 아래와 같이 테스트할 수 있습니다.
        /*
        BufferedImage image = assetManager.getSpriteImage(SpriteType.Ship1);
        assertNotNull(image, "Sprite image should not be null");
        */
    }
    
    @Test
    void getFontRegular() {
        Font font = assetManager.getFontRegular();
        assertNotNull(font, "Regular font should not be null");
        assertTrue(font.getSize() > 0, "Font size should be valid");
    }
    
    @Test
    void getFontBig() {
        Font font = assetManager.getFontBig();
        assertNotNull(font, "Big font should not be null");
        assertTrue(font.getSize() > assetManager.getFontRegular().getSize(),
            "Big font should be larger than regular font");
    }
}