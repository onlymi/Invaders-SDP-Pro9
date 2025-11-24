package engine;

@SuppressWarnings("checkstyle:RightCurly")
public class UserStats {
    
    private String userId;
    private int coin;
    
    private int healthLevel;
    private int manaLevel;
    private int defenceLevel;
    private int speedLevel;
    private int damageLevel;
    private int attackSpeedLevel;
    private int attackRangeLevel;
    private int criticalLevel;
    
    public UserStats(String userId) {
        this.userId = userId;
        this.coin = 100;
    }
    
    public String getUserId() {
        return userId;
    }
    
    public int getCoin() {
        return coin;
    }
    
    public void setCoin(int coin) {
        this.coin = coin;
    }
    
    public void addCoin(int amount) {
        this.coin += amount;
    }
    
    public boolean spendCoin(int amount) {
        if (this.coin >= amount) {
            this.coin -= amount;
            return true;
        }
        return false;
    }
    
    // 0:HP, 1:MP, 2:Speed, 3:Dmg, 4:AS, 5:Range, 6:Crit, 7:Def
    public int getStatLevel(int index) {
        return switch (index) {
            case 0 -> healthLevel;
            case 1 -> manaLevel;
            case 2 -> speedLevel;
            case 3 -> damageLevel;
            case 4 -> attackSpeedLevel;
            case 5 -> attackRangeLevel;
            case 6 -> criticalLevel;
            case 7 -> defenceLevel;
            default -> 0;
        };
    }
    
    public void upgradeStat(int index) {
        switch (index) {
            case 0 -> healthLevel++;
            case 1 -> manaLevel++;
            case 2 -> speedLevel++;
            case 3 -> damageLevel++;
            case 4 -> attackSpeedLevel++;
            case 5 -> attackRangeLevel++;
            case 6 -> criticalLevel++;
            case 7 -> defenceLevel++;
        }
    }
    
    public String toCSV() {
        return userId + "," + coin + "," + healthLevel + "," + manaLevel + "," + speedLevel + ","
            + damageLevel + "," + attackSpeedLevel + "," + attackRangeLevel + "," + criticalLevel
            + "," + defenceLevel;
    }
    
    public static UserStats fromCSV(String line) {
        String[] parts = line.split(",");
        UserStats stats = new UserStats(parts[0]);
        stats.coin = Integer.parseInt(parts[1]);
        stats.healthLevel = Integer.parseInt(parts[2]);
        stats.manaLevel = Integer.parseInt(parts[3]);
        stats.speedLevel = Integer.parseInt(parts[4]);
        stats.damageLevel = Integer.parseInt(parts[5]);
        stats.attackSpeedLevel = Integer.parseInt(parts[6]);
        stats.attackRangeLevel = Integer.parseInt(parts[7]);
        stats.criticalLevel = Integer.parseInt(parts[8]);
        stats.defenceLevel = Integer.parseInt(parts[9]);
        return stats;
    }
}
