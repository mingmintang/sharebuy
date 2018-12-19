package com.mingmin.sharebuy.cloud;

import android.util.Log;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.mingmin.sharebuy.Order;
import com.mingmin.sharebuy.UserEndOrderRecyclerAdapter;

import java.util.HashMap;

import javax.annotation.Nullable;

public class UserEndOrderCloud {
    private final String TAG = getClass().getSimpleName();
    private Firestore fs;
    private HashMap<String, ListenerRegistration> registrations;

    public UserEndOrderCloud() {
        fs = Firestore.getInstance();
        registrations = new HashMap<>();
    }

    public interface UserEndOrderListener {
        void onUserEndOrderChanged(Order order, UserEndOrderRecyclerAdapter.OrderHolder holder);
    }

    public void addUserEndOrderListener(String uid, Order order, UserEndOrderRecyclerAdapter.OrderHolder holder) {
        if (registrations.containsKey(order.getId())) {
            return;
        }
        if (order.getGroupId() != null) {
            listenGroupOrder(order, holder);
        } else {
            listenPersonalOrder(uid, order, holder);
        }
    }

    private void listenGroupOrder(final Order endOrder, final UserEndOrderRecyclerAdapter.OrderHolder holder) {
        ListenerRegistration registration = fs.getGroupOrderDoc(endOrder.getGroupId(), endOrder.getId())
                .addSnapshotListener(new EventListener<DocumentSnapshot>() {
                    @Override
                    public void onEvent(@Nullable DocumentSnapshot documentSnapshot,
                                        @Nullable FirebaseFirestoreException e) {
                        if (e != null) {
                            Log.w(TAG, "Group end order listen failed.", e);
                            return;
                        }
                        if (documentSnapshot != null && documentSnapshot.exists()) {
                            GroupOrderDoc groupOrderDoc = documentSnapshot.toObject(GroupOrderDoc.class);
                            Order order = new Order(endOrder.getId(), groupOrderDoc);
                            holder.onUserEndOrderChanged(order, holder);
                        } else {
                            Log.d(TAG, "Group end order is empty.");
                        }
                    }
                });
        registrations.put(endOrder.getId(), registration);
    }

    private void listenPersonalOrder(String uid, final Order endOrder, final UserEndOrderRecyclerAdapter.OrderHolder holder) {
        ListenerRegistration registration = fs.getUserPersonalOrderDoc(uid, endOrder.getId())
                .addSnapshotListener(new EventListener<DocumentSnapshot>() {
                    @Override
                    public void onEvent(@Nullable DocumentSnapshot documentSnapshot,
                                        @Nullable FirebaseFirestoreException e) {
                        if (e != null) {
                            Log.w(TAG, "Personal end order listen failed.", e);
                            return;
                        }
                        if (documentSnapshot != null && documentSnapshot.exists()) {
                            PersonalOrderDoc personalOrderDoc = documentSnapshot.toObject(PersonalOrderDoc.class);
                            Order order = new Order(endOrder.getId(), endOrder.getCreateTime(), endOrder.getUpdateTime(), personalOrderDoc);
                            holder.onUserEndOrderChanged(order, holder);
                        }
                    }
                });
        registrations.put(endOrder.getId(), registration);
    }

    public void removeUserEndOrderListener(String orderId) {
        ListenerRegistration reg = registrations.get(orderId);
        if (reg != null) {
            reg.remove();
            registrations.remove(orderId);
        }
    }

    public void removeAllListener() {
        if (!registrations.isEmpty()) {
            for (ListenerRegistration reg : registrations.values()) {
                reg.remove();
            }
            registrations.clear();
        }
    }
}
