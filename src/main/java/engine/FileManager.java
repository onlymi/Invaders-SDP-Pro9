package engine;

import engine.gameplay.achievement.Achievement;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Logger;

/**
 * Manages files used in the application.
 *
 * @author <a href="mailto:RobertoIA1987@gmail.com">Roberto Izquierdo Amo</a>
 *
 */
public final class FileManager {
    
    /**
     * user account info file path.
     */
    private String userAccountPath = "src/main/resources/game_data/user_acct_info.csv";
    /**
     * user account stats info file path.
     */
    private String userStatsPath = "src/main/resources/game_data/user_stats.csv";
    /**
     * Singleton instance of the class.
     */
    private static FileManager instance;
    /**
     * Application logger.
     */
    private static Logger LOGGER;
    
    /**
     * Enum indicating login result status.
     */
    public enum LoginResult {
        SUCCESS,
        ID_NOT_FOUND,
        PASSWORD_MISMATCH
    }
    
    /**
     * private constructor.
     */
    private FileManager() {
        LOGGER = Core.getLogger();
    }
    
    /**
     * Returns shared instance of FileManager.
     *
     * @return Shared instance of FileManager.
     */
    public static FileManager getInstance() {
        if (instance == null) {
            instance = new FileManager();
        }
        return instance;
    }
    
    /**
     * Returns the application default scores if there is no user high scores file.
     *
     * @return Default high scores. In case of loading problems.
     */
    private List<Score> loadDefaultHighScores(String mode) {
        List<Score> highScores = new ArrayList<>();
        int list_size = 7;
        Score highScore;
        for (int i = 0; i < list_size; i++) {
            highScore = new Score("ERR", 0, mode);
            highScores.add(highScore);
        }
        
        return highScores;
    }
    
    /**
     * Loads high scores from file, and returns a sorted list of pairs score - value.
     *
     * @param mode get game mode 1P/2P.
     * @return Sorted list of scores - players.
     * @throws IOException In case of loading problems.
     */
    public List<Score> loadHighScores(String mode) throws IOException {
        List<Score> highScores = new ArrayList<>();
        InputStream inputStream = null;
        BufferedReader bufferedReader = null;
        
        try {
            String scoresPath = getFilePath("game_data/" + mode + "_scores.csv");
            
            File scoresFile = new File(scoresPath);
            inputStream = new FileInputStream(scoresFile);
            bufferedReader = new BufferedReader(
                new InputStreamReader(inputStream, StandardCharsets.UTF_8));
            LOGGER.info("Loading user high scores.");
            // except first line
            bufferedReader.readLine();
            String input;
            while ((input = bufferedReader.readLine()) != null) {
                String[] pair = input.split(",");
                String name = pair[0], score = pair[1];
                Score highScore = new Score(name, Integer.parseInt(score), mode);
                highScores.add(highScore);
            }
        } catch (FileNotFoundException e) {
            // loads default if there's no user scores.
            LOGGER.info("Loading default high scores.");
            highScores = loadDefaultHighScores(mode);
        } finally {
            if (bufferedReader != null) {
                bufferedReader.close();
            }
        }
        
        Collections.sort(highScores);
        return highScores;
    }
    
    /**
     * Saves user high scores to disk.
     *
     * @param highScores High scores to save.
     * @param mode       get game mode 1P/2P.
     * @throws IOException In case of loading problems.
     */
    public void saveHighScores(final List<Score> highScores, String mode) throws IOException {
        OutputStream outputStream = null;
        BufferedWriter bufferedWriter = null;
        
        try {
            String scoresPath = getFilePath("game_data/" + mode + "_scores.csv");
            
            File scoresFile = new File(scoresPath);
            
            if (!scoresFile.exists()) {
                scoresFile.createNewFile();
            }
            
            outputStream = new FileOutputStream(scoresFile);
            bufferedWriter = new BufferedWriter(
                new OutputStreamWriter(outputStream, StandardCharsets.UTF_8));
            
            LOGGER.info("Saving user high scores.");
            bufferedWriter.write("player,score");
            bufferedWriter.newLine();
            
            for (Score score : highScores) {
                bufferedWriter.write(score.getName() + "," + score.getScore());
                bufferedWriter.newLine();
            }
            
        } finally {
            if (bufferedWriter != null) {
                bufferedWriter.close();
            }
        }
    }
    
