package com.mingmin.sharebuy;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

public class OrderHolder extends RecyclerView.ViewHolder {
    ImageView imageView;
    TextView tvName;
    TextView tvDesc;
    TextView tvPrice;
    TextView tvCount;
    TextView tvAmount;
    TextView tvCoinUnit;
    TextView tvNickname;
    TextView tvState;
    ImageButton ibMenu;
    OrderHolder(View itemView) {
        super(itemView);
        initViews(itemView);
    }

    private void initViews(View itemView) {
        imageView = itemView.findViewById(R.id.row_order_image);
        tvName = itemView.findViewById(R.id.row_order_name);
        tvDesc = itemView.findViewById(R.id.row_order_desc);
        tvPrice = itemView.findViewById(R.id.row_order_price);
        tvCount = itemView.findViewById(R.id.row_order_count);
        tvAmount = itemView.findViewById(R.id.row_order_amount);
        tvCoinUnit = itemView.findViewById(R.id.row_order_coin_unit);
        tvNickname = itemView.findViewById(R.id.row_order_nickname);
        tvState = itemView.findViewById(R.id.row_order_state);
        ibMenu = itemView.findViewById(R.id.row_order_menu);
    }

    public void calculateAmount() {
        int count = Integer.parseInt(tvCount.getText().toString());
        int price = Integer.parseInt(tvPrice.getText().toString());
        tvAmount.setText(String.valueOf(price * count));
    }
}
