package engine;

import java.awt.Font;
import java.awt.FontFormatException;
import java.awt.image.BufferedImage;
import java.io.File;
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
    
    private static final int characterWidth = 64;
    private static final int characterHeight = 64;
    
    public int getCharacterWidth() {
        return characterWidth;
    }
    
    public int getCharacterHeight() {
        return characterHeight;
    }
    
    /**
     * Sprite types.
     */
    public enum SpriteType {
        /**
         * Warrior Character.
         */
        CharacterWarriorBasic(SourceCategory.CHARACTER, "warrior/warrior_basic.png",
            characterWidth, characterHeight),
        CharacterWarriorAttack1(SourceCategory.CHARACTER, "warrior/warrior_basic.png",
            characterWidth, characterHeight),
        CharacterWarriorWalk1(SourceCategory.CHARACTER, "warrior/warrior_basic.png",
            characterWidth, characterHeight),
        CharacterWarriorWalk2(SourceCategory.CHARACTER, "warrior/warrior_basic.png",
            characterWidth, characterHeight),
        CharacterWarriorDefaultProjectile(SourceCategory.WEAPON,
            "warrior/default_attack_warrior.png", 32, characterHeight),
        /**
         * Archer Character.
         */
        CharacterArcherBasic(SourceCategory.CHARACTER, "archer/archer_basic.png",
            characterWidth, characterHeight),
        CharacterArcherShadow(SourceCategory.CHARACTER, "archer/archer_shadow.png",
            characterWidth, characterHeight),
        CharacterArcherLeftAttack(SourceCategory.CHARACTER,
            "archer/attack/archer_left_attack.png", characterWidth, characterHeight),
        CharacterArcherRightAttack(SourceCategory.CHARACTER,
            "archer/attack/archer_right_attack.png", characterWidth, characterHeight),
        CharacterArcherFrontAttack(SourceCategory.CHARACTER,
            "archer/attack/archer_front_attack.png", characterWidth, characterHeight),
        CharacterArcherBackAttack(SourceCategory.CHARACTER,
            "archer/attack/archer_back_attack.png", characterWidth, characterHeight),
        CharacterArcherStand(SourceCategory.CHARACTER, "archer/archer_stand.png",
            characterWidth, characterHeight),
        CharacterArcherLeftWalk(SourceCategory.CHARACTER, "archer/walk/archer_left_walk",
            characterWidth, characterHeight, 4),
        CharacterArcherRightWalk(SourceCategory.CHARACTER, "archer/walk/archer_right_walk",
            characterWidth, characterHeight, 4),
        CharacterArcherFrontWalk(SourceCategory.CHARACTER, "archer/walk/archer_front_walk",
            characterWidth, characterHeight, 4),
        CharacterArcherBackWalk(SourceCategory.CHARACTER, "archer/walk/archer_back_walk",
            characterWidth, characterHeight, 4),
        CharacterArcherGravestone(SourceCategory.CHARACTER, "archer/archer_gravestone.png",
            characterWidth, characterHeight),
        CharacterArcherDefaultProjectile(SourceCategory.WEAPON,
            "archer/default_attack_archer.png",
            14, 32),
        /**
         * Wizard Character.
         */
        CharacterWizardBasic(SourceCategory.CHARACTER, "wizard/wizard_basic.png",
            characterWidth, characterHeight),
        CharacterWizardAttack1(SourceCategory.CHARACTER, "wizard/wizard_basic.png",
            characterWidth, characterHeight),
        CharacterWizardWalk1(SourceCategory.CHARACTER, "wizard/wizard_basic.png",
            characterWidth, characterHeight),
        CharacterWizardWalk2(SourceCategory.CHARACTER, "wizard/wizard_basic.png",
            characterWidth, characterHeight),
        CharacterWizardDefaultProjectile(SourceCategory.WEAPON,
            "wizard/default_attack_wizard.png",
            16, 16),
        /**
         * Laser Character.
         */
        CharacterLaserBasic(SourceCategory.CHARACTER, "laser/laser_basic.png",
            characterWidth, characterHeight),
        CharacterLaserAttack1(SourceCategory.CHARACTER, "laser/laser_basic.png",
            characterWidth, characterHeight),
        CharacterLaserWalk1(SourceCategory.CHARACTER, "laser/laser_basic.png",
            characterWidth, characterHeight),
        CharacterLaserWalk2(SourceCategory.CHARACTER, "laser/laser_basic.png",
            characterWidth, characterHeight),
        CharacterLaserDefaultProjectile(SourceCategory.WEAPON,
            "laser/default_attack_laser.png",
            32, 32),
        /**
         * Electric Character.
         */
        CharacterElectricBasic(SourceCategory.CHARACTER, "electric/electric_basic.png",
            characterWidth, characterHeight),
        CharacterElectricAttack1(SourceCategory.CHARACTER, "electric/electric_basic.png",
            characterWidth, characterHeight),
        CharacterElectricWalk1(SourceCategory.CHARACTER, "electric/electric_basic.png",
            characterWidth, characterHeight),
        CharacterElectricWalk2(SourceCategory.CHARACTER, "electric/electric_basic.png",
            characterWidth, characterHeight),
        CharacterElectricDefaultProjectile(SourceCategory.WEAPON,
            "electric/default_attack_electric.png",
            32, 32),
        /**
         * Bomber Character.
         */
        CharacterBomberBasic(SourceCategory.CHARACTER, "bomber/bomber_basic.png",
            characterWidth, characterHeight),
        CharacterBomberAttack1(SourceCategory.CHARACTER, "bomber/bomber_basic.png",
            characterWidth, characterHeight),
        CharacterBomberWalk1(SourceCategory.CHARACTER, "bomber/bomber_basic.png",
            characterWidth, characterHeight),
        CharacterBomberWalk2(SourceCategory.CHARACTER, "bomber/bomber_basic.png",
            characterWidth, characterHeight),
        CharacterBomberDefaultProjectile(SourceCategory.WEAPON,
            "bomber/default_attack_bomber.png",
            32, 32),
        /**
         * Healer Character.
         */
        CharacterHealerBasic(SourceCategory.CHARACTER, "healer/healer_basic.png",
            characterWidth, characterHeight),
        CharacterHealerAttack1(SourceCategory.CHARACTER, "healer/healer_basic.png",
            characterWidth, characterHeight),
        CharacterHealerWalk1(SourceCategory.CHARACTER, "healer/healer_basic.png",
            characterWidth, characterHeight),
        CharacterHealerWalk2(SourceCategory.CHARACTER, "healer/healer_basic.png",
            characterWidth, characterHeight),
        
        BossMainBody(SourceCategory.ENEMY, "boss_main.png", 240, 160),
        
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
        PlayerBullet(SourceCategory.BULLET, 3, 5),
        /**
         * GasterBlaster of boss.
         */
        GasterBlaster(SourceCategory.BULLET, 128, 128),
        /**
         * Enemy bullet.
         */
        EnemyBullet(SourceCategory.BULLET, 3, 5),
        /**
         * Boss bullet.
         */
        BossBullet(SourceCategory.BULLET, 5, 5),
        /**
         * Boss laser.
         */
        BigLaserBeam(SourceCategory.BULLET, 11, 20),
        /**
         * Enemy A.
         */
        EnemyA_Move(SourceCategory.ENEMY, "enemy_type_a/Enemy_typeA.png", 48, 48),
        EnemyA_Attack(SourceCategory.ENEMY, "enemy_type_a/Enemy_typeA_attack.png", 48, 48),
        EnemyA_Weapon(SourceCategory.WEAPON, "enemy/typeA_weapon.png", 36, 36),
        /**
         * Enemy B.
         */
        EnemyB_Move(SourceCategory.ENEMY, "enemy_type_b/Enemy_typeB.png", 48, 48),
        EnemyB_Weapon(SourceCategory.WEAPON, "enemy/typeB_weapon.png", 24, 10),
        /**
         * Enemy C.
         */
        EnemyC_move(SourceCategory.ENEMY, "enemy_type_c/Enemy_typeC.png", 48, 48),
        EnemyC_attack(SourceCategory.ENEMY, "enemy_type_c/Enemy_typeC_attack.png", 48, 48),
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
        ItemScoreBooster(SourceCategory.ITEM, 5, 5),
        ItemMoveSpeedUp(SourceCategory.ITEM, 5, 5),
        ItemTimeFreeze(SourceCategory.ITEM, 5, 5),
        ItemTimeSlow(SourceCategory.ITEM, 5, 5),
        ItemDash(SourceCategory.ITEM, 5, 5),
        ItemPetGun(SourceCategory.ITEM, 5, 5),
        ItemShield(SourceCategory.ITEM, 5, 5);
        
        // Enum이 자신의 정보를 저장할 변수들
        private final SourceCategory category;
        private final String filename;
        private final int width;
        private final int height;
        private final boolean isImage;
        private final int frameCount;
        
        SpriteType(SourceCategory category, String filename, int width, int height) {
            this(category, filename, width, height, 1);
        }
        
        SpriteType(SourceCategory category, String filename, int width, int height,
            int frameCount) {
            this.category = category;
            this.filename = filename;
            this.width = width;
            this.height = height;
            this.isImage = true;
            this.frameCount = frameCount;
        }
        
        SpriteType(SourceCategory category, int width, int height) {
            this.category = category;
            this.filename = null;
            this.width = width;
            this.height = height;
            this.isImage = false;
            this.frameCount = 1;
        }
        
        // Getter 메서드
        public SourceCategory getCategory() {
            return this.category;
        }
        
        public String getFilename() {
            return this.filename;
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
        
        public int getFrameCount() {
            return this.frameCount;
        }
    }
    
    private static AssetManager instance;
    private static final Logger LOGGER = Core.getLogger();
    private static final FileManager fileManager = Core.getFileManager();
    
    Map<SpriteType, boolean[][]> spriteMap;
    Map<SpriteType, BufferedImage> spriteImageMap;
    Map<SpriteType, BufferedImage[]> animationMap;
    HashMap<String, Clip> soundMap;
    HashMap<String, File> csvDataMap;
    private Font fontRegular;
    private Font fontBig;
    
    private AssetManager() {
        LOGGER.info("Started loading resources.");
        
        try {
            spriteMap = new LinkedHashMap<>();
            spriteImageMap = new LinkedHashMap<>();
            animationMap = new LinkedHashMap<>();
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
            soundMap.put("enemy_A_attack_sound", loadSound("sound/swing_weapon.wav"));
            soundMap.put("laser_big", loadSound("sound/shoot_enemies.wav"));
            soundMap.put("boss_hit", loadSound("sound/invader_killed.wav"));
            
            LOGGER.info("Finished loading the sounds.");
        } catch (Exception e) {
            LOGGER.warning("Sound loading failed.");
        }
        
        try {
            csvDataMap = new HashMap<String, File>();
            // 모든 CSV 파일을 미리 로드
            csvDataMap.put("user_acct_info", loadCsv("game_data/user_acct_info.csv"));
            csvDataMap.put("user_stats", loadCsv("game_data/user_stats.csv"));
            csvDataMap.put("key_config", loadCsv("game_data/key_config.csv"));
            csvDataMap.put("1p_scores", loadCsv("game_data/1p_scores.csv"));
            csvDataMap.put("2p_scores", loadCsv("game_data/2p_scores.csv"));
            csvDataMap.put("item_db", loadCsv("game_data/item_db.csv"));
            csvDataMap.put("achievement", loadCsv("game_data/achievement.csv"));
            csvDataMap.put("level", loadCsv("game_data/level.csv"));
            
            LOGGER.info("Finished loading the game data file.");
        } catch (Exception e) {
            LOGGER.warning("Game data file loading failed.");
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
                    String basePath = type.getCategory().getFilePath() + type.getFilename();
                    int targetWidth = type.getWidth();
                    int targetHeight = type.getHeight();
                    
                    BufferedImage img;
                    
                    if (type.getFrameCount() == 1) {
                        img = engine.utils.ImageLoader.loadImage(
                            basePath, targetWidth, targetHeight
                        );
                        spriteImageMap.put(type, img);
                    } else {
                        BufferedImage[] frames = new BufferedImage[type.getFrameCount()];
                        
                        for (int i = 0; i < type.getFrameCount(); i++) {
                            String fullPath = basePath + (i + 1) + ".png";
                            
                            frames[i] = engine.utils.ImageLoader.loadImage(fullPath, targetWidth,
                                targetHeight);
                        }
                        animationMap.put(type, frames);
                    }
                    
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
     * @param resourcePath 리소스 폴더 내의 사운드 파일 경로
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
    public static AudioInputStream toPcmSigned(AudioInputStream source) {
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
    
    /**
     * Loads a CSV file from the given path.
     *
     * @param filePath Path to the CSV file.
     * @return File object for the CSV file.
     */
    private File loadCsv(String filePath) {
        String rootDir = System.getProperty("user.dir");
        return new File(rootDir + File.separator + filePath);
    }
    
    public Font getFontRegular() {
        return fontRegular;
    }
    
    public Font getFontBig() {
        return fontBig;
    }
    
    public File getCsvData(String fileName) {
        return csvDataMap.get(fileName);
    }
    
    public Clip getSound(String soundName) {
        return soundMap.get(soundName);
    }
    
    public boolean[][] getSpriteMap(SpriteType type) {
        return spriteMap.get(type);
    }
    
    public BufferedImage getSpriteImage(SpriteType type) {
        return spriteImageMap.get(type);
    }
    
    public BufferedImage[] getAnimation(SpriteType type) {
        return animationMap.get(type);
    }
}