    /**
     * Returns the filepath
     *
     * @param fileName file to get path
     * @return full file path
     * @throws IOException In case of loading problems
     *
     */
    private static String getFilePath(String fileName) throws IOException {
        String rootDir = System.getProperty("user.dir");
        String filePath = rootDir + File.separator + fileName;
        
        File file = new File(filePath);
        File parentDir = file.getParentFile();
        if (parentDir != null && !parentDir.exists()) {
            parentDir.mkdirs();
        }
        
        return filePath;
    }
    
    /**
     * Search Achievement list of user
     *
     * @param userName user's name to search.
     * @throws IOException In case of loading problems.
     */
    public List<Boolean> searchAchievementsByName(String userName) throws IOException {
        List<Boolean> achievementList = new ArrayList<>();
        
        try {
            String achievementPath = getFilePath("game_data/achievement.csv");
            
            try (BufferedReader bReader = new BufferedReader(
                new InputStreamReader(new FileInputStream(achievementPath),
                    StandardCharsets.UTF_8))) {
                
                bReader.readLine(); // Skip header
                String line;
                boolean found = false;
                
                while ((line = bReader.readLine()) != null) {
                    String[] playRecord = line.split(",");
                    if (playRecord.length < 3) {
                        continue; // Minimum fields: mode, userName, at least 1 achievement
                    }
                    
                    String mode = playRecord[0].trim(); // Mode: "1" or "2"
                    String name = playRecord[1].trim();
                    
                    if (name.equals(userName)) {
                        found = true;
                        LOGGER.info("Loading user achievements.");
                        // Achievements start from index 2
                        for (int i = 2; i < playRecord.length; i++) {
                            achievementList.add(playRecord[i].equals("1"));
                        }
                        break;
                    }
                }
                
                if (!found) {
                    LOGGER.info("Loading default achievements.");
                    for (int i = 0; i < 5; i++) { // Default to 5 achievements, all set to false
                        achievementList.add(false);
                    }
                }
            }
            
        } catch (FileNotFoundException e) {
            LOGGER.info("Achievement file not found, loading default achievements.");
            for (int i = 0; i < 5; i++) {
                achievementList.add(false);
            }
        }
        
        return achievementList;
    }
    
    
    /**
     * Unlocks achievements for a specific user.
     *
     * @param userName            The name of the user.
     * @param unlockedAchievement A list of booleans representing which achievements have been
     *                            unlocked.
     */
    public void unlockAchievement(String userName, List<Boolean> unlockedAchievement, String mode) {
        List<String[]> records = new ArrayList<>();
        
        // Extract only numeric part from mode string (e.g., "1P" → "1", "2P" → "2")
        String numericMode = mode.replaceAll("[^0-9]", "");
        
        try {
            String achievementPath = getFilePath("game_data/achievement.csv");
            
            try (BufferedReader bReader = new BufferedReader(
                new InputStreamReader(new FileInputStream(achievementPath),
                    StandardCharsets.UTF_8))) {
                
                String line;
                boolean found = false;
                
                while ((line = bReader.readLine()) != null) {
                    String[] playRecord = line.split(",");
                    
                    // Skip invalid or incomplete lines
                    if (playRecord.length < 3) {
                        records.add(playRecord);
                        continue;
                    }
                    
                    String currentMode = playRecord[0].trim();
                    String name = playRecord[1].trim();
                    
                    // Match both username and mode to consider it the same record
                    if (name.equals(userName) && currentMode.equals(numericMode)) {
                        found = true;
                        Logger.getLogger(getClass().getName())
                            .info("Achievement has been updated.");
                        for (int i = 2; i < playRecord.length; i++) {
                            if (playRecord[i].equals("0") && unlockedAchievement.get(i - 2)) {
                                playRecord[i] = "1";
                            }
                        }
                    }
                    
                    records.add(playRecord);
                }
                
                // If no existing record found, create a new one
                if (!found) {
                    Logger.getLogger(getClass().getName())
                        .info("User not found, creating new record.");
                    String[] newRecord = new String[unlockedAchievement.size() + 2];
                    newRecord[0] = numericMode; // Store numeric mode only
                    newRecord[1] = userName;
                    for (int i = 0; i < unlockedAchievement.size(); i++) {
                        newRecord[i + 2] = unlockedAchievement.get(i) ? "1" : "0";
                    }
                    records.add(newRecord);
                }
            }
            
            // Write the updated records back to the CSV file
            try (BufferedWriter bWriter = new BufferedWriter(
                new OutputStreamWriter(new FileOutputStream(achievementPath),
                    StandardCharsets.UTF_8))) {
                for (String[] record : records) {
                    bWriter.write(String.join(",", record));
                    bWriter.newLine();
                }
            }
            
        } catch (IOException e) {
            Logger.getLogger(getClass().getName())
                .info("No achievements to save or error occurred.");
        }
    }
    
