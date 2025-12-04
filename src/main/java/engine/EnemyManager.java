package engine;

import engine.AssetManager.SpriteType;
import engine.utils.Cooldown;
import entity.EnemyShip;
import entity.character.GameCharacter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import screen.GameScreen;

/**
 * Manages the spawning, updating, and drawing of enemy ships. Replaces EnemyShipFormation.
 */
public class EnemyManager {
    
    private static final int SPAWN_INTERVAL = 1000; // 2초마다 적 생성 (테스트용)
    private static final int SPAWN_VARIANCE = 1000;
    
    private GameScreen gameScreen;
    private List<EnemyShip> enemies;
    private Cooldown spawnCooldown;
    private Random random;
    
    public EnemyManager(GameScreen gameScreen) {
        this.gameScreen = gameScreen;
        this.enemies = new ArrayList<>();
        this.spawnCooldown = Core.getVariableCooldown(SPAWN_INTERVAL, SPAWN_VARIANCE);
        this.spawnCooldown.reset();
        this.random = new Random();
    }
    
    /**
     * Updates the state of all enemies.
     */
    public void update() {
        // Freeze 상태라면 스폰 중단
        if (gameScreen.getGameState().areEnemiesFrozen()) {
            return;
        }
        // 스폰 로직
        if (this.spawnCooldown.checkFinished()) {
            spawnEnemy();
            this.spawnCooldown.reset();
        }
        
        Iterator<EnemyShip> iterator = enemies.iterator();
        while (iterator.hasNext()) {
            EnemyShip enemy = iterator.next();
            
            // 가장 가까운 플레이어 찾기
            GameCharacter target = findTargetPlayer(enemy);
            
            // 타겟 정보를 넘겨주며 적 업데이트
            enemy.update(target);
            
            // 화면 아래로 나가면 삭제
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
        // 화면 너비 내에서 랜덤 X 좌표
        int x = 50 + random.nextInt(gameScreen.getWidth() - 100);
        int uiLine = gameScreen.getSeparationLineHeight();
        int minY = uiLine + 50; // UI 제외
        int maxY = gameScreen.getHeight() - 200; // 플레이어 근처 제외
        // 화면 너비 내에서 랜덤 Y 좌표
        int y = minY + random.nextInt(maxY - minY);
        
        EnemyShip enemy;
        int type = random.nextInt(3);
        GameState gameState = gameScreen.getGameState();
        switch (type) { // TODO: enemy 타입 만든 후 수정 예정
            case 0:
                enemy = new EnemyShip(x, y, SpriteType.EnemyShipA1);
                break;
            case 1:
                enemy = new EnemyShip(x, y, SpriteType.EnemyShipB1);
                break;
            case 2:
            default:
                enemy = new EnemyShip(x, y, SpriteType.EnemyShipC1);
                break;
        }
        this.enemies.add(enemy);
    }
    
    /**
     * Draws all enemies.
     */
    public void draw() {
        for (EnemyShip enemy : enemies) {
            Core.getDrawManager().getEntityRenderer().drawEntityByScale(
                Core.getDrawManager().getBackBufferGraphics(),
                enemy,
                enemy.getPositionX(),
                enemy.getPositionY(), 1
            );
        }
    }
    
    /**
     *
     * @param enemy 타겟을 찾으려는 적 유닛
     * @return 가장 가까운 GameCharacter (모든 플레이어가 죽었으면 null 반환)
     */
    private GameCharacter findTargetPlayer(EnemyShip enemy) {
        GameCharacter closestPlayer = null;
        double minClosestSq = Double.MAX_VALUE;
        
        for (GameCharacter player : gameScreen.getCharacters()) {
            if (player == null || player.isDie()) {
                continue;
            }
            double dx = player.getPositionX() - enemy.getPositionX();
            double dy = player.getPositionY() - enemy.getPositionY();
            double distanceSq = dx * dx + dy * dy;
            if (distanceSq < minClosestSq) {
                minClosestSq = distanceSq;
                closestPlayer = player;
            }
        }
        return closestPlayer;
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
    

