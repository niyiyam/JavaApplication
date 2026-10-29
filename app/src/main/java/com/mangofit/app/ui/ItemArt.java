package com.mangofit.app.ui;

import android.content.Context;
import android.graphics.PorterDuff;
import android.widget.ImageView;

import androidx.annotation.ColorRes;
import androidx.annotation.DrawableRes;
import androidx.annotation.StringRes;
import androidx.core.content.ContextCompat;

import com.mangofit.app.R;
import com.mangofit.app.logic.Item;

/** 아이템 그림과 이름. 모양 이미지 하나를 색 3개가 같이 쓰고 틴트(MULTIPLY)만 바꾼다. */
public final class ItemArt {

    private ItemArt() { }

    @DrawableRes
    public static int drawableFor(Item item) {
        boolean a = "a".equals(item.shape);
        switch (item.slot) {
            case HAT: return a ? R.drawable.item_hat_a : R.drawable.item_hat_b;
            case TOP: return a ? R.drawable.item_top_a : R.drawable.item_top_b;
            default: return a ? R.drawable.item_hand_a : R.drawable.item_hand_b;
        }
    }

    @ColorRes
    public static int colorRes(Item item) {
        switch (item.color) {
            case "red": return R.color.item_red;
            case "blue": return R.color.item_blue;
            default: return R.color.item_green;
        }
    }

    @StringRes
    private static int colorLabel(Item item) {
        switch (item.color) {
            case "red": return R.string.color_red;
            case "blue": return R.string.color_blue;
            default: return R.string.color_green;
        }
    }

    /** "모자 A - 빨강" */
    public static String displayName(Context context, Item item) {
        return context.getString(R.string.item_name,
                context.getString(item.slot.labelRes),
                item.shape.toUpperCase(),
                context.getString(colorLabel(item)));
    }

    /** 이미지뷰에 아이템 그림을 넣고 색을 입힌다. item이 null이면 비운다. */
    public static void apply(ImageView view, Item item) {
        if (item == null) {
            view.setImageDrawable(null);
            view.clearColorFilter();
            return;
        }
        view.setImageResource(drawableFor(item));
        view.setColorFilter(ContextCompat.getColor(view.getContext(), colorRes(item)), PorterDuff.Mode.MULTIPLY);
    }
}
