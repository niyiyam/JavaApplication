package com.mangofit.app.logic;

/** 망고 자신의 레벨에 따라 익는 순서. */
public enum MangoStage {
    GREEN(1), YELLOW_GREEN(3), YELLOW(5), GOLDEN_YELLOW(7), ORANGE(9);

    public final int minLevel;

    MangoStage(int minLevel) {
        this.minLevel = minLevel;
    }

    public static MangoStage forLevel(int level) {
        MangoStage result = GREEN;
        for (MangoStage stage : values()) {
            if (level < stage.minLevel) break;
            result = stage;
        }
        return result;
    }
}
