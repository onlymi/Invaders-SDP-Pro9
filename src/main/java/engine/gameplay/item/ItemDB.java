package engine.gameplay.item;

import engine.Core;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

/**
 * Handles loading and managing item data from the CSV file. (item_db.csv)
 * <p>
 * Expected base CSV columns (at least 5): type,spriteType,dropTier,effectValue,effectDuration
 * <p>
 * Extended format supports:
 * id,displayName,description,activationType,maxCharges,cooldownSec,autoUseOnPickup,stackable
 */
public class ItemDB {

    /**
     * Path to the item database CSV file.
     */
    private static final String FILE_PATH = "game_data/item_db.csv";

    /**
     * Map of item type name to its corresponding ItemData.
     */
    private final Map<String, ItemData> itemMap = new HashMap<>();

    /**
     * Constructor. Automatically loads the CSV file into memory.
     */
    public ItemDB() {
        loadItemDB();
    }

    /**
     * Loads all item data from the CSV file into the itemMap.
     */
    private void loadItemDB() {
        Logger logger = Core.getLogger();

        InputStream is = ItemDB.class.getClassLoader().getResourceAsStream(FILE_PATH);
        if (is == null) {
            Core.getLogger().severe("Item DB file not found in classpath: " + FILE_PATH);
            return;
        }

        try (BufferedReader br = new BufferedReader(
            new InputStreamReader(is, StandardCharsets.UTF_8))) {

            String line;
            boolean header = true;

            while ((line = br.readLine()) != null) {
                if (header) {
                    header = false;
                    continue;
                }

                // split on comma - trim tokens
                String[] tokens = line.split(",");
                if (tokens.length < 5) {
                    logger.warning("[ItemDB] Skipping malformed line (expected >=5 cols): " + line);
                    continue;
                }

                String type = tokens[0].trim();
                String spriteType = tokens[1].trim();
                String dropTier = tokens[2].trim();

                int effectValue = 0;
                int effectDuration = 0;

                try {
                    effectValue = Integer.parseInt(tokens[3].trim());
                } catch (NumberFormatException e) {
                    logger.warning("[ItemDB] Invalid effectValue for " + type + " -> '" + tokens[3]
                        + "'. Using 0.");
                }

                try {
                    effectDuration = Integer.parseInt(tokens[4].trim());
                } catch (NumberFormatException e) {
                    logger.warning(
                        "[ItemDB] Invalid effectDuration for " + type + " -> '" + tokens[4]
                            + "'. Using 0.");
                }

                // Optional extended columns: id, displayName, description
                String id = type;                 // fallback: use type as id
                String displayName = type;        // fallback: show type as name
                String description = "No description.";

                if (tokens.length > 5 && !tokens[5].trim().isEmpty()) {
                    id = tokens[5].trim();
                }
                if (tokens.length > 6 && !tokens[6].trim().isEmpty()) {
                    displayName = tokens[6].trim();
                }
                if (tokens.length > 7 && !tokens[7].trim().isEmpty()) {
                    description = tokens[7].trim();
                }

                // activationType (index 8)
                ActivationType activationType = ActivationType.INSTANT_ON_PICKUP;
                if (tokens.length > 8 && tokens[8] != null && !tokens[8].trim().isEmpty()) {
                    String at = tokens[8].trim().toUpperCase();
                    try {
                        activationType = ActivationType.valueOf(at);
                    } catch (IllegalArgumentException e) {
                        logger.warning("[ItemDB] Unknown activationType for " + type + " -> '" + at
                            + "'. Using INSTANT_ON_PICKUP.");
                    }
                }

                // maxCharges (index 9)
                int maxCharges = 0;
                if (tokens.length > 9 && tokens[9] != null && !tokens[9].trim().isEmpty()) {
                    try {
                        maxCharges = Integer.parseInt(tokens[9].trim());
                        if (maxCharges < 0) {
                            logger.warning("[ItemDB] Negative maxCharges for " + type + " -> '"
                                + tokens[9] + "'. Using 0.");
                            maxCharges = 0;
                        }
                    } catch (NumberFormatException e) {
                        logger.warning("[ItemDB] Invalid maxCharges for " + type + " -> '"
                            + tokens[9] + "'. Using 0.");
                    }
                }

                // cooldownSec (index 10)
                int cooldownSec = 0;
                if (tokens.length > 10 && tokens[10] != null && !tokens[10].trim().isEmpty()) {
                    try {
                        cooldownSec = Integer.parseInt(tokens[10].trim());
                        if (cooldownSec < 0) {
                            logger.warning("[ItemDB] Negative cooldownSec for " + type + " -> '"
                                + tokens[10] + "'. Using 0.");
                            cooldownSec = 0;
                        }
                    } catch (NumberFormatException e) {
                        logger.warning("[ItemDB] Invalid cooldownSec for " + type + " -> '"
                            + tokens[10] + "'. Using 0.");
                    }
                }

                // autoUseOnPickup (index 11) - default true
                boolean autoUseOnPickup = true;
                if (tokens.length > 11 && tokens[11] != null && !tokens[11].trim().isEmpty()) {
                    String v = tokens[11].trim().toLowerCase();
                    autoUseOnPickup = v.equals("true") || v.equals("1") || v.equals("yes");
                }

                // stackable (index 12) - default false
                boolean stackable = false;
                if (tokens.length > 12 && tokens[12] != null && !tokens[12].trim().isEmpty()) {
                    String v = tokens[12].trim().toLowerCase();
                    stackable = v.equals("true") || v.equals("1") || v.equals("yes");
                }

                ItemData data = new ItemData(
                    type,
                    spriteType,
                    dropTier,
                    effectValue,
                    effectDuration,
                    id,
                    displayName,
                    description,
                    activationType,
                    maxCharges,
                    cooldownSec,
                    autoUseOnPickup,
                    stackable
                );

                itemMap.put(type, data);
            }
        } catch (FileNotFoundException e) {
            Logger l = Core.getLogger();
            l.severe("Item DB file not found: " + FILE_PATH + " (" + e.getMessage() + ")");
        } catch (IOException e) {
            Logger l = Core.getLogger();
            l.severe("Failed to load item database from " + FILE_PATH + ": " + e.getMessage());
        }
    }

    /**
     * Return the ItemData object for the given item type.
     *
     * @param type type of the item.
     * @return ItemData object, or null if not found.
     */
    public ItemData getItemData(String type) {
        return itemMap.get(type);
    }

    /**
     * Return a collection of all ItemData objects.
     *
     * @return Collection of all items in the database.
     */
    public Collection<ItemData> getAllItems() {
        return itemMap.values();
    }
}