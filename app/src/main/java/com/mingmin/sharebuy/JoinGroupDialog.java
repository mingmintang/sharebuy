package com.mingmin.sharebuy;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.InputFilter;
import android.text.Spanned;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RadioButton;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.RemoteMessage;
import com.mingmin.sharebuy.notification.GroupNotification;
import com.mingmin.sharebuy.notification.Notification;

import java.util.ArrayList;

public class JoinGroupDialog extends DialogFragment {

    private static JoinGroupDialog instance;
    private OnJoinGroupListener listener;
    private ArrayList<Group> groups = new ArrayList<>();
    private RecyclerView recyclerView;
    private Query searchCodeQuery;
    private ValueEventListener searchCodeEvent;

    private void setListener(OnJoinGroupListener listener) {
        this.listener = listener;
    }

    public static JoinGroupDialog getInstance(OnJoinGroupListener listener) {
        if (instance == null) {
            instance = new JoinGroupDialog();
            instance.setListener(listener);
        }
        return instance;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        View view = LayoutInflater.from(getContext())
                .inflate(R.layout.dialog_join_group, null, false);

        initRecyclerView(view);

        final EditText etSearchCode = view.findViewById(R.id.join_group_searchCode);
        etSearchCode.setFilters(new InputFilter[]{new InputFilter() {
            @Override
            public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
                int input = Integer.parseInt(dest.toString() + source.toString());
                if (input > 0 && input < 1000000) {
                    return null;
                }
                return "";
            }
        }});

        ImageButton ibSearch = view.findViewById(R.id.join_group_search);
        ibSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (TextUtils.isEmpty(etSearchCode.getText())) {
                    etSearchCode.setError("不能空白");
                } else {
                    int searchCode = Integer.parseInt(etSearchCode.getText().toString());
                    searchCodeQuery = FirebaseDatabase.getInstance()
                            .getReference("groups")
                            .orderByChild("searchCode")
                            .equalTo(searchCode);
                    searchCodeEvent = new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            groups.clear();
                            for (DataSnapshot item : dataSnapshot.getChildren()) {
                                Group group = item.getValue(Group.class);
                                Log.d("wwwww", "onDataChange: " + group.getName() + "/" + group.getSearchCode());
                                groups.add(group);
                            }
                            recyclerView.setAdapter(new GroupAdapter(groups));
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    };
                    searchCodeQuery.addValueEventListener(searchCodeEvent);
                }
            }
        });

        Button btnConfirm = view.findViewById(R.id.join_group_confirm);
        btnConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                GroupAdapter adapter = (GroupAdapter) recyclerView.getAdapter();
                Group group = groups.get(adapter.getSelectedPosition());
                FirebaseUser fuser = FirebaseAuth.getInstance().getCurrentUser();
                if (fuser != null) {
                    GroupNotification notification = new GroupNotification(
                            fuser.getUid(),
                            group.getFounderUid(),
                            Notification.ACTION_REQUEST_JOIN_GROUP,
                            group.getId());

                    FirebaseDatabase.getInstance()
                            .getReference("groups")
                            .child(group.getId())
                            .child("notifications")
                            .push()
                            .setValue(notification);
                }
            }
        });

        AlertDialog dialog = new AlertDialog.Builder(getActivity())
                .setView(view)
                .create();
        return dialog;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (searchCodeQuery != null && searchCodeEvent != null) {
            searchCodeQuery.removeEventListener(searchCodeEvent);
        }
    }

    public interface OnJoinGroupListener {
        void onJoinGroupConfirm(Group group);
    }

    private void initRecyclerView(View view) {
        recyclerView = view.findViewById(R.id.join_group_recyclerView);
        recyclerView.setHasFixedSize(true);
        LinearLayoutManager llm = new LinearLayoutManager(getContext()){
            @Override
            public boolean isAutoMeasureEnabled() {
                return false;
            }
        };
        recyclerView.setLayoutManager(llm);
    }

    class GroupAdapter extends RecyclerView.Adapter<GroupAdapter.ViewHolder> implements View.OnClickListener {
        class ViewHolder extends RecyclerView.ViewHolder {
            TextView tvName;
            TextView tvFounderName;
            RadioButton radioButton;
            ViewHolder(View itemView) {
                super(itemView);
                tvName = itemView.findViewById(R.id.group_name);
                tvFounderName = itemView.findViewById(R.id.group_founderName);
                radioButton = itemView.findViewById(R.id.group_radioButton);
                radioButton.setClickable(false);
            }
        }

        private ArrayList<Group> groups;
        private int selectedPosition = 0;

        public int getSelectedPosition() {
            return selectedPosition;
        }

        GroupAdapter(ArrayList<Group> groups) {
            this.groups = groups;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View itemView = getLayoutInflater().inflate(R.layout.row_group, parent, false);
            itemView.setOnClickListener(this);
            return new ViewHolder(itemView);
        }

        @Override
        public void onBindViewHolder(@NonNull final ViewHolder holder, int position) {
            Group group = groups.get(position);
            holder.tvName.setText(group.getName());
            FirebaseDatabase.getInstance()
                    .getReference("users")
                    .child(group.getFounderUid())
                    .child("data")
                    .child("nickname")
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            String nickname = (String) dataSnapshot.getValue();
                            if (nickname != null) {
                                holder.tvFounderName.setText(nickname);
                            }
                        }
                        @Override
                        public void onCancelled(DatabaseError databaseError) {
                        }
                    });
            holder.radioButton.setChecked(selectedPosition == position);
            holder.itemView.setTag(position);

        }

        @Override
        public int getItemCount() {
            return groups.size();
        }

        @Override
        public void onClick(View v) {
            selectedPosition = (int) v.getTag();
            notifyDataSetChanged();
        }

    }
}
