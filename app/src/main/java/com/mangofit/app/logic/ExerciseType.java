package com.mangofit.app.logic;

import androidx.annotation.ColorInt;
import androidx.annotation.StringRes;

import com.mangofit.app.R;

/** 운동 종류. 점수 = MET × 시간(분). 줄넘기도 프로토타입에서는 분으로 입력한다. */
public enum ExerciseType {
    WALK("walk", 3.5, R.string.exercise_walk, 0xFF5D9E5A),
    RUN("run", 9.8, R.string.exercise_run, 0xFFE07B3E),
    BIKE("bike", 7.5, R.string.exercise_bike, 0xFF3E7EC4),
    HIKE("hike", 6.0, R.string.exercise_hike, 0xFF8B6440),
    GYM("gym", 5.0, R.string.exercise_gym, 0xFF8E5DB5),
    YOGA("yoga", 2.5, R.string.exercise_yoga, 0xFFE3B13A),
    ROPE("rope", 11.8, R.string.exercise_rope, 0xFFC2392E);

    public final String code;
    public final double met;
    @StringRes public final int labelRes;
    @ColorInt public final int color;

    ExerciseType(String code, double met, int labelRes, int color) {
        this.code = code;
        this.met = met;
        this.labelRes = labelRes;
        this.color = color;
    }

    public static ExerciseType fromCode(String code) {
        for (ExerciseType type : values()) {
            if (type.code.equals(code)) return type;
        }
        return null;
    }
}
