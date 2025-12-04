package animations;

import java.util.Random;

/*
 * The basic background stars effect during the game
 * */
public class BasicGameSpace {
    
    public final Star[] stars;
    private final Random rand = new Random();
    private int[][] positions;
    private int speed = 0;
    private int numStars;
    private final int screenWidth;
    private final int screenHeight;
    private boolean bossStage = false;
    
    /**
     * Countdown to game start.
     *
     * @param numStars     Num of stars.
     * @param screenHeight Game screen's Height.
     * @param screenWidth  .Game screen's Width.
     */
    public BasicGameSpace(int numStars, int screenWidth, int screenHeight) {
        
        this.numStars = numStars;
        this.stars = new Star[this.numStars];
        this.positions = new int[this.numStars][3];
        this.screenWidth = screenWidth;
        this.screenHeight = screenHeight;
        
        for (int i = 0; i < this.numStars; i++) {
            
            stars[i] = new Star(rand.nextInt(10, screenWidth), rand.nextInt(-1 * screenHeight, 5),
                (randomSpeed()) ? 2 : 1);
            positions[i][0] = stars[i].x;
            positions[i][1] = stars[i].y;
            positions[i][2] = stars[i].speed;
        }
    }
    
    // Update star locations
    public void update() {
        int i = 0;
        for (Star star : stars) {
            if (this.bossStage) {
                star.y += 10;
            } else if (this.speed == 3) { // Last life condition
                star.y += 3;
            } else {
                star.y += star.speed;
            }
            
            positions[i][1] = star.y;
            
            if (star.y >= screenHeight + 5) {
                star.y = 0;
                positions[i][1] = 0;
            }
            i++;
        }
    }
    
    public void setLastLife(boolean status) {
        if (status) {
            this.speed = 3;
        } else {
            this.speed = 1;
        }
    }
    
    public boolean isLastLife() {
        return this.speed == 3;
    }
    
    public void setBossStage(boolean active) {
        this.bossStage = active;
    }
    
    public boolean isBossStage() {
        return this.bossStage;
    }
    
    public int[][] getStarLocations() {
        return this.positions;
    }
    
    public int getNumStars() {
        return this.numStars;
    }
    
    public boolean randomSpeed() {
        double r = Math.random();
        
        return (r < 0.85);
    }
    
    // Star format
    private static class Star {
        
        int x, y, speed;
        
        Star(int x, int y, int speed) {
            this.x = x;
            this.y = y;
            this.speed = speed;
        }
        
    }
}
