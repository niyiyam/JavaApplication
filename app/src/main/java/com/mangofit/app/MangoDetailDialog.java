package com.mangofit.app;

import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

/** 망고집 상세 창. 화면을 거의 전체 덮고, 닫기 버튼으로 닫는다. */
public class MangoDetailDialog extends DialogFragment {

    private static final String ARG_MANGO_NO = "mango_no";

    public static MangoDetailDialog newInstance(int mangoNo) {
        Bundle args = new Bundle();
        args.putInt(ARG_MANGO_NO, mangoNo);
        MangoDetailDialog dialog = new MangoDetailDialog();
        dialog.setArguments(args);
        return dialog;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.dialog_mango_detail, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        view.findViewById(R.id.detail_close).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });
        if (savedInstanceState == null) {
            getChildFragmentManager()
                    .beginTransaction()
                    .replace(R.id.detail_container,
                            DetailBlockFragment.forMango(requireArguments().getInt(ARG_MANGO_NO)))
                    .commit();
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        if (getDialog() == null) return;
        Window window = getDialog().getWindow();
        if (window == null) return;
        // 가장자리만 살짝 남기고 화면을 덮는다
        window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        int marginX = Math.round(12 * getResources().getDisplayMetrics().density);
        int marginY = Math.round(24 * getResources().getDisplayMetrics().density);
        int width = getResources().getDisplayMetrics().widthPixels - marginX * 2;
        int height = getResources().getDisplayMetrics().heightPixels - marginY * 2;
        window.setLayout(width, height);
    }
}
