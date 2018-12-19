package com.mingmin.sharebuy;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.firebase.firestore.Query;
import com.mingmin.sharebuy.cloud.Clouds;
import com.mingmin.sharebuy.cloud.StoreOrdersCloud;

public class UserEndOrdersFragment extends Fragment implements UserEndOrderRecyclerAdapter.UserEndOrderRecyclerAdapterListener {
    private User user;
    private OnFragmentInteractionListener mListener;
    private StoreOrdersCloud cloud;
    private UserEndOrderRecyclerAdapter adapter;

    public static UserEndOrdersFragment newInstance(User user) {
        Bundle args = new Bundle();
        args.putSerializable("user", user);
        UserEndOrdersFragment fragment = new UserEndOrdersFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        user = (User) getArguments().getSerializable("user");
        cloud = new StoreOrdersCloud(getContext());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_user_end_orders, container, false);
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
        RecyclerView recyclerView = view.findViewById(R.id.user_end_orders_recyclerView);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        Query query = Clouds.getInstance().getUserEndOrdersQuery(user.getUid());
        adapter = new UserEndOrderRecyclerAdapter(getContext(), this, user.getUid(), query);
        recyclerView.setAdapter(adapter);
        adapter.startListening();
    }

    @Override
    public void onUserEndOrderItemViewClicked(Order order) {

    }
}
