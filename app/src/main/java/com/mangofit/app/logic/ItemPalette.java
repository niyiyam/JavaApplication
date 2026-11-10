package com.mangofit.app.logic;

import com.mangofit.app.R;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/** 상품 재질에 맞춘 제한 팔레트. RGB 직접 입력 대신 이름 있는 색상을 고른다. */
public final class ItemPalette {
    private ItemPalette() { }

    public static final class ColorOption {
        public final String code;
        public final int label;
        public final int argb;
        private ColorOption(String code, int label, int argb) {
            this.code = code; this.label = label; this.argb = argb;
        }
    }
    public static final ColorOption ROSE = new ColorOption("rose", R.string.tint_rose, 0xFFE8B7C4);
    public static final ColorOption MINT = new ColorOption("mint", R.string.tint_mint, 0xFFB7D9CA);
    public static final ColorOption LAVENDER = new ColorOption("lavender", R.string.tint_lavender, 0xFFC9BCE3);
    public static final ColorOption SKY = new ColorOption("sky", R.string.tint_sky, 0xFFB5CEE5);
    public static final ColorOption PEACH = new ColorOption("peach", R.string.tint_peach, 0xFFF0C3A5);
    public static final ColorOption CREAM = new ColorOption("cream", R.string.tint_cream, 0xFFEADCB9);
    public static final ColorOption SAGE = new ColorOption("sage", R.string.tint_sage, 0xFFB8C8AE);
    public static final ColorOption COCOA = new ColorOption("cocoa", R.string.tint_cocoa, 0xFFC8B1A0);
    public static final ColorOption MATCHA = new ColorOption("matcha", R.string.tint_matcha, 0xFFC4D1A2);
    public static final ColorOption TARO = new ColorOption("taro", R.string.tint_taro, 0xFFCEBADA);
    public static final ColorOption HONEY = new ColorOption("honey", R.string.tint_honey, 0xFFDDBB82);
    public static final ColorOption ROAST = new ColorOption("roast", R.string.tint_roast, 0xFFC89575);

    public static List<ColorOption> colors(Item item) {
        if (item == null || item.artwork == 0) return Collections.emptyList();
        String id = item.baseId;
        if (id.endsWith("chicken_leg")) return Arrays.asList(HONEY, ROAST, COCOA);
        if (id.endsWith("bubble_tea")) return Arrays.asList(MATCHA, ROSE, TARO, CREAM);
        if (id.endsWith("lollipop") || id.endsWith("balloon")) return Arrays.asList(ROSE, MINT, LAVENDER, PEACH);
        if (id.endsWith("basketball") || id.endsWith("tennis_racket")) return Arrays.asList(PEACH, SAGE, SKY, LAVENDER);
        if (id.endsWith("army") || id.endsWith("broom") || id.endsWith("cane")) return Arrays.asList(SAGE, COCOA, CREAM, SKY);
        if (id.endsWith("king") || id.endsWith("onepiece")) return Arrays.asList(ROSE, LAVENDER, SAGE, SKY);
        if (id.endsWith("suit") || id.endsWith("briefcase")) return Arrays.asList(SKY, SAGE, COCOA, LAVENDER);
        return Arrays.asList(MINT, LAVENDER, SKY, ROSE);
    }

    /** 제공된 마스크가 사실상 비어 있는 금속 아이템은 밝기 마스크로 대신 처리한다. */
    public static boolean metallic(Item item) {
        return item.baseId.endsWith("dumbbell") || item.baseId.endsWith("golf_club");
    }

