package com.mangofit.app;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.mangofit.app.data.GameRepository;
import com.mangofit.app.logic.Item;
import com.mangofit.app.logic.ItemCatalog;
import com.mangofit.app.logic.Slot;
import com.mangofit.app.ui.Fmt;
import com.mangofit.app.ui.ItemArt;

/**
 * 상점: 부위별 탭 → 아이템 그리드(그림 + 가격).
 * 보유한 아이템은 "보유 중", 이파리가 모자라면 살 수 없다. 장착은 꾸미기 화면에서만 한다.
 */
public class ShopFragment extends Fragment {

    private GameRepository repo;
    private GridLayout grid;
    private TextView[] tabs;
    private Slot slot = Slot.HAT;
    private final GameRepository.Listener listener = this::render;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_shop, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        repo = GameRepository.get(requireContext());
        grid = view.findViewById(R.id.shop_grid);
        tabs = new TextView[]{
                view.findViewById(R.id.tab_hat),
                view.findViewById(R.id.tab_top),
                view.findViewById(R.id.tab_hand)};
        Slot[] slots = Slot.values();
        for (int i = 0; i < tabs.length; i++) {
            Slot s = slots[i];
            tabs[i].setOnClickListener(v -> {
                slot = s;
                render();
            });
        }
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
        Slot[] slots = Slot.values();
        for (int i = 0; i < tabs.length; i++) tabs[i].setSelected(slots[i] == slot);

        grid.removeAllViews();
        LayoutInflater inflater = LayoutInflater.from(requireContext());
        int margin = dp(6);
        int leaves = repo.getLeaves();
        for (Item item : ItemCatalog.bySlot(slot)) {
            View card = inflater.inflate(R.layout.item_shop, grid, false);
            ItemArt.apply(card.findViewById(R.id.shop_item_art), item);
            ((TextView) card.findViewById(R.id.shop_item_name)).setText(ItemArt.displayName(requireContext(), item));

            View button = card.findViewById(R.id.shop_item_button);
            View leaf = card.findViewById(R.id.shop_item_leaf);
            TextView price = card.findViewById(R.id.shop_item_price);
            if (repo.isOwned(item.id)) {
                button.setBackgroundResource(R.drawable.bg_btn_muted);
                leaf.setVisibility(View.GONE);
                price.setText(R.string.shop_owned);
                price.setTextColor(ContextCompat.getColor(requireContext(), R.color.text_secondary));
                button.setOnClickListener(null);
                button.setClickable(false);
            } else {
                price.setText(Fmt.num(item.price));
                if (leaves < item.price) {
                    button.setBackgroundResource(R.drawable.bg_btn_muted);
                    price.setTextColor(ContextCompat.getColor(requireContext(), R.color.text_secondary));
                }
                button.setOnClickListener(v -> confirmBuy(item));
            }

            GridLayout.LayoutParams lp = new GridLayout.LayoutParams(
                    GridLayout.spec(GridLayout.UNDEFINED),
                    GridLayout.spec(GridLayout.UNDEFINED, 1f));
            lp.width = 0;
            lp.setMargins(margin, margin, margin, margin);
            grid.addView(card, lp);
        }
    }

    private void confirmBuy(Item item) {
        if (repo.getLeaves() < item.price) {
            Toast.makeText(requireContext(), R.string.buy_not_enough, Toast.LENGTH_SHORT).show();
            return;
        }
        String name = ItemArt.displayName(requireContext(), item);
        new AlertDialog.Builder(requireContext())
                .setMessage(getString(R.string.buy_confirm, name, Fmt.num(item.price)))
                .setNegativeButton(R.string.cancel, null)
                .setPositiveButton(R.string.ok, (d, w) -> {
                    GameRepository.BuyResult result = repo.buy(item.id);
                    if (result == GameRepository.BuyResult.OK) {
                        Toast.makeText(requireContext(), R.string.buy_done, Toast.LENGTH_SHORT).show();
                    } else if (result == GameRepository.BuyResult.NOT_ENOUGH_LEAVES) {
                        Toast.makeText(requireContext(), R.string.buy_not_enough, Toast.LENGTH_SHORT).show();
                    }
                })
                .show();
    }

    private int dp(int value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }
}
