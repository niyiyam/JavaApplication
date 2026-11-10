package com.mangofit.app.ui;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.util.LruCache;

import com.mangofit.app.logic.Item;
import com.mangofit.app.logic.ItemPalette;

/** 합쳐진 의상 PNG와 무채색 금속용 재질 마스크. 원본 파일은 수정하지 않는다. */
final class MaterialTint {
    private static final LruCache<String, Bitmap> CACHE = new LruCache<String, Bitmap>(16 * 1024 * 1024) {
        @Override protected int sizeOf(String key, Bitmap value) { return value.getAllocationByteCount(); }
    };

    private MaterialTint() { }

    static Bitmap bitmap(Resources resources, Item item) {
        Bitmap cached = CACHE.get(item.id);
        if (cached != null) return cached;
        Bitmap source = BitmapFactory.decodeResource(resources, item.artwork);
        int width = source.getWidth(), height = source.getHeight();
        int[] pixels = new int[width * height];
        source.getPixels(pixels, 0, width, 0, 0, width, height);
        int[] tintMask = null;
        Bitmap detail = null;
        if (!item.fullBody && !ItemPalette.metallic(item)) {
            int[] resourcesIds = ItemPalette.masks(item);
            Bitmap suppliedMask = BitmapFactory.decodeResource(resources, resourcesIds[0]);
            tintMask = new int[pixels.length];
            suppliedMask.getPixels(tintMask, 0, width, 0, 0, width, height);
            suppliedMask.recycle();
            detail = BitmapFactory.decodeResource(resources, resourcesIds[1]);
        }
        float[] hsv = new float[3];
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int index = y * width + x;
                int pixel = pixels[index];
                Color.colorToHSV(pixel, hsv);
                boolean selected = item.fullBody
                        ? fabric(item.baseId, y / (float) height, hsv)
                        : tintMask == null
                            ? hsv[1] < 0.22f && hsv[2] > 0.18f && hsv[2] < 0.94f
                            : Color.alpha(tintMask[index]) > 8 && handMaterial(item.baseId, y / (float) height, hsv);
                if (!selected || Color.alpha(pixel) == 0) {
                    pixels[index] = Color.TRANSPARENT;
                    continue;
                }
                // 명암은 유지하되 밝은 파스텔 원단이 어둡게 가라앉지 않게 한다.
                int gray = Math.round(255 * (0.28f + 0.72f * hsv[2]));
                int alpha = tintMask == null ? Color.alpha(pixel) : Color.alpha(tintMask[index]);
                pixels[index] = Color.argb(alpha, gray, gray, gray);
            }
        }
        Bitmap mask = Bitmap.createBitmap(pixels, width, height, Bitmap.Config.ARGB_8888);
        Bitmap result = source.copy(Bitmap.Config.ARGB_8888, true);
        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setColorFilter(new PorterDuffColorFilter(item.tint.argb, PorterDuff.Mode.MULTIPLY));
        Canvas canvas = new Canvas(result);
        canvas.drawBitmap(mask, 0, 0, paint);
        if (detail != null) {
            canvas.drawBitmap(detail, 0, 0, null);
            detail.recycle();
        }
        mask.recycle();
        source.recycle();
        CACHE.put(item.id, result);
        return result;
    }

    private static boolean handMaterial(String id, float y, float[] hsv) {
        // 원본 명암 위에 색을 얹고 검정 윤곽과 흰 하이라이트는 그대로 둔다.
        if (hsv[2] < 0.30f || (hsv[1] < 0.12f && hsv[2] > 0.85f)) return false;
        if (id.endsWith("bubble_tea")) {
            // 펄과 빨대는 고정 디테일. 음료 부분만 다른 맛의 색으로 바꾼다.
            return y > 0.27f && hsv[2] > 0.64f && hsv[0] > 20 && hsv[0] < 65;
        }
        return true;
    }

    private static boolean fabric(String id, float y, float[] hsv) {
        float hue = hsv[0], saturation = hsv[1], value = hsv[2];
        // 검정 외곽선, 흰 장식, 노란 피부와 초록 잎을 보호한다.
        if (value < 0.20f || saturation < 0.18f) return false;
        switch (id) {
            case "outfit_army":
                return y > 0.48f && hue >= 45 && hue <= 105 && saturation < 0.70f;
            case "outfit_casual":
                return y > 0.48f && hue >= 80 && hue <= 175;
            case "outfit_king":
            case "outfit_onepiece":
                return hue < 22 || hue > 340;
            case "outfit_basketball":
                return hue >= 245 && hue <= 310;
            default:
                return hue >= 195 && hue <= 290;
        }
    }
}
