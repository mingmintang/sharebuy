package com.mingmin.sharebuy.dialog;

import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.support.v7.app.AppCompatDialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.mingmin.sharebuy.Buyer;
import com.mingmin.sharebuy.Order;
import com.mingmin.sharebuy.R;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;

public class BuyOrderDialog extends AppCompatDialogFragment {
    private Order order;

    public static BuyOrderDialog newInstance(Order order) {
        BuyOrderDialog fragment = new BuyOrderDialog();
        fragment.order = order;
        return fragment;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        View view = LayoutInflater.from(getContext())
                .inflate(R.layout.dialog_buy_order, null, false);
        initView(view);
        AlertDialog dialog = new AlertDialog.Builder(getContext())
                .setView(view)
                .create();
        return dialog;
    }

    private void initView(View view) {
        ImageView imageView = view.findViewById(R.id.buy_order_image);
        Glide.with(getContext())
                .load(new File(order.getImagePath()))
                .into(imageView);

        TextView tvPrice = view.findViewById(R.id.buy_order_price);
        tvPrice.setText(order.getPrice());

        TextView tvCoinUnit = view.findViewById(R.id.buy_order_coin_unit);
        String[] coinUnits = getContext().getResources().getStringArray(R.array.coin_units);
        tvCoinUnit.setText(coinUnits[order.getCoinUnit()]);

        TextView tvName = view.findViewById(R.id.buy_order_name);
        tvName.setText(order.getName());

        TextView tvDesc = view.findViewById(R.id.buy_order_desc);
        tvDesc.setText(order.getDesc());

        TextView tvBuyerList = view.findViewById(R.id.buy_order_buyer_list);
        tvBuyerList.setText(getBuyerList());


    }

    private String getBuyerList() {
        StringBuffer sb = new StringBuffer();
        HashMap<String, Buyer> map = (HashMap<String, Buyer>) order.getBuyers();
        ArrayList<Buyer> buyers = new ArrayList<>(map.values());
        Collections.sort(buyers, new Comparator<Buyer>() {
            @Override
            public int compare(Buyer o1, Buyer o2) {
                return (int) (o1.getOrderTime() - o2.getOrderTime());
            }
        });
        for (Buyer buyer : buyers) {
//            sb.append(buyer.get)
        }

        return sb.toString();
    }

    public interface OnBuyOrderListener {

    }
}
