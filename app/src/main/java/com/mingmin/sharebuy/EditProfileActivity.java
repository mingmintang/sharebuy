package com.mingmin.sharebuy;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

public class EditProfileActivity extends AppCompatActivity {

    private EditText etNickname;
    private Button btnConfirm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        User user = (User) getIntent().getSerializableExtra("user");
        btnConfirm = findViewById(R.id.edit_profile_confirm);
        etNickname = findViewById(R.id.edit_profile_nickname);
        etNickname.setText(user.getNickname());
        etNickname.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String nickname = s.toString().trim();
                if (!nickname.equals("")) {
                    btnConfirm.setEnabled(true);
                } else {
                    btnConfirm.setEnabled(false);
                }
            }

            @Override
            public void afterTextChanged(Editable s) { }
        });
    }

    public void confirm(final View view) {
        String nickname = etNickname.getText().toString().trim();
        Intent intent = new Intent();
        intent.putExtra("nickname", nickname);
        setResult(RESULT_OK, intent);
        finish();
    }
}
