package com.mangofit.app;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.mangofit.app.data.GameRepository;
import com.mangofit.app.ui.SystemBars;

import java.time.LocalDate;

/** 첫 실행: 유저 이름 입력. 앞뒤 공백을 뺀 이름이 빈칸이면 시작할 수 없다. */
public class NameInputActivity extends AppCompatActivity {

    private EditText input;
    private TextView confirm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_name_input);
        SystemBars.setLightStatusBar(this, true);
        SystemBars.padTopAndBottom(findViewById(R.id.name_root));

        input = findViewById(R.id.name_input);
        confirm = findViewById(R.id.name_confirm);

        input.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) { }
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) { }

            @Override
            public void afterTextChanged(Editable s) {
                confirm.setEnabled(!trimmedName().isEmpty());
            }
        });
        input.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE && !trimmedName().isEmpty()) {
                start();
                return true;
            }
            return false;
        });
        confirm.setOnClickListener(v -> start());
    }

    private String trimmedName() {
        return input.getText().toString().trim();
    }

    private void start() {
        String name = trimmedName();
        if (name.isEmpty()) return;
        GameRepository.get(this).startNewUser(name, LocalDate.now());
        startActivity(new Intent(this, MainActivity.class));
        finish();
    }
}
