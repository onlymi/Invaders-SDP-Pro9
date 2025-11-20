package entity;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class BossShipTest {

    // 테스트마다 새로운 BossShip 인스턴스를 생성하는 헬퍼 메서드
    private BossShip createTestBoss() {
        return new BossShip(100, 100);
    }

    @Test
    void initialHealthIsSetCorrectly() {
        BossShip boss = createTestBoss();
        // BOSS_INITIAL_HEALTH가 500으로 설정되었는지 확인
        assertEquals(500, boss.getHealth(), "Initial health must be 500");
    }

    @Test
    void healthDecrementsByOneOnHit() {
        BossShip boss = createTestBoss();
        int initialHealth = boss.getHealth();
        boss.hit();
        // hit() 메서드 호출 후 체력이 정확히 1 감소했는지 확인
        assertEquals(initialHealth - 1, boss.getHealth(), "Health must decrease by 1 after hit()");
    }

    @Test
    void bossDestroysWhenHealthReachesZero() {
        BossShip boss = createTestBoss();

        // 1. getDamage() 메서드를 사용하여 보스의 HP를 1로 설정 (파괴 직전)
        // EnemyShip의 getDamage 메서드는 보스의 체력을 정확히 감소시키므로 이를 이용합니다.
        boss.getDamage(boss.getHealth() - 1);

        // 파괴 직전 상태 검증
        assertEquals(1, boss.getHealth(), "Boss HP must be exactly 1 before the final hit.");
        assertFalse(boss.isDestroyed(), "Boss should not be destroyed yet (HP=1).");

        // 2. 최종 타격 (파괴 유발)
        boss.hit();

        // 3. 파괴 상태 검증
        assertTrue(boss.isDestroyed(), "Boss must be destroyed after the final hit.");
        assertTrue(boss.getHealth() <= 0, "Boss health must be zero or less after destruction.");
    }

    @Test
    void pointAndCoinValuesAreCorrect() {
        BossShip boss = createTestBoss();
        // BOSS_POINTS 및 BOSS_COINS가 5000으로 설정되었는지 확인
        assertEquals(5000, boss.getPointValue(), "Point value must be 5000");
        assertEquals(5000, boss.getCoinValue(), "Coin value must be 5000");
    }
}