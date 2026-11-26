package com.mangofit.app.logic;

/**
 * 아이템을 망고 위 어디에 얼마나 크게 겹칠지.
 * 망고 이미지 정중앙이 (0, 0)이고 아래쪽이 +y. 좌표와 너비는 모두 망고 너비 = 1.0 기준이다.
 * 높이는 아이템 그림의 가로세로 비율대로 정해진다.
 */
public final class Placement {
    public final float x;
    public final float y;
    public final float width;

    public Placement(float x, float y, float width) {
        this.x = x;
        this.y = y;
        this.width = width;
    }

    /**
     * 화면에 그려진 망고 영역 기준으로 아이템을 그릴 영역을 구한다.
     *
     * @param itemAspect 아이템 그림의 너비 / 높이
     * @return {left, top, width, height}
     */
    public float[] rectIn(float mangoLeft, float mangoTop, float mangoWidth, float mangoHeight, float itemAspect) {
        float w = width * mangoWidth;
        float h = itemAspect > 0 ? w / itemAspect : w;
        float centerX = mangoLeft + mangoWidth / 2f + x * mangoWidth;
        float centerY = mangoTop + mangoHeight / 2f + y * mangoWidth;
        return new float[]{centerX - w / 2f, centerY - h / 2f, w, h};
    }
}
