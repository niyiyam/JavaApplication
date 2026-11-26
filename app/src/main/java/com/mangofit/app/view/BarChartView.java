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

/**
 * 날짜별 운동 시간 막대 그래프. 막대 위에 분, 아래에 요일을 쓴다.
 * 목표(할당량)를 채운 날은 진한 초록, 아닌 날은 연한 초록, 운동 안 한 날은 바닥선만.
 */
public class BarChartView extends View {

    private final Paint barPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint valuePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint labelPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint basePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final RectF rect = new RectF();
    private final int goalColor;
    private final int normalColor;

    private int[] values = new int[0];
    private String[] labels = new String[0];
    private String[] valueTexts = new String[0];
    private boolean[] goals = new boolean[0];

    public BarChartView(Context context) {
        this(context, null);
    }

    public BarChartView(Context context, AttributeSet attrs) {
        super(context, attrs);
        goalColor = ContextCompat.getColor(context, R.color.green_primary);
        normalColor = ContextCompat.getColor(context, R.color.avatar_bg);
        valuePaint.setTextAlign(Paint.Align.CENTER);
        valuePaint.setTextSize(sp(11));
        valuePaint.setTypeface(Typeface.DEFAULT_BOLD);
        valuePaint.setColor(ContextCompat.getColor(context, R.color.text_primary));
        labelPaint.setTextAlign(Paint.Align.CENTER);
        labelPaint.setTextSize(sp(12));
        labelPaint.setColor(ContextCompat.getColor(context, R.color.text_secondary));
        basePaint.setColor(ContextCompat.getColor(context, R.color.border));
    }

    /** values: 막대 높이(같은 단위), valueTexts: 막대 위 글자, goals: 진하게 칠할 막대 */
    public void setData(int[] values, String[] valueTexts, String[] labels, boolean[] goals) {
        this.values = values;
        this.valueTexts = valueTexts;
        this.labels = labels;
        this.goals = goals;
        setContentDescription(describe());
        invalidate();
    }

    private String describe() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < values.length; i++) {
            if (i > 0) sb.append(", ");
            sb.append(labels[i]).append(' ').append(valueTexts[i]);
        }
        return sb.toString();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        int n = values.length;
        if (n == 0) return;
        float left = getPaddingLeft();
        float width = getWidth() - getPaddingLeft() - getPaddingRight();
        float labelArea = sp(22);
        float valueArea = sp(18);
        float bottom = getHeight() - getPaddingBottom() - labelArea;
        float top = getPaddingTop() + valueArea;
        float slot = width / n;
        float barWidth = Math.min(slot * 0.56f, dp(28));

        int max = 1;
        for (int v : values) max = Math.max(max, v);

        canvas.drawRect(left, bottom, left + width, bottom + dp(1), basePaint);
        for (int i = 0; i < n; i++) {
            float cx = left + slot * (i + 0.5f);
            if (values[i] > 0) {
                float h = Math.max(dp(4), (bottom - top) * values[i] / max);
                rect.set(cx - barWidth / 2f, bottom - h, cx + barWidth / 2f, bottom);
                barPaint.setColor(goals[i] ? goalColor : normalColor);
                canvas.drawRoundRect(rect, dp(5), dp(5), barPaint);
                canvas.drawText(valueTexts[i], cx, rect.top - dp(4), valuePaint);
            }
            canvas.drawText(labels[i], cx, bottom + labelArea - dp(5), labelPaint);
        }
    }

    private float sp(float v) {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, v, getResources().getDisplayMetrics());
    }

    private float dp(float v) {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, v, getResources().getDisplayMetrics());
    }
}
