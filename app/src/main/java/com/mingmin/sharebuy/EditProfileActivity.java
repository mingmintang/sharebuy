package com.mingmin.sharebuy;

import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.mingmin.sharebuy.cloud.Fdb;

public class EditProfileActivity extends AppCompatActivity {

    private EditText etNickname;
    private Button btnConfirm;
    private String email;
    private DatabaseReference nicknameRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        User user = (User) getIntent().getSerializableExtra("user");
        email = getIntent().getStringExtra("email");
        nicknameRef = Fdb.getUserNicknameRef(user.getUid());

        btnConfirm = findViewById(R.id.edit_profile_confirm);

        etNickname = findViewById(R.id.edit_profile_nickname);
        initNicknameValue();
        etNickname.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

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
            public void afterTextChanged(Editable s) {

            }
        });
    }

    private void initNicknameValue() {
        final String tempName = email.split("@")[0];
        etNickname.setText(tempName);

        nicknameRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String nickname = (String) dataSnapshot.getValue();
                if (nickname != null) {
                    etNickname.setText(nickname);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    public void confirm(final View view) {
        String nickname = etNickname.getText().toString().trim();
        nicknameRef.setValue(nickname)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        setResult(RESULT_OK);
                        finish();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Snackbar.make(view, "修改失敗", Snackbar.LENGTH_LONG).show();
                    }
                });
    }
}
