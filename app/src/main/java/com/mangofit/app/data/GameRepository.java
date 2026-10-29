package com.mangofit.app.data;

import android.content.Context;

import com.mangofit.app.logic.ExerciseType;
import com.mangofit.app.logic.Item;
import com.mangofit.app.logic.ItemCatalog;
import com.mangofit.app.logic.Levels;
import com.mangofit.app.logic.MangoCycle;
import com.mangofit.app.logic.RewardCalc;
import com.mangofit.app.logic.Slot;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * 앱 데이터의 단일 창구. 화면은 저장 방식을 몰라도 되고 여기만 호출한다.
 * 모든 메서드는 메인 스레드에서 호출한다고 가정한다.
 */
public final class GameRepository {

    public interface Listener {
        void onDataChanged();
    }

    public enum BuyResult { OK, ALREADY_OWNED, NOT_ENOUGH_LEAVES, UNKNOWN_ITEM }

    private static final String WORKOUTS_FILE = "workouts.json";
    private static final String MANGOS_FILE = "mangos.json";
    private static final DateTimeFormatter CREATED_AT = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");

    private static GameRepository instance;

    public static synchronized GameRepository get(Context context) {
        if (instance == null) instance = new GameRepository(context.getApplicationContext());
        return instance;
    }

    private final Context app;
    private final Prefs prefs;
    private final List<Listener> listeners = new ArrayList<>();
    private List<WorkoutRecord> workouts;
    private List<ArchivedMango> mangos;

    private GameRepository(Context app) {
        this.app = app;
        this.prefs = new Prefs(app);
    }

    // ---------- 변경 알림 ----------

    public void addListener(Listener l) {
        if (!listeners.contains(l)) listeners.add(l);
    }

    public void removeListener(Listener l) {
        listeners.remove(l);
    }

    private void notifyChanged() {
        for (Listener l : new ArrayList<>(listeners)) l.onDataChanged();
    }

    // ---------- 첫 실행, 앱 열기 ----------

    public boolean needsUserName() {
        return prefs.getUserName().isEmpty();
    }

    public void startNewUser(String name, LocalDate today) {
        prefs.initUser(name, today);
        workouts = new ArrayList<>();
        mangos = new ArrayList<>();
        saveWorkouts();
        saveMangos();
        ensureQuota(today);
        notifyChanged();
    }

    /**
     * 앱을 열 때마다 호출한다. 28일차가 끝났으면 지금 망고를 보관하고 오늘부터 새 망고를 시작한다.
     * 오래 접속하지 않았어도 보관되는 망고는 하나뿐이다.
     *
     * @return 이번에 정산되어 보관된 망고, 없으면 null
     */
    public ArchivedMango onAppOpen(LocalDate today) {
        ArchivedMango settled = null;
        LocalDate start = prefs.getMangoStartDate();
        if (MangoCycle.isOver(start, today)) {
            settled = archive(MangoCycle.endDate(start), today);
        }
        ensureQuota(today);
        if (settled != null) notifyChanged();
        return settled;
    }

    /** 할당량은 그날 처음 열 때 고정한다. 하루 도중 레벨이 올라도 다음 날부터 적용된다. */
    private void ensureQuota(LocalDate today) {
        if (!today.toString().equals(prefs.getQuotaDate())) {
            prefs.setQuota(today, Levels.quotaFor(getUserProgress().level));
        }
    }

    // ---------- 유저 ----------

    public String getUserName() {
        return prefs.getUserName();
    }

    public LocalDate getFirstStartDate() {
        return prefs.getFirstStartDate();
    }

    public int getLeaves() {
        return prefs.getLeaves();
    }

    /** 유저 경험치 = 보관된 망고 경험치 합 + 지금 망고 경험치 */
    public int getUserExp() {
        int sum = prefs.getMangoExp();
        for (ArchivedMango m : mangos()) sum += m.getExp();
        return sum;
    }

    public Levels.Progress getUserProgress() {
        return Levels.progressFor(getUserExp());
    }

    /** 지금까지 번 이파리 전체 (써도 줄지 않음) */
    public int getTotalLeavesEarned() {
        int sum = prefs.getMangoLeavesEarned();
        for (ArchivedMango m : mangos()) sum += m.getLeavesEarned();
        return sum;
    }

