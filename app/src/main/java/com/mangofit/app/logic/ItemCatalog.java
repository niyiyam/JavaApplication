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
                    items.add(new Item(slot, shape, color, priceFor(shape), placementFor(slot, shape)));
                }
            }
        }
        ALL = Collections.unmodifiableList(items);
    }

    private ItemCatalog() { }

    private static int priceFor(String shape) {
        return "a".equals(shape) ? 200 : 500;
    }

    /**
     * 모양별 위치·크기. 망고 이미지 정중앙 (0, 0), 아래쪽 +y, 망고 너비 = 1.0.
     * [임시] 값은 임시 아이템 그림에 맞춘 것이라 실제 에셋이 나오면 다시 맞춘다.
     */
    private static Placement placementFor(Slot slot, String shape) {
        boolean a = "a".equals(shape);
        switch (slot) {
            case HAT: return a ? new Placement(0f, -0.277f, 0.704f) : new Placement(0f, -0.206f, 0.694f);
            case TOP: return a ? new Placement(0f, 0.349f, 0.767f) : new Placement(0f, 0.266f, 0.701f);
            default: return a ? new Placement(0.417f, 0.279f, 0.159f) : new Placement(-0.390f, 0.312f, 0.219f);
        }
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