    /**
     * Returns a list of users who have completed a specific achievement.
     *
     * @param achievement The achievement to check.
     * @return A list of strings in the format "mode:username" for those who have completed the
     * achievement.
     * <p>
     * [2025-10-09] Added in commit: feat: add method to retrieve achievement completer
     */
    public List<String> getAchievementCompleter(Achievement achievement) {
        List<String> completer = new ArrayList<>();
        try {
            String achievementPath = getFilePath("game_data/achievement.csv");
            
            try (BufferedReader bReader = new BufferedReader(
                new InputStreamReader(new FileInputStream(achievementPath),
                    StandardCharsets.UTF_8))) {
                
                String line;
                String[] header = bReader.readLine().split(",");
                int idx = -1;
                
                // Find the column index of the given achievement name
                for (int i = 2; i < header.length; i++) { // Achievements start from column index 2
                    if (header[i].trim().equalsIgnoreCase(achievement.getName().trim())) {
                        idx = i;
                        break;
                    }
                }
                
                if (idx == -1) {
                    LOGGER.warning("Achievement not found: " + achievement.getName());
                    return completer;
                }
                
                // Parse each line in the file
                while ((line = bReader.readLine()) != null) {
                    String[] tokens = line.split(",");
                    if (tokens.length <= idx) {
                        continue;
                    }
                    
                    String mode = tokens[0].trim();
                    String playerName = tokens[1].trim();
                    String value = tokens[idx].trim();
                    
                    if (value.equals("1")) {
                        completer.add(mode + ":" + playerName);
                    }
                }
                
            }
            
        } catch (IOException e) {
            LOGGER.warning("Error reading achievement file. Returning default users...");
            completer.add("1:ABC");
            completer.add("2:DEF");
        }
        
        return completer;
    }
    
    /**
     * Save new user at user_acct_info.csv.
     *
     * @param id       user ID
     * @param password user password(not encryption)
     * @return True if membership is successful, false if ID is duplicated
     * @throws IOException              If file writing fails
     * @throws NoSuchAlgorithmException If the encryption algorithm cannot be found
     */
    public boolean saveUser(final String id, final String password)
        throws IOException, NoSuchAlgorithmException {
        String trimmedId = id.trim();
        // ID 중복 검사
        if (isUserExists(trimmedId)) {
            LOGGER.warning("User ID already exists: " + trimmedId);
            return false;
        }
        
        // password 해싱
        String hashedPassword = hashPassword(password);
        
        // CSV 파일에 쓰기
        try (FileWriter writer = new FileWriter(userAccountPath, true)) {
            writer.append(trimmedId);
            writer.append(',');
            writer.append(hashedPassword);
            writer.append('\n');
            writer.flush();
        }
        LOGGER.info("New user " + id + " saved");
        return true;
    }
    
    /**
     * Verify that the user with that ID already exists in CSV.
     *
     * @param id User ID to check
     * @return if ID exists, return true
     * @throws IOException If the file fails to read
     */
    private boolean isUserExists(final String id) throws IOException {
        try (BufferedReader reader = new BufferedReader(new FileReader(userAccountPath))) {
            String line;
            reader.readLine(); // 헤더(id,password_hash) 스킵
            
            while ((line = reader.readLine()) != null) {
                String[] values = line.split(",");
                if (values.length > 0 && values[0].trim().equals(id)) {
                    return true;
                }
            }
        }
        return false;
    }
    
