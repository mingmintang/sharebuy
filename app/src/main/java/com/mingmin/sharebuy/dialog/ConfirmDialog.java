package com.mingmin.sharebuy.dialog;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.view.View;

import com.mingmin.sharebuy.R;

public class ConfirmDialog extends DialogFragment {
    private static ConfirmDialog instance;
    private OnConfirmListener listener;
    private String title;
    private String message;
    private View view;

    public static ConfirmDialog getInstance(OnConfirmListener listener,
                                            String title, String message, View view) {
        if (instance == null) {
            instance = new ConfirmDialog();
        }
        instance.listener = listener;
        instance.title = title;
        instance.message = message;
        instance.view = view;
        return instance;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog dialog = new AlertDialog.Builder(getContext())
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton("確定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        listener.onConfirm(view);
                    }
                })
                .setNegativeButton("取消", null)
                .create();
        dialog.getWindow().setWindowAnimations(R.style.dialog_animation);
        return dialog;
    }

    public interface OnConfirmListener {
        void onConfirm(View view);
    }
}
