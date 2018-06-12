package com.mingmin.sharebuy.dialog;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v7.recyclerview.extensions.ListAdapter;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.mingmin.sharebuy.Group;
import com.mingmin.sharebuy.R;

import java.util.ArrayList;

public class SelectGroupDialog extends DialogFragment {
    private String title;
    private String uid;
    private OnSelectGroupListener listener;
    private Object tag;
    private ArrayList<Group> groups = new ArrayList<>();
    private RecyclerView recyclerView;
    private Button btnConfirm;

    public static SelectGroupDialog newInstance(String title, String uid,
                                                OnSelectGroupListener listener, Object tag) {
        SelectGroupDialog fragment = new SelectGroupDialog();
        fragment.title = title;
        fragment.uid = uid;
        fragment.listener = listener;
        fragment.tag = tag;
        return fragment;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        View view = LayoutInflater.from(getContext())
                .inflate(R.layout.dialog_select_group, null, false);
        final AlertDialog dialog = new AlertDialog.Builder(getContext())
                .setTitle(title)
                .setView(view)
                .create();
        dialog.getWindow().setWindowAnimations(R.style.dialog_animation_up);

        initRecyclerView(view);

        btnConfirm = view.findViewById(R.id.select_group_confirm);
        btnConfirm.setEnabled(false);
        btnConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                GroupAdapter adapter = (GroupAdapter) recyclerView.getAdapter();
                listener.onSelectGroupConfirm(groups.get(adapter.getSelectedPosition()), tag);
                dialog.dismiss();
            }
        });

        return dialog;
    }

    public interface OnSelectGroupListener {
        void onSelectGroupConfirm(Group group, Object tag);
    }

    private void initRecyclerView(View view) {
        recyclerView = view.findViewById(R.id.select_group_recyclerView);
        recyclerView.setHasFixedSize(true);
        LinearLayoutManager llm = new LinearLayoutManager(getContext()){
            @Override
            public boolean isAutoMeasureEnabled() {
                return false;
            }
        };
        recyclerView.setLayoutManager(llm);

        final FirebaseDatabase fdb = FirebaseDatabase.getInstance();
        fdb.getReference("users")
                .child(uid)
                .child("groups")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        final long count = dataSnapshot.getChildrenCount();
                        groups.clear();
                        if (count == 0) {
                            recyclerView.setAdapter(new GroupAdapter(groups));
                            return;
                        }
                        for (DataSnapshot childSnap : dataSnapshot.getChildren()) {
                            fdb.getReference("groups")
                                    .child(childSnap.getKey())
                                    .addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(DataSnapshot dataSnapshot) {
                                            Group group = dataSnapshot.getValue(Group.class);
                                            groups.add(group);
                                            if (groups.size() == count) {
                                                recyclerView.setAdapter(new GroupAdapter(groups));
                                                btnConfirm.setEnabled(true);
                                            }
                                        }

                                        @Override
                                        public void onCancelled(DatabaseError databaseError) {

                                        }
                                    });
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
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
            View view = getLayoutInflater().inflate(R.layout.row_group, parent, false);
            view.setOnClickListener(this);
            return new ViewHolder(view);
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