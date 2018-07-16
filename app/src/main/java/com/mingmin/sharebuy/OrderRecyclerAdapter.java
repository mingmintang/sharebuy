package com.mingmin.sharebuy;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.firebase.ui.firestore.SnapshotParser;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.Query;
import com.mingmin.sharebuy.cloud.Clouds;
import com.mingmin.sharebuy.cloud.OrderDoc;

import java.util.Date;

public class OrderRecyclerAdapter extends FirestoreRecyclerAdapter<Order, OrderRecyclerAdapter.OrderHolder> implements PopupMenu.OnMenuItemClickListener {
    class OrderHolder extends RecyclerView.ViewHolder {
        ImageView imageView;
        TextView tvName;
        TextView tvDesc;
        TextView tvPrice;
        TextView tvCount;
        TextView tvAmount;
        TextView tvCoinUnit;
        TextView tvStatus;
        ImageButton ibMenu;
        PopupMenu popupMenu;
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
            tvStatus = itemView.findViewById(R.id.row_order_status);
            ibMenu = itemView.findViewById(R.id.row_order_menu);
            popupMenu = new PopupMenu(context, ibMenu);
            popupMenu.getMenuInflater().inflate(R.menu.order_menu, popupMenu.getMenu());
            popupMenu.setOnMenuItemClickListener(OrderRecyclerAdapter.this);
            ibMenu.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    popupMenu.show();
                }
            });
        }

        private void calculateAmount() {
            int count = Integer.parseInt(tvCount.getText().toString());
            int price = Integer.parseInt(tvPrice.getText().toString());
            tvAmount.setText(String.valueOf(price * count));
        }
    }

    private Context context;
    private String[] coinUnits;
    private String[] orderStatus;
    private OrderRecyclerAdapterListener listener;
    private Group group;

    private OrderRecyclerAdapter(@NonNull FirestoreRecyclerOptions<Order> options) {
        super(options);
    }

    public OrderRecyclerAdapter(Context context, OrderRecyclerAdapterListener listener, Group group, Query query) {
        this(new FirestoreRecyclerOptions.Builder<Order>()
                .setQuery(query, new SnapshotParser<Order>() {
                    @NonNull
                    @Override
                    public Order parseSnapshot(@NonNull DocumentSnapshot snapshot) {
                        int state = snapshot.getLong("state").intValue();
                        int maxBuyCount = snapshot.getLong("maxBuyCount").intValue();
                        int buyCount = snapshot.getLong("buyCount").intValue();
                        String imageUrl = snapshot.getString("imageUrl");
                        String managerUid = snapshot.getString("managerUid");
                        String managerName = snapshot.getString("managerName");
                        String name = snapshot.getString("name");
                        String desc = snapshot.getString("desc");
                        String groupId = snapshot.getString("groupId");
                        int price = snapshot.getLong("price").intValue();
                        int coinUnit = snapshot.getLong("coinUnit").intValue();
                        Date createTime = snapshot.getDate("createTime");
                        Date endTime = new Date(0);
                        if (state == Order.STATE_END) {
                            endTime = snapshot.getDate("endTime");
                        }
                        Order order = new Order(snapshot.getId(), state, maxBuyCount, buyCount,
                                imageUrl, managerUid, managerName, name, desc, groupId,
                                price, coinUnit, createTime, endTime);

                        return order;
                    }
                })
                .build());
        this.context = context;
        this.listener = listener;
        this.group = group;
        coinUnits = context.getResources().getStringArray(R.array.coin_units);
        orderStatus = context.getResources().getStringArray(R.array.order_status);
    }

    @NonNull
    @Override
    public OrderHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context)
                .inflate(R.layout.row_order, parent, false);
        return new OrderHolder(view);
    }

    @Override
    protected void onBindViewHolder(@NonNull OrderHolder holder, int position, @NonNull final Order order) {
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.onOrderItemViewClicked(order, group);
            }
        });
        holder.tvName.setText(order.getName());
        holder.tvDesc.setText(order.getDesc());
        holder.tvPrice.setText(String.valueOf(order.getPrice()));
        holder.tvCount.setText(String.valueOf(order.getBuyCount()));
        holder.calculateAmount();
        holder.tvCoinUnit.setText(coinUnits[order.getCoinUnit()]);
        String status = order.getManagerName() + orderStatus[order.getState()];
        holder.tvStatus.setText(status);

        RequestOptions requestOptions = new RequestOptions()
                .centerCrop()
                .override(300, 300)
                .placeholder(R.drawable.ic_downloading)
                .error(R.drawable.ic_alert);
        Glide.with(context)
                .load(order.getImageUrl())
                .apply(requestOptions)
                .into(holder.imageView);
    }

    @Override
    public boolean onMenuItemClick(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.order_menu_buy:
                return true;
            default:
                return false;
        }
    }

    public interface OrderRecyclerAdapterListener {
        void onOrderItemViewClicked(Order order, Group group);
    }
}
