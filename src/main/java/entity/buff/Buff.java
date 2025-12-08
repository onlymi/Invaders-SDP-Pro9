package entity.buff;

import entity.character.CharacterStats;

public abstract class Buff {
    
    protected float duration;
    private float timeElapsed;
    protected boolean active;
    
    public Buff(float duration) {
        this.duration = duration;
        this.timeElapsed = 0;
        this.active = true;
    }
    
    public abstract void applyToStats(CharacterStats characterStats);
    
    /**
     * Check the time for each frame.
     *
     * @param deltaTime The time it took from the last frame to the present
     */
    public void update(float deltaTime) {
        if (!this.active) {
            return;
        }
        
        this.timeElapsed += deltaTime;
        if (this.timeElapsed > this.duration) {
            this.timeElapsed = this.duration;
        }
        if (this.timeElapsed >= this.duration) {
            this.active = false;
        }
    }
    
    public boolean isActive() {
        return this.active;
    }
    
}
