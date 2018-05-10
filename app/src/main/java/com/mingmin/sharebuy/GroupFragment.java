package com.mingmin.sharebuy;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.PopupMenu;
import android.widget.SimpleAdapter;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;

import com.firebase.ui.database.FirebaseListAdapter;
import com.firebase.ui.database.FirebaseListOptions;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class GroupFragment extends Fragment implements AddGroupDialog.OnAddGroupListener, PopupMenu.OnMenuItemClickListener {
    private final String TAG = getClass().getSimpleName();
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
        ImageButton ibEdit = view.findViewById(R.id.group_edit);
        final PopupMenu popup = new PopupMenu(getContext(), ibEdit);
        popup.getMenuInflater().inflate(R.menu.fragment_group_edit, popup.getMenu());
        popup.setOnMenuItemClickListener(this);
        ibEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                popup.show();
            }
        });

        spinner = view.findViewById(R.id.group_spinner);
        final ArrayList<Group> list = new ArrayList<>();
        Query query = FirebaseDatabase.getInstance()
                .getReference("users")
                .child(fuser.getUid())
                .child("groups")
                .orderByChild("createdTime");
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot item : dataSnapshot.getChildren()) {
                    Group group = item.getValue(Group.class);
                    list.add(group);
                    adapter = new ArrayAdapter<Group>(getActivity(), android.R.layout.simple_list_item_1, list);
                    spinner.setAdapter(adapter);
                    if (adapter.getCount() > 0) {
                        spinner.setSelection(0);
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e(TAG, databaseError.getMessage());
            }
        });

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
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
    public boolean onMenuItemClick(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.group_edit_join:
                return true;
            case R.id.group_edit_add:
                AddGroupDialog.getInstance(GroupFragment.this)
                        .show(getFragmentManager(), "ADD_GROUP");
                return true;
            case R.id.group_edit_exit:
                return true;
            default:
                return false;
        }
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
