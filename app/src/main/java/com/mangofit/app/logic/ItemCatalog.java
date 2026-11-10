package com.mangofit.app.logic;

import com.mangofit.app.R;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 판매할 원본 이미지 아이템과 기존 구매 데이터의 호환 목록.
 * 임시 가격: 의상 500(곤룡포 800), 좌우 손 아이템 각각 300 이파리.
 */
public final class ItemCatalog {

    public static final String[] SHAPES = {"a", "b"};
    public static final String[] COLORS = {"red", "blue", "green"};

    private static final List<Item> ALL;

    static {
        List<Item> items = new ArrayList<>();
        for (Slot slot : new Slot[]{Slot.HAT, Slot.TOP, Slot.HAND}) {
            for (String shape : SHAPES) {
                for (String color : COLORS) {
                    items.add(new Item(slot, shape, color, priceFor(shape), placementFor(slot, shape)));
                }
            }
        }
        items.add(new Item("outfit_army", Slot.TOP, R.drawable.mangofit_yellow_army, R.string.art_army, 500, new Placement(0f, 0f, 1f), true));
        items.add(new Item("outfit_baseball", Slot.TOP, R.drawable.mangofit_yellow_baseball, R.string.art_baseball, 500, new Placement(0f, 0f, 1f), true));
        items.add(new Item("outfit_basketball", Slot.TOP, R.drawable.mangofit_yellow_basketball, R.string.art_basketball_outfit, 500, new Placement(0f, 0f, 1f), true));
        items.add(new Item("outfit_casual", Slot.TOP, R.drawable.mangofit_yellow_casual, R.string.art_casual, 500, new Placement(0f, 0f, 1f), true));
        items.add(new Item("outfit_king", Slot.TOP, R.drawable.mangofit_yellow_king, R.string.art_king, 800, new Placement(0f, 0f, 1f), true));
        items.add(new Item("outfit_marathon", Slot.TOP, R.drawable.mangofit_yellow_marathon, R.string.art_marathon, 500, new Placement(0f, 0f, 1f), true));
        items.add(new Item("outfit_onepiece", Slot.TOP, R.drawable.mangofit_yellow_onepiece, R.string.art_onepiece, 500, new Placement(0f, 0f, 1f), true));
        items.add(new Item("outfit_soccer", Slot.TOP, R.drawable.mangofit_yellow_soccer, R.string.art_soccer, 500, new Placement(0f, 0f, 1f), true));
        items.add(new Item("outfit_student", Slot.TOP, R.drawable.mangofit_yellow_student, R.string.art_student, 500, new Placement(0f, 0f, 1f), true));
        items.add(new Item("outfit_suit", Slot.TOP, R.drawable.mangofit_yellow_suit, R.string.art_suit, 500, new Placement(0f, 0f, 1f), true));
        items.add(new Item("left_ak47", Slot.LEFT_HAND, R.drawable.mangofit_item_ak47_left_preview, R.string.art_ak47, 300, new Placement(-0.30f, 0.17f, 0.46875f), false));
        items.add(new Item("right_ak47", Slot.HAND, R.drawable.mangofit_item_ak47_right_preview, R.string.art_ak47, 300, new Placement(0.30f, 0.17f, 0.46875f), false));
        items.add(new Item("left_balloon", Slot.LEFT_HAND, R.drawable.mangofit_item_balloon_left_preview, R.string.art_balloon, 300, new Placement(-0.30f, 0.17f, 0.46875f), false));
        items.add(new Item("right_balloon", Slot.HAND, R.drawable.mangofit_item_balloon_right_preview, R.string.art_balloon, 300, new Placement(0.30f, 0.17f, 0.46875f), false));
        items.add(new Item("left_basketball", Slot.LEFT_HAND, R.drawable.mangofit_item_basketball_left_preview, R.string.art_basketball, 300, new Placement(-0.30f, 0.17f, 0.46875f), false));
        items.add(new Item("right_basketball", Slot.HAND, R.drawable.mangofit_item_basketball_right_preview, R.string.art_basketball, 300, new Placement(0.30f, 0.17f, 0.46875f), false));
        items.add(new Item("left_briefcase", Slot.LEFT_HAND, R.drawable.mangofit_item_briefcase_left_preview, R.string.art_briefcase, 300, new Placement(-0.30f, 0.17f, 0.46875f), false));
        items.add(new Item("right_briefcase", Slot.HAND, R.drawable.mangofit_item_briefcase_right_preview, R.string.art_briefcase, 300, new Placement(0.30f, 0.17f, 0.46875f), false));
        items.add(new Item("left_broom", Slot.LEFT_HAND, R.drawable.mangofit_item_broom_left_preview, R.string.art_broom, 300, new Placement(-0.30f, 0.17f, 0.46875f), false));
        items.add(new Item("right_broom", Slot.HAND, R.drawable.mangofit_item_broom_right_preview, R.string.art_broom, 300, new Placement(0.30f, 0.17f, 0.46875f), false));
        items.add(new Item("left_bubble_tea", Slot.LEFT_HAND, R.drawable.mangofit_item_bubble_tea_left_preview, R.string.art_bubble_tea, 300, new Placement(-0.30f, 0.17f, 0.46875f), false));
        items.add(new Item("right_bubble_tea", Slot.HAND, R.drawable.mangofit_item_bubble_tea_right_preview, R.string.art_bubble_tea, 300, new Placement(0.30f, 0.17f, 0.46875f), false));
        items.add(new Item("left_camera", Slot.LEFT_HAND, R.drawable.mangofit_item_camera_left_preview, R.string.art_camera, 300, new Placement(-0.30f, 0.17f, 0.46875f), false));
        items.add(new Item("right_camera", Slot.HAND, R.drawable.mangofit_item_camera_right_preview, R.string.art_camera, 300, new Placement(0.30f, 0.17f, 0.46875f), false));
        items.add(new Item("left_cane", Slot.LEFT_HAND, R.drawable.mangofit_item_cane_left_preview, R.string.art_cane, 300, new Placement(-0.30f, 0.17f, 0.46875f), false));
        items.add(new Item("right_cane", Slot.HAND, R.drawable.mangofit_item_cane_right_preview, R.string.art_cane, 300, new Placement(0.30f, 0.17f, 0.46875f), false));
        items.add(new Item("left_chicken_leg", Slot.LEFT_HAND, R.drawable.mangofit_item_chicken_leg_left_preview, R.string.art_chicken_leg, 300, new Placement(-0.30f, 0.17f, 0.46875f), false));
        items.add(new Item("right_chicken_leg", Slot.HAND, R.drawable.mangofit_item_chicken_leg_right_preview, R.string.art_chicken_leg, 300, new Placement(0.30f, 0.17f, 0.46875f), false));
        items.add(new Item("left_dumbbell", Slot.LEFT_HAND, R.drawable.mangofit_item_dumbbell_left_preview, R.string.art_dumbbell, 300, new Placement(-0.30f, 0.17f, 0.46875f), false));
        items.add(new Item("right_dumbbell", Slot.HAND, R.drawable.mangofit_item_dumbbell_right_preview, R.string.art_dumbbell, 300, new Placement(0.30f, 0.17f, 0.46875f), false));
        items.add(new Item("left_fishing_rod", Slot.LEFT_HAND, R.drawable.mangofit_item_fishing_rod_left_preview, R.string.art_fishing_rod, 300, new Placement(-0.30f, 0.17f, 0.46875f), false));
        items.add(new Item("right_fishing_rod", Slot.HAND, R.drawable.mangofit_item_fishing_rod_right_preview, R.string.art_fishing_rod, 300, new Placement(0.30f, 0.17f, 0.46875f), false));
        items.add(new Item("left_gamepad", Slot.LEFT_HAND, R.drawable.mangofit_item_gamepad_left_preview, R.string.art_gamepad, 300, new Placement(-0.30f, 0.17f, 0.46875f), false));
        items.add(new Item("right_gamepad", Slot.HAND, R.drawable.mangofit_item_gamepad_right_preview, R.string.art_gamepad, 300, new Placement(0.30f, 0.17f, 0.46875f), false));
        items.add(new Item("left_golf_club", Slot.LEFT_HAND, R.drawable.mangofit_item_golf_club_left_preview, R.string.art_golf_club, 300, new Placement(-0.30f, 0.17f, 0.46875f), false));
        items.add(new Item("right_golf_club", Slot.HAND, R.drawable.mangofit_item_golf_club_right_preview, R.string.art_golf_club, 300, new Placement(0.30f, 0.17f, 0.46875f), false));
        items.add(new Item("left_lollipop", Slot.LEFT_HAND, R.drawable.mangofit_item_lollipop_left_preview, R.string.art_lollipop, 300, new Placement(-0.30f, 0.17f, 0.46875f), false));
        items.add(new Item("right_lollipop", Slot.HAND, R.drawable.mangofit_item_lollipop_right_preview, R.string.art_lollipop, 300, new Placement(0.30f, 0.17f, 0.46875f), false));
        items.add(new Item("left_magic_wand", Slot.LEFT_HAND, R.drawable.mangofit_item_magic_wand_left_preview, R.string.art_magic_wand, 300, new Placement(-0.30f, 0.17f, 0.46875f), false));
        items.add(new Item("right_magic_wand", Slot.HAND, R.drawable.mangofit_item_magic_wand_right_preview, R.string.art_magic_wand, 300, new Placement(0.30f, 0.17f, 0.46875f), false));
        items.add(new Item("left_pistol", Slot.LEFT_HAND, R.drawable.mangofit_item_pistol_left_preview, R.string.art_pistol, 300, new Placement(-0.30f, 0.17f, 0.46875f), false));
        items.add(new Item("right_pistol", Slot.HAND, R.drawable.mangofit_item_pistol_right_preview, R.string.art_pistol, 300, new Placement(0.30f, 0.17f, 0.46875f), false));
        items.add(new Item("left_sword", Slot.LEFT_HAND, R.drawable.mangofit_item_sword_left_preview, R.string.art_sword, 300, new Placement(-0.30f, 0.17f, 0.46875f), false));
        items.add(new Item("right_sword", Slot.HAND, R.drawable.mangofit_item_sword_right_preview, R.string.art_sword, 300, new Placement(0.30f, 0.17f, 0.46875f), false));
        items.add(new Item("left_tennis_racket", Slot.LEFT_HAND, R.drawable.mangofit_item_tennis_racket_left_preview, R.string.art_tennis_racket, 300, new Placement(-0.30f, 0.17f, 0.46875f), false));
        items.add(new Item("right_tennis_racket", Slot.HAND, R.drawable.mangofit_item_tennis_racket_right_preview, R.string.art_tennis_racket, 300, new Placement(0.30f, 0.17f, 0.46875f), false));
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

    /** 기존 상의·손 아이템은 보유 목록에서 유지하고 새 판매 목록에서는 제외한다. */
    public static List<Item> shopItems(Slot slot) {
        List<Item> result = new ArrayList<>();
        for (Item item : bySlot(slot)) {
            if (item.artwork != 0 || slot == Slot.HAT) result.add(item);
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
