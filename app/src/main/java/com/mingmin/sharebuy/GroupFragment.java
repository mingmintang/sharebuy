package com.mingmin.sharebuy;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
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

import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.Query;
import com.mingmin.sharebuy.cloud.Clouds;
import com.mingmin.sharebuy.dialog.AddGroupDialog;
import com.mingmin.sharebuy.dialog.BuyOrderDialog;
import com.mingmin.sharebuy.dialog.ConfirmDialog;
import com.mingmin.sharebuy.dialog.JoinGroupDialog;

import java.util.ArrayList;

public class GroupFragment extends Fragment implements AddGroupDialog.AddGroupListener,
        PopupMenu.OnMenuItemClickListener, JoinGroupDialog.JoinGroupListener,
        ConfirmDialog.OnConfirmListener, OrderRecyclerAdapter.OrderRecyclerAdapterListener,
        BuyOrderDialog.BuyOrderListener, Clouds.UserGroupsListener{
    private final String TAG = getClass().getSimpleName();
    private User user;
    private OnFragmentInteractionListener mListener;
    private Spinner spinner;
    private Group currentGroup;
    private PopupMenu popupMenu;
    private FirestoreRecyclerAdapter<Order, OrderRecyclerAdapter.OrderHolder> recyclerAdapter;
    private RecyclerView recyclerView;
    private ImageButton ibManage;

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
        ibManage = view.findViewById(R.id.group_manage);
        spinner = view.findViewById(R.id.group_spinner);
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
        Clouds.getInstance().addUserGroupsListener(user.getUid(), this);
        if (recyclerAdapter != null) {
            recyclerAdapter.startListening();
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        Clouds.getInstance().removeUserGroupsListener();
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
                intent.putExtra("group", currentGroup);
                getActivity().startActivityForResult(intent, MainActivity.RC_GROUP_INFO);
                return true;
            case R.id.group_menu_exit:
                ConfirmDialog.newInstance(this,
                        "退出群組",
                        "確定退出 " + currentGroup.getName() +" 群組？",
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
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onUserGroupsChanged(ArrayList<Group> groups) {
        if (groups.size() > 0) {
            setupSpinner(groups);
            popupMenu.getMenu().setGroupVisible(R.id.group_menu_infoGroup, true);
            popupMenu.getMenu().setGroupVisible(R.id.group_menu_exitGroup, true);
        } else {
            popupMenu.getMenu().setGroupVisible(R.id.group_menu_infoGroup, false);
            popupMenu.getMenu().setGroupVisible(R.id.group_menu_exitGroup, false);
        }

    }

    @Override
    public void onJoinGroupConfirm(Group group, String myName) {
        Clouds.getInstance().requestJoinGroup(group, user.getUid(), myName)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Snackbar.make(getView(), "已送加入群組通知", Snackbar.LENGTH_LONG).show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Snackbar.make(getView(), "請求加入群組失敗，請檢查連線", Snackbar.LENGTH_LONG).show();
                    }
                });
    }

    @Override
    public void onAddGroupConfirm(String groupName, String managerName) {
        Clouds.getInstance().createNewGroup(groupName, user.getUid(), managerName)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Snackbar.make(getView(), "新增群組成功", Snackbar.LENGTH_LONG).show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Snackbar.make(getView(), "新增群組失敗", Snackbar.LENGTH_LONG).show();
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
        Clouds.getInstance().exitGroup(currentGroup.getId(), user.getUid())
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Snackbar.make(recyclerView, "退出群組成功", Snackbar.LENGTH_LONG).show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Snackbar.make(recyclerView, "退出群組失敗", Snackbar.LENGTH_LONG).show();
                    }
                });
    }

    private void setupSpinner(final ArrayList<Group> groups) {
        SpinnerAdapter adapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_list_item_1, groups);
        spinner.setAdapter(adapter);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                currentGroup = groups.get(position);
                setupRecyclerView();
                if (currentGroup.getManagerUid().equals(user.getUid())) {
                    ibManage.setEnabled(true);
                    ibManage.setImageResource(R.drawable.ic_group_setting);
                    ibManage.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Intent intent = new Intent(getContext(), GroupManageActivity.class);
                            intent.putExtra("group", currentGroup);
                            getActivity().startActivityForResult(intent, MainActivity.RC_GROUP_MANAGE);
                        }
                    });
                    popupMenu.getMenu().setGroupVisible(R.id.group_menu_exitGroup, false);
                } else {
                    ibManage.setEnabled(false);
                    ibManage.setImageResource(R.drawable.ic_group_setting_disabled);
                    popupMenu.getMenu().setGroupVisible(R.id.group_menu_exitGroup, true);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    private void setupRecyclerView() {
        Query query = Clouds.getInstance().getGroupOrdersQuery(currentGroup.getId());
        recyclerAdapter = new OrderRecyclerAdapter(getContext(), GroupFragment.this, currentGroup, query);
        recyclerView.setAdapter(recyclerAdapter);
        recyclerAdapter.startListening();
    }

    @Override
    public void onOrderItemViewClicked(Order order, Group group) {
        BuyOrderDialog.newInstance(this, order, group)
                .show(getFragmentManager(), "buyOrderDialog");
    }

    @Override
    public void onBuyOrderConfirm(Order order, Group group, int buyCount) {
        Clouds.getInstance().buyGroupOrder(group.getId(), order.getId(), user.getUid(), group.getMyName(), buyCount)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Snackbar.make(recyclerView, "購買成功", Snackbar.LENGTH_LONG).show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Snackbar.make(recyclerView, "購買失敗", Snackbar.LENGTH_LONG).show();
                    }
                });
    }
}
