package entity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import engine.gameplay.item.ActivationType;
import engine.gameplay.item.ItemData;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for Item
 */
public class ItemTest {
    
    private ItemData createEpicActiveMoveSpeedUpData(int maxCharges, int cooldownSec) {
        return new ItemData(
            "MOVE_SPEED_UP",           // type
            "ItemMoveSpeedUp",              // spriteType
            "EPIC",                         // dropTier
            50,                             // effectValue
            3,                              // effectDuration (seconds)
            "EPIC_MOVE_SPEED_UP",           // id
            "Move Speed Up",                // displayName
            "Increases move speed temporarily.", // description
            ActivationType.ACTIVE_ON_KEY,   // activationType
            maxCharges,                     // maxCharges
            cooldownSec,                    // cooldownSec
            false,                          // autoUseOnPickup
            false                           // stackable
        );
    }
    
    private ItemData createInstantHealItemData() {
        return new ItemData(
            "HEAL",                         // type
            "ItemHeal",                     // spriteType
            "RARE",                         // dropTier
            20,                             // effectValue
            0,                              // effectDuration
            "HEAL_INSTANT",                 // id
            "Small Heal",                   // displayName
            "Restores a small amount of HP.", // description
            ActivationType.INSTANT_ON_PICKUP,
            0,                    // maxCharges
            0,                              // cooldownSec
            true,                           // autoUseOnPickup
            false                           // stackable
        );
    }
    
    @Test
    void getActivationType_returnsValueFromItemData() {
        // given
        ItemData data = createEpicActiveMoveSpeedUpData(10, 15);
        Item item = new Item(data, 100, 100, 2);
        
        // when
        ActivationType activationType = item.getActivationType();
        
        // then
        assertEquals(ActivationType.ACTIVE_ON_KEY, activationType);
    }
    
    @Test
    void isAutoUseOnPickup_usesFlagFromItemData() {
        // given: autoUseOnPickup = false 인 active 아이템
        ItemData data = createEpicActiveMoveSpeedUpData(10, 15);
        Item item = new Item(data, 100, 100, 2);
        
        // when
        boolean autoUse = item.isAutoUseOnPickup();
        
        // then
        assertFalse(autoUse, "ACTIVE_ON_KEY 아이템은 autoUseOnPickup=false 여야 한다.");
    }
    
    @Test
    void getMaxCharges_and_getCooldownSec_returnValuesFromItemData() {
        // given
        ItemData data = createEpicActiveMoveSpeedUpData(3, 15);
        Item item = new Item(data, 100, 100, 2);
        
        // when & then
        assertEquals(3, item.getMaxCharges());
        assertEquals(15, item.getCooldownSec());
    }
    
    @Test
    void getFullDescription_returnsBaseDescriptionOnly() {
        // given: 즉시 발동 힐 아이템
        ItemData data = createInstantHealItemData();
        Item item = new Item(data, 100, 100, 2);
        
        // when
        String desc = item.getFullDescription(0);
    }
}