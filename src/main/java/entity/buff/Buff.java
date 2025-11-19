package entity.buff;

import entity.character.GameCharacter;

public abstract class Buff {
    
    protected float duration;
    private float timeElapsed;
    protected boolean active;
    
    public Buff(float duration) {
        this.duration = duration;
        this.timeElapsed = 0;
        this.active = true;
    }
    
    public abstract void apply(GameCharacter gameCharacter);
    
    public abstract void remove(GameCharacter gameCharacter);
    
    /**
     * Check the time for each frame.
     *
     * @param deltaTime     The time it took from the last frame to the present
     * @param gameCharacter Buffed character
     */
    public void update(float deltaTime, GameCharacter gameCharacter) {
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
