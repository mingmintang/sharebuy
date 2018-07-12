package com.mingmin.sharebuy.dialog;

import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatDialogFragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.TextView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.mingmin.sharebuy.Group;
import com.mingmin.sharebuy.R;
import com.mingmin.sharebuy.cloud.Clouds;

import java.util.ArrayList;

public class SelectGroupDialog extends AppCompatDialogFragment {
    private final String TAG = getClass().getSimpleName();
    private String title;
    private String uid;
    private SelectGroupListener listener;
    private Object tag;
    private RecyclerView recyclerView;
    private Button btnConfirm;

    public static SelectGroupDialog newInstance(String title, String uid,
                                                SelectGroupListener listener, Object tag) {
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
                listener.onSelectGroupConfirm(adapter.getSelectedGroup(), tag);
                dialog.dismiss();
            }
        });

        return dialog;
    }

    public interface SelectGroupListener {
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

        Clouds.getInstance().getUserGroups(uid)
                .addOnSuccessListener(new OnSuccessListener<ArrayList<Group>>() {
                    @Override
                    public void onSuccess(ArrayList<Group> groups) {
                        recyclerView.setAdapter(new GroupAdapter(groups));
                        btnConfirm.setEnabled(true);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(TAG, "onFailure: " + e.getMessage());
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

        public Group getSelectedGroup() {
            return groups.get(selectedPosition);
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
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            Group group = groups.get(position);
            holder.tvName.setText(group.getName());
            holder.tvFounderName.setText(group.getFounderNickname());
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
