package com.mingmin.sharebuy.dialog;

import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatDialogFragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.InputFilter;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RadioButton;
import android.widget.TextView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.mingmin.sharebuy.item.Group;
import com.mingmin.sharebuy.R;
import com.mingmin.sharebuy.cloud.Clouds;

import java.util.ArrayList;

public class JoinGroupDialog extends AppCompatDialogFragment {

    private final String TAG = getClass().getSimpleName();
    private JoinGroupListener listener;
    private ArrayList<Group> searchedGroups = new ArrayList<>();
    private RecyclerView recyclerView;
    private Button btnConfirm;
    private EditText etMemberName;
    private TextInputLayout tlMemberName;

    public static JoinGroupDialog newInstance(JoinGroupListener listener) {
        JoinGroupDialog fragment = new JoinGroupDialog();
        fragment.listener = listener;
        return fragment;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        View view = LayoutInflater.from(getContext())
                .inflate(R.layout.dialog_join_group, null, false);

        final AlertDialog dialog = new AlertDialog.Builder(getActivity())
                .setView(view)
                .create();
        dialog.getWindow().setWindowAnimations(R.style.dialog_animation_up);

        final EditText etSearchCode = view.findViewById(R.id.join_group_searchCode);
        final TextInputLayout tlSearchCode = view.findViewById(R.id.join_group_searchCode_layout);
        ImageButton ibSearch = view.findViewById(R.id.join_group_search);
        etMemberName = view.findViewById(R.id.join_group_memberName);
        tlMemberName = view.findViewById(R.id.join_group_memberName_layout);
        btnConfirm = view.findViewById(R.id.join_group_confirm);
        btnConfirm.setEnabled(false);

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

        ibSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (TextUtils.isEmpty(etSearchCode.getText())) {
                    tlSearchCode.setError("不能空白");
                    return;
                }
                tlSearchCode.setErrorEnabled(false);

                int searchCode = Integer.parseInt(etSearchCode.getText().toString());
                Clouds.getInstance().searchGroupsBySearchCode(searchCode)
                        .addOnSuccessListener(new OnSuccessListener<ArrayList<Group>>() {
                            @Override
                            public void onSuccess(ArrayList<Group> groups) {
                                searchedGroups = groups;
                                recyclerView.setAdapter(new GroupAdapter(searchedGroups));
                                switchBtnConfirmEnable();
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Log.d(TAG, "onFailure: " + e.getMessage());
                            }
                        });
            }
        });

        etMemberName.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                switchBtnConfirmEnable();
            }

            @Override
            public void afterTextChanged(Editable s) { }
        });

        btnConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                GroupAdapter adapter = (GroupAdapter) recyclerView.getAdapter();
                final Group selectedGroup = searchedGroups.get(adapter.getSelectedPosition());
                final String memberName = etMemberName.getText().toString();
                Clouds.getInstance().checkMemberNameDuplicate(selectedGroup, memberName)
                        .addOnSuccessListener(new OnSuccessListener<Boolean>() {
                            @Override
                            public void onSuccess(Boolean isDuplicate) {
                                if (isDuplicate) {
                                    tlMemberName.setError("名稱重複");
                                } else {
                                    tlMemberName.setErrorEnabled(false);
                                    listener.onJoinGroupConfirm(selectedGroup, memberName);
                                    dialog.dismiss();
                                }
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Log.d(TAG, "onFailure: " + e.getMessage());
                            }
                        });
            }
        });

        initRecyclerView(view);

        return dialog;
    }

    private void switchBtnConfirmEnable() {
        if (searchedGroups.size() > 0 && !TextUtils.isEmpty(etMemberName.getText().toString())) {
            btnConfirm.setEnabled(true);
        } else {
            btnConfirm.setEnabled(false);
        }
    }

    public interface JoinGroupListener {
        void onJoinGroupConfirm(Group group, String myName);
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
            TextView tvManagerName;
            RadioButton radioButton;
            ViewHolder(View itemView) {
                super(itemView);
                tvName = itemView.findViewById(R.id.group_name);
                tvManagerName = itemView.findViewById(R.id.group_founderName);
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
            holder.tvManagerName.setText(group.getManagerName());
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
