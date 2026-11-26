package com.mangofit.app;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import com.mangofit.app.data.ArchivedMango;
import com.mangofit.app.data.GameRepository;

import java.time.LocalDate;

/**
 * 발표 시연용 숨은 메뉴. 상단 바의 이파리 칸을 길게 누르면 열린다.
 * 기다려야 보이는 화면(망고집 보관, 레벨업, 상점 구매)을 바로 보여주기 위한 도구다.
 */
public class DemoToolsDialog extends DialogFragment {

    public static final String TAG = "demo_tools";
    private static final long TEN_MINUTES_MS = 10 * 60 * 1000L;
    private static final int LEAVES_BONUS = 1000;
    private static final int EXP_BONUS = 200;

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle state) {
        final GameRepository repo = GameRepository.get(requireContext());
        final Context app = requireContext().getApplicationContext();
        String[] items = {
                getString(R.string.demo_archive),
                getString(R.string.demo_timer_10),
                getString(R.string.demo_timer_30),
                getString(R.string.demo_leaves, LEAVES_BONUS),
                getString(R.string.demo_exp, EXP_BONUS),
                getString(R.string.demo_reset_today)
        };
        return new AlertDialog.Builder(requireContext())
                .setTitle(R.string.demo_title)
                .setItems(items, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface d, int which) {
                        switch (which) {
                            case 0:
                                confirm(R.string.demo_archive_confirm, new Runnable() {
                                    @Override
                                    public void run() {
                                        ArchivedMango m = repo.demoArchiveNow(LocalDate.now());
                                        toast(app, app.getString(R.string.demo_archive_done, m.no));
                                    }
                                });
                                break;
                            case 1:
                            case 2:
                                long ms = which == 1 ? TEN_MINUTES_MS : 3 * TEN_MINUTES_MS;
                                toast(app, app.getString(repo.demoAddTimerTime(ms)
                                        ? R.string.demo_timer_done : R.string.demo_timer_off));
                                break;
                            case 3:
                                repo.demoAddLeaves(LEAVES_BONUS);
                                toast(app, app.getString(R.string.demo_leaves_done, LEAVES_BONUS));
                                break;
                            case 4:
                                int[] levels = repo.demoAddMangoExp(EXP_BONUS);
                                toast(app, levels[1] > levels[0]
                                        ? app.getString(R.string.demo_exp_levelup, EXP_BONUS, levels[0], levels[1])
                                        : app.getString(R.string.demo_exp_done, EXP_BONUS));
                                break;
                            default:
                                confirm(R.string.demo_reset_confirm, new Runnable() {
                                    @Override
                                    public void run() {
                                        repo.demoResetToday(LocalDate.now());
                                        toast(app, app.getString(R.string.demo_reset_done));
                                    }
                                });
                                break;
                        }
                    }
                })
                .setNegativeButton(R.string.cancel, null)
                .create();
    }

    /** 되돌리기 어려운 동작은 한 번 더 묻는다. 메뉴 창이 닫힌 뒤에도 뜨도록 액티비티에 붙인다 */
    private void confirm(int messageRes, final Runnable action) {
        new AlertDialog.Builder(requireActivity())
                .setTitle(R.string.demo_title)
                .setMessage(messageRes)
                .setNegativeButton(R.string.cancel, null)
                .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface d, int w) {
                        action.run();
                    }
                })
                .show();
    }

    private static void toast(Context context, String text) {
        Toast.makeText(context, text, Toast.LENGTH_SHORT).show();
    }
}
