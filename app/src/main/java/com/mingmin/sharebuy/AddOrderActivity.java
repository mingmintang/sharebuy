package com.mingmin.sharebuy;

import android.Manifest;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;

import com.mingmin.sharebuy.cloud.GroupOrderDoc;
import com.mingmin.sharebuy.cloud.PersonalOrderDoc;
import com.mingmin.sharebuy.dialog.ConfirmDialog;
import com.mingmin.sharebuy.dialog.SelectGroupDialog;
import com.mingmin.sharebuy.fragment.EditOrderFragment;
import com.mingmin.sharebuy.fragment.SelectOrderImageFragment;
import com.mingmin.sharebuy.item.Group;
import com.mingmin.sharebuy.item.GroupOrderResult;
import com.mingmin.sharebuy.item.Order;
import com.mingmin.sharebuy.item.PersonalOrderResult;
import com.mingmin.sharebuy.item.User;
import com.tbruyelle.rxpermissions2.RxPermissions;

import java.util.Objects;

import io.reactivex.functions.Consumer;

public class AddOrderActivity extends AppCompatActivity implements
        SelectOrderImageFragment.OnFragmentInteractionListener,
        EditOrderFragment.OnFragmentInteractionListener, ViewPager.OnPageChangeListener, SelectGroupDialog.SelectGroupListener, ConfirmDialog.ConfirmListener {

    private ViewPager viewPager;
//    private Button btnAskFor;
    private Button btnHelpBuy;
    private Button btnEndOrder;
    private Button ibPrevious;
    private Button ibNext;
    private User user;
    private EditOrderFragment editOrderFragment;
    private String imagePath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_order);

        Objects.requireNonNull(getSupportActionBar()).setTitle(R.string.add_order);

        user = (User) getIntent().getSerializableExtra("user");
        initViews();

        checkPermissions();
    }

    private void checkPermissions() {
        final RxPermissions rxPermissions = new RxPermissions(this);
        rxPermissions.request(Manifest.permission.CAMERA,
                Manifest.permission.WRITE_EXTERNAL_STORAGE)
                .subscribe(new Consumer<Boolean>() {
                    @Override
                    public void accept(Boolean granted) throws Exception {
                        if (!granted) {
                            finishAfterTransition();
                        }
                    }
                });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent data = new Intent();
        data.putExtra("type", MainActivity.TYPE_BACK_PRESSED);
        setResult(RESULT_OK, data);
        finishAfterTransition();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        viewPager.removeOnPageChangeListener(this);
    }

    private void initViews() {
        viewPager = findViewById(R.id.add_order_viewPager);
        viewPager.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                // Disable switch page by swiping
                return true;
            }
        });
        viewPager.addOnPageChangeListener(this);
        viewPager.setAdapter(new AddOrderPagerAdapter(getSupportFragmentManager()));

