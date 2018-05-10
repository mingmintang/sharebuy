package com.mingmin.sharebuy;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.PopupMenu;
import android.widget.SimpleAdapter;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;

import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.FirebaseDatabase;

public class GroupFragment extends Fragment implements AddGroupDialog.OnAddGroupListener {
    private static GroupFragment fragment;
    private FirebaseUser fuser;
    private OnFragmentInteractionListener mListener;
    private SpinnerAdapter adapter;
    private Spinner spinner;

    public static GroupFragment getInstance(FirebaseUser fuser) {
        if (fragment == null) {
            fragment = new GroupFragment();
        }
        fragment.fuser = fuser;

        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_group, container, false);
        initViews(view);
        return view;
    }

    private void initViews(View view) {
        Button btnJoinGroup = view.findViewById(R.id.group_join);
        Button btnAddGroup = view.findViewById(R.id.group_add);
        Button btnExitGroup = view.findViewById(R.id.group_exit);

        btnJoinGroup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PopupMenu menu = new PopupMenu(getContext(), v);
                menu.show();
            }
        });

        btnAddGroup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AddGroupDialog.getInstance(GroupFragment.this)
                        .show(getFragmentManager(), "ADD_GROUP");
            }
        });

//        spinner = view.findViewById(R.id.group_spinner);
//        adapter = new ArrayAdapter<Group>(getContext(),
//                android.R.layout.simple_list_item_1)
//        spinner.setAdapter(adapter);
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

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }

    @Override
    public void onConfirm(String groupName) {
        FirebaseDatabase fdb = FirebaseDatabase.getInstance();
        String groupId = fdb.getReference("groups")
                .push()
                .getKey();
        Group group = new Group(groupId, groupName, fuser.getUid(), fuser.getDisplayName());
        fdb.getReference("groups")
                .child(group.getId())
                .child("group")
                .setValue(group);

        User user = new User(fuser.getUid(), fuser.getDisplayName());
        fdb.getReference("groups")
                .child(group.getId())
                .child("users")
                .child(user.getUid())
                .setValue(user);

        fdb.getReference("users")
                .child(user.getUid())
                .child("groups")
                .child(group.getId())
                .setValue(group);


    }
}
