package com.mangofit.app.temp;

import android.content.Context;
import android.util.Log;

import com.mangofit.app.data.GameRepository;
import com.mangofit.app.data.JsonFiles;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

/**
 * [임시] 프로토타입 전용 도구. 최종에서는 이 패키지를 통째로 지운다.
 * 더미 데이터 불러오기: assets/dummy_data.json으로 앱 데이터를 덮어쓴다.
 * 파일의 날짜는 base_date 기준으로 적혀 있고, 불러오는 날을 base_date로 보고 전부 옮긴다.
 * 그래서 언제 시연해도 '지금 망고 12일째' 같은 같은 모습이 나온다.
 */
public final class TempTools {

    private static final String TAG = "TempTools";
    private static final String DUMMY_FILE = "dummy_data.json";
    private static final DateTimeFormatter CREATED_AT = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");

    private TempTools() { }

    /** @return 성공하면 true */
    public static boolean loadDummyData(Context context, LocalDate today) {
        try (InputStream in = context.getAssets().open(DUMMY_FILE)) {
            JSONObject root = new JSONObject(JsonFiles.readAll(in));
            long shift = ChronoUnit.DAYS.between(LocalDate.parse(root.getString("base_date")), today);

            JSONObject prefs = root.getJSONObject("prefs");
            shiftDate(prefs, "first_start_date", shift);
            shiftDate(prefs, "mango_start_date", shift);

            JSONArray workouts = root.getJSONArray("workouts");
            for (int i = 0; i < workouts.length(); i++) {
                JSONObject w = workouts.getJSONObject(i);
                shiftDate(w, "date", shift);
                LocalDateTime created = LocalDateTime.parse(w.getString("created_at"), CREATED_AT);
                w.put("created_at", created.plusDays(shift).format(CREATED_AT));
            }

            JSONArray mangos = root.getJSONArray("mangos");
            for (int i = 0; i < mangos.length(); i++) {
                JSONObject m = mangos.getJSONObject(i);
                shiftDate(m, "start_date", shift);
                shiftDate(m, "end_date", shift);
            }

            GameRepository.get(context).tempImportAll(prefs, workouts, mangos, today);
            return true;
        } catch (IOException | JSONException | RuntimeException e) {
            Log.e(TAG, "더미 데이터 불러오기 실패", e);
            return false;
        }
    }

    private static void shiftDate(JSONObject o, String key, long days) throws JSONException {
        o.put(key, LocalDate.parse(o.getString(key)).plusDays(days).toString());
    }
}
