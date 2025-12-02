package animations;

import engine.AssetManager.SpriteType;
import java.awt.Color;
import java.util.Random;

public class Explosion {
    
    private static final int NUM_PARTICLES = 40;
    private Particle[] particles;
    private boolean active;
    private boolean enemy;
    private int size;
    
    private SpriteType spriteType;
    private int spriteDuration;
    
    private static final Random random = new Random();
    
    public Explosion(double startX, double startY, Color customColor) {
        this.active = true;
        this.spriteType = null;
        this.size = 5;
        initParticles(startX, startY, customColor);
    }
    
    public Explosion(double startX, double startY, boolean enemy, boolean finalExplosion) {
        initParticles(startX, startY, enemy, finalExplosion);
        this.spriteType = null;
    }
    
    public Explosion(double startX, double startY, SpriteType spriteType, int duration) {
        initParticles(startX, startY, true, false);
        
        this.spriteType = spriteType;
        this.spriteDuration = duration;
        this.active = true;
    }
    
    private void initParticles(double startX, double startY, boolean enemy, boolean finalExplosion) {
        this.particles = new Particle[NUM_PARTICLES];
        this.active = true;
        this.enemy = enemy;
        if (finalExplosion) {
            this.size = 20;
        } else {
            this.size = 4;
        }
        
        for (int i = 0; i < NUM_PARTICLES; i++) {
            double angle = 2 * Math.PI * random.nextDouble();
            double speed = 2 + random.nextDouble() * 2;
            double dx = Math.cos(angle) * speed;
            double dy = Math.sin(angle) * speed;
            
            Color color = new Color(255, random.nextInt(150), 0, 255);
            int life = 60;
            
            particles[i] = new Particle(startX, startY, dx, dy, color, life);
        }
    }
    
    private void initParticles(double startX, double startY, Color color) {
        this.particles = new Particle[NUM_PARTICLES];
        for (int i = 0; i < NUM_PARTICLES; i++) {
            double angle = 2 * Math.PI * random.nextDouble();
            // 더 빠르고 강렬하게 퍼지도록 속도 조정
            double speed = 2 + random.nextDouble() * 3;
            double dx = Math.cos(angle) * speed;
            double dy = Math.sin(angle) * speed;
            
            // 수명 랜덤 설정 (40~60 프레임)
            int life = 40 + random.nextInt(20);
            
            particles[i] = new Particle(startX, startY, dx, dy, color, life);
        }
    }
    
    public void update() {
        if (!active) {
            return;
        }
        
        if (this.spriteType != null) {
            this.spriteDuration--;
            if (this.spriteDuration <= 0) {
                this.active = false;
                return;
            }
        }
        
        boolean anyAlive = false;
        for (Particle p : particles) {
            if (!p.active) {
                continue;
            }
            
            p.x += p.dx;
            p.y += p.dy;
            p.dy += 0.1;
            p.dx *= 0.98;
            p.dy *= 0.98;
            
            int alpha = (int) (255 * ((double) p.life / 60));
            alpha = Math.max(alpha, 0);
            p.color = new Color(p.color.getRed(), p.color.getGreen(), p.color.getBlue(), alpha);
            
            p.life--;
            if (p.life <= 0) {
                p.active = false;
            }
            
            if (p.active) {
                anyAlive = true;
            }
        }
        
        if (this.spriteType == null && !anyAlive) {
            active = false;
        }
    }
    
    public Particle[] getParticles() {
        return particles;
    }
    
    public boolean enemy() {
        return this.enemy;
    }
    
    public boolean isActive() {
        return active;
    }
    
    public int getSize() {
        return this.size;
    }
    
    // [추가] Getter
    public SpriteType getSpriteType() {
        return this.spriteType;
    }
    
    public static class Particle {
        public double x, y;
        public double dx, dy;
        public Color color;
        public int life;
        public boolean active;
        
        public Particle(double x, double y, double dx, double dy, Color color, int life) {
            this.x = x;
            this.y = y;
            this.dx = dx;
            this.dy = dy;
            this.color = color;
            this.life = life;
            this.active = true;
        }
    }
}