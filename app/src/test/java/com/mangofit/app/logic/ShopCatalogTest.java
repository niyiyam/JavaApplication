package com.mangofit.app.logic;

import org.junit.Test;
import java.util.HashSet;
import java.util.Set;
import static org.junit.Assert.*;

public class ShopCatalogTest {
    @Test public void catalogHasUniqueIdsAndCompleteNewCollections() {
        Set<String> ids = new HashSet<>();
        for (Item item : ItemCatalog.all()) {
            assertTrue(item.id, ids.add(item.id));
            assertTrue(item.price > 0);
        }
        assertEquals(10, ItemCatalog.shopItems(Slot.TOP).size());
        assertEquals(18, ItemCatalog.shopItems(Slot.HAND).size());
        assertEquals(18, ItemCatalog.shopItems(Slot.LEFT_HAND).size());
    }

    @Test public void oldPurchasesRemainResolvableButAreNotSoldAsNewArt() {
        Item old = ItemCatalog.get("top_a_red");
        assertNotNull(old);
        assertTrue(ItemCatalog.bySlot(Slot.TOP).contains(old));
        assertFalse(ItemCatalog.shopItems(Slot.TOP).contains(old));
        assertEquals(Slot.HAND, ItemCatalog.get("hand_b_green").slot);
    }
}
