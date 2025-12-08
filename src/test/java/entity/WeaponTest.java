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
        
        // Core의 정적 메서드 Mocking (Cooldown 생성 방지)
        coreMock = mockStatic(Core.class);
        coreMock.when(() -> Core.getCooldown(anyInt())).thenReturn(mockCooldown);
        
        // 기본 총알 생성 (속도 5)
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
        bullet.setSpeed(-5);
        bullet.setSpriteMap();
        assertEquals(SpriteType.PlayerBullet, bullet.getSpriteType());
    }
    
    @Test
    void setBigLaser() {
        bullet.setSpeed(-5);
        bullet.setBigLaser(true);
        assertEquals(SpriteType.BigLaserBeam, bullet.getSpriteType());
        
        bullet.setBigLaser(false);
        bullet.setSpriteMap();
        assertEquals(SpriteType.PlayerBullet, bullet.getSpriteType());
    }
    
    @Test
    void setSpriteType() {
        bullet.setSpriteType(SpriteType.BossShip1);
        assertEquals(SpriteType.BossShip1, bullet.getSpriteType());
    }
    
    @Test
    void update_NormalMovement() {
        bullet.setPositionX(100);
        bullet.setPositionY(100);
        bullet.setSpeed(5);
        bullet.setSpeedX(2);
        
        bullet.update();
        
        // Y: 100 + 5 = 105
        // X: 100 + 0(VelocityX) + 2(SpeedX) = 102
        assertEquals(102, bullet.getPositionX());
        assertEquals(105, bullet.getPositionY());
    }
    
    @Test
    void update_HomingMovement() {
        // Target 설정 (현재 위치보다 오른쪽 아래)
        when(mockTarget.getPositionX()).thenReturn(200);
        when(mockTarget.getPositionY()).thenReturn(200);
        when(mockTarget.getWidth()).thenReturn(20);
        when(mockTarget.getHeight()).thenReturn(20);
        when(mockTarget.isInvincible()).thenReturn(false);
        
        // 유도 기능 활성화
        bullet.setHoming(mockTarget);
        
        // 초기 회전값 0
        bullet.setRotation(0);
        
        // 업데이트 실행
        bullet.update();
        
        // 타겟을 향해 회전했는지 확인
        assertNotEquals(0, bullet.getRotation());
    }
    
    @Test
    void update_HomingExpired() {
        bullet.setHoming(mockTarget);
        
        // 쿨다운이 끝났다고 설정 (유도 시간 만료)
        when(mockCooldown.checkFinished()).thenReturn(true);
        
        bullet.update();
        
        // 화면 밖으로 이동되어야 함 (재활용을 위해)
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
        assertEquals(2, bullet.getPlayerId());
        assertEquals(2, bullet.getOwnerPlayerId());
    }
    
    @Test
    void resetHoming() {
        bullet.setHoming(mockTarget);
        bullet.setRotation(45);
        
        bullet.resetHoming();
        
        assertEquals(0, bullet.getRotation());
        
        bullet.setSpeed(5);
        bullet.setSpeedX(0);
        bullet.setPositionX(100);
        bullet.setPositionY(100);
        
        // 유도가 풀렸으므로 직선 이동
        bullet.update();
        assertEquals(100, bullet.getPositionX());
        assertEquals(105, bullet.getPositionY());
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