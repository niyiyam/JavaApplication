package com.mangofit.app;

import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.GridLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentResultListener;

import com.mangofit.app.data.Equipment;
import com.mangofit.app.data.GameRepository;
import com.mangofit.app.logic.Item;
import com.mangofit.app.logic.ItemCatalog;
import com.mangofit.app.logic.ItemPalette;
import com.mangofit.app.logic.Slot;
import com.mangofit.app.ui.ItemArt;
import com.mangofit.app.ui.SystemBars;
import com.mangofit.app.view.MangoView;
import com.mangofit.app.ui.MangoFace;

import java.util.List;

/**
 * 꾸미기 화면. 아이템을 누르면 위 망고에 미리보기로 보이고, "적용"을 눌러야 장착이 저장된다.
 * 닫기를 누르면 저장하지 않는다.
 * 손 도구는 칸 오른쪽 위 색 버튼으로 색을 바꿀 수 있다 (무료, 바로 저장).
 */
public class CustomizeActivity extends AppCompatActivity {

    private static final String STATE_HAT = "hat";
    private static final String STATE_TOP = "top";
    private static final String STATE_HAND = "hand";
    private static final String STATE_SLOT = "slot";
    private static final String TAG_PALETTE = "item_palette";

    private GameRepository repo;
    private MangoView preview;
    private GridLayout grid;
    private TextView empty;
    private TextView[] tabs;
    private Equipment draft;
    private Slot slot = Slot.TOP;

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
                    savedInstanceState.getString(STATE_HAND), savedInstanceState.getString("left_hand"));
            slot = Slot.valueOf(savedInstanceState.getString(STATE_SLOT, Slot.TOP.name()));
        } else {
            draft = repo.getEquipment();
        }

        if (slot == Slot.HAT) slot = Slot.TOP;
        preview = findViewById(R.id.scene_mango);
        grid = findViewById(R.id.customize_grid);
        empty = findViewById(R.id.customize_empty);
        tabs = new TextView[]{findViewById(R.id.tab_top), findViewById(R.id.tab_hand), findViewById(R.id.tab_left_hand)};
        Slot[] slots = Slot.visibleSlots();
        for (int i = 0; i < tabs.length; i++) {
            Slot s = slots[i];
            tabs[i].setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    slot = s;
                    render();
                }
            });
        }

        findViewById(R.id.customize_close).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        findViewById(R.id.customize_apply).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                repo.setEquipment(draft);
                repo.startLove();
                finish();
            }
        });

        // 색 고르기 창에서 고른 색. 보유 아이템을 그 색으로 바꾸고, 미리보기에 끼고 있던 것이면 같이 바꾼다
        getSupportFragmentManager().setFragmentResultListener(ItemColorDialog.RESULT, this,
                new FragmentResultListener() {
                    @Override
                    public void onFragmentResult(@NonNull String key, @NonNull Bundle result) {
                        Item selected = ItemCatalog.get(result.getString("item"));
                        if (selected == null) return;
                        Item before = repo.getOwnedVariant(selected.id);
                        if (before == null || !repo.changeItemColor(selected.id)) return;
                        if (before.id.equals(draft.get(selected.slot))) {
                            draft = draft.with(selected.slot, selected.id);
                        }
                        render();
                    }
                });

        fitMangoToScene();
        render();
        preview.setFace(MangoFace.LOVE);
        preview.postDelayed(new Runnable() {
            @Override
            public void run() {
                preview.setFace(MangoFace.SMILE);
            }
        }, GameRepository.LOVE_DURATION_MS);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(STATE_HAT, draft.hat);
        outState.putString(STATE_TOP, draft.top);
        outState.putString(STATE_HAND, draft.hand);
        outState.putString("left_hand", draft.leftHand);
        outState.putString(STATE_SLOT, slot.name());
    }

    /** 화면이 작아 미리보기 칸(위 35%)이 낮으면 망고를 줄여서 제목과 겹치지 않게 한다 */
    private void fitMangoToScene() {
        View scene = findViewById(R.id.customize_scene);
        scene.post(new Runnable() {
            @Override
            public void run() {
                float density = getResources().getDisplayMetrics().density;
                float available = scene.getHeight() - scene.getPaddingTop() - 70 * density - 64 * density;
                float scale = Math.min(1f, available / preview.getHeight());
                if (scale <= 0) return;
                // 줄여도 발이 그루터기에 붙어 있도록 발 위치를 기준으로 줄인다
                // (망고 그림은 가운데 정사각형, 발은 그 정사각형 위에서 92% 지점)
                float side = Math.min(preview.getWidth(), preview.getHeight());
                preview.setPivotX(preview.getWidth() / 2f);
                preview.setPivotY(preview.getHeight() / 2f + side * 0.42f);
                preview.setScaleX(scale);
                preview.setScaleY(scale);
            }
        });
    }

    private void render() {
        preview.setAppearance(draft, repo.getMangoProgress().level);

        Slot[] slots = Slot.visibleSlots();
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
            if (item.fullBody) {
                art.setVisibility(View.GONE);
                MangoView mangoArt = cell.findViewById(R.id.cell_mango_art);
                mangoArt.setVisibility(View.VISIBLE);
                mangoArt.setAppearance(Equipment.empty().with(item.slot, item.id), repo.getMangoProgress().level);
            }
            name.setText(ItemArt.displayName(this, item));
            cell.setSelected(item.id.equals(current));
            bindColorButton(cell.findViewById(R.id.cell_color), item);
        }
        cell.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                draft = draft.with(slot, item == null ? null : item.id);
                render();
            }
        });

        grid.addView(cell, cellParams());
    }

    /** 손 도구이고 고를 색이 있으면 지금 색 견본 버튼을 보여준다 */
    private void bindColorButton(ImageButton button, Item item) {
        boolean hand = item.slot == Slot.HAND || item.slot == Slot.LEFT_HAND;
        if (!hand || ItemPalette.colors(ItemCatalog.get(item.baseId)).isEmpty()) {
            button.setVisibility(View.GONE);
            return;
        }
        float density = getResources().getDisplayMetrics().density;
        GradientDrawable swatch = new GradientDrawable();
        swatch.setShape(GradientDrawable.OVAL);
        swatch.setColor(item.tint == null ? 0xFFFFFCF4 : item.tint.argb);
        swatch.setStroke(Math.round(1.5f * density), 0xFF9DB08F);
        button.setBackground(swatch);
        button.setContentDescription(getString(R.string.tint_choose, getString(item.label)));
        button.setVisibility(View.VISIBLE);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (getSupportFragmentManager().findFragmentByTag(TAG_PALETTE) == null) {
                    ItemColorDialog.create(item).show(getSupportFragmentManager(), TAG_PALETTE);
                }
            }
        });
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
