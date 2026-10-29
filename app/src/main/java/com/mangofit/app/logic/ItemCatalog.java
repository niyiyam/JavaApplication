package com.mangofit.app.logic;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 아이템 목록 (코드 상수). 부위별 모양 2개 × 색 3개.
 * 프로토타입 가격: 모양 A = 200, 모양 B = 500.
 */
public final class ItemCatalog {

    public static final String[] SHAPES = {"a", "b"};
    public static final String[] COLORS = {"red", "blue", "green"};

    private static final List<Item> ALL;

    static {
        List<Item> items = new ArrayList<>();
        for (Slot slot : Slot.values()) {
            for (String shape : SHAPES) {
                for (String color : COLORS) {
                    items.add(new Item(slot, shape, color, priceFor(shape)));
                }
            }
        }
        ALL = Collections.unmodifiableList(items);
    }

    private ItemCatalog() { }

    private static int priceFor(String shape) {
        return "a".equals(shape) ? 200 : 500;
    }

    public static List<Item> all() {
        return ALL;
    }

    public static List<Item> bySlot(Slot slot) {
        List<Item> result = new ArrayList<>();
        for (Item item : ALL) {
            if (item.slot == slot) result.add(item);
        }
        return result;
    }

    /** 없는 ID면 null */
    public static Item get(String id) {
        if (id == null) return null;
        for (Item item : ALL) {
            if (item.id.equals(id)) return item;
        }
        return null;
    }
}
