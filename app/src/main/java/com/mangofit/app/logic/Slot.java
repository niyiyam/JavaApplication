package com.mangofit.app.logic;

import androidx.annotation.StringRes;

import com.mangofit.app.R;

/** 꾸미기 부위. 기존 hand 코드는 오른손으로 유지하고 왼손을 추가한다. */
public enum Slot {
    HAT("hat", R.string.slot_hat),
    TOP("top", R.string.slot_top),
    HAND("hand", R.string.slot_hand),
    LEFT_HAND("left_hand", R.string.slot_left_hand);

    public final String code;
    @StringRes public final int labelRes;

    Slot(String code, int labelRes) {
        this.code = code;
        this.labelRes = labelRes;
    }

    public static Slot fromCode(String code) {
        for (Slot slot : values()) {
            if (slot.code.equals(code)) return slot;
        }
        return null;
    }
}
