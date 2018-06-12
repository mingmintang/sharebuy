package com.mingmin.sharebuy;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;

public class OrderListFragment extends Fragment {
    private static OrderListFragment fragment;
    private User user;
    private FirebaseRecyclerAdapter<Order, OrderHolder> adapter;
    private OnFragmentInteractionListener mListener;

    public static OrderListFragment getInstance(User user) {
        if (fragment == null) {
            fragment = new OrderListFragment();
        }
        fragment.user = user;
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_order_list, container, false);
        setupRecyclerView(view);
        return view;
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }

    private void setupRecyclerView(View view) {
        RecyclerView recyclerView = view.findViewById(R.id.order_list_recyclerView);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        Query query = FirebaseDatabase.getInstance()
                .getReference("users")
                .child(user.getUid())
                .child("orders")
                .orderByChild("nStartTime")
                .limitToFirst(30);

        final FirebaseRecyclerOptions<Order> options = new FirebaseRecyclerOptions.Builder<Order>()
                .setQuery(query, Order.class)
                .build();
        adapter = new FirebaseRecyclerAdapter<Order, OrderHolder>(options) {
            String[] coinUnits = getResources().getStringArray(R.array.coin_units);
            String[] orderStates = getResources().getStringArray(R.array.order_states);

            @NonNull
            @Override
            public OrderHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(getContext())
                        .inflate(R.layout.row_order, parent, false);
                return new OrderHolder(view);
            }

            @Override
            protected void onBindViewHolder(@NonNull OrderHolder holder, int position, @NonNull final Order order) {
                holder.tvName.setText(order.getName());
                holder.tvDesc.setText(order.getDesc());
                holder.tvPrice.setText(String.valueOf(order.getPrice()));
                holder.tvCount.setText(String.valueOf(order.getBuyCount()));
                holder.calculateAmount();
                holder.tvCoinUnit.setText(coinUnits[order.getCoinUnit()]);
                holder.tvState.setText(orderStates[order.getState()]);

                RequestOptions requestOptions = new RequestOptions()
                        .centerCrop()
                        .override(300, 300)
                        .placeholder(R.drawable.ic_downloading)
                        .error(R.drawable.ic_alert);
                Glide.with(OrderListFragment.this)
                        .load(order.getImageUrl())
                        .apply(requestOptions)
                        .into(holder.imageView);
            }
        };

        recyclerView.setAdapter(adapter);
        adapter.startListening();
    }

    @Override
    public void onStart() {
        super.onStart();
        if (adapter != null) {
            adapter.startListening();
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        if (adapter != null) {
            adapter.stopListening();
        }
    }

}
