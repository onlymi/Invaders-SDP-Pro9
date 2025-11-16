package engine;

import java.awt.Font;
import java.awt.FontFormatException;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.logging.Logger;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;

/**
 * 게임에 필요한 모든 Asset(스프라이트, 폰트 등)을 로드하고 저장 및 관리하는 클래스
 */
public final class AssetManager {
    
    /**
     * 6개의 그래픽 소스 파일을 구분하기 위한 Enum
     */
    private enum SourceCategory {
        WARRIOR_CHARACTER("graphics/character/warrior_graphics"),
        ARCHER_CHARACTER("graphics/character/archer_graphics"),
        WIZARD_CHARACTER("graphics/character/wizard_graphics"),
        LASER_CHARACTER("graphics/character/laser_graphics"),
        ELECTRIC_CHARACTER("graphics/character/electric_graphics"),
        BOMBER_CHARACTER("graphics/character/bomber_graphics"),
        HEALER_CHARACTER("graphics/character/healer_graphics"),
        
        PLAYERSHIP("graphics/playersShip_graphics"),
        ENEMY("graphics/enemy_graphics"),
        BOSS("graphics/boss_graphics"),
        BULLET("graphics/bullet_graphics"),
        MUTUAL("graphics/mutual_graphics"),
        ITEM("graphics/item_graphics");
        
        private final String filePath;
        
        SourceCategory(String path) {
            this.filePath = path;
        }
        
        public String getFilePath() {
            return this.filePath;
        }
    }
    
