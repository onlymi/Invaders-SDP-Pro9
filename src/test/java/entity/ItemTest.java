package entity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import engine.gameplay.item.ActivationType;
import engine.gameplay.item.ItemData;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for Item
 */

public class ItemTest {

    private ItemData createEpicActiveMoveSpeedUpData(int cost, int maxCharges, int cooldownSec) {
        return new ItemData(
            "MOVE_SPEED_UP",
            "ItemMoveSpeedUp",
            "EPIC",
            50,
            3,
            cost,
            "EPIC_MOVE_SPEED_UP",
            "Move Speed Up",
            "Increases move speed temporarily.",
            ActivationType.ACTIVE_ON_KEY,
            maxCharges,
            cooldownSec,
            false,
            false
        );
    }

    private ItemData createFreeInstantItemData() {
        return new ItemData(
            "HEAL",
            "ItemHeal",
            "RARE",
            20,                     // effectValue
            0,                      // effectDuration
            0,                      // cost = 0 (free)
            "HEAL_FREE",
            "Small Heal",
            "Restores a small amount of HP.",
            ActivationType.INSTANT_ON_PICKUP,
            0,
            0,
            true,                   // autoUseOnPickup
            false
        );
    }

    @Test
    void getActivationType_returnsValueFromItemData() {
        ItemData data = createEpicActiveMoveSpeedUpData(10, 3, 15);
        Item item = new Item(data, 100, 100, 2);

        // when
        ActivationType activationType = item.getActivationType();

        // then
        assertEquals(ActivationType.ACTIVE_ON_KEY, activationType);
    }

    @Test
    void isAutoUseOnPickup_usesFlagFromItemData() {
        // given: autoUseOnPickup = false 인 active 아이템
        ItemData data = createEpicActiveMoveSpeedUpData(10, 3, 15);
        Item item = new Item(data, 100, 100, 2);

        // when
        boolean autoUse = item.isAutoUseOnPickup();

        // then
        assertFalse(autoUse, "ACTIVE_ON_KEY 아이템은 autoUseOnPickup=false 여야 한다.");
    }

    @Test
    void getMaxCharges_and_getCooldownSec_returnValuesFromItemData() {
        // given
        ItemData data = createEpicActiveMoveSpeedUpData(10, 3, 15);
        Item item = new Item(data, 100, 100, 2);

        // when & then
        assertEquals(3, item.getMaxCharges());
        assertEquals(15, item.getCooldownSec());
    }

    @Test
    void getFullDescription_costZero_addsNoCostRequiredLine() {
        // given: cost = 0 인 즉시 발동 힐 아이템
        ItemData data = createFreeInstantItemData();
        Item item = new Item(data, 100, 100, 2);

        // when
        String desc = item.getFullDescription(0);

        // then
        assertTrue(desc.contains("No cost required"),
            "cost == 0이면 'No cost required.' 문구가 포함되어야 한다.");
    }

    @Test
    void getFullDescription_paidItem_withEnoughCoins_showsCostButNoWarning() {
        // given: cost = 10 인 Epic Move Speed Up
        ItemData data = createEpicActiveMoveSpeedUpData(10, 3, 15);
        Item item = new Item(data, 100, 100, 2);

        // when
        String desc = item.getFullDescription(20); // 코인 20개 (충분)

        // then
        assertTrue(desc.contains("(Cost: 10)"),
            "비용이 있는 아이템은 '(Cost: X)' 문구를 포함해야 한다.");
        assertFalse(desc.contains("Not enough coins"),
            "코인이 충분하면 'Not enough coins to activate this item.' 문구는 나오면 안 된다.");
    }

    @Test
    void getFullDescription_paidItem_withNotEnoughCoins_addsWarningLine() {
        // given: cost = 10 인 Epic Move Speed Up
        ItemData data = createEpicActiveMoveSpeedUpData(10, 3, 15);
        Item item = new Item(data, 100, 100, 2);

        // when
        String desc = item.getFullDescription(5); // 코인 5개 (부족)

        // then
        assertTrue(desc.contains("(Cost: 10)"),
            "비용이 있는 아이템은 '(Cost: 10)' 문구를 포함해야 한다.");
        assertTrue(desc.contains("Not enough coins"),
            "코인이 부족하면 'Not enough coins to activate this item.' 문구가 포함되어야 한다.");
    }

    @Test
    void setSprite() {
    }

    @Test
    void applyEffect() {
    }

    @Test
    void getDisplayName() {
    }

    @Test
    void getDescription() {
    }

    @Test
    void getFullDescription() {
    }
}
