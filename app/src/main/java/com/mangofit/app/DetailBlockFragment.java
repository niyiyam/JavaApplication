package com.mangofit.app;

import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.mangofit.app.data.ArchivedMango;
import com.mangofit.app.data.GameRepository;
import com.mangofit.app.logic.ExerciseType;
import com.mangofit.app.logic.Levels;
import com.mangofit.app.logic.MangoCycle;
import com.mangofit.app.ui.Fmt;
import com.mangofit.app.view.AvatarView;
import com.mangofit.app.view.DonutChartView;
import com.mangofit.app.view.MangoView;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

/**
 * 공용 상세 블록. 망고집 상세 창과 기록 화면에서 같은 Fragment를 쓴다.
 * - 망고 모드: 망고 얼굴(꾸민 모습), 키운 기간, 레벨 + 경험치, 그 기간에 모은 이파리
 * - 유저 모드: 유저 아이콘, 이름, 레벨, 경험치, 첫날 ~ 오늘 기간
 * 본문은 기간 동안 한 운동의 종류별 비율 도넛 차트(운동 시간 기준)와 추가 통계.
 */
public class DetailBlockFragment extends Fragment {

    private static final String ARG_MODE = "mode";
    private static final String ARG_MANGO_NO = "mango_no";
    private static final String MODE_MANGO = "mango";
    private static final String MODE_USER = "user";
    /** 도넛에 따로 보여줄 종목 수. 나머지는 '기타'로 묶는다 */
    private static final int DONUT_TOP = 5;

    private GameRepository repo;
    private final GameRepository.Listener listener = new GameRepository.Listener() {
        @Override
        public void onDataChanged() {
            refresh();
        }
    };

    /** 망고 모드. 지금 망고 번호를 주면 진행 중인 망고로 보여준다. */
    public static DetailBlockFragment forMango(int mangoNo) {
        Bundle args = new Bundle();
        args.putString(ARG_MODE, MODE_MANGO);
        args.putInt(ARG_MANGO_NO, mangoNo);
        DetailBlockFragment f = new DetailBlockFragment();
        f.setArguments(args);
        return f;
    }