    /**
     * Setter Method for Path Change
     *
     * @param path File path to save
     */
    public void setUserAccountPath(String path) {
        this.userAccountPath = path;
    }
    
    /**
     *
     * @param id       ID entered by the user
     * @param password The (unencrypted) password entered by the user
     * @return True if login successful, False if failed
     * @throws IOException              If the file fails to read
     * @throws NoSuchAlgorithmException If the hashing algorithm cannot be found
     */
    public LoginResult validateUser(final String id, final String password)
        throws IOException, NoSuchAlgorithmException {
        
        String trimmedId = id.trim();   // ID 공백 제거
        String hashedPassword = hashPassword(password);     // password 해싱
        
        try (BufferedReader reader = new BufferedReader(new FileReader(userAccountPath))) {
            String line;
            reader.readLine();      // 헤더 스킵
            
            while ((line = reader.readLine()) != null) {
                String[] values = line.split(",");
                if (values.length < 2) {
                    continue;   // 손상된 데이터는 스킵
                }
                String storedId = values[0].trim();
                String storedHashPassword = values[1].trim();
                
                if (storedId.equals(trimmedId)) {   // ID가 일치하는지 확인
                    if (storedHashPassword.equals(hashedPassword)) {    // Password가 일치하는지 확인
                        LOGGER.info("User " + trimmedId + " is valid");
                        return LoginResult.SUCCESS;
                    } else {
                        LOGGER.warning("User " + trimmedId + "'s password does not match");
                        return LoginResult.PASSWORD_MISMATCH;
                    }
                }
            }
        }
        LOGGER.warning("User " + trimmedId + " is not found");
        return LoginResult.ID_NOT_FOUND;
    }
    
    /**
     * Hashes the password SHA-256.
     *
     * @param password original password
     * @return Hashed password. (Hex string)
     * @throws NoSuchAlgorithmException If the encryption algorithm cannot be found
     */
    private String hashPassword(final String password) throws NoSuchAlgorithmException {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] encodedhash = digest.digest(
            password.getBytes(StandardCharsets.UTF_8));
        return bytesToHex(encodedhash);
    }
    
    /**
     * Convert the byte array to a hex string.
     *
     * @param hash Byte array to convert
     * @return hexadecimal string
     */
    private static String bytesToHex(final byte[] hash) {
        StringBuilder hexString = new StringBuilder(2 * hash.length);
        for (byte b : hash) {
            String hex = Integer.toHexString(0xff & b);
            if (hex.length() == 1) {
                hexString.append('0');
            }
            hexString.append(hex);
        }
        return hexString.toString();
    }
    
    /**
     * Load stat information with the user ID. If the stat file does not exist, create the file and
     * save the default value.
     *
     * @param userId user account ID
     * @return UserStats objects that match userId
     * @throws IOException user stats data file does not exist
     */
    public UserStats loadUserStats(String userId) throws IOException {
        File file = new File(userStatsPath);
        if (!file.exists()) {
            file.getParentFile().mkdirs();
            file.createNewFile();
        }
        
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.startsWith(userId + ",")) {
                    return UserStats.fromCSV(line);
                }
            }
        }
        
        // 정보가 없으면 새로 생성 후 저장
        UserStats newStats = new UserStats(userId);
        saveUserStats(newStats);
        return newStats;
    }
    
    /**
     * 유저 스탯 정보를 파일에 저장(업데이트)합니다.
     *
     * @param stats
     * @throws IOException
     */
    public void saveUserStats(UserStats stats) throws IOException {
        File file = new File(userStatsPath);
        List<String> lines = new ArrayList<>();
        boolean found = false;
        
        if (file.exists()) {
            try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    if (line.startsWith(stats.getUserId() + ",")) {
                        lines.add(stats.toCSV());
                        found = true;
                    }
                }
            }
        }
        // 신규 유저 추가
        if (!found) {
            lines.add(stats.toCSV());
        }
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
            for (String line : lines) {
                writer.write(line);
                writer.newLine();
            }
        }
    }
}