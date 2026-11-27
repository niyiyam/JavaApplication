package com.mangofit.app;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.animation.ValueAnimator;
import android.graphics.PointF;
import android.widget.FrameLayout;
import android.widget.ImageView;
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
import com.mangofit.app.ui.MangoFace;

import java.time.LocalDate;


public class HomeFragment extends Fragment {

    private GameRepository repo;
    private MangoView mango;
    private long lastInteraction;
    private final Runnable updateFace = this::refreshFace;

    void onUserInteraction() {
        lastInteraction = android.os.SystemClock.elapsedRealtime();
        if (isResumed()) refreshFace();
    }

    private void refreshFace() {
        mango.removeCallbacks(updateFace);
        long love = repo.loveRemaining();
        long grin = repo.workoutGrinRemaining();
        long idle = android.os.SystemClock.elapsedRealtime() - lastInteraction;
        if (love > 0) {
            mango.setFace(MangoFace.LOVE);
            mango.postDelayed(updateFace, love);
        } else if (grin > 0) {
            mango.setFace(MangoFace.GRIN);
            mango.postDelayed(updateFace, grin);
        } else if (idle < 120_000) {
            mango.setFace(MangoFace.SMILE);
            mango.postDelayed(updateFace, 120_000 - idle);
        } else if (idle < 122_000) {
            mango.setFace(MangoFace.YAWN);
            mango.postDelayed(updateFace, 122_000 - idle);
        } else {
            mango.setFace(MangoFace.NEUTRAL);
        }
    }
    private void reactWithLove() {
        repo.startLove();
        onUserInteraction();
    }

    private void popHearts(FrameLayout scene) {
        PointF leaf = mango.leafAnchor();
        float density = getResources().getDisplayMetrics().density;
        int size = Math.round(22 * density);
        for (int i = 0; i < 3; i++) {
            ImageView heart = new ImageView(requireContext());
            heart.setImageResource(R.drawable.ic_mango_heart);
            heart.setImportantForAccessibility(View.IMPORTANT_FOR_ACCESSIBILITY_NO);
            FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(size, size);
            params.leftMargin = Math.round(mango.getX() + leaf.x - size / 2f);
            params.topMargin = Math.round(mango.getY() + leaf.y - size / 2f);
            scene.addView(heart, params);
            heart.setAlpha(0f);
            float drift = (i - 1) * 16 * density;
            ValueAnimator pop = ValueAnimator.ofFloat(0f, 1f);
            pop.setStartDelay(i * 95L);
            pop.setDuration(560);
            pop.setInterpolator(new android.view.animation.LinearInterpolator());
            pop.addUpdateListener(animation -> {
                float t = (float) animation.getAnimatedValue();
                heart.setTranslationX(drift * t);
                heart.setTranslationY(-52 * density * (2 * t - t * t));
                heart.setScaleX(Math.min(1f, 0.35f + t * 5f));
                heart.setScaleY(heart.getScaleX());
                heart.setAlpha(Math.min(t * 12f, Math.min(1f, (1f - t) * 3f)));
            });
            pop.addListener(new android.animation.AnimatorListenerAdapter() {
                @Override public void onAnimationEnd(android.animation.Animator animation) {
                    scene.removeView(heart);
                }
            });
            pop.start();
        }
    }

    private TextView title;
    private TextView days;
    private TextView level;
    private TextView exp;
    private ProgressBar expBar;
    private TextView todayScore;
    private DayProgressBar dayBar;
    private TextView todayLeaves;
    private TextView todayExp;
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
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        repo = GameRepository.get(requireContext());

        FrameLayout scene = view.findViewById(R.id.home_scene);
        scene.setClipToOutline(true);
        mango = view.findViewById(R.id.scene_mango);
        mango.setOnClickListener(v -> reactWithLove());
        view.findViewById(R.id.home_heart).setOnClickListener(v -> {
            reactWithLove();
            popHearts(scene);
        });
        view.findViewById(R.id.home_customize).setOnClickListener(v ->
                startActivity(new Intent(requireContext(), CustomizeActivity.class)));

        title = view.findViewById(R.id.home_mango_title);
        days = view.findViewById(R.id.home_mango_days);
        level = view.findViewById(R.id.home_mango_level);
        exp = view.findViewById(R.id.home_mango_exp);
        expBar = view.findViewById(R.id.home_mango_exp_bar);
        todayScore = view.findViewById(R.id.home_today_score);
        dayBar = view.findViewById(R.id.day_progress_bar);
        todayLeaves = view.findViewById(R.id.home_today_leaves);
        todayExp = view.findViewById(R.id.home_today_exp);

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
        onUserInteraction();
    }

    @Override
    public void onPause() {
        mango.removeCallbacks(updateFace);
        super.onPause();
    }

    @Override
    public void onStop() {
        super.onStop();
        repo.removeListener(listener);
    }

    private void refresh() {
        if (getView() == null) return;
        LocalDate today = LocalDate.now();

        Levels.Progress p = repo.getMangoProgress();
        mango.setAppearance(repo.getEquipment(), p.level);
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

}