    // ---------- 지금 망고 ----------

    public int getMangoNo() {
        return prefs.getMangoNo();
    }

    public LocalDate getMangoStartDate() {
        return prefs.getMangoStartDate();
    }

    public int getMangoDay(LocalDate today) {
        return MangoCycle.dayNumber(prefs.getMangoStartDate(), today);
    }

    public int getMangoExp() {
        return prefs.getMangoExp();
    }

    public Levels.Progress getMangoProgress() {
        return Levels.progressFor(prefs.getMangoExp());
    }

    public int getMangoLeavesEarned() {
        return prefs.getMangoLeavesEarned();
    }

    // ---------- 오늘 운동 ----------

    public int getQuota(LocalDate today) {
        ensureQuota(today);
        return prefs.getQuotaValue();
    }

    public List<WorkoutRecord> getWorkoutsOn(LocalDate day) {
        List<WorkoutRecord> result = new ArrayList<>();
        for (WorkoutRecord r : workouts()) {
            if (r.date.equals(day)) result.add(r);
        }
        return result;
    }

    public DaySummary getDaySummary(LocalDate day) {
        int score = 0, minutes = 0, leaves = 0, exp = 0;
        List<WorkoutRecord> records = getWorkoutsOn(day);
        for (WorkoutRecord r : records) {
            score += r.score;
            minutes += r.minutes;
            leaves += r.leaves;
            exp += r.exp;
        }
        return new DaySummary(getQuota(day), score, minutes, leaves, exp, records);
    }

    /** 지금 기록하면 받을 보상 (저장하지 않음) */
    public RewardCalc.Reward previewWorkout(ExerciseType type, int minutes, LocalDate today) {
        return RewardCalc.calc(getQuota(today), getDaySummary(today).score,
                RewardCalc.rawScore(type.met, minutes));
    }

    public AddResult addWorkout(ExerciseType type, int minutes, LocalDateTime now) {
        LocalDate today = now.toLocalDate();
        RewardCalc.Reward reward = previewWorkout(type, minutes, today);
        int mangoLevelBefore = getMangoProgress().level;
        int userLevelBefore = getUserProgress().level;

        WorkoutRecord record = new WorkoutRecord(nextWorkoutId(), now.format(CREATED_AT), today,
                type.code, minutes, reward.score, reward.leaves, reward.exp, prefs.getMangoNo());
        workouts().add(record);
        saveWorkouts();

        prefs.setLeaves(prefs.getLeaves() + reward.leaves);
        prefs.setMangoProgress(prefs.getMangoExp() + reward.exp,
                prefs.getMangoLeavesEarned() + reward.leaves);

        AddResult result = new AddResult(reward, mangoLevelBefore, getMangoProgress().level,
                userLevelBefore, getUserProgress().level);
        notifyChanged();
        return result;
    }

    private int nextWorkoutId() {
        int max = 0;
        for (WorkoutRecord r : workouts()) max = Math.max(max, r.id);
        return max + 1;
    }

    // ---------- 상점, 꾸미기 ----------

    public boolean isOwned(String itemId) {
        return prefs.getOwnedItems().contains(itemId);
    }

    /** 그 부위의 보유 아이템 (상점 목록 순서) */
    public List<Item> getOwnedItems(Slot slot) {
        List<String> owned = prefs.getOwnedItems();
        List<Item> result = new ArrayList<>();
        for (Item item : ItemCatalog.bySlot(slot)) {
            if (owned.contains(item.id)) result.add(item);
        }
        return result;
    }

    public BuyResult buy(String itemId) {
        Item item = ItemCatalog.get(itemId);
        if (item == null) return BuyResult.UNKNOWN_ITEM;
        List<String> owned = prefs.getOwnedItems();
        if (owned.contains(itemId)) return BuyResult.ALREADY_OWNED;
        if (prefs.getLeaves() < item.price) return BuyResult.NOT_ENOUGH_LEAVES;
        prefs.setLeaves(prefs.getLeaves() - item.price);
        owned.add(itemId);
        prefs.setOwnedItems(owned);
        notifyChanged();
        return BuyResult.OK;
    }

