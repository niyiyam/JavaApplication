package com.mangofit.app.data;

import android.content.Context;
import android.content.ContextWrapper;
import android.content.SharedPreferences;
import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import com.mangofit.app.logic.ExerciseType;
import com.mangofit.app.logic.MangoCycle;
import com.mangofit.app.logic.Slot;
import org.junit.Test;
import org.junit.runner.RunWith;
import java.io.File;
import java.lang.reflect.Constructor;
import java.time.LocalDate;
import java.time.LocalDateTime;
import static org.junit.Assert.*;

@RunWith(AndroidJUnit4.class)
public class ItemColorStorageTest {
    @Test public void recolorIsFreeSurvivesRestartAndKeepsArchivedColor() throws Exception {
        Context app = InstrumentationRegistry.getInstrumentation().getTargetContext();
        String fixture = "color_test_" + System.nanoTime();
        File files = new File(app.getCacheDir(), fixture);
        assertTrue(files.mkdirs());
        Context context = new ContextWrapper(app) {
            @Override public Context getApplicationContext() { return this; }
            @Override public File getFilesDir() { return files; }
            @Override public SharedPreferences getSharedPreferences(String name, int mode) {
                return app.getSharedPreferences(fixture, mode);
            }
        };
        try {
            Constructor<GameRepository> constructor = GameRepository.class.getDeclaredConstructor(Context.class);
            constructor.setAccessible(true);
            GameRepository repo = constructor.newInstance(context);
            LocalDate date = LocalDate.of(2026, 11, 11);
            repo.startNewUser("색상 테스트", date);
            repo.addWorkout(ExerciseType.fromCode("run"), 60 * 60, date.atTime(10, 0));
            repo.addWorkout(ExerciseType.fromCode("run"), 60 * 60, date.plusDays(1).atTime(10, 0));
            assertEquals(GameRepository.BuyResult.OK, repo.buy("right_bubble_tea__matcha"));
            repo.setEquipment(Equipment.empty().with(Slot.HAND, "right_bubble_tea__matcha"));
            int leaves = repo.getLeaves();
            assertEquals(GameRepository.BuyResult.ALREADY_OWNED, repo.buy("right_bubble_tea__rose"));
            assertTrue(repo.changeItemColor("right_bubble_tea__taro"));
            assertEquals(leaves, repo.getLeaves());
            assertEquals("right_bubble_tea__taro", repo.getEquipment().hand);
            assertEquals(1, repo.getOwnedItems(Slot.HAND).size());
            repo = constructor.newInstance(context);
            assertEquals("right_bubble_tea__taro", repo.getEquipment().hand);
            assertEquals("right_bubble_tea__taro", repo.getOwnedVariant("right_bubble_tea").id);
            ArchivedMango archived = repo.onAppOpen(date.plusDays(MangoCycle.DAYS));
            assertNotNull(archived);
            assertEquals("right_bubble_tea__taro", archived.equipped.hand);
            assertFalse(repo.isOwned("right_bubble_tea"));
            assertFalse(repo.changeItemColor("right_bubble_tea__rose"));
            repo = constructor.newInstance(context);
            assertEquals("right_bubble_tea__taro", repo.getArchivedMango(archived.no).equipped.hand);
        } finally {
            app.deleteSharedPreferences(fixture);
            File[] children = files.listFiles();
            if (children != null) for (File child : children) child.delete();
            files.delete();
        }
    }
}
