package com.mingmin.sharebuy;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.PopupMenu;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.mingmin.sharebuy.cloud.CloudActions;
import com.mingmin.sharebuy.cloud.Fdb;
import com.mingmin.sharebuy.cloud.Group;
import com.mingmin.sharebuy.cloud.Member;
import com.mingmin.sharebuy.cloud.Order;
import com.mingmin.sharebuy.dialog.AddGroupDialog;
import com.mingmin.sharebuy.dialog.BuyOrderDialog;
import com.mingmin.sharebuy.dialog.ConfirmDialog;
import com.mingmin.sharebuy.dialog.JoinGroupDialog;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;

public class GroupFragment extends Fragment implements AddGroupDialog.AddGroupListener,
        PopupMenu.OnMenuItemClickListener, JoinGroupDialog.JoinGroupListener,
        ConfirmDialog.OnConfirmListener, OrderRecyclerAdapter.OrderRecyclerAdapterListener,
        BuyOrderDialog.BuyOrderListener {
    private final String TAG = getClass().getSimpleName();
    private User user;
    private OnFragmentInteractionListener mListener;
    private Spinner spinner;
    private DatabaseReference groupsRef;
    private GroupsValueEventListener groupsValueEventListener;
    private ArrayList<Group> groups = new ArrayList<>();
    private PopupMenu popupMenu;
    private Group group;
    private FirebaseRecyclerAdapter<Order, OrderRecyclerAdapter.OrderHolder> recyclerAdapter;
    private RecyclerView recyclerView;

    public static GroupFragment newInstance(User user) {
        Bundle args = new Bundle();
        args.putSerializable("user", user);
        GroupFragment fragment = new GroupFragment();
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
        View view = inflater.inflate(R.layout.fragment_group, container, false);
        initViews(view);
        return view;
    }

    private void initViews(View view) {
        recyclerView = view.findViewById(R.id.group_recyclerView);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        ImageButton ibMenu = view.findViewById(R.id.group_menu);
        popupMenu = new PopupMenu(getContext(), ibMenu);
        popupMenu.getMenuInflater().inflate(R.menu.fragment_group_menu, popupMenu.getMenu());
        popupMenu.setOnMenuItemClickListener(this);
        ibMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                popupMenu.show();
            }
        });

        final ImageButton ibManage = view.findViewById(R.id.group_manage);

        spinner = view.findViewById(R.id.group_spinner);
