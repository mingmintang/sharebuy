package com.mingmin.sharebuy;

import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class EditProfileActivity extends AppCompatActivity {

    private FirebaseUser fuser;
    private EditText etNickname;
    private Button btnConfirm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        fuser = FirebaseAuth.getInstance().getCurrentUser();

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
                Log.d("wwwww", "onTextChanged: " + nickname);
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
    }

    private void initNicknameValue() {
        final String tempName = fuser.getEmail().split("@")[0];
        etNickname.setText(tempName);

        final DatabaseReference ref = FirebaseDatabase.getInstance()
                .getReference("users")
                .child(fuser.getUid())
                .child("data")
                .child("nickname");
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String nickname = (String) dataSnapshot.getValue();
                if (nickname != null) {
                    etNickname.setText(nickname);
                }
                ref.removeEventListener(this);
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
                ref.removeEventListener(this);
            }
        });
    }

    public void confirm(final View view) {
        String nickname = etNickname.getText().toString().trim();
        FirebaseDatabase.getInstance()
                .getReference("users")
                .child(fuser.getUid())
                .child("data")
                .child("nickname")
                .setValue(nickname)
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
