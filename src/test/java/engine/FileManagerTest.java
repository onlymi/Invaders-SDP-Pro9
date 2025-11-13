package engine;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

/**
 * FileManager의 파일 I/O 로직을 테스트합니다. (실제 파일을 생성/삭제하므로 통합 테스트에 가깝습니다)
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS) // @AfterAll을 static이 아닌 메소드로 사용
class FileManagerTest {
    
    // 1. [수정] 사용자가 변경한 파일 이름 적용
    private static final String TEST_CSV_PATH = "src/main/resources/game_data/user_acct_info.csv";
    
    // FileManager는 싱글톤이므로 실제 인스턴스를 가져옴
    private FileManager fileManager = FileManager.getInstance();
    
    /**
     * 각 테스트 전에 파일을 깨끗한 상태(헤더만)로 만듭니다.
     */
    @BeforeEach
    void setUp() throws Exception {
        // 1. 기존 파일이 있다면 삭제
        Files.deleteIfExists(Paths.get(TEST_CSV_PATH));
        
        // 2. 헤더만 있는 새 파일 생성
        try (FileWriter writer = new FileWriter(TEST_CSV_PATH)) {
            writer.write("id,password_hash\n");
        }
    }
    
    /**
     * 모든 테스트가 끝난 후 파일을 삭제합니다.
     */
    @AfterAll
    void tearDownAll() throws Exception {
        Files.deleteIfExists(Paths.get(TEST_CSV_PATH));
    }
    
    @Test
    void testSaveUser_Success() throws Exception {
        // 1. 실행: "newUser" 저장
        boolean result = fileManager.saveUser("newUser", "pass123");
        
        // 2. 검증:
        // 2.1. saveUser는 true를 반환해야 함
        assertTrue(result);
        
        // 2.2. CSV 파일을 직접 읽어서 확인
        try (BufferedReader reader = new BufferedReader(new FileReader(TEST_CSV_PATH))) {
            String header = reader.readLine();
            String line = reader.readLine();
            
            // "pass123"의 SHA-256 해시값
            String expectedHash = "9b8769a4a742959a2d0298c36fb70623f2dfacda8436237df08d8dfd5b37374c";
            
            assertEquals("newUser," + expectedHash, line);
            assertEquals(null, reader.readLine()); // 파일이 더 이상 내용이 없어야 함
        }
    }
    
    @Test
    void testSaveUser_DuplicateId() throws Exception {
        // 1. 실행 1: "dupeUser"를 한 번 저장
        boolean result1 = fileManager.saveUser("dupeUser", "pass1");
        
        // 2. 실행 2: "dupeUser"를 다시 저장
        boolean result2 = fileManager.saveUser("dupeUser", "pass2");
        
        // 3. 검증:
        // 3.1. 첫 번째 저장은 성공(true)
        assertTrue(result1);
        // 3.2. 두 번째 저장은 ID 중복으로 실패(false)
        assertFalse(result2);
        
        // 3.3. CSV 파일을 읽어서 "dupeUser"가 한 번만 저장되었는지 확인
        try (BufferedReader reader = new BufferedReader(new FileReader(TEST_CSV_PATH))) {
            reader.readLine(); // 헤더 스킵
            String line1 = reader.readLine(); // "dupeUser,pass1"
            String line2 = reader.readLine(); // null
            
            assertTrue(line1.startsWith("dupeUser,"));
            assertEquals(null, line2); // 두 번째 라인이 없어야 함
        }
    }
}