    /**
     * Sprite types.
     */
    public enum SpriteType {
        /**
         * Warrior Character.
         */
        CharacterWarriorBasic(SourceCategory.WARRIOR_CHARACTER, 19, 15),
        CharacterWarriorAttack1(SourceCategory.WARRIOR_CHARACTER, 19, 15),
        CharacterWarriorWalk1(SourceCategory.WARRIOR_CHARACTER, 19, 15),
        CharacterWarriorWalk2(SourceCategory.WARRIOR_CHARACTER, 19, 15),
        /**
         * Archer Character.
         */
        CharacterArcherBasic(SourceCategory.ARCHER_CHARACTER, 17, 13),
        CharacterArcherAttack1(SourceCategory.ARCHER_CHARACTER, 17, 13),
        CharacterArcherWalk1(SourceCategory.ARCHER_CHARACTER, 17, 13),
        CharacterArcherWalk2(SourceCategory.ARCHER_CHARACTER, 17, 13),
        /**
         * Wizard Character.
         */
        CharacterWizardBasic(SourceCategory.WIZARD_CHARACTER, 19, 15),
        CharacterWizardAttack1(SourceCategory.WIZARD_CHARACTER, 19, 15),
        CharacterWizardWalk1(SourceCategory.WIZARD_CHARACTER, 19, 15),
        CharacterWizardWalk2(SourceCategory.WIZARD_CHARACTER, 19, 15),
        /**
         * Laser Character.
         */
        CharacterLaserBasic(SourceCategory.LASER_CHARACTER, 19, 17),
        CharacterLaserAttack1(SourceCategory.LASER_CHARACTER, 19, 17),
        CharacterLaserWalk1(SourceCategory.LASER_CHARACTER, 19, 17),
        CharacterLaserWalk2(SourceCategory.LASER_CHARACTER, 19, 17),
        /**
         * Electric Character.
         */
        CharacterElectricBasic(SourceCategory.ELECTRIC_CHARACTER, 18, 18),
        CharacterElectricAttack1(SourceCategory.ELECTRIC_CHARACTER, 18, 18),
        CharacterElectricWalk1(SourceCategory.ELECTRIC_CHARACTER, 18, 18),
        CharacterElectricWalk2(SourceCategory.ELECTRIC_CHARACTER, 18, 18),
        /**
         * Bomber Character.
         */
        CharacterBomberBasic(SourceCategory.BOMBER_CHARACTER, 19, 16),
        CharacterBomberAttack1(SourceCategory.BOMBER_CHARACTER, 19, 16),
        CharacterBomberWalk1(SourceCategory.BOMBER_CHARACTER, 19, 16),
        CharacterBomberWalk2(SourceCategory.BOMBER_CHARACTER, 19, 16),
        /**
         * Healer Character.
         */
        CharacterHealerBasic(SourceCategory.HEALER_CHARACTER, 17, 15),
        CharacterHealerAttack1(SourceCategory.HEALER_CHARACTER, 17, 15),
        CharacterHealerWalk1(SourceCategory.HEALER_CHARACTER, 17, 15),
        CharacterHealerWalk2(SourceCategory.HEALER_CHARACTER, 17, 15),
        /**
         * Player ship.
         */
        Ship1(SourceCategory.PLAYERSHIP, 13, 8),
        Ship2(SourceCategory.PLAYERSHIP, 13, 8),
        Ship3(SourceCategory.PLAYERSHIP, 13, 8),
        Ship4(SourceCategory.PLAYERSHIP, 13, 8),
        /**
         * Destroyed player ship.
         */
        ShipDestroyed1(SourceCategory.PLAYERSHIP, 13, 8),
        ShipDestroyed2(SourceCategory.PLAYERSHIP, 13, 8),
        ShipDestroyed3(SourceCategory.PLAYERSHIP, 13, 8),
        ShipDestroyed4(SourceCategory.PLAYERSHIP, 13, 8),
        /**
         * Player bullet.
         */
        Bullet(SourceCategory.BULLET, 3, 5),
        /**
         * Enemy bullet.
         */
        EnemyBullet(SourceCategory.BULLET, 3, 5),
        /**
         * First enemy ship - first form.
         */
        EnemyShipA1(SourceCategory.ENEMY, 12, 8),
        /**
         * First enemy ship - second form.
         */
        EnemyShipA2(SourceCategory.ENEMY, 12, 8),
        /**
         * Second enemy ship - first form.
         */
        EnemyShipB1(SourceCategory.ENEMY, 12, 8),
        /**
         * Second enemy ship - second form.
         */
        EnemyShipB2(SourceCategory.ENEMY, 12, 8),
        /**
         * Third enemy ship - first form.
         */
        EnemyShipC1(SourceCategory.ENEMY, 12, 8),
        /**
         * Third enemy ship - second form.
         */
        EnemyShipC2(SourceCategory.ENEMY, 12, 8),
        /**
         * Bonus ship.
         */
        EnemyShipSpecial(SourceCategory.ENEMY, 16, 7),
        /**
         * Boss ship.
         */
        BossEnemy1(SourceCategory.BOSS, 21, 10),
        BossEnemy2(SourceCategory.BOSS, 21, 10),
        BossEnemy3(SourceCategory.BOSS, 21, 10),
        /**
         * Destroyed enemy ship.
         */
        Explosion(SourceCategory.MUTUAL, 13, 7),
        /**
         * Heart for lives display.
         */
        Heart(SourceCategory.MUTUAL, 11, 10),
        /**
         * Item Graphics Temp
         */
        ItemScore(SourceCategory.ITEM, 5, 5),
        ItemCoin(SourceCategory.ITEM, 5, 5),
        ItemHeal(SourceCategory.ITEM, 5, 5),
        ItemTripleShot(SourceCategory.ITEM, 5, 7),
        ItemScoreBooster(SourceCategory.ITEM, 5, 5),
        ItemBulletSpeedUp(SourceCategory.ITEM, 5, 5);
        
        // Enum이 자신의 정보를 저장할 변수들
        private final SourceCategory category;
        private final int width;
        private final int height;
        
        // Enum 생성자
        SpriteType(SourceCategory category, int width, int height) {
            this.category = category;
            this.width = width;
            this.height = height;
        }
        
        // Getter 메서드
        public SourceCategory getCategory() {
            return this.category;
        }
        
