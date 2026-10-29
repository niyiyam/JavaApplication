package com.mangofit.app.ui;

import android.app.Activity;
import android.view.View;

import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;

/**
 * targetSdk 35 이상에서는 화면이 상태 바·내비게이션 바 아래까지 그려진다.
 * 겹치지 않도록 해당 뷰에 시스템 바 높이만큼 여백을 더한다.
 */
public final class SystemBars {

    private SystemBars() { }

    /** 상태 바 아이콘 색. 배경이 밝으면 true(어두운 아이콘) */
    public static void setLightStatusBar(Activity activity, boolean light) {
        WindowCompat.getInsetsController(activity.getWindow(), activity.getWindow().getDecorView())
                .setAppearanceLightStatusBars(light);
    }

    public static void padTop(View view) {
        pad(view, true, false);
    }

    public static void padBottom(View view) {
        pad(view, false, true);
    }

    public static void padTopAndBottom(View view) {
        pad(view, true, true);
    }

    private static void pad(View view, boolean top, boolean bottom) {
        final int left = view.getPaddingLeft();
        final int baseTop = view.getPaddingTop();
        final int right = view.getPaddingRight();
        final int baseBottom = view.getPaddingBottom();
        ViewCompat.setOnApplyWindowInsetsListener(view, (v, insets) -> {
            Insets bars = insets.getInsets(WindowInsetsCompat.Type.systemBars() | WindowInsetsCompat.Type.ime());
            v.setPadding(left, baseTop + (top ? bars.top : 0), right, baseBottom + (bottom ? bars.bottom : 0));
            return insets;
        });
        ViewCompat.requestApplyInsets(view);
    }
}
