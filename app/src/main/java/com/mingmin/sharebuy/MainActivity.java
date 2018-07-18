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
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.firebase.ui.auth.AuthUI;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.iid.FirebaseInstanceId;
import com.mingmin.sharebuy.cloud.Clouds;

import java.util.Arrays;

public class MainActivity extends AppCompatActivity implements FirebaseAuth.AuthStateListener,
        NavigationView.OnNavigationItemSelectedListener,
        UserOrdersFragment.OnFragmentInteractionListener,
        OrderHistoryFragment.OnFragmentInteractionListener,
        GroupFragment.OnFragmentInteractionListener {

    public static final int RC_SIGN_IN = 1;
    public static final int RC_ADD_ORDER = 2;
    public static final int RC_GROUP_MANAGE = 3;
    public static final int RC_GROUP_INFO = 4;
    private final String TAG = getClass().getSimpleName();
    private FirebaseUser fuser;
    private DrawerLayout drawer;
    private TextView tvAccount;
    private NavigationView navigationView;
    private FirebaseAuth firebaseAuth;
    private User user;
    private FragmentManager fm;
    private int backToNavItemId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
    }

    private void initView() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = findViewById(R.id.fab);
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
            if (getCurrentNavigationItem() != R.id.nav_order) {
                goToNavItemHome();
            } else {
                super.onBackPressed();
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
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
            case R.id.nav_order:
                fm.beginTransaction()
                        .replace(R.id.frame_layout, UserOrdersFragment.newInstance(user))
                        .commit();
                break;
            case R.id.nav_order_history:
                fm.beginTransaction()
                        .replace(R.id.frame_layout, OrderHistoryFragment.newInstance(user))
                        .commit();
                break;
            case R.id.nav_group:
                fm.beginTransaction()
                        .replace(R.id.frame_layout, GroupFragment.newInstance(user))
                        .commit();
                break;
        }
        drawer.closeDrawer(GravityCompat.START);
        return true;
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
        goToNavItem(R.id.nav_order);
    }

    private void goToNavItem(int itemId) {
        navigationView.getMenu().performIdentifierAction(itemId, 0);
        navigationView.setCheckedItem(itemId);
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
                }
                break;
            case RC_ADD_ORDER:
                if (resultCode == RESULT_OK) {
                    Snackbar.make(navigationView, "建立訂單成功", Snackbar.LENGTH_LONG).show();
                }
                if (resultCode == RESULT_CANCELED) {
                    Snackbar.make(navigationView, "建立訂單失敗", Snackbar.LENGTH_LONG).show();
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

    @Override
    public void onFragmentInteraction(Uri uri) {
        Log.d(TAG, "onFragmentInteraction: " + uri.getPath());
    }
}