    /** 유저 모드: 지금까지 모든 기록의 합 */
    public static DetailBlockFragment forUser() {
        Bundle args = new Bundle();
        args.putString(ARG_MODE, MODE_USER);
        DetailBlockFragment f = new DetailBlockFragment();
        f.setArguments(args);
        return f;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_detail_block, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        repo = GameRepository.get(requireContext());
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

    private void refresh() {
        View view = getView();
        if (view == null) return;
        Bundle args = requireArguments();
        Integer mangoNo;
        if (MODE_USER.equals(args.getString(ARG_MODE))) {
            bindUser(view);
            mangoNo = null;
        } else {
            int no = args.getInt(ARG_MANGO_NO);
            bindMango(view, no);
            mangoNo = no;
        }
        bindDonut(view, repo.getSecondsByType(mangoNo));
        bindStats(view, repo.getPeriodStats(mangoNo, LocalDate.now()), mangoNo == null);
    }

    private void bindMango(View view, int no) {
        LocalDate today = LocalDate.now();
        MangoView mango = view.findViewById(R.id.detail_mango);
        String period;
        int exp;
        int leaves;
        if (no == repo.getMangoNo()) {
            LocalDate start = repo.getMangoStartDate();
            mango.setAppearance(repo.getEquipment(), repo.getMangoProgress().level);
            period = getString(R.string.detail_period_current, Fmt.dotDate(start),
                    MangoCycle.dayNumber(start, today), MangoCycle.DAYS);
            exp = repo.getMangoExp();
            leaves = repo.getMangoLeavesEarned();
        } else {
            ArchivedMango m = repo.getArchivedMango(no);
            if (m == null) return;
            mango.setAppearance(m.equipped, Levels.levelForExp(m.getExp()));
            period = getString(R.string.detail_period_done, Fmt.dotDate(m.startDate), Fmt.dotDate(m.endDate),
                    MangoCycle.lengthDays(m.startDate, m.endDate));
            exp = m.getExp();
            leaves = m.getLeavesEarned();
        }
        bindHeader(view, getString(R.string.mango_title, no), period, Levels.progressFor(exp), leaves);
    }

    private void bindUser(View view) {
        view.findViewById(R.id.detail_mango).setVisibility(View.GONE);
        AvatarView avatar = view.findViewById(R.id.detail_avatar);
        avatar.setVisibility(View.VISIBLE);
        avatar.bind(repo);
        LocalDate first = repo.getFirstStartDate();
        String period = getString(R.string.detail_period_user, Fmt.dotDate(first),
                MangoCycle.dayNumber(first, LocalDate.now()));
        bindHeader(view, repo.getUserName(), period, repo.getUserProgress(), repo.getTotalLeavesEarned());
    }

    private void bindHeader(View view, String title, String period, Levels.Progress p, int leaves) {
        ((TextView) view.findViewById(R.id.detail_title)).setText(title);
        ((TextView) view.findViewById(R.id.detail_period)).setText(period);
        ((TextView) view.findViewById(R.id.detail_level)).setText(getString(R.string.detail_level, p.level));
        ((TextView) view.findViewById(R.id.detail_exp))
                .setText(getString(R.string.detail_exp, Fmt.num(p.current), Fmt.num(p.needed)));
        ((ProgressBar) view.findViewById(R.id.detail_exp_bar)).setProgress(Fmt.permille(p.current, p.needed));
        ((TextView) view.findViewById(R.id.detail_leaves)).setText(Fmt.num(leaves));
    }

    private void bindDonut(View view, Map<ExerciseType, Integer> secondsByType) {
        // 많이 한 운동부터
        List<Map.Entry<ExerciseType, Integer>> entries = new ArrayList<>(secondsByType.entrySet());
        entries.sort(new Comparator<Map.Entry<ExerciseType, Integer>>() {
            @Override
            public int compare(Map.Entry<ExerciseType, Integer> a, Map.Entry<ExerciseType, Integer> b) {
                return b.getValue() - a.getValue();
            }
        });
        int total = 0;
        for (Map.Entry<ExerciseType, Integer> e : entries) total += e.getValue();

        // 종목이 많으므로 색은 종목마다 정하지 않고 순위 순서로 칠한다. 6위부터는 '기타' 한 조각
        TypedArray palette = getResources().obtainTypedArray(R.array.donut_palette);
        List<String> names = new ArrayList<>();
        List<Integer> values = new ArrayList<>();
        int other = 0;
        for (int i = 0; i < entries.size(); i++) {
            if (i < DONUT_TOP || entries.size() == DONUT_TOP + 1) {
                names.add(entries.get(i).getKey().label());
                values.add(entries.get(i).getValue());
            } else {
                other += entries.get(i).getValue();
            }
        }
        if (other > 0) {
            names.add(getString(R.string.donut_other));
            values.add(other);
        }

        List<DonutChartView.Slice> slices = new ArrayList<>();
        LinearLayout legend = view.findViewById(R.id.detail_legend);
        legend.removeAllViews();
        LayoutInflater inflater = LayoutInflater.from(requireContext());
        for (int i = 0; i < names.size(); i++) {
            boolean isOther = other > 0 && i == names.size() - 1;
            int color = isOther ? requireContext().getColor(R.color.chart_other)
                    : palette.getColor(i % palette.length(), 0);
            int seconds = values.get(i);
            slices.add(new DonutChartView.Slice(seconds, color));

            View row = inflater.inflate(R.layout.item_legend_row, legend, false);
            row.findViewById(R.id.legend_dot).setBackgroundTintList(ColorStateList.valueOf(color));
            ((TextView) row.findViewById(R.id.legend_name)).setText(names.get(i));
            int percent = (int) Math.round(seconds * 100.0 / total);
            ((TextView) row.findViewById(R.id.legend_value))
                    .setText(getString(R.string.donut_legend, Fmt.time(seconds), percent));
            legend.addView(row);
        }
        palette.recycle();

        DonutChartView donut = view.findViewById(R.id.detail_donut);
        donut.setData(slices, getString(R.string.donut_center), Fmt.time(total));
        view.findViewById(R.id.detail_donut_empty).setVisibility(total == 0 ? View.VISIBLE : View.GONE);
    }

    private void bindStats(View view, GameRepository.PeriodStats s, boolean allTime) {
        ((TextView) view.findViewById(R.id.detail_stats_title))
                .setText(allTime ? R.string.stats_title_all : R.string.stats_title_mango);
        LinearLayout list = view.findViewById(R.id.detail_stats);
        list.removeAllViews();

        addStat(list, R.string.stat_total_time, Fmt.time(s.seconds), null);
        addStat(list, R.string.stat_total_score, getString(R.string.stat_value_score, Fmt.num(s.score)), null);

        // 소모 칼로리(kcal) = 체중(kg) × MET × 시간(h). 체중을 모르면 공식 그대로 보여준다
        float weight = repo.getWeightKg();
        String metHours = Fmt.decimal(Math.round(s.metHours * 100) / 100.0);
        if (weight > 0) {
            addStat(list, R.string.stat_calories, getString(R.string.stat_value_kcal,
                    Fmt.num((int) Math.round(weight * s.metHours))),
                    getString(R.string.stat_calories_note, Fmt.decimal(Math.round(weight * 10) / 10.0), metHours));
        } else {
            addStat(list, R.string.stat_calories, getString(R.string.stat_value_kcal_formula, metHours),
                    getString(R.string.stat_calories_no_weight));
        }

        addStat(list, R.string.stat_count, getString(R.string.stat_value_count, Fmt.num(s.count)), null);
        int avg = s.seconds / Math.max(1, s.periodDays);
        addStat(list, R.string.stat_daily_avg, Fmt.time(avg),
                getString(R.string.stat_daily_avg_note, s.periodDays));
        addStat(list, R.string.stat_active_days,
                getString(R.string.stat_value_days_of, s.activeDays, s.periodDays), null);
        addStat(list, R.string.stat_streak, getString(R.string.stat_value_days, s.longestStreak), null);
        addStat(list, R.string.stat_goal_days, getString(R.string.stat_value_days, s.goalDays), null);
        addStat(list, R.string.stat_over_days, getString(R.string.stat_value_days, s.overDays), null);
        addStat(list, R.string.stat_top_type, s.topType == null ? getString(R.string.stat_none) : s.topType.label(), null);
        if (allTime) {
            addStat(list, R.string.stat_mango_count, getString(R.string.stat_value_mangos, s.mangoCount), null);
            addStat(list, R.string.stat_best_mango, getString(R.string.stat_value_best_mango,
                    getString(R.string.mango_title, s.bestMangoNo), s.bestMangoLevel), null);
        }
    }

    private void addStat(LinearLayout list, int labelRes, String value, @Nullable String note) {
        View row = LayoutInflater.from(requireContext()).inflate(R.layout.item_stat_row, list, false);
        ((TextView) row.findViewById(R.id.stat_row_label)).setText(labelRes);
        ((TextView) row.findViewById(R.id.stat_row_value)).setText(value);
        TextView noteView = row.findViewById(R.id.stat_row_note);
        noteView.setVisibility(note == null ? View.GONE : View.VISIBLE);
        if (note != null) noteView.setText(note);
        list.addView(row);
    }
}
