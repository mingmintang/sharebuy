package com.mingmin.sharebuy;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import com.google.firebase.database.FirebaseDatabase;

public class GroupInfoActivity extends AppCompatActivity {

    private Group group;
    private FirebaseDatabase fdb;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_info);

        group = (Group) getIntent().getSerializableExtra("group");

        fdb = FirebaseDatabase.getInstance();
        TextView tvSearchCode = findViewById(R.id.group_info_searchCode);
        tvSearchCode.setText(String.valueOf(group.getSearchCode()));

    }

    @Override
    public void onBackPressed() {
        setResult(RESULT_OK);
        super.onBackPressed();
    }
}
