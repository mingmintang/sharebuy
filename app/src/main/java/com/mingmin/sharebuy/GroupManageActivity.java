package com.mingmin.sharebuy;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.mingmin.sharebuy.cloud.Fdb;
import com.mingmin.sharebuy.cloud.Group;
import com.mingmin.sharebuy.cloud.Member;
import com.mingmin.sharebuy.dialog.ConfirmDialog;

import java.util.ArrayList;

public class GroupManageActivity extends AppCompatActivity implements ConfirmDialog.OnConfirmListener {

    private DatabaseReference membersRef;
    private ValueEventListener joinedValueEventListener;
    private ValueEventListener joiningValueEventListener;
    private RecyclerView recyclerView;
    private Group group;
    private int selectedItemId;
    private BottomNavigationView navigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_manage);

        group = (Group) getIntent().getSerializableExtra("group");
        selectedItemId = getIntent().getIntExtra("selectedItemId", R.id.group_manage_nav_joined);
        membersRef = Fdb.getGroupMembersRef(group.getId());

        recyclerView = findViewById(R.id.group_manage_recyclerView);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        initValueEventListener();

        navigationView = findViewById(R.id.group_manage_navigationView);
        navigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                addValueEventListener(item.getItemId());
                return true;
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        navigationView.setSelectedItemId(selectedItemId);
    }

    @Override
    protected void onStop() {
        super.onStop();
        removeAllValueEventListener();
        selectedItemId = navigationView.getSelectedItemId();
    }

    @Override
    public void onBackPressed() {
        setResult(RESULT_OK);
        super.onBackPressed();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_group_manage_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.group_manage_menu_delete) {
            String tag = "group_delete";
            ConfirmDialog.newInstance(this,
                    "刪除群組(危險操作)",
                    "這將會刪除 " + group.getName() + " 群組，退出所有成員，刪除所有未接單內容",
                    tag,
                    true)
                    .show(getSupportFragmentManager(), tag);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onConfirm(Object tag) {
        if (tag != null) {
            switch ((String) tag) {
                case "group_delete":
                    deleteGroup();
                    break;
            }
        }
    }

    private void deleteGroup() {

    }

    private void initValueEventListener() {
        joinedValueEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                long dataCount = dataSnapshot.getChildrenCount();
                ArrayList<User> members = new ArrayList<>();
                if (dataCount == 0) {
                    recyclerView.setAdapter(new JoinedMemberAdapter(members));
                    return;
                }
                for (DataSnapshot snap : dataSnapshot.getChildren()) {
                    String memberUid = snap.getKey();
                    Member member = snap.getValue(Member.class);
                    if (member != null) {
                        members.add(new User(memberUid, member.getNickname()));
                    } else {
                        dataCount -= 1;
                    }
                    if (members.size() == dataCount) {
                        recyclerView.setAdapter(new JoinedMemberAdapter(members));
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };

        joiningValueEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                long dataCount = dataSnapshot.getChildrenCount();
                ArrayList<User> members = new ArrayList<>();
                if (dataCount == 0) {
                    recyclerView.setAdapter(new JoiningMemberAdapter(members));
                    return;
                }
                for (DataSnapshot snap : dataSnapshot.getChildren()) {
                    String memberUid = snap.getKey();
                    Member member = snap.getValue(Member.class);
                    if (member != null) {
                        members.add(new User(memberUid, member.getNickname()));
                    } else {
                        dataCount -= 1;
                    }
                    if (members.size() == dataCount) {
                        recyclerView.setAdapter(new JoiningMemberAdapter(members));
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };
    }

    private void addValueEventListener(int navItemId) {
        removeAllValueEventListener();
        switch (navItemId) {
            case R.id.group_manage_nav_joined:
                membersRef.orderByChild("isJoined").equalTo(true)
                        .addValueEventListener(joinedValueEventListener);
                break;
            case R.id.group_manage_nav_joining:
                membersRef.orderByChild("isJoined").equalTo(false)
                        .addValueEventListener(joiningValueEventListener);
                break;
        }
    }

    private void removeAllValueEventListener() {
        if (joiningValueEventListener != null) {
            membersRef.removeEventListener(joiningValueEventListener);
        }
        if (joinedValueEventListener != null) {
            membersRef.removeEventListener(joinedValueEventListener);
        }
    }

    class JoinedMemberAdapter extends RecyclerView.Adapter<JoinedMemberAdapter.ViewHolder>
            implements ConfirmDialog.OnConfirmListener {
        private ArrayList<User> members;
        class ViewHolder extends RecyclerView.ViewHolder {
            TextView tvNickname;
            Button btnRemove;
            ViewHolder(View itemView) {
                super(itemView);
                tvNickname = itemView.findViewById(R.id.joined_group_member_nickname);
                btnRemove = itemView.findViewById(R.id.joined_group_member_remove);
            }
        }
        JoinedMemberAdapter(ArrayList<User> members) {
            this.members = members;
        }

        @NonNull
        @Override
        public JoinedMemberAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View itemView = getLayoutInflater()
                    .inflate(R.layout.row_joined_group_member, parent, false);
            return new ViewHolder(itemView);
        }

        @Override
        public void onBindViewHolder(@NonNull JoinedMemberAdapter.ViewHolder holder, int position) {
            final User user = members.get(position);
            holder.tvNickname.setText(user.getNickname());
            holder.btnRemove.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ConfirmDialog.newInstance(JoinedMemberAdapter.this,
                            "退出群組",
                            "確定將 " + user.getNickname() + " 退出群組？",
                            user)
                            .show(getSupportFragmentManager(), "GroupRemove");
                }
            });
            if (user.getUid().equals(group.getFounderUid())) {
                holder.btnRemove.setVisibility(View.INVISIBLE);
            } else {
                holder.btnRemove.setVisibility(View.VISIBLE);
            }
        }

        @Override
        public int getItemCount() {
            return members.size();
        }

        @Override
        public void onConfirm(Object tag) {
            final User user = (User) tag;
            membersRef.child(user.getUid()).removeValue()
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Fdb.getUserGroupRef(user.getUid(), group.getId())
                                    .removeValue()
                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            Snackbar.make(recyclerView, "退出群組成功", Snackbar.LENGTH_LONG).show();
                                        }
                                    });
                        }
                    });
        }
    }

    class JoiningMemberAdapter extends RecyclerView.Adapter<JoiningMemberAdapter.ViewHolder>
            implements ConfirmDialog.OnConfirmListener{
        private ArrayList<User> members;
        class TagData {
            User user;
            int clickedViewId;
        }
        class ViewHolder extends RecyclerView.ViewHolder {
            TextView tvNickname;
            Button btnAccept;
            Button btnDecline;
            ViewHolder(View itemView) {
                super(itemView);
                tvNickname = itemView.findViewById(R.id.joining_group_member_nickname);
                btnAccept = itemView.findViewById(R.id.joining_group_member_accept);
                btnDecline = itemView.findViewById(R.id.joining_group_member_decline);
            }
        }
        JoiningMemberAdapter(ArrayList<User> members) {
            this.members = members;
        }

        @NonNull
        @Override
        public JoiningMemberAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View itemView = getLayoutInflater()
                    .inflate(R.layout.row_joining_group_member, parent, false);
            return new ViewHolder(itemView);
        }

        @Override
        public void onBindViewHolder(@NonNull JoiningMemberAdapter.ViewHolder holder, int position) {
            final User user = members.get(position);
            final TagData tagData = new TagData();
            tagData.user = user;
            holder.tvNickname.setText(user.getNickname());
            holder.btnAccept.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    tagData.clickedViewId = v.getId();
                    ConfirmDialog.newInstance(JoiningMemberAdapter.this,
                            "加入群組",
                            "確定將 " + user.getNickname() + " 加入群組？",
                            tagData)
                            .show(getSupportFragmentManager(), "GroupJoin");
                }
            });
            holder.btnDecline.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    tagData.clickedViewId = v.getId();
                    ConfirmDialog.newInstance(JoiningMemberAdapter.this,
                            "取消加入申請",
                            "取消 " + user.getNickname() + " 加入群組申請？",
                            tagData)
                            .show(getSupportFragmentManager(), "GroupJoinCancel");
                }
            });
        }

        @Override
        public int getItemCount() {
            return members.size();
        }

        @Override
        public void onConfirm(Object tag) {
            final TagData data = (TagData) tag;
            switch (data.clickedViewId) {
                case R.id.joining_group_member_accept:
                    acceptJoinGroup(data.user);
                    break;
                case R.id.joining_group_member_decline:
                    declineJoinGroup(data.user);
                    break;
            }
        }

        private void acceptJoinGroup(final User user) {
            membersRef.child(user.getUid()).child("isJoined").setValue(true)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Fdb.getUserGroupRef(user.getUid(), group.getId())
                                    .setValue(true)
                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            Snackbar.make(recyclerView, "加入群組成功", Snackbar.LENGTH_LONG).show();
                                        }
                                    });
                        }
                    });
        }

        private void declineJoinGroup(final User user) {
            membersRef.child(user.getUid()).removeValue()
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Snackbar.make(recyclerView, "已取消加入申請", Snackbar.LENGTH_LONG).show();
                        }
                    });
        }
    }
}
