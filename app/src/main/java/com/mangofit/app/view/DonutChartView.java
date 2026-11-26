package com.mangofit.app.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;

import androidx.core.content.ContextCompat;

import com.mangofit.app.R;

import java.util.ArrayList;
import java.util.List;

/** 운동 종류별 비율 도넛 차트. 가운데에 이름표와 값(예: 총 운동 / 1,800분)을 쓴다. */
public class DonutChartView extends View {

    public static final class Slice {
        final float value;
        final int color;

        public Slice(float value, int color) {
            this.value = value;
            this.color = color;
        }
    }

    private final Paint arcPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint labelPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint valuePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final RectF oval = new RectF();
    private final List<Slice> slices = new ArrayList<>();
    private final int trackColor;
    private String centerLabel = "";
    private String centerValue = "";

    public DonutChartView(Context context) {
        this(context, null);
    }

    public DonutChartView(Context context, AttributeSet attrs) {
        super(context, attrs);
        arcPaint.setStyle(Paint.Style.STROKE);
        trackColor = ContextCompat.getColor(context, R.color.beige);

        labelPaint.setTextAlign(Paint.Align.CENTER);
        labelPaint.setColor(ContextCompat.getColor(context, R.color.text_secondary));
        labelPaint.setTextSize(sp(13));

        valuePaint.setTextAlign(Paint.Align.CENTER);
        valuePaint.setColor(ContextCompat.getColor(context, R.color.text_primary));
        valuePaint.setTextSize(sp(22));
        valuePaint.setTypeface(Typeface.DEFAULT_BOLD);
    }

    public void setData(List<Slice> data, String label, String value) {
        slices.clear();
        slices.addAll(data);
        centerLabel = label;
        centerValue = value;
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        float size = Math.min(getWidth() - getPaddingLeft() - getPaddingRight(),
                getHeight() - getPaddingTop() - getPaddingBottom());
        float stroke = size * 0.18f;
        float cx = getPaddingLeft() + (getWidth() - getPaddingLeft() - getPaddingRight()) / 2f;
        float cy = getPaddingTop() + (getHeight() - getPaddingTop() - getPaddingBottom()) / 2f;
        float r = (size - stroke) / 2f;
        oval.set(cx - r, cy - r, cx + r, cy + r);
        arcPaint.setStrokeWidth(stroke);

        float total = 0;
        for (Slice s : slices) total += s.value;

        if (total <= 0) {
            arcPaint.setColor(trackColor);
            canvas.drawArc(oval, 0, 360, false, arcPaint);
        } else {
            float start = -90;
            for (Slice s : slices) {
                float sweep = 360f * s.value / total;
                arcPaint.setColor(s.color);
                canvas.drawArc(oval, start, sweep, false, arcPaint);
                start += sweep;
            }
        }

        float labelY = cy - sp(6);
        canvas.drawText(centerLabel, cx, labelY, labelPaint);
        canvas.drawText(centerValue, cx, labelY + sp(28), valuePaint);
    }

    private float sp(float value) {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, value, getResources().getDisplayMetrics());
    }
}
