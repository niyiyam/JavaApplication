package com.mangofit.app;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.mangofit.app.data.ArchivedMango;
import com.mangofit.app.data.GameRepository;
import com.mangofit.app.logic.Levels;
import com.mangofit.app.logic.MangoCycle;
import com.mangofit.app.logic.RewardCalc;
import com.mangofit.app.ui.Fmt;
import com.mangofit.app.view.DayProgressBar;
import com.mangofit.app.view.MangoView;

import java.time.LocalDate;


public class HomeFragment extends Fragment {

    private GameRepository repo;
    private MangoView mango;
    private TextView title;
    private TextView days;
    private TextView level;
    private TextView exp;
    private ProgressBar expBar;
    private TextView todayScore;
    private DayProgressBar dayBar;
    private TextView todayLeaves;
    private TextView todayExp;
    private final GameRepository.Listener listener = this::refresh;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        repo = GameRepository.get(requireContext());

        View scene = view.findViewById(R.id.home_scene);
        scene.setClipToOutline(true);
        mango = view.findViewById(R.id.scene_mango);
        mango.setOnClickListener(v -> startActivity(new Intent(requireContext(), CustomizeActivity.class)));

        title = view.findViewById(R.id.home_mango_title);
        days = view.findViewById(R.id.home_mango_days);
        level = view.findViewById(R.id.home_mango_level);
        exp = view.findViewById(R.id.home_mango_exp);
        expBar = view.findViewById(R.id.home_mango_exp_bar);
        todayScore = view.findViewById(R.id.home_today_score);
        dayBar = view.findViewById(R.id.day_progress_bar);
        todayLeaves = view.findViewById(R.id.home_today_leaves);
        todayExp = view.findViewById(R.id.home_today_exp);

        view.findViewById(R.id.home_temp_archive).setOnClickListener(v -> confirmTempArchive());
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
        if (getView() == null) return;
        LocalDate today = LocalDate.now();

        mango.setEquipment(repo.getEquipment());

        Levels.Progress p = repo.getMangoProgress();
        title.setText(getString(R.string.mango_title, repo.getMangoNo()));
        days.setText(getString(R.string.mango_days, repo.getMangoDay(today), MangoCycle.DAYS));
        level.setText(getString(R.string.mango_level, p.level));
        exp.setText(getString(R.string.exp_progress, Fmt.num(p.current), Fmt.num(p.needed)));
        expBar.setProgress(Fmt.permille(p.current, p.needed));

        GameRepository.DaySummary day = repo.getDaySummary(today);
        int max = RewardCalc.dailyMax(day.quota);
        todayScore.setText(getString(R.string.today_score, Fmt.num(day.score), Fmt.num(day.quota)));
        dayBar.setData(day.score, day.quota);
        todayLeaves.setText(getString(R.string.today_leaves, Fmt.num(day.leaves), Fmt.num(max)));
        todayExp.setText(getString(R.string.today_exp, Fmt.num(day.exp), Fmt.num(max)));
    }

    /** [임시] 지금 망고를 오늘 날짜로 보관하고 새 망고를 시작한다 */
    private void confirmTempArchive() {
        new AlertDialog.Builder(requireContext())
                .setTitle(R.string.archive_confirm_title)
                .setMessage(getString(R.string.archive_confirm_msg, repo.getMangoNo()))
                .setNegativeButton(R.string.cancel, null)
                .setPositiveButton(R.string.ok, (d, w) -> {
                    ArchivedMango archived = repo.tempArchiveNow(LocalDate.now());
                    Toast.makeText(requireContext(), getString(R.string.archive_done, archived.no),
                            Toast.LENGTH_SHORT).show();
                })
                .show();
    }
}
