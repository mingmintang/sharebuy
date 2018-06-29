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
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.database.Query;
import com.mingmin.sharebuy.cloud.Member;
import com.mingmin.sharebuy.cloud.Order;

import java.util.HashMap;

public class OrderRecyclerAdapter extends FirebaseRecyclerAdapter<Order, OrderRecyclerAdapter.OrderHolder> implements PopupMenu.OnMenuItemClickListener {
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
    private User user;
    private HashMap<String, Member> members;
    private OrderRecyclerAdapterListener listener;

    private OrderRecyclerAdapter(@NonNull FirebaseRecyclerOptions<Order> options) {
        super(options);
    }

    public OrderRecyclerAdapter(Context context, OrderRecyclerAdapterListener listener, Query query, User user, HashMap<String, Member> members) {
        this(new FirebaseRecyclerOptions.Builder<Order>()
                .setQuery(query, Order.class)
                .build());
        this.context = context;
        this.listener = listener;
        coinUnits = context.getResources().getStringArray(R.array.coin_units);
        orderStatus = context.getResources().getStringArray(R.array.order_status);
        this.user = user;
        this.members = members;
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
                listener.onOrderItemViewClicked(order, members);
            }
        });
        holder.tvName.setText(order.getName());
        holder.tvDesc.setText(order.getDesc());
        holder.tvPrice.setText(String.valueOf(order.getPrice()));
        holder.tvCount.setText(String.valueOf(order.getBuyCount()));
        holder.calculateAmount();
        holder.tvCoinUnit.setText(coinUnits[order.getCoinUnit()]);
        String nickname = getNickname(order);
        if (nickname != null) {
            String status = nickname + orderStatus[order.getState()];
            holder.tvStatus.setText(status);
        }

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

    private String getNickname(Order order) {
        String nickname = null;
        switch (order.getState()) {
            case Order.STATE_CREATE:
                nickname = members.get(order.getCreatorUid()).getNickname();
                break;
            case Order.STATE_TAKE:
                nickname = members.get(order.getTakerUid()).getNickname();
                break;
            case Order.STATE_END:
                nickname = members.get(order.getTakerUid()).getNickname();
                break;
            case Order.STATE_CANCEL:
                if (order.getTakerUid() == null) {
                    nickname = members.get(order.getCreatorUid()).getNickname();
                } else {
                    nickname = members.get(order.getTakerUid()).getNickname();
                }
                break;
        }
        return nickname;
    }

    @Override
    public boolean onMenuItemClick(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.order_menu_buy:
//                BuyDialog.newInstance(5).show(((AppCompatActivity) context).getSupportFragmentManager(), "buyDialog");
                return true;
            default:
                return false;
        }
    }

    public interface OrderRecyclerAdapterListener {
        void onOrderItemViewClicked(Order order, HashMap<String, Member> members);
    }
}
