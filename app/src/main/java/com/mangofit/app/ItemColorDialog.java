package com.mangofit.app;

import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import com.mangofit.app.data.Equipment;
import com.mangofit.app.logic.Item;
import com.mangofit.app.logic.ItemCatalog;
import com.mangofit.app.logic.ItemPalette;
import com.mangofit.app.ui.ItemArt;
import com.mangofit.app.view.MangoView;
import java.util.ArrayList;
import java.util.List;

/** 취소 전까지는 미리보기만 변경한다. 선택 결과는 Fragment 결과 API로 전달한다. */
public class ItemColorDialog extends DialogFragment {
    public static final String RESULT = "item_color";
    private Item selected;
    private ImageView art;
    private MangoView mango;
    private TextView colorName;
    private final List<View> swatches = new ArrayList<>();
    private final List<Item> options = new ArrayList<>();

    public static ItemColorDialog create(Item item) {
        ItemColorDialog dialog = new ItemColorDialog();
        Bundle args = new Bundle();
        args.putString("item", item.id);
        dialog.setArguments(args);
        return dialog;
    }

    @NonNull @Override public Dialog onCreateDialog(Bundle state) {
        String id = state == null ? requireArguments().getString("item") : state.getString("selected");
        selected = ItemCatalog.get(id);
        Item base = ItemCatalog.get(selected.baseId);
        options.clear(); swatches.clear();
        options.add(base);
        for (ItemPalette.ColorOption color : ItemPalette.colors(base)) {
            options.add(ItemCatalog.get(base.id + "__" + color.code));
        }
        ScrollView scroll = new ScrollView(requireContext());
        LinearLayout content = new LinearLayout(requireContext());
        content.setOrientation(LinearLayout.VERTICAL);
        content.setPadding(dp(20), dp(8), dp(20), dp(8));
        scroll.addView(content);
        if (base.fullBody) {
            mango = new MangoView(requireContext());
            content.addView(mango, new LinearLayout.LayoutParams(-1, dp(190)));
        } else {
            art = new ImageView(requireContext());
            art.setScaleType(ImageView.ScaleType.FIT_CENTER);
            art.setContentDescription(getString(base.label));
            content.addView(art, new LinearLayout.LayoutParams(-1, dp(170)));
        }
        colorName = text(16);
        colorName.setTypeface(null, android.graphics.Typeface.BOLD);
        content.addView(colorName);
        TextView hint = text(13);
        hint.setText(R.string.tint_hint);
        hint.setPadding(0, dp(8), 0, dp(12));
        content.addView(hint);
        GridLayout colors = new GridLayout(requireContext());
        colors.setColumnCount(3);
        content.addView(colors);
        for (Item option : options) {
            LinearLayout swatch = new LinearLayout(requireContext());
            swatch.setOrientation(LinearLayout.VERTICAL);
            swatch.setGravity(Gravity.CENTER);
            swatch.setPadding(dp(4), dp(8), dp(4), dp(8));
            View dot = new View(requireContext());
            GradientDrawable circle = new GradientDrawable();
            circle.setShape(GradientDrawable.OVAL);
            circle.setColor(option.tint == null ? 0xFFF5EAD7 : option.tint.argb);
            circle.setStroke(dp(1), 0xFFBAAA92);
            dot.setBackground(circle);
            swatch.addView(dot, new LinearLayout.LayoutParams(dp(30), dp(30)));
            TextView label = text(11);
            label.setText(option.tint == null ? R.string.tint_original : option.tint.label);
            label.setPadding(0, dp(5), 0, 0);
            swatch.addView(label, new LinearLayout.LayoutParams(-1, -2));
            swatch.setContentDescription(label.getText());
            swatch.setFocusable(true);
            swatch.setOnClickListener(v -> { selected = option; refresh(); });
            GridLayout.LayoutParams lp = new GridLayout.LayoutParams(
                    GridLayout.spec(GridLayout.UNDEFINED), GridLayout.spec(GridLayout.UNDEFINED, 1f));
            lp.width = 0; lp.height = dp(84);
            lp.setMargins(dp(3), dp(3), dp(3), dp(3));
            colors.addView(swatch, lp);
            swatches.add(swatch);
        }
        // 불완전한 행에서도 같은 열 너비를 유지한다.
        while (colors.getChildCount() % 3 != 0) {
            GridLayout.LayoutParams lp = new GridLayout.LayoutParams(
                    GridLayout.spec(GridLayout.UNDEFINED), GridLayout.spec(GridLayout.UNDEFINED, 1f));
            lp.width = 0;
            colors.addView(new View(requireContext()), lp);
        }
        refresh();
        return new AlertDialog.Builder(requireContext())
                .setTitle(R.string.tint_title)
                .setView(scroll)
                .setNegativeButton(R.string.cancel, null)
                .setPositiveButton(R.string.tint_apply, (dialog, which) -> {
                    Bundle result = new Bundle();
                    result.putString("item", selected.id);
                    getParentFragmentManager().setFragmentResult(RESULT, result);
                }).create();
    }

    private void refresh() {
        if (mango != null) mango.setEquipment(Equipment.empty().with(selected.slot, selected.id));
        else ItemArt.apply(art, selected);
        colorName.setText(selected.tint == null ? R.string.tint_original : selected.tint.label);
        for (int i = 0; i < swatches.size(); i++) {
            boolean active = options.get(i).id.equals(selected.id);
            GradientDrawable background = new GradientDrawable();
            background.setCornerRadius(dp(12));
            background.setColor(active ? 0xFFEDF3E8 : Color.TRANSPARENT);
            background.setStroke(dp(active ? 2 : 1), active ? 0xFF426B49 : 0xFFE9E1D3);
            swatches.get(i).setBackground(background);
            swatches.get(i).setSelected(active);
            String name = getString(options.get(i).tint == null ? R.string.tint_original : options.get(i).tint.label);
            swatches.get(i).setContentDescription(active ? getString(R.string.tint_selected, name) : name);
        }
    }

    private TextView text(int size) {
        TextView view = new TextView(requireContext());
        view.setTextColor(0xFF4E5147);
        view.setTextSize(size);
        view.setGravity(Gravity.CENTER);
        return view;
    }

    @Override public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString("selected", selected.id);
    }

    private int dp(int value) { return Math.round(value * getResources().getDisplayMetrics().density); }
}
