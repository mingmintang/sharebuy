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
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Arrays;

public class MainActivity extends AppCompatActivity implements FirebaseAuth.AuthStateListener,
        NavigationView.OnNavigationItemSelectedListener,
        OrderListFragment.OnFragmentInteractionListener,
        GroupFragment.OnFragmentInteractionListener {

    public static final int RC_SIGN_IN = 1;
    public static final int RC_ADD_ITEM = 2;
    public static final int RC_EDIT_PROFILE = 3;
    public static final int RC_GROUP_MANAGE = 4;
    public static final int RC_GROUP_INFO = 5;
    private final String TAG = getClass().getSimpleName();
    private FirebaseUser fuser;
    private DrawerLayout drawer;
    private TextView tvNickname;
    private TextView tvAccount;
    private NavigationView navigationView;
    private FirebaseAuth firebaseAuth;
    private User user;
    private FragmentManager fm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
    }

    @Override
    protected void onStart() {
        super.onStart();
        firebaseAuth = FirebaseAuth.getInstance();
        firebaseAuth.addAuthStateListener(this);
        fm = getSupportFragmentManager();
        switchNavItemByTag();
    }

    /**
     * Switch navigation menu item by tag of NavigationView.
     * Tag will be set null after switching.
     * @return true if tag is not null, false if tag is null
     */
    private boolean switchNavItemByTag() {
        if (navigationView.getTag() != null) {
            final int itemId = (int) navigationView.getTag();
            navigationView.post(new Runnable() {
                @Override
                public void run() {
                    navigationView.getMenu().performIdentifierAction(itemId, 0);
                }
            });
            return true;
        }
        return false;
    }

    @Override
    protected void onStop() {
        super.onStop();
        firebaseAuth.removeAuthStateListener(this);
    }

    private void initView() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, AddOrderActivity.class);
                intent.putExtra("USER_UID", fuser.getUid());
                startActivityForResult(intent, RC_ADD_ITEM);
            }
        });

        drawer = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toogle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toogle);
        toogle.syncState();

        navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        tvNickname = navigationView.getHeaderView(0).findViewById(R.id.main_nav_userNickname);
        tvAccount = navigationView.getHeaderView(0).findViewById(R.id.main_nav_userAccount);
    }

    @Override
    public void onBackPressed() {
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            if (!switchNavItemByTag()) {
                finish();
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
        if (id == R.id.menu_editProfile) {
            Intent intent = new Intent(this, EditProfileActivity.class);
            startActivityForResult(intent, RC_EDIT_PROFILE);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        navigationView.setTag(null);
        int id = item.getItemId();
        if (id == getCurrentNavigationItem()) {
            drawer.closeDrawer(GravityCompat.START);
            return false;
        }
        switch (id) {
            case R.id.nav_order:
                fm.beginTransaction()
                        .replace(R.id.frame_layout, OrderListFragment.getInstance(user))
                        .commit();
                break;
            case R.id.nav_order_closed:
                break;
            case R.id.nav_group:
                navigationView.setTag(R.id.nav_order);
                fm.beginTransaction()
                        .replace(R.id.frame_layout, GroupFragment.getInstance(user))
                        .addToBackStack(null)
                        .commit();
                break;
        }
        navigationView.setCheckedItem(id);
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
            SharebuyFirebaseInstanceIdService.checkRegistrationUpdated();
        }
    }

    private void initAfterSignIn() {
        updateNickname();
        tvAccount.setText(fuser.getEmail());

        user = new User(fuser.getUid(), tvNickname.getText().toString());
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.frame_layout, OrderListFragment.getInstance(user))
                .commit();
        navigationView.setCheckedItem(R.id.nav_order);
    }

    private void updateNickname() {
        final DatabaseReference ref = FirebaseDatabase.getInstance()
                .getReference("users")
                .child(fuser.getUid())
                .child("data")
                .child("nickname");
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String nickname = (String) dataSnapshot.getValue();
                if (nickname != null) {
                    tvNickname.setText(nickname);
                } else {
                    String tempName = fuser.getEmail().split("@")[0];
                    ref.setValue(tempName);
                    tvNickname.setText(tempName);
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case RC_SIGN_IN:
                if (resultCode == RESULT_OK) {
                    initAfterSignIn();
                    Intent intent = new Intent(this, EditProfileActivity.class);
                    startActivityForResult(intent, RC_EDIT_PROFILE);
                }
                break;
            case RC_EDIT_PROFILE:
                if (resultCode == RESULT_OK) {
                    updateNickname();
                    Snackbar.make(tvNickname, "修改成功", Snackbar.LENGTH_LONG).show();
                }
                break;
            case RC_GROUP_MANAGE:
                if (resultCode == RESULT_OK) {
                    navigationView.setTag(R.id.nav_group);
                }
                break;
            case RC_GROUP_INFO:
                if (resultCode == RESULT_OK) {
                    navigationView.setTag(R.id.nav_group);
                }
                break;
        }
    }

    @Override
    public void onFragmentInteraction(Uri uri) {

    }
}
