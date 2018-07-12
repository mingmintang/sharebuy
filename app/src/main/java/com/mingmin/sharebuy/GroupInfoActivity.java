package com.mingmin.sharebuy;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.mingmin.sharebuy.cloud.Clouds;

import java.util.ArrayList;

public class GroupInfoActivity extends AppCompatActivity {
    private final String TAG = getClass().getSimpleName();
    private Group group;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_info);

        group = (Group) getIntent().getSerializableExtra("group");

        TextView tvSearchCode = findViewById(R.id.group_info_searchCode);
        tvSearchCode.setText(String.valueOf(group.getSearchCode()));

        RecyclerView recyclerView = findViewById(R.id.group_info_recyclerView);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        setupRecyclerView(recyclerView);
    }

    private void setupRecyclerView(final RecyclerView recyclerView) {
        Clouds.getInstance().getJoinedGroupMembers(group.getId())
                .addOnSuccessListener(new OnSuccessListener<ArrayList<Member>>() {
                    @Override
                    public void onSuccess(ArrayList<Member> members) {
                        recyclerView.setAdapter(new MembersAdapter(members));
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d(TAG, "onFailure: " + e.getMessage());
                    }
                });
    }

    @Override
    public void onBackPressed() {
        setResult(RESULT_OK);
        super.onBackPressed();
    }

    class MembersAdapter extends RecyclerView.Adapter<MembersAdapter.ViewHolder> {
        private ArrayList<Member> members;

        class ViewHolder extends RecyclerView.ViewHolder {
            TextView tvNickname;
            ViewHolder(View itemView) {
                super(itemView);
                tvNickname = itemView.findViewById(R.id.group_info_nickname);
            }
        }

        MembersAdapter(ArrayList<Member> members) {
            this.members = members;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View itemView = getLayoutInflater().inflate(R.layout.row_group_member_info, parent, false);
            return new ViewHolder(itemView);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            holder.tvNickname.setText(members.get(position).getNickname());
        }

        @Override
        public int getItemCount() {
            return members.size();
        }
    }
}
