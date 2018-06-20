package com.mingmin.sharebuy;

import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.mingmin.sharebuy.cloud.Fdb;
import com.mingmin.sharebuy.dialog.ConfirmDialog;
import com.mingmin.sharebuy.dialog.SelectGroupDialog;
import com.mingmin.sharebuy.fragment.EditOrderFragment;
import com.mingmin.sharebuy.fragment.SelectOrderImageFragment;

import java.io.File;

public class AddOrderActivity extends AppCompatActivity implements
        SelectOrderImageFragment.OnFragmentInteractionListener,
        EditOrderFragment.OnFragmentInteractionListener, ViewPager.OnPageChangeListener, SelectGroupDialog.OnSelectGroupListener, ConfirmDialog.OnConfirmListener {

    private ViewPager viewPager;
    private Button btnAskFor;
    private Button btnHelpBuy;
    private Button btnEndOrder;
    private Button ibPrevious;
    private Button ibNext;
    private String uid;
    private EditOrderFragment editOrderFragment;
    private String imagePath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_order);

        uid = getIntent().getStringExtra("USER_UID");

        initViews();
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

        btnAskFor = findViewById(R.id.add_order_earlyOrder);
        btnAskFor.setEnabled(false);
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
                btnAskFor.setVisibility(View.GONE);
                btnHelpBuy.setVisibility(View.GONE);
                btnEndOrder.setVisibility(View.GONE);
                ibPrevious.setVisibility(View.GONE);
                ibNext.setVisibility(View.VISIBLE);
                break;
            case 1:
                btnAskFor.setVisibility(View.VISIBLE);
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
    public void onEditOrderCompleted(Order order) {

    }

    @Override
    public void onCountEqualZero() {
        if (btnAskFor.isEnabled()) {
            btnAskFor.setEnabled(false);
        }
        if (btnEndOrder.isEnabled()) {
            btnEndOrder.setEnabled(false);
        }
    }

    @Override
    public void onCountGreaterThanZero() {
        if (!btnAskFor.isEnabled()) {
            btnAskFor.setEnabled(true);
        }
        if (!btnEndOrder.isEnabled()) {
            btnEndOrder.setEnabled(true);
        }
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

    }

    @Override
    public void onPageSelected(int position) {
        switchVisibleByPage(position);
        switch (position) {
            case 0:
                Log.d("wwwww", "onPageSelected: 0");
                break;
            case 1:
                Log.d("wwwww", "onPageSelected: 1");
                editOrderFragment.setImagePath(imagePath);
                break;
        }
    }

    @Override
    public void onPageScrollStateChanged(int state) {

    }

    @Override
    public void onSelectGroupConfirm(Group group, Object tag) {
        String action = (String) tag;
        switch (action) {
            case "askForHelpBuy":
//                buildCreateOrder(group);
                break;
            case "helpBuy":
                buildTakeOrder(group);
                break;
        }
    }

    @Override
    public void onConfirm(Object tag) {
        String action = (String) tag;
        switch (action) {
            case "endOrder":
                buildEndOrder();
                break;
        }
    }

    private void buildCreateOrder(Group group) {
        final Order order = editOrderFragment.getOrder();
        order.setCreatorUid(uid);
        order.setGroupId(group.getId());
        order.setState(Order.STATE_CREATE);

        String imagePath = order.getImagePath();
        String fileName = imagePath.substring(imagePath.lastIndexOf("/"));
        StorageReference storageReference = FirebaseStorage.getInstance()
                .getReference(fileName);
        storageReference.putFile(Uri.fromFile(new File(imagePath)))
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        String imageUrl = taskSnapshot.getDownloadUrl().toString();
                        order.setImageUrl(imageUrl);

                        DatabaseReference orderRef = Fdb.getGroupOrdersRef(order.getGroupId());
                        String orderId = orderRef.push().getKey();
                        order.setId(orderId);
                        orderRef.child(orderId).setValue(order)
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                setResult(RESULT_OK);
                                finish();
                            }
                        });
                    }
                });
    }

    private void buildTakeOrder(Group group) {
        final Order order = editOrderFragment.getOrder();
        order.setCreatorUid(uid);
        order.setTakerUid(uid);
        order.setGroupId(group.getId());
        order.setState(Order.STATE_TAKE);

        String imagePath = order.getImagePath();
        String fileName = imagePath.substring(imagePath.lastIndexOf("/"));
        StorageReference storageReference = FirebaseStorage.getInstance()
                .getReference(fileName);
        storageReference.putFile(Uri.fromFile(new File(imagePath)))
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        String imageUrl = taskSnapshot.getDownloadUrl().toString();
                        order.setImageUrl(imageUrl);

                        DatabaseReference orderRef = Fdb.getGroupOrdersRef(order.getGroupId());
                        String orderId = orderRef.push().getKey();
                        order.setId(orderId);
                        orderRef.child(orderId).setValue(order)
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        setResult(RESULT_OK);
                                        finish();
                                    }
                                });
                    }
                });
    }

    private void buildEndOrder() {
        final Order order = editOrderFragment.getOrder();
        order.setCreatorUid(uid);
        order.setTakerUid(uid);
        order.setEndTime(System.currentTimeMillis());
        order.setState(Order.STATE_END);

        String imagePath = order.getImagePath();
        String fileName = imagePath.substring(imagePath.lastIndexOf("/"));
        StorageReference storageReference = FirebaseStorage.getInstance()
                .getReference(fileName);
        storageReference.putFile(Uri.fromFile(new File(imagePath)))
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        String imageUrl = taskSnapshot.getDownloadUrl().toString();
                        order.setImageUrl(imageUrl);

                        DatabaseReference orderRef = Fdb.getUserOrdersRef(order.getTakerUid());
                        String orderId = orderRef.push().getKey();
                        order.setId(orderId);
                        orderRef.child(orderId).setValue(order)
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        setResult(RESULT_OK);
                                        finish();
                                    }
                                });
                    }
                });
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
                    editOrderFragment = EditOrderFragment.newInstance(uid);
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
        SelectGroupDialog.newInstance("選擇群組找人幫買", uid, this, "askForHelpBuy")
                .show(getSupportFragmentManager(), "askForHelpBuy");
    }

    public void helpBuy(View view) {
        SelectGroupDialog.newInstance("選擇群組幫買東西", uid, this, "helpBuy")
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

    private void updateFirebaseData(boolean isEndOrder) {
//        order.setName(etName.getText().toString());
//        order.setPrice(Integer.parseInt(etPrice.getText().toString()));
//        order.setCount(Integer.parseInt(etCount.getText().toString()));
//        order.setStartTime(System.currentTimeMillis());
//        if (isEndOrder) {
//            order.setEndTime(System.currentTimeMillis());
//        }
//        String imagePath = order.getImagePath();
//        if (imagePath == null) {
//            Snackbar.make(imageView, "圖片不能空白", Snackbar.LENGTH_LONG).show();
//            return;
//        }
//        String fileName = imagePath.substring(imagePath.lastIndexOf("/"));
//        StorageReference storageReference = FirebaseStorage.getInstance()
//                .getReference(fileName);
//        storageReference.putFile(Uri.fromFile(new File(imagePath)))
//                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
//                    @Override
//                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
//                        String imageUrl = taskSnapshot.getDownloadUrl().toString();
//                        order.setImageUrl(imageUrl);
//
//                        FirebaseDatabase.getInstance()
//                                .getReference("users")
//                                .child(order.getUid())
//                                .child("orders")
//                                .child(order.getId() + "")
//                                .setValue(order);
//
//                        setResult(RESULT_OK);
//                        finish();
//                    }
//                });
    }
}
