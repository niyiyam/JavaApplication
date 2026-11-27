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

    @Test public void oldPlaceholderItemsAreRemoved() {
        // 옛 임시 아이템(모자/상의/손 a·b × 빨강·파랑·초록)은 합성 이미지로 바꾸면서 지웠다
        assertNull(ItemCatalog.get("top_a_red"));
        assertNull(ItemCatalog.get("hand_b_green"));
        assertNull(ItemCatalog.get("hat_a_red"));
        assertTrue(ItemCatalog.bySlot(Slot.HAT).isEmpty());
    }
}
