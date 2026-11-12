package com.mangofit.app.ui;

import com.mangofit.app.R;

public enum MangoFace {
    SMILE(R.drawable.mangofit_face_01_smile),
    GRIN(R.drawable.mangofit_face_02_grin),
    PARTY(R.drawable.mangofit_face_03_party),
    HAPPY_TEARS(R.drawable.mangofit_face_05_happy_tears),
    YAWN(R.drawable.mangofit_face_06_yawn),
    NEUTRAL(R.drawable.mangofit_face_08_neutral),
    LOVE(R.drawable.mangofit_face_09_love);

    public final int drawable;

    MangoFace(int drawable) {
        this.drawable = drawable;
    }
}
