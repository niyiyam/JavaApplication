package com.mangofit.app.logic;

import androidx.annotation.StringRes;

import com.mangofit.app.R;

/** 꾸미기 부위. 프로토타입은 모자, 상의, 손 3부위만 쓴다. */
public enum Slot {
    HAT("hat", R.string.slot_hat),
    TOP("top", R.string.slot_top),
    HAND("hand", R.string.slot_hand);

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