//        btnAskFor = findViewById(R.id.add_order_earlyOrder);
//        btnAskFor.setEnabled(false);
        btnHelpBuy = findViewById(R.id.add_order_createOrder);
        btnEndOrder = findViewById(R.id.add_order_endOrder);
        btnEndOrder.setEnabled(false);
        ibPrevious = findViewById(R.id.add_order_previous);
        ibNext = findViewById(R.id.add_order_next);
        ibNext.setEnabled(false);

        switchVisibleByPage(0);
    }

    private void switchVisibleByPage(int pagePosition) {
        switch (pagePosition) {
            case 0:
//                btnAskFor.setVisibility(View.GONE);
                btnHelpBuy.setVisibility(View.GONE);
                btnEndOrder.setVisibility(View.GONE);
                ibPrevious.setVisibility(View.GONE);
                ibNext.setVisibility(View.VISIBLE);
                break;
            case 1:
//                btnAskFor.setVisibility(View.VISIBLE);
                btnHelpBuy.setVisibility(View.VISIBLE);
                btnEndOrder.setVisibility(View.VISIBLE);
                ibPrevious.setVisibility(View.VISIBLE);
                ibNext.setVisibility(View.GONE);
                break;
        }
    }

    @Override
    public void onSelectImageCompleted(String imagePath) {
        if (imagePath != null) {
            ibNext.setEnabled(true);
            this.imagePath = imagePath;
        }
    }

    @Override
    public void onCountEqualZero() {
//        if (btnAskFor.isEnabled()) {
//            btnAskFor.setEnabled(false);
//        }
        if (btnEndOrder.isEnabled()) {
            btnEndOrder.setEnabled(false);
        }
    }

    @Override
    public void onCountGreaterThanZero() {
//        if (!btnAskFor.isEnabled()) {
//            btnAskFor.setEnabled(true);
//        }
        if (!btnEndOrder.isEnabled()) {
            btnEndOrder.setEnabled(true);
        }
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) { }

    @Override
    public void onPageSelected(int position) {
        switchVisibleByPage(position);
        switch (position) {
            case 0:
                break;
            case 1:
                if (imagePath != null) {
                    editOrderFragment.setImagePath(imagePath);
                }
                break;
        }
    }

    @Override
    public void onPageScrollStateChanged(int state) { }

    @Override
    public void onSelectGroupConfirm(Group group, Object tag) {
        String action = (String) tag;
        switch (action) {
            case "askForHelpBuy":
                buildGroupOrder(Order.STATE_CREATE, group);
                break;
            case "helpBuy":
                buildGroupOrder(Order.STATE_TAKE, group);
                break;
        }
    }

    @Override
    public void onConfirm(Object tag) {
        String action = (String) tag;
        switch (action) {
            case "endOrder":
                buildPersonalOrder();
                break;
        }
    }

    private void buildGroupOrder(int orderState, Group group) {
        GroupOrderDoc groupOrderDoc = editOrderFragment.getGroupOrder();
        Intent data = new Intent();
        GroupOrderResult result = new GroupOrderResult(orderState, imagePath, user.getUid(), groupOrderDoc, group);
        data.putExtra("type", MainActivity.TYPE_GROUP_ORDER);
        data.putExtra("result", result);
        setResult(RESULT_OK, data);
        finishAfterTransition();
    }

    private void buildPersonalOrder() {
        PersonalOrderDoc personalOrder = editOrderFragment.getPersoanlOrder();
        Intent data = new Intent();
        PersonalOrderResult result = new PersonalOrderResult(personalOrder, imagePath, user.getUid());
        data.putExtra("type", MainActivity.TYPE_PERSONAL_ORDER);
        data.putExtra("result", result);
        setResult(RESULT_OK, data);
        finishAfterTransition();
    }

    class AddOrderPagerAdapter extends FragmentPagerAdapter {
        AddOrderPagerAdapter(FragmentManager fm) {
            super(fm);
        }
        @Override
        public Fragment getItem(int position) {
            switch (position) {
                case 0:
                    return SelectOrderImageFragment.newInstance();
                case 1:
                    editOrderFragment = EditOrderFragment.newInstance(user.getUid());
                    return editOrderFragment;
            }
            return null;
        }
        @Override
        public int getCount() {
            return 2;
        }
    }

    public void askForHelpBuy(View view) {
        SelectGroupDialog.newInstance("選擇群組找人幫買", user.getUid(), this, "askForHelpBuy")
                .show(getSupportFragmentManager(), "askForHelpBuy");
    }

    public void helpBuy(View view) {
        SelectGroupDialog.newInstance("選擇群組幫買東西", user.getUid(), this, "helpBuy")
                .show(getSupportFragmentManager(), "helpBuy");
    }

    public void endOrder(View view) {
        ConfirmDialog.newInstance(this, "直接結單", "確定結束訂單？", "endOrder")
                .show(getSupportFragmentManager(), "endOrder");
    }

    public void previousPage(View view) {
        viewPager.setCurrentItem(0);
    }

    public void nextPage(View view) {
        viewPager.setCurrentItem(1);
    }
}
