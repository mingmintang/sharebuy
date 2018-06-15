package com.mingmin.sharebuy.dialog;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatDialogFragment;

import com.mingmin.sharebuy.R;

public class ConfirmDialog extends AppCompatDialogFragment {
    private OnConfirmListener listener;
    private String title;
    private String message;
    private Object tag;
    private boolean isTwice;

    public static ConfirmDialog newInstance(OnConfirmListener listener,
                                            String title, String message, Object tag) {
        ConfirmDialog fragment = new ConfirmDialog();
        fragment.listener = listener;
        fragment.title = title;
        fragment.message = message;
        fragment.tag = tag;
        fragment.isTwice = false;
        return fragment;
    }

    public static ConfirmDialog newInstance(OnConfirmListener listener,
                                            String title, String message, Object tag, boolean isTwice) {
        ConfirmDialog fragment = newInstance(listener, title, message, tag);
        fragment.isTwice = isTwice;
        return fragment;
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
