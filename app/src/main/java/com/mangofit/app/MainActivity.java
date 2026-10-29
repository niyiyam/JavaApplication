package com.mangofit.app;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;
import com.mangofit.app.data.ArchivedMango;
import com.mangofit.app.data.GameRepository;
import com.mangofit.app.logic.Levels;
import com.mangofit.app.ui.Fmt;
import com.mangofit.app.ui.SystemBars;

import java.time.LocalDate;


public class MainActivity extends AppCompatActivity {

    private BottomNavigationView bottomNav;
    private GameRepository repo;
    private TextView nameText;
    private TextView levelText;
    private ProgressBar expBar;
    private TextView leavesText;
    private final GameRepository.Listener topBarListener = new GameRepository.Listener() {
        @Override
        public void onDataChanged() {
            refreshTopBar();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        repo = GameRepository.get(this);

        // 첫 실행이면 이름 입력부터
        if (repo.needsUserName()) {
            startActivity(new Intent(this, NameInputActivity.class));
            finish();
            return;
        }

        setContentView(R.layout.activity_main);

        SystemBars.setLightStatusBar(this, false);
        SystemBars.padTop(findViewById(R.id.top_bar));
        nameText = findViewById(R.id.top_bar_name);
        levelText = findViewById(R.id.top_bar_level);
        expBar = findViewById(R.id.top_bar_exp);
        leavesText = findViewById(R.id.top_bar_leaves_text);

        bottomNav = findViewById(R.id.bottom_nav);

        // 앱 처음 켰을 때만 홈 화면 표시 (화면 회전 시 중복 생성 방지)
        if (savedInstanceState == null) {
            replaceFragment(new HomeFragment());
            bottomNav.setSelectedItemId(R.id.nav_home);
        }

        bottomNav.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int id = item.getItemId();
                Fragment fragment;

                if (id == R.id.nav_home) {
                    fragment = new HomeFragment();
                } else if (id == R.id.nav_mango_house) {
                    fragment = new MangoHouseFragment();
                } else if (id == R.id.nav_exercise) {
                    fragment = new ExerciseFragment();
                } else if (id == R.id.nav_record) {
                    fragment = new RecordFragment();
                } else if (id == R.id.nav_shop) {
                    fragment = new ShopFragment();
                } else {
                    return false;
                }

                replaceFragment(fragment);
                return true; // true를 반환해야 탭이 선택 상태(초록색)로 바뀜
            }
        });

        // 이미 선택된 탭을 다시 눌렀을 때 화면을 새로 만들지 않도록
        bottomNav.setOnItemReselectedListener(new NavigationBarView.OnItemReselectedListener() {
            @Override
            public void onNavigationItemReselected(@NonNull MenuItem item) {
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (repo != null) repo.addListener(topBarListener);
    }

    @Override
    protected void onResume() {
        super.onResume();
        // 28일 자동 정산과 오늘 할당량 고정은 앱을 열 때마다 확인한다
        ArchivedMango settled = repo.onAppOpen(LocalDate.now());
        if (settled != null) {
            new AlertDialog.Builder(this)
                    .setTitle(R.string.settled_title)
                    .setMessage(getString(R.string.settled_msg, settled.no))
                    .setPositiveButton(R.string.ok, null)
                    .show();
        }
        refreshTopBar();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (repo != null) repo.removeListener(topBarListener);
    }

    private void refreshTopBar() {
        if (nameText == null) return;
        Levels.Progress user = repo.getUserProgress();
        nameText.setText(repo.getUserName());
        levelText.setText(getString(R.string.top_bar_level, user.level,
                Fmt.num(user.current), Fmt.num(user.needed)));
        expBar.setProgress(Fmt.permille(user.current, user.needed));
        leavesText.setText(Fmt.num(repo.getLeaves()));
    }

    private void replaceFragment(Fragment fragment) {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .commit();
    }
}
