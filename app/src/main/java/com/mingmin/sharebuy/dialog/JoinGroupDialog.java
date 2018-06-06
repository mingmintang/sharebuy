package com.mingmin.sharebuy.dialog;

import android.app.AlertDialog;
import android.app.Dialog;
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
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.mingmin.sharebuy.Group;
import com.mingmin.sharebuy.R;
import com.mingmin.sharebuy.notification.GroupNotification;
import com.mingmin.sharebuy.notification.Notification;

import java.util.ArrayList;

public class JoinGroupDialog extends DialogFragment {

    private static JoinGroupDialog instance;
    private OnJoinGroupListener listener;
    private ArrayList<Group> searchedGroups = new ArrayList<>();
    private RecyclerView recyclerView;

    public static JoinGroupDialog getInstance(OnJoinGroupListener listener) {
        if (instance == null) {
            instance = new JoinGroupDialog();
        }
        instance.listener = listener;

        return instance;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        View view = LayoutInflater.from(getContext())
                .inflate(R.layout.dialog_join_group, null, false);

        final AlertDialog dialog = new AlertDialog.Builder(getActivity())
                .setView(view)
                .create();
        dialog.getWindow().setWindowAnimations(R.style.dialog_animation);

        initRecyclerView(view);

        final Button btnConfirm = view.findViewById(R.id.join_group_confirm);
        btnConfirm.setEnabled(false);
        btnConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final GroupAdapter adapter = (GroupAdapter) recyclerView.getAdapter();
                adapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
                    @Override
                    public void onChanged() {
                        super.onChanged();
                    }
                });
                listener.onJoinGroupConfirm(searchedGroups.get(adapter.getSelectedPosition()));
                dialog.dismiss();
            }
        });

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
                    FirebaseDatabase.getInstance()
                            .getReference("groups")
                            .orderByChild("searchCode")
                            .equalTo(searchCode)
                            .addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    searchedGroups.clear();
                                    for (DataSnapshot item : dataSnapshot.getChildren()) {
                                        Group group = item.getValue(Group.class);
                                        searchedGroups.add(group);
                                    }
                                    recyclerView.setAdapter(new GroupAdapter(searchedGroups));
                                    if (searchedGroups.size() > 0) {
                                        btnConfirm.setEnabled(true);
                                    } else {
                                        btnConfirm.setEnabled(false);
                                    }
                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {

                                }
                            });
                }
            }
        });

        return dialog;
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
