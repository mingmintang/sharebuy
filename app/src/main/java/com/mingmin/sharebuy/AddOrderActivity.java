package com.mingmin.sharebuy;

import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.jph.takephoto.app.TakePhotoActivity;
import com.jph.takephoto.model.CropOptions;
import com.jph.takephoto.model.TResult;

import java.io.File;

public class AddOrderActivity extends TakePhotoActivity {

    private ImageView imageView;
    private EditText etName;
    private EditText etPrice;
    private EditText etCount;

    private Order order;
    private TextView tvAmount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_order);

        String uid = getIntent().getStringExtra("USER_UID");
        order = new Order(uid);

        initViews();
    }

    private void initViews() {
        imageView = findViewById(R.id.add_order_image);
        etName = findViewById(R.id.add_order_name);
        etPrice = findViewById(R.id.add_order_price);
        etCount = findViewById(R.id.add_order_count);
        tvAmount = findViewById(R.id.add_order_amount);

        etPrice.setText("0");
        etPrice.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() == 0) {
                    etPrice.setText("0");
                }
                calculateAmount();
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        etCount.setText("1");
        etCount.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() == 0) {
                    etCount.setText("0");
                }
                calculateAmount();
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        tvAmount.setText("0");
    }

    private void calculateAmount() {
        int price = Integer.parseInt(etPrice.getText().toString());
        int count = Integer.parseInt(etCount.getText().toString());
        int amount = price * count;
        tvAmount.setText(String.valueOf(amount));
    }

    public void pickImage(View view) {
        String dirPath = getApplicationInfo().dataDir + "/images";
        File file = new File(dirPath + "/" + System.currentTimeMillis() + ".jpg");
        if (!file.getParentFile().exists()) {
            file.getParentFile().mkdirs();
        }
        Uri uri = Uri.fromFile(file);
        CropOptions options = new CropOptions.Builder()
                .setAspectX(800)
                .setAspectY(800)
                .setWithOwnCrop(true)
                .create();
        getTakePhoto().onPickFromGalleryWithCrop(uri, options);
    }

    @Override
    public void takeSuccess(TResult result) {
        super.takeSuccess(result);
        order.setImagePath(result.getImage().getOriginalPath());
        Glide.with(this)
                .load(new File(order.getImagePath()))
                .into(imageView);
    }

    private void updateFirebaseData(boolean isEndOrder) {
        order.setName(etName.getText().toString());
        order.setPrice(Integer.parseInt(etPrice.getText().toString()));
        order.setCount(Integer.parseInt(etCount.getText().toString()));
        order.setStartTime(System.currentTimeMillis());
        if (isEndOrder) {
            order.setEndTime(System.currentTimeMillis());
        }
        String imagePath = order.getImagePath();
        if (imagePath == null) {
            Snackbar.make(imageView, "圖片不能空白", Snackbar.LENGTH_LONG).show();
            return;
        }
        String fileName = imagePath.substring(imagePath.lastIndexOf("/"));
        StorageReference storageReference = FirebaseStorage.getInstance()
                .getReference(fileName);
        storageReference.putFile(Uri.fromFile(new File(imagePath)))
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        String imageUrl = taskSnapshot.getDownloadUrl().toString();
                        order.setImageUrl(imageUrl);

                        FirebaseDatabase.getInstance()
                                .getReference("users")
                                .child(order.getUid())
                                .child("orders")
                                .child(order.getId() + "")
                                .setValue(order);

                        setResult(RESULT_OK);
                        finish();
                    }
                });
    }

    public void createOrder(View view) {
        updateFirebaseData(false);
    }

    public void endOrder(View view) {
        updateFirebaseData(true);
    }
}
