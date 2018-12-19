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
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.Query;
import com.mingmin.sharebuy.cloud.Clouds;
import com.mingmin.sharebuy.cloud.GroupOrderDoc;

public class OrderRecyclerAdapter extends FirestoreRecyclerAdapter<Order, OrderRecyclerAdapter.OrderHolder> {

    private final String TAG = getClass().getSimpleName();

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
            ibMenu.setVisibility(View.INVISIBLE);
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
    private User user;

    private OrderRecyclerAdapter(@NonNull FirestoreRecyclerOptions<Order> options) {
        super(options);
    }

    public OrderRecyclerAdapter(Context context, OrderRecyclerAdapterListener listener, Group group, User user, Query query) {
        this(new FirestoreRecyclerOptions.Builder<Order>()
                .setQuery(query, new SnapshotParser<Order>() {
                    @NonNull
                    @Override
                    public Order parseSnapshot(@NonNull DocumentSnapshot snapshot) {
                        GroupOrderDoc groupOrderDoc = snapshot.toObject(GroupOrderDoc.class);
                        return new Order(snapshot.getId(), groupOrderDoc);
                    }
                })
                .build());
        this.context = context;
        this.listener = listener;
        this.group = group;
        this.user = user;
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
    protected void onBindViewHolder(@NonNull final OrderHolder holder, int position, @NonNull final Order order) {
        if (user.getUid().equals(order.getManagerUid())) {
            if (order.getState() == Order.STATE_TAKE) {
                holder.ibMenu.setVisibility(View.VISIBLE);
                holder.popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        switch (item.getItemId()) {
                            case R.id.order_menu_end:
                                listener.onOrderMenuEndClicked(order);
                                return true;
                            default:
                                return false;
                        }
                    }
                });
            }
            holder.tvStatus.setText(order.getManagerName() + "接單");
        } else {
            Clouds.getInstance().checkOrderBuyerExist(order.getGroupId(), order.getId(), user.getUid())
                    .addOnSuccessListener(new OnSuccessListener<Boolean>() {
                        @Override
                        public void onSuccess(Boolean aBoolean) {
                            String status;
                            if (aBoolean) {
                                status = order.getManagerName() + "接單\n已下單購買";
                            } else {
                                holder.itemView.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        listener.onOrderItemViewClicked(order, group);
                                    }
                                });
                                status = order.getManagerName() + orderStatus[order.getState()];
                            }
                            holder.tvStatus.setText(status);
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.d(TAG, "onFailure: " + e.getMessage());
                        }
                    });
        }

        holder.tvName.setText(order.getName());
        holder.tvDesc.setText(order.getDesc());
        holder.tvPrice.setText(String.valueOf(order.getPrice()));
        holder.tvCount.setText(String.valueOf(order.getBuyCount()));
        holder.calculateAmount();
        holder.tvCoinUnit.setText(coinUnits[order.getCoinUnit()]);

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

    public interface OrderRecyclerAdapterListener {
        void onOrderItemViewClicked(Order order, Group group);
        void onOrderMenuEndClicked(Order order);
    }
}