        public int getWidth() {
            return this.width;
        }
        
        public int getHeight() {
            return this.height;
        }
    }
    
    private static AssetManager instance;
    private static final Logger LOGGER = Core.getLogger();
    private static final FileManager fileManager = Core.getFileManager();
    
    Map<SpriteType, boolean[][]> spriteMap;
    HashMap<String, Clip> soundMap;
    private Font fontRegular;
    private Font fontBig;
    
    private AssetManager() {
        LOGGER.info("Started loading resources.");
        
        try {
            spriteMap = new LinkedHashMap<SpriteType, boolean[][]>();
            for (SpriteType type : SpriteType.values()) {
                spriteMap.put(type, new boolean[type.getWidth()][type.getHeight()]);
            }
            // Sprite graphics loading
            this.loadSprite(spriteMap);
            LOGGER.info("Finished loading the sprites.");
            
            // Font loading
            fontRegular = this.loadFont(14f);
            fontBig = this.loadFont(24f);
            LOGGER.info("Finished loading the fonts.");
            
        } catch (IOException e) {
            LOGGER.warning("Loading failed.");
        } catch (FontFormatException e) {
            LOGGER.warning("Font formating failed.");
        }
        
        try {
            soundMap = new HashMap<String, Clip>();
            // 모든 사운드 파일을 미리 로드
            soundMap.put("title_sound", loadSound("sound/title_sound.wav"));
            soundMap.put("game_theme", loadSound("sound/game_theme.wav"));
            soundMap.put("select", loadSound("sound/select.wav"));
            soundMap.put("hover", loadSound("sound/hover.wav"));
            soundMap.put("count_down_sound", loadSound("sound/count_down_sound.wav"));
            soundMap.put("shoot_enemies", loadSound("sound/shoot_enemies.wav"));
            soundMap.put("invader_killed", loadSound("sound/invader_killed.wav"));
            soundMap.put("achievement", loadSound("sound/achievement.wav"));
            soundMap.put("shoot", loadSound("sound/shoot.wav"));
            soundMap.put("shooting", loadSound("sound/shooting.wav"));
            soundMap.put("explosion", loadSound("sound/explosion.wav"));
            soundMap.put("special_ship_sound", loadSound("sound/special_ship_sound.wav"));
            soundMap.put("win", loadSound("sound/win.wav"));
            soundMap.put("lose", loadSound("sound/lose.wav"));
            
            LOGGER.info("Finished loading the sounds.");
        } catch (Exception e) {
            LOGGER.warning("Sound loading failed.");
        }
    }
    
    /**
     * Returns shared instance of AssetManager.
     *
     * @return Shared instance of AssetManager.
     */
    public static AssetManager getInstance() {
        if (instance == null) {
            instance = new AssetManager();
        }
        return instance;
    }
    
    /**
     * Loads a font of a given size.
     *
     * @param size Point size of the font.
     * @return New font.
     * @throws IOException         In case of loading problems.
     * @throws FontFormatException In case of incorrect font format.
     */
    public Font loadFont(final float size) throws IOException,
        FontFormatException {
        InputStream inputStream = null;
        Font font;
        
        try {
            // Font loading.
            inputStream = FileManager.class.getClassLoader().getResourceAsStream("font/font.ttf");
            font = Font.createFont(Font.TRUETYPE_FONT, inputStream).deriveFont(
                size);
        } finally {
            if (inputStream != null) {
                inputStream.close();
            }
        }
        
        return font;
    }
    
