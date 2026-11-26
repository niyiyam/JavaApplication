package com.mangofit.app.ui;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.widget.ImageView;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;
import com.mangofit.app.logic.Item;
import com.mangofit.app.logic.ItemCatalog;
import com.mangofit.app.logic.MangoStage;
import org.junit.Test;
import org.junit.runner.RunWith;
import static org.junit.Assert.*;

@RunWith(AndroidJUnit4.class)
public class TintRenderingTest {
    @Test public void clothingTintChangesFabricButKeepsSkinLeafAndTransparentCorners() {
        for (Item base : ItemCatalog.shopItems(com.mangofit.app.logic.Slot.TOP)) {
            Item variant = ItemCatalog.get(base.id + "__" + com.mangofit.app.logic.ItemPalette.colors(base).get(0).code);
            Bitmap before = original(base);
            Bitmap after = render(variant, before.getWidth(), before.getHeight());
            assertEquals(base.id + " face", before.getPixel(620, 460), after.getPixel(620, 460));
            assertEquals(base.id + " leaf", before.getPixel(790, 140), after.getPixel(790, 140));
            assertEquals(0, after.getPixel(0, 0));
            assertTrue(base.id + " fabric should change", difference(before, after) > 1000);
            before.recycle(); after.recycle();
        }
    }

    @Test public void outfitTintUsesTheCurrentGrowthArtwork() {
        Item outfit = ItemCatalog.get("outfit_army__sky");
        int previousSkin = 0;
        for (MangoStage stage : MangoStage.values()) {
            int resource = MangoArt.bodyResource(stage, outfit);
            Bitmap original = BitmapFactory.decodeResource(context().getResources(), resource);
            Bitmap tinted = MaterialTint.bitmap(context().getResources(), outfit, resource);
            int skin = original.getPixel(620, 460);
            assertNotEquals(previousSkin, skin);
            assertEquals(skin, tinted.getPixel(620, 460));
            assertTrue(stage.name(), difference(original, tinted) > 1000);
            previousSkin = skin;
            original.recycle();
        }
    }

    @Test public void teaAndMetalKeepOutlinesAndHaveVisibleTint() {
        for (String id : new String[]{"right_bubble_tea__matcha", "left_dumbbell__lavender", "right_golf_club__mint"}) {
            Item item = ItemCatalog.get(id);
            Bitmap before = original(item);
            Bitmap after = render(item, before.getWidth(), before.getHeight());
            assertEquals(0, after.getPixel(0, 0));
            assertTrue(id, difference(before, after) > 500);
            before.recycle(); after.recycle();
        }
    }

    @Test public void teaPearlsAndStrawKeepTheirOriginalColors() {
        Item item = ItemCatalog.get("right_bubble_tea__matcha");
        Bitmap before = original(item);
        Bitmap after = render(item, before.getWidth(), before.getHeight());
        // 원본 이미지의 펄 내부와 빨대 윤곽. 마스크에 포함돼도 재색칠하지 않는다.
        for (int[] point : new int[][]{{250, 390}, {260, 150}}) {
            int a = before.getPixel(point[0], point[1]);
            int b = after.getPixel(point[0], point[1]);
            assertTrue(Math.abs(Color.red(a) - Color.red(b)) < 5);
            assertTrue(Math.abs(Color.green(a) - Color.green(b)) < 5);
            assertTrue(Math.abs(Color.blue(a) - Color.blue(b)) < 5);
        }
        before.recycle(); after.recycle();
    }

    private Context context() { return InstrumentationRegistry.getInstrumentation().getTargetContext(); }
    private Bitmap original(Item item) { return BitmapFactory.decodeResource(context().getResources(), item.artwork); }
    private Bitmap render(Item item, int width, int height) {
        Bitmap out = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        InstrumentationRegistry.getInstrumentation().runOnMainSync(() -> {
            ImageView view = new ImageView(context());
            ItemArt.apply(view, item);
            Drawable drawable = view.getDrawable();
            drawable.setBounds(0, 0, width, height);
            drawable.draw(new Canvas(out));
        });
        return out;
    }
    private int difference(Bitmap a, Bitmap b) {
        int[] first = new int[a.getWidth() * a.getHeight()];
        int[] second = new int[first.length];
        a.getPixels(first, 0, a.getWidth(), 0, 0, a.getWidth(), a.getHeight());
        b.getPixels(second, 0, b.getWidth(), 0, 0, b.getWidth(), b.getHeight());
        int changed = 0;
        for (int i = 0; i < first.length; i++) if (first[i] != second[i]) changed++;
        return changed;
    }
}
