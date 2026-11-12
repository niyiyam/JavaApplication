package com.mangofit.app.view;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.TransitionDrawable;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.mangofit.app.R;
import com.mangofit.app.data.Equipment;
import com.mangofit.app.logic.Item;
import com.mangofit.app.logic.ItemCatalog;
import com.mangofit.app.logic.Slot;
import com.mangofit.app.logic.MangoStage;
import com.mangofit.app.ui.ItemArt;
import com.mangofit.app.ui.MangoArt;
import com.mangofit.app.ui.MangoFace;

/**
 * 망고 본체 위에 장착 아이템을 겹쳐 그린다.
 * 아이템마다 위치·크기(Placement)가 있고, 화면에 그려진 망고 영역을 기준으로 배치한다.
 */
public class MangoView extends ViewGroup {

    private final ImageView base;
    private final ImageView face;
    private final ImageView leftHand;
    private Item leftHandItem;
    private final ImageView top;
    private final ImageView hand;
    private Item topItem;
    private MangoStage stage = MangoStage.GREEN;
    private Drawable bodyDrawable;
    private boolean hasAppearance;
    private Item handItem;

    public MangoView(Context context) {
        this(context, null);
    }

    public MangoView(Context context, AttributeSet attrs) {
        super(context, attrs);
        base = addLayer();
        bodyDrawable = MangoArt.body(context, stage, null);
        base.setImageDrawable(bodyDrawable);
        base.setScaleType(ImageView.ScaleType.FIT_CENTER);
        base.setContentDescription(context.getString(R.string.mango_image));
        top = addLayer();
        face = addLayer();
        face.setImageResource(R.drawable.mangofit_face_01_smile);
        leftHand = addLayer();
        hand = addLayer();
    }

    private ImageView addLayer() {
        ImageView iv = new ImageView(getContext());
        iv.setScaleType(ImageView.ScaleType.FIT_XY);
        addView(iv);
        return iv;
    }

    public void setFace(MangoFace expression) {
        face.setImageResource(expression.drawable);
    }

    public void setAppearance(Equipment equipment, int mangoLevel) {
        MangoStage nextStage = MangoStage.forLevel(mangoLevel);
        Item nextTop = ItemCatalog.get(equipment.get(Slot.TOP));
        if (stage != nextStage || topItem != nextTop) {
            Drawable next = MangoArt.body(getContext(), nextStage, nextTop);
            if (hasAppearance && stage != nextStage && isShown() && isLaidOut()) {
                TransitionDrawable transition = new TransitionDrawable(new Drawable[]{bodyDrawable, next});
                transition.setCrossFadeEnabled(true);
                base.setImageDrawable(transition);
                transition.startTransition(320);
                base.postDelayed(() -> {
                    if (base.getDrawable() == transition) base.setImageDrawable(next);
                }, 320);
            } else {
                base.setImageDrawable(next);
            }
            bodyDrawable = next;
        }
        stage = nextStage;
        hasAppearance = true;
        topItem = nextTop;
        leftHandItem = ItemCatalog.get(equipment.get(Slot.LEFT_HAND));
        handItem = ItemCatalog.get(equipment.get(Slot.HAND));
        bindLayer(top, topItem != null && !topItem.fullBody ? topItem : null);
        bindLayer(leftHand, leftHandItem);
        bindLayer(hand, handItem);
        requestLayout();
    }

    private void bindLayer(ImageView layer, Item item) {
        ItemArt.apply(layer, item);
        layer.setVisibility(item == null ? View.GONE : View.VISIBLE);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        Drawable d = base.getDrawable();
        int w = getDefaultSize(d.getIntrinsicWidth(), widthMeasureSpec);
        int h = getDefaultSize(d.getIntrinsicHeight(), heightMeasureSpec);
        setMeasuredDimension(w, h);

        base.measure(exactly(w), exactly(h));
        float[] mango = mangoRect(w, h);
        face.measure(exactly(Math.round(mango[2])), exactly(Math.round(mango[3])));
        measureItem(leftHand, leftHandItem, mango);
        measureItem(top, topItem, mango);
        measureItem(hand, handItem, mango);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        int w = r - l;
        int h = b - t;
        base.layout(0, 0, w, h);
        float[] mango = mangoRect(w, h);
        int faceLeft = Math.round(mango[0]);
        int faceTop = Math.round(mango[1] - mango[2] * 0.125f);
        face.layout(faceLeft, faceTop, faceLeft + face.getMeasuredWidth(), faceTop + face.getMeasuredHeight());
        layoutItem(leftHand, leftHandItem, mango);
        layoutItem(top, topItem, mango);
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
        if (item == null || item.fullBody) return;
        float[] r = itemRect(view, item, mango);
        view.measure(exactly(Math.round(r[2])), exactly(Math.round(r[3])));
    }

    private void layoutItem(ImageView view, Item item, float[] mango) {
        if (item == null || item.fullBody) return;
        float[] r = itemRect(view, item, mango);
        int left = Math.round(r[0]);
        int topPx = Math.round(r[1]);
        view.layout(left, topPx, left + view.getMeasuredWidth(), topPx + view.getMeasuredHeight());
    }

    private static int exactly(int size) {
        return MeasureSpec.makeMeasureSpec(Math.max(0, size), MeasureSpec.EXACTLY);
    }
}
