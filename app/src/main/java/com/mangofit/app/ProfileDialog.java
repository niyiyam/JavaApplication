package com.mangofit.app;

import android.app.Dialog;
import android.content.DialogInterface;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.text.InputFilter;
import android.text.InputType;
import android.view.Gravity;
import android.view.View;
import android.widget.EditText;
import android.widget.GridLayout;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.DialogFragment;

import com.mangofit.app.data.ArchivedMango;
import com.mangofit.app.data.GameRepository;
import com.mangofit.app.logic.MangoStage;
import com.mangofit.app.ui.Fmt;
import com.mangofit.app.view.AvatarView;

import java.util.ArrayList;
import java.util.List;

/**
 * 프로필: 이름, 체중, 프로필 사진. 상단 바의 유저 부분을 누르면 열린다.
 * 프로필 사진은 기본 망고 5색과, 지금 망고·망고집의 망고 얼굴 중에서 고른다.
 * 저장을 눌러야 반영된다.
 */
public class ProfileDialog extends DialogFragment {

    public static final String TAG = "profile";
    private static final String STATE_AVATAR = "avatar";
    private static final int NAME_MAX = 12;
    private static final float WEIGHT_MIN = 20f;
    private static final float WEIGHT_MAX = 300f;

    private GameRepository repo;
    private EditText nameInput;
    private EditText weightInput;
    private String selectedAvatar;
    private final List<View> cells = new ArrayList<>();
    private final List<String> avatars = new ArrayList<>();

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle state) {
        repo = GameRepository.get(requireContext());
        selectedAvatar = state == null ? repo.getAvatar() : state.getString(STATE_AVATAR, "");

        ScrollView scroll = new ScrollView(requireContext());
        LinearLayout content = new LinearLayout(requireContext());
        content.setOrientation(LinearLayout.VERTICAL);
        content.setPadding(dp(20), dp(8), dp(20), dp(8));
        scroll.addView(content);

        content.addView(label(R.string.profile_name));
        nameInput = new EditText(requireContext());
        nameInput.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PERSON_NAME);
        nameInput.setFilters(new InputFilter[]{new InputFilter.LengthFilter(NAME_MAX)});
        nameInput.setSingleLine(true);
        nameInput.setHint(R.string.name_hint);
        if (state == null) nameInput.setText(repo.getUserName());
        content.addView(nameInput);

        content.addView(label(R.string.profile_weight));
        weightInput = new EditText(requireContext());
        weightInput.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
        weightInput.setSingleLine(true);
        weightInput.setHint(R.string.profile_weight_hint);
        float kg = repo.getWeightKg();
        if (state == null && kg > 0) weightInput.setText(Fmt.decimal(Math.round(kg * 10) / 10.0));
        content.addView(weightInput);
        TextView weightHelp = text(13, R.color.text_secondary);
        weightHelp.setText(R.string.profile_weight_help);
        content.addView(weightHelp);

        content.addView(label(R.string.profile_avatar));
        GridLayout grid = new GridLayout(requireContext());
        grid.setColumnCount(4);
        content.addView(grid, new LinearLayout.LayoutParams(-1, -2));

        addOption(grid, "", getString(R.string.avatar_default));
        String[] baseNames = getResources().getStringArray(R.array.avatar_base_names);
        MangoStage[] stages = MangoStage.values();
        for (int i = 0; i < stages.length; i++) {
            addOption(grid, "base:" + stages[i].name(), baseNames[i]);
        }
        addOption(grid, "current", getString(R.string.avatar_current, repo.getMangoNo()));
        List<ArchivedMango> archived = repo.getArchivedMangos();
        for (int i = archived.size() - 1; i >= 0; i--) {
            ArchivedMango m = archived.get(i);
            addOption(grid, "mango:" + m.no, getString(R.string.mango_title, m.no));
        }
        // 지워진 망고를 골라 둔 경우 등 목록에 없으면 기본으로
        if (!avatars.contains(selectedAvatar)) selectedAvatar = "";
        updateSelection();

        final AlertDialog dialog = new AlertDialog.Builder(requireContext())
                .setTitle(R.string.profile_title)
                .setView(scroll)
                .setNegativeButton(R.string.cancel, null)
                .setPositiveButton(R.string.profile_save, null)
                .create();
        // 입력이 잘못되면 창을 닫지 않도록 버튼 동작을 직접 붙인다
        dialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface d) {
                dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (save()) dialog.dismiss();
                    }
                });
            }
        });
        return dialog;
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(STATE_AVATAR, selectedAvatar);
    }

    private boolean save() {
        String name = nameInput.getText().toString().trim();
        if (name.isEmpty()) {
            Toast.makeText(requireContext(), R.string.profile_name_empty, Toast.LENGTH_SHORT).show();
            return false;
        }
        String weightText = weightInput.getText().toString().trim();
        float weight = 0f;
        if (!weightText.isEmpty()) {
            try {
                weight = Float.parseFloat(weightText);
            } catch (NumberFormatException e) {
                weight = -1f;
            }
            if (weight < WEIGHT_MIN || weight > WEIGHT_MAX) {
                Toast.makeText(requireContext(), getString(R.string.profile_weight_invalid,
                        Fmt.decimal(WEIGHT_MIN), Fmt.decimal(WEIGHT_MAX)), Toast.LENGTH_SHORT).show();
                return false;
            }
        }
        if (!name.equals(repo.getUserName())) repo.setUserName(name);
        if (weight != repo.getWeightKg()) repo.setWeightKg(weight);
        if (!selectedAvatar.equals(repo.getAvatar())) repo.setAvatar(selectedAvatar);
        return true;
    }

    private void addOption(GridLayout grid, final String avatar, String name) {
        LinearLayout cell = new LinearLayout(requireContext());
        cell.setOrientation(LinearLayout.VERTICAL);
        cell.setGravity(Gravity.CENTER_HORIZONTAL);
        cell.setPadding(dp(4), dp(6), dp(4), dp(6));
        cell.setContentDescription(name);

        AvatarView avatarView = new AvatarView(requireContext());
        avatarView.bind(repo, avatar);
        avatarView.setImportantForAccessibility(View.IMPORTANT_FOR_ACCESSIBILITY_NO);
        cell.addView(avatarView, new LinearLayout.LayoutParams(dp(54), dp(54)));

        TextView label = text(12, R.color.text_primary);
        label.setGravity(Gravity.CENTER);
        label.setMaxLines(2);
        label.setText(name);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(-1, -2);
        lp.topMargin = dp(4);
        cell.addView(label, lp);

        cell.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectedAvatar = avatar;
                updateSelection();
            }
        });
        GridLayout.LayoutParams glp = new GridLayout.LayoutParams(
                GridLayout.spec(GridLayout.UNDEFINED), GridLayout.spec(GridLayout.UNDEFINED, 1f));
        glp.width = 0;
        glp.setMargins(dp(2), dp(2), dp(2), dp(2));
        grid.addView(cell, glp);
        cells.add(cell);
        avatars.add(avatar);
    }

    private void updateSelection() {
        for (int i = 0; i < cells.size(); i++) {
            boolean on = avatars.get(i).equals(selectedAvatar);
            GradientDrawable bg = new GradientDrawable();
            bg.setCornerRadius(dp(12));
            bg.setColor(ContextCompat.getColor(requireContext(), on ? R.color.thumb_bg : android.R.color.transparent));
            if (on) bg.setStroke(dp(2), ContextCompat.getColor(requireContext(), R.color.green_primary));
            cells.get(i).setBackground(bg);
            cells.get(i).setSelected(on);
        }
    }

    private TextView label(int res) {
        TextView t = text(14, R.color.text_primary);
        t.setTypeface(null, android.graphics.Typeface.BOLD);
        t.setText(res);
        t.setPadding(0, dp(14), 0, dp(2));
        return t;
    }

    private TextView text(int sp, int colorRes) {
        TextView t = new TextView(requireContext());
        t.setTextSize(sp);
        t.setTextColor(ContextCompat.getColor(requireContext(), colorRes));
        return t;
    }

    private int dp(int value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }
}
