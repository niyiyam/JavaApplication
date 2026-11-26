package com.mangofit.app;

import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Filter;
import android.widget.FrameLayout;
import android.widget.Spinner;
import com.mangofit.app.logic.MangoStage;
import com.mangofit.app.data.Equipment;
import com.mangofit.app.view.MangoView;
import com.mangofit.app.ui.MangoFace;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import com.mangofit.app.data.GameRepository;
import com.mangofit.app.data.WorkoutRecord;
import com.mangofit.app.logic.ExerciseType;
import com.mangofit.app.logic.RewardCalc;
import com.mangofit.app.temp.TempTools;
import com.mangofit.app.ui.Fmt;
import com.mangofit.app.view.DayProgressBar;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;


public class ExerciseFragment extends Fragment {

    private static final long TICK_MS = 1000;

    private GameRepository repo;
    private TextView zone;
    private DayProgressBar dayBar;
    private TextView scoreValue;
    private TextView minutesValue;
    private TextView leavesValue;
    private TextView expValue;
    private LinearLayout todayList;
    private TextView todayEmpty;
    private Spinner categorySpinner;
    private AutoCompleteTextView search;
    private TextView selectedText;
    private TextView selectedNote;
    private TextView timerText;
    private TextView timerState;
    private TextView timerMain;
    private View timerActions;
    private TextView preview;
    private final Handler ticker = new Handler(Looper.getMainLooper());
    private final Runnable tick = new Runnable() {
        @Override
        public void run() {
            updateTimer();
            ticker.postDelayed(this, TICK_MS);
        }
    };
    private MangoView workoutMango;
    private final Runnable updateWorkoutFace = this::refreshWorkoutFace;

    private void refreshWorkoutFace() {
        workoutMango.removeCallbacks(updateWorkoutFace);
        long remaining = repo.workoutGrinRemaining();
        workoutMango.setVisibility(remaining > 0 ? View.VISIBLE : View.GONE);
        if (remaining > 0) {
            workoutMango.setAppearance(repo.getEquipment(), repo.getMangoProgress().level);
            workoutMango.setFace(MangoFace.GRIN);
            workoutMango.postDelayed(updateWorkoutFace, remaining);
        }
    }
    private ExerciseType selectedType = ExerciseType.defaultType();
    private final GameRepository.Listener listener = new GameRepository.Listener() {
        @Override
        public void onDataChanged() {
            refresh();
        }
    };

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_exercise, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        repo = GameRepository.get(requireContext());

        workoutMango = view.findViewById(R.id.exercise_mango);
        zone = view.findViewById(R.id.exercise_zone);
        dayBar = view.findViewById(R.id.day_progress_bar);
        scoreValue = setupTile(view, R.id.tile_score, R.string.tile_score);
        minutesValue = setupTile(view, R.id.tile_minutes, R.string.tile_minutes);
        leavesValue = setupTile(view, R.id.tile_leaves, R.string.tile_leaves);
        expValue = setupTile(view, R.id.tile_exp, R.string.tile_exp);
        todayList = view.findViewById(R.id.exercise_today_list);
        todayEmpty = view.findViewById(R.id.exercise_today_empty);

        setupPicker(view);

