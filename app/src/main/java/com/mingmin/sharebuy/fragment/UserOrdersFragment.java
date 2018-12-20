package com.mingmin.sharebuy.fragment;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.Query;
import com.mingmin.sharebuy.R;
import com.mingmin.sharebuy.adapter.UserOrderRecyclerAdapter;
import com.mingmin.sharebuy.cloud.Clouds;
import com.mingmin.sharebuy.dialog.ConfirmDialog;
import com.mingmin.sharebuy.item.Order;
import com.mingmin.sharebuy.item.User;

public class UserOrdersFragment extends Fragment implements UserOrderRecyclerAdapter.UserOrderRecyclerAdapterListener,
        ConfirmDialog.ConfirmListener {
    private User user;
    private UserOrderRecyclerAdapter adapter;
    private OnFragmentInteractionListener mListener;
    private RecyclerView recyclerView;

    public static UserOrdersFragment newInstance(User user) {
        Bundle args = new Bundle();
        args.putSerializable("user", user);
        UserOrdersFragment fragment = new UserOrdersFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        user = (User) getArguments().getSerializable("user");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_user_orders, container, false);
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
            adapter.removeAllListener();
        }
    }


    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }

    private void setupRecyclerView(View view) {
        recyclerView = view.findViewById(R.id.user_orders_recyclerView);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        Query query = Clouds.getInstance().getUserOrdersQuery(user.getUid());
        adapter = new UserOrderRecyclerAdapter(getContext(), this, user, query);
        recyclerView.setAdapter(adapter);
        adapter.startListening();
    }

    class ConfirmTag {
        String tag;
        Object object;
        public ConfirmTag(String tag, @Nullable Object object) {
            this.tag = tag;
            this.object = object;
        }
    }

    @Override
    public void onConfirm(Object tag) {
        UserOrdersFragment.ConfirmTag confirmTag = (UserOrdersFragment.ConfirmTag) tag;
        switch (confirmTag.tag) {
            case "end_group_order":
                Order order = (Order) confirmTag.object;
                endGroupOrder(order);
                break;
        }
    }

    private void endGroupOrder(Order order) {
        Clouds.getInstance().endGroupOrder(order.getGroupId(), order.getId(), user.getUid())
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Snackbar.make(recyclerView, "結單成功", Snackbar.LENGTH_LONG).show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Snackbar.make(recyclerView, "結單失敗", Snackbar.LENGTH_LONG).show();
                    }
                });
    }

    @Override
    public void onUserOrderItemViewClicked(Order order, String myName) {

    }

    @Override
    public void onUserOrderMenuEndClicked(Order order) {
        UserOrdersFragment.ConfirmTag tag = new UserOrdersFragment.ConfirmTag("end_group_order", order);
        ConfirmDialog.newInstance(this, "結單", order.getName() + " 確定結單？", tag)
                .show(getFragmentManager(), "endOrderDialog");
    }
}
