package entity;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class ShipTest {
    
    // Ship.java의 BASE_SPEED가 2라고 가정합니다.
    private static final int BASE_SPEED = 2;
    
    @Test
    void testShip_BigShotType_ShouldHaveSlowSpeed() {
        // 1. Arrange
        // GameState는 null로 전달하여 Ship 객체만 순수하게 테스트(독립성)
        Ship bigShotShip = new Ship(0, 0, null, Ship.ShipType.BIG_SHOT, null);
        int expectedSpeed = BASE_SPEED - 1; // 예상 속도는 1
        
        // 2. Act
        int actualSpeed = bigShotShip.getSpeed();
        
        // 3. Assert
        assertEquals(expectedSpeed, actualSpeed, "BIG_SHOT 타입의 속도가 예상과 다릅니다.");
    }
    
    @Test
    void testShip_MoveFastType_ShouldHaveFastSpeed() {
        // 1. Arrange
        Ship fastShip = new Ship(0, 0, null, Ship.ShipType.MOVE_FAST, null);
        int expectedSpeed = BASE_SPEED + 1; // 예상 속도는 3
        
        // 2. Act
        int actualSpeed = fastShip.getSpeed();
        
        // 3. Assert
        assertEquals(expectedSpeed, actualSpeed, "MOVE_FAST 타입의 속도가 예상과 다릅니다.");
    }
    
    @Test
    void testShip_NormalType_ShouldHaveBaseSpeed() {
        // 1. Arrange
        Ship normalShip = new Ship(0, 0, null, Ship.ShipType.NORMAL, null);
        int expectedSpeed = BASE_SPEED; // 예상 속도는 2
        
        // 2. Act
        int actualSpeed = normalShip.getSpeed();
        
        // 3. Assert
        assertEquals(expectedSpeed, actualSpeed, "NORMAL 타입의 속도가 기본 속도와 다릅니다.");
    }
}