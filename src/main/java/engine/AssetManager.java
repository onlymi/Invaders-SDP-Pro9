package engine;

import java.awt.Font;
import java.awt.FontFormatException;
import java.awt.image.BufferedImage;
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
 * Classes that load, store, and manage all the assets (Sprite, fonts, etc.) needed for the game.
 */
public final class AssetManager {

    /**
     * Enum to distinguish between graphical source files.
     */
    private enum SourceCategory {
        WARRIOR_CHARACTER("image/character/warrior/"),
        ARCHER_CHARACTER("image/character/archer/"),
        WIZARD_CHARACTER("image/character/wizard/"),
        LASER_CHARACTER("image/character/laser/"),
        ELECTRIC_CHARACTER("image/character/electric/"),
        BOMBER_CHARACTER("image/character/bomber/"),
        HEALER_CHARACTER("image/character/healer/"),
        
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
    private static int characterWidth = 32;
    private static int characterHeight = 32;
    
    public int getCharacterWidth() {
        return characterWidth;
    }
    
    public int getCharacterHeight() {
        return characterHeight;
    }
    
    public enum SpriteType {
        /**
         * Warrior Character.
         */
        CharacterWarriorBasic(SourceCategory.WARRIOR_CHARACTER, "warrior_basic.png",
            characterWidth, characterHeight),
        CharacterWarriorAttack1(SourceCategory.WARRIOR_CHARACTER, "warrior_basic.png",
            characterWidth, characterHeight),
        CharacterWarriorWalk1(SourceCategory.WARRIOR_CHARACTER, "warrior_basic.png",
            characterWidth, characterHeight),
        CharacterWarriorWalk2(SourceCategory.WARRIOR_CHARACTER, "warrior_basic.png",
            characterWidth, characterHeight),
        /**
         * Archer Character.
         */
        CharacterArcherBasic(SourceCategory.ARCHER_CHARACTER, "archer_basic.png",
            characterWidth, characterHeight),
        CharacterArcherShadow(SourceCategory.ARCHER_CHARACTER, "archer_shadow.png",
            characterWidth, characterHeight),
        CharacterArcherAttack1(SourceCategory.ARCHER_CHARACTER, "archer_basic.png",
            characterWidth, characterHeight),
        CharacterArcherWalk1(SourceCategory.ARCHER_CHARACTER, "archer_basic.png",
            characterWidth, characterHeight),
        CharacterArcherWalk2(SourceCategory.ARCHER_CHARACTER, "archer_basic.png",
            characterWidth, characterHeight),
        /**
         * Wizard Character.
         */
        CharacterWizardBasic(SourceCategory.WIZARD_CHARACTER, "wizard_basic.png",
            characterWidth, characterHeight),
        CharacterWizardAttack1(SourceCategory.WIZARD_CHARACTER, "wizard_basic.png",
            characterWidth, characterHeight),
        CharacterWizardWalk1(SourceCategory.WIZARD_CHARACTER, "wizard_basic.png",
            characterWidth, characterHeight),
        CharacterWizardWalk2(SourceCategory.WIZARD_CHARACTER, "wizard_basic.png",
            characterWidth, characterHeight),
        /**
         * Laser Character.
         */
        CharacterLaserBasic(SourceCategory.LASER_CHARACTER, "laser_basic.png",
            characterWidth, characterHeight),
        CharacterLaserAttack1(SourceCategory.LASER_CHARACTER, "laser_basic.png",
            characterWidth, characterHeight),
        CharacterLaserWalk1(SourceCategory.LASER_CHARACTER, "laser_basic.png",
            characterWidth, characterHeight),
        CharacterLaserWalk2(SourceCategory.LASER_CHARACTER, "laser_basic.png",
            characterWidth, characterHeight),
        /**
         * Electric Character.
         */
        CharacterElectricBasic(SourceCategory.ELECTRIC_CHARACTER, "electric_basic.png",
            characterWidth, characterHeight),
        CharacterElectricAttack1(SourceCategory.ELECTRIC_CHARACTER, "electric_basic.png",
            characterWidth, characterHeight),
        CharacterElectricWalk1(SourceCategory.ELECTRIC_CHARACTER, "electric_basic.png",
            characterWidth, characterHeight),
        CharacterElectricWalk2(SourceCategory.ELECTRIC_CHARACTER, "electric_basic.png",
            characterWidth, characterHeight),
        /**
         * Bomber Character.
         */
        CharacterBomberBasic(SourceCategory.BOMBER_CHARACTER, "bomber_basic.png",
            characterWidth, characterHeight),
        CharacterBomberAttack1(SourceCategory.BOMBER_CHARACTER, "bomber_basic.png",
            characterWidth, characterHeight),
        CharacterBomberWalk1(SourceCategory.BOMBER_CHARACTER, "bomber_basic.png",
            characterWidth, characterHeight),
        CharacterBomberWalk2(SourceCategory.BOMBER_CHARACTER, "bomber_basic.png",
            characterWidth, characterHeight),
        /**
         * Healer Character.
         */
        CharacterHealerBasic(SourceCategory.HEALER_CHARACTER, "healer_basic.png",
            characterWidth, characterHeight),
        CharacterHealerAttack1(SourceCategory.HEALER_CHARACTER, "healer_basic.png",
            characterWidth, characterHeight),
        CharacterHealerWalk1(SourceCategory.HEALER_CHARACTER, "healer_basic.png",
            characterWidth, characterHeight),
        CharacterHealerWalk2(SourceCategory.HEALER_CHARACTER, "healer_basic.png",
            characterWidth, characterHeight),
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
        BossShip1(SourceCategory.BOSS, 21, 10),
        BossShip2(SourceCategory.BOSS, 21, 10),
        BossShip3(SourceCategory.BOSS, 21, 10),
        /**
         * Destroyed enemy ship.
         */
        Explosion(SourceCategory.MUTUAL, 13, 7),
        /**
         * Heart for lives display.
         */
        Heart(SourceCategory.MUTUAL, 11, 10),
        /**
         * Item Graphics Temp.
         */
        ItemScore(SourceCategory.ITEM, 5, 5),
        ItemCoin(SourceCategory.ITEM, 5, 5),
        ItemHeal(SourceCategory.ITEM, 5, 5),
        ItemTripleShot(SourceCategory.ITEM, 5, 5),
        ItemScoreBooster(SourceCategory.ITEM, 5, 5),
        ItemBulletSpeedUp(SourceCategory.ITEM, 5, 5),
        ItemMoveSpeedUp(SourceCategory.ITEM, 5, 5),
        ItemTimeFreeze(SourceCategory.ITEM, 5, 5);

