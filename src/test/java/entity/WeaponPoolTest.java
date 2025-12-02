package entity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import entity.Entity.Team;
import java.lang.reflect.Field;
import java.util.HashSet;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class WeaponPoolTest {
    
    @BeforeEach
    void setUp() throws Exception {
        // Static 필드인 pool을 테스트 전에 비워줍니다 (Reflection 사용)
        Field poolField = WeaponPool.class.getDeclaredField("pool");
        poolField.setAccessible(true);
        ((Set<?>) poolField.get(null)).clear();
    }
    
    @Test
    void getWeapon_EmptyPool_ReturnsNewWeapon() {
        // 1. 풀이 비어있을 때 새 총알 요청
        Weapon weapon = WeaponPool.getWeapon(100, 200, 5, 10, 20, Team.PLAYER1);
        
        // 2. 검증
        assertNotNull(weapon);
        // getBullet 내부 로직: positionX = requestedX - width/2
        assertEquals(100 - 10 / 2, weapon.getPositionX());
        assertEquals(200, weapon.getPositionY());
        assertEquals(5, weapon.getSpeed());
        assertEquals(10, weapon.getWidth());
        assertEquals(20, weapon.getHeight());
        assertEquals(Team.PLAYER1, weapon.getTeam());
        
        // 초기화 검증
        assertEquals(0, weapon.getRotation());
        assertEquals(0, weapon.getSpeedX());
    }
    
    @Test
    void recycle_And_getWeapon_ReturnsRecycledBullet() {
        // 1. 총알 하나를 수동으로 생성하고 속성을 더럽힘 (회전, 속도 등)
        Weapon originalBullet = new Weapon(0, 0, 5, 5, 5);
        originalBullet.setRotation(45); // 회전값 변경
        originalBullet.setSpeedX(10);   // X속도 변경
        
        // 2. 총알 반환 (Recycle)
        Set<Weapon> weaponsToRecycle = new HashSet<>();
        weaponsToRecycle.add(originalBullet);
        WeaponPool.recycle(weaponsToRecycle);
        
        // 3. 풀에서 다시 총알 요청
        Weapon recycledWeapon = WeaponPool.getWeapon(50, 50, 10, 20, 30, Team.ENEMY);
        
        // 4. 검증: 반환받은 객체가 원래 그 객체인지 (Identity 비교)
        assertSame(originalBullet, recycledWeapon, "재활용된 총알은 반환했던 객체와 동일해야 합니다.");
        
        // 5. 검증: 속성이 올바르게 초기화(Reset)되었는지 확인
        assertEquals(Team.ENEMY, recycledWeapon.getTeam());
        assertEquals(20, recycledWeapon.getWidth());
        assertEquals(0, recycledWeapon.getRotation(), "재활용 시 회전값이 0으로 초기화되어야 합니다.");
        assertEquals(0, recycledWeapon.getSpeedX(), "재활용 시 SpeedX가 0으로 초기화되어야 합니다.");
    }
    
    @Test
    void recycle_MultipleBullets() throws Exception {
        // 1. 여러 총알 반환
        Set<Weapon> bullets = new HashSet<>();
        bullets.add(new Weapon(0,0,0,0,0));
        bullets.add(new Weapon(0,0,0,0,0));
        
        WeaponPool.recycle(bullets);
        
        // 2. 풀 사이즈 확인 (Reflection)
        Field poolField = WeaponPool.class.getDeclaredField("pool");
        poolField.setAccessible(true);
        Set<?> pool = (Set<?>) poolField.get(null);
        
        assertEquals(2, pool.size(), "2개의 총알이 풀에 반환되어야 합니다.");
    }
}