//        groupsValueEventListener = new GroupsValueEventListener();
//        groupsRef = Fdb.getUserGroupsRef(user.getUid());
//        groupsRef.addValueEventListener(groupsValueEventListener);
//        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
//            @Override
//            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
//                group = groups.get(position);
//                setupRecyclerView(group);
//                if (group.getFounderUid().equals(user.getUid())) {
//                    ibManage.setEnabled(true);
//                    ibManage.setImageResource(R.drawable.ic_group_setting);
//                    ibManage.setOnClickListener(new View.OnClickListener() {
//                        @Override
//                        public void onClick(View v) {
//                            Intent intent = new Intent(getContext(), GroupManageActivity.class);
//                            intent.putExtra("group", group);
//                            getActivity().startActivityForResult(intent, MainActivity.RC_GROUP_MANAGE);
//                        }
//                    });
//                    popupMenu.getMenu().setGroupVisible(R.id.group_menu_exitGroup, false);
//                } else {
//                    ibManage.setEnabled(false);
//                    ibManage.setImageResource(R.drawable.ic_group_setting_disabled);
//                    popupMenu.getMenu().setGroupVisible(R.id.group_menu_exitGroup, true);
//                }
//            }
//
//            @Override
//            public void onNothingSelected(AdapterView<?> parent) {
//
//            }
//        });
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
    public void onStart() {
        super.onStart();
        if (recyclerAdapter != null) {
            recyclerAdapter.startListening();
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        if (recyclerAdapter != null) {
            recyclerAdapter.stopListening();
        }
    }

    @Override
    public boolean onMenuItemClick(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.group_menu_join:
                JoinGroupDialog.newInstance(GroupFragment.this)
                        .show(getFragmentManager(), "JOIN_GROUP");
                return true;
            case R.id.group_menu_add:
                AddGroupDialog.newInstance(GroupFragment.this)
                        .show(getFragmentManager(), "ADD_GROUP");
                return true;
            case R.id.group_menu_info:
                Intent intent = new Intent(getContext(), GroupInfoActivity.class);
                intent.putExtra("group", groups.get(spinner.getSelectedItemPosition()));
                getActivity().startActivityForResult(intent, MainActivity.RC_GROUP_INFO);
                return true;
            case R.id.group_menu_exit:
                ConfirmDialog.newInstance(this,
                        "退出群組",
                        "確定退出 " + group.getName() +" 群組？",
                        "exit_group")
                        .show(getFragmentManager(), "exit_group");
                return true;
            default:
                return false;
        }
    }

    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.d(TAG, "onActivityResult: ");
        super.onActivityResult(requestCode, resultCode, data);
    }

    class GroupsValueEventListener implements ValueEventListener {
        @Override
        public void onDataChange(DataSnapshot dataSnapshot) {
            final HashMap<String, Boolean> groupIds = (HashMap<String, Boolean>) dataSnapshot.getValue();
            if (groupIds != null && !groupIds.isEmpty()) {
                groups.clear();
                for (String groupId : groupIds.keySet()) {
                    Fdb.getGroupRef(groupId)
                            .addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    Group group = dataSnapshot.getValue(Group.class);
                                    groups.add(group);
                                    if (groups.size() == groupIds.size()) {
                                        Collections.sort(groups, new Comparator<Group>() {
                                            @Override
                                            public int compare(Group o1, Group o2) {
                                                return (int) (o2.getCreatedTime() - o1.getCreatedTime());
                                            }
                                        });
                                        SpinnerAdapter adapter = new ArrayAdapter<Group>(getActivity(), android.R.layout.simple_list_item_1, groups);
                                        spinner.setAdapter(adapter);
                                        popupMenu.getMenu().setGroupVisible(R.id.group_menu_infoGroup, true);
                                        popupMenu.getMenu().setGroupVisible(R.id.group_menu_exitGroup, true);
                                    }
                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {

                                }
                            });
                }
            } else {
                popupMenu.getMenu().setGroupVisible(R.id.group_menu_infoGroup, false);
                popupMenu.getMenu().setGroupVisible(R.id.group_menu_exitGroup, false);
            }
        }

        @Override
        public void onCancelled(DatabaseError databaseError) {

        }
    }

    @Override
    public void onJoinGroupConfirm(Group group) {
        CloudActions.requestJoinGroup(group, user.getUid());
    }

    @Override
    public void onAddGroupConfirm(String groupName) {
        CloudActions.createNewGroup(groupName, user).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Snackbar.make(getView(), "新增群組成功", Snackbar.LENGTH_LONG).show();
            }
        });
    }

    @Override
    public void onConfirm(Object tag) {
        String tagStr = (String) tag;
        switch (tagStr) {
            case "exit_group":
                exitGroup();
                break;
        }
    }

    private void exitGroup() {
        CloudActions.exitGroup(group.getId(), user.getUid()).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Snackbar.make(getView(), "退出群組成功", Snackbar.LENGTH_LONG).show();
            }
        });
    }

    private void setupRecyclerView(Group group) {
//        final Query query = Fdb.getGroupOrdersRef(group.getId())
//                .orderByChild("nCreateTime")
//                .limitToFirst(30);
//
//        CloudActions.readGroupMembers(group.getId()).addOnSuccessListener(new OnSuccessListener<HashMap<String, Member>>() {
//            @Override
//            public void onSuccess(HashMap<String, Member> members) {
//                recyclerAdapter = new OrderRecyclerAdapter(getContext(), GroupFragment.this, query, user, members);
//                recyclerView.setAdapter(recyclerAdapter);
//                recyclerAdapter.startListening();
//            }
//        });
    }

    @Override
    public void onOrderItemViewClicked(Order order, HashMap<String, Member> members) {
        BuyOrderDialog.newInstance(this, order, members)
                .show(getFragmentManager(), "buyOrderDialog");
    }

    @Override
    public void onBuyOrderConfirm(Order order, int buyCount) {
        CloudActions.buyGroupOrder(group.getId(), order.getId(), user.getUid(), buyCount)
                .addOnSuccessListener(new OnSuccessListener<Boolean>() {
                    @Override
                    public void onSuccess(Boolean aBoolean) {
                        if (aBoolean) {
                            Snackbar.make(recyclerView, "購買成功", Snackbar.LENGTH_LONG).show();
                        } else {
                            Snackbar.make(recyclerView, "購買失敗", Snackbar.LENGTH_LONG).show();
                        }
                    }
                });
    }
}
