package com.mingmin.sharebuy.dialog;

import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatDialogFragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.mingmin.sharebuy.R;

public class AddGroupDialog extends AppCompatDialogFragment {
    private AddGroupListener listener;

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

        final EditText etName = view.findViewById(R.id.add_group_name);
        final TextInputLayout tilName = view.findViewById(R.id.add_group_name_layout);

        final AlertDialog dialog = new AlertDialog.Builder(getContext())
                .setTitle("新增群組")
                .setView(view)
                .create();
        dialog.getWindow().setWindowAnimations(R.style.dialog_animation_up);

        Button btnConfirm = view.findViewById(R.id.add_group_confirm);
        btnConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String groupName = etName.getText().toString();
                if (TextUtils.isEmpty(groupName)) {
                    tilName.setError("名稱不能空白");
                } else {
                    tilName.setError("");
                    listener.onAddGroupConfirm(groupName);
                    dialog.dismiss();
                }
            }
        });

        return dialog;
    }

    public interface AddGroupListener {
        void onAddGroupConfirm(String groupName);
    }
}