    public Equipment getEquipment() {
        return prefs.getEquipment();
    }

    /** 꾸미기 화면의 '적용'. 보유하지 않은 아이템은 장착하지 않는다. */
    public void setEquipment(Equipment equipment) {
        List<String> owned = prefs.getOwnedItems();
        Equipment safe = Equipment.empty();
        for (Slot slot : Slot.values()) {
            String id = equipment.get(slot);
            if (id != null && owned.contains(id)) safe = safe.with(slot, id);
        }
        prefs.setEquipment(safe);
        notifyChanged();
    }

    // ---------- 망고집 ----------

    public List<ArchivedMango> getArchivedMangos() {
        return Collections.unmodifiableList(mangos());
    }

    public ArchivedMango getArchivedMango(int no) {
        for (ArchivedMango m : mangos()) {
            if (m.no == no) return m;
        }
        return null;
    }

    /**
     * 망고를 보관하고 새 망고를 시작한다. 장착 아이템은 보관된 망고에 남고 보유 목록에서 사라진다.
     * 장착하지 않은 보유 아이템과 이파리 지갑은 그대로다.
     */
    private ArchivedMango archive(LocalDate endDate, LocalDate newStart) {
        Equipment equipped = prefs.getEquipment();
        ArchivedMango mango = new ArchivedMango(prefs.getMangoNo(), prefs.getMangoStartDate(), endDate,
                prefs.getMangoExp(), prefs.getMangoLeavesEarned(), equipped);
        mangos().add(mango);
        saveMangos();

        List<String> owned = prefs.getOwnedItems();
        for (Slot slot : Slot.values()) owned.remove(equipped.get(slot));
        prefs.setOwnedItems(owned);
        prefs.setEquipment(Equipment.empty());
        prefs.startMango(mango.no + 1, newStart);
        return mango;
    }

    // ---------- 통계 ----------

    /** 운동 종류별 시간(분). mangoNo가 null이면 전체 기록. 0분인 종류는 빠진다. */
    public Map<ExerciseType, Integer> getMinutesByType(Integer mangoNo) {
        Map<ExerciseType, Integer> result = new EnumMap<>(ExerciseType.class);
        for (WorkoutRecord r : workouts()) {
            if (mangoNo != null && r.mangoNo != mangoNo) continue;
            ExerciseType type = ExerciseType.fromCode(r.type);
            if (type == null) continue;
            Integer before = result.get(type);
            result.put(type, (before == null ? 0 : before) + r.minutes);
        }
        return result;
    }

    // ---------- [임시] 프로토타입 전용: 최종에서 이 구역을 지운다 ----------

    /**
     * [임시] 오늘 기록 초기화. 오늘 날짜 기록을 지우고 그 기록으로 받은 이파리·경험치를 되돌린다.
     * 이파리를 이미 써서 모자라면 보유량은 0에서 멈춘다.
     */
    public void tempResetToday(LocalDate today) {
        int leavesBack = 0;
        int mangoExp = prefs.getMangoExp();
        int mangoLeaves = prefs.getMangoLeavesEarned();
        boolean mangosChanged = false;
        Iterator<WorkoutRecord> it = workouts().iterator();
        while (it.hasNext()) {
            WorkoutRecord r = it.next();
            if (!r.date.equals(today)) continue;
            leavesBack += r.leaves;
            if (r.mangoNo == prefs.getMangoNo()) {
                mangoExp -= r.exp;
                mangoLeaves -= r.leaves;
            } else {
                ArchivedMango m = getArchivedMango(r.mangoNo);
                if (m != null) {
                    m.exp = Math.max(0, m.exp - r.exp);
                    m.leavesEarned = Math.max(0, m.leavesEarned - r.leaves);
                    mangosChanged = true;
                }
            }
            it.remove();
        }
        saveWorkouts();
        if (mangosChanged) saveMangos();
        prefs.setMangoProgress(Math.max(0, mangoExp), Math.max(0, mangoLeaves));
        prefs.setLeaves(Math.max(0, prefs.getLeaves() - leavesBack));
        notifyChanged();
    }

