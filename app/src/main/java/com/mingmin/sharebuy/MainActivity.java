package com.mingmin.sharebuy;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;

import java.util.Arrays;

public class MainActivity extends AppCompatActivity implements FirebaseAuth.AuthStateListener {

    private static final int RC_SIGN_IN = 1;
    private static final int RC_ADD_ITEM = 2;
    private FirebaseUser user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        firebaseAuth.addAuthStateListener(this);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, AddOrderActivity.class);
                intent.putExtra("USER_UID", user.getUid());
                startActivityForResult(intent, RC_ADD_ITEM);
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
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

    @Override
    public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
        user = firebaseAuth.getCurrentUser();
        if (user == null) {
            startActivityForResult(AuthUI.getInstance().createSignInIntentBuilder()
                    .setAvailableProviders(Arrays.asList(
                            new AuthUI.IdpConfig.EmailBuilder().build()
                    )).build(),
                    RC_SIGN_IN);
        } else {
            setupRecyclerView();
        }
    }

    private void setupRecyclerView() {
        RecyclerView recyclerView = findViewById(R.id.main_order_list);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        Query query = FirebaseDatabase.getInstance()
                .getReference("users")
                .child(user.getUid())
                .child("orders")
                .orderByChild("nStartTime")
                .limitToLast(30);

        final FirebaseRecyclerOptions<Order> options = new FirebaseRecyclerOptions.Builder<Order>()
                .setQuery(query, Order.class)
                .build();
        FirebaseRecyclerAdapter<Order, OrderHolder> adapter = new FirebaseRecyclerAdapter<Order, OrderHolder>(options) {
            @NonNull
            @Override
            public OrderHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(MainActivity.this)
                        .inflate(R.layout.row_order, parent, false);
                return new OrderHolder(view);
            }

            @Override
            protected void onBindViewHolder(@NonNull OrderHolder holder, int position, @NonNull final Order order) {
                holder.tvName.setText(order.getName());
                holder.tvPrice.setText(String.valueOf(order.getPrice()));
                holder.tvCount.setText(String.valueOf(order.getCount()));
                holder.calculateAmount();

                RequestOptions requestOptions = new RequestOptions()
                        .centerCrop()
                        .override(300, 300)
                        .placeholder(R.drawable.ic_downloading)
                        .error(R.drawable.ic_alert);
                Glide.with(MainActivity.this)
                        .load(order.getImageUrl())
                        .apply(requestOptions)
                        .into(holder.imageView);

                if (order.getEndTime() != 0) {
                    holder.btnEndOrder.setClickable(false);
                    holder.btnEndOrder.setText("");
                    holder.btnEndOrder.setHint("已結單");
                }

                holder.btnEndOrder.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        order.setEndTime(System.currentTimeMillis());
                    }
                });
            }
        };

        recyclerView.setAdapter(adapter);
        adapter.startListening();
    }
}