    /**
     * Loads sprites from disk.
     *
     * @param spriteMap Mapping of sprite type and empty boolean matrix that will contain the
     *                  image.
     * @throws IOException In case of loading problems.
     */
    public void loadSprite(final Map<SpriteType, boolean[][]> spriteMap) throws IOException {
        Map<SourceCategory, InputStream> streamMap = new EnumMap<>(SourceCategory.class);
        for (SourceCategory category : SourceCategory.values()) {
            streamMap.put(category,
                AssetManager.class.getClassLoader().getResourceAsStream(category.getFilePath()));
        }
        
        try {
            char c;
            for (Map.Entry<SpriteType, boolean[][]> sprite : spriteMap.entrySet()) {
                SpriteType type = sprite.getKey();
                boolean[][] data = sprite.getValue();
                InputStream selectedStream = streamMap.get(type.getCategory());
                for (int i = 0; i < sprite.getValue().length; i++) {
                    for (int j = 0; j < sprite.getValue()[i].length; j++) {
                        do {
                            c = (char) selectedStream.read();
                        } while (c != '0' && c != '1');
                        
                        data[i][j] = (c == '1');
                    }
                }
                LOGGER.fine("Sprite " + sprite.getKey() + " loaded.");
            }
        } finally {
            for (InputStream stream : streamMap.values()) {
                if (stream != null) {
                    stream.close();
                }
            }
        }
    }
    
    /**
     * 지정된 리소스 경로에서 오디오 파일을 읽어와 재생 준비가 완료된 Clip 객체로 반환합니다.
     *
     * @param resourcePath 리소스 폴더 내의 사운드 파일 경로 (예: "sound/shoot.wav")
     * @return 메모리에 로드된 Clip 객체
     * @throws UnsupportedAudioFileException 오디오 파일 형식이 지원되지 않는 경우
     * @throws IOException                   파일 입출력 오류가 발생한 경우
     * @throws LineUnavailableException      오디오 라인을 열 수 없는 경우
     */
    private Clip loadSound(String resourcePath)
        throws UnsupportedAudioFileException, IOException, LineUnavailableException {
        AudioInputStream audioStream = openAudioStream(resourcePath);
        if (audioStream == null) {
            throw new FileNotFoundException("Audio resource not found: " + resourcePath);
        }
        
        audioStream = toPcmSigned(audioStream);
        
        DataLine.Info info = new DataLine.Info(Clip.class, audioStream.getFormat());
        Clip clip = (Clip) AudioSystem.getLine(info);
        clip.open(audioStream);
        
        return clip;
    }
    
    /**
     * Opens an audio stream from classpath resources or absolute/relative file path.
     */
    private static AudioInputStream openAudioStream(String resourcePath)
        throws UnsupportedAudioFileException, IOException {
        InputStream in = SoundManager.class.getClassLoader().getResourceAsStream(resourcePath);
        if (in != null) {
            return AudioSystem.getAudioInputStream(in);
        }
        // Fallback to file system path for developer/local runs
        try (FileInputStream fis = new FileInputStream(resourcePath)) {
            return AudioSystem.getAudioInputStream(fis);
        } catch (FileNotFoundException e) {
            LOGGER.fine("Audio resource not found: " + resourcePath);
            return null;
        }
    }
    
    /**
     * Ensures the audio stream is PCM_SIGNED for Clip compatibility on all JVMs.
     */
    public static AudioInputStream toPcmSigned(AudioInputStream source)
        throws UnsupportedAudioFileException, IOException {
        AudioFormat format = source.getFormat();
        if (format.getEncoding() == AudioFormat.Encoding.PCM_SIGNED) {
            return source;
        }
        
        AudioFormat targetFormat = new AudioFormat(
            AudioFormat.Encoding.PCM_SIGNED,
            format.getSampleRate(),
            16,
            format.getChannels(),
            format.getChannels() * 2,
            format.getSampleRate(),
            false
        );
        return AudioSystem.getAudioInputStream(targetFormat, source);
    }
    
    public Clip getSound(String soundName) {
        return soundMap.get(soundName);
    }
    
    public boolean[][] getSprite(SpriteType type) {
        return spriteMap.get(type);
    }
    
    public Font getFontRegular() {
        return fontRegular;
    }
    
    public Font getFontBig() {
        return fontBig;
    }
}
