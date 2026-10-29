package com.mangofit.app;

import android.content.res.ColorStateList;
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
 * 본문은 기간 동안 한 운동의 종류별 비율 도넛 차트(운동 시간 기준).
 */
public class DetailBlockFragment extends Fragment {

    private static final String ARG_MODE = "mode";
    private static final String ARG_MANGO_NO = "mango_no";
    private static final String MODE_MANGO = "mango";
    private static final String MODE_USER = "user";

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
        Integer donutMangoNo;
        if (MODE_USER.equals(args.getString(ARG_MODE))) {
            bindUser(view);
            donutMangoNo = null;
        } else {
            int no = args.getInt(ARG_MANGO_NO);
            bindMango(view, no);
            donutMangoNo = no;
        }
        bindDonut(view, repo.getMinutesByType(donutMangoNo));
    }

    private void bindMango(View view, int no) {
        LocalDate today = LocalDate.now();
        MangoView mango = view.findViewById(R.id.detail_mango);
        String period;
        int exp;
        int leaves;
        if (no == repo.getMangoNo()) {
            LocalDate start = repo.getMangoStartDate();
            mango.setEquipment(repo.getEquipment());
            period = getString(R.string.detail_period_current, Fmt.dotDate(start),
                    MangoCycle.dayNumber(start, today), MangoCycle.DAYS);
            exp = repo.getMangoExp();
            leaves = repo.getMangoLeavesEarned();
        } else {
            ArchivedMango m = repo.getArchivedMango(no);
            if (m == null) return;
            mango.setEquipment(m.equipped);
            period = getString(R.string.detail_period_done, Fmt.dotDate(m.startDate), Fmt.dotDate(m.endDate),
                    MangoCycle.lengthDays(m.startDate, m.endDate));
            exp = m.getExp();
            leaves = m.getLeavesEarned();
        }
        bindHeader(view, getString(R.string.mango_title, no), period, Levels.progressFor(exp), leaves);
    }

    private void bindUser(View view) {
        view.findViewById(R.id.detail_mango).setVisibility(View.GONE);
        view.findViewById(R.id.detail_avatar).setVisibility(View.VISIBLE);
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

    private void bindDonut(View view, Map<ExerciseType, Integer> minutesByType) {
        // 많이 한 운동부터
        List<Map.Entry<ExerciseType, Integer>> entries = new ArrayList<>(minutesByType.entrySet());
        entries.sort(new Comparator<Map.Entry<ExerciseType, Integer>>() {
            @Override
            public int compare(Map.Entry<ExerciseType, Integer> a, Map.Entry<ExerciseType, Integer> b) {
                return b.getValue() - a.getValue();
            }
        });
        int total = 0;
        for (Map.Entry<ExerciseType, Integer> e : entries) total += e.getValue();

        List<DonutChartView.Slice> slices = new ArrayList<>();
        LinearLayout legend = view.findViewById(R.id.detail_legend);
        legend.removeAllViews();
        LayoutInflater inflater = LayoutInflater.from(requireContext());
        for (Map.Entry<ExerciseType, Integer> e : entries) {
            ExerciseType type = e.getKey();
            int minutes = e.getValue();
            slices.add(new DonutChartView.Slice(minutes, type.color));

            View row = inflater.inflate(R.layout.item_legend_row, legend, false);
            row.findViewById(R.id.legend_dot).setBackgroundTintList(ColorStateList.valueOf(type.color));
            ((TextView) row.findViewById(R.id.legend_name)).setText(type.labelRes);
            int percent = (int) Math.round(minutes * 100.0 / total);
            ((TextView) row.findViewById(R.id.legend_value))
                    .setText(getString(R.string.donut_legend, Fmt.num(minutes), percent));
            legend.addView(row);
        }

        DonutChartView donut = view.findViewById(R.id.detail_donut);
        donut.setData(slices, getString(R.string.donut_center), getString(R.string.value_minutes, Fmt.num(total)));
        view.findViewById(R.id.detail_donut_empty).setVisibility(total == 0 ? View.VISIBLE : View.GONE);
    }
}
