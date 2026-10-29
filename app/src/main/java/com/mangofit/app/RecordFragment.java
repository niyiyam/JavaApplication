package com.mangofit.app;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.mangofit.app.data.GameRepository;

/** 기록 화면: [이번 망고]는 지금 망고(망고 모드), [전체 기록]은 모든 기록의 합(유저 모드). */
public class RecordFragment extends Fragment {

    private static final String STATE_ALL = "show_all";

    private View tabCurrent;
    private View tabAll;
    private boolean showAll;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_record, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (savedInstanceState != null) showAll = savedInstanceState.getBoolean(STATE_ALL);
        tabCurrent = view.findViewById(R.id.record_tab_current);
        tabAll = view.findViewById(R.id.record_tab_all);
        tabCurrent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                show(false);
            }
        });
        tabAll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                show(true);
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        // 그사이 망고가 보관됐을 수 있으니 지금 망고 번호로 다시 붙인다
        show(showAll);
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(STATE_ALL, showAll);
    }

    private void show(boolean all) {
        showAll = all;
        tabCurrent.setSelected(!all);
        tabAll.setSelected(all);
        int mangoNo = GameRepository.get(requireContext()).getMangoNo();
        Fragment block = all ? DetailBlockFragment.forUser() : DetailBlockFragment.forMango(mangoNo);
        getChildFragmentManager()
                .beginTransaction()
                .replace(R.id.record_container, block)
                .commit();
    }
}
