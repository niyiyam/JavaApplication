package com.mangofit.app.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.mangofit.app.R;
import com.mangofit.app.data.Equipment;
import com.mangofit.app.logic.ItemCatalog;
import com.mangofit.app.logic.Slot;
import com.mangofit.app.ui.ItemArt;

/**
 * 망고 본체 위에 장착 아이템을 겹쳐 그린다.
 * 아이템 이미지는 망고와 같은 크기의 캔버스에 실제 위치로 그려져 있어서 그대로 겹치기만 한다.
 */
public class MangoView extends FrameLayout {

    private final ImageView base;
    private final ImageView top;
    private final ImageView hat;
    private final ImageView hand;

    public MangoView(Context context) {
        this(context, null);
    }

    public MangoView(Context context, AttributeSet attrs) {
        super(context, attrs);
        base = addLayer();
        base.setImageResource(R.drawable.mango_base);
        base.setContentDescription(context.getString(R.string.mango_image));
        // 아래에서 위로: 본체 → 상의 → 모자 → 손
        top = addLayer();
        hat = addLayer();
        hand = addLayer();
    }

    private ImageView addLayer() {
        ImageView iv = new ImageView(getContext());
        iv.setScaleType(ImageView.ScaleType.FIT_CENTER);
        addView(iv, new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        return iv;
    }

    public void setEquipment(Equipment equipment) {
        for (Slot slot : Slot.values()) {
            setItem(slot, equipment.get(slot));
        }
    }

    /** itemId가 null이면 그 부위를 비운다 */
    public void setItem(Slot slot, String itemId) {
        ItemArt.apply(layerFor(slot), ItemCatalog.get(itemId));
    }

    /** 상점 썸네일처럼 아이템만 강조할 때 본체를 흐리게 */
    public void setBaseAlpha(float alpha) {
        base.setAlpha(alpha);
    }

    private ImageView layerFor(Slot slot) {
        switch (slot) {
            case HAT: return hat;
            case TOP: return top;
            default: return hand;
        }
    }
}
