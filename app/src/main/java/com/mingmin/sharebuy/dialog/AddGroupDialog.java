package com.mingmin.sharebuy.dialog;

import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatDialogFragment;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.mingmin.sharebuy.R;

public class AddGroupDialog extends AppCompatDialogFragment {
    private AddGroupListener listener;
    private Button btnConfirm;
    private EditText etGroupName;
    private EditText etManagerName;

    public static AddGroupDialog newInstance(AddGroupListener listener) {
        AddGroupDialog fragment = new AddGroupDialog();
        fragment.listener = listener;
        return fragment;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        View view = LayoutInflater.from(getContext())
                .inflate(R.layout.dialog_add_group, null);

        final AlertDialog dialog = new AlertDialog.Builder(getContext())
                .setTitle("新增群組")
                .setView(view)
                .create();
        dialog.getWindow().setWindowAnimations(R.style.dialog_animation_up);

        etGroupName = view.findViewById(R.id.add_group_name);
        etManagerName = view.findViewById(R.id.add_group_managerName);
        btnConfirm = view.findViewById(R.id.add_group_confirm);
        btnConfirm.setEnabled(false);

        etGroupName.addTextChangedListener(new AddGroupTextWatcher());
        etManagerName.addTextChangedListener(new AddGroupTextWatcher());
        btnConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String groupName = etGroupName.getText().toString();
                String managerName = etManagerName.getText().toString();
                listener.onAddGroupConfirm(groupName, managerName);
                dialog.dismiss();
            }
        });

        return dialog;
    }

    private class AddGroupTextWatcher implements TextWatcher {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            String[] checkedTexts = new String[] {etGroupName.getText().toString(), etManagerName.getText().toString()};
            boolean hasEmpty = false;
            for (String checkedText : checkedTexts) {
                if (TextUtils.isEmpty(checkedText)) {
                    hasEmpty = true;
                }
            }
            if (hasEmpty) {
                btnConfirm.setEnabled(false);
            } else {
                btnConfirm.setEnabled(true);
            }
        }

        @Override
        public void afterTextChanged(Editable s) { }
    }

    public interface AddGroupListener {
        void onAddGroupConfirm(String groupName, String managerName);
    }
}
