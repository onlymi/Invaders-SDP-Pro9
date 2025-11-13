package engine;

import java.util.logging.Logger;
import javax.sound.sampled.Clip;
import javax.sound.sampled.FloatControl;

/**
 * Minimal sound manager for short SFX.
 */
public final class SoundManager {
    
    private static Logger LOGGER = null;
    private static SoundManager instance;
    
    private static Clip currentLoopClip = null;
    
    private SoundManager() {
        LOGGER = Core.getLogger();
    }
    
    /**
     * Returns shared instance of Sound Manager.
     *
     * @return Shared instance of Sound Manager.
     */
    public static SoundManager getInstance() {
        if (instance == null) {
            instance = new SoundManager();
        }
        return instance;
    }
    
    /**
     * 클립을 가져오고 볼륨을 설정하며 재생 위치를 처음으로 되돌립니다.
     *
     * @param soundName 사운드 파일 이름
     * @return 재생 준비가 완료된 Clip 객체
     */
    private static Clip prepareClip(String soundName) {
        // 1. AssetManager에서 미리 로드된 Clip을 가져옵니다.
        Clip clip = Core.getAssetManager().getSound(soundName);
        
        if (clip == null) {
            LOGGER.warning("Sound not found in AssetManager: " + soundName);
            return null;
        }
        
        // 2. 혹시 재생 중이라면 멈추고, 재생 위치를 처음(0)으로 되돌립니다.
        if (clip.isRunning()) {
            clip.stop();
        }
        clip.setFramePosition(0);
        
        // 3. Core에 저장된 현재 볼륨 설정을 클립에 적용합니다.
        if (clip.isControlSupported(FloatControl.Type.MASTER_GAIN)) {
            FloatControl gain = (FloatControl) clip.getControl(FloatControl.Type.MASTER_GAIN);
            float volumeDb = calculateVolumeDecibels(Core.getVolumeLevel());
            gain.setValue(Math.max(gain.getMinimum(), Math.min(gain.getMaximum(), volumeDb)));
        }
        
        return clip;
    }
    
    /**
     * Plays a short WAV from resources folder. Uses a new Clip per invocation for simplicity;
     * suitable for very short SFX.
     */
    public static void playOnce(String soundName) {
        try {
            Clip clip = prepareClip(soundName);
            
            if (clip != null) {
                clip.start();
            } else {
                LOGGER.warning("Sound not found in AssetManager: " + soundName);
            }
            
        } catch (Exception e) {
            LOGGER.info("Unable to play sound '" + soundName + "': " + e.getMessage());
        }
    }
    
    /**
     * Plays a .wav in a loop until {@link #loopStop()} is called.
     */
    public static void playLoop(String soundName) {
        // 모든 기존 루프 사운드 중지
        stopAllMusic();
        try {
            Clip clip = prepareClip(soundName);
            
            if (clip != null) {
                // 무한 반복 설정 후 재생합니다.
                clip.loop(Clip.LOOP_CONTINUOUSLY);
                clip.start();
                
                // 현재 재생 중인 클립을 추적
                currentLoopClip = clip;
                
            } else {
                LOGGER.warning("Looping sound not found in AssetManager: " + soundName);
            }
        } catch (Exception e) {
            LOGGER.info("Unable to loop sound '" + soundName + "': " + e.getMessage());
        }
    }
    
    /**
     * Stops and releases the current looped clip, if any.
     */
    public static void loopStop() {
        if (currentLoopClip != null) {
            try {
                currentLoopClip.stop();
                currentLoopClip.setFramePosition(0);
                // currentLoopClip.close();
            } catch (Exception e) {
                LOGGER.fine("Error stopping looped sound: " + e.getMessage());
            } finally {
                currentLoopClip = null;
            }
        }
    }
    
    /**
     * Stops all music (both looped and background music). Use this when transitioning between
     * screens to ensure no overlap.
     */
    public static void stopAllMusic() {
        loopStop(); // stops looped music
    }
    
    /**
     * Updates the volume of currently playing sounds. This should be called when the volume slider
     * is changed.
     */
    public static void updateVolume() {
        float volumeDb = calculateVolumeDecibels(Core.getVolumeLevel());
        
        // Update looped sound volume.
        if (currentLoopClip != null && currentLoopClip.isControlSupported(
            FloatControl.Type.MASTER_GAIN)) {
            FloatControl gain = (FloatControl) currentLoopClip.getControl(
                FloatControl.Type.MASTER_GAIN);
            gain.setValue(Math.max(gain.getMinimum(), Math.min(gain.getMaximum(), volumeDb)));
        }
    }
    
    /**
     * Calculates the volume in decibels based on the volume level (0-100). Volume level 100 = 0dB
     * (full volume), Volume level 0 = -80dB (silent)
     *
     * @param volumeLevel Volume level from 0 to 100
     * @return Volume in decibels
     */
    public static float calculateVolumeDecibels(int volumeLevel) {
        if (volumeLevel <= 0) {
            return -80.0f; // Silent
        }
        if (volumeLevel >= 100) {
            return 0.0f; // Full volume
        }
        
        // Convert percentage to decibels
        // Using logarithmic scale: dB = 20 * log10(volumeLevel/100)
        // But we'll use a simpler linear mapping for better user experience
        float ratio = volumeLevel / 100.0f;
        return (float) (20.0 * Math.log10(ratio));
    }
}


