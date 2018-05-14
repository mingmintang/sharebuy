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
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioButton;
import android.widget.TextView;

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
