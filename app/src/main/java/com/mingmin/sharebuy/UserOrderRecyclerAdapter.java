package com.mingmin.sharebuy;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
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
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.Query;
import com.mingmin.sharebuy.cloud.Clouds;
import com.mingmin.sharebuy.cloud.UserOrderCloud;
import com.mingmin.sharebuy.cloud.UserOrderDoc;

public class UserOrderRecyclerAdapter extends FirestoreRecyclerAdapter<Order, UserOrderRecyclerAdapter.OrderHolder> implements PopupMenu.OnMenuItemClickListener {

    private UserOrderCloud cloud;

    public class OrderHolder extends RecyclerView.ViewHolder implements UserOrderCloud.UserOrderListener {
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
            popupMenu.setOnMenuItemClickListener(UserOrderRecyclerAdapter.this);
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

        @Override
        public void onUserOrderChanged(final Order order, OrderHolder holder) {
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    listener.onUserOrderItemViewClicked(order, order.getMyName());
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
    }

    private Context context;
    private String[] coinUnits;
    private String[] orderStatus;
    private UserOrderRecyclerAdapterListener listener;
    private String uid;

    private UserOrderRecyclerAdapter(@NonNull FirestoreRecyclerOptions<Order> options) {
        super(options);
    }

    public UserOrderRecyclerAdapter(Context context, UserOrderRecyclerAdapterListener listener, String uid, Query query) {
        this(new FirestoreRecyclerOptions.Builder<Order>()
                .setQuery(query, new SnapshotParser<Order>() {
                    @NonNull
                    @Override
                    public Order parseSnapshot(@NonNull DocumentSnapshot snapshot) {
                        UserOrderDoc userOrderDoc = snapshot.toObject(UserOrderDoc.class);
                        return new Order(snapshot.getId(), userOrderDoc);
                    }
                })
                .build());
        this.context = context;
        this.listener = listener;
        this.uid = uid;
        coinUnits = context.getResources().getStringArray(R.array.coin_units);
        orderStatus = context.getResources().getStringArray(R.array.order_status);
        cloud = new UserOrderCloud();
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
        holder.itemView.setTag(order);
    }

    @Override
    public void onViewAttachedToWindow(@NonNull OrderHolder holder) {
        super.onViewAttachedToWindow(holder);
        Order order = (Order) holder.itemView.getTag();
        cloud.addUserOrderListener(uid, order.getMyName(), order.getGroupId(), order.getId(), holder);
    }

    @Override
    public void onViewDetachedFromWindow(@NonNull OrderHolder holder) {
        super.onViewDetachedFromWindow(holder);
        Order order = (Order) holder.itemView.getTag();
        cloud.removeUserOrderListener(order.getId());
    }

    public void removeAllListener() {
        cloud.removeAllListener();
    }

    @Override
    public boolean onMenuItemClick(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.order_menu_end:
                return true;
            default:
                return false;
        }
    }

    public interface UserOrderRecyclerAdapterListener {
        void onUserOrderItemViewClicked(Order order, String myName);
    }
}
