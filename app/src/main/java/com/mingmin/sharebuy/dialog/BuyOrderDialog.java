package com.mingmin.sharebuy.dialog;

import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.support.v7.app.AppCompatDialogFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.mingmin.sharebuy.Buyer;
import com.mingmin.sharebuy.Member;
import com.mingmin.sharebuy.Order;
import com.mingmin.sharebuy.R;
import com.mingmin.sharebuy.cloud.Fdb;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;

public class BuyOrderDialog extends AppCompatDialogFragment {
    private Order order;
    private HashMap<String, Member> members;

    public static BuyOrderDialog newInstance(Order order, HashMap<String, Member> members) {
        BuyOrderDialog fragment = new BuyOrderDialog();
        fragment.order = order;
        fragment.members = members;
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
        RequestOptions requestOptions = new RequestOptions().centerCrop()
                .centerInside()
                .placeholder(R.drawable.ic_downloading)
                .error(R.drawable.ic_alert);
        Glide.with(getContext())
                .load(order.getImageUrl())
                .apply(requestOptions)
                .into(imageView);

        TextView tvPrice = view.findViewById(R.id.buy_order_price);
        tvPrice.setText(String.valueOf(order.getPrice()));

        TextView tvCoinUnit = view.findViewById(R.id.buy_order_coin_unit);
        String[] coinUnits = getContext().getResources().getStringArray(R.array.coin_units);
        tvCoinUnit.setText(coinUnits[order.getCoinUnit()]);

        TextView tvName = view.findViewById(R.id.buy_order_name);
        tvName.setText(order.getName());

        TextView tvDesc = view.findViewById(R.id.buy_order_desc);
        tvDesc.setText(order.getDesc());

        setupBuyerList(view);
    }

    private void setupBuyerList(View view) {
        final TextView tvBuyerList = view.findViewById(R.id.buy_order_buyer_list);
        if (members == null && order.getGroupId() != null) {
            Fdb.getGroupMembersRef(order.getGroupId())
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            HashMap<String, Member> members = (HashMap<String, Member>) dataSnapshot.getValue();
                            tvBuyerList.setText(getBuyerList(members));
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });
        } else {
            tvBuyerList.setText(getBuyerList(members));
        }
    }

    private String getBuyerList(HashMap<String, Member> members) {
        HashMap<String, Buyer> map = (HashMap<String, Buyer>) order.getBuyers();
        if (map.size() == 0) {
            return "";
        }
        ArrayList<Buyer> buyers = new ArrayList<>(map.values());
        Collections.sort(buyers, new Comparator<Buyer>() {
            @Override
            public int compare(Buyer o1, Buyer o2) {
                return (int) (o1.getOrderTime() - o2.getOrderTime());
            }
        });
        StringBuffer sb = new StringBuffer();
        for (Buyer buyer : buyers) {
            sb.append(members.get(buyer.getUid()).getNickname())
                    .append(" +")
                    .append(buyer.getBuyCount())
                    .append(" = ")
                    .append(order.getPrice() * buyer.getBuyCount())
                    .append("\n");
        }
        return sb.toString();
    }

    public interface OnBuyOrderListener {

    }
}
