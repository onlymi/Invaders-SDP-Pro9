package engine;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class ScoreTest {

    @Test
    void testCompareTo() {
        // 1. Arrange
        Score score_low = new Score("Alice", 1000, "1P");
        Score score_high = new Score("Bob", 2000, "1P");
        Score score_same = new Score("Charlie", 1000, "1P");

        // 2. Act & Assert
        // compareTo는 점수를 내림차순(높은 점수가 앞)으로 정렬합니다.

        // Bob(2000)이 Alice(1000)보다 "작아야" (앞에 와야) 합니다.
        assertTrue(score_high.compareTo(score_low) < 0, "높은 점수가 낮은 점수보다 앞에 와야 합니다.");
        // Alice(1000)는 Bob(2000)보다 "커야" (뒤에 와야) 합니다.
        assertTrue(score_low.compareTo(score_high) > 0, "낮은 점수가 높은 점수보다 뒤에 와야 합니다.");
        // Alice(1000)와 Charlie(1000)는 "같아야" 합니다.
        assertEquals(0, score_low.compareTo(score_same), "같은 점수는 0을 반환해야 합니다.");
    }
}