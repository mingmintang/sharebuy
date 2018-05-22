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

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class JoinGroupDialog extends DialogFragment {

    private static JoinGroupDialog instance;
    private OnJoinGroupListener listener;
    private ArrayList<Group> groups;

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

        groups = new ArrayList<>();
        groups.add(new Group("1a2s3d4f", "keke1", "koaqkisjwis", "Jennit1"));
        groups.add(new Group("1a2s3d4f", "keke2", "koaqkisjwis", "Jennit2"));
        groups.add(new Group("1a2s3d4f", "keke3", "koaqkisjwis", "Jennit3"));
        groups.add(new Group("1a2s3d4f", "keke4", "koaqkisjwis", "Jennit4"));

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
                            .addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    for (DataSnapshot item : dataSnapshot.getChildren()) {
                                        Group g = item.getValue(Group.class);
                                        Log.d("wwwww", "onDataChange: " + g.getName() + "/" + g.getSearchCode());
                                    }

//                                    for (DataSnapshot ds : dataSnapshot.getChildren()) {
//                                        Group group = ds.getValue(Group.class);
//                                        Log.d("wwwww", "onDataChange: " + group.getName() + "/" + group.getSearchCode());
//                                    }
                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {
                                    Log.d("wwwww", "onCancelled: " + databaseError.getDetails());
                                }
                            });
//                            .addChildEventListener(new ChildEventListener() {
//                                @Override
//                                public void onChildAdded(DataSnapshot dataSnapshot, String s) {
//                                    Group group = dataSnapshot.getValue(Group.class);
//                                    Log.d("wwwww", "onDataChange: " + group.getName() + "/" + group.getSearchCode());
//                                }
//
//                                @Override
//                                public void onChildChanged(DataSnapshot dataSnapshot, String s) {
//
//                                }
//
//                                @Override
//                                public void onChildRemoved(DataSnapshot dataSnapshot) {
//
//                                }
//
//                                @Override
//                                public void onChildMoved(DataSnapshot dataSnapshot, String s) {
//
//                                }
//
//                                @Override
//                                public void onCancelled(DatabaseError databaseError) {
//
//                                }
//                            });
                }

            }
        });
        Button btnConfirm = view.findViewById(R.id.join_group_confirm);
        initRecyclerView(view);

        AlertDialog dialog = new AlertDialog.Builder(getActivity())
                .setView(view)
                .create();
        return dialog;
    }

    public interface OnJoinGroupListener {
        void onJoinGroupConfirm(Group group);
    }

    private void initRecyclerView(View view) {
        RecyclerView recyclerView = view.findViewById(R.id.join_group_recyclerView);
        recyclerView.setHasFixedSize(true);
        LinearLayoutManager llm = new LinearLayoutManager(getContext()){
            @Override
            public boolean isAutoMeasureEnabled() {
                return false;
            }
        };
        recyclerView.setLayoutManager(llm);
        recyclerView.setAdapter(new GroupAdapter(groups));
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
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            Group group = groups.get(position);
            holder.tvName.setText(group.getName());
            holder.tvFounderName.setText(group.getFounderName());
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
