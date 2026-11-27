package com.mangofit.app.logic;

import org.junit.Test;
import java.util.HashSet;
import java.util.Set;
import static org.junit.Assert.*;

public class ItemPaletteTest {
    @Test public void everySoldProductHasResolvableColorsWithSamePriceAndOwnership() {
        Set<String> ids = new HashSet<>();
        for (Item item : ItemCatalog.all()) assertTrue(item.id, ids.add(item.id));
        for (Slot slot : Slot.visibleSlots()) {
            for (Item base : ItemCatalog.shopItems(slot)) {
                assertFalse(ItemPalette.colors(base).isEmpty());
                for (ItemPalette.ColorOption color : ItemPalette.colors(base)) {
                    Item variant = ItemCatalog.get(base.id + "__" + color.code);
                    assertNotNull(variant);
                    assertEquals(base.id, variant.baseId);
                    assertEquals(base.slot, variant.slot);
                    assertEquals(base.price, variant.price);
                }
            }
        }
    }

    @Test public void hiddenHatsCannotBeSoldAndFoodUsesSuitablePalette() {
        assertTrue(ItemCatalog.shopItems(Slot.HAT).isEmpty());
        for (ItemPalette.ColorOption color : ItemPalette.colors(ItemCatalog.get("right_chicken_leg"))) {
            assertNotEquals("sky", color.code);
            assertNotEquals("mint", color.code);
        }
    }
}