        timerText = view.findViewById(R.id.exercise_timer);
        timerState = view.findViewById(R.id.exercise_timer_state);
        timerMain = view.findViewById(R.id.exercise_timer_main);
        timerActions = view.findViewById(R.id.exercise_timer_actions);
        preview = view.findViewById(R.id.exercise_preview);
        timerMain.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onTimerMain();
            }
        });
        view.findViewById(R.id.exercise_timer_finish).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                confirmFinish();
            }
        });
        view.findViewById(R.id.exercise_timer_cancel).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                confirmCancel();
            }
        });

        view.findViewById(R.id.exercise_temp_reset).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                confirmTempReset();
            }
        });
        view.findViewById(R.id.exercise_temp_dummy).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                confirmTempDummy();
            }
        });
    }

    @Override
    public void onStart() {
        super.onStart();
        repo.addListener(listener);
    }

    @Override
    public void onResume() {
        super.onResume();
        refreshWorkoutFace();
        refresh();
        ticker.post(tick);
    }

    @Override
    public void onPause() {
        workoutMango.removeCallbacks(updateWorkoutFace);
        ticker.removeCallbacks(tick);
        super.onPause();
    }

    @Override
    public void onStop() {
        super.onStop();
        repo.removeListener(listener);
    }

    private TextView setupTile(View root, int tileId, int labelRes) {
        View tile = root.findViewById(tileId);
        ((TextView) tile.findViewById(R.id.stat_label)).setText(labelRes);
        return tile.findViewById(R.id.stat_value);
    }

    // ---------- 종목 고르기 ----------
    // 분류 드롭다운으로 목록을 좁히고, 검색창에 글자를 치면 이름에 그 글자가 들어간 종목을 보여준다.

    private void setupPicker(View view) {
        categorySpinner = view.findViewById(R.id.exercise_category);
        search = view.findViewById(R.id.exercise_search);
        selectedText = view.findViewById(R.id.exercise_selected);
        selectedNote = view.findViewById(R.id.exercise_selected_note);

        final List<String> categories = new ArrayList<>();
        categories.add(getString(R.string.exercise_category_all));
        categories.addAll(ExerciseType.categories());
        ArrayAdapter<String> categoryAdapter = new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_spinner_item, categories);
        categoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        categorySpinner.setAdapter(categoryAdapter);
        categorySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View v, int position, long id) {
                List<ExerciseType> list = position == 0 ? ExerciseType.selectable()
                        : ExerciseType.inCategory(categories.get(position));
                search.setAdapter(new ExerciseAdapter(list));
                if (position > 0 && search.isEnabled()) {
                    search.requestFocus();
                    search.post(new Runnable() {
                        @Override
                        public void run() {
                            if (isAdded()) search.showDropDown();
                        }
                    });
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) { }
        });

        search.setAdapter(new ExerciseAdapter(ExerciseType.selectable()));
        search.setThreshold(1);
        search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                search.showDropDown();
            }
        });
        search.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
                selectType((ExerciseType) parent.getItemAtPosition(position));
                search.setText("", false);
                search.clearFocus();
                InputMethodManager imm = (InputMethodManager) requireContext()
                        .getSystemService(Context.INPUT_METHOD_SERVICE);
                if (imm != null) imm.hideSoftInputFromWindow(search.getWindowToken(), 0);
                updateTimer();
            }
        });
        selectType(selectedType);
    }

    private void selectType(ExerciseType type) {
        selectedType = type;
        selectedText.setText(getString(R.string.exercise_selected, type.label(), Fmt.decimal(type.met)));
        String note = type.note;
        if (type.estimated) {
            note = note.isEmpty() ? getString(R.string.exercise_estimated)
                    : note + ", " + getString(R.string.exercise_estimated);
        }
        selectedNote.setText(note.isEmpty() ? type.category : type.category + " | " + note);
    }

    /** 이름의 어느 부분이든 친 글자가 들어 있으면 보여준다 (기본 ArrayAdapter는 단어 앞부분만 맞춘다) */
    private final class ExerciseAdapter extends ArrayAdapter<ExerciseType> {
        private final List<ExerciseType> all;

        ExerciseAdapter(List<ExerciseType> items) {
            super(requireContext(), R.layout.item_exercise_dropdown, new ArrayList<>(items));
            all = new ArrayList<>(items);
        }

        @NonNull
        @Override
        public Filter getFilter() {
            return new Filter() {
                @Override
                protected FilterResults performFiltering(CharSequence constraint) {
                    List<ExerciseType> matched = new ArrayList<>();
                    String q = constraint == null ? "" : constraint.toString().replace(" ", "");
                    for (ExerciseType type : all) {
                        if (q.isEmpty() || type.label().replace(" ", "").contains(q)) matched.add(type);
                    }
                    FilterResults results = new FilterResults();
                    results.values = matched;
                    results.count = matched.size();
                    return results;
                }

                @Override
                @SuppressWarnings("unchecked")
                protected void publishResults(CharSequence constraint, FilterResults results) {
                    clear();
                    if (results.values != null) addAll((List<ExerciseType>) results.values);
                    notifyDataSetChanged();
                }

                @Override
                public CharSequence convertResultToString(Object resultValue) {
                    return ((ExerciseType) resultValue).label();
                }
            };
        }
    }

    // ---------- 타이머 ----------
    // 시작 시각을 저장해 두므로 앱을 나가도 시간이 흐른다. 화면은 1초마다 다시 그리기만 한다.

    private int elapsedSeconds(long nowMs) {
        return (int) Math.min(GameRepository.MAX_WORKOUT_SECONDS, repo.getTimerElapsedMs(nowMs) / 1000);
    }

    private void onTimerMain() {
        long now = System.currentTimeMillis();
        if (repo.getTimerType() == null) {
            repo.startTimer(selectedType, now);
        } else if (repo.isTimerRunning()) {
            repo.pauseTimer(now);
        } else {
            repo.resumeTimer(now);
        }
        updateTimer();
    }

    /** 타이머가 켜져 있으면 그 종목으로 고정하고 종목 고르기를 막는다 */
    private void updateTimer() {
        if (timerText == null) return;
        ExerciseType active = repo.getTimerType();
        boolean on = active != null;
        boolean running = repo.isTimerRunning();
        if (on && active != selectedType) selectType(active);

        int seconds = elapsedSeconds(System.currentTimeMillis());
        timerText.setText(Fmt.clock(seconds));
        timerState.setText(!on ? R.string.timer_state_idle
                : running ? R.string.timer_state_running : R.string.timer_state_paused);
        timerMain.setText(!on ? R.string.timer_start : running ? R.string.timer_pause : R.string.timer_resume);
        timerActions.setVisibility(on ? View.VISIBLE : View.GONE);
        categorySpinner.setEnabled(!on);
        search.setEnabled(!on);

        if (on) {
            RewardCalc.Reward r = repo.previewWorkout(selectedType, seconds, LocalDate.now());
            preview.setText(getString(R.string.timer_preview, Fmt.decimal(selectedType.met),
                    Fmt.duration(seconds), Fmt.num(r.score), Fmt.num(r.leaves), Fmt.num(r.exp)));
        } else {
            preview.setText(getString(R.string.timer_preview_idle, Fmt.decimal(selectedType.met),
                    Fmt.num((int) Math.round(selectedType.met * 30))));
        }
    }

    private void confirmFinish() {
        long now = System.currentTimeMillis();
        int seconds = elapsedSeconds(now);
        if (seconds < 1) {
            Toast.makeText(requireContext(), R.string.timer_too_short, Toast.LENGTH_SHORT).show();
            return;
        }
        // 확인창을 보는 동안 시간이 더 쌓이지 않도록 먼저 멈춘다
        if (repo.isTimerRunning()) repo.pauseTimer(now);
        updateTimer();
        new AlertDialog.Builder(requireContext())
                .setMessage(getString(R.string.timer_finish_confirm, selectedType.label(), Fmt.duration(seconds)))
                .setNegativeButton(R.string.cancel, null)
                .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface d, int w) {
                        GameRepository.AddResult result = repo.finishTimer(System.currentTimeMillis(),
                                LocalDateTime.now());
                        if (result != null) onWorkoutAdded(result);
                    }
                })
                .show();
    }

    private void confirmCancel() {
        new AlertDialog.Builder(requireContext())
                .setMessage(R.string.timer_cancel_confirm)
                .setNegativeButton(R.string.cancel, null)
                .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface d, int w) {
                        repo.cancelTimer();
                        updateTimer();
                    }
                })
                .show();
    }

    private void refresh() {
        if (getView() == null) return;
        GameRepository.DaySummary day = repo.getDaySummary(LocalDate.now());

        zone.setText(getString(R.string.exercise_zone, getString(zoneLabel(day.score, day.quota))));
        dayBar.setData(day.score, day.quota);
        scoreValue.setText(getString(R.string.value_score_quota, Fmt.num(day.score), Fmt.num(day.quota)));
        minutesValue.setText(Fmt.time(day.seconds));
        leavesValue.setText(getString(R.string.value_plus, Fmt.num(day.leaves)));
        expValue.setText(getString(R.string.value_plus, Fmt.num(day.exp)));

        todayList.removeAllViews();
        LayoutInflater inflater = LayoutInflater.from(requireContext());
        for (WorkoutRecord r : day.records) {
            View row = inflater.inflate(R.layout.item_workout_row, todayList, false);
            ExerciseType type = ExerciseType.fromCode(r.type);
            String name = type == null ? r.type : type.label();
            // 이름이 길 수 있어 두 줄로: 윗줄 종목, 아랫줄 시간과 보상
            ((TextView) row.findViewById(R.id.row_title)).setText(name);
            ((TextView) row.findViewById(R.id.row_detail)).setText(getString(R.string.workout_row_detail,
                    Fmt.duration(r.seconds), Fmt.num(r.score), Fmt.num(r.leaves), Fmt.num(r.exp)));
            todayList.addView(row);
        }
        todayEmpty.setVisibility(day.records.isEmpty() ? View.VISIBLE : View.GONE);

        updateTimer();
    }

    private int zoneLabel(int score, int quota) {
        switch (RewardCalc.zoneOf(score, quota)) {
            case BONUS: return R.string.zone_bonus;
            case NORMAL: return R.string.zone_normal;
            case OVER: return R.string.zone_over;
            default: return R.string.zone_maxed;
        }
    }

    private void onWorkoutAdded(GameRepository.AddResult result) {
        Toast.makeText(requireContext(), getString(R.string.reward_toast,
                Fmt.num(result.reward.leaves), Fmt.num(result.reward.exp)), Toast.LENGTH_SHORT).show();

        // 망고와 유저가 같이 레벨업하면 팝업 2개: 망고 먼저, 닫으면 유저
        String userMsg = result.userLevelAfter > result.userLevelBefore
                ? getString(R.string.levelup_user, result.userLevelBefore, result.userLevelAfter) : null;
        if (result.mangoLevelAfter > result.mangoLevelBefore) {
            showLevelUp(getString(R.string.levelup_mango, result.mangoLevelBefore, result.mangoLevelAfter),
                    new Runnable() {
                        @Override
                        public void run() {
                            if (userMsg != null) showLevelUp(userMsg, null);
                        }
                    }, result);
        } else if (userMsg != null) {
            showLevelUp(userMsg, null);
        } else {
            repo.startWorkoutGrin();
            refreshWorkoutFace();
            workoutMango.post(() -> workoutMango.requestRectangleOnScreen(
                    new android.graphics.Rect(0, 0, workoutMango.getWidth(), workoutMango.getHeight())));
        }
    }

    private void showLevelUp(String message, @Nullable Runnable onClosed) {
        showLevelUp(message, onClosed, null);
    }

    private void showLevelUp(String message, @Nullable Runnable onClosed,
                             @Nullable GameRepository.AddResult growth) {
        if (!isAdded()) return;
        AlertDialog.Builder dialog = new AlertDialog.Builder(requireContext())
                .setTitle(R.string.levelup_title)
                .setMessage(message)
                .setPositiveButton(R.string.ok, null)
                .setOnDismissListener(new DialogInterface.OnDismissListener() {
                    @Override
                    public void onDismiss(DialogInterface d) {
                        if (onClosed != null) onClosed.run();
                    }
                });
        int level = repo.getMangoProgress().level;
        boolean changesColor = growth != null && MangoStage.forLevel(growth.mangoLevelBefore)
                != MangoStage.forLevel(growth.mangoLevelAfter);
        Equipment equipped = repo.getEquipment();
        FrameLayout panel = new FrameLayout(requireContext());
        MangoView preview = new MangoView(requireContext());
        panel.addView(preview, new FrameLayout.LayoutParams(-1, dp(180)));
        preview.setFace(growth == null ? MangoFace.HAPPY_TEARS : MangoFace.PARTY);
        preview.setAppearance(equipped, changesColor ? growth.mangoLevelBefore : level);
        if (changesColor) {
            preview.postDelayed(() -> {
                if (preview.isShown()) preview.setAppearance(equipped, level);
            }, 250);
        }
        dialog.setView(panel);
        dialog.show();
    }

    /** [임시] 오늘 기록 초기화 */
    private void confirmTempReset() {
        new AlertDialog.Builder(requireContext())
                .setMessage(R.string.reset_confirm)
                .setNegativeButton(R.string.cancel, null)
                .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface d, int w) {
                        repo.tempResetToday(LocalDate.now());
                        Toast.makeText(requireContext(), R.string.reset_done, Toast.LENGTH_SHORT).show();
                    }
                })
                .show();
    }

    /** [임시] 더미 데이터 불러오기 */
    private void confirmTempDummy() {
        new AlertDialog.Builder(requireContext())
                .setMessage(R.string.dummy_confirm)
                .setNegativeButton(R.string.cancel, null)
                .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface d, int w) {
                        boolean ok = TempTools.loadDummyData(requireContext(), LocalDate.now());
                        Toast.makeText(requireContext(), ok ? R.string.dummy_done : R.string.dummy_failed,
                                Toast.LENGTH_SHORT).show();
                    }
                })
                .show();
    }

    private int dp(int value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }
}