    /** [임시] 홈의 보관 버튼. 오늘을 종료일로 보관하고 오늘부터 새 망고를 시작한다. */
    public ArchivedMango tempArchiveNow(LocalDate today) {
        ArchivedMango mango = archive(today, today);
        notifyChanged();
        return mango;
    }

    /** [임시] 더미 데이터 불러오기. 앱 데이터를 통째로 덮어쓴다. */
    public void tempImportAll(JSONObject prefsJson, JSONArray workoutsJson, JSONArray mangosJson,
                              LocalDate today) throws JSONException {
        List<WorkoutRecord> newWorkouts = new ArrayList<>();
        for (int i = 0; i < workoutsJson.length(); i++) {
            newWorkouts.add(WorkoutRecord.fromJson(workoutsJson.getJSONObject(i)));
        }
        List<ArchivedMango> newMangos = new ArrayList<>();
        for (int i = 0; i < mangosJson.length(); i++) {
            newMangos.add(ArchivedMango.fromJson(mangosJson.getJSONObject(i)));
        }
        prefs.importFrom(prefsJson);
        workouts = newWorkouts;
        mangos = newMangos;
        saveWorkouts();
        saveMangos();
        ensureQuota(today);
        notifyChanged();
    }

    // ---------- 파일 ----------

    private List<WorkoutRecord> workouts() {
        if (workouts == null) {
            workouts = new ArrayList<>();
            JSONArray array = JsonFiles.readArray(app, WORKOUTS_FILE);
            try {
                for (int i = 0; i < array.length(); i++) {
                    workouts.add(WorkoutRecord.fromJson(array.getJSONObject(i)));
                }
            } catch (JSONException e) {
                throw new IllegalStateException(WORKOUTS_FILE + " 형식 오류", e);
            }
        }
        return workouts;
    }

    private List<ArchivedMango> mangos() {
        if (mangos == null) {
            mangos = new ArrayList<>();
            JSONArray array = JsonFiles.readArray(app, MANGOS_FILE);
            try {
                for (int i = 0; i < array.length(); i++) {
                    mangos.add(ArchivedMango.fromJson(array.getJSONObject(i)));
                }
            } catch (JSONException e) {
                throw new IllegalStateException(MANGOS_FILE + " 형식 오류", e);
            }
        }
        return mangos;
    }

    private void saveWorkouts() {
        JSONArray array = new JSONArray();
        try {
            for (WorkoutRecord r : workouts()) array.put(r.toJson());
        } catch (JSONException e) {
            throw new IllegalStateException(e);
        }
        JsonFiles.writeArray(app, WORKOUTS_FILE, array);
    }

    private void saveMangos() {
        JSONArray array = new JSONArray();
        try {
            for (ArchivedMango m : mangos()) array.put(m.toJson());
        } catch (JSONException e) {
            throw new IllegalStateException(e);
        }
        JsonFiles.writeArray(app, MANGOS_FILE, array);
    }

    // ---------- 결과 타입 ----------

    public static final class DaySummary {
        public final int quota;
        public final int score;
        public final int minutes;
        public final int leaves;
        public final int exp;
        public final List<WorkoutRecord> records;

        DaySummary(int quota, int score, int minutes, int leaves, int exp, List<WorkoutRecord> records) {
            this.quota = quota;
            this.score = score;
            this.minutes = minutes;
            this.leaves = leaves;
            this.exp = exp;
            this.records = records;
        }
    }

    public static final class AddResult {
        public final RewardCalc.Reward reward;
        public final int mangoLevelBefore;
        public final int mangoLevelAfter;
        public final int userLevelBefore;
        public final int userLevelAfter;

        AddResult(RewardCalc.Reward reward, int mangoLevelBefore, int mangoLevelAfter,
                  int userLevelBefore, int userLevelAfter) {
            this.reward = reward;
            this.mangoLevelBefore = mangoLevelBefore;
            this.mangoLevelAfter = mangoLevelAfter;
            this.userLevelBefore = userLevelBefore;
            this.userLevelAfter = userLevelAfter;
        }

        public boolean leveledUp() {
            return mangoLevelAfter > mangoLevelBefore || userLevelAfter > userLevelBefore;
        }
    }
}
