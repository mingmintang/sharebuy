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

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.mingmin.sharebuy.cloud.Clouds;
import com.mingmin.sharebuy.dialog.ConfirmDialog;
import com.mingmin.sharebuy.item.Group;
import com.mingmin.sharebuy.item.Member;

import java.util.ArrayList;

public class GroupManageActivity extends AppCompatActivity implements ConfirmDialog.ConfirmListener,
        Clouds.GroupMembersListener {
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

        recyclerView = findViewById(R.id.group_manage_recyclerView);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        navigationView = findViewById(R.id.group_manage_navigationView);
        navigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                addGroupMembersListener(item.getItemId());
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
        removeAllGroupMembersListener();
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
    public void onJoiningGroupMembersChanged(ArrayList<Member> members) {
        recyclerView.setAdapter(new JoiningMemberAdapter(members));
    }

    @Override
    public void onJoinedGroupMembersChanged(ArrayList<Member> members) {
        recyclerView.setAdapter(new JoinedMemberAdapter(members));
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

    private void addGroupMembersListener(int navItemId) {
        removeAllGroupMembersListener();
        switch (navItemId) {
            case R.id.group_manage_nav_joined:
                Clouds.getInstance().addJoinedGroupMembersListener(group.getId(), this);
                break;
            case R.id.group_manage_nav_joining:
                Clouds.getInstance().addJoiningGroupMembersListener(group.getId(), this);
                break;
        }
    }

    private void removeAllGroupMembersListener() {
        Clouds.getInstance().removeJoinedGroupMembersListener();
        Clouds.getInstance().removeJoiningGroupMembersListener();
    }

    class JoinedMemberAdapter extends RecyclerView.Adapter<JoinedMemberAdapter.ViewHolder>
            implements ConfirmDialog.ConfirmListener {
        private ArrayList<Member> members;
        class ViewHolder extends RecyclerView.ViewHolder {
            TextView tvMemberName;
            Button btnRemove;
            ViewHolder(View itemView) {
                super(itemView);
                tvMemberName = itemView.findViewById(R.id.joined_group_member_name);
                btnRemove = itemView.findViewById(R.id.joined_group_member_remove);
            }
        }
        JoinedMemberAdapter(ArrayList<Member> members) {
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
            final Member member = members.get(position);
            holder.tvMemberName.setText(member.getName());
            holder.btnRemove.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ConfirmDialog.newInstance(JoinedMemberAdapter.this,
                            "退出群組",
                            "確定將 " + member.getName() + " 退出群組？",
                            member)
                            .show(getSupportFragmentManager(), "GroupRemove");
                }
            });
        }

        @Override
        public int getItemCount() {
            return members.size();
        }

        @Override
        public void onConfirm(Object tag) {
            Member member = (Member) tag;
            Clouds.getInstance().exitGroup(group.getId(), member.getUid())
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Snackbar.make(recyclerView, "退出群組成功", Snackbar.LENGTH_LONG).show();
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Snackbar.make(recyclerView, "退出群組失敗", Snackbar.LENGTH_LONG).show();
                        }
                    });
        }
    }

    class JoiningMemberAdapter extends RecyclerView.Adapter<JoiningMemberAdapter.ViewHolder>
            implements ConfirmDialog.ConfirmListener {
        private ArrayList<Member> members;
        class TagData {
            Member member;
            int clickedViewId;
        }
        class ViewHolder extends RecyclerView.ViewHolder {
            TextView tvMemberName;
            Button btnAccept;
            Button btnDecline;
            ViewHolder(View itemView) {
                super(itemView);
                tvMemberName = itemView.findViewById(R.id.joining_group_member_name);
                btnAccept = itemView.findViewById(R.id.joining_group_member_accept);
                btnDecline = itemView.findViewById(R.id.joining_group_member_decline);
            }
        }
        JoiningMemberAdapter(ArrayList<Member> members) {
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
            final Member member = members.get(position);
            final TagData tagData = new TagData();
            tagData.member = member;
            holder.tvMemberName.setText(member.getName());
            holder.btnAccept.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    tagData.clickedViewId = v.getId();
                    ConfirmDialog.newInstance(JoiningMemberAdapter.this,
                            "加入群組",
                            "確定將 " + member.getName() + " 加入群組？",
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
                            "取消 " + member.getName() + " 加入群組申請？",
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
                    acceptJoinGroup(data.member);
                    break;
                case R.id.joining_group_member_decline:
                    declineJoinGroup(data.member);
                    break;
            }
        }

        private void acceptJoinGroup(Member member) {
            Clouds.getInstance().acceptJoinGroup(group, member.getUid(), member.getName())
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Snackbar.make(recyclerView, "加入群組成功", Snackbar.LENGTH_LONG).show();
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Snackbar.make(recyclerView, "加入群組失敗", Snackbar.LENGTH_LONG).show();
                        }
                    });
        }

        private void declineJoinGroup(Member member) {
            Clouds.getInstance().declineJoinGroup(group.getId(), member.getUid())
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Snackbar.make(recyclerView, "已取消加入申請", Snackbar.LENGTH_LONG).show();
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Snackbar.make(recyclerView, "取消加入申請失敗", Snackbar.LENGTH_LONG).show();
                        }
                    });
        }
    }
}
