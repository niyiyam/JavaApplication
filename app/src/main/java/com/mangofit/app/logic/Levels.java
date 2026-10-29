package com.mangofit.app.logic;

/** 레벨 곡선과 하루 할당량. 망고와 유저가 같은 곡선을 쓴다. */
public final class Levels {

    private Levels() { }

    /** 하루 할당량(점수) = 150 + 50 × 유저 레벨 */
    public static int quotaFor(int userLevel) {
        return 150 + 50 * userLevel;
    }

    /** 레벨 L에서 L+1로 가는 데 필요한 경험치 = 할당량 × 2 */
    public static int expToNext(int level) {
        return 300 + 100 * level;
    }

    /** 레벨 L에 도달하는 데 필요한 누적 경험치 */
    public static int cumulativeExp(int level) {
        int n = level - 1;
        return 400 * n + 50 * n * (n - 1);
    }

    public static int levelForExp(int exp) {
        int level = 1;
        while (cumulativeExp(level + 1) <= exp) {
            level++;
        }
        return level;
    }

    public static Progress progressFor(int exp) {
        int level = levelForExp(exp);
        return new Progress(level, exp - cumulativeExp(level), expToNext(level));
    }

    /** 레벨과 그 레벨 안에서의 경험치 진행도 */
    public static final class Progress {
        public final int level;
        public final int current;
        public final int needed;

        public Progress(int level, int current, int needed) {
            this.level = level;
            this.current = current;
            this.needed = needed;
        }
    }
}
