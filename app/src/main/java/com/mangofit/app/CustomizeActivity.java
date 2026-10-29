package com.mangofit.app;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.mangofit.app.data.Equipment;
import com.mangofit.app.data.GameRepository;
import com.mangofit.app.logic.Item;
import com.mangofit.app.logic.Slot;
import com.mangofit.app.ui.ItemArt;
import com.mangofit.app.ui.SystemBars;
import com.mangofit.app.view.MangoView;

import java.util.List;

/**
 * 꾸미기 화면. 아이템을 누르면 위 망고에 미리보기로 보이고, "적용"을 눌러야 장착이 저장된다.
 * 닫기를 누르면 저장하지 않는다.
 */
public class CustomizeActivity extends AppCompatActivity {

    private static final String STATE_HAT = "hat";
    private static final String STATE_TOP = "top";
    private static final String STATE_HAND = "hand";
    private static final String STATE_SLOT = "slot";

    private GameRepository repo;
    private MangoView preview;
    private GridLayout grid;
    private TextView empty;
    private TextView[] tabs;
    private Equipment draft;
    private Slot slot = Slot.HAT;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customize);
        SystemBars.setLightStatusBar(this, true);
        SystemBars.padTop(findViewById(R.id.customize_scene));
        SystemBars.padBottom(findViewById(R.id.customize_sheet));

        repo = GameRepository.get(this);
        if (savedInstanceState != null) {
            draft = new Equipment(savedInstanceState.getString(STATE_HAT),
                    savedInstanceState.getString(STATE_TOP),
                    savedInstanceState.getString(STATE_HAND));
            slot = Slot.valueOf(savedInstanceState.getString(STATE_SLOT, Slot.HAT.name()));
        } else {
            draft = repo.getEquipment();
        }

        preview = findViewById(R.id.scene_mango);
        grid = findViewById(R.id.customize_grid);
        empty = findViewById(R.id.customize_empty);
        tabs = new TextView[]{findViewById(R.id.tab_hat), findViewById(R.id.tab_top), findViewById(R.id.tab_hand)};
        Slot[] slots = Slot.values();
        for (int i = 0; i < tabs.length; i++) {
            Slot s = slots[i];
            tabs[i].setOnClickListener(v -> {
                slot = s;
                render();
            });
        }

        findViewById(R.id.customize_close).setOnClickListener(v -> finish());
        findViewById(R.id.customize_apply).setOnClickListener(v -> {
            repo.setEquipment(draft);
            finish();
        });

        fitMangoToScene();
        render();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(STATE_HAT, draft.hat);
        outState.putString(STATE_TOP, draft.top);
        outState.putString(STATE_HAND, draft.hand);
        outState.putString(STATE_SLOT, slot.name());
    }

    /** 화면이 작아 미리보기 칸(위 35%)이 낮으면 망고를 줄여서 제목과 겹치지 않게 한다 */
    private void fitMangoToScene() {
        View scene = findViewById(R.id.customize_scene);
        scene.post(() -> {
            float density = getResources().getDisplayMetrics().density;
            float available = scene.getHeight() - scene.getPaddingTop() - 70 * density - 64 * density;
            float scale = Math.min(1f, available / preview.getHeight());
            if (scale <= 0) return;
            preview.setPivotX(preview.getWidth() / 2f);
            preview.setPivotY(preview.getHeight());
            preview.setScaleX(scale);
            preview.setScaleY(scale);
        });
    }

    private void render() {
        preview.setEquipment(draft);

        Slot[] slots = Slot.values();
        for (int i = 0; i < tabs.length; i++) tabs[i].setSelected(slots[i] == slot);

        grid.removeAllViews();
        addCell(null);
        List<Item> owned = repo.getOwnedItems(slot);
        for (Item item : owned) addCell(item);
        // 마지막 줄이 덜 차면 칸이 옆으로 늘어나므로 빈칸으로 채운다
        int columns = grid.getColumnCount();
        while (grid.getChildCount() % columns != 0) {
            View spacer = new View(this);
            grid.addView(spacer, cellParams());
        }
        if (owned.isEmpty()) {
            empty.setText(getString(R.string.customize_empty, getString(slot.labelRes)));
            empty.setVisibility(View.VISIBLE);
        } else {
            empty.setVisibility(View.GONE);
        }
    }

    /** item이 null이면 "해제" 칸 */
    private void addCell(Item item) {
        View cell = LayoutInflater.from(this).inflate(R.layout.item_customize, grid, false);
        ImageView art = cell.findViewById(R.id.cell_art);
        TextView name = cell.findViewById(R.id.cell_name);
        String current = draft.get(slot);
        if (item == null) {
            art.setVisibility(View.GONE);
            cell.findViewById(R.id.cell_none).setVisibility(View.VISIBLE);
            name.setText(R.string.customize_none);
            cell.setSelected(current == null);
        } else {
            ItemArt.apply(art, item);
            name.setText(ItemArt.displayName(this, item));
            cell.setSelected(item.id.equals(current));
        }
        cell.setOnClickListener(v -> {
            draft = draft.with(slot, item == null ? null : item.id);
            render();
        });

        grid.addView(cell, cellParams());
    }

    private GridLayout.LayoutParams cellParams() {
        int margin = Math.round(5 * getResources().getDisplayMetrics().density);
        GridLayout.LayoutParams lp = new GridLayout.LayoutParams(
                GridLayout.spec(GridLayout.UNDEFINED),
                GridLayout.spec(GridLayout.UNDEFINED, 1f));
        lp.width = 0;
        lp.setMargins(margin, margin, margin, margin);
        return lp;
    }
}
