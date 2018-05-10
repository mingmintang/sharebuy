package com.mingmin.sharebuy;

import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

public class AddGroupDialog extends DialogFragment {
    private static AddGroupDialog instance;
    private OnAddGroupListener listener;

    private void setListener(OnAddGroupListener listener) {
        this.listener = listener;
    }

    public static AddGroupDialog getInstance(OnAddGroupListener listener) {
        if (instance == null) {
            instance = new AddGroupDialog();
            instance.setListener(listener);
        }
        return instance;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        View view = LayoutInflater.from(getContext())
                .inflate(R.layout.dialog_add_group, null);

        final EditText etName = view.findViewById(R.id.add_group_name);
        final TextInputLayout tilName = view.findViewById(R.id.add_group_name_layout);

        final AlertDialog alertDialog = new AlertDialog.Builder(getContext())
                .setTitle("新增群組")
                .setView(view)
                .create();
        alertDialog.getWindow().setWindowAnimations(R.style.dialog_animation);

        Button btnConfirm = view.findViewById(R.id.add_group_confirm);
        btnConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String groupName = etName.getText().toString();
                if (TextUtils.isEmpty(groupName)) {
                    tilName.setError("名稱不能空白");
                } else {
                    tilName.setError("");
                    listener.onConfirm(groupName);
                    alertDialog.dismiss();
                }
            }
        });

        return alertDialog;
    }

    public interface OnAddGroupListener {
        void onConfirm(String groupName);
    }
}
