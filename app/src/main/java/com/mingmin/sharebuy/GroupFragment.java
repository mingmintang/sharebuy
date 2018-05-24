package com.mingmin.sharebuy;

import android.content.Context;
import android.database.DataSetObserver;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
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
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

public class GroupFragment extends Fragment implements AddGroupDialog.OnAddGroupListener, PopupMenu.OnMenuItemClickListener, JoinGroupDialog.OnJoinGroupListener {
    private final String TAG = getClass().getSimpleName();
    private static GroupFragment fragment;
    private FirebaseUser fuser;
    private OnFragmentInteractionListener mListener;
    private SpinnerAdapter adapter;
    private Spinner spinner;
    private FirebaseDatabase fdb;
    private DatabaseReference groupsRef;
    private GroupsValueEventListener groupsValueEventListener;

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
        fdb = FirebaseDatabase.getInstance();
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
        groupsValueEventListener = new GroupsValueEventListener();
        groupsRef = fdb.getReference("users")
                .child(fuser.getUid())
                .child("groups");
        groupsRef.addValueEventListener(groupsValueEventListener);
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
    public void onDestroyView() {
        super.onDestroyView();
        groupsRef.removeEventListener(groupsValueEventListener);
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
                JoinGroupDialog.getInstance(GroupFragment.this)
                        .show(getFragmentManager(), "JOIN_GROUP");
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

    class GroupsValueEventListener implements ValueEventListener {
        private ArrayList<Group> groups = new ArrayList<>();
        @Override
        public void onDataChange(DataSnapshot dataSnapshot) {
            final HashMap<String, Boolean> groupIds = (HashMap<String, Boolean>) dataSnapshot.getValue();
            if (groupIds != null && !groupIds.isEmpty()) {
                groups.clear();
                for (String groupId : groupIds.keySet()) {
                    Log.d(TAG, "onDataChange: " + groupId);
                    final DatabaseReference ref = fdb.getReference("groups").child(groupId);
                    ref.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            Group group = dataSnapshot.getValue(Group.class);
                            Log.d(TAG, "group: " + group.toString());
                            groups.add(group);
                            ref.removeEventListener(this);
                            if (groups.size() == groupIds.size()) {
                                Collections.sort(groups, new Comparator<Group>() {
                                    @Override
                                    public int compare(Group o1, Group o2) {
                                        return (int) (o2.getCreatedTime() - o1.getCreatedTime());
                                    }
                                });
                                adapter = new ArrayAdapter<Group>(getActivity(), android.R.layout.simple_list_item_1, groups);
                                spinner.setAdapter(adapter);
                            }
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });
                }
            }
        }

        @Override
        public void onCancelled(DatabaseError databaseError) {

        }
    }

    @Override
    public void onJoinGroupConfirm(Group group) {

    }

    @Override
    public void onAddGroupConfirm(String groupName) {
        String groupId = fdb.getReference("groups")
                .push()
                .getKey();
        final Group group = new Group(groupId, groupName, fuser.getUid());
        fdb.getReference("groups")
                .child(group.getId())
                .setValue(group)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        fdb.getReference("groups")
                                .child(group.getId())
                                .child("users")
                                .child(group.getFounderUid())
                                .setValue(true)
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        fdb.getReference("users")
                                                .child(group.getFounderUid())
                                                .child("groups")
                                                .child(group.getId())
                                                .setValue(true)
                                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                    @Override
                                                    public void onSuccess(Void aVoid) {
                                                        Snackbar.make(getView(), "新增群組成功", Snackbar.LENGTH_LONG).show();
                                                    }
                                                });
                                    }
                                });
                    }
                });
    }
}
