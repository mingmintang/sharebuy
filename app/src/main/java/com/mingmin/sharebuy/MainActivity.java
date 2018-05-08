package com.mingmin.sharebuy;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.firebase.ui.auth.AuthUI;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.Arrays;

public class MainActivity extends AppCompatActivity implements FirebaseAuth.AuthStateListener,
        NavigationView.OnNavigationItemSelectedListener,
        OrderListFragment.OnFragmentInteractionListener,
        GroupFragment.OnFragmentInteractionListener {

    private static final int RC_SIGN_IN = 1;
    private static final int RC_ADD_ITEM = 2;
    private final String TAG = getClass().getSimpleName();
    private FirebaseUser user;
    private DrawerLayout drawer;
    private TextView tvName;
    private TextView tvAccount;
    private NavigationView navigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        firebaseAuth.addAuthStateListener(this);

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
                intent.putExtra("USER_UID", user.getUid());
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

        tvName = navigationView.getHeaderView(0).findViewById(R.id.main_nav_userName);
        tvAccount = navigationView.getHeaderView(0).findViewById(R.id.main_nav_userAccount);
    }

    @Override
    public void onBackPressed() {
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
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

        switch (id) {
            case R.id.nav_order:
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.frame_layout, OrderListFragment.getInstance(user))
                        .commit();
                break;
            case R.id.nav_order_closed:
                break;
            case R.id.nav_group:
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.frame_layout, GroupFragment.getInstance(user))
                        .commit();
                break;
        }

        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
        user = firebaseAuth.getCurrentUser();
        if (user == null) {
            startActivityForResult(AuthUI.getInstance().createSignInIntentBuilder()
                    .setAvailableProviders(Arrays.asList(
                            new AuthUI.IdpConfig.GoogleBuilder().build()
                    )).build(),
                    RC_SIGN_IN);
        } else {
            tvName.setText(user.getDisplayName());
            tvAccount.setText(user.getEmail());

            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.frame_layout, OrderListFragment.getInstance(user))
                    .commit();
            navigationView.setCheckedItem(R.id.nav_order);
        }
    }

    @Override
    public void onFragmentInteraction(Uri uri) {

    }
}
