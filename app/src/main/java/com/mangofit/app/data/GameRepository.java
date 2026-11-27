package com.mangofit.app.data;

import android.content.Context;
import android.os.SystemClock;

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
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.HashSet;
import java.util.HashMap;
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
    public static final long LOVE_DURATION_MS = 7_000;
    private int reactionMango;
    private long grinUntil;
    private long loveUntil;

    public void startWorkoutGrin() {
        reactionMango = getMangoNo();
        grinUntil = SystemClock.elapsedRealtime() + 120_000;
        loveUntil = 0;
        notifyChanged();
    }

    public void startLove() {
        if (reactionMango != getMangoNo()) grinUntil = 0;
        reactionMango = getMangoNo();
        loveUntil = SystemClock.elapsedRealtime() + LOVE_DURATION_MS;
        notifyChanged();
    }

    public long workoutGrinRemaining() {
        return reactionMango == getMangoNo() ? Math.max(0, grinUntil - SystemClock.elapsedRealtime()) : 0;
    }

    public long loveRemaining() {
        return reactionMango == getMangoNo() ? Math.max(0, loveUntil - SystemClock.elapsedRealtime()) : 0;
    }

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
        int score = 0, seconds = 0, leaves = 0, exp = 0;
        List<WorkoutRecord> records = getWorkoutsOn(day);
        for (WorkoutRecord r : records) {
            score += r.score;
            seconds += r.seconds;
            leaves += r.leaves;
            exp += r.exp;
        }
        return new DaySummary(getQuota(day), score, seconds, leaves, exp, records);
    }

    /** 지금 기록하면 받을 보상 (저장하지 않음). 운동 시간은 초 */
    public RewardCalc.Reward previewWorkout(ExerciseType type, int seconds, LocalDate today) {
        return RewardCalc.calc(getQuota(today), getDaySummary(today).score,
                RewardCalc.rawScoreSeconds(type.met, seconds));
    }

    public AddResult addWorkout(ExerciseType type, int seconds, LocalDateTime now) {
        LocalDate today = now.toLocalDate();
        RewardCalc.Reward reward = previewWorkout(type, seconds, today);
        int mangoLevelBefore = getMangoProgress().level;
        int userLevelBefore = getUserProgress().level;

        WorkoutRecord record = new WorkoutRecord(nextWorkoutId(), now.format(CREATED_AT), today,
                type.code, seconds, reward.score, reward.leaves, reward.exp, prefs.getMangoNo());
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

    // ---------- 운동 타이머 ----------
    // 직접 입력은 없고, 타이머로 잰 시간만 기록한다. 앱을 꺼도 시간이 흐르도록 시작 시각을 저장한다.

    /** 한 번에 기록할 수 있는 최대 시간 (10시간). 타이머를 잊고 둔 경우를 막는다 */
    public static final int MAX_WORKOUT_SECONDS = 600 * 60;

    /**
     * 한 번에 기록할 수 있는 최소 시간 (1분).
     * 점수·이파리·경험치를 기록마다 따로 반올림하므로, 몇 초짜리 기록을 반복하면
     * 점수는 0점인데 이파리만 1개씩 쌓일 수 있다. 이를 막는다.
     */
    public static final int MIN_WORKOUT_SECONDS = 60;

    /** 켜져 있는(일시정지 포함) 타이머의 운동, 없으면 null */
    public ExerciseType getTimerType() {
        String code = prefs.getTimerType();
        return code.isEmpty() ? null : ExerciseType.fromCode(code);
    }

    public boolean isTimerRunning() {
        return getTimerType() != null && prefs.getTimerStartedAt() > 0;
    }

    /** 지금까지 잰 시간(ms). 흐르는 중이면 마지막 시작 이후 시간까지 더한다 */
    public long getTimerElapsedMs(long nowMs) {
        if (getTimerType() == null) return 0;
        long started = prefs.getTimerStartedAt();
        long running = started > 0 ? Math.max(0, nowMs - started) : 0;
        return prefs.getTimerAccumMs() + running;
    }

    public void startTimer(ExerciseType type, long nowMs) {
        prefs.setTimer(type.code, nowMs, 0);
        notifyChanged();
    }

    public void pauseTimer(long nowMs) {
        if (!isTimerRunning()) return;
        prefs.setTimer(prefs.getTimerType(), 0, getTimerElapsedMs(nowMs));
        notifyChanged();
    }

    public void resumeTimer(long nowMs) {
        if (getTimerType() == null || isTimerRunning()) return;
        prefs.setTimer(prefs.getTimerType(), nowMs, prefs.getTimerAccumMs());
        notifyChanged();
    }

    public void cancelTimer() {
        prefs.clearTimer();
        notifyChanged();
    }

    /** 타이머를 끝내고 잰 시간만큼 기록한다. 1분 미만이면 기록하지 않고 null */
    public AddResult finishTimer(long nowMs, LocalDateTime now) {
        ExerciseType type = getTimerType();
        int seconds = (int) Math.min(MAX_WORKOUT_SECONDS, getTimerElapsedMs(nowMs) / 1000);
        prefs.clearTimer();
        if (type == null || seconds < MIN_WORKOUT_SECONDS) {
            notifyChanged();
            return null;
        }
        return addWorkout(type, seconds, now);
    }

    private int nextWorkoutId() {
        int max = 0;
        for (WorkoutRecord r : workouts()) max = Math.max(max, r.id);
        return max + 1;
    }

    // ---------- 상점, 꾸미기 ----------

    public boolean isOwned(String itemId) {
        return getOwnedVariant(itemId) != null;
    }

    /** 색상을 바꿔도 동일한 상품 하나를 보유한다. 기존 원본 ID도 그대로 읽는다. */
    public Item getOwnedVariant(String itemId) {
        Item requested = ItemCatalog.get(itemId);
        if (requested == null) return null;
        for (String id : prefs.getOwnedItems()) {
            Item owned = ItemCatalog.get(id);
            if (owned != null && owned.baseId.equals(requested.baseId)) return owned;
        }
        return null;
    }

    /** 보유 상품의 색을 무료로 변경한다. 이미 장착한 상품이면 장착 색도 갱신한다. */
    public boolean changeItemColor(String itemId) {
        Item selected = ItemCatalog.get(itemId);
        Item before = getOwnedVariant(itemId);
        if (selected == null || before == null) return false;
        if (before.id.equals(itemId)) return true;
        List<String> owned = prefs.getOwnedItems();
        owned.set(owned.indexOf(before.id), itemId);
        prefs.setOwnedItems(owned);
        Equipment equipped = prefs.getEquipment();
        if (before.id.equals(equipped.get(selected.slot))) {
            prefs.setEquipment(equipped.with(selected.slot, itemId));
        }
        notifyChanged();
        return true;
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
        if (isOwned(itemId)) return BuyResult.ALREADY_OWNED;
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
            Item item = ItemCatalog.get(id);
            if (item != null && item.slot == slot && owned.contains(id)) safe = safe.with(slot, id);
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

    /**
     * 운동 종목별 시간(초). mangoNo가 null이면 전체 기록. 기록이 없는 종목은 빠진다.
     * 강도(약/강)는 나누지 않고 종목 이름으로 합친다.
     */
    public Map<String, Integer> getSecondsByName(Integer mangoNo) {
        Map<String, Integer> result = new LinkedHashMap<>();
        for (WorkoutRecord r : workouts()) {
            if (mangoNo != null && r.mangoNo != mangoNo) continue;
            String name = nameOf(r);
            Integer before = result.get(name);
            result.put(name, (before == null ? 0 : before) + r.seconds);
        }
        return result;
    }

    /** 기록에 보여줄 종목 이름 (강도 제외). 모르는 코드면 코드 그대로 */
    public static String nameOf(WorkoutRecord r) {
        ExerciseType type = ExerciseType.fromCode(r.type);
        return type == null ? r.type : type.name;
    }

    /**
     * 기간 통계. mangoNo가 null이면 전체 기록(첫날 ~ 오늘), 아니면 그 망고의 기간.
     * 그날의 할당량은 저장하지 않으므로, 그날 첫 기록 전까지의 유저 경험치로 다시 계산한다.
     * (할당량은 하루 시작에 고정되고 하루 중 레벨이 올라도 바뀌지 않는다는 규칙과 같다.)
     */
    public PeriodStats getPeriodStats(Integer mangoNo, LocalDate today) {
        LocalDate start;
        LocalDate end;
        if (mangoNo == null) {
            start = prefs.getFirstStartDate();
            end = today;
        } else if (mangoNo == prefs.getMangoNo()) {
            start = prefs.getMangoStartDate();
            end = today;
        } else {
            ArchivedMango m = getArchivedMango(mangoNo);
            start = m == null ? today : m.startDate;
            end = m == null ? today : m.endDate;
        }

        List<WorkoutRecord> sorted = new ArrayList<>(workouts());
        Collections.sort(sorted, new Comparator<WorkoutRecord>() {
            @Override
            public int compare(WorkoutRecord a, WorkoutRecord b) {
                int c = a.date.compareTo(b.date);
                return c != 0 ? c : Integer.compare(a.id, b.id);
            }
        });

        PeriodStats s = new PeriodStats();
        s.periodDays = Math.max(1, (int) ChronoUnit.DAYS.between(start, end) + 1);
        s.end = end;
        Map<LocalDate, Integer> dayScore = new LinkedHashMap<>();
        Map<LocalDate, Integer> dayQuota = new LinkedHashMap<>();
        Map<String, Integer> secondsByName = new LinkedHashMap<>();
        int cumExp = 0;
        LocalDate curDay = null;
        int dayStartExp = 0;
        for (WorkoutRecord r : sorted) {
            if (!r.date.equals(curDay)) {
                curDay = r.date;
                dayStartExp = cumExp;
            }
            cumExp += r.exp;
            if (mangoNo != null && r.mangoNo != mangoNo) continue;
            s.seconds += r.seconds;
            Integer daySec = s.daySeconds.get(r.date);
            s.daySeconds.put(r.date, (daySec == null ? 0 : daySec) + r.seconds);
            s.score += r.score;
            s.count++;
            s.metHours += metOf(r) * r.seconds / 3600.0;
            Integer before = dayScore.get(r.date);
            dayScore.put(r.date, (before == null ? 0 : before) + r.score);
            if (!dayQuota.containsKey(r.date)) {
                dayQuota.put(r.date, Levels.quotaFor(Levels.levelForExp(dayStartExp)));
            }
            String name = nameOf(r);
            Integer sec = secondsByName.get(name);
            secondsByName.put(name, (sec == null ? 0 : sec) + r.seconds);
        }
        s.activeDays = dayScore.size();
        for (Map.Entry<LocalDate, Integer> e : dayScore.entrySet()) {
            int quota = dayQuota.get(e.getKey());
            if (e.getValue() >= quota) {
                s.goalDays++;
                s.goalDates.add(e.getKey());
            }
            if (e.getValue() > quota) s.overDays++;
        }
        LocalDate prev = null;
        int run = 0;
        for (LocalDate d : dayScore.keySet()) {
            run = (prev != null && prev.plusDays(1).equals(d)) ? run + 1 : 1;
            s.longestStreak = Math.max(s.longestStreak, run);
            prev = d;
        }
        int best = -1;
        for (Map.Entry<String, Integer> e : secondsByName.entrySet()) {
            if (e.getValue() > best) {
                best = e.getValue();
                s.topName = e.getKey();
            }
        }
        if (mangoNo == null) {
            s.mangoCount = mangos().size() + 1;
            s.bestMangoNo = prefs.getMangoNo();
            s.bestMangoLevel = getMangoProgress().level;
            for (ArchivedMango m : mangos()) {
                int level = Levels.levelForExp(m.getExp());
                if (level > s.bestMangoLevel) {
                    s.bestMangoLevel = level;
                    s.bestMangoNo = m.no;
                }
            }
        }
        return s;
    }

    private static double metOf(WorkoutRecord r) {
        ExerciseType type = ExerciseType.fromCode(r.type);
        return type == null ? 0 : type.met;
    }

    // ---------- 프로필 ----------

    public void setUserName(String name) {
        prefs.setUserName(name);
        notifyChanged();
    }

    /** 체중(kg). 입력하지 않았으면 0 */
    public float getWeightKg() {
        return prefs.getWeightKg();
    }

    public void setWeightKg(float kg) {
        prefs.setWeightKg(kg);
        notifyChanged();
    }

    /**
     * 프로필 사진. 형식: "" (기본 아이콘), "base:GREEN" (기본 망고 색), "current" (지금 망고), "mango:3" (보관된 3번째 망고)
     */
    public String getAvatar() {
        return prefs.getAvatar();
    }

    public void setAvatar(String avatar) {
        prefs.setAvatar(avatar);
        notifyChanged();
    }

    // ---------- 시연 도구 ----------
    // 발표 시연용. 상단 바의 이파리 칸을 길게 누르면 나오는 숨은 메뉴에서만 쓴다.

    /** 지금 망고를 오늘 날짜로 망고집에 보내고 오늘부터 새 망고를 시작한다 (28일을 기다리지 않고) */
    public ArchivedMango demoArchiveNow(LocalDate today) {
        ArchivedMango mango = archive(today, today);
        notifyChanged();
        return mango;
    }

    /** 켜진 타이머에 시간을 더한다. 타이머가 꺼져 있으면 false */
    public boolean demoAddTimerTime(long ms) {
        if (getTimerType() == null) return false;
        prefs.setTimer(prefs.getTimerType(), prefs.getTimerStartedAt(), prefs.getTimerAccumMs() + ms);
        notifyChanged();
        return true;
    }

    /**
     * 지금 망고에 경험치를 더한다 (유저 경험치는 망고 경험치의 합이라 같이 오른다).
     * 하루 보상은 할당량에서 막히므로, 망고 색이 바뀌는 레벨(3·5·7·9)을 바로 보여줄 때 쓴다.
     * @return 더하기 전과 후의 망고 레벨
     */
    public int[] demoAddMangoExp(int amount) {
        int before = getMangoProgress().level;
        prefs.setMangoProgress(prefs.getMangoExp() + amount, prefs.getMangoLeavesEarned());
        notifyChanged();
        return new int[]{before, getMangoProgress().level};
    }

    public void demoAddLeaves(int amount) {
        prefs.setLeaves(prefs.getLeaves() + amount);
        notifyChanged();
    }

    /**
     * 오늘 기록 초기화. 오늘 날짜 기록을 지우고 그 기록으로 받은 이파리·경험치를 되돌린다.
     * 이파리를 이미 써서 모자라면 보유량은 0에서 멈춘다.
     */
    public void demoResetToday(LocalDate today) {
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

    /** 기간 통계 (공용 상세 블록의 추가 통계) */
    public static final class PeriodStats {
        public int periodDays;
        /** 기간 마지막 날 (진행 중이면 오늘) */
        public LocalDate end;
        /** 날짜별 운동 시간(초). 운동한 날만 들어 있다 */
        public final Map<LocalDate, Integer> daySeconds = new HashMap<>();
        /** 할당량을 채운 날 */
        public final Set<LocalDate> goalDates = new HashSet<>();
        public int seconds;
        public int score;
        public int count;
        /** 기록마다 MET × 시간(시간 단위)을 더한 값. 소모 칼로리(kcal) = 체중(kg) × 이 값 */
        public double metHours;
        public int activeDays;
        public int longestStreak;
        public int goalDays;
        public int overDays;
        /** 가장 오래 한 종목 이름 (강도 제외). 기록이 없으면 null */
        public String topName;
        /** 아래는 전체 기록에서만 채운다 */
        public int mangoCount;
        public int bestMangoNo;
        public int bestMangoLevel;
    }

    public static final class DaySummary {
        public final int quota;
        public final int score;
        public final int seconds;
        /** 분 단위 표시용 (반올림) */
        public final int minutes;
        public final int leaves;
        public final int exp;
        public final List<WorkoutRecord> records;

        DaySummary(int quota, int score, int seconds, int leaves, int exp, List<WorkoutRecord> records) {
            this.quota = quota;
            this.score = score;
            this.seconds = seconds;
            this.minutes = Math.round(seconds / 60f);
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
