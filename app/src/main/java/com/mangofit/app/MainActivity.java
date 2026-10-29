package com.mangofit.app;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;


public class MainActivity extends AppCompatActivity {

    private BottomNavigationView bottomNav;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        bottomNav = findViewById(R.id.bottom_nav);

        // 앱 처음 켰을 때만 홈 화면 표시 (화면 회전 시 중복 생성 방지)
        if (savedInstanceState == null) {
            replaceFragment(new HomeFragment());
            bottomNav.setSelectedItemId(R.id.nav_home);
        }

        bottomNav.setOnItemSelectedListener(item -> {
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
        });

        // 이미 선택된 탭을 다시 눌렀을 때 화면을 새로 만들지 않도록
        bottomNav.setOnItemReselectedListener(item -> { });
    }

    private void replaceFragment(Fragment fragment) {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .commit();
    }
}