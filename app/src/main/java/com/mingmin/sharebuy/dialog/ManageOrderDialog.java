package com.mingmin.sharebuy.dialog;

import android.os.Bundle;
import android.support.v7.app.AppCompatDialogFragment;

public class ManageOrderDialog extends AppCompatDialogFragment {
    public static ManageOrderDialog newInstance() {
        ManageOrderDialog fragment = new ManageOrderDialog();
        return fragment;
    }

    public interface ManageOrderListener {

    }
}
