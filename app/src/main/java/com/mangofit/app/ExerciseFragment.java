package com.mangofit.app;

import android.content.DialogInterface;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.GridLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
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

    private static final int MIN_MINUTES = 1;
    private static final int MAX_MINUTES = 600;
    private static final int STEP_MINUTES = 5;

    private GameRepository repo;
    private TextView zone;
    private DayProgressBar dayBar;
    private TextView scoreValue;
    private TextView minutesValue;
    private TextView leavesValue;
    private TextView expValue;
    private LinearLayout todayList;
    private TextView todayEmpty;
    private final List<TextView> typeButtons = new ArrayList<>();
    private EditText minutesInput;
    private TextView preview;
    private TextView addButton;
    private ExerciseType selectedType = ExerciseType.WALK;
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

        zone = view.findViewById(R.id.exercise_zone);
        dayBar = view.findViewById(R.id.day_progress_bar);
        scoreValue = setupTile(view, R.id.tile_score, R.string.tile_score);
        minutesValue = setupTile(view, R.id.tile_minutes, R.string.tile_minutes);
        leavesValue = setupTile(view, R.id.tile_leaves, R.string.tile_leaves);
        expValue = setupTile(view, R.id.tile_exp, R.string.tile_exp);
        todayList = view.findViewById(R.id.exercise_today_list);
        todayEmpty = view.findViewById(R.id.exercise_today_empty);

        setupTypeGrid(view.findViewById(R.id.exercise_type_grid));

        minutesInput = view.findViewById(R.id.exercise_minutes);
        minutesInput.setText("30");
        minutesInput.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) { }
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) { }

            @Override
            public void afterTextChanged(Editable s) {
                updatePreview();
            }
        });
        view.findViewById(R.id.exercise_minus).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stepMinutes(-STEP_MINUTES);
            }
        });
        view.findViewById(R.id.exercise_plus).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stepMinutes(STEP_MINUTES);
            }
        });

        preview = view.findViewById(R.id.exercise_preview);
        addButton = view.findViewById(R.id.exercise_add);
        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                confirmAdd();
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
        refresh();
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

    private void setupTypeGrid(GridLayout grid) {
        int margin = dp(4);
        for (ExerciseType type : ExerciseType.values()) {
            TextView button = new TextView(requireContext());
            button.setText(type.labelRes);
            button.setGravity(Gravity.CENTER);
            button.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
            button.setTypeface(button.getTypeface(), android.graphics.Typeface.BOLD);
            button.setTextColor(ContextCompat.getColorStateList(requireContext(), R.color.chip_text));
            button.setBackgroundResource(R.drawable.bg_chip);
            button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    selectType(type);
                }
            });

            GridLayout.LayoutParams lp = new GridLayout.LayoutParams(
                    GridLayout.spec(GridLayout.UNDEFINED),
                    GridLayout.spec(GridLayout.UNDEFINED, 1f));
            lp.width = 0;
            lp.height = dp(52);
            lp.setMargins(margin, margin, margin, margin);
            grid.addView(button, lp);
            typeButtons.add(button);
        }
        selectType(selectedType);
    }

    private void selectType(ExerciseType type) {
        selectedType = type;
        ExerciseType[] types = ExerciseType.values();
        for (int i = 0; i < types.length; i++) {
            typeButtons.get(i).setSelected(types[i] == type);
        }
        updatePreview();
    }

    /** 입력값이 1~600분이 아니면 -1 */
    private int readMinutes() {
        try {
            int minutes = Integer.parseInt(minutesInput.getText().toString().trim());
            return (minutes < MIN_MINUTES || minutes > MAX_MINUTES) ? -1 : minutes;
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    private void stepMinutes(int delta) {
        int current = readMinutes();
        if (current < 0) current = 30;
        int next = Math.max(MIN_MINUTES, Math.min(MAX_MINUTES, current + delta));
        minutesInput.setText(String.valueOf(next));
        minutesInput.setSelection(minutesInput.length());
    }

    private void updatePreview() {
        if (preview == null) return;
        int minutes = readMinutes();
        if (minutes < 0) {
            preview.setText(R.string.add_minutes_invalid);
            addButton.setEnabled(false);
            return;
        }
        RewardCalc.Reward r = repo.previewWorkout(selectedType, minutes, LocalDate.now());
        preview.setText(getString(R.string.add_preview, Fmt.decimal(selectedType.met), minutes,
                Fmt.num(r.score), Fmt.num(r.leaves), Fmt.num(r.exp)));
        addButton.setEnabled(true);
    }

    private void refresh() {
        if (getView() == null) return;
        GameRepository.DaySummary day = repo.getDaySummary(LocalDate.now());

        zone.setText(getString(R.string.exercise_zone, getString(zoneLabel(day.score, day.quota))));
        dayBar.setData(day.score, day.quota);
        scoreValue.setText(getString(R.string.value_score_quota, Fmt.num(day.score), Fmt.num(day.quota)));
        minutesValue.setText(getString(R.string.value_minutes, Fmt.num(day.minutes)));
        leavesValue.setText(getString(R.string.value_plus, Fmt.num(day.leaves)));
        expValue.setText(getString(R.string.value_plus, Fmt.num(day.exp)));

        todayList.removeAllViews();
        LayoutInflater inflater = LayoutInflater.from(requireContext());
        for (WorkoutRecord r : day.records) {
            View row = inflater.inflate(R.layout.item_workout_row, todayList, false);
            ExerciseType type = ExerciseType.fromCode(r.type);
            String name = type == null ? r.type : getString(type.labelRes);
            ((TextView) row.findViewById(R.id.row_title))
                    .setText(getString(R.string.workout_row_title, name, r.minutes));
            ((TextView) row.findViewById(R.id.row_detail)).setText(getString(R.string.workout_row_detail,
                    Fmt.num(r.score), Fmt.num(r.leaves), Fmt.num(r.exp)));
            todayList.addView(row);
        }
        todayEmpty.setVisibility(day.records.isEmpty() ? View.VISIBLE : View.GONE);

        updatePreview();
    }

    private int zoneLabel(int score, int quota) {
        switch (RewardCalc.zoneOf(score, quota)) {
            case BONUS: return R.string.zone_bonus;
            case NORMAL: return R.string.zone_normal;
            case OVER: return R.string.zone_over;
            default: return R.string.zone_maxed;
        }
    }

    private void confirmAdd() {
        int minutes = readMinutes();
        if (minutes < 0) {
            Toast.makeText(requireContext(), R.string.add_minutes_invalid, Toast.LENGTH_SHORT).show();
            return;
        }
        ExerciseType type = selectedType;
        new AlertDialog.Builder(requireContext())
                .setMessage(getString(R.string.add_confirm, getString(type.labelRes), minutes))
                .setNegativeButton(R.string.cancel, null)
                .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface d, int w) {
                        addWorkout(type, minutes);
                    }
                })
                .show();
    }

    private void addWorkout(ExerciseType type, int minutes) {
        GameRepository.AddResult result = repo.addWorkout(type, minutes, LocalDateTime.now());
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
                    });
        } else if (userMsg != null) {
            showLevelUp(userMsg, null);
        }
    }

    private void showLevelUp(String message, @Nullable Runnable onClosed) {
        if (!isAdded()) return;
        new AlertDialog.Builder(requireContext())
                .setTitle(R.string.levelup_title)
                .setMessage(message)
                .setPositiveButton(R.string.ok, null)
                .setOnDismissListener(new DialogInterface.OnDismissListener() {
                    @Override
                    public void onDismiss(DialogInterface d) {
                        if (onClosed != null) onClosed.run();
                    }
                })
                .show();
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
