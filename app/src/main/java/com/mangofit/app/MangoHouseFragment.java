package com.mangofit.app;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridLayout;
import android.widget.TextView;

import com.mangofit.app.data.ArchivedMango;
import com.mangofit.app.data.GameRepository;
import com.mangofit.app.logic.Levels;
import com.mangofit.app.ui.Fmt;
import com.mangofit.app.view.MangoView;

import java.util.List;

/** 망고집: 보관된 망고를 보관 순서대로 보여준다. 망고를 누르면 상세 창이 열린다. */
public class MangoHouseFragment extends Fragment {

    private GameRepository repo;
    private GridLayout grid;
    private TextView count;
    private TextView empty;
    private final GameRepository.Listener listener = this::render;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_mango_house, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        repo = GameRepository.get(requireContext());
        grid = view.findViewById(R.id.house_grid);
        count = view.findViewById(R.id.house_count);
        empty = view.findViewById(R.id.house_empty);
    }

    @Override
    public void onStart() {
        super.onStart();
        repo.addListener(listener);
    }

    @Override
    public void onResume() {
        super.onResume();
        render();
    }

    @Override
    public void onStop() {
        super.onStop();
        repo.removeListener(listener);
    }

    private void render() {
        if (getView() == null) return;
        List<ArchivedMango> mangos = repo.getArchivedMangos();
        count.setText(getString(R.string.house_count, mangos.size()));
        empty.setVisibility(mangos.isEmpty() ? View.VISIBLE : View.GONE);

        grid.removeAllViews();
        LayoutInflater inflater = LayoutInflater.from(requireContext());
        int margin = Math.round(6 * getResources().getDisplayMetrics().density);
        for (ArchivedMango m : mangos) {
            View cell = inflater.inflate(R.layout.item_house_mango, grid, false);
            ((MangoView) cell.findViewById(R.id.house_mango)).setEquipment(m.equipped);
            ((TextView) cell.findViewById(R.id.house_no)).setText(getString(R.string.house_no, m.no));
            ((TextView) cell.findViewById(R.id.house_level))
                    .setText(getString(R.string.house_level, Levels.levelForExp(m.getExp())));
            ((TextView) cell.findViewById(R.id.house_period))
                    .setText(getString(R.string.house_period, Fmt.shortDate(m.startDate), Fmt.shortDate(m.endDate)));
            cell.setOnClickListener(v -> MangoDetailDialog.newInstance(m.no)
                    .show(getChildFragmentManager(), "mango_detail"));

            GridLayout.LayoutParams lp = new GridLayout.LayoutParams(
                    GridLayout.spec(GridLayout.UNDEFINED),
                    GridLayout.spec(GridLayout.UNDEFINED, 1f));
            lp.width = 0;
            lp.setMargins(margin, margin, margin, margin);
            grid.addView(cell, lp);
        }
    }
}
