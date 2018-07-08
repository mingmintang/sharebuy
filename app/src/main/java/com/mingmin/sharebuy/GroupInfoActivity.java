package com.mingmin.sharebuy;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.mingmin.sharebuy.cloud.Fdb;
import com.mingmin.sharebuy.cloud.Group;
import com.mingmin.sharebuy.cloud.Member;

import java.util.ArrayList;

public class GroupInfoActivity extends AppCompatActivity {

    private DatabaseReference membersRef;
    private ValueEventListener membersValueEventListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_info);

        Group group = (Group) getIntent().getSerializableExtra("group");
        membersRef = Fdb.getInstance().getGroupMembersRef(group.getId());

        TextView tvSearchCode = findViewById(R.id.group_info_searchCode);
        tvSearchCode.setText(String.valueOf(group.getSearchCode()));

        RecyclerView recyclerView = findViewById(R.id.group_info_recyclerView);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        setupRecyclerView(recyclerView);
    }

    private void setupRecyclerView(final RecyclerView recyclerView) {
        membersValueEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                final ArrayList<Member> members = new ArrayList<>();
                final long childCount = dataSnapshot.getChildrenCount();
                if (childCount == 0) {
                    recyclerView.setAdapter(new MembersAdapter(members));
                    return;
                }
                for (DataSnapshot childSnap : dataSnapshot.getChildren()) {
                    Member member = dataSnapshot.getValue(Member.class);
                    members.add(member);
                    if (members.size() == childCount) {
                        recyclerView.setAdapter(new MembersAdapter(members));
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };
    }

    @Override
    protected void onStart() {
        super.onStart();
        membersRef.orderByChild("isJoined").equalTo(true)
                .addValueEventListener(membersValueEventListener);
    }

    @Override
    protected void onStop() {
        super.onStop();
        membersRef.removeEventListener(membersValueEventListener);
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