    public static int[] masks(Item item) {
        switch (item.baseId) {
            case "left_ak47": return new int[]{R.drawable.mangofit_item_ak47_left_tint, R.drawable.mangofit_item_ak47_left_detail};
            case "right_ak47": return new int[]{R.drawable.mangofit_item_ak47_right_tint, R.drawable.mangofit_item_ak47_right_detail};
            case "left_balloon": return new int[]{R.drawable.mangofit_item_balloon_left_tint, R.drawable.mangofit_item_balloon_left_detail};
            case "right_balloon": return new int[]{R.drawable.mangofit_item_balloon_right_tint, R.drawable.mangofit_item_balloon_right_detail};
            case "left_basketball": return new int[]{R.drawable.mangofit_item_basketball_left_tint, R.drawable.mangofit_item_basketball_left_detail};
            case "right_basketball": return new int[]{R.drawable.mangofit_item_basketball_right_tint, R.drawable.mangofit_item_basketball_right_detail};
            case "left_briefcase": return new int[]{R.drawable.mangofit_item_briefcase_left_tint, R.drawable.mangofit_item_briefcase_left_detail};
            case "right_briefcase": return new int[]{R.drawable.mangofit_item_briefcase_right_tint, R.drawable.mangofit_item_briefcase_right_detail};
            case "left_broom": return new int[]{R.drawable.mangofit_item_broom_left_tint, R.drawable.mangofit_item_broom_left_detail};
            case "right_broom": return new int[]{R.drawable.mangofit_item_broom_right_tint, R.drawable.mangofit_item_broom_right_detail};
            case "left_bubble_tea": return new int[]{R.drawable.mangofit_item_bubble_tea_left_tint, R.drawable.mangofit_item_bubble_tea_left_detail};
            case "right_bubble_tea": return new int[]{R.drawable.mangofit_item_bubble_tea_right_tint, R.drawable.mangofit_item_bubble_tea_right_detail};
            case "left_camera": return new int[]{R.drawable.mangofit_item_camera_left_tint, R.drawable.mangofit_item_camera_left_detail};
            case "right_camera": return new int[]{R.drawable.mangofit_item_camera_right_tint, R.drawable.mangofit_item_camera_right_detail};
            case "left_cane": return new int[]{R.drawable.mangofit_item_cane_left_tint, R.drawable.mangofit_item_cane_left_detail};
            case "right_cane": return new int[]{R.drawable.mangofit_item_cane_right_tint, R.drawable.mangofit_item_cane_right_detail};
            case "left_chicken_leg": return new int[]{R.drawable.mangofit_item_chicken_leg_left_tint, R.drawable.mangofit_item_chicken_leg_left_detail};
            case "right_chicken_leg": return new int[]{R.drawable.mangofit_item_chicken_leg_right_tint, R.drawable.mangofit_item_chicken_leg_right_detail};
            case "left_dumbbell": return new int[]{R.drawable.mangofit_item_dumbbell_left_tint, R.drawable.mangofit_item_dumbbell_left_detail};
            case "right_dumbbell": return new int[]{R.drawable.mangofit_item_dumbbell_right_tint, R.drawable.mangofit_item_dumbbell_right_detail};
            case "left_fishing_rod": return new int[]{R.drawable.mangofit_item_fishing_rod_left_tint, R.drawable.mangofit_item_fishing_rod_left_detail};
            case "right_fishing_rod": return new int[]{R.drawable.mangofit_item_fishing_rod_right_tint, R.drawable.mangofit_item_fishing_rod_right_detail};
            case "left_gamepad": return new int[]{R.drawable.mangofit_item_gamepad_left_tint, R.drawable.mangofit_item_gamepad_left_detail};
            case "right_gamepad": return new int[]{R.drawable.mangofit_item_gamepad_right_tint, R.drawable.mangofit_item_gamepad_right_detail};
            case "left_golf_club": return new int[]{R.drawable.mangofit_item_golf_club_left_tint, R.drawable.mangofit_item_golf_club_left_detail};
            case "right_golf_club": return new int[]{R.drawable.mangofit_item_golf_club_right_tint, R.drawable.mangofit_item_golf_club_right_detail};
            case "left_lollipop": return new int[]{R.drawable.mangofit_item_lollipop_left_tint, R.drawable.mangofit_item_lollipop_left_detail};
            case "right_lollipop": return new int[]{R.drawable.mangofit_item_lollipop_right_tint, R.drawable.mangofit_item_lollipop_right_detail};
            case "left_magic_wand": return new int[]{R.drawable.mangofit_item_magic_wand_left_tint, R.drawable.mangofit_item_magic_wand_left_detail};
            case "right_magic_wand": return new int[]{R.drawable.mangofit_item_magic_wand_right_tint, R.drawable.mangofit_item_magic_wand_right_detail};
            case "left_pistol": return new int[]{R.drawable.mangofit_item_pistol_left_tint, R.drawable.mangofit_item_pistol_left_detail};
            case "right_pistol": return new int[]{R.drawable.mangofit_item_pistol_right_tint, R.drawable.mangofit_item_pistol_right_detail};
            case "left_sword": return new int[]{R.drawable.mangofit_item_sword_left_tint, R.drawable.mangofit_item_sword_left_detail};
            case "right_sword": return new int[]{R.drawable.mangofit_item_sword_right_tint, R.drawable.mangofit_item_sword_right_detail};
            case "left_tennis_racket": return new int[]{R.drawable.mangofit_item_tennis_racket_left_tint, R.drawable.mangofit_item_tennis_racket_left_detail};
            case "right_tennis_racket": return new int[]{R.drawable.mangofit_item_tennis_racket_right_tint, R.drawable.mangofit_item_tennis_racket_right_detail};
            default: return new int[]{0, 0};
        }
    }
}
