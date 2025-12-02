package engine.utils;

import engine.Core;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Logger;
import javax.imageio.ImageIO;

public class ImageLoader {
    
    private Logger LOGGER = Core.getLogger();
    
    private static final int baseWidth = 128;
    private static final int baseHeight = 128;
    
    /**
     * 리소스 경로에서 이미지를 원본 크기 그대로 로드합니다.
     */
    public static BufferedImage loadImage(String path) {
        BufferedImage image = null;
        try {
            InputStream is = ImageLoader.class.getClassLoader().getResourceAsStream(path);
            if (is == null) {
                System.err.println("[ImageLoader] Error: File not found - " + path);
                return createPlaceholderImage(baseWidth, baseHeight);
            }
            image = ImageIO.read(is);
            is.close();
        } catch (IOException e) {
            System.err.println("[ImageLoader] Error: Could not load image - " + path);
            e.printStackTrace();
            return createPlaceholderImage(baseWidth, baseHeight);
        }
        return image;
    }
    
    /**
     * Loads the image, resizes it to the specified width and height and returns it.
     *
     * @param path   File Path
     * @param width  Desired width in pixels
     * @param height Desired height in pixels
     * @return resized image
     */
    public static BufferedImage loadImage(String path, int width, int height) {
        BufferedImage originalImage = loadImage(path);
        
        BufferedImage resizedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        
        Graphics2D g = resizedImage.createGraphics();
        
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
            RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
        g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        
        g.drawImage(originalImage, 0, 0, width, height, null);
        g.dispose();
        
        return resizedImage;
    }
    
    /**
     * Loads the image, resizes it to the specified scale and returns it.
     *
     * @param path  File Path
     * @param scale The scale that set the image size magnification
     * @return Resized image
     */
    public static BufferedImage loadImage(String path, int scale) {
        BufferedImage originalImage = loadImage(path);
        
        int width = originalImage.getWidth() * scale;
        int height = originalImage.getHeight() * scale;
        
        BufferedImage resizedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        
        Graphics2D g = resizedImage.createGraphics();
        
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
            RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
        g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        
        g.drawImage(originalImage, 0, 0, width, height, null);
        g.dispose();
        
        return resizedImage;
    }
    
    /**
     * Create an alternate image to show in case of load failure.
     */
    public static BufferedImage createPlaceholderImage(int width, int height) {
        BufferedImage placeholder = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = placeholder.createGraphics();
        
        g.setColor(Color.MAGENTA);
        g.fillRect(0, 0, width, height);
        
        g.setColor(Color.BLACK);
        g.fillRect(width / 2, 0, width / 2, height / 2);
        g.fillRect(0, height / 2, width / 2, height / 2);
        
        g.dispose();
        return placeholder;
    }
}