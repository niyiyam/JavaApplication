package com.mangofit.app.ui;

import android.content.Context;
import android.widget.ImageView;


import com.mangofit.app.R;
import com.mangofit.app.logic.Item;

/** 원본·색상별 아이템을 공통으로 표시한다. 마스크 tint와 고정 디테일을 합성한다. */
public final class ItemArt {

    private ItemArt() { }

    /** "풍선" 또는 색을 바꾼 경우 "풍선 · 민트 크림" */
    public static String displayName(Context context, Item item) {
        String name = context.getString(item.label);
        return item.tint == null ? name : context.getString(R.string.tint_named_item,
                name, context.getString(item.tint.label));
    }

    /** 이미지뷰에 아이템 그림을 넣고 색을 입힌다. item이 null이면 비운다. */
    public static void apply(ImageView view, Item item) {
        if (item == null) {
            view.setImageDrawable(null);
            view.clearColorFilter();
            return;
        }
        view.clearColorFilter();
        if (item.tint != null) {
            view.setImageBitmap(MaterialTint.bitmap(view.getResources(), item));
            return;
        }
        view.setImageResource(item.artwork);
    }
}
