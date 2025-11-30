package engine;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * UserStats Test.
 */
class UserStatsTest {
    
    private UserStats userStats;
    private final String testUserId = "testUser";
    
    @BeforeEach
    void setUp() {
        // 기본 생성 시 코인은 100으로 설정
        userStats = new UserStats(testUserId);
    }
    
    @Test
    void testInitialState() {
        assertEquals(testUserId, userStats.getUserId());
        assertEquals(100, userStats.getCoin()); // 생성자 코인 기본값 확인
        
        // 모든 스탯 레벨 0 확인
        for (int i = 0; i < 8; i++) {
            assertEquals(0, userStats.getStatLevel(i));
        }
    }
    
    @Test
    void testCoinManagement() {
        // 코인 추가
        userStats.addCoin(500);
        assertEquals(600, userStats.getCoin());
        
        // 코인 사용 (성공)
        boolean success = userStats.spendCoin(200);
        assertTrue(success);
        assertEquals(400, userStats.getCoin());
        
        // 코인 사용 (실패 - 잔액 부족)
        boolean fail = userStats.spendCoin(1000);
        assertFalse(fail);
        assertEquals(400, userStats.getCoin()); // 잔액 변동 없어야 함
    }
    
    @Test
    void testStatUpgrade() {
        // 초기 레벨 0
        assertEquals(0, userStats.getStatLevel(0));
        
        // 업그레이드
        userStats.upgradeStat(0); // Health
        assertEquals(1, userStats.getStatLevel(0));
        
        userStats.upgradeStat(1); // Mana
        userStats.upgradeStat(1);
        assertEquals(2, userStats.getStatLevel(1));
        
        userStats.upgradeStat(2); // Speed
        userStats.upgradeStat(2);
        userStats.upgradeStat(2);
        assertEquals(3, userStats.getStatLevel(2));
        
        userStats.upgradeStat(3); // Damage
        assertEquals(1, userStats.getStatLevel(3));
        
        userStats.upgradeStat(4); // Attack speed
        userStats.upgradeStat(4);
        assertEquals(2, userStats.getStatLevel(4));
        
        userStats.upgradeStat(5); // Attack range
        userStats.upgradeStat(5);
        userStats.upgradeStat(5);
        assertEquals(3, userStats.getStatLevel(5));
        
        userStats.upgradeStat(6); // Critical chance
        userStats.upgradeStat(6);
        assertEquals(2, userStats.getStatLevel(6));
        
        userStats.upgradeStat(7); // Defence
        assertEquals(1, userStats.getStatLevel(7));
    }
    
    @Test
    void testCSVSerialization() { // 직렬화 테스트
        // 상태 설정
        userStats.setCoin(1500);
        userStats.upgradeStat(0); // Health Lv 1
        userStats.upgradeStat(2); // Speed Lv 1
        userStats.upgradeStat(2); // Speed Lv 2
        
        String expected = "testUser,1500,1,0,2,0,0,0,0,0";
        assertEquals(expected, userStats.toCSV());
    }
    
    @Test
    void testCSVDeserialization() { // 역직렬화 테스트
        String csvLine = "player1,999,3,0,1,2,0,0,0,3";
        UserStats stats = UserStats.fromCSV(csvLine);
        
        assertNotNull(stats);
        assertEquals("player1", stats.getUserId());
        assertEquals(999, stats.getCoin());
        assertEquals(3, stats.getStatLevel(0)); // Health
        assertEquals(1, stats.getStatLevel(2)); // Speed
        assertEquals(3, stats.getStatLevel(7)); // Defence
    }
    
    @Test
    void testInvalidCSV() {
        // column 부족 테스트
        String invalidLine = "player1,100";
        assertNull(UserStats.fromCSV(invalidLine));
    }
}
