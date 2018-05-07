package com.mingmin.sharebuy;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

public class OrderHolder extends RecyclerView.ViewHolder {
    ImageView imageView;
    TextView tvName;
    TextView tvPrice;
    TextView tvCount;
    TextView tvHelpCount;
    TextView tvAmount;
    Button btnEndOrder;
    public OrderHolder(View itemView) {
        super(itemView);
        initViews(itemView);
    }

    private void initViews(View itemView) {
        imageView = itemView.findViewById(R.id.row_order_image);
        tvName = itemView.findViewById(R.id.row_order_name);
        tvPrice = itemView.findViewById(R.id.row_order_price);
        tvCount = itemView.findViewById(R.id.row_order_count);
        tvHelpCount = itemView.findViewById(R.id.row_order_help_count);
        tvAmount = itemView.findViewById(R.id.row_order_amount);
        btnEndOrder = itemView.findViewById(R.id.row_order_endOrder);

        tvHelpCount.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                calculateAmount();
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    public void calculateAmount() {
        int count = Integer.parseInt(tvCount.getText().toString());
        int helpCount = Integer.parseInt(tvHelpCount.getText().toString());
        int price = Integer.parseInt(tvPrice.getText().toString());
        tvAmount.setText(String.valueOf(price * (count + helpCount)));
    }
}
