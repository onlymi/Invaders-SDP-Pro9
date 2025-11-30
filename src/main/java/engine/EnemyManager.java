package engine;

import engine.utils.Cooldown;
import entity.EnemyShip;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import screen.GameScreen;

/**
 * Manages the spawning, updating, and drawing of enemy ships. Replaces EnemyShipFormation.
 */
public class EnemyManager {
    
    private static final int SPAWN_INTERVAL = 2000; // 2초마다 적 생성 (테스트용)
    private static final int SPAWN_VARIANCE = 1000;
    
    private GameScreen gameScreen;
    private List<EnemyShip> enemies;
    private Cooldown spawnCooldown;
    
    public EnemyManager(GameScreen gameScreen) {
        this.gameScreen = gameScreen;
        this.enemies = new ArrayList<>();
        this.spawnCooldown = Core.getVariableCooldown(SPAWN_INTERVAL, SPAWN_VARIANCE);
        this.spawnCooldown.reset();
    }
    
    /**
     * Updates the state of all enemies.
     */
    public void update() {
        // 1. 스폰 로직
        if (this.spawnCooldown.checkFinished()) {
            spawnEnemy();
            this.spawnCooldown.reset();
        }
        // 2. 적 업데이트 및 화면 밖 삭제 로직
        Iterator<EnemyShip> iterator = enemies.iterator();
        while (iterator.hasNext()) {
            EnemyShip enemy = iterator.next();
            enemy.update();
            
            // 화면 아래로 나가면 삭제 (Y좌표 기준)
            if (enemy.getPositionY() > gameScreen.getHeight()) {
                iterator.remove();
                Core.getLogger().info("Enemy escaped!");
            }
        }
    }
    
    /**
     * Spawns a random enemy at a random X position.
     */
    private void spawnEnemy() {
        // 화면 너비 내에서 랜덤 X 좌표 (여백 50px 제외)
        int x = 50 + (int) (Math.random() * (gameScreen.getWidth() - 100));
        int y = -50; // 화면 위쪽에서 시작
        
        // 현재는 기본 생성자 사용 (나중에 타입별 생성 로직 추가)
        EnemyShip enemy = new EnemyShip();
        this.enemies.add(enemy);
    }
    
    /**
     * Draws all enemies.
     */
    public void draw() {
        for (EnemyShip enemy : enemies) {
            Core.getDrawManager().getEntityRenderer().drawEntity(
                Core.getDrawManager().getBackBufferGraphics(),
                enemy,
                enemy.getPositionX(),
                enemy.getPositionY()
            );
        }
    }
    
    public List<EnemyShip> getEnemies() {
        return enemies;
    }
    
    public void destroy(EnemyShip enemy) {
        enemies.remove(enemy);
    }
    
    public boolean isEmpty() {
        return enemies.isEmpty();
    }
    
    public int getShipCount() {
        return enemies.size();
    }
    
    public boolean lastShip() {
        if (enemies.size() == 0) {
            return true;
        }
        return false;
    }
}
    

