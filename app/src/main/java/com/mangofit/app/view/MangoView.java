package com.mangofit.app.view;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.mangofit.app.R;
import com.mangofit.app.data.Equipment;
import com.mangofit.app.logic.Item;
import com.mangofit.app.logic.ItemCatalog;
import com.mangofit.app.logic.Slot;
import com.mangofit.app.ui.ItemArt;

/**
 * 망고 본체 위에 장착 아이템을 겹쳐 그린다.
 * 아이템마다 위치·크기(Placement)가 있고, 화면에 그려진 망고 영역을 기준으로 배치한다.
 */
public class MangoView extends ViewGroup {

    private final ImageView base;
    // 아래에서 위로: 본체 → 상의 → 모자 → 손
    private final ImageView top;
    private final ImageView hat;
    private final ImageView hand;
    private Item topItem;
    private Item hatItem;
    private Item handItem;

    public MangoView(Context context) {
        this(context, null);
    }

    public MangoView(Context context, AttributeSet attrs) {
        super(context, attrs);
        base = addLayer();
        base.setImageResource(R.drawable.mango_base);
        base.setScaleType(ImageView.ScaleType.FIT_CENTER);
        base.setContentDescription(context.getString(R.string.mango_image));
        top = addLayer();
        hat = addLayer();
        hand = addLayer();
    }

    private ImageView addLayer() {
        ImageView iv = new ImageView(getContext());
        iv.setScaleType(ImageView.ScaleType.FIT_XY);
        addView(iv);
        return iv;
    }

    public void setEquipment(Equipment equipment) {
        for (Slot slot : Slot.values()) {
            setItem(slot, equipment.get(slot));
        }
    }

    /** itemId가 null이면 그 부위를 비운다 */
    public void setItem(Slot slot, String itemId) {
        Item item = ItemCatalog.get(itemId);
        switch (slot) {
            case HAT: hatItem = item; break;
            case TOP: topItem = item; break;
            default: handItem = item; break;
        }
        ImageView layer = layerFor(slot);
        ItemArt.apply(layer, item);
        layer.setVisibility(item == null ? View.GONE : View.VISIBLE);
        requestLayout();
    }

    private ImageView layerFor(Slot slot) {
        switch (slot) {
            case HAT: return hat;
            case TOP: return top;
            default: return hand;
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        Drawable d = base.getDrawable();
        int w = getDefaultSize(d.getIntrinsicWidth(), widthMeasureSpec);
        int h = getDefaultSize(d.getIntrinsicHeight(), heightMeasureSpec);
        setMeasuredDimension(w, h);

        base.measure(exactly(w), exactly(h));
        float[] mango = mangoRect(w, h);
        measureItem(top, topItem, mango);
        measureItem(hat, hatItem, mango);
        measureItem(hand, handItem, mango);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        int w = r - l;
        int h = b - t;
        base.layout(0, 0, w, h);
        float[] mango = mangoRect(w, h);
        layoutItem(top, topItem, mango);
        layoutItem(hat, hatItem, mango);
        layoutItem(hand, handItem, mango);
    }

    /** 뷰 안에서 망고 그림이 실제로 그려지는 영역 (비율 유지, 가운데 정렬) */
    private float[] mangoRect(int w, int h) {
        Drawable d = base.getDrawable();
        float dw = d.getIntrinsicWidth();
        float dh = d.getIntrinsicHeight();
        float scale = Math.min(w / dw, h / dh);
        float mw = dw * scale;
        float mh = dh * scale;
        return new float[]{(w - mw) / 2f, (h - mh) / 2f, mw, mh};
    }

    private float[] itemRect(ImageView view, Item item, float[] mango) {
        Drawable d = view.getDrawable();
        float aspect = (float) d.getIntrinsicWidth() / d.getIntrinsicHeight();
        return item.placement.rectIn(mango[0], mango[1], mango[2], mango[3], aspect);
    }

    private void measureItem(ImageView view, Item item, float[] mango) {
        if (item == null) return;
        float[] r = itemRect(view, item, mango);
        view.measure(exactly(Math.round(r[2])), exactly(Math.round(r[3])));
    }

    private void layoutItem(ImageView view, Item item, float[] mango) {
        if (item == null) return;
        float[] r = itemRect(view, item, mango);
        int left = Math.round(r[0]);
        int topPx = Math.round(r[1]);
        view.layout(left, topPx, left + view.getMeasuredWidth(), topPx + view.getMeasuredHeight());
    }

    private static int exactly(int size) {
        return MeasureSpec.makeMeasureSpec(Math.max(0, size), MeasureSpec.EXACTLY);
    }
}
