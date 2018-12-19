package com.mingmin.sharebuy.fragment;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.jph.takephoto.app.TakePhotoFragment;
import com.jph.takephoto.model.CropOptions;
import com.jph.takephoto.model.TResult;
import com.mingmin.sharebuy.R;

import java.io.File;

public class SelectOrderImageFragment extends TakePhotoFragment {
    private OnFragmentInteractionListener mListener;
    private ImageView imageView;

    public SelectOrderImageFragment() {
        // Required empty public constructor
    }

    public static SelectOrderImageFragment newInstance() {
        return new SelectOrderImageFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_select_order_image, container, false);
        imageView = view.findViewById(R.id.select_order_image);
        Button btnCamera = view.findViewById(R.id.select_order_image_camera);
        btnCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                runCamera();
            }
        });
        Button btnPickImage = view.findViewById(R.id.select_order_image_pickImage);
        btnPickImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pickImage();
            }
        });
        return view;
    }

    public void onButtonPressed(String imagePath) {
        if (mListener != null) {
            mListener.onSelectImageCompleted(imagePath);
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

    public interface OnFragmentInteractionListener {
        void onSelectImageCompleted(String imagePath);
    }

    private void pickImage() {
        getTakePhoto().onPickFromGalleryWithCrop(createImageUri(), getCropOptions());
    }

    private void runCamera() {
        getTakePhoto().onPickFromCaptureWithCrop(createImageUri(), getCropOptions());
    }

    private Uri createImageUri() {
        String dirPath = getActivity().getApplicationInfo().dataDir + "/images";
        File file = new File(dirPath + "/" + System.currentTimeMillis() + ".jpg");
        if (!file.getParentFile().exists()) {
            file.getParentFile().mkdirs();
        }
        return Uri.fromFile(file);
    }

    private CropOptions getCropOptions() {
        return new CropOptions.Builder()
                .setAspectX(800)
                .setAspectY(800)
                .setWithOwnCrop(true)
                .create();
    }

    @Override
    public void takeSuccess(TResult result) {
        super.takeSuccess(result);
        String imagePath = result.getImage().getOriginalPath();
        mListener.onSelectImageCompleted(imagePath);
        Glide.with(this)
                .load(new File(imagePath))
                .into(imageView);
    }
}
