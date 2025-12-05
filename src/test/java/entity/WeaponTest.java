package entity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

import engine.AssetManager.SpriteType;
import engine.Core;
import engine.utils.Cooldown;
import entity.character.GameCharacter;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.MockitoAnnotations;

class WeaponTest {
    
    private Weapon bullet;
    private MockedStatic<Core> coreMock;
    
    @Mock
    private Cooldown mockCooldown;
    @Mock
    private GameCharacter mockTarget;
    
    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        
        // Static Mocking for Core to handle Cooldown creation
        coreMock = mockStatic(Core.class);
        coreMock.when(() -> Core.getCooldown(anyInt())).thenReturn(mockCooldown);
        
        // Create a default bullet
        bullet = new Weapon(100, 100, 6, 10, 5);
    }
    
    @AfterEach
    void tearDown() {
        coreMock.close();
    }
    
    @Test
    void setSize() {
        bullet.setSize(20, 30);
        assertEquals(20, bullet.getWidth());
        assertEquals(30, bullet.getHeight());
    }
    
    @Test
    void setSprite_NormalBullet() {
        // Speed > 0 (Enemy Bullet)
        bullet.setSpeed(5);
        bullet.setSpriteMap();
        assertEquals(SpriteType.EnemyBullet, bullet.getSpriteType());
    }
    
    @Test
    void setBigLaser() {
        bullet.setBigLaser(true);
        assertEquals(SpriteType.BigLaserBeam, bullet.getSpriteType());
        
        bullet.setBigLaser(false);
        // Reverts to default based on speed
        bullet.setSpriteMap();
        assertEquals(SpriteType.EnemyBullet, bullet.getSpriteType());
    }
    
    @Test
    void setSpriteType() {
        bullet.setSpriteType(SpriteType.BossShip1); // Any sprite
        assertEquals(SpriteType.BossShip1, bullet.getSpriteType());
    }
    
    @Test
    void update_NormalMovement() {
        bullet.setPositionX(100);
        bullet.setPositionY(100);
        bullet.setSpeed(5);
        bullet.setSpeedX(2);
        
        bullet.update();
        
        assertEquals(102, bullet.getPositionX());
        assertEquals(105, bullet.getPositionY());
    }
    
    @Test
    void update_HomingMovement() {
        // Setup Target
        when(mockTarget.getPositionX()).thenReturn(200);
        when(mockTarget.getPositionY()).thenReturn(200);
        when(mockTarget.getWidth()).thenReturn(20);
        when(mockTarget.getHeight()).thenReturn(20);
        when(mockTarget.isDestroyed()).thenReturn(false);
        
        // Enable Homing
        bullet.setHoming(mockTarget);
        
        // Initial rotation is 0
        bullet.setRotation(0);
        
        // Update should calculate new speed and rotation
        bullet.update();
        
        // Rotation should change to face target
        assertNotEquals(0, bullet.getRotation());
        // SpeedX and Speed should be updated
        assertNotEquals(0, bullet.getSpeedX());
        assertNotEquals(0, bullet.getSpeed());
    }
    
    @Test
    void update_HomingExpired() {
        bullet.setHoming(mockTarget);
        
        // Simulate cooldown finished (expired)
        when(mockCooldown.checkFinished()).thenReturn(true);
        
        bullet.update();
        
        // Should be moved off-screen for recycling
        assertEquals(2000, bullet.getPositionY());
    }
    
    @Test
    void setSpeed() {
        bullet.setSpeed(10);
        assertEquals(10, bullet.getSpeed());
    }
    
    @Test
    void getSpeed() {
        bullet.setSpeed(-5);
        assertEquals(-5, bullet.getSpeed());
    }
    
    @Test
    void setSpeedX() {
        bullet.setSpeedX(3);
        assertEquals(3, bullet.getSpeedX());
    }
    
    @Test
    void getSpeedX() {
        bullet.setSpeedX(-3);
        assertEquals(-3, bullet.getSpeedX());
    }
    
    @Test
    void getOwnerPlayerId() {
        bullet.setOwnerPlayerId(1);
        assertEquals(1, bullet.getOwnerPlayerId());
    }
    
    @Test
    void setOwnerPlayerId() {
        bullet.setOwnerPlayerId(2);
        assertEquals(2, bullet.getOwnerPlayerId());
    }
    
    @Test
    void getPlayerId() {
        bullet.setPlayerId(1);
        assertEquals(1, bullet.getPlayerId());
    }
    
    @Test
    void setPlayerId() {
        bullet.setPlayerId(2);
        // Assuming setPlayerId also sets ownerPlayerId in Bullet implementation
        assertEquals(2, bullet.getPlayerId());
        assertEquals(2, bullet.getOwnerPlayerId());
    }
    
    @Test
    void setHoming() {
        bullet.setHoming(mockTarget);
        // We can verify internal state indirectly by checking if update changes rotation
        // or simply trust that no exception was thrown and logic is exercised in update test
    }
    
    @Test
    void resetHoming() {
        bullet.setHoming(mockTarget);
        bullet.setRotation(45);
        
        bullet.resetHoming();
        
        assertEquals(0, bullet.getRotation());
        // Can't easily check private fields isHoming/target without getters,
        // but behavior should revert to normal update
        
        bullet.setSpeed(5);
        bullet.setSpeedX(0);
        bullet.setPositionX(100);
        bullet.setPositionY(100);
        
        // Update should be linear now, not homing
        bullet.update();
        assertEquals(100, bullet.getPositionX()); // SpeedX is 0
        assertEquals(105, bullet.getPositionY()); // SpeedY is 5
    }
    
    @Test
    void testDurationAndExpiry() throws InterruptedException {
        assertFalse(bullet.isExpired());
        
        // 수명 설정
        bullet.setDuration(10);
        assertFalse(bullet.isExpired());
        
        // 시간 경과 후 만료 확인
        Thread.sleep(15);
        assertTrue(bullet.isExpired());
    }
    
    @Test
    void testHitPlayerTracking() {
        // 초기 상태
        assertFalse(bullet.isHitPlayer(1));
        assertFalse(bullet.isHitPlayer(2));
        
        // player 1 피격
        bullet.addHitPlayer(1);
        assertTrue(bullet.isHitPlayer(1));
        assertFalse(bullet.isHitPlayer(2));
        
        // player 2 피격
        bullet.addHitPlayer(2);
        assertTrue(bullet.isHitPlayer(1));
        assertTrue(bullet.isHitPlayer(2));
    }
    
    @Test
    void testResetFullState() {
        bullet.setDuration(100);
        bullet.setSpeedX(10);
        bullet.setRotation(90);
        bullet.addHitPlayer(1);
        bullet.setHoming(mockTarget);
        
        bullet.reset();
        
        // 수명이 -1로 초기화되어 만료되지 않아야 함
        assertFalse(bullet.isExpired());
        
        // 속도 및 회전 초기화 확인
        assertEquals(0, bullet.getSpeedX());
        assertEquals(0, bullet.getRotation());
        
        // 피격 기록 초기화 확인
        assertFalse(bullet.isHitPlayer(1));
        
        // 유도 기능 해제 확인
        bullet.setPositionX(100);
        bullet.setPositionY(100);
        bullet.setSpeed(5);
        bullet.update();
        assertEquals(0, bullet.getRotation());
    }
}