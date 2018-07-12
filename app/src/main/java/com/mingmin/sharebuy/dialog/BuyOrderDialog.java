package com.mingmin.sharebuy.dialog;

import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputEditText;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatDialogFragment;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.mingmin.sharebuy.Buyer;
import com.mingmin.sharebuy.Group;
import com.mingmin.sharebuy.Order;
import com.mingmin.sharebuy.R;
import com.mingmin.sharebuy.cloud.Clouds;
import com.mingmin.sharebuy.utils.InputFilterMinMax;

import java.util.ArrayList;

public class BuyOrderDialog extends AppCompatDialogFragment {
    private final String TAG = getClass().getSimpleName();
    private BuyOrderListener listener;
    private Order order;
    private Group group;
    private TextView tvAmount;

    public static BuyOrderDialog newInstance(BuyOrderListener listener, Order order, Group group) {
        BuyOrderDialog fragment = new BuyOrderDialog();
        fragment.listener = listener;
        fragment.order = order;
        fragment.group = group;
        return fragment;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        View view = LayoutInflater.from(getContext())
                .inflate(R.layout.dialog_buy_order, null, false);
        AlertDialog dialog = new AlertDialog.Builder(getContext())
                .setView(view)
                .create();
        dialog.getWindow().setWindowAnimations(R.style.dialog_animation_up);
        initView(view, dialog);
        initBuyerList(view);
        return dialog;
    }

    private void initView(View view, final AlertDialog dialog) {
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

        final int maxBuyCount = (order.getMaxBuyCount() == -1) ? -1 : (order.getMaxBuyCount() - order.getBuyCount());
        int minBuyCount = (maxBuyCount == 0) ? 0 : 1;

        tvAmount = view.findViewById(R.id.buy_order_amount);
        updateAmount(minBuyCount);

        TextInputLayout etBuyCountLayout = view.findViewById(R.id.buy_order_buyCount_layout);
        if (maxBuyCount != -1) {
            etBuyCountLayout.setHint(String.valueOf(maxBuyCount));
        }

        final TextInputEditText etBuyCount = view.findViewById(R.id.buy_order_buyCount);
        etBuyCount.setText(String.valueOf(minBuyCount));
        etBuyCount.setFilters(new InputFilter[]{new InputFilterMinMax(1, maxBuyCount)});
        etBuyCount.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                int buyCount = Integer.parseInt(s.toString());
                updateAmount(buyCount);
            }

            @Override
            public void afterTextChanged(Editable s) { }
        });

        Button btnDecrease = view.findViewById(R.id.buy_order_decrease);
        btnDecrease.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int buyCount = Integer.parseInt(etBuyCount.getText().toString());
                if (buyCount > 1) {
                    buyCount -= 1;
                    etBuyCount.setText(String.valueOf(buyCount));
                }
            }
        });

        Button btnIncrease = view.findViewById(R.id.buy_order_increase);
        btnIncrease.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int buyCount = Integer.parseInt(etBuyCount.getText().toString());
                if (buyCount < maxBuyCount) {
                    buyCount += 1;
                    etBuyCount.setText(String.valueOf(buyCount));
                }
            }
        });

        Button btnConfirm = view.findViewById(R.id.buy_order_confirm);
        btnConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.onBuyOrderConfirm(order, Integer.parseInt(etBuyCount.getText().toString()));
                dialog.dismiss();
            }
        });

        if (etBuyCount.getText().toString().equals("0")) {
            btnConfirm.setEnabled(false);
            btnDecrease.setEnabled(false);
            btnIncrease.setEnabled(false);
            etBuyCount.setEnabled(false);
        }
    }

    private void updateAmount(int buyCount) {
        int amount = buyCount * order.getPrice();
        this.tvAmount.setText(String.valueOf(amount));
    }

    private void initBuyerList(View view) {
        final TextView tvBuyerList = view.findViewById(R.id.buy_order_buyer_list);
        Clouds.getInstance().getGroupOrderBuyers(group.getId(), order.getId())
                .addOnSuccessListener(new OnSuccessListener<ArrayList<Buyer>>() {
                    @Override
                    public void onSuccess(ArrayList<Buyer> buyers) {
                        if (buyers.size() > 0) {
                            StringBuffer sb = new StringBuffer();
                            for (Buyer buyer : buyers) {
                                sb.append(group.searchNicknameByUid(buyer.getUid()))
                                        .append(" +")
                                        .append(buyer.getBuyCount())
                                        .append(" = ")
                                        .append(order.getPrice() * buyer.getBuyCount())
                                        .append("\n");
                            }
                            tvBuyerList.setText(sb.toString());
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(TAG, "onFailure: " + e.getMessage());
                    }
                });
    }

    public interface BuyOrderListener {
        void onBuyOrderConfirm(Order order, int buyCount);
    }
}
