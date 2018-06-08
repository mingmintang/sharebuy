package com.mingmin.sharebuy.dialog;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;

import com.mingmin.sharebuy.R;

public class ConfirmDialog extends DialogFragment {
    private static ConfirmDialog instance;
    private OnConfirmListener listener;
    private String title;
    private String message;
    private Object tag;
    private boolean isTwice;

    public static ConfirmDialog getInstance(OnConfirmListener listener,
                                            String title, String message, Object tag) {
        if (instance == null) {
            instance = new ConfirmDialog();
        }
        instance.listener = listener;
        instance.title = title;
        instance.message = message;
        instance.tag = tag;
        instance.isTwice = false;
        return instance;
    }

    public static ConfirmDialog getInstance(OnConfirmListener listener,
                                            String title, String message, Object tag, boolean isTwice) {
        getInstance(listener, title, message, tag);
        instance.isTwice = isTwice;
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
                        if (isTwice) {
                            confirmTwice();
                        } else {
                            listener.onConfirm(tag);
                        }
                    }
                })
                .setNegativeButton("取消", null)
                .create();
        if (isTwice) {
            dialog.getWindow().setWindowAnimations(R.style.dialog_animation_left);
        } else {
            dialog.getWindow().setWindowAnimations(R.style.dialog_animation_up);
        }
        return dialog;
    }

    private void confirmTwice() {
        AlertDialog dialogTwice = new AlertDialog.Builder(getContext())
                .setTitle(title)
                .setMessage("確定嗎？")
                .setPositiveButton("確定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        listener.onConfirm(tag);
                    }
                })
                .setNegativeButton("取消", null)
                .create();
        dialogTwice.getWindow().setWindowAnimations(R.style.dialog_animation_left);
        dialogTwice.show();
    }

    public interface OnConfirmListener {
        void onConfirm(Object tag);
    }
}
