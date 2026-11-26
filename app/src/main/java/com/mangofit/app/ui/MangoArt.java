package com.mangofit.app.ui;

import android.content.Context;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;

import androidx.core.content.ContextCompat;

import com.mangofit.app.R;
import com.mangofit.app.logic.Item;
import com.mangofit.app.logic.MangoStage;

/** 성장 단계별 본체와 의상 이미지를 고른다. */
public final class MangoArt {
    private MangoArt() { }

    public static Drawable body(Context context, MangoStage stage, Item outfit) {
        int resource = bodyResource(stage, outfit);
        if (outfit != null && outfit.fullBody && outfit.tint != null) {
            return new BitmapDrawable(context.getResources(),
                    MaterialTint.bitmap(context.getResources(), outfit, resource));
        }
        return ContextCompat.getDrawable(context, resource);
    }

    public static int bodyResource(MangoStage stage, Item outfit) {
        int index = stage.ordinal();
        if (outfit != null && outfit.fullBody) {
            switch (outfit.baseId) {
                case "outfit_army": return ARMY[index];
                case "outfit_baseball": return BASEBALL[index];
                case "outfit_basketball": return BASKETBALL[index];
                case "outfit_casual": return CASUAL[index];
                case "outfit_king": return KING[index];
                case "outfit_marathon": return MARATHON[index];
                case "outfit_onepiece": return ONEPIECE[index];
                case "outfit_soccer": return SOCCER[index];
                case "outfit_student": return STUDENT[index];
                case "outfit_suit": return SUIT[index];
                default: return outfit.artwork;
            }
        }
        return BASE[index];
    }

    private static final int[] BASE = {
            R.drawable.mangofit_green,
            R.drawable.mangofit_yellowgreen,
            R.drawable.mangofit_yellow,
            R.drawable.mangofit_goldenyellow,
            R.drawable.mangofit_orange
    };
    private static final int[] ARMY = {
            R.drawable.mangofit_green_army,
            R.drawable.mangofit_yellowgreen_army,
            R.drawable.mangofit_yellow_army,
            R.drawable.mangofit_goldenyellow_army,
            R.drawable.mangofit_orange_army
    };
    private static final int[] BASEBALL = {
            R.drawable.mangofit_green_baseball,
            R.drawable.mangofit_yellowgreen_baseball,
            R.drawable.mangofit_yellow_baseball,
            R.drawable.mangofit_goldenyellow_baseball,
            R.drawable.mangofit_orange_baseball
    };
    private static final int[] BASKETBALL = {
            R.drawable.mangofit_green_basketball,
            R.drawable.mangofit_yellowgreen_basketball,
            R.drawable.mangofit_yellow_basketball,
            R.drawable.mangofit_goldenyellow_basketball,
            R.drawable.mangofit_orange_basketball
    };
    private static final int[] CASUAL = {
            R.drawable.mangofit_green_casual,
            R.drawable.mangofit_yellowgreen_casual,
            R.drawable.mangofit_yellow_casual,
            R.drawable.mangofit_goldenyellow_casual,
            R.drawable.mangofit_orange_casual
    };
    private static final int[] KING = {
            R.drawable.mangofit_green_king,
            R.drawable.mangofit_yellowgreen_king,
            R.drawable.mangofit_yellow_king,
            R.drawable.mangofit_goldenyellow_king,
            R.drawable.mangofit_orange_king
    };
    private static final int[] MARATHON = {
            R.drawable.mangofit_green_marathon,
            R.drawable.mangofit_yellowgreen_marathon,
            R.drawable.mangofit_yellow_marathon,
            R.drawable.mangofit_goldenyellow_marathon,
            R.drawable.mangofit_orange_marathon
    };
    private static final int[] ONEPIECE = {
            R.drawable.mangofit_green_onepiece,
            R.drawable.mangofit_yellowgreen_onepiece,
            R.drawable.mangofit_yellow_onepiece,
            R.drawable.mangofit_goldenyellow_onepiece,
            R.drawable.mangofit_orange_onepiece
    };
    private static final int[] SOCCER = {
            R.drawable.mangofit_green_soccer,
            R.drawable.mangofit_yellowgreen_soccer,
            R.drawable.mangofit_yellow_soccer,
            R.drawable.mangofit_goldenyellow_soccer,
            R.drawable.mangofit_orange_soccer
    };
    private static final int[] STUDENT = {
            R.drawable.mangofit_green_student,
            R.drawable.mangofit_yellowgreen_student,
            R.drawable.mangofit_yellow_student,
            R.drawable.mangofit_goldenyellow_student,
            R.drawable.mangofit_orange_student
    };
    private static final int[] SUIT = {
            R.drawable.mangofit_green_suit,
            R.drawable.mangofit_yellowgreen_suit,
            R.drawable.mangofit_yellow_suit,
            R.drawable.mangofit_goldenyellow_suit,
            R.drawable.mangofit_orange_suit
    };
}
