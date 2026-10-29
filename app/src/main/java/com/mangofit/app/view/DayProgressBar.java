package com.mangofit.app.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

import androidx.core.content.ContextCompat;

import com.mangofit.app.R;
import com.mangofit.app.logic.RewardCalc;

/**
 * 오늘 진행 막대. 구간 길이 비율은 보너스 25 : 일반 75 : 무리 50 (할당량의 0~25% / 25~100% / 100~150%).
 * 바탕은 연한 색이고, 운동한 만큼 해당 구간이 진한 색으로 채워진다. 홈과 운동 탭이 같이 쓴다.
 */
public class DayProgressBar extends View {

    /** 각 구간이 맡는 할당량 비율 (시작, 끝) */
    private static final double[][] ZONES = {
            {0, RewardCalc.BONUS_END},
            {RewardCalc.BONUS_END, RewardCalc.NORMAL_END},
            {RewardCalc.NORMAL_END, RewardCalc.OVER_END},
    };

    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final int[] fillColors;
    private final int[] emptyColors;
    private final float gap;
    private int score;
    private int quota = 1;

    public DayProgressBar(Context context) {
        this(context, null);
    }

    public DayProgressBar(Context context, AttributeSet attrs) {
        super(context, attrs);
        fillColors = new int[]{
                ContextCompat.getColor(context, R.color.zone_bonus),
                ContextCompat.getColor(context, R.color.zone_normal),
                ContextCompat.getColor(context, R.color.zone_over)};
        int empty = ContextCompat.getColor(context, R.color.zone_empty);
        emptyColors = new int[]{empty, empty, ContextCompat.getColor(context, R.color.zone_empty_over)};
        gap = 4 * getResources().getDisplayMetrics().density;
    }

    public void setData(int score, int quota) {
        this.score = score;
        this.quota = Math.max(1, quota);
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        float w = getWidth() - getPaddingLeft() - getPaddingRight();
        float h = getHeight() - getPaddingTop() - getPaddingBottom();
        float left = getPaddingLeft();
        float top = getPaddingTop();

        double total = RewardCalc.OVER_END;
        float usable = w - gap * (ZONES.length - 1);
        float x = left;
        for (int i = 0; i < ZONES.length; i++) {
            double lo = ZONES[i][0];
            double hi = ZONES[i][1];
            float segW = (float) (usable * (hi - lo) / total);

            paint.setColor(emptyColors[i]);
            canvas.drawRect(x, top, x + segW, top + h, paint);

            double ratio = (double) score / quota;
            double fill = Math.max(0, Math.min(1, (ratio - lo) / (hi - lo)));
            if (fill > 0) {
                paint.setColor(fillColors[i]);
                canvas.drawRect(x, top, x + (float) (segW * fill), top + h, paint);
            }
            x += segW + gap;
        }
    }
}
