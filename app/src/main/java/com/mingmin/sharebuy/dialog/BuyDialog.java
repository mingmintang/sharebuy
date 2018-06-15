package com.mingmin.sharebuy.dialog;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatDialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.NumberPicker;

import com.mingmin.sharebuy.R;

public class BuyDialog extends AppCompatDialogFragment {
    private int maxCount;

    public static BuyDialog newInstance(int maxCount) {
        BuyDialog fragment = new BuyDialog();
        fragment.maxCount = maxCount;
        return fragment;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
//        NumberPicker numberPicker = new NumberPicker(getContext());
//        numberPicker.setMinValue(1);
//        numberPicker.setMaxValue(maxCount);
//        numberPicker.
        View view = LayoutInflater.from(getContext())
                .inflate(R.layout.dialog_buy, null, false);
        NumberPicker numberPicker = view.findViewById(R.id.buy_count);
        numberPicker.setMaxValue(5);
        numberPicker.setMinValue(1);
        numberPicker.setValue(1);
        AlertDialog dialog = new AlertDialog.Builder(getContext())
                .setView(view)
                .setPositiveButton("確定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                })
                .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                })
                .create();
        return dialog;
    }

    public interface OnBuyListener {
        void onBuyConfirm(String groupName);
    }
}
