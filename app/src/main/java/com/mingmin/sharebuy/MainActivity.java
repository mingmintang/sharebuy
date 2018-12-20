package com.mingmin.sharebuy;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.firebase.ui.auth.AuthUI;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.iid.FirebaseInstanceId;
import com.mingmin.sharebuy.cloud.Clouds;
import com.mingmin.sharebuy.fragment.GroupsFragment;
import com.mingmin.sharebuy.fragment.UserEndOrdersFragment;
import com.mingmin.sharebuy.fragment.UserOrdersFragment;
import com.mingmin.sharebuy.item.Group;
import com.mingmin.sharebuy.item.GroupOrderResult;
import com.mingmin.sharebuy.item.PersonalOrderResult;
import com.mingmin.sharebuy.item.User;

import java.util.Arrays;

public class MainActivity extends AppCompatActivity implements FirebaseAuth.AuthStateListener,
        NavigationView.OnNavigationItemSelectedListener,
        UserOrdersFragment.OnFragmentInteractionListener,
        UserEndOrdersFragment.OnFragmentInteractionListener,
        GroupsFragment.OnFragmentInteractionListener {

    public static final int RC_SIGN_IN = 1;
    public static final int RC_ADD_ORDER = 2;
    public static final int RC_GROUP_MANAGE = 3;
    public static final int RC_GROUP_INFO = 4;
    public static final int TYPE_GROUP_ORDER = 10;
    public static final int TYPE_PERSONAL_ORDER = 11;
    public static final int TYPE_BACK_PRESSED = 12;
    private final String TAG = getClass().getSimpleName();
    private FirebaseUser fuser;
    private DrawerLayout drawer;
    private TextView tvAccount;
    private NavigationView navigationView;
    private FrameLayout fragment;
    private ProgressBar loading;
    private FloatingActionButton fab;
    private Toolbar toolbar;
    private FirebaseAuth firebaseAuth;
    private User user;
    private FragmentManager fm;
    private int backToNavItemId = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
    }

    private void initView() {
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, AddOrderActivity.class);
                intent.putExtra("user", user);
                startActivityForResult(intent, RC_ADD_ORDER);
            }
        });

        drawer = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toogle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toogle);
        toogle.syncState();

        navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        tvAccount = navigationView.getHeaderView(0).findViewById(R.id.main_nav_userAccount);

        fragment = findViewById(R.id.frame_layout);
        loading = findViewById(R.id.loading);
    }

    @Override
    protected void onStart() {
        super.onStart();
        firebaseAuth = FirebaseAuth.getInstance();
        firebaseAuth.addAuthStateListener(this);
        fm = getSupportFragmentManager();
    }

    @Override
    protected void onStop() {
        super.onStop();
        firebaseAuth.removeAuthStateListener(this);
        saveCurrentNavItemId();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            if (getCurrentNavigationItem() != R.id.nav_group) {
                goToNavItemHome();
            } else {
                super.onBackPressed();
            }
        }
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id == getCurrentNavigationItem()) {
            drawer.closeDrawer(GravityCompat.START);
            return false;
        }
        switch (id) {
            case R.id.nav_group:
                switchGroupsFragment();
                break;
            case R.id.nav_order:
                switchUserOrdersFragment();
                break;
            case R.id.nav_order_ended:
                switchUserEndOrdersFragment();
                break;
        }
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private void saveCurrentNavItemId() {
        backToNavItemId = navigationView.getId();
    }

    private void switchUserOrdersFragment() {
        fm.beginTransaction()
                .replace(R.id.frame_layout, UserOrdersFragment.newInstance(user))
                .commit();
        toolbar.setTitle(R.string.order);
    }

    private void switchUserEndOrdersFragment() {
        fm.beginTransaction()
                .replace(R.id.frame_layout, UserEndOrdersFragment.newInstance(user))
                .commit();
        toolbar.setTitle(R.string.order_ended);
    }

    private void switchGroupsFragment() {
        fm.beginTransaction()
                .replace(R.id.frame_layout, GroupsFragment.newInstance(user))
                .commit();
        toolbar.setTitle(R.string.group);
    }

    private int getCurrentNavigationItem() {
        for (int i = 0; i < navigationView.getMenu().size(); i++) {
            MenuItem item = navigationView.getMenu().getItem(i);
            if (item.isChecked()) {
                return item.getItemId();
            }
        }
        return  -1;
    }

    @Override
    public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
        fuser = firebaseAuth.getCurrentUser();
        if (fuser == null) {
            startActivityForResult(AuthUI.getInstance().createSignInIntentBuilder()
                    .setAvailableProviders(Arrays.asList(
                            new AuthUI.IdpConfig.GoogleBuilder().build()
                    )).build(),
                    RC_SIGN_IN);
        } else {
            initAfterSignIn();
        }
    }

    private void initAfterSignIn() {
        user = new User(fuser.getUid());
        tvAccount.setText(fuser.getEmail());
        Clouds.getInstance().initUserData(fuser, FirebaseInstanceId.getInstance().getToken())
                .addOnSuccessListener(new OnSuccessListener<Clouds.InitUserDataResult>() {
                    @Override
                    public void onSuccess(Clouds.InitUserDataResult initUserDataResult) {
                        switch (initUserDataResult.status) {
                            case Clouds.InitUserDataResult.STATUS_FIRST_LOGIN:
                            case Clouds.InitUserDataResult.STATUS_UPDATE_TOKEN:
                            case Clouds.InitUserDataResult.STATUS_NO_UPDATE:
                                if (!goToGroupManage() && !backToNavItemByFlag()) {
                                    goToNavItemHome();
                                }
                                break;
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d(TAG, "onFailure: " + e.getMessage());
                    }
                });
    }

    private void goToNavItemHome() {
        goToNavItem(R.id.nav_group);
    }

    private void goToNavItem(int itemId) {
        navigationView.getMenu().performIdentifierAction(itemId, 0);
        navigationView.setCheckedItem(itemId);
    }

    private boolean backToNavItemByFlag(int navItemId) {
        backToNavItemId = navItemId;
        return backToNavItemByFlag();
    }

    private boolean backToNavItemByFlag() {
        if (backToNavItemId != 0) {
            goToNavItem(backToNavItemId);
            backToNavItemId = 0;
            return true;
        }
        return false;
    }

    private boolean goToGroupManage() {
        boolean goToGroupManage = getIntent().getBooleanExtra("goToGroupManage", false);
        getIntent().putExtra("goToGroupManage", false);
        if (goToGroupManage) {
            Group group = (Group) getIntent().getSerializableExtra("group");
            goToNavItem(R.id.nav_group);
            Intent intent = new Intent(MainActivity.this, GroupManageActivity.class);
            intent.putExtra("group", group);
            intent.putExtra("selectedItemId", R.id.group_manage_nav_joining);
            startActivityForResult(intent, MainActivity.RC_GROUP_MANAGE);
            return true;
        }
        return false;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case RC_SIGN_IN:
                if (resultCode == RESULT_OK) {
                    initAfterSignIn();
                } else {
                    finishAfterTransition();
                }
                break;
            case RC_ADD_ORDER:
                if (resultCode == RESULT_OK) {
                    showLoading(true);
                    int type = data.getIntExtra("type", 0);
                    switch (type) {
                        case TYPE_GROUP_ORDER:
                            GroupOrderResult groupOrderResult = (GroupOrderResult) data.getSerializableExtra("result");
                            buildGroupOrder(groupOrderResult);
                            break;
                        case TYPE_PERSONAL_ORDER:
                            PersonalOrderResult personalOrderResult = (PersonalOrderResult) data.getSerializableExtra("result");
                            buildPersonalOrder(personalOrderResult);
                            break;
                        case TYPE_BACK_PRESSED:
                            backToNavItemByFlag();
                            break;
                    }
                }
                break;
            case RC_GROUP_MANAGE:
                if (resultCode == RESULT_OK) {
                    backToNavItemId = R.id.nav_group;
                }
                break;
            case RC_GROUP_INFO:
                if (resultCode == RESULT_OK) {
                    backToNavItemId = R.id.nav_group;
                }
                break;
        }
    }

    private void showLoading(boolean isLoading) {
        if (isLoading) {
            loading.setVisibility(View.VISIBLE);
            fragment.setVisibility(View.GONE);
            fab.hide();
            toolbar.setVisibility(View.GONE);
        } else {
            loading.setVisibility(View.GONE);
            fragment.setVisibility(View.VISIBLE);
            fab.show();
            toolbar.setVisibility(View.VISIBLE);
        }
    }

    private void buildGroupOrder(GroupOrderResult result) {
        Clouds.getInstance().buildGroupOrder(result.orderState, result.imagePath, result.uid,
                result.groupOrderDoc, result.group)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        backToNavItemByFlag(R.id.nav_group);
                        showLoading(false);
                        if (task.isSuccessful()) {
                            Snackbar.make(navigationView, "建立訂單成功", Snackbar.LENGTH_LONG).show();
                        } else {
                            Snackbar.make(navigationView, "建立訂單失敗", Snackbar.LENGTH_LONG).show();
                        }
                    }
                });
    }

    private void buildPersonalOrder(PersonalOrderResult result) {
        Clouds.getInstance().buildPersonalOrder(result.personalOrderDoc, result.imagePath, result.uid)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        backToNavItemByFlag(R.id.nav_order_ended);
                        showLoading(false);
                        if (task.isSuccessful()) {
                            Snackbar.make(navigationView, "建立訂單成功", Snackbar.LENGTH_LONG).show();
                        } else {
                            Snackbar.make(navigationView, "建立訂單失敗", Snackbar.LENGTH_LONG).show();
                        }
                    }
                });
    }

    @Override
    public void onFragmentInteraction(Uri uri) {
        Log.d(TAG, "onFragmentInteraction: " + uri.getPath());
    }
}