        // Enum이 자신의 정보를 저장할 변수들
        private final SourceCategory category;
        private final String filename;
        private final int width;
        private final int height;
        private final boolean isImage;
        
        SpriteType(SourceCategory category, String filename, int width, int height) {
            this.category = category;
            this.filename = filename;
            this.width = width;
            this.height = height;
            this.isImage = true;
        }
        
        SpriteType(SourceCategory category, int width, int height) {
            this.category = category;
            this.filename = null;
            this.width = width;
            this.height = height;
            this.isImage = false;
        }
        
        public SourceCategory getCategory() {
            return this.category;
        }

        public int getWidth() {
            return this.width;
        }

        public int getHeight() {
            return this.height;
        }
        
        public boolean isImage() {
            return this.isImage;
        }
        
        public String getFilename() {
            return this.filename;
        }
    }

    private static AssetManager instance;
    private static final Logger LOGGER = Core.getLogger();
    private static final FileManager fileManager = Core.getFileManager();

    Map<SpriteType, boolean[][]> spriteMap;
    Map<SpriteType, BufferedImage> spriteImageMap;
    HashMap<String, Clip> soundMap;
    private Font fontRegular;
    private Font fontBig;

    private AssetManager() {
        LOGGER.info("Started loading resources.");

        try {
            spriteMap = new LinkedHashMap<>();
            spriteImageMap = new LinkedHashMap<>();
            this.loadResources();
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
    
    private boolean isPixelMapCategory(SourceCategory category) {
        String path = category.getFilePath();
        return !path.contains("character");
    }
    
    public void loadResources() throws IOException {
        Map<SourceCategory, InputStream> streamMap = new EnumMap<>(SourceCategory.class);
        
        for (SourceCategory category : SourceCategory.values()) {
            if (isPixelMapCategory(category)) {
                streamMap.put(category, AssetManager.class.getClassLoader()
                    .getResourceAsStream(category.getFilePath()));
            }
        }
        
        try {
            for (SpriteType type : SpriteType.values()) {
                if (type.isImage()) {
                    String fullPath = type.getCategory().getFilePath() + type.getFilename();
                    int targetWidth = type.getWidth() * 2;
                    int targetHeight = type.getHeight() * 2;
                    
                    BufferedImage img = engine.utils.ImageLoader.loadImage(fullPath, targetWidth,
                        targetHeight);
                    spriteImageMap.put(type, img);
                    
                } else {
                    boolean[][] data = new boolean[type.getWidth()][type.getHeight()];
                    InputStream stream = streamMap.get(type.getCategory());
                    
                    if (stream != null) {
                        char c;
                        for (int i = 0; i < type.getWidth(); i++) {
                            for (int j = 0; j < type.getHeight(); j++) {
                                do {
                                    c = (char) stream.read();
                                } while (c != '0' && c != '1');
                                data[i][j] = (c == '1');
                            }
                        }
                        spriteMap.put(type, data);
                    }
                }
            }
        } finally {
            for (InputStream is : streamMap.values()) {
                if (is != null) {
                    is.close();
                }
            }
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
            font = Font.createFont(Font.TRUETYPE_FONT, inputStream).deriveFont(size);
        } finally {
            if (inputStream != null) {
                inputStream.close();
            }
        }

        return font;
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
    
    public boolean[][] getSpriteMap(SpriteType type) {
        return spriteMap.get(type);
    }

    public boolean[][] getSprite(SpriteType type) {
        return spriteMap.get(type);
    }
    
    public BufferedImage getSpriteImage(SpriteType type) {
        return spriteImageMap.get(type);
    }
    
    public Font getFontRegular() {
        return fontRegular;
    }

    public Font getFontBig() {
        return fontBig;
    }
}
