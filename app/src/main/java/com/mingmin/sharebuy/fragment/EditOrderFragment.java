package com.mingmin.sharebuy.fragment;

import android.content.Context;
import android.os.Bundle;
import android.support.design.widget.TextInputEditText;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.mingmin.sharebuy.R;
import com.mingmin.sharebuy.cloud.GroupOrderDoc;
import com.mingmin.sharebuy.cloud.UserEndOrderDoc;

import java.io.File;

public class EditOrderFragment extends Fragment {
    private String uid;

    private OnFragmentInteractionListener mListener;
    private ImageView imageView;
    private TextInputEditText etName;
    private TextInputEditText etDesc;
    private TextInputEditText etPrice;
    private Spinner coinUnitSpinner;
    private TextInputEditText etCount;
    private TextInputEditText etMaxBuyCount;
    private TextView tvAmount;
    private TextWatcher priceNumberTextWatcher;
    private TextWatcher countNumberTextWatcher;
    private TextWatcher maxBuyCountTextWatcher;

    public EditOrderFragment() {
        // Required empty public constructor
    }

    public static EditOrderFragment newInstance(String uid) {
        EditOrderFragment fragment = new EditOrderFragment();
        fragment.uid = uid;
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_edit_order, container, false);
        imageView = view.findViewById(R.id.edit_order_image);
        etName = view.findViewById(R.id.edit_order_name);
        etDesc = view.findViewById(R.id.edit_order_desc);
        etPrice = view.findViewById(R.id.edit_order_price);
        coinUnitSpinner = view.findViewById(R.id.edit_order_coin_unit);
        etCount = view.findViewById(R.id.edit_order_count);
        etMaxBuyCount = view.findViewById(R.id.edit_order_maxCount);
        tvAmount = view.findViewById(R.id.edit_order_amount);

        priceNumberTextWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                calculateAmount();
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        };
        etPrice.addTextChangedListener(priceNumberTextWatcher);

        countNumberTextWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                calculateAmount();
                if (s.toString().equals("") || s.toString().equals("0")) {
                    mListener.onCountEqualZero();
                } else {
                    mListener.onCountGreaterThanZero();
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        };
        etCount.addTextChangedListener(countNumberTextWatcher);

        coinUnitSpinner.setAdapter(new ArrayAdapter<String>(getContext(),
                android.R.layout.simple_list_item_1,
                getResources().getStringArray(R.array.coin_units)));
        return view;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        etPrice.removeTextChangedListener(priceNumberTextWatcher);
        etCount.removeTextChangedListener(countNumberTextWatcher);
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

    private void calculateAmount() {
        if (!TextUtils.isEmpty(etPrice.getText().toString()) &&
                !TextUtils.isEmpty(etCount.getText().toString())) {
            int price = Integer.parseInt(etPrice.getText().toString());
            int count = Integer.parseInt(etCount.getText().toString());
            tvAmount.setText(String.valueOf(price * count));
        }
    }

    public interface OnFragmentInteractionListener {
        void onCountEqualZero();
        void onCountGreaterThanZero();
    }

    public void setImagePath(String imagePath) {
        Glide.with(this)
                .load(new File(imagePath))
                .into(imageView);
    }

    class EditedOrder {
        String name;
        String desc;
        int price;
        int coinUnit;
        int buyCount;
        int maxBuyCount;

        public EditedOrder() {
            name = etName.getText().toString();
            desc = etDesc.getText().toString();
            String priceStr = etPrice.getText().toString();
            if (!TextUtils.isEmpty(priceStr)) {
                price = Integer.parseInt(priceStr);
            }
            coinUnit = coinUnitSpinner.getSelectedItemPosition();
            String countStr = etCount.getText().toString();
            if (!TextUtils.isEmpty(countStr)) {
                buyCount = Integer.parseInt(countStr);
            }
            String maxBuyCountStr = etMaxBuyCount.getText().toString();
            if (!TextUtils.isEmpty(maxBuyCountStr)) {
                maxBuyCount = Integer.parseInt(maxBuyCountStr);
            }
        }
    }

    public GroupOrderDoc getGroupOrder() {
        EditedOrder editedOrder = new EditedOrder();
        GroupOrderDoc groupOrderDoc = new GroupOrderDoc();
        groupOrderDoc.setName(editedOrder.name);
        groupOrderDoc.setDesc(editedOrder.desc);
        groupOrderDoc.setPrice(editedOrder.price);
        groupOrderDoc.setCoinUnit(editedOrder.coinUnit);
        groupOrderDoc.setBuyCount(editedOrder.buyCount);
        groupOrderDoc.setMaxBuyCount(editedOrder.maxBuyCount);
        return groupOrderDoc;
    }

    public UserEndOrderDoc.Personal getPersoanlOrder() {
        EditedOrder editedOrder = new EditedOrder();
        UserEndOrderDoc.Personal personalOrder = new UserEndOrderDoc.Personal(editedOrder.buyCount,
                editedOrder.name, editedOrder.desc, editedOrder.price, editedOrder.coinUnit);
        return personalOrder;
    }
}
