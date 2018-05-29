package com.mingmin.sharebuy;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

public class GroupManageActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_manage);

//        Spinner spinner = findViewById(R.id.group_manage_spinner);
//        spinner.setAdapter(new ArrayAdapter<String>(this,
//                android.R.layout.simple_list_item_1,
//                new String[]{"已加入成員", "未加入成員"}));
    }
